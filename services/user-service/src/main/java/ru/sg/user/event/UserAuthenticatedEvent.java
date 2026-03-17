package ru.sg.user.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuthenticatedEvent {
    private String keycloakId;
    private String username;
    private String email;
    private Instant authenticatedAt;
}
