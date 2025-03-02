# 스프링 시큐리티

스프링 시큐리티(Spring Security)는 자바 애플리케이션, 특히 스프링 기반 애플리케이션의 보안(인증과 권한 부여)을 담당하는 프레임워크입니다.
단순히 로그인 기능을 넘어 권한 관리, 세션 보안, CSRF 방어 등을 해주는 프레임 워크이기 때문에 개인적으로 공부를 시작하였습니다

## 스프링 시큐리티 기본 구성

스프링 시큐리티에서 필요한 인터페이스및 인터페이스를 상속받아 구현해야하는 메소들의 정리 및 실제 http 요청이 들어왔을때 어떻게 스프링 시큐리티에서 사용자를 인증하는지 정리했습니다.

[스프링 시큐리티 기본 구성](https://github.com/ByeonJuHwan/spring-security-study/blob/master/docs/spring-security-basic.md)

암호화가 필요한 항목의 경우 스프링 시큐리티에서 제공하는 PasswordEncoder 를 사용하여 암호화를 진행합니다.

API 요청으로 들어오는 필드중 암호화가 필요한 필드가 있다면 해당 암호화해야하는데 이를 AOP 를 통해서 어노테이션이 붙어 있는 필드만 처리할 수 있습니다.


[PasswordEncoder 로 필드 암호화 하기](https://github.com/ByeonJuHwan/spring-security-study/blob/master/docs/passwordEncoder.md)

AuthenticationProvider는 Spring Security에서 사용자 인증을 처리하는 핵심 인터페이스입니다. 
이는 사용자의 자격 증명(credentials)을 검증하고, 인증 성공 시 필요한 권한과 세부 정보가 포함된 Authentication 객체를 생성합니다.

[커스텀 AuthenticationProvider 구현](https://github.com/ByeonJuHwan/spring-security-study/blob/master/docs/authentication-provider.md)

위 내용들을 토대로 인증을 성공하면 인증된 사용자 정보는 SecurityContext 에 저장되고 요청이 들어올때 마다 SecurityContext 에서 인증 정보를 가져와 사용자 정보를 사용할 수 있습니다.

[SecurityContext 에서 인증 정보 가져오기]()