package ru.sg.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboxStatus {
    CREATED("CREATED"),
    SENT("SENT"),
    PROCESSED("PROCESSED"),
    ERROR("ERROR"),
    FAILED("FAILED");

    private final String value;

    public static OutboxStatus fromValue(String value) {
        for (OutboxStatus status : OutboxStatus.values()) {
            if (status.getValue().equals(value)) return status;
        }

        throw new IllegalArgumentException("Unknown type: " + value);
    }
}
