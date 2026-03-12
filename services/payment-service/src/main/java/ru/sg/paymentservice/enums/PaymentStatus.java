package ru.sg.paymentservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    SUCCESS("SUCCESS"),
    NOT_SUCCESS("NOT_SUCCESS"),
    ERROR("ERROR"),
    FAILED("FAILED");

    private final String value;

    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getValue().equals(value)) return status;
        }

        throw new IllegalArgumentException("Unknow type: " + value);
    }
}
