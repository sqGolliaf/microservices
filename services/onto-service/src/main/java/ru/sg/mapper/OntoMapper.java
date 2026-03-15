package ru.sg.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sg.dto.OntoResponse;
import ru.sg.model.Node;

@Mapper(componentModel = "spring")
public interface OntoMapper {

    @Mapping(target = "name", source = "node", qualifiedByName = "mapName")
    @Mapping(target = "attribute", source = "node", qualifiedByName = "mapFinalyze")
    OntoResponse toDto(Node node);

    @Named("mapName")
    default String mapName(Node node) {
        return node.getName();
    }

    @Named("mapFinalyze")
    default String mapFinalyze(Node node) {
        if (node.getAttribute("finalyze").isPresent()) {
            return node.getAttribute("finalyze").get();
        }
        if (node.getAttribute("text").isPresent()) {
            return node.getAttribute("text").get();
        }
        return null;
    }
}
