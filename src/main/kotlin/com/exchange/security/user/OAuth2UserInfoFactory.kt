package com.exchange.security.user

import com.exchange.entity.AuthProvider
import com.exchange.exception.OAuth2UserProcessingException


object OAuth2UserInfoFactory {

    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {

        return when {
            registrationId.equals(AuthProvider.facebook.toString(), ignoreCase = true) ->
                FacebookOAuth2UserInfo(attributes)
            registrationId.equals(AuthProvider.google.toString(), ignoreCase = true) ->
                GoogleOAuth2UserInfo(attributes)
            else -> throw
            OAuth2UserProcessingException("Login with $registrationId is not supported.")
        }
    }
}