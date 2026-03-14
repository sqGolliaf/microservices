package ru.sg.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sg.dto.OntoResponse;
import ru.sg.services.OntoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onto")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.onto.cors.allowed-origins:http://localhost:9000}")
public class OntoController {

    private final OntoService ontoService;

    @Value("${app.onto.start-node-name:#Старт}")
    private String startNodeName;

    @GetMapping
    public ResponseEntity<List<OntoResponse>> getStartNodeName() {
        List<OntoResponse> ontoList = ontoService.firstNodeByName(startNodeName);
        return new ResponseEntity<>(ontoList, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OntoResponse>> getNodes() {
        return new ResponseEntity<>(ontoService.nodes(), HttpStatus.OK);
    }

    @GetMapping("/to/{id}")
    public ResponseEntity<List<OntoResponse>> getNodesByIdTo(@PathVariable Integer id) {
        return new ResponseEntity<>(ontoService.getNodesByIdToIs(id), HttpStatus.OK);
    }

    @GetMapping("/path/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<OntoResponse> getNodesByIdPath(@PathVariable Integer id) {
        return ontoService.getNodesByIdToPath(id);
    }

    @GetMapping("/from/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<OntoResponse> getNodesByIdFrom(@PathVariable Integer id) {
        return ontoService.getNodesByIdFrom(id);
    }
}
