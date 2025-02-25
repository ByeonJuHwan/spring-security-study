package com.dev.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

/**
 * 항상 userDetailsService 를 정의하면
 */
@Configuration
class SecurityConfig {

    @Bean
    fun userDetailsService(): UserDetailsService {
        val inMemoryUserDetailsManager = InMemoryUserDetailsManager()
        val user = User.builder()
            .username("user")
            .password("password")
            .build()

        inMemoryUserDetailsManager.createUser(user)

        return InMemoryUserDetailsManager()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return NoOpPasswordEncoder.getInstance()
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity) : SecurityFilterChain{
        httpSecurity
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .httpBasic(withDefaults())

        return httpSecurity.build()
    }

}