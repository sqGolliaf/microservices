package ru.sg.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.sg.dto.ApiException;
import ru.sg.exception.NotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class OntoExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiException> handleException(Exception ex, WebRequest req) {
        log.error("Unexpected error occurred", ex);

        ApiException error = new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiException> handleNotFoundException(NotFoundException ex, WebRequest req) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiException error = new ApiException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiException> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest req) {
        log.warn("Invalid argument: {}", ex.getMessage());

        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                List.of(ex.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiException> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest req) {
        log.warn("Validation failed");

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(er -> String.format("%s: %s", er.getField(), er.getDefaultMessage()))
                .toList();

        ApiException error = new ApiException(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiException> handleIOException(IOException ex, WebRequest req) {
        log.error("IO Error occurred", ex);

        ApiException error = new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "IO Error",
                List.of("Failed to process ontology file"),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
