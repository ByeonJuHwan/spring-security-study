package com.dev.security.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class ByeonUser : UserDetails{
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(GrantedAuthority { "READ" })
    }

    override fun getPassword(): String {
        return "12345"
    }

    override fun getUsername(): String {
        return "byeon"
    }
}