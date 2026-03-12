package ru.sg.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.sg.order.client.UserServiceClient;
import ru.sg.order.dto.request.CreateOrderRequest;
import ru.sg.order.dto.response.OrderResponse;
import ru.sg.order.dto.response.UserResponse;
import ru.sg.order.entity.Order;
import ru.sg.order.enums.OrderStatus;
import ru.sg.order.enums.OutboxType;
import ru.sg.order.mapper.OrderMapper;
import ru.sg.order.repository.OrderRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    public OrderResponse createOrder(
            CreateOrderRequest createOrderRequest,
            String keycloakId,
            String email,
            String username
    ) {
        try {
            UserResponse user = userServiceClient.getOrCreateUser(keycloakId, email, username);
            log.info("User found/created: userId={}, email={}", user.userId(), user.email());

            String sagaId = UUID.randomUUID().toString();
            Order order = Order.builder()
                    .sagaId(sagaId)
                    .keycloakId(keycloakId)
                    .name(createOrderRequest.name())
                    .quantity(createOrderRequest.quantity())
                    .price(createOrderRequest.price())
                    .status(OrderStatus.PENDING)
                    .build();

            Order saveOrder = orderRepository.save(order);

            outboxService.createOutboxEvent(saveOrder, OutboxType.ORDER_CREATED);

            log.info("Order created: orderId={}, sagaId={}", saveOrder.getId(), sagaId);
            return orderMapper.toResponse(saveOrder);
        } catch (Exception e) {
            log.error("Failed to create order for keycloakId: {}", keycloakId, e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    public void updateOrderStatus(String sagaId, OrderStatus status) {
        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found for sagaId: " + sagaId));

        if (orderIsConfirmed(order)) return;

        order.setStatus(status);
        orderRepository.save(order);

        outboxService.createOutboxEvent(order, OutboxType.ORDER_UPDATED);

        log.info("Order {} status updated to {}", order.getId(), status);
    }

    public void confirmOrder(String sagaId) {
        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + sagaId));

        if (orderIsConfirmed(order)) return;

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        outboxService.createOutboxEvent(order, OutboxType.ORDER_CONFIRMED);

        log.info("Order {} confirmed", order.getId());
    }

    public void failOrder(String sagaId) {
        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + sagaId));

        if (orderIsConfirmed(order)) return;

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        outboxService.createOutboxEvent(order, OutboxType.ORDER_FAILED);
        log.warn("Order {} failed", sagaId);
    }

    public void cancelOrder(String sagaId) {
        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + sagaId));

        if (orderIsConfirmed(order)) return;

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        outboxService.createOutboxEvent(order, OutboxType.ORDER_CANCELED);
        log.warn("Order {} canceled", order.getId());
    }

    private static boolean orderIsConfirmed(Order order) {
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.info("Order already confirmed {}", order.getId());
            return true;
        }
        return false;
    }
}
