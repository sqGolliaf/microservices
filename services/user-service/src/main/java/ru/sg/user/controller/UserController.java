package ru.sg.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sg.user.dto.request.CreditRequest;
import ru.sg.user.dto.request.DebitRequest;
import ru.sg.user.dto.request.event.LoginRequest;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.service.UserService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegistrationRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader("X-TOKEN") String tokenKeycloakId) {
        UserResponse currentUser = userService.getCurrentUser(tokenKeycloakId);
        return ResponseEntity.ok(currentUser);
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
