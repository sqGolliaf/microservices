package ru.sg.notification.entity.enums

enum class Status(
    val value: String
) {
    PENDING("Pending"),
    SENT("Sent"),
    FAILED("Failed"),
    VERIFIED("Verified")
}
