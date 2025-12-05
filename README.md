# KFC (Korea Free Financial Data Collector)

[![CI](https://github.com/kairos-code-dev/kfc/actions/workflows/ci.yml/badge.svg)](https://github.com/kairos-code-dev/kfc/actions/workflows/ci.yml)
[![Integration Test](https://github.com/kairos-code-dev/kfc/actions/workflows/integration-test.yml/badge.svg)](https://github.com/kairos-code-dev/kfc/actions/workflows/integration-test.yml)
[![JitPack](https://jitpack.io/v/kairos-code-dev/kfc.svg)](https://jitpack.io/#kairos-code-dev/kfc)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**한국 금융 데이터 수집을 위한 Kotlin 라이브러리**

KFC는 [pykrx](https://github.com/sharebook-kr/pykrx)에서 영감을 받아 만들어진 Kotlin 기반의 한국 금융 데이터 라이브러리입니다. KRX(한국거래소)와 OPENDART의 데이터를 Kotlin/JVM 환경에서 쉽고 안전하게 조회할 수 있습니다.

## Why KFC?

Python의 pykrx가 한국 금융 데이터 분석의 표준이 되었다면, KFC는 **JVM 생태계를 위한 대안**입니다.

| | pykrx (Python) | KFC (Kotlin) |
|---|---|---|
| **타입 안전성** | 런타임 에러 | 컴파일 타임 검증 |
| **동시성** | GIL 제약 | Coroutines 네이티브 지원 |
| **플랫폼** | Python | JVM (Android, Spring, Backend) |
| **Rate Limiting** | 수동 관리 | 자동 Rate Limiting 내장 |
| **재무제표** | 별도 구현 필요 | OPENDART 통합 |

### KFC가 필요한 경우

- **Android 앱**에서 주식 데이터를 표시하고 싶을 때
- **Spring Boot 백엔드**에서 금융 데이터 API를 구축할 때
- **Kotlin Multiplatform** 프로젝트에서 사용할 때
- **타입 안전성**이 중요한 금융 애플리케이션을 개발할 때
- **비동기 처리**가 필요한 대량 데이터 수집 시

## Features

### Data Coverage

| 도메인 | 데이터 | 소스 |
|--------|--------|------|
| **Stock** | 종목 리스트, 기본정보, 섹터/산업 분류 | KRX |
| **Funds** | ETF/ETN/REIT 목록, 포트폴리오, NAV, 괴리율, 투자자별 거래, 공매도 | KRX |
| **Index** | 지수 목록, OHLCV, PER/PBR, 구성 종목 | KRX |
| **Bond** | 국고채, 회사채, CD 수익률 | KRX |
| **Future** | 선물 상품 목록, OHLCV | KRX |
| **Price** | 분단위 시세, 일별 시세 | KRX |
| **Financials** | 손익계산서, 재무상태표, 현금흐름표 | OPENDART |
| **Corp** | 기업 공시, 배당 정보, 액면분할 | OPENDART |

### Technical Highlights

- **100% Kotlin** - Kotlin Coroutines 네이티브 지원
- **Type-Safe** - 모든 응답이 타입이 지정된 도메인 모델로 매핑
- **Auto Rate Limiting** - KRX API 제한(25 RPS)을 자동으로 준수
- **Clean Architecture** - 도메인 중심 설계로 확장 가능
- **Null Safety** - Kotlin의 null 안전성으로 NPE 방지

## Installation

KFC는 JitPack을 통해 배포됩니다. 아래 설정을 추가하여 사용할 수 있습니다.

### Gradle (Kotlin DSL)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.kairos-code-dev:kfc:v1.0.0")
}
```

### Gradle (Groovy)

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
dependencies {
    implementation 'com.github.kairos-code-dev:kfc:v1.0.0'
}
```

### Maven

```xml
<!-- pom.xml -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.kairos-code-dev</groupId>
        <artifactId>kfc</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Version Tags

JitPack은 GitHub 릴리즈 태그를 사용합니다:

- Latest release: `v1.0.0`
- Specific commit: `commit-hash`
- Development snapshot: `main-SNAPSHOT`

릴리즈 목록은 [GitHub Releases](https://github.com/kairos-code-dev/kfc/releases)에서 확인할 수 있습니다.

## Documentation

전체 API 문서는 Dokka로 자동 생성되어 GitHub Pages에 호스팅됩니다:

- [API Reference](https://kairos-code-dev.github.io/kfc/) - 전체 API 문서
- [GitHub Repository](https://github.com/kairos-code-dev/kfc) - 소스 코드
- [Issue Tracker](https://github.com/kairos-code-dev/kfc/issues) - 버그 리포트 및 기능 요청

API 문서는 태그 푸시 시 자동으로 업데이트됩니다.

## Quick Start

### Basic Usage

```kotlin
import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.stock.Market
import java.time.LocalDate

suspend fun main() {
    // 클라이언트 생성
    val kfc = KfcClient.create()

    // 코스피 종목 리스트 조회
    val stocks = kfc.stock.getStockList(market = Market.KOSPI)
    println("코스피 종목 수: ${stocks.size}")

    // 삼성전자 기본정보 조회
    val samsung = kfc.stock.getStockInfo("005930")
    println("종목명: ${samsung?.name}, ISIN: ${samsung?.isin}")
}
```

### ETF Data

```kotlin
// ETF 목록 조회
val etfList = kfc.funds.getList(type = FundType.ETF)

// KODEX 200 상세정보
val kodex200 = kfc.funds.getDetailedInfo(isin = "KR7069500007")
println("NAV: ${kodex200?.nav}, 괴리율: ${kodex200?.divergenceRate}%")

// 포트폴리오 구성 종목
val portfolio = kfc.funds.getPortfolio(isin = "KR7069500007")
portfolio.take(5).forEach {
    println("${it.stockName}: ${it.weight}%")
}

// 공매도 데이터
val shortSelling = kfc.funds.getShortSelling(
    isin = "KR7069500007",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

### Index Data

```kotlin
// 코스피 지수 OHLCV
val kospiOhlcv = kfc.index.getOhlcvByDate(
    ticker = "1001",  // 코스피
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)

// 코스피 200 구성 종목
val constituents = kfc.index.getIndexConstituents(ticker = "1028")
println("코스피 200 구성 종목 수: ${constituents.size}")

// 지수 밸류에이션 (PER, PBR, 배당수익률)
val fundamentals = kfc.index.getFundamentalByDate(
    ticker = "1001",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

### Bond Yields

```kotlin
// 오늘의 채권 수익률 전체 조회
val bondSnapshot = kfc.bond.getBondYieldsByDate()
println("국고채 3년: ${bondSnapshot.treasury3Y?.yield}%")
println("회사채 AA-: ${bondSnapshot.corporateAA}%")

// 특정 채권 수익률 추이
val treasury10Y = kfc.bond.getBondYields(
    bondType = BondType.TREASURY_10Y,
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

### Financial Statements (OPENDART)

```kotlin
// OPENDART API Key가 필요합니다
val kfc = KfcClient.create(opendartApiKey = "YOUR_API_KEY")

// 삼성전자 손익계산서
val incomeStatement = kfc.financials?.getIncomeStatement(
    corpCode = "00126380",  // 삼성전자 OPENDART 고유번호
    year = 2024
)
println("매출액: ${incomeStatement?.revenue}")
println("영업이익: ${incomeStatement?.operatingProfit}")

// 전체 재무제표 한번에 조회
val financials = kfc.financials?.getAllFinancials(
    corpCode = "00126380",
    year = 2024
)
```

### Futures Data

```kotlin
// 선물 상품 목록
val futures = kfc.future.getFutureTickerList()
futures.forEach { println("${it.name}: ${it.productId}") }

// KOSPI 200 선물 OHLCV
val futureOhlcv = kfc.future.getOhlcvByTicker(
    date = LocalDate.now(),
    productId = "KRDRVFUEST"
)
```

## API Reference

### KfcClient

| Property | Description |
|----------|-------------|
| `stock` | 주식 종목 정보 (종목 리스트, 기본정보, 섹터 분류) |
| `funds` | 펀드/ETF 정보 (목록, 포트폴리오, NAV, 투자자 거래, 공매도) |
| `index` | 지수 정보 (목록, OHLCV, 밸류에이션, 구성 종목) |
| `bond` | 채권 수익률 (국고채, 회사채, CD 등) |
| `future` | 선물 정보 (상품 목록, OHLCV) |
| `price` | 가격 정보 (분단위 시세, 일별 시세) |
| `corp` | 기업 공시 (배당, 액면분할) - OPENDART API Key 필요 |
| `financials` | 재무제표 (손익계산서, 재무상태표, 현금흐름표) - OPENDART API Key 필요 |

### Rate Limiting

KFC는 KRX API의 Rate Limit(25 RPS)을 자동으로 준수합니다. 별도 설정 없이 안전하게 사용할 수 있습니다.

```kotlin
// 커스텀 Rate Limit 설정 (선택사항)
val kfc = KfcClient.create(
    rateLimitingSettings = RateLimitingSettings(
        krx = RateLimitConfig(capacity = 20, refillRate = 20),
        naver = RateLimitConfig(capacity = 50, refillRate = 50),
        opendart = RateLimitConfig(capacity = 50, refillRate = 50)
    )
)
```

## Requirements

- **JDK 21** or higher
- **Kotlin 2.0** or higher
- **Ktor Client** (transitive dependency)

## OPENDART API Key

재무제표 및 기업 공시 데이터를 사용하려면 OPENDART API Key가 필요합니다.

1. [OPENDART](https://opendart.fss.or.kr/)에 회원가입
2. 인증키 발급 (무료, 일 20,000건 제한)
3. KfcClient 생성 시 API Key 전달

```kotlin
val kfc = KfcClient.create(opendartApiKey = "YOUR_API_KEY")
```

## Comparison with pykrx

```python
# pykrx (Python)
from pykrx import stock

df = stock.get_market_ohlcv("20240101", "20241231", "005930")
```

```kotlin
// KFC (Kotlin)
val kfc = KfcClient.create()
val ohlcv = kfc.funds.getOhlcv(
    isin = "KR7005930003",
    fromDate = LocalDate.of(2024, 1, 1),
    toDate = LocalDate.of(2024, 12, 31)
)
```

**KFC의 장점:**
- 컴파일 타임 타입 체크
- IDE 자동완성 지원
- Null 안전성
- Coroutines로 비동기 처리
- 에러 타입이 명확함

## Contributing

기여를 환영합니다! Pull Request를 보내기 전에 다음을 확인해주세요:

1. 코드 스타일 준수 (ktlint)
2. 테스트 작성 및 통과
3. 문서 업데이트

## License

```
Copyright 2024 Kairos Code

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Acknowledgments

- [pykrx](https://github.com/sharebook-kr/pykrx) - Python KRX 데이터 라이브러리. KFC의 설계에 많은 영감을 주었습니다.
- [KRX 정보데이터시스템](https://data.krx.co.kr) - 한국거래소 공식 데이터 포털
- [OPENDART](https://opendart.fss.or.kr) - 금융감독원 전자공시시스템 API

---

**Made with in Korea**
