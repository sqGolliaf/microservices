package ru.sg.back.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.sg.back.dto.ApiException;
import ru.sg.back.exception.NotFoundException;

import java.util.List;

@RestControllerAdvice
public class OntoExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiException handleException(Exception e) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, List.of(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiException handleNotFoundException(NotFoundException e) {
        return new ApiException(HttpStatus.NOT_FOUND, List.of(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiException handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> list = e.getBindingResult().getFieldErrors().stream()
                .map(it -> it.getField() + "=" + it.getRejectedValue() + ":" + it.getDefaultMessage())
                .toList();

        return new ApiException(HttpStatus.BAD_REQUEST, list);
    }
}
