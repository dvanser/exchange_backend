package com.exchange.security.user

/**
 * GoogleOAuth2UserInfo class parse data retrieved from user google account
 */

class GoogleOAuth2UserInfo(attributes: Map<String, Any>): OAuth2UserInfo(attributes) {
    override val id: String
        get() = attributes["sub"] as String

    override val name: String
        get() = attributes["name"] as String

    override val email: String
        get() = attributes["email"] as String
}