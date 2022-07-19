package com.exchange.payload

import javax.validation.constraints.NotBlank


class VeriffRequest {
    @NotBlank
    var firstName: String = ""

    @NotBlank
    var lastName: String = ""
}