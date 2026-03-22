package ru.sg.email.client

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import com.mailgun.model.message.Message
import com.mailgun.model.message.MessageResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MailgunEmailClient(
    @Value($$"${mailgun.api-key}") private val apiKey: String,
    @Value($$"${mailgun.domain}") private val domain: String,
    @Value($$"${mailgun.from-email}") private val fromEmail: String
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MailgunEmailClient::class.java)
    }

    private val mg = MailgunClient.config(apiKey)
        .createApi(MailgunMessagesApi::class.java)


    fun sendVerificationEmail(
        toEmail: String,
        firstName: String,
        verificationToken: String,
        verificationUrl: String
    ): String {
        return try {
            val url = "$verificationUrl?token=$verificationToken"

            val message = Message.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject("Verify Your Email")
                    .html(buildHtmlTemplate(firstName, url))
                    .build()

            val response: MessageResponse = mg.sendMessage(domain, message)

            logger.info("Email sent successfully to $toEmail. Message ID: ${response.id}")
            response.id ?: throw Exception("Failed to send email - no message ID returned")
        } catch (e: Exception) {
            logger.error("Failed to send verification email to $toEmail: ${e.message}")
            throw Exception(e.message)
        }
    }

    private fun buildHtmlTemplate(firstName: String, verificationUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Welcome, $firstName!</h1>
                <p>Please verify your email by clicking the link below:</p>
                <a href="$verificationUrl">Verify Email</a>
            </body>
            </html>
        """.trimIndent()
    }
}