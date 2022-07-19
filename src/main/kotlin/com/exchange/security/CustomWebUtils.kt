package com.exchange.security

import org.springframework.util.SerializationUtils
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Class implements workflow with cookie
 */
object CustomWebUtils {

    private const val cookieSecure: Boolean = false

    fun getCookie(request: HttpServletRequest, name: String): Optional<Cookie> {

        val cookies = request.cookies

        if (cookies != null && cookies.isNotEmpty()) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    return Optional.of(cookie)
                }
            }
        }

        return Optional.empty()
    }

    fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
        return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.value)))
    }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value)

        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = cookieSecure
        cookie.maxAge = maxAge
        response.addCookie(cookie)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies

        if (cookies != null && cookies.isNotEmpty()) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    cookie.value = ""
                    cookie.path = "/"
                    cookie.maxAge = 0
                    response.addCookie(cookie)
                }
            }
        }
    }

    fun serialize(obj: Any): String {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj))
    }
}