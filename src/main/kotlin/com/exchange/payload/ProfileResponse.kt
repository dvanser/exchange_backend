package com.exchange.payload

import com.exchange.entity.AuthProvider
import com.exchange.entity.TwoFactorAuth
import com.exchange.entity.User


class ProfileResponse(user: User,  generatedGoogleQRUrl: String) {

    var authProvider: AuthProvider = user.authProvider

    var receiveUpdateEmails: Boolean = user.receiveUpdateEmails

    var twoFactorAuth: TwoFactorAuth = user.twoFactorAuth

    var accountVerified: Boolean = user.accountVerified

    var phoneNumber: String = user.phoneNumber

    var phoneNumberVerified: Boolean = user.phoneNumberVerified

    var googleQRUrl: String = generatedGoogleQRUrl
}