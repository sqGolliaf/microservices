package ru.sg.notification.service

import ru.sg.notification.dto.event.UserRegisteredEvent

interface EmailService {

    fun sendVerificationEmail(userRegisteredEvent: UserRegisteredEvent)
}