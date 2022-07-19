package com.exchange.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Custom RuntimeException handler related to Bad Request business logic exceptions
 */

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String): RuntimeException(message)