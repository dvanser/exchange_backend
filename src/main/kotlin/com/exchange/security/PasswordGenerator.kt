package com.exchange.security

import org.springframework.stereotype.Service
import java.security.SecureRandom

/**
 * PasswordGenerator class contains logic related to User password generation
 */

@Service
class PasswordGenerator {

    private val random = SecureRandom()
    private val passwordLength = 10
    private val chars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ123456789+@"

    fun generateRandomPassword(): String {
        var password = ""

        for (i in 0 until passwordLength) {
            val index: Int = (random.nextDouble() * chars.length).toInt()
            password += chars.substring(index, index + 1)
        }
        return password
    }
}