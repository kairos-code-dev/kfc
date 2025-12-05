# Financials (재무제표) 네임스페이스 기술명세서

> **작성일**: 2025-12-04
> **버전**: 1.0
> **대상 프로젝트**: KFC (Korea Financial data Collector)

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

기업의 재무 상태 및 실적 정보를 제공하는 `financials` 네임스페이스를 KFC 프로젝트에 추가합니다. 이를 통해 사용자는 한국 상장 기업의 표준화된 재무제표 데이터를 손쉽게 조회할 수 있습니다.

### 1.2. 범위

다음 세 가지 핵심 재무제표를 지원합니다:

| 재무제표 | 영문명 | 설명 |
|---------|--------|------|
| 손익계산서 | Income Statement | 일정 기간 동안의 수익과 비용, 순이익 |
| 재무상태표 | Balance Sheet | 특정 시점의 자산, 부채, 자본 |
| 현금흐름표 | Cash Flow Statement | 일정 기간의 현금 유입과 유출 |

### 1.3. 설계 원칙

1. **도메인 중심 설계**: 데이터 소스가 아닌 재무제표 도메인 기준으로 분류
2. **데이터 소스 독립성**: OPENDART API를 우선 지원하되, 향후 다른 소스 추가 가능하도록 추상화
3. **타입 안전성**: Kotlin의 타입 시스템을 활용한 명시적 타입 변환
4. **IFRS 표준 준수**: 국제회계기준(IFRS)에 따른 계정과목 매핑
5. **기존 인프라 재사용**: 기존 `OpenDartApi`, `OpenDartApiImpl` 확장 방식으로 구현

### 1.4. 주요 제약사항

| 제약 | 설명 |
|------|------|
| **API Key 필수** | OPENDART API Key 필요 (미설정 시 `financials` API 사용 불가) |
| **corp_code 사용** | OPENDART는 종목코드(6자리)가 아닌 `corp_code`(8자리) 사용 |
| **데이터 기간** | 2015년 이후 데이터만 지원 |

---

## 2. 데이터 소스 분석

### 2.1. OPENDART API

#### 2.1.1. API 개요

| 항목 | 내용 |
|------|------|
| **API 명** | 단일회사 전체 재무제표 (`fnlttSinglAcntAll`) |
| **엔드포인트** | `https://opendart.fss.or.kr/api/fnlttSinglAcntAll.json` |
| **인증 방식** | API Key (40자리) |
| **호출 제한** | 일일 20,000건 |
| **지원 기간** | 2015년 이후 |

#### 2.1.2. 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `crtfc_key` | STRING(40) | ✅ | API 인증키 | `xxxxxxxx...` |
| `corp_code` | STRING(8) | ✅ | 고유번호 | `00126380` |
| `bsns_year` | STRING(4) | ✅ | 사업연도 (2015년 이후) | `2024` |
| `reprt_code` | STRING(5) | ✅ | 보고서 코드 | `11011` |
| `fs_div` | STRING(3) | ✅ | 재무제표 구분 | `CFS`, `OFS` |

#### 2.1.3. 보고서 코드 (reprt_code)

| 코드 | 보고서 | 주기 | 설명 |
|------|--------|------|------|
| `11011` | 사업보고서 | 연간 | 감사받은 연간 재무제표 |
| `11012` | 반기보고서 | 반기 | 반기 재무제표 |
| `11013` | 1분기보고서 | 분기 | 1분기 재무제표 |
| `11014` | 3분기보고서 | 분기 | 3분기 재무제표 |

#### 2.1.4. 재무제표 구분 (fs_div)

| 구분 | 설명 | 우선순위 |
|------|------|----------|
| `CFS` | 연결재무제표 (Consolidated Financial Statements) | 높음 ⭐ |
| `OFS` | 재무제표 (Separate Financial Statements) | 낮음 |

> **참고**: 연결재무제표(CFS)가 그룹 전체의 재무 상태를 반영하므로 우선 사용 권장

#### 2.1.5. 응답 데이터 구조

```json
{
  "status": "000",
  "message": "정상",
  "list": [
    {
      "rcept_no": "20190401004781",
      "reprt_code": "11011",
      "bsns_year": "2018",
      "corp_code": "00126380",
      "sj_div": "BS",
      "sj_nm": "재무상태표",
      "account_id": "ifrs-full_CurrentAssets",
      "account_nm": "유동자산",
      "account_detail": "-",
      "thstrm_nm": "제 31 기",
      "thstrm_amount": "40748180399558",
      "thstrm_add_amount": "",
      "frmtrm_nm": "제 30 기",
      "frmtrm_amount": "33783888494924",
      "frmtrm_q_nm": "",
      "frmtrm_q_amount": "",
      "frmtrm_add_amount": "",
      "bfefrmtrm_nm": "제 29 기",
      "bfefrmtrm_amount": "28462291338166",
      "ord": "1"
    }
  ]
}
```

#### 2.1.6. 응답 필드 명세

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `rcept_no` | STRING | 접수번호 | `20190401004781` |
| `reprt_code` | STRING | 보고서 코드 | `11011` |
| `bsns_year` | STRING | 사업연도 | `2018` |
| `corp_code` | STRING | 고유번호 | `00126380` |
| `sj_div` | STRING | 재무제표 구분 코드 | `BS`, `IS`, `CF` |
| `sj_nm` | STRING | 재무제표 구분명 | `재무상태표` |
| `account_id` | STRING | 계정 ID (XBRL 기반) | `ifrs-full_CurrentAssets` |
| `account_nm` | STRING | 계정명 | `유동자산` |
| `account_detail` | STRING | 계정상세 | `-` |
| `thstrm_nm` | STRING | 당기명 | `제 31 기` |
| `thstrm_amount` | STRING | 당기금액 | `40748180399558` |
| `frmtrm_nm` | STRING | 전기명 | `제 30 기` |
| `frmtrm_amount` | STRING | 전기금액 | `33783888494924` |
| `bfefrmtrm_nm` | STRING | 전전기명 | `제 29 기` |
| `bfefrmtrm_amount` | STRING | 전전기금액 | `28462291338166` |
| `ord` | STRING | 정렬순서 | `1` |

#### 2.1.7. 재무제표 구분 코드 (sj_div)

| 코드 | 한글명 | 영문명 | 설명 |
|------|--------|--------|------|
| `BS` | 재무상태표 | Balance Sheet | 자산, 부채, 자본 |
| `IS` | 손익계산서 | Income Statement | 수익, 비용, 순이익 |
| `CF` | 현금흐름표 | Cash Flow Statement | 영업/투자/재무 현금흐름 |
| `CIS` | 포괄손익계산서 | Comprehensive Income Statement | 당기순이익 + 기타포괄손익 |
| `SCE` | 자본변동표 | Statement of Changes in Equity | 자본의 변동 내역 |

### 2.2. 데이터 소스 비교

| 데이터 소스 | 재무제표 제공 | 비고 |
|------------|--------------|------|
| KRX | ❌ | 시세/거래 데이터만 제공 |
| pykrx | ❌ | KRX 데이터 래퍼 |
| **OPENDART** | ✅ | **유일한 공식 데이터 소스** |

> **결론**: 한국 상장사 재무제표 데이터는 OPENDART API가 유일한 공식 소스입니다.

---

## 3. 도메인 모델 설계

### 3.1. 패키지 구조

```
dev.kairoscode.kfc/
├── domain/
│   └── financials/
│       ├── FinancialStatement.kt        # 재무제표 공통 인터페이스
│       ├── IncomeStatement.kt           # 손익계산서 모델
│       ├── BalanceSheet.kt              # 재무상태표 모델
│       ├── CashFlowStatement.kt         # 현금흐름표 모델
│       ├── FinancialLineItem.kt         # 재무제표 항목 (계정과목)
│       ├── FinancialPeriod.kt           # 재무 기간 (당기/전기/전전기)
│       ├── ReportType.kt                # 보고서 유형 (연간/분기)
│       └── StatementType.kt             # 재무제표 유형 (BS/IS/CF)
```

### 3.2. 핵심 모델 명세

#### 3.2.1. FinancialLineItem (재무제표 항목)

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `accountId` | String | 계정 ID (XBRL) | `ifrs-full_CurrentAssets` |
| `accountName` | String | 계정명 | `유동자산` |
| `accountDetail` | String? | 계정상세 | `-` |
| `currentPeriodAmount` | BigDecimal | 당기 금액 | `40748180399558` |
| `previousPeriodAmount` | BigDecimal? | 전기 금액 | `33783888494924` |
| `previous2PeriodAmount` | BigDecimal? | 전전기 금액 | `28462291338166` |
| `order` | Int | 정렬순서 | `1` |

**설계 의도**:
- `BigDecimal` 사용: 금융 데이터의 정확한 계산을 위해 부동소수점 오류 방지
- `nullable` 타입: 전기/전전기 데이터가 없을 수 있음 (신규 상장사 등)
- `order`: API 응답의 순서를 유지하여 표준 재무제표 포맷 재현

#### 3.2.2. FinancialPeriod (재무 기간)

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `periodName` | String | 기명 | `제 31 기` |
| `fiscalYear` | Int | 회계연도 | `2024` |
| `periodType` | PeriodType | 기간 유형 | `ANNUAL`, `Q1`, `Q2`, `Q3` |

#### 3.2.3. IncomeStatement (손익계산서)

| 필드명 | 타입 | 설명 |
|-------|------|------|
| `corpCode` | String | 법인 고유번호 |
| `reportType` | ReportType | 보고서 유형 (사업/반기/분기) |
| `fiscalYear` | Int | 사업연도 |
| `statementType` | StatementType | 재무제표 구분 (CFS/OFS) |
| `currentPeriod` | FinancialPeriod | 당기 정보 |
| `previousPeriod` | FinancialPeriod? | 전기 정보 |
| `lineItems` | List<FinancialLineItem> | 계정과목 목록 |

**주요 계정과목 (일부)**:
- 매출액 (`Revenue`)
- 매출원가 (`CostOfRevenue`)
- 매출총이익 (`GrossProfit`)
- 판매비와관리비 (`SellingGeneralAndAdministrativeExpense`)
- 영업이익 (`OperatingIncome`)
- 법인세비용차감전순이익 (`PretaxIncome`)
- 당기순이익 (`NetIncome`)

#### 3.2.4. BalanceSheet (재무상태표)

| 필드명 | 타입 | 설명 |
|-------|------|------|
| `corpCode` | String | 법인 고유번호 |
| `reportType` | ReportType | 보고서 유형 |
| `fiscalYear` | Int | 사업연도 |
| `statementType` | StatementType | 재무제표 구분 (CFS/OFS) |
| `currentPeriod` | FinancialPeriod | 당기 정보 |
| `previousPeriod` | FinancialPeriod? | 전기 정보 |
| `lineItems` | List<FinancialLineItem> | 계정과목 목록 |

**주요 계정과목 (일부)**:
- **자산 (Assets)**:
  - 유동자산 (`CurrentAssets`)
  - 비유동자산 (`NoncurrentAssets`)
  - 자산총계 (`TotalAssets`)
- **부채 (Liabilities)**:
  - 유동부채 (`CurrentLiabilities`)
  - 비유동부채 (`NoncurrentLiabilities`)
  - 부채총계 (`TotalLiabilities`)
- **자본 (Equity)**:
  - 자본금 (`ShareCapital`)
  - 이익잉여금 (`RetainedEarnings`)
  - 자본총계 (`TotalEquity`)

#### 3.2.5. CashFlowStatement (현금흐름표)

| 필드명 | 타입 | 설명 |
|-------|------|------|
| `corpCode` | String | 법인 고유번호 |
| `reportType` | ReportType | 보고서 유형 |
| `fiscalYear` | Int | 사업연도 |
| `statementType` | StatementType | 재무제표 구분 (CFS/OFS) |
| `currentPeriod` | FinancialPeriod | 당기 정보 |
| `previousPeriod` | FinancialPeriod? | 전기 정보 |
| `lineItems` | List<FinancialLineItem> | 계정과목 목록 |

**주요 계정과목 (일부)**:
- 영업활동 현금흐름 (`CashFlowFromOperatingActivities`)
- 투자활동 현금흐름 (`CashFlowFromInvestingActivities`)
- 재무활동 현금흐름 (`CashFlowFromFinancingActivities`)
- 현금및현금성자산의증감 (`NetChangeInCash`)

#### 3.2.6. Enum 클래스

**ReportType (보고서 유형)**:
```
enum class ReportType(val code: String, val description: String) {
    ANNUAL("11011", "사업보고서"),
    HALF_YEAR("11012", "반기보고서"),
    Q1("11013", "1분기보고서"),
    Q3("11014", "3분기보고서")
}
```

**StatementType (재무제표 구분)**:
```
enum class StatementType(val code: String, val description: String) {
    CONSOLIDATED("CFS", "연결재무제표"),
    SEPARATE("OFS", "재무제표")
}
```

**FinancialStatementCategory (재무제표 카테고리)**:
```
enum class FinancialStatementCategory(val code: String, val koreanName: String, val englishName: String) {
    BALANCE_SHEET("BS", "재무상태표", "Balance Sheet"),
    INCOME_STATEMENT("IS", "손익계산서", "Income Statement"),
    CASH_FLOW("CF", "현금흐름표", "Cash Flow Statement"),
    COMPREHENSIVE_INCOME("CIS", "포괄손익계산서", "Comprehensive Income Statement"),
    CHANGES_IN_EQUITY("SCE", "자본변동표", "Statement of Changes in Equity")
}
```

### 3.3. 헬퍼 함수 명세

각 재무제표 모델에 다음 확장 함수를 제공합니다:

#### IncomeStatement 확장 함수

| 함수명 | 반환 타입 | 설명 |
|-------|----------|------|
| `getRevenue()` | BigDecimal? | 매출액 조회 |
| `getNetIncome()` | BigDecimal? | 당기순이익 조회 |
| `getOperatingIncome()` | BigDecimal? | 영업이익 조회 |
| `getGrossProfit()` | BigDecimal? | 매출총이익 조회 |
| `calculateGrossMargin()` | BigDecimal? | 매출총이익률 계산 (%) |
| `calculateOperatingMargin()` | BigDecimal? | 영업이익률 계산 (%) |
| `calculateNetMargin()` | BigDecimal? | 순이익률 계산 (%) |

#### BalanceSheet 확장 함수

| 함수명 | 반환 타입 | 설명 |
|-------|----------|------|
| `getTotalAssets()` | BigDecimal? | 자산총계 조회 |
| `getTotalLiabilities()` | BigDecimal? | 부채총계 조회 |
| `getTotalEquity()` | BigDecimal? | 자본총계 조회 |
| `getCurrentAssets()` | BigDecimal? | 유동자산 조회 |
| `getCurrentLiabilities()` | BigDecimal? | 유동부채 조회 |
| `calculateDebtToEquityRatio()` | BigDecimal? | 부채비율 계산 (%) |
| `calculateCurrentRatio()` | BigDecimal? | 유동비율 계산 (%) |

#### CashFlowStatement 확장 함수

| 함수명 | 반환 타입 | 설명 |
|-------|----------|------|
| `getOperatingCashFlow()` | BigDecimal? | 영업활동 현금흐름 조회 |
| `getInvestingCashFlow()` | BigDecimal? | 투자활동 현금흐름 조회 |
| `getFinancingCashFlow()` | BigDecimal? | 재무활동 현금흐름 조회 |
| `calculateFreeCashFlow()` | BigDecimal? | 잉여현금흐름 계산 (영업 - 투자) |

---

## 4. API 레이어 설계

### 4.1. FinancialsApi 인터페이스

#### 4.1.1. 패키지 위치
```
dev.kairoscode.kfc.api.FinancialsApi
```

#### 4.1.2. 메서드 명세

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getIncomeStatement` | IncomeStatement | `corpCode`, `year`, `reportType`, `statementType` | 손익계산서 조회 |
| `getBalanceSheet` | BalanceSheet | `corpCode`, `year`, `reportType`, `statementType` | 재무상태표 조회 |
| `getCashFlowStatement` | CashFlowStatement | `corpCode`, `year`, `reportType`, `statementType` | 현금흐름표 조회 |
| `getAllFinancials` | FinancialStatements | `corpCode`, `year`, `reportType`, `statementType` | 모든 재무제표 일괄 조회 |

#### 4.1.3. 메서드 시그니처 예시

```kotlin
interface FinancialsApi {
    /**
     * 손익계산서 조회
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: ANNUAL)
     * @param statementType 재무제표 구분 (기본값: CONSOLIDATED)
     * @return 손익계산서 데이터
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getIncomeStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): IncomeStatement

    /**
     * 재무상태표 조회
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: ANNUAL)
     * @param statementType 재무제표 구분 (기본값: CONSOLIDATED)
     * @return 재무상태표 데이터
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getBalanceSheet(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): BalanceSheet

    /**
     * 현금흐름표 조회
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: ANNUAL)
     * @param statementType 재무제표 구분 (기본값: CONSOLIDATED)
     * @return 현금흐름표 데이터
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getCashFlowStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): CashFlowStatement

    /**
     * 전체 재무제표 조회
     *
     * 손익계산서, 재무상태표, 현금흐름표를 한 번에 조회합니다.
     * 단일 API 호출로 모든 재무제표를 가져오므로 효율적입니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: ANNUAL)
     * @param statementType 재무제표 구분 (기본값: CONSOLIDATED)
     * @return 전체 재무제표 데이터
     * @throws KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getAllFinancials(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): FinancialStatements
}
```

#### 4.1.4. FinancialStatements (통합 모델)

```kotlin
data class FinancialStatements(
    val corpCode: String,
    val fiscalYear: Int,
    val reportType: ReportType,
    val statementType: StatementType,
    val incomeStatement: IncomeStatement?,
    val balanceSheet: BalanceSheet?,
    val cashFlowStatement: CashFlowStatement?
)
```

### 4.2. KfcClient 통합

기존 `KfcClient`에 `financials` 속성을 추가합니다:

```kotlin
// 현재 KfcClient 구조
class KfcClient internal constructor(
    val funds: FundsApi,
    val price: PriceApi,
    val corp: CorpApi?,            // nullable (API Key 필요)
    val financials: FinancialsApi? // ⬅️ 추가 (nullable, API Key 필요)
)
```

#### 4.2.1. KfcClient.create() 수정

```kotlin
companion object {
    fun create(
        opendartApiKey: String? = null,
        rateLimitingSettings: RateLimitingSettings = RateLimitingSettings()
    ): KfcClient {
        // ... 기존 코드 ...

        // OPENDART API Key가 있을 때만 financials API 생성
        val financialsApi = openDartApi?.let { FinancialsApiImpl(openDartApi = it) }

        return KfcClient(
            funds = fundsApi,
            price = priceApi,
            corp = corpApi,
            financials = financialsApi  // ⬅️ 추가
        )
    }
}
```

#### 4.2.2. API Key 미설정 시 동작

```kotlin
val kfc = KfcClient.create()  // API Key 없음

// financials는 null
kfc.financials?.getIncomeStatement(...)  // 안전 호출 필요

// 또는 명시적 에러 처리
val financials = kfc.financials
    ?: throw IllegalStateException("OPENDART API Key가 필요합니다")
```

### 4.3. 사용 예시

```kotlin
val kfc = KfcClient.create(opendartApiKey = "YOUR_API_KEY")

// financials API 사용 가능 여부 확인
val financialsApi = kfc.financials
    ?: throw IllegalStateException("OPENDART API Key가 필요합니다")

// 1. 삼성전자 연결 손익계산서 조회 (2024년 사업보고서)
val incomeStatement = kfc.financials.getIncomeStatement(
    corpCode = "00126380",
    year = 2024,
    reportType = ReportType.ANNUAL,
    statementType = StatementType.CONSOLIDATED
)

// 2. 주요 지표 조회
val revenue = incomeStatement.getRevenue()
val netIncome = incomeStatement.getNetIncome()
val netMargin = incomeStatement.calculateNetMargin()

// 3. 전체 재무제표 조회 (단일 API 호출)
val financials = kfc.financials.getAllFinancials(
    corpCode = "00126380",
    year = 2024
)

val balanceSheet = financials.balanceSheet
val debtRatio = balanceSheet?.calculateDebtToEquityRatio()
```

---

## 5. 인프라 레이어 설계

### 5.1. 패키지 구조

기존 `OpenDartApi`, `OpenDartApiImpl`을 확장하는 방식으로 구현합니다:

```
dev.kairoscode.kfc/
└── infrastructure/
    └── opendart/
        ├── OpenDartApi.kt                     # 기존 (메서드 추가)
        ├── OpenDartApiImpl.kt                 # 기존 (구현 추가)
        ├── FinancialsApiImpl.kt               # 공개 API 구현체 (위임)
        ├── model/
        │   ├── FinancialStatementRaw.kt       # API 응답 DTO
        │   └── OpenDartAccountMapping.kt      # 계정과목 매핑 테이블
        └── internal/
            └── FinancialStatementParser.kt    # 응답 파서
```

### 5.2. 기존 OpenDartApi 확장

#### 5.2.1. 기존 인터페이스 (참고)

```kotlin
// 현재 OpenDartApi.kt에 존재하는 메서드
interface OpenDartApi {
    suspend fun getCorpCodeList(): List<CorpCode>           // 고유번호 목록
    suspend fun getDividendInfo(...)                         // 배당 정보
    suspend fun getStockSplitInfo(...)                       // 증자/감자
    suspend fun searchDisclosures(...)                       // 공시 검색
}
```

#### 5.2.2. 추가할 메서드

```kotlin
// OpenDartApi.kt에 추가
interface OpenDartApi {
    // ... 기존 메서드 ...

    /**
     * 단일회사 전체 재무제표 조회
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportCode 보고서 코드 (11011: 사업보고서 등)
     * @param fsDiv 재무제표 구분 (CFS: 연결, OFS: 별도)
     * @return 재무제표 원시 데이터
     */
    suspend fun getAllFinancialStatements(
        corpCode: String,
        year: Int,
        reportCode: String = "11011",
        fsDiv: String = "CFS"
    ): List<FinancialStatementRaw>
}
```

### 5.3. corp_code 변환 유틸리티

OPENDART는 종목코드(ticker)가 아닌 `corp_code`를 사용합니다.
기존 `getCorpCodeList()` 메서드를 활용한 변환 방법:

#### 5.3.1. 변환 흐름

```
ticker (6자리)  →  getCorpCodeList()  →  corp_code (8자리)
   005930                조회               00126380
```

#### 5.3.2. 변환 유틸리티 명세

| 함수명 | 반환 타입 | 설명 |
|-------|----------|------|
| `findCorpCodeByTicker` | String? | 종목코드 → corp_code 변환 |
| `findTickerByCorpCode` | String? | corp_code → 종목코드 변환 |
| `getCorpCodeMap` | Map<String, String> | 전체 매핑 맵 (캐싱용) |

#### 5.3.3. 사용 예시

```kotlin
// 종목코드로 재무제표 조회 (내부적으로 corp_code 변환)
val corpCodeList = openDartApi.getCorpCodeList()
val corpCode = corpCodeList.find { it.stockCode == "005930" }?.corpCode
    ?: throw KfcException(ErrorCode.STOCK_NOT_FOUND, "종목을 찾을 수 없습니다")

val financials = openDartApi.getAllFinancialStatements(
    corpCode = corpCode,  // "00126380"
    year = 2024
)
```

### 5.4. FinancialStatementParser

#### 5.3.1. 책임
- JSON 응답을 Kotlin 객체로 파싱
- 데이터 타입 변환 (String → BigDecimal, Int 등)
- null 처리 및 기본값 설정

#### 5.3.2. 주요 메서드

| 메서드명 | 반환 타입 | 설명 |
|---------|----------|------|
| `parseResponse` | List<OpenDartLineItem> | JSON 응답 파싱 |
| `parseAmount` | BigDecimal | 금액 문자열을 BigDecimal로 변환 |
| `validateResponse` | Unit | 응답 상태 코드 검증 |

#### 5.3.3. 응답 상태 코드

| 상태 | 코드 | 메시지 | 처리 |
|------|------|--------|------|
| 정상 | `000` | 정상 | 데이터 반환 |
| 에러 | `010` | 등록되지 않은 키 | `KfcException` 발생 |
| 에러 | `011` | 사용 제한 초과 | `RateLimitExceededException` 발생 |
| 에러 | `013` | 요청 제한 초과 | `RateLimitExceededException` 발생 |
| 에러 | `020` | 필수 파라미터 누락 | `InvalidParameterException` 발생 |
| 에러 | `100` | 데이터 없음 | 빈 리스트 반환 |

### 5.4. OpenDartFinancialsMapper

#### 5.4.1. 책임
- OPENDART 응답을 도메인 모델로 변환
- 계정과목별 분류 (손익계산서/재무상태표/현금흐름표)
- 당기/전기/전전기 데이터 매핑

#### 5.4.2. 주요 메서드

| 메서드명 | 반환 타입 | 설명 |
|---------|----------|------|
| `toIncomeStatement` | IncomeStatement | 손익계산서 모델로 변환 |
| `toBalanceSheet` | BalanceSheet | 재무상태표 모델로 변환 |
| `toCashFlowStatement` | CashFlowStatement | 현금흐름표 모델로 변환 |
| `groupByStatementType` | Map<String, List<...>> | 재무제표 유형별 그룹핑 |
| `createLineItems` | List<FinancialLineItem> | 계정과목 목록 생성 |

### 5.5. OpenDartAccountMapping

#### 5.5.1. 목적
OPENDART의 계정과목명을 표준화된 영문 키로 매핑합니다.

#### 5.5.2. 매핑 테이블 예시

**손익계산서 (Income Statement)**:

| OPENDART 계정명 | 표준 키 | 설명 |
|---------------|---------|------|
| 매출액, 수익(매출액) | `Revenue` | 총 매출 |
| 매출원가 | `CostOfRevenue` | 매출원가 |
| 매출총이익 | `GrossProfit` | 매출총이익 |
| 판매비와관리비 | `SellingGeneralAndAdministrativeExpense` | 판관비 |
| 영업이익(손실) | `OperatingIncome` | 영업이익 |
| 법인세비용차감전순이익(손실) | `PretaxIncome` | 세전이익 |
| 당기순이익(손실) | `NetIncome` | 당기순이익 |

**재무상태표 (Balance Sheet)**:

| OPENDART 계정명 | 표준 키 | 설명 |
|---------------|---------|------|
| 유동자산 | `CurrentAssets` | 유동자산 |
| 비유동자산 | `NoncurrentAssets` | 비유동자산 |
| 자산총계 | `TotalAssets` | 자산총계 |
| 유동부채 | `CurrentLiabilities` | 유동부채 |
| 비유동부채 | `NoncurrentLiabilities` | 비유동부채 |
| 부채총계 | `TotalLiabilities` | 부채총계 |
| 자본금 | `ShareCapital` | 자본금 |
| 이익잉여금 | `RetainedEarnings` | 이익잉여금 |
| 자본총계 | `TotalEquity` | 자본총계 |

**현금흐름표 (Cash Flow Statement)**:

| OPENDART 계정명 | 표준 키 | 설명 |
|---------------|---------|------|
| 영업활동현금흐름 | `CashFlowFromOperatingActivities` | 영업 현금흐름 |
| 투자활동현금흐름 | `CashFlowFromInvestingActivities` | 투자 현금흐름 |
| 재무활동현금흐름 | `CashFlowFromFinancingActivities` | 재무 현금흐름 |
| 현금및현금성자산의증감 | `NetChangeInCash` | 현금 증감 |

#### 5.5.3. 매핑 전략

1. **정확한 매칭 우선**: 계정명이 정확히 일치하면 해당 키 사용
2. **부분 매칭**: 포함 관계 검사 (예: "매출액" 포함 → `Revenue`)
3. **XBRL ID 활용**: `account_id` 필드의 XBRL 표준 ID 활용
4. **폴백**: 매칭 실패 시 원본 계정명 그대로 사용

### 5.6. 타입 변환 유틸리티

#### 5.6.1. 금액 변환

```kotlin
// 예시 명세
fun String.toFinancialAmount(): BigDecimal {
    // "40748180399558" → BigDecimal(40748180399558)
    // "-1234567890" → BigDecimal(-1234567890)
    // "" → BigDecimal.ZERO
}
```

| 입력 | 출력 | 설명 |
|------|------|------|
| `"40748180399558"` | `BigDecimal(40748180399558)` | 정상 변환 |
| `"-1234567890"` | `BigDecimal(-1234567890)` | 음수 처리 |
| `""` | `BigDecimal.ZERO` | 빈 문자열은 0 |
| `"-"` | `BigDecimal.ZERO` | 하이픈은 0 |
| `null` | `BigDecimal.ZERO` | null은 0 |

#### 5.6.2. 연도 변환

```kotlin
fun String.toFiscalYear(): Int {
    // "2024" → 2024
    // "제 31 기" → 파싱 불가 시 예외 또는 기본값
}
```

---

## 6. 구현 우선순위

### Phase 1: 핵심 기능 구현 (MVP)

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 1 | 도메인 모델 | `FinancialLineItem`, `IncomeStatement`, `BalanceSheet`, `CashFlowStatement` | 2일 |
| 2 | 인프라 레이어 | OPENDART API 클라이언트, 파서, 매퍼 | 3일 |
| 3 | API 레이어 | `FinancialsApi` 인터페이스 및 구현체 | 2일 |
| 4 | 계정과목 매핑 | 주요 계정과목 30개 매핑 | 1일 |
| 5 | 단위 테스트 | Mock 기반 테스트 작성 | 2일 |
| **합계** | | | **10일** |

### Phase 2: 확장 기능

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 6 | 헬퍼 함수 | 재무비율 계산 함수 추가 | 1일 |
| 7 | 계정과목 매핑 확장 | 전체 계정과목 100+ 매핑 | 2일 |
| 8 | 캐싱 전략 | 재무제표 데이터 로컬 캐싱 | 2일 |
| 9 | Live 테스트 | 실제 API 호출 테스트 | 1일 |
| 10 | 문서화 | API 문서, 예제 코드 작성 | 1일 |
| **합계** | | | **7일** |

### Phase 3: 고도화 (향후)

| 항목 | 범위 |
|------|------|
| 포괄손익계산서 | CIS (Comprehensive Income Statement) 지원 |
| 자본변동표 | SCE (Statement of Changes in Equity) 지원 |
| 다년도 비교 | 여러 연도 재무제표 비교 분석 |
| 재무비율 확장 | ROE, ROA, ROIC 등 고급 비율 |
| 데이터 시각화 | 차트 생성 지원 (선택적) |

---

## 7. 예외 처리

### 7.1. 에러 코드 활용

기존 `ErrorCode`를 최대한 재사용하고, 필요시에만 신규 코드를 추가합니다.

#### 7.1.1. 기존 에러 코드 재사용

| 시나리오 | 기존 에러 코드 | 설명 |
|---------|--------------|------|
| API 키 오류 | `OPENDART_API_ERROR(3002)` | 기존 코드 재사용 |
| Rate Limit | `RATE_LIMIT_EXCEEDED(4001)` | 기존 코드 재사용 |
| 파싱 실패 | `JSON_PARSE_ERROR(2001)` | 기존 코드 재사용 |
| 필수 필드 누락 | `REQUIRED_FIELD_MISSING(2005)` | 기존 코드 재사용 |

#### 7.1.2. 신규 에러 코드 (필요시만 추가)

| 코드 | 번대 | 에러 코드 | 메시지 |
|------|------|---------|--------|
| 5003 | 5000번대 (검증) | `INVALID_FISCAL_YEAR` | 사업연도가 유효하지 않습니다 (2015년 이후만 지원) |
| 5004 | 5000번대 | `INVALID_CORP_CODE` | 법인 고유번호 형식이 올바르지 않습니다 (8자리) |

> **참고**: 기존 `OPENDART_API_ERROR(3002)`로 대부분의 API 에러를 처리할 수 있으므로, 세분화된 에러 코드 추가는 최소화합니다.

### 7.2. 예외 처리 시나리오

| 시나리오 | 에러 코드 | 처리 방법 |
|---------|----------|----------|
| API 키 미설정 | - | `financials` API가 null (사용 불가) |
| API 키 무효 | `OPENDART_API_ERROR(3002)` | 명확한 에러 메시지 반환 |
| 일일 제한 초과 | `RATE_LIMIT_EXCEEDED(4001)` | Rate Limiter로 사전 방지 |
| 유효하지 않은 연도 | `INVALID_FISCAL_YEAR(5003)` | 2015년 미만 입력 시 예외 발생 |
| 법인코드 형식 오류 | `INVALID_CORP_CODE(5004)` | 8자리 검증 후 예외 발생 |
| 종목코드 → corp_code 변환 실패 | `INVALID_PARAMETER(5002)` | 매핑 실패 시 예외 발생 |
| 데이터 없음 (신규 상장사 등) | 예외 없음 | 빈 리스트 또는 null 반환 |

### 7.3. 사용자 친화적 에러 메시지

```kotlin
try {
    val financials = kfc.financials.getIncomeStatement(
        corpCode = "00126380",
        year = 2014  // 2015년 미만
    )
} catch (e: KfcException) {
    when (e.errorCode) {
        ErrorCode.INVALID_FISCAL_YEAR ->
            println("2015년 이후 데이터만 조회 가능합니다.")
        ErrorCode.OPENDART_RATE_LIMIT_EXCEEDED ->
            println("일일 호출 제한(20,000건)을 초과했습니다. 내일 다시 시도해주세요.")
        else ->
            println("오류: ${e.message}")
    }
}
```

---

## 8. 참고 자료

### 8.1. 공식 문서

- [OPENDART 개발가이드 - 단일회사 전체 재무제표](https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS003&apiId=2019016)
- [OPENDART 오픈API 소개](https://opendart.fss.or.kr/intro/main.do)
- [OPENDART 재무정보 일괄다운로드](https://opendart.fss.or.kr/disclosureinfo/fnltt/dwld/main.do)

### 8.2. 오픈소스 라이브러리

- [dart-fss](https://dart-fss.readthedocs.io/en/latest/dart_fs.html) - Python DART 라이브러리
- [OpenDartReader](https://github.com/FinanceData/OpenDartReader) - Python DART API 도구

### 8.3. 기술 블로그

- [퀀티랩 블로그 - 파이썬으로 DART에서 재무제표 수집하기](https://blog.quantylab.com/2021-03-28-dart_fs.html)
- [데이터로 보는 세상 - DART API 3개년 재무제표 자동 수집](https://lovsun.github.io/quant/2021/08/07/quant-dartfs.html)

### 8.4. 내부 문서

- [네임스페이스 표준](/home/ulalax/project/kairos/kfc/doc/네임스페이스.md)
- [KFC README.md](/home/ulalax/project/kairos/kfc/README.md)

---

## 부록: 주요 계정과목 매핑 테이블

### A. 손익계산서 (Income Statement)

| 순서 | OPENDART 계정명 | 표준 키 | XBRL ID |
|-----|---------------|---------|---------|
| 1 | 매출액 | `Revenue` | `ifrs-full_Revenue` |
| 2 | 매출원가 | `CostOfRevenue` | `ifrs-full_CostOfSales` |
| 3 | 매출총이익 | `GrossProfit` | `ifrs-full_GrossProfit` |
| 4 | 판매비와관리비 | `SellingGeneralAndAdministrativeExpense` | `dart_SellingGeneralAndAdministrativeExpenses` |
| 5 | 영업이익(손실) | `OperatingIncome` | `ifrs-full_ProfitLossFromOperatingActivities` |
| 6 | 금융수익 | `FinancialIncome` | `dart_FinanceIncome` |
| 7 | 금융비용 | `FinancialExpense` | `dart_FinanceCosts` |
| 8 | 법인세비용차감전순이익(손실) | `PretaxIncome` | `ifrs-full_ProfitLossBeforeTax` |
| 9 | 법인세비용 | `IncomeTaxExpense` | `ifrs-full_IncomeTaxExpenseContinuingOperations` |
| 10 | 당기순이익(손실) | `NetIncome` | `ifrs-full_ProfitLoss` |

### B. 재무상태표 (Balance Sheet)

| 순서 | OPENDART 계정명 | 표준 키 | XBRL ID |
|-----|---------------|---------|---------|
| 1 | 유동자산 | `CurrentAssets` | `ifrs-full_CurrentAssets` |
| 2 | 비유동자산 | `NoncurrentAssets` | `ifrs-full_NoncurrentAssets` |
| 3 | 자산총계 | `TotalAssets` | `ifrs-full_Assets` |
| 4 | 유동부채 | `CurrentLiabilities` | `ifrs-full_CurrentLiabilities` |
| 5 | 비유동부채 | `NoncurrentLiabilities` | `ifrs-full_NoncurrentLiabilities` |
| 6 | 부채총계 | `TotalLiabilities` | `ifrs-full_Liabilities` |
| 7 | 자본금 | `ShareCapital` | `ifrs-full_IssuedCapital` |
| 8 | 이익잉여금 | `RetainedEarnings` | `ifrs-full_RetainedEarnings` |
| 9 | 자본총계 | `TotalEquity` | `ifrs-full_Equity` |

### C. 현금흐름표 (Cash Flow Statement)

| 순서 | OPENDART 계정명 | 표준 키 | XBRL ID |
|-----|---------------|---------|---------|
| 1 | 영업활동현금흐름 | `CashFlowFromOperatingActivities` | `ifrs-full_CashFlowsFromUsedInOperatingActivities` |
| 2 | 투자활동현금흐름 | `CashFlowFromInvestingActivities` | `ifrs-full_CashFlowsFromUsedInInvestingActivities` |
| 3 | 재무활동현금흐름 | `CashFlowFromFinancingActivities` | `ifrs-full_CashFlowsFromUsedInFinancingActivities` |
| 4 | 현금및현금성자산의증감 | `NetChangeInCash` | `ifrs-full_IncreaseDecreaseInCashAndCashEquivalents` |

---

**문서 끝**
