package com.exchange.service

import com.exchange.configuration.AppConfig
import com.exchange.entity.Language
import com.exchange.entity.TwoFactorAuth
import com.exchange.entity.User
import com.exchange.exception.BadRequestException
import com.exchange.payload.*
import com.exchange.repository.UserRepository
import com.exchange.security.TwoFA.Totp
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import org.springframework.beans.factory.annotation.Autowired

/**
 * UserService contains all business logic related to user
 */

@Component
class UserService(var userRepository: UserRepository) {

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @Autowired
    private val appConfig: AppConfig? = null

    fun getCurrent(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        val userOptional: Optional<User> = userRepository.findByEmail(authentication.name)

        if (userOptional.isPresent) {
            return userOptional.get()
        } else {
            throw UnauthorizedUserException("User is not authorized")
        }
    }

    @Transactional
    fun setLanguage(language: Language) {
        val currentUser = getCurrent()

        currentUser.language = language
        userRepository.save(currentUser)
    }

    @Transactional
    fun toggleGoogle2FA(google2FARequest: Google2FARequest): String {
        val currentUser = getCurrent()
        val totp = Totp(currentUser.TwoFASecret, appConfig?.twoFA!!.googleCodeLifeTime)
        var message = "Google two-factor authentication enabled!"
        var googleQRUrl = ""

        if (!isPasswordValid(currentUser, google2FARequest.password)) {
            throw BadRequestException("Password is not correct!")
        }

        if (!totp.verify(google2FARequest.code)) {
            throw BadRequestException("Google authentication code is not correct!")
        }

        if (currentUser.twoFactorAuth !== TwoFactorAuth.GOOGLE) {
            currentUser.twoFactorAuth = TwoFactorAuth.GOOGLE
        } else {
            currentUser.twoFactorAuth = TwoFactorAuth.NONE
            message = "Google two-factor authentication disabled!"
            googleQRUrl = totp.generateQRUrl("Exchange", currentUser.email)
        }

        userRepository.save(currentUser)

        return "{\"message\":\"$message\", \"twoFactorAuth\":\"${currentUser.twoFactorAuth}\"" +
                (if (googleQRUrl !== "") ", \"googleQRUrl\": \"$googleQRUrl\"" else "") + "}"
    }

    @Transactional
    fun toggleSms2FA(sms2FARequest: Sms2FARequest): String {
        val currentUser = getCurrent()
        val totp = Totp(currentUser.TwoFASecret, appConfig?.twoFA!!.smsCodeLifeTime)
        var message = "SMS two-factor authentication enabled!"

        if (!isPasswordValid(currentUser, sms2FARequest.password)) {
            throw BadRequestException("Password is not correct!")
        }

        if (!totp.verify(sms2FARequest.code)) {
            throw BadRequestException("SMS authentication code is not correct!")
        }

        if (currentUser.twoFactorAuth !== TwoFactorAuth.SMS) {
            currentUser.twoFactorAuth = TwoFactorAuth.SMS
        } else {
            currentUser.twoFactorAuth = TwoFactorAuth.NONE
            message = "SMS two-factor authentication disabled!"
        }

        userRepository.save(currentUser)

        return message
    }

    @Transactional
    fun editPhoneNumber(phoneNumberRequest: PhoneNumberRequest) {
        val currentUser = getCurrent()
        check2FAAttempts(currentUser)
        currentUser.phoneNumber = phoneNumberRequest.phoneNumber
    }

    @Transactional
    fun updatePassword(passwordUpdateRequest: PasswordUpdateRequest) {
        val currentUser = getCurrent()

        if (!isPasswordValid(currentUser, passwordUpdateRequest.currentPassword)) {
            throw BadRequestException("Password is not correct!")
        }

        currentUser.password = passwordEncoder!!.encode(passwordUpdateRequest.newPassword)
        userRepository.save(currentUser)
    }

    @Transactional
    fun save(user: User) {
        userRepository.save(user)
    }

    fun findByEmail(email: String): User {

        val userOptional: Optional<User> = userRepository.findByEmail(email)

        if (userOptional.isPresent) {
            return userOptional.get()
        } else {
            throw BadRequestException("User is not found by email")
        }
    }

    fun findById(id: Long): User {
        val userOptional: Optional<User> = userRepository.findById(id)

        if (userOptional.isPresent) {
            return userOptional.get()
        } else {
            throw BadRequestException("User is not found by id")
        }
    }

    private fun isPasswordValid(user: User, oldPassword: String): Boolean {
        return passwordEncoder!!.matches(oldPassword, user.password)
    }

    fun check2FAAttempts(user: User) {
        if (user.TwoFAAttempts >= appConfig!!.twoFA.twoFAMaxAttempts) {

            val lastAttemptTime: Date = user.TwoFALastAttempt
            val blockingExpirationTime = Date(System.currentTimeMillis() - appConfig.twoFA.twoFAAttemptDelay * 1000)

            if (blockingExpirationTime > lastAttemptTime) {
                user.TwoFAAttempts = 1
            } else {
                throw BadRequestException("Too many attempts!")
            }
        } else {
            user.TwoFAAttempts = user.TwoFAAttempts.inc()
        }

        user.TwoFALastAttempt = Date()
        userRepository.save(user)
    }

    @Transactional
    fun updateReceiveUpdateEmails(user: User, receiveUpdateEmails: Boolean) {
        user.receiveUpdateEmails = receiveUpdateEmails
        userRepository.save(user)
    }

    @Transactional
    fun updateFirstAndLastName(firstName: String, lastName: String) {
        val currentUser = getCurrent()

        if (currentUser.firstName != firstName) {
            currentUser.firstName = firstName
        }

        if (currentUser.lastName != lastName) {
            currentUser.lastName = lastName
        }

        userRepository.save(currentUser)
    }
}
