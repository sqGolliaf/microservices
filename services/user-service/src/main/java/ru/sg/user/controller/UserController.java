package ru.sg.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sg.user.dto.request.CreditRequest;
import ru.sg.user.dto.request.DebitRequest;
import ru.sg.user.dto.request.TokenResponse;
import ru.sg.user.dto.request.event.LoginRequest;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegistrationRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok("Email is verified");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader("X-USER-ID") String keycloakId) {
        return ResponseEntity.ok(userService.getCurrentUser(keycloakId));
    }

    @PostMapping("/debit")
    public void debit(
            @RequestHeader("X-USER-ID") String keycloakId,
            @RequestBody DebitRequest request
    ) {
        userService.debit(keycloakId, request.amount());
    }

    @PostMapping("/credit")
    public void credit(
            @RequestHeader("X-USER-ID") String keycloakId,
            @RequestBody CreditRequest request
    ) {
        userService.credit(keycloakId, request.amount());
    }
}
