package com.exchange.payload

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank


class SignUpRequest {
    @NotBlank
    @Email
    var email: String = ""
}