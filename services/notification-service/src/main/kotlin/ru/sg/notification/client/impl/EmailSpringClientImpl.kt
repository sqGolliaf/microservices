package ru.sg.notification.client.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import ru.sg.notification.client.EmailClient

@Component
class EmailSpringClientImpl(
    private val javaMailSender: JavaMailSender,
    @Value($$"${spring.mail.from}") private val fromEmail: String
) : EmailClient {

    override fun sendVerificationEmail(
        toEmail: String,
        firstName: String,
        verificationToken: String,
        verificationUrl: String
    ): String {
        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(toEmail)
        helper.setSubject("Verify Your Email")

        val htmlTemplate = buildHtmlTemplate(firstName, "$verificationUrl?token=$verificationToken")
        helper.setText(htmlTemplate, true)

        javaMailSender.send(message)
        return "Email sent successfully"
    }

    private fun buildHtmlTemplate(firstName: String, verificationUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h1>Welcome, $firstName!</h1>
                <p>Please verify your email by clicking the link below:</p>
                <a href="$verificationUrl" style="...">Verify Email</a>
                <p>This link will expire in 24 hours.</p>
            </body>
            </html>
        """.trimIndent()
    }
}