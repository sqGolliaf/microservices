package ru.sg.order.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sg.order.entity.Order;
import ru.sg.order.enums.OrderStatus;
import ru.sg.order.repository.OrderRepository;
import ru.sg.order.service.OrderService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPublisher {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${order.saga.timeout-minutes:30}")
    private int sagaTimeoutMinutes;

    @Scheduled(fixedRate = 60_000)
    public void failStuckOrders() {
        Instant timeoutTime = Instant.now().minus(sagaTimeoutMinutes, ChronoUnit.MINUTES);

        List<Order> orderList = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, timeoutTime);

        if (orderList.isEmpty()) return;

        for (Order order : orderList) {
            log.warn("Order {} stuck in PENDING more than {} minutes, failing it", order.getId(), sagaTimeoutMinutes);

            orderService.failOrder(order.getSagaId());
        }
    }
}
