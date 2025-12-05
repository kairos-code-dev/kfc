# Future (선물) 네임스페이스 기술명세서

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
8. [테스트 전략](#8-테스트-전략)
9. [참고 자료](#9-참고-자료)

---

## 1. 개요

### 1.1. 목적

한국 파생상품시장의 선물(Futures) 거래 데이터를 제공하는 `future` 네임스페이스를 KFC 프로젝트에 추가합니다. 이를 통해 사용자는 선물 종목 리스트, 선물 OHLCV 데이터 등을 표준화된 방식으로 조회할 수 있습니다.

### 1.2. 범위

다음 핵심 기능을 지원합니다:

| 기능 | 설명 | 데이터 소스 |
|------|------|------------|
| 선물 티커 목록 조회 | 거래 가능한 선물 종목 전체 목록 | KRX |
| 선물명 조회 | 선물 티커 → 선물명 매핑 | KRX |
| 선물 OHLCV | 특정 일자의 전종목 시가, 고가, 저가, 종가, 거래량 | KRX |

### 1.3. 설계 원칙

1. **기존 패턴 준수**: Stock/ETF 네임스페이스와 동일한 인프라 구조 사용
2. **도메인 중심 설계**: 선물 상품 특성을 반영한 모델 설계
3. **데이터 소스 독립성**: KRX API를 우선 지원하되, 향후 다른 소스 추가 가능하도록 추상화
4. **타입 안전성**: Kotlin의 타입 시스템을 활용한 명시적 타입 변환
5. **단순성 우선**: 선물 API는 단순하므로 과도한 추상화 지양

### 1.4. 네임스페이스 경계

| 데이터 | 담당 네임스페이스 | 기준 |
|--------|-----------------|------|
| **선물 종목 정보, OHLCV** | `future` | 선물 상품 거래 데이터 |
| **주식 OHLCV** | `stock` | 현물 주식 거래 데이터 |
| **ETF OHLCV** | `price` | ETF 거래 데이터 |
| **옵션** | (향후 추가) | 옵션 상품 거래 데이터 |

#### 1.4.1. pykrx 기능 대응

pykrx의 `stock.future_api` 모듈에서 제공하는 기능을 모두 지원합니다:

| pykrx 함수 | kfc 메서드 | 비고 |
|-----------|-----------|------|
| `get_future_ticker_list()` | `getFutureTickerList()` | 선물 티커 목록 |
| `get_future_ticker_name(ticker)` | `getFutureName(ticker)` | 선물명 조회 |
| `get_future_ohlcv_by_ticker(date, prod)` | `getOhlcvByTicker(date, product)` | 전종목 OHLCV |

**미구현 기능**:
- `get_future_ohlcv_by_date(fromDate, toDate, ticker)`: pykrx에서도 `NotImplementedError`로 미구현됨

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

기존 Stock/ETF 구현과 동일한 방식을 사용합니다:

**요청 예시**:
```http
POST http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd
Content-Type: application/x-www-form-urlencoded
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
Accept: application/json, text/plain, */*
Accept-Language: ko-KR,ko;q=0.9
Referer: http://data.krx.co.kr/
Origin: http://data.krx.co.kr

bld=dbms/MDC/STAT/standard/MDCSTAT40301&locale=ko_KR&trdDd=20220902&prodId=KRDRVFUEST
```

**응답 구조**:
```json
{
  "OutBlock_1": [
    {
      "ISU_CD": "KRDRVFUEST202212",
      "ISU_NM": "EURO STOXX50 선물 2022/12",
      "TDD_OPNPRC": "3565.00",
      "TDD_HGPRC": "3565.00",
      "TDD_LWPRC": "3550.00",
      "TDD_CLSPRC": "3555.00",
      "CMPPREVDD_PRC": "15.00",
      "FLUC_TP_CD": "1",
      "FLUC_RT": "0.42",
      "ACC_TRDVOL": "85",
      "ACC_TRDVAL": "3022250000"
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

##### A. 선물 티커 목록 (MDCSTAT40001)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT40001` |
| **용도** | 거래 가능한 선물 종목 티커 목록 조회 |
| **요청 파라미터** | `locale=ko_KR` |
| **응답 필드** | `prodId`, `한글종목명` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT40001
locale=ko_KR
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "prodId": "KRDRVFUK2I",
      "한글종목명": "코스피200 선물"
    },
    {
      "prodId": "KRDRVFUMKI",
      "한글종목명": "미니 코스피200 선물"
    },
    {
      "prodId": "KRDRVFUEST",
      "한글종목명": "EURO STOXX50 선물"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `prodId` | STRING | 상품 ID (선물 티커) | `KRDRVFUK2I` |
| `한글종목명` | STRING | 선물 상품 한글명 | `코스피200 선물` |

##### B. 선물 OHLCV (MDCSTAT40301)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT40301` |
| **용도** | 특정 일자의 특정 선물 상품 전종목(만기별) OHLCV 조회 |
| **요청 파라미터** | `trdDd` (YYYYMMDD), `prodId` (선물 티커) |
| **응답 필드** | `ISU_CD`, `ISU_NM`, `TDD_OPNPRC`, `TDD_HGPRC`, `TDD_LWPRC`, `TDD_CLSPRC`, `ACC_TRDVOL`, `ACC_TRDVAL`, `FLUC_RT` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT40301
locale=ko_KR
trdDd=20220902
prodId=KRDRVFUEST
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "ISU_CD": "KRDRVFUEST202212",
      "ISU_NM": "EURO STOXX50 선물 2022/12",
      "TDD_OPNPRC": "3565.00",
      "TDD_HGPRC": "3565.00",
      "TDD_LWPRC": "3550.00",
      "TDD_CLSPRC": "3555.00",
      "CMPPREVDD_PRC": "15.00",
      "FLUC_TP_CD": "1",
      "FLUC_RT": "0.42",
      "ACC_TRDVOL": "85",
      "ACC_TRDVAL": "3022250000"
    },
    {
      "ISU_CD": "KRDRVFUEST202303",
      "ISU_NM": "EURO STOXX50 선물 2023/03",
      "TDD_OPNPRC": "3570.00",
      "TDD_HGPRC": "3570.00",
      "TDD_LWPRC": "3555.00",
      "TDD_CLSPRC": "3560.00",
      "CMPPREVDD_PRC": "20.00",
      "FLUC_TP_CD": "1",
      "FLUC_RT": "0.56",
      "ACC_TRDVOL": "12",
      "ACC_TRDVAL": "427800000"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `ISU_CD` | STRING | 종목 코드 (만기 포함) | `KRDRVFUEST202212` |
| `ISU_NM` | STRING | 종목명 (만기 포함) | `EURO STOXX50 선물 2022/12` |
| `TDD_OPNPRC` | STRING | 시가 (소수점, 콤마 포함) | `3565.00` |
| `TDD_HGPRC` | STRING | 고가 | `3565.00` |
| `TDD_LWPRC` | STRING | 저가 | `3550.00` |
| `TDD_CLSPRC` | STRING | 종가 | `3555.00` |
| `CMPPREVDD_PRC` | STRING | 전일대비 가격 | `15.00` |
| `FLUC_TP_CD` | STRING | 등락 구분 (1:상승, 2:하락, 3:보합) | `1` |
| `FLUC_RT` | STRING | 등락률 (%) | `0.42` |
| `ACC_TRDVOL` | STRING | 거래량 (계약 수) | `85` |
| `ACC_TRDVAL` | STRING | 거래대금 (원) | `3022250000` |

### 2.2. 데이터 특성

#### 2.2.1. 선물 티커 체계

선물 티커는 `prodId` 형식으로 상품을 구분하며, 실제 거래되는 종목은 만기별로 구분됩니다:

| 상품 ID (prodId) | 한글명 | 실제 종목 코드 예시 |
|-----------------|--------|------------------|
| `KRDRVFUK2I` | 코스피200 선물 | `KRDRVFUK2I202412`, `KRDRVFUK2I202503` |
| `KRDRVFUMKI` | 미니 코스피200 선물 | `KRDRVFUMKI202412` |
| `KRDRVFUEST` | EURO STOXX50 선물 | `KRDRVFUEST202212`, `KRDRVFUEST202303` |

**특징**:
- `prodId`: 선물 상품 식별자 (예: `KRDRVFUEST`)
- `ISU_CD`: 만기가 포함된 실제 종목 코드 (예: `KRDRVFUEST202212`)
- 하나의 `prodId`에 여러 만기의 종목이 존재

#### 2.2.2. 가격 표기

- 주식과 달리 **소수점**이 있을 수 있음 (예: `3555.00`)
- 콤마 구분자 사용 여부는 API 응답에 따라 다름
- 거래량은 **계약 수** 단위

#### 2.2.3. 데이터 제공 범위

- **일별 데이터만 제공**: 특정 일자의 스냅샷
- **기간별 조회 미지원**: pykrx에서도 `get_future_ohlcv_by_date(fromDate, toDate, ticker)`는 미구현
- **만기별 구분**: 동일 상품이라도 만기별로 별도 종목으로 제공

---

## 3. 도메인 모델 설계

### 3.1. 패키지 구조

**베이스 패키지**: `dev.kairoscode.kfc.domain.future`

| 클래스명 | 타입 | 설명 |
|---------|------|------|
| `FutureProduct` | Data Class | 선물 상품 정보 |
| `FutureOhlcv` | Data Class | 선물 OHLCV 데이터 |
| `FutureProductType` | Enum | 선물 상품 분류 (선택 사항) |

### 3.2. 핵심 모델 명세

#### 3.2.1. FutureProduct (선물 상품 정보)

선물 상품의 기본 정보입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `productId` | String | 선물 상품 ID (티커) | `KRDRVFUEST` |
| `name` | String | 선물 상품명 (한글) | `EURO STOXX50 선물` |

**비즈니스 규칙**:
- `productId`는 KRX 표준 형식 (예: `KRDRVFU` 접두사)
- `name`은 사용자 표시용 한글명

#### 3.2.2. FutureOhlcv (선물 OHLCV)

특정 일자의 선물 종목(만기별) OHLCV 데이터입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `date` | LocalDate | 거래일 | `2022-09-02` |
| `productId` | String | 상품 ID | `KRDRVFUEST` |
| `issueCode` | String | 종목 코드 (만기 포함) | `KRDRVFUEST202212` |
| `issueName` | String | 종목명 (만기 포함) | `EURO STOXX50 선물 2022/12` |
| `open` | BigDecimal | 시가 | `3565.00` |
| `high` | BigDecimal | 고가 | `3565.00` |
| `low` | BigDecimal | 저가 | `3550.00` |
| `close` | BigDecimal | 종가 | `3555.00` |
| `changeFromPrev` | BigDecimal? | 전일대비 가격 | `15.00` |
| `changeRate` | BigDecimal? | 등락률 (%) | `0.42` |
| `priceChangeType` | PriceChangeType? | 등락 구분 | `PriceChangeType.RISE` |
| `volume` | Long | 거래량 (계약 수) | `85` |
| `tradingValue` | Long? | 거래대금 (원) | `3022250000` |

**비즈니스 규칙**:
- `open`, `high`, `low`, `close`는 소수점을 포함할 수 있으므로 `BigDecimal` 사용
- `volume`은 계약 수 단위 (주식과 의미가 다름)
- `changeFromPrev`, `changeRate`는 거래가 없을 경우 null 가능
- `priceChangeType`은 `PriceChangeType` Enum 사용 (Stock과 동일)

#### 3.2.3. PriceChangeType (등락 구분)

Stock 네임스페이스의 동일한 Enum을 재사용합니다:

| code | koreanName | 설명 |
|------|-----------|------|
| `1` | RISE | 상승 |
| `2` | FALL | 하락 |
| `3` | UNCHANGED | 보합 |

### 3.3. 헬퍼 함수 명세

편의성을 위한 확장 함수를 제공합니다.

| 대상 | 함수 | 설명 |
|------|------|------|
| `FutureOhlcv` | `isRising()`, `isFalling()` | 등락 여부 확인 |
| `FutureOhlcv` | `extractMaturityDate()` | 종목 코드에서 만기일 추출 |
| `List<FutureOhlcv>` | `filterByProduct(productId)` | 특정 상품으로 필터링 |
| `List<FutureOhlcv>` | `sortByVolume()` | 거래량 순 정렬 |

---

## 4. API 레이어 설계

### 4.1. FutureApi 인터페이스

#### 4.1.1. 패키지 위치

`dev.kairoscode.kfc.api.FutureApi`

#### 4.1.2. 메서드 명세

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getFutureTickerList` | List<FutureProduct> | - | 선물 티커 목록 조회 |
| `getFutureName` | String? | `productId` | 선물 티커 → 선물명 조회 |
| `getOhlcvByTicker` | List<FutureOhlcv> | `date`, `productId`, `alternative`, `previousBusiness` | 특정 일자/상품의 전종목(만기별) OHLCV |

**파라미터 상세**:

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|-------|------|
| `productId` | String | (필수) | 선물 상품 ID (예: `KRDRVFUEST`) |
| `date` | LocalDate | `LocalDate.now()` | 조회 일자 |
| `alternative` | Boolean | `false` | 데이터 없을 시 대체 날짜 조회 여부 |
| `previousBusiness` | Boolean | `true` | 대체 날짜 방향 (true: 이전 영업일, false: 다음 영업일) |

#### 4.1.3. 공통 규약

- 모든 메서드는 `suspend` 함수로 비동기 처리
- 실패 시 `KfcException` 예외 발생 (네트워크 에러, 파싱 실패, API 에러)
- 존재하지 않는 데이터 조회 시 빈 리스트 또는 `null` 반환 (예외 미발생)
- 날짜 파라미터 기본값: `LocalDate.now()`

### 4.2. KfcClient 통합

기존 `KfcClient`에 `future` 속성을 추가합니다.

| 속성 | 타입 | Nullable | 설명 |
|------|------|---------|------|
| `funds` | FundsApi | No | 펀드 API |
| `price` | PriceApi | No | 가격 API |
| `corp` | CorpApi | Yes | 기업정보 API (API Key 필요) |
| `stock` | StockApi | No | 주식 API |
| `future` | FutureApi | No | 선물 API (추가, API Key 불필요) |
| `financials` | FinancialsApi | Yes | 재무제표 API (API Key 필요) |

#### 4.2.1. API Key 불필요

`future` API는 공개 KRX API를 사용하므로 API Key 없이 사용 가능합니다.

### 4.3. 사용 예시

#### 기본 사용 패턴

| 시나리오 | 메서드 | 파라미터 예시 |
|---------|-------|-------------|
| 선물 티커 목록 조회 | `getFutureTickerList()` | - |
| 선물명 조회 | `getFutureName(productId)` | `"KRDRVFUEST"` |
| OHLCV 조회 | `getOhlcvByTicker(date, productId)` | `LocalDate.of(2022, 9, 2)`, `"KRDRVFUEST"` |
| 휴장일 대체 조회 | `getOhlcvByTicker(..., alternative=true)` | `alternative=true`, `previousBusiness=true` |

---

## 5. 인프라 레이어 설계

### 5.1. 패키지 구조

**베이스 패키지**: `dev.kairoscode.kfc.infrastructure.krx.future`

| 항목 | 타입 | 설명 |
|------|------|------|
| `KrxFutureApiImpl` | Class | KRX API 호출 구현체 |
| `dto/FutureTickerListResponse` | Data Class | 티커 목록 응답 DTO |
| `dto/FutureOhlcvResponse` | Data Class | OHLCV 응답 DTO |

### 5.2. 구현체 명세

#### 5.2.1. KrxFutureApiImpl

KRX API를 호출하여 선물 데이터를 조회하는 인프라 레이어 구현체입니다.

**역할**:

| 책임 | 설명 |
|------|------|
| KRX API 호출 | HTTP 요청 생성 및 응답 수신 |
| 데이터 변환 | JSON 응답 → 도메인 모델 변환 |
| 에러 처리 | 예외 변환 및 에러 메시지 생성 |
| Rate Limiting | API 호출 빈도 제한 적용 |

**의존성**:

| 의존 객체 | 용도 | 재사용 여부 |
|----------|------|-----------|
| `KrxHttpClient` | HTTP 통신 | 기존 재사용 |
| `TokenBucketRateLimiter` | Rate Limiting | 기존 재사용 |

**주요 메서드**:

| 메서드명 | 파라미터 | 반환 타입 | 설명 |
|---------|---------|----------|------|
| `fetchFutureTickerList()` | - | List<FutureProduct> | 선물 티커 목록 조회 |
| `fetchFutureOhlcv(date, productId)` | date, productId | List<FutureOhlcv> | 선물 OHLCV 조회 |

#### 5.2.2. FutureApiImpl

`FutureApi` 인터페이스 구현체로, 도메인 레이어와 인프라 레이어를 연결합니다.

**역할**:

| 책임 | 설명 |
|------|------|
| 인프라 호출 | `KrxFutureApiImpl` 메서드 호출 |
| 비즈니스 로직 | Alternative 날짜 조회 로직 등 |
| 인터페이스 구현 | `FutureApi` 인터페이스 메서드 구현 |

### 5.3. API 엔드포인트 매핑

| 기능 | bld 경로 | 파라미터 | 응답 필드 |
|------|---------|---------|----------|
| 티커 목록 | `MDCSTAT40001` | `locale` | `prodId`, `한글종목명` |
| OHLCV | `MDCSTAT40301` | `trdDd`, `prodId`, `locale` | `ISU_CD`, `ISU_NM`, `TDD_OPNPRC`, `TDD_HGPRC`, `TDD_LWPRC`, `TDD_CLSPRC`, `ACC_TRDVOL`, `ACC_TRDVAL`, `FLUC_RT` |

### 5.4. 데이터 변환 패턴

#### 5.4.1. 데이터 파싱 규칙

| 필드 유형 | 입력 형식 | 출력 타입 | 변환 규칙 | 예시 |
|----------|---------|---------|---------|------|
| 가격 데이터 | 문자열 (콤마, 소수점) | BigDecimal | 콤마 제거 후 변환 | `"3,555.00"` → `BigDecimal(3555.00)` |
| 거래량 | 문자열 (콤마) | Long | 콤마 제거 후 변환 | `"1,234"` → `Long(1234)` |
| 거래대금 | 문자열 (콤마) | Long | 콤마 제거 후 변환 | `"3,022,250,000"` → `Long(3022250000)` |
| 등락률 | 문자열 (소수점) | BigDecimal | 직접 변환 | `"0.42"` → `BigDecimal(0.42)` |
| 등락 구분 | 문자열 코드 | PriceChangeType | Enum 매핑 | `"1"` → `PriceChangeType.RISE` |

#### 5.4.2. 응답 블록 추출

| API | 응답 블록 키 | 데이터 형식 |
|-----|------------|-----------|
| MDCSTAT40001 | `OutBlock_1` | List (선물 티커 목록) |
| MDCSTAT40301 | `OutBlock_1` | List (OHLCV 데이터) |

### 5.5. Rate Limiting

기존 `TokenBucketRateLimiter`를 재사용합니다. 각 API 호출 전 `rateLimiter.acquire()` 호출하여 호출 제한을 적용합니다.

### 5.6. 캐싱 전략 (선택 사항)

| 데이터 | TTL | 캐싱 전략 | 사유 | 구현 방식 |
|--------|-----|----------|------|----------|
| 선물 티커 목록 | 1일 | 메모리 캐시 (로컬) | 변경 빈도 낮음 | `ConcurrentHashMap` + 만료 시간 관리 |
| OHLCV | 캐싱 안함 | - | 실시간성 중요 | - |

---

## 6. 구현 우선순위

### Phase 1: 핵심 기능 구현 (MVP)

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 1 | 도메인 모델 | `FutureProduct`, `FutureOhlcv` | 0.5일 |
| 2 | 인프라 레이어 | `KrxFutureApiImpl` (티커 목록, OHLCV) | 1일 |
| 3 | API 레이어 | `getFutureTickerList`, `getFutureName`, `getOhlcvByTicker` | 0.5일 |
| 4 | 통합 테스트 | Live API 테스트 | 0.5일 |
| **합계** | | | **2.5일** |

### Phase 2: 고도화 (선택 사항)

| 항목 | 범위 | 우선순위 |
|------|------|---------|
| 캐싱 전략 | 티커 목록 캐싱 | 낮음 |
| 헬퍼 함수 | 확장 함수 추가 (`extractMaturityDate` 등) | 낮음 |
| 상품 분류 | `FutureProductType` Enum 추가 (지수/금리/통화 등) | 낮음 |

### 총 예상 공수

**MVP (Phase 1)**: 2.5일

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

현재는 신규 에러 코드 불필요. 기존 에러 코드로 충분히 처리 가능.

### 7.2. 예외 처리 시나리오

| 시나리오 | 에러 코드 | 처리 방법 |
|---------|----------|----------|
| 잘못된 선물 상품 ID | - | 빈 리스트 반환 (예외 발생 안함) |
| 존재하지 않는 상품 ID | - | `getFutureName()`: null 반환 |
| 휴장일 데이터 조회 | - | `alternative=false`: 빈 리스트, `alternative=true`: 이전/다음 영업일 데이터 |
| KRX API 응답 오류 | `KRX_API_ERROR(3001)` | 재시도 로직 또는 명확한 에러 메시지 |
| 파싱 실패 | `JSON_PARSE_ERROR(2001)` | 로그 기록 후 예외 발생 |
| 네트워크 에러 | `NETWORK_CONNECTION_FAILED(1001)` | 재시도 로직 (기존 `KrxHttpClient` 사용) |

### 7.3. 에러 메시지 가이드

| 에러 코드 | 권장 메시지 | 사용자 액션 |
|----------|-----------|-----------|
| `NETWORK_CONNECTION_FAILED` | 네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요. | 재시도 |
| `KRX_API_ERROR` | KRX 시스템 오류로 데이터를 가져올 수 없습니다. | 재시도 또는 지원 문의 |
| `JSON_PARSE_ERROR` | 데이터 형식 오류가 발생했습니다. | 지원 문의 |

**참고**: 존재하지 않는 상품 조회 시 예외 대신 `null` 또는 빈 리스트를 반환합니다.

---

## 8. 테스트 전략

[아키텍처 가이드](/home/ulalax/project/kairos/kfc/doc/archtecture-guide.md) 기준을 따릅니다.

### 8.1. 단위 테스트 (Unit Test)

도메인 모델의 비즈니스 규칙을 검증합니다. 테스트 코드가 **스펙 문서**처럼 읽혀야 합니다.

#### 테스트 시나리오

| 카테고리 | 시나리오 |
|---------|---------|
| **FutureProduct** | 상품 ID와 이름이 올바르게 저장됨 |
| **FutureOhlcv** | 시가/고가/저가/종가 관계 검증 (low ≤ open/close ≤ high) |
| | 등락 여부 판별 (`isRising()`, `isFalling()`) |
| | 종목 코드에서 만기일 추출 가능 |
| **헬퍼 함수** | 리스트 거래량 순 정렬 |
| | 특정 상품으로 필터링 |

### 8.2. 통합 테스트 (Integration Test)

API 레이어의 동작을 검증합니다. 테스트 코드가 **API 문서**처럼 읽혀야 합니다.

#### 테스트 카테고리

**1. 기본 동작 (Basic Operations)**
- 선물 티커 목록 조회 시 1개 이상 반환
- 코스피200 선물(`KRDRVFUK2I`) 이름 조회 성공
- 특정 일자/상품의 OHLCV 조회 성공

**2. 응답 데이터 검증 (Response Validation)**
- 선물 티커는 `KRDRVFU` 접두사로 시작
- OHLCV의 시가/고가/저가/종가는 양수
- 거래량은 0 이상
- 저가 ≤ 시가 ≤ 고가, 저가 ≤ 종가 ≤ 고가

**3. 입력 파라미터 검증 (Input Validation)**
- 날짜 파라미터 미지정 시 오늘 날짜 사용
- `alternative=true` 설정 시 휴장일에도 데이터 반환
- `previousBusiness=false` 설정 시 다음 영업일 데이터 반환

**4. 엣지 케이스 (Edge Cases)**
- 존재하지 않는 상품 ID 조회 시 빈 리스트 반환
- 휴장일 데이터 조회 시 (`alternative=false`) 빈 리스트
- 과거 데이터 조회 시 상장폐지된 만기 종목 포함 가능

**5. 실무 활용 예제 (Usage Examples)**
- 전체 선물 상품의 당일 OHLCV 일괄 조회
- 거래량 상위 N개 종목 필터링
- 특정 상품의 만기별 종목 비교

### 8.3. 테스트 데이터

- Fake 객체 우선 사용 (Mock 프레임워크 최소화)
- 의미 있는 테스트 데이터: `kospi200_future`, `euro_stoxx50_future` 등
- 실제 KRX 응답을 JSON 파일로 저장하여 재사용

### 8.4. 테스트 파일 구조

**위치**: `src/integrationTest/kotlin/dev/kairoscode/kfc/api/FutureApiIntegrationTest.kt`

**구조**:

| 레벨 | 항목 | 설명 |
|------|------|------|
| Top | `FutureApiIntegrationTest` | 최상위 테스트 클래스 |
| Nested | `1. 기본 동작` | 기본 기능 검증 (티커 목록, 선물명, OHLCV 조회) |
| Nested | `2. 응답 데이터 검증` | 응답 필드 유효성 검증 (가격, 거래량, 티커 형식) |
| Nested | `3. 입력 파라미터 검증` | 파라미터 처리 검증 (기본값, alternative, previousBusiness) |
| Nested | `4. 엣지 케이스` | 예외 상황 검증 (잘못된 ID, 휴장일, 과거 데이터) |
| Nested | `5. 실무 활용 예제` | 실무 시나리오 검증 (일괄 조회, 필터링, 정렬) |

---

## 9. 참고 자료

### 9.1. 공식 문서

- [KRX 정보데이터시스템](https://data.krx.co.kr)
- [KRX 파생상품 시장정보](https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC0201020402)

### 9.2. 오픈소스 라이브러리

- [pykrx](https://github.com/sharebook-kr/pykrx) - Python KRX API 래퍼
- [pykrx future_api.py](https://github.com/sharebook-kr/pykrx/blob/master/pykrx/stock/future_api.py) - 선물 API 참고 구현

### 9.3. 내부 문서

- [아키텍처 가이드](/home/ulalax/project/kairos/kfc/doc/archtecture-guide.md)
- [pykrx Gap 분석](/home/ulalax/project/kairos/kfc/doc/pykrx-gap-analysis.md)
- [Stock 기술명세서](/home/ulalax/project/kairos/kfc/doc/specs/stock-기술명세서.md)
- [KFC README.md](/home/ulalax/project/kairos/kfc/README.md)

### 9.4. 기존 구현체 참고

- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxHttpClient.kt`
- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxStockApiImpl.kt`

---

## 부록: KRX API 매핑 테이블

### A. 선물 상품 분류

| 상품 ID | 한글명 | 분류 | 기초 자산 |
|--------|--------|------|----------|
| `KRDRVFUK2I` | 코스피200 선물 | 지수 선물 | 코스피200 지수 |
| `KRDRVFUMKI` | 미니 코스피200 선물 | 지수 선물 | 코스피200 지수 (소액) |
| `KRDRVOPK2I` | 코스피200 옵션 | 지수 옵션 | 코스피200 지수 |
| `KRDRVFUEST` | EURO STOXX50 선물 | 해외지수 선물 | EURO STOXX50 지수 |
| `KRDRVFUUSD` | 달러 선물 | 통화 선물 | USD/KRW |
| `KRDRVFUJPY` | 엔 선물 | 통화 선물 | JPY/KRW |
| `KRDRVFUEUR` | 유로 선물 | 통화 선물 | EUR/KRW |
| `KRDRVFUBM3` | 국채 3년 선물 | 금리 선물 | 국고채 3년 |
| `KRDRVFUKGD` | 금 선물 | 상품 선물 | 금 |

### B. API 엔드포인트 요약

| 기능 | bld | 요청 파라미터 | 응답 필드 (주요) |
|------|-----|-------------|----------------|
| 티커 목록 | `MDCSTAT40001` | `locale` | `prodId`, `한글종목명` |
| OHLCV | `MDCSTAT40301` | `trdDd`, `prodId`, `locale` | `ISU_CD`, `ISU_NM`, `TDD_OPNPRC`, `TDD_HGPRC`, `TDD_LWPRC`, `TDD_CLSPRC`, `ACC_TRDVOL` |

### C. HTTP 헤더 매핑

| 헤더 | 값 | 필수 여부 | 용도 |
|------|-----|---------|------|
| `User-Agent` | Mozilla/5.0 (...) | 필수 | 브라우저 흉내 |
| `Accept` | application/json, text/plain, */* | 권장 | JSON 응답 수신 |
| `Accept-Language` | ko-KR,ko;q=0.9 | 선택 | 한국어 응답 |
| `Referer` | http://data.krx.co.kr/ | 필수 | 출처 인증 |
| `Origin` | http://data.krx.co.kr | 권장 | CORS 처리 |

### D. 가격 데이터 형식

| 항목 | 형식 | 예시 | 파싱 결과 |
|------|------|------|----------|
| 시가/고가/저가/종가 | 문자열 (소수점, 콤마) | `"3,555.00"` | `BigDecimal(3555.00)` |
| 거래량 | 문자열 (콤마) | `"1,234"` | `Long(1234)` |
| 거래대금 | 문자열 (콤마) | `"3,022,250,000"` | `Long(3022250000)` |
| 등락률 | 문자열 (소수점) | `"0.42"` | `BigDecimal(0.42)` |

---

## 변경 이력

### v1.0 (2025-12-05)
- 초기 작성
- pykrx `future_api.py` 기능 동등성 확보
- 도메인 모델: `FutureProduct`, `FutureOhlcv`
- API 메서드: `getFutureTickerList`, `getFutureName`, `getOhlcvByTicker`
- KRX API 엔드포인트 문서화 (MDCSTAT40001, MDCSTAT40301)
- 테스트 전략: 5개 카테고리 정의
- 예상 공수: 2.5일 (MVP)

---

**문서 끝**
