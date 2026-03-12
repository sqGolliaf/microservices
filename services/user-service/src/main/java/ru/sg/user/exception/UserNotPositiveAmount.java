package ru.sg.user.exception;

public class UserNotPositiveAmount extends RuntimeException {
    public UserNotPositiveAmount(String message) {
        super(message);
    }
}
