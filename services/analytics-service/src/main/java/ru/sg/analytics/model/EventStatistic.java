package ru.sg.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "t_event_statistic", indexes = {
        @Index(name = "idx_event_type", columnList = "event-type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class EventStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @ElementCollection
    @CollectionTable(name = "t_event_data", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> eventData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> objectEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != objectEffectiveClass) {
            return false;
        }
        EventStatistic that = (EventStatistic) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}