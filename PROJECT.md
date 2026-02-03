# GovLICA - 공공데이터 API 탐색기 (Public Data API Explorer)

## 1. 프로젝트 개요

공공데이터포털(data.go.kr)에 등록된 REST API들을 검색·조회하고, Swagger와 유사한 인터페이스로 API 명세를 시각화하며, 샘플 데이터를 직접 테스트해볼 수 있는 웹 서비스.

### 핵심 가치
- 공공데이터 REST API를 **한눈에 검색**하고 필터링
- 각 API의 요청/응답 스펙을 **Swagger-like UI**로 시각화
- 인증키만 있으면 **즉시 테스트** 가능한 API 플레이그라운드

---

## 2. 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                     Browser (Client)                     │
│  ┌──────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │ API 검색  │  │ API 상세/명세 │  │ API 테스트 콘솔  │  │
│  └─────┬────┘  └──────┬───────┘  └─────────┬─────────┘  │
│        │     auth.js (JWT)                  │            │
└────────┼──────────────┼────────────────────┼────────────┘
         │              │                    │
    ─────┼──────────────┼────────────────────┼─────────────
         ▼              ▼                    ▼
┌─────────────────────────────────────────────────────────┐
│                  GovLICA Spring Boot API                  │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ Catalog      │  │ CatalogDetail│  │ API Proxy      │  │
│  │ Service      │  │ Service      │  │ Service        │  │
│  │ (목록조회)   │  │ (명세파싱)   │  │ (테스트중계)   │  │
│  └──────┬──────┘  └──────┬───────┘  └───────┬────────┘  │
│         │                │                   │           │
│  ┌──────┴──────────────┴───────────────────┘           │
│  │              DataGoKr Client                          │
│  │  (포털 HTML 스크래핑 + Swagger JSON 파싱)             │
│  └──────┬───────────────────────────────────────────┐   │
│         │                                            │   │
│  ┌──────┴──────┐  ┌─────────────┐  ┌──────────────┐│   │
│  │ H2 In-Mem   │  │ Auth/User   │  │ ServiceKey   ││   │
│  │ (JPA)       │  │ (JWT+BCrypt)│  │ (AES-GCM)    ││   │
│  └─────────────┘  └─────────────┘  └──────────────┘│   │
│                                                      │   │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐│   │
│  │ Favorites   │  │ TestHistory │  │ Admin Stats  ││   │
│  └─────────────┘  └─────────────┘  └──────────────┘│   │
└──────────────────────────────────────────────────────┘   │
         │                                                  │
    ─────┼──────────────────────────────────────────────────┘
         ▼
┌─────────────────────────────────────────────────────────┐
│              data.go.kr (공공데이터포털)                  │
│  ┌─────────────────┐  ┌──────────────────────────────┐  │
│  │ 웹 검색 (HTML)   │  │ 개별 공공데이터 REST API     │  │
│  │ + Swagger JSON   │  │ (실제 데이터 API)            │  │
│  └─────────────────┘  └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 3. 핵심 기능

### 3.1 API 카탈로그 검색 (Phase 1) ✅
- data.go.kr 웹 페이지를 HTML 스크래핑하여 REST API 목록 조회
- **검색 필터**: 키워드 검색
- **이중 소스**: `portal` (data.go.kr 실시간) / `local` (로컬 DB 동기화 캐시)
- **결과 표시**: 카드형 그리드 레이아웃, Per Page 조절
- 페이지네이션 지원
- 로컬 DB Sync 기능 (수동 트리거)

### 3.2 API 상세 명세 뷰어 (Phase 2) ✅
- 선택한 API의 상세 페이지에서 Swagger JSON 파싱
- **2단계 파싱 전략**:
  1. `swaggerOprtinVOs` 배열 파싱 (data.go.kr 전용, 가장 풍부한 데이터)
     - `reqList`: 요청 파라미터 (기본값 `paramtrBassValue` 포함)
     - `resList`: 응답 필드 (중첩 `subParam` 재귀 평탄화)
     - `oprtinUrl`: 실제 엔드포인트 URL
  2. 표준 Swagger paths 파싱 (fallback, path-level 파라미터 병합 + `$ref` 해석)
- **Swagger-like UI**:
  - HTTP 메서드 컬러 배지 (GET=green, POST=blue 등)
  - 오퍼레이션별 접기/펼치기 (accordion)
  - 요청 파라미터 테이블 (이름, 타입, 필수여부, 설명, 기본값)
  - 응답 필드 테이블 (이름, 타입, 설명)
- Refresh 기능: data.go.kr에서 최신 명세 재파싱
- API별 즐겨찾기 토글 (로그인 시)

### 3.3 API 테스트 콘솔 (Phase 3) ✅
- 사용자가 자신의 serviceKey를 입력하여 API 직접 호출
- 요청 파라미터를 폼으로 입력 (명세 기반 자동 생성, 기본값 자동 채움)
- 실시간 응답 결과를 JSON/XML 뷰어로 표시 (구문 강조)
- 응답 상태코드, 응답시간(ms) 표시
- cURL 명령어 자동 생성 및 복사
- 복수 오퍼레이션 선택 시 파라미터 그룹 자동 전환
- 커스텀 파라미터 추가 기능
- serviceKey localStorage 기억 기능
- 저장된 키 선택 드롭다운 (로그인 시, AES 복호화)

### 3.4 사용자 관리 (Phase 4) ✅
- 회원가입 / 로그인 (JWT 기반, BCrypt 비밀번호 해싱)
- 사용자별 serviceKey 저장·관리 (AES-GCM 암호화, 마스킹 표시)
- 즐겨찾기 API 목록 (추가/삭제/체크)
- 테스트 히스토리 조회 (페이지네이션)
- 마이페이지: 3탭 구조 (Service Keys / Favorites / History)
- 로그인/닉네임 Nav바 자동 업데이트 (auth.js)
- Admin 대시보드: 사용자 수, API 수, 테스트 통계, Sync 버튼

---

## 4. 기술 스택

### 핵심 모듈
| 모듈 | 용도 | 상태 |
|------|------|------|
| **Spring Boot 3.5.0** | 애플리케이션 프레임워크 | ✅ active |
| **Java 21** | 런타임 | ✅ active |
| **JPA (Hibernate)** | 엔티티 매핑, 데이터 영속화 | ✅ active |
| **H2 Database** | 인메모리 DB (개발용) | ✅ active |
| **Thymeleaf** | 서버 사이드 렌더링 | ✅ active |
| **Spring Security** | 2-tier: Admin(form) + API(JWT stateless) | ✅ active |
| **JWT (jjwt 0.12.6)** | 사용자 인증 토큰 | ✅ active |
| **Validation** | 입력값 검증 (@Valid, @NotBlank 등) | ✅ active |
| **WebFlux (WebClient)** | data.go.kr API 비동기 호출 | ✅ active |
| **Jsoup 1.18.3** | data.go.kr HTML 스크래핑 | ✅ active |
| **Jackson XML** | XML 응답 파싱 | ✅ active |
| **Lombok** | 보일러플레이트 제거 | ✅ active |

### 대기 모듈 (application.properties에서 비활성화)
| 모듈 | 상태 |
|------|------|
| QueryDSL, GraphQL, Flyway, Mail, AOP, WebSocket, OAuth2, P6Spy, Redis, Kafka, RabbitMQ | disabled |

---

## 5. data.go.kr 연동 전략

### 5.1 카탈로그 검색 (HTML 스크래핑)

인증키 불필요. data.go.kr 웹 검색 결과 HTML을 파싱하여 API 목록 수집.

```
DataGoKrClient.searchRestApis(page, pageSize, keyword)
  → HTML scraping → PortalApiResponse (ApiItem list)
```

### 5.2 API 상세정보 조회 (Swagger JSON 파싱)

개별 API 상세 페이지에서 JavaScript 변수로 삽입된 Swagger JSON을 추출.

**파싱 우선순위**:
1. **`swaggerOprtinVOs`** (data.go.kr 전용 확장)
   - 가장 풍부한 데이터: 요청 파라미터 기본값, 응답 필드 중첩 구조, 실제 엔드포인트 URL
   - `var swaggerOprtinVOs = [...]` 패턴 매칭
2. **표준 Swagger paths** (fallback)
   - `var swaggerJson = '...'` 또는 `` `...` `` 패턴 매칭
   - path-level 파라미터를 operation-level로 병합
   - `$ref` → `#/definitions/...` 해석

### 5.3 API 프록시 (테스트 콘솔)

브라우저 CORS 우회를 위한 서버 프록시.

```
Browser → POST /api/proxy/execute → GovLICA Server → data.go.kr API
                                      ↓
                             응답 + 히스토리 저장 (로그인 시)
```

**보안**:
- 화이트리스트 도메인만 허용 (`apis.data.go.kr`)
- serviceKey는 서버 메모리에서만 처리, 로그에서 마스킹
- cURL 출력에서 serviceKey를 `YOUR_SERVICE_KEY`로 치환

---

## 6. 데이터 모델 (ERD)

```
┌─────────────────────┐     ┌──────────────────────────┐
│ users                │     │ api_catalog              │
├─────────────────────┤     ├──────────────────────────┤
│ id (PK)             │     │ id (PK)                  │
│ email (UNIQUE)      │     │ uddi_seq (UNIQUE)        │
│ password (BCrypt)   │     │ api_name                 │
│ nickname            │     │ description              │
│ created_at          │     │ provider_org             │
│ updated_at          │     │ category                 │
└────────┬────────────┘     │ service_type (REST 등)   │
         │                  │ data_format (JSON/XML)   │
         │                  │ endpoint_url             │
         │                  │ last_synced_at           │
         │                  │ created_at / updated_at  │
         │                  └────────────┬─────────────┘
         │                               │
┌────────┴────────────┐     ┌────────────┴─────────────┐
│ user_service_keys   │     │ api_operations           │
├─────────────────────┤     ├──────────────────────────┤
│ id (PK)             │     │ id (PK)                  │
│ user_id (FK)        │     │ catalog_id (FK)          │
│ key_name            │     │ operation_name           │
│ service_key (AES)   │     │ http_method              │
│ created_at          │     │ endpoint_url             │
└─────────────────────┘     │ description              │
                            └────────────┬─────────────┘
┌─────────────────────┐                  │
│ user_favorites      │     ┌────────────┴─────────────┐
├─────────────────────┤     │ api_parameters           │
│ id (PK)             │     ├──────────────────────────┤
│ user_id (FK)        │     │ id (PK)                  │
│ catalog_id (FK)     │     │ operation_id (FK)        │
│ created_at          │     │ param_name               │
│ UNIQUE(user,catalog)│     │ param_type               │
└─────────────────────┘     │ required                 │
                            │ description              │
┌─────────────────────┐     │ default_value            │
│ test_history        │     │ direction (REQ/RES)      │
├─────────────────────┤     └──────────────────────────┘
│ id (PK)             │
│ user_id (FK)        │
│ catalog_id (FK null)│
│ request_url         │
│ request_params      │
│ response_body (CLOB)│
│ response_status     │
│ executed_at         │
└─────────────────────┘
```

---

## 7. API 설계 (REST Endpoints)

### 7.1 인증 API ✅
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) |
| GET | `/api/auth/me` | 내 정보 조회 (JWT 필요) |

### 7.2 카탈로그 API ✅
| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 메인 검색 (Thymeleaf, portal/local 소스 전환) |
| GET | `/explore/{uddiSeq}` | API 상세 명세 뷰어 |
| GET | `/console/{uddiSeq}` | API 테스트 콘솔 |
| POST | `/api/catalog/sync` | 카탈로그 동기화 |

### 7.3 테스트 API ✅
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/proxy/execute` | API 프록시 호출 (uddiSeq 포함 시 히스토리 저장) |

### 7.4 사용자 API ✅
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/user/keys` | 내 serviceKey 목록 (마스킹) |
| POST | `/api/user/keys` | serviceKey 등록 (AES 암호화) |
| DELETE | `/api/user/keys/{id}` | serviceKey 삭제 |
| GET | `/api/user/keys/{id}/decrypt` | serviceKey 복호화 |
| GET | `/api/user/favorites` | 즐겨찾기 목록 |
| POST | `/api/user/favorites/{catalogId}` | 즐겨찾기 추가 |
| DELETE | `/api/user/favorites/{catalogId}` | 즐겨찾기 삭제 |
| GET | `/api/user/favorites/check/{catalogId}` | 즐겨찾기 여부 확인 |
| GET | `/api/user/history` | 테스트 히스토리 (페이지네이션) |

### 7.5 웹 뷰 (Thymeleaf) ✅
| Method | Path | 설명 |
|--------|------|------|
| GET | `/` | 메인 페이지 (검색) |
| GET | `/explore/{uddiSeq}` | API 상세 명세 뷰 |
| GET | `/console/{uddiSeq}` | API 테스트 콘솔 |
| GET | `/user/login` | 사용자 로그인 |
| GET | `/signup` | 회원가입 |
| GET | `/mypage` | 마이페이지 |

### 7.6 관리자 ✅
| Method | Path | 설명 |
|--------|------|------|
| GET | `/login` | Admin 로그인 (form-based) |
| GET | `/admin/dashboard` | 대시보드 (통계 + Sync) |
| POST | `/admin/sync` | 카탈로그 동기화 실행 |
| GET | `/admin/settings` | application.properties 편집 |
| POST | `/admin/settings` | 설정 저장 |

---

## 8. Spring Security 구조

```
                        ┌─────────────────────────────┐
                        │ Request                      │
                        └─────────────┬───────────────┘
                                      ▼
                    ┌─────────────────────────────────┐
                    │ AdminSecurityConfig (@Order 1)   │
                    │ securityMatcher:                  │
                    │   /admin/**, /login, /css/admin.css│
                    │ form-based login                  │
                    │ InMemoryUserDetailsManager(ADMIN) │
                    └─────────────┬───────────────────┘
                                  │ (not matched)
                                  ▼
                    ┌─────────────────────────────────┐
                    │ SecurityConfig (@Order 2)        │
                    │ STATELESS session                 │
                    │ JwtAuthenticationFilter           │
                    │ permitAll:                        │
                    │   /h2-console/**, /api/auth/**    │
                    │   /api/catalog/**, /api/proxy/**  │
                    │   /, /explore/**, /console/**     │
                    │   /signup, /user/login, /mypage   │
                    │   /css/**, /js/**, /images/**     │
                    │ anyRequest: authenticated         │
                    └─────────────────────────────────┘
```

---

## 9. 패키지 구조 (현재)

```
com.spring.lica/
├── LicaApplication.java                # Main
├── config/                             # 프레임워크 설정 (Security, JPA, etc.)
│   ├── SecurityConfig.java             # JWT stateless security (@Order 2)
│   └── ... (JpaConfig, ThymeleafConfig 등)
├── security/jwt/                       # JWT 인프라
│   ├── JwtTokenProvider.java           # 토큰 생성/검증
│   ├── JwtAuthenticationFilter.java    # OncePerRequestFilter
│   └── JwtProperties.java             # jwt.secret, jwt.expiration
├── admin/                              # 관리자 UI
│   ├── AdminSecurityConfig.java        # Form-based admin security (@Order 1)
│   ├── AdminController.java            # /login, /admin/dashboard, /admin/settings, /admin/sync
│   ├── AdminStatsService.java          # 통계 집계 (users, catalogs, tests)
│   ├── AdminProperties.java
│   ├── SettingsService.java
│   └── PropertySection/PropertyEntry
│
├── client/datagokr/                    # 외부 API 클라이언트
│   ├── DataGoKrClient.java             # HTML 스크래핑 + Swagger JSON 파싱
│   ├── DataGoKrProperties.java         # base-url, portal-url, timeout
│   ├── DataGoKrApiException.java
│   └── dto/
│       ├── PortalApiResponse.java      # 포털 검색 결과 DTO
│       └── ApiDetailParseResult.java   # 스크래핑 결과 DTO
│
├── domain/
│   ├── catalog/                        # API 카탈로그 도메인
│   │   ├── entity/
│   │   │   ├── ApiCatalog.java         # 메인 엔티티 (uddiSeq 기준)
│   │   │   ├── ApiOperation.java       # 오퍼레이션 (ManyToOne → Catalog)
│   │   │   └── ApiParameter.java       # 파라미터 (ManyToOne → Operation, REQ/RES)
│   │   ├── repository/
│   │   │   ├── ApiCatalogRepository.java
│   │   │   └── ApiOperationRepository.java
│   │   ├── service/
│   │   │   ├── CatalogService.java         # 검색 (portal/local 이중 소스)
│   │   │   ├── CatalogSyncService.java     # 로컬 DB 동기화
│   │   │   └── CatalogDetailService.java   # 상세 명세 조회 + 스크래핑
│   │   ├── dto/
│   │   │   ├── CatalogSearchRequest.java
│   │   │   ├── CatalogResponse.java
│   │   │   └── CatalogDetailResponse.java
│   │   └── controller/
│   │       ├── CatalogViewController.java  # GET /, /explore/{uddiSeq}, /console/{uddiSeq}
│   │       └── CatalogController.java      # POST /api/catalog/sync
│   │
│   ├── proxy/                          # API 프록시 도메인
│   │   ├── service/
│   │   │   └── ApiProxyService.java    # 프록시 실행 + 히스토리 저장
│   │   ├── dto/
│   │   │   ├── ProxyRequest.java       # targetUrl, serviceKey, params, uddiSeq
│   │   │   └── ProxyResponse.java      # status, body, elapsedMs, curl
│   │   └── controller/
│   │       └── ProxyController.java    # POST /api/proxy/execute
│   │
│   └── user/                           # 사용자 도메인
│       ├── entity/
│       │   ├── User.java               # email, password(BCrypt), nickname
│       │   ├── UserServiceKey.java     # keyName, serviceKey(AES-GCM)
│       │   ├── UserFavorite.java       # UNIQUE(user, catalog)
│       │   └── TestHistory.java        # requestUrl, responseBody(CLOB), responseStatus
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── UserServiceKeyRepository.java
│       │   ├── UserFavoriteRepository.java
│       │   └── TestHistoryRepository.java
│       ├── service/
│       │   ├── AuthService.java        # signup, login(JWT), getMe
│       │   ├── ServiceKeyService.java  # CRUD + AES encrypt/decrypt
│       │   ├── FavoriteService.java    # CRUD + isFavorite
│       │   └── TestHistoryService.java # getHistory(paginated), saveHistory
│       ├── dto/
│       │   ├── SignupRequest.java      # @NotBlank @Email @Size
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java      # token, email, nickname
│       │   ├── UserResponse.java
│       │   ├── ServiceKeyRequest.java
│       │   ├── ServiceKeyResponse.java # maskedKey
│       │   ├── FavoriteResponse.java
│       │   └── TestHistoryResponse.java
│       └── controller/
│           ├── AuthController.java     # /api/auth/signup, login, me
│           ├── UserController.java     # /api/user/keys, favorites, history
│           └── UserViewController.java # /user/login, /signup, /mypage
│
└── common/
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── DuplicateEmailException.java    # 409
    │   ├── InvalidCredentialsException.java# 401
    │   └── UserNotFoundException.java      # 404
    └── util/
        └── AesEncryptionUtil.java          # AES/GCM/NoPadding, SHA-256 key derivation
```

### 템플릿 / 정적 파일

```
src/main/resources/
├── templates/
│   ├── index.html              # 메인 검색 페이지
│   ├── explore.html            # API 상세 명세 (Swagger-like)
│   ├── console.html            # API 테스트 콘솔
│   ├── user-login.html         # 사용자 로그인
│   ├── user-signup.html        # 회원가입
│   ├── mypage.html             # 마이페이지 (3탭)
│   ├── login.html              # Admin 로그인
│   ├── settings.html           # Admin 설정
│   └── admin-dashboard.html    # Admin 대시보드
├── static/
│   ├── css/
│   │   ├── govlica.css         # 사용자 UI 스타일
│   │   └── admin.css           # Admin UI 스타일
│   └── js/
│       └── auth.js             # JWT 토큰 관리, nav 업데이트, Auth.fetch()
└── application.properties      # 설정 파일
```

---

## 10. 설정 (application.properties 주요 항목)

```properties
# Database
spring.datasource.url=jdbc:h2:mem:licadb
spring.jpa.hibernate.ddl-auto=create-drop

# JWT
jwt.secret=your-256-bit-secret-key-here-change-in-production
jwt.expiration=3600000

# Admin
app.admin.username=admin
app.admin.password=admin

# data.go.kr
app.datagokr.portal-url=https://www.data.go.kr
app.datagokr.connect-timeout=5000
app.datagokr.read-timeout=15000

# Proxy
app.proxy.allowed-domains=apis.data.go.kr

# AES Encryption (ServiceKey)
app.security.encryption-key=${ENCRYPTION_KEY:govlica-default-aes-256-key!!}

# Catalog Sync
app.catalog.sync-page-size=100
```

---

## 11. 보안 고려사항

| 항목 | 대응 |
|------|------|
| 사용자 ServiceKey 보호 | AES-256-GCM 암호화 저장, API 응답에서 마스킹 표시 |
| 비밀번호 | BCrypt 해싱 |
| SSRF (서버 측 요청 위조) | 프록시 허용 도메인 화이트리스트 (`apis.data.go.kr`만 허용) |
| XSS | Thymeleaf 기본 이스케이핑 + JS에서 `escHtml()` 처리 |
| SQL Injection | JPA Parameterized Query 사용 |
| 인증/인가 | 2-tier Security: Admin(form+ROLE_ADMIN), User(JWT+ROLE_USER) |
| CSRF | Admin chain은 Spring Security 기본 CSRF, API chain은 stateless이므로 disable |
| cURL 출력 | serviceKey를 `YOUR_SERVICE_KEY`로 마스킹 |

---

## 12. 구현 상태

### Phase 1 — 기반 구축 + API 카탈로그 검색 ✅
- [x] DataGoKrClient — HTML 스크래핑 기반 (인증키 불필요)
- [x] ApiCatalog, ApiOperation, ApiParameter 엔티티 + Repository
- [x] CatalogSyncService — 증분 동기화
- [x] CatalogService — portal/local 이중 소스 검색
- [x] 메인 페이지 UI (카드 그리드, 페이지네이션, 필터)

### Phase 2 — API 명세 뷰어 ✅
- [x] CatalogDetailService — Swagger JSON 파싱 + DB 캐싱
- [x] DataGoKrClient — swaggerOprtinVOs 파싱 (reqList/resList/oprtinUrl)
- [x] DataGoKrClient — Swagger paths fallback (path-level params, $ref 해석)
- [x] explore.html — Swagger-like UI (accordion, 파라미터 테이블, 메서드 배지)
- [x] Refresh 기능 (data.go.kr에서 재파싱)

### Phase 3 — API 테스트 콘솔 ✅
- [x] ApiProxyService — 도메인 화이트리스트, cURL 생성
- [x] console.html — 파라미터 폼, serviceKey 입력, JSON 뷰어
- [x] 오퍼레이션 선택 시 파라미터 그룹 전환
- [x] 커스텀 파라미터 추가, cURL 복사
- [x] serviceKey localStorage 기억

### Phase 4 — 사용자 관리 + 편의 기능 ✅
- [x] User, UserServiceKey, UserFavorite, TestHistory 엔티티
- [x] AesEncryptionUtil — AES/GCM/NoPadding 암호화
- [x] AuthService — signup(BCrypt), login(JWT), getMe
- [x] ServiceKeyService — CRUD + AES encrypt/decrypt + 마스킹
- [x] FavoriteService — CRUD + isFavorite 체크
- [x] TestHistoryService — 히스토리 저장/조회 (페이지네이션)
- [x] SecurityConfig — JWT 경로 permitAll 설정
- [x] AdminSecurityConfig — CSS 경로 축소 (`/css/admin.css`)
- [x] AuthController, UserController, UserViewController
- [x] ProxyController — Authentication 전달, 히스토리 자동 저장
- [x] AdminStatsService + AdminController — 대시보드, Sync
- [x] auth.js — JWT 토큰 관리, nav 업데이트, Auth.fetch()
- [x] user-login.html, user-signup.html, mypage.html
- [x] admin-dashboard.html — 통계 카드, Sync 버튼
- [x] explore.html — 즐겨찾기 토글 버튼
- [x] console.html — 저장된 키 선택 드롭다운, uddiSeq 전송
- [x] govlica.css — auth/mypage/favorite 스타일 추가

---

## 13. 제약사항 및 리스크

| 항목 | 설명 | 대응 방안 |
|------|------|----------|
| data.go.kr 구조 변경 | 웹 스크래핑 기반이므로 HTML/JS 구조 변경 시 파싱 실패 | 방어적 파싱, 다중 전략 (swaggerOprtinVOs + paths fallback) |
| H2 인메모리 DB | 서버 재시작 시 데이터 소멸 | 개발용, 운영 시 PostgreSQL 등으로 교체 필요 |
| MultipleBagFetchException | Hibernate에서 2개 이상 List JOIN FETCH 불가 | Lazy loading + @Transactional로 해결 (JOIN FETCH 회피) |
| Swagger JSON 포맷 다양성 | API마다 Swagger JSON 구조가 다를 수 있음 | swaggerOprtinVOs 우선, paths fallback, 빈 상태 안내 |
| 인증키 관리 | 사용자 serviceKey는 민감정보 | AES-GCM 암호화, HTTPS 권장, API 응답 마스킹 |

---

## 14. 향후 확장 가능성

- **OpenAPI 3.0 스펙 자동 생성**: 공공 API 명세를 OpenAPI 3.0 형식으로 변환·내보내기
- **API 모니터링**: 등록한 API의 가용성 주기적 체크
- **커뮤니티 기능**: API별 사용 후기, 활용 팁 공유
- **코드 생성기**: 선택한 API에 대한 Java/Python/JS 클라이언트 코드 자동 생성
- **다크모드**: UI 테마 지원
- **운영 DB 전환**: H2 → PostgreSQL/MySQL + Flyway 마이그레이션
- **Rate Limiting**: 프록시 요청에 사용자별/IP별 분당 제한
