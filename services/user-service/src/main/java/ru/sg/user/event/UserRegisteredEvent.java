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
public class UserRegisteredEvent {
    private String keycloakId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Instant registeredAt;
    private Instant updatedAt;
}
