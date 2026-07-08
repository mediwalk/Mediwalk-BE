# Mediwalk-BE

사용자가 폐의약품 등 수거함까지 걸어가면 보상을 받는 앱 **mediwalk**의
백엔드입니다. Tmap 보행자 경로 API로 실제 걷기 좋은 경로를 생성하고, 일일 미션/업적, 포인트·
소모품 보상 경제를 제공합니다.

## 시연 영상

[https://www.youtube.com/shorts/AXX3TT8bUGo](https://www.youtube.com/shorts/AXX3TT8bUGo)

## 기술 스택

- Java 21 / Spring Boot 3.5
- Spring Web, Spring Data JPA, Spring Security(CORS/CSRF 설정 및 Firebase 토큰 인증), Validation
- MySQL 8 (+ Flyway, Java 기반 마이그레이션)
- Firebase Admin SDK (구글 로그인 토큰 검증)
- Google Cloud Vertex AI (의약품 이미지 검증)
- Tmap 보행자 경로 / 주변 POI 검색 API
- springdoc-openapi (Swagger UI)
- Apache POI (엑셀 기반 수거 위치 일괄 등록)
- Gradle, Docker / Docker Compose (EC2 배포)

## 아키텍처

### 전체 시스템 구성

![아키텍처 다이어그램](./docs/images/architecture.jpeg)

- AWS EC2 위에서 Docker → Nginx → Spring Boot 순으로 요청을 처리하고, EC2 호스트에 설치된
  MySQL에 접속합니다 (컨테이너 내부에 MySQL을 띄우지 않음). Flyway가 MySQL 스키마를 관리합니다.
- 프론트엔드(React + Vite)와는 HTTPS로 통신합니다.
- 외부 API로 Firebase Auth(로그인 토큰 검증), Tmap API(보행자 경로/POI), Vertex AI(의약품
  이미지 검증)를 호출합니다.

### 패키지 구조

도메인 주도 패키지 구조로 나눴습니다. 컨트롤러/서비스/레포지토리/엔티티 계층을 도메인마다
분리하면 라우트 생성처럼 복잡한 로직과, 미션/보상처럼 단순 CRUD에 가까운 로직이 섞이지 않고
각자 응집도 있게 관리된다고 판단했습니다.

```
domain/
  auth/      - Firebase ID 토큰 로그인(구글 로그인) -> AuthLoginResponse
  user/      - User 엔티티/역할/상태
  mission/   - Mission, Achievement, UserDailyMission, UserAchievement
  reward/    - RewardTransaction(포인트), ConsumableItem 카탈로그, Event
  walk/      - 핵심 도메인: 경로 생성, Tmap 연동, CollectionLocation, POI, DailySteps
  common/    - BaseEntity(생성/수정일 감사), 공용 ErrorResponse
config/      - Security, CORS, OpenAPI/Swagger, Firebase Admin SDK, RestTemplate
exception/   - GlobalExceptionHandler (전역 예외 처리)
flyway/      - Java 기반 Flyway 마이그레이션
```

## 설계 방식

**1. 경로 생성 — 2단계 필터링 (직선거리 → Tmap 호출)**

- Tmap API는 호출당 비용/지연이 있음 → 후보 수거함마다 매번 실제 경로를 조회하면 비효율적
- 먼저 **직선 거리**로 후보를 빠르게 추린 뒤 (`TMAP_STRAIGHT_TO_WALK_RATIO`,
  `ESTIMATE_METERS_PER_STEP`로 목표 걸음 수 → 직선 거리 역산), 실제 Tmap 호출은 최종 후보에만 수행

**2. MAXIMUM 활동 레벨 — 캡 우선 방식**

- 요구사항: "최대한 멀리, 단 3km는 넘지 않게"
- 랭킹 순으로 후보를 하나씩 Tmap 조회 → 3km(`MAXIMUM_MAX_TMAP_DISTANCE_METERS`) 초과 시 다음
  후보로 (`pickMaximumWithinTmapCap`)
- **이유**: 목표 걸음 수를 정확히 맞추는 것보다, 실제 도보 거리가 상한을 넘지 않는 제약이 더
  중요하다고 판단

**3. POI + 3구간 타임라인 동봉**

- 경로 응답에 마트/공원 POI와 초반/중반/막판 3구간 타임라인(`RouteSegmentBuilderService`)을
  함께 제공
- **이유**: "목적지로 가라"는 단순 지시가 아니라 "가는 길에 이런 게 있다"는 걷기 동기 부여

**4. 에러 처리 — 설정 누락은 503, 예외는 한 곳에 집중**

- Tmap 키/GCP 인증정보는 환경마다 없을 수 있다고 가정 → 서버 기동 실패나 500이 아니라
  `IllegalStateException → 503 SERVICE_UNAVAILABLE`로 처리
- **이유**: 외부 연동 키가 없어도 미션/보상 등 나머지 API는 정상 동작 → 개발/테스트 편의성 확보
- 예외 처리를 도메인별로 흩어놓지 않고 `GlobalExceptionHandler` 하나로 통합 → 도메인이 늘어나도
  에러 응답 포맷(`ErrorResponse`)이 흔들리지 않게 함

**5. 리워드 — 적립/환급 분리 + 원장(ledger) 구조**

- 적립(ACCUMULATION)과 환급(REFUND)을 하나의 `RewardTransaction` 테이블에 타입으로 구분해
  기록 → 잔액 변동 이력을 거래 원장처럼 추적 가능
- 적립은 이벤트 생성 시(`EventService`)에서만 자동 처리되도록 막고, 환급 API는 REFUND
  타입만 받도록 제한 → 중복 적립/임의 적립 조작 방지
- 환급 시 최소 금액(10,000원)과 사용자 잔액 검증을 거치고, 은행명/마스킹된 계좌번호를
  저장 → 실제 현금화(출금) 흐름을 반영
- **한계**: 잔액 갱신에 낙관적 락(`@Version`) 등 동시성 제어는 아직 없어, 동시 요청 시
  정합성 처리는 개선 과제로 남아있음

## 설치 및 실행

### 사전 준비

- JDK 21
- 로컬 MySQL, DB 이름 `mediwalk` (`jdbc:mysql://localhost:3306/mediwalk`)
- `src/main/resources/application-local.yaml` 생성 (git에서 제외됨, 패키징 시에도 제외)
  아래 값을 채워야 합니다.
  - `spring.datasource.password` (MySQL 비밀번호)
  - `firebase.enabled: true`, `firebase.project-id`, Firebase 서비스 계정 JSON 경로
  - Vertex AI 의약품 검증용 GCP 프로젝트/리전/엔드포인트, 인증 정보
  - `app.tmap.api-key` (Tmap 보행자 경로 API 키)

  Tmap 키나 GCP 인증 정보가 없어도 서버는 죽지 않고, 관련 기능만 `503 SERVICE_UNAVAILABLE`로
  응답합니다.

### 로컬 실행

```bash
./gradlew bootRun            # http://localhost:8080 에서 기동
```

### 빌드 / 테스트

```bash
./gradlew build              # 전체 빌드 (컴파일 + 테스트)
./gradlew test                # 전체 테스트
./gradlew test --tests "com.example.mediwalk_be.MediwalkBeApplicationTests"   # 단일 테스트
```

### API 문서 (Swagger)

로컬 기동 후 `http://localhost:8080/swagger-ui/index.html` 에서 확인할 수 있습니다.

## API 엔드포인트

| 도메인 | Base path | 컨트롤러 |
|---|---|---|
| Auth | `/api/auth` | `AuthController` (`POST /google`: Firebase ID 토큰 로그인) |
| Route | `/api/routes` | `RouteController` (`POST /generate`: Tmap 경로 생성) |
| Collection Location | `/api/collection-locations` | `CollectionLocationController` (`POST /import/xlsx`: 엑셀 일괄 등록, `/nearby`, `/search`) |
| Mission / Achievement | `/api/missions`, `/api/achievements` | `MissionController`, `AchievementController` |
| Daily Steps | `/api/daily-steps` | `DailyStepsController` |
| Medicine Verification | `/api/medicine-verifications` | `MedicineVerificationController` (Vertex AI 의약품 이미지 검증) |
| Reward Transaction | `/api/reward-transactions` | `RewardTransactionController` |

그 외 세부/부가 엔드포인트(User, User Achievement, User Daily Mission, Reward Main,
Consumable, Event 등)를 포함한 전체 스펙은 Swagger UI를 참고하세요.

## 배포

실제 서비스: [https://api.mediwalk.site](https://api.mediwalk.site)

Docker Compose 기반 배포 절차와 롤백 방법은 [DEPLOY.md](./DEPLOY.md)를 참고하세요.
