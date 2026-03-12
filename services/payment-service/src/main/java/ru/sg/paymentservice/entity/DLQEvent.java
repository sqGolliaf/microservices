package ru.sg.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "t_dlq_event")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class DLQEvent {

    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;

    @Column(name = "saga_id", length = 36)
    private String sagaId;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(name = "original_message", nullable = false, columnDefinition = "TEXT")
    private String originalMessage;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_trace", columnDefinition = "TEXT")
    private String errorTrace;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_retry_at")
    private Instant lastRetryAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
