package ru.sg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Node class represents a concept/entity in the ontology.
 * Provides safe access to node properties with proper validation and error handling.
 * Thread-safe storage using ConcurrentHashMap.
 *
 * @since 2.0
 */
@Slf4j
@Getter(AccessLevel.PACKAGE)
public class Node {

    // JSON field constants
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ATTRIBUTES = "attributes";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Default values
    private static final String EMPTY_NAME = "";

    /**
     * -- GETTER --
     *  Gets the unique identifier of this node.
     *
     */
    @JsonProperty("id")
    private final int id;

    /**
     * -- GETTER --
     *  Gets the name of this node.
     *
     */
    @JsonProperty("name")
    private final String name;

    @JsonProperty("attributes")
    private final Map<String, String> attributes;

    /**
     * -- GETTER --
     *  Gets the runtime storage for this node.
     *  Thread-safe map for storing computed or temporary data.
     *
     */
    // Thread-safe runtime storage for computed data
    private final Map<String, Object> storage;

    // Reference to parent ontology (lazy-loaded, optional)
    private final transient Onto onto;

    /**
     * Creates a new Node from Jackson JsonNode.
     *
     * @param nodeData Jackson JsonNode containing node properties
     * @param onto reference to parent Onto (can be null for testing)
     * @throws NullPointerException if nodeData is null
     * @throws IllegalArgumentException if required fields are missing
     */
    public Node(JsonNode nodeData, Onto onto) {
        this.id = validateAndGetInt(nodeData, FIELD_ID, "Node ID");
        this.name = nodeData.has(FIELD_NAME) ? nodeData.get(FIELD_NAME).asString() : EMPTY_NAME;
        this.attributes = parseAttributes(nodeData);
        this.storage = new ConcurrentHashMap<>();
        this.onto = onto;

        log.debug("Node created: id={}, name='{}', attributesCount={}",
                id, name, attributes.size());
    }

    /**
     * Alternative constructor for programmatic creation.
     *
     * @param id node ID
     * @param name node name
     * @param attributes node attributes (can be null)
     * @param onto reference to parent Onto
     */
    public Node(int id, String name, Map<String, String> attributes, Onto onto) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, EMPTY_NAME);
        this.attributes = Objects.requireNonNullElse(attributes, Collections.emptyMap());
        this.storage = new ConcurrentHashMap<>();
        this.onto = onto;

        log.debug("Node created programmatically: id={}, name='{}'", id, name);
    }

    /**
     * Gets the name as an Optional for safer handling.
     *
     * @return Optional containing the name if non-empty
     */
    public Optional<String> getNameOptional() {
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    /**
     * Gets an attribute value stored directly in this node.
     * Does not perform inheritance lookup.
     *
     * @param attributeName the attribute name
     * @return the attribute value, or empty Optional if not found
     */
    public Optional<String> getUniqueAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        return Optional.ofNullable(attributes.get(attributeName));
    }

    /**
     * Gets an attribute value with inheritance lookup.
     * Searches upwards through the ontology hierarchy via "is_a" relations.
     * Only works if onto reference is available.
     *
     * @param attributeName the attribute name
     * @return the attribute value, or empty Optional if not found in hierarchy
     */
    public Optional<String> getAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");

        if (onto == null) {
            log.warn("Cannot perform inherited attribute lookup: onto reference is null");
            return getUniqueAttribute(attributeName);
        }

        return Optional.ofNullable(onto.getInheritedAttributeOfNode(this, attributeName));
    }

    /**
     * Gets all attributes of this node.
     * Returns an unmodifiable map.
     *
     * @return unmodifiable map of attributes
     */
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Checks if this node has a specific attribute.
     *
     * @param attributeName the attribute name
     * @return true if attribute exists, false otherwise
     */
    public boolean hasAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        return attributes.containsKey(attributeName);
    }

    /**
     * Stores a value in the runtime storage.
     *
     * @param key the storage key
     * @param value the value to store
     */
    public void putInStorage(String key, Object value) {
        Objects.requireNonNull(key, "Storage key cannot be null");
        storage.put(key, value);
        log.trace("Stored in node {}: {} = {}", id, key, value);
    }

    /**
     * Retrieves a value from the runtime storage.
     *
     * @param key the storage key
     * @return the stored value, or empty Optional if not found
     */
    public Optional<Object> getFromStorage(String key) {
        Objects.requireNonNull(key, "Storage key cannot be null");
        return Optional.ofNullable(storage.get(key));
    }

    /**
     * Clears the runtime storage.
     * Useful for memory management in long-running operations.
     */
    public void clearStorage() {
        storage.clear();
        log.debug("Cleared storage for node {}", id);
    }

    /**
     * Sets an attribute value for this node.
     *
     * @param attributeName the attribute name
     * @param value the attribute value
     */
    public void setAttribute(String attributeName, String value) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        attributes.put(attributeName, Objects.requireNonNullElse(value, ""));
        log.debug("Attribute set for node {}: {} = {}", id, attributeName, value);
    }

    /**
     * Removes an attribute from this node.
     *
     * @param attributeName the attribute name
     * @return true if attribute was present, false otherwise
     */
    public boolean removeAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        boolean removed = attributes.remove(attributeName) != null;
        if (removed) {
            log.debug("Attribute removed from node {}: {}", id, attributeName);
        }
        return removed;
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
            log.error("{} field missing in node data", fieldDescription);
            throw new IllegalArgumentException(fieldDescription + " is required");
        }

        JsonNode field = node.get(fieldName);
        if (!field.isNumber()) {
            log.error("{} is not a number: {}", fieldDescription, field.asText());
            throw new IllegalArgumentException(fieldDescription + " must be a number");
        }

        return field.asInt();
    }

    /**
     * Parses attributes from JsonNode into a mutable map.
     * Использует ObjectMapper для конвертации в Map.
     *
     * @param nodeData the JsonNode containing node data
     * @return map of attributes, or empty map if attributes section is missing
     */
    private Map<String, String> parseAttributes(JsonNode nodeData) {
        if (!nodeData.has(FIELD_ATTRIBUTES)) {
            return new HashMap<>();
        }

        JsonNode attributesNode = nodeData.get(FIELD_ATTRIBUTES);
        if (!attributesNode.isObject()) {
            log.warn("Attributes field is not an object for node {}",
                    nodeData.get(FIELD_ID).asText());
            return new HashMap<>();
        }

        try {
            // Конвертируем JsonNode в Map<String, String>
            return MAPPER.convertValue(attributesNode,
                    MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
        } catch (Exception e) {
            log.error("Failed to parse attributes for node {}", nodeData.get(FIELD_ID).asText(), e);
            return new HashMap<>();
        }
    }

    @Override
    public String toString() {
        return String.format("Node{id=%d, name='%s', attributes=%d, storage=%d}",
                id, name, attributes.size(), storage.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}