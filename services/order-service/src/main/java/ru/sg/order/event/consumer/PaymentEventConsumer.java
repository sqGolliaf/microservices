package ru.sg.order.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.order.dto.event.PaymentEvent;
import ru.sg.order.enums.OrderStatus;
import ru.sg.order.enums.OutboxType;
import ru.sg.order.service.DLQService;
import ru.sg.order.service.OrderService;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final DLQService dlqService;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "saga-id", required = false) String sagaId,
            @Header(value = "event-type", required = false) String eventType) {
        try {
            log.info("Received payment event: sagaId={}, eventType={}", sagaId, eventType);

            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);

            OutboxType type = OutboxType.fromValue(eventType);

            switch (type) {
                case PAYMENT_PROCESSED -> orderService.confirmOrder(event.sagaId());
                case PAYMENT_CANCELED -> orderService.cancelOrder(event.sagaId());
                case PAYMENT_FAILED, PAYMENT_REFUNDED -> orderService.failOrder(event.sagaId());
            }
        } catch (Exception e) {
            log.error("Fauled to process payment event: {}", e.getMessage(), e);
            dlqService.sendToDLQ(message, topic, e);
            throw new RuntimeException("Event processing failed", e);
        }
    }
}
