package com.dev.security.request

import com.dev.security.annotation.CustomEncryption

data class HelloRequestBody(
    val id: String,

    @CustomEncryption
    val password: String,
)
