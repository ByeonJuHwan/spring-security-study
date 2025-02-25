package com.dev.security.user

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ByeonUserTest {

    @Test
    @DisplayName("변 사용자 테스트")
    fun ByeonUserTest() {
        val byeon = ByeonUser()

        assertThat(byeon.username).isEqualTo("byeon")
        assertThat(byeon.password).isEqualTo("12345")
        assertThat(byeon.authorities.size).isEqualTo(1)

        val authority = byeon.authorities
            .stream()
            .filter{
                it.authority == "READ"
            }
            .findFirst()

        authority.ifPresent {
            assertThat(it.authority).isEqualTo("READ")
        }
    }
}