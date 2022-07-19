package com.exchange.security

import com.exchange.configuration.AppConfig
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import io.jsonwebtoken.*
import java.util.*
import org.slf4j.LoggerFactory

/**
 * In SessionToken class are created, validated and retrieved sessionToken
 */

@Service
class SessionToken(private val appConfig: AppConfig) {

    fun create(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        val expiryDate = Date(Date().time + appConfig.auth.tokenExpirationMilliSeconds)

        return Jwts.builder()
                .setSubject(java.lang.Long.toString(userPrincipal.id))
                .signWith(SignatureAlgorithm.HS512, appConfig.auth.tokenSecret)
                .setIssuedAt(Date())
                .setExpiration(expiryDate)
                .compact()
    }

    fun getUserId(token: String): Long? {
        val claims = Jwts.parser()
                .setSigningKey(appConfig.auth.tokenSecret)
                .parseClaimsJws(token)
                .body

        return java.lang.Long.parseLong(claims.subject)
    }

    fun validate(authToken: String): Boolean {

        try {
            Jwts.parser().setSigningKey(appConfig.auth.tokenSecret).parseClaimsJws(authToken)
            return true
        } catch (ex: SignatureException) {
            logger.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty.")
        }

        return false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SessionToken::class.java)
    }

}