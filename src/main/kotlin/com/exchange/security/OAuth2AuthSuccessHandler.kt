package com.exchange.security

import com.exchange.configuration.AppConfig
import com.exchange.exception.BadRequestException
import com.exchange.security.HttpCookieOAuth2AuthorizationRequestRepository.Companion.OAuth2RedirectParamCookieName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.URI
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class OAuth2AuthSuccessHandler @Autowired
internal constructor(private val sessionToken: SessionToken, private val appConfig: AppConfig):
        SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                         authentication: Authentication) {

        val targetUrl = getTargetUrl(request, response, authentication)

        if (response.isCommitted) {
            logger.debug("Unable to redirect to $targetUrl. Response committed already.")
            return
        }

        clearAuthAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    protected fun getTargetUrl(request: HttpServletRequest, response: HttpServletResponse,
                               authentication: Authentication): String {

        val redirectUrl = CustomWebUtils.getCookie(request, OAuth2RedirectParamCookieName)
                .map(({ it.value }))
        val targetUrl = redirectUrl.orElse(defaultTargetUrl)
        val token = sessionToken.create(authentication)

        if (redirectUrl.isPresent && !isAllowedRedirectUrl(redirectUrl.get())) {
            throw BadRequestException("We cannot proceed with authentication because unauthorized redirect URL received")
        }

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString()
    }

    private fun isAllowedRedirectUrl(url: String): Boolean {

        val redirectUrl = URI.create(url)

        return appConfig.oauth2.allowedRedirectUrls
                .stream()
                .anyMatch { allowedRedirectUrl ->
                    val url = URI.create(allowedRedirectUrl)

                    (url.host.equals(redirectUrl.host, ignoreCase = true)
                            && url.port == redirectUrl.port)
                }
    }

    protected fun clearAuthAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        HttpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response)
    }
}