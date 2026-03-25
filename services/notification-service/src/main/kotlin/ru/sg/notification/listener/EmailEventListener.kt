package ru.sg.notification.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.sg.notification.dto.event.EmailEvent
import ru.sg.notification.service.impl.EmailServiceImpl

@Component
class EmailEventListener(
    private val emailService: EmailServiceImpl,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(EmailEventListener::class.java)
    }

    @KafkaListener(
        topics = [$$"${email.kafka.topic.email-registered:email-events}"],
        groupId = $$"${email.kafka.group-id:emails}"
    )
    fun listenEmailService(message: String) {
        log.info("Received email status event: $message")

        val emailActivateEvent = objectMapper.readValue(message, EmailEvent::class.java)

        emailService.changeStatus(emailActivateEvent)
    }
}