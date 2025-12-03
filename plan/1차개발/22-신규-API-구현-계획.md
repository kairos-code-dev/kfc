# 신규 KRX ETF API 엔드포인트 구현 계획

## 문서 정보

- **작성일**: 2024-12-02
- **버전**: 1.0.0
- **대상 엔드포인트**: MDCSTAT04702, MDCSTAT04703, MDCSTAT04704, MDCSTAT04705
- **프로젝트**: KFC (Kairos Financial Client)

---

## 1. 개요

### 1.1 프로젝트 배경

KFC 프로젝트에서 KRX ETF API 통합을 진행하며, 개별종목 종합정보 페이지와 포트폴리오 조회 기능을 완성하기 위해 4개의 새로운 API 엔드포인트를 발견했습니다. 이 엔드포인트들은 기존 API로는 제공되지 않는 중요한 데이터를 포함하고 있습니다.

### 1.2 신규 엔드포인트 요약

| 엔드포인트 | 이름 | 목적 | 데이터 크기 | 우선순위 |
|----------|------|------|------------|---------|
| **MDCSTAT04702** | ETF 분단위 시세 | 장중 1분 단위 OHLCV (09:00-14:56) | 대용량 (~330+ bars) | 높음 |
| **MDCSTAT04703** | ETF 최근 일별 거래 | 최근 10거래일 시세 요약 | 소규모 (10 records) | 높음 |
| **MDCSTAT04704** | ETF 기본정보 | 상장 후 변경 없는 정적 메타데이터 | 단일 객체 | 높음 |
| **MDCSTAT04705** | PDF 상위 10 종목 | 포트폴리오 상위 10개 구성종목 | 소규모 (10 records) | 중간 |

### 1.3 명명 규칙 정정

프로젝트 초기에 MDCSTAT04701과 MDCSTAT04704의 명명이 혼동되었으나, 다음과 같이 정정되었습니다:

| 엔드포인트 | **정정 전** | **정정 후** | 설명 |
|----------|----------|----------|------|
| MDCSTAT04701 | ComprehensiveEtfInfo | **EtfDetailedInfo** | 거래일 기준 시간 의존 데이터 (가격, NAV, 거래량) |
| MDCSTAT04704 | (없음) | **EtfGeneralInfo** | 상장 후 변경 없는 정적 메타데이터 (종목명, 상장일, 결산월일) |

**이유**:
- `DetailedInfo`는 "상세 정보"로서 가격, NAV, 거래량 등 **시간에 따라 변하는 데이터**를 의미
- `GeneralInfo`는 "기본 정보"로서 상장일, 종목명, 결산월일 등 **상장 후 거의 변하지 않는 정적 메타데이터**를 의미

### 1.4 파일 정리 이력

| 작업 | 정정 전 | 정정 후 | 비고 |
|-----|--------|--------|-----|
| 모델 리네이밍 | `ComprehensiveEtfInfo.kt` | `EtfDetailedInfo.kt` | MDCSTAT04701 모델 |
| API 메서드 리네이밍 | `getComprehensiveInfo()` | `getDetailedInfo()` | EtfApi, KrxEtfApi |
| 중복 파일 삭제 | `EtfDetailInfo.kt` | (삭제됨) | EtfGeneralInfo.kt와 중복 |

### 1.5 구현 스코프

이 계획 문서는 다음 작업을 포함합니다:

1. **Model Layer**: 4개의 새로운 데이터 모델 클래스 생성
2. **API Layer**: KrxEtfApi 및 KrxEtfApiImpl에 4개의 메서드 추가
3. **Domain Layer**: EtfApi 도메인 레벨 공개 API에 래퍼 메서드 추가
4. **Test Layer**: 4개의 LiveTest 클래스 작성 (SmartRecorder 활용)
5. **Documentation**: 기존 명명 규칙 정정 및 문서 업데이트

### 1.6 구현 순서

다음 순서로 구현을 진행합니다:

1. **Phase 1**: Model 클래스 구현 (4개)
2. **Phase 2**: API 메서드 추가 (KrxEtfApi 및 KrxEtfApiImpl)
3. **Phase 3**: 도메인 레벨 래퍼 추가 (EtfApi)
4. **Phase 4**: LiveTest 구현 (4개)
5. **Phase 5**: 검증 및 통합 테스트

---

## 2. 아키텍처 설계

### 2.1 전체 아키텍처 개요

KFC 라이브러리는 계층형 아키텍처를 따릅니다:

```
┌─────────────────────────────────────────────────────────────┐
│                    Public API Layer (EtfApi)                 │
│  - 도메인별 통합 API (KRX + Naver)                           │
│  - 라이브러리 사용자가 직접 사용                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Provider API Layer (KrxEtfApi)                  │
│  - 각 데이터 제공자별 API (KRX, Naver, OpenDart)             │
│  - 라이브러리 사용자가 직접 사용 가능                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           Implementation Layer (KrxEtfApiImpl)               │
│  - HTTP 클라이언트를 통한 실제 API 호출                      │
│  - 응답 파싱 및 모델 변환                                     │
│  - Rate Limiting 처리                                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Model Layer (data class)                    │
│  - 타입 안전 데이터 모델                                      │
│  - BigDecimal 사용 (금융 데이터 정밀도 보장)                 │
│  - fromRaw() 팩토리 메서드                                   │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Model 클래스 설계

#### 2.2.1 EtfIntradayBar (MDCSTAT04702)

**목적**: 장중 1분 단위 OHLCV 데이터

**특징**:
- 09:00 ~ 14:56 시간대 데이터 (약 330+ 데이터 포인트)
- 거래일에만 데이터 존재 (비거래일 빈 응답)
- 대용량 데이터셋 (SmartRecorder Tier 2 전략)

**데이터 모델**:

```kotlin
package dev.kairoscode.kfc.model.krx

import dev.kairoscode.kfc.internal.krx.KrxApiFields
import dev.kairoscode.kfc.util.toKrxInt
import dev.kairoscode.kfc.util.toKrxLong
import dev.kairoscode.kfc.util.toStringSafe
import java.time.LocalDate

/**
 * MDCSTAT04702 - ETF 분단위 시세
 *
 * 장중 1분 단위로 제공되는 OHLCV 데이터입니다.
 * 09:00부터 14:56까지 1분 간격으로 약 330개 이상의 데이터 포인트가 제공됩니다.
 *
 * 주의사항:
 * - 거래일에만 데이터가 제공됩니다 (비거래일은 빈 응답)
 * - 누적 거래량은 해당 시간까지의 누적값입니다
 * - 기준가는 당일 기준가입니다
 *
 * @property time 시간 (HH:MM 형식, 예: "09:00", "14:56")
 * @property closePrice 현재가 (분단위 종가)
 * @property openPrice 시가 (분단위 시가)
 * @property highPrice 고가 (분단위 고가)
 * @property lowPrice 저가 (분단위 저가)
 * @property cumulativeVolume 누적 거래량 (해당 시간까지의 누적)
 * @property basePrice 기준가 (당일 기준가)
 */
data class EtfIntradayBar(
    val time: String,
    val closePrice: Int,
    val openPrice: Int,
    val highPrice: Int,
    val lowPrice: Int,
    val cumulativeVolume: Long,
    val basePrice: Int
) {
    companion object {
        /**
         * KRX API 원시 응답으로부터 EtfIntradayBar 생성
         *
         * @param raw KRX API 응답 Map
         * @return EtfIntradayBar 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): EtfIntradayBar {
            return EtfIntradayBar(
                time = raw["TRD_DD"].toStringSafe(),
                closePrice = raw["TDD_CLSPRC"].toStringSafe().toKrxInt(),
                openPrice = raw["TDD_OPNPRC"].toStringSafe().toKrxInt(),
                highPrice = raw["TDD_HGPRC"].toStringSafe().toKrxInt(),
                lowPrice = raw["TDD_LWPRC"].toStringSafe().toKrxInt(),
                cumulativeVolume = raw["ACC_TRDVOL"].toStringSafe().toKrxLong(),
                basePrice = raw["BAS_PRC"].toStringSafe().toKrxInt()
            )
        }
    }

    /**
     * 시간을 LocalTime으로 변환
     *
     * @return LocalTime 인스턴스
     */
    fun toLocalTime(): java.time.LocalTime {
        val parts = time.split(":")
        return java.time.LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }

    /**
     * 분단위 변동폭 계산
     *
     * @return 변동폭 (closePrice - openPrice)
     */
    fun getPriceRange(): Int = highPrice - lowPrice

    /**
     * 분단위 변동률 계산 (%)
     *
     * @return 변동률
     */
    fun getChangeRate(): Double {
        if (openPrice == 0) return 0.0
        return ((closePrice - openPrice).toDouble() / openPrice) * 100
    }
}
```

**필드 매핑**:

| KRX 필드 | 한글명 | Kotlin 필드 | 타입 | 변환 함수 |
|---------|--------|------------|------|----------|
| TRD_DD | 시간 | time | String | (변환 없음) |
| TDD_CLSPRC | 현재가 | closePrice | Int | toKrxInt() |
| TDD_OPNPRC | 시가 | openPrice | Int | toKrxInt() |
| TDD_HGPRC | 고가 | highPrice | Int | toKrxInt() |
| TDD_LWPRC | 저가 | lowPrice | Int | toKrxInt() |
| ACC_TRDVOL | 누적거래량 | cumulativeVolume | Long | toKrxLong() |
| BAS_PRC | 기준가 | basePrice | Int | toKrxInt() |

---

#### 2.2.2 EtfRecentDaily (MDCSTAT04703)

**목적**: 최근 10거래일 시세 요약

**특징**:
- 최근 10거래일 데이터만 제공
- 소규모 데이터셋 (10-20 records)
- 빠른 응답 시간

**데이터 모델**:

```kotlin
package dev.kairoscode.kfc.model.krx

import dev.kairoscode.kfc.internal.krx.KrxApiFields
import dev.kairoscode.kfc.util.*
import java.time.LocalDate

/**
 * MDCSTAT04703 - ETF 최근 일별 거래
 *
 * 최근 10거래일의 일별 시세 요약 데이터입니다.
 *
 * @property tradeDate 거래일
 * @property closePrice 종가
 * @property direction 등락구분 (UP, DOWN, UNCHANGED)
 * @property change 전일대비 (부호 있음)
 * @property changeRate 등락률 (%, 부호 있음)
 * @property volume 거래량
 * @property tradingValue 거래대금 (원)
 */
data class EtfRecentDaily(
    val tradeDate: LocalDate,
    val closePrice: Int,
    val direction: Direction,
    val change: Int,
    val changeRate: Double,
    val volume: Long,
    val tradingValue: Long
) {
    companion object {
        /**
         * KRX API 원시 응답으로부터 EtfRecentDaily 생성
         *
         * @param raw KRX API 응답 Map
         * @return EtfRecentDaily 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): EtfRecentDaily {
            return EtfRecentDaily(
                tradeDate = raw["TRD_DD"].toStringSafe().toKrxDate(),
                closePrice = raw["TDD_CLSPRC"].toStringSafe().toKrxInt(),
                direction = raw["FLUC_TP_CD"].toStringSafe().toKrxDirection(),
                change = raw["CMPPREVDD_PRC"].toStringSafe().toKrxInt(),
                changeRate = raw["FLUC_RT"].toStringSafe().toKrxDouble(),
                volume = raw["ACC_TRDVOL"].toStringSafe().toKrxLong(),
                tradingValue = raw["ACC_TRDVAL"].toStringSafe().toKrxLong()
            )
        }
    }

    /**
     * 상승일인지 확인
     */
    fun isPositive(): Boolean = direction == Direction.UP

    /**
     * 하락일인지 확인
     */
    fun isNegative(): Boolean = direction == Direction.DOWN

    /**
     * 거래대금을 억원 단위로 반환
     */
    fun getTradingValueInBillions(): Double = tradingValue / 100_000_000.0
}
```

**필드 매핑**:

| KRX 필드 | 한글명 | Kotlin 필드 | 타입 | 변환 함수 |
|---------|--------|------------|------|----------|
| TRD_DD | 거래일 | tradeDate | LocalDate | toKrxDate() |
| TDD_CLSPRC | 종가 | closePrice | Int | toKrxInt() |
| FLUC_TP_CD | 등락구분 | direction | Direction | toKrxDirection() |
| CMPPREVDD_PRC | 전일대비 | change | Int | toKrxInt() |
| FLUC_RT | 등락률 | changeRate | Double | toKrxDouble() |
| ACC_TRDVOL | 거래량 | volume | Long | toKrxLong() |
| ACC_TRDVAL | 거래대금 | tradingValue | Long | toKrxLong() |

---

#### 2.2.3 EtfGeneralInfo (MDCSTAT04704) - 기존 클래스 수정

**목적**: 상장 후 변경되지 않는 정적 메타데이터

**중요**: 기존 `EtfGeneralInfo` 클래스를 **완전히 재작성**합니다.

**기존 클래스와의 차이점**:
- 기존: MDCSTAT04701의 일부 필드만 포함
- 신규: MDCSTAT04704의 모든 정적 메타데이터 포함 (종목명, 상장일, 결산월일, 만기일 등)

**데이터 모델**:

```kotlin
package dev.kairoscode.kfc.model.krx

import dev.kairoscode.kfc.internal.krx.KrxApiFields
import dev.kairoscode.kfc.util.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * MDCSTAT04704 - ETF 기본정보
 *
 * ETF의 상장 이후 변경되지 않는 정적 메타데이터입니다.
 * 종목명, 상장일, 결산월일, 만기일, 액면가 등 구조적 정보를 포함합니다.
 *
 * MDCSTAT04701(EtfDetailedInfo)과의 차이점:
 * - EtfDetailedInfo: 거래일 기준 시간 의존 데이터 (가격, NAV, 거래량)
 * - EtfGeneralInfo: 상장 후 거의 변경되지 않는 정적 메타데이터
 *
 * @property name 종목명 (전체명)
 * @property isin 종목코드 (ISIN 12자리)
 * @property ticker 단축코드 (6자리 티커)
 * @property shortName 종목약명
 * @property englishName 영문명
 * @property listingDate 상장일
 * @property totalNetAssets 순자산총액 (원)
 * @property previousNav 전일NAV
 * @property listedShares 상장주식수
 * @property etfType ETF유형코드
 * @property settlementMonthDay 결산월일 (MMDD 형식)
 * @property expirationDate 만기일 (영구형은 null)
 * @property parValue 액면가
 * @property dividendBaseDate 배당기준일 (없으면 null)
 * @property indexValue 기초지수값
 * @property trackingMultiple 추적배수 (1.0 또는 -1.0)
 * @property rightNetAssets 권리NAV총액
 * @property rightNav 권리NAV
 */
data class EtfGeneralInfo(
    val name: String,
    val isin: String,
    val ticker: String,
    val shortName: String,
    val englishName: String,
    val listingDate: LocalDate,
    val totalNetAssets: Long,
    val previousNav: BigDecimal,
    val listedShares: Long,
    val etfType: String,
    val settlementMonthDay: String,
    val expirationDate: LocalDate?,
    val parValue: Int,
    val dividendBaseDate: LocalDate?,
    val indexValue: BigDecimal,
    val trackingMultiple: BigDecimal,
    val rightNetAssets: Long,
    val rightNav: BigDecimal
) {
    companion object {
        /**
         * KRX API 원시 응답으로부터 EtfGeneralInfo 생성
         *
         * @param raw KRX API 응답 Map
         * @return EtfGeneralInfo 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): EtfGeneralInfo {
            return EtfGeneralInfo(
                name = raw["ISU_NM"].toStringSafe(),
                isin = raw["ISU_CD"].toStringSafe(),
                ticker = raw["ISU_SRT_CD"].toStringSafe(),
                shortName = raw["ISU_ABBRV"].toStringSafe(),
                englishName = raw["ISU_ENG_NM"].toStringSafe(),
                listingDate = raw["LIST_DD"].toStringSafe().toKrxDate(),
                totalNetAssets = raw["NETASST_TOTAMT"].toStringSafe().toKrxLong(),
                previousNav = raw["PREVDD_NAV"].toStringSafe().toKrxBigDecimal(),
                listedShares = raw["LIST_SHRS"].toStringSafe().toKrxLong(),
                etfType = raw["ETF_TP_CD"].toStringSafe(),
                settlementMonthDay = raw["SETL_MMDD"].toStringSafe(),
                expirationDate = raw["EXPD_DD"].toStringSafe().let {
                    if (it.isEmpty() || it == "-") null else it.toKrxDate()
                },
                parValue = raw["PAR"].toStringSafe().toKrxInt(),
                dividendBaseDate = raw["ETF_DIVI_DD"].toStringSafe().let {
                    if (it.isEmpty() || it == "-") null else it.toKrxDate()
                },
                indexValue = raw["OBJ_STKPRC_IDX"].toStringSafe().toKrxBigDecimal(),
                trackingMultiple = raw["TRACE_YD_MULT"].toStringSafe().toKrxBigDecimal(),
                rightNetAssets = raw["RGT_NETASST_TOTAMT"].toStringSafe().toKrxLong(),
                rightNav = raw["RGT_NAV"].toStringSafe().toKrxBigDecimal()
            )
        }
    }

    /**
     * 영구형 ETF인지 확인
     */
    fun isPerpetual(): Boolean = expirationDate == null

    /**
     * 레버리지형 ETF인지 확인
     */
    fun isLeveraged(): Boolean = trackingMultiple.abs() > BigDecimal.ONE

    /**
     * 인버스형 ETF인지 확인
     */
    fun isInverse(): Boolean = trackingMultiple < BigDecimal.ZERO
}
```

**필드 매핑**:

| KRX 필드 | 한글명 | Kotlin 필드 | 타입 | 변환 함수 | Nullable |
|---------|--------|------------|------|----------|---------|
| ISU_NM | 종목명 | name | String | (변환 없음) | No |
| ISU_CD | 종목코드 | isin | String | (변환 없음) | No |
| ISU_SRT_CD | 단축코드 | ticker | String | (변환 없음) | No |
| ISU_ABBRV | 종목약명 | shortName | String | (변환 없음) | No |
| ISU_ENG_NM | 영문명 | englishName | String | (변환 없음) | No |
| LIST_DD | 상장일 | listingDate | LocalDate | toKrxDate() | No |
| NETASST_TOTAMT | 순자산총액 | totalNetAssets | Long | toKrxLong() | No |
| PREVDD_NAV | 전일NAV | previousNav | BigDecimal | toKrxBigDecimal() | No |
| LIST_SHRS | 상장주식수 | listedShares | Long | toKrxLong() | No |
| ETF_TP_CD | ETF유형코드 | etfType | String | (변환 없음) | No |
| SETL_MMDD | 결산월일 | settlementMonthDay | String | (변환 없음) | No |
| EXPD_DD | 만기일 | expirationDate | LocalDate? | toKrxDate() | **Yes** |
| PAR | 액면가 | parValue | Int | toKrxInt() | No |
| ETF_DIVI_DD | 배당기준일 | dividendBaseDate | LocalDate? | toKrxDate() | **Yes** |
| OBJ_STKPRC_IDX | 기초지수 | indexValue | BigDecimal | toKrxBigDecimal() | No |
| TRACE_YD_MULT | 추적배수 | trackingMultiple | BigDecimal | toKrxBigDecimal() | No |
| RGT_NETASST_TOTAMT | 권리NAV총액 | rightNetAssets | Long | toKrxLong() | No |
| RGT_NAV | 권리NAV | rightNav | BigDecimal | toKrxBigDecimal() | No |

---

#### 2.2.4 PortfolioTopItem (MDCSTAT04705)

**목적**: 포트폴리오 상위 10개 구성종목

**특징**:
- MDCSTAT05001 (전체 포트폴리오)의 축약 버전
- 소규모 데이터셋 (최대 10 records)
- 빠른 응답 시간 우선

**데이터 모델**:

```kotlin
package dev.kairoscode.kfc.model.krx

import dev.kairoscode.kfc.internal.krx.KrxApiFields
import dev.kairoscode.kfc.util.*
import java.math.BigDecimal

/**
 * MDCSTAT04705 - PDF 상위 10 종목
 *
 * ETF 포트폴리오 구성 종목 중 상위 10개의 요약 정보입니다.
 * 전체 포트폴리오(MDCSTAT05001)보다 빠른 응답을 위해 상위 10개만 제공합니다.
 *
 * @property ticker 구성종목코드 (6자리 티커)
 * @property name 구성종목명
 * @property cuQuantity CU당 수량 (소수점 가능)
 * @property value 현재 가치 (원)
 * @property compositionAmount 구성금액 (원)
 * @property compositionRatio 구성 비중 (백분율)
 */
data class PortfolioTopItem(
    val ticker: String,
    val name: String,
    val cuQuantity: BigDecimal,
    val value: Long,
    val compositionAmount: Long,
    val compositionRatio: BigDecimal
) {
    companion object {
        /**
         * KRX API 원시 응답으로부터 PortfolioTopItem 생성
         *
         * @param raw KRX API 응답 Map
         * @return PortfolioTopItem 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): PortfolioTopItem {
            return PortfolioTopItem(
                ticker = raw["ISU_CD"].toStringSafe(),
                name = raw["ISU_ABBRV"].toStringSafe(),
                cuQuantity = raw["COMPST_ISU_CU1_SHRS"].toStringSafe().toKrxBigDecimal(),
                value = raw["VALU_AMT"].toStringSafe().toKrxLong(),
                compositionAmount = raw["COMPST_AMT"].toStringSafe().toKrxLong(),
                compositionRatio = raw["COMPST_RTO"].toStringSafe().toKrxBigDecimal()
            )
        }
    }

    /**
     * 비중이 5% 이상인지 확인
     */
    fun isSignificantWeight(): Boolean = compositionRatio >= BigDecimal("5.0")

    /**
     * 비중을 소수로 반환 (예: 8.77% -> 0.0877)
     */
    fun getWeightAsDecimal(): BigDecimal = compositionRatio.divide(BigDecimal("100"))
}
```

**필드 매핑**:

| KRX 필드 | 한글명 | Kotlin 필드 | 타입 | 변환 함수 |
|---------|--------|------------|------|----------|
| ISU_CD | 종목코드 | ticker | String | (변환 없음) |
| ISU_ABBRV | 종목명 | name | String | (변환 없음) |
| COMPST_ISU_CU1_SHRS | CU당수량 | cuQuantity | BigDecimal | toKrxBigDecimal() |
| VALU_AMT | 가치 | value | Long | toKrxLong() |
| COMPST_AMT | 구성금액 | compositionAmount | Long | toKrxLong() |
| COMPST_RTO | 비중 | compositionRatio | BigDecimal | toKrxBigDecimal() |

---

### 2.3 API 인터페이스 설계

#### 2.3.1 KrxEtfApi 인터페이스 확장

기존 `/src/main/kotlin/dev/kairoscode/kfc/api/krx/KrxEtfApi.kt`에 다음 메서드를 추가합니다:

```kotlin
// ================================
// 2. ETF 시세 및 OHLCV (기존 섹션에 추가)
// ================================

/**
 * ETF 분단위 시세 조회 (MDCSTAT04702)
 *
 * 장중 1분 단위 OHLCV 데이터를 조회합니다.
 * 09:00부터 14:56까지 약 330개 이상의 minute bar를 제공합니다.
 *
 * **주의사항**:
 * - 거래일에만 데이터가 제공됩니다 (비거래일은 빈 리스트 반환)
 * - 대용량 데이터이므로 적절한 캐싱 권장
 *
 * @param isin ISIN 코드 (예: "KR7152100004")
 * @param tradeDate 거래일 (기본값: 오늘)
 * @return 분단위 시세 목록 (시간순 정렬)
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 */
suspend fun getEtfIntradayBars(
    isin: String,
    tradeDate: LocalDate = LocalDate.now()
): List<EtfIntradayBar>

/**
 * ETF 최근 일별 거래 조회 (MDCSTAT04703)
 *
 * 최근 10거래일의 시세 요약을 조회합니다.
 *
 * @param isin ISIN 코드
 * @param tradeDate 기준 거래일 (기본값: 오늘)
 * @return 최근 일별 거래 목록 (날짜순 정렬, 최대 10개)
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 */
suspend fun getEtfRecentDaily(
    isin: String,
    tradeDate: LocalDate = LocalDate.now()
): List<EtfRecentDaily>

/**
 * ETF 기본정보 조회 (MDCSTAT04704)
 *
 * ETF의 상장 이후 변경되지 않는 정적 메타데이터를 조회합니다.
 * 종목명, 상장일, 결산월일, 만기일, 액면가 등을 포함합니다.
 *
 * @param isin ISIN 코드
 * @param tradeDate 기준 거래일 (기본값: 오늘)
 * @return ETF 기본정보, 데이터가 없으면 null
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 */
suspend fun getEtfGeneralInfo(
    isin: String,
    tradeDate: LocalDate = LocalDate.now()
): EtfGeneralInfo?

// ================================
// 3. ETF 포트폴리오 구성 (기존 섹션에 추가)
// ================================

/**
 * ETF 포트폴리오 상위 10 종목 조회 (MDCSTAT04705)
 *
 * 포트폴리오 구성 종목 중 상위 10개의 요약 정보를 조회합니다.
 * 전체 포트폴리오(getEtfPortfolio)보다 빠른 응답이 필요할 때 사용합니다.
 *
 * **사용 시나리오**:
 * - 빠른 개요 확인: 상위 10개만 조회 (이 메서드)
 * - 정확한 분석/복제: 전체 포트폴리오 조회 (getEtfPortfolio)
 *
 * @param isin ISIN 코드
 * @param date 조회 날짜 (기본값: 오늘)
 * @return 포트폴리오 상위 10 종목 목록
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 */
suspend fun getEtfPortfolioTop10(
    isin: String,
    date: LocalDate = LocalDate.now()
): List<PortfolioTopItem>
```

#### 2.3.2 EtfApi 도메인 레벨 래퍼 추가

기존 `/src/main/kotlin/dev/kairoscode/kfc/api/EtfApi.kt`에 다음 메서드를 추가합니다:

```kotlin
// ================================
// 2. ETF 시세 및 OHLCV (기존 섹션에 추가)
// ================================

/**
 * ETF 분단위 시세 조회
 *
 * 장중 1분 단위 OHLCV 데이터를 조회합니다.
 *
 * @param isin ISIN 코드
 * @param tradeDate 거래일 (기본값: 오늘)
 * @return 분단위 시세 목록
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 * @source KRX API (MDCSTAT04702)
 */
suspend fun getIntradayBars(
    isin: String,
    tradeDate: LocalDate = LocalDate.now()
): List<EtfIntradayBar>

/**
 * ETF 최근 일별 거래 조회
 *
 * 최근 10거래일의 시세 요약을 조회합니다.
 *
 * @param isin ISIN 코드
 * @param tradeDate 기준 거래일 (기본값: 오늘)
 * @return 최근 일별 거래 목록
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 * @source KRX API (MDCSTAT04703)
 */
suspend fun getRecentDaily(
    isin: String,
    tradeDate: LocalDate = LocalDate.now()
): List<EtfRecentDaily>

/**
 * ETF 기본정보 조회
 *
 * 상장 이후 변경되지 않는 정적 메타데이터를 조회합니다.
 *
 * @param isin ISIN 코드
 * @param tradeDate 기준 거래일 (기본값: 오늘)
 * @return ETF 기본정보, 데이터가 없으면 null
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 * @source KRX API (MDCSTAT04704)
 */
suspend fun getGeneralInfo(
    isin: String,
    tradeDate: LocalDate = LocalDate.now()
): EtfGeneralInfo?

// ================================
// 3. ETF 포트폴리오 구성 (기존 섹션에 추가)
// ================================

/**
 * ETF 포트폴리오 상위 10 종목 조회
 *
 * 포트폴리오 구성 종목 중 상위 10개만 조회합니다.
 *
 * @param isin ISIN 코드
 * @param date 조회 날짜 (기본값: 오늘)
 * @return 포트폴리오 상위 10 종목 목록
 * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
 * @source KRX API (MDCSTAT04705)
 */
suspend fun getPortfolioTop10(
    isin: String,
    date: LocalDate = LocalDate.now()
): List<PortfolioTopItem>
```

---

### 2.4 데이터 변환 파이프라인

#### 2.4.1 변환 흐름도

```
┌─────────────────────────────────────────────────────────────┐
│  Step 1: KRX API HTTP 요청                                   │
│  POST http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd│
│  Body: bld=MDCSTAT04702&trdDd=20241202&isuCd=KR7152100004    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 2: JSON 응답 수신                                      │
│  {                                                           │
│    "output": [                                               │
│      {                                                       │
│        "TRD_DD": "09:00",                                    │
│        "TDD_CLSPRC": "30,455",                               │
│        "ACC_TRDVOL": "5,234,000",                            │
│        ...                                                   │
│      }                                                       │
│    ]                                                         │
│  }                                                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 3: 리스트 추출 및 파싱                                 │
│  val rows = (response["output"] as? List<*>) ?: emptyList() │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 4: 각 항목을 fromRaw()로 변환                          │
│  rows.map { EtfIntradayBar.fromRaw(it as Map<*, *>) }       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 5: 타입 안전 모델 반환                                 │
│  List<EtfIntradayBar>                                        │
└─────────────────────────────────────────────────────────────┘
```

#### 2.4.2 변환 함수 활용

모든 데이터 변환은 `/src/main/kotlin/dev/kairoscode/kfc/util/NormalizationExtensions.kt`에 정의된 확장 함수를 사용합니다:

| 원본 타입 | 변환 함수 | 결과 타입 | 용도 |
|---------|----------|---------|------|
| String | `toKrxInt()` | Int | 정수 가격 (분단위 시세) |
| String | `toKrxLong()` | Long | 수량 (거래량, 주식수) |
| String | `toKrxDouble()` | Double | 부동소수점 비율 |
| String | `toKrxBigDecimal()` | BigDecimal | 고정밀 숫자 (NAV, 지수) |
| String | `toKrxDate()` | LocalDate | 날짜 |
| String | `toKrxDirection()` | Direction | 등락구분 |

**참조**: `plan/1차개발/04-데이터-매핑-명세.md`

---

## 3. 구현 단계

### Phase 1: Model 클래스 구현

**소요 예상**: 1단계

**작업 항목**:

1. **EtfIntradayBar.kt 생성**
   - 경로: `/src/main/kotlin/dev/kairoscode/kfc/model/krx/EtfIntradayBar.kt`
   - 작업:
     - Data class 정의
     - `fromRaw()` 팩토리 메서드 구현
     - 유틸리티 메서드 추가 (`toLocalTime()`, `getPriceRange()`, `getChangeRate()`)

2. **EtfRecentDaily.kt 생성**
   - 경로: `/src/main/kotlin/dev/kairoscode/kfc/model/krx/EtfRecentDaily.kt`
   - 작업:
     - Data class 정의
     - `fromRaw()` 팩토리 메서드 구현
     - 유틸리티 메서드 추가 (`isPositive()`, `isNegative()`, `getTradingValueInBillions()`)

3. **EtfGeneralInfo.kt 수정**
   - 경로: `/src/main/kotlin/dev/kairoscode/kfc/model/krx/EtfGeneralInfo.kt`
   - 작업:
     - **기존 클래스 완전 재작성** (MDCSTAT04704 스펙에 맞게)
     - Nullable 필드 처리 (`expirationDate`, `dividendBaseDate`)
     - 유틸리티 메서드 추가 (`isPerpetual()`, `isLeveraged()`, `isInverse()`)

4. **PortfolioTopItem.kt 생성**
   - 경로: `/src/main/kotlin/dev/kairoscode/kfc/model/krx/PortfolioTopItem.kt`
   - 작업:
     - Data class 정의
     - `fromRaw()` 팩토리 메서드 구현
     - 유틸리티 메서드 추가 (`isSignificantWeight()`, `getWeightAsDecimal()`)

**검증**:
- 모든 모델 클래스가 컴파일 되는지 확인
- KDoc 문서가 명확한지 확인
- `fromRaw()` 메서드가 모든 필드를 올바르게 매핑하는지 확인

---

### Phase 2: API 메서드 추가

**소요 예상**: 1-2단계

#### 2.1 KrxEtfApi 인터페이스 확장

**파일**: `/src/main/kotlin/dev/kairoscode/kfc/api/krx/KrxEtfApi.kt`

**작업**:
1. 기존 섹션 2 (ETF 시세 및 OHLCV)에 3개 메서드 추가:
   - `getEtfIntradayBars()`
   - `getEtfRecentDaily()`
   - `getEtfGeneralInfo()`

2. 기존 섹션 3 (ETF 포트폴리오 구성)에 1개 메서드 추가:
   - `getEtfPortfolioTop10()`

3. KDoc 문서 작성:
   - 각 메서드의 목적과 사용 사례 명시
   - 파라미터 설명
   - 반환값 설명
   - 예외 처리 명시

#### 2.2 KrxEtfApiImpl 구현

**파일**: `/src/main/kotlin/dev/kairoscode/kfc/api/krx/KrxEtfApiImpl.kt`

**작업 항목**:

1. **getEtfIntradayBars() 구현**
   ```kotlin
   override suspend fun getEtfIntradayBars(
       isin: String,
       tradeDate: LocalDate
   ): List<EtfIntradayBar> {
       val params = mapOf(
           "trdDd" to tradeDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
           "isuCd" to isin
       )

       val response = krxHttpClient.post(
           bld = "dbms/MDC/STAT/standard/MDCSTAT04702",
           parameters = params
       )

       val rows = (response["output"] as? List<*>) ?: emptyList()
       return rows.mapNotNull { it as? Map<*, *> }
           .map { EtfIntradayBar.fromRaw(it) }
           .sortedBy { it.time }  // 시간순 정렬
   }
   ```

2. **getEtfRecentDaily() 구현**
   ```kotlin
   override suspend fun getEtfRecentDaily(
       isin: String,
       tradeDate: LocalDate
   ): List<EtfRecentDaily> {
       val params = mapOf(
           "trdDd" to tradeDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
           "isuCd" to isin
       )

       val response = krxHttpClient.post(
           bld = "dbms/MDC/STAT/standard/MDCSTAT04703",
           parameters = params
       )

       val rows = (response["output"] as? List<*>) ?: emptyList()
       return rows.mapNotNull { it as? Map<*, *> }
           .map { EtfRecentDaily.fromRaw(it) }
           .sortedBy { it.tradeDate }  // 날짜순 정렬
   }
   ```

3. **getEtfGeneralInfo() 구현**
   ```kotlin
   override suspend fun getEtfGeneralInfo(
       isin: String,
       tradeDate: LocalDate
   ): EtfGeneralInfo? {
       val params = mapOf(
           "trdDd" to tradeDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
           "isuCd" to isin
       )

       val response = krxHttpClient.post(
           bld = "dbms/MDC/STAT/standard/MDCSTAT04704",
           parameters = params
       )

       val rows = (response["output"] as? List<*>) ?: emptyList()
       return rows.mapNotNull { it as? Map<*, *> }
           .firstOrNull()
           ?.let { EtfGeneralInfo.fromRaw(it) }
   }
   ```

4. **getEtfPortfolioTop10() 구현**
   ```kotlin
   override suspend fun getEtfPortfolioTop10(
       isin: String,
       date: LocalDate
   ): List<PortfolioTopItem> {
       val params = mapOf(
           "trdDd" to date.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
           "isuCd" to isin
       )

       val response = krxHttpClient.post(
           bld = "dbms/MDC/STAT/standard/MDCSTAT04705",
           parameters = params
       )

       val rows = (response["output"] as? List<*>) ?: emptyList()
       return rows.mapNotNull { it as? Map<*, *> }
           .filter { (it["VALU_AMT"] as? String)?.toKrxLong() ?: 0 > 0 }  // 가치가 0인 항목 필터링
           .map { PortfolioTopItem.fromRaw(it) }
   }
   ```

**검증**:
- 모든 메서드가 컴파일 되는지 확인
- Rate Limiting이 적용되는지 확인 (KrxHttpClient 내장)
- 예외 처리가 올바른지 확인

---

### Phase 3: 도메인 레벨 래퍼 추가

**소요 예상**: 0.5단계

#### 3.1 EtfApi 인터페이스 확장

**파일**: `/src/main/kotlin/dev/kairoscode/kfc/api/EtfApi.kt`

**작업**:
1. 4개의 도메인 레벨 메서드 추가
2. KDoc에 `@source KRX API (MDCSTAT0470X)` 추가

#### 3.2 EtfApiImpl 구현

**파일**: `/src/main/kotlin/dev/kairoscode/kfc/api/EtfApiImpl.kt`

**작업**:

```kotlin
override suspend fun getIntradayBars(
    isin: String,
    tradeDate: LocalDate
): List<EtfIntradayBar> {
    return krxEtfApi.getEtfIntradayBars(isin, tradeDate)
}

override suspend fun getRecentDaily(
    isin: String,
    tradeDate: LocalDate
): List<EtfRecentDaily> {
    return krxEtfApi.getEtfRecentDaily(isin, tradeDate)
}

override suspend fun getGeneralInfo(
    isin: String,
    tradeDate: LocalDate
): EtfGeneralInfo? {
    return krxEtfApi.getEtfGeneralInfo(isin, tradeDate)
}

override suspend fun getPortfolioTop10(
    isin: String,
    date: LocalDate
): List<PortfolioTopItem> {
    return krxEtfApi.getEtfPortfolioTop10(isin, date)
}
```

**검증**:
- EtfApi를 통한 호출이 정상적으로 작동하는지 확인

---

### Phase 4: LiveTest 구현

**소요 예상**: 2-3단계

#### 4.1 EtfIntradayLiveTest.kt

**파일**: `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfIntradayLiveTest.kt`

```kotlin
package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.KfcClient
import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.SmartRecorder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EtfIntradayLiveTest : LiveTestBase() {

    private val client = KfcClient()

    // 테스트용 고정 날짜 (거래일 확인 필수)
    private val testTradingDate = LocalDate.of(2024, 11, 29)  // 금요일 (거래일)
    private val testNonTradingDate = LocalDate.of(2024, 11, 30)  // 토요일 (비거래일)

    private val testIsin = "KR7069500008"  // KODEX 200
    private val testTicker = "069500"

    @Test
    fun `MDCSTAT04702 - 분단위 시세 조회 (거래일)`() = runBlockingWithRecording {
        val result = assertDoesNotThrow {
            client.etf.getIntradayBars(
                isin = testIsin,
                tradeDate = testTradingDate
            )
        }

        // 검증
        assertNotNull(result, "분단위 시세 데이터는 null이 아니어야 합니다")
        assertTrue(result.isNotEmpty(), "거래일에는 분단위 시세 데이터가 있어야 합니다")

        // 약 330개 정도의 minute bar가 예상됨 (09:00 ~ 14:56)
        assertTrue(result.size >= 300, "분단위 시세는 최소 300개 이상이어야 합니다 (실제: ${result.size})")

        // 시간 순서 검증
        val times = result.map { it.time }
        assertTrue(times.first() == "09:00", "첫 시간은 09:00이어야 합니다")
        assertTrue(times.last() <= "14:56", "마지막 시간은 14:56 이하여야 합니다")

        // 데이터 무결성 검증
        result.forEach { bar ->
            assertTrue(bar.closePrice >= 0, "현재가는 0 이상이어야 합니다")
            assertTrue(bar.cumulativeVolume >= 0, "누적 거래량은 0 이상이어야 합니다")
            assertTrue(bar.highPrice >= bar.lowPrice, "고가는 저가 이상이어야 합니다")
        }

        // 레코딩 (대용량 데이터 - SmartRecorder 사용)
        SmartRecorder.recordSmartly(
            data = result,
            category = RecordingConfig.Paths.Etf.INTRADAY,
            fileName = "etf_intraday_${testTicker}_${testTradingDate}"
        )

        println("✓ 분단위 시세 조회 성공: ${result.size}개 minute bars")
        println("  - 시작 시간: ${result.first().time}")
        println("  - 종료 시간: ${result.last().time}")
        println("  - 마지막 현재가: ${result.last().closePrice}")
    }

    @Test
    fun `MDCSTAT04702 - 분단위 시세 조회 (비거래일)`() = runBlockingWithRecording {
        val result = assertDoesNotThrow {
            client.etf.getIntradayBars(
                isin = testIsin,
                tradeDate = testNonTradingDate
            )
        }

        // 비거래일에는 빈 리스트 반환
        assertTrue(result.isEmpty(), "비거래일에는 분단위 시세 데이터가 없어야 합니다")

        println("✓ 비거래일 분단위 시세 조회 성공: 빈 리스트 반환")
    }

    @Test
    fun `분단위 시세 - 유틸리티 메서드 검증`() = runBlockingWithRecording {
        val result = client.etf.getIntradayBars(testIsin, testTradingDate)

        if (result.isNotEmpty()) {
            val firstBar = result.first()

            // toLocalTime() 검증
            val localTime = assertDoesNotThrow { firstBar.toLocalTime() }
            assertNotNull(localTime)

            // getPriceRange() 검증
            val priceRange = firstBar.getPriceRange()
            assertTrue(priceRange >= 0, "가격 범위는 0 이상이어야 합니다")

            // getChangeRate() 검증
            val changeRate = firstBar.getChangeRate()
            // 변동률은 -10% ~ +10% 범위 내일 것으로 예상
            assertTrue(changeRate >= -10.0 && changeRate <= 10.0,
                "1분 변동률은 -10% ~ +10% 범위 내여야 합니다 (실제: $changeRate%)")

            println("✓ 유틸리티 메서드 검증 성공")
            println("  - LocalTime: $localTime")
            println("  - Price Range: $priceRange")
            println("  - Change Rate: ${String.format("%.2f", changeRate)}%")
        }
    }
}
```

**레코딩 경로 추가**:

`RecordingConfig.kt`에 다음 경로 추가:

```kotlin
object Etf {
    const val INTRADAY = "etf/intraday"
    const val RECENT = "etf/recent"
    const val GENERAL = "etf/general"
    const val PORTFOLIO_TOP = "etf/portfolio_top"
    // ... 기존 경로들
}
```

---

#### 4.2 EtfRecentDailyLiveTest.kt

**파일**: `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfRecentDailyLiveTest.kt`

```kotlin
package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.KfcClient
import dev.kairoscode.kfc.model.krx.Direction
import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EtfRecentDailyLiveTest : LiveTestBase() {

    private val client = KfcClient()

    private val testTradingDate = LocalDate.of(2024, 11, 29)
    private val testIsin = "KR7069500008"  // KODEX 200
    private val testTicker = "069500"

    @Test
    fun `MDCSTAT04703 - 최근 일별 거래 조회`() = runBlockingWithRecording {
        val result = assertDoesNotThrow {
            client.etf.getRecentDaily(
                isin = testIsin,
                tradeDate = testTradingDate
            )
        }

        // 검증
        assertNotNull(result, "최근 일별 거래 데이터는 null이 아니어야 합니다")
        assertTrue(result.isNotEmpty(), "최근 거래일 데이터가 있어야 합니다")
        assertTrue(result.size <= 10, "최근 10거래일 데이터만 반환되어야 합니다 (실제: ${result.size})")

        // 날짜 순서 검증 (과거 -> 최근)
        val dates = result.map { it.tradeDate }
        assertTrue(dates == dates.sorted(), "날짜는 오름차순으로 정렬되어야 합니다")

        // 데이터 무결성 검증
        result.forEach { daily ->
            assertTrue(daily.closePrice >= 0, "종가는 0 이상이어야 합니다")
            assertTrue(daily.volume >= 0, "거래량은 0 이상이어야 합니다")
            assertTrue(daily.tradingValue >= 0, "거래대금은 0 이상이어야 합니다")
            assertTrue(daily.direction in listOf(Direction.UP, Direction.DOWN, Direction.UNCHANGED),
                "등락구분은 UP/DOWN/UNCHANGED 중 하나여야 합니다")
        }

        // 레코딩 (소규모 데이터 - ResponseRecorder 사용)
        ResponseRecorder.recordList(
            data = result,
            category = RecordingConfig.Paths.Etf.RECENT,
            fileName = "etf_recent_${testTicker}_${testTradingDate}"
        )

        println("✓ 최근 일별 거래 조회 성공: ${result.size}개 거래일")
        println("  - 기간: ${result.first().tradeDate} ~ ${result.last().tradeDate}")
        result.takeLast(3).forEach { daily ->
            val sign = when (daily.direction) {
                Direction.UP -> "▲"
                Direction.DOWN -> "▼"
                Direction.UNCHANGED -> "-"
            }
            println("  - ${daily.tradeDate}: ${daily.closePrice} ($sign ${daily.changeRate}%)")
        }
    }

    @Test
    fun `최근 일별 거래 - 유틸리티 메서드 검증`() = runBlockingWithRecording {
        val result = client.etf.getRecentDaily(testIsin, testTradingDate)

        if (result.isNotEmpty()) {
            val latest = result.last()

            // isPositive() / isNegative() 검증
            val isPositive = latest.isPositive()
            val isNegative = latest.isNegative()
            assertTrue(!(isPositive && isNegative), "상승과 하락이 동시에 true일 수 없습니다")

            // getTradingValueInBillions() 검증
            val tradingValueBillions = latest.getTradingValueInBillions()
            assertTrue(tradingValueBillions >= 0, "거래대금(억원)은 0 이상이어야 합니다")

            println("✓ 유틸리티 메서드 검증 성공")
            println("  - Is Positive: $isPositive")
            println("  - Is Negative: $isNegative")
            println("  - Trading Value: ${String.format("%.2f", tradingValueBillions)}억원")
        }
    }
}
```

---

#### 4.3 EtfGeneralInfoLiveTest.kt

**파일**: `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfGeneralInfoLiveTest.kt`

```kotlin
package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.KfcClient
import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EtfGeneralInfoLiveTest : LiveTestBase() {

    private val client = KfcClient()

    private val testTradingDate = LocalDate.of(2024, 11, 29)
    private val testIsin = "KR7069500008"  // KODEX 200 (영구형, 일반)
    private val testTicker = "069500"

    private val testLeveragedIsin = "KR7122630009"  // KODEX 레버리지
    private val testInverseIsin = "KR7114800009"    // KODEX 인버스

    @Test
    fun `MDCSTAT04704 - ETF 기본정보 조회 (일반형)`() = runBlockingWithRecording {
        val result = assertDoesNotThrow {
            client.etf.getGeneralInfo(
                isin = testIsin,
                tradeDate = testTradingDate
            )
        }

        // 검증
        assertNotNull(result, "ETF 기본정보는 null이 아니어야 합니다")

        // 필수 필드 검증
        assertTrue(result.name.isNotEmpty(), "종목명은 비어있지 않아야 합니다")
        assertTrue(result.isin == testIsin, "ISIN이 일치해야 합니다")
        assertTrue(result.ticker == testTicker, "티커가 일치해야 합니다")
        assertTrue(result.listingDate.isBefore(LocalDate.now()), "상장일은 과거여야 합니다")
        assertTrue(result.listedShares > 0, "상장주식수는 0보다 커야 합니다")

        // 결산월일 검증 (MMDD 형식)
        assertTrue(result.settlementMonthDay.matches(Regex("\\d{4}")),
            "결산월일은 MMDD 형식이어야 합니다 (실제: ${result.settlementMonthDay})")

        // 액면가 검증
        assertTrue(result.parValue >= 0, "액면가는 0 이상이어야 합니다")

        // 추적배수 검증 (일반형은 1.0)
        assertTrue(result.trackingMultiple == BigDecimal.ONE,
            "일반형 ETF의 추적배수는 1.0이어야 합니다")

        // 레코딩
        ResponseRecorder.recordSingle(
            data = result,
            category = RecordingConfig.Paths.Etf.GENERAL,
            fileName = "etf_general_${testTicker}_${testTradingDate}"
        )

        println("✓ ETF 기본정보 조회 성공")
        println("  - 종목명: ${result.name}")
        println("  - 상장일: ${result.listingDate}")
        println("  - 결산월일: ${result.settlementMonthDay}")
        println("  - 만기일: ${result.expirationDate ?: "영구형"}")
        println("  - 액면가: ${result.parValue}")
        println("  - 추적배수: ${result.trackingMultiple}")
    }

    @Test
    fun `ETF 기본정보 - 영구형 확인`() = runBlockingWithRecording {
        val result = client.etf.getGeneralInfo(testIsin, testTradingDate)

        assertNotNull(result)
        assertTrue(result.isPerpetual(), "KODEX 200은 영구형이어야 합니다")

        println("✓ 영구형 ETF 확인 성공: expirationDate = ${result.expirationDate}")
    }

    @Test
    fun `ETF 기본정보 - 레버리지형 확인`() = runBlockingWithRecording {
        val result = client.etf.getGeneralInfo(testLeveragedIsin, testTradingDate)

        if (result != null) {
            assertTrue(result.isLeveraged(), "레버리지 ETF는 isLeveraged()가 true여야 합니다")
            assertTrue(result.trackingMultiple > BigDecimal.ONE,
                "레버리지 ETF의 추적배수는 1보다 커야 합니다 (실제: ${result.trackingMultiple})")

            println("✓ 레버리지형 ETF 확인 성공")
            println("  - 종목명: ${result.name}")
            println("  - 추적배수: ${result.trackingMultiple}")
        }
    }

    @Test
    fun `ETF 기본정보 - 인버스형 확인`() = runBlockingWithRecording {
        val result = client.etf.getGeneralInfo(testInverseIsin, testTradingDate)

        if (result != null) {
            assertTrue(result.isInverse(), "인버스 ETF는 isInverse()가 true여야 합니다")
            assertTrue(result.trackingMultiple < BigDecimal.ZERO,
                "인버스 ETF의 추적배수는 음수여야 합니다 (실제: ${result.trackingMultiple})")

            println("✓ 인버스형 ETF 확인 성공")
            println("  - 종목명: ${result.name}")
            println("  - 추적배수: ${result.trackingMultiple}")
        }
    }

    @Test
    fun `ETF 기본정보 - Nullable 필드 처리 검증`() = runBlockingWithRecording {
        val result = client.etf.getGeneralInfo(testIsin, testTradingDate)

        assertNotNull(result)

        // expirationDate는 nullable
        println("  - 만기일: ${result.expirationDate ?: "(영구형)"}")

        // dividendBaseDate는 nullable
        println("  - 배당기준일: ${result.dividendBaseDate ?: "(없음)"}")

        println("✓ Nullable 필드 처리 검증 성공")
    }
}
```

---

#### 4.4 EtfPortfolioTop10LiveTest.kt

**파일**: `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfPortfolioTop10LiveTest.kt`

```kotlin
package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.KfcClient
import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EtfPortfolioTop10LiveTest : LiveTestBase() {

    private val client = KfcClient()

    private val testDate = LocalDate.of(2024, 11, 29)
    private val testIsin = "KR7364980009"  // TIGER 차이나전기차SOLACTIVE (상위 종목이 명확한 테마 ETF)
    private val testTicker = "364980"

    @Test
    fun `MDCSTAT04705 - 포트폴리오 상위 10 종목 조회`() = runBlockingWithRecording {
        val result = assertDoesNotThrow {
            client.etf.getPortfolioTop10(
                isin = testIsin,
                date = testDate
            )
        }

        // 검증
        assertNotNull(result, "포트폴리오 상위 10 종목은 null이 아니어야 합니다")
        assertTrue(result.isNotEmpty(), "포트폴리오 데이터가 있어야 합니다")
        assertTrue(result.size <= 10, "최대 10개 종목만 반환되어야 합니다 (실제: ${result.size})")

        // 데이터 무결성 검증
        result.forEach { item ->
            assertTrue(item.ticker.isNotEmpty(), "종목코드는 비어있지 않아야 합니다")
            assertTrue(item.name.isNotEmpty(), "종목명은 비어있지 않아야 합니다")
            assertTrue(item.cuQuantity >= BigDecimal.ZERO, "CU당 수량은 0 이상이어야 합니다")
            assertTrue(item.value >= 0, "가치는 0 이상이어야 합니다")
            assertTrue(item.compositionRatio >= BigDecimal.ZERO, "비중은 0 이상이어야 합니다")
            assertTrue(item.compositionRatio <= BigDecimal("100"), "비중은 100% 이하여야 합니다")
        }

        // 비중 합계 검증 (상위 10개이므로 100% 미만)
        val totalRatio = result.sumOf { it.compositionRatio }
        assertTrue(totalRatio <= BigDecimal("100"),
            "비중 합계는 100% 이하여야 합니다 (실제: $totalRatio%)")

        // 비중 순서 검증 (내림차순)
        val ratios = result.map { it.compositionRatio }
        assertTrue(ratios == ratios.sortedDescending(),
            "비중은 내림차순으로 정렬되어야 합니다")

        // 레코딩
        ResponseRecorder.recordList(
            data = result,
            category = RecordingConfig.Paths.Etf.PORTFOLIO_TOP,
            fileName = "etf_portfolio_top10_${testTicker}_${testDate}"
        )

        println("✓ 포트폴리오 상위 10 종목 조회 성공: ${result.size}개 종목")
        println("  - 비중 합계: ${String.format("%.2f", totalRatio)}%")
        result.take(3).forEach { item ->
            println("  - ${item.name} (${item.ticker}): ${String.format("%.2f", item.compositionRatio)}%")
        }
    }

    @Test
    fun `포트폴리오 상위 10 - 유틸리티 메서드 검증`() = runBlockingWithRecording {
        val result = client.etf.getPortfolioTop10(testIsin, testDate)

        if (result.isNotEmpty()) {
            val topItem = result.first()

            // isSignificantWeight() 검증 (상위 종목은 5% 이상일 가능성 높음)
            val isSignificant = topItem.isSignificantWeight()
            println("  - Top 1 종목 비중: ${topItem.compositionRatio}% (Significant: $isSignificant)")

            // getWeightAsDecimal() 검증
            val weightDecimal = topItem.getWeightAsDecimal()
            assertTrue(weightDecimal >= BigDecimal.ZERO && weightDecimal <= BigDecimal.ONE,
                "비중(소수)은 0 ~ 1 범위여야 합니다 (실제: $weightDecimal)")

            println("✓ 유틸리티 메서드 검증 성공")
            println("  - Weight Decimal: $weightDecimal")
        }
    }

    @Test
    fun `포트폴리오 상위 10 vs 전체 포트폴리오 비교`() = runBlockingWithRecording {
        // 상위 10 종목 조회
        val top10 = client.etf.getPortfolioTop10(testIsin, testDate)

        // 전체 포트폴리오 조회
        val fullPortfolio = client.etf.getPortfolio(testIsin, testDate)

        if (top10.isNotEmpty() && fullPortfolio.isNotEmpty()) {
            println("✓ 포트폴리오 비교")
            println("  - 상위 10: ${top10.size}개 종목 (비중: ${top10.sumOf { it.compositionRatio }}%)")
            println("  - 전체: ${fullPortfolio.size}개 종목")

            // 상위 10이 전체의 일부인지 확인
            assertTrue(top10.size <= fullPortfolio.size,
                "상위 10은 전체 포트폴리오의 일부여야 합니다")

            // 상위 종목의 비중이 더 큰지 확인
            val top10Ratio = top10.sumOf { it.compositionRatio }
            assertTrue(top10Ratio >= BigDecimal("50"),
                "상위 10개 종목의 비중 합계는 최소 50% 이상이어야 합니다 (실제: $top10Ratio%)")
        }
    }
}
```

---

### Phase 5: 검증 및 통합 테스트

**소요 예상**: 1단계

#### 5.1 검증 항목

1. **컴파일 검증**
   - 모든 새 파일이 오류 없이 컴파일되는지 확인
   - Import 문이 올바른지 확인

2. **LiveTest 실행**
   - 각 LiveTest가 성공적으로 실행되는지 확인
   - 레코딩 파일이 올바르게 생성되는지 확인
   - SmartRecorder가 대용량 데이터를 올바르게 처리하는지 확인

3. **데이터 검증**
   - 레코딩된 JSON 파일 확인
   - 필드 매핑이 올바른지 확인
   - Null 값 처리가 적절한지 확인

4. **Rate Limiting 확인**
   - KrxHttpClient의 Rate Limiting이 정상 작동하는지 확인
   - 429 에러 없이 테스트가 완료되는지 확인

#### 5.2 통합 테스트 시나리오

**파일**: `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfIntegrationLiveTest.kt`

```kotlin
@Test
fun `신규 4개 API 통합 테스트`() = runBlockingWithRecording {
    val isin = "KR7069500008"
    val tradeDate = LocalDate.of(2024, 11, 29)

    // 1. 분단위 시세
    val intradayBars = client.etf.getIntradayBars(isin, tradeDate)
    assertTrue(intradayBars.isNotEmpty(), "분단위 시세 조회 실패")

    // 2. 최근 일별 거래
    val recentDaily = client.etf.getRecentDaily(isin, tradeDate)
    assertTrue(recentDaily.isNotEmpty(), "최근 일별 거래 조회 실패")

    // 3. 기본정보
    val generalInfo = client.etf.getGeneralInfo(isin, tradeDate)
    assertNotNull(generalInfo, "기본정보 조회 실패")

    // 4. 포트폴리오 상위 10
    val portfolioTop10 = client.etf.getPortfolioTop10(isin, tradeDate)
    assertTrue(portfolioTop10.isNotEmpty(), "포트폴리오 상위 10 조회 실패")

    println("✓ 신규 4개 API 통합 테스트 성공")
}
```

---

## 4. 기술 고려사항

### 4.1 대용량 데이터셋 처리

#### 4.1.1 문제점

MDCSTAT04702 (분단위 시세)는 약 330개 이상의 데이터 포인트를 반환하므로 대용량 데이터로 간주됩니다.

#### 4.1.2 해결책: SmartRecorder 활용

SmartRecorder의 3-tier 전략을 활용:

| Tier | 데이터 크기 | 전략 | 파일명 suffix |
|------|------------|------|---------------|
| 1 | <= 10,000 | 전체 레코딩 | (없음) |
| 2 | 10,001 ~ 100,000 | 처음 10,000개만 | `_limited` |
| 3 | > 100,000 | 랜덤 1,000개 샘플 | `_sample` |

분단위 시세의 경우:
- 330개 데이터 포인트 → **Tier 1 (전체 레코딩)** 적용
- 파일명: `etf_intraday_069500_2024-11-29.json`

#### 4.1.3 메모리 최적화

- 대용량 데이터 조회 시 스트리밍 방식 고려 (현재는 일괄 로딩)
- 필요한 경우 페이징 처리 (KRX API가 지원하지 않으므로 클라이언트 측 구현)

---

### 4.2 BigDecimal 사용 원칙

#### 4.2.1 금융 데이터 정밀도 보장

모든 금융 관련 값은 `BigDecimal`을 사용:

**이유**:
1. **정밀도 보장**: 부동소수점 오류 없음
2. **오버플로우 방지**: 임의 정밀도 지원
3. **금융 표준**: 금융 시스템의 업계 표준
4. **감사 추적**: 정확한 계산 결과 보장

#### 4.2.2 타입별 용도

| 데이터 종류 | Kotlin 타입 | 스케일 | 예시 |
|------------|------------|--------|------|
| 정수 가격 (Int) | `Int` | - | 분단위 현재가, 시가 |
| 가격 (Price) | `BigDecimal` | 2 | 종가, NAV |
| 금액 (Amount) | `BigDecimal` 또는 `Long` | 0 또는 - | 시가총액, 거래대금 |
| 비율 (Rate) | `BigDecimal` | 4 | 등락률, 보수율 |
| 수량 (Quantity) | `Long` | - | 거래량, 주식수 |

**참조**: `plan/1차개발/04-데이터-매핑-명세.md`

---

### 4.3 Rate Limiting 준수

#### 4.3.1 KRX API Rate Limiting

KRX API는 다음과 같은 Rate Limiting을 적용합니다:

- **분당 최대 요청 수**: 20개 (예상, 정확한 제한은 미공개)
- **초당 최대 요청 수**: 2개 (예상)

#### 4.3.2 KrxHttpClient의 Rate Limiting

`KrxHttpClient`는 다음 전략을 사용:

1. **Exponential Backoff**: 429 에러 발생 시 지수적 재시도
2. **Request Throttling**: 요청 간 최소 간격 보장 (500ms)
3. **Retry Logic**: 최대 3회 재시도

#### 4.3.3 LiveTest 실행 시 주의사항

- 여러 LiveTest를 동시에 실행하지 않기
- 테스트 간 충분한 대기 시간 확보 (1초 이상)
- 필요한 경우 `delay(1000)` 추가

---

### 4.4 거래일/비거래일 처리

#### 4.4.1 문제점

- 비거래일에는 데이터가 없음 (빈 응답)
- MDCSTAT04702 (분단위 시세)는 거래일에만 데이터 제공

#### 4.4.2 해결책

**LiveTest에서 고정 날짜 사용**:

```kotlin
// 거래일 (확인된 날짜)
private val testTradingDate = LocalDate.of(2024, 11, 29)  // 금요일

// 비거래일 (확인된 날짜)
private val testNonTradingDate = LocalDate.of(2024, 11, 30)  // 토요일
```

**비거래일 테스트**:

```kotlin
@Test
fun `분단위 시세 - 비거래일`() = runBlockingWithRecording {
    val result = client.etf.getIntradayBars(isin, testNonTradingDate)
    assertTrue(result.isEmpty(), "비거래일에는 빈 리스트 반환")
}
```

#### 4.4.3 거래일 캘린더

향후 개선 사항으로 한국 거래일 캘린더 구현 고려:

- KRX 공휴일 정보 API 활용
- 로컬 캐싱
- 거래일/비거래일 자동 판별

---

## 5. 파일 생성 및 수정 목록

### 5.1 새로 생성할 파일

#### Model Layer (4개)

1. `/src/main/kotlin/dev/kairoscode/kfc/model/krx/EtfIntradayBar.kt`
2. `/src/main/kotlin/dev/kairoscode/kfc/model/krx/EtfRecentDaily.kt`
3. `/src/main/kotlin/dev/kairoscode/kfc/model/krx/PortfolioTopItem.kt`

#### Test Layer (4개)

4. `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfIntradayLiveTest.kt`
5. `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfRecentDailyLiveTest.kt`
6. `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfGeneralInfoLiveTest.kt`
7. `/src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfPortfolioTop10LiveTest.kt`

---

### 5.2 수정할 기존 파일

#### Model Layer (1개)

1. **`/src/main/kotlin/dev/kairoscode/kfc/model/krx/EtfGeneralInfo.kt`**
   - **작업**: 전체 재작성 (MDCSTAT04704 스펙에 맞게)
   - **이유**: 기존 클래스는 MDCSTAT04701의 일부 필드만 포함
   - **변경 사항**:
     - 필드 추가: `settlementMonthDay`, `expirationDate`, `parValue`, `dividendBaseDate`, `trackingMultiple`, `rightNetAssets`, `rightNav`
     - Nullable 필드 처리: `expirationDate`, `dividendBaseDate`
     - 유틸리티 메서드 추가: `isPerpetual()`, `isLeveraged()`, `isInverse()`

#### API Layer (4개)

2. **`/src/main/kotlin/dev/kairoscode/kfc/api/krx/KrxEtfApi.kt`**
   - **작업**: 인터페이스에 4개 메서드 추가
   - **메서드**:
     - `getEtfIntradayBars()`
     - `getEtfRecentDaily()`
     - `getEtfGeneralInfo()`
     - `getEtfPortfolioTop10()`

3. **`/src/main/kotlin/dev/kairoscode/kfc/api/krx/KrxEtfApiImpl.kt`**
   - **작업**: 4개 메서드 구현
   - **변경 사항**: HTTP 요청, 응답 파싱, 모델 변환 로직 추가

4. **`/src/main/kotlin/dev/kairoscode/kfc/api/EtfApi.kt`**
   - **작업**: 도메인 레벨 인터페이스에 4개 메서드 추가
   - **메서드**:
     - `getIntradayBars()`
     - `getRecentDaily()`
     - `getGeneralInfo()`
     - `getPortfolioTop10()`

5. **`/src/main/kotlin/dev/kairoscode/kfc/api/EtfApiImpl.kt`**
   - **작업**: 4개 도메인 레벨 래퍼 메서드 구현
   - **변경 사항**: KrxEtfApi 위임 호출 추가

#### Test Utilities (1개)

6. **`/src/liveTest/kotlin/dev/kairoscode/kfc/utils/RecordingConfig.kt`**
   - **작업**: 레코딩 경로 추가
   - **추가 경로**:
     ```kotlin
     object Etf {
         const val INTRADAY = "etf/intraday"
         const val RECENT = "etf/recent"
         const val GENERAL = "etf/general"
         const val PORTFOLIO_TOP = "etf/portfolio_top"
         // ... 기존 경로들
     }
     ```

---

### 5.3 파일 수정 요약

| 파일 경로 | 작업 | 변경 유형 | 우선순위 |
|---------|------|----------|---------|
| `model/krx/EtfIntradayBar.kt` | 신규 생성 | 추가 | 높음 |
| `model/krx/EtfRecentDaily.kt` | 신규 생성 | 추가 | 높음 |
| `model/krx/EtfGeneralInfo.kt` | **전체 재작성** | 수정 | **최우선** |
| `model/krx/PortfolioTopItem.kt` | 신규 생성 | 추가 | 중간 |
| `api/krx/KrxEtfApi.kt` | 인터페이스 확장 | 수정 | 높음 |
| `api/krx/KrxEtfApiImpl.kt` | 구현 추가 | 수정 | 높음 |
| `api/EtfApi.kt` | 인터페이스 확장 | 수정 | 중간 |
| `api/EtfApiImpl.kt` | 구현 추가 | 수정 | 중간 |
| `liveTest/.../EtfIntradayLiveTest.kt` | 신규 생성 | 추가 | 높음 |
| `liveTest/.../EtfRecentDailyLiveTest.kt` | 신규 생성 | 추가 | 높음 |
| `liveTest/.../EtfGeneralInfoLiveTest.kt` | 신규 생성 | 추가 | 높음 |
| `liveTest/.../EtfPortfolioTop10LiveTest.kt` | 신규 생성 | 추가 | 중간 |
| `utils/RecordingConfig.kt` | 경로 추가 | 수정 | 낮음 |

**총 파일 수**: 13개 (신규 7개 + 수정 6개)

---

## 6. 구현 체크리스트

### Phase 1: Model 클래스 구현

- [ ] `EtfIntradayBar.kt` 생성
  - [ ] Data class 정의
  - [ ] `fromRaw()` 팩토리 메서드 구현
  - [ ] 유틸리티 메서드 추가
  - [ ] KDoc 문서 작성
- [ ] `EtfRecentDaily.kt` 생성
  - [ ] Data class 정의
  - [ ] `fromRaw()` 팩토리 메서드 구현
  - [ ] 유틸리티 메서드 추가
  - [ ] KDoc 문서 작성
- [ ] `EtfGeneralInfo.kt` 재작성
  - [ ] 기존 클래스 백업
  - [ ] MDCSTAT04704 스펙에 맞게 전체 재작성
  - [ ] Nullable 필드 처리 (`expirationDate`, `dividendBaseDate`)
  - [ ] 유틸리티 메서드 추가
  - [ ] KDoc 문서 작성
- [ ] `PortfolioTopItem.kt` 생성
  - [ ] Data class 정의
  - [ ] `fromRaw()` 팩토리 메서드 구현
  - [ ] 유틸리티 메서드 추가
  - [ ] KDoc 문서 작성
- [ ] 컴파일 검증
  - [ ] 모든 모델 클래스가 오류 없이 컴파일되는지 확인

### Phase 2: API 메서드 추가

- [ ] `KrxEtfApi.kt` 인터페이스 확장
  - [ ] `getEtfIntradayBars()` 메서드 추가
  - [ ] `getEtfRecentDaily()` 메서드 추가
  - [ ] `getEtfGeneralInfo()` 메서드 추가
  - [ ] `getEtfPortfolioTop10()` 메서드 추가
  - [ ] KDoc 문서 작성
- [ ] `KrxEtfApiImpl.kt` 구현
  - [ ] `getEtfIntradayBars()` 구현
  - [ ] `getEtfRecentDaily()` 구현
  - [ ] `getEtfGeneralInfo()` 구현
  - [ ] `getEtfPortfolioTop10()` 구현
- [ ] 컴파일 검증
  - [ ] 모든 API 메서드가 오류 없이 컴파일되는지 확인

### Phase 3: 도메인 레벨 래퍼 추가

- [ ] `EtfApi.kt` 인터페이스 확장
  - [ ] `getIntradayBars()` 메서드 추가
  - [ ] `getRecentDaily()` 메서드 추가
  - [ ] `getGeneralInfo()` 메서드 추가
  - [ ] `getPortfolioTop10()` 메서드 추가
  - [ ] KDoc 문서 작성 (`@source` 추가)
- [ ] `EtfApiImpl.kt` 구현
  - [ ] 4개 메서드 위임 구현
- [ ] 컴파일 검증

### Phase 4: LiveTest 구현

- [ ] `RecordingConfig.kt` 수정
  - [ ] 레코딩 경로 추가 (`INTRADAY`, `RECENT`, `GENERAL`, `PORTFOLIO_TOP`)
- [ ] `EtfIntradayLiveTest.kt` 작성
  - [ ] 거래일 분단위 시세 조회 테스트
  - [ ] 비거래일 분단위 시세 조회 테스트
  - [ ] 유틸리티 메서드 검증 테스트
  - [ ] SmartRecorder 레코딩
- [ ] `EtfRecentDailyLiveTest.kt` 작성
  - [ ] 최근 일별 거래 조회 테스트
  - [ ] 유틸리티 메서드 검증 테스트
  - [ ] ResponseRecorder 레코딩
- [ ] `EtfGeneralInfoLiveTest.kt` 작성
  - [ ] 일반형 ETF 기본정보 조회 테스트
  - [ ] 영구형 확인 테스트
  - [ ] 레버리지형 확인 테스트
  - [ ] 인버스형 확인 테스트
  - [ ] Nullable 필드 처리 검증 테스트
  - [ ] ResponseRecorder 레코딩
- [ ] `EtfPortfolioTop10LiveTest.kt` 작성
  - [ ] 포트폴리오 상위 10 종목 조회 테스트
  - [ ] 유틸리티 메서드 검증 테스트
  - [ ] 전체 포트폴리오 비교 테스트
  - [ ] ResponseRecorder 레코딩

### Phase 5: 검증 및 통합 테스트

- [ ] LiveTest 실행
  - [ ] EtfIntradayLiveTest 실행 및 검증
  - [ ] EtfRecentDailyLiveTest 실행 및 검증
  - [ ] EtfGeneralInfoLiveTest 실행 및 검증
  - [ ] EtfPortfolioTop10LiveTest 실행 및 검증
- [ ] 레코딩 파일 검증
  - [ ] 분단위 시세 JSON 파일 확인
  - [ ] 최근 일별 거래 JSON 파일 확인
  - [ ] 기본정보 JSON 파일 확인
  - [ ] 포트폴리오 상위 10 JSON 파일 확인
- [ ] 데이터 무결성 검증
  - [ ] 필드 매핑 정확성 확인
  - [ ] Null 값 처리 확인
  - [ ] 타입 변환 정확성 확인
- [ ] Rate Limiting 확인
  - [ ] 429 에러 없이 테스트 완료 확인
- [ ] 통합 테스트 실행
  - [ ] 4개 API 동시 호출 테스트
  - [ ] 전체 시나리오 검증

---

## 7. 위험 요소 및 대응 방안

### 7.1 API 응답 형식 변경

**위험**: KRX API 응답 형식이 문서화된 스펙과 다를 수 있음

**대응**:
1. LiveTest에서 실제 응답 레코딩
2. 레코딩된 JSON과 스펙 비교
3. 차이점 발견 시 모델 클래스 조정

### 7.2 비거래일 데이터 부재

**위험**: 비거래일에 빈 응답으로 인한 테스트 실패

**대응**:
1. LiveTest에서 고정 날짜 사용 (확인된 거래일/비거래일)
2. 비거래일 전용 테스트 케이스 작성
3. 빈 응답 처리 로직 구현

### 7.3 Rate Limiting 초과

**위험**: LiveTest 실행 시 429 에러 발생 가능

**대응**:
1. KrxHttpClient의 Rate Limiting 활용
2. 테스트 간 충분한 대기 시간 확보
3. 필요 시 `delay()` 추가

### 7.4 EtfGeneralInfo 재작성으로 인한 기존 코드 영향

**위험**: EtfGeneralInfo를 사용하는 기존 코드가 깨질 수 있음

**대응**:
1. 기존 클래스 백업
2. 기존 사용처 검색 (Grep)
3. 영향 받는 코드 수정 또는 마이그레이션 가이드 작성

---

## 8. 다음 단계 및 향후 계획

### 8.1 즉시 구현 (이 문서의 스코프)

1. Phase 1: Model 클래스 구현 (4개)
2. Phase 2: API 메서드 추가
3. Phase 3: 도메인 레벨 래퍼 추가
4. Phase 4: LiveTest 구현 (4개)
5. Phase 5: 검증 및 통합 테스트

### 8.2 향후 개선 사항

1. **거래일 캘린더 구현**
   - KRX 공휴일 정보 API 활용
   - 로컬 캐싱
   - 거래일/비거래일 자동 판별

2. **데이터 캐싱 전략**
   - EtfGeneralInfo는 정적 메타데이터이므로 캐싱 적합
   - 캐시 TTL 설정 (예: 1일)
   - 메모리 기반 또는 파일 기반 캐시 구현

3. **성능 최적화**
   - 대용량 데이터 스트리밍 처리
   - 병렬 요청 지원 (여러 ETF 동시 조회)

4. **문서화 보강**
   - 사용 예제 추가
   - 튜토리얼 작성
   - API 레퍼런스 개선

---

## 9. 참조 문서

| 문서명 | 경로 | 내용 |
|-------|------|------|
| **KRX ETF 엔드포인트 분석** | `plan/1차개발/02-KRX-ETF-엔드포인트-분석.md` | 4개 신규 엔드포인트 완전 분석 |
| **데이터 매핑 명세** | `plan/1차개발/04-데이터-매핑-명세.md` | 데이터 타입 변환 규칙 |
| **MDCSTAT04701 상세명세** | `plan/1차개발/03-MDCSTAT04701-상세명세.md` | EtfDetailedInfo 구현 예제 |
| **테스트 전략** | `plan/1차개발/07-테스트-전략.md` | LiveTest 작성 가이드 |
| **API 설계** | `plan/1차개발/06-API-설계.md` | API 인터페이스 설계 원칙 |

---

## 10. 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 1.0.0 | 2024-12-02 | 초안 작성 | Claude (Sonnet 4.5) |

---

## 부록 A: 명명 규칙 정정 매핑 테이블

### A.1 엔드포인트별 명명 매핑

| 엔드포인트 | 이전 명명 | **현재 명명** | 데이터 특성 |
|----------|---------|------------|-----------|
| MDCSTAT04701 | ComprehensiveEtfInfo | **EtfDetailedInfo** | 거래일 기준 시간 의존 데이터 (가격, NAV, 거래량) |
| MDCSTAT04704 | (없음) | **EtfGeneralInfo** | 상장 후 변경 없는 정적 메타데이터 (종목명, 상장일) |

### A.2 용어 정의

- **DetailedInfo** (상세정보): 거래일 기준으로 변하는 동적 데이터 (가격, NAV, 거래량, 52주 고가/저가 등)
- **GeneralInfo** (기본정보): 상장 이후 거의 변하지 않는 정적 메타데이터 (종목명, 상장일, 결산월일, 만기일, 액면가 등)

### A.3 마이그레이션 가이드

기존 코드에서 `ComprehensiveEtfInfo`를 사용하는 경우:

**Before**:
```kotlin
// 기존 코드 (MDCSTAT04701)
val info = client.etf.getComprehensiveInfo(isin)
val assetClass = info?.assetClass
```

**After**:
```kotlin
// 신규 코드 (MDCSTAT04704 전용)
val generalInfo = client.etf.getGeneralInfo(isin)
val settlementMonthDay = generalInfo?.settlementMonthDay
val expirationDate = generalInfo?.expirationDate  // Nullable
```

---

## 부록 B: SmartRecorder 전략 가이드

### B.1 전략 선택 기준

| Tier | 데이터 크기 | 전략 | 파일명 suffix | 적용 API |
|------|------------|------|---------------|---------|
| 1 | <= 10,000 | 전체 레코딩 | (없음) | MDCSTAT04702 (330개) |
| 2 | 10,001 ~ 100,000 | 처음 10,000개만 | `_limited` | - |
| 3 | > 100,000 | 랜덤 1,000개 샘플 | `_sample` | - |

### B.2 사용 예제

```kotlin
// 분단위 시세 (대용량)
SmartRecorder.recordSmartly(
    data = intradayBars,  // 330개
    category = RecordingConfig.Paths.Etf.INTRADAY,
    fileName = "etf_intraday_069500_2024-11-29"
)
// 결과: etf_intraday_069500_2024-11-29.json (전체 330개)

// 최근 일별 거래 (소규모)
ResponseRecorder.recordList(
    data = recentDaily,  // 10개
    category = RecordingConfig.Paths.Etf.RECENT,
    fileName = "etf_recent_069500_2024-11-29"
)
// 결과: etf_recent_069500_2024-11-29.json (전체 10개)
```

---

**문서 종료**
