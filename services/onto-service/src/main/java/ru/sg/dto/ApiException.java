package ru.sg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiException(
        @JsonProperty("status")
        Integer status,

        @JsonProperty("error")
        String error,

        @JsonProperty("details")
        List<String> details,

        @JsonProperty("timestamp")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public ApiException {
        if (status == null || error == null) throw new IllegalArgumentException("Status and error are required");
    }
}
