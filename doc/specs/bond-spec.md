# Bond (채권 수익률) 네임스페이스 기술명세서

> **작성일**: 2025-12-05
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

> **Note**: BLD 코드는 pykrx 소스 코드 (`pykrx/website/krx/bond/core.py`)를 기준으로 검증되었습니다. MDCSTAT04301/04302는 ETF 코드이므로 채권에는 MDCSTAT11401/11402를 사용합니다.

---

## 1. 개요

### 1.1. 목적

한국 장외 채권시장의 수익률 정보를 제공하는 `bond` 네임스페이스를 KFC 프로젝트에 추가합니다. 이를 통해 사용자는 국고채, 회사채, CD 등 주요 채권의 수익률 및 변동 추이를 표준화된 방식으로 조회할 수 있습니다.

### 1.2. 범위

다음 핵심 기능을 지원합니다:

| 기능 | 설명 | 데이터 소스 |
|------|------|------------|
| 특정일 채권 수익률 조회 | 특정 일자의 전체 채권 수익률 및 대비 | KRX |
| 기간별 채권 수익률 조회 | 특정 채권의 기간별 수익률 추이 | KRX |
| 지원 채권 종류 | 국고채(1/2/3/5/10/20/30년), 국민주택1종5년, 회사채(AA-/BBB-), CD(91일) | KRX |

### 1.3. 설계 원칙

1. **기존 패턴 준수**: ETF/Stock 네임스페이스와 동일한 인프라 구조 사용
2. **도메인 중심 설계**: 채권 수익률 데이터 중심으로 분류
3. **데이터 소스 독립성**: KRX API를 우선 지원하되, 향후 다른 소스 추가 가능하도록 추상화
4. **타입 안전성**: Kotlin의 타입 시스템을 활용한 명시적 타입 변환
5. **단순성**: 채권 수익률은 단순한 데이터 구조이므로 과도한 추상화 지양

### 1.4. 네임스페이스 경계

| 데이터 | 담당 네임스페이스 | 기준 |
|--------|-----------------|------|
| **장외 채권 수익률** | `bond` | 채권시장 금리 데이터 |
| **국채 선물** | - (미구현) | 파생상품 시장 데이터 |
| **회사채 발행 정보** | `corp` | 기업 이벤트 |
| **채권형 ETF 가격** | `price` | ETF 시세 |

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

#### 2.1.2. HTTP 요청 구조

기존 ETF/Stock 구현과 동일한 방식을 사용합니다:

**요청 예시**:
```http
POST http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd
Content-Type: application/x-www-form-urlencoded
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Accept: application/json, text/plain, */*
Accept-Language: ko-KR,ko;q=0.9
Referer: http://data.krx.co.kr/
Origin: http://data.krx.co.kr

bld=dbms/MDC/STAT/standard/MDCSTAT11401&inqTpCd=T&trdDd=20220204
```

**응답 구조**:
```json
{
  "OutBlock_1": [
    {
      "BND_KIND_TP_NM": "국고채 1년",
      "BND_SRTN_YILD": "1.467",
      "DIFF": "0.015"
    },
    {
      "BND_KIND_TP_NM": "국고채 2년",
      "BND_SRTN_YILD": "1.995",
      "DIFF": "0.026"
    }
  ]
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

##### A. 장외채권 수익률 조회 (특정일) - MDCSTAT11401

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT11401` |
| **용도** | 특정 일자의 전체 채권 수익률 조회 |
| **요청 파라미터** | `inqTpCd` (조회 타입: T=전종목), `trdDd` (거래일, YYYYMMDD) |
| **응답 필드** | `BND_TP_NM` (채권종류명), `SRTN_YD` (수익률), `CMPPREVDD_YD` (대비) |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT11401
inqTpCd=T
trdDd=20220204
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "BND_KIND_TP_NM": "국고채 1년",
      "BND_SRTN_YILD": "1.467",
      "DIFF": "0.015"
    },
    {
      "BND_KIND_TP_NM": "국고채 2년",
      "BND_SRTN_YILD": "1.995",
      "DIFF": "0.026"
    },
    {
      "BND_KIND_TP_NM": "국고채 3년",
      "BND_SRTN_YILD": "2.194",
      "DIFF": "0.036"
    },
    {
      "BND_KIND_TP_NM": "국고채 5년",
      "BND_SRTN_YILD": "2.418",
      "DIFF": "0.045"
    },
    {
      "BND_KIND_TP_NM": "국고채 10년",
      "BND_SRTN_YILD": "2.619",
      "DIFF": "0.053"
    },
    {
      "BND_KIND_TP_NM": "국고채 20년",
      "BND_SRTN_YILD": "2.639",
      "DIFF": "0.055"
    },
    {
      "BND_KIND_TP_NM": "국고채 30년",
      "BND_SRTN_YILD": "2.559",
      "DIFF": "0.057"
    },
    {
      "BND_KIND_TP_NM": "국민주택 1종 5년",
      "BND_SRTN_YILD": "2.570",
      "DIFF": "0.048"
    },
    {
      "BND_KIND_TP_NM": "회사채 AA-(무보증 3년)",
      "BND_SRTN_YILD": "2.771",
      "DIFF": "0.038"
    },
    {
      "BND_KIND_TP_NM": "회사채 BBB- (무보증 3년)",
      "BND_SRTN_YILD": "8.637",
      "DIFF": "0.036"
    },
    {
      "BND_KIND_TP_NM": "CD(91일)",
      "BND_SRTN_YILD": "1.500",
      "DIFF": "0.000"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `BND_KIND_TP_NM` | STRING | 채권 종류명 | `국고채 1년` |
| `BND_SRTN_YILD` | STRING | 수익률 (%) | `1.467` |
| `DIFF` | STRING | 전일 대비 변동폭 (bp) | `0.015` |

##### B. 장외채권 수익률 조회 (기간별) - MDCSTAT11402

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT11402` |
| **용도** | 특정 채권의 기간별 수익률 추이 조회 |
| **요청 파라미터** | `inqTpCd` (조회 타입: E=개별추이), `strtDd` (시작일), `endDd` (종료일), `bndKindTpCd` (채권종류코드) |
| **응답 필드** | `TRD_DD` (일자), `SRTN_YD` (수익률), `CMPPREVDD_YD` (대비) |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT11402
inqTpCd=E
strtDd=20220104
endDd=20220204
bndKindTpCd=국고채2년
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "TRD_DD": "2022/01/04",
      "BND_SRTN_YILD": "1.717",
      "DIFF": "0.007"
    },
    {
      "TRD_DD": "2022/01/05",
      "BND_SRTN_YILD": "1.791",
      "DIFF": "0.074"
    },
    {
      "TRD_DD": "2022/01/06",
      "BND_SRTN_YILD": "1.878",
      "DIFF": "0.087"
    },
    {
      "TRD_DD": "2022/01/07",
      "BND_SRTN_YILD": "1.895",
      "DIFF": "0.017"
    },
    {
      "TRD_DD": "2022/01/10",
      "BND_SRTN_YILD": "1.902",
      "DIFF": "0.007"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `TRD_DD` | STRING | 거래일 (YYYY/MM/DD) | `2022/01/04` |
| `BND_SRTN_YILD` | STRING | 수익률 (%) | `1.717` |
| `DIFF` | STRING | 전일 대비 변동폭 (bp) | `0.007` |

#### 2.1.5. 채권 종류 코드 (bndKindTpCd)

| 코드 | 채권명 | 만기 | 설명 |
|------|--------|------|------|
| `국고채1년` | 국고채 1년 | 1년 | 국가가 발행하는 국채 |
| `국고채2년` | 국고채 2년 | 2년 | 국가가 발행하는 국채 |
| `국고채3년` | 국고채 3년 | 3년 | 국가가 발행하는 국채 |
| `국고채5년` | 국고채 5년 | 5년 | 국가가 발행하는 국채 |
| `국고채10년` | 국고채 10년 | 10년 | 국가가 발행하는 국채 (벤치마크) |
| `국고채20년` | 국고채 20년 | 20년 | 국가가 발행하는 국채 |
| `국고채30년` | 국고채 30년 | 30년 | 국가가 발행하는 국채 |
| `국민주택1종5년` | 국민주택 1종 5년 | 5년 | 국민주택채권 |
| `회사채AA` | 회사채 AA-(무보증 3년) | 3년 | 신용등급 AA- 회사채 |
| `회사채BBB` | 회사채 BBB- (무보증 3년) | 3년 | 신용등급 BBB- 회사채 |
| `CD` | CD(91일) | 91일 | 양도성예금증서 |

#### 2.1.6. 기존 시스템과의 통합

`KrxHttpClient`를 재사용하여 구현합니다.

| 컴포넌트 | 역할 |
|---------|------|
| `KrxHttpClient` | HTTP POST 요청 처리 |
| `TokenBucketRateLimiter` | Rate Limiting 적용 |
| `BondApiImpl` | 도메인 모델 변환 및 위임 |

### 2.2. pykrx 라이브러리 참고

pykrx는 KRX API를 Python으로 래핑한 라이브러리로, 다음 함수를 제공합니다:

| pykrx 함수 | pykrx 클래스명 | KRX API bld | 용도 |
|-----------|--------------|------------|------|
| `get_otc_treasury_yields(date)` | 장외채권수익률전종목 | `MDCSTAT11401` | 특정일 전체 채권 수익률 |
| `get_otc_treasury_yields(startDd, endDd, bndKindTpCd)` | 장외채권수익률개별추이 | `MDCSTAT11402` | 특정 채권 기간별 수익률 |

**참고**: KFC에서는 pykrx를 직접 사용하지 않고, 동일한 KRX API를 Kotlin으로 구현합니다.

### 2.3. 데이터 소스 한계 및 대안

| 정보 | KRX 제공 여부 | 대안 |
|------|-------------|------|
| 장외 채권 수익률 | ✅ 제공 | - |
| 국채 선물 가격 | ❌ 미제공 | 별도 파생상품 API 필요 (향후 확장) |
| 회사채 발행 정보 | ❌ 미제공 | OPENDART API (향후 확장) |
| 채권 거래량/거래대금 | ❌ 미제공 | - |

---

## 3. 도메인 모델 설계

### 3.1. 패키지 구조

```
dev.kairoscode.kfc/
├── domain/
│   └── bond/
│       ├── BondYield.kt              # 채권 수익률 모델
│       ├── BondYieldSnapshot.kt      # 특정일 전체 채권 수익률 스냅샷
│       ├── BondType.kt               # 채권 종류 Enum
│       └── YieldCurve.kt             # 수익률 곡선 (옵션)
```

### 3.2. 핵심 모델 명세

#### 3.2.1. BondYield (채권 수익률)

특정 채권의 수익률 정보를 담는 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `date` | LocalDate | 거래일 | `2022-01-04` |
| `bondType` | BondType | 채권 종류 | `BondType.TREASURY_2Y` |
| `yield` | BigDecimal | 수익률 (%) | `1.717` |
| `change` | BigDecimal | 전일 대비 변동폭 (bp) | `0.007` |

**설계 의도**:
- 시계열 데이터 분석에 적합
- 채권 수익률 추이 추적 가능
- 수익률 곡선 생성 시 활용

#### 3.2.2. BondYieldSnapshot (특정일 전체 채권 수익률)

특정 일자의 전체 채권 수익률을 담는 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `date` | LocalDate | 거래일 | `2022-02-04` |
| `yields` | List<BondYieldItem> | 채권별 수익률 목록 | `[...]` |

**BondYieldItem 구조**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `bondType` | BondType | 채권 종류 | `BondType.TREASURY_10Y` |
| `yield` | BigDecimal | 수익률 (%) | `2.619` |
| `change` | BigDecimal | 전일 대비 변동폭 (bp) | `0.053` |

**설계 의도**:
- 특정 시점의 전체 채권 시장 현황 파악
- 수익률 곡선 생성
- 채권 간 스프레드 비교

#### 3.2.3. BondType (채권 종류 Enum)

**Enum 값 정의**:

| Enum 상수 | code | koreanName | maturity | category |
|----------|------|-----------|----------|----------|
| `TREASURY_1Y` | `국고채1년` | 국고채 1년 | 1Y | TREASURY |
| `TREASURY_2Y` | `국고채2년` | 국고채 2년 | 2Y | TREASURY |
| `TREASURY_3Y` | `국고채3년` | 국고채 3년 | 3Y | TREASURY |
| `TREASURY_5Y` | `국고채5년` | 국고채 5년 | 5Y | TREASURY |
| `TREASURY_10Y` | `국고채10년` | 국고채 10년 | 10Y | TREASURY |
| `TREASURY_20Y` | `국고채20년` | 국고채 20년 | 20Y | TREASURY |
| `TREASURY_30Y` | `국고채30년` | 국고채 30년 | 30Y | TREASURY |
| `HOUSING_5Y` | `국민주택1종5년` | 국민주택 1종 5년 | 5Y | SPECIAL |
| `CORPORATE_AA` | `회사채AA` | 회사채 AA-(무보증 3년) | 3Y | CORPORATE |
| `CORPORATE_BBB` | `회사채BBB` | 회사채 BBB- (무보증 3년) | 3Y | CORPORATE |
| `CD_91` | `CD` | CD(91일) | 91D | SHORT_TERM |

**BondCategory Enum 값**:

| Enum 상수 | 설명 |
|----------|------|
| `TREASURY` | 국고채 |
| `SPECIAL` | 특수채 (국민주택채권 등) |
| `CORPORATE` | 회사채 |
| `SHORT_TERM` | 단기 (CD 등) |

**Companion 메서드**:

| 메서드명 | 파라미터 | 반환 타입 | 설명 |
|---------|---------|----------|------|
| `fromCode` | `code: String` | `BondType?` | 코드로 BondType 조회 |
| `fromKoreanName` | `name: String` | `BondType?` | 한글명으로 BondType 조회 |

**설계 의도**:
- 채권 종류를 타입 안전하게 표현
- 채권 카테고리별 그룹화 가능
- 만기 정보 포함으로 수익률 곡선 생성 용이

#### 3.2.4. YieldCurve (수익률 곡선 - 옵션)

국고채 수익률 곡선을 표현하는 모델입니다 (Phase 2 구현).

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `date` | LocalDate | 거래일 | `2022-02-04` |
| `treasuryYields` | Map<String, BigDecimal> | 만기별 수익률 | `{"1Y": 1.467, "10Y": 2.619}` |
| `spread10Y2Y` | BigDecimal | 10년-2년 스프레드 | `0.624` |

**설계 의도**:
- 수익률 곡선 시각화 지원
- 장단기 금리 스프레드 분석
- 경기 선행지표로 활용

### 3.3. 헬퍼 함수 명세

편의성을 위한 확장 함수를 제공합니다.

| 대상 | 함수 | 설명 |
|------|------|------|
| `BondYield` | `isTreasury()`, `isCorporate()` | 채권 카테고리 확인 |
| `BondYieldSnapshot` | `getTreasuryYields()`, `getCorporateYields()` | 카테고리별 필터링 |
| `List<BondYield>` | `toYieldCurve()`, `calculateSpread()` | 수익률 곡선 생성, 스프레드 계산 |

---

## 4. API 레이어 설계

### 4.1. BondApi 인터페이스

#### 4.1.1. 패키지 위치
```
dev.kairoscode.kfc.api.BondApi
```

#### 4.1.2. 메서드 명세

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getBondYieldsByDate` | BondYieldSnapshot | `date` | 특정 일자의 전체 채권 수익률 조회 |
| `getBondYields` | List<BondYield> | `bondType`, `fromDate`, `toDate` | 특정 채권의 기간별 수익률 추이 조회 |
| `getYieldCurve` | YieldCurve | `date` | 특정 일자의 국고채 수익률 곡선 조회 (Phase 2) |

#### 4.1.3. 공통 규약

- 모든 메서드는 `suspend` 함수로 비동기 처리
- 실패 시 `KfcException` 예외 발생 (네트워크 에러, 파싱 실패, API 에러)
- 존재하지 않는 데이터 조회 시 빈 리스트 반환 (예외 미발생)
- 날짜 파라미터 기본값: `LocalDate.now()`
- 휴장일 데이터 조회 시 빈 리스트 반환 또는 직전 영업일 데이터 (선택 가능)

### 4.2. KfcClient 통합

기존 `KfcClient`에 `bond` 속성을 추가합니다.

#### 4.2.1. KfcClient 속성

| 속성명 | 타입 | nullable 여부 | API Key 필요 여부 |
|-------|------|--------------|----------------|
| `funds` | FundsApi | non-nullable | 불필요 |
| `price` | PriceApi | non-nullable | 불필요 |
| `corp` | CorpApi | nullable | 필요 |
| `stock` | StockApi | non-nullable | 불필요 |
| `bond` | BondApi | non-nullable | 불필요 (신규 추가) |
| `financials` | FinancialsApi | nullable | 필요 |

#### 4.2.2. 생성 로직

| 단계 | 처리 내용 |
|-----|---------|
| 1 | `TokenBucketRateLimiter` 인스턴스 생성 (KRX 용) |
| 2 | `KrxBondApiImpl` 생성 (Rate Limiter 주입) |
| 3 | `BondApiImpl` 생성 (KrxBondApi 위임) |
| 4 | `KfcClient`에 `bond` 속성 추가 |

#### 4.2.3. API Key 불필요

BondApi는 KRX 공개 API를 사용하므로 API Key 없이도 사용 가능합니다.

### 4.3. 주요 사용 시나리오

| 시나리오 | 메서드 | 파라미터 예시 |
|---------|-------|-------------|
| 전체 채권 수익률 조회 | `getBondYieldsByDate()` | `date = LocalDate.of(2022, 2, 4)` |
| 국고채 10년물 추이 조회 | `getBondYields()` | `bondType = TREASURY_10Y`, `fromDate`, `toDate` |
| 국고채만 필터링 | `getTreasuryYields()` | (확장 함수) |
| 장단기 금리 스프레드 계산 | 수동 계산 | 10년물 - 2년물 |
| 신용 스프레드 계산 | 수동 계산 | 회사채 - 동일 만기 국고채 |

---

## 5. 인프라 레이어 설계

### 5.1. 패키지 구조

기존 ETF/Stock과 동일한 구조를 따릅니다:

```
dev.kairoscode.kfc/
└── infrastructure/
    └── krx/
        ├── KrxHttpClient.kt             # 재사용 (기존)
        ├── KrxBondApi.kt                # 내부 KRX API 인터페이스
        ├── KrxBondApiImpl.kt            # KRX API 구현체
        ├── BondApiImpl.kt               # 공개 API 구현체 (위임)
        └── internal/
            ├── KrxApiFields.kt          # 재사용 (기존)
            ├── KrxApiParams.kt          # 재사용 (기존)
            └── HttpExtensions.kt        # 재사용 (기존)
```

### 5.2. KrxBondApiImpl (내부 구현체)

기존 `KrxStockApiImpl` 패턴을 그대로 따릅니다.

**구현 패턴**:

| 단계 | 처리 내용 |
|-----|---------|
| 1 | Rate Limiter 적용 (API 호출 전) |
| 2 | `KrxHttpClient`를 통한 POST 요청 |
| 3 | 응답 필드 추출 (`OutBlock_1`) |
| 4 | 도메인 모델 변환 (`BondType.fromKoreanName()`, `toBigDecimal()`) |
| 5 | 에러 응답 시 `KfcException` 발생 |

**변환 규칙**:

| 입력 | 변환 방법 | 출력 |
|-----|---------|------|
| `BND_KIND_TP_NM` (String) | `BondType.fromKoreanName()` | `BondType` enum |
| `BND_SRTN_YILD` (String) | `toBigDecimal()` | `BigDecimal` (수익률) |
| `DIFF` (String) | `toBigDecimal()` | `BigDecimal` (변동폭) |
| `TRD_DD` (String) | `parseKrxDate()` | `LocalDate` |

### 5.3. BondApiImpl (공개 API 구현체)

`BondApi` 인터페이스의 구현체로, `KrxBondApi`에 위임하거나 조합합니다.

**구현 패턴**:
- 단순 위임: `getBondYieldsByDate`, `getBondYields`
- 조합: `getYieldCurve`는 전체 수익률 데이터에서 국고채만 필터링하여 수익률 곡선 생성 (Phase 2)

### 5.4. API 필드 확장

기존 `KrxApiFields`에 Bond 관련 필드 상수 추가:
- `BND_KIND_TP_NM` (채권종류명)
- `BND_SRTN_YILD` (수익률)
- `DIFF` (대비)
- `TRD_DD` (거래일)

### 5.5. 타입 변환 유틸리티

- `String.toBondType()`: KRX 채권종류명 → `BondType` enum
- `String.parseKrxDate()`: KRX 날짜 형식(`YYYY/MM/DD`) → `LocalDate`
- `Map<String, Any?>.getString(key)`: null-safe 문자열 추출
- `Map<String, Any?>.getBigDecimal(key)`: null-safe BigDecimal 추출

### 5.6. 응답 구조 처리

KRX Bond API는 일관된 응답 구조를 사용합니다:

| API | 응답 필드 |
|-----|---------|
| MDCSTAT11401 (특정일) | `output` (List) |
| MDCSTAT11402 (기간별) | `output` (List) |

---

## 6. 구현 우선순위

### Phase 1: 핵심 기능 (MVP)

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 1 | 도메인 모델 | `BondYield`, `BondYieldSnapshot`, `BondType` enum | 0.5일 |
| 2 | KRX 인프라 | `KrxBondApiImpl` (특정일 조회, 기간별 조회) | 1일 |
| 3 | API 레이어 | `BondApi` 인터페이스 및 구현 | 0.5일 |
| 4 | KfcClient 통합 | `bond` 속성 추가 | 0.25일 |
| 5 | 통합 테스트 | 주요 시나리오 테스트 | 0.75일 |
| **합계** | | | **3일** |

### Phase 2: 고도화 (옵션)

| 항목 | 범위 | 우선순위 |
|------|------|---------|
| 수익률 곡선 | `YieldCurve` 모델 및 `getYieldCurve` API | 중간 |
| 스프레드 계산 | 장단기 스프레드, 신용 스프레드 헬퍼 함수 | 중간 |
| 캐싱 전략 | 채권 수익률 데이터 캐싱 | 낮음 |
| 영업일 처리 | 휴장일 조회 시 자동으로 직전 영업일 데이터 반환 | 낮음 |

### 총 예상 공수

| Phase | 범위 | 예상 공수 |
|-------|------|----------|
| Phase 1 | 핵심 기능 (MVP) | 3일 |
| Phase 2 | 고도화 (옵션) | 1일 |
| **총계** | | **4일** |

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
| 5006 | 5000번대 (검증) | `INVALID_BOND_TYPE` | 지원하지 않는 채권 종류입니다 |

### 7.2. 예외 처리 시나리오

| 시나리오 | 에러 코드 | 처리 방법 |
|---------|----------|----------|
| 잘못된 채권 종류 코드 | `INVALID_BOND_TYPE(5006)` | 지원되는 채권 종류 목록 제공 |
| 휴장일 데이터 조회 | - | 빈 리스트 반환 (예외 발생 안함) |
| KRX API 응답 오류 | `KRX_API_ERROR(3001)` | 재시도 로직 또는 명확한 에러 메시지 |
| 파싱 실패 | `JSON_PARSE_ERROR(2001)` | 로그 기록 후 예외 발생 |
| 네트워크 에러 | `NETWORK_CONNECTION_FAILED(1001)` | 재시도 로직 (기존 `KrxHttpClient` 사용) |

### 7.3. 사용자 친화적 에러 메시지

| 에러 코드 | 권장 메시지 |
|----------|-----------|
| `NETWORK_CONNECTION_FAILED` | 네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요. |
| `KRX_API_ERROR` | KRX API 오류가 발생했습니다. 나중에 다시 시도해주세요. |
| `INVALID_BOND_TYPE` | 지원하지 않는 채권 종류입니다. 국고채1년~30년, 회사채AA, 회사채BBB, CD를 사용하세요. |
| `JSON_PARSE_ERROR` | 데이터 파싱에 실패했습니다. 관리자에게 문의하세요. |

---

## 8. 참고 자료

### 8.1. 공식 문서

- [KRX 정보데이터시스템](https://data.krx.co.kr)
- [KRX 채권정보](https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020201)

### 8.2. 오픈소스 라이브러리

- [pykrx](https://github.com/sharebook-kr/pykrx) - Python KRX API 래퍼
- [pykrx bond.py](https://github.com/sharebook-kr/pykrx/blob/master/pykrx/bond/bond.py) - 채권 API 구현 참고

### 8.3. 내부 문서

- [아키텍처 가이드](/home/ulalax/project/kairos/kfc/doc/archtecture-guide.md)
- [네임스페이스 표준](/home/ulalax/project/kairos/kfc/doc/네임스페이스.md)
- [pykrx Gap 분석](/home/ulalax/project/kairos/kfc/doc/pykrx-gap-analysis.md) - 섹션 2.11 채권
- [stock 기술명세서](/home/ulalax/project/kairos/kfc/doc/specs/stock-기술명세서.md)
- [KFC README.md](/home/ulalax/project/kairos/kfc/README.md)

### 8.4. 기존 구현체 참고

- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxHttpClient.kt`
- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxStockApiImpl.kt`
- `/home/ulalax/project/kairos/pykrx/pykrx/bond/bond.py`

---

## 부록: KRX API 매핑 테이블

### A. 채권 종류 코드 매핑

| KRX 코드 | KFC Enum | 한글명 | 만기 | 카테고리 |
|---------|---------|--------|------|---------|
| `국고채1년` | `BondType.TREASURY_1Y` | 국고채 1년 | 1Y | TREASURY |
| `국고채2년` | `BondType.TREASURY_2Y` | 국고채 2년 | 2Y | TREASURY |
| `국고채3년` | `BondType.TREASURY_3Y` | 국고채 3년 | 3Y | TREASURY |
| `국고채5년` | `BondType.TREASURY_5Y` | 국고채 5년 | 5Y | TREASURY |
| `국고채10년` | `BondType.TREASURY_10Y` | 국고채 10년 | 10Y | TREASURY |
| `국고채20년` | `BondType.TREASURY_20Y` | 국고채 20년 | 20Y | TREASURY |
| `국고채30년` | `BondType.TREASURY_30Y` | 국고채 30년 | 30Y | TREASURY |
| `국민주택1종5년` | `BondType.HOUSING_5Y` | 국민주택 1종 5년 | 5Y | SPECIAL |
| `회사채AA` | `BondType.CORPORATE_AA` | 회사채 AA-(무보증 3년) | 3Y | CORPORATE |
| `회사채BBB` | `BondType.CORPORATE_BBB` | 회사채 BBB- (무보증 3년) | 3Y | CORPORATE |
| `CD` | `BondType.CD_91` | CD(91일) | 91D | SHORT_TERM |

### B. API 엔드포인트 요약

| 기능 | bld | 요청 파라미터 | 응답 필드 (주요) |
|------|-----|-------------|----------------|
| 특정일 수익률 | `MDCSTAT11401` | `inqTpCd=T`, `trdDd` | `BND_KIND_TP_NM`, `BND_SRTN_YILD`, `DIFF` |
| 기간별 수익률 | `MDCSTAT11402` | `inqTpCd=E`, `strtDd`, `endDd`, `bndKindTpCd` | `TRD_DD`, `BND_SRTN_YILD`, `DIFF` |

### C. 수익률 곡선 벤치마크

| 만기 | 주요 용도 | 설명 |
|------|---------|------|
| 1년 | 단기 금리 | 통화정책 효과 반영 |
| 2년 | 단기 금리 | 경기 선행성 높음 |
| 3년 | 중단기 금리 | 회사채 벤치마크 |
| 5년 | 중장기 금리 | 국민주택채권 벤치마크 |
| 10년 | 장기 금리 | 가장 중요한 벤치마크, 경기 전망 반영 |
| 20년 | 초장기 금리 | 연금, 보험 등 장기 자금 운용 |
| 30년 | 초장기 금리 | 장기 인프라 프로젝트 |

### D. HTTP 헤더 매핑

| 헤더 | 값 | 필수 여부 | 용도 |
|------|-----|---------|------|
| `User-Agent` | Mozilla/5.0 (...) | 필수 | 브라우저 흉내 |
| `Accept` | application/json, text/plain, */* | 권장 | JSON 응답 수신 |
| `Accept-Language` | ko-KR,ko;q=0.9 | 선택 | 한국어 응답 |
| `Referer` | http://data.krx.co.kr/ | 필수 | 출처 인증 |
| `Origin` | http://data.krx.co.kr | 권장 | CORS 처리 |

---

**문서 끝**
