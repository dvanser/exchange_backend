package com.exchange.service

import com.exchange.configuration.AppConfig
import com.exchange.security.TwoFA.Totp
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * SmsService class responsible for SMS sending.
 * For SMS sending is used Twilio Programmable SMS
 * Twilio Programmable SMS docs https://www.twilio.com/docs/sms
 */

@Component
class SmsService {

    @Autowired
    private val appConfig: AppConfig? = null

    fun sendSms(userPhoneNumber: String, secret: String) {
        val totp = Totp(secret, appConfig?.twoFA!!.smsCodeLifeTime)
        Twilio.init(appConfig.sms2FA.twilioAccountSid, appConfig.sms2FA.twilioAuthToken)

        Message.creator(PhoneNumber(userPhoneNumber),                    // to User phone number
                        PhoneNumber(appConfig.sms2FA.twilioPhoneNumber), // from Twilio phone number
                        "Your verification code " + totp.generateSmsCode())
                .create()
    }
}