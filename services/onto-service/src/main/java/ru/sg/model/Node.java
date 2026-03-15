package ru.sg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class Node {
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ATTRIBUTES = "attributes";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String EMPTY_NAME = "";

    @JsonProperty("id")
    private final int id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("attributes")
    private final Map<String, String> attributes;

    private final Map<String, Object> storage;

    private final transient Onto onto;

    public Node(JsonNode nodeData, Onto onto) {
        this.id = validateAndGetInt(nodeData);
        this.name = nodeData.has(FIELD_NAME) ? nodeData.get(FIELD_NAME).asString() : EMPTY_NAME;
        this.attributes = parseAttributes(nodeData);
        this.storage = new ConcurrentHashMap<>();
        this.onto = onto;

        log.debug("Node created: id={}, name='{}', attributesCount={}",
                id, name, attributes.size());
    }

    public Node(int id, String name, Map<String, String> attributes, Onto onto) {
        this.id = id;
        this.name = Objects.requireNonNullElse(name, EMPTY_NAME);
        this.attributes = new ConcurrentHashMap<>(Objects.requireNonNullElse(attributes, Map.of()));
        this.storage = new ConcurrentHashMap<>();
        this.onto = onto;

        log.debug("Node created programmatically: id={}, name='{}'", id, name);
    }

    public Optional<String> getNameOptional() {
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    public Optional<String> getUniqueAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        return Optional.ofNullable(attributes.get(attributeName));
    }

    public Optional<String> getAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");

        if (onto == null) {
            log.warn("Cannot perform inherited attribute lookup: onto reference is null");
            return getUniqueAttribute(attributeName);
        }

        return Optional.ofNullable(onto.getInheritedAttributeOfNode(this, attributeName));
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public boolean hasAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        return attributes.containsKey(attributeName);
    }

    public void putInStorage(String key, Object value) {
        Objects.requireNonNull(key, "Storage key cannot be null");
        storage.put(key, value);
        log.trace("Stored in node {}: {} = {}", id, key, value);
    }

    public Optional<Object> getFromStorage(String key) {
        Objects.requireNonNull(key, "Storage key cannot be null");
        return Optional.ofNullable(storage.get(key));
    }

    public void clearStorage() {
        storage.clear();
        log.debug("Cleared storage for node {}", id);
    }

    public void setAttribute(String attributeName, String value) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        attributes.put(attributeName, Objects.requireNonNullElse(value, ""));
        log.debug("Attribute set for node {}: {} = {}", id, attributeName, value);
    }

    public boolean removeAttribute(String attributeName) {
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");
        boolean removed = attributes.remove(attributeName) != null;
        if (removed) {
            log.debug("Attribute removed from node {}: {}", id, attributeName);
        }
        return removed;
    }

    private int validateAndGetInt(JsonNode node) {
        Objects.requireNonNull(node, "JsonNode cannot be null");

        if (!node.has(Node.FIELD_ID)) {
            log.error("{} field missing in node data", "Node ID");
            throw new IllegalArgumentException("Node ID" + " is required");
        }

        JsonNode field = node.get(Node.FIELD_ID);
        if (!field.isNumber()) {
            log.error("{} is not a number: {}", "Node ID", field.asString());
            throw new IllegalArgumentException("Node ID" + " must be a number");
        }

        return field.asInt();
    }

    private Map<String, String> parseAttributes(JsonNode nodeData) {
        if (!nodeData.has(FIELD_ATTRIBUTES)) {
            return new ConcurrentHashMap<>();
        }

        JsonNode attributesNode = nodeData.get(FIELD_ATTRIBUTES);

        if (!attributesNode.isObject()) {
            log.warn("Attributes not object for node {}", id);
            return new ConcurrentHashMap<>();
        }

        Map<String, String> map = new ConcurrentHashMap<>();

        attributesNode.properties().forEach(e ->
                map.put(e.getKey(), e.getValue().asString())
        );

        return map;
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