package ru.sg.email.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sg.email.entity.Email

@Repository
interface EmailRepository: JpaRepository<Email, Long>