package ru.sg.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sg.user.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakId(String keycloakId);

    @Query("select u from User u where u.verificationToken = :verificationToken")
    Optional<User> findByVerificationToken(@Param("verificationToken") String verificationToken);

    List<User> findByTokenExpireDateBefore(Instant dateTime);
}
