package ru.sg.paymentservice.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.paymentservice.client.UserServiceClient;
import ru.sg.paymentservice.dto.event.OrderEvent;
import ru.sg.paymentservice.dto.request.CreditRequest;
import ru.sg.paymentservice.dto.request.DebitRequest;
import ru.sg.paymentservice.enums.OutboxType;
import ru.sg.paymentservice.service.DLQService;
import ru.sg.paymentservice.service.PaymentService;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final UserServiceClient userServiceClient;
    private final DLQService dlqService;

    @KafkaListener(
            topics = "order-events",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "saga-id", required = false) String sagaId,
            @Header(value = "event-type", required = false) String eventType) {
        try {
            log.info("Received inventory event: sagaId={}, eventType={}", sagaId, eventType);

            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);

            OutboxType type = OutboxType.fromValue(eventType);

            switch (type) {
                case ORDER_CREATED -> paymentService.confirmPayment(event);
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage(), e);
            dlqService.sendToDLQ(message, topic, e);
            throw new RuntimeException("Event processing failed", e);
        }
    }

    private void handleOrderCreated(OrderEvent event, String keycloakId) {
        try {
            log.info("Order created event: orderId={}, price={}, keycloakId={}",
                    event.orderId(), event.price(), keycloakId);

            userServiceClient.debit(keycloakId, new DebitRequest(event.price()));

            log.info("Debit successful: keycloakId={}, amount={}", keycloakId, event.price());

            paymentService.confirmPayment(event);
        } catch (Exception e) {
            log.error("Payment failed for order {}: {}", event.orderId(), e.getMessage(), e);

            try {
                userServiceClient.credit(keycloakId, new CreditRequest(event.price()));
                log.info("Compensation successful: credited {} back to {}", event.price(), keycloakId);
            } catch (Exception compensationError) {
                log.error("Compensation failed! Manual intervention required", compensationError);
                dlqService.sendToDLQ(objectMapper.writeValueAsString(event), "order-events", compensationError);
            }

            throw new RuntimeException("Payment processing failed", e);
        }
    }

    private void handleOrderCanceled(OrderEvent event, String keycloakId) {
        try {
            log.info("Order canceled event: orderId={}, price={}, keycloakId={}",
                    event.orderId(), event.price(), keycloakId);

            userServiceClient.credit(keycloakId, new CreditRequest(event.price()));
            log.info("Credit successful: keycloakId={}, amount={}", keycloakId, event.price());

            paymentService.cancelPayment(event);
        } catch (Exception e) {
            log.error("Refund failed for order {}: {}", event.orderId(), e.getMessage(), e);
            dlqService.sendToDLQ(objectMapper.writeValueAsString(event), "order-events", e);
            throw new RuntimeException("Refund processing failed", e);
        }
    }
}
