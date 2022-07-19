package com.exchange.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * TokenAuthFilter class responsible for token filtering and bearerToken cutting from the Authorization request header
 */

class TokenAuthFilter : OncePerRequestFilter() {

    @Autowired
    private val sessionToken: SessionToken? = null

    @Autowired
    private val customUserDetailsService: CustomUserDetailsService? = null

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse,
                                  filterChain: FilterChain) {

        try {
            val jwt = getJwtFromRequest(request)

            if (StringUtils.hasText(jwt) && sessionToken!!.validate(jwt!!)) {
                val userId = sessionToken.getUserId(jwt)
                val userDetails = customUserDetailsService!!.loadUserById(userId!!)
                val auth = UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.authorities)

                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (exception: Exception) {
            logger.error("Cannot set user authentication in security context", exception)
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {

        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
                bearerToken.substring(7, bearerToken.length)
            else null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenAuthFilter::class.java)
    }
}