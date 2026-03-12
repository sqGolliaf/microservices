package ru.sg.order.mapper;

import org.springframework.stereotype.Component;
import ru.sg.order.dto.response.OrderResponse;
import ru.sg.order.entity.Order;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .sagaId(order.getSagaId())
                .keycloakId(order.getKeycloakId())
                .name(order.getName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .status(order.getStatus().getValue())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
