package ru.sg.notification.dto.event

import java.io.Serializable
import java.time.Instant

data class UserRegisteredEvent(
    val keycloakId: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val registeredAt: Instant,
    val updatedAt: Instant
) : Serializable