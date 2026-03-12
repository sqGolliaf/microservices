package ru.sg.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sg.order.entity.DLQEvent;

import java.time.Instant;
import java.util.List;

@Repository
public interface DLQEventRepository extends JpaRepository<DLQEvent, String> {

    List<DLQEvent> findByIsResolvedFalseOrderByCreatedAtAsc();

    List<DLQEvent> findByCreatedAtBeforeAndIsResolvedTrue(Instant before);
}
