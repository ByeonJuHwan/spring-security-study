## AuthenticationProvider

AuthenticationProvider 는 인증 논리를 담당하는 부분이다 즉, 요청을 허용할 건지 정하는 기능을 맡고있다.

AuthenticationManager 는 Http 필터 계층에서 요청을 수신하고 이 책임을 AuthenticationProvider 에게 위임한다.

두 가지 케이스로 정리가 가능한데
 - 사용자를 찾을 수 없음 : 애플리케이션이 사용자를 인식하지 못해 권한 부여 프로세스에 윔하지 않고 요청을 거부함
 - 사용자를 찾을 수 있음 : 사용자 정보가 저정되어 있기 때문에 애플리케이션이 이를 활용해 권한 부여를 할 수 있음.

### AuthenticationProvider 란??

AuthenticationProvider 는 맞춤형 인증 놀리를 정의할 수 있다.

예를 들어, 단순 비밀번호 기반의 인증 뿐만 아니라 지문, SMS 코드 등 다양한 방법으로 신원 증명을 할 수 있다
(어떤 인증방법이 필요 하더라도 이를 구현할 수 있도록 프레임워크에서 지원을 해준다)

Authentication : 인증이라는 의미를 가지고 있음
- 스프링 시큐리티에서 Authentication 은 인증 프로세스의 필수 인터페이스이다.
- 인증 요청 이벤트
- 애플리케이션에 접근을 요청한 엔티티의 세부 정보를 담고 있다

Principal : 주체라는 의미를 가지고 있음

Authentication, Principal 인터페이스를 살펴보면 Authentication 인터페이스는 Principal 인터페이스를 상속받고 있다.

```java
public interface Principal {
    
    public boolean equals(Object another);
    
    public String toString();
    
    public int hashCode();
    
    public String getName(); // 인증하고자 하는 사용자 이름을 반환한다 (아이디)
    
    public default boolean implies(Subject subject) {
        if (subject == null)
            return false;
        return subject.getPrincipals().contains(this);
    }
}
```
```java
/*
    Authentication 은 Principal 만 포함하는 것이 아니라 인증 프로세스의 완료 여부, 권한의 컬렉션 정보를 추가로 포함하고 있다.
 */

public interface Authentication extends Principal, Serializable {
  Collection<? extends GrantedAuthority> getAuthorities(); // 인증 후 사용자의 이용 권리와 권힌 반환

  Object getCredentials(); // 스프링 시큐리티에서의 인증은 사용자 암호가 있어야함 (비밀번호, 지문 등)

  Object getDetails(); // 사용자 요청에 대한 추가 세부 정보

  Object getPrincipal();

  boolean isAuthenticated(); // 사용자가 인증되었는지를 나타냄. 인증 프로세스가 끝났으면 true 아직 진행중이라면 false 반환

  void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
```

### AuthenticationProvider 기본 구현

AuthenticationProvider 인터페이스의 기본 구현
- 사용자를 찾는 UserDetailsService 에 위임
- PasswordEncoder 로 인증 프로세스에서 암호를 관리함
- Authentication 인터페이스와 강결합이 되어 있음

AuthenticationProvider 인터페이스를 살펴보면 아래와 같이 되어있다.

```java
public interface AuthenticationProvider {
  Authentication authenticate(Authentication authentication) throws AuthenticationException;

  boolean supports(Class<?> authentication);
}
```

- authenticate() : Authentication 객체를 파라미터로 받고 다시 Authentication 를 반환한다
    - 인증에 실패하면 AuthenticationException 을 던진다
    - AuthenticationProvider 구현체에서 지원되지 않는 인증 객체를 받으면 null 을 반환한다
    - 반환되는 Authentication 객체에는 인증돤 사용자의 필수 세부 정보가 포함되어있다.
- supports() : 현재 AuthenticationProvider 가 Authentication 객체로 제공된 형식을 지원하면 true 를 반환
  - 객체에 대해 true 를 반환하더라도  authenticate() 메소드가 null 을 반환하면 요청을 거부할 수 있음.

### AuthenticationProvider 와 AuthenticationManager

인증 요청을 허용하거나 거부하기 위해 AuthenticationManager 와 AuthenticationProvider 는 서로 연결되어 있다.
- AuthenticationManager 가 중앙에서 사용자를 허용할지 거부할지 판단하는 컨트롤 타워 역할을 한다.
- AuthenticationManager 는 AuthenticationProvider 에게 인증 작업을 위임하여 이를 판단한다

AuthenticationManager 는 사용 가능한 인증 공급자 중 하나에 인증을 위임한다.
- AuthenticationProvider 는 주어진 인증 유형을 지원하지 않거나, 객체 유형은 지원하지만 해당 특정 객체를 인증하는 방법은 모를수 있다.
- 인증을 평가한 후 요청이 올바른지 판단할 수 있는 AuthenticationProvider 가 AuthenticationManager 에 응답한다.

예를 들어, 열쇠와 카드로 잠금 장치를 여는 시스템이 있다고 하자.(열쇠와 카드는 인증 공급자)
- 열쇠 담당 인증 공급자는 카드 인증에 대해서는 처리할 수 없지만 카드 인증 공급자는 카드 인증에 대해 처리할 수 있음.
- 이 역살을 하기 위해 support 가 필요하다

카드 인증 공급자에 카드를 인증하더라도 인증할 수 없는 유형인 경우에 support 는 true 를 반환,  authenticate() 는 null 을 반환한다.

### 커스텀 AuthenticationProvider 구현
1. AuthenticationProvider 계약을 구현하는 클래스를 선언
2. 커스텀 AuthenticationProvider 가 어떤 종류의 Authentication 객체를 지원할지 결정 (supports(), authenticate()  메소드 구현)
3. 커스텀 AuthenticationProvider 구현 인스턴스를 스프링 시큐리티에 등록