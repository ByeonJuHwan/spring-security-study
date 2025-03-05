package com.dev.security.config

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

/**
 * 항상 userDetailsService 를 정의하면
 */
@EnableAsync
@Configuration
class SecurityConfig () {

//    @Bean
//    fun passwordEncoder(): PasswordEncoder {
//        return NoOpPasswordEncoder.getInstance()
//    }
//
//    @Bean
//    fun userDetailsService(dataSource: DataSource): UserDetailsService {
//        val inMemoryUserDetailsManager = InMemoryUserDetailsManager()
//        inMemoryUserDetailsManager.createUser(
//            User.withUsername("byeon")
//                .password("12345")
//                .build()
//        )
//        return JdbcUserDetailsManager(dataSource)
//    }

    @Bean
    fun securityFilterChain(http: HttpSecurity) : SecurityFilterChain {
        //http.httpBasic(Customizer.withDefaults())

//        http.httpBasic {
//            it.realmName("REALM_1")
//            it.authenticationEntryPoint(CustomEntryPoint())
//        }

        http.formLogin(Customizer.withDefaults())
        http.authorizeHttpRequests {
            it.anyRequest().authenticated()
        }

        return http.build()
    }

    @Bean
    fun initializingBean() : InitializingBean {
        return InitializingBean {
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
        }
    }

}