package ru.sg.user.event;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    @PrePersist
    protected void onCreate() {
        registeredAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdated() {
        updatedAt = Instant.now();
    }
}
