package ru.sg.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sg.order.dto.event.OrderEvent;
import ru.sg.order.entity.Order;
import ru.sg.order.entity.OutboxEvent;
import ru.sg.order.enums.OutboxStatus;
import ru.sg.order.enums.OutboxType;
import ru.sg.order.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${order.events.topic.name:order-events}")
    private String orderTopic;

    public void createOutboxEvent(Order order, OutboxType eventType) {
        try {
            String eventId = UUID.randomUUID().toString();

            OrderEvent orderEvent = OrderEvent.builder()
                    .orderId(order.getId())
                    .eventId(eventId)
                    .sagaId(order.getSagaId())
                    .eventType(eventType.toString())
                    .orderStatus(order.getStatus().getValue())
                    .name(order.getName())
                    .quantity(order.getQuantity())
                    .price(order.getPrice())
                    .timestamp(Instant.now())
                    .source("order-service")
                    .build();

            String payload = objectMapper.writeValueAsString(orderEvent);

            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .orderId(order.getId())
                    .eventType(eventType)
                    .status(OutboxStatus.CREATED)
                    .payload(payload)
                    .topic(orderTopic)
                    .partitionKey(order.getId().toString())
                    .retryCount(0)
                    .build();

            outboxEventRepository.save(event);

            log.info("OutboxEvent created: id={}, orderId={}, type={}, sagaId={}",
                    event.getId(), order.getId(), eventType, order.getSagaId());
        } catch (Exception e) {
            log.error("Failed to create OutboxEvent for order {}", order.getId(), e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
