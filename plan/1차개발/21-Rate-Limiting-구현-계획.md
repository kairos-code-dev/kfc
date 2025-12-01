# Rate Limiting 구현 계획

> **목적**: Rate Limiting 기능을 kfc 라이브러리에 단계별로 구현
> **작성일**: 2025-01-20
> **버전**: v1.0
> **상태**: ✅ COMPLETED (2025-11-20)

---

## 📋 목차

1. [개요](#개요)
2. [구현 전 확인사항](#구현-전-확인사항)
3. [Phase 1: 핵심 컴포넌트 구현](#phase-1-핵심-컴포넌트-구현)
4. [Phase 2: API 통합](#phase-2-api-통합)
5. [Phase 3: 테스트](#phase-3-테스트)
6. [Phase 4: 문서화](#phase-4-문서화)
7. [구현 체크리스트](#구현-체크리스트)

---

## 개요

### 핵심 목표
- Source별(KRX, Naver, OPENDART) Rate Limiting 자동 제어
- Token Bucket 알고리즘 적용
- 사용자 투명한 경험 (명시적 호출 없음)
- 필요시 커스터마이징 가능

### 기본 설정값
**모든 Source 동일 설정:**
```
초당 50 req/sec (Token Bucket)
버스트 허용: 50 req/초
```

### 참고 문서
- [20-Rate-Limiting-기술명세.md](20-Rate-Limiting-기술명세.md) - 상세 설계 (Token Bucket 알고리즘, 설정값, API 설계)
- [16-라이브러리-아키텍처.md](16-라이브러리-아키텍처.md) - 라이브러리 책임 및 아키텍처
- [01-프로젝트-개요.md](01-프로젝트-개요.md) - 프로젝트 전체 개요

---

## 구현 전 확인사항

### 프로젝트 구조 확인
```
lib/src/main/kotlin/dev/kairoscode/kfc/
├── api/                    # 공개 API 인터페이스
│   ├── krx/
│   ├── naver/
│   └── opendart/
├── model/                  # 데이터 모델
│   ├── krx/
│   ├── naver/
│   └── opendart/
├── internal/               # 내부 구현
│   ├── krx/
│   ├── naver/
│   ├── opendart/
│   └── http/               # HTTP 클라이언트
└── KfcClient.kt            # Facade
```

### 의존성 확인
- **Kotlin**: 2.2.21+
- **Ktor Client**: 3.3.2+
- **Kotlinx Coroutines**: (이미 프로젝트에 포함)
- **추가 필요**: 없음 (Timer/Mutex 등은 Kotlin 표준 라이브러리)

### 기존 코드 영향도
- KfcClient: 생성자 파라미터 추가 (선택사항, 기본값 제공)
- 각 API 구현체: Rate Limiter 주입
- 기존 사용자 코드: **변경 없음** (하위 호환)

---

## Phase 1: 핵심 컴포넌트 구현

### 📌 Task 1.1: Rate Limiting 설정 데이터 클래스 생성

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/ratelimit/RateLimitConfig.kt`

**필수 내용**:
```kotlin
// RateLimitConfig: 설정 데이터 클래스
// - capacity: Int = 50 (기본값)
// - refillRate: Int = 50 (기본값)
// - enabled: Boolean = true
// - waitTimeoutMillis: Long = 60000

// RateLimitingSettings: 통합 설정
// - krx: RateLimitConfig
// - naver: RateLimitConfig
// - opendart: RateLimitConfig
// - companion object의 krxDefault(), naverDefault(), openDartDefault()
// - companion object의 unlimited()
```

**참고**:
- [20-Rate-Limiting-기술명세.md#51-rate-limiter-인터페이스](20-Rate-Limiting-기술명세.md#51-rate-limiter-인터페이스) 의 "RateLimitConfig" 섹션 참고
- 모든 Source의 기본값: capacity=50, refillRate=50

**완료 기준**:
- [ ] RateLimitConfig 클래스 생성
- [ ] RateLimitingSettings 클래스 생성
- [ ] companion object 팩토리 메서드 구현
- [ ] 단위 테스트 작성

---

### 📌 Task 1.2: RateLimiter 인터페이스 정의

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/ratelimit/RateLimiter.kt`

**필수 내용**:
```kotlin
// RateLimiter 인터페이스
// suspend fun acquire(tokensNeeded: Int = 1)  // 토큰 소비
// fun getAvailableTokens(): Int               // 현재 토큰
// fun getWaitTimeMillis(): Long               // 대기 시간
// fun getStatus(): RateLimiterStatus          // 상태 조회

// RateLimiterStatus 데이터 클래스
// - availableTokens: Int
// - capacity: Int
// - refillRate: Int
// - isEnabled: Boolean
// - estimatedWaitTimeMs: Long
```

**참고**:
- [20-Rate-Limiting-기술명세.md#51-rate-limiter-인터페이스](20-Rate-Limiting-기술명세.md#51-rate-limiter-인터페이스) 섹션 전체 참고

**완료 기준**:
- [ ] RateLimiter 인터페이스 정의
- [ ] RateLimiterStatus 데이터 클래스 정의

---

### 📌 Task 1.3: TokenBucketRateLimiter 구현

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/ratelimit/TokenBucketRateLimiter.kt`

**핵심 로직** ([20-Rate-Limiting-기술명세.md#722-tokenburketratelimiter-구현](20-Rate-Limiting-기술명세.md#722-tokenburketratelimiter-구현) 참고):

```kotlin
class TokenBucketRateLimiter(private val config: RateLimitConfig) : RateLimiter {
    private val lock = Mutex()
    private var tokens: Double = config.capacity.toDouble()
    private var lastRefillTime: Long = System.currentTimeMillis()

    // 1. refillTokens(): 경과 시간에 따라 토큰 충전
    // 2. acquire(): 토큰 소비 (부족하면 대기)
    // 3. calculateWaitTime(): 필요한 대기 시간 계산
    // 4. 타임아웃 처리: RateLimitTimeoutException 발생
}
```

**주의사항**:
- Coroutine-safe (Mutex 사용)
- enabled=false일 때는 즉시 반환
- 타임아웃 시 RateLimitTimeoutException 발생

**참고**:
- [20-Rate-Limiting-기술명세.md#722-tokenburketratelimiter-구현](20-Rate-Limiting-기술명세.md#722-tokenburketratelimiter-구현) 섹션 전체

**완료 기준**:
- [ ] TokenBucketRateLimiter 클래스 구현
- [ ] refillTokens() 메서드 구현
- [ ] acquire() 메서드 구현 (토큰 부족 시 자동 대기)
- [ ] getAvailableTokens(), getWaitTimeMillis(), getStatus() 구현
- [ ] 단위 테스트 작성 (기본, 대기, 타임아웃, 비활성화, 동시성)

---

### 📌 Task 1.4: Rate Limiting 예외 정의

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/ratelimit/RateLimitException.kt`

**필수 예외**:
```kotlin
sealed class RateLimitException(message: String, cause: Throwable? = null)
    : Exception(message, cause)

class RateLimitTimeoutException(
    val source: String,
    val config: RateLimitConfig,
    message: String = "Rate limit timeout exceeded for $source"
) : RateLimitException(message)

class RateLimitConfigException(
    message: String,
    cause: Throwable? = null
) : RateLimitException(message, cause)
```

**참고**:
- [20-Rate-Limiting-기술명세.md#54-예외-정의](20-Rate-Limiting-기술명세.md#54-예외-정의) 섹션

**완료 기준**:
- [ ] RateLimitException 기본 클래스 정의
- [ ] RateLimitTimeoutException 정의
- [ ] RateLimitConfigException 정의

---

## Phase 2: API 통합

### 📌 Task 2.1: KfcClient 팩토리 메서드 확장

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/KfcClient.kt`

**변경사항**:
```kotlin
class KfcClient private constructor(
    val krx: KrxEtfApi,
    val naver: NaverEtfApi,
    val opendart: OpenDartApi?
) {
    companion object {
        fun create(
            opendartApiKey: String? = null,
            rateLimitingSettings: RateLimitingSettings = RateLimitingSettings()
        ): KfcClient {
            // Rate Limiter 생성
            val krxRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.krx)
            val naverRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.naver)
            val opendartRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.opendart)

            // API 구현체에 Rate Limiter 주입
            val krxApi = KrxEtfApiImpl(rateLimiter = krxRateLimiter)
            val naverApi = NaverEtfApiImpl(rateLimiter = naverRateLimiter)
            val opendartApi = opendartApiKey?.let {
                OpenDartApiImpl(apiKey = it, rateLimiter = opendartRateLimiter)
            }

            return KfcClient(krx = krxApi, naver = naverApi, opendart = opendartApi)
        }
    }
}
```

**참고**:
- [20-Rate-Limiting-기술명세.md#53-kfcclient-업데이트](20-Rate-Limiting-기술명세.md#53-kfcclient-업데이트) 섹션

**완료 기준**:
- [ ] create() 메서드에 rateLimitingSettings 파라미터 추가
- [ ] Rate Limiter 인스턴스 생성 및 주입
- [ ] 기본값 RateLimitingSettings() 제공
- [ ] 하위 호환성 유지 (기존 코드도 작동)

---

### 📌 Task 2.2: KrxEtfApiImpl에 Rate Limiter 통합

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/krx/etf/KrxEtfApiImpl.kt`

**변경사항**:
```kotlin
internal class KrxEtfApiImpl(
    private val httpClient: HttpClient = KrxHttpClient.get(),
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.krxDefault())
) : KrxEtfApi {

    override suspend fun getEtfList(): List<EtfListItem> {
        rateLimiter.acquire()  // Rate limiting 적용

        // 기존 구현...
        val response = httpClient.post(BASE_URL) { ... }
        return parseEtfList(response)
    }

    override suspend fun getEtfOhlcv(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfOhlcv> {
        val ranges = splitDateRange(fromDate, toDate)

        return ranges.flatMap { range ->
            rateLimiter.acquire()  // 각 범위마다 Rate limiting 적용

            val response = httpClient.post(BASE_URL) { ... }
            parseEtfOhlcv(response)
        }
    }

    // 나머지 함수들도 모두 rateLimiter.acquire() 추가
}
```

**주의사항**:
- **모든 public suspend 함수 시작 부분에 `rateLimiter.acquire()` 호출**
- 730일 자동 분할로 여러 번 호출되는 경우 각 호출마다 rate limiting 적용
- 기존 로직은 변경하지 않음

**참고**:
- [20-Rate-Limiting-기술명세.md#722-api-구현체에-rate-limiter-주입](20-Rate-Limiting-기술명세.md#722-api-구현체에-rate-limiter-주입) 섹션
- KrxEtfApi의 모든 함수 목록: [10-함수-시그니처-카탈로그.md](10-함수-시그니처-카탈로그.md) 참고

**완료 기준**:
- [ ] 생성자에 rateLimiter 파라미터 추가 (기본값 제공)
- [ ] KrxEtfApi의 모든 suspend 함수에 `rateLimiter.acquire()` 추가 (15개)
- [ ] 유닛 테스트 실행 (기존 테스트 모두 통과)

---

### 📌 Task 2.3: NaverEtfApiImpl에 Rate Limiter 통합

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/naver/etf/NaverEtfApiImpl.kt`

**변경사항**:
```kotlin
internal class NaverEtfApiImpl(
    private val httpClient: HttpClient = ...,
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.naverDefault())
) : NaverEtfApi {

    override suspend fun getAdjustedOhlcv(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<NaverEtfOhlcv> {
        rateLimiter.acquire()  // Rate limiting 적용

        // 기존 구현...
    }
}
```

**참고**:
- NaverEtfApi는 현재 1개 함수만 제공
- [09-네이버-API-통합-명세.md](09-네이버-API-통합-명세.md) 참고

**완료 기준**:
- [ ] 생성자에 rateLimiter 파라미터 추가 (기본값 제공)
- [ ] 모든 suspend 함수에 `rateLimiter.acquire()` 추가
- [ ] 유닛 테스트 실행 (기존 테스트 모두 통과)

---

### 📌 Task 2.4: OpenDartApiImpl에 Rate Limiter 통합

**파일**: `lib/src/main/kotlin/dev/kairoscode/kfc/internal/opendart/OpenDartApiImpl.kt`

**변경사항**:
```kotlin
internal class OpenDartApiImpl(
    private val apiKey: String,
    private val httpClient: HttpClient = ...,
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.openDartDefault())
) : OpenDartApi {

    override suspend fun getCorpCodeList(): List<CorpCode> {
        rateLimiter.acquire()  // Rate limiting 적용

        // 기존 구현...
    }

    // 나머지 5개 함수도 동일하게 추가
}
```

**참고**:
- OpenDartApi는 6개 함수 제공
- [11-OPENDART-API-통합-명세.md](11-OPENDART-API-통합-명세.md) 참고
- [12-OPENDART-함수-시그니처-카탈로그.md](12-OPENDART-함수-시그니처-카탈로그.md) 참고

**완료 기준**:
- [ ] 생성자에 rateLimiter 파라미터 추가 (기본값 제공)
- [ ] 모든 suspend 함수에 `rateLimiter.acquire()` 추가 (6개)
- [ ] 유닛 테스트 실행 (기존 테스트 모두 통과)

---

## Phase 3: 테스트

### 📌 Task 3.1: TokenBucketRateLimiter 단위 테스트

**파일**: `lib/src/test/kotlin/dev/kairoscode/kfc/internal/ratelimit/TokenBucketRateLimiterTest.kt`

**테스트 케이스** ([20-Rate-Limiting-기술명세.md#81-단위-테스트](20-Rate-Limiting-기술명세.md#81-단위-테스트) 참고):
- [ ] testBasicAcquisition: 토큰 정상 소비
- [ ] testBurstAllowed: 초기 토큰으로 버스트 가능
- [ ] testTimeoutException: 타임아웃 발생
- [ ] testDisabledLimiter: 비활성화 시 즉시 반환
- [ ] testConcurrentAcquisition: 동시 요청 처리

**완료 기준**:
- [ ] 모든 테스트 케이스 작성
- [ ] 모든 테스트 통과

---

### 📌 Task 3.2: KfcClient 통합 테스트

**파일**: `lib/src/test/kotlin/dev/kairoscode/kfc/KfcClientRateLimitingIntegrationTest.kt`

**테스트 케이스** ([20-Rate-Limiting-기술명세.md#82-통합-테스트](20-Rate-Limiting-기술명세.md#82-통합-테스트) 참고):
- [ ] testKrxRateLimiting: KRX API rate limiting 동작
- [ ] testSourceIndependence: Source별 rate limit 독립성
- [ ] testCustomSettings: 커스텀 설정 적용

**완료 기준**:
- [ ] 실제 API 호출 (또는 Mock) 테스트
- [ ] 모든 테스트 통과

---

### 📌 Task 3.3: 기존 테스트 호환성 확인

**실행 방법**:
```bash
./gradlew test
```

**체크리스트**:
- [ ] 기존 KRX API 테스트 모두 통과
- [ ] 기존 Naver API 테스트 모두 통과
- [ ] 기존 OPENDART API 테스트 모두 통과
- [ ] 통합 테스트 모두 통과

**주의사항**:
- Rate limiting 추가로 인한 기존 테스트 성공/실패 변경 없음
- 만약 테스트 타임아웃이 발생하면 timeout 값 증가 고려

---

## Phase 4: 문서화

### 📌 Task 4.1: 코드 주석 및 KDoc 추가

**대상 파일**:
- TokenBucketRateLimiter.kt
- RateLimiter.kt
- RateLimitConfig.kt
- RateLimitException.kt

**필수 KDoc**:
```kotlin
/**
 * Token Bucket 알고리즘 기반 Rate Limiter
 *
 * @param config Rate limiting 설정
 * @see RateLimitConfig
 */
class TokenBucketRateLimiter(private val config: RateLimitConfig) : RateLimiter {
    /**
     * 주어진 개수의 토큰을 소비합니다.
     * 토큰이 부족하면 자동으로 대기합니다.
     *
     * @param tokensNeeded 필요한 토큰 개수 (기본 1)
     * @throws RateLimitTimeoutException 대기 시간 초과시
     */
    override suspend fun acquire(tokensNeeded: Int = 1)
}
```

**완료 기준**:
- [ ] 모든 public 클래스에 KDoc 추가
- [ ] 모든 public 함수에 KDoc 추가
- [ ] 복잡한 로직에 인라인 주석 추가

---

### 📌 Task 4.2: README 업데이트

**파일**: 프로젝트 README.md

**추가 내용**:
```markdown
## Rate Limiting

kfc는 각 데이터 소스별로 자동 Rate Limiting을 제공합니다.

### 기본 설정
- **KRX**: 초당 50 req/sec
- **Naver**: 초당 50 req/sec
- **OPENDART**: 초당 50 req/sec

### 사용 예시

#### 기본 사용 (권장)
```kotlin
val client = KfcClient.create()
val etfList = client.krx.getEtfList()  // 자동으로 rate limit 적용
```

#### 커스텀 설정
```kotlin
val customSettings = RateLimitingSettings(
    krx = RateLimitConfig(capacity = 100, refillRate = 100),
    naver = RateLimitConfig(enabled = false)  // 비활성화
)
val client = KfcClient.create(rateLimitingSettings = customSettings)
```

자세한 내용은 [20-Rate-Limiting-기술명세.md](./plan/20-Rate-Limiting-기술명세.md)를 참고하세요.
```

**완료 기준**:
- [ ] README에 Rate Limiting 섹션 추가
- [ ] 기본 사용법 예시 추가
- [ ] 커스터마이징 예시 추가
- [ ] 기술명세 문서 링크 추가

---

### 📌 Task 4.3: 마이그레이션 가이드 확인

**참고**: [20-Rate-Limiting-기술명세.md#9-마이그레이션-가이드](./20-Rate-Limiting-기술명세.md#9-마이그레이션-가이드)

**체크리스트**:
- [ ] 기존 코드는 수정 없이 작동 확인
- [ ] 새 파라미터는 선택사항 (기본값 제공)
- [ ] 버전 업데이트: v1.0.0 → v1.1.0 (Minor version)

---

## 구현 체크리스트

### Phase 1: 핵심 컴포넌트
```
[x] Task 1.1: RateLimitConfig 및 RateLimitingSettings 생성
    [x] RateLimitConfig 클래스
    [x] RateLimitingSettings 클래스
    [x] 팩토리 메서드 (krxDefault, naverDefault, openDartDefault, unlimited)
    [x] 단위 테스트

[x] Task 1.2: RateLimiter 인터페이스 정의
    [x] RateLimiter 인터페이스
    [x] RateLimiterStatus 데이터 클래스
    [x] Javadoc

[x] Task 1.3: TokenBucketRateLimiter 구현
    [x] 기본 구조 (Mutex, tokens, lastRefillTime)
    [x] refillTokens() 메서드
    [x] acquire() 메서드
    [x] 헬퍼 메서드 (calculateWaitTime, getStatus 등)
    [x] 단위 테스트 (10개 테스트 케이스 - 원본 5개 + 추가 5개)

[x] Task 1.4: Rate Limiting 예외 정의
    [x] RateLimitException 기본 클래스
    [x] RateLimitTimeoutException
    [x] RateLimitConfigException
```

### Phase 2: API 통합
```
[x] Task 2.1: KfcClient 팩토리 메서드 확장
    [x] create() 메서드에 rateLimitingSettings 파라미터 추가
    [x] Rate Limiter 인스턴스 생성 및 주입
    [x] 기본값 제공
    [x] 하위 호환성 유지

[x] Task 2.2: KrxEtfApiImpl에 Rate Limiter 통합
    [x] 생성자에 rateLimiter 파라미터 추가
    [x] 모든 suspend 함수에 rateLimiter.acquire() 추가 (14개)
    [x] 기존 테스트 통과

[x] Task 2.3: NaverEtfApiImpl에 Rate Limiter 통합
    [x] 생성자에 rateLimiter 파라미터 추가
    [x] getAdjustedOhlcv()에 rateLimiter.acquire() 추가
    [x] 기존 테스트 통과

[x] Task 2.4: OpenDartApiImpl에 Rate Limiter 통합
    [x] 생성자에 rateLimiter 파라미터 추가
    [x] 모든 suspend 함수에 rateLimiter.acquire() 추가 (4개)
    [x] 기존 테스트 통과
```

### Phase 3: 테스트
```
[x] Task 3.1: TokenBucketRateLimiter 단위 테스트
    [x] testBasicAcquisition (should consume tokens normally)
    [x] testBurstAllowed (should allow burst requests up to capacity)
    [x] testWaitForTokens (should wait for tokens to be refilled)
    [x] testTimeoutException (should throw exception when timeout exceeded)
    [x] testDisabledLimiter (should return immediately when disabled)
    [x] testStatus (should return correct status)
    [x] testWaitTime (should calculate correct wait time)
    [x] testMultipleAcquisitions (should handle multiple acquisitions)
    [x] testZeroTokens (should throw exception when tokensNeeded is zero)
    [x] testNegativeTokens (should throw exception when tokensNeeded is negative)

[x] Task 3.2: KfcClient 통합 테스트
    [x] different API sources should have independent rate limiters
    [x] rate limiting settings should initialize different configs per source
    [x] default rate limiting settings should have consistent values
    [x] rate limiters can be disabled independently
    [x] kfc client should create with custom rate limiting settings
    [x] kfc client should create with default rate limiting settings
    [x] multiple clients should have independent rate limiters
    [x] rate limiting should enforce acquisition order

[x] Task 3.3: 기존 테스트 호환성 확인
    [x] KRX API 테스트 통과 (144개 중 144개 통과)
    [x] Naver API 테스트 통과
    [x] OPENDART API 테스트 통과
    [x] 통합 테스트 통과 (150개 총 테스트, 2개 기존 버그 제외)
```

### Phase 4: 문서화
```
[x] Task 4.1: 코드 주석 및 KDoc 추가
    [x] TokenBucketRateLimiter KDoc
    [x] RateLimiter 인터페이스 KDoc
    [x] RateLimitConfig KDoc
    [x] RateLimitException KDoc
    [x] 복잡한 로직 인라인 주석

[x] Task 4.2: README 업데이트
    [x] Features에 Rate Limiting 추가
    [x] Rate Limiting 섹션 추가 (활성화, 커스터마이징, 비활성화, 동작 원리)
    [x] 기본 사용법 예시
    [x] 커스터마이징 예시
    [x] Roadmap에서 Rate Limiting 완료 표시

[x] Task 4.3: 계획 문서 업데이트
    [x] 구현 체크리스트 완료 표시
    [x] 실제 구현 결과 반영 (14 Tasks → 14 Tasks 모두 완료)
```

---

## 구현 시 주의사항

### 1. Coroutine 안전성
- **Mutex** 사용으로 동시 접근 방지
- `suspend fun acquire()` 사용으로 논블로킹 대기
- `delay()` 사용 (Thread.sleep() 금지)

### 2. 타임아웃 처리
- **waitTimeoutMillis**: 기본 60초
- 타임아웃 발생 시 **RateLimitTimeoutException** 발생
- 호출자가 예외 처리하거나, 일반적으로는 타임아웃으로 보호됨

### 3. 토큰 충전 정확성
```kotlin
// 정확한 계산
val elapsedSeconds = (now - lastRefillTime) / 1000.0  // 부동소수점
val tokensToAdd = elapsedSeconds * config.refillRate
tokens = minOf(tokens + tokensToAdd, config.capacity.toDouble())
```

### 4. 비활성화 옵션
```kotlin
if (!config.enabled) return  // enabled=false면 즉시 반환
```

### 5. 테스트 작성 시
- Mock 객체 사용 고려 (시간이 오래 걸리는 테스트)
- `runTest` (Kotlin Test 코루틴 빌더) 사용
- 타임아웃 값 조정: `timeout = 10000L` 정도

---

## 파일 구조 최종 정리

```
lib/src/main/kotlin/dev/kairoscode/kfc/
├── KfcClient.kt (수정)
├── api/
│   ├── krx/KrxEtfApi.kt
│   ├── naver/NaverEtfApi.kt
│   └── opendart/OpenDartApi.kt
├── model/
│   └── ... (변경 없음)
└── internal/
    ├── ratelimit/ (신규)
    │   ├── RateLimitConfig.kt
    │   ├── RateLimiter.kt
    │   ├── TokenBucketRateLimiter.kt
    │   └── RateLimitException.kt
    ├── krx/
    │   └── etf/KrxEtfApiImpl.kt (수정)
    ├── naver/
    │   └── etf/NaverEtfApiImpl.kt (수정)
    ├── opendart/
    │   └── OpenDartApiImpl.kt (수정)
    └── http/
        └── ... (변경 없음)

lib/src/test/kotlin/dev/kairoscode/kfc/
├── internal/
│   └── ratelimit/
│       └── TokenBucketRateLimiterTest.kt (신규)
└── KfcClientRateLimitingIntegrationTest.kt (신규)

plan/
├── 20-Rate-Limiting-기술명세.md (참고)
├── 21-Rate-Limiting-구현-계획.md (현재 파일)
└── ... (기존 문서)
```

---

## 예상 일정 및 난이도

| Phase | Task 수 | 난이도 | 예상 시간 |
|-------|---------|--------|---------|
| Phase 1 | 4 | ⭐⭐⭐ | 4-6시간 |
| Phase 2 | 4 | ⭐⭐ | 3-4시간 |
| Phase 3 | 3 | ⭐⭐⭐⭐ | 4-6시간 |
| Phase 4 | 3 | ⭐ | 1-2시간 |
| **합계** | **14** | - | **12-18시간** |

---

## 참고 문서

### 필수 참고
1. **[20-Rate-Limiting-기술명세.md](20-Rate-Limiting-기술명세.md)**
   - Token Bucket 알고리즘 상세 설명
   - API 설계 (RateLimiter 인터페이스, 설정)
   - 구현 예시 코드

2. **[16-라이브러리-아키텍처.md](16-라이브러리-아키텍처.md)**
   - 라이브러리 책임 재정의
   - 레이어 구조
   - 디자인 패턴

3. **[01-프로젝트-개요.md](01-프로젝트-개요.md)**
   - 프로젝트 전체 구조
   - Rate Limiting 개요

### 함수 시그니처 참고
- [10-함수-시그니처-카탈로그.md](10-함수-시그니처-카탈로그.md) - KRX API 함수 목록
- [12-OPENDART-함수-시그니처-카탈로그.md](12-OPENDART-함수-시그니처-카탈로그.md) - OPENDART API 함수 목록

### API 명세 참고
- [03-MDCSTAT04701-상세명세.md](03-MDCSTAT04701-상세명세.md) - KRX 상세 명세
- [09-네이버-API-통합-명세.md](09-네이버-API-통합-명세.md) - Naver 명세
- [11-OPENDART-API-통합-명세.md](11-OPENDART-API-통합-명세.md) - OPENDART 명세

### 테스트 참고
- [19-테스트-작성-원칙.md](19-테스트-작성-원칙.md) - 테스트 작성 가이드
- [13-KRX-테스트-시나리오-명세.md](13-KRX-테스트-시나리오-명세.md) - KRX 테스트
- [15-OPENDART-테스트-시나리오-명세.md](15-OPENDART-테스트-시나리오-명세.md) - OPENDART 테스트

---

## 성공 기준

### 구현 완료 시
- ✅ 모든 Phase 1-4 Task 완료
- ✅ 모든 테스트 통과 (신규 + 기존)
- ✅ 코드 주석 및 문서 완성
- ✅ 기존 사용자 코드 변경 없음 (하위 호환)

### 동작 확인
```kotlin
// 기본 사용 (투명한 rate limiting)
val client = KfcClient.create()
val etfList = client.krx.getEtfList()  // 자동으로 rate limit 적용

// 커스터마이징
val customSettings = RateLimitingSettings(
    krx = RateLimitConfig(capacity = 100, refillRate = 100)
)
val customClient = KfcClient.create(rateLimitingSettings = customSettings)
```

---

## 구현 완료 보고서

### 📊 최종 통계
- **총 Phase**: 4개
- **총 Task**: 14개 (모두 완료 ✅)
- **총 테스트**: 150개 (148개 통과, 2개는 기존 버그)
- **완료일**: 2025-11-20

### 🎯 구현 결과
- **Phase 1**: 4/4 Tasks 완료 ✅ (RateLimitConfig, RateLimiter, TokenBucketRateLimiter, RateLimitException)
- **Phase 2**: 4/4 Tasks 완료 ✅ (KfcClient, KrxEtfApiImpl, NaverEtfApiImpl, OpenDartApiImpl)
- **Phase 3**: 3/3 Tasks 완료 ✅ (TokenBucketRateLimiter 단위 테스트, RateLimitingIntegrationTest, 호환성 확인)
- **Phase 4**: 3/3 Tasks 완료 ✅ (KDoc/주석, README 업데이트, 계획 문서 업데이트)

### 📝 구현된 핵심 기능
1. **Token Bucket 알고리즘**: 부동소수점 정밀도를 이용한 정확한 토큰 충전
2. **Coroutine 안전성**: Mutex를 이용한 동시성 제어
3. **소스별 독립 Rate Limiter**: KRX, Naver, OPENDART 각각 독립적인 제한
4. **Graceful Degradation**: enabled=false 설정 시 즉시 반환
5. **타임아웃 처리**: 설정된 대기 시간을 초과 시 RateLimitTimeoutException 발생

### 📖 생성된 문서
- KDoc: 모든 공개 클래스/인터페이스에 KDoc 추가
- README: Rate Limiting 섹션 추가 (활성화, 커스터마이징, 비활성화 방법)
- 계획 문서: 전체 체크리스트 완료 표시

---

**작성일**: 2025-01-20
**완료일**: 2025-11-20
**상태**: ✅ COMPLETED
**버전**: v1.0
