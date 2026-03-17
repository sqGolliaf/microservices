package ru.sg.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;
import ru.sg.user.event.UserAuthenticatedEvent;
import ru.sg.user.event.UserRegisteredEvent;
import ru.sg.user.exception.EmailAlreadyExistsException;
import ru.sg.user.exception.RegistrationException;
import ru.sg.user.exception.UserNotFoundException;
import ru.sg.user.exception.UserNotPositiveAmount;
import ru.sg.user.mapper.UserMapper;
import ru.sg.user.repository.UserRepository;
import ru.sg.user.service.KeycloakService;
import ru.sg.user.service.UserService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${spring.kafka.topics.user-registered}")
    private String userRegisteredTopic;

    @Override
    public UserResponse register(RegistrationRequest request) throws EmailAlreadyExistsException {
        if (userRepository.existsByEmail(request.getEmail())) throw new EmailAlreadyExistsException("Email alreadyExists");

        UserRepresentation keycloakUser = keycloakService.registerUserInKeycloak(request);

        User user = User.builder()
                .keycloakId(keycloakUser.getId())
                .balance(BigDecimal.ZERO)
                .tier("STANDARD")
                .build();

        User savedUser = userRepository.save(user);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .keycloakId(keycloakUser.getId())
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        kafkaTemplate.send(userRegisteredTopic, event);
        log.info("User registered successfully: {} (keycloakId: {})",
                request.getUsername(), keycloakUser.getId());

        return userMapper.toResponse(savedUser, request.getEmail(), request.getUsername(), keycloakUser.getId());
    }

    public String authenticate(String username, String password) throws InvalidCredentialsException {
        boolean isValid = keycloakService.validateUserCredentials(username, password);
        if (!isValid) throw new InvalidCredentialsException("Invalid username or password");

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        UserAuthenticatedEvent authEvent = UserAuthenticatedEvent.builder()
                .keycloakId(user.getKeycloakId())
                .username(username)
                .email(user.getEmail())
                .authenticatedAt(Instant.now())
                .build();

        eventPublisher.publishEvent(authEvent);
        log.info("User authenticated: {}", username);

        return user.getKeycloakId();
    }

    @Override
    public UserResponse createIfNotExists(
            String keycloakId,
            String email,
            String username
    ) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    log.info("Creating new user: keycloakId={}, email={}", keycloakId, email);
                    User saveUser = User.builder()
                            .keycloakId(keycloakId)
                            .balance(BigDecimal.ZERO)
                            .tier("STANDARD")
                            .build();

                    return userRepository.save(saveUser);
                });

        return userMapper.toResponse(user, email, username, keycloakId);
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
    }

    @Override
    public UserResponse getByKeycloakId(
            String keycloakId,
            String email,
            String username
    ) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found"));

        return userMapper.toResponse(user, email, username, keycloakId);
    }

    @Override
    public void debit(String keycloakId, BigDecimal amount) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found, "));

        if (user.getBalance().compareTo(amount) < 0) throw new UserNotPositiveAmount("User " + keycloakId + " has insufficient balance");
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        log.info("Debited {} from user {}, new balance: {}", amount, keycloakId, user.getBalance());
    }

    @Override
    public void credit(String keycloakId, BigDecimal amount) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found, "));

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        log.info("Credited {} to user {}, new balance: {}", amount, keycloakId, user.getBalance());
    }
}
