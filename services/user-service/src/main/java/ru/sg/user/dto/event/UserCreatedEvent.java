package ru.sg.user.dto.event;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email,
        String verificationToken
) {
}
