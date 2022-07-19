package com.exchange.payload

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class PhoneNumberRequest {
    @NotBlank
    @Size(min = 5, max = 15)
    @Pattern(regexp="(^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*\$)", message = "Not valid phone number!")
    var phoneNumber: String = ""
}