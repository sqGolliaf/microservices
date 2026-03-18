package ru.sg.user.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.exception.RegistrationException;
import ru.sg.user.exception.UserAlreadyExistsException;
import ru.sg.user.exception.UserInvalidCredentialsException;
import ru.sg.user.exception.UserNotFoundException;
import ru.sg.user.service.KeycloakService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    private UsersResource users() {
        return keycloak.realm(realm).users();
    }

    @Override
    public UserRepresentation registerUser(RegistrationRequest request) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false);

            List<UserRepresentation> byUsername = users().search(request.getUsername(), 0, 1);
            List<UserRepresentation> byEmail = users().searchByEmail(request.getEmail(), true);
            if (!byUsername.isEmpty() || !byEmail.isEmpty())
                throw new UserAlreadyExistsException("User already exists");

            Response response = users().create(user);
            if (response.getStatus() != 201) throw new RegistrationException("Failed to create user in Keycloak");

            String keycloakId = CreatedResponseUtil.getCreatedId(response);

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(request.getPassword());
            passwordCred.setTemporary(false);

            users().get(keycloakId).resetPassword(passwordCred);

            log.info("User registered in Keycloak: {} (id: {})", user.getUsername(), keycloakId);
            user.setId(keycloakId);
            return user;
        } catch (Exception e) {
            log.error("Error registering user in Keycloak", e);
            throw new RegistrationException(e.getMessage());
        }
    }

    @Override
    public String login(String username, String password) {
        try {
            return keycloak.tokenManager().getAccessTokenString();
        } catch (Exception e) {
            throw new UserInvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    public void sendVerifyEmail(String userId) {
        try {
            users().get(userId).sendVerifyEmail();
            log.info("Verification email sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
        }
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        try {
            return users().get(userId).toRepresentation();
        } catch (Exception e) {
            throw new UserNotFoundException("User not found in Keycloak");
        }
    }
}
