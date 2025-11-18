# API 설계

## 개요

본 문서는 kotlin-krx 라이브러리의 Public API 설계 원칙과 사용 패턴을 제공합니다. Kotlin 관용구를 활용하여 타입 안전하고 직관적인 API를 설계하며, 백테스팅 중심 사용 사례에 최적화합니다.

---

## 설계 원칙

### 1. Kotlin 관용적 (Idiomatic)

**명명 규칙**:
- 함수명: camelCase
- 클래스명: PascalCase
- 상수: UPPER_SNAKE_CASE
- Boolean 프로퍼티: `isXxx`, `hasXxx`

**Null 안전성**:
- Nullable 타입 명시적 표현 (`Type?`)
- Safe call 연산자 (`?.`) 활용
- Elvis 연산자 (`?:`)로 기본값 제공

**확장 함수**:
- 데이터 변환은 확장 함수로 구현
- 유틸리티 메서드는 확장 함수 우선

---

### 2. 타입 안전성

**String 대신 구체적 타입 사용**:
```kotlin
// ❌ 나쁜 예
fun getData(date: String): List<Map<String, Any>>

// ✅ 좋은 예
fun getData(date: LocalDate): List<Ohlcv>
```

**Enum으로 상수 관리**:
```kotlin
enum class AssetClass {
    STOCK,    // 주식
    BOND,     // 채권
    COMMODITY, // 상품
    DERIVATIVE // 파생
}

enum class MarketClassification {
    DOMESTIC, // 국내
    FOREIGN   // 해외
}
```

---

### 3. 명확한 의도 표현

**함수명으로 동작 명시**:
```kotlin
// 조회
fun getEtfInfo(ticker: String)
fun getHistoricalOhlcv(ticker: String)

// 검색
fun searchEtfsByName(keyword: String)

// 계산
fun calculateReturns(prices: List<BigDecimal>)

// 필터링
fun filterLowFeeEtfs(etfs: List<EtfInfo>)
```

**파라미터 순서**:
1. 필수 파라미터 (티커, 날짜)
2. 선택적 파라미터 (기본값 제공)
3. 설정 파라미터 (마지막)

---

### 4. 불변성 (Immutability)

**데이터 클래스는 불변**:
```kotlin
data class Ohlcv(
    val date: LocalDate,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)
```

**컬렉션은 읽기 전용**:
```kotlin
fun getAllEtfs(): List<EtfBasicInfo>  // List, not MutableList
```

---

## 서비스 레이어 API

### EtfComprehensiveService (핵심)

```kotlin
@Service
class EtfComprehensiveService {

    /**
     * ETF 종합 정보 조회
     *
     * @param ticker 6자리 티커 (예: "152100")
     * @param date 거래일 (기본값: 오늘)
     * @return ComprehensiveEtfInfo 또는 null (데이터 없음)
     */
    fun getComprehensiveInfo(
        ticker: String,
        date: LocalDate = LocalDate.now()
    ): ComprehensiveEtfInfo?

    /**
     * 여러 ETF의 종합 정보 동시 조회
     *
     * @param tickers 티커 목록
     * @param date 거래일
     * @return Map<티커, ComprehensiveEtfInfo>
     */
    fun getComprehensiveInfoMultiple(
        tickers: List<String>,
        date: LocalDate = LocalDate.now()
    ): Map<String, ComprehensiveEtfInfo>

    /**
     * 저보수 ETF 필터링 (0.3% 이하)
     */
    fun getLowFeeEtfs(
        tickers: List<String>,
        date: LocalDate = LocalDate.now(),
        maxFee: BigDecimal = BigDecimal("0.3")
    ): List<ComprehensiveEtfInfo>

    /**
     * 52주 고가 근처 ETF (95% 이상)
     */
    fun getEtfsNear52WeekHigh(
        tickers: List<String>,
        date: LocalDate = LocalDate.now(),
        threshold: BigDecimal = BigDecimal("0.95")
    ): List<ComprehensiveEtfInfo>

    /**
     * 52주 저가 근처 ETF (105% 이하)
     */
    fun getEtfsNear52WeekLow(
        tickers: List<String>,
        date: LocalDate = LocalDate.now(),
        threshold: BigDecimal = BigDecimal("1.05")
    ): List<ComprehensiveEtfInfo>

    /**
     * 과도한 괴리율 ETF (1% 이상)
     */
    fun getEtfsWithExcessiveDivergence(
        tickers: List<String>,
        date: LocalDate = LocalDate.now(),
        threshold: BigDecimal = BigDecimal("1.0")
    ): List<ComprehensiveEtfInfo>
}
```

---

### EtfService (OHLCV 및 기본 데이터)

```kotlin
@Service
class EtfService {

    /**
     * ETF 기본 정보 조회 (전체 목록)
     */
    fun getAllBasicInfo(): List<EtfBasicInfo>

    /**
     * 개별 ETF 기본 정보 조회
     */
    fun getBasicInfo(ticker: String): EtfBasicInfo?

    /**
     * 특정 날짜의 OHLCV 데이터 조회
     *
     * @param ticker 티커
     * @param from 시작일
     * @param to 종료일 (기본값: from과 동일)
     * @param adjusted true면 조정주가 (네이버), false면 원본 (KRX)
     * @return OHLCV 리스트 (날짜 오름차순)
     */
    fun getOhlcv(
        ticker: String,
        from: LocalDate,
        to: LocalDate = from,
        adjusted: Boolean = false
    ): List<Ohlcv>

    /**
     * 포트폴리오 구성 조회 (PDF)
     */
    fun getPortfolio(
        ticker: String,
        date: LocalDate = LocalDate.now()
    ): List<Constituent>

    /**
     * 추적 오차율 조회
     */
    fun getTrackingError(
        ticker: String,
        from: LocalDate,
        to: LocalDate = LocalDate.now()
    ): List<TrackingError>

    /**
     * 괴리율 조회
     */
    fun getDivergence(
        ticker: String,
        from: LocalDate,
        to: LocalDate = LocalDate.now()
    ): List<Divergence>
}
```

---

### HistoricalDataService (백테스팅 중심)

```kotlin
@Service
class HistoricalDataService {

    /**
     * 과거 OHLCV 데이터 조회
     *
     * @param ticker 티커
     * @param from 시작일 (null = 상장일)
     * @param to 종료일 (null = 최근 거래일)
     * @param adjusted true면 조정주가 (네이버), false면 원본 (KRX)
     * @return OHLCV 리스트
     */
    fun getHistoricalOhlcv(
        ticker: String,
        from: LocalDate? = null,
        to: LocalDate? = null,
        adjusted: Boolean = false
    ): List<Ohlcv>

    /**
     * 과거 NAV 데이터 조회
     */
    fun getHistoricalNav(
        ticker: String,
        from: LocalDate? = null,
        to: LocalDate? = null
    ): List<NavData>

    /**
     * 과거 괴리율 데이터 조회
     */
    fun getHistoricalDivergence(
        ticker: String,
        from: LocalDate? = null,
        to: LocalDate? = null
    ): List<Divergence>

    /**
     * 과거 투자자별 거래 데이터 조회
     */
    fun getHistoricalInvestorTrading(
        ticker: String,
        from: LocalDate? = null,
        to: LocalDate? = null,
        queryType: InvestorQueryType = InvestorQueryType.NET_BUY_VALUE
    ): List<InvestorTrading>
}

enum class InvestorQueryType {
    NET_BUY_VALUE,    // 순매수 거래대금
    NET_BUY_VOLUME,   // 순매수 거래량
    BUY_VALUE,        // 매수 거래대금
    SELL_VALUE        // 매도 거래대금
}
```

---

### InvestorService (투자자 거래 분석)

```kotlin
@Service
class InvestorService {

    /**
     * 전체 ETF 투자자별 거래 (기간 합계)
     */
    fun getAggregatedTrading(
        from: LocalDate,
        to: LocalDate = LocalDate.now()
    ): List<InvestorTrading>

    /**
     * 전체 ETF 투자자별 거래 (일별 추이)
     */
    fun getDailyTrading(
        from: LocalDate,
        to: LocalDate = LocalDate.now(),
        queryType: InvestorQueryType = InvestorQueryType.NET_BUY_VALUE
    ): List<DailyInvestorTrading>

    /**
     * 개별 ETF 투자자별 거래 (기간 합계)
     */
    fun getIndividualAggregatedTrading(
        ticker: String,
        from: LocalDate,
        to: LocalDate = LocalDate.now()
    ): List<InvestorTrading>

    /**
     * 개별 ETF 투자자별 거래 (일별 추이)
     */
    fun getIndividualDailyTrading(
        ticker: String,
        from: LocalDate,
        to: LocalDate = LocalDate.now(),
        queryType: InvestorQueryType = InvestorQueryType.NET_BUY_VALUE
    ): List<DailyInvestorTrading>
}
```

---

### ShortSellingService (공매도 데이터)

```kotlin
@Service
class ShortSellingService {

    /**
     * 개별 종목 공매도 거래 조회
     *
     * @param ticker 종목 티커 또는 ISIN
     * @param from 시작일
     * @param to 종료일
     * @return 공매도 거래 데이터 리스트
     */
    fun getShortSellingTransactions(
        ticker: String,
        from: LocalDate,
        to: LocalDate = LocalDate.now()
    ): List<ShortSellingTransaction>

    /**
     * 개별 종목 공매도 종합정보 (잔고 포함) 조회
     *
     * @param ticker 종목 티커 또는 ISIN
     * @param from 시작일
     * @param to 종료일
     * @return 공매도 종합 데이터 리스트
     */
    fun getShortSellingBalance(
        ticker: String,
        from: LocalDate,
        to: LocalDate = LocalDate.now()
    ): List<ShortSellingBalance>

    /**
     * 높은 공매도 비중 종목 필터링
     *
     * @param tickers 종목 리스트
     * @param date 기준일
     * @param minRatio 최소 공매도 비중 (%)
     * @return 높은 공매도 비중 종목 리스트
     */
    fun getHighShortSellingRatioStocks(
        tickers: List<String>,
        date: LocalDate = LocalDate.now(),
        minRatio: BigDecimal = BigDecimal("10.0")
    ): List<ShortSellingTransaction>

    /**
     * 공매도 잔고 증가 종목 필터링
     *
     * @param tickers 종목 리스트
     * @param date 기준일
     * @param minBalanceRatio 최소 잔고 비율 (%)
     * @return 높은 잔고 비율 종목 리스트
     */
    fun getHighBalanceRatioStocks(
        tickers: List<String>,
        date: LocalDate = LocalDate.now(),
        minBalanceRatio: BigDecimal = BigDecimal("1.0")
    ): List<ShortSellingBalance>
}
```

---

## 일반적인 사용 시나리오

### 시나리오 1: ETF 상세 정보 조회

```kotlin
// 단일 ETF 종합 정보
val kodex200 = comprehensiveService.getComprehensiveInfo("069500")

kodex200?.let { etf ->
    println("이름: ${etf.name}")
    println("종가: ${etf.closePrice}")
    println("NAV: ${etf.nav}")
    println("시가총액: ${etf.marketCap}")
    println("총 보수: ${etf.totalFee}%")
    println("52주 고가: ${etf.week52High}")
    println("52주 저가: ${etf.week52Low}")
    println("자산군: ${etf.assetClass}")
}
```

---

### 시나리오 2: 백테스팅용 과거 OHLCV 조회

```kotlin
// 상장일부터 현재까지 (원본 가격)
val allData = historicalService.getHistoricalOhlcv(
    ticker = "152100",
    from = null,  // 상장일 자동 감지
    to = null,    // 최근 거래일 자동 설정
    adjusted = false
)

// 조정주가로 조회 (배당/분할 반영)
val adjustedData = historicalService.getHistoricalOhlcv(
    ticker = "152100",
    from = LocalDate.now().minusYears(1),
    to = LocalDate.now(),
    adjusted = true  // 네이버에서 조정주가 가져옴
)

// 특정 기간 (원본)
val lastYear = historicalService.getHistoricalOhlcv(
    ticker = "152100",
    from = LocalDate.now().minusYears(1),
    to = LocalDate.now()
)

// 수익률 계산
val prices = lastYear.map { it.close }
val returns = BacktestUtils.calculateReturns(prices)
val mdd = BacktestUtils.calculateMaxDrawdown(prices)
val sharpe = BacktestUtils.calculateSharpeRatio(returns, BigDecimal("0.02"))

println("연간 수익률: ${returns.reduce { acc, r -> acc + r } * BigDecimal("100")}%")
println("최대 낙폭: ${mdd * BigDecimal("100")}%")
println("샤프 비율: $sharpe")
```

---

### 시나리오 3: 저보수 ETF 스크리닝

```kotlin
// 전체 ETF 목록 조회
val allTickers = etfService.getAllBasicInfo().map { it.ticker }

// 저보수 필터링 (0.3% 이하)
val lowFeeEtfs = comprehensiveService.getLowFeeEtfs(
    tickers = allTickers,
    maxFee = BigDecimal("0.3")
)

// 보수 순으로 정렬
lowFeeEtfs.sortedBy { it.totalFee }.forEach { etf ->
    println("${etf.name}: ${etf.totalFee}%")
}
```

---

### 시나리오 4: 52주 고가/저가 분석

```kotlin
// 52주 고가 근처 ETF
val nearHigh = comprehensiveService.getEtfsNear52WeekHigh(
    tickers = listOf("069500", "102110", "114800"),
    threshold = BigDecimal("0.95")  // 95% 이상
)

nearHigh.forEach { etf ->
    val percentage = etf.closePrice
        .divide(etf.week52High, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal("100"))
    println("${etf.name}: 52주 고가 대비 ${percentage.setScale(1, RoundingMode.HALF_UP)}%")
}

// 52주 저가 근처 ETF (매수 기회)
val nearLow = comprehensiveService.getEtfsNear52WeekLow(
    tickers = listOf("069500", "102110", "114800"),
    threshold = BigDecimal("1.05")  // 105% 이하
)
```

---

### 시나리오 5: 포트폴리오 구성 조회

```kotlin
// ETF 구성 종목
val portfolio = etfService.getPortfolio("069500")  // KODEX 200

// 상위 10개 종목
portfolio.sortedByDescending { it.weight }
    .take(10)
    .forEach { constituent ->
        println("${constituent.name}: ${constituent.weight}%")
    }

// 특정 종목 보유 여부 확인
val hasSamsung = portfolio.any { it.name.contains("삼성전자") }
```

---

### 시나리오 6: 투자자별 거래 추이 분석

```kotlin
// 최근 1개월 기관 vs 개인 순매수
val trading = investorService.getDailyTrading(
    from = LocalDate.now().minusMonths(1),
    to = LocalDate.now(),
    queryType = InvestorQueryType.NET_BUY_VALUE
)

trading.forEach { daily ->
    println("${daily.date}: 기관 ${daily.institutional}, 개인 ${daily.retail}")
}

// 개별 ETF 기관 순매수 추이
val individualTrading = investorService.getIndividualDailyTrading(
    ticker = "069500",
    from = LocalDate.now().minusMonths(3)
)
```

---

### 시나리오 7: 공매도 데이터 분석

```kotlin
// 개별 종목 공매도 거래 추이
val shortSellingTxns = shortSellingService.getShortSellingTransactions(
    ticker = "005930",  // 삼성전자
    from = LocalDate.now().minusMonths(1),
    to = LocalDate.now()
)

shortSellingTxns.forEach { txn ->
    println("${txn.date}: 공매도 비중 ${txn.volumeRatio}%")
}

// 공매도 잔고 추이
val shortSellingBalance = shortSellingService.getShortSellingBalance(
    ticker = "005930",
    from = LocalDate.now().minusMonths(3),
    to = LocalDate.now()
)

shortSellingBalance.forEach { balance ->
    println("${balance.date}: 잔고 ${balance.balanceQuantity}주 (${balance.balanceRatio}%)")
}

// 높은 공매도 비중 종목 찾기
val highShortStocks = shortSellingService.getHighShortSellingRatioStocks(
    tickers = listOf("005930", "000660", "035720"),  // 삼성전자, SK하이닉스, 카카오
    minRatio = BigDecimal("15.0")  // 15% 이상
)

highShortStocks.forEach { stock ->
    println("${stock.name}: 공매도 비중 ${stock.volumeRatio}%")
}

// 높은 잔고 비율 종목
val highBalanceStocks = shortSellingService.getHighBalanceRatioStocks(
    tickers = listOf("005930", "000660", "035720"),
    minBalanceRatio = BigDecimal("2.0")  // 2% 이상
)

highBalanceStocks.forEach { stock ->
    println("${stock.name}: 잔고 비율 ${stock.balanceRatio}%")
}
```

---

### 시나리오 8: 괴리율 모니터링

```kotlin
// 과도한 괴리율 ETF 찾기
val highDivergence = comprehensiveService.getEtfsWithExcessiveDivergence(
    tickers = allTickers,
    threshold = BigDecimal("1.0")  // 1% 이상
)

highDivergence.forEach { etf ->
    val direction = if (etf.divergenceRate > BigDecimal.ZERO) "프리미엄" else "디스카운트"
    println("${etf.name}: ${direction} ${etf.divergenceRate.abs().setScale(2, RoundingMode.HALF_UP)}%")
}

// 과거 괴리율 추이
val divergenceHistory = etfService.getDivergence(
    ticker = "069500",
    from = LocalDate.now().minusMonths(1)
)
```

---

## 빌더 패턴 (복잡한 쿼리)

### EtfQueryBuilder

```kotlin
class EtfQueryBuilder {
    private var ticker: String? = null
    private var fromDate: LocalDate? = null
    private var toDate: LocalDate = LocalDate.now()
    private var includeWeekends: Boolean = false
    private var includeHolidays: Boolean = false

    fun ticker(ticker: String) = apply { this.ticker = ticker }

    fun from(date: LocalDate?) = apply { this.fromDate = date }

    fun to(date: LocalDate) = apply { this.toDate = date }

    fun includeWeekends(include: Boolean = true) = apply {
        this.includeWeekends = include
    }

    fun includeHolidays(include: Boolean = true) = apply {
        this.includeHolidays = include
    }

    fun build(): EtfQuery {
        requireNotNull(ticker) { "ticker must be specified" }
        return EtfQuery(
            ticker = ticker!!,
            fromDate = fromDate,
            toDate = toDate,
            includeWeekends = includeWeekends,
            includeHolidays = includeHolidays
        )
    }
}

data class EtfQuery(
    val ticker: String,
    val fromDate: LocalDate?,
    val toDate: LocalDate,
    val includeWeekends: Boolean,
    val includeHolidays: Boolean
)

// 사용 예제
val query = EtfQueryBuilder()
    .ticker("069500")
    .from(LocalDate.of(2020, 1, 1))
    .to(LocalDate.now())
    .includeWeekends(false)
    .build()

val data = historicalService.execute(query)
```

---

## DSL 스타일 API (고급)

### ETF 쿼리 DSL

```kotlin
fun etfQuery(block: EtfQueryBuilder.() -> Unit): EtfQuery {
    return EtfQueryBuilder().apply(block).build()
}

// 사용 예제
val data = historicalService.execute(
    etfQuery {
        ticker("069500")
        from(LocalDate.of(2020, 1, 1))
        to(LocalDate.now())
        includeWeekends(false)
    }
)
```

---

### 백테스트 DSL

```kotlin
class BacktestBuilder {
    private var ticker: String? = null
    private var startDate: LocalDate? = null
    private var endDate: LocalDate = LocalDate.now()
    private var initialCapital: BigDecimal = BigDecimal("10000000")
    private var rebalancing: RebalancingStrategy = RebalancingStrategy.NONE

    fun ticker(ticker: String) = apply { this.ticker = ticker }
    fun start(date: LocalDate?) = apply { this.startDate = date }
    fun end(date: LocalDate) = apply { this.endDate = date }
    fun capital(amount: BigDecimal) = apply { this.initialCapital = amount }
    fun rebalance(strategy: RebalancingStrategy) = apply {
        this.rebalancing = strategy
    }

    fun execute(): BacktestResult {
        // 백테스트 로직
        return BacktestResult(/* ... */)
    }
}

enum class RebalancingStrategy {
    NONE,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

fun backtest(block: BacktestBuilder.() -> Unit): BacktestResult {
    return BacktestBuilder().apply(block).execute()
}

// 사용 예제
val result = backtest {
    ticker("069500")
    start(null)  // 상장일부터
    end(LocalDate.now())
    capital(BigDecimal("10000000"))
    rebalance(RebalancingStrategy.MONTHLY)
}

println("최종 자산: ${result.finalCapital}")
println("수익률: ${result.totalReturn}%")
println("MDD: ${result.maxDrawdown}%")
```

---

## 오류 처리 전략

### 커스텀 예외

```kotlin
// 기본 예외
sealed class KrxException(message: String) : RuntimeException(message)

// 구체적 예외
class TickerNotFoundException(ticker: String) :
    KrxException("Ticker not found: $ticker")

class InvalidDateRangeException(from: LocalDate, to: LocalDate) :
    KrxException("Invalid date range: $from to $to")

class KrxApiException(message: String, cause: Throwable? = null) :
    KrxException(message)

class RateLimitExceededException(retryAfter: Int) :
    KrxException("Rate limit exceeded. Retry after $retryAfter seconds")
```

---

### 오류 처리 예제

```kotlin
try {
    val info = comprehensiveService.getComprehensiveInfo("INVALID")
} catch (e: TickerNotFoundException) {
    logger.error("Ticker not found", e)
    // 사용자에게 유효한 티커 목록 제공
} catch (e: KrxApiException) {
    logger.error("KRX API error", e)
    // 재시도 로직 또는 캐시된 데이터 사용
}
```

---

### Retry 로직

```kotlin
@Retry(
    maxAttempts = 3,
    backoff = Backoff(delay = 1000, multiplier = 2.0)
)
fun getDataWithRetry(ticker: String): ComprehensiveEtfInfo? {
    return comprehensiveService.getComprehensiveInfo(ticker)
}
```

---

## Rate Limiting 전략

### 요청 간 지연

```kotlin
class KrxClient(
    private val requestDelayMs: Long = 1000L
) {
    private var lastRequestTime = 0L

    fun post(bld: String, params: Map<String, String>): Map<String, Any?> {
        // 이전 요청 이후 경과 시간 계산
        val elapsed = System.currentTimeMillis() - lastRequestTime

        if (elapsed < requestDelayMs) {
            Thread.sleep(requestDelayMs - elapsed)
        }

        val response = executeRequest(bld, params)
        lastRequestTime = System.currentTimeMillis()

        return response
    }
}
```

---

### 설정 가능한 지연

```kotlin
@ConfigurationProperties("krx.api")
data class KrxApiConfig(
    val requestDelayMs: Long = 1000L,
    val maxRetries: Int = 3,
    val connectionTimeoutMs: Long = 10000L,
    val readTimeoutMs: Long = 30000L
)
```

---

## pykrx API 스타일과의 비교

### pykrx (Python)

```python
from pykrx import etx

# 기본 정보
info = etx.get_etf_ticker_list()

# OHLCV
ohlcv = etx.get_etf_ohlcv_by_date("20240101", "20240131", "069500")

# 포트폴리오
portfolio = etx.get_etf_portfolio_deposit_file("20240102", "069500")
```

---

### kotlin-krx (Kotlin) - 개선 사항

```kotlin
// 타입 안전성
val info: List<EtfBasicInfo> = etfService.getAllBasicInfo()

// LocalDate 사용 (String 대신)
val ohlcv: List<Ohlcv> = etfService.getOhlcv(
    ticker = "069500",
    from = LocalDate.of(2024, 1, 1),
    to = LocalDate.of(2024, 1, 31)
)

// 명명된 파라미터
val portfolio: List<Constituent> = etfService.getPortfolio(
    ticker = "069500",
    date = LocalDate.of(2024, 1, 2)
)

// 기본값 제공
val today = etfService.getPortfolio("069500")  // date = LocalDate.now()
```

---

## 성능 최적화 팁

### 1. 배치 조회 사용

```kotlin
// ❌ 나쁜 예 (N개 요청)
val infos = tickers.map { ticker ->
    comprehensiveService.getComprehensiveInfo(ticker)
}

// ✅ 좋은 예 (1개 요청 또는 최소화)
val infos = comprehensiveService.getComprehensiveInfoMultiple(tickers)
```

---

### 2. 캐싱 활용

```kotlin
@Cacheable("etf-basic-info")
fun getAllBasicInfo(): List<EtfBasicInfo> {
    // 기본 정보는 자주 변하지 않으므로 캐싱 적합
    return client.post(/* ... */)
}
```

---

### 3. 비동기 처리 (향후)

```kotlin
// 코루틴 지원 (향후 구현)
suspend fun getComprehensiveInfoAsync(
    ticker: String
): ComprehensiveEtfInfo? = coroutineScope {
    withContext(Dispatchers.IO) {
        getComprehensiveInfo(ticker)
    }
}
```

---

## 요약

### 핵심 API

| 서비스 | 주요 메서드 | 사용 사례 |
|--------|------------|----------|
| **EtfComprehensiveService** | `getComprehensiveInfo()` | ETF 상세 페이지 |
| **HistoricalDataService** | `getHistoricalOhlcv()` | 백테스팅 |
| **EtfService** | `getOhlcv()`, `getPortfolio()` | 일반 데이터 조회 |
| **InvestorService** | `getDailyTrading()` | 투자자 흐름 분석 |
| **ShortSellingService** | `getShortSellingTransactions()`, `getShortSellingBalance()` | 공매도 분석 |

### 개선 사항 (pykrx 대비)

1. **타입 안전성**: String → 구체적 타입
2. **Null 안전성**: Kotlin null 안전성 활용
3. **기본값 제공**: 일반적 사용 사례 간소화
4. **명명된 파라미터**: 가독성 향상
5. **확장 함수**: 유틸리티 메서드 추가
6. **빌더/DSL**: 복잡한 쿼리 간소화

---

## 참조

- 01-프로젝트-개요.md: 아키텍처 개요
- 03-MDCSTAT04701-상세명세.md: ComprehensiveEtfInfo API
- 05-구현-로드맵.md: 구현 계획
- 07-테스트-전략.md: API 테스트 전략
