package ru.sg.user.service;

import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;

import java.math.BigDecimal;

public interface UserService {

    UserResponse register(RegistrationRequest request);

    UserResponse getCurrentUser(String keycloakId);

    void verifyEmail(String token);

    void debit(String keycloakId, BigDecimal amount);

    void credit(String keycloakId, BigDecimal amount);
}
