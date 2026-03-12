package ru.sg.paymentservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.paymentservice.entity.OutboxEvent;
import ru.sg.paymentservice.repository.OutboxEventRepository;
import ru.sg.paymentservice.service.DLQService;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DLQService dlqService;

    @Value("${payment.event.outbox.batch-size}")
    private int batchSize;

    @Value("${payment.event.outbox.max-retries}")
    private int maxRetries;

    @Scheduled(fixedDelay = 5000, initialDelay = 2000)
    @Transactional
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findBatchForPublish(batchSize);

        if (events.isEmpty()) return;

        log.debug("Found {} new outbox events to publish", events.size());

        for (OutboxEvent event : events) {
            try {
                publishToKafka(event);
                outboxEventRepository.markAsSent(event.getId());
            } catch (Exception e) {
                log.warn("Failed to publish event {}: {}", event.getId(), e.getMessage());
                long delaySeconds = 30L * (long) Math.pow(2, event.getRetryCount());
                outboxEventRepository.markAsError(
                        event.getId(),
                        Instant.now().plusSeconds(delaySeconds),
                        truncate(e.getMessage())
                );
            }
        }
    }

    private void publishToKafka(OutboxEvent event) {
        Message<String> message = MessageBuilder
                .withPayload(event.getPayload())
                .setHeader(KafkaHeaders.TOPIC, event.getTopic())
                .setHeader(KafkaHeaders.KEY, event.getPartitionKey())
                .setHeader("event-id", event.getId().toString())
                .setHeader("event-type", event.getEventType().getValue())
                .setHeader("saga-id", "order-saga")
                .build();

        kafkaTemplate.send(message);
    }

    private String truncate(String message) {
        if (message == null || message.isEmpty()) return null;
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
