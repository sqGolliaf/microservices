package ru.sg.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.order.entity.DLQEvent;
import ru.sg.order.repository.DLQEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DLQService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DLQEventRepository dlqEventRepository;
    private final ObjectMapper objectMapper;

    public void sendToDLQ(String originalMessage, String originalTopic, Exception error) {
        try {
            String eventId = UUID.randomUUID().toString();

            DLQEvent dlqEvent = DLQEvent.builder()
                    .eventId(eventId)
                    .originalTopic(originalTopic)
                    .originalMessage(originalMessage)
                    .errorMessage(error.getMessage())
                    .errorTrace(getStackTrace(error))
                    .retryCount(0)
                    .isResolved(false)
                    .createdAt(Instant.now())
                    .build();

            String dlqMessage = objectMapper.writeValueAsString(dlqEvent);

            Message<String> message = MessageBuilder
                    .withPayload(dlqMessage)
                    .setHeader(KafkaHeaders.TOPIC, "order-events-dlq")
                    .setHeader("event-id", eventId)
                    .setHeader("original-topic", originalTopic)
                    .build();

            kafkaTemplate.send(message).whenComplete((res, ex) -> {
                if (ex == null) {
                    log.info("Event sent to DLQ: eventId={}, originalTopic={}", eventId, originalTopic);
                    dlqEventRepository.save(dlqEvent);
                } else {
                    log.error("Failed to send event to DLQ", ex);
                }
            });

        } catch (Exception e) {
            log.error("Failed to send event to DLQ", e);
        }
    }

    @Transactional
    public void retryDLQEvent(String eventId) {
        DLQEvent entity = dlqEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("DLQ event not found: " + eventId));

        try {
            log.info("Retrying DLQ event: {}", eventId);

            Message<String> message = MessageBuilder
                    .withPayload(entity.getOriginalMessage())
                    .setHeader(KafkaHeaders.TOPIC, entity.getOriginalTopic())
                    .setHeader("retry-from-dlq", "true")
                    .build();

            kafkaTemplate.send(message);

            entity.setRetryCount(entity.getRetryCount() + 1);
            entity.setLastRetryAt(Instant.now());
            dlqEventRepository.save(entity);

            log.info("DLQ event {} retried successfully", eventId);
        } catch (Exception e) {
            log.error("Failed to retry DLQ event {}", eventId, e);
            throw new RuntimeException("Failed to retry DLQ event", e);
        }
    }

    @Transactional
    public void resolveDLQEvent(String eventId) {
        DLQEvent entity = dlqEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("DLQ event not found: " + eventId));

        entity.setIsResolved(true);
        entity.setResolvedAt(Instant.now());
        dlqEventRepository.save(entity);

        log.info("DLQ event {} marked as resolved", eventId);
    }

    @Transactional
    public void cleanupResolvedEvents(Instant olderThan) {
        List<DLQEvent> events = dlqEventRepository.findByCreatedAtBeforeAndIsResolvedTrue(olderThan);
        if (events.isEmpty()) {
            log.debug("No resolved DLQ events found older than {}", olderThan);
            return;
        }

        dlqEventRepository.deleteAll(events);
        log.info("Cleaned up {} resolved DLQ events", events.size());
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
