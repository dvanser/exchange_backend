package com.exchange.payload

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class EmailVerifyRequest {
    @NotBlank
    @Email
    var newEmail: String = ""

    @NotNull
    var receiveUpdateEmails: Boolean = false
}