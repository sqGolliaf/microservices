package ru.sg.user.dto.response;

import java.math.BigDecimal;

public record UserResponse(
        String email,
        String username,
        Boolean isActive,
        BigDecimal balance,
        String tier,
        String preferences
) {
}
