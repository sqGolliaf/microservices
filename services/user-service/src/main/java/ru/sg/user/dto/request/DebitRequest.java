package ru.sg.user.dto.request;

import java.math.BigDecimal;

public record DebitRequest(
        BigDecimal amount
) {}
