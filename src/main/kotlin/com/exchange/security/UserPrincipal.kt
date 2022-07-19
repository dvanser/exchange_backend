package com.exchange.security

import com.exchange.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * UserPrincipal class representing an identity used to determine access rights to objects in a file system
 */

class UserPrincipal(val id: Long, private val email: String, private val password: String,
                    private val authorities: Collection<GrantedAuthority>): OAuth2User, UserDetails {

    var attributesOverride: Map<String, Any> = emptyMap()

    private val nameOverride: String
        get() = id.toString()

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getUsername(): String {
        return email
    }

    override fun getPassword(): String {
        return password
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getName(): String {
        return nameOverride
    }

    override fun getAttributes(): Map<String, Any> {
        return attributesOverride
    }

    companion object {

        fun create(user: User): UserPrincipal {
            val authorities = listOf<GrantedAuthority>(SimpleGrantedAuthority("USER"))

            return UserPrincipal(
                    user.id!!,
                    user.email!!,
                    user.password,
                    authorities
            )
        }

        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            val userPrincipal = UserPrincipal.create(user)
            userPrincipal.attributesOverride = attributes

            return userPrincipal
        }
    }
}