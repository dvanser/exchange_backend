package com.exchange.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class RestAuthEntryPoint: AuthenticationEntryPoint {

    @Throws(IOException::class, ServletException::class)
    override fun commence(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse,
                          exception: AuthenticationException) {

        logger.error("Unauthorized error. ${exception.message}")
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                exception.localizedMessage)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RestAuthEntryPoint::class.java)
    }
}