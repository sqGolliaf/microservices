package ru.sg.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sg.dto.OntoResponse;
import ru.sg.model.Node;

@Mapper(componentModel = "spring")
public interface OntoMapper {

    @Mapping(target = "text", source = "node", qualifiedByName = "mapName")
    @Mapping(target = "attribute", source = "node", qualifiedByName = "mapFinalyze")
    OntoResponse toDto(Node node);

    @Named("mapName")
    default String mapName(Node node) {
        return node.name();
    }

    @Named("mapFinalyze")
    default String mapFinalyze(Node node) {
        if (node.getAttribute("finalyze") != null) {
            return node.getAttribute("finalyze");
        }
        if (node.getAttribute("text") != null) {
            return node.getAttribute("text");
        }
        return null; // или пустая строка ""
    }
}
