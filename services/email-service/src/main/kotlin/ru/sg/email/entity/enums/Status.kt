package ru.sg.email.entity.enums

enum class Status(
    val value: String
) {
    PENDING("Pending"),
    SENT("Sent"),
    FAILED("Failed"),
    VERIFIED("Verified")
}
