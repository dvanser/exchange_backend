package com.exchange.security

import com.exchange.configuration.AppConfig
import com.exchange.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import javax.crypto.Cipher
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap


/**
 * Class provides encryption, decryption and URL parsing functionality
 */

@Component
class Cryptography {

    @Autowired
    private val appConfig: AppConfig? = null

    // Private function that implements encryption and decryption logic, depends on mode
    private fun encryptOrDecrypt (string: String, mode: String): String {

        try {
            val initVectorParams = IvParameterSpec(appConfig!!.encrypt.initVector.toByteArray(StandardCharsets.UTF_8))
            val secretKeySpec = SecretKeySpec(appConfig.encrypt.privateKey.toByteArray(StandardCharsets.UTF_8), "AES")
            val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val result: ByteArray

            return when (mode) {
                "ENCRYPT" -> {
                    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, initVectorParams)
                    result = cipher.doFinal(string.toByteArray(StandardCharsets.UTF_8))
                    Base64.getEncoder().encode(result).toString(StandardCharsets.UTF_8)
                }
                "DECRYPT" -> {
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initVectorParams)
                    result = cipher.doFinal(Base64.getDecoder().decode(string))
                    result.toString(StandardCharsets.UTF_8)
                } else -> ""
            }

        } catch (exception: Exception) {
            throw BadRequestException("Couldn't process string!")
        }

    }

    fun encrypt (string: String): String {
        return encryptOrDecrypt(string, "ENCRYPT")
    }

    fun decrypt (string: String): String {
        return encryptOrDecrypt(string, "DECRYPT")
    }

    // Functions parses url to HashMap<Key, Value>
    // Assume that URL params looks like ?key=value&key=value
    fun parseUrl(url: String): HashMap<String, String> {
        val pairs = url.split("&")
        val result = HashMap<String, String>()

        for (pair in pairs) {
            val index = pair.indexOf("=")

            if (index > 0) {
                result[pair.substring(0, index)] = pair.substring(index + 1)
            } else {
                throw BadRequestException("No parameters in url!")
            }
        }

        return result
    }
}