package ru.sg.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sg.dto.OntoResponse;
import ru.sg.model.Link;
import ru.sg.model.Node;
import ru.sg.model.Onto;
import ru.sg.services.OntoService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for ontology operations.
 * Transforms domain models (Node, Link) into DTOs for API responses.
 * Provides business logic for ontology graph traversal.
 *
 * @since 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OntoServiceImpl implements OntoService {

    private static final String ATTR_FINALIZED = "finalyze";
    private static final String LINK_IS_A = "is_a";
    private static final String LINK_PATH_OF = "a_path_of";
    private static final String LINK_POSITION = "position";

    private final Onto dataOnt;

    /**
     * Gets all nodes in the ontology as DTOs.
     *
     * @return list of all nodes converted to OntoResponse
     */
    @Override
    public List<OntoResponse> nodes() {
        log.debug("Fetching all nodes");
        return dataOnt.getNodes().stream()
                .map(this::mapNodeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets the first node by name and all nodes linked from it.
     * Used as entry point for ontology traversal.
     *
     * @param name the node name to find
     * @return list of nodes linked from the start node
     * @throws IllegalArgumentException if start node not found
     */
    @Override
    public List<OntoResponse> firstNodeByName(String name) {
        log.debug("Fetching start node with name: {}", name);

        var startNode = dataOnt.getFirstNodeByName(name)
                .orElseThrow(() -> {
                    log.error("Start node not found with name: {}", name);
                    return new IllegalArgumentException("Node not found: " + name);
                });

        // Получаем узлы, связанные от стартового (без фильтра связи)
        List<Node> linkedNodes = dataOnt.getNodesLinkedFrom(startNode, "");

        log.debug("Found {} nodes linked from start node", linkedNodes.size());
        return linkedNodes.stream()
                .map(this::mapNodeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets nodes linked via "a_path_of" relation to a given node.
     * Represents path/hierarchy relations.
     *
     * @param id the source node ID
     * @return list of nodes in the path relationship
     */
    @Override
    public List<OntoResponse> getNodesByIdToPath(Integer id) {
        log.debug("Fetching path nodes for node id: {}", id);

        var node = dataOnt.getNodeByID(id)
                .orElseThrow(() -> {
                    log.error("Node not found with id: {}", id);
                    return new IllegalArgumentException("Node not found: " + id);
                });

        List<Node> result = dataOnt.getNodesLinkedTo(node, LINK_PATH_OF);
        log.debug("Found {} path nodes for node id: {}", result.size(), id);

        return result.stream()
                .map(this::mapNodeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets nodes linked via "is_a" relation to a given node.
     * Represents type/hierarchy relations.
     *
     * @param id the source node ID
     * @return list of nodes in the is_a relationship
     */
    @Override
    public List<OntoResponse> getNodesByIdToIs(Integer id) {
        log.debug("Fetching is_a nodes for node id: {}", id);

        var node = dataOnt.getNodeByID(id)
                .orElseThrow(() -> {
                    log.error("Node not found with id: {}", id);
                    return new IllegalArgumentException("Node not found: " + id);
                });

        List<Node> result = dataOnt.getNodesLinkedTo(node, LINK_IS_A);
        log.debug("Found {} is_a nodes for node id: {}", result.size(), id);

        return result.stream()
                .map(this::mapNodeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets nodes linked FROM a given node via "position" relation.
     * Represents positional relations.
     *
     * @param id the source node ID
     * @return list of nodes with position relations
     */
    @Override
    public List<OntoResponse> getNodesByIdFrom(Integer id) {
        log.debug("Fetching position nodes from node id: {}", id);

        var node = dataOnt.getNodeByID(id)
                .orElseThrow(() -> {
                    log.error("Node not found with id: {}", id);
                    return new IllegalArgumentException("Node not found: " + id);
                });

        List<Node> nodes = dataOnt.getNodesLinkedFrom(node, LINK_POSITION);
        log.debug("Found {} position nodes from node id: {}", nodes.size(), id);

        return nodes.stream()
                .map(this::mapNodeToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Node domain model to OntoResponse DTO.
     * Extracts key attributes for API response.
     *
     * @param node the node to map
     * @return OntoResponse DTO
     */
    private OntoResponse mapNodeToResponse(Node node) {
        String finalizedAttr = node.getUniqueAttribute(ATTR_FINALIZED)
                .orElse(null);

        return new OntoResponse(
                node.getId(),
                node.getName(),
                node.getStorage(),
                finalizedAttr
        );
    }
}