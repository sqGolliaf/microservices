package ru.sg.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.order.entity.Order;
import ru.sg.order.enums.OrderStatus;
import ru.sg.order.repository.OrderRepository;
import ru.sg.order.service.OrderService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SagaOrchestrator {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public void startOrderSaga(Long orderId, String sagaId) {
        log.info("Starting order saga: orderId={}, sagaId={}", orderId, sagaId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        log.info("Order saga started: orderId={}, status=PENDING", orderId);
    }

    public void onPaymentSuccess(String sagaId) {
        log.info("Payment successful for saga: {}", sagaId);

        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found for sagaId: " + sagaId));

        orderService.confirmOrder(sagaId);

        log.info("Order confirmed: orderId={}, sagaId={}", order.getId(), sagaId);
    }

    public void onPaymentFailure(String sagaId, String reason) {
        log.warn("Payment failed for saga {}: {}", sagaId, reason);

        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found for sagaId: " + sagaId));

        orderService.cancelOrder(sagaId);
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        log.warn("Order canceled due to payment failure: orderId={}, sagaId={}", order.getId(), sagaId);
    }

    public void handleSagaTimeout(String sagaId) {
        log.error("Saga timeout: {}", sagaId);

        Order order = orderRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Order not found for sagaId: " + sagaId));

        if (order.getStatus() == OrderStatus.PENDING) {
            // �� Компенсирующая транзакция: отменить и вернуть деньги
            orderService.cancelOrder(sagaId);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            log.error("Order canceled due to saga timeout: orderId={}, sagaId={}", order.getId(), sagaId);
        }
    }
}
