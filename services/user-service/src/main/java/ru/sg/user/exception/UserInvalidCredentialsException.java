package ru.sg.user.exception;

public class UserInvalidCredentialsException extends RuntimeException {
    public UserInvalidCredentialsException(String message) {
        super(message);
    }
}
