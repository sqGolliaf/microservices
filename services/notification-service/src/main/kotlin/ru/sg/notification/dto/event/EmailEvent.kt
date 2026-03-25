package ru.sg.notification.dto.event

import ru.sg.notification.entity.enums.Status
import java.io.Serializable

data class EmailEvent(
    val id: Long,
    val email: String,
    val status: Status
) : Serializable