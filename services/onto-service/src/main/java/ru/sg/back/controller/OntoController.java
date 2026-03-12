package ru.sg.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sg.back.dto.OntoResponse;
import ru.sg.back.services.OntoService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class OntoController {

    private final OntoService ontoService;

    @GetMapping("/")
    public ResponseEntity<List<OntoResponse>> getFirstNode() {
        List<OntoResponse> ontoList = ontoService.firstNodeByName("#Старт");
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
