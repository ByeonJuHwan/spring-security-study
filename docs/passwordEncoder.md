# PasswordEncoder

정말 간단하게 평문으로 비밀번호를 처리할수도 있겠지만 보통은 함호롸를 하여 비밀번호를 암호화 합니다.

이를 위해서는 spring security 에서 제공하는 PasswordEncoder 를 사용하면 됩니다.

![](https://velog.velcdn.com/images/asdcz11/post/800fb1c7-83f2-4a71-90e8-0bcfea3c44d1/image.png)

PasswordEncoder 는 앞전에 봤듯이 인터페이스로 이루어져 있어 2개의 메소드를 구현해야합니다. (1개는 default 메소드)

- encode(CharSequence rawPassword)
  - 주어진 암호의 해시를 제공
  - 암호화를 수행
- matches(CharSequence rawPassword, String encodedPassword)
  - 인코딩된 문자열이 원시 암호와 일치하는지 검증
- upgradeEncoding(String encodedPassword)
  - 암호화된 문자열이 업그레이드 되었는지 확인
  - 기본적으로 false 를 반환
  - true 를 반환하도록 메소드를 재정의하면 보안 향상을 위해 인코딩된 암호를 다시 인코딩 함

구현해야하는 2 개의 메소드는 서로 강결합이 되어 있기 대문에 두 메소드의 구현체는 기능상 서로 맞아야 합니다.
(encode() 에서 반환된 인코딩 문자열은 matches() 에서 검증이 가능해야함)

## AOP 기반 비밀번호 암호화

아래 암호화 요구사항은 어떻게 해결 가능할까요?
- API 를 호출하는 클라이언트는 평문으로 비밀번호를 입력해야한다
- 보안을 위해 서버는 비밀번호를 암호화하여 관리한다
- 암호화 알고리즘은 수시로 변경 될 수 있다

정말 간단하게 아래 코드와 같이 RequestBody 로 받게되면 어떨까??

당연히 비밀번호가 암호화도 안되고 그대로 노출되어 버린다.

```kotlin
data class HelloRequestBody(
    val id: String,
    val password: String,
)

@PostMapping("/hello")
fun hello(@RequestBody requestBody: HelloRequestBody): String {
    return "비밀번호 암호화"
}    
```
HelloRequestBody 로 평문 비밀번호를 입력 받은 다음 직접 암호화 로직을 수행시키면 이제 이 이후의 로직에서는 암호화된 비밀번호를 사용 가능하다

하지만, 단점으로는 모든 API 에서 아래와 같은 service 를 호출해서 암호화 해야한다는 담점이 있다.

```kotlin
@PostMapping("/hello")
fun hello(@RequestBody requestBody: HelloRequestBody): String {
    // 요청 받은 비밀번호를 암호화 해주는 로직
    val encrypted = encryptService.encrypt(requestBody.password)
    return "비밀번호 암호화"
}  
```
이러한 단점을 해결하기 위해 AOP 를 사용하여 비밀번호 암호화를 수행하는 방법이 있다.

1. 암호화하고 싶은 필드에 커스텀 어노테이션을 부여
2. API 요청이 들어온 시점을 AOP 를 통해 파알
3. Java reflection 을 통해 암호화하고 싶은 필드를 파악
4. 암호화 수행

어노테이션으로 암호화필드를 알기 위해서 아래와 같이 커스텀 어노테이션을 생성한다
- `@Target(AnnotationTarget.FIELD)`: 이 어노테이션은 필드에만 적용될 수 있음을 의미합니다.
- `@Retention(AnnotationRetention.RUNTIME)`: 이 어노테이션 정보가 런타임에도 유지되어 리플렉션을 통해 접근할 수 있음을 의미합니다.

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomEncryption()
```

만들어진 어노테이션을 사용하여 암호화하고 싶은 필드에 부여한뒤 Aspect 를 생성해서 메소드가 호출될때 AOP 가 해당 어노테이션이 붙은 필드를 암호화 할수 있도록 로직을 작성해 줍니다.

```kotlin
data class HelloRequestBody(
    val id: String,

    @CustomEncryption
    val password: String,
)
```
```kotlin
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
```

이후 테스트를 통해서 정상적으로 커스텀 어노테이션을 붙인 필드가 암호화 되는지 확인합니다.

```kotlin
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
```

![](https://velog.velcdn.com/images/asdcz11/post/3fd4f3dc-80c9-4f94-adc9-3d9190495aa7/image.png)

이렇게 AOP 를 사용하여 비밀번호 암호화를 수행하면 비밀번호 암호화 로직을 모든 API 에서 중복해서 작성하지 않아도 되고, 암호화 로직이 변경되어도 AOP 만 수정하면 되기 때문에 유지보수성이 높아집니다.