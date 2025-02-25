package com.dev.security.config

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider : AuthenticationProvider{
    // 인증 논리 추가 메소드
    override fun authenticate(authentication: Authentication?): Authentication {
        val username = authentication?.name
        val password = authentication?.credentials.toString()

        if("user" == username && "password" == password) {
            return UsernamePasswordAuthenticationToken(username, password, emptyList())
        }

        throw RuntimeException("인증 정보가 일치하지 않습니다.")
    }

    // 인증 형식의 구현을 추가하는 메소드
    override fun supports(authentication: Class<*>?): Boolean {
       return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}