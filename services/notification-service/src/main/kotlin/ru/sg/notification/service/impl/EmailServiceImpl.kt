package ru.sg.notification.service.impl

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sg.notification.client.EmailClient
import ru.sg.notification.client.impl.EmailSpringClientImpl
import ru.sg.notification.dto.event.UserRegisteredEvent
import ru.sg.notification.entity.Email
import ru.sg.notification.entity.enums.Status
import ru.sg.notification.repository.EmailRepository
import ru.sg.notification.service.EmailService
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class EmailServiceImpl(
    private val emailRepository: EmailRepository,
    private val emailClient: EmailClient,
    @Value($$"${email.verification-url}") private val verificationUrl: String,
    @Value($$"${email.max-retries}") private val maxRetries: Int
) : EmailService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EmailServiceImpl::class.java)
    }

    override fun sendVerificationEmail(userRegisteredEvent: UserRegisteredEvent) {
        val token = UUID.randomUUID().toString()
        val now = Instant.now()

        val email = Email(
            email = userRegisteredEvent.email,
            keycloakId = userRegisteredEvent.keycloakId,
            verificationToken = token,
            status = Status.PENDING,
            sentAt = now,
            createdAt = now,
            updatedAt = now
        )

        emailRepository.save(email)

        try {
            emailClient.sendVerificationEmail(
                toEmail = userRegisteredEvent.email,
                firstName = userRegisteredEvent.firstName,
                verificationToken = token,
                verificationUrl = verificationUrl
            )

            email.status = Status.SENT
            email.updatedAt = Instant.now()
            emailRepository.save(email)
        } catch (e: Exception) {
            email.status = Status.FAILED
            email.errorMessage = e.message
            email.retryCount++
            email.updatedAt = Instant.now()
            emailRepository.save(email)
        }
    }
}