package ru.sg.email.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import org.springframework.data.annotation.Id
import ru.sg.email.entity.enums.Status
import java.time.Instant

@Entity
@Table(name = "t_email")
data class Email(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    val id: Long? = null,

    @Column(name = "email", nullable = false)
    val email: String = "",

    @Column(name = "keycloak_id", nullable = false)
    val keycloakId: String = "",

    @Column(name = "verification_token", nullable = false, unique = true)
    val verificationToken: String = "",

    @Column(name = "status", nullable = false)
    var status: Status = Status.PENDING,

    @Column(name = "sent_at", nullable = false)
    var sentAt: Instant = Instant.now(),

    @Column(name = "verified_at")
    var verifiedAt: Instant? = null,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    @Column(name = "mailgun_message_id")
    var mailgunMessageId: String? = null,

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(email = $email , id = $id , keycloakId = $keycloakId , verificationToken = $verificationToken , sentAt = $sentAt , status = $status , verifiedAt = $verifiedAt , mailgunMessageId = $mailgunMessageId , retryCount = $retryCount , errorMessage = $errorMessage , createdAt = $createdAt , updatedAt = $updatedAt )"
    }
}