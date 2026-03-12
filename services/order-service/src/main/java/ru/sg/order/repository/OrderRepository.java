package ru.sg.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sg.order.entity.Order;
import ru.sg.order.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findBySagaId(String sagaId);

    List<Order> findByKeycloakId(String keycloakId);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant createdAtBefore);
}
