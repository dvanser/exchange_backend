package com.exchange.security.user

/**
 * FacebookOAuth2UserInfo class parse data retrieved from user facebook account
 */

class FacebookOAuth2UserInfo(attributes: Map<String, Any>): OAuth2UserInfo(attributes) {
    override val id: String
        get() = attributes["id"] as String

    override val name: String
        get() = attributes["name"] as String

    override val email: String
        get() = attributes["email"] as String
}