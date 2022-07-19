package com.exchange.security

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class OAuth2AuthFailureHandler: SimpleUrlAuthenticationFailureHandler() {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse,
                                         exception: AuthenticationException) {

        var targetUrl = CustomWebUtils.getCookie(request,
                HttpCookieOAuth2AuthorizationRequestRepository.OAuth2RedirectParamCookieName)
                .map(({ it.value }))
                .orElse("/")

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.localizedMessage)
                .build().toUriString()

        HttpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}