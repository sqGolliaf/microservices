package ru.sg.user.service;

import org.keycloak.representations.idm.UserRepresentation;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.exception.RegistrationException;

public interface KeycloakService {

    UserRepresentation registerUser(RegistrationRequest request) throws RegistrationException;
    void sendVerifyEmail(String userId);
    UserRepresentation getUserById(String userId);
    String login(String username, String password);
}
