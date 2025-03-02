package com.dev.security.controller

import com.dev.security.request.HelloRequestBody
import com.dev.security.service.EncryptService
import org.springframework.scheduling.annotation.Async
import org.springframework.security.concurrent.DelegatingSecurityContextCallable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

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

    @Async
    @GetMapping("/api/v1/hello")
    fun getSecurityContext(): CompletableFuture<String> {
        val context = SecurityContextHolder.getContext();
        val userName = context.authentication.name
        return CompletableFuture.completedFuture("Hello, $userName")
    }

    @GetMapping("/api/v2/hello")
    fun getSecurityContext2(authentication: Authentication): String {
        return "Hello, ${authentication.name}"
    }

    @GetMapping("/api/v3/hello")
    fun getSecurityContext3(): String {
        val task = Callable {
            val context = SecurityContextHolder.getContext()
            context.authentication.name
        }

        val executorService = Executors.newSingleThreadExecutor()
        try {
            val callable = DelegatingSecurityContextCallable(task)
            return "hello v3, ${executorService.submit(callable).get()}"
        } finally {
            executorService.shutdown()
        }
    }
}