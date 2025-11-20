# KFC 
(Korea Financial data Collector)

> Kotlin library for collecting KRX, Naver, and OPENDART ETF data

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![JDK](https://img.shields.io/badge/JDK-21-orange.svg)](https://openjdk.org/)
[![Ktor](https://img.shields.io/badge/Ktor-3.3.2-blueviolet.svg)](https://ktor.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

**kotlin-krx** (Korea Finance Collector, KFC)는 한국 금융 데이터를 수집하는 Kotlin 라이브러리입니다. KRX(한국거래소), Naver Finance, OPENDART의 ETF 데이터를 타입 안전하게 수집할 수 있습니다.

---

## Features

- ✅ **타입 안전**: 명시적 타입 변환 (`String → Int, BigDecimal, LocalDate`)
- ✅ **소스별 분류**: KRX, Naver, OPENDART API를 독립적으로 관리
- ✅ **자동 분할**: KRX API의 730일 제한을 자동으로 처리
- ✅ **Facade 패턴**: 통합 API 클라이언트로 간편한 사용
- ✅ **코루틴 지원**: Kotlin Coroutines 기반 비동기 API
- ✅ **Rate Limiting**: Token Bucket 알고리즘 기반의 내장 속도 제어 (소스별 독립 설정 가능)
- ✅ **확장 가능**: 새로운 데이터 소스 및 상품 추가 용이

---

## Supported APIs

### v1.0.0 (ETF 전용)

| 데이터 소스 | 함수 수 | 주요 기능 |
|------------|--------|----------|
| **KRX** | 15 | ETF 목록, OHLCV, 포트폴리오, 추적오차, 괴리율, 투자자거래, 공매도 |
| **Naver** | 1 | 조정주가 OHLCV (분할/병합 반영) |
| **OPENDART** | 4 | 법인코드, 배당정보, 분할/병합 정보, 공시목록 |
| **총계** | **20** | |

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.ulalax:kotlin-krx:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.ulalax:kotlin-krx:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.ulalax</groupId>
    <artifactId>kotlin-krx</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Quick Start

### 1. Facade 사용 (권장)

```kotlin
import dev.kairoscode.kfc.KfcClient
import java.time.LocalDate

suspend fun main() {
    // 클라이언트 생성
    val client = KfcClient.create(
        opendartApiKey = "YOUR_OPENDART_API_KEY" // 선택적
    )

    // KRX API: ETF 목록 조회
    val etfList = client.krx.getEtfList()
    println("ETF 개수: ${etfList.size}")

    // KRX API: ETF OHLCV 조회
    val ohlcv = client.krx.getEtfOhlcv(
        isin = "KR7152100004", // ARIRANG 200
        fromDate = LocalDate.of(2024, 1, 1),
        toDate = LocalDate.of(2024, 12, 31)
    )
    println("OHLCV 데이터: ${ohlcv.size}일")

    // Naver API: 조정주가 조회
    val adjustedOhlcv = client.naver.getAdjustedOhlcv(
        ticker = "152100",
        fromDate = LocalDate.of(2024, 1, 1),
        toDate = LocalDate.of(2024, 12, 31)
    )
    println("조정주가 데이터: ${adjustedOhlcv.size}일")

    // OPENDART API: 법인코드 목록 조회
    val corpCodes = client.opendart?.getCorpCodeList()
    println("법인코드 개수: ${corpCodes?.size}")
}
```

### 2. 개별 API 사용 (세밀한 제어)

```kotlin
import dev.kairoscode.kfc.api.krx.KrxEtfApi
import dev.kairoscode.kfc.api.naver.NaverEtfApi
import dev.kairoscode.kfc.api.opendart.OpenDartApi

suspend fun main() {
    // 개별 API 클라이언트 생성
    val krxApi = KrxEtfApiFactory.create()
    val naverApi = NaverEtfApiFactory.create()
    val openDartApi = OpenDartApiFactory.create(apiKey = "YOUR_API_KEY")

    // 사용
    val etfList = krxApi.getEtfList()
    val adjustedClose = naverApi.getAdjustedClose(...)
    val corpCodes = openDartApi.getCorpCodeList()
}
```

---

## Rate Limiting

KFC는 Token Bucket 알고리즘 기반의 Rate Limiting을 내장하고 있어 API 호출 속도를 자동으로 제어합니다.

### Rate Limiting 활성화

기본적으로 각 API 소스별로 **초당 50개 요청(req/sec)**의 레이트 제한이 적용됩니다.

```kotlin
// 기본 설정 사용 (KRX/Naver/OPENDART 모두 50 req/sec)
val client = KfcClient.create()
```

### 커스텀 Rate Limiting 설정

```kotlin
import dev.kairoscode.kfc.internal.ratelimit.RateLimitConfig
import dev.kairoscode.kfc.internal.ratelimit.RateLimitingSettings

suspend fun main() {
    // 소스별로 다른 레이트 제한 설정
    val customSettings = RateLimitingSettings(
        krx = RateLimitConfig(
            capacity = 100,           // 최대 100개 요청
            refillRate = 100,         // 초당 100개 토큰 충전
            enabled = true,
            waitTimeoutMillis = 60000 // 60초 타임아웃
        ),
        naver = RateLimitConfig(
            capacity = 50,
            refillRate = 50,
            enabled = true,
            waitTimeoutMillis = 60000
        ),
        opendart = RateLimitConfig(
            capacity = 30,
            refillRate = 30,
            enabled = true,
            waitTimeoutMillis = 60000
        )
    )

    val client = KfcClient.create(rateLimitingSettings = customSettings)

    // 이제 각 API 호출이 설정된 레이트 제한을 따릅니다
    val etfList = client.krx.getEtfList()   // KRX 레이트 제한 적용
    val ohlcv = client.naver.getAdjustedOhlcv(...)  // Naver 레이트 제한 적용
}
```

### Rate Limiting 비활성화

```kotlin
// 모든 API 소스의 레이트 제한을 비활성화
val unlimitedSettings = RateLimitingSettings(
    krx = RateLimitConfig(enabled = false),
    naver = RateLimitConfig(enabled = false),
    opendart = RateLimitConfig(enabled = false)
)

val client = KfcClient.create(rateLimitingSettings = unlimitedSettings)
```

### Rate Limiting 동작 원리

- **Token Bucket Algorithm**: 초기에 최대 용량(capacity)만큼의 토큰으로 시작하며, 시간 경과에 따라 refillRate만큼 토큰이 충전됩니다
- **자동 대기**: 토큰이 부족하면 필요한 토큰이 충전될 때까지 자동으로 요청을 대기시킵니다
- **타임아웃**: waitTimeoutMillis를 초과하면 `RateLimitTimeoutException`이 발생합니다
- **소스 독립성**: 각 API 소스(KRX, Naver, OPENDART)는 독립적인 Rate Limiter를 사용합니다

---

## API Examples

### KRX ETF API (15개 함수)

#### ETF 목록 조회

```kotlin
val etfList = client.krx.etf.getEtfList()
etfList.forEach { etf ->
    println("${etf.ticker} ${etf.name} (${etf.totalExpenseRatio}%)")
}
```

#### ETF 상세 정보 조회

```kotlin
val detail = client.krx.etf.getEtfDetail(
    isin = "KR7069500007",
    date = LocalDate.now()
)
println("NAV: ${detail.nav}, 시가총액: ${detail.marketCap}")
```

#### ETF OHLCV 조회 (자동 분할 지원)

```kotlin
// 730일 초과 시 자동으로 분할 후 병합
val ohlcv = client.krx.etf.getEtfOhlcv(
    isin = "KR7069500007",
    fromDate = LocalDate(2020, 1, 1), // 5년치 데이터
    toDate = LocalDate(2024, 12, 31)
)
println("총 ${ohlcv.size}일치 OHLCV 데이터")
```

#### ETF 포트폴리오 구성 종목 조회

```kotlin
val portfolio = client.krx.etf.getEtfPortfolioConstituents(
    isin = "KR7069500007",
    date = LocalDate.now()
)
portfolio.forEach { stock ->
    println("${stock.ticker} ${stock.name}: ${stock.weight}%")
}
```

### Naver ETF API (1개 함수)

#### 조정 종가 조회

```kotlin
val adjustedClose = client.naver.etf.getAdjustedClose(
    ticker = "069500",
    fromDate = LocalDate(2024, 1, 1),
    toDate = LocalDate(2024, 12, 31)
)
adjustedClose.forEach { data ->
    println("${data.tradeDate}: ${data.adjustedClose}")
}
```

### OPENDART API (6개 함수)

#### 법인코드 목록 조회

```kotlin
val corpCodes = client.openDart.getCorpCodeList()
val kodex200 = corpCodes.find { it.stockCode == "069500" }
println("법인코드: ${kodex200?.corpCode}")
```

#### 배당 정보 조회

```kotlin
val dividends = client.openDart.getDividendInfo(
    corpCode = "00164779", // KODEX 200
    year = 2024
)
dividends.forEach { div ->
    println("${div.dividendDate}: ${div.dividendPerShare}원")
}
```

---

## Architecture

### 레이어 구조

```
┌─────────────────────────────────────────┐
│         API Layer (Public)              │
│  KrxEtfApi, NaverEtfApi, OpenDartApi    │
│  KfcClient (Facade)                     │
└─────────────────┬───────────────────────┘
                  │ (반환)
        Model (Data Transfer Objects)
      EtfListItem, EtfOhlcv, AdjustedClose
                  │ (사용)
┌─────────────────▼───────────────────────┐
│ Implementation Layer (Internal)         │
│  HTTP Client, Parser, Type Converter    │
└─────────────────────────────────────────┘
```

### 패키지 구조 (소스별 분류)

```
dev.kairoscode.kfc/
├── api/              # Public API
│   ├── krx/
│   ├── naver/
│   ├── opendart/
│   └── KfcClient.kt  # Facade
│
├── model/            # 데이터 클래스
│   ├── krx/etf/
│   ├── naver/etf/
│   ├── opendart/
│   └── common/
│
├── internal/         # 내부 구현 (internal)
│   ├── krx/etf/
│   ├── naver/etf/
│   ├── opendart/
│   └── http/
│
└── exception/        # 예외 클래스
```

---

## Exception Handling

모든 예외는 `KfcException`으로 통합되며, `ErrorCode`를 통해 에러 종류를 구분합니다.

### 사용 예시

```kotlin
import dev.kairoscode.kfc.exception.*

try {
    val etfList = client.krx.getEtfList()
} catch (e: KfcException) {
    when (e.errorCode) {
        ErrorCode.NETWORK_CONNECTION_FAILED -> println("네트워크 연결 실패")
        ErrorCode.HTTP_ERROR_RESPONSE -> println("HTTP 오류 응답")
        ErrorCode.JSON_PARSE_ERROR -> println("JSON 파싱 실패")
        ErrorCode.XML_PARSE_ERROR -> println("XML 파싱 실패")
        ErrorCode.KRX_API_ERROR -> println("KRX API 오류")
        ErrorCode.RATE_LIMIT_EXCEEDED -> println("API 호출 제한 초과")
        ErrorCode.INVALID_DATE_RANGE -> println("잘못된 날짜 범위")
        else -> println("오류: ${e.message}")
    }
}
```

### 에러 코드 목록

| 코드 | 번대 | 에러 코드 | 메시지 |
|------|------|---------|--------|
| 1001 | 1000번대 (네트워크) | `NETWORK_CONNECTION_FAILED` | 네트워크 연결에 실패했습니다 |
| 1002 | 1000번대 | `NETWORK_TIMEOUT` | 네트워크 요청 시간이 초과되었습니다 |
| 1003 | 1000번대 | `HTTP_REQUEST_FAILED` | HTTP 요청이 실패했습니다 |
| 1004 | 1000번대 | `HTTP_ERROR_RESPONSE` | HTTP 요청이 오류 응답을 반환했습니다 |
| 2001 | 2000번대 (파싱) | `JSON_PARSE_ERROR` | JSON 파싱에 실패했습니다 |
| 2002 | 2000번대 | `XML_PARSE_ERROR` | XML 파싱에 실패했습니다 |
| 2003 | 2000번대 | `INVALID_DATA_FORMAT` | 데이터 형식이 올바르지 않습니다 |
| 2004 | 2000번대 | `FIELD_TYPE_MISMATCH` | 필드의 타입이 예상과 다릅니다 |
| 2005 | 2000번대 | `REQUIRED_FIELD_MISSING` | 필수 필드가 누락되었습니다 |
| 2006 | 2000번대 | `NUMBER_FORMAT_ERROR` | 숫자 형식이 올바르지 않습니다 |
| 2007 | 2000번대 | `DATE_FORMAT_ERROR` | 날짜 형식이 올바르지 않습니다 |
| 2008 | 2000번대 | `ZIP_PARSE_ERROR` | ZIP 파일 파싱에 실패했습니다 |
| 3001 | 3000번대 (API) | `KRX_API_ERROR` | KRX API에서 오류가 발생했습니다 |
| 3002 | 3000번대 | `OPENDART_API_ERROR` | OPENDART API에서 오류가 발생했습니다 |
| 3003 | 3000번대 | `NAVER_API_ERROR` | Naver API에서 오류가 발생했습니다 |
| 4001 | 4000번대 (Rate Limit) | `RATE_LIMIT_EXCEEDED` | API 호출 제한을 초과했습니다 |
| 5001 | 5000번대 (검증) | `INVALID_DATE_RANGE` | 날짜 범위가 올바르지 않습니다 |
| 5002 | 5000번대 | `INVALID_PARAMETER` | 파라미터가 올바르지 않습니다 |
| 9999 | 9000번대 | `UNKNOWN_ERROR` | 알 수 없는 오류가 발생했습니다 |

---

## Requirements

- **Kotlin**: 2.0.21+
- **JDK**: 21 (LTS)
- **Kotlinx Coroutines**: 1.8.0+
- **Ktor Client**: 2.3.7+

---

## Documentation

- [프로젝트 개요](plan/01-프로젝트-개요.md)
- [라이브러리 아키텍처](plan/02-라이브러리-아키텍처.md)
- [패키지 구조](plan/03-패키지-구조.md)

---

## Roadmap

### v1.0.0 (현재 - 개발 진행 중)

- [x] 기획 문서 작성
- [x] 디렉토리 구조 생성
- [x] 프로젝트 초기화 (Gradle 멀티모듈, Kotlin 2.2.21, Ktor 3.3.2)
- [x] 정규화 유틸리티 구현 (BigDecimal 기반 금융 데이터 처리)
- [x] HTTP 클라이언트 인프라 구축
- [x] 에러 핸들링 구조 구현
- [x] MDCSTAT04701 구현 (ETF 종합정보 - 52주 고가/저가, 총보수 포함)
- [x] 나머지 KRX ETF API 구현 (14개 함수)
- [x] Naver ETF API 구현 (1개 함수)
- [x] OPENDART API 구현 (4개 함수)
- [x] Facade 패턴 적용 (KfcClient)
- [x] Rate Limiting 구현 (Token Bucket 알고리즘)
- [ ] 테스트 작성 (커버리지 80% 이상)
- [ ] Maven Central 배포

### v2.0.0 (향후)

- [ ] KRX 채권 API 추가
- [ ] KRX 주식 API 추가
- [ ] Naver 주식 API 추가

### v3.0.0 (향후)

- [ ] Yahoo Finance API 추가
- [ ] Investing.com API 추가

---

## Contributing

현재 기획 단계이며, 기여는 v1.0.0 릴리스 이후 받을 예정입니다.

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

## Credits

- Inspired by [pykrx](https://github.com/sharebook-kr/pykrx) (Python)
- Data sources: [KRX](http://data.krx.co.kr), [Naver Finance](https://finance.naver.com), [OPENDART](https://opendart.fss.or.kr)

---

## Contact

- **Author**: Kairos
- **Email**: ulalax@kairoscode.dev
- **Website**: https://www.kairoscode.dev
- **GitHub**: https://github.com/ulalax-kairos/kotlin-krx

---

**⚠️ 주의사항**

- 이 라이브러리는 KRX, Naver, OPENDART에서 공개한 API를 사용합니다.
- KRX와 Naver는 공식 API 문서가 없으며, 프론트엔드에서 사용하는 API를 분석하여 사용합니다.
- OPENDART API 사용 시 API Key가 필요합니다 ([발급 방법](https://opendart.fss.or.kr/mng/apiKey.do)).
- 각 데이터 소스의 이용 약관 및 이용 정책을 확인하고 준수해야 합니다.
- 투자 판단은 본인의 책임이며, 이 라이브러리는 투자 조언을 제공하지 않습니다.
