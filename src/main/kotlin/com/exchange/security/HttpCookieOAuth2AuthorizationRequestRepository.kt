package com.exchange.security

import com.nimbusds.oauth2.sdk.util.StringUtils
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Class implements authorization request managing
 */
class HttpCookieOAuth2AuthorizationRequestRepository: AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    override fun saveAuthorizationRequest(authRequest: OAuth2AuthorizationRequest?,
                                          request: HttpServletRequest, response: HttpServletResponse) {
        if (authRequest == null) {
            CustomWebUtils.deleteCookie(request, response, OAuth2RequestCookieName)
            CustomWebUtils.deleteCookie(request, response, OAuth2RedirectParamCookieName)
            return
        }

        CustomWebUtils.addCookie(response, OAuth2RequestCookieName,
                CustomWebUtils.serialize(authRequest), cookieExpirationInSeconds)

        val redirectUriAfterLogin = request.getParameter(OAuth2RedirectParamCookieName)

        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            CustomWebUtils.addCookie(response, OAuth2RedirectParamCookieName,
                    redirectUriAfterLogin, cookieExpirationInSeconds)
        }
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return CustomWebUtils.getCookie(request, OAuth2RequestCookieName)
                .map { cookie -> CustomWebUtils.deserialize(cookie, OAuth2AuthorizationRequest::class.java) }
                .orElse(null)
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return loadAuthorizationRequest(request)
    }

    companion object {
        const val OAuth2RequestCookieName = "oauth2"
        const val OAuth2RedirectParamCookieName = "redirect_url"
        private const val cookieExpirationInSeconds = 180 //3 minutes

        fun removeAuthorizationRequest(request: HttpServletRequest, response: HttpServletResponse?) {
            CustomWebUtils.deleteCookie(request, response!!, OAuth2RequestCookieName)
            CustomWebUtils.deleteCookie(request, response, OAuth2RedirectParamCookieName)
        }
    }
}