package ru.sg.paymentservice.dto.request;

import java.math.BigDecimal;

public record CreditRequest(BigDecimal amount) {}
