package com.dev.security.aop

import com.dev.security.request.HelloRequestBody
import com.dev.security.service.EncryptService
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
 class PasswordEncryptionAspectTest {

 @Mock
 private lateinit var encryptService: EncryptService

  @InjectMocks
  private lateinit var aspect: PasswordEncryptionAspect

 @Test
 fun passwordEncryptionAspect() {
  val requestBody = HelloRequestBody("id", "password")

  `when`(encryptService.encrypt(anyString())).thenReturn("encrypted password")

  //when
  aspect.fieldEncryption(requestBody)

  // then
  assertThat(requestBody.password).isEqualTo("encrypted password")
 }
}
