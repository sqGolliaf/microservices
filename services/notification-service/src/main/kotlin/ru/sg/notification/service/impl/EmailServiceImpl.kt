package ru.sg.notification.service.impl

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.sg.notification.client.EmailClient
import ru.sg.notification.dto.event.EmailEvent
import ru.sg.notification.dto.event.UserRegisteredEvent
import ru.sg.notification.entity.Email
import ru.sg.notification.entity.enums.Status
import ru.sg.notification.exception.EmailException
import ru.sg.notification.repository.EmailRepository
import ru.sg.notification.service.EmailService
import java.time.Instant

@Service
@Transactional
class EmailServiceImpl(
    private val emailRepository: EmailRepository,
    private val emailClient: EmailClient,
    @Value($$"${email.verification-url}") private val verificationUrl: String
) : EmailService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EmailServiceImpl::class.java)
    }

    @Async("emailExecutor")
    override fun sendVerificationEmail(userRegisteredEvent: UserRegisteredEvent) {
        val email = Email(
            email = userRegisteredEvent.email,
            keycloakId = userRegisteredEvent.keycloakId,
            verificationToken = userRegisteredEvent.verificationToken,
            status = Status.PENDING,
            sentAt = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        emailRepository.save(email)

        try {
            emailClient.sendVerificationEmail(
                toEmail = userRegisteredEvent.email,
                firstName = userRegisteredEvent.firstName,
                verificationToken = userRegisteredEvent.verificationToken,
                verificationUrl = verificationUrl
            )

            email.status = Status.SENT
            email.updatedAt = Instant.now()
            emailRepository.save(email)
            logger.info("Verification email sent successfully to: ${userRegisteredEvent.email}")
        } catch (e: Exception) {
            email.status = Status.FAILED
            email.errorMessage = e.message
            email.retryCount++
            email.updatedAt = Instant.now()
            emailRepository.save(email)
            logger.error("Failed to send verification email to: ${userRegisteredEvent.email}", e)
        }
    }

    override fun changeStatus(emailEvent: EmailEvent) {
        val findByEmail = emailRepository.findByEmail(emailEvent.email)
            .orElseThrow { EmailException("Not find email ${emailEvent.email}") }

        findByEmail.status = Status.VERIFIED
        val save = emailRepository.save(findByEmail)
        logger.info("Email is verified successfully to: ${save.email}")
    }
}