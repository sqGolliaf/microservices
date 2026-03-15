package ru.sg.auth.dto;

import jakarta.validation.constraints.*;

public record UserDTO(
        @Positive
        String name,

        @Size(min = 2, max = 50)
        String surname,

        @Min(1)
        @Max(100)
        int age,

        @Email
        String email
) {
}
