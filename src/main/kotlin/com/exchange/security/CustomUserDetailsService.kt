package com.exchange.security

import com.exchange.exception.ResourceNotFoundException
import com.exchange.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Class implements loading users info
 */
@Service
class CustomUserDetailsService: UserDetailsService {

    @Autowired
    internal var userRepository: UserRepository? = null

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {

        val user = userRepository!!.findByEmail(email)
                .orElseThrow{ UsernameNotFoundException("User with email $email not found") }

        return UserPrincipal.create(user)
    }

    @Transactional
    @Throws(ResourceNotFoundException::class)
    fun loadUserById(id: Long): UserDetails {

        val user = userRepository!!.findById(id).orElseThrow { ResourceNotFoundException("User", "id", id) }

        return UserPrincipal.create(user)
    }
}