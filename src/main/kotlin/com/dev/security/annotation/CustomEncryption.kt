package com.dev.security.annotation

import java.lang.annotation.ElementType

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomEncryption()
