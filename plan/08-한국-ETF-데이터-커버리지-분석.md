# 한국 ETF 데이터 커버리지 분석

## 개요

본 문서는 kotlin-krx 라이브러리가 제공 가능한 한국 ETF 데이터의 범위를 분석하고, 일반적인 ETF 데이터베이스 스키마(16개 테이블)와 비교하여 커버리지를 평가합니다. EODHD API(미국 ETF), EODHD API(한국 ETF - 대부분 누락), pykrx_client(한국 ETF) 데이터와 비교하여 kotlin-krx의 강점과 한계를 명확히 제시합니다.

**문서 작성일**: 2025-11-18
**대상 독자**: 백엔드 개발자, 데이터 엔지니어, 프로덕트 매니저

---

## 요약 (Executive Summary)

### kotlin-krx 데이터 제공 능력

| 지표 | 값 | 상세 |
|------|-----|------|
| 전체 필드 커버리지 | **30%** (56/188) | 핵심 ETF 운용 데이터에 집중 |
| 핵심 강점 분야 | **투자자 거래, 공매도, 추적 성과** | KRX 공식 데이터 기반 |
| 치명적 누락 사항 | **밸류에이션, 채권 상세, 배당** | KRX API 미제공 |
| pykrx 대비 우위 | **MDCSTAT04701, 공매도** | pykrx 미구현 엔드포인트 |

### 핵심 강점

1. **NAV 및 추적 성과 데이터**: 추적오차율, 괴리율, NAV 변동률 등 ETF 품질 평가에 필수적인 지표 제공
2. **투자자별 거래 데이터**: 기관/개인/외국인 순매수 데이터로 시장 심리 분석 가능 (커버리지 86%)
3. **공매도 데이터**: pykrx가 제공하지 않는 ETF 공매도 거래 및 잔고 정보 (커버리지 100%)
4. **종합 ETF 정보**: MDCSTAT04701을 통한 단일 요청 30개 이상 필드 제공 (pykrx 미지원)
5. **포트폴리오 구성**: PDF(Portfolio Deposit File)를 통한 실시간 구성 종목 및 비중 데이터

### 치명적 누락 사항

1. **밸류에이션 메트릭**: P/E, P/B, P/S, dividend_yield 등 (KRX API 미제공)
2. **채권 ETF 상세 데이터**: 듀레이션, 만기, 신용등급 등 (KRX API 미제공)
3. **배당 및 분배금 이력**: 배당 지급 날짜, 금액 등 (OPENDART API 통합 필요)
4. **외국인 보유 현황**: ETF 보유 비율 (KRX는 개별 주식만 제공)
5. **자산/섹터/지역 배분**: 직접 제공 없음 (holdings 데이터 기반 2차 계산 필요)

### pykrx_client 대비 커버리지 비교

| 데이터 카테고리 | pykrx_client | kotlin-krx | 차이점 |
|----------------|-------------|-----------|--------|
| 기본 정보 | ✅ 완전 제공 | ✅ 완전 제공 | MDCSTAT04701로 더 풍부 |
| 가격 데이터 (OHLCV) | ✅ 완전 제공 | ✅ 완전 제공 | 동일 |
| NAV 및 추적 성과 | ✅ 완전 제공 | ✅ 완전 제공 | 동일 |
| 포트폴리오 구성 | ✅ 완전 제공 | ✅ 완전 제공 | 동일 |
| 투자자별 거래 | ✅ 완전 제공 | ✅ 완전 제공 | 동일 |
| 공매도 데이터 | ❌ 미제공 | ✅ **제공** | **kotlin-krx 우위** |
| 종합 정보 (04701) | ❌ 미구현 | ✅ **구현** | **kotlin-krx 우위** |

---

## 1. 테이블별 상세 분석

### A. etf_master (ETF 마스터 정보)

**테이블 목적**: ETF 기본 메타데이터 및 정적 정보

**kotlin-krx 제공 가능 여부**: 🟡 부분 제공 (33% - 6/18)

#### 제공 가능한 필드

| 필드명 | 데이터 소스 | 예시 값 | 비고 |
|--------|------------|---------|------|
| ticker | MDCSTAT04601 `ISU_SRT_CD` | "152100" | 6자리 티커 |
| name | MDCSTAT04601 `ISU_ABBRV` | "ARIRANG 200" | 약식 이름 |
| full_name | MDCSTAT04701 `ISU_NM` | "ARIRANG 200 증권상장지수투자신탁[주식]" | 전체 공식 이름 |
| asset_class | MDCSTAT04701 `IDX_ASST_CLSS_NM` | "주식" | 주식/채권/상품/파생 |
| benchmark_index | MDCSTAT04701 `ETF_OBJ_IDX_NM` | "코스피 200 지수" | 벤치마크 지수명 |
| listing_date | MDCSTAT04601 `LIST_DD` | "2008-02-01" | 상장일 |

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| country | KRX API 미제공 (한국 ETF는 항상 "KR") |
| exchange | KRX API 미제공 (KOSPI/KOSDAQ 구분은 MKT_NM으로 부분 가능) |
| currency | KRX API 미제공 (한국 ETF는 항상 "KRW") |
| isin | MDCSTAT04601로 제공 가능하나 일반적으로 내부용 |
| inception_date | listing_date와 동일하므로 계산 가능 |
| fund_family | 운용사(asset_manager)로 대체 가능 |
| category | asset_class로 부분 대체 |
| focus | KRX API 미제공 (섹터/테마 정보 없음) |
| niche | KRX API 미제공 |
| strategy | 레버리지/인버스 여부만 제공 (`IDX_CALC_INST_NM2`) |
| weighting_scheme | KRX API 미제공 |
| selection_criteria | KRX API 미제공 |

#### 대안/해결책

- **exchange**: MDCSTAT04701의 `MKT_NM` 필드로 "KOSPI"/"KOSDAQ" 구분 가능
- **fund_family**: MDCSTAT04701의 `COM_ABBRV` (운용사) 활용
- **strategy**: MDCSTAT04601의 `IDX_CALC_INST_NM2`로 "레버리지"/"인버스"/"일반" 구분
- **나머지 필드**: 수동 큐레이션 또는 외부 소스(OPENDART) 필요

---

### B. etf_daily_prices (ETF 일별 가격 데이터)

**테이블 목적**: OHLCV 및 조정가, 기술적 지표

**kotlin-krx 제공 가능 여부**: 🟡 부분 제공 (42% - 19/45)

#### 제공 가능한 필드

| 필드명 | 데이터 소스 | 예시 값 | 비고 |
|--------|------------|---------|------|
| trade_date | MDCSTAT04501 `TRD_DD` | "2024-01-02" | 거래일 |
| open | MDCSTAT04501 `TDD_OPNPRC` | 42075 | 시가 (BigDecimal) |
| high | MDCSTAT04501 `TDD_HGPRC` | 43250 | 고가 |
| low | MDCSTAT04501 `TDD_LWPRC` | 41900 | 저가 |
| close | MDCSTAT04501 `TDD_CLSPRC` | 42965 | 종가 |
| volume | MDCSTAT04501 `ACC_TRDVOL` | 192061 | 거래량 |
| trading_value | MDCSTAT04501 `ACC_TRDVAL` | 8222510755 | 거래대금 (원) |
| nav | MDCSTAT04501 `LST_NAV` | 43079.14 | 순자산가치 |
| divergence_rate | MDCSTAT06001 `DIVRG_RT` | -0.27 | 괴리율 (%) |
| price_change | MDCSTAT04501 `CMPPREVDD_PRC` | 1080 | 전일 대비 |
| price_change_rate | MDCSTAT04501 `FLUC_RT` | 2.58 | 등락률 (%) |
| market_cap | MDCSTAT04501 `MKTCAP` | 850707000000 | 시가총액 |
| listed_shares | MDCSTAT04501 `LIST_SHRS` | 19800000 | 상장 주식 수 |
| index_value | MDCSTAT04501 `OBJ_STKPRC_IDX` | 421.35 | 기초 지수 값 |
| index_change_rate | MDCSTAT04501 `IDX_FLUC_RT` | 2.64 | 지수 변동률 |
| tracking_error | MDCSTAT05901 `TRACE_ERR_RT` | 0.05 | 추적오차율 (%) |
| nav_change_rate | MDCSTAT05901 `NAV_CHG_RT` | 2.61 | NAV 변동률 |
| week_52_high | MDCSTAT04701 `WK52_HGPR` | 45230 | 52주 최고가 |
| week_52_low | MDCSTAT04701 `WK52_LWPR` | 38125 | 52주 최저가 |

#### 제공 불가능한 필드 (기술적 지표)

| 필드명 | 사유 |
|--------|------|
| adjusted_close | KRX API 미제공 (Naver API 사용 가능) |
| ma_10, ma_20, ma_50, ma_200 | 기술적 지표 계산 엔진 필요 |
| ema_10, ema_20 | 기술적 지표 계산 엔진 필요 |
| rsi_14 | 기술적 지표 계산 엔진 필요 |
| macd, macd_signal, macd_histogram | 기술적 지표 계산 엔진 필요 |
| bb_upper, bb_middle, bb_lower | 기술적 지표 계산 엔진 필요 |
| atr_14 | 기술적 지표 계산 엔진 필요 |
| obv | 기술적 지표 계산 엔진 필요 |
| vwap | 기술적 지표 계산 엔진 필요 |
| 기타 22개 기술적 지표 | 모두 자체 계산 필요 |

#### 대안/해결책

- **adjusted_close**:
  - **pykrx는 `get_market_ohlcv(ticker, adjusted=True)`로 ETF 조정주가 제공 가능**
  - 네이버 증권 API 활용 (KRX API는 미제공)
  - kotlin-krx도 네이버 API 통합으로 구현 가능
  - 또는 OPENDART 배당 이력 기반 자체 계산
- **기술적 지표**: 자체 계산 엔진 구현 필요
  - TA-Lib (Technical Analysis Library) Kotlin 포팅
  - 또는 간단한 이동평균/RSI 등은 직접 구현
  - OHLCV 데이터는 완전 제공되므로 모든 지표 계산 가능

```kotlin
// 예시: 단순 이동평균 계산
fun calculateSMA(prices: List<BigDecimal>, period: Int): List<BigDecimal> {
    return prices.windowed(period) { window ->
        window.reduce(BigDecimal::add).divide(
            BigDecimal(period), 2, RoundingMode.HALF_UP
        )
    }
}
```

---

### C. etf_monthly_fundamentals (ETF 월별 펀더멘털)

**테이블 목적**: 포트폴리오 메트릭, 리스크 메트릭, 성과 메트릭, 밸류에이션

**kotlin-krx 제공 가능 여부**: 🟡 부분 제공 (61% - 17/28)

#### 제공 가능한 필드

**포트폴리오 메트릭** (3/3)
| 필드명 | 데이터 소스 | 예시 값 |
|--------|------------|---------|
| aum | MDCSTAT04501 `INVSTASST_NETASST_TOTAMT` | 852966972000 |
| shares_outstanding | MDCSTAT04501 `LIST_SHRS` | 19800000 |
| average_volume_30d | MDCSTAT04501 기반 30일 평균 계산 | 250000 |

**리스크 메트릭** (7/7)
| 필드명 | 데이터 소스 | 계산 방법 |
|--------|------------|----------|
| beta | OHLCV + 지수 데이터 | 회귀 분석 |
| volatility_30d | OHLCV 데이터 | 30일 표준편차 |
| sharpe_ratio | OHLCV 데이터 | (수익률 - 무위험이자율) / 변동성 |
| sortino_ratio | OHLCV 데이터 | (수익률 - 무위험이자율) / 하방 변동성 |
| max_drawdown | OHLCV 데이터 | 최대 낙폭 계산 |
| var_95 | OHLCV 데이터 | Value at Risk 계산 |
| cvar_95 | OHLCV 데이터 | Conditional VaR 계산 |

**성과 메트릭** (4/7)
| 필드명 | 데이터 소스 | 계산 방법 |
|--------|------------|----------|
| return_1m | MDCSTAT04401 | 1개월 등락률 |
| return_3m | MDCSTAT04401 | 3개월 등락률 |
| return_6m | MDCSTAT04401 | 6개월 등락률 |
| return_1y | MDCSTAT04401 | 1년 등락률 |

**트래킹 메트릭** (3/3)
| 필드명 | 데이터 소스 | 예시 값 |
|--------|------------|---------|
| tracking_error | MDCSTAT05901 `TRACE_ERR_RT` | 0.05 |
| tracking_difference | MDCSTAT05901 계산 | NAV수익률 - 지수수익률 |
| correlation | MDCSTAT05901 데이터 | NAV와 지수 상관계수 |

#### 제공 불가능한 필드

**밸류에이션 메트릭** (0/11)
| 필드명 | 사유 |
|--------|------|
| pe_ratio | KRX API 미제공 (구성 종목 P/E 데이터 없음) |
| pb_ratio | KRX API 미제공 |
| ps_ratio | KRX API 미제공 |
| dividend_yield | KRX API 미제공 (배당 데이터 없음) |
| earnings_yield | KRX API 미제공 |
| fcf_yield | KRX API 미제공 |
| roe | KRX API 미제공 |
| roa | KRX API 미제공 |
| debt_to_equity | KRX API 미제공 |
| current_ratio | KRX API 미제공 |
| price_to_cashflow | KRX API 미제공 |

**성과 메트릭 일부** (0/3)
| 필드명 | 사유 |
|--------|------|
| return_ytd | 계산 가능하나 MDCSTAT04401이 제공하지 않음 |
| return_3y | 계산 가능 |
| return_5y | 계산 가능 |

#### 대안/해결책

- **성과 메트릭**: MDCSTAT04501 OHLCV 데이터로 모든 기간 수익률 자체 계산 가능
- **리스크 메트릭**: OHLCV 데이터 완전 제공으로 모든 리스크 지표 계산 가능
- **밸류에이션 메트릭**: **구현 불가능** (KRX API가 구성 종목의 재무 데이터 미제공)
  - 대안 1: OPENDART API 통합하여 구성 종목별 재무제표 수집 후 가중평균 계산
  - 대안 2: 외부 데이터 소스(FnGuide, WISEfn 등) 유료 구독

```kotlin
// 예시: Sharpe Ratio 계산
fun calculateSharpeRatio(
    returns: List<BigDecimal>,
    riskFreeRate: BigDecimal = BigDecimal("0.03")
): BigDecimal {
    val avgReturn = returns.average()
    val stdDev = calculateStdDev(returns)
    return (avgReturn - riskFreeRate).divide(stdDev, 4, RoundingMode.HALF_UP)
}
```

---

### D. etf_holdings (ETF 구성 종목)

**테이블 목적**: 포트폴리오 구성 종목 및 비중

**kotlin-krx 제공 가능 여부**: 🟡 부분 제공 (13% - 3/23)

#### 제공 가능한 필드

| 필드명 | 데이터 소스 | 예시 값 | 비고 |
|--------|------------|---------|------|
| ticker | MDCSTAT05001 `COMPST_ISU_CD` | "005930" | 구성 종목 티커 |
| shares | MDCSTAT05001 `COMPST_ISU_CU1_SHRS` | 50.5 | CU당 주식 수 |
| weight | MDCSTAT05001 `COMPST_RTO` | 15.32 | 비중 (%) |

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| holding_name | KRX API 미제공 (종목명은 `COMPST_ISU_NM`으로 제공되나 불완전) |
| isin | KRX API는 혼합 형식 제공 (일부 ISIN, 일부 티커) |
| sedol | KRX API 미제공 |
| cusip | KRX API 미제공 |
| sector | KRX API 미제공 (종목 마스터 테이블 필요) |
| industry | KRX API 미제공 |
| country | KRX API 미제공 (국내 ETF는 대부분 "KR") |
| currency | KRX API 미제공 |
| market_value | MDCSTAT05001 `VALU_AMT` 제공하나 문서화 불충분 |
| price | KRX API 미제공 (별도 조회 필요) |
| asset_type | KRX API 미제공 |
| maturity_date | KRX API 미제공 (채권 ETF용) |
| coupon_rate | KRX API 미제공 |
| duration | KRX API 미제공 |
| yield_to_maturity | KRX API 미제공 |
| credit_rating | KRX API 미제공 |
| face_value | KRX API 미제공 |
| accrued_interest | KRX API 미제공 |
| contract_month | KRX API 미제공 (선물 ETF용) |
| notional_value | KRX API 미제공 |

#### 대안/해결책

1. **종목 마스터 테이블 구축 필요**
   - 별도로 KRX 주식 종목 정보 API 활용 (pykrx의 `stock.get_market_ticker_list()` 등)
   - ISIN, 종목명, 섹터, 산업 매핑 테이블 생성

```kotlin
// 예시: 종목 마스터 조인
data class EnrichedHolding(
    val ticker: String,
    val name: String,
    val sector: String,        // 외부 조인
    val industry: String,      // 외부 조인
    val weight: BigDecimal,
    val shares: BigDecimal
)

fun getEnrichedHoldings(etfTicker: String): List<EnrichedHolding> {
    val holdings = etfService.getHoldings(etfTicker)
    return holdings.map { holding ->
        val stockInfo = stockMasterService.getStockInfo(holding.ticker)
        EnrichedHolding(
            ticker = holding.ticker,
            name = stockInfo.name,
            sector = stockInfo.sector,
            industry = stockInfo.industry,
            weight = holding.weight,
            shares = holding.shares
        )
    }
}
```

2. **채권 ETF 상세 정보**: KRX API 미제공으로 **구현 불가능**

---

### E. etf_asset_allocation (자산 배분)

**테이블 목적**: 주식/채권/현금/기타 자산 배분 비율

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/10)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| stocks_percent | KRX API 직접 제공 안 함 |
| bonds_percent | KRX API 직접 제공 안 함 |
| cash_percent | KRX API 직접 제공 안 함 |
| other_percent | KRX API 직접 제공 안 함 |
| us_stocks_percent | KRX API 미제공 |
| non_us_stocks_percent | KRX API 미제공 |
| government_bonds_percent | KRX API 미제공 |
| corporate_bonds_percent | KRX API 미제공 |
| high_yield_bonds_percent | KRX API 미제공 |
| convertible_bonds_percent | KRX API 미제공 |

#### 대안/해결책

**2차 계산으로 구현 가능**:
1. MDCSTAT05001 (PDF)에서 구성 종목 목록 수집
2. 각 구성 종목의 자산 유형 판별 (주식/채권/현금 등)
3. 비중(`COMPST_RTO`)을 자산 유형별로 집계

```kotlin
data class AssetAllocation(
    val stocksPercent: BigDecimal,
    val bondsPercent: BigDecimal,
    val cashPercent: BigDecimal,
    val otherPercent: BigDecimal
)

fun calculateAssetAllocation(etfTicker: String): AssetAllocation {
    val holdings = etfService.getHoldings(etfTicker)

    var stocksWeight = BigDecimal.ZERO
    var bondsWeight = BigDecimal.ZERO
    var cashWeight = BigDecimal.ZERO

    holdings.forEach { holding ->
        val assetType = stockMasterService.getAssetType(holding.ticker)
        when (assetType) {
            "STOCK" -> stocksWeight += holding.weight
            "BOND" -> bondsWeight += holding.weight
            "CASH" -> cashWeight += holding.weight
        }
    }

    return AssetAllocation(
        stocksPercent = stocksWeight,
        bondsPercent = bondsWeight,
        cashPercent = cashWeight,
        otherPercent = BigDecimal("100") - stocksWeight - bondsWeight - cashWeight
    )
}
```

**제약사항**:
- 종목 마스터에 자산 유형 정보 필요
- 현금 비중은 추정 불가능 (holdings에 표시 안 됨)

---

### F. etf_sector_allocation (섹터 배분)

**테이블 목적**: GICS 섹터별 배분 비율

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/4)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| sector_name | KRX API 직접 제공 안 함 |
| sector_weight | KRX API 직접 제공 안 함 |
| sector_code | KRX API 미제공 |
| sector_level | KRX API 미제공 |

#### 대안/해결책

**2차 계산으로 구현 가능** (자산 배분과 동일한 방식):
1. MDCSTAT05001에서 구성 종목 수집
2. 종목 마스터에서 GICS 섹터 정보 조회
3. 비중을 섹터별로 집계

```kotlin
data class SectorAllocation(
    val sectorName: String,
    val sectorWeight: BigDecimal,
    val sectorCode: String
)

fun calculateSectorAllocation(etfTicker: String): List<SectorAllocation> {
    val holdings = etfService.getHoldings(etfTicker)

    return holdings
        .mapNotNull { holding ->
            stockMasterService.getSectorInfo(holding.ticker)?.let { sector ->
                sector to holding.weight
            }
        }
        .groupBy { it.first }
        .map { (sector, holdings) ->
            SectorAllocation(
                sectorName = sector.name,
                sectorWeight = holdings.sumOf { it.second },
                sectorCode = sector.code
            )
        }
        .sortedByDescending { it.sectorWeight }
}
```

**필수 요구사항**:
- 종목 마스터 테이블에 GICS 섹터 정보 필요
- 섹터 분류는 GICS, WICS, ICB 등 표준 체계 사용 권장

---

### G. etf_regional_allocation (지역 배분)

**테이블 목적**: 국가/지역별 배분 비율

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/4)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| region_name | KRX API 직접 제공 안 함 |
| region_weight | KRX API 직접 제공 안 함 |
| country_name | KRX API 미제공 |
| country_weight | KRX API 미제공 |

#### 대안/해결책

**2차 계산 가능** (단, 국내 ETF는 대부분 국내 자산):
1. MDCSTAT05001에서 구성 종목 수집
2. 종목 마스터에서 국가/지역 정보 조회
3. 비중을 지역별로 집계

```kotlin
data class RegionalAllocation(
    val regionName: String,
    val regionWeight: BigDecimal,
    val countryName: String?,
    val countryWeight: BigDecimal?
)

fun calculateRegionalAllocation(etfTicker: String): List<RegionalAllocation> {
    val holdings = etfService.getHoldings(etfTicker)

    return holdings
        .mapNotNull { holding ->
            stockMasterService.getCountryInfo(holding.ticker)?.let { country ->
                country to holding.weight
            }
        }
        .groupBy { it.first }
        .map { (country, holdings) ->
            RegionalAllocation(
                regionName = country.region,
                regionWeight = holdings.sumOf { it.second },
                countryName = country.name,
                countryWeight = holdings.sumOf { it.second }
            )
        }
}
```

**현실적 제약**:
- 한국 ETF는 대부분 국내 자산 (KR: 95-100%)
- 해외 ETF의 경우 구성 종목 정보가 불완전할 수 있음

---

### H. etf_investor_trading (투자자별 거래)

**테이블 목적**: 기관/개인/외국인 순매수 데이터

**kotlin-krx 제공 가능 여부**: ✅ 거의 완전 제공 (86% - 6/7)

#### 제공 가능한 필드

| 필드명 | 데이터 소스 | 예시 값 | 비고 |
|--------|------------|---------|------|
| trade_date | MDCSTAT04802 `TRD_DD` | "2024-01-02" | 거래일 |
| institutional_buy | MDCSTAT04802 `NUM_ITM_VAL21` | 1234567 | 기관 순매수 |
| corporate_buy | MDCSTAT04802 `NUM_ITM_VAL22` | -500000 | 기타법인 (부호 있음) |
| individual_buy | MDCSTAT04802 `NUM_ITM_VAL23` | -734567 | 개인 순매수 |
| foreign_buy | MDCSTAT04802 `NUM_ITM_VAL24` | 2000000 | 외국인 순매수 |
| total_volume | MDCSTAT04802 `NUM_ITM_VAL25` | 0 | 항상 0 (순매수 합계) |

**상세 투자자 유형** (MDCSTAT04801):
- 금융투자 (증권사)
- 보험
- 투신 (투자신탁)
- 사모 (사모펀드)
- 은행
- 기타금융
- 연기금 등
- 기관합계
- 기타법인
- 개인
- 외국인
- 기타외국인

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| institutional_ownership | KRX API 미제공 (보유 비율 정보 없음) |

#### 특징 및 장점

1. **실시간 시장 심리 분석 가능**: 기관/외국인 순매수 추이로 수급 파악
2. **일별 + 기간 집계 모두 제공**: MDCSTAT04802 (일별), MDCSTAT04801 (기간 합계)
3. **세부 투자자 유형 제공**: 증권사, 보험, 연기금 등 10개 이상 유형
4. **부호 있는 정수**: 순매도 시 음수로 표현

```kotlin
data class InvestorTrading(
    val date: LocalDate,
    val institutionalNet: Long,      // 기관 순매수 (부호 있음)
    val individualNet: Long,         // 개인 순매수
    val foreignNet: Long,            // 외국인 순매수
    val totalVolume: Long
)

fun getInvestorFlow(ticker: String, from: LocalDate, to: LocalDate): List<InvestorTrading> {
    return investorService.getDailyInvestorTrading(
        ticker = ticker,
        fromDate = from,
        toDate = to,
        queryType1 = "2",  // 거래량 기준
        queryType2 = "1"   // 순매수
    )
}
```

---

### I. etf_short_selling (공매도 데이터)

**테이블 목적**: 공매도 거래 및 잔고 정보

**kotlin-krx 제공 가능 여부**: ✅ 완전 제공 (100% - 5/5)

#### 제공 가능한 필드

| 필드명 | 데이터 소스 | 예시 값 | 비고 |
|--------|------------|---------|------|
| trade_date | MDCSTAT31401/31501 `TRD_DD` | "2024-01-02" | 거래일 |
| short_volume | MDCSTAT31401 `CVSRTSELL_TRDVOL` | 123456 | 공매도 거래량 |
| total_volume | MDCSTAT31401 `TRDVOL` | 1000000 | 전체 거래량 |
| short_value | MDCSTAT31401 `CVSRTSELL_TRDVAL` | 5300000000 | 공매도 거래대금 |
| short_balance | MDCSTAT31501 `VALU_PD_SALE_PSTK_LQTY` | 5600000 | 공매도 잔고 수량 |

**추가 제공 필드** (MDCSTAT31501):
- `STR_CONST_VAL1`: 상환 거래량 (공매도 포지션 청산)
- `CVSRTSELL_RPMNT_TRDVOL`: 공매도 순거래량 (공매도 - 상환)
- `VALU_PD_SALE_PSTK_RTO`: 잔고 비율 (잔고 / 상장주식수)

#### pykrx 대비 우위점

| 측면 | pykrx | kotlin-krx |
|------|-------|-----------|
| ETF 공매도 지원 | ❌ 미지원 (개별 주식만) | ✅ **지원** |
| 엔드포인트 | - | MDCSTAT31401, MDCSTAT31501 |
| 데이터 상세도 | - | 거래 + 잔고 모두 제공 |

```kotlin
data class ShortSellingInfo(
    val date: LocalDate,
    val shortVolume: Long,
    val totalVolume: Long,
    val shortValue: Long,
    val shortBalance: Long,
    val balanceRatio: BigDecimal,
    val repaymentVolume: Long
)

fun getShortSellingInfo(ticker: String, from: LocalDate, to: LocalDate): List<ShortSellingInfo> {
    val transactions = shortingService.getShortSellingTransactions(ticker, from, to)
    val balances = shortingService.getShortSellingBalance(ticker, from, to)

    return transactions.zip(balances) { tx, bal ->
        ShortSellingInfo(
            date = tx.date,
            shortVolume = tx.shortSellingVolume,
            totalVolume = tx.totalVolume,
            shortValue = tx.shortSellingValue,
            shortBalance = bal.balanceQuantity,
            balanceRatio = bal.balanceRatio,
            repaymentVolume = bal.repaymentVolume
        )
    }
}
```

**활용 사례**:
- 공매도 과열 종목 탐지 (잔고 비율 > 1% 등)
- 숏 커버링 모니터링 (상환 거래량 급증)
- 공매도 vs 가격 변동 상관관계 분석

---

### J. etf_foreign_ownership (외국인 보유 현황)

**테이블 목적**: 외국인 보유 비율 및 추이

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/5)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| trade_date | - |
| foreign_shares | KRX는 ETF 외국인 보유 미제공 |
| foreign_ownership_percent | KRX는 ETF 외국인 보유 미제공 |
| foreign_limit_percent | KRX는 ETF 외국인 보유 미제공 |
| foreign_limit_exhaustion | KRX는 ETF 외국인 보유 미제공 |

#### 대안/해결책

**구현 불가능**:
- KRX API는 **개별 주식**에 대해서만 외국인 보유 현황 제공 (`stock` 엔드포인트)
- ETF 외국인 보유 정보는 공식 통계 미공개
- 대안 없음 (금융감독원 등 다른 소스에서도 미제공)

---

### K. etf_bonds_static (채권 정적 정보)

**테이블 목적**: 채권 ETF의 듀레이션, 만기, 신용등급 등

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/6)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| average_duration | KRX API 미제공 |
| average_maturity | KRX API 미제공 |
| average_yield | KRX API 미제공 |
| average_coupon | KRX API 미제공 |
| credit_quality | KRX API 미제공 |
| effective_duration | KRX API 미제공 |

#### 대안/해결책

**구현 불가능**:
- KRX API는 채권 ETF 상세 정보 미제공
- holdings 데이터에도 채권 상세 정보 없음
- 외부 데이터 소스 필요 (KIS, 금투협 채권정보센터 등)

---

### L. etf_bonds_daily (채권 일별 데이터)

**테이블 목적**: 채권 ETF의 일별 수익률 및 스프레드

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/4)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| trade_date | - |
| sec_yield | KRX API 미제공 (채권 수익률 정보 없음) |
| distribution_yield | KRX API 미제공 |
| oas | KRX API 미제공 (Option-Adjusted Spread) |

---

### M. etf_bonds_monthly (채권 월별 데이터)

**테이블 목적**: 채권 ETF의 월별 통계

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/5)

---

### N. etf_bonds_breakdown (채권 상세 분류)

**테이블 목적**: 만기별, 신용등급별, 채권 유형별 분포

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/10)

---

### O. etf_dividends (배당금 데이터)

**테이블 목적**: 분배금 지급 이력

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/8)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| ex_date | KRX API 미제공 (배당락일 정보 없음) |
| payment_date | KRX API 미제공 |
| record_date | KRX API 미제공 |
| amount | KRX API 미제공 |
| dividend_type | KRX API 미제공 |
| currency | KRX API 미제공 |
| frequency | KRX API 미제공 |
| yield | KRX API 미제공 |

#### 대안/해결책

**OPENDART API 통합 필요**:
- 전자공시시스템(DART)에서 "배당 결정 공시" 수집
- API: `https://opendart.fss.or.kr/api/`
- 필요 정보: 분배금 지급 결정, 배당락일, 지급일, 금액

```kotlin
// OPENDART API 통합 예시
class OpendartService(private val apiKey: String) {

    fun getDividendHistory(corpCode: String): List<Dividend> {
        val url = "https://opendart.fss.or.kr/api/divid.json"
        val params = mapOf(
            "crtfc_key" to apiKey,
            "corp_code" to corpCode,
            "bgn_de" to "20200101"
        )
        // HTTP 호출 및 파싱
    }
}
```

---

### P. etf_corporate_actions (기업 활동)

**테이블 목적**: 액면분할, 병합, 상장폐지 등 이벤트

**kotlin-krx 제공 가능 여부**: ❌ 제공 불가 (0% - 0/6)

#### 제공 불가능한 필드

| 필드명 | 사유 |
|--------|------|
| action_date | KRX API 미제공 |
| action_type | KRX API 미제공 |
| ratio | KRX API 미제공 |
| description | KRX API 미제공 |
| impact_on_price | KRX API 미제공 |
| impact_on_shares | KRX API 미제공 |

#### 대안/해결책

**OPENDART API 통합 필요**:
- 전자공시시스템에서 "[기재정정]주요사항보고서", "[기타]수시공시" 등 수집
- 상장폐지는 KRX 공시에서 확인 가능

---

## 2. 카테고리별 커버리지 종합

### A. 가격 데이터 (etf_daily_prices)

**제공 가능**: ✅ OHLCV, NAV, volume, 시가총액, 52주 고가/저가
**누락**: ❌ 조정가, 기술적 지표 (MA, EMA, RSI, MACD, BB 등)
**대안**: 자체 계산 엔진 구현 필요

```kotlin
// 기술적 지표 계산 라이브러리 구현 예시
class TechnicalIndicators {
    fun sma(prices: List<BigDecimal>, period: Int): List<BigDecimal>
    fun ema(prices: List<BigDecimal>, period: Int): List<BigDecimal>
    fun rsi(prices: List<BigDecimal>, period: Int = 14): List<BigDecimal>
    fun macd(prices: List<BigDecimal>): List<MacdResult>
    fun bollingerBands(prices: List<BigDecimal>, period: Int, stdDev: Int): List<BBResult>
}
```

### B. 펀더멘털 데이터 (etf_monthly_fundamentals)

**제공 가능**: ✅ 포트폴리오 메트릭, 리스크 메트릭, 성과 메트릭, 트래킹 메트릭
**누락**: ❌ 밸류에이션 메트릭 (P/E, P/B, P/S, dividend_yield)
**대안**: 불가능 (KRX API 미제공, 외부 소스 필요)

### C. 포트폴리오 구성 (etf_holdings)

**제공 가능**: ✅ ticker, shares, weight
**누락**: ❌ holding_name, sector, industry, ISIN (완전한 정보)
**대안**: 별도 종목 마스터 테이블 구축 필요

### D. 자산/섹터/지역 배분

**제공 가능**: ❌ 모두 직접 제공 안 함
**대안**: holdings 데이터 + 종목 마스터 기반 2차 계산 가능

### E. 투자자별 거래 (etf_investor_trading)

**제공 가능**: ✅ 완전 제공 (86%)
**누락**: ❌ 외국인 보유 비율 (ownership)
**소스**: pykrx `stock.get_etf_trading_volume_and_value()`와 동일

### F. 공매도 데이터 (etf_short_selling)

**제공 가능**: ✅ 완전 제공 (100%)
**현황**: pykrx는 개별 주식만 제공, ETF 미지원
**계획**: kotlin-krx에서 MDCSTAT31401, 31501 직접 구현

### G. 배당 데이터 (etf_dividends, etf_corporate_actions)

**제공 가능**: ❌ 모두 제공 불가
**대안**: OPENDART API 통합 필요

### H. 채권 데이터 (etf_bonds_*)

**제공 가능**: ❌ 모두 제공 불가
**대안**: 불가능 (KRX API 미제공)

---

## 3. 데이터 커버리지 요약 테이블

| 데이터 카테고리 | 필드 수 | kotlin-krx 제공 | 제공 불가 | 커버리지 | 상태 |
|----------------|---------|----------------|----------|---------|------|
| A. 마스터 정보 | 18 | 6 | 12 | 33% | 🟡 |
| B. 가격 데이터 | 45 | 19 | 26 | 42% | 🟡 |
| C. 펀더멘털 | 28 | 17 | 11 | 61% | 🟡 |
| D. 포트폴리오 구성 | 23 | 3 | 20 | 13% | ❌ |
| E. 자산 배분 | 10 | 0 | 10 | 0% | ❌ |
| F. 섹터 배분 | 4 | 0 | 4 | 0% | ❌ |
| G. 지역 배분 | 4 | 0 | 4 | 0% | ❌ |
| H. 투자자 거래 | 7 | 6 | 1 | 86% | ✅ |
| I. 공매도 | 5 | 5 | 0 | **100%** | ✅ |
| J. 외국인 보유 | 5 | 0 | 5 | 0% | ❌ |
| K. 채권 정적 | 6 | 0 | 6 | 0% | ❌ |
| L. 채권 일별 | 4 | 0 | 4 | 0% | ❌ |
| M. 채권 월별 | 5 | 0 | 5 | 0% | ❌ |
| N. 채권 상세 | 10 | 0 | 10 | 0% | ❌ |
| O. 배당금 | 8 | 0 | 8 | 0% | ❌ |
| P. Corporate Actions | 6 | 0 | 6 | 0% | ❌ |
| **전체 합계** | **188** | **56** | **132** | **30%** | 🟡 |

### 범례
- ✅ 70% 이상 제공
- 🟡 30-70% 제공
- ❌ 30% 미만 제공

---

## 4. kotlin-krx 구현 우선순위

### Phase 1: 핵심 데이터 (이미 계획됨)

**목표**: pykrx 동등성 + 추가 엔드포인트

1. ✅ **MDCSTAT04501** (OHLCV) - 백테스팅 필수
2. ✅ **MDCSTAT04601** (기본 정보) - ETF 마스터
3. 🚧 **MDCSTAT04701** (종합 정보) - **최우선 순위** (pykrx 미구현)
4. ✅ **MDCSTAT05001** (PDF) - 포트폴리오 구성
5. ✅ **MDCSTAT05901** (추적오차)
6. ✅ **MDCSTAT06001** (괴리율)
7. ✅ **MDCSTAT04801/04802** (투자자 거래)
8. 🚧 **MDCSTAT31401/31501** (공매도) - **kotlin-krx 독점**

**예상 완료**: 2025-12 (2개월)

### Phase 2: 확장 기능

**목표**: 기술적 지표 및 파생 데이터

1. **기술적 지표 계산 엔진**
   - SMA, EMA (이동평균)
   - RSI, Stochastic (모멘텀)
   - MACD, Signal (추세)
   - Bollinger Bands (변동성)
   - ATR, ADX (변동성/추세 강도)

2. **리스크 메트릭 계산**
   - Sharpe Ratio
   - Sortino Ratio
   - Maximum Drawdown
   - Value at Risk (VaR)
   - Beta, Alpha

3. **성과 분석**
   - 기간별 수익률 (1M, 3M, 6M, 1Y, 3Y, 5Y, YTD)
   - 누적 수익률
   - 연환산 수익률
   - 벤치마크 대비 초과 수익률

**예상 완료**: 2026-02 (3개월)

### Phase 3: 외부 데이터 통합

**목표**: KRX API 한계 극복

1. **OPENDART API 통합**
   - 배당/분배금 데이터
   - 상장폐지 공시
   - 액면분할/병합 이벤트
   - 기업 활동 공시

2. **종목 마스터 테이블**
   - GICS 섹터/산업 분류
   - 국가/지역 정보
   - 자산 유형 (주식/채권/현금)
   - ISIN, SEDOL, CUSIP 코드

3. **자산/섹터/지역 배분 계산**
   - holdings 데이터 + 종목 마스터 조인
   - 자산 유형별 집계
   - 섹터별 집계
   - 지역별 집계

**예상 완료**: 2026-05 (3개월)

### Phase 4: 선택 사항 (낮은 우선순위)

1. **조정가 계산 (네이버 API 통합)**
   - pykrx는 네이버 증권 API로 ETF 조정주가 제공
   - kotlin-krx도 네이버 스크래핑 또는 API 통합 검토
   - 대안: OPENDART 배당 이력 기반 자체 계산
   - 액면분할은 드물게 발생하나 조정 필요

2. **밸류에이션 메트릭 (제한적)**
   - 구성 종목 기반 가중평균 P/E, P/B 계산
   - 외부 데이터 소스 필요 (유료)

3. **채권 ETF 상세 정보**
   - 외부 채권 데이터 소스 통합
   - 듀레이션, 만기, 신용등급 계산

**예상 완료**: TBD (데이터 소스 확보 시)

### 구현 불가능 항목 (KRX 데이터 없음)

1. ❌ **밸류에이션 메트릭 (P/E, P/B, P/S 등)**
   - KRX API는 구성 종목 재무제표 미제공
   - 외부 유료 서비스 필요 (FnGuide, WISEfn 등)

2. ❌ **채권 ETF 상세 데이터**
   - 듀레이션, YTM, OAS 등 KRX 미제공
   - 채권정보센터 또는 KIS 채권 API 필요

3. ❌ **외국인 보유 현황 (ETF)**
   - KRX는 개별 주식만 제공, ETF 미제공
   - 공식 통계 미공개

---

## 5. 데이터 품질 및 제약사항

### 히스토리 데이터

**제공 범위**:
- ✅ **일별 시계열**: pykrx와 동일하게 상장일부터 현재까지 제공
- ❌ **월별 펀더멘털 히스토리**: 없음 (현재 스냅샷만)
- ❌ **분기별/연별 집계**: 없음 (자체 계산 필요)

**자동 분할**:
- KrxClient가 730일 초과 범위를 자동 분할
- 여러 요청으로 나눠 병합하여 반환
- 백테스팅에 이상적 (2000년대 초반 상장 ETF도 전체 이력 조회 가능)

```kotlin
// 예시: 10년 치 데이터 조회 (자동 분할)
val ohlcv = etfService.getOhlcv(
    ticker = "152100",
    from = LocalDate.of(2014, 1, 1),
    to = LocalDate.of(2024, 1, 1)
)
// 내부적으로 14개 청크로 분할 (730일 * 14 ≈ 10년)
```

### 실시간성

**pykrx**: 당일 장마감 후 데이터 제공 (T+0 EOD)
**kotlin-krx**: 동일 (KRX API 제약)

**업데이트 시점**:
- 장 중: 미제공 (실시간 데이터 없음)
- 15:30 이후: 당일 데이터 반영 (장 마감 후)
- 주말/공휴일: 전 영업일 데이터 유지

**제약사항**:
- 실시간 체결가 없음 (별도 실시간 API 필요)
- 장 중 NAV는 미제공
- 분/시간 단위 데이터 없음 (일별만)

### 데이터 정확성

**공식 KRX 데이터 (높은 신뢰도)**:
- NAV (순자산가치)
- 추적오차율
- 괴리율
- OHLCV
- 거래량/거래대금
- 시가총액

**계산 지표 (알고리즘 의존)**:
- 기술적 지표 (SMA, RSI 등)
- 리스크 메트릭 (Sharpe, MDD 등)
- 자산/섹터 배분 (종목 마스터 품질에 의존)

**알려진 데이터 이슈**:
1. **INVSTASST_NETASST_TOTAMT** (순자산총액): 종종 0으로 반환 (KRX API 버그)
   - 대안: `NAV * LIST_SHRS`로 계산
2. **COMPST_ISU_CD**: 혼합 ISIN/티커 형식 (일관성 없음)
   - 대안: ISIN 형식 감지 후 substring(3, 9)로 티커 추출
3. **특수 값 처리**: "-", "", null 등 다양한 결측값 형식
   - 대안: 정규화 함수로 BigDecimal.ZERO 또는 0L 변환

---

## 6. 사용 사례별 가능 여부

### kotlin-krx 단독으로 가능한 것

#### 1. ETF 백테스팅 (전략 시뮬레이션)

**가능 여부**: ✅ **완전 가능**

**필요 데이터**:
- OHLCV (MDCSTAT04501)
- NAV (MDCSTAT04501)
- 조정가 (자체 계산)
- 포트폴리오 구성 (MDCSTAT05001)

**구현 예시**:
```kotlin
class BacktestEngine {
    fun runStrategy(
        tickers: List<String>,
        startDate: LocalDate,
        endDate: LocalDate,
        initialCapital: BigDecimal,
        strategy: TradingStrategy
    ): BacktestResult {
        // OHLCV 데이터 로드
        val priceData = tickers.associateWith { ticker ->
            etfService.getOhlcv(ticker, startDate, endDate)
        }

        // 전략 실행
        return strategy.execute(priceData, initialCapital)
    }
}
```

#### 2. 리스크 분석

**가능 여부**: ✅ **완전 가능**

**제공 메트릭**:
- 변동성 (Volatility): OHLCV 기반 표준편차
- Sharpe Ratio: (수익률 - 무위험이자율) / 변동성
- Sortino Ratio: 하방 위험 기반
- Maximum Drawdown: OHLCV 기반 최대 낙폭
- Beta: ETF vs 벤치마크 지수 회귀 분석
- VaR, CVaR: 히스토리 시뮬레이션

#### 3. 트래킹 성과 분석

**가능 여부**: ✅ **완전 가능**

**제공 데이터**:
- 추적오차율 (MDCSTAT05901)
- 괴리율 (MDCSTAT06001)
- NAV vs 지수 상관계수
- 트래킹 디퍼런스 (NAV 수익률 - 지수 수익률)

#### 4. 투자자 거래 패턴 분석

**가능 여부**: ✅ **완전 가능**

**제공 데이터**:
- 기관/개인/외국인 일별 순매수 (MDCSTAT04802)
- 세부 투자자 유형별 거래 (MDCSTAT04801)
- 순매수 추세 분석
- 수급 신호 탐지

#### 5. 공매도 분석

**가능 여부**: ✅ **완전 가능** (pykrx 불가능)

**제공 데이터**:
- 일별 공매도 거래량/거래대금 (MDCSTAT31401)
- 공매도 잔고 및 비율 (MDCSTAT31501)
- 상환 거래량 (숏 커버링)
- 공매도 과열 종목 탐지

### 추가 통합 필요한 것

#### 6. 배당 전략 백테스팅

**가능 여부**: ❌ **불가능** (OPENDART 통합 필요)

**필요 데이터**:
- 배당락일 (ex_date)
- 배당 지급일 (payment_date)
- 배당 금액 (amount)
- 배당 빈도 (frequency)

**해결책**: OPENDART API 통합
```kotlin
class DividendService(
    private val opendartClient: OpendartClient
) {
    fun getDividendHistory(ticker: String): List<Dividend> {
        val corpCode = getCorpCode(ticker)
        return opendartClient.getDividendAnnouncements(corpCode)
    }
}
```

#### 7. 섹터/테마 로테이션 전략

**가능 여부**: 🟡 **부분 가능** (종목 마스터 필요)

**필요 데이터**:
- ETF 구성 종목 (MDCSTAT05001) ✅
- 종목별 GICS 섹터 ❌ (외부 소스)
- 섹터별 가중치 ❌ (자체 계산)

**해결책**: 종목 마스터 테이블 구축
```kotlin
data class StockMaster(
    val ticker: String,
    val name: String,
    val sector: String,      // GICS Level 1
    val industry: String,    // GICS Level 2
    val country: String
)

fun calculateSectorAllocation(etfTicker: String): Map<String, BigDecimal> {
    val holdings = etfService.getHoldings(etfTicker)
    return holdings
        .groupBy { stockMaster.getSector(it.ticker) }
        .mapValues { (_, holdings) -> holdings.sumOf { it.weight } }
}
```

#### 8. 밸류에이션 기반 스크리닝

**가능 여부**: ❌ **불가능** (KRX 미제공)

**필요 데이터**:
- P/E Ratio
- P/B Ratio
- Dividend Yield
- EPS, BPS

**해결책**: 외부 유료 데이터 소스 (FnGuide, WISEfn 등)

### 구현 불가능한 것 (데이터 없음)

#### 9. 채권 ETF 듀레이션 매칭 전략

**가능 여부**: ❌ **완전 불가능**

**사유**: KRX API는 채권 ETF 상세 정보 미제공
- Average Duration
- Effective Duration
- YTM (Yield to Maturity)
- OAS (Option-Adjusted Spread)
- Credit Quality

#### 10. 외국인 보유 비율 기반 전략

**가능 여부**: ❌ **완전 불가능**

**사유**: KRX는 ETF 외국인 보유 현황 미제공
- 개별 주식은 제공하나 ETF는 공식 통계 없음

---

## 7. 권장 구현 로드맵

### Step 1: 핵심 데이터 수집 (즉시 시작)

```kotlin
// 1. MDCSTAT04701 구현 (최우선)
val info = etfComprehensiveService.getComprehensiveInfo("152100")
println("총 보수: ${info.totalFee}%")
println("52주 고가: ${info.week52High}")

// 2. 공매도 데이터 구현
val shortSelling = shortingService.getShortSellingInfo("152100", from, to)
println("공매도 잔고 비율: ${shortSelling.balanceRatio}%")
```

### Step 2: 계산 엔진 구축 (Phase 2)

```kotlin
// 기술적 지표 계산
val indicators = TechnicalIndicators()
val ohlcv = etfService.getOhlcv("152100", from, to)
val prices = ohlcv.map { it.close }

val sma20 = indicators.sma(prices, 20)
val rsi14 = indicators.rsi(prices, 14)
val macd = indicators.macd(prices)

// 리스크 메트릭 계산
val riskMetrics = RiskAnalyzer()
val sharpe = riskMetrics.sharpeRatio(ohlcv)
val maxDD = riskMetrics.maxDrawdown(ohlcv)
```

### Step 3: 외부 통합 (Phase 3)

```kotlin
// OPENDART 배당 데이터
val dividends = opendartService.getDividendHistory("152100")

// 종목 마스터 조인
val holdings = etfService.getHoldings("152100")
val enrichedHoldings = holdings.map { holding ->
    val stockInfo = stockMasterService.getStockInfo(holding.ticker)
    EnrichedHolding(
        ticker = holding.ticker,
        name = stockInfo.name,
        sector = stockInfo.sector,
        weight = holding.weight
    )
}

// 섹터 배분 계산
val sectorAllocation = enrichedHoldings
    .groupBy { it.sector }
    .mapValues { (_, holdings) -> holdings.sumOf { it.weight } }
```

### Step 4: 백테스팅 엔진 완성 (Phase 4)

```kotlin
class AdvancedBacktestEngine(
    private val etfService: EtfService,
    private val indicators: TechnicalIndicators,
    private val riskAnalyzer: RiskAnalyzer,
    private val dividendService: DividendService
) {
    fun runBacktest(
        strategy: Strategy,
        tickers: List<String>,
        startDate: LocalDate,
        endDate: LocalDate,
        initialCapital: BigDecimal
    ): BacktestResult {
        // 1. 가격 데이터 로드
        val priceData = loadPriceData(tickers, startDate, endDate)

        // 2. 배당 데이터 로드
        val dividends = loadDividends(tickers, startDate, endDate)

        // 3. 기술적 지표 계산
        val technicals = calculateTechnicals(priceData)

        // 4. 전략 실행
        val trades = strategy.generateSignals(priceData, technicals)

        // 5. 포트폴리오 시뮬레이션
        val portfolio = simulatePortfolio(trades, priceData, dividends, initialCapital)

        // 6. 성과 분석
        return analyzePerformance(portfolio)
    }
}
```

---

## 8. pykrx 대비 경쟁 우위

### kotlin-krx가 더 나은 점

1. **MDCSTAT04701 지원**
   - pykrx: ❌ 미구현
   - kotlin-krx: ✅ 구현
   - 차별점: 단일 요청으로 30개 이상 필드 제공 (총 보수, 52주 고가/저가 등)

2. **공매도 데이터 (ETF)**
   - pykrx: ❌ ETF 미지원 (개별 주식만)
   - kotlin-krx: ✅ 완전 지원 (MDCSTAT31401, 31501)
   - 차별점: 공매도 거래 + 잔고 모두 제공

3. **타입 안전성**
   - pykrx: Python 동적 타이핑, Pandas DataFrame
   - kotlin-krx: Kotlin 정적 타이핑, 데이터 클래스
   - 차별점: 컴파일 타임 오류 검증, IDE 자동완성

4. **Spring 생태계 통합**
   - pykrx: 독립 라이브러리
   - kotlin-krx: Spring Boot 네이티브 지원
   - 차별점: DI, 트랜잭션, 캐싱, 스케줄링 등

5. **문서화**
   - pykrx: 제한적 (주로 코드 주석)
   - kotlin-krx: 포괄적 (8개 기술 문서, KDoc)
   - 차별점: 엔드포인트 상세 명세, 필드 매핑, 사용 예제

### pykrx가 더 나은 점

1. **생태계 성숙도**
   - pykrx: Python 데이터 분석 생태계 (Pandas, NumPy, Matplotlib)
   - kotlin-krx: JVM 생태계 (상대적으로 데이터 분석 도구 부족)

2. **즉시 사용 가능한 분석 도구**
   - pykrx: DataFrame 반환으로 즉시 시각화/분석 가능
   - kotlin-krx: 별도 시각화 라이브러리 필요 (Lets-Plot, Kravis 등)

3. **네이버 API 통합 (조정주가)**
   - pykrx: 네이버 증권 API로 ETF 조정주가 제공
   - kotlin-krx: KRX API만 사용 (조정주가 미제공)
   - pykrx는 `get_market_ohlcv(ticker, adjusted=True)`로 ETF도 지원

4. **커뮤니티 및 레퍼런스**
   - pykrx: 널리 사용됨, 많은 튜토리얼
   - kotlin-krx: 신규 라이브러리, 제한적 레퍼런스

### 공통점

- 730일 자동 분할
- 동일한 KRX 엔드포인트 사용
- 일별 EOD 데이터 제공
- OHLCV, NAV, 추적오차, 괴리율 제공

---

## 9. 결론 및 제안

### 주요 발견 사항

1. **kotlin-krx는 핵심 ETF 운용 데이터를 충실히 제공** (30% 전체 커버리지)
   - 가격, NAV, 추적 성과, 투자자 거래 등 백테스팅 핵심 데이터 완비
   - 공매도 데이터는 pykrx보다 우수

2. **밸류에이션, 채권 상세, 배당 데이터는 KRX API 한계로 제공 불가**
   - 외부 데이터 소스 통합 필요 (OPENDART, 유료 서비스)

3. **자산/섹터/지역 배분은 2차 계산으로 구현 가능**
   - holdings 데이터 + 종목 마스터 조인
   - 데이터 품질은 종목 마스터에 의존

### 추천 구현 전략

#### 단기 (2-3개월)
1. MDCSTAT04701 구현 완료
2. 공매도 엔드포인트 구현
3. 기본 백테스팅 기능 제공
4. 기술적 지표 계산 엔진 (SMA, EMA, RSI, MACD)

#### 중기 (3-6개월)
1. OPENDART API 통합 (배당 데이터)
2. 종목 마스터 테이블 구축 (GICS 섹터)
3. 자산/섹터 배분 계산 기능
4. 리스크 메트릭 확장 (Sharpe, Sortino, MDD, VaR)

#### 장기 (6-12개월)
1. 외부 유료 데이터 소스 평가 및 통합
2. 밸류에이션 메트릭 제공 (가능한 범위)
3. 실시간 데이터 연동 (별도 API)
4. 머신러닝 기반 예측 모델 통합

### 최종 권장사항

**kotlin-krx는 다음 용도에 최적화됨**:
1. ✅ ETF 가격 기반 백테스팅
2. ✅ 추적 성과 및 리스크 분석
3. ✅ 투자자 심리 및 공매도 분석
4. ✅ 포트폴리오 구성 추적
5. ✅ 기술적 분석 전략

**추가 통합 권장**:
1. 🔧 OPENDART API (배당, 공시)
2. 🔧 종목 마스터 (섹터, 산업)
3. 💰 유료 데이터 (밸류에이션, 채권 상세)

**구현 불가능 (데이터 없음)**:
1. ❌ ETF 밸류에이션 메트릭 (P/E, P/B 등)
2. ❌ 채권 ETF 듀레이션, YTM 등
3. ❌ 외국인 보유 현황

---

## 부록: 데이터 소스 매핑

### KRX API 엔드포인트

| BLD 코드 | 명칭 | 제공 데이터 | kotlin-krx 구현 |
|---------|------|-----------|----------------|
| MDCSTAT04301 | 전종목 시세 | 일별 스냅샷 | ✅ |
| MDCSTAT04401 | 전종목 등락률 | 기간 수익률 | ✅ |
| MDCSTAT04501 | 개별종목 시세 추이 | OHLCV 시계열 | ✅ |
| MDCSTAT04601 | 전종목 기본정보 | ETF 마스터 | ✅ |
| **MDCSTAT04701** | **개별종목 종합정보** | **종합 데이터** | **🚧** |
| MDCSTAT04801 | 투자자별 거래 (기간) | 투자자 거래 집계 | ✅ |
| MDCSTAT04802 | 투자자별 거래 (일별) | 투자자 거래 시계열 | ✅ |
| MDCSTAT04901 | 투자자별 거래 개별 (기간) | 개별 ETF 집계 | ✅ |
| MDCSTAT04902 | 투자자별 거래 개별 (일별) | 개별 ETF 시계열 | ✅ |
| MDCSTAT05001 | PDF | 포트폴리오 구성 | ✅ |
| MDCSTAT05901 | 추적오차율 추이 | 추적 성과 | ✅ |
| MDCSTAT06001 | 괴리율 추이 | 가격 괴리 | ✅ |
| **MDCSTAT31401** | **공매도 거래** | **공매도 거래** | **🚧** |
| **MDCSTAT31501** | **공매도 종합정보** | **공매도 잔고** | **🚧** |

### 외부 API 필요 사항

| 데이터 | API 소스 | 용도 |
|--------|---------|------|
| 배당/분배금 | OPENDART | 배당 전략, 수익률 조정 |
| 상장폐지 공시 | OPENDART | Corporate Actions |
| GICS 섹터 | 자체 구축 또는 FnGuide | 섹터 배분 |
| 밸류에이션 | FnGuide, WISEfn | P/E, P/B 스크리닝 |
| 채권 상세 | KIS 채권 API | 채권 ETF 분석 |

---

**문서 버전**: 1.0
**최종 수정일**: 2025-11-18
**작성자**: Claude (Anthropic AI)
**검토자**: TBD
