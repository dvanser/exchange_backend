package com.exchange.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Custom RuntimeException handler related to Not Found business logic exceptions
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(resourceName: String, fieldName: String, fieldValue: Any):
        RuntimeException("$resourceName not found with $fieldName=$fieldValue")