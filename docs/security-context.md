# Security Context

**사용자 인증 정보는 어디에 저장할까??**
 
인증 프로세스가 완료되면 인증 엔티티에 대한 정보를 얻을 수 있다.
- AuthenticationManager 는 인증 프로세스를 성공적으로 완료한 후 요청이 유지되는 동안 Authentication 인스턴스를 저장한다.
- 저장하는 위치가 보안 컨텍스트 (security context) 라고 한다.

**보안 컨텍스트에 저장되기까지의 흐름**

인증 프로세스가 완료되면 보안 컨텍스트에 인증 정보가 담기게 되고 컨트롤러에서는 보안 컨텍스트에서 정보를 이용할 수 있음.
![](https://velog.velcdn.com/images/asdcz11/post/5a1524be-7b35-42cc-9c53-706c5ae7d47f/image.png)

보안 컨텍스트 인터페이스

SecurityContext 는 인터페이스로 두 개의 메소드를 제공한다.

- getAuthentication() : 현재 사용자의 인증 정보를 반환한다. 인증되지 않은 경우 null 을 반환한다.
- setAuthentication(Authentication authentication) : 현재 사용자의 인증 정보를 설정한다.

SecurityContext 의 역할은 인증, Authentication 을 저장하고 관리하는 것이다.

```java
public interface SecurityContext extends Serializable {
  Authentication getAuthentication();

  void setAuthentication(Authentication authentication);
}
```

### SecurityContext 를 관리하는 세 가지 전략

SecurityContext 는 세 가지 전략에 의해 관리되며 관리자 역할을 하는 객체를 SecurityContextHolder 라고 한다.

1. MODE_THREADLOCAL 
- 기본 전략으로 현재 스레드에 대한 SecurityContext 를 저장한다.
- 각 스레드가 보안 컨텍스트에 각자의 세부 정보를 저장할 수 있게 해준다.
- 일반적인 웹 애프리케이션에서는 각 요청이 개별 스레드를 가지므로 이는 일반적인 접근법

2. MODE_INHERITABLETHREADLOCAL
- 비동기 메소드의 경우, 보안 컨텍스트를 다음 스레드로 복사하도록 스프링 시큐리티에 알려준다.
- 이 방식을 사용하면 @Async 메소드를 실행하는 새로운 스레드가 보안 컨텍스트를 상속하게 할 수 있다.

3. MODE_GLOBAL
- 애플리케이션의 모든 스레드가 같은 보안 컨텍스트 인스턴스를 참조함

**SecurityContext 를 관리하는 기본 전략**

MODE_THREADLOCAL 은 SecurityContext 를 관리하는 기본 전략으로 ThreadLocal 을 활용한다.

- ThreadLocal 은 JDK 에 있는 구현으로 애플리케이션의 각 스레드가 저장된 데이터만 볼 수 있도록 보장한다.
- 즉, 다른 스레드의 ThreadLocal 에는 접근할 수 없고 자신의 보안 컨텍스트에만 접근 가능하다.
- 각 요청은 자신의 할당된 스레드를 가지며 자신의 보안 컨텍스트에 저장된 인증 세부 정보만 참조 할 수 있다.

**만약 새로운 스레드가 생성되는 경우에는 어떻게 동작할까? (비동기 메소드가 호출되어 새로운 스레드가 생기는경우)**

- 새로운 스레드 역시 자체 보안 컨텍스트를 가지며 기존 스레드에 있던 정보는 복사되지 않는다.

### SecurityContext 에서 인증 정보 가져오기

먼저 UserDetailsService 에 사용자 정보를 등록한다

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder {
    return NoOpPasswordEncoder.getInstance()
}

@Bean
fun userDetailsService(): UserDetailsService {
    val inMemoryUserDetailsManager = InMemoryUserDetailsManager()
    inMemoryUserDetailsManager.createUser(
        User.withUsername("byeon")
            .password("12345")
            .build()
    )
    return inMemoryUserDetailsManager
}
```

SecurityContextHolder 를 이용하여 SecurityContext 에서 인증 정보를 가져올 수 있다.

```kotlin
@GetMapping("/api/v1/hello")
fun getSecurityContext(): String {
    val context = SecurityContextHolder.getContext();
    val authentication = context.authentication
    return "Hello, ${authentication.name}"
}

// SecurityContextHolder 를 이용하지 않고 직접 Authentication 을 받아올 수 있다.
@GetMapping("/api/v2/hello")
fun getSecurityContext2(authentication: Authentication): String {
    return "Hello, ${authentication.name}"
}
```

### 비곧기 호출

엔드포인트가 비동기가 되는 경우
- 메소드를 실행하는 스레드와 요청을 수행하는 스레드가 다른스레드가 된다.

@Async 어노테이션을 사용하여 비동기로 API 를 실행시켜보자

```kotlin
@Async
@GetMapping("/api/v1/hello")
fun getSecurityContext(): CompletableFuture<String> {
    val context = SecurityContextHolder.getContext();
    val userName = context.authentication.name
    return CompletableFuture.completedFuture("Hello, $userName")
}
```

위와 같이 비동기로 메소드를 실행시 NPE 가 발생하게 되는데 메소드가 보안컨텍스트를 상속하지 않는 다른 스레드에서 실행되기 때문이다.
- Authentication 은 null 이기 때문에 getName() 을 호출하면 NPE 가 발생한다.
  ![](https://velog.velcdn.com/images/asdcz11/post/c23a5e56-ef96-452a-b489-9df45d1ca7b8/image.png)

어떻게 해결할 수 있을까?

해결방법은 MODE_INHERITABLETHREADLOCAL 을 사용하는 것이다.

```kotlin
@Bean
fun initializingBean() : InitializingBean {
    return InitializingBean {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }
}
```

**주의할 점**
- 프레임워크 자체적으로 스레드를 만들 때 작동삼 (@Async)
- 직접 스레드를 만드는 경우에는 이 방법이 작동하지 않음
- 프레임워크가 직접 생성한 스레드에 대해서는 알 수 없음

**MODE_GLOBAL**

모든 스레드가 동일한 보안 컨텍스트를 참조한다.
- 모든 스레드가 같은 데이터에 접근하고 해당 정보를 변경할수 있다는 의미
- 경합 상황이 발생할 수 있기 때문에 동기촤 처리가 필요하다

보안 컨텍스트는 thread safe 를 지원하지 않기 때문에 직접 동시성 이슈 처리를 해줘야함

### 자체 관리 스레드

프레임워크에서 관리하지 않고 개발자가 직접 스레드를 만드는 것을 자체 관리 스레드라고 한다.
- MODE_INHERITABLETHREADLOCAL 설정으로도 해결할 수 없다고 했는데 어떻게 해결할 수 있을까?

`DelegatingSecurityContextRunnable` 를 활용해서 해결가능하다.
- 별도의 스레드에서 실행하고 싶은 작업을 `DelegatingSecurityContextRunnable` 로 감싸는 방법이 있다
- Runnable 을 확장하여 반환 값이 없느 작업을 실행하고 이용할 수 있음
- 반환값이 존재하는 경우에는 `DelegatingSecurityContextCallable` 을 사용하면 된다.

```kotlin
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
```