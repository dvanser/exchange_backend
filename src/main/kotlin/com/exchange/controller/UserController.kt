package com.exchange.controller

import com.exchange.configuration.AppConfig
import com.exchange.entity.TwoFactorAuth
import com.exchange.entity.User
import com.exchange.payload.*
import com.exchange.security.Cryptography
import com.exchange.service.SmsService
import com.exchange.security.TwoFA.Totp
import com.exchange.service.MailService
import com.exchange.service.UserService
import com.exchange.service.VeriffService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

/**
 * UserController class responsible for user requests management and data redirectorion to UserService or other services
 */

@RestController
class UserController(var userService: UserService, var veriffService: VeriffService) {

    @Autowired
    private val smsService = SmsService()

    @Autowired
    private val appConfig: AppConfig? = null

    @Autowired
    private val crypto = Cryptography()

    @Autowired
    private val mailService = MailService()

    //Retrieve user information
    @RequestMapping("/users/profile")
    @GetMapping
    fun getProfile(): ProfileResponse {
        val user = userService.getCurrent()
        var generatedGoogleQRUrl = ""

        if (user.twoFactorAuth !== TwoFactorAuth.GOOGLE) {
            val totp = Totp(user.TwoFASecret, appConfig?.twoFA!!.googleCodeLifeTime)
            generatedGoogleQRUrl = totp.generateQRUrl("Exchange", user.email)
        }

        return ProfileResponse(user, generatedGoogleQRUrl)
    }

    //Set user language to one of the supported languages
    @RequestMapping("/users/language")
    @PatchMapping
    fun updateLanguage(@Valid @RequestBody languageRequest: UserLanguageRequest): String {
        userService.setLanguage(languageRequest.language)
        return "{\"message\":\"User language is set to ${languageRequest.language}!\"}"
    }

    //Enable or disable google 2FA authentication
    @RequestMapping("/users/google-authenticator")
    @PatchMapping
    fun changeGoogle2FAStatus(@Valid @RequestBody google2FARequest: Google2FARequest): String {
        return userService.toggleGoogle2FA(google2FARequest)
    }

    //Enable or disable SMS 2FA authentication
    @RequestMapping("/users/sms-authenticator")
    @PatchMapping
    fun changeSMS2FAStatus(@Valid @RequestBody sms2FARequest: Sms2FARequest): String {
        val message: String = userService.toggleSms2FA(sms2FARequest)
        return "{\"message\":\"$message\"}"
    }

    //Set user phone number for SMS 2FA authentication
    @RequestMapping("/users/phone-number")
    @PatchMapping
    fun editPhoneNumber(@Valid @RequestBody phoneNumberRequest: PhoneNumberRequest): String {
        userService.editPhoneNumber(phoneNumberRequest)
        smsService.sendSms(phoneNumberRequest.phoneNumber, userService.getCurrent().TwoFASecret)
        return "{\"message\":\"Verification code sent to ${phoneNumberRequest.phoneNumber}!\"}"
    }

    //Update signed in user password
    @RequestMapping("/users/password")
    @PatchMapping
    fun updatePassword(@Valid @RequestBody passwordUpdateRequest: PasswordUpdateRequest): String {
        userService.updatePassword(passwordUpdateRequest)
        return "{\"message\":\"Password successfully updated!\"}"
    }

    //Send e-mail verification link to the user and manage user subscription
    @RequestMapping("/users/email-update-link")
    @PatchMapping
    fun updateEmailSettings(@Valid @RequestBody emailVerifyRequest: EmailVerifyRequest, request: HttpServletRequest): String {
        val currentUser = userService.getCurrent()
        var returnMessage: String

        returnMessage = updateReceiveUpdateEmails(currentUser, emailVerifyRequest) +
                updateEmailLinkRequest(currentUser, emailVerifyRequest)

        if (returnMessage.isEmpty()) {
            returnMessage = "Nothing changed!"
        }


        return "{\"message\":\"${returnMessage.trim()}\"}"
    }

    //Change user e-mail
    @RequestMapping("/users/email")
    @PatchMapping
    fun changeEmail(@Valid @RequestBody emailChangeRequest: EmailChangeRequest): String {
        val currentUser = userService.getCurrent()
        val urlParams = crypto.decrypt(emailChangeRequest.token)
        val urlParamsMap = crypto.parseUrl(urlParams.substring(1))

        if (currentUser.id == urlParamsMap["id"]!!.toLong() && currentUser.email == urlParamsMap["email"]) {
            if (System.currentTimeMillis() <= urlParamsMap["timestamp"]!!.toLong() && !currentUser.newEmail.isEmpty()) {
                currentUser.email = urlParamsMap["newEmail"]!!
            } else {
                return  "{\"message\":\"Email change token expired!\"}"
            }
        } else {
            return "{\"message\":\"Invalid token!\"}"
        }

        currentUser.newEmail = ""
        userService.save(currentUser)

        return "{\"message\":\"Your email is verified!\"}"
    }

    private fun updateReceiveUpdateEmails(currentUser: User, emailVerifyRequest: EmailVerifyRequest): String {
        var returnMessage = ""

        if (currentUser.receiveUpdateEmails != emailVerifyRequest.receiveUpdateEmails) {
            userService.updateReceiveUpdateEmails(currentUser, emailVerifyRequest.receiveUpdateEmails)
            if (emailVerifyRequest.receiveUpdateEmails) {
                returnMessage = "You have subscribed for email updates! "
            } else {
                returnMessage = "You have unsubscribed for email updates! "
            }
        }

        return returnMessage
    }

    private fun updateEmailLinkRequest(currentUser: User, emailVerifyRequest: EmailVerifyRequest): String {
        var returnMessage = ""

        if (currentUser.email != emailVerifyRequest.newEmail) { //user wants to change email

            val link = appConfig!!.appUrl + "/users/email/"
            var emailMessage = "Email verify link below:\n"
            var params = "?id=" + currentUser.id +
                    "&newEmail=" + emailVerifyRequest.newEmail +
                    "&timestamp=" + (System.currentTimeMillis() + appConfig.linkValidInterval) +
                    "&email=" + currentUser.email

            params = crypto.encrypt(params)
            emailMessage += link + params
            mailService.sendMail(appConfig.mail.supportEmail, emailVerifyRequest.newEmail, "Email change request", emailMessage)
            currentUser.newEmail = emailVerifyRequest.newEmail
            userService.save(currentUser)
            returnMessage = "Link to verify email is sent to You!"
        }

        return returnMessage
    }

    //Returns VeriffMe url to the user
    @RequestMapping("/users/verification")
    @PostMapping
    fun getVeriffMeUrl(@Valid @RequestBody veriffRequest: VeriffRequest): String {
        userService.updateFirstAndLastName(veriffRequest.firstName, veriffRequest.lastName)
        val (message, url) = veriffService.getUrl(veriffRequest, userService.getCurrent())

        return "{\"message\":\"$message\", \"verificationUrl\":\"$url\"}"
    }

    //Sets verification decision to the user
    @RequestMapping("/users/verification/decision")
    @PostMapping
    fun setVerificationDecision(@Valid @RequestBody veriffDecisionRequest: VeriffDecisionRequest): ResponseEntity<Any> {

        veriffService.getStatus(veriffDecisionRequest.verification, userService.getCurrent())

        return ResponseEntity.ok<Any>("ok")
    }
}