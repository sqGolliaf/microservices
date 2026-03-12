package ru.sg.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("PENDING"),
    RESERVED("RESERVED"),
    PAYMENT_DONE("PAYMENT DONE"),
    CONFIRMED("CONFIRMED"),
    FAILED("FAILED"),
    COMPENSATING("COMPENSATING"),
    CANCELED("CANCELED");

    private final String value;

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue().equals(value)) return status;
        }

        throw new IllegalArgumentException("Unknown type: " + value);
    }
}
