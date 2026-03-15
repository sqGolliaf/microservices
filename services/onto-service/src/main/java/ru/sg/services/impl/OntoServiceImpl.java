package ru.sg.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sg.dto.OntoResponse;
import ru.sg.model.Node;
import ru.sg.model.Onto;
import ru.sg.services.OntoService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OntoServiceImpl implements OntoService {

    private static final String ATTR_FINALIZED = "finalyze";
    private static final String LINK_IS_A = "is_a";
    private static final String LINK_PATH_OF = "a_path_of";
    private static final String LINK_POSITION = "position";

    private final Onto dataOnt;

    @Override
    public List<OntoResponse> nodes() {
        log.debug("Fetching all nodes");
        return dataOnt.getNodes().stream()
                .map(this::mapNodeToResponse)
                .collect(Collectors.toList());
    }

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