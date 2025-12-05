# KFC (Korea Financial Client)

> 🇰🇷 Kotlin library for accessing Korean financial market data from KRX, Naver, and OPENDART

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![JDK](https://img.shields.io/badge/JDK-21-orange.svg)](https://openjdk.org/)
[![Ktor](https://img.shields.io/badge/Ktor-3.3.2-blueviolet.svg)](https://ktor.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

**KFC** (Korea Financial Client)는 한국 금융 시장 데이터를 수집하는 Kotlin 라이브러리입니다. KRX(한국거래소), Naver Finance, OPENDART의 데이터를 타입 안전하고 사용하기 쉬운 API로 제공합니다.

---

## ✨ Features

- 🎯 **Type-Safe API**: 명시적 타입 변환으로 런타임 에러 최소화
- 🚀 **Coroutine Support**: Kotlin Coroutines 기반 비동기 API
- 🛡️ **Built-in Rate Limiting**: Token Bucket 알고리즘 기반 자동 속도 제어
- 🔄 **Auto-Retry**: 토큰 부족 시 자동 대기 및 재시도
- 📦 **Unified Client**: 5개 도메인을 하나의 통합 클라이언트로 제공
- 🎨 **Clean Architecture**: 도메인별 명확한 책임 분리
- ⚡ **High Performance**: GlobalRateLimiters를 통한 JVM 전역 속도 제어
- 🧪 **Well Tested**: 100% API 커버리지 (Unit + Integration Tests)

---

## 📊 Supported Domains & APIs

KFC는 5개의 도메인 API를 제공하며, 총 **29개의 메서드**를 통해 한국 금융 시장 데이터에 접근할 수 있습니다.

| Domain | API Count | Data Sources | Description |
|--------|-----------|--------------|-------------|
| **Funds** | 13 | KRX, Naver | ETF 목록, 상세정보, 포트폴리오, 성과지표, 투자자거래, 공매도 |
| **Price** | 2 | KRX, Naver | 시세, OHLCV, 조정주가 (분할/병합 반영) |
| **Stock** | 6 | KRX | 주식 종목 리스트, 기본정보, 업종분류, 산업그룹 |
| **Corp** | 4 | OPENDART | 법인코드, 배당정보, 주식분할/병합, 공시검색 |
| **Financials** | 4 | OPENDART | 손익계산서, 재무상태표, 현금흐름표, 전체 재무제표 |
| **Total** | **29** | | |

### Data Sources

- **KRX (한국거래소)**: ETF 메타데이터, 시세, 포트폴리오, 투자자거래 등
- **Naver Finance**: 조정주가 OHLCV (분할/병합 반영)
- **OPENDART (금융감독원)**: 법인정보, 공시, 재무제표

---

## 🚀 Quick Start

### Installation

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.kairoscode:kfc:1.0.0")
}
```

#### Gradle (Groovy)

```groovy
dependencies {
    implementation 'dev.kairoscode:kfc:1.0.0'
}
```

#### Maven

```xml
<dependency>
    <groupId>dev.kairoscode</groupId>
    <artifactId>kfc</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```kotlin
import dev.kairoscode.kfc.api.KfcClient
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

fun main() = runBlocking {
    // 1. Create client
    val kfc = KfcClient.create(
        opendartApiKey = "YOUR_API_KEY" // Optional, required for Corp/Financials domains
    )

    // 2. Funds Domain: Get ETF list
    val etfList = kfc.funds.getList()
    println("Total ETFs: ${etfList.size}")
    println("First ETF: ${etfList.first().name} (${etfList.first().ticker})")

    // 3. Price Domain: Get OHLCV data
    val ohlcv = kfc.price.getOhlcv(
        isin = "KR7069500007", // KODEX 200
        fromDate = LocalDate.of(2024, 1, 1),
        toDate = LocalDate.of(2024, 1, 31)
    )
    println("OHLCV data: ${ohlcv.size} days")

    // 4. Stock Domain: Get stock list
    val stocks = kfc.stock.getStockList()
    println("Total stocks: ${stocks.size}")

    // 5. Corp Domain: Get corporate codes
    val corpCodes = kfc.corp?.getCorpCodeList()
    val kodex200Corp = corpCodes?.find { it.stockCode == "069500" }
    println("KODEX 200 Corp Code: ${kodex200Corp?.corpCode}")

    // 6. Financials Domain: Get income statement
    val incomeStatement = kfc.financials?.getIncomeStatement(
        corpCode = "00126380",
        year = 2024
    )
    println("Income statement items: ${incomeStatement?.size}")
}
```

---

## 📚 API Documentation

### 1. Funds Domain API

ETF 펀드의 메타데이터, 포트폴리오, 성과지표 등을 조회합니다.

```kotlin
val kfc = KfcClient.create()

// ETF 목록 조회
val etfList = kfc.funds.getList()

// ETF 상세 정보 조회
val detail = kfc.funds.getDetailedInfo(
    isin = "KR7069500007",
    tradeDate = LocalDate.now()
)

// 포트폴리오 구성 종목
val portfolio = kfc.funds.getPortfolio(
    isin = "KR7069500007",
    date = LocalDate.now()
)

// 추적오차 (Tracking Error)
val trackingError = kfc.funds.getTrackingError(
    isin = "KR7069500007",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)

// 괴리율 (Divergence Rate)
val divergenceRate = kfc.funds.getDivergenceRate(
    isin = "KR7069500007",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)

// 투자자별 거래
val investorTrading = kfc.funds.getInvestorTrading(
    isin = "KR7069500007",
    date = LocalDate.now()
)

// 공매도 잔고
val shortBalance = kfc.funds.getShortBalance(
    isin = "KR7069500007",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)

// 공매도 거래
val shortSelling = kfc.funds.getShortSelling(
    isin = "KR7069500007",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

### 2. Price Domain API

시세 및 OHLCV 데이터를 조회합니다.

```kotlin
// KRX OHLCV (자동 분할 지원: 730일 초과 시 자동 분할 후 병합)
val ohlcv = kfc.price.getOhlcv(
    isin = "KR7069500007",
    fromDate = LocalDate.of(2020, 1, 1), // 5년치 데이터도 자동 처리
    toDate = LocalDate.of(2024, 12, 31)
)

// Naver 조정주가 OHLCV (분할/병합 반영)
val adjustedOhlcv = kfc.price.getAdjustedOhlcv(
    ticker = "069500",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

### 3. Stock Domain API

주식 종목 리스트, 기본정보, 업종분류 등을 조회합니다.

```kotlin
// 전체 종목 리스트
val allStocks = kfc.stock.getStockList()

// 시장별 종목 리스트
val kospiStocks = kfc.stock.getStockList(market = Market.KOSPI)
val kosdaqStocks = kfc.stock.getStockList(market = Market.KOSDAQ)

// 종목 정보 조회
val stockInfo = kfc.stock.getStockInfo(ticker = "005930") // 삼성전자

// 종목명 조회
val stockName = kfc.stock.getStockName(ticker = "005930")

// 종목 검색
val searchResults = kfc.stock.searchStocks(keyword = "삼성")

// 업종분류 현황
val sectors = kfc.stock.getSectorClassifications(market = Market.KOSPI)

// 산업별 그룹화
val industryGroups = kfc.stock.getIndustryGroups()
```

### 4. Corp Domain API

기업 공시 관련 데이터를 조회합니다 (OPENDART API Key 필요).

```kotlin
val kfc = KfcClient.create(opendartApiKey = "YOUR_API_KEY")

// 법인코드 목록 조회
val corpCodes = kfc.corp?.getCorpCodeList()
val kodex200 = corpCodes?.find { it.stockCode == "069500" }

// 배당 정보 조회
val dividends = kfc.corp?.getDividendInfo(
    corpCode = "00164779",
    year = 2024
)

// 주식 분할/병합 정보
val stockSplits = kfc.corp?.getStockSplitInfo(
    corpCode = "00164779",
    year = 2024
)

// 공시 검색
val disclosures = kfc.corp?.searchDisclosures(
    corpCode = "00164779",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

### 5. Financials Domain API

재무제표 데이터를 조회합니다 (OPENDART API Key 필요).

```kotlin
// 손익계산서
val incomeStatement = kfc.financials?.getIncomeStatement(
    corpCode = "00126380",
    year = 2024,
    reportCode = ReportCode.Q1 // 분기별 또는 연간
)

// 재무상태표
val balanceSheet = kfc.financials?.getBalanceSheet(
    corpCode = "00126380",
    year = 2024
)

// 현금흐름표
val cashFlowStatement = kfc.financials?.getCashFlowStatement(
    corpCode = "00126380",
    year = 2024
)

// 전체 재무제표 (손익계산서 + 재무상태표 + 현금흐름표)
val allFinancials = kfc.financials?.getAllFinancials(
    corpCode = "00126380",
    year = 2024
)
```

---

## ⚙️ Rate Limiting

KFC는 **Token Bucket 알고리즘** 기반의 Rate Limiting을 내장하여 API 호출 속도를 자동으로 제어합니다.

### Default Configuration

각 API 소스별 기본 설정은 실제 테스트를 통해 측정된 한계값을 기준으로 설정되어 있습니다:

| API Source | Rate Limit | Test Result | Default Config |
|------------|------------|-------------|----------------|
| **KRX** | ~25 RPS | RPS 25: 100% ✓ / RPS 30: 72% | `capacity=25, refillRate=25` |
| **Naver** | TBD | - | `capacity=50, refillRate=50` |
| **OPENDART** | 40,000 req/day | - | `capacity=50, refillRate=50` |

### Basic Usage (Default Settings)

```kotlin
// 기본 설정 사용 (권장)
val client = KfcClient.create()
```

### Custom Rate Limiting

소스별로 다른 Rate Limit을 설정할 수 있습니다:

```kotlin
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimitConfig
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimitingSettings

val customSettings = RateLimitingSettings(
    krx = RateLimitConfig(
        capacity = 25,            // 최대 버스트 크기
        refillRate = 25,          // 초당 토큰 충전 속도 (RPS)
        enabled = true,
        waitTimeoutMillis = 60000 // 대기 타임아웃 (60초)
    ),
    naver = RateLimitConfig(capacity = 50, refillRate = 50),
    opendart = RateLimitConfig(capacity = 50, refillRate = 50)
)

val client = KfcClient.create(rateLimitingSettings = customSettings)
```

### How It Works

1. **Token Bucket Algorithm**: 초기에 최대 용량(`capacity`)만큼의 토큰으로 시작
2. **Auto Refill**: 시간 경과에 따라 `refillRate`(초당 토큰 수)만큼 자동 충전
3. **Auto Wait**: 토큰 부족 시 충전될 때까지 자동 대기 (10ms 간격 재시도)
4. **Timeout**: `waitTimeoutMillis` 초과 시 `RateLimitTimeoutException` 발생
5. **Global Singleton**: 동일 JVM 프로세스 내 모든 `KfcClient` 인스턴스가 소스별 Rate Limiter 공유

### GlobalRateLimiters

KFC는 `GlobalRateLimiters` 싱글톤을 사용하여 JVM 프로세스 전역에서 Rate Limiter를 공유합니다:

```kotlin
// 첫 번째 클라이언트 생성 (이 설정이 전역으로 적용됨)
val client1 = KfcClient.create(
    rateLimitingSettings = RateLimitingSettings(
        krx = RateLimitConfig(capacity = 25, refillRate = 25)
    )
)

// 두 번째 클라이언트 생성 (위와 동일한 Rate Limiter 공유)
val client2 = KfcClient.create()
// ✅ client1과 client2는 동일한 KRX Rate Limiter를 공유
// → 두 클라이언트의 API 호출이 합쳐져도 25 RPS를 초과하지 않음
```

**중요**: 첫 번째 `KfcClient.create()` 호출 시 전달된 `rateLimitingSettings`가 해당 JVM 프로세스의 Rate Limiter를 초기화합니다. 이후 호출에서는 동일한 Rate Limiter 인스턴스가 재사용됩니다.

### Disable Rate Limiting

```kotlin
// 모든 소스의 Rate Limiting 비활성화 (권장하지 않음)
val unlimitedSettings = RateLimitingSettings.unlimited()
val client = KfcClient.create(rateLimitingSettings = unlimitedSettings)
```

---

## 🧪 Testing

KFC는 **Unit Test**와 **Integration Test**를 통해 API 안정성을 보장합니다.

### Test Structure

```
src/test/kotlin/
├── unit/                     # Unit Tests (40 tests)
│   ├── corp/                 # CorpApi unit tests
│   ├── financials/           # FinancialsApi unit tests
│   ├── funds/                # FundsApi unit tests
│   └── ratelimit/            # GlobalRateLimiters unit tests
│
└── integration/              # Integration Tests (95 tests)
    ├── krx/                  # KRX API integration tests
    ├── opendart/             # OPENDART API integration tests
    └── utils/                # Test utilities
```

### Run Tests

#### Unit Tests (Fast, No API Key Required)

```bash
./gradlew unitTest
```

- **실행 시간**: ~5초
- **테스트 수**: 40개
- **특징**: Mock 데이터 사용, API 키 불필요

#### Integration Tests (Live API Calls)

```bash
# 1. Set OPENDART API Key (optional, for Corp/Financials tests)
echo "OPENDART_API_KEY=your_key_here" > local.properties

# 2. Run integration tests
./gradlew integrationTest
```

- **실행 시간**: ~90초
- **테스트 수**: 95개 (1개 skip)
- **특징**: 실제 API 호출, Rate Limiting 자동 적용

#### All Tests

```bash
./gradlew test
```

### Test Coverage

| Domain | API Methods | Unit Tests | Integration Tests |
|--------|-------------|------------|-------------------|
| **Funds** | 13 | ✅ 13/13 | ✅ 13/13 |
| **Price** | 2 | ✅ 2/2 | ✅ 2/2 |
| **Stock** | 6 | - | ✅ 6/6 |
| **Corp** | 4 | ✅ 4/4 | ✅ 4/4 |
| **Financials** | 4 | ✅ 4/4 | ✅ 4/4 |
| **RateLimiting** | - | ✅ 10/10 | - |
| **Total** | **29** | **✅ 40 tests** | **✅ 95 tests** |

---

## 🏗️ Architecture

### Layered Architecture

KFC는 레이어드 아키텍처를 기반으로 비즈니스 영역별로 API를 구분하여 제공합니다:

```
┌─────────────────────────────────────────────────────────────┐
│                    KfcClient (Facade)                       │
│   - Unified entry point for all domains                    │
│   - GlobalRateLimiters integration                         │
└────────────┬────────────────────────────────────────────────┘
             │
    ┌────────┴────────┬───────────┬──────────┬──────────────┐
    │                 │           │          │              │
┌───▼────┐  ┌────────▼──┐  ┌─────▼────┐  ┌─▼──────┐  ┌───▼────────┐
│ Funds  │  │  Price    │  │  Stock   │  │  Corp  │  │ Financials │
│ Domain │  │  Domain   │  │  Domain  │  │ Domain │  │  Domain    │
└───┬────┘  └────┬──────┘  └─────┬────┘  └─┬──────┘  └───┬────────┘
    │            │               │         │             │
┌───▼────────────▼───────────────▼─────────▼─────────────▼────────┐
│            Infrastructure Layer                                  │
│  - KrxFundsApiImpl, KrxStockApiImpl                             │
│  - NaverFundsApiImpl                                            │
│  - OpenDartApiImpl                                              │
│  - GlobalRateLimiters (Singleton)                               │
│  - HTTP Client, Parser, Type Converter                          │
└──────────────────────────────────────────────────────────────────┘
```

### Package Structure

```
dev.kairoscode.kfc/
├── api/                          # Public API
│   ├── FundsApi.kt               # Funds domain interface
│   ├── PriceApi.kt               # Price domain interface
│   ├── StockApi.kt               # Stock domain interface
│   ├── CorpApi.kt                # Corp domain interface
│   ├── FinancialsApi.kt          # Financials domain interface
│   └── KfcClient.kt              # Facade
│
├── model/                        # Data models
│   ├── krx/                      # KRX models
│   ├── naver/                    # Naver models
│   ├── opendart/                 # OPENDART models
│   └── common/                   # Common models
│
├── infrastructure/               # Implementation layer
│   ├── krx/                      # KRX API implementations
│   ├── naver/                    # Naver API implementations
│   ├── opendart/                 # OPENDART API implementations
│   └── common/
│       └── ratelimit/            # Rate limiting
│           ├── GlobalRateLimiters.kt
│           ├── TokenBucketRateLimiter.kt
│           └── RateLimitConfig.kt
│
└── exception/                    # Exception handling
    ├── KfcException.kt
    └── ErrorCode.kt
```

---

## 🔧 Exception Handling

모든 예외는 `KfcException`으로 통합되며, `ErrorCode`를 통해 에러 종류를 구분합니다.

### Example

```kotlin
import dev.kairoscode.kfc.exception.*

try {
    val etfList = kfc.funds.getList()
} catch (e: KfcException) {
    when (e.errorCode) {
        ErrorCode.NETWORK_CONNECTION_FAILED -> println("Network error")
        ErrorCode.HTTP_ERROR_RESPONSE -> println("HTTP error: ${e.message}")
        ErrorCode.RATE_LIMIT_EXCEEDED -> println("Rate limit exceeded")
        ErrorCode.KRX_API_ERROR -> println("KRX API error")
        else -> println("Unknown error: ${e.message}")
    }
}
```

### Error Codes

| Code | Category | Error Code | Description |
|------|----------|-----------|-------------|
| 1001 | Network | `NETWORK_CONNECTION_FAILED` | 네트워크 연결 실패 |
| 1002 | Network | `NETWORK_TIMEOUT` | 네트워크 타임아웃 |
| 1003 | Network | `HTTP_REQUEST_FAILED` | HTTP 요청 실패 |
| 1004 | Network | `HTTP_ERROR_RESPONSE` | HTTP 오류 응답 |
| 2001 | Parsing | `JSON_PARSE_ERROR` | JSON 파싱 실패 |
| 2002 | Parsing | `XML_PARSE_ERROR` | XML 파싱 실패 |
| 2003 | Parsing | `INVALID_DATA_FORMAT` | 잘못된 데이터 형식 |
| 3001 | API | `KRX_API_ERROR` | KRX API 오류 |
| 3002 | API | `OPENDART_API_ERROR` | OPENDART API 오류 |
| 3003 | API | `NAVER_API_ERROR` | Naver API 오류 |
| 4001 | Rate Limit | `RATE_LIMIT_EXCEEDED` | API 호출 제한 초과 |
| 5001 | Validation | `INVALID_DATE_RANGE` | 잘못된 날짜 범위 |
| 5002 | Validation | `INVALID_PARAMETER` | 잘못된 파라미터 |

---

## 📋 Requirements

- **Kotlin**: 2.2.21+
- **JDK**: 21 (LTS)
- **Gradle**: 8.0+
- **Kotlinx Coroutines**: 1.8.0+
- **Ktor Client**: 3.3.2+

---

## 🗺️ Roadmap

### v1.0.0 (Current)

- [x] KRX API 구현 (15개 함수)
- [x] Naver API 구현 (1개 함수)
- [x] OPENDART API 구현 (8개 함수)
- [x] 5개 도메인 API (Funds, Price, Stock, Corp, Financials)
- [x] GlobalRateLimiters 구현 (JVM 전역 Rate Limiting)
- [x] 포괄적인 테스트 작성 (Unit + Integration)
- [ ] Maven Central 배포
- [ ] API 문서 사이트 구축

### v2.0.0 (Future)

- [ ] KRX 채권 API 추가
- [ ] KRX 파생상품 API 추가
- [ ] 실시간 시세 WebSocket 지원

### v3.0.0 (Future)

- [ ] Yahoo Finance API 추가
- [ ] Alpha Vantage API 추가
- [ ] 다중 데이터 소스 통합 조회

---

## 🤝 Contributing

KFC는 오픈소스 프로젝트입니다. 기여를 환영합니다!

### How to Contribute

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone repository
git clone https://github.com/kairos-code-dev/kfc.git
cd kfc

# Run tests
./gradlew test

# Build
./gradlew build
```

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Credits

- Inspired by [pykrx](https://github.com/sharebook-kr/pykrx) (Python library for Korean financial data)
- Data sources:
  - [KRX (한국거래소)](http://data.krx.co.kr)
  - [Naver Finance](https://finance.naver.com)
  - [OPENDART (금융감독원)](https://opendart.fss.or.kr)

---

## 📞 Contact

- **Author**: Kairos
- **Email**: ulalax@kairoscode.dev
- **Website**: https://www.kairoscode.dev
- **GitHub**: https://github.com/kairos-code-dev/kfc

---

## ⚠️ Disclaimer

- 이 라이브러리는 KRX, Naver, OPENDART에서 공개한 데이터를 사용합니다
- KRX와 Naver는 공식 API 문서가 없으며, 웹사이트에서 사용하는 API를 분석하여 구현했습니다
- OPENDART API 사용 시 API Key가 필요합니다 ([발급 방법](https://opendart.fss.or.kr/mng/apiKey.do))
- 각 데이터 소스의 이용 약관 및 이용 정책을 확인하고 준수해야 합니다
- **투자 판단은 본인의 책임**이며, 이 라이브러리는 투자 조언을 제공하지 않습니다
- 데이터의 정확성과 완전성을 보장하지 않습니다. 중요한 결정에는 공식 출처를 확인하세요

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/kairos-code-dev">Kairos Code</a>
</p>

<p align="center">
  <a href="#kfc-korea-financial-client">⬆️ Back to top</a>
</p>
