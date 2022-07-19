package com.exchange.service


import com.exchange.configuration.AppConfig
import com.exchange.entity.AccountVerificationStatus
import com.exchange.entity.User
import com.exchange.exception.BadRequestException
import com.exchange.payload.VeriffRequest
import com.exchange.repository.UserRepository
import khttp.post
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * VeriffService class contains logic related to user verification
 * For user verification is used veriff.me service
 * veriff.me documentation https://veriff.com/
 */

@Component
class VeriffService(var userRepository: UserRepository) {

    @Autowired
    private val appConfig: AppConfig? = null

    @Transactional
    fun getStatus(veriffDecisionRequest: JSONObject, user: User) {

        when (veriffDecisionRequest["status"]) {
            "approved" -> {
                //Code - 9001
                //Positive: Person was verified.
                //The verification process is complete.
                //Accessing the sessionURL again will show the client that nothing is to be done here.
                //Code - 9151
                //Intermediate Positive: SelfID was successful
                //Code - 9161
                //Intermediate Positive: Video Call was successful
                resetUserAccountVerificationDataAndSetStatus(user, AccountVerificationStatus.VERIFIED)
            }
            "resubmission_requested" -> {
                //Code - 9103
                //Resubmitted: Resubmission has been requested.
                //The verification process is not completed.
                //Something was missing from the client and she or he needs to go through the flow once more.
                //The same sessionURL can and should be used for this purpose.
                user.accountVerificationStatus = AccountVerificationStatus.RESUBMIT_REQUIRED
                userRepository.save(user)
            }
            "declined" -> {
                //Code - 9102
                //Negative: Person has not been verified.
                //The verification process is complete.
                resetUserAccountVerificationDataAndSetStatus(user, AccountVerificationStatus.DECLINED_CAN_RETRY,
                        veriffDecisionRequest["reason"].toString())
            }

            "expired" -> {
                //Code - 9104
                //Negative: Verification has been expired.
                //The verification process is complete.
                //After 7 days the session get's expired.
                //the client never arrived in our environment
                resetUserAccountVerificationDataAndSetStatus(user, AccountVerificationStatus.EXPIRED)
            }
            "abandoned" -> {
                //Code - 9104
                //Negative: Verification has been expired.
                //The verification process is complete.
                //After 7 days the session get's expired.
                //the client started the verification process, but not ended it and session expired
                resetUserAccountVerificationDataAndSetStatus(user, AccountVerificationStatus.ABANDONED)
            }
            else -> {
            }
        }
    }

    @Transactional
    protected fun resetUserAccountVerificationDataAndSetStatus(
            user: User, verificationStatus: AccountVerificationStatus, reason: String = "") {

        user.accountVerificationStatus = verificationStatus
        user.accountVerificationId = ""
        user.accountVerificationSessionToken = ""
        user.accountVerificationUrl = ""
        user.accountVerificationReason = reason
        userRepository.save(user)
    }

    fun getUrl(veriffRequest: VeriffRequest, user: User): List<String> {

        var returnMessage = ""
        var verificationUrl = ""

        when(user.accountVerificationStatus) {
            AccountVerificationStatus.NOT_VERIFIED,
            AccountVerificationStatus.EXPIRED,
            AccountVerificationStatus.ABANDONED,
            AccountVerificationStatus.DECLINED_CAN_RETRY -> {

                val veriffResponse = requestVerificationUrl(user.firstName, user.lastName)

                verificationUrl = updateUserAccountVerificationDataAndGetUrl(user, veriffResponse,
                        AccountVerificationStatus.IN_PROGRESS)
                returnMessage = "Verification link successfully retrieved"
            }
            AccountVerificationStatus.VERIFIED -> {
                returnMessage = "Account already verified"
            }
            AccountVerificationStatus.IN_PROGRESS,
            AccountVerificationStatus.RESUBMIT_REQUIRED -> run {

                //!! used because we know that in this state date will be set
                if (user.accountVerificationSessionStart!! < Date(System.currentTimeMillis())) {
                    verificationUrl = user.accountVerificationUrl
                    returnMessage = "Verification link successfully retrieved"
                    return@run
                }

                //session expired, link not valid, must request new one
                val veriffResponse = requestVerificationUrl(user.firstName, user.lastName)

                verificationUrl = updateUserAccountVerificationDataAndGetUrl(user, veriffResponse,
                        user.accountVerificationStatus)
                returnMessage = ""
            }
            AccountVerificationStatus.DECLINED -> {
                returnMessage = "Verification was unsuccessful and cannot be repeated. Reason: " +
                        user.accountVerificationReason
            }
        }

        return listOf(returnMessage, verificationUrl)
    }

    @Transactional
    protected fun updateUserAccountVerificationDataAndGetUrl(
            user: User, veriffResponse: JSONObject, verificationStatus: AccountVerificationStatus): String {

        val verificationUrl = veriffResponse.getString("url")

        user.accountVerificationStatus = verificationStatus
        user.accountVerificationId = veriffResponse.getString("id")
        user.accountVerificationUrl = verificationUrl
        user.accountVerificationSessionToken = veriffResponse.getString("sessionToken")
        user.accountVerificationSessionStart = Date(System.currentTimeMillis())
        user.accountVerificationReason = ""

        userRepository.save(user)

        return verificationUrl
    }

    private fun requestVerificationUrl(firstName: String, lastName: String): JSONObject {

        val parentObjData = JSONObject()
        val requestData = JSONObject()
        val personData = JSONObject()
        val features = JSONArray()
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        df.timeZone = tz

        personData.put("firstName", firstName)
        personData.put("lastName", lastName)
        features.put("selfid")
        features.put("video_call")
        requestData.put("person", personData)
        requestData.put("features", features)
        requestData.put("timestamp", df.format(Date()))
        parentObjData.put("verification", requestData)

        val response = post(url = "https://api.veriff.me/v1/sessions",
                headers = mapOf("X-AUTH-CLIENT" to appConfig!!.veriffMe.client,
                        "X-SIGNATURE" to generateSignature(parentObjData,
                                appConfig.veriffMe.secret),
                        "CONTENT-TYPE" to "application/json"),
                json = parentObjData
        )

        val responseObject : JSONObject = response.jsonObject

        if (responseObject.getString("status") != "success") {
            throw BadRequestException("Verification URL fetching was unsuccessful")
        }

        return responseObject.getJSONObject("verification")
    }

    private fun generateSignature(payload: JSONObject, secret: String): String {

        val payloadString = payload.toString()
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(payloadString.toByteArray(StandardCharsets.UTF_8))
        digest.update(secret.toByteArray(StandardCharsets.UTF_8))
        val hash = digest.digest()

        return bytesToHex(hash)
    }

    private fun bytesToHex(hash: ByteArray): String {

        val hexString = StringBuffer()

        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())

            if (hex.length == 1) hexString.append('0')

            hexString.append(hex)
        }

        return hexString.toString()
    }

}