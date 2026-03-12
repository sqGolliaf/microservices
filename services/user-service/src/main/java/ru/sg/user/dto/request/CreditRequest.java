package ru.sg.user.dto.request;

import java.math.BigDecimal;

public record CreditRequest(
    BigDecimal amount
) {}
