package com.exchange.configuration

import com.exchange.security.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.BeanIds
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.web.context.request.RequestContextListener
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfig: WebSecurityConfigurerAdapter() {

    @Autowired
    private val customUserDetailsService: CustomUserDetailsService? = null

    @Autowired
    private val customOAuth2UserService: CustomOAuth2UserService? = null

    @Autowired
    private val oAuth2AuthSuccessHandler: OAuth2AuthSuccessHandler? = null

    @Autowired
    private val oAuth2AuthFailureHandler: OAuth2AuthFailureHandler? = null

    @Bean
    fun tokenAuthFilter(): TokenAuthFilter {
        return TokenAuthFilter()
    }

    @Bean
    fun requestContextListener(): RequestContextListener {
        return RequestContextListener()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Throws(Exception::class)
    public override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder?) {
        authenticationManagerBuilder!!
                .userDetailsService<UserDetailsService>(customUserDetailsService)
                .passwordEncoder(passwordEncoder())
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(RestAuthEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/", "/error", "/login", "/signup", "/oauth2/**", "/reset-password", "/reset-password-link")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .logout()
                .logoutSuccessHandler((HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                .permitAll()
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth2/login")
                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/oauth2/callback/*")
                .and()
                .userInfoEndpoint() //OAuth 2.0 Protected Resource that returns claims about the authenticated end-user
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oAuth2AuthSuccessHandler)
                .failureHandler(oAuth2AuthFailureHandler)

        http.addFilterBefore(tokenAuthFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    private fun cookieAuthorizationRequestRepository(): AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
        return HttpCookieOAuth2AuthorizationRequestRepository()
    }
}