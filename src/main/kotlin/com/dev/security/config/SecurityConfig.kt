package com.dev.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

/**
 * 항상 userDetailsService 를 정의하면
 */
@Configuration
class SecurityConfig {

//    @Bean
//    fun userDetailsService(): UserDetailsService {
//        val inMemoryUserDetailsManager = InMemoryUserDetailsManager()
//        val user = User.builder()
//            .username("user")
//            .password("password")
//            .build()
//
//        inMemoryUserDetailsManager.createUser(user)
//
//        return InMemoryUserDetailsManager()
//    }

//    @Bean
//    fun userDetailsService(): UserDetailsService {
//        val byeon = User.withUsername("byeon")
//            .password("12345")
//            .build()
//
//        val kim = User.withUsername("kim")
//            .password("12345")
//            .build()
//
//        val ken = User.withUsername("ken")
//            .password("12345")
//            .build()
//
//        val users = mutableListOf(byeon, kim, ken)
//
//        return InMemoryUserDetailsService(users)
//    }

    @Bean
    fun userDetailsService(dataSource: DataSource): UserDetailsService {
        return JdbcUserDetailsManager(dataSource)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return NoOpPasswordEncoder.getInstance()
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity) : SecurityFilterChain{
        httpSecurity
            .authorizeHttpRequests { auth ->
                auth.anyRequest().authenticated()
            }
            .httpBasic(withDefaults())

        return httpSecurity.build()
    }

}