package ru.sg.user.service;

import org.keycloak.representations.idm.UserRepresentation;
import ru.sg.user.dto.request.RegistrationRequest;
import ru.sg.user.exception.RegistrationException;

public interface KeycloakService {

    UserRepresentation registerUser(RegistrationRequest request) throws RegistrationException;
    void sendVerifyEmail(String userId);
    UserRepresentation getUserById(String userId);
    void deleteUser(String userId);

    void enableUser(String keycloakId);

    void setEmailVerified(String keycloakId, boolean active);
}
