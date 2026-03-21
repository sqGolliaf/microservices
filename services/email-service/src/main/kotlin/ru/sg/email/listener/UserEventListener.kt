package ru.sg.email.listener

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class UserEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(UserEventListener::class.java)
    }

    @KafkaListener(
        topics = [$$"${users.kafka.topic.user-registered:user-events}"],
        groupId = $$"${users.kafka.groupId:user}"
    )
    fun listenUserService(consumerRecord: ConsumerRecord<String, String>) {
        val userEvent = consumerRecord.value()
        log.info("Received user registration event: $userEvent")
    }
}