package ru.sg.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class Onto {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String FIELD_NODES = "nodes";
    private static final String FIELD_RELATIONS = "relations";
    private static final String LINK_IS_A = "is_a";

    private final JsonNode ontoData;
    private final List<Node> nodes;
    private final List<Link> links;

    private final Map<Integer, Node> nodeById;
    private final Map<Integer, Link> linkById;
    private final Map<String, List<Node>> nodesByName;

    public Onto(InputStream ontoFile) {
        Objects.requireNonNull(ontoFile, "Input stream cannot be null");

        this.ontoData = MAPPER.readTree(ontoFile);

        if (ontoData == null || !ontoData.isObject()) {
            throw new IllegalArgumentException("Input must be a valid JSON object");
        }

        this.nodes = parseNodes();
        this.links = parseLinks();

        this.nodeById = new HashMap<>();
        this.linkById = new HashMap<>();
        this.nodesByName = new HashMap<>();

        buildIndices();

        log.info("Ontology loaded: {} nodes, {} links", nodes.size(), links.size());
    }

    private List<Node> parseNodes() {

        if (!ontoData.has(FIELD_NODES) || !ontoData.get(FIELD_NODES).isArray()) {
            log.warn("Nodes field missing or not array");
            return Collections.emptyList();
        }

        List<Node> parsed = new ArrayList<>();

        ontoData.get(FIELD_NODES).forEach(nodeData -> {
            try {
                parsed.add(new Node(nodeData, this));
            } catch (Exception e) {
                log.error("Failed to parse node: {}", nodeData, e);
            }
        });

        return parsed;
    }

    private List<Link> parseLinks() {

        if (!ontoData.has(FIELD_RELATIONS) || !ontoData.get(FIELD_RELATIONS).isArray()) {
            log.warn("Relations field missing or not array");
            return Collections.emptyList();
        }

        List<Link> parsed = new ArrayList<>();

        ontoData.get(FIELD_RELATIONS).forEach(relationData -> {
            try {
                parsed.add(new Link(relationData));
            } catch (Exception e) {
                log.error("Failed to parse link: {}", relationData, e);
            }
        });

        return parsed;
    }

    private void buildIndices() {

        for (Node node : nodes) {
            nodeById.put(node.getId(), node);
            nodesByName.computeIfAbsent(node.getName(), k -> new ArrayList<>()).add(node);
        }

        for (Link link : links) {
            linkById.put(link.getId(), link);
        }

        log.debug("Indices built: {} nodes, {} links", nodeById.size(), linkById.size());
    }

    public Optional<Node> getNodeByID(int id) {
        return Optional.ofNullable(nodeById.get(id));
    }

    public Optional<Link> getLinkByID(int id) {
        return Optional.ofNullable(linkById.get(id));
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Node> getNodesByName(String name) {
        Objects.requireNonNull(name);
        return nodesByName.getOrDefault(name, List.of());
    }

    public Optional<Node> getFirstNodeByName(String name) {

        List<Node> nodes = nodesByName.get(name);
        if (nodes == null || nodes.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(nodes.get(0));
    }

    public List<Node> getNodesLinkedFrom(Node node, String linkName) {

        Objects.requireNonNull(node);
        Objects.requireNonNull(linkName);

        int nodeId = node.getId();

        return links.stream()
                .filter(link ->
                        link.getSourceNodeId() == nodeId &&
                                (linkName.isEmpty() || link.isOfType(linkName))
                )
                .map(link -> nodeById.get(link.getDestinationNodeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Node> getNodesLinkedTo(Node node, String linkName) {

        Objects.requireNonNull(node);
        Objects.requireNonNull(linkName);

        int nodeId = node.getId();

        return links.stream()
                .filter(link ->
                        link.getDestinationNodeId() == nodeId &&
                                (linkName.isEmpty() || link.isOfType(linkName))
                )
                .map(link -> nodeById.get(link.getSourceNodeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean isNodeOfTypeRecursive(Node node, String typeName) {
        return isNodeOfTypeRecursive(node, typeName, new HashSet<>());
    }

    private boolean isNodeOfTypeRecursive(Node node, String typeName, Set<Integer> visited) {

        if (!visited.add(node.getId())) {
            return false;
        }

        for (Node proto : getNodesLinkedFrom(node, LINK_IS_A)) {

            if (proto.getName().equals(typeName)) {
                return true;
            }

            if (isNodeOfTypeRecursive(proto, typeName, visited)) {
                return true;
            }
        }

        return false;
    }

    public String getInheritedAttributeOfNode(Node node, String attributeName) {
        return getInheritedAttributeOfNode(node, attributeName, new HashSet<>());
    }

    private String getInheritedAttributeOfNode(Node node, String attributeName, Set<Integer> visited) {

        if (!visited.add(node.getId())) {
            return null;
        }

        Optional<String> own = node.getUniqueAttribute(attributeName);
        if (own.isPresent()) {
            return own.get();
        }

        for (Node proto : getNodesLinkedFrom(node, LINK_IS_A)) {

            String value = getInheritedAttributeOfNode(proto, attributeName, visited);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

}