package ru.sg.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.sg.paymentservice.entity.DLQEvent;
import ru.sg.paymentservice.repository.DLQEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
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

            DLQEvent event = DLQEvent.builder()
                    .eventId(eventId)
                    .originalTopic(originalTopic)
                    .originalMessage(originalMessage)
                    .errorMessage(error.getMessage())
                    .errorTrace(getStackTrace(error))
                    .retryCount(0)
                    .isResolved(false)
                    .build();

            String dlqMessage = objectMapper.writeValueAsString(event);

            Message<String> message = MessageBuilder
                    .withPayload(dlqMessage)
                    .setHeader(KafkaHeaders.TOPIC, "payment-events-dlq")
                    .setHeader("event-id", eventId)
                    .setHeader("original-topic", originalTopic)
                    .build();

            kafkaTemplate.send(message);
            dlqEventRepository.save(event);

            log.info("Event sent to DLQ: eventId={}, originalTopic={}", eventId, originalTopic);
        } catch (Exception e) {
            log.error("Failed to send event to DLQ", e);
        }
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
