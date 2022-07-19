package com.exchange.payload

import javax.validation.constraints.NotNull


class VerifyPhoneNumberRequest {
    @NotNull
    var code: Int = 0
}