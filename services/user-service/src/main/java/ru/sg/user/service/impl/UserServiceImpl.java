package ru.sg.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.user.dto.request.event.RegistrationRequest;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;
import ru.sg.user.event.UserRegisteredEvent;
import ru.sg.user.exception.UserNotFoundException;
import ru.sg.user.exception.UserNotPositiveAmount;
import ru.sg.user.mapper.UserMapper;
import ru.sg.user.repository.UserRepository;
import ru.sg.user.service.KeycloakService;
import ru.sg.user.service.UserService;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.user-registered}")
    private String userRegisteredTopic;

    @Override
    public UserResponse register(RegistrationRequest request) {
        UserRepresentation keycloakUser = keycloakService.registerUser(request);

        User user = User.builder()
                .keycloakId(keycloakUser.getId())
                .balance(BigDecimal.ZERO)
                .tier("STANDARD")
                .build();

        User savedUser = userRepository.save(user);

        keycloakService.sendVerifyEmail(keycloakUser.getId());

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .keycloakId(keycloakUser.getId())
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .registeredAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        kafkaTemplate.send(userRegisteredTopic, event);
        log.info("User registered successfully: {} (keycloakId: {})",
                request.getUsername(), keycloakUser.getId());

        return userMapper.toResponse(savedUser, keycloakUser.getEmail(),  keycloakUser.getUsername());
    }

    @Override
    public String login(String username, String password) {
        return keycloakService.login(username, password);
    }

    @Override
    public UserResponse getCurrentUser(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserRepresentation keycloakUser = keycloakService.getUserById(keycloakId);

        return userMapper.toResponse(user, keycloakUser.getEmail(), keycloakUser.getUsername());
    }

    @Transactional
    @Override
    public void debit(String keycloakId, BigDecimal amount) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found, "));

        if (user.getBalance().compareTo(amount) < 0) throw new UserNotPositiveAmount("User " + keycloakId + " has insufficient balance");
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        log.info("Debited {} from user {}, new balance: {}", amount, keycloakId, user.getBalance());
    }

    @Transactional
    @Override
    public void credit(String keycloakId, BigDecimal amount) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User with keycloakId " + keycloakId + " not found, "));

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        log.info("Credited {} to user {}, new balance: {}", amount, keycloakId, user.getBalance());
    }
}
