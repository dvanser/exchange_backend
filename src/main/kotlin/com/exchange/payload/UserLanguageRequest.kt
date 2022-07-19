package com.exchange.payload

import com.exchange.entity.Language
import javax.validation.constraints.NotNull

class UserLanguageRequest {
    @NotNull
    var language: Language = Language.EN
}