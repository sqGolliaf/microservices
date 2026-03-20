package ru.sg.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "t_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 36)
    private String keycloakId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "tier", nullable = false, length = 50)
    @Builder.Default
    private String tier = "STANDARD";

    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;

    @Column(name = "create_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "update_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
