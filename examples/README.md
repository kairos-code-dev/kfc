# KFC 라이브러리 예제

KFC (Korea Financial Client) 라이브러리의 사용 예제 모음입니다.

## 개요

이 디렉토리에는 KFC 라이브러리의 주요 API를 활용하는 실행 가능한 예제 코드가 포함되어 있습니다.
각 예제는 독립적으로 실행 가능하며, 실제 API를 호출하여 데이터를 조회합니다.

## 예제 목록

### 1. StockExample.kt - 주식 종목 정보 조회

주식 종목 관련 데이터를 조회하는 방법을 보여줍니다.

**주요 기능:**
- 코스피/코스닥 종목 리스트 조회
- 개별 종목 기본정보 조회
- 종목명으로 검색
- 업종분류 현황 조회
- 산업별 그룹화 데이터 조회

**실행 방법:**
```bash
# Kotlin 스크립트로 실행
kotlin examples/StockExample.kt

# 또는 Gradle로 실행
./gradlew runStockExample
```

**예제 코드 하이라이트:**
```kotlin
val kfc = KfcClient.create()

// 코스피 종목 리스트 조회
val stocks = kfc.stock.getStockList(market = Market.KOSPI)

// 삼성전자 정보 조회
val samsungInfo = kfc.stock.getStockInfo("005930")

// 종목 검색
val results = kfc.stock.searchStocks(keyword = "삼성")
```

---

### 2. EtfExample.kt - ETF 데이터 조회

ETF 관련 상세 데이터를 조회하는 방법을 보여줍니다.

**주요 기능:**
- ETF 목록 조회
- ETF 상세정보 조회 (NAV, 괴리율, 52주 고가/저가 등)
- ETF 포트폴리오 구성 종목 조회
- 추적 오차 및 괴리율 추이 조회
- 공매도 거래/잔고 조회
- 투자자별 거래 현황 조회

**실행 방법:**
```bash
kotlin examples/EtfExample.kt
```

**예제 코드 하이라이트:**
```kotlin
val kfc = KfcClient.create()

// ETF 목록 조회
val etfs = kfc.funds.getList(type = FundType.ETF)

// KODEX 200 상세정보 조회
val detailedInfo = kfc.funds.getDetailedInfo(isin = "KR7069500007")

// 포트폴리오 구성 조회
val portfolio = kfc.funds.getPortfolio(isin = "KR7069500007")

// 공매도 데이터 조회
val shortSelling = kfc.funds.getShortSelling(
    isin = "KR7069500007",
    fromDate = LocalDate.now().minusDays(30),
    toDate = LocalDate.now()
)
```

---

### 3. IndexExample.kt - 지수 데이터 조회

한국 증시 지수 관련 데이터를 조회하는 방법을 보여줍니다.

**주요 기능:**
- 지수 목록 조회
- 지수 OHLCV 조회 (시가, 고가, 저가, 종가, 거래량)
- 지수 밸류에이션 조회 (PER, PBR, 배당수익률)
- 지수 구성 종목 조회
- 지수 등락률 조회
- 여러 지수 비교 분석

**실행 방법:**
```bash
kotlin examples/IndexExample.kt
```

**예제 코드 하이라이트:**
```kotlin
val kfc = KfcClient.create()

// 지수 목록 조회
val indices = kfc.index.getIndexList(market = IndexMarket.ALL)

// 코스피 지수 OHLCV 조회
val ohlcv = kfc.index.getOhlcvByDate(
    ticker = "1001", // 코스피
    fromDate = LocalDate.now().minusDays(30),
    toDate = LocalDate.now()
)

// 코스피 200 밸류에이션 조회
val fundamental = kfc.index.getFundamentalByDate(
    ticker = "1028", // 코스피 200
    fromDate = LocalDate.now().minusDays(30),
    toDate = LocalDate.now()
)

// 코스피 200 구성 종목 조회
val constituents = kfc.index.getIndexConstituents("1028")
```

---

### 4. BondExample.kt - 채권 수익률 조회

장외 채권시장의 수익률 데이터를 조회하는 방법을 보여줍니다.

**주요 기능:**
- 특정 일자 전체 채권 수익률 조회
- 특정 채권의 기간별 수익률 추이 조회
- 수익률 곡선(Yield Curve) 분석
- 국고채-회사채 스프레드 분석
- CD 금리와 국고채 비교

**실행 방법:**
```bash
kotlin examples/BondExample.kt
```

**예제 코드 하이라이트:**
```kotlin
val kfc = KfcClient.create()

// 오늘의 전체 채권 수익률 조회
val todayYields = kfc.bond.getBondYieldsByDate()

// 국고채 10년물 수익률 추이 조회
val treasury10Y = kfc.bond.getBondYields(
    bondType = BondType.TREASURY_10Y,
    fromDate = LocalDate.now().minusDays(90),
    toDate = LocalDate.now()
)

// 회사채 스프레드 계산
val aaSpread = todayYields.corporateAA.yield - todayYields.treasury3Y.yield
val bbbSpread = todayYields.corporateBBB.yield - todayYields.treasury3Y.yield
```

---

### 5. FinancialsExample.kt - 재무제표 조회

OPENDART API를 활용한 재무제표 및 기업 공시 데이터 조회 예제입니다.

**주요 기능:**
- OPENDART 고유번호 조회
- 손익계산서 조회
- 재무상태표 조회
- 현금흐름표 조회
- 전체 재무제표 한 번에 조회
- 배당 정보 조회
- 공시 검색
- 여러 기업 재무제표 비교

**사전 요구사항:**
- OPENDART API Key 필요 (https://opendart.fss.or.kr/)
- 환경변수 `OPENDART_API_KEY` 설정 필요

**실행 방법:**
```bash
# 환경변수 설정
export OPENDART_API_KEY='your-api-key-here'

# 예제 실행
kotlin examples/FinancialsExample.kt
```

**예제 코드 하이라이트:**
```kotlin
// API Key를 포함한 클라이언트 생성
val kfc = KfcClient.create(opendartApiKey = apiKey)

// 고유번호 조회
val corpCodeList = kfc.corp?.getCorpCodeList()
val samsungCorp = corpCodeList?.find { it.stockCode == "005930" }

// 손익계산서 조회
val incomeStatement = kfc.financials?.getIncomeStatement(
    corpCode = samsungCorp.corpCode,
    year = 2023,
    reportType = ReportType.ANNUAL,
    statementType = StatementType.CONSOLIDATED
)

// 전체 재무제표 한 번에 조회
val allFinancials = kfc.financials?.getAllFinancials(
    corpCode = samsungCorp.corpCode,
    year = 2023
)

// 배당 정보 조회
val dividendInfo = kfc.corp?.getDividendInfo(
    corpCode = samsungCorp.corpCode,
    year = 2023
)
```

---

## 공통 사용 패턴

### 1. KfcClient 생성

```kotlin
// 기본 생성 (KRX API만 사용)
val kfc = KfcClient.create()

// OPENDART API 포함
val kfcWithOpendart = KfcClient.create(
    opendartApiKey = "YOUR_API_KEY"
)

// Rate Limiting 설정 커스터마이징
val kfcCustom = KfcClient.create(
    rateLimitingSettings = RateLimitingSettings(
        krx = RateLimitConfig(capacity = 25, refillRate = 25),
        naver = RateLimitConfig(capacity = 50, refillRate = 50),
        opendart = RateLimitConfig(capacity = 50, refillRate = 50)
    )
)
```

### 2. Coroutine 사용

모든 API는 suspend 함수로 구현되어 있으므로 코루틴 스코프 내에서 호출해야 합니다.

```kotlin
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val kfc = KfcClient.create()
    val stocks = kfc.stock.getStockList()
    // ...
}
```

### 3. 예외 처리

```kotlin
import dev.kairoscode.kfc.domain.exception.KfcException

try {
    val stocks = kfc.stock.getStockList()
} catch (e: KfcException) {
    println("API 호출 실패: ${e.message}")
}
```

## API Rate Limiting

KFC 라이브러리는 자동으로 Rate Limiting을 관리합니다:

- **KRX API**: 초당 25 요청 (기본값)
- **Naver API**: 초당 50 요청 (기본값)
- **OPENDART API**: 초당 50 요청 (기본값), 일일 20,000건 제한

동일 JVM 프로세스 내의 모든 KfcClient 인스턴스가 Rate Limiter를 공유하므로
여러 클라이언트를 생성해도 안전합니다.

## 주의사항

1. **실제 API 호출**: 모든 예제는 실제 API를 호출하므로 인터넷 연결이 필요합니다.

2. **OPENDART API**: FinancialsExample 실행 시 유효한 API Key가 필요합니다.

3. **데이터 가용성**: 일부 API는 최근 거래일 데이터만 제공하므로,
   주말이나 공휴일에는 일부 예제가 빈 데이터를 반환할 수 있습니다.

4. **Rate Limiting**: 너무 많은 요청을 짧은 시간에 보내면 자동으로 속도 제한됩니다.

5. **2015년 이후 데이터**: OPENDART 재무제표는 2015년 이후 데이터만 지원합니다.

## 참고 문서

- [KFC 라이브러리 README](../README.md)
- [API 문서](../doc/)
- [OPENDART API 가이드](https://opendart.fss.or.kr/guide/main.do)
- [KRX 정보데이터시스템](http://data.krx.co.kr/)

## 라이선스

이 예제 코드는 KFC 라이브러리와 동일한 Apache License 2.0으로 배포됩니다.
