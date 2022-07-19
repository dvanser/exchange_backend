package com.exchange.security.user

abstract class OAuth2UserInfo(attributes: Map<String, Any>) {
    var attributes: Map<String, Any>
        protected set

    abstract val id: String

    abstract val name: String

    abstract val email: String

    init {
        this.attributes = attributes
    }
}