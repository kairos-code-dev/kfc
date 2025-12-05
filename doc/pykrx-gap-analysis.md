# pykrx vs kfc API Gap 분석

> 이 문서는 pykrx에서 제공하는 기능 중 kfc에 아직 구현되지 않은 기능을 정리합니다.

## 1. 요약

| 카테고리 | pykrx 제공 | kfc 구현 상태 | 우선순위 |
|---------|-----------|-------------|---------|
| 주식 OHLCV | O | X | 높음 |
| 시가총액 | O | X | 높음 |
| 밸류에이션 (PER/PBR/DIV) | O | X | 높음 |
| 투자자별 거래 (주식) | O | X | 중간 |
| 지수(Index) | O | X | 중간 |
| 공매도 (주식) | O | X | 중간 |
| ETN/ELW 티커 목록 | O | △ (FundsApi) | 낮음 |
| 선물 | O | X | 낮음 |
| 채권 | O | X | 낮음 |

---

## 2. 상세 Gap 분석

### 2.1 주식 OHLCV (우선순위: 높음)

kfc는 현재 ETF/펀드 중심의 가격 데이터만 제공합니다. 일반 주식의 OHLCV 데이터가 필요합니다.

#### pykrx 함수

```python
# 특정 종목의 기간별 OHLCV
get_market_ohlcv_by_date(fromdate, todate, ticker, freq='d', adjusted=True)
# 반환: DataFrame[날짜, 시가, 고가, 저가, 종가, 거래량]

# 특정 일자의 전종목 OHLCV
get_market_ohlcv_by_ticker(date, market='KOSPI', alternative=False)
# 반환: DataFrame[티커, 시가, 고가, 저가, 종가, 거래량, 거래대금, 등락률]
```

#### 사용 예시

```python
# 삼성전자 2021년 1월 OHLCV
df = get_market_ohlcv_by_date("20210101", "20210131", "005930")

# 2021년 1월 22일 KOSPI 전종목 OHLCV
df = get_market_ohlcv_by_ticker("20210122", market="KOSPI")
```

#### 반환 데이터 (기간별)

| 날짜 | 시가 | 고가 | 저가 | 종가 | 거래량 |
|------|------|------|------|------|--------|
| 2021-01-18 | 86600 | 87300 | 84100 | 85000 | 43227951 |
| 2021-01-19 | 84500 | 88000 | 83600 | 87000 | 39895044 |

#### kfc 구현 제안

```kotlin
interface StockPriceApi {
    // 특정 종목의 기간별 OHLCV
    suspend fun getOhlcvByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        freq: Frequency = Frequency.DAILY,
        adjusted: Boolean = true
    ): List<StockOhlcv>

    // 특정 일자의 전종목 OHLCV
    suspend fun getOhlcvByTicker(
        date: LocalDate,
        market: Market = Market.ALL
    ): List<StockOhlcvSnapshot>
}
```

---

### 2.2 시가총액 (우선순위: 높음)

#### pykrx 함수

```python
# 특정 종목의 기간별 시가총액
get_market_cap_by_date(fromdate, todate, ticker, freq='d')
# 반환: DataFrame[날짜, 시가총액, 거래량, 거래대금, 상장주식수]

# 특정 일자의 전종목 시가총액
get_market_cap_by_ticker(date, market='ALL', ascending=False)
# 반환: DataFrame[티커, 종가, 시가총액, 거래량, 거래대금, 상장주식수]
```

#### 사용 예시

```python
# 삼성전자 2015년 7월 시가총액
df = get_market_cap_by_date("20150720", "20150724", "005930")

# 2021년 1월 4일 전종목 시가총액 (시가총액 순 정렬)
df = get_market_cap_by_ticker("20210104")
```

#### 반환 데이터

| 날짜 | 시가총액 | 거래량 | 거래대금 | 상장주식수 |
|------|----------|--------|----------|------------|
| 2015-07-20 | 187806654675000 | 128928 | 165366199000 | 147299337 |

#### kfc 구현 제안

```kotlin
interface MarketCapApi {
    suspend fun getMarketCapByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<MarketCap>

    suspend fun getMarketCapByTicker(
        date: LocalDate,
        market: Market = Market.ALL
    ): List<MarketCapSnapshot>
}
```

---

### 2.3 밸류에이션 지표 (PER/PBR/DIV) (우선순위: 높음)

#### pykrx 함수

```python
# 특정 종목의 기간별 밸류에이션
get_market_fundamental_by_date(fromdate, todate, ticker, freq='d')
# 반환: DataFrame[날짜, BPS, PER, PBR, EPS, DIV, DPS]

# 특정 일자의 전종목 밸류에이션
get_market_fundamental_by_ticker(date, market='KOSPI')
# 반환: DataFrame[티커, BPS, PER, PBR, EPS, DIV, DPS]
```

#### 사용 예시

```python
# 삼성전자 2021년 1월 밸류에이션
df = get_market_fundamental_by_date("20210104", "20210108", "005930")

# 2021년 1월 4일 KOSPI 전종목 밸류에이션
df = get_market_fundamental_by_ticker("20210104")
```

#### 반환 데이터

| 날짜 | BPS | PER | PBR | EPS | DIV | DPS |
|------|-----|-----|-----|-----|-----|-----|
| 2021-01-04 | 37528 | 26.22 | 2.21 | 3166 | 1.71 | 1416 |

#### kfc 구현 제안

```kotlin
data class Fundamental(
    val date: LocalDate,
    val bps: BigDecimal,      // 주당순자산가치
    val per: BigDecimal,      // 주가수익비율
    val pbr: BigDecimal,      // 주가순자산비율
    val eps: BigDecimal,      // 주당순이익
    val div: BigDecimal,      // 배당수익률
    val dps: BigDecimal       // 주당배당금
)

interface FundamentalApi {
    suspend fun getFundamentalByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<Fundamental>

    suspend fun getFundamentalByTicker(
        date: LocalDate,
        market: Market = Market.KOSPI
    ): List<FundamentalSnapshot>
}
```

---

### 2.4 투자자별 거래 - 주식 (우선순위: 중간)

kfc의 FundsApi에는 ETF 투자자별 거래가 있으나, 일반 주식의 투자자별 거래는 미구현입니다.

#### pykrx 함수

```python
# 투자자별 거래대금 (기간 합계)
get_market_trading_value_by_investor(fromdate, todate, ticker, etf=False, etn=False, elw=False)
# 반환: DataFrame[투자자구분, 매도, 매수, 순매수]

# 투자자별 거래량 (기간 합계)
get_market_trading_volume_by_investor(fromdate, todate, ticker, etf=False, etn=False, elw=False)
# 반환: DataFrame[투자자구분, 매도, 매수, 순매수]

# 투자자별 거래대금 (일별 추이)
get_market_trading_value_by_date(fromdate, todate, ticker, on='순매수', detail=False)
# 반환: DataFrame[날짜, 기관합계, 기타법인, 개인, 외국인합계, 전체]

# 순매수 상위 종목
get_market_net_purchases_of_equities_by_ticker(fromdate, todate, market, investor)
# 반환: DataFrame[티커, 종목명, 매도거래량, 매수거래량, 순매수거래량, 매도거래대금, 매수거래대금, 순매수거래대금]
```

#### 사용 예시

```python
# 삼성전자 투자자별 거래대금 (기간 합계)
df = get_market_trading_value_by_investor("20210115", "20210122", "005930")

# KOSPI 시장 전체 투자자별 거래대금
df = get_market_trading_value_by_investor("20210115", "20210122", "KOSPI")

# 개인 순매수 상위 종목
df = get_market_net_purchases_of_equities_by_ticker("20210115", "20210122", "KOSPI", "개인")
```

#### 반환 데이터 (기간 합계)

| 투자자구분 | 매도 | 매수 | 순매수 |
|-----------|------|------|--------|
| 금융투자 | 2580964135000 | 2309054317700 | -271909817300 |
| 보험 | 153322228800 | 44505136200 | -108817092600 |
| 개인 | ... | ... | ... |
| 외국인 | ... | ... | ... |

#### kfc 구현 제안

```kotlin
interface InvestorTradingApi {
    // 종목별 투자자별 거래 (기간 합계)
    suspend fun getInvestorTradingByPeriod(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingSummary>

    // 시장별 투자자별 거래 (일별 추이)
    suspend fun getInvestorTradingByDate(
        ticker: String,  // KOSPI, KOSDAQ, 또는 개별 종목코드
        fromDate: LocalDate,
        toDate: LocalDate,
        tradeType: TradeType = TradeType.NET_PURCHASE
    ): List<InvestorTradingDaily>

    // 순매수 상위 종목
    suspend fun getTopNetPurchases(
        market: Market,
        investor: InvestorType,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<NetPurchaseRanking>
}
```

---

### 2.5 지수(Index) 데이터 (우선순위: 중간)

KOSPI, KOSDAQ, KRX 등 주요 지수 데이터입니다.

#### pykrx 함수

```python
# 지수 티커 목록
get_index_ticker_list(date=None, market='KOSPI')
# 반환: ['1001', '1002', '1003', ...]

# 지수명 조회
get_index_ticker_name(ticker)
# 반환: '코스피'

# 지수 구성 종목
get_index_portfolio_deposit_file(ticker, date=None, alternative=False)
# 반환: ['005930', '000660', ...]  (구성 종목 티커 리스트)

# 지수 OHLCV (기간별)
get_index_ohlcv_by_date(fromdate, todate, ticker, freq='d')
# 반환: DataFrame[날짜, 시가, 고가, 저가, 종가, 거래량, 거래대금]

# 지수 OHLCV (전종목)
get_index_ohlcv_by_ticker(date, market='KOSPI')
# 반환: DataFrame[지수명, 시가, 고가, 저가, 종가, 거래량, 거래대금]

# 지수 밸류에이션 (기간별)
get_index_fundamental_by_date(fromdate, todate, ticker)
# 반환: DataFrame[날짜, 종가, 등락률, PER, 선행PER, PBR, 배당수익률]

# 지수 밸류에이션 (전종목)
get_index_fundamental_by_ticker(date, market='KOSPI')
# 반환: DataFrame[지수명, 종가, 등락률, PER, 선행PER, PBR, 배당수익률]

# 지수 등락률
get_index_price_change_by_ticker(fromdate, todate, market='KOSPI')
# 반환: DataFrame[지수명, 시가, 종가, 등락률, 거래량, 거래대금]

# 지수 기본정보 (상장일 등)
get_index_listing_date(계열구분='KOSPI')
# 반환: DataFrame[지수명, 기준시점, 발표시점, 기준지수, 종목수]
```

#### 사용 예시

```python
# KOSPI 지수 티커 목록
tickers = get_index_ticker_list(market="KOSPI")

# 코스피 지수 (1001) 2021년 1월 OHLCV
df = get_index_ohlcv_by_date("20210101", "20210130", "1001")

# 코스피200 구성 종목
constituents = get_index_portfolio_deposit_file("1028")
```

#### 반환 데이터 (지수 OHLCV)

| 날짜 | 시가 | 고가 | 저가 | 종가 | 거래량 | 거래대금 |
|------|------|------|------|------|--------|----------|
| 2021-01-04 | 2874.50 | 2946.54 | 2869.11 | 2944.45 | 1026510465 | 25011393960858 |

#### kfc 구현 제안

```kotlin
interface IndexApi {
    // 지수 티커 목록
    suspend fun getIndexTickerList(
        market: IndexMarket = IndexMarket.KOSPI
    ): List<String>

    // 지수명 조회
    suspend fun getIndexName(ticker: String): String?

    // 지수 구성 종목
    suspend fun getIndexConstituents(
        ticker: String,
        date: LocalDate = LocalDate.now()
    ): List<String>

    // 지수 OHLCV
    suspend fun getIndexOhlcv(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<IndexOhlcv>

    // 지수 밸류에이션
    suspend fun getIndexFundamental(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<IndexFundamental>
}
```

---

### 2.6 공매도 - 주식 (우선순위: 중간)

kfc의 FundsApi에는 ETF 공매도가 있으나, 일반 주식의 공매도는 미구현입니다.

#### pykrx 함수

```python
# 공매도 거래량/대금 (일별)
get_shorting_volume_by_date(fromdate, todate, ticker)
get_shorting_value_by_date(fromdate, todate, ticker)
# 반환: DataFrame[날짜, 공매도, 매수, 비중]

# 공매도 거래량/대금 (티커별)
get_shorting_volume_by_ticker(date, market='KOSPI', include=['주식'])
get_shorting_value_by_ticker(date, market='KOSPI', include=['주식'])
# 반환: DataFrame[티커, 공매도, 매수, 비중]

# 공매도 잔고 (일별)
get_shorting_balance_by_date(fromdate, todate, ticker)
# 반환: DataFrame[날짜, 공매도잔고, 상장주식수, 공매도금액, 시가총액, 비중]

# 공매도 잔고 (티커별)
get_shorting_balance_by_ticker(date, market='KOSPI')
# 반환: DataFrame[티커, 공매도잔고, 상장주식수, 공매도금액, 시가총액, 비중]

# 투자자별 공매도
get_shorting_investor_volume_by_date(fromdate, todate, market='KOSPI')
get_shorting_investor_value_by_date(fromdate, todate, market='KOSPI')
# 반환: DataFrame[날짜, 기관, 개인, 외국인, 기타, 합계]

# 공매도 상위 50종목
get_shorting_volume_top50(date, market='KOSPI')
get_shorting_balance_top50(date, market='KOSPI')
# 반환: DataFrame[티커, 순위, 공매도거래대금, 총거래대금, 공매도비중, ...]

# 공매도 상태 (거래량/잔고수량/거래대금/잔고금액)
get_shorting_status_by_date(fromdate, todate, ticker)
# 반환: DataFrame[날짜, 거래량, 잔고수량, 거래대금, 잔고금액]
```

#### 사용 예시

```python
# 삼성전자 공매도 거래량
df = get_shorting_volume_by_date("20210104", "20210108", "005930")

# KOSPI 공매도 잔고 상위 50
df = get_shorting_balance_top50("20210127", market="KOSPI")
```

#### 반환 데이터

| 날짜 | 공매도 | 매수 | 비중 |
|------|--------|------|------|
| 2021-01-04 | 9279 | 38655276 | 0.02 |

#### kfc 구현 제안

```kotlin
interface ShortSellingApi {
    // 공매도 거래 (일별)
    suspend fun getShortTradingByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortTrading>

    // 공매도 거래 (티커별)
    suspend fun getShortTradingByTicker(
        date: LocalDate,
        market: Market = Market.KOSPI
    ): List<ShortTradingSnapshot>

    // 공매도 잔고 (일별)
    suspend fun getShortBalanceByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortBalance>

    // 공매도 잔고 (티커별)
    suspend fun getShortBalanceByTicker(
        date: LocalDate,
        market: Market = Market.KOSPI
    ): List<ShortBalanceSnapshot>

    // 공매도 상위 종목
    suspend fun getShortTop50(
        date: LocalDate,
        market: Market = Market.KOSPI,
        type: ShortRankingType = ShortRankingType.VOLUME
    ): List<ShortRanking>
}
```

---

### 2.7 외국인 한도소진률 (우선순위: 낮음)

#### pykrx 함수

```python
# 특정 종목의 기간별 외국인 한도소진률
get_exhaustion_rates_of_foreign_investment_by_date(fromdate, todate, ticker)
# 반환: DataFrame[날짜, 상장주식수, 보유수량, 지분율, 한도수량, 한도소진률]

# 특정 일자의 전종목 외국인 한도소진률
get_exhaustion_rates_of_foreign_investment_by_ticker(date, market='KOSPI', balance_limit=False)
# 반환: DataFrame[티커, 상장주식수, 보유수량, 지분율, 한도수량, 한도소진률]
```

#### 반환 데이터

| 날짜 | 상장주식수 | 보유수량 | 지분율 | 한도수량 | 한도소진률 |
|------|------------|----------|--------|----------|------------|
| 2021-01-08 | 5969782550 | 3314966371 | 55.53 | 5969782550 | 55.53 |

---

### 2.8 기간별 등락률 (우선순위: 낮음)

#### pykrx 함수

```python
# 특정 기간 전종목 등락률
get_market_price_change_by_ticker(fromdate, todate, market='KOSPI', adjusted=True, delist=False)
# 반환: DataFrame[티커, 종목명, 시가, 종가, 변동폭, 등락률, 거래량, 거래대금]
```

#### 반환 데이터

| 티커 | 종목명 | 시가 | 종가 | 변동폭 | 등락률 | 거래량 | 거래대금 |
|------|--------|------|------|--------|--------|--------|----------|
| 095570 | AJ네트웍스 | 4615 | 4540 | -75 | -1.63 | 3004291 | 14398725745 |

---

### 2.9 ETN/ELW 티커 목록 (우선순위: 낮음)

kfc FundsApi의 getList(FundType.ETN)으로 부분 지원됨.

#### pykrx 함수

```python
# ETN 티커 목록
get_etn_ticker_list(date=None)
# 반환: ['550001', '550002', '500001', ...]

# ELW 티커 목록
get_elw_ticker_list(date=None)
# 반환: ['58F194', '58F195', ...]

# ETX 통합 티커 목록
get_etx_ticker_list(market, date=None)  # market: ETF/ETN/ELW/ALL
```

---

### 2.10 선물 (우선순위: 낮음)

#### pykrx 함수

```python
# 선물 티커 목록
get_future_ticker_list()
# 반환: ['KRDRVFUK2I', 'KRDRVFUMKI', ...]

# 선물명 조회
get_future_ticker_name(ticker)
# 반환: 'EURO STOXX50 Futures'

# 선물 OHLCV (티커별)
get_future_ohlcv_by_ticker(date, prod, alternative=False, prev=True)
# 반환: DataFrame[티커, 시가, 고가, 저가, 종가, 거래량, 거래대금, 등락률]
```

---

### 2.11 채권 (우선순위: 낮음)

#### pykrx 함수

```python
# 장외 채권 수익률 (특정일)
get_otc_treasury_yields(date)
# 반환: DataFrame[채권종류, 수익률, 대비]

# 장외 채권 수익률 (기간별)
get_otc_treasury_yields(startDd, endDd, bndKindTpCd)
# 반환: DataFrame[일자, 수익률, 대비]
```

#### 채권 종류

- 국고채 1/2/3/5/10/20/30년
- 국민주택 1종 5년
- 회사채 AA-/BBB- (무보증 3년)
- CD (91일)

#### 반환 데이터

| 채권종류 | 수익률 | 대비 |
|----------|--------|------|
| 국고채 1년 | 1.467 | 0.015 |
| 국고채 2년 | 1.995 | 0.026 |
| 국고채 10년 | 2.619 | 0.053 |

---

### 2.12 영업일 유틸리티 (우선순위: 낮음)

#### pykrx 함수

```python
# 인접 영업일 조회
get_nearest_business_day_in_a_week(date=None, prev=True)
# 반환: 'YYYYMMDD'

# 과거 영업일 목록 조회
get_previous_business_days(year=2020, month=10)
get_previous_business_days(fromdate='20200101', todate='20200115')
# 반환: [Timestamp('2020-01-02'), Timestamp('2020-01-03'), ...]
```

---

## 3. 구현 우선순위 권장

### Phase 1: 핵심 주식 데이터 (높음)

1. **주식 OHLCV** - 가격 데이터는 모든 분석의 기초
2. **시가총액** - 스크리닝, 인덱스 계산에 필수
3. **밸류에이션 (PER/PBR/DIV)** - 가치 평가의 핵심 지표

### Phase 2: 시장 분석 (중간)

4. **투자자별 거래 (주식)** - 시장 심리 분석
5. **지수 데이터** - 벤치마크 분석
6. **공매도 (주식)** - 수급 분석

### Phase 3: 확장 (낮음)

7. 외국인 한도소진률
8. 기간별 등락률
9. 선물/채권
10. 영업일 유틸리티

---

## 4. 참고 사항

### 4.1 pykrx 데이터 소스

- **KRX**: 대부분의 시장 데이터
- **Naver 증권**: 수정주가 OHLCV (adjusted=True)

### 4.2 kfc 현재 구현 상태

| API | 설명 |
|-----|------|
| StockApi | 종목 목록, 기본정보, 업종분류 |
| FundsApi | ETF 목록, 상세정보, 포트폴리오, 투자자별 거래, 공매도 |
| PriceApi | ETF 분단위/일별 시세 |
| FinancialsApi | 재무제표 (손익계산서, 재무상태표, 현금흐름표) |
| CorpApi | 배당/증자감자 정보, 공시 검색 |

### 4.3 구현 시 고려사항

- **Rate Limiting**: KRX API 호출 제한 준수 (현재 GlobalRateLimiter 구현됨)
- **수정주가**: Naver 데이터 소스 통합 필요 (adjusted OHLCV)
- **시장 구분**: KOSPI/KOSDAQ/KONEX/ALL 지원
- **날짜 처리**: 휴일 시 이전/이후 영업일 자동 조회 옵션 (alternative 파라미터)
