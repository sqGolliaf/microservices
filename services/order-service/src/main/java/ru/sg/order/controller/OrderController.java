package ru.sg.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sg.order.dto.request.CreateOrderRequest;
import ru.sg.order.dto.response.OrderResponse;
import ru.sg.order.service.OrderService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> saveOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-USER-ID") String keycloakId,
            @RequestHeader("X-EMAIL") String email,
            @RequestHeader("X-USERNAME") String username) {
        log.info("Creating order: {}", request.name());
        OrderResponse response = orderService.createOrder(request, keycloakId, email, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
