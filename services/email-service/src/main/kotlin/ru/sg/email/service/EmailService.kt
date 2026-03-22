package ru.sg.email.service

import ru.sg.email.dto.event.UserRegisteredEvent

interface EmailService {

    fun sendVerificationEmail(userRegisteredEvent: UserRegisteredEvent)
}