package com.dev.security.user

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class InMemoryUserDetailsService (
    private val users: List<UserDetails>,
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        return users.stream()
            .filter { it.username.equals(username) }
            .findFirst()
            .orElseThrow { RuntimeException("일치하는 사용자 정보가 없습니다.") }
    }
}