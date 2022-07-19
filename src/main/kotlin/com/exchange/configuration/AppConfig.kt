package com.exchange.configuration

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "app")
class AppConfig {

    var auth = Auth()
    val oauth2 = OAuth2()
    val sms2FA = Sms2FA()
    val twoFA = TwoFA()
    val mail = Mail()
    var appUrl: String = ""
    val encrypt = Encrypt()
    var linkValidInterval: Int = 0
    val veriffMe = VeriffMe()

    class Auth {
        var tokenSecret: String = ""
        var tokenExpirationMilliSeconds: Long = 0
    }

    class OAuth2 {
        var allowedRedirectUrls: List<String> = ArrayList()
    }

    class Sms2FA {
        var twilioAccountSid: String = ""
        var twilioAuthToken: String = ""
        var twilioPhoneNumber: String = ""
    }

    class TwoFA {
        var googleCodeLifeTime: Int = 0
        var smsCodeLifeTime: Int = 0
        var twoFAMaxAttempts: Int = 0
        var twoFAAttemptDelay: Int = 0
    }

    class Mail {
        var supportEmail: String = ""
    }

    class Encrypt {
        var privateKey: String = ""
        var initVector: String = ""
    }

    class VeriffMe {
        var client: String = ""
        var secret: String = ""
    }

}