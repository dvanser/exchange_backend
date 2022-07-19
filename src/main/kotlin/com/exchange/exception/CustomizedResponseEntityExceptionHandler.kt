package com.exchange.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException

/**
 * CustomizedResponseEntityExceptionHandler class allows you to handle entity exceptions across the whole application.
 */

@ControllerAdvice
@RestController
class CustomizedResponseEntityExceptionHandler: ResponseEntityExceptionHandler() {
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val exceptionResponse = ExceptionResponse(ex.message.toString())
        return ResponseEntity(exceptionResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(EntityExistsException::class)
    fun handleEntityExistsExceptions(ex: EntityExistsException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val exceptionResponse = ExceptionResponse(ex.message.toString())
        return ResponseEntity(exceptionResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val exceptionResponse = ExceptionResponse("Invalid argument provided")
        return ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST)
    }

    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders,
                                              status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val exceptionResponse = ExceptionResponse(ex.bindingResult.fieldError?.defaultMessage.toString())
        return handleExceptionInternal(ex, exceptionResponse, headers, HttpStatus.BAD_REQUEST, request)
    }
}