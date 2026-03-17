package ru.sg.user.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.exception.RegistrationException;
import ru.sg.user.exception.UserAlreadyExistsException;
import ru.sg.user.service.KeycloakService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Override
    public UserRepresentation registerUserInKeycloak(RegistrationRequest request) throws RegistrationException {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);

            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> existing = usersResource.search(request.getUsername(), 0, 1);
            if (!existing.isEmpty()) throw new UserAlreadyExistsException("Username already exists");

            Response response = usersResource.create(user);
            if (response.getStatus() != 201) throw new RegistrationException("Failed to create user in Keycloak");

            String keycloakId = CreatedResponseUtil.getCreatedId(response);

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(request.getPassword());
            passwordCred.setTemporary(false);

            UserResource userResource = usersResource.get(keycloakId);
            userResource.resetPassword(passwordCred);

            log.info("User registered in Keycloak: {} (id: {})", request.getUsername(), keycloakId);
            return userResource.toRepresentation();
        } catch (Exception e) {
            log.error("Error registering user in Keycloak", e);
            throw new RegistrationException(e.getMessage());
        }
    }

    @Override
    public boolean validateUserCredentials(String username, String password) {
        try {
            Keycloak userKeycloak = Keycloak.getInstance(
                    serverUrl,
                    realm,
                    username,
                    password,
                    "frontend-client"
            );

            return true;
        } catch (Exception e) {
            log.debug("Authentication failed for user: {}", username);
            return false;
        }
    }
}
