package ru.sg.paymentservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboxStatus {
    CREATED("CREATED"),
    SENT("SENT"),
    CONFIRMED("CONFIRMED"),
    FAILED("FAILED"),
    ERROR("ERROR");

    private final String value;

    public static OutboxStatus fromValue(String value) {
        for (OutboxStatus status : OutboxStatus.values()) {
            if (status.getValue().equals(value)) return status;
        }

        throw new IllegalArgumentException("Unknown type: " + value);
    }
}
