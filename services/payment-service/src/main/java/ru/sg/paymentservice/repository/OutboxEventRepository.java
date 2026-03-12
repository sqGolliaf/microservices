package ru.sg.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sg.paymentservice.entity.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query(value = """
            SELECT *
            FROM t_outbox_event
            WHERE status = 'CREATED'
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findBatchForPublish(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE OutboxEvent e set e.status = 'SENT', e.errorMessage = null where e.id = :id")
    void markAsSent(@Param("id") UUID id);

    @Modifying
    @Query("""
                    update OutboxEvent e
                    set e.status = 'ERROR',
                        e.retryCount = e.retryCount - 1,
                        e.nextRetryAt = :nextRetry,
                        e.errorMessage = :error
                    where e.id = :id
            """)
    void markAsError(UUID id, Instant instant, String truncate);

    @Modifying
    @Query("update OutboxEvent e set e.status = 'FAILED' where e.id = :id")
    void markAsFailed(@Param("id") UUID id);
}
