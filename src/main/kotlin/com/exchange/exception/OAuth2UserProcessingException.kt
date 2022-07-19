package com.exchange.exception

import org.springframework.security.core.AuthenticationException

/**
 * Custom AuthenticationException handler
 */

class OAuth2UserProcessingException(msg: String): AuthenticationException(msg)