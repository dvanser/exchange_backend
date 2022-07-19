package com.exchange.controller

import com.exchange.configuration.AppConfig
import com.exchange.entity.TwoFactorAuth
import com.exchange.entity.User
import com.exchange.payload.*
import com.exchange.security.Cryptography
import com.exchange.security.PasswordGenerator
import com.exchange.security.SessionToken
import com.exchange.security.TwoFA.Totp
import com.exchange.security.TwoFA.api.Base32
import com.exchange.service.MailService
import com.exchange.service.SmsService
import com.exchange.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.Valid

/**
 * AuthController class responsible for user account creation, sign in process and password reset
 * For authorization is used OAuth2 protocol and org.springframework.security artifact
 * OAuth2 documentation https://oauth.net/2/
 */

@RestController
class AuthController(var userService: UserService) {

    @Autowired
    private val authenticationManager: AuthenticationManager? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @Autowired
    private val sessionToken: SessionToken? = null

    @Autowired
    private val smsService = SmsService()

    @Autowired
    private val appConfig: AppConfig? = null

    @Autowired
    private val mailService = MailService()

    @Autowired
    private val crypto = Cryptography()

    @Autowired
    private val passwordGenerator = PasswordGenerator()

    @PostMapping("/login")
    fun authenticateUser(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        val authentication = authenticationManager!!.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password
            )
        )

        val user = userService.findByEmail(loginRequest.email)

        if ((user.twoFactorAuth == TwoFactorAuth.GOOGLE || user.twoFactorAuth == TwoFactorAuth.SMS)
                && loginRequest.code.isEmpty()) {
            if (user.twoFactorAuth == TwoFactorAuth.SMS) {
                userService.check2FAAttempts(user)
                smsService.sendSms(user.phoneNumber, user.TwoFASecret)
            }
            return ResponseEntity.ok<Any>("{\"codeRequired\":\"${user.twoFactorAuth}\"}")
        } else if (user.twoFactorAuth == TwoFactorAuth.GOOGLE && !loginRequest.code.isEmpty()) {
            val totp = Totp(user.TwoFASecret, appConfig?.twoFA!!.googleCodeLifeTime)

            if (!totp.verify(loginRequest.code)) {
                throw BadCredentialsException("Invalid verification code!")
            }
        } else if (user.twoFactorAuth == TwoFactorAuth.SMS && !loginRequest.code.isEmpty()) {
            val totp = Totp(user.TwoFASecret, appConfig?.twoFA!!.smsCodeLifeTime)

            if (!totp.verify(loginRequest.code)) {
                throw BadCredentialsException("Invalid verification code!")
            }
        }

        SecurityContextHolder.getContext().authentication = authentication

        val token = sessionToken!!.create(authentication)

        return ResponseEntity.ok<Any>(LoginResponse(token))
    }

    @PostMapping("/signup")
    fun registerUser(@Valid @RequestBody singUpRequest: SignUpRequest): ResponseEntity<*> {
        val user = User(singUpRequest.email)

        user.TwoFASecret = Base32.random()
        val tmpPassword = passwordGenerator.generateRandomPassword()
        user.password = passwordEncoder!!.encode(tmpPassword)
        userService.save(user)
        mailService.sendMail(appConfig!!.mail.supportEmail, user.email,
                "Password to login", "Your password: $tmpPassword")

        return ResponseEntity.ok<Any>(ApiResponse("Please check your email for password. You can login using it."))
    }

    @PostMapping("/reset-password-link")
    fun sendResetPasswordLink(@Valid @RequestBody sendResetPasswordLinkRequest: SendResetPasswordLinkRequest): ResponseEntity<Any> {
        val user = userService.findByEmail(sendResetPasswordLinkRequest.email)
        val link = appConfig!!.appUrl + "/password/reset/"
        val token = UUID.randomUUID().toString()
        var emailMessage = "Password reset link below:\n"
        var params = "?id=" + user.id +
                "&email=" + user.email +
                "&timestamp=" + (System.currentTimeMillis() + appConfig.linkValidInterval) +
                "&token=" + token

        params = crypto.encrypt(params)
        emailMessage += link + params
        user.resetPasswordToken = token
        userService.save(user)
        mailService.sendMail(appConfig.mail.supportEmail, user.email, "Password Reset Request", emailMessage)

        return ResponseEntity.ok<Any>(ApiResponse("Please check your email for password reset link."))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody passwordResetRequest: PasswordResetRequest): ResponseEntity<Any> {
        val urlParams = crypto.decrypt(passwordResetRequest.token)
        val urlParamsMap = crypto.parseUrl(urlParams.substring(1))
        val user = userService.findById(urlParamsMap["id"]!!.toLong())

        if (user.resetPasswordToken == "") {
            return ResponseEntity.ok<Any>(ApiResponse("User reset password token not valid!"))
        }
        else if (user.email != urlParamsMap["email"]) {
            return ResponseEntity.ok<Any>(ApiResponse("Password reset token not valid!"))
        }

        if (user.resetPasswordToken == urlParamsMap["token"]) {
            if (System.currentTimeMillis() <= urlParamsMap["timestamp"]!!.toLong()) {
                user.password = passwordEncoder!!.encode(passwordResetRequest.newPassword)
                user.resetPasswordToken = ""
                userService.save(user)
            } else {
                return ResponseEntity.ok<Any>(ApiResponse("Password reset token expired!"))
            }
        } else {
            return ResponseEntity.ok<Any>(ApiResponse("Password reset token not valid!"))
        }

        val emailMessage = "Your password has been reset successfully!"

        mailService.sendMail(appConfig!!.mail.supportEmail, user.email, "Password Reset confirmation", emailMessage)

        return ResponseEntity.ok<Any>(ApiResponse("Your password has been reset successfully!"))
    }

}