package ru.sg.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sg.paymentservice.entity.DLQEvent;

@Repository
public interface DLQEventRepository extends JpaRepository<DLQEvent, String> {
}
