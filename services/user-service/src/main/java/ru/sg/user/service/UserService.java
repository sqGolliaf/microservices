package ru.sg.user.service;

import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;

import java.math.BigDecimal;

public interface UserService {

    UserResponse createIfNotExists(String keycloakId, String email, String username);

    User getUser(Long userId);

    UserResponse getByKeycloakId(String keycloakId, String email, String username);

    void debit(String keycloakId, BigDecimal amount);

    void credit(String keycloakId, BigDecimal amount);
}
