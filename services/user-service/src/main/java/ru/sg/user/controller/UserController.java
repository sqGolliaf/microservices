package ru.sg.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.sg.user.dto.request.CreditRequest;
import ru.sg.user.dto.request.DebitRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/me")
    public UserResponse registerUser(
            @RequestHeader("X-USER-ID") String keycloakId,
            @RequestHeader("X-EMAIL") String email,
            @RequestHeader("X-USERNAME") String username
    ) {
        log.info("Registering or getting user: keycloakId={}, email={}", keycloakId, email);
        return userService.createIfNotExists(keycloakId, email, username);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @RequestHeader("X-USER-ID") String keycloakId,
            @RequestHeader("X-EMAIL") String email,
            @RequestHeader("X-USERNAME") String username
    ) {
        log.info("Getting current user: keycloakId={}", keycloakId);
        return userService.getByKeycloakId(keycloakId, email, username);
    }

    @PostMapping("/{keycloakId}/debit")
    public void debit(
            @PathVariable String keycloakId,
            @RequestBody DebitRequest request
    ) {
        log.info("Debiting {} from user: {}", request.amount(), keycloakId);
        userService.debit(keycloakId, request.amount());
    }

    @PostMapping("/{keycloakId}/credit")
    public void credit(
            @PathVariable String keycloakId,
            @RequestBody CreditRequest request
    ) {
        log.info("Crediting {} to user: {}", request.amount(), keycloakId);
        userService.credit(keycloakId, request.amount());
    }
}
