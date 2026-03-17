package ru.sg.user.service;

import org.keycloak.representations.idm.UserRepresentation;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.exception.RegistrationException;

public interface KeycloakService {

    UserRepresentation registerUserInKeycloak(RegistrationRequest request) throws RegistrationException;
    boolean validateUserCredentials(String username, String password);

}
