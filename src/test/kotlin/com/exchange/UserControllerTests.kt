package com.exchange

import com.exchange.controller.UserController
import com.exchange.entity.AuthProvider
import com.exchange.entity.Language
import com.exchange.entity.TwoFactorAuth
import com.exchange.entity.User
import com.exchange.service.SmsService
import com.exchange.service.UserService
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.mockito.Mockito
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

/**
 * Unit tests for User endpoints.
 * Mockito testing framework is used for unit tests development.
 * Mockito framework https://site.mockito.org/
 */

@RunWith(SpringRunner::class)
@WebMvcTest(value = UserController::class, secure = false)
class UserControllerTest {

    @Autowired
    val mockMvc: MockMvc? = null

    @MockBean
    val userService: UserService? = null

    @MockBean
    val smsService: SmsService? = null

    @MockBean
    val passwordEncoder: PasswordEncoder? = null

    val mockUser: User = User(124, "email@email.com", "firstname",
            "lastname", "\$2a\$10\$DK8QRCPWcSbb.riQD0WwQetgVgW9/xZPseB.ywxtp.y7upEEWlnIC", AuthProvider.local, "",
            false, Language.EN, TwoFactorAuth.NONE, false)

    @Test
    fun retrieveProfilePositive() {
        Mockito.`when`(userService!!.getCurrent()).thenReturn(mockUser)

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.get(
                "/users/profile").accept(MediaType.APPLICATION_JSON)
        val result: MvcResult = mockMvc?.perform(requestBuilder)!!.andReturn()
        val expected = "{\"authProvider\":\"local\",\"receiveUpdateEmails\":false,\"twoFactorAuth\":\"NONE\",\"accountVerified\":false,\"phoneNumber\":\"\",\"phoneNumberVerified\":false,\"googleQRUrl\":\"https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth%3A%2F%2Ftotp%2FExchange%3Aemail%40email.com%3Fsecret%3D%26issuer%3DExchange\"}"
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
        JSONAssert.assertEquals(expected, result.response.contentAsString, false)
    }

    @Test
    fun setLanguagePositive() {
        val language: Language = Language.RU
        val languageResponseMessage = "{\"message\":\"User language is set to $language!\"}"
        val languageRequestPayload = "{\"language\":\"$language\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/language").accept(MediaType.APPLICATION_JSON).
                content(languageRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
        JSONAssert.assertEquals(languageResponseMessage, result.response.contentAsString, false)
    }

    @Test
    fun setLanguageNegative() {
        val language: Language = Language.RU
        val languageRequestPayload = "{\"language\":\"Not valid param\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/language").accept(MediaType.APPLICATION_JSON).
                content(languageRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }

    @Test
    fun enableGoogle2FAPositive() {
        val enableGoogle2FARequestPayload = "{\"code\":\"1234\", \"password\":\"qwe123\", \"status\":true}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/google-authenticator").accept(MediaType.APPLICATION_JSON).
                content(enableGoogle2FARequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
    }

    @Test
    fun enableGoogle2FANegative() {
        val enableGoogle2FARequestPayload = "{\"password\":\"qwe123\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/google-authenticator").accept(MediaType.APPLICATION_JSON).
                content(enableGoogle2FARequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }

    @Test
    fun disableGoogle2FAPositive() {
        val disableGoogle2FARequestPayload = "{\"code\":\"1234\", \"password\":\"qwe123\", \"status\":false}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/google-authenticator").accept(MediaType.APPLICATION_JSON).
                content(disableGoogle2FARequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
    }

    @Test
    fun disableGoogle2FANegative() {
        val disableGoogle2FARequestPayload = "{\"code\":\"1234\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/google-authenticator").accept(MediaType.APPLICATION_JSON).
                content(disableGoogle2FARequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }

    @Test
    fun enableSMS2FAPositive() {
        val google2FAEnableRequestPayload = "{\"code\":\"1234\", \"password\":\"qwe123\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/sms-authenticator").accept(MediaType.APPLICATION_JSON).
                content(google2FAEnableRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
    }

    @Test
    fun enableSMS2FANegative() {
        val google2FAEnableRequestPayload = "{\"code\":\"1234\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/sms-authenticator").accept(MediaType.APPLICATION_JSON).
                content(google2FAEnableRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }

    @Test
    fun disableSMS2FAPositive() {
        val disableSMSRequestPayload = "{\"code\":\"1234\", \"password\":\"qwe123\", \"status\":false}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/sms-authenticator").accept(MediaType.APPLICATION_JSON).
                content(disableSMSRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
    }


    @Test
    fun disableSMS2FANegative() {
        val disableSMSRequestPayload = "{}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/sms-authenticator").accept(MediaType.APPLICATION_JSON).
                content(disableSMSRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }

    @Test
    fun setPhoneNumberNegative() {
        val phoneNumberRequestPayload = "{\"phoneNumber\":\"\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/phone-number").accept(MediaType.APPLICATION_JSON).
                content(phoneNumberRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }

    @Test
    fun updatePasswordPositive() {
        val updatePasswordResponseMessage = "{\"message\":\"Password successfully updated!\"}"
        val updatePasswordRequestPayload = "{\"currentPassword\":\"qwe123\", \"newPassword\":\"qwe1234\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/password").accept(MediaType.APPLICATION_JSON).
                content(updatePasswordRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.OK.value(), response.status)
        JSONAssert.assertEquals(updatePasswordResponseMessage, result.response.contentAsString, false)
    }

    @Test
    fun updatePasswordNegative() {
        val updatePasswordRequestPayload = "{\"currentPassword\":\"qwe123\"}"

        val requestBuilder: RequestBuilder = MockMvcRequestBuilders.patch(
                "/users/password").accept(MediaType.APPLICATION_JSON).
                content(updatePasswordRequestPayload).contentType(MediaType.APPLICATION_JSON)

        val result: MvcResult = mockMvc!!.perform(requestBuilder).andReturn()
        val response: MockHttpServletResponse = result.response

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
    }
}