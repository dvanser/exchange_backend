package com.exchange.service

import com.exchange.exception.BadRequestException
import org.springframework.stereotype.Component
import org.springframework.mail.SimpleMailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.mail.MailSender

/**
 * MailService class responsible for e-mail sending.
 * E-mail sending functionality is managed by org.springframework.mail artifact
 */

@Component
class MailService {

    @Autowired
    private val mailSender: MailSender? = null

    @Transactional
    fun sendMail(from: String, to: String, subject: String, body: String) {

        val email = SimpleMailMessage()

        email.setFrom(from)
        email.setTo(to)
        email.setSubject(subject)
        email.setText(body)

        if (mailSender != null) {
            try {
                mailSender.send(email)
            } catch (exception: Exception) {
                throw BadRequestException("${exception.message}")
            }
        } else {
            throw BadRequestException("Email cannot be sent, mailSender is null!")
        }

    }
}