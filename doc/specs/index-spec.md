# Index (지수) 네임스페이스 기술명세서

> **작성일**: 2025-12-05
> **버전**: 1.0
> **대상 프로젝트**: KFC (Korea Financial data Collector)
> **변경 이력**: pykrx 기능 동등성 확보를 위한 지수 API 추가

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

> **Note**: BLD 코드는 pykrx 소스 코드 (`pykrx/website/krx/market/core.py`)를 기준으로 검증되었습니다.

---

## 1. 개요

### 1.1. 목적

한국 증시의 주요 지수(KOSPI, KOSDAQ, KRX 등) 데이터를 제공하는 `index` 네임스페이스를 KFC 프로젝트에 추가합니다. 이를 통해 사용자는 지수 티커 목록, 지수명, 구성 종목, OHLCV, 밸류에이션 지표, 등락률, 기본정보 등을 표준화된 방식으로 조회할 수 있습니다.

### 1.2. 범위

다음 핵심 기능을 지원합니다:

| 기능 | 설명 | 데이터 소스 |
|------|------|------------|
| 지수 티커 목록 조회 | 시장별 지수 전체 목록 | KRX |
| 지수명 조회 | 지수 코드 → 지수명 변환 | KRX |
| 지수 구성 종목 조회 | 특정 지수의 구성 종목 티커 리스트 | KRX |
| 지수 OHLCV | 개별/전체 지수 시가, 고가, 저가, 종가, 거래량 | KRX |
| 지수 밸류에이션 | PER, 선행PER, PBR, 배당수익률 | KRX |
| 지수 등락률 | 특정 기간 전체 지수 등락률 | KRX |
| 지수 기본정보 | 상장일(기준시점, 발표시점), 기준지수, 종목수 | KRX |

### 1.3. 설계 원칙

1. **기존 패턴 준수**: Stock/ETF 네임스페이스와 동일한 인프라 구조 사용
2. **도메인 중심 설계**: 지수 데이터 도메인 기준으로 분류
3. **데이터 소스 독립성**: KRX API를 우선 지원하되, 향후 다른 소스 추가 가능하도록 추상화
4. **타입 안전성**: Kotlin의 타입 시스템을 활용한 명시적 타입 변환
5. **캐싱 전략**: 지수 티커 목록, 구성 종목은 변경 빈도가 낮으므로 캐싱 적용 권장

### 1.4. 네임스페이스 경계

| 데이터 | 담당 네임스페이스 | 기준 |
|--------|-----------------|------|
| **지수명, 티커, 구성종목** | `index` | 지수 기본 정보 |
| **지수 OHLCV, 밸류에이션** | `index` | 지수 가격 및 평가 지표 |
| **지수 등락률, 기본정보** | `index` | 지수 분석 데이터 |
| **주식 OHLCV, 시가총액** | `stock` | 개별 주식 데이터 |
| **ETF 시세** | `price` | ETF 실시간/일별 시세 |

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

bld=dbms/MDC/STAT/standard/MDCSTAT00301&mktId=1&trdDd=20210104
```

**응답 구조**:
```json
{
  "OutBlock_1": [
    {
      "IDX_NM": "코스피",
      "TDD_OPNPRC": "2874.50",
      "TDD_HGPRC": "2946.54",
      "TDD_LWPRC": "2869.11",
      "TDD_CLSPRC": "2944.45",
      "ACC_TRDVOL": "1,026,510,465",
      "ACC_TRDVAL": "25,011,393,960,858"
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

##### A. 지수 티커 목록 (MDCSTAT00401)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00401` |
| **용도** | 특정 시장의 모든 지수 티커 조회 |
| **요청 파라미터** | `idxIndMidclssCd` (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마) |
| **응답 필드** | `IDX_NM`, `IDX_ENG_NM`, `BAS_TM_CONTN`, `ANNC_TM_CONTN`, `BAS_IDX_CONTN`, `CALC_CYCLE_CONTN`, `CALC_TM_CONTN`, `COMPST_ISU_CNT`, `IND_TP_CD`, `IDX_IND_CD` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT00401
idxIndMidclssCd=02
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "IDX_IND_CD": "1001",
      "IDX_NM": "코스피"
    },
    {
      "IDX_IND_CD": "1002",
      "IDX_NM": "대형주"
    },
    {
      "IDX_IND_CD": "1028",
      "IDX_NM": "코스피 200"
    }
  ]
}
```

##### B. 지수 구성 종목 (MDCSTAT00601)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00601` |
| **용도** | 특정 지수의 구성 종목 조회 |
| **요청 파라미터** | `trdDd` (거래일, YYYYMMDD), `indTpCd` (지수 그룹 ID), `indTpCd2` (지수 티커) |
| **응답 필드** | `ISU_SRT_CD`, `ISU_ABBRV` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT00601
trdDd=20210104
indTpCd=1
indTpCd2=028
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "ISU_SRT_CD": "005930",
      "ISU_ABBRV": "삼성전자"
    },
    {
      "ISU_SRT_CD": "000660",
      "ISU_ABBRV": "SK하이닉스"
    }
  ]
}
```

##### C. 지수 OHLCV - 기간별 (MDCSTAT00301)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00301` |
| **용도** | 특정 지수의 기간별 OHLCV 조회 |
| **요청 파라미터** | `strtDd` (시작일), `endDd` (종료일), `indIdx` (지수 그룹 ID), `indIdx2` (지수 티커) |
| **응답 필드** | `TRD_DD`, `CLSPRC_IDX`, `FLUC_TP_CD`, `PRV_DD_CMPR`, `UPDN_RATE`, `OPNPRC_IDX`, `HGPRC_IDX`, `LWPRC_IDX`, `ACC_TRDVOL`, `ACC_TRDVAL`, `MKTCAP` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT00301
strtDd=20210101
endDd=20210130
indIdx=1
indIdx2=001
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "TRD_DD": "2021/01/04",
      "TDD_OPNPRC": "2874.50",
      "TDD_HGPRC": "2946.54",
      "TDD_LWPRC": "2869.11",
      "TDD_CLSPRC": "2944.45",
      "ACC_TRDVOL": "1,026,510,465",
      "ACC_TRDVAL": "25,011,393,960,858"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `TRD_DD` | STRING | 거래일 (YYYY/MM/DD) | `2021/01/04` |
| `TDD_OPNPRC` | STRING | 시가 (콤마 포함, 소수점 2자리) | `2874.50` |
| `TDD_HGPRC` | STRING | 고가 (콤마 포함, 소수점 2자리) | `2946.54` |
| `TDD_LWPRC` | STRING | 저가 (콤마 포함, 소수점 2자리) | `2869.11` |
| `TDD_CLSPRC` | STRING | 종가 (콤마 포함, 소수점 2자리) | `2944.45` |
| `ACC_TRDVOL` | STRING | 거래량 (콤마 포함) | `1,026,510,465` |
| `ACC_TRDVAL` | STRING | 거래대금 (원, 콤마 포함) | `25,011,393,960,858` |

##### D. 지수 OHLCV - 전체 지수 (MDCSTAT00101)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00101` |
| **용도** | 특정 일자 전체 지수 OHLCV 조회 |
| **요청 파라미터** | `trdDd` (YYYYMMDD), `idxIndMidclssCd` (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마) |
| **응답 필드** | `IDX_NM`, `CLSPRC_IDX`, `FLUC_TP_CD`, `CMPPREVDD_IDX`, `FLUC_RT`, `OPNPRC_IDX`, `HGPRC_IDX`, `LWPRC_IDX`, `ACC_TRDVOL`, `ACC_TRDVAL`, `MKTCAP` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT00101
trdDd=20210104
idxIndMidclssCd=02
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "IDX_NM": "코스피",
      "TDD_OPNPRC": "2874.50",
      "TDD_HGPRC": "2946.54",
      "TDD_LWPRC": "2869.11",
      "TDD_CLSPRC": "2944.45",
      "ACC_TRDVOL": "1,026,510,465",
      "ACC_TRDVAL": "25,011,393,960,858"
    },
    {
      "IDX_NM": "코스피 200",
      "TDD_OPNPRC": "390.12",
      "TDD_HGPRC": "399.55",
      "TDD_LWPRC": "389.23",
      "TDD_CLSPRC": "398.89",
      "ACC_TRDVOL": "850,234,123",
      "ACC_TRDVAL": "18,500,234,567,890"
    }
  ]
}
```

##### E. 지수 밸류에이션 - 기간별 (MDCSTAT00702)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00702` |
| **용도** | 특정 지수의 기간별 PER/PBR/배당수익률 조회 |
| **요청 파라미터** | `strtDd` (시작일), `endDd` (종료일), `indTpCd` (지수 그룹 ID), `indTpCd2` (지수 티커) |
| **응답 필드** | `TRD_DD`, `CLSPRC_IDX`, `FLUC_TP_CD`, `PRV_DD_CMPR`, `FLUC_RT`, `WT_PER`, `FWD_PER`, `WT_STKPRC_NETASST_RTO`, `DIV_YD` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT00702
strtDd=20210104
endDd=20210108
indTpCd=1
indTpCd2=001
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "TRD_DD": "2021/01/04",
      "CLSPRC": "2944.45",
      "FLUC_RT": "2.42",
      "PER": "28.12",
      "FWD_PER": "25.34",
      "PBR": "1.15",
      "DVD_YLD": "1.52"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `TRD_DD` | STRING | 거래일 (YYYY/MM/DD) | `2021/01/04` |
| `CLSPRC` | STRING | 종가 (콤마 포함, 소수점 2자리) | `2944.45` |
| `FLUC_RT` | STRING | 등락률 (%) | `2.42` |
| `PER` | STRING | 주가수익비율 | `28.12` |
| `FWD_PER` | STRING | 선행 주가수익비율 | `25.34` |
| `PBR` | STRING | 주가순자산비율 | `1.15` |
| `DVD_YLD` | STRING | 배당수익률 (%) | `1.52` |

##### F. 지수 밸류에이션 - 전체 지수 (MDCSTAT00701)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00701` |
| **용도** | 특정 일자 전체 지수 밸류에이션 조회 |
| **요청 파라미터** | `trdDd` (YYYYMMDD), `mktId` (1=KOSPI, 2=KOSDAQ) |
| **응답 필드** | `IDX_NM`, `CLSPRC`, `FLUC_RT`, `PER`, `FWD_PER`, `PBR`, `DVD_YLD` |

##### G. 지수 등락률 (MDCSTAT00201)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00201` |
| **용도** | 특정 기간 전체 지수 등락률 조회 |
| **요청 파라미터** | `strtDd` (시작일), `endDd` (종료일), `idxIndMidclssCd` (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마) |
| **응답 필드** | `IDX_IND_NM`, `OPN_DD_INDX`, `END_DD_INDX`, `FLUC_TP`, `PRV_DD_CMPR`, `FLUC_RT`, `ACC_TRDVOL`, `ACC_TRDVAL` |

**요청 예시**:
```
bld=dbms/MDC/STAT/standard/MDCSTAT00201
strtDd=20210101
endDd=20210130
idxIndMidclssCd=02
```

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "IDX_NM": "코스피",
      "OPNPRC": "2873.47",
      "CLSPRC": "3041.16",
      "FLUC_RT": "5.83",
      "ACC_TRDVOL": "18,234,567,890",
      "ACC_TRDVAL": "450,123,456,789,012"
    },
    {
      "IDX_NM": "코스피 200",
      "OPNPRC": "390.01",
      "CLSPRC": "412.45",
      "FLUC_RT": "5.76",
      "ACC_TRDVOL": "15,123,456,789",
      "ACC_TRDVAL": "380,234,567,890,123"
    }
  ]
}
```

##### H. 지수 기본정보 (MDCSTAT00401)

| 항목 | 내용 |
|------|------|
| **bld** | `dbms/MDC/STAT/standard/MDCSTAT00401` (티커 목록과 동일) |
| **용도** | 지수별 상장일, 기준지수, 종목수 조회 |
| **요청 파라미터** | `idxIndMidclssCd` (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마) |
| **응답 필드** | `IDX_NM`, `IDX_ENG_NM`, `BAS_TM_CONTN`, `ANNC_TM_CONTN`, `BAS_IDX_CONTN`, `CALC_CYCLE_CONTN`, `CALC_TM_CONTN`, `COMPST_ISU_CNT`, `IND_TP_CD`, `IDX_IND_CD` |

**응답 예시**:
```json
{
  "OutBlock_1": [
    {
      "IDX_IND_CD": "1001",
      "IDX_NM": "코스피",
      "BASE_TM": "1980.01.04",
      "ANN_TM": "1983.01.04",
      "BASE_IDX": "100",
      "COMPST_ISU_CNT": "900"
    }
  ]
}
```

**응답 필드 명세**:

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `IDX_IND_CD` | STRING | 지수 코드 | `1001` |
| `IDX_NM` | STRING | 지수명 | `코스피` |
| `BASE_TM` | STRING | 기준시점 (YYYY.MM.DD) | `1980.01.04` |
| `ANN_TM` | STRING | 발표시점 (YYYY.MM.DD) | `1983.01.04` |
| `BASE_IDX` | STRING | 기준지수 | `100` |
| `COMPST_ISU_CNT` | STRING | 종목수 | `900` |

### 2.2. pykrx 라이브러리 참고

pykrx는 KRX API를 Python으로 래핑한 라이브러리로, 다음 함수들을 제공합니다:

| pykrx 함수 | pykrx 클래스명 | KRX API bld | 용도 |
|-----------|--------------|------------|------|
| `get_index_ticker_list()` | 전체지수기본정보 | `MDCSTAT00401` | 지수 티커 목록 |
| `get_index_ticker_name()` | 전체지수기본정보 | `MDCSTAT00401` | 지수 코드 → 지수명 |
| `get_index_portfolio_deposit_file()` | 지수구성종목 | `MDCSTAT00601` | 지수 구성 종목 |
| `get_index_ohlcv_by_date()` | 개별지수시세 | `MDCSTAT00301` | 지수 OHLCV (기간별) |
| `get_index_ohlcv_by_ticker()` | 전체지수시세 | `MDCSTAT00101` | 지수 OHLCV (전체 지수) |
| `get_index_fundamental_by_date()` | PER_PBR_배당수익률_개별지수 | `MDCSTAT00702` | 지수 밸류에이션 (기간별) |
| `get_index_fundamental_by_ticker()` | PER_PBR_배당수익률_전지수 | `MDCSTAT00701` | 지수 밸류에이션 (전체 지수) |
| `get_index_price_change_by_ticker()` | 전체지수등락률 | `MDCSTAT00201` | 지수 등락률 |
| `get_index_listing_date()` | 전체지수기본정보 | `MDCSTAT00401` | 지수 기본정보 |

**참고**: KFC에서는 pykrx를 직접 사용하지 않고, 동일한 KRX API를 Kotlin으로 구현합니다.

### 2.3. 데이터 소스 한계 및 대안

| 정보 | KRX 제공 여부 | 대안 |
|------|-------------|------|
| 지수명, 티커 | ✅ 제공 | - |
| 구성 종목 | ✅ 제공 | - |
| OHLCV | ✅ 제공 | - |
| PER/PBR/배당수익률 | ✅ 제공 | - |
| 기준시점, 발표시점 | ✅ 제공 | - |
| 섹터별 세부 분류 | ⚠️ 일부 제공 | 별도 매핑 필요 시 고려 |

---

## 3. 도메인 모델 설계

### 3.1. 패키지 구조

**dev.kairoscode.kfc.domain.index 패키지**:

| 파일명 | 설명 |
|-------|------|
| IndexInfo.kt | 지수 기본정보 모델 |
| IndexOhlcv.kt | 지수 OHLCV 모델 |
| IndexOhlcvSnapshot.kt | 전체 지수 OHLCV 스냅샷 |
| IndexFundamental.kt | 지수 밸류에이션 모델 |
| IndexFundamentalSnapshot.kt | 전체 지수 밸류에이션 스냅샷 |
| IndexPriceChange.kt | 지수 등락률 모델 |
| IndexConstituent.kt | 지수 구성 종목 모델 |
| IndexMarket.kt | 지수 시장 Enum |

### 3.2. 핵심 모델 명세

#### 3.2.1. IndexInfo (지수 기본정보)

지수의 메타데이터를 담는 모델입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `ticker` | String | 지수 코드 | `1001` |
| `name` | String | 지수명 | `코스피` |
| `market` | IndexMarket | 시장 구분 | `IndexMarket.KOSPI` |
| `baseDate` | LocalDate? | 기준시점 | `1980-01-04` |
| `announcementDate` | LocalDate? | 발표시점 | `1983-01-04` |
| `baseIndex` | BigDecimal? | 기준지수 | `100.00` |
| `constituentCount` | Int? | 구성 종목수 | `900` |

**설계 의도**:
- 지수의 기본 속성을 한 곳에 집약
- null 허용: 일부 정보는 KRX API에서 제공하지 않을 수 있음
- 지수 분석 및 비교에 활용

#### 3.2.2. IndexOhlcv (지수 OHLCV)

특정 지수의 일별 시가/고가/저가/종가/거래량 정보입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `date` | LocalDate | 거래일 | `2021-01-04` |
| `ticker` | String | 지수 코드 | `1001` |
| `open` | BigDecimal | 시가 | `2874.50` |
| `high` | BigDecimal | 고가 | `2946.54` |
| `low` | BigDecimal | 저가 | `2869.11` |
| `close` | BigDecimal | 종가 | `2944.45` |
| `volume` | Long | 거래량 | `1026510465` |
| `tradingValue` | Long? | 거래대금 | `25011393960858` |

**설계 의도**:
- 지수는 소수점 2자리까지 표현되므로 `BigDecimal` 사용
- 주식 OHLCV와 구분하여 별도 모델로 관리
- 시계열 분석 및 차트 생성에 활용

#### 3.2.3. IndexOhlcvSnapshot (전체 지수 OHLCV 스냅샷)

특정 일자의 전체 지수 OHLCV 조회 결과입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `name` | String | 지수명 | `코스피` |
| `open` | BigDecimal | 시가 | `2874.50` |
| `high` | BigDecimal | 고가 | `2946.54` |
| `low` | BigDecimal | 저가 | `2869.11` |
| `close` | BigDecimal | 종가 | `2944.45` |
| `volume` | Long | 거래량 | `1026510465` |
| `tradingValue` | Long? | 거래대금 | `25011393960858` |

**설계 의도**:
- 특정 시점의 전체 지수 스냅샷 제공
- 지수명 기준으로 조회 (ticker 불필요)
- 시장 전체 현황 파악에 활용

#### 3.2.4. IndexFundamental (지수 밸류에이션)

PER, PBR, 배당수익률 등 지수 밸류에이션 지표입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `date` | LocalDate | 거래일 | `2021-01-04` |
| `ticker` | String | 지수 코드 | `1001` |
| `close` | BigDecimal | 종가 | `2944.45` |
| `changeRate` | BigDecimal? | 등락률 (%) | `2.42` |
| `per` | BigDecimal? | 주가수익비율 | `28.12` |
| `forwardPer` | BigDecimal? | 선행 주가수익비율 | `25.34` |
| `pbr` | BigDecimal? | 주가순자산비율 | `1.15` |
| `dividendYield` | BigDecimal? | 배당수익률 (%) | `1.52` |

**설계 의도**:
- 시장 전체 밸류에이션 평가에 활용
- null 허용: 일부 지수는 PER/PBR 제공하지 않을 수 있음
- 역사적 밸류에이션 추이 분석 가능

#### 3.2.5. IndexFundamentalSnapshot (전체 지수 밸류에이션 스냅샷)

특정 일자의 전체 지수 밸류에이션 조회 결과입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `name` | String | 지수명 | `코스피` |
| `close` | BigDecimal | 종가 | `2944.45` |
| `changeRate` | BigDecimal? | 등락률 (%) | `2.42` |
| `per` | BigDecimal? | 주가수익비율 | `28.12` |
| `forwardPer` | BigDecimal? | 선행 주가수익비율 | `25.34` |
| `pbr` | BigDecimal? | 주가순자산비율 | `1.15` |
| `dividendYield` | BigDecimal? | 배당수익률 (%) | `1.52` |

#### 3.2.6. IndexPriceChange (지수 등락률)

특정 기간 동안의 지수 등락률 정보입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `name` | String | 지수명 | `코스피` |
| `openPrice` | BigDecimal | 시작일 시가 | `2873.47` |
| `closePrice` | BigDecimal | 종료일 종가 | `3041.16` |
| `changeRate` | BigDecimal | 등락률 (%) | `5.83` |
| `volume` | Long | 누적 거래량 | `18234567890` |
| `tradingValue` | Long? | 누적 거래대금 | `450123456789012` |

**설계 의도**:
- 특정 기간 동안의 지수 수익률 계산
- 기간별 성과 비교에 활용
- 섹터 로테이션 분석 가능

#### 3.2.7. IndexConstituent (지수 구성 종목)

특정 지수에 포함된 종목 목록입니다.

| 필드명 | 타입 | 설명 | 예시 |
|-------|------|------|------|
| `indexTicker` | String | 지수 코드 | `1028` |
| `indexName` | String | 지수명 | `코스피 200` |
| `constituents` | List<String> | 구성 종목 티커 리스트 | `["005930", "000660", ...]` |
| `asOfDate` | LocalDate | 기준일 | `2021-01-04` |

**설계 의도**:
- 지수 추종 ETF 포트폴리오 구성에 활용
- 지수 리밸런싱 분석
- 구성 종목별 비중 계산 가능 (별도 API 조합)

#### 3.2.8. Enum 클래스

**IndexMarket (지수 시장 구분)**

| code | koreanName |
|------|-----------|
| 1 | 코스피 |
| 2 | 코스닥 |
| 3 | 파생 |
| ALL | 전체 |

### 3.3. 헬퍼 함수 명세

편의성을 위한 확장 함수를 제공합니다.

| 대상 | 함수 | 설명 |
|------|------|------|
| `IndexInfo` | `isKospi()`, `isKosdaq()` | 시장 구분 확인 |
| `IndexOhlcv` | `calculateReturn()`, `isPriceRising()` | 수익률 계산, 상승 여부 |
| `List<IndexOhlcv>` | `toTimeSeries()`, `calculateVolatility()` | 시계열 변환, 변동성 계산 |

---

## 4. API 레이어 설계

### 4.1. IndexApi 인터페이스

#### 4.1.1. 패키지 위치

`dev.kairoscode.kfc.api.IndexApi`

#### 4.1.2. 메서드 명세

##### A. 기본 정보 API

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getIndexList` | List<IndexInfo> | `market` | 시장별 지수 목록 조회 |
| `getIndexName` | String? | `ticker` | 지수 코드 → 지수명 조회 |
| `getIndexInfo` | IndexInfo? | `ticker` | 지수 기본정보 조회 |
| `getIndexConstituents` | List<String> | `ticker`, `date` | 지수 구성 종목 티커 리스트 |

##### B. OHLCV API

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getOhlcvByDate` | List<IndexOhlcv> | `ticker`, `fromDate`, `toDate` | 특정 지수 기간별 OHLCV |
| `getOhlcvByTicker` | List<IndexOhlcvSnapshot> | `date`, `market` | 특정 일자 전체 지수 OHLCV |

##### C. 밸류에이션 API

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getFundamentalByDate` | List<IndexFundamental> | `ticker`, `fromDate`, `toDate` | 특정 지수 기간별 밸류에이션 |
| `getFundamentalByTicker` | List<IndexFundamentalSnapshot> | `date`, `market` | 특정 일자 전체 지수 밸류에이션 |

##### D. 등락률 API

| 메서드명 | 반환 타입 | 파라미터 | 설명 |
|---------|----------|---------|------|
| `getPriceChange` | List<IndexPriceChange> | `fromDate`, `toDate`, `market` | 기간별 전체 지수 등락률 |

#### 4.1.3. 공통 규약

- 모든 메서드는 `suspend` 함수로 비동기 처리
- 실패 시 `KfcException` 예외 발생 (네트워크 에러, 파싱 실패, API 에러)
- 존재하지 않는 데이터 조회 시 `null` 반환 (예외 미발생)
- 날짜 파라미터 기본값: `LocalDate.now()`
- 시장 파라미터 기본값: `IndexMarket.ALL`

### 4.2. KfcClient 통합

기존 `KfcClient`에 `index` 속성을 추가합니다.

**KfcClient 속성**:

| 속성명 | 타입 | nullable | 설명 |
|-------|------|---------|------|
| funds | FundsApi | No | 펀드 API |
| price | PriceApi | No | 가격 API |
| corp | CorpApi | Yes | 기업 API (API Key 필요) |
| stock | StockApi | No | 주식 API |
| index | IndexApi | No | 지수 API (신규 추가) |
| financials | FinancialsApi | Yes | 재무 API (API Key 필요) |

**통합 방식**:
- `KfcClient.create()` 팩토리 메서드에서 `IndexApi` 인스턴스 생성
- KRX API 기반이므로 API Key 불필요
- `GlobalRateLimiters`를 통한 Rate Limiting 적용

### 4.3. 사용 예시

**기본 사용 패턴**:

| 시나리오 | 메서드 호출 | 예상 결과 |
|---------|----------|----------|
| 코스피 지수 목록 | `kfc.index.getIndexList(IndexMarket.KOSPI)` | List<IndexInfo> |
| 지수명 조회 | `kfc.index.getIndexName("1001")` | "코스피" |
| 구성 종목 조회 | `kfc.index.getIndexConstituents("1028", date)` | List<String> |
| OHLCV 조회 | `kfc.index.getOhlcvByDate("1001", from, to)` | List<IndexOhlcv> |
| 밸류에이션 조회 | `kfc.index.getFundamentalByDate("1001", from, to)` | List<IndexFundamental> |
| 등락률 조회 | `kfc.index.getPriceChange(from, to, market)` | List<IndexPriceChange> |

---

## 5. 인프라 레이어 설계

### 5.1. 패키지 구조

**dev.kairoscode.kfc.infrastructure.krx 패키지**:

| 경로 | 파일명 | 설명 |
|------|-------|------|
| krx/ | KrxIndexApiImpl.kt | KRX 지수 API 구현체 |
| krx/dto/ | IndexListResponse.kt | 지수 목록 응답 DTO |
| krx/dto/ | IndexOhlcvResponse.kt | 지수 OHLCV 응답 DTO |
| krx/dto/ | IndexFundamentalResponse.kt | 지수 밸류에이션 응답 DTO |

### 5.2. 구현 패턴

#### 5.2.1. KrxIndexApiImpl

**주요 책임**:

| 책임 | 설명 |
|------|------|
| HTTP 요청 수행 | 기존 `KrxHttpClient` 재사용 |
| 응답 데이터 변환 | KRX API JSON → 도메인 모델 |
| 데이터 정규화 | 콤마 제거, 날짜 형식 변환 |
| Rate Limiting | GlobalRateLimiters 사용 |
| 에러 핸들링 | 재시도 로직 및 예외 변환 |

#### 5.2.2. DTO 매핑 전략

**매핑 원칙**:
- KRX API 응답 필드명을 그대로 사용 (snake_case 또는 UPPER_CASE)
- DTO → Domain Model 변환 로직은 별도 매퍼 함수로 분리
- null 안전성 보장: KRX API에서 누락된 필드는 null로 처리

**IndexOhlcvDto 예시**:

| DTO 필드 | 타입 | 예시 값 | 도메인 필드 | 변환 함수 |
|---------|------|--------|-----------|----------|
| TRD_DD | String | "2021/01/04" | date | parseKrxDate() |
| TDD_OPNPRC | String | "2,874.50" | open | parseKrxDecimal() |
| TDD_HGPRC | String | "2,946.54" | high | parseKrxDecimal() |
| TDD_LWPRC | String | "2,869.11" | low | parseKrxDecimal() |
| TDD_CLSPRC | String | "2,944.45" | close | parseKrxDecimal() |
| ACC_TRDVOL | String | "1,026,510,465" | volume | parseKrxLong() |
| ACC_TRDVAL | String? | "25,011,393,960,858" | tradingValue | parseKrxLong() |

#### 5.2.3. 유틸리티 함수

기존 Stock API 구현과 동일한 파싱 유틸리티를 재사용합니다.

| 함수명 | 입력 예시 | 출력 타입 | 설명 |
|-------|---------|----------|------|
| parseKrxDate() | "2021/01/04" | LocalDate | KRX 날짜 형식 파싱 |
| parseKrxDecimal() | "2,874.50" | BigDecimal | 콤마 제거 후 숫자 변환 |
| parseKrxLong() | "1,026,510,465" | Long | 콤마 제거 후 정수 변환 |
| parseKrxInt() | "900" | Int | 문자열을 정수로 변환 |

---

## 6. 구현 우선순위

### Phase 1: 기본 정보 API (MVP)

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 1 | IndexApi 인터페이스 | 전체 메서드 시그니처 정의 | 0.5일 |
| 2 | 도메인 모델 | `IndexInfo`, `IndexOhlcv`, `IndexFundamental` 등 | 0.5일 |
| 3 | 지수 목록 API | `getIndexList`, `getIndexName` | 0.5일 |
| 4 | 지수 구성 종목 API | `getIndexConstituents` | 0.5일 |
| 5 | KfcClient 통합 | `index` 속성 추가 및 팩토리 메서드 수정 | 0.5일 |
| **합계** | | | **2.5일** |

### Phase 2: OHLCV 및 밸류에이션 API

| 우선순위 | 항목 | 범위 | 예상 공수 |
|---------|------|------|----------|
| 6 | 지수 OHLCV API | `getOhlcvByDate`, `getOhlcvByTicker` | 1일 |
| 7 | 지수 밸류에이션 API | `getFundamentalByDate`, `getFundamentalByTicker` | 1일 |
| 8 | 지수 등락률 API | `getPriceChange` | 0.5일 |
| 9 | 지수 기본정보 API | `getIndexInfo` (상장일, 기준지수 등) | 0.5일 |
| **합계** | | | **3일** |

### Phase 3: 고도화

| 항목 | 범위 | 우선순위 |
|------|------|---------|
| 캐싱 전략 | 지수 목록, 구성 종목 캐싱 | 중간 |
| 섹터별 세부 분류 | 산업별 지수 그룹화 | 낮음 |
| 히스토리 관리 | 지수 구성 종목 변경 이력 추적 | 낮음 |

### 총 예상 공수

| Phase | 범위 | 예상 공수 |
|-------|------|----------|
| Phase 1 | 기본정보 (MVP) | 2.5일 |
| Phase 2 | OHLCV 및 밸류에이션 | 3일 |
| **총계** | | **5.5일** |

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
| 5006 | 5000번대 (검증) | `INVALID_INDEX_TICKER` | 지수 코드 형식이 올바르지 않습니다 |

> **참고**: `INDEX_NOT_FOUND`는 null 반환으로 처리하므로 에러 코드 불필요

### 7.2. 예외 처리 시나리오

| 시나리오 | 에러 코드 | 처리 방법 |
|---------|----------|----------|
| 잘못된 지수 코드 형식 | `INVALID_INDEX_TICKER(5006)` | 형식 검증 후 예외 발생 |
| 존재하지 않는 지수 | - | null 반환 (예외 발생 안함) |
| KRX API 응답 오류 | `KRX_API_ERROR(3001)` | 재시도 로직 또는 명확한 에러 메시지 |
| 파싱 실패 | `JSON_PARSE_ERROR(2001)` | 로그 기록 후 예외 발생 |
| 네트워크 에러 | `NETWORK_CONNECTION_FAILED(1001)` | 재시도 로직 (기존 `KrxHttpClient` 사용) |

### 7.3. 에러 처리 패턴

| 시나리오 | 처리 방법 | 사용자 메시지 |
|---------|----------|-------------|
| 네트워크 연결 실패 | KfcException 발생 | "네트워크 연결에 실패했습니다. 잠시 후 다시 시도해주세요." |
| 데이터 파싱 오류 | KfcException 발생 | "지수 데이터를 처리하는 중 오류가 발생했습니다." |
| 존재하지 않는 지수 | null 반환 | "해당 지수를 찾을 수 없습니다." |
| 잘못된 지수 코드 형식 | KfcException 발생 | "지수 코드 형식이 올바르지 않습니다." |

---

## 8. 참고 자료

### 8.1. 공식 문서

- [KRX 정보데이터시스템](https://data.krx.co.kr)
- [KRX 지수 정보](https://data.krx.co.kr/contents/MDC/MDI/mdiLoader/index.cmd?menuId=MDC02)

### 8.2. 오픈소스 라이브러리

- [pykrx](https://github.com/sharebook-kr/pykrx) - Python KRX API 래퍼
- [pykrx 문서](https://github.com/sharebook-kr/pykrx/wiki)

### 8.3. 기술 블로그

- [파이썬으로 주식 데이터 수집하기 (pykrx)](https://wikidocs.net/153861)

### 8.4. 내부 문서

- [pykrx Gap 분석](/home/ulalax/project/kairos/kfc/doc/pykrx-gap-analysis.md)
- [아키텍처 가이드](/home/ulalax/project/kairos/kfc/doc/archtecture-guide.md)
- [Stock 기술명세서](/home/ulalax/project/kairos/kfc/doc/specs/stock-기술명세서.md)

### 8.5. 기존 구현체 참고

- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxHttpClient.kt`
- `/home/ulalax/project/kairos/kfc/src/main/kotlin/dev/kairoscode/kfc/infrastructure/krx/KrxStockApiImpl.kt`

---

## 부록: KRX API 매핑 테이블

### A. 시장 코드 매핑

| KRX 코드 | KFC Enum | 한글명 | 설명 |
|---------|---------|--------|------|
| `1` | `IndexMarket.KOSPI` | 코스피 | 한국거래소 유가증권시장 지수 |
| `2` | `IndexMarket.KOSDAQ` | 코스닥 | 코스닥시장 지수 |
| `3` | `IndexMarket.DERIVATIVES` | 파생 | 파생상품 관련 지수 |
| `ALL` | `IndexMarket.ALL` | 전체 | 모든 시장 통합 |

### B. 주요 지수 예시

| 지수 코드 | 지수명 | 시장 | 설명 |
|---------|-------|------|------|
| 1001 | 코스피 | KOSPI | 유가증권시장 대표지수 |
| 1002 | 대형주 | KOSPI | 시가총액 상위 대형주 |
| 1003 | 중형주 | KOSPI | 시가총액 중간 종목 |
| 1004 | 소형주 | KOSPI | 시가총액 하위 소형주 |
| 1028 | 코스피 200 | KOSPI | 시가총액 상위 200개 종목 |
| 2001 | 코스닥 | KOSDAQ | 코스닥시장 대표지수 |
| 2203 | 코스닥 150 | KOSDAQ | 시가총액 상위 150개 종목 |

### C. API 엔드포인트 매핑

| 기능 | KRX API bld | pykrx 클래스 | IndexApi 메서드 |
|------|-------------|-------------|-----------------|
| 지수 목록 | `MDCSTAT00401` | 전체지수기본정보 | `getIndexList()` |
| 지수명 | `MDCSTAT00401` | 전체지수기본정보 | `getIndexName()` |
| 구성 종목 | `MDCSTAT00601` | 지수구성종목 | `getIndexConstituents()` |
| OHLCV (기간별) | `MDCSTAT00301` | 개별지수시세 | `getOhlcvByDate()` |
| OHLCV (전체) | `MDCSTAT00101` | 전체지수시세 | `getOhlcvByTicker()` |
| 밸류에이션 (기간별) | `MDCSTAT00702` | PER_PBR_배당수익률_개별지수 | `getFundamentalByDate()` |
| 밸류에이션 (전체) | `MDCSTAT00701` | PER_PBR_배당수익률_전지수 | `getFundamentalByTicker()` |
| 등락률 | `MDCSTAT00201` | 전체지수등락률 | `getPriceChange()` |
| 기본정보 | `MDCSTAT00401` | 전체지수기본정보 | `getIndexInfo()` |

### D. 응답 필드 매핑

| KRX 필드 | 타입 | 도메인 필드 | 변환 로직 |
|---------|------|-----------|----------|
| `IDX_IND_CD` | STRING | `ticker` | 그대로 사용 |
| `IDX_NM` | STRING | `name` | 그대로 사용 |
| `TRD_DD` | STRING | `date` | `parseKrxDate()` |
| `TDD_OPNPRC` | STRING | `open` | `parseKrxDecimal()` |
| `TDD_HGPRC` | STRING | `high` | `parseKrxDecimal()` |
| `TDD_LWPRC` | STRING | `low` | `parseKrxDecimal()` |
| `TDD_CLSPRC` | STRING | `close` | `parseKrxDecimal()` |
| `ACC_TRDVOL` | STRING | `volume` | `parseKrxLong()` |
| `ACC_TRDVAL` | STRING | `tradingValue` | `parseKrxLong()` |
| `PER` | STRING | `per` | `parseKrxDecimal()` |
| `FWD_PER` | STRING | `forwardPer` | `parseKrxDecimal()` |
| `PBR` | STRING | `pbr` | `parseKrxDecimal()` |
| `DVD_YLD` | STRING | `dividendYield` | `parseKrxDecimal()` |
| `BASE_TM` | STRING | `baseDate` | `parseKrxDate()` |
| `ANN_TM` | STRING | `announcementDate` | `parseKrxDate()` |
| `BASE_IDX` | STRING | `baseIndex` | `parseKrxDecimal()` |
| `COMPST_ISU_CNT` | STRING | `constituentCount` | `parseKrxInt()` |

---

**문서 끝**
