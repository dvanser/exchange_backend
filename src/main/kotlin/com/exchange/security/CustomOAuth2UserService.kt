package com.exchange.security

import com.exchange.entity.AuthProvider
import com.exchange.entity.User
import com.exchange.exception.OAuth2UserProcessingException
import com.exchange.repository.UserRepository
import com.exchange.security.user.OAuth2UserInfo
import com.exchange.security.user.OAuth2UserInfoFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.security.core.AuthenticationException
import org.springframework.util.StringUtils


/**
 * Class implements user authentication settings load
 */
@Service
class CustomOAuth2UserService: DefaultOAuth2UserService() {

    @Autowired
    private val userRepository: UserRepository? = null

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(oAuth2UserRequest: OAuth2UserRequest): OAuth2User {

        val oAuth2User = super.loadUser(oAuth2UserRequest)

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User)
        } catch (exception: AuthenticationException) {
            throw exception
        } catch (exception: Exception) {
            throw InternalAuthenticationServiceException(exception.message, exception.cause)
        }
    }

    private fun processOAuth2User(oAuth2UserRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {

        val oAuth2UserInfo = OAuth2UserInfoFactory.
                getOAuth2UserInfo(oAuth2UserRequest.clientRegistration.registrationId, oAuth2User.attributes)

        if (StringUtils.isEmpty(oAuth2UserInfo.email)) {
            throw OAuth2UserProcessingException("Email not found in data fetched using oauth2")
        }

        val userFromRepo = userRepository!!.findByEmail(oAuth2UserInfo.email)
        var user: User

        if (userFromRepo.isPresent) {
            user = userFromRepo.get()

            if (user.authProvider != AuthProvider.valueOf(oAuth2UserRequest.clientRegistration.registrationId)) {
                throw OAuth2UserProcessingException("Please use your ${user.authProvider} account to login.")
            }

            user = updateUser(user, oAuth2UserInfo)
        } else {
            user = registerUser(oAuth2UserRequest, oAuth2UserInfo)
        }

        return UserPrincipal.create(user, oAuth2User.attributes)
    }

    private fun registerUser(oAuth2UserRequest: OAuth2UserRequest, oAuth2UserInfo: OAuth2UserInfo): User {

        val user = User()

        user.authProvider = AuthProvider.valueOf(oAuth2UserRequest.clientRegistration.registrationId)
        user.authProviderId = oAuth2UserInfo.id
        user.email = oAuth2UserInfo.email
        user.firstName = oAuth2UserInfo.name

        return userRepository!!.save(user)
    }

    private fun updateUser(user: User, oAuth2UserInfo: OAuth2UserInfo): User {

        user.firstName = oAuth2UserInfo.name

        return userRepository!!.save(user)
    }
}