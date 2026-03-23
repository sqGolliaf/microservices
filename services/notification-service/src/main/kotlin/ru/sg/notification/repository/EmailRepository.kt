package ru.sg.notification.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sg.notification.entity.Email
import java.util.Optional

@Repository
interface EmailRepository: JpaRepository<Email, Long> {
    fun findByVerificationToken(token: String): Optional<Email>
    fun findByEmail(email: String): Optional<Email>
    fun findByKeycloakId(keycloakId: String): Optional<Email>
}