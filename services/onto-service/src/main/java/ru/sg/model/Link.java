package ru.sg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@Slf4j
public class Link {
    private static final String FIELD_ID = "id";
    private static final String FIELD_SOURCE_NODE_ID = "source_node_id";
    private static final String FIELD_DESTINATION_NODE_ID = "destination_node_id";
    private static final String FIELD_NAME = "name";

    private static final int INVALID_ID = -1;
    private static final String EMPTY_NAME = "";

    @JsonProperty("id")
    private final int id;

    @JsonProperty("source_node_id")
    private final int sourceNodeId;

    @JsonProperty("destination_node_id")
    private final int destinationNodeId;

    @JsonProperty("name")
    private final String name;

    public Link(JsonNode relationData) {
        this.id = validateAndGetInt(relationData, FIELD_ID, "Link ID");
        this.sourceNodeId = validateAndGetInt(relationData, FIELD_SOURCE_NODE_ID, "Source Node ID");
        this.destinationNodeId = validateAndGetInt(relationData, FIELD_DESTINATION_NODE_ID, "Destination Node ID");
        this.name = relationData.get(FIELD_NAME) != null ? relationData.get(FIELD_NAME).asString() : EMPTY_NAME;

        log.info("Link created: id={}, source={}, dest={}, name={}", id, sourceNodeId, destinationNodeId, name);
    }

    public Link(int id, int sourceNodeId, int destinationNodeId, String name) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.name = Objects.requireNonNullElse(name, EMPTY_NAME);
    }

    public Optional<String> getNameOptional() {
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    public boolean isOfType(String relationType) {
        return Objects.nonNull(relationType) && name.equals(relationType);
    }

    private int validateAndGetInt(JsonNode node, String fieldName, String fieldDescription) {
        Objects.requireNonNull(node, "JsonNode cannot be null");

        if (!node.has(fieldName)) {
            log.warn("{} field missing in relation data", fieldDescription);
            throw new IllegalArgumentException(fieldDescription + " is required");
        }

        JsonNode field = node.get(fieldName);
        if (!field.isNumber()) {
            log.warn("{} is not a number: {}", fieldDescription, field.asString());
            throw new IllegalArgumentException(fieldDescription + " must be a number");
        }

        return field.asInt();
    }

    @Override
    public String toString() {
        return String.format("Link{id=%d, %d->%d, name='%s'}", id, sourceNodeId, destinationNodeId, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return id == link.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
