package com.exchange.payload

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

class Google2FARequest {
    @NotBlank
    var code: String = ""

    @NotBlank
    @Pattern(regexp="(^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}\$)",
            message = "Not valid password. Password:" +
                    "Should contain at least 8 chars\n" +
                    "Should contain at least one digit\n" +
                    "Should contain at least one lower alpha char and one upper alpha char\n")
    var password: String = ""
}