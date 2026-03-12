package ru.sg.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.user.dto.response.UserResponse;
import ru.sg.user.entity.User;
import ru.sg.user.exception.UserNotFoundException;
import ru.sg.user.exception.UserNotPositiveAmount;
import ru.sg.user.mapper.UserMapper;
import ru.sg.user.repository.UserRepository;
import ru.sg.user.service.UserService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
