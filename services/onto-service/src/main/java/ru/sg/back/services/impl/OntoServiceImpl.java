package ru.sg.back.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sg.models.Node;
import ru.sg.models.Onto;
import ru.sg.back.dto.OntoResponse;
import ru.sg.back.services.OntoService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OntoServiceImpl implements OntoService {

    private final Onto dataOnt;

    @Override
    public List<OntoResponse> nodes() {
        return Arrays.stream(dataOnt.getNodes())
                .map(it -> new OntoResponse(
                        it.getID(),
                        it.getName(),
                        it.getStorage(),
                        it.getUniqueAttribute("finalyze")))
                .toList();
    }

    @Override
    public List<OntoResponse> firstNodeByName(String name) {
        Node firstNodeByName = dataOnt.getFirstNodeByName(name);
        ArrayList<Node> getNodesForStart = dataOnt.getNodesLinkedFrom(firstNodeByName, "");
        return getNodesForStart.stream()
                .map(it -> new OntoResponse(
                        it.getID(),
                        it.getName(),
                        it.getStorage(),
                        it.getUniqueAttribute("finalyze"))
                ).toList();
    }

    @Override
    public List<OntoResponse> getNodesByIdToPath(Integer id) {
        Node node = dataOnt.getNodeByID(id);
        ArrayList<Node> result = dataOnt.getNodesLinkedTo(node, "a_path_of");

        return result.stream()
                .map(it -> new OntoResponse(
                        it.getID(),
                        it.getName(),
                        it.getStorage(),
                        it.getUniqueAttribute("finalyze"))
                ).toList();
    }

    @Override
    public List<OntoResponse> getNodesByIdToIs(Integer id) {
        Node node = dataOnt.getNodeByID(id);
        ArrayList<Node> result = dataOnt.getNodesLinkedTo(node, "is_a");

        return result.stream()
                .map(it -> new OntoResponse(
                        it.getID(),
                        it.getName(),
                        it.getStorage(),
                        it.getUniqueAttribute("finalyze"))
                ).toList();
    }

    @Override
    public List<OntoResponse> getNodesByIdFrom(Integer id) {
        Node node = dataOnt.getNodeByID(id);
        ArrayList<Node> nodes = dataOnt.getNodesLinkedFrom(node, "position");

        return nodes.stream()
                .map(it -> new OntoResponse(
                        it.getID(),
                        it.getName(),
                        it.getStorage(),
                        it.getUniqueAttribute("finalyze"))
                ).toList();
    }
}
