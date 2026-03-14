package ru.sg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.Optional;

/**
 * The Link class represents an ontology relation (edge) between two nodes.
 * Provides sage access to JSON relation data with proper error handling.
 *
 * @since 1.0
 */
@Getter(AccessLevel.PACKAGE)
@Slf4j
public class Link {
    private static final String FIELD_ID = "id";
    private static final String FIELD_SOURCE_NODE_ID = "source_node_id";
    private static final String FIELD_DESTINATION_NODE_ID = "destination_node_id";
    private static final String FIELD_NAME = "name";

    // Default values
    private static final int INVALID_ID = -1;
    private static final String EMPTY_NAME = "";

    /**
     * -- GETTER --
     *  Gets the unique identifier of this relation.
     *
     */
    @JsonProperty("id")
    private final int id;

    /**
     * -- GETTER --
     *  Gets the source node ID of this relation.
     *
     */
    @JsonProperty("source_node_id")
    private final int sourceNodeId;

    /**
     * -- GETTER --
     *  Gets the destination node ID of this relation.
     *
     */
    @JsonProperty("destination_node_id")
    private final int destinationNodeId;

    /**
     * -- GETTER --
     *  Gets the name/type of this relation (e.g., "is_a", "a_part_of").
     *
     */
    @JsonProperty("name")
    private final String name;

    public Link(JsonNode relationData) {
        this.id = validateAndGetInt(relationData, FIELD_ID, "Link ID");
        this.sourceNodeId = validateAndGetInt(relationData, FIELD_SOURCE_NODE_ID, "Source Node ID");
        this.destinationNodeId = validateAndGetInt(relationData, FIELD_DESTINATION_NODE_ID, "Destination Node ID");
        this.name = relationData.get(FIELD_NAME) != null ? relationData.get(FIELD_NAME).asText() : EMPTY_NAME;

        log.info("Link created: id={}, source={}, dest={}, name={}", id, sourceNodeId, destinationNodeId, name);
    }

    public Link(int id, int sourceNodeId, int destinationNodeId, String name) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.name = Objects.requireNonNull(name, EMPTY_NAME);

        log.info("Link created: id={}, source={}, dest={}, name={}", id, sourceNodeId, destinationNodeId, this.name);
    }

    /**
     * Gets the relation name as an Optional.
     * Useful for optional chaining and null-safety.
     *
     * @return Optional containing the name if non-empty
     */
    public Optional<String> getNameOptional() {
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    /**
     * Checks if this relation is of a specific type.
     *
     * @param relationType the relation type to check against
     * @return true if the name matches the given type, false otherwise
     */
    public boolean isOfType(String relationType) {
        return Objects.nonNull(relationType) && name.equals(relationType);
    }

    /**
     * Validates and extracts an integer value from JsonNode.
     *
     * @param node the JsonNode to extract from
     * @param fieldName the field name
     * @param fieldDescription human-readable description for logging
     * @return the integer value
     * @throws IllegalArgumentException if field is missing or not a number
     */
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
