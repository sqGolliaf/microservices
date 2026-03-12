package ru.sg.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboxType {
    ORDER_CREATED("ORDER CREATED"),
    ORDER_CONFIRMED("ORDER CONFIRMED"),
    ORDER_FAILED("ORDER FAILED"),
    ORDER_CANCELED("ORDER CANCELED"),
    ORDER_UPDATED("ORDER UPDATED"),
    ORDER_COMPENSATED("ORDER COMPENSATED"),

    SEND_CREATE_DELIVERY_REQUEST("SEND CREATE DELIVERY REQUEST"),
    DELIVERY_REQUEST_CREATED("DELIVERY REQUEST CREATED"),
    DELIVERY_REQUEST_FAILED("DELIVERY REQUEST FAILED"),

    INVENTORY_RESERVED("INVENTORY RESERVED"),
    INVENTORY_RESERVATION_FAILED("INVENTORY RESERVATION FAILED"),
    INVENTORY_RELEASED("INVENTORY RELEASED"),

    PAYMENT_PROCESSED("PAYMENT PROCESSED"),
    PAYMENT_FAILED("PAYMENT FAILED"),
    PAYMENT_CANCELED("PAYMENT CANCELED"),
    PAYMENT_REFUNDED("PAYMENT REFUNDED");

    private final String value;

    public static OutboxType fromValue(String value) {
        for (OutboxType type : OutboxType.values()) {
            if (type.getValue().equals(value)) return type;
        }

        throw new IllegalArgumentException("Unknow type: " + value);
    }
}
