# HTTP Basic 인증 방식

인증 프로세스가 실패했을 때, 특정 비즈니스 로직을 수행하도록 구현하려면 어떻게 해야할까?
- 인증 과정에서 클라이언트로 반환하는 값의 일부를 설정하기 위해서는 어떻게 해야할까?

SecurityFilterChain 을 설정
- HttpSecurity 를 파라키터로 받음
- HttpSecurity 의 httpBasic 을 설정

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity) : SecurityFilterChain {
    http.httpBasic(Customizer.withDefaults()) // 가장 기본 형식 설정
    return http.build()
}
```

## 인증이 실패했을 때의 응답 설정

AuthenticationEntryPoint 를 설정
- commence() 메소드는 HttpServletRequest 와 HttpServletResponse, AuthenticationException 를 파라미터로 받음
```java
public interface AuthenticationEntryPoint {
  void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException;
}
```

응답에 헤더를 추가하고 HTTP 상태를 401 Unauthorized 로 설정
- CustomEntryPoint 클래스를 생성하고 AuthenticationEntryPoint 인터페이스를 구현
- commence() 메소드를 오버라이드하여 응답에 헤더를 추가하고 HTTP 상태를 401 Unauthorized 로 설정
```kotlin
class CustomEntryPoint : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {
        response?.addHeader("message", "인증이 필요합니다.")
        response?.sendError(HttpStatus.UNAUTHORIZED.value())
    }
}
```

SecurityFilterChain 을 설정
```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity) : SecurityFilterChain {
    //http.httpBasic(Customizer.withDefaults())

    http.httpBasic {
        it.realmName("REALM_1")
        it.authenticationEntryPoint(CustomEntryPoint())
    }
    http.authorizeHttpRequests {
        it.anyRequest().authenticated()
    }

   return http.build()
}
```

## 양식 기반 로그인

일반적인 웹 애플리케이션에서는 사용자가 아이다와 패스워드를 직접 입력할 수 있는 UI 화면이 존재함
- UI 화면을 바탕으로 로그인을 하고 로그아웃을 하는 기능을제공함
- 양식 기반의 로그인 인증 방식을 할용할 수 있음

인증되지 않은 사용자가 /home 등 자격 증명이 필요한 URL 으로 연결을 시도하면 아래와 같은 작업이 실행된다.
- 먼저 로그인이 되어있는지 확인을 하고
- 로그인이 되어 있지 않다면(= 자격이 증명되지 않았다면) 로그인 양식 화면으로 redirect 한다.
- 만약 로그인이 되어 있다면 요청한 URL 로 연결을 허용한다

위 기능을 위해서는 .formLogin() 메소드를 활용한다.

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity) : SecurityFilterChain {
    http.formLogin(Customizer.withDefaults())
    http.authorizeHttpRequests {
        it.anyRequest().authenticated()
    }

    return http.build()
}
```

위 설정 적용후 /hello API 접근하려고 하면 다시 로그인 화면으로 redirect 된다.
- ID 는 user, 패스워드는 콘솔에 출력된 임시 패스워드를 입력하면 로그인이 성공한다.