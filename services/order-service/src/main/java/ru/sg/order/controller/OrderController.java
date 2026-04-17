package ru.sg.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> saveOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-USER-ID") @NotBlank String keycloakId,
            @RequestHeader("X-EMAIL") @Email String email,
            @RequestHeader("X-USERNAME") @NotBlank String username) {
        log.info("Creating order: {}", request.name());
        OrderResponse response = orderService.createOrder(request, keycloakId, email, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
