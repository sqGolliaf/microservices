package ru.sg.user.dto.response;

import java.math.BigDecimal;

public record UserResponse(
        Long userId,
        String keycloakId,
        String email,
        String username,
        BigDecimal balance,
        String tier,
        String preferences
) {
}
