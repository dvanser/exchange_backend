package com.exchange.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull


enum class AuthProvider {
    local,
    facebook,
    google
}

enum class Language {
    EN,
    RU
}

enum class TwoFactorAuth {
    NONE,
    GOOGLE,
    SMS
}

enum class AccountVerificationStatus {
    NOT_VERIFIED,
    VERIFIED,
    IN_PROGRESS,
    RESUBMIT_REQUIRED, //verification resubmit required, maybe some of docs not of good quality, etc.
    EXPIRED, //verification url requested, but verification never started, session expired
    ABANDONED, //verification url requested, verification started, but not ended, session expired
    DECLINED_CAN_RETRY, //declined and user can be retry verification
    DECLINED //declined and user cannot retry verification
}

@Entity
@Table(name = "users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Email
    @Column(unique = true, nullable = false)
    var email: String = "",

    var firstName: String = "",

    var lastName: String = "",

    @JsonIgnore
    var password: String = "",

    @NotNull
    @Enumerated(EnumType.STRING)
    var authProvider: AuthProvider = AuthProvider.local,

    var authProviderId: String? = null,

    @Column(nullable = false)
    var receiveUpdateEmails: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    var language: Language = Language.EN,

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    var twoFactorAuth: TwoFactorAuth = TwoFactorAuth.NONE,

    @Column(nullable = false)
    var accountVerified: Boolean = false,

    @Column
    var phoneNumber: String = "",

    @Column(nullable = false)
    var phoneNumberVerified: Boolean = false,

    @Column
    @JsonIgnore
    var TwoFASecret: String = "",

    @Column
    @JsonIgnore
    var TwoFALoginAttempts: Int = 0, //not legged in user

    @Column
    @JsonIgnore
    var TwoFALoginLastAttempt: Date = Date(),

    @Column
    @JsonIgnore
    var TwoFAAttempts: Int = 0,      //logged in user

    @Column
    @JsonIgnore
    var TwoFALastAttempt: Date = Date(),

    @Column
    @JsonIgnore
    var resetPasswordToken: String = "",

    @Email
    @Column
    var newEmail: String = "",

    @Column(nullable = false)
    var accountVerificationStatus: AccountVerificationStatus = AccountVerificationStatus.NOT_VERIFIED,

    @Column(nullable = false)
    var accountVerificationId: String = "",

    @Column(nullable = false)
    var accountVerificationUrl: String = "",

    @Column(nullable = false)
    var accountVerificationSessionToken: String = "",

    @Column(nullable = false)
    var accountVerificationReason: String = "", //used in case verification failed, to store reason of fail

    @Column
    var accountVerificationSessionStart: Date? = null
) {
    constructor(email: String) : this() {
        this.email = email
    }
}
