package ru.sg.email.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.sg.email.client.MailgunEmailClient
import ru.sg.email.repository.EmailRepository

@Service
@Transactional
class EmailService(
    private val emailRepository: EmailRepository,
    private val mailgunClient: MailgunEmailClient,
    @Value($$"${app.verification-url}") private val verificationUrl: String,
    @Value($$"${app.max-retries}") private val maxRetries: Int
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EmailService::class.java)
    }
}