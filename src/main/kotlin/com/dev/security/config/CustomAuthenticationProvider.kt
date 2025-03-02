package com.dev.security.config

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider (
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
) : AuthenticationProvider{
    // 인증 논리 추가 메소드
    override fun authenticate(authentication: Authentication?): Authentication {
        val username = authentication?.name
        val password = authentication?.credentials.toString()

        val user = userDetailsService.loadUserByUsername(username)

        if(passwordEncoder.matches(password, user.password)){
            return UsernamePasswordAuthenticationToken(user,password,user.authorities)
        }

        throw BadCredentialsException("Invalid password")
    }

    // 인증 형식의 구현을 추가하는 메소드
    override fun supports(authentication: Class<*>?): Boolean {
       return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication!!)
    }
}