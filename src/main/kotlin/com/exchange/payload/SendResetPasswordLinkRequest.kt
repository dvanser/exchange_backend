package com.exchange.payload

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

class SendResetPasswordLinkRequest {
    @NotBlank
    @Email
    var email: String = ""
}