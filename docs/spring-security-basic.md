# 스프링 시큐리티 기본 구조

![](https://velog.velcdn.com/images/asdcz11/post/4fca4015-cc09-4fbf-8d3d-baf0c960b987/image.png)

- 인증필터가 요청을 가로챔 (intercept)
  - 인증필터는 인증 요청을 가로채 인증 관리자에게 위임

- 인증관리자는 인증 논리를 구현하는 인증 공급자를 이용하여 인증을 처리함

- 인증 인증공급자는 사용자 관리 책임을 구현하는 사용자 세부 정보 서비스를 인증논리에 이용하고 암호 관리르 구현하는 암호 인코더를 인증 논리에 이용함

- 사용자 세부 정보 서비스는 사용자를 찾을 수 있도록 하며 암호 인코더는 암호를 검증하며 그 인증결과가 반환된다. 
  - 사용자 세부 정보 서비스 (UserDetailsService)
  - 암호 인코더 (PasswordEncoder)

- 인증된 엔티티에 대한 세부 정보가 보안 컨텍스트에 저장되며 인증 데이터를 유지한다 (다시 언제든 꺼내서 쓸수 있다)


## UserDetailsService

UserDetailsService 를 구현하는 구현체가 사용자에 대한 정보를 관리한다.
- 스프링 시큐리티에서 제공하는 기본 구현체는 내부 메모리 (InMemory) 에 기본 자격 증명을 등록한다
- 기본 자격 증명의 사용자 이름으로 user 를 사용한다
- user 는 스프링 컨텍스트가 로드될 때, 자동으로 생성된다
- **내부 메모리에서 자격 증명을 관리하는것은 절대로 운영환경에서는 사용하면 안된다!!**

## PasswordEncoder

PasswordEncoder 는 크게 두 가지 역할을 수행
- 암호를 인코딩
- 암호가 기존 인코딩과 일치하는지 확인

실제로 PasswordEncoder 안터페이스를 살펴보면 두 가지 메소드를 구현하도록 되어있다.

```java
public interface PasswordEncoder {
  // 암호를 인코딩
  String encode(CharSequence rawPassword);

  // 암소가 기존 인코딩과 일치하는지 확인
  boolean matches(CharSequence rawPassword, String encodedPassword);

  default boolean upgradeEncoding(String encodedPassword) {
    return false;
  }
}
```

**반드시 지켜야 하는 점은 UserDetailsService 의 구현을 변경하면 PasswordEncoder 역시 지정정을 꼭 해줘야한다**


## UserDetails

유저 정보를 관리하기 위해서는 UserDetails 가 필요하다
- 인터페이스를 이요하여 효과적으로 설계가 이미 되어있다.

사용자 관리르 위한 인터페이스로는 아래와 같은 2개의 인터페이스가 있다

- UserDetailsService (Read)
- UserDetailsManager (Create, Update, Delete)

두 개의 이터페이스가 분리되어 있는 이유는 인터페이스 분리 법칙 때문이다

**따라서 , Read, Write 를 분리해 두었기 때문에 프레임 워크의 유연성이 향상되었다.**

- 사용자를 인증하는 기능만 필요하면 UserDetailsService 인터페이스만 구현하면 된다.
- 추가, 수정, 삭제 등 더 많은 기능을 제공해야 한다면 UserDetailsManager 를 함께 구현하면 된다.


### UserDetailsService

username 을 파라미터로 받아 UserDetails 객체를 반환하는 loadUserByUsername 메소드를 구현해야 한다.

```java
public interface UserDetailsService {
  UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

여기서 username 은 사용자 ID 와 같은 개념이며 고유하다고 간주한다

반환하는 사용자는 UserDetails 이며 사용자가 존재하지 않으면 UsernameNotFoundException 예외를 발생시킨다


### UserDetailsManager

사용자 생성, 수정, 삭제, 암호 변경, 사용자 존재 여부 확인 메소드를 제공한다
- UserDetailsManager 는 UserDetailsService 를 상속받아 구현하고 있기 때문에 UserDetailsService 까지 구현해야 한다.

```java
public interface UserDetailsManager extends UserDetailsService {
  void createUser(UserDetails user);

  void updateUser(UserDetails user);

  void deleteUser(String username);

  void changePassword(String oldPassword, String newPassword);

  boolean userExists(String username);
}
```

## GrantedAuthority

사용자 권한 집합이라고 보면 된다. 

사용자는 하나 이상의 권한을 가질 수 있으며 GrantedAuthority 라는 인터페이스를 통해 이를 구현 가능하다

```java
public interface GrantedAuthority extends Serializable {
  String getAuthority();
}
```

### 각 인터페이스 간의 관계

- UserDetailsService 는 사용자 이름으로 조회한 사용자 세부 정보인 UserDetails 를 반환한다.
- 사용자는 하나 이상의 권한을 가질수 있으며 이는 GrantedAuthority 로 구현이 가능하다.

![](https://velog.velcdn.com/images/asdcz11/post/e27024bb-57ac-471b-9d6e-31757cdaa2f9/image.png)

### UserDetails

사용자의 권한, 암호, ID 등을 포함하는 사용자 정보를 나타내며 UserDetails 인터페이스를 구현해야 한다.

하단의 4개의 메소드는 이미 구현 되어있으므로 필요할때 상황에 맞춰서 구현하면 된다.

```java
public interface UserDetails extends Serializable {
  Collection<? extends GrantedAuthority> getAuthorities();

  String getPassword();

  String getUsername();

  default boolean isAccountNonExpired() {
    return true;
  }

  default boolean isAccountNonLocked() {
    return true;
  }

  default boolean isCredentialsNonExpired() {
    return true;
  }

  default boolean isEnabled() {
    return true;
  }
}
```

## DB 를 사용한 사용자 관리

데이터베이스에서 사용자를 관리하는 경우에는 JdbcUserDetailsManager 를 활용 할 수 있다.

JdbcUserDetailsManager 는 UserDetailsManager 를 구현하고 있으며, UserDetailsService 를 상속받아 구현하고 있다.

```kotlin
@Bean
fun userDetailsService(dataSource: DataSource): UserDetailsService {
    return JdbcUserDetailsManager(dataSource)
}
```

실제 JdbcUserDetailsManager 를 확인해보면 이미 사용자를 관리하는 쿼리가 전부 작성되어있다.

하지만 기본 테이블 값이 ```users``` 이므로 테이블 이름이 ```users``` 가 아니라면 정상적으로 동작하지 않는다.

![](https://velog.velcdn.com/images/asdcz11/post/da5d93d3-bdc0-4f04-a9ac-950ce4414d82/image.png)

물론 DB에 사용자 정보가 미리 들어있어야 아래와같이 포스트맨 요청상 200 응답을 받을 수 있다.

![](https://velog.velcdn.com/images/asdcz11/post/84f457db-336a-409d-a64a-8d96f64c992e/image.png)