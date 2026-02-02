# Springboot-base

여러 시스템에 범용적으로 활용할 수 있도록 기본 설정을 갖춘 Spring Boot REST API 백엔드 서버 템플릿 프로젝트.

새로운 프로젝트를 시작할 때 이 저장소를 기반으로 빠르게 개발 환경을 구성할 수 있습니다.

## 기술 스택

- **Java** 21
- **Spring Boot** 3.5.0
- **Spring Web** - REST API
- **Spring JDBC** - JDBC 데이터 액세스 (선택적 모듈)
- **Spring Data JPA** - ORM 데이터 액세스 (선택적 모듈)
- **QueryDSL** - 타입 안전 쿼리 (선택적 모듈)
- **Spring Security** - 인증/인가 (JWT 기반)
- **H2 Database** - 인메모리 데이터베이스
- **JWT (jjwt)** - JSON Web Token 인증
- **Validation** - Bean Validation (기본 포함)
- **Lombok** - 보일러플레이트 코드 제거 (기본 포함)
- **MapStruct** - Entity/DTO 변환 코드 생성 (기본 포함)
- **Actuator** - 모니터링/헬스체크 (선택적 모듈)
- **OpenAPI (springdoc)** - Swagger API 문서 (선택적 모듈)
- **Flyway** - DB 마이그레이션 (선택적 모듈)
- **Mail** - 이메일 발송 (선택적 모듈)
- **AOP** - AspectJ 기반 관점 지향 프로그래밍 (선택적 모듈)
- **WebSocket** - 실시간 양방향 통신 (선택적 모듈)
- **OAuth2 Resource Server** - OAuth2 JWT 리소스 서버 (선택적 모듈)
- **Thymeleaf** - 서버 사이드 템플릿 엔진 (선택적 모듈)
- **GraphQL** - GraphQL API (선택적 모듈)
- **Config Reload** - 설정 파일 변경 자동 리로드 (선택적 모듈)
- **P6Spy** - SQL 로그 파라미터 바인딩 (선택적 모듈)
- **DevTools** - 개발 시 자동 재시작/LiveReload (선택적 모듈)
- **Redis** - 캐싱 및 세션 관리 (선택적 모듈)
- **Kafka** - 메시지 브로커 (선택적 모듈)
- **RabbitMQ** - 메시지 큐 (선택적 모듈)
- **Gradle** (Kotlin DSL) - 빌드 도구
- **JUnit 5** - 테스트

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/spring/lica/
│   │   ├── LicaApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JdbcConfig.java
│   │   │   ├── JpaConfig.java
│   │   │   ├── QueryDslConfig.java
│   │   │   ├── ActuatorConfig.java
│   │   │   ├── OpenApiConfig.java
│   │   │   ├── FlywayConfig.java
│   │   │   ├── MailConfig.java
│   │   │   ├── AopConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── OAuth2Config.java
│   │   │   ├── ThymeleafConfig.java
│   │   │   ├── GraphQlConfig.java
│   │   │   ├── ConfigReloadConfig.java
│   │   │   ├── P6SpyConfig.java
│   │   │   ├── DevToolsConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   ├── KafkaConfig.java
│   │   │   └── RabbitMQConfig.java
│   │   ├── security/jwt/
│   │   │   ├── JwtProperties.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── admin/
│   │   │   ├── AdminProperties.java
│   │   │   ├── AdminSecurityConfig.java
│   │   │   ├── AdminController.java
│   │   │   ├── SettingsService.java
│   │   │   ├── PropertyEntry.java
│   │   │   └── PropertySection.java
│   │   └── messaging/
│   │       ├── kafka/
│   │       │   ├── KafkaProducerService.java
│   │       │   └── KafkaConsumerService.java
│   │       └── rabbitmq/
│   │           ├── RabbitMQProducerService.java
│   │           └── RabbitMQConsumerService.java
│   └── resources/
│       ├── application.properties
│       ├── logback-spring.xml
│       ├── static/
│       │   └── css/
│       │       └── admin.css
│       └── templates/
│           ├── login.html
│           └── settings.html
└── test/
    └── java/com/spring/lica/
        └── LicaApplicationTests.java
```

## 시작하기

### 요구사항

- JDK 21

### 빌드

```bash
./gradlew build
```

### 실행

```bash
./gradlew bootRun
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다.

### H2 콘솔

H2 데이터베이스 콘솔은 `http://localhost:8080/h2-console`에서 접근할 수 있습니다.

- JDBC URL: `jdbc:h2:mem:licadb`
- Username: `sa`
- Password: (비어 있음)

## JWT 인증

JWT 기반 Stateless 인증이 기본으로 활성화되어 있습니다.

- **공개 엔드포인트**: `/api/auth/**`, `/h2-console/**`
- **인증 필요**: 그 외 모든 엔드포인트
- **토큰 전달**: `Authorization: Bearer <token>` 헤더

### 설정 (`application.properties`)

```properties
jwt.secret=your-256-bit-secret-key-here-change-in-production
jwt.expiration=3600000
```

> 프로덕션 환경에서는 반드시 `jwt.secret` 값을 변경하세요.

## 선택적 모듈

모든 선택적 모듈은 `@ConditionalOnProperty`로 제어됩니다. `application.properties`에서 `enabled=true/false`로 변경하여 활성화/비활성화할 수 있습니다.

### 기본 포함 라이브러리

다음 라이브러리는 토글 없이 항상 포함됩니다:

- **Validation** (`spring-boot-starter-validation`) - `@Valid`, `@NotBlank`, `@Size` 등 Bean Validation
- **Lombok** - `@Getter`, `@Builder`, `@Slf4j` 등 보일러플레이트 제거
- **MapStruct** (`1.5.5.Final`) - Entity/DTO 변환 매퍼 코드 자동 생성 (컴파일 타임)
- **Configuration Processor** - `@ConfigurationProperties` IDE 자동완성 지원

### JDBC

```properties
app.module.jdbc.enabled=true
```

기본 활성화. `DataSource`, `JdbcTemplate`, `TransactionManager`, H2 콘솔이 자동 구성됩니다. JPA 없이 순수 JDBC만 사용할 때도 이 모듈만 활성화하면 됩니다.

### JPA

```properties
app.module.jpa.enabled=true
```

기본 활성화. Hibernate JPA 자동 구성 및 `@EnableJpaRepositories`가 활성화됩니다. JDBC 모듈이 함께 활성화되어야 합니다.

### QueryDSL

```properties
app.module.querydsl.enabled=true
```

기본 비활성화. 활성화 시 `JPAQueryFactory` 빈이 등록됩니다. JPA 모듈이 함께 활성화되어야 합니다.

### Actuator

```properties
app.module.actuator.enabled=true
```

기본 비활성화. 활성화 시 `/actuator/health`, `/actuator/info` 등 모니터링 엔드포인트가 노출됩니다. `management.endpoints.web.exposure.include`로 노출할 엔드포인트를 조정할 수 있습니다.

### OpenAPI (Swagger)

```properties
app.module.openapi.enabled=true
```

기본 비활성화. 활성화 시 `/swagger-ui/index.html`에서 API 문서를 확인할 수 있습니다. `springdoc.api-docs.enabled`와 `springdoc.swagger-ui.enabled` 속성과 연동됩니다.

### Flyway

```properties
app.module.flyway.enabled=true
```

기본 비활성화. 활성화 시 `src/main/resources/db/migration/` 경로의 SQL 스크립트로 DB 스키마를 관리합니다. Flyway 사용 시 `spring.jpa.hibernate.ddl-auto`를 `validate` 또는 `none`으로 변경하세요.

### Mail

```properties
app.module.mail.enabled=true
spring.mail.host=smtp.example.com
spring.mail.port=587
```

기본 비활성화. 활성화 시 `JavaMailSender` 빈이 자동 구성됩니다.

### AOP

```properties
app.module.aop.enabled=true
```

기본 비활성화. 활성화 시 `@Aspect`, `@EnableAspectJAutoProxy`를 사용한 커스텀 AOP가 동작합니다. 참고: `@Transactional` 등 Spring 내장 AOP는 이 모듈과 무관하게 동작합니다.

### WebSocket

```properties
app.module.websocket.enabled=true
```

기본 비활성화. 활성화 시 JSR 356 WebSocket 및 STOMP 메시징을 사용할 수 있습니다.

### OAuth2 Resource Server

```properties
app.module.oauth2.enabled=true
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-auth-server.com
```

기본 비활성화. 활성화 시 외부 IdP(Keycloak, Auth0 등)의 JWT 토큰을 검증하는 리소스 서버가 구성됩니다.

### Thymeleaf

```properties
app.module.thymeleaf.enabled=true
```

기본 비활성화. 활성화 시 Thymeleaf 템플릿 엔진이 구성됩니다. `src/main/resources/templates/` 경로의 `.html` 파일을 뷰로 사용합니다.

### GraphQL

```properties
app.module.graphql.enabled=true
```

기본 비활성화. 활성화 시 `/graphql` 엔드포인트가 구성됩니다. 스키마 파일은 `src/main/resources/graphql/` 경로에 `.graphqls` 확장자로 작성합니다.

### Config Reload (설정 자동 리로드)

```properties
app.module.config-reload.enabled=true
app.module.config-reload.watch-path=./application.properties
app.module.config-reload.interval=5000
```

기본 비활성화. Spring Cloud Context의 `@RefreshScope`를 활용하여 설정 파일 변경 시 해당 빈을 자동으로 재생성합니다.

- `@RefreshScope` 선언된 빈 → 설정 변경 시 자동 재생성
- `@ConfigurationProperties` 빈 → 속성 자동 리바인딩
- `watch-path` 설정 시 → 파일 변경 자동 감지 (주기: `interval`ms)
- `watch-path` 미설정 시 → `POST /actuator/refresh`로 수동 트리거 (Actuator 모듈 필요)

### P6Spy

```properties
app.module.p6spy.enabled=true
```

기본 비활성화. 활성화 시 SQL 쿼리 로그에 바인딩 파라미터 값이 함께 출력됩니다. 개발/디버깅 용도로 사용하세요.

### DevTools

```properties
app.module.devtools.enabled=true
```

기본 활성화. 개발 시 코드 변경 감지 자동 재시작 및 LiveReload를 제공합니다. 프로덕션 JAR에서는 자동으로 비활성화됩니다.

### Redis (캐싱 + 세션)

```properties
app.module.redis.enabled=true
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

활성화 시 `RedisTemplate`, `CacheManager`, Redis HTTP 세션이 자동 구성됩니다.

### Kafka

```properties
app.module.kafka.enabled=true
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=lica-group
```

활성화 시 `KafkaTemplate`, `KafkaListenerContainerFactory`, Producer/Consumer 서비스가 구성됩니다.

### RabbitMQ

```properties
app.module.rabbitmq.enabled=true
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

활성화 시 Queue(`lica-queue`), TopicExchange(`lica-exchange`), Producer/Consumer 서비스가 구성됩니다.

## Admin 설정 UI

웹 기반 관리 페이지에서 `application.properties`의 모든 설정을 읽고 수정할 수 있습니다.

### 접속

- **로그인 페이지**: `http://localhost:8080/login`
- **설정 페이지**: `http://localhost:8080/admin/settings` (로그인 필요)

### 기본 계정

```properties
app.admin.username=admin
app.admin.password=admin
```

### 설정

```properties
app.admin.username=admin
app.admin.password=admin
app.admin.settings-file=src/main/resources/application.properties
```

- `settings-file`: 읽고 쓸 설정 파일 경로

### 동작 방식

1. 로그인 페이지에서 인증 후 설정 페이지로 이동
2. 설정 페이지에서 모든 프로퍼티를 섹션별로 확인/수정 가능
3. boolean 값(`true`/`false`)은 드롭다운으로, 그 외 값은 텍스트 입력으로 표시
4. **Save** 버튼 클릭 시 파일 저장 후 Config Reload 모듈이 활성화되어 있으면 자동으로 설정이 리로드됨

### 보안

- Admin UI는 세션 기반 폼 로그인(`@Order(1)`)으로 동작하며, API용 JWT 인증(`@Order(2)`)과 독립적으로 구성됩니다
- `/admin/**` 경로는 `ROLE_ADMIN` 권한이 필요합니다
- Thymeleaf 모듈(`app.module.thymeleaf.enabled=true`)이 활성화되어야 합니다

> 프로덕션 환경에서는 반드시 `app.admin.password`를 변경하세요.

## 로깅

`logback-spring.xml`로 로그 포맷과 출력 대상이 설정되어 있습니다.

### 로그 포맷

```
2026-01-15 10:30:45.123 DEBUG [http-nio-8080-exec-1] com.spring.lica.config.SecurityConfig    : message
```

### 프로파일별 동작

| Profile | 출력 대상 |
|---|---|
| `default` / `local` / `dev` | Console (컬러) |
| `prod` | Console + `./logs/{앱명}.log` + `./logs/{앱명}-error.log` |

파일 로그: 일별 로테이션, 파일당 100MB, 30일 보관, 총 1GB 제한

### SQL 로깅

Hibernate SQL과 바인딩 파라미터가 logback 포맷으로 출력됩니다:

```
DEBUG org.hibernate.SQL                          : select u1_0.id, u1_0.name from users u1_0 where u1_0.id=?
TRACE org.hibernate.orm.jdbc.bind                : binding parameter (1:BIGINT) <- [42]
```

`spring.jpa.properties.hibernate.format_sql=true`로 SQL이 정렬되어 출력됩니다.

## 라이선스

[MIT License](LICENSE)
