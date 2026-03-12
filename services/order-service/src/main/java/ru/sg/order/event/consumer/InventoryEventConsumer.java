package ru.sg.order.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.order.dto.event.InventoryEvent;
import ru.sg.order.enums.OrderStatus;
import ru.sg.order.enums.OutboxType;
import ru.sg.order.service.DLQService;
import ru.sg.order.service.OrderService;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final DLQService dlqService;

    @KafkaListener(
            topics = "inventory-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleInventoryEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "saga-id", required = false) String sagaId,
            @Header(value = "event-type", required = false) String eventType) {
        try {
            log.info("Received inventory event: sagaId={}, eventType={}", sagaId, eventType);

            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);

            if (OutboxType.INVENTORY_RESERVED.getValue().equals(eventType))
                orderService.updateOrderStatus(event.sagaId(), OrderStatus.RESERVED);
            else if (OutboxType.INVENTORY_RESERVATION_FAILED.getValue().equals(eventType))
                orderService.failOrder(event.sagaId());

        } catch (Exception e) {
            log.error("Failed to process inventory event: {}", e.getMessage(), e);
            dlqService.sendToDLQ(message, topic, e);
            throw new RuntimeException("Event processing failed", e);
        }
    }
}
