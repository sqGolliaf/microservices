package ru.sg.user.advice;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.sg.user.exception.EmailAlreadyExistsException;
import ru.sg.user.exception.RegistrationException;
import ru.sg.user.exception.UserAlreadyExistsException;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler({EmailAlreadyExistsException.class, RegistrationException.class, UserAlreadyExistsException.class})
    public ResponseEntity<String> handleNotRegistration(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidAuthentication(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
