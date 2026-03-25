package ru.sg.notification.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
class MailConfig {

    @Value($$"${spring.mail.host:localhost}")
    private lateinit var mailHost: String

    @Value($$"${spring.mail.port:1025}")
    private var mailPort: Int = 0

    @Value($$"${spring.mail.username:}")
    private var mailUsername: String = ""

    @Value($$"${spring.mail.password:}")
    private var mailPassword: String = ""

    @Profile("dev")
    @Bean
    fun javaMailSenderDev(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailHost
        mailSender.port = mailPort
        mailSender.username = mailUsername
        mailSender.password = mailPassword

        val props: Properties = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "false"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.starttls.required"] = "false"
        props["mail.smtp.connectiontimeout"] = "5000"
        props["mail.smtp.timeout"] = "5000"
        props["mail.smtp.writetimeout"] = "5000"

        return mailSender
    }

    @Profile("prod")
    @Bean
    fun javaMailSenderProd(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailHost
        mailSender.port = mailPort
        mailSender.username = mailUsername
        mailSender.password = mailPassword

        val props: Properties = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.starttls.required"] = "false"
        props["mail.smtp.connectiontimeout"] = "5000"
        props["mail.smtp.timeout"] = "5000"
        props["mail.smtp.writetimeout"] = "5000"

        return mailSender
    }
}