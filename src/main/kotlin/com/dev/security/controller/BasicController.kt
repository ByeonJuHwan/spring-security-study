package com.dev.security.controller

import com.dev.security.request.HelloRequestBody
import com.dev.security.service.EncryptService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BasicController (
    private val encryptService: EncryptService,
) {

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, World!"
    }

    @PostMapping("/hello")
    fun hello(@RequestBody requestBody: HelloRequestBody): String {
        val encrypted = encryptService.encrypt(requestBody.password)
        return "비밀번호 암호화"
    }
}