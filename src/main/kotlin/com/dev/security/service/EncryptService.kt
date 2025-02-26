package com.dev.security.service

import org.springframework.stereotype.Service

@Service
class EncryptService {
    fun encrypt(password: String): String {
        return "encrypted $password"
    }
}