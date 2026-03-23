package ru.sg.notification.client

interface EmailClient {
    fun sendVerificationEmail(
        toEmail: String, firstName: String, verificationToken: String, verificationUrl: String
    ): String
}