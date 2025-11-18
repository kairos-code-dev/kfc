# KRX ETF 엔드포인트 분석

## 개요

본 문서는 "증권상품 > ETF" 및 "통계 > 공매도 통계" 섹션에서 사용 가능한 모든 KRX ETF 및 공매도 엔드포인트의 포괄적인 카탈로그를 제공합니다. 각 엔드포인트는 BLD 코드, 목적, 파라미터, 응답 필드, 사용 예제와 함께 문서화되어 있습니다.

**총 엔드포인트**: 15개 (ETF: 13개, 공매도: 2개)

## 엔드포인트 카테고리

1. **티커 및 기본 정보** (2개 엔드포인트)
2. **가격 및 시장 데이터** (4개 엔드포인트)
3. **포트폴리오 구성** (1개 엔드포인트)
4. **성과 및 추적** (2개 엔드포인트)
5. **투자자별 거래 패턴** (4개 엔드포인트)
6. **공매도 데이터** (2개 엔드포인트)

---

## 카테고리 1: 티커 및 기본 정보

### 1.1 finder_secuprodisu - 상장종목검색

**BLD 코드**: `dbms/comm/finder/finder_secuprodisu`

**목적**: 기본 메타데이터로 모든 ETF/ETN/ELW 티커 검색 및 나열

**사용 사례**: 초기 티커 발견, 드롭다운 채우기, 티커 검증

**URL**: `http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd`

**HTTP 메서드**: POST

**헤더**:
```
User-Agent: Mozilla/5.0
Referer: http://data.krx.co.kr/
Content-Type: application/x-www-form-urlencoded
```

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 | 값 |
|---------|------|------|------|------|-----|
| mktsel | String | 아니오 | ALL/ETF | 시장 선택 | ALL (기본), ETF |
| searchText | String | 아니오 | Text | 이름 매칭을 위한 검색어 | 비어있음 = 전체, 예: "KODEX" |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 | 예시 |
|---------|--------|------|------|------|
| full_code | 전체코드 | String | 전체 ISIN 코드 | KR7152100004 |
| short_code | 단축코드 | String | 6자리 티커 | 152100 |
| codeName | 종목명 | String | ETF 전체 이름 | ARIRANG 200 |

#### 응답 샘플

```json
{
  "block1": [
    {
      "full_code": "KR7152100004",
      "short_code": "152100",
      "codeName": "ARIRANG 200"
    },
    {
      "full_code": "KR7295820005",
      "short_code": "295820",
      "codeName": "ARIRANG 200동일가중"
    }
  ]
}
```

#### Kotlin 구현 예제

```kotlin
data class TickerInfo(
    val isin: String,
    val ticker: String,
    val name: String
)

fun searchEtfTickers(market: String = "ETF", searchTerm: String = ""): List<TickerInfo> {
    val resp = client.post("dbms/comm/finder/finder_secuprodisu", mapOf(
        "mktsel" to market,
        "searchText" to searchTerm
    ))
    val rows = (resp["block1"] as? List<*>) ?: emptyList()
    return rows.mapNotNull { it as? Map<*, *> }.map { m ->
        TickerInfo(
            isin = m["full_code"].toString(),
            ticker = m["short_code"].toString(),
            name = m["codeName"].toString()
        )
    }
}
```

---

### 1.2 MDCSTAT04601 - ETF 전종목 기본정보

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04601`

**목적**: 상세 메타데이터가 포함된 모든 ETF의 종합 목록

**사용 사례**: ETF 마스터 테이블, 초기 데이터 로드, 메타데이터 보강

**HTTP 메서드**: POST

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| 없음 | - | - | - | 모든 ETF 반환 |

#### 응답 필드

| KRX 필드 | 한글명 | 데이터 타입 (원본 → 정규화) | 설명 | 예시 |
|---------|--------|---------------------------|------|------|
| ISU_CD | 종목코드 | String | 전체 ISIN 코드 | KR7292340007 |
| ISU_SRT_CD | 단축코드 | String → String | 6자리 티커 | 292340 |
| ISU_NM | 종목명 | String | 전체 공식 이름 | DB 마이티 200커버드콜ATM레버리지증권상장지수투자신탁[주식-파생형] |
| ISU_ABBRV | 종목약명 | String | 약식 이름 | 마이티 200커버드콜ATM레버리지 |
| ISU_ENG_NM | 영문명 | String | 영문 이름 | DB Mighty KOSPI200 Covered Call ATM Leverage ETF |
| LIST_DD | 상장일 | String → LocalDate | 상장일 | 2018/03/20 |
| ETF_OBJ_IDX_NM | 기초지수명 | String | 벤치마크 지수명 | 코스피 200 커버드콜 ATM 지수 |
| IDX_CALC_INST_NM1 | 지수산출기관 | String | 지수 제공기관 | KRX |
| IDX_CALC_INST_NM2 | 레버리지/인버스 | String | 레버리지/인버스 유형 | 2X 레버리지 (2) |
| ETF_REPLICA_METHD_TP_CD | 복제방법 | String | 복제 방법 | 실물, 합성 |
| IDX_MKT_CLSS_NM | 시장구분 | String | 시장 분류 | 국내, 해외 |
| IDX_ASST_CLSS_NM | 자산구분 | String | 자산군 | 주식, 채권, 상품, 파생 |
| LIST_SHRS | 상장주식수 | String → Long | 상장 주식 수 | 500,000 |
| COM_ABBRV | 운용사 | String | 자산 운용사 | 디비자산운용 |
| CU_QTY | CU 수량 | String → Long | 생성 단위 수량 | 100,000 |
| ETF_TOT_FEE | 총보수 | String → BigDecimal | 총 보수율 (%) | 0.510 |
| TAX_TP_CD | 과세유형 | String | 세금 유형 | 배당소득세(보유기간과세), 비과세 |

#### 응답 샘플

```json
{
  "output": [
    {
      "ISU_CD": "KR7292340007",
      "ISU_SRT_CD": "292340",
      "ISU_ABBRV": "마이티 200커버드콜ATM레버리지",
      "LIST_DD": "2018/03/20",
      "ETF_TOT_FEE": "0.510",
      "LIST_SHRS": "500,000",
      "IDX_ASST_CLSS_NM": "주식"
    }
  ]
}
```

#### Kotlin 구현 예제

```kotlin
data class EtfBasicInfo(
    val isin: String,
    val ticker: String,
    val name: String,
    val listingDate: LocalDate,
    val benchmark: String,
    val assetClass: String,
    val totalFee: BigDecimal,
    val listedShares: Long
)

fun getAllEtfBasicInfo(): List<EtfBasicInfo> {
    val resp = client.post("dbms/MDC/STAT/standard/MDCSTAT04601", emptyMap())
    val rows = (resp["output"] as? List<*>) ?: emptyList()
    return rows.mapNotNull { it as? Map<*, *> }.map { m ->
        EtfBasicInfo(
            isin = m["ISU_CD"].toString(),
            ticker = m["ISU_SRT_CD"].toString(),
            name = m["ISU_ABBRV"].toString(),
            listingDate = m["LIST_DD"].toString().toKrxDate(),
            benchmark = m["ETF_OBJ_IDX_NM"].toString(),
            assetClass = m["IDX_ASST_CLSS_NM"].toString(),
            totalFee = m["ETF_TOT_FEE"].toString().toKrxBigDecimal(),
            listedShares = m["LIST_SHRS"].toString().toKrxLong()
        )
    }
}
```

---

## 카테고리 2: 가격 및 시장 데이터

### 2.1 MDCSTAT04301 - 전종목 시세 (일자별 전종목)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04301`

**목적**: 모든 ETF 가격, NAV, 거래량의 단일 날짜 스냅샷

**사용 사례**: 일일 시장 개요, 스크리너, 히트맵

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| trdDd | String | 예 | YYYYMMDD | 거래 날짜 | 20240102 |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 (원본 → 정규화) | 설명 | 예시 |
|---------|--------|---------------------|------|------|
| ISU_SRT_CD | 티커 | String | 6자리 티커 | 152100 |
| ISU_ABBRV | 종목명 | String | ETF 이름 | ARIRANG 200 |
| TDD_CLSPRC | 종가 | String → Int | 종가 | 42,965 → 42965 |
| CMPPREVDD_PRC | 대비 | String → Int | 가격 변화 | 1,080 → 1080 |
| FLUC_TP_CD | 등락구분 | String → Int | 1=상승, 2=하락, 3=보합 | 1 |
| FLUC_RT | 등락률 | String → Double | 변화율 (%) | 2.58 |
| NAV | NAV | String → BigDecimal | 순자산가치 | 43,079.14 |
| TDD_OPNPRC | 시가 | String → Int | 시가 | 42,075 |
| TDD_HGPRC | 고가 | String → Int | 고가 | 43,250 |
| TDD_LWPRC | 저가 | String → Int | 저가 | 41,900 |
| ACC_TRDVOL | 거래량 | String → Long | 거래량 | 192,061 |
| ACC_TRDVAL | 거래대금 | String → Long | 거래대금 (원) | 8,222,510,755 |
| MKTCAP | 시가총액 | String → Long | 시가총액 | 850,707,000,000 |
| INVSTASST_NETASST_TOTAMT | 순자산총액 | String → Long | 순자산 총액 | 0 (종종 0) |
| LIST_SHRS | 상장주식수 | String → Long | 상장 주식 수 | 19,800,000 |
| IDX_IND_NM | 지수명 | String | 지수명 | 코스피 200 |
| OBJ_STKPRC_IDX | 지수 | String → BigDecimal | 지수 값 | 421.35 |
| CMPPREVDD_IDX | 지수대비 | String → BigDecimal | 지수 변화 | 10.85 |
| FLUC_TP_CD1 | 지수등락구분 | String → Int | 지수 방향 | 1 |
| FLUC_RT1 | 지수등락률 | String → Double | 지수 변화율 | 2.64 |

#### 사용 참고사항

- 일일 시장 스냅샷에 이 엔드포인트 사용
- 보강된 메타데이터를 위해 04601과 결합
- NAV 값은 괴리율 계산에 중요

---

### 2.2 MDCSTAT04401 - 전종목 등락률 (기간별 등락률)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04401`

**목적**: 모든 ETF에 대한 기간 대비 기간 가격 변화

**사용 사례**: 성과 비교, 순위, 기간 수익률

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 | 20240101 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 | 20240105 |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| ISU_SRT_CD | 티커 | String | 티커 |
| ISU_ABBRV | 종목명 | String | 이름 |
| BAS_PRC | 시작가 | String → Int | 시작 가격 |
| CLSPRC | 종가 | String → Int | 종료 가격 |
| FLUC_TP_CD | 등락구분 | String → Int | 방향 |
| CMP_PRC | 등락폭 | String → Int | 가격 변화 |
| FLUC_RT | 등락률 | String → Double | 변화율 (%) |
| ACC_TRDVOL | 누적거래량 | String → Long | 누적 거래량 |
| ACC_TRDVAL | 누적거래대금 | String → Long | 누적 거래대금 |

---

### 2.3 MDCSTAT04501 - 개별종목 시세 추이 (개별 ETF OHLCV)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04501`

**목적**: 단일 ETF에 대한 과거 OHLCV 데이터

**사용 사례**: 가격 차트, 백테스팅, 기술적 분석

**우선순위**: 높음 (가장 일반적으로 사용됨)

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 |
| isuCd | String | 예 | ISIN | 전체 ISIN 코드 (예: KR7152100004) |

#### 날짜 범위 처리

- **최대**: 요청당 730일
- **자동 분할**: KrxClient가 자동으로 730일 이상 범위 분할
- **병합**: 결과는 시간순으로 병합됨

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| TRD_DD | 날짜 | String → LocalDate | 거래일 |
| TDD_CLSPRC | 종가 | String → Int | 종가 |
| FLUC_TP_CD | 등락구분 | String → Int | 방향 |
| CMPPREVDD_PRC | 대비 | String → Int | 변화 |
| FLUC_RT | 등락률 | String → Double | 변화율 % |
| LST_NAV | NAV | String → BigDecimal | 순자산가치 |
| TDD_OPNPRC | 시가 | String → Int | 시가 |
| TDD_HGPRC | 고가 | String → Int | 고가 |
| TDD_LWPRC | 저가 | String → Int | 저가 |
| ACC_TRDVOL | 거래량 | String → Long | 거래량 |
| ACC_TRDVAL | 거래대금 | String → Long | 거래대금 |
| MKTCAP | 시가총액 | String → Long | 시가총액 |
| INVSTASST_NETASST_TOTAMT | 순자산총액 | String → Long | 순자산 |
| LIST_SHRS | 상장주식수 | String → Long | 상장 주식 수 |
| IDX_IND_NM | 지수명 | String | 지수명 |
| OBJ_STKPRC_IDX | 지수 | String → BigDecimal | 지수 값 |
| FLUC_TP_CD1 | 지수등락구분 | String → Int | 지수 방향 |
| CMPPREVDD_IDX | 지수대비 | String → BigDecimal | 지수 변화 |
| IDX_FLUC_RT | 지수등락률 | String → Double | 지수 변화율 % |

#### Kotlin 구현 (현재)

```kotlin
fun getEtfOhlcvByDate(fromDate: String, toDate: String, ticker: String): List<Map<String, Any>> {
    val isin = etx.getIsin(ticker)
    val resp = client.post("dbms/MDC/STAT/standard/MDCSTAT04501", mapOf(
        "strtDd" to fromDate, "endDd" to toDate, "isuCd" to isin
    ))
    val rows = (resp["output"] as? List<*>) ?: return emptyList()
    return rows.mapNotNull { it as? Map<*, *> }.map { m ->
        mapOf(
            "날짜" to m["TRD_DD"].toString().toKrxDate().toString(),
            "NAV" to m["LST_NAV"].toString().toKrxBigDecimal(),
            "시가" to m["TDD_OPNPRC"].toString().toKrxInt(),
            "고가" to m["TDD_HGPRC"].toString().toKrxInt(),
            "저가" to m["TDD_LWPRC"].toString().toKrxInt(),
            "종가" to m["TDD_CLSPRC"].toString().toKrxInt(),
            "거래량" to m["ACC_TRDVOL"].toString().toKrxLong(),
            "거래대금" to m["ACC_TRDVAL"].toString().toKrxLong(),
            "기초지수" to m["OBJ_STKPRC_IDX"].toString().toKrxBigDecimal(),
        )
    }.sortedBy { it["날짜"].toString() }
}
```

---

### 2.4 MDCSTAT04701 - ETF 개별종목 종합정보 ⭐ 최우선 순위

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04701`

**목적**: **단일 요청으로 종합 ETF 정보** - 기본 정보, OHLCV, 시가총액, 보수, 52주 고가/저가 등을 결합

**사용 사례**: ETF 상세 페이지, 스냅샷 뷰, 데이터 완전성

**상태**: **pykrx에 미구현** - 이것이 최우선 순위 엔드포인트입니다

**최우선 순위인 이유**:
- 다른 방법으로는 여러 엔드포인트 호출이 필요한 30개 이상의 필드 제공
- `ETF_TOT_FEE` (총 보수율)와 같은 중요 데이터 포함
- `MKTCAP` (시가총액), `LST_NAV` (NAV), 52주 고가/저가 포함
- 단일 요청으로 지연 시간 및 API 부하 감소

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| trdDd | String | 예 | YYYYMMDD | 거래 날짜 |
| isuCd | String | 예 | ISIN | 전체 ISIN 코드 |

#### 응답 필드 (30개 이상 필드)

**상세 명세는 `03-MDCSTAT04701-상세명세.md` 참조**

주요 필드는 다음과 같습니다:
- MDCSTAT04501 (OHLCV)의 모든 필드
- MDCSTAT04601 (기본 정보)의 모든 필드
- **ETF_TOT_FEE**: 총 보수율 (04501에 없음)
- **WK52_HGPR**: 52주 고가
- **WK52_LWPR**: 52주 저가
- **IDX_ASST_CLSS_NM**: 자산군 (주식, 채권 등)
- **ETF_OBJ_IDX_NM**: 벤치마크 지수

#### 다른 엔드포인트와 비교

| 데이터 | 04501 (OHLCV) | 04601 (기본) | **04701 (종합)** |
|--------|--------------|--------------|------------------|
| OHLCV | ✅ | ❌ | ✅ |
| NAV | ✅ | ❌ | ✅ |
| 시가총액 | ✅ | ❌ | ✅ |
| 총 보수 | ❌ | ✅ | ✅ |
| 52주 고가/저가 | ❌ | ❌ | ✅ |
| 자산군 | ❌ | ✅ | ✅ |
| 벤치마크 | ✅ | ✅ | ✅ |
| 상장일 | ❌ | ✅ | ✅ |
| 운용사 | ❌ | ✅ | ✅ |

**권장사항**: 상세 페이지 및 데이터 완전성을 위해 04701 사용; 백테스팅(과거 범위)을 위해 04501 사용.

---

## 카테고리 3: 포트폴리오 구성

### 3.1 MDCSTAT05001 - PDF (Portfolio Deposit File)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT05001`

**목적**: ETF 구성 종목 보유 현황 (바스켓 구성)

**사용 사례**: 포트폴리오 분석, 복제, 구성 종목 추적

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| trdDd | String | 예 | YYYYMMDD | 거래 날짜 |
| isuCd | String | 예 | ISIN | ETF ISIN 코드 |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| COMPST_ISU_CD | 구성종목코드 | String | 구성 종목 티커 (혼합 ISIN/티커) |
| COMPST_ISU_NM | 구성종목명 | String | 구성 종목명 |
| COMPST_ISU_CU1_SHRS | 수량 | String → BigDecimal | CU당 주식 수 |
| VALU_AMT | 금액 | String → Long | 가치 (원) |
| COMPST_AMT | 구성금액 | String → Long | 구성 금액 |
| COMPST_RTO | 비중 | String → BigDecimal | 가중치 (%) |

#### 알려진 문제

- KRX는 `COMPST_ISU_CD`에 혼합 ISIN 및 티커 형식 반환
- 가치가 0인 구성 종목 필터링: `VALU_AMT != 0`
- ISIN에서 티커 추출: ISIN 형식에 대해 `substring(3, 9)`

---

## 카테고리 4: 성과 및 추적

### 4.1 MDCSTAT05901 - 추적오차율 추이 (추적 오차)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT05901`

**목적**: 과거 추적 오차율 (ETF vs 벤치마크)

**사용 사례**: ETF 품질 평가, 추적 효율성

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 |
| isuCd | String | 예 | ISIN | ETF ISIN 코드 |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| TRD_DD | 날짜 | String → LocalDate | 거래일 |
| LST_NAV | NAV | String → BigDecimal | NAV |
| NAV_CHG_RT | NAV변동률 | String → Double | NAV 변화율 % |
| OBJ_STKPRC_IDX | 지수 | String → BigDecimal | 지수 값 |
| IDX_CHG_RTO | 지수변동률 | String → Double | 지수 변화율 % |
| TRACE_YD_MULT | 추적배수 | String → BigDecimal | 추적 배수 |
| TRACE_ERR_RT | 추적오차율 | String → Double | 추적 오차율 % |

---

### 4.2 MDCSTAT06001 - 괴리율 추이 (가격 괴리)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT06001`

**목적**: 가격과 NAV 간의 과거 괴리

**사용 사례**: 차익거래 기회, 가격 효율성

#### 파라미터

MDCSTAT05901과 동일

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| TRD_DD | 날짜 | String → LocalDate | 거래일 |
| FLUC_TP_CD | 등락구분 | String → Int | 방향 |
| CLSPRC | 종가 | String → Int | 종가 |
| LST_NAV | NAV | String → BigDecimal | NAV |
| DIVRG_RT | 괴리율 | String → Double | 괴리율 % |

**공식**: `괴리율 = ((종가 - NAV) / NAV) * 100`

---

## 카테고리 5: 투자자별 거래 패턴

### 5.1 MDCSTAT04801 - ETF 투자자별 거래실적 (기간합계)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04801`

**목적**: 모든 ETF에 대한 유형별 투자자 거래 집계

**사용 사례**: 시장 심리, 기관 vs 개인 분석

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| CONV_OBJ_TP_CD | 구분 | String | 카테고리 표시 (TS = 합계/소계) |
| INVST_NM | 투자자명 | String | 투자자 유형 (금융투자, 보험, 개인, 외국인 등) |
| ASK_TRDVOL | 매도거래량 | String → Long | 매도 거래량 |
| BID_TRDVOL | 매수거래량 | String → Long | 매수 거래량 |
| NETBID_TRDVOL | 순매수거래량 | String → Long | 순매수 거래량 |
| ASK_TRDVAL | 매도거래대금 | String → Long | 매도 거래대금 |
| BID_TRDVAL | 매수거래대금 | String → Long | 매수 거래대금 |
| NETBID_TRDVAL | 순매수거래대금 | String → Long | 순매수 거래대금 (부호 있음) |

**투자자 유형**:
- 금융투자 (증권사)
- 보험
- 투신 (투자신탁)
- 사모 (사모펀드)
- 은행
- 기타금융
- 연기금 등
- 기관합계 (기관 합계) - "TS"로 표시
- 기타법인
- 개인
- 외국인
- 기타외국인
- 전체 (전체 합계) - "TS"로 표시

---

### 5.2 MDCSTAT04802 - ETF 투자자별 거래실적 (일별추이)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04802`

**목적**: 모든 ETF에 대한 일일 시계열 투자자 거래

**사용 사례**: 추세 분석, 투자자 흐름 추적

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 | 값 |
|---------|------|------|------|------|-----|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 | |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 | |
| inqCondTpCd1 | String | 예 | 1 또는 2 | 쿼리 유형 1 | 1=거래대금, 2=거래량 |
| inqCondTpCd2 | String | 예 | 1/2/3 | 쿼리 유형 2 | 1=순매수, 2=매수, 3=매도 |

#### 응답 필드

| KRX 필드 | 한글명 | 타입 | 설명 |
|---------|--------|------|------|
| TRD_DD | 날짜 | String → LocalDate | 거래일 |
| NUM_ITM_VAL21 | 기관 | String → Long | 기관 (부호 있음) |
| NUM_ITM_VAL22 | 기타법인 | String → Long | 기타법인 (부호 있음) |
| NUM_ITM_VAL23 | 개인 | String → Long | 개인 (부호 있음) |
| NUM_ITM_VAL24 | 외국인 | String → Long | 외국인 (부호 있음) |
| NUM_ITM_VAL25 | 전체 | String → Long | 전체 (순매수는 항상 0) |

**참고**: 값은 **부호 있는 정수** (순매도의 경우 음수 가능)

---

### 5.3 MDCSTAT04901 - ETF 투자자별 거래실적 개별종목 (기간합계)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04901`

**목적**: 단일 ETF에 대한 투자자 거래 집계

**사용 사례**: 개별 ETF 심리, 기관 관심도

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 |
| isuCd | String | 예 | ISIN | ETF ISIN 코드 |

응답 필드: MDCSTAT04801과 동일

---

### 5.4 MDCSTAT04902 - ETF 투자자별 거래실적 개별종목 (일별추이)

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT04902`

**목적**: 단일 ETF에 대한 일일 투자자 거래

**사용 사례**: ETF별 흐름 분석, 일일 심리

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 | 값 |
|---------|------|------|------|------|-----|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 | |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 | |
| isuCd | String | 예 | ISIN | ETF ISIN 코드 | |
| inqCondTpCd1 | String | 예 | 1 또는 2 | 쿼리 유형 1 | 1=거래대금, 2=거래량 |
| inqCondTpCd2 | String | 예 | 1/2/3 | 쿼리 유형 2 | 1=순매수, 2=매수, 3=매도 |

응답 필드: MDCSTAT04802와 동일

---

## 카테고리 6: 공매도 데이터

### 6.1 MDCSTAT31401 - 개별종목 공매도 거래

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT31401`

**목적**: 개별 종목의 일별 공매도 거래 현황 조회

**사용 사례**: 공매도 거래량/거래대금 추이 분석, 공매도 비중 모니터링

**HTTP 메서드**: POST

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 | 20240101 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 | 20240131 |
| isuCd | String | 예 | ISIN | 종목 ISIN 코드 | KR7005930003 |

#### 응답 필드

| KRX 필드 | 한글명 | 데이터 타입 | 설명 | 예시 |
|---------|--------|------------|------|------|
| TRD_DD | 거래일자 | String → LocalDate | 거래일 | 2024/01/02 |
| ISU_CD | 종목코드 | String | ISIN 코드 | KR7005930003 |
| ISU_ABBRV | 종목명 | String | 종목 약명 | 삼성전자 |
| CVSRTSELL_TRDVOL | 공매도거래량 | String → Long | 공매도 거래량 (주) | 1,234,567 |
| TRDVOL | 총거래량 | String → Long | 전체 거래량 (주) | 10,000,000 |
| CVSRTSELL_TRDVAL | 공매도거래대금 | String → Long | 공매도 거래대금 (원) | 85,000,000,000 |
| TRDVAL | 총거래대금 | String → Long | 전체 거래대금 (원) | 700,000,000,000 |
| CVSRTSELL_TRDVOL_RTO | 거래량비중 | String → Double | 공매도 거래량 / 총거래량 (%) | 12.35 |
| CVSRTSELL_TRDVAL_RTO | 거래대금비중 | String → Double | 공매도 거래대금 / 총거래대금 (%) | 12.14 |

#### 응답 샘플

```json
{
  "output": [
    {
      "TRD_DD": "2024/01/02",
      "ISU_CD": "KR7005930003",
      "ISU_ABBRV": "삼성전자",
      "CVSRTSELL_TRDVOL": "1,234,567",
      "TRDVOL": "10,000,000",
      "CVSRTSELL_TRDVAL": "85,000,000,000",
      "TRDVAL": "700,000,000,000",
      "CVSRTSELL_TRDVOL_RTO": "12.35",
      "CVSRTSELL_TRDVAL_RTO": "12.14"
    }
  ]
}
```

#### Kotlin 구현 예제

```kotlin
data class ShortSellingTransaction(
    val date: LocalDate,
    val isin: String,
    val name: String,
    val shortSellingVolume: Long,
    val totalVolume: Long,
    val shortSellingValue: Long,
    val totalValue: Long,
    val volumeRatio: Double,
    val valueRatio: Double
)

fun getShortSellingTransactions(
    ticker: String,
    from: LocalDate,
    to: LocalDate
): List<ShortSellingTransaction> {
    val isin = etx.getIsin(ticker)
    val resp = client.post("dbms/MDC/STAT/standard/MDCSTAT31401", mapOf(
        "strtDd" to from.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "endDd" to to.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "isuCd" to isin
    ))
    val rows = (resp["output"] as? List<*>) ?: emptyList()
    return rows.mapNotNull { it as? Map<*, *> }.map { m ->
        ShortSellingTransaction(
            date = m["TRD_DD"].toString().toKrxDate(),
            isin = m["ISU_CD"].toString(),
            name = m["ISU_ABBRV"].toString(),
            shortSellingVolume = m["CVSRTSELL_TRDVOL"].toString().toKrxLong(),
            totalVolume = m["TRDVOL"].toString().toKrxLong(),
            shortSellingValue = m["CVSRTSELL_TRDVAL"].toString().toKrxLong(),
            totalValue = m["TRDVAL"].toString().toKrxLong(),
            volumeRatio = m["CVSRTSELL_TRDVOL_RTO"].toString().toKrxDouble(),
            valueRatio = m["CVSRTSELL_TRDVAL_RTO"].toString().toKrxDouble()
        )
    }
}
```

---

### 6.2 MDCSTAT31501 - 개별종목 공매도 종합정보

**BLD 코드**: `dbms/MDC/STAT/standard/MDCSTAT31501`

**목적**: 개별 종목의 공매도 잔고 및 종합 정보 조회

**사용 사례**: 공매도 잔고 추이 분석, 잔고 비율 모니터링, 상환 거래 추적

**HTTP 메서드**: POST

#### 파라미터

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|---------|------|------|------|------|
| strtDd | String | 예 | YYYYMMDD | 시작 날짜 | 20240101 |
| endDd | String | 예 | YYYYMMDD | 종료 날짜 | 20240131 |
| isuCd | String | 예 | ISIN | 종목 ISIN 코드 | KR7005930003 |

#### 응답 필드

| KRX 필드 | 한글명 | 데이터 타입 | 설명 | 예시 |
|---------|--------|------------|------|------|
| TRD_DD | 거래일자 | String → LocalDate | 거래일 | 2024/01/02 |
| ISU_CD | 종목코드 | String | ISIN 코드 | KR7005930003 |
| ISU_ABBRV | 종목명 | String | 종목 약명 | 삼성전자 |
| CVSRTSELL_TRDVOL | 공매도거래량 | String → Long | 공매도 거래량 (주) | 1,234,567 |
| STR_CONST_VAL1 | 상환거래량 | String → Long | 상환 거래량 (주) | 987,654 |
| CVSRTSELL_RPMNT_TRDVOL | 공매도순거래량 | String → Long | 공매도 - 상환 (주) | 246,913 |
| ACC_TRDVOL | 총거래량 | String → Long | 전체 거래량 (주) | 10,000,000 |
| VALU_PD_SALE_PSTK_LQTY | 공매도잔고수량 | String → Long | 공매도 잔고 (주) | 5,600,000 |
| VALU_PD_SALE_PSTK_RTO | 잔고비율 | String → Double | 잔고 / 상장주식수 (%) | 0.95 |
| LIST_SHRS | 상장주식수 | String → Long | 상장 주식 수 (주) | 589,000,000 |

#### 응답 샘플

```json
{
  "output": [
    {
      "TRD_DD": "2024/01/02",
      "ISU_CD": "KR7005930003",
      "ISU_ABBRV": "삼성전자",
      "CVSRTSELL_TRDVOL": "1,234,567",
      "STR_CONST_VAL1": "987,654",
      "CVSRTSELL_RPMNT_TRDVOL": "246,913",
      "ACC_TRDVOL": "10,000,000",
      "VALU_PD_SALE_PSTK_LQTY": "5,600,000",
      "VALU_PD_SALE_PSTK_RTO": "0.95",
      "LIST_SHRS": "589,000,000"
    }
  ]
}
```

#### Kotlin 구현 예제

```kotlin
data class ShortSellingBalance(
    val date: LocalDate,
    val isin: String,
    val name: String,
    val shortSellingVolume: Long,
    val repaymentVolume: Long,
    val netShortSellingVolume: Long,
    val totalVolume: Long,
    val balanceQuantity: Long,
    val balanceRatio: Double,
    val listedShares: Long
)

fun getShortSellingBalance(
    ticker: String,
    from: LocalDate,
    to: LocalDate
): List<ShortSellingBalance> {
    val isin = etx.getIsin(ticker)
    val resp = client.post("dbms/MDC/STAT/standard/MDCSTAT31501", mapOf(
        "strtDd" to from.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "endDd" to to.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "isuCd" to isin
    ))
    val rows = (resp["output"] as? List<*>) ?: emptyList()
    return rows.mapNotNull { it as? Map<*, *> }.map { m ->
        ShortSellingBalance(
            date = m["TRD_DD"].toString().toKrxDate(),
            isin = m["ISU_CD"].toString(),
            name = m["ISU_ABBRV"].toString(),
            shortSellingVolume = m["CVSRTSELL_TRDVOL"].toString().toKrxLong(),
            repaymentVolume = m["STR_CONST_VAL1"].toString().toKrxLong(),
            netShortSellingVolume = m["CVSRTSELL_RPMNT_TRDVOL"].toString().toKrxLong(),
            totalVolume = m["ACC_TRDVOL"].toString().toKrxLong(),
            balanceQuantity = m["VALU_PD_SALE_PSTK_LQTY"].toString().toKrxLong(),
            balanceRatio = m["VALU_PD_SALE_PSTK_RTO"].toString().toKrxDouble(),
            listedShares = m["LIST_SHRS"].toString().toKrxLong()
        )
    }
}
```

---

## 엔드포인트 요약 테이블

| # | BLD 코드 | 엔드포인트명 | pykrx | kotlin-krx | 우선순위 |
|---|---------|-------------|-------|-----------|---------|
| 1 | finder_secuprodisu | 상장종목검색 | ✅ | ✅ | 중간 |
| 2 | MDCSTAT04601 | ETF 전종목 기본정보 | ✅ | ✅ | 높음 |
| 3 | MDCSTAT04301 | 전종목 시세 | ✅ | ✅ | 높음 |
| 4 | MDCSTAT04401 | 전종목 등락률 | ✅ | ✅ | 중간 |
| 5 | MDCSTAT04501 | 개별종목 시세 추이 (OHLCV) | ✅ | ✅ | **필수** |
| **6** | **MDCSTAT04701** | **개별종목 종합정보** | **❌** | **🚧 TODO** | **🔥 최우선** |
| 7 | MDCSTAT05001 | PDF (포트폴리오) | ✅ | ✅ | 높음 |
| 8 | MDCSTAT05901 | 추적오차율 추이 | ✅ | ✅ | 중간 |
| 9 | MDCSTAT06001 | 괴리율 추이 | ✅ | ✅ | 중간 |
| 10 | MDCSTAT04801 | 투자자별 거래 (기간합계) | ✅ | ✅ | 중간 |
| 11 | MDCSTAT04802 | 투자자별 거래 (일별) | ✅ | ✅ | 낮음 |
| 12 | MDCSTAT04901 | 투자자별 거래 개별 (기간합계) | ✅ | ✅ | 중간 |
| 13 | MDCSTAT04902 | 투자자별 거래 개별 (일별) | ✅ | ✅ | 낮음 |
| 14 | MDCSTAT31401 | 개별종목 공매도 거래 | ❌ | 🚧 TODO | 중간 |
| 15 | MDCSTAT31501 | 개별종목 공매도 종합정보 | ❌ | 🚧 TODO | 중간 |

**범례**:
- ✅ 구현됨
- 🚧 진행 중
- ❌ 미구현
- 🔥 필수 우선순위

---

## 파라미터 조합 매트릭스

### 일반적인 파라미터 패턴

| 패턴 | 파라미터 | 엔드포인트 | 사용 사례 |
|------|---------|----------|---------|
| **파라미터 없음** | 없음 | 04601, 06701, 08501 | 전체 목록 쿼리 |
| **단일 날짜** | trdDd | 04301, 05001 | 일일 스냅샷 |
| **날짜 범위** | strtDd, endDd | 04401, 04501, 05901, 06001 | 과거 시계열 |
| **날짜 범위 + ISIN** | strtDd, endDd, isuCd | 04501, 05901, 06001, 04901, 04902 | 개별 ETF 과거 데이터 |
| **날짜 범위 + 쿼리 유형** | strtDd, endDd, inqCondTpCd1, inqCondTpCd2 | 04802, 04902, 07002 | 투자자 거래 분석 |
| **단일 날짜 + ISIN** | trdDd, isuCd | **04701**, 05001 | 상세 스냅샷 |

---

## 데이터 정규화 규칙 (요약)

### String → Int

```kotlin
fun String.toKrxInt(): Int {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toIntOrNull() ?: 0
}
```

### String → Long (부호 있음)

```kotlin
fun String.toKrxLong(): Long {
    val clean = this.replace(",", "").trim()
    if (clean.isEmpty() || clean == "-") return 0L
    return clean.toLongOrNull() ?: 0L
}
```

**중요**: `NETBID_TRDVAL`과 같은 값은 음수일 수 있음 (매도 압력)

### String → BigDecimal

```kotlin
fun String.toKrxBigDecimal(): BigDecimal {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toBigDecimalOrNull() ?: BigDecimal.ZERO
}
```

### String → LocalDate

```kotlin
fun String.toKrxDate(): LocalDate {
    val s = this.trim()
    return when {
        s.isEmpty() || s == "-" -> LocalDate.MIN
        s.contains("/") -> LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        else -> LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"))
    }
}
```

---

## 모범 사례

1. **isuCd 파라미터에는 항상 ISIN 사용**, 티커 아님
2. **EtxTickerCache.getIsin()을 사용**하여 티커 확인
3. **모든 숫자 필드에서 "-" 및 빈 문자열 처리**
4. **시계열 데이터를 날짜별로 정렬**
5. **투자자 데이터에서 소계/합계를 위해 CONV_OBJ_TP_CD == "TS" 필터링**
6. **엔드포인트 호출 전 날짜 범위 검증**
7. **MDCSTAT04601 결과 캐싱** (기본 정보는 자주 변경되지 않음)
8. **상세 페이지에는 MDCSTAT04701 사용** (종합 데이터를 위한 단일 요청)
9. **백테스팅에는 MDCSTAT04501 사용** (날짜 범위 지원)
10. **속도 제한을 위한 지수 백오프 구현**

---

## 다음 단계

1. **MDCSTAT04701 구현** (`03-MDCSTAT04701-상세명세.md` 참조)
2. **포괄적인 매핑 테이블 생성** (`04-데이터-매핑-명세.md` 참조)
3. **모든 엔드포인트에 대한 통합 테스트 작성** (`07-테스트-전략.md` 참조)
4. **백테스트 중심 API 문서화** (`06-API-설계.md` 참조)
