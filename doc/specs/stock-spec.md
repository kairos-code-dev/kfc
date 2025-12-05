# Stock (주식 종목 정보) 네임스페이스 기술명세서

> **작성일**: 2025-12-04
> **버전**: 2.0
> **대상 프로젝트**: KFC (Korea Financial data Collector)
> **변경 이력**: OTP 기반 인증 제거, 실제 KRX API 호출 방식 반영

---

## 목차

1. [개요](#1-개요)
2. [데이터 소스 분석](#2-데이터-소스-분석)
3. [도메인 모델 설계](#3-도메인-모델-설계)
4. [API 레이어 설계](#4-api-레이어-설계)
5. [인프라 레이어 설계](#5-인프라-레이어-설계)
6. [구현 우선순위](#6-구현-우선순위)
7. [예외 처리](#7-예외-처리)
8. [참고 자료](#8-참고-자료)

---

## 1. 개요

### 1.1. 목적

한국 상장 기업의 기본 정보 및 메타데이터를 제공하는 `stock` 네임스페이스를 KFC 프로젝트에 추가합니다. 이를 통해 사용자는 종목 리스트, 종목명, 시장 구분, 섹터/산업 분류, ISIN 코드 등 회사 기본 정보를 표준화된 방식으로 조회할 수 있습니다.

### 1.2. 범위

다음 핵심 기능을 지원합니다:

| 기능 | 설명 | 데이터 소스 |
|------|------|------------|
| 종목 리스트 조회 | 시장별 상장 종목 전체 목록 | KRX |
| 종목 기본정보 조회 | 종목명, ISIN, 시장, 상장일 등 | KRX |
| 섹터/산업 분류 조회 | 업종 분류 현황 및 시가총액 | KRX |
| 종목 검색 | 종목명/코드 기반 검색 | KRX |
| 상장/상폐 종목 구분 | 상장 종목 및 상폐 종목 관리 | KRX |

### 1.3. 설계 원칙

1. **기존 패턴 준수**: ETF/Index 네임스페이스와 동일한 인프라 구조 사용
2. **도메인 중심 설계**: 회사 기본 정보 도메인 기준으로 분류
3. **데이터 소스 독립성**: KRX API를 우선 지원하되, 향후 다른 소스 추가 가능하도록 추상화
4. **타입 안전성**: Kotlin의 타입 시스템을 활용한 명시적 타입 변환
5. **캐싱 전략**: 종목 리스트, 기본정보는 변경 빈도가 낮으므로 캐싱 적용 권장

### 1.4. 네임스페이스 경계

| 데이터 | 담당 네임스페이스 | 기준 |
|--------|-----------------|------|
| **종목명, ISIN, 섹터, 산업** | `stock` | 회사 기본 정보, 자주 변경 안됨 |
| **현재가, 거래량, 시가총액** | `price` | 실시간 변동 데이터 |
| **재무제표** | `financials` | 재무 상태 및 실적 |
| **배당, 분할** | `corp` | 기업 이벤트 |

#### 1.4.1. price 네임스페이스와의 중복 처리

`StockSectorInfo` 모델에 `closePrice`, `marketCap` 필드가 포함됩니다:

| 필드 | stock (StockSectorInfo) | price (PriceApi) | 비고 |
|------|------------------------|------------------|------|
| `closePrice` | ✅ 포함 | ✅ 포함 | 업종분류 API 응답에 포함됨 |
| `marketCap` | ✅ 포함 | ✅ 포함 | 업종분류 API 응답에 포함됨 |

**설계 결정**:
- `stock` 네임스페이스에서는 **업종분류 맥락**에서 시가총액/종가를 제공
- `price` 네임스페이스에서는 **시계열 가격 데이터** 제공
- 동일 데이터가 중복되지만, 각 네임스페이스의 목적이 다르므로 허용

---

## 2. 데이터 소스 분석

### 2.1. KRX API

#### 2.1.1. API 개요

| 항목 | 내용 |
|------|------|
| **API 명** | KRX 정보데이터시스템 |
| **도메인** | `http://data.krx.co.kr` |
| **베이스 URL** | `http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd` |
| **인증 방식** | **없음 (공개 API)** |
| **요청 방식** | POST (application/x-www-form-urlencoded) |
| **호출 제한** | 명시적 제한 없음 (Rate Limiting 권장) |
| **지원 기간** | 제한 없음 (API별 상이) |

**중요**: 이전 버전에서 언급된 OTP 인증은 실제로 사용되지 않습니다. KRX API는 단순 POST 요청으로 데이터를 조회할 수 있습니다.

#### 2.1.2. HTTP 요청 구조

기존 ETF/Index 구현과 동일한 방식을 사용합니다:

**요청 예시**:
```http
POST http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd
Content-Type: application/x-www-form-urlencoded
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Accept: application/json, text/plain, */*
Accept-Language: ko-KR,ko;q=0.9
Referer: http://data.krx.co.kr/
Origin: http://data.krx.co.kr

bld=dbms/comm/finder/finder_stkisu&mktsel=STK
```

**응답 구조**:
```json
{
  "block1": [
    {
      "full_code": "KR7005930003",
      "short_code": "005930",
      "codeName": "삼성전자",
      "marketCode": "STK",
      "marketName": "코스피"
    }
  ]
}
```

또는

```json
{
  "OutBlock_1": [...],
  "result": {
    "status": "success"
  }
}
```

#### 2.1.3. 필수 HTTP 헤더

기존 `KrxHttpClient`에서 사용하는 헤더를 그대로 사용:

| 헤더 | 값 | 용도 |
|------|-----|------|
| `User-Agent` | Mozilla/5.0 (브라우저) | 브라우저 흉내 (필수) |
| `Accept` | application/json, text/plain, */* | JSON 응답 수신 |
| `Accept-Language` | ko-KR,ko;q=0.9 | 한국어 응답 |
| `Referer` | http://data.krx.co.kr/ | 출처 인증 (필수) |
| `Origin` | http://data.krx.co.kr | CORS 처리 |

#### 2.1.4. 주요 API 엔드포인트

##### A. 상장 종목 검색 (finder_stkisu)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/comm/finder/finder_stkisu` |
| **용도** | 특정 시장의 모든 상장 종목 조회 |
| **요청 파라미터** | `mktsel` (STK/KSQ/KNX/ALL), `searchText`, `typeNo` |
| **응답 필드** | `full_code`, `short_code`, `codeName`, `marketCode`, `marketName` |

**요청 예시**:
```
bld=dbms/comm/finder/finder_stkisu
mktsel=STK
searchText=
typeNo=0
```

**응답 예시**:
```json
{
  "block1": [
    {
      "full_code": "KR7005930003",
      "short_code": "005930",
      "codeName": "삼성전자",
      "marketCode": "STK",
      "marketName": "코스피"
    },
    {
      "full_code": "KR7000660001",
      "short_code": "000660",
      "codeName": "SK하이닉스",
      "marketCode": "STK",
      "marketName": "코스피"
    }
  ]
}
```

##### B. 상폐 종목 검색 (finder_listdelisu)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/comm/finder/finder_listdelisu` |
| **용도** | 상장폐지된 종목 조회 |
| **요청 파라미터** | `mktsel`, `searchText`, `typeNo` |
| **응답 필드** | `full_code`, `short_code`, `codeName`, `marketCode`, `marketName` |

##### C. 업종분류현황 (MDCSTAT03901)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT03901` |
| **용도** | 종목별 섹터/산업 분류 및 시가총액 조회 |
| **요청 파라미터** | `trdDd` (거래일, YYYYMMDD), `mktId` (STK/KSQ/ALL) |
| **응답 필드** | `ISU_SRT_CD`, `ISU_ABBRV`, `IDX_IND_NM`, `TDD_CLSPRC`, `MKTCAP`, `FLUC_TP_CD` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT03901
trdDd=20241204
mktId=STK
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "ISU_SRT_CD": "005930",
      "ISU_ABBRV": "삼성전자",
      "IDX_IND_NM": "전기전자",
      "TDD_CLSPRC": "71,500",
      "MKTCAP": "426,789,000,000,000",
      "FLUC_TP_CD": "2"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `ISU_SRT_CD` | STRING | 종목 코드 (6자리) | `005930` |
| `ISU_ABBRV` | STRING | 종목 약칭 | `삼성전자` |
| `IDX_IND_NM` | STRING | 산업 분류명 | `전기전자` |
| `TDD_CLSPRC` | STRING | 당일 종가 (콤마 포함) | `71,500` |
| `MKTCAP` | STRING | 시가총액 (원, 콤마 포함) | `426,789,000,000,000` |
| `FLUC_TP_CD` | STRING | 등락 구분 (1:상승, 2:하락, 3:보합) | `2` |

##### D. 전체 종목 시세 (MDCSTAT01501)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT01501` |
| **용도** | 특정 날짜의 전체 종목 시세 조회 (종목명, 종가, 시가총액 포함) |
| **요청 파라미터** | `trdDd` (YYYYMMDD), `mktId` (STK/KSQ/ALL) |
| **응답 필드** | `ISU_SRT_CD`, `ISU_NM`, `TDD_CLSPRC`, `MKTCAP`, `LIST_SHRS` |

#### 2.1.5. 시장 구분 코드 (mktId / mktsel)

| 코드 | 시장명 | 설명 |
|------|--------|------|
| `STK` | 코스피 (KOSPI) | 한국 대형주 시장 |
| `KSQ` | 코스닥 (KOSDAQ) | 한국 중소형주 시장 |
| `KNX` | 코넥스 (KONEX) | 한국 벤처기업 시장 |
| `ALL` | 전체 시장 | 모든 시장 통합 조회 |

#### 2.1.6. 기존 시스템과의 통합

`KrxHttpClient`를 재사용하여 구현:

```kotlin
// 기존 KrxHttpClient 사용
private val httpClient = KrxHttpClient()

suspend fun fetchListedStocks(market: String): Map<String, Any?> {
    val parameters = mapOf(
        "bld" to "dbms/comm/finder/finder_stkisu",
        "mktsel" to market,
        "searchText" to "",
        "typeNo" to "0"
    )

    return httpClient.post(BASE_URL, parameters)
}
```

### 2.2. pykrx 라이브러리 참고

pykrx는 KRX API를 Python으로 래핑한 라이브러리로, 다음 함수들을 제공합니다:

| pykrx 함수 | KRX API bld | 용도 |
|-----------|------------|------|
| `get_market_ticker_list()` | `finder_stkisu` | 시장별 종목 리스트 |
| `get_market_ticker_name()` | `finder_stkisu` | 종목 코드 → 종목명 |
| `get_market_sector_classifications()` | `MDCSTAT03901` | 섹터/산업 분류 |
| - | `finder_listdelisu` | 상폐 종목 조회 |

**중요 발견**: pykrx 분석 결과, **OTP 인증을 전혀 사용하지 않습니다**. 단순 HTTP GET/POST 요청으로 데이터를 조회합니다.

**참고**: KFC에서는 pykrx를 직접 사용하지 않고, 동일한 KRX API를 Kotlin으로 구현합니다.

### 2.3. 데이터 소스 한계 및 대안

| 정보 | KRX 제공 여부 | 대안 |
|------|-------------|------|
| 종목명, ISIN | ✅ 제공 | - |
| 섹터/산업 분류 | ✅ 제공 (업종분류현황) | - |
| 상장일 | ⚠️ 일부 제공 | 별도 API 필요 시 고려 |
| 직원 수, 웹사이트 | ❌ 미제공 | OPENDART API (향후 확장) |
| 사업 개요 | ❌ 미제공 | OPENDART API (향후 확장) |
| 발행주식수 | ✅ 제공 (시세 API) | - |

---

## 3. 도메인 모델 설계

### 3.1. 패키지 구조

```
dev.kairoscode.kfc/
├── domain/
│   └── stock/
│       ├── StockInfo.kt               # 종목 기본정보 모델
│       ├── StockListItem.kt           # 종목 리스트 항목
│       ├── StockSectorInfo.kt         # 섹터/산업 분류 정보
│       ├── Market.kt                  # 시장 구분 Enum
│       ├── IndustryClassification.kt  # 산업 분류 상세
│       └── ListingStatus.kt           # 상장 상태 Enum
```

### 3.2. 핵심 모델 명세

#### 3.2.1. StockListItem (종목 리스트 항목)

간단한 종목 목록 조회 시 사용하는 경량 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `ticker` | String | 종목 코드 (6자리) | `005930` |
| `name` | String | 종목명 | `삼성전자` |
| `isin` | String | ISIN 코드 (12자리) | `KR7005930003` |
| `market` | Market | 시장 구분 | `Market.KOSPI` |
| `listingStatus` | ListingStatus | 상장 상태 | `ListingStatus.LISTED` |

**설계 의도**:
- 최소한의 정보만 포함하여 응답 속도 최적화
- 종목 리스트 페이지네이션, 검색 결과 등에 적합
- 상세 정보가 필요한 경우 `StockInfo` 또는 `StockSectorInfo` 추가 조회

**코드 스켈레톤**:
```kotlin
data class StockListItem(
    val ticker: String,
    val name: String,
    val isin: String,
    val market: Market,
    val listingStatus: ListingStatus
) {
    companion object {
        fun fromRaw(raw: Map<String, Any?>): StockListItem {
            return StockListItem(
                ticker = raw.getString("short_code"),
                name = raw.getString("codeName"),
                isin = raw.getString("full_code"),
                market = raw.getString("marketCode").toMarket(),
                listingStatus = ListingStatus.LISTED
            )
        }
    }
}
```

#### 3.2.2. StockInfo (종목 기본정보)

개별 종목의 상세 메타데이터를 담는 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `ticker` | String | 종목 코드 | `005930` |
| `name` | String | 종목명 | `삼성전자` |
| `fullName` | String? | 정식 종목명 | `삼성전자보통주` |
| `isin` | String | ISIN 코드 | `KR7005930003` |
| `market` | Market | 시장 구분 | `Market.KOSPI` |
| `listingStatus` | ListingStatus | 상장 상태 | `ListingStatus.LISTED` |
| `listingDate` | LocalDate? | 상장일 | `1975-06-11` |
| `sharesOutstanding` | Long? | 발행주식수 | `5969782550` |

**설계 의도**:
- 종목 상세 페이지, 포트폴리오 관리 등에 활용
- null 허용: 일부 정보는 KRX API에서 제공하지 않을 수 있음
- `sharesOutstanding`: 별도 API 호출 필요 (전체 종목 시세 API 활용)

#### 3.2.3. StockSectorInfo (섹터/산업 분류 정보)

종목의 섹터, 산업 분류 및 시가총액 정보를 담는 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `ticker` | String | 종목 코드 | `005930` |
| `name` | String | 종목명 | `삼성전자` |
| `market` | Market | 시장 구분 | `Market.KOSPI` |
| `industry` | String | 산업 분류명 | `전기전자` |
| `closePrice` | Long? | 종가 | `71500` |
| `marketCap` | Long? | 시가총액 (원) | `426789000000000` |
| `priceChangeType` | PriceChangeType? | 등락 구분 | `PriceChangeType.FALL` |

**설계 의도**:
- 업종별 분석, 섹터 로테이션 전략 등에 활용
- 시가총액 포함: 섹터 내 비중 계산 가능
- KRX "업종분류현황" API 응답을 직접 매핑

**코드 스켈레톤**:
```kotlin
data class StockSectorInfo(
    val ticker: String,
    val name: String,
    val market: Market,
    val industry: String,
    val closePrice: Long?,
    val marketCap: Long?,
    val priceChangeType: PriceChangeType?
) {
    companion object {
        fun fromRaw(raw: Map<String, Any?>, market: Market): StockSectorInfo {
            return StockSectorInfo(
                ticker = raw.getString(KrxApiFields.Identity.TICKER),
                name = raw.getString(KrxApiFields.Identity.NAME_SHORT),
                market = market,
                industry = raw.getString(KrxApiFields.Index.NAME),
                closePrice = raw.getString(KrxApiFields.Price.CLOSE_ALT)
                    .replace(",", "").toLongOrNull(),
                marketCap = raw.getString(KrxApiFields.Asset.MARKET_CAP)
                    .replace(",", "").toLongOrNull(),
                priceChangeType = raw.getString(KrxApiFields.PriceChange.DIRECTION)
                    .toPriceChangeType()
            )
        }
    }
}
```

#### 3.2.4. IndustryClassification (산업 분류 상세)

특정 산업에 속한 모든 종목을 그룹화한 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `industryName` | String | 산업 분류명 | `전기전자` |
| `market` | Market | 시장 구분 | `Market.KOSPI` |
| `stocks` | List<StockSectorInfo> | 해당 산업 종목 목록 | `[...]` |
| `totalMarketCap` | Long | 산업 전체 시가총액 | `500000000000000` |
| `stockCount` | Int | 종목 수 | `42` |

**설계 의도**:
- 산업별 분석 및 비교 (예: 반도체 vs 자동차)
- 산업 내 시가총액 상위 종목 필터링
- ETF 포트폴리오 구성 참고 데이터

#### 3.2.5. Enum 클래스

**Market (시장 구분)**:
```kotlin
enum class Market(val code: String, val koreanName: String) {
    KOSPI("STK", "코스피"),
    KOSDAQ("KSQ", "코스닥"),
    KONEX("KNX", "코넥스"),
    ALL("ALL", "전체");

    companion object {
        fun fromCode(code: String): Market {
            return entries.find { it.code == code } ?: ALL
        }
    }
}
```

**ListingStatus (상장 상태)**:
```kotlin
enum class ListingStatus {
    LISTED,      // 상장
    DELISTED     // 상폐
}
```

**PriceChangeType (등락 구분)**:
```kotlin
enum class PriceChangeType(val code: String) {
    RISE("1"),       // 상승
    FALL("2"),       // 하락
    UNCHANGED("3");  // 보합

    companion object {
        fun fromCode(code: String): PriceChangeType? {
            return entries.find { it.code == code }
        }
    }
}
```

### 3.3. 헬퍼 함수 명세

#### 3.3.1. StockListItem 확장 함수

```kotlin
fun StockListItem.isKospi(): Boolean = market == Market.KOSPI
fun StockListItem.isKosdaq(): Boolean = market == Market.KOSDAQ
fun StockListItem.isListed(): Boolean = listingStatus == ListingStatus.LISTED
```

#### 3.3.2. StockSectorInfo 확장 함수

```kotlin
fun StockSectorInfo.calculateSectorWeight(totalMarketCap: Long): BigDecimal {
    if (marketCap == null || totalMarketCap == 0L) return BigDecimal.ZERO
    return (marketCap.toBigDecimal() / totalMarketCap.toBigDecimal()) * BigDecimal(100)
}

fun StockSectorInfo.isPriceRising(): Boolean = priceChangeType == PriceChangeType.RISE
```

#### 3.3.3. List<StockSectorInfo> 확장 함수

```kotlin
fun List<StockSectorInfo>.groupByIndustry(): Map<String, List<StockSectorInfo>> {
    return groupBy { it.industry }
}

fun List<StockSectorInfo>.filterByMarketCap(minCap: Long): List<StockSectorInfo> {
    return filter { (it.marketCap ?: 0L) >= minCap }
}

fun List<StockSectorInfo>.sortByMarketCap(descending: Boolean = true): List<StockSectorInfo> {
    return if (descending) {
        sortedByDescending { it.marketCap ?: 0L }
    } else {
        sortedBy { it.marketCap ?: 0L }
    }
}

fun List<StockSectorInfo>.calculateTotalMarketCap(): Long {
    return sumOf { it.marketCap ?: 0L }
}
```

---

## 4. API 레이어 설계

### 4.1. StockApi 인터페이스

#### 4.1.1. 패키지 위치
```
dev.kairoscode.kfc.api.StockApi
```

#### 4.1.2. 메서드 명세

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getStockList` | List<StockListItem> | `market`, `listingStatus` | 시장별 종목 리스트 조회 |
| `getStockInfo` | StockInfo? | `ticker` | 개별 종목 기본정보 조회 |
| `getStockName` | String? | `ticker` | 종목 코드 → 종목명 조회 |
| `getSectorClassifications` | List<StockSectorInfo> | `date`, `market` | 업종분류 현황 조회 |
| `getIndustryGroups` | List<IndustryClassification> | `date`, `market` | 산업별 그룹화 데이터 조회 |
| `searchStocks` | List<StockListItem> | `keyword`, `market` | 종목명/코드 검색 |

#### 4.1.3. 메서드 시그니처

```kotlin
interface StockApi {
    /**
     * 종목 리스트 조회
     *
     * 특정 시장의 모든 종목 목록을 조회합니다.
     *
     * @param market 시장 구분 (기본값: ALL)
     * @param listingStatus 상장 상태 (기본값: LISTED)
     * @return 종목 리스트
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu / finder_listdelisu)
     */
    suspend fun getStockList(
        market: Market = Market.ALL,
        listingStatus: ListingStatus = ListingStatus.LISTED
    ): List<StockListItem>

    /**
     * 종목 기본정보 조회
     *
     * 개별 종목의 상세 메타데이터를 조회합니다.
     * 종목이 존재하지 않으면 null을 반환합니다.
     *
     * @param ticker 종목 코드 (6자리)
     * @return 종목 기본정보, 없으면 null
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu)
     */
    suspend fun getStockInfo(ticker: String): StockInfo?

    /**
     * 종목명 조회
     *
     * 종목 코드로 종목명을 조회합니다.
     *
     * @param ticker 종목 코드 (6자리)
     * @return 종목명, 없으면 null
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu)
     */
    suspend fun getStockName(ticker: String): String?

    /**
     * 업종분류 현황 조회
     *
     * 모든 종목의 산업 분류 및 시가총액 정보를 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @param market 시장 구분 (기본값: ALL)
     * @return 업종분류 현황 목록
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT03901)
     */
    suspend fun getSectorClassifications(
        date: LocalDate = LocalDate.now(),
        market: Market = Market.ALL
    ): List<StockSectorInfo>

    /**
     * 산업별 그룹화 데이터 조회
     *
     * 업종분류 현황을 산업별로 그룹화하여 반환합니다.
     * 각 산업의 전체 시가총액 및 종목 수를 포함합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @param market 시장 구분 (기본값: ALL)
     * @return 산업별 그룹화 데이터 목록
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT03901)
     */
    suspend fun getIndustryGroups(
        date: LocalDate = LocalDate.now(),
        market: Market = Market.ALL
    ): List<IndustryClassification>

    /**
     * 종목 검색
     *
     * 종목명 또는 종목 코드로 종목을 검색합니다.
     * 부분 일치 검색을 지원합니다.
     *
     * **성능 고려사항**:
     * - 현재 구현: 전체 목록 조회 후 클라이언트 측 필터링
     * - 캐싱 적용 시 성능 개선 가능 (Phase 2)
     * - 대안: KRX API의 `searchText` 파라미터 활용 (서버 측 검색)
     *
     * @param keyword 검색 키워드 (종목명 또는 종목 코드)
     * @param market 시장 구분 (기본값: ALL)
     * @return 검색된 종목 목록
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu)
     */
    suspend fun searchStocks(
        keyword: String,
        market: Market = Market.ALL
    ): List<StockListItem>
}
```

### 4.2. KfcClient 통합

기존 `KfcClient`에 `stock` 속성을 추가합니다:

```kotlin
// 현재 KfcClient 구조
class KfcClient internal constructor(
    val funds: FundsApi,
    val price: PriceApi,
    val corp: CorpApi?,            // nullable (API Key 필요)
    val stock: StockApi,           // ⬅️ 추가 (non-nullable, API Key 불필요)
    val financials: FinancialsApi? // nullable (API Key 필요)
)
```

#### 4.2.1. KfcClient.create() 수정

```kotlin
companion object {
    fun create(
        opendartApiKey: String? = null,
        rateLimitingSettings: RateLimitingSettings = RateLimitingSettings()
    ): KfcClient {
        // Rate Limiter 인스턴스 생성
        val krxRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.krx)
        // ... 기존 코드 ...

        // KRX API 기반 - API Key 불필요
        val krxStockApi = KrxStockApiImpl(rateLimiter = krxRateLimiter)
        val stockApi = StockApiImpl(krxStockApi = krxStockApi)

        return KfcClient(
            funds = fundsApi,
            price = priceApi,
            corp = corpApi,
            stock = stockApi,        // ⬅️ 추가 (항상 사용 가능)
            financials = financialsApi
        )
    }
}
```

#### 4.2.2. API Key 불필요

```kotlin
val kfc = KfcClient.create()  // API Key 없이 생성

// stock API는 항상 사용 가능 (KRX API는 공개)
val stocks = kfc.stock.getStockList(market = Market.KOSPI)  // OK
```

### 4.3. 사용 예시

```kotlin
val kfc = KfcClient.create()

// 1. 코스피 전체 종목 리스트 조회
val kospiStocks = kfc.stock.getStockList(market = Market.KOSPI)
println("코스피 상장 종목 수: ${kospiStocks.size}")

// 2. 삼성전자 기본정보 조회
val samsungInfo = kfc.stock.getStockInfo("005930")
println("종목명: ${samsungInfo?.name}")
println("ISIN: ${samsungInfo?.isin}")
println("상장일: ${samsungInfo?.listingDate}")

// 3. 종목명 조회
val name = kfc.stock.getStockName("005930")
println("종목명: $name")  // 출력: 삼성전자

// 4. 업종분류 현황 조회
val sectors = kfc.stock.getSectorClassifications(
    date = LocalDate.of(2024, 12, 4),
    market = Market.KOSPI
)
sectors.forEach { sector ->
    println("${sector.name} - ${sector.industry} - ${sector.marketCap?.formatCurrency()}")
}

// 5. 산업별 그룹화
val industries = kfc.stock.getIndustryGroups()
industries.forEach { industry ->
    println("${industry.industryName}: ${industry.stockCount}개 종목, 시총 ${industry.totalMarketCap}")
}

// 6. 종목 검색
val searchResults = kfc.stock.searchStocks("삼성")
searchResults.forEach { stock ->
    println("${stock.ticker} - ${stock.name}")
}
// 출력:
// 005930 - 삼성전자
// 000810 - 삼성화재
// 028260 - 삼성물산
```

---

## 5. 인프라 레이어 설계

### 5.1. 패키지 구조

기존 ETF/Index와 동일한 구조를 따릅니다:

```
dev.kairoscode.kfc/
└── infrastructure/
    └── krx/
        ├── KrxHttpClient.kt             # 재사용 (기존)
        ├── KrxStockApi.kt               # 내부 KRX API 인터페이스
        ├── KrxStockApiImpl.kt           # KRX API 구현체
        ├── StockApiImpl.kt              # 공개 API 구현체 (위임)
        └── internal/
            ├── KrxApiFields.kt          # 재사용 (기존)
            ├── KrxApiParams.kt          # 재사용 (기존)
            └── HttpExtensions.kt        # 재사용 (기존)
```

### 5.2. KrxStockApiImpl (구현체)

기존 `KrxFundsApiImpl` 패턴을 그대로 따릅니다:

```kotlin
internal class KrxStockApiImpl(
    private val httpClient: KrxHttpClient = KrxHttpClient(),
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.krxDefault())
) : KrxStockApi {

    companion object {
        private const val BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd"

        // BLD 코드 상수
        private const val BLD_LISTED_STOCKS = "dbms/comm/finder/finder_stkisu"
        private const val BLD_DELISTED_STOCKS = "dbms/comm/finder/finder_listdelisu"
        private const val BLD_SECTOR_CLASSIFICATIONS = "dbms/MDC/STAT/standard/MDCSTAT03901"
        private const val BLD_ALL_STOCKS_PRICE = "dbms/MDC/STAT/standard/MDCSTAT01501"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    override suspend fun getStockList(
        market: Market,
        listingStatus: ListingStatus
    ): List<StockListItem> {
        rateLimiter.acquire()
        logger.debug { "Fetching stock list for market: $market, status: $listingStatus" }

        val bld = when (listingStatus) {
            ListingStatus.LISTED -> BLD_LISTED_STOCKS
            ListingStatus.DELISTED -> BLD_DELISTED_STOCKS
        }

        val parameters = mapOf(
            "bld" to bld,
            "mktsel" to market.code,
            "searchText" to "",
            "typeNo" to "0"
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "block1" 필드 추출
        val block1 = response["block1"] as? List<Map<String, Any?>> ?: emptyList()

        return block1.map { raw ->
            StockListItem.fromRaw(raw)
        }.also { logger.debug { "Fetched ${it.size} stocks" } }
    }

    override suspend fun getSectorClassifications(
        date: LocalDate,
        market: Market
    ): List<StockSectorInfo> {
        rateLimiter.acquire()
        logger.debug { "Fetching sector classifications for date: $date, market: $market" }

        val parameters = mapOf(
            "bld" to BLD_SECTOR_CLASSIFICATIONS,
            "trdDd" to date.format(dateFormatter),
            "mktId" to market.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "OutBlock_1" 필드 추출
        val output = response["OutBlock_1"] as? List<Map<String, Any?>> ?: emptyList()

        return output.map { raw ->
            StockSectorInfo.fromRaw(raw, market)
        }.also { logger.debug { "Fetched ${it.size} sector classifications" } }
    }

    // ... 기타 메서드
}
```

### 5.3. StockApiImpl (공개 API 구현체)

```kotlin
internal class StockApiImpl(
    private val krxStockApi: KrxStockApi
) : StockApi {

    override suspend fun getStockList(
        market: Market,
        listingStatus: ListingStatus
    ): List<StockListItem> {
        return krxStockApi.getStockList(market, listingStatus)
    }

    override suspend fun getStockInfo(ticker: String): StockInfo? {
        // ticker로 종목 검색 후 StockInfo 구성
        val allStocks = krxStockApi.getStockList(Market.ALL, ListingStatus.LISTED)
        return allStocks.find { it.ticker == ticker }?.let {
            StockInfo(
                ticker = it.ticker,
                name = it.name,
                fullName = null,
                isin = it.isin,
                market = it.market,
                listingStatus = it.listingStatus,
                listingDate = null,
                sharesOutstanding = null
            )
        }
    }

    override suspend fun getStockName(ticker: String): String? {
        return getStockInfo(ticker)?.name
    }

    override suspend fun getSectorClassifications(
        date: LocalDate,
        market: Market
    ): List<StockSectorInfo> {
        return krxStockApi.getSectorClassifications(date, market)
    }

    override suspend fun getIndustryGroups(
        date: LocalDate,
        market: Market
    ): List<IndustryClassification> {
        val sectors = getSectorClassifications(date, market)

        return sectors.groupBy { it.industry }.map { (industryName, stocks) ->
            IndustryClassification(
                industryName = industryName,
                market = market,
                stocks = stocks,
                totalMarketCap = stocks.sumOf { it.marketCap ?: 0L },
                stockCount = stocks.size
            )
        }
    }

    override suspend fun searchStocks(
        keyword: String,
        market: Market
    ): List<StockListItem> {
        // 방법 1: 클라이언트 측 필터링 (현재 구현)
        // - 장점: 간단, KRX API searchText 파라미터 동작 불안정
        // - 단점: 전체 목록 조회 필요 (캐싱으로 완화 가능)
        val allStocks = krxStockApi.getStockList(market, ListingStatus.LISTED)
        return allStocks.filter {
            it.name.contains(keyword, ignoreCase = true) ||
            it.ticker.contains(keyword, ignoreCase = true)
        }

        // 방법 2: 서버 측 검색 (Phase 2 고려)
        // - KRX API의 searchText 파라미터 활용
        // - 단점: 검색 결과가 불안정할 수 있음
        // return krxStockApi.searchStocks(keyword, market)
    }
}
```

### 5.4. API 필드 확장

기존 `KrxApiFields`에 Stock 관련 필드를 추가합니다 (필요 시):

```kotlin
// KrxApiFields.kt에 추가
object Stock {
    const val FULL_CODE = "full_code"      // ISIN 코드
    const val SHORT_CODE = "short_code"    // 종목 코드
    const val CODE_NAME = "codeName"       // 종목명
    const val MARKET_CODE = "marketCode"   // 시장 코드
    const val MARKET_NAME = "marketName"   // 시장명
}
```

### 5.5. 타입 변환 유틸리티

```kotlin
// String 확장 함수
fun String.toMarket(): Market {
    return when (this) {
        "STK" -> Market.KOSPI
        "KSQ" -> Market.KOSDAQ
        "KNX" -> Market.KONEX
        else -> Market.ALL
    }
}

fun String.toPriceChangeType(): PriceChangeType? {
    return when (this) {
        "1" -> PriceChangeType.RISE
        "2" -> PriceChangeType.FALL
        "3" -> PriceChangeType.UNCHANGED
        else -> null
    }
}

// Map 확장 함수
fun Map<String, Any?>.getString(key: String): String {
    return this[key]?.toString() ?: ""
}
```

### 5.6. 응답 구조 차이 처리

KRX API는 엔드포인트에 따라 응답 구조가 다릅니다:

| API | 응답 필드 |
|-----|---------|
| finder_stkisu | `block1` (List) |
| finder_listdelisu | `block1` (List) |
| MDCSTAT03901 | `OutBlock_1` (List) |
| MDCSTAT01501 | `output` (List) |

**처리 방식**:
```kotlin
// 응답 필드를 유연하게 추출
fun Map<String, Any?>.extractStockData(): List<Map<String, Any?>> {
    return when {
        containsKey("block1") -> this["block1"] as? List<Map<String, Any?>> ?: emptyList()
        containsKey("OutBlock_1") -> this["OutBlock_1"] as? List<Map<String, Any?>> ?: emptyList()
        containsKey("output") -> this["output"] as? List<Map<String, Any?>> ?: emptyList()
        else -> emptyList()
    }
}
```

### 5.7. Rate Limiting

기존 `RateLimiter`를 재사용:

```kotlin
private val rateLimiter: RateLimiter = TokenBucketRateLimiter(
    RateLimitingSettings.krxDefault()
)

// 각 API 호출 전
rateLimiter.acquire()
```

### 5.8. 캐싱 전략 (선택 사항)

종목 리스트 및 기본정보는 변경 빈도가 낮으므로 캐싱 적용 권장:

| 데이터 | TTL | 캐싱 전략 |
|--------|-----|----------|
| 종목 리스트 | 1일 | 메모리 캐시 (로컬) |
| 종목 기본정보 | 1주 | 메모리 캐시 (로컬) |
| 업종분류 현황 | 1시간 | 메모리 캐시 (로컬) |

**구현 방식**:
- Kotlin 표준 라이브러리 사용 (별도 캐싱 라이브러리 불필요)
- `ConcurrentHashMap` + 만료 시간 관리
- 향후 Redis 등 외부 캐시 통합 가능하도록 인터페이스 설계

---

## 6. 구현 우선순위

### Phase 1: 핵심 기능 구현 (MVP)

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 1 | 도메인 모델 | `StockListItem`, `StockInfo`, `StockSectorInfo`, Enum 클래스 | 0.5일 |
| 2 | 인프라 레이어 | `KrxStockApiImpl` (기존 `KrxHttpClient` 재사용) | 1일 |
| 3 | API 레이어 | `StockApi` 인터페이스 및 구현체 | 0.5일 |
| 4 | 단위 테스트 | Mock 기반 테스트 작성 | 1일 |
| **합계** | | | **3일** |

**OTP 제거로 인한 공수 절감**: 2일 → 3일 (예상 공수 대폭 감소)

### Phase 2: 확장 기능

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|----------|----------|
| 5 | 추가 API 메서드 | `getStockInfo`, `getStockName`, `searchStocks`, `getIndustryGroups` | 1일 |
| 6 | 헬퍼 함수 | 확장 함수 추가 (`groupByIndustry`, `filterByMarketCap` 등) | 0.5일 |
| 7 | 캐싱 전략 | 종목 리스트 로컬 캐싱 | 1일 |
| 8 | Live 테스트 | 실제 KRX API 호출 테스트 | 0.5일 |
| 9 | 문서화 | API 문서, 예제 코드 작성 | 0.5일 |
| **합계** | | | **3.5일** |

### Phase 3: 고도화 (향후)

| 항목 | 범위 |
|------|------|
| 상장일 조회 | 별도 API 추가 (필요 시) |
| OPENDART 통합 | 직원 수, 웹사이트, 사업 개요 등 추가 정보 |
| 실시간 검색 | 자동완성 기능 지원 |
| 히스토리 관리 | 상장/상폐 이력 추적 |

---

## 7. 예외 처리

### 7.1. 에러 코드 활용

기존 `ErrorCode`를 최대한 재사용하고, 필요시에만 신규 코드를 추가합니다.

#### 7.1.1. 기존 에러 코드 재사용

| 시나리오 | 기존 에러 코드 | 설명 |
|---------|--------------|------|
| KRX API 오류 | `KRX_API_ERROR(3001)` | 기존 코드 재사용 |
| 파싱 실패 | `JSON_PARSE_ERROR(2001)` | 기존 코드 재사용 |
| 네트워크 오류 | `NETWORK_CONNECTION_FAILED(1001)` | 기존 코드 재사용 |

#### 7.1.2. 신규 에러 코드 (필요시만 추가)

| 코드 | 번대 | 에러 코드 | 메시지 |
|------|------|---------|--------|
| 5005 | 5000번대 (검증) | `INVALID_TICKER` | 종목 코드 형식이 올바르지 않습니다 (6자리 숫자) |

> **참고**: `STOCK_NOT_FOUND`는 null 반환으로 처리하므로 에러 코드 불필요

### 7.2. 예외 처리 시나리오

| 시나리오 | 에러 코드 | 처리 방법 |
|---------|----------|----------|
| 잘못된 종목 코드 형식 | `INVALID_TICKER(5005)` | 6자리 숫자 검증 후 예외 발생 |
| 존재하지 않는 종목 | - | null 반환 (예외 발생 안함) |
| KRX API 응답 오류 | `KRX_API_ERROR(3001)` | 재시도 로직 또는 명확한 에러 메시지 |
| 파싱 실패 | `JSON_PARSE_ERROR(2001)` | 로그 기록 후 예외 발생 |
| 네트워크 에러 | `NETWORK_CONNECTION_FAILED(1001)` | 재시도 로직 (기존 `KrxHttpClient` 사용) |

### 7.3. 사용자 친화적 에러 메시지

```kotlin
try {
    val stocks = kfc.stock.getStockList(market = Market.KOSPI)
} catch (e: KfcException) {
    when (e.errorCode) {
        ErrorCode.NETWORK_CONNECTION_FAILED ->
            println("네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요.")
        ErrorCode.STOCK_DATA_PARSING_ERROR ->
            println("종목 데이터를 처리하는 중 오류가 발생했습니다.")
        else ->
            println("오류: ${e.message}")
    }
}

try {
    val info = kfc.stock.getStockInfo("00593")  // 잘못된 형식
} catch (e: KfcException) {
    when (e.errorCode) {
        ErrorCode.INVALID_TICKER ->
            println("종목 코드는 6자리 숫자여야 합니다.")
        else ->
            println("오류: ${e.message}")
    }
}
```

---

## 8. 참고 자료

### 8.1. 공식 문서

- [KRX 정보데이터시스템](https://data.krx.co.kr)
- [KRX 상장종목 검색](https://data.krx.co.kr/comm/finder/finder_stkisu.jsp)

### 8.2. 오픈소스 라이브러리

- [pykrx](https://github.com/sharebook-kr/pykrx) - Python KRX API 래퍼
- [pykrx 문서](https://github.com/sharebook-kr/pykrx/wiki)

### 8.3. 기술 블로그

- [파이썬으로 주식 데이터 수집하기 (pykrx)](https://wikidocs.net/153861)

### 8.4. 내부 문서

- [네임스페이스 표준](/home/ulalax/project/kairos/kfc/doc/네임스페이스.md)
- [financials 기술명세서](/home/ulalax/project/kairos/kfc/doc/financials-기술명세서.md)
- [KFC README.md](/home/ulalax/project/kairos/kfc/README.md)

### 8.5. 기존 구현체 참고

- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxHttpClient.kt`
- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxFundsApiImpl.kt`

---

## 부록: KRX API 매핑 테이블

### A. 시장 코드 매핑

| KRX 코드 | KFC Enum | 한글명 | 설명 |
|---------|---------|--------|------|
| `STK` | `Market.KOSPI` | 코스피 | 한국거래소 유가증권시장 |
| `KSQ` | `Market.KOSDAQ` | 코스닥 | 코스닥시장 |
| `KNX` | `Market.KONEX` | 코넥스 | 중소기업전용시장 |
| `ALL` | `Market.ALL` | 전체 | 모든 시장 통합 |

### B. 산업 분류 예시

| 산업명 | 코스피 종목 수 (예시) | 대표 종목 |
|-------|-------------------|----------|
| 전기전자 | 80+ | 삼성전자, SK하이닉스, LG전자 |
| 운수장비 | 30+ | 현대차, 기아, 현대모비스 |
| 철강금속 | 40+ | 포스코홀딩스, 현대제철 |
| 화학 | 50+ | LG화학, SK이노베이션 |
| 은행 | 10+ | KB금융, 신한지주, 하나금융지주 |
| 증권 | 20+ | 미래에셋증권, NH투자증권 |
| 보험 | 10+ | 삼성화재, 삼성생명 |
| 건설업 | 30+ | 삼성물산, 현대건설 |
| 유통업 | 20+ | 신세계, 롯데쇼핑 |
| 의약품 | 30+ | 셀트리온, 삼성바이오로직스 |

### C. API 엔드포인트 요약

| 기능 | bld | 요청 파라미터 | 응답 필드 (주요) |
|------|-----|-------------|----------------|
| 상장종목검색 | `finder_stkisu` | `mktsel`, `searchText` | `full_code`, `short_code`, `codeName` |
| 상폐종목검색 | `finder_listdelisu` | `mktsel`, `searchText` | `full_code`, `short_code`, `codeName` |
| 업종분류현황 | `MDCSTAT03901` | `trdDd`, `mktId` | `ISU_SRT_CD`, `IDX_IND_NM`, `MKTCAP` |
| 전종목시세 | `MDCSTAT01501` | `trdDd`, `mktId` | `ISU_SRT_CD`, `TDD_CLSPRC`, `LIST_SHRS` |

### D. HTTP 헤더 매핑

| 헤더 | 값 | 필수 여부 | 용도 |
|------|-----|---------|------|
| `User-Agent` | Mozilla/5.0 (...) | 필수 | 브라우저 흉내 |
| `Accept` | application/json, text/plain, */* | 권장 | JSON 응답 수신 |
| `Accept-Language` | ko-KR,ko;q=0.9 | 선택 | 한국어 응답 |
| `Referer` | http://data.krx.co.kr/ | 필수 | 출처 인증 |
| `Origin` | http://data.krx.co.kr | 권장 | CORS 처리 |

---

## 변경 이력

### v2.0 (2025-12-04)
- **주요 변경**: OTP 기반 인증 제거
- 실제 KRX API 호출 방식 반영 (단순 POST 요청)
- 기존 `KrxHttpClient` 재사용으로 구현 단순화
- pykrx 소스코드 분석 결과 반영
- 예상 공수 감소: 7일 → 3일 (MVP)

### v1.0 (2025-12-04)
- 초기 작성 (OTP 방식 가정, 실제와 불일치)

---

**문서 끝**
