package ru.sg.back.services;

import ru.sg.back.dto.OntoResponse;
import java.util.List;

public interface OntoService {
    List<OntoResponse> nodes();
    List<OntoResponse> firstNodeByName(String name);
    List<OntoResponse> getNodesByIdToPath(Integer id);
    List<OntoResponse> getNodesByIdToIs(Integer id);
    List<OntoResponse> getNodesByIdFrom(Integer id);
}
