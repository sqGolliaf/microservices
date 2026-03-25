package ru.sg.user.event.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EmailStatus {
    PENDING("Pending"),
    SENT("Sent"),
    FAILED("Failed"),
    VERIFIED("Verified");


    private final String value;
}
