package ru.sg.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.user.dto.request.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;
import ru.sg.user.event.EmailEvent;
import ru.sg.user.event.UserRegisteredEvent;
import ru.sg.user.event.enums.EmailStatus;
import ru.sg.user.exception.TokenExpiredException;
import ru.sg.user.exception.UserNotFoundException;
import ru.sg.user.exception.UserNotPositiveAmount;
import ru.sg.user.mapper.UserMapper;
import ru.sg.user.repository.UserRepository;
import ru.sg.user.service.KeycloakService;
import ru.sg.user.service.UserService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper jsonMapper;

    @Value("${spring.kafka.topics.user-registered}")
    private String topicUser;

    @Value("${spring.kafka.topics.email-verified}")
    private String topicEmail;

    @Override
    public UserResponse register(RegistrationRequest request) {
        UserRepresentation keycloakUser = null;

        try {
            keycloakUser = keycloakService.registerUser(request);

            String token = UUID.randomUUID().toString();

            User user = User.builder()
                    .keycloakId(keycloakUser.getId())
                    .balance(BigDecimal.ZERO)
                    .verificationToken(token)
                    .tokenExpireDate(Instant.now().plus(1, ChronoUnit.DAYS))
                    .tier("STANDARD")
                    .build();

            User savedUser = userRepository.save(user);

            sendEvent(keycloakUser, user);

            log.info("User registered successfully: {} (keycloakId: {})",
                    request.getUsername(), keycloakUser.getId());

            return userMapper.toResponse(savedUser, keycloakUser.isEnabled(), keycloakUser.getEmail(), keycloakUser.getUsername());
        } catch (Exception e) {
            if (keycloakUser != null) keycloakService.deleteUser(keycloakUser.getId());
            log.error("User is not save, exception: ", e);
            throw e;
        }
    }

    @Cacheable(value = "users", key = "#keycloakId")
    @Override
    public UserResponse getCurrentUser(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserRepresentation keycloakUser = keycloakService.getUserById(keycloakId);

        return userMapper.toResponse(user, keycloakUser.isEnabled(), keycloakUser.getEmail(), keycloakUser.getUsername());
    }

    @Transactional
    @Override
    public void debit(String keycloakId, BigDecimal amount) {
        validateAmount(amount);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found, "));

        if (user.getBalance().compareTo(amount) < 0)
            throw new UserNotPositiveAmount("User " + keycloakId + " has insufficient balance");
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        log.info("Debited {} from user {}, new balance: {}", amount, keycloakId, user.getBalance());
    }

    @Transactional
    @Override
    public void credit(String keycloakId, BigDecimal amount) {
        validateAmount(amount);

        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found, "));

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        log.info("Credited {} to user {}, new balance: {}", amount, keycloakId, user.getBalance());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new TokenExpiredException("Invalid token"));

        if (user.getTokenExpireDate().isBefore(Instant.now())) throw new TokenExpiredException("Token expired");

        user.setVerificationToken(null);
        User saveUser = userRepository.save(user);

        keycloakService.enableUser(user.getKeycloakId());
        keycloakService.setEmailVerified(user.getKeycloakId(), true);
        UserRepresentation userById = keycloakService.getUserById(user.getKeycloakId());

        sendEventEmail(userById, saveUser);
        log.info("Email is activated: {}", userById.getEmail());
    }

    private void sendEvent(UserRepresentation keycloakUser, User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .keycloakId(keycloakUser.getId())
                .username(keycloakUser.getUsername())
                .email(keycloakUser.getEmail())
                .verificationToken(user.getVerificationToken())
                .firstName(keycloakUser.getFirstName())
                .lastName(keycloakUser.getLastName())
                .registeredAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        try {
            String jsonUserRegisteredEvent = jsonMapper.writeValueAsString(event);

            // TODO Переделать на Outbox Pattern
            kafkaTemplate.send(topicUser, jsonUserRegisteredEvent).whenComplete((r, ex) -> {
                if (ex != null) {
                    log.error("Kafka send failed", ex);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendEventEmail(UserRepresentation keycloakUser, User user) {
        EmailEvent event = EmailEvent.builder()
                .id(user.getId())
                .email(keycloakUser.getEmail())
                .status(EmailStatus.VERIFIED)
                .build();

        try {
            String jsonEmailEvent = jsonMapper.writeValueAsString(event);
            kafkaTemplate.send(topicEmail, jsonEmailEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
