package ru.sg.user.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sg.user.entity.User;
import ru.sg.user.repository.UserRepository;
import ru.sg.user.service.KeycloakService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        List<User> expiredUsers = userRepository.findByTokenExpiryDateBefore(LocalDateTime.now().minusDays(1));
        if (expiredUsers.isEmpty()) {
            log.info("No users with tokens expired more than 24 hours ago found");
            return;
        }
        userRepository.deleteAllInBatch(expiredUsers);

        for (User user : expiredUsers) {
            keycloakService.deleteUser(user.getKeycloakId());
        }
    }
}
