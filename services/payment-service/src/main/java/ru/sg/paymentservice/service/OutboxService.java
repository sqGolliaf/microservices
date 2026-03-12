package ru.sg.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sg.paymentservice.dto.event.PaymentEvent;
import ru.sg.paymentservice.entity.OutboxEvent;
import ru.sg.paymentservice.entity.Payment;
import ru.sg.paymentservice.enums.OutboxStatus;
import ru.sg.paymentservice.enums.OutboxType;
import ru.sg.paymentservice.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;


    @Value("${payment.event.topic.name:payment-events}")
    private String paymentTopic;


    public void createOutboxEvent(Payment savePayment, OutboxType outboxType) {
        try {
            String eventId = UUID.randomUUID().toString();

            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .eventId(eventId)
                    .orderId(savePayment.getId())
                    .sagaId(savePayment.getSagaId())
                    .eventType(outboxType.getValue())
                    .orderStatus(savePayment.getStatus().getValue())
                    .amount(savePayment.getAmount())
                    .timestamp(Instant.now())
                    .source("payment-service")
                    .build();

            String payload = objectMapper.writeValueAsString(paymentEvent);

            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .paymentId(savePayment.getId())
                    .eventType(outboxType)
                    .status(OutboxStatus.CREATED)
                    .payload(payload)
                    .topic(paymentTopic)
                    .partitionKey(savePayment.getId().toString())
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(event);

            log.info("OutboxEvent created: id={}, orderId={}, type={}, sagaId={}",
                    event.getId(), savePayment.getId(), outboxType, savePayment.getSagaId());
        } catch (Exception e) {
            log.error("Failed to create PaymentEvent for payment {}", savePayment.getId(), e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
