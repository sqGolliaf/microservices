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

    @Named("mapFinalize")
    default String mapFinalize(Node node) {
        if (node.getAttribute("finalize").isPresent()) {
            return node.getAttribute("finalize").get();
        }
        if (node.getAttribute("text").isPresent()) {
            return node.getAttribute("text").get();
        }
        return null;
    }
}
