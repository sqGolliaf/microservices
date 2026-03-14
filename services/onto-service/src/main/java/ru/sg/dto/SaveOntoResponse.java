package ru.sg.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

public record SaveOntoResponse(
        @JsonProperty("id")
        Integer id,

        @JsonProperty("text")
        String text
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public SaveOntoResponse {
        if (id == null || text == null) throw new IllegalArgumentException("ID and text are required");
    }
}
