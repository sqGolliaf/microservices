package ru.sg.notification.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.sg.notification.dto.event.UserRegisteredEvent
import ru.sg.notification.service.impl.EmailServiceImpl

@Component
class UserEventListener(
    private val emailService: EmailServiceImpl,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserEventListener::class.java)
    }

    @KafkaListener(
        topics = [$$"${users.kafka.topic.user-registered:user-events}"],
        groupId = $$"${users.kafka.groupId:user}"
    )
    fun listenUserService(message: String) {
        log.info("Received user registration event: $message")

        val userRegisteredEvent = objectMapper.readValue(message, UserRegisteredEvent::class.java)

        emailService.sendVerificationEmail(userRegisteredEvent)
    }
}