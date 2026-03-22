package ru.sg.email.listener

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.sg.email.dto.event.UserRegisteredEvent
import ru.sg.email.service.impl.EmailServiceImpl

@Component
class UserEventListener(
    val emailService: EmailServiceImpl
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserEventListener::class.java)
    }

    @KafkaListener(
        topics = [$$"${users.kafka.topic.user-registered:user-events}"],
        groupId = $$"${users.kafka.groupId:user}"
    )
    fun listenUserService(userRegisteredEvent: UserRegisteredEvent) {
        log.info("Received user registration event: $userRegisteredEvent")

        emailService.sendVerificationEmail(userRegisteredEvent)
    }
}