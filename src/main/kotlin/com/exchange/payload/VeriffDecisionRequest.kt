package com.exchange.payload

import org.json.JSONObject
import javax.validation.constraints.NotBlank

class VeriffDecisionRequest {
    @NotBlank
    var status: String = ""

    var verification: JSONObject = JSONObject()
}