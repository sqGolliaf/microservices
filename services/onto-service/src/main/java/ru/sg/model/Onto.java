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

/**
 * The Onto class provides a container to store and query ontology data.
 * Supports loading from JSON files and provides efficient search operations
 * with caching for O(1) node lookup by ID.
 *
 * Optimized with Stream API, proper data structures (List instead of arrays),
 * and HashMap indexing for fast lookups.
 *
 * @since 2.0
 */
@Slf4j
@Getter(AccessLevel.PACKAGE)
public class Onto {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // JSON field constants
    private static final String FIELD_NODES = "nodes";
    private static final String FIELD_RELATIONS = "relations";

    private final JsonNode ontoData;
    private final List<Node> nodes;
    private final List<Link> links;

    // Индексы для быстрого поиска O(1)
    private final Map<Integer, Node> nodeById;
    private final Map<Integer, Link> linkById;
    private final Map<String, List<Node>> nodesByName;

    /**
     * Creates a new Onto from an input stream containing JSON ontology data.
     *
     * @param ontoFile input stream with JSON ontology
     * @throws IOException if unable to read the stream
     * @throws IllegalArgumentException if JSON is malformed or required fields are missing
     */
    public Onto(InputStream ontoFile) {
        Objects.requireNonNull(ontoFile, "Input stream cannot be null");

        // Парсим JSON один раз
        this.ontoData = MAPPER.readTree(ontoFile);

        if (ontoData == null || !ontoData.isObject()) {
            throw new IllegalArgumentException("Input must be a valid JSON object");
        }

        // Парсим узлы
        this.nodes = parseNodes();

        // Парсим связи
        this.links = parseLinks();

        // Создаем индексы для быстрого поиска
        this.nodeById = new HashMap<>();
        this.linkById = new HashMap<>();
        this.nodesByName = new HashMap<>();

        buildIndices();

        log.info("Ontology loaded: {} nodes, {} links", nodes.size(), links.size());

    }

    /**
     * Parses nodes from the ontology JSON.
     *
     * @return list of nodes
     */
    private List<Node> parseNodes() {
        if (!ontoData.has(FIELD_NODES)) {
            log.warn("No nodes field found in ontology");
            return Collections.emptyList();
        }

        JsonNode nodesArray = ontoData.get(FIELD_NODES);
        if (!nodesArray.isArray()) {
            log.error("Nodes field is not an array");
            return Collections.emptyList();
        }

        List<Node> parsedNodes = new ArrayList<>();
        nodesArray.forEach(nodeData -> {
            try {
                Node node = new Node(nodeData, this);
                parsedNodes.add(node);
            } catch (Exception e) {
                log.error("Failed to parse node: {}", nodeData.asText(), e);
            }
        });

        return parsedNodes;
    }

    /**
     * Parses links/relations from the ontology JSON.
     *
     * @return list of links
     */
    private List<Link> parseLinks() {
        if (!ontoData.has(FIELD_RELATIONS)) {
            log.warn("No relations field found in ontology");
            return Collections.emptyList();
        }

        JsonNode relationsArray = ontoData.get(FIELD_RELATIONS);
        if (!relationsArray.isArray()) {
            log.error("Relations field is not an array");
            return Collections.emptyList();
        }

        List<Link> parsedLinks = new ArrayList<>();
        relationsArray.forEach(relationData -> {
            try {
                Link link = new Link(relationData);
                parsedLinks.add(link);
            } catch (Exception e) {
                log.error("Failed to parse link: {}", relationData.asText(), e);
            }
        });

        return parsedLinks;
    }

    /**
     * Builds indices for fast lookup operations.
     */
    private void buildIndices() {
        // Индекс узлов по ID
        for (Node node : nodes) {
            nodeById.put(node.getId(), node);
        }

        // Индекс связей по ID
        for (Link link : links) {
            linkById.put(link.getId(), link);
        }

        // Индекс узлов по имени (может быть несколько с одним именем)
        for (Node node : nodes) {
            nodesByName.computeIfAbsent(node.getName(), k -> new ArrayList<>()).add(node);
        }

        log.debug("Built indices: {} nodes by id, {} links by id, {} node name groups",
                nodeById.size(), linkById.size(), nodesByName.size());
    }

    /**
     * Gets all nodes in the ontology.
     *
     * @return unmodifiable list of nodes
     */
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Gets all links/relations in the ontology.
     *
     * @return unmodifiable list of links
     */
    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    /**
     * Gets a node by its unique identifier.
     * O(1) operation using index.
     *
     * @param id the node ID
     * @return the node, or empty Optional if not found
     */
    public Optional<Node> getNodeByID(int id) {
        return Optional.ofNullable(nodeById.get(id));
    }

    /**
     * Gets a link by its unique identifier.
     * O(1) operation using index.
     *
     * @param id the link ID
     * @return the link, or empty Optional if not found
     */
    public Optional<Link> getLinkByID(int id) {
        return Optional.ofNullable(linkById.get(id));
    }

    /**
     * Gets all nodes matching a specific name.
     *
     * @param name the node name
     * @return list of matching nodes, or empty list if none found
     */
    public List<Node> getNodesByName(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return nodesByName.getOrDefault(name, Collections.emptyList());
    }

    /**
     * Gets the first node with a specific name.
     *
     * @param name the node name
     * @return the first matching node, or empty Optional if not found
     */
    public Optional<Node> getFirstNodeByName(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        List<Node> matching = nodesByName.get(name);
        return matching != null && !matching.isEmpty()
                ? Optional.of(matching.get(0))
                : Optional.empty();
    }

    /**
     * Gets all nodes linked FROM the given node with a specific link type.
     * Direction: given node -> returned nodes
     *
     * @param node the source node
     * @param linkName the link/relation name (e.g., "is_a", "a_part_of")
     * @return list of nodes, or empty list if none found
     */
    public List<Node> getNodesLinkedFrom(Node node, String linkName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(linkName, "Link name cannot be null");

        int nodeId = node.getId();
        return links.stream()
                .filter(link -> link.getSourceNodeId() == nodeId && link.isOfType(linkName))
                .map(link -> getNodeByID(link.getDestinationNodeId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Gets all nodes linked TO the given node with a specific link type.
     * Direction: returned nodes -> given node
     *
     * @param node the destination node
     * @param linkName the link/relation name
     * @return list of nodes, or empty list if none found
     */
    public List<Node> getNodesLinkedTo(Node node, String linkName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(linkName, "Link name cannot be null");

        int nodeId = node.getId();
        return links.stream()
                .filter(link -> link.getDestinationNodeId() == nodeId && link.isOfType(linkName))
                .map(link -> getNodeByID(link.getSourceNodeId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Gets typed nodes linked FROM the given node.
     * Returns only nodes that have a specific type (is_a relation).
     *
     * @param node the source node
     * @param linkName the link/relation name
     * @param typeName the type name (via is_a relation)
     * @return list of typed nodes, or empty list if none found
     */
    public List<Node> getTypedNodesLinkedFrom(Node node, String linkName, String typeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(linkName, "Link name cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");

        return getNodesLinkedFrom(node, linkName).stream()
                .filter(linkedNode -> isNodeOfTypeRecursive(linkedNode, typeName))
                .collect(Collectors.toList());
    }

    /**
     * Gets typed nodes linked TO the given node.
     * Returns only nodes that have a specific type (is_a relation).
     *
     * @param node the destination node
     * @param linkName the link/relation name
     * @param typeName the type name (via is_a relation)
     * @return list of typed nodes, or empty list if none found
     */
    public List<Node> getTypedNodesLinkedTo(Node node, String linkName, String typeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(linkName, "Link name cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");

        return getNodesLinkedTo(node, linkName).stream()
                .filter(linkedNode -> isNodeOfTypeRecursive(linkedNode, typeName))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a node has a specific type (direct is_a relation).
     *
     * @param node the node to check
     * @param typeName the type name
     * @return true if node has the type, false otherwise
     */
    public boolean isNodeOfType(Node node, String typeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");

        return getNodesLinkedFrom(node, "is_a").stream()
                .anyMatch(proto -> proto.getName().equals(typeName));
    }

    /**
     * Checks if a node has a specific type recursively through is_a hierarchy.
     *
     * @param node the node to check
     * @param typeName the type name
     * @return true if node has the type at any inheritance level, false otherwise
     */
    public boolean isNodeOfTypeRecursive(Node node, String typeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");

        List<Node> protos = getNodesLinkedFrom(node, "is_a");

        return protos.stream()
                .anyMatch(proto -> proto.getName().equals(typeName) ||
                        isNodeOfTypeRecursive(proto, typeName));
    }

    /**
     * Checks if a node has a specific type through outgoing links.
     *
     * @param node the node to check
     * @param linkName the link/relation name
     * @param typeName the type name
     * @return true if any node linked from this node has the type, false otherwise
     */
    public boolean isNodeOfTypeRecursiveFrom(Node node, String linkName, String typeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(linkName, "Link name cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");

        return getNodesLinkedFrom(node, linkName).stream()
                .anyMatch(linkedNode -> isNodeOfTypeRecursive(linkedNode, typeName));
    }

    /**
     * Checks if a node has a specific type through incoming links.
     *
     * @param node the node to check
     * @param linkName the link/relation name
     * @param typeName the type name
     * @return true if any node linked to this node has the type, false otherwise
     */
    public boolean isNodeOfTypeRecursiveTo(Node node, String linkName, String typeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(linkName, "Link name cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");

        return getNodesLinkedTo(node, linkName).stream()
                .anyMatch(linkedNode -> isNodeOfTypeRecursive(linkedNode, typeName));
    }

    /**
     * Gets an inherited attribute value, searching up the is_a hierarchy.
     *
     * @param node the node to get attribute from
     * @param attributeName the attribute name
     * @return the attribute value, or null if not found in hierarchy
     */
    public String getInheritedAttributeOfNode(Node node, String attributeName) {
        Objects.requireNonNull(node, "Node cannot be null");
        Objects.requireNonNull(attributeName, "Attribute name cannot be null");

        // Сначала проверяем сам узел
        Optional<String> ownAttribute = node.getUniqueAttribute(attributeName);
        if (ownAttribute.isPresent()) {
            return ownAttribute.get();
        }

        // Затем ищем в прототипах (is_a)
        List<Node> protos = getNodesLinkedFrom(node, "is_a");
        for (Node proto : protos) {
            String inheritedValue = getInheritedAttributeOfNode(proto, attributeName);
            if (inheritedValue != null) {
                return inheritedValue;
            }
        }

        return null;
    }

    /**
     * Gets statistics about the ontology.
     *
     * @return a string with ontology statistics
     */
    public String getStatistics() {
        return String.format(
                "Ontology Statistics:\n" +
                        "  Total Nodes: %d\n" +
                        "  Total Links: %d\n" +
                        "  Unique Node Names: %d",
                nodes.size(),
                links.size(),
                nodesByName.size()
        );
    }

    @Override
    public String toString() {
        return String.format("Onto{nodes=%d, links=%d}", nodes.size(), links.size());
    }
}