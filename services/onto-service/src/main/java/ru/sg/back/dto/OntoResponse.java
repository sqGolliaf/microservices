package ru.sg.back.dto;

import java.io.Serializable;
import java.util.Hashtable;

public record OntoResponse(
        Integer id,
        String text,
        Hashtable<String, Object> storage,
        String attribute
) implements Serializable {
}
