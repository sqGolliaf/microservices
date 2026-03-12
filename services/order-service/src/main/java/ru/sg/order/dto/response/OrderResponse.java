package ru.sg.order.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record OrderResponse(
        Long id,
        String sagaId,
        String keycloakId,
        String name,
        Integer quantity,
        BigDecimal price,
        String status,
        Instant createdAt,
        Instant updatedAt) {
}
