package ru.sg.user.service;

import org.apache.http.auth.InvalidCredentialsException;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;
import ru.sg.user.exception.EmailAlreadyExistsException;

import java.math.BigDecimal;

public interface UserService {

    UserResponse register(RegistrationRequest request) throws EmailAlreadyExistsException;

    String authenticate(String username, String password) throws InvalidCredentialsException;

    UserResponse createIfNotExists(String keycloakId, String email, String username);

    User getUser(Long userId);

    UserResponse getByKeycloakId(String keycloakId, String email, String username);

    void debit(String keycloakId, BigDecimal amount);

    void credit(String keycloakId, BigDecimal amount);
}
