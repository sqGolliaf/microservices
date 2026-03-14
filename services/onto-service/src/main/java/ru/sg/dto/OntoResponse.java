package ru.sg.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OntoResponse(
        @JsonProperty("id")
        Integer id,

        @JsonProperty("name")
        String name,

        @JsonProperty("storage")
        Map<String, Object> storage,

        @JsonProperty("attribute")
        String attribute
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public OntoResponse {
        if (id == null || name == null) throw new IllegalArgumentException("ID and name cannot be null");
    }
}
