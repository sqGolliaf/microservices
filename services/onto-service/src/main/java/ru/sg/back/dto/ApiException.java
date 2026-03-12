package ru.sg.back.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ApiException(
        HttpStatus status,
        List<String> details
) {
}
