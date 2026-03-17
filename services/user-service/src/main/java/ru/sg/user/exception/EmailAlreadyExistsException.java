package ru.sg.user.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String emailAlreadyExists) {
        super(emailAlreadyExists);
    }
}
