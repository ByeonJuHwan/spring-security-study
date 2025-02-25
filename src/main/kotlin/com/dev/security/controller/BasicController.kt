package com.dev.security.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BasicController {

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, World!"
    }
}