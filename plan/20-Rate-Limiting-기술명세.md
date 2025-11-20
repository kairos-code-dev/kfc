# Rate Limiting 기술명세서

> **목적**: Source별 Rate Limiting 기능 설계 및 구현 명세
> **작성일**: 2025-01-20
> **버전**: v1.0

---

## 목차

1. [개요](#개요)
2. [설계 원칙](#설계-원칙)
3. [Token Bucket 알고리즘](#token-bucket-알고리즘)
4. [Source별 권장 설정](#source별-권장-설정)
5. [API 설계](#api-설계)
6. [사용 예시](#사용-예시)
7. [구현 아키텍처](#구현-아키텍처)
8. [테스트 전략](#테스트-전략)
9. [마이그레이션 가이드](#마이그레이션-가이드)

---

## 개요

### 1.1 필요성

현재 kfc 라이브러리는 rate limiting을 애플리케이션 책임으로 정의했습니다. 하지만 재검토 결과, 다음과 같은 이유로 라이브러리에서 관리하는 것이 더 효과적입니다:

| 측면 | 이유 |
|------|------|
| **동일한 제약** | 모든 사용자에게 KRX, Naver, OPENDART의 기술적 제한이 동일 |
| **투명한 처리** | 사용자가 명시적으로 관리할 필요 없음 (자동 대기) |
| **라이브러리 책임** | 730일 자동 분할처럼, 구현 세부사항으로 투명하게 처리 |
| **통일된 경험** | 모든 API 호출에 일관된 rate limiting 적용 |
| **선택 가능** | 필요시 비활성화 또는 커스터마이징 가능 |

### 1.2 정의

**Rate Limiting**: 일정 시간 내 API 호출 횟수를 제한하는 메커니즘으로, 다음을 제공합니다:

- ✅ **자동 제어**: Token Bucket으로 자동 호출 속도 조절
- ✅ **자동 대기**: 제한 초과시 자동으로 대기 후 재시도
- ✅ **Source별 설정**: 각 데이터 소스별 독립적 제한 설정
- ✅ **투명한 처리**: 사용자는 API 호출만 하면 됨

---

## 설계 원칙

### 2.1 라이브러리 책임으로의 변경

#### Before (기존)
```
애플리케이션 계층
↓ (rate limit 제어 책임)
↓ 10req/sec를 초과하지 않도록 호출 조절
↓
kfc 라이브러리 (rate limiting 없음)
↓
KRX API
```

#### After (변경)
```
애플리케이션 계층
↓ (투명한 API 호출)
↓ 호출하면 자동으로 rate limit 적용
↓
kfc 라이브러리 (rate limiting 포함)
  ├─ KRX Rate Limiter (10 req/sec)
  ├─ Naver Rate Limiter (10 req/sec)
  └─ OPENDART Rate Limiter (10 req/sec)
↓
KRX/Naver/OPENDART API
```

### 2.2 설계 결정사항

| 결정 | 선택 | 이유 |
|------|------|------|
| **알고리즘** | Token Bucket | 버스트 트래픽 허용, 유연한 제어, 구현 간단 |
| **초과 동작** | 자동 대기 | 사용자가 예외 처리 불필요, 투명한 호출 |
| **초과 대기 방식** | Exponential Backoff | 재시도 성공률 높음, 서버 부하 감소 |
| **설정 레벨** | Source별 | 각 API의 제한이 독립적 |
| **기본값 제공** | Yes | 안전한 기본값으로 OOTB 동작 |
| **커스터마이징** | Yes | 필요시 사용자가 변경 가능 |
| **비활성화** | Yes | 필요시 `unlimited` 옵션 제공 |

### 2.3 사용자 경험

```kotlin
// 사용자 관점: 투명한 호출
val client = KfcClient.create()

// KRX는 자동으로 10 req/sec 제한 적용
val etfList = client.krx.getEtfList()  // ← 대기 자동 처리

// 여러 요청도 순차적으로 자동 조절
for (isin in isinList) {
    val detail = client.krx.getComprehensiveEtfInfo(isin)
    // 각 요청은 자동으로 rate limit 체크 후 필요시 대기
}
```

---

## Token Bucket 알고리즘

### 3.1 개념

**Token Bucket은 다음과 같이 동작합니다:**

```
초기 상태: [●●●●●●●●●●] 토큰 10개
시간 흐름: 초당 1개씩 추가 (최대 10개)

요청 1: 토큰 1개 사용 → [●●●●●●●●●] 9개 남음
요청 2: 토큰 1개 사용 → [●●●●●●●●] 8개 남음
...
요청 10: 토큰 1개 사용 → [] 0개 남음

다음 요청: 토큰이 없으므로 대기
1초 후: 1개 추가 → [●] 1개 → 요청 가능
```

### 3.2 수학적 정의

```
Parameters:
- capacity: 최대 토큰 개수 (기본 10)
- refill_rate: 초당 토큰 추가 개수 (기본 10)
- last_refill_time: 마지막 토큰 추가 시간

함수:
refill_tokens():
    elapsed = now - last_refill_time
    tokens_to_add = elapsed * refill_rate
    tokens = min(tokens + tokens_to_add, capacity)
    last_refill_time = now

try_consume(tokens_needed):
    refill_tokens()
    if tokens >= tokens_needed:
        tokens -= tokens_needed
        return success
    else:
        return failed (need to wait)

wait_for_tokens(tokens_needed):
    while not try_consume(tokens_needed):
        deficit = tokens_needed - tokens
        wait_time = ceil(deficit / refill_rate)
        sleep(wait_time)
```

### 3.3 시각화

```
시간 축을 따라 token 변화:

t=0s: [●●●●●●●●●●] 10 tokens
      ↓ 요청 (1개 소비)
      [●●●●●●●●●] 9 tokens

t=1s: [●●●●●●●●●●] 10 tokens (1초동안 1개 충전, 이미 9개 있으므로 초과)
      ↓ 요청 (1개 소비)
      [●●●●●●●●●] 9 tokens

t=2s: [●●●●●●●●●●] 10 tokens
      ↓ 요청 (1개 소비)
      [●●●●●●●●●] 9 tokens

...

만약 t=0.5s에 10개 요청이 들어오면:
[●●●●●●●●●●] 10 tokens → 모두 소비
다음 요청은 대기 필요 (버킷이 차우 기다려야 함)
```

### 3.4 특징

| 특징 | 설명 | 예시 |
|------|------|------|
| **버스트 허용** | 초기 토큰 또는 충전된 토큰으로 순간적 다량 요청 가능 | 10개 토큰 저장 후 한 번에 10개 요청 |
| **공정성** | 시간에 따라 정확히 제한 적용 | 10 req/sec = 100ms마다 1개 요청 가능 |
| **구현 간단** | 복잡한 상태 관리 불필요 | 최소한의 메모리, 빠른 계산 |
| **유연성** | capacity와 rate 커스터마이징으로 다양한 전략 지원 | 보수적~적극적 설정 모두 가능 |

---

## Source별 권장 설정

### 4.1 KRX API

**공식 문서**: http://data.krx.co.kr/

**기술 명세**:
- **공식 Rate Limit**: 초당/일일 명시되지 않음
- **실제 관찰**: pykrx 경험상 무분별한 빠른 재시도 시 IP 차단 가능
- **권장 기준**: OPENDART와 동일 수준으로 안정적 운영

**권장 설정**:

```kotlin
data class KrxRateLimitConfig(
    val capacity: Int = 50,              // 최대 토큰: 50개
    val refillRate: Int = 50,            // 초당 충전: 50개 (= 50 req/sec)
    val enabled: Boolean = true,         // 기본 활성화
    val description: String = "KRX (OPENDART 수준, 공식 제한 미정의)"
)

// 해석:
// - 초당 최대 50개 요청 처리 가능
// - 순간적으로 50개까지 버스트 가능 (~1초 동안 집중 요청)
// - 이후 초당 50개씩 충전되므로 안정적 처리
// - 명시적 제한이 없으므로 OPENDART 수준으로 통일
```

**조사 결과**:
1. **공식 제한 미정의**: KRX는 초당/일일 제한을 공식 명시하지 않음
2. **pykrx 경험**: 무분별한 빠른 재시도 시 IP 차단 가능
3. **합리적 기준**: OPENDART(40K/일)와 동일 수준(초당 50)으로 설정
4. **여유있는 설정**: 명시된 제약이 없으므로 리즈너블한 속도로 운영
5. **실제 성능**: Ktor HTTP 클라이언트는 초당 50회 충분히 처리 가능

### 4.2 Naver Finance API

**공식 문서**: https://finance.naver.com/

**기술 명세**:
- **공식 Rate Limit**: 명시되지 않음
- **실제 관찰**: pykrx 경험상 Yahoo/Naver는 다른 소스보다 관대한 편
- **Cloudflare 보호**: Cloudflare CDN을 사용하여 악의적 요청 차단 (정상 속도는 허용)
- **권장 기준**: 공개 데이터이며 웹 서버 특성상 일반적인 속도로 운영 가능

**권장 설정**:

```kotlin
data class NaverRateLimitConfig(
    val capacity: Int = 50,              // 최대 토큰: 50개
    val refillRate: Int = 50,            // 초당 충전: 50개 (= 50 req/sec)
    val enabled: Boolean = true,         // 기본 활성화
    val description: String = "Naver 웹 서버 특성상 관대, 공개 데이터"
)

// 해석:
// - 초당 최대 50개 요청 처리 가능
// - 순간적으로 50개까지 버스트 가능
// - Naver 차트 API는 공개 데이터로 일반적인 속도 운영 가능
```

**조사 결과**:
1. **공식 제한 없음**: Naver Finance 차트 API 공식 rate limit 미정의
2. **상대적 관대함**: pykrx 통계상 KRX보다 관대한 것으로 관찰
3. **Cloudflare 보호**: 과도한 악의적 요청만 차단 (정상 속도 허용)
4. **공개 데이터**: 주식 차트는 누구나 접근 가능한 공개 정보
5. **웹 서버 특성**: CDN과 캐싱으로 많은 요청 처리 가능

### 4.3 OPENDART API

**공식 문서**: https://opendart.fss.or.kr/

**기술 명세**:
- **공식 Rate Limit**: 초당 제한 미정의, 일일 누적 제한만 존재
- **일일 허용량**: 40,000건/일
- **API Key 기반**: API Key마다 별도 할당량 관리 (금융감독원이 변경 가능)
- **권장 기준**: 공식 초당 제한이 명확하지 않으므로 안정적 속도로 운영 권장

**권장 설정**:

```kotlin
data class OpenDartRateLimitConfig(
    val capacity: Int = 50,              // 최대 토큰: 50개
    val refillRate: Int = 50,            // 초당 충전: 50개 (= 50 req/sec)
    val enabled: Boolean = true,         // 기본 활성화
    val description: String = "OPENDART 40K/일 허용량 기반"
)

// 해석:
// - 초당 최대 50개 요청 처리 가능
// - 순간적으로 50개까지 버스트 가능
// - 일일 40,000회 제한: 초당 50개 × 86,400초 = 4,320,000회 >> 40,000회 (충분함)
// - 현실적으로는 일일 40K 제한이 병목이지만, 초당 속도는 자유로움
```

**조사 결과**:
1. **공식 제한 미정의**: 초당 제한은 공식 정의 없음
2. **일일 허용량**: 40,000건/일 (API Key 기준)
3. **API Key별 관리**: 금융감독원이 API Key마다 별도 할당량 설정 가능
4. **ZIP 처리**: 매 요청마다 ZIP 압축 해제 필요 (처리 시간 고려)
5. **실제 병목**: 초당은 자유로우나 일일 40K가 실질적 제한

### 4.4 비교표

| Source | Capacity | Refill Rate | Max Burst | 일일 계산 | 근거 |
|--------|----------|-------------|-----------|----------|------|
| **KRX** | 50 | 50 req/sec | 50 req | 4.3M (미정의) | 공식 제한 미정의 → OPENDART 수준 |
| **Naver** | 50 | 50 req/sec | 50 req | 4.3M (무제한) | 공식 제한 없음, 공개 데이터 |
| **OPENDART** | 50 | 50 req/sec | 50 req | 4.3M >> 40K ✅ | 공식 40K/일 |

**설명**:
- **Capacity**: Token Bucket 초기 토큰 (버스트 허용)
- **Refill Rate**: 초당 충전 토큰 수
- **Max Burst**: 버스트 가능한 최대 요청 수 (~1초 동안 몰아서 요청 가능)
- **일일 제한**: 초당 × 86,400초 = 일일 최대 가능 요청 수

---

## API 설계

### 5.1 Rate Limiter 인터페이스

```kotlin
// 공개 인터페이스
data class RateLimitConfig(
    val capacity: Int = 10,              // 버킷 용량
    val refillRate: Int = 10,            // 초당 충전 개수
    val enabled: Boolean = true,         // 활성화 여부
    val waitTimeoutMillis: Long = 60000  // 최대 대기 시간 (60초)
) {
    init {
        require(capacity > 0) { "Capacity must be positive" }
        require(refillRate > 0) { "Refill rate must be positive" }
        require(waitTimeoutMillis > 0) { "Wait timeout must be positive" }
    }
}

// Public API (사용자)
interface RateLimiter {
    /**
     * 주어진 개수의 토큰을 소비합니다.
     * 토큰이 부족하면 자동으로 대기합니다.
     *
     * @param tokensNeeded 필요한 토큰 개수 (기본 1)
     * @throws RateLimitTimeoutException 대기 시간 초과시
     */
    suspend fun acquire(tokensNeeded: Int = 1)

    /**
     * 현재 사용 가능한 토큰 개수를 반환합니다.
     */
    fun getAvailableTokens(): Int

    /**
     * 현재 대기 시간을 반환합니다 (토큰이 부족할 경우).
     */
    fun getWaitTimeMillis(): Long

    /**
     * Rate limiter 상태를 반환합니다.
     */
    fun getStatus(): RateLimiterStatus
}

// 상태 정보
data class RateLimiterStatus(
    val availableTokens: Int,
    val capacity: Int,
    val refillRate: Int,
    val isEnabled: Boolean,
    val estimatedWaitTimeMs: Long
)
```

### 5.2 Source별 설정 인터페이스

```kotlin
// 통합 Rate Limiting 설정
data class RateLimitingSettings(
    val krx: RateLimitConfig = krxDefault(),
    val naver: RateLimitConfig = naverDefault(),
    val opendart: RateLimitConfig = openDartDefault(),
    val globalTimeout: Long = 60000
) {
    companion object {
        fun krxDefault() = RateLimitConfig(
            capacity = 10,
            refillRate = 10,
            enabled = true
        )

        fun naverDefault() = RateLimitConfig(
            capacity = 15,
            refillRate = 20,
            enabled = true
        )

        fun openDartDefault() = RateLimitConfig(
            capacity = 5,
            refillRate = 5,
            enabled = true
        )

        fun unlimited() = RateLimitingSettings(
            krx = RateLimitConfig(enabled = false),
            naver = RateLimitConfig(enabled = false),
            opendart = RateLimitConfig(enabled = false)
        )
    }
}
```

### 5.3 KfcClient 업데이트

```kotlin
// KfcClient Factory 확장
class KfcClient private constructor(
    val krx: KrxEtfApi,
    val naver: NaverEtfApi,
    val opendart: OpenDartApi?
) {
    companion object {
        /**
         * KfcClient를 생성합니다.
         *
         * @param opendartApiKey OPENDART API Key (선택사항)
         * @param rateLimitingSettings Rate Limiting 설정
         */
        fun create(
            opendartApiKey: String? = null,
            rateLimitingSettings: RateLimitingSettings = RateLimitingSettings()
        ): KfcClient {
            val krxApi = KrxEtfApiImpl(
                rateLimiter = TokenBucketRateLimiter(rateLimitingSettings.krx)
            )
            val naverApi = NaverEtfApiImpl(
                rateLimiter = TokenBucketRateLimiter(rateLimitingSettings.naver)
            )
            val opendartApi = opendartApiKey?.let {
                OpenDartApiImpl(
                    apiKey = it,
                    rateLimiter = TokenBucketRateLimiter(rateLimitingSettings.opendart)
                )
            }

            return KfcClient(
                krx = krxApi,
                naver = naverApi,
                opendart = opendartApi
            )
        }
    }
}
```

### 5.4 예외 정의

```kotlin
// Rate Limiting 관련 예외
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

---

## 사용 예시

### 6.1 기본 사용 (기본 설정)

```kotlin
// 기본 설정으로 클라이언트 생성
// KRX: 10 req/sec, Naver: 20 req/sec, OPENDART: 5 req/sec
val client = KfcClient.create()

// 투명한 호출 - rate limiting 자동 적용
val etfList = client.krx.getEtfList()  // 자동으로 rate limit 체크

// 루프에서 여러 요청
for (isin in isinList) {
    val detail = client.krx.getComprehensiveEtfInfo(isin)
    // 각 요청이 rate limit을 준수하므로 서버에 부하 X
}
```

### 6.2 커스터마이징 (보수적 설정)

```kotlin
// 더 보수적인 설정 (느린 인터넷 환경)
val customSettings = RateLimitingSettings(
    krx = RateLimitConfig(capacity = 5, refillRate = 5),      // 5 req/sec
    naver = RateLimitConfig(capacity = 10, refillRate = 10),  // 10 req/sec
    opendart = RateLimitConfig(capacity = 2, refillRate = 2)  // 2 req/sec
)

val client = KfcClient.create(rateLimitingSettings = customSettings)
val etfList = client.krx.getEtfList()
```

### 6.3 적극적 설정 (고속 인터넷 환경)

```kotlin
// 더 적극적인 설정 (고속 인터넷)
val aggressiveSettings = RateLimitingSettings(
    krx = RateLimitConfig(capacity = 20, refillRate = 20),     // 20 req/sec
    naver = RateLimitConfig(capacity = 30, refillRate = 30),   // 30 req/sec
    opendart = RateLimitConfig(capacity = 10, refillRate = 10) // 10 req/sec
)

val client = KfcClient.create(rateLimitingSettings = aggressiveSettings)
```

### 6.4 Rate Limiting 비활성화

```kotlin
// Rate Limiting 완전히 비활성화
val unlimitedSettings = RateLimitingSettings.unlimited()
val client = KfcClient.create(rateLimitingSettings = unlimitedSettings)

// 또는 Source별 비활성화
val partialSettings = RateLimitingSettings(
    krx = RateLimitConfig(enabled = false),  // KRX만 비활성화
    naver = RateLimitConfig(),               // Naver는 기본 설정
    opendart = RateLimitConfig()             // OPENDART는 기본 설정
)

val client = KfcClient.create(rateLimitingSettings = partialSettings)
```

### 6.5 Rate Limiter 상태 조회

```kotlin
// API 호출 전 상태 확인 (선택)
val client = KfcClient.create()

// 내부 rate limiter 접근 (필요시)
val krxRateLimiter: RateLimiter = client.krx.getRateLimiter()
val status = krxRateLimiter.getStatus()

println("KRX Rate Limiter Status:")
println("  Available tokens: ${status.availableTokens}/${status.capacity}")
println("  Refill rate: ${status.refillRate} req/sec")
println("  Estimated wait: ${status.estimatedWaitTimeMs}ms")
```

### 6.6 병렬 요청 처리

```kotlin
// Rate limiting이 자동으로 직렬화 처리
val client = KfcClient.create()

// Coroutine으로 병렬로 요청을 시작하면
// 내부적으로 rate limiting이 순차 처리
val results = coroutineScope {
    isinList.map { isin ->
        async {
            // 각 요청은 자동으로 rate limit을 준수
            client.krx.getEtfOhlcv(isin, startDate, endDate)
        }
    }
}.awaitAll()
```

---

## 구현 아키텍처

### 7.1 패키지 구조

```
internal/
├── ratelimit/
│   ├── RateLimitConfig.kt           # 설정 데이터 클래스
│   ├── RateLimiter.kt               # Rate Limiter 인터페이스
│   ├── TokenBucketRateLimiter.kt    # Token Bucket 구현
│   ├── RateLimitInterceptor.kt      # HTTP 요청 인터셉터 (선택)
│   └── RateLimitException.kt        # Rate Limiting 예외
│
├── krx/etf/
│   ├── KrxEtfApiImpl.kt              # Rate Limiter 주입
│   └── ...
│
├── naver/etf/
│   ├── NaverEtfApiImpl.kt            # Rate Limiter 주입
│   └── ...
│
└── opendart/
    ├── OpenDartApiImpl.kt            # Rate Limiter 주입
    └── ...
```

### 7.2 상세 구현 개요

#### 7.2.1 TokenBucketRateLimiter 구현

```kotlin
internal class TokenBucketRateLimiter(
    private val config: RateLimitConfig
) : RateLimiter {

    private val lock = Mutex()
    private var tokens: Double = config.capacity.toDouble()
    private var lastRefillTime: Long = System.currentTimeMillis()

    override suspend fun acquire(tokensNeeded: Int) {
        if (!config.enabled) return

        val deadline = System.currentTimeMillis() + config.waitTimeoutMillis

        lock.withLock {
            while (true) {
                // 토큰 충전
                refillTokens()

                // 토큰 충분하면 소비
                if (tokens >= tokensNeeded) {
                    tokens -= tokensNeeded
                    return@withLock
                }

                // 타임아웃 체크
                if (System.currentTimeMillis() > deadline) {
                    throw RateLimitTimeoutException(
                        source = "Rate Limiter",
                        config = config
                    )
                }

                // 대기
                val waitMs = calculateWaitTime(tokensNeeded)
                delay(waitMs)
            }
        }
    }

    private fun refillTokens() {
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - lastRefillTime) / 1000.0
        val tokensToAdd = elapsedSeconds * config.refillRate

        tokens = minOf(tokens + tokensToAdd, config.capacity.toDouble())
        lastRefillTime = now
    }

    private fun calculateWaitTime(tokensNeeded: Int): Long {
        val deficit = tokensNeeded - tokens
        val waitSeconds = deficit / config.refillRate
        return (waitSeconds * 1000).toLong()
    }

    override fun getAvailableTokens(): Int {
        return tokens.toInt()
    }

    override fun getWaitTimeMillis(): Long {
        return if (tokens < 1) {
            (1000 / config.refillRate).toLong()
        } else {
            0
        }
    }

    override fun getStatus(): RateLimiterStatus {
        return RateLimiterStatus(
            availableTokens = tokens.toInt(),
            capacity = config.capacity,
            refillRate = config.refillRate,
            isEnabled = config.enabled,
            estimatedWaitTimeMs = getWaitTimeMillis()
        )
    }
}
```

#### 7.2.2 API 구현체에 Rate Limiter 주입

```kotlin
internal class KrxEtfApiImpl(
    private val httpClient: HttpClient = KrxHttpClient.get(),
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(
        RateLimitingSettings.krxDefault()
    )
) : KrxEtfApi {

    override suspend fun getEtfList(): List<EtfListItem> {
        // Rate limiting 적용
        rateLimiter.acquire()

        return try {
            // 실제 API 호출
            val response = httpClient.post(BASE_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(mapOf("bld" to "MCD_equSrtistStats01", ...))
            }

            // 응답 파싱
            parseEtfList(response)
        } catch (e: Exception) {
            throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED, e)
        }
    }

    override suspend fun getEtfOhlcv(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfOhlcv> {
        // 큰 기간은 여러 번의 API 호출 필요
        val ranges = splitDateRange(fromDate, toDate)

        return ranges.flatMap { range ->
            // 각 범위마다 rate limiting 적용
            rateLimiter.acquire()

            val response = httpClient.post(BASE_URL) { ... }
            parseEtfOhlcv(response)
        }
    }
}
```

---

## 테스트 전략

### 8.1 단위 테스트

```kotlin
// TokenBucketRateLimiter 단위 테스트
class TokenBucketRateLimiterTest {

    @Test
    fun testBasicAcquisition() = runTest {
        val config = RateLimitConfig(capacity = 10, refillRate = 10)
        val limiter = TokenBucketRateLimiter(config)

        // 10개 토큰 모두 소비 가능
        repeat(10) {
            limiter.acquire()
        }

        // 11번째 요청은 1초 대기
        val start = System.currentTimeMillis()
        limiter.acquire()
        val elapsed = System.currentTimeMillis() - start

        assertTrue(elapsed >= 900) // ~1000ms
    }

    @Test
    fun testBurstAllowed() = runTest {
        val config = RateLimitConfig(capacity = 5, refillRate = 1)
        val limiter = TokenBucketRateLimiter(config)

        // 초기 5개 토큰으로 버스트 가능
        val start = System.currentTimeMillis()
        repeat(5) {
            limiter.acquire()
        }
        val elapsed = System.currentTimeMillis() - start

        assertTrue(elapsed < 100) // 거의 즉시
    }

    @Test
    fun testTimeoutException() = runTest {
        val config = RateLimitConfig(
            capacity = 1,
            refillRate = 1,
            waitTimeoutMillis = 100
        )
        val limiter = TokenBucketRateLimiter(config)

        limiter.acquire() // 마지막 토큰 소비

        // 다음 요청은 타임아웃
        assertThrows<RateLimitTimeoutException> {
            runBlocking {
                limiter.acquire()
            }
        }
    }

    @Test
    fun testDisabledLimiter() = runTest {
        val config = RateLimitConfig(enabled = false)
        val limiter = TokenBucketRateLimiter(config)

        // 토큰이 없어도 즉시 반환
        repeat(1000) {
            limiter.acquire()
        }
    }

    @Test
    fun testConcurrentAcquisition() = runTest {
        val config = RateLimitConfig(capacity = 10, refillRate = 10)
        val limiter = TokenBucketRateLimiter(config)

        val start = System.currentTimeMillis()

        // 10개 요청 동시 진행 -> 모두 즉시 처리
        val jobs = (0..9).map {
            launch { limiter.acquire() }
        }
        joinAll(jobs)

        // 11번째 요청 -> 1초 대기
        limiter.acquire()

        val elapsed = System.currentTimeMillis() - start
        assertTrue(elapsed >= 900)
    }
}
```

### 8.2 통합 테스트

```kotlin
// KfcClient Rate Limiting 통합 테스트
class KfcClientRateLimitingIntegrationTest {

    @Test
    fun testKrxRateLimiting() = runTest {
        val customSettings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 3, refillRate = 3, enabled = true)
        )
        val client = KfcClient.create(rateLimitingSettings = customSettings)

        val start = System.currentTimeMillis()

        // 처음 3개는 빠르고, 4번째는 1초 대기 필요
        repeat(5) {
            client.krx.getEtfList()
        }

        val elapsed = System.currentTimeMillis() - start
        assertTrue(elapsed >= 900) // 최소 1초
    }

    @Test
    fun testSourceIndependence() = runTest {
        val settings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 2, refillRate = 2),
            naver = RateLimitConfig(capacity = 10, refillRate = 10)
        )
        val client = KfcClient.create(rateLimitingSettings = settings)

        // KRX와 Naver의 rate limit이 독립적
        repeat(5) {
            client.krx.getEtfList()      // rate limit 적용
            client.naver.getAdjustedOhlcv(...)  // 다른 limit
        }
    }
}
```

### 8.3 성능 테스트

```kotlin
// Rate Limiting 성능 벤치마크
class RateLimitingPerformanceTest {

    @Test
    fun benchmarkTokenBucket() {
        val config = RateLimitConfig(capacity = 1000, refillRate = 1000)
        val limiter = TokenBucketRateLimiter(config)

        val start = System.nanoTime()
        repeat(10000) {
            runBlocking { limiter.acquire() }
        }
        val elapsed = System.nanoTime() - start

        val opsPerSecond = 10000 * 1e9 / elapsed
        println("Operations per second: $opsPerSecond")
        assertTrue(opsPerSecond > 100000) // 최소 100k ops/sec
    }
}
```

---

## 마이그레이션 가이드

### 9.1 버전 변경

이 기능은 다음 버전에서 도입됩니다:

- **v1.0.0 → v1.1.0** (Minor Version 증가)
  - Breaking change 아님 (하위 호환)
  - 기존 코드는 수정 없이 작동
  - 자동으로 rate limiting 적용됨

### 9.2 기존 코드 영향도

```kotlin
// v1.0.0 코드 (변경 필요 없음)
val client = KfcClient.create()
val etfList = client.krx.getEtfList()
// ↓ v1.1.0에서도 동일하게 작동
// 단, 자동으로 rate limiting 적용됨
```

### 9.3 업그레이드 경로

#### 기본 (아무 변경 없음)
```kotlin
// v1.0.0 → v1.1.0
val client = KfcClient.create()  // 기본 rate limiting 적용
```

#### 커스터마이징 필요시
```kotlin
// v1.1.0+
val customSettings = RateLimitingSettings(
    krx = RateLimitConfig(capacity = 20, refillRate = 20),
    naver = RateLimitConfig(enabled = false)
)
val client = KfcClient.create(rateLimitingSettings = customSettings)
```

### 9.4 문서 업데이트 계획

| 문서 | 변경 | 우선도 |
|------|------|--------|
| README | Rate Limiting 개요 추가 | P0 |
| API 문서 | Rate Limiting 파라미터 설명 | P0 |
| 아키텍처 문서 | 라이브러리 책임 재정의 | P0 |
| 튜토리얼 | Rate Limiting 커스터마이징 예시 | P1 |
| FAQ | Rate Limiting 관련 Q&A | P1 |

---

## 요약

### 핵심 특징

- ✅ **Token Bucket 알고리즘**: 유연하고 공정한 rate limiting
- ✅ **Source별 독립 설정**: 각 API의 특성에 맞는 설정
- ✅ **자동 대기**: 사용자가 명시적으로 처리할 필요 없음
- ✅ **투명한 처리**: 마치 rate limiting이 없는 것처럼 동작
- ✅ **커스터마이징**: 필요시 설정 변경 가능
- ✅ **하위 호환**: 기존 코드 수정 불필요

### 설정 비교표

| 설정 | 사용 시나리오 |
|------|-------------|
| **기본 (권장)** | 대부분의 사용 사례 |
| **보수적** | 느린 인터넷, 서버 부하 우려 |
| **적극적** | 고속 인터넷, 대량 데이터 수집 |
| **비활성화** | 프라이빗 환경, 격리 테스트 |

### 예상 이점

1. **사용자 편의**: 예외 처리 불필요
2. **서버 안정성**: API 서버 부하 감소
3. **신뢰성**: 안정적인 데이터 수집
4. **유연성**: 커스터마이징 가능
5. **표준 준수**: REST API 모범 사례 따름

---

**작성일**: 2025-01-20
**버전**: v1.0
**상태**: Ready for Implementation
