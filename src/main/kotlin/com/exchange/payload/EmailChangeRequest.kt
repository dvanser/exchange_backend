package com.exchange.payload

import javax.validation.constraints.NotBlank


class EmailChangeRequest {
    @NotBlank
    var token: String = ""
}