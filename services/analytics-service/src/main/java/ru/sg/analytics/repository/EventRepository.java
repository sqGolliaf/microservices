package ru.sg.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sg.analytics.model.EventStatistic;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<EventStatistic, Long> {

    @Query("""
        select e from EventStatistic e
        where e.createdAt >= :since
        group by e.eventType
        order by e.createdAt DESC
        """)
    List<EventStatistic> findEventsSince(Instant since);

    @Query("""
        select e from EventStatistic e
        where e.eventType = :eventType
        """)
    List<EventStatistic> findByEventType(String eventType);
}
