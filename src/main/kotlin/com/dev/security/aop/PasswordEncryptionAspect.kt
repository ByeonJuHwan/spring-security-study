package com.dev.security.aop

import com.dev.security.annotation.CustomEncryption
import com.dev.security.service.EncryptService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Component
@Aspect
class PasswordEncryptionAspect (
    private val encryptService: EncryptService,
) {

    @Around("@annotation(com.dev.security.annotation.CustomEncryption)")
    fun passwordEncryptionAspect(pjp: ProceedingJoinPoint): Any? {
        pjp.args.forEach (::fieldEncryption)
        return pjp.proceed()
    }

    fun fieldEncryption(obj: Any) {
        val clazz = obj.javaClass

        // 객체의 모든 필드를 검사
        clazz.declaredFields.forEach { field ->
            if(field.isAnnotationPresent(CustomEncryption::class.java)) {
                field.isAccessible = true

                // 필드가 String 타입인 경우에만 암호화
                val value = field.get(obj)
                if (value is String) {
                    val encryptedValue = encryptService.encrypt(value)
                    field.set(obj, encryptedValue)
                }

                field.isAccessible = false
            }
        }
    }
}