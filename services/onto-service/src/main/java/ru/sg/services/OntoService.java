package ru.sg.services;

import ru.sg.dto.OntoResponse;
import java.util.List;

public interface OntoService {
    List<OntoResponse> nodes();
    List<OntoResponse> firstNodeByName(String name);
    List<OntoResponse> getNodesByIdToPath(Integer id);
    List<OntoResponse> getNodesByIdToIs(Integer id);
    List<OntoResponse> getNodesByIdFrom(Integer id);
}
