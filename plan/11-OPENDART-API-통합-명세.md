# OPENDART API 통합 명세서

> **목적**: ETF 배당/분할 정보 및 공시 데이터 수집을 위한 OPENDART API 연동 기술 명세
> **데이터 소스**: 금융감독원 전자공시시스템 (OPENDART)
> **공식 문서**: https://opendart.fss.or.kr/

---

## 목차

1. [OPENDART API 개요](#1-opendart-api-개요)
2. [API 인증](#2-api-인증)
3. [고유번호 조회 API](#3-고유번호-조회-api)
4. [배당 정보 API](#4-배당-정보-api)
5. [증자감자 정보 API](#5-증자감자-정보-api)
6. [공시검색 API](#6-공시검색-api)
7. [에러 처리](#7-에러-처리)
8. [Rate Limiting](#8-rate-limiting)
9. [구현 가이드](#9-구현-가이드)

---

## 1. OPENDART API 개요

### 1.1 제공 서비스

OPENDART는 금융감독원 전자공시시스템에서 제공하는 공공 API로, 상장법인의 공시 정보를 제공합니다.

**주요 API 그룹**:
- **DS001**: 공시정보
- **DS002**: 정기보고서 주요정보 (배당, 증자감자 등)
- **DS003**: 정기보고서 재무정보
- **DS004**: 지분공시 종합정보
- **DS005**: 주요사항보고서 주요정보
- **DS006**: 증권신고서 주요정보

### 1.2 ETF 데이터 활용 범위

kotlin-krx 프로젝트에서 OPENDART API를 활용하는 목적:

| 데이터 | API | 용도 |
|--------|-----|------|
| **배당 정보** | `alotMatter` | 조정주가 계산을 위한 배당금 데이터 |
| **증자/감자** | `irdsSttus` | 주식 분할/병합 정보 |
| **ETF 공시** | `list` | ETF 관련 중요 공시 감지 |
| **고유번호** | `corpCode` | stock_code → corp_code 매핑 |

### 1.3 기술 스펙

- **프로토콜**: HTTPS
- **메서드**: GET
- **인코딩**: UTF-8
- **응답 형식**: JSON / XML (선택 가능)
- **인증 방식**: API Key (Query Parameter)

---

## 2. API 인증

### 2.1 API Key 발급

1. OPENDART 웹사이트 접속: https://opendart.fss.or.kr/
2. 회원가입 (개인/기업)
3. "인증키 신청/관리" 메뉴에서 API Key 발급
4. 발급된 40자리 인증키 보관

### 2.2 API Key 사용

모든 API 요청에 `crtfc_key` 파라미터로 인증키 전달:

```
GET https://opendart.fss.or.kr/api/{endpoint}.json?crtfc_key={YOUR_API_KEY}&...
```

### 2.3 Kotlin 구현 예제

```kotlin
class OpenDartApiClient(
    private val apiKey: String,
    private val httpClient: HttpClient = HttpClient()
) {
    companion object {
        private const val BASE_URL = "https://opendart.fss.or.kr/api"
    }

    private fun buildUrl(endpoint: String, params: Map<String, String>): String {
        val allParams = params + ("crtfc_key" to apiKey)
        val queryString = allParams.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }
        return "$BASE_URL/$endpoint.json?$queryString"
    }
}
```

### 2.4 환경 변수 관리

API Key는 환경 변수로 관리 권장:

```bash
# .env
OPENDART_API_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

```kotlin
// 사용
val apiKey = System.getenv("OPENDART_API_KEY")
    ?: throw IllegalStateException("OPENDART_API_KEY not set")
```

---

## 3. 고유번호 조회 API

### 3.1 개요

**목적**: 종목코드(stock_code)를 OPENDART 고유번호(corp_code)로 변환

- OPENDART API는 `corp_code` (8자리)를 식별자로 사용
- KRX API는 `stock_code` (6자리) 또는 `ISIN` (12자리) 사용
- 두 시스템 간 매핑을 위해 고유번호 테이블 필요

### 3.2 API 엔드포인트

```
GET https://opendart.fss.or.kr/api/corpCode.xml
```

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| crtfc_key | STRING(40) | Y | API 인증키 |

**응답 형식**: ZIP 압축된 XML 파일

### 3.3 응답 데이터 구조

ZIP 파일 내 `CORPCODE.xml` 구조:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<result>
    <list>
        <corp_code>00126380</corp_code>
        <corp_name>삼성전자</corp_name>
        <stock_code>005930</stock_code>
        <modify_date>20231225</modify_date>
    </list>
    <list>
        <corp_code>00164779</corp_code>
        <corp_name>KODEX 200</corp_name>
        <stock_code>069500</stock_code>
        <modify_date>20231225</modify_date>
    </list>
    <!-- ... -->
</result>
```

**필드 설명**:
- `corp_code`: OPENDART 고유번호 (8자리, 필수)
- `corp_name`: 법인명/ETF명
- `stock_code`: 종목코드 (6자리, 상장사만 존재)
- `modify_date`: 최종 수정일 (YYYYMMDD)

### 3.4 Kotlin 구현 예제

```kotlin
import java.util.zip.ZipInputStream
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

data class CorpCode(
    val corpCode: String,     // 고유번호 (8자리)
    val corpName: String,     // 법인명
    val stockCode: String?,   // 종목코드 (6자리, nullable)
    val modifyDate: LocalDate // 수정일
)

suspend fun getCorpCodeList(): List<CorpCode> {
    val url = "$BASE_URL/corpCode.xml?crtfc_key=$apiKey"
    val response = httpClient.get(url)

    // ZIP 압축 해제
    val zipInputStream = ZipInputStream(response.bodyAsStream())
    zipInputStream.nextEntry // CORPCODE.xml 진입

    // XML 파싱
    val doc = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(zipInputStream)

    val listNodes = doc.getElementsByTagName("list")

    return (0 until listNodes.length).map { i ->
        val node = listNodes.item(i) as Element
        CorpCode(
            corpCode = node.getElementsByTagName("corp_code").item(0).textContent,
            corpName = node.getElementsByTagName("corp_name").item(0).textContent,
            stockCode = node.getElementsByTagName("stock_code").item(0)?.textContent,
            modifyDate = LocalDate.parse(
                node.getElementsByTagName("modify_date").item(0).textContent,
                DateTimeFormatter.ofPattern("yyyyMMdd")
            )
        )
    }
}
```

### 3.5 사용 전략

**매핑 테이블 구축**:

```sql
CREATE TABLE corp_code_mapping (
    stock_code VARCHAR(6) PRIMARY KEY,  -- 069500
    corp_code VARCHAR(8) NOT NULL,      -- 00164779
    corp_name VARCHAR(200),
    modify_date DATE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_corp_code ON corp_code_mapping(corp_code);
```

**갱신 주기**:
- 주 1회 전체 데이터 다운로드 및 갱신 권장
- 신규 상장 ETF 감지 및 매핑 테이블 업데이트

---

## 4. 배당 정보 API

### 4.1 개요

**목적**: ETF의 배당금 정보 조회 (조정주가 계산에 필수)

- 정기보고서(사업보고서, 반기보고서, 분기보고서)의 배당 정보 제공
- 현금배당, 주식배당 모두 포함

### 4.2 API 엔드포인트

```
GET https://opendart.fss.or.kr/api/alotMatter.json
```

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| crtfc_key | STRING(40) | Y | API 인증키 |
| corp_code | STRING(8) | Y | 고유번호 (예: 00164779) |
| bsns_year | STRING(4) | Y | 사업연도 (예: 2024) ※ 2015년 이후 |
| reprt_code | STRING(5) | Y | 11011(사업보고서), 11012(반기), 11013(1분기), 11014(3분기) |

### 4.3 응답 데이터 구조

```json
{
  "status": "000",
  "message": "정상",
  "list": [
    {
      "rcept_no": "20240315000123",
      "corp_cls": "K",
      "corp_code": "00164779",
      "corp_name": "KODEX 200",
      "se": "현금",
      "stock_knd": "보통주",
      "thstrm": "100",
      "frmtrm": "95",
      "lwfr": "90",
      "stlm_dt": "2024-12-31"
    }
  ]
}
```

**필드 설명**:
- `rcept_no`: 접수번호 (14자리)
- `corp_cls`: 법인구분 (Y: 유가증권, K: 코스닥, N: 코넥스, E: 기타)
- `corp_code`: 고유번호
- `corp_name`: 법인명
- `se`: 배당 구분 (현금, 주식)
- `stock_knd`: 주식 종류 (보통주, 우선주)
- `thstrm`: 당기 배당금 (원)
- `frmtrm`: 전기 배당금 (원)
- `lwfr`: 전전기 배당금 (원)
- `stlm_dt`: 결산기준일 (YYYY-MM-DD)

### 4.4 Kotlin 데이터 모델

```kotlin
data class DividendInfo(
    val rceptNo: String,           // 접수번호
    val corpCode: String,          // 고유번호
    val corpName: String,          // 법인명
    val dividendType: String,      // 배당구분 (현금/주식)
    val stockKind: String,         // 주식종류
    val currentYear: BigDecimal?,  // 당기 배당금
    val previousYear: BigDecimal?, // 전기 배당금
    val twoYearsAgo: BigDecimal?,  // 전전기 배당금
    val settlementDate: LocalDate  // 결산기준일
)
```

### 4.5 사용 예제

```kotlin
suspend fun getDividendInfo(
    corpCode: String,
    year: Int,
    reportCode: String = "11011" // 사업보고서
): List<DividendInfo> {
    val url = buildUrl("alotMatter", mapOf(
        "corp_code" to corpCode,
        "bsns_year" to year.toString(),
        "reprt_code" to reportCode
    ))

    val response = httpClient.get(url).body<OpenDartResponse<DividendInfo>>()

    if (response.status != "000") {
        throw OpenDartException(response.status, response.message)
    }

    return response.list ?: emptyList()
}
```

### 4.6 ETF 배당 특징

- ETF는 일반적으로 **분배금** 형태로 지급
- 월배당 ETF: 월 1회 분배 (연 12회)
- 분기배당 ETF: 분기 1회 분배 (연 4회)
- 연배당 ETF: 연 1회 분배

**주의**: OPENDART API는 정기보고서 기준이므로, 월배당 ETF의 경우 분기/반기/사업보고서에서 합산된 배당금을 제공할 수 있습니다. 정확한 배당락일별 배당금은 별도 공시 확인 필요.

---

## 5. 증자감자 정보 API

### 5.1 개요

**목적**: 주식 분할/병합 정보 조회 (조정주가 계산에 필수)

### 5.2 API 엔드포인트

```
GET https://opendart.fss.or.kr/api/irdsSttus.json
```

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| crtfc_key | STRING(40) | Y | API 인증키 |
| corp_code | STRING(8) | Y | 고유번호 |
| bsns_year | STRING(4) | Y | 사업연도 |
| reprt_code | STRING(5) | Y | 보고서 코드 |

### 5.3 응답 데이터 구조

```json
{
  "status": "000",
  "message": "정상",
  "list": [
    {
      "rcept_no": "20240315000123",
      "corp_cls": "K",
      "corp_code": "00164779",
      "corp_name": "KODEX 200",
      "isu_dcrs_de": "2024-03-15",
      "isu_dcrs_stle": "주식분할",
      "isu_dcrs_stock_knd": "보통주",
      "isu_dcrs_qy": "10000000",
      "isu_dcrs_mstvdv_fval_amount": "500",
      "isu_dcrs_mstvdv_amount": "5000000000"
    }
  ]
}
```

**필드 설명**:
- `isu_dcrs_de`: 증자(감자)일 (YYYY-MM-DD)
- `isu_dcrs_stle`: 증자(감자) 방법 (유상증자, 무상증자, 주식분할, 주식병합 등)
- `isu_dcrs_stock_knd`: 주식 종류
- `isu_dcrs_qy`: 증자(감자) 주식수
- `isu_dcrs_mstvdv_fval_amount`: 1주당 액면가액 (원)
- `isu_dcrs_mstvdv_amount`: 증가(감소) 액면총액 (원)

### 5.4 Kotlin 데이터 모델

```kotlin
data class StockSplitInfo(
    val rceptNo: String,          // 접수번호
    val corpCode: String,         // 고유번호
    val corpName: String,         // 법인명
    val eventDate: LocalDate,     // 증자/감자일
    val eventType: String,        // 방법 (주식분할, 주식병합 등)
    val stockKind: String,        // 주식종류
    val quantity: Long,           // 주식수
    val parValuePerShare: Int,    // 1주당 액면가
    val totalAmount: Long         // 액면총액
)
```

### 5.5 분할 비율 계산

```kotlin
fun calculateSplitRatio(splitInfo: StockSplitInfo): BigDecimal {
    // 예: 1주 → 2주 분할 = 2.0
    // 예: 5주 → 1주 병합 = 0.2

    return when (splitInfo.eventType) {
        "주식분할" -> {
            // 분할 비율 계산 로직
            // (신주수 / 구주수)
            BigDecimal.valueOf(2.0) // 예시
        }
        "주식병합" -> {
            // 병합 비율 계산 로직
            BigDecimal.valueOf(0.5) // 예시
        }
        else -> BigDecimal.ONE
    }
}
```

---

## 6. 공시검색 API

### 6.1 개요

**목적**: ETF 관련 공시 조회 (상장/폐지, 중요 공시 감지)

### 6.2 API 엔드포인트

```
GET https://opendart.fss.or.kr/api/list.json
```

**파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| crtfc_key | STRING(40) | Y | API 인증키 |
| corp_code | STRING(8) | N | 고유번호 (미입력 시 전체) |
| bgn_de | STRING(8) | Y | 시작일 (YYYYMMDD) |
| end_de | STRING(8) | Y | 종료일 (YYYYMMDD) |
| last_reprt_at | STRING(1) | N | 최종보고서만 검색 (Y/N) |
| pblntf_ty | STRING(1) | N | 공시유형 (A: 정기, B: 주요사항, C: 발행, D: 지분, E: 기타, F: 외부감사 등) |
| pblntf_detail_ty | STRING(4) | N | 공시상세유형 |
| page_no | NUMBER | N | 페이지 번호 (기본값: 1) |
| page_count | NUMBER | N | 페이지당 건수 (기본값: 10, 최대: 100) |

### 6.3 응답 데이터 구조

```json
{
  "status": "000",
  "message": "정상",
  "page_no": 1,
  "page_count": 10,
  "total_count": 123,
  "total_page": 13,
  "list": [
    {
      "corp_code": "00164779",
      "corp_name": "KODEX 200",
      "stock_code": "069500",
      "corp_cls": "K",
      "report_nm": "증권발행실적보고서",
      "rcept_no": "20240315000123",
      "flr_nm": "한국투자신탁운용",
      "rcept_dt": "20240315",
      "rm": "기타"
    }
  ]
}
```

**필드 설명**:
- `corp_code`: 고유번호
- `corp_name`: 법인명
- `stock_code`: 종목코드
- `corp_cls`: 법인구분
- `report_nm`: 보고서명
- `rcept_no`: 접수번호 (공시 상세 조회에 사용)
- `flr_nm`: 제출인명
- `rcept_dt`: 접수일자 (YYYYMMDD)
- `rm`: 비고

### 6.4 Kotlin 데이터 모델

```kotlin
data class DisclosureListResponse(
    val status: String,
    val message: String,
    val pageNo: Int,
    val pageCount: Int,
    val totalCount: Int,
    val totalPage: Int,
    val list: List<DisclosureItem>?
)

data class DisclosureItem(
    val corpCode: String,      // 고유번호
    val corpName: String,      // 법인명
    val stockCode: String?,    // 종목코드
    val corpCls: String,       // 법인구분
    val reportName: String,    // 보고서명
    val rceptNo: String,       // 접수번호
    val filerName: String,     // 제출인명
    val rceptDate: LocalDate,  // 접수일자
    val remark: String?        // 비고
)
```

### 6.5 사용 예제

```kotlin
suspend fun searchDisclosures(
    corpCode: String? = null,
    startDate: LocalDate,
    endDate: LocalDate,
    pageNo: Int = 1,
    pageCount: Int = 100
): DisclosureListResponse {
    val params = mutableMapOf(
        "bgn_de" to startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "end_de" to endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "page_no" to pageNo.toString(),
        "page_count" to pageCount.toString()
    )

    corpCode?.let { params["corp_code"] = it }

    val url = buildUrl("list", params)
    return httpClient.get(url).body<DisclosureListResponse>()
}
```

---

## 7. 에러 처리

### 7.1 에러 코드

OPENDART API 공통 에러 코드:

| 코드 | 메시지 | 설명 | 처리 방법 |
|------|--------|------|-----------|
| 000 | 정상 | 성공 | - |
| 010 | 등록되지 않은 키입니다 | 잘못된 API Key | API Key 확인 |
| 011 | 사용할 수 없는 키입니다 | 정지/만료된 Key | Key 재발급 |
| 013 | 조회된 데이타가 없습니다 | 결과 없음 | 빈 리스트 반환 |
| 014 | 제한된 IP 주소입니다 | IP 제한 | IP 등록 확인 |
| 020 | 요청 제한을 초과하였습니다 | Rate Limit 초과 | 지연 후 재시도 |
| 100 | 필드의 부적절한 값입니다 | 파라미터 오류 | 파라미터 검증 |
| 800 | 시스템 점검중입니다 | 점검 중 | 나중에 재시도 |
| 900 | 정의되지 않은 오류가 발생하였습니다 | 서버 오류 | 재시도 |

### 7.2 Kotlin 예외 클래스

```kotlin
sealed class OpenDartException(
    val code: String,
    message: String
) : Exception(message) {

    class InvalidApiKey(code: String, message: String) : OpenDartException(code, message)
    class NoData(code: String, message: String) : OpenDartException(code, message)
    class RateLimitExceeded(code: String, message: String) : OpenDartException(code, message)
    class InvalidParameter(code: String, message: String) : OpenDartException(code, message)
    class SystemError(code: String, message: String) : OpenDartException(code, message)

    companion object {
        fun from(code: String, message: String): OpenDartException {
            return when (code) {
                "010", "011", "014" -> InvalidApiKey(code, message)
                "013" -> NoData(code, message)
                "020" -> RateLimitExceeded(code, message)
                "100" -> InvalidParameter(code, message)
                else -> SystemError(code, message)
            }
        }
    }
}
```

### 7.3 응답 처리 예제

```kotlin
data class OpenDartResponse<T>(
    val status: String,
    val message: String,
    val list: List<T>?
)

suspend inline fun <reified T> handleResponse(url: String): List<T> {
    val response = httpClient.get(url).body<OpenDartResponse<T>>()

    return when (response.status) {
        "000" -> response.list ?: emptyList()
        "013" -> emptyList() // 데이터 없음
        else -> throw OpenDartException.from(response.status, response.message)
    }
}
```

---

## 8. Rate Limiting (권장 사항)

### 8.1 제한 정책

OPENDART 공식 문서에 명시된 제한:
- **일일 요청 제한**: 20,000건
- **시간당 제한**: 명시되지 않음 (추정: 1,000건/시간)
- **권장 요청 간격**: 100ms (초당 10 요청)

**중요**: Rate Limiting은 **애플리케이션 레이어에서 구현**하는 것을 권장합니다. 이 라이브러리는 Raw API 호출만 담당합니다.

### 8.2 Rate Limiter 구현 예제 (애플리케이션 레이어)

```kotlin
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import java.time.LocalDate

// 애플리케이션 레이어에서 구현
class RateLimiter(
    private val maxRequestsPerDay: Int = 20000,
    private val delayBetweenRequests: Long = 100 // ms (권장)
) {
    private val dailyCounter = AtomicInteger(0)
    private var currentDate = LocalDate.now()

    suspend fun acquire() {
        // 날짜 변경 시 카운터 리셋
        if (LocalDate.now() != currentDate) {
            dailyCounter.set(0)
            currentDate = LocalDate.now()
        }

        // 일일 제한 체크
        val count = dailyCounter.incrementAndGet()
        if (count > maxRequestsPerDay) {
            throw RuntimeException("Daily request limit exceeded: $maxRequestsPerDay")
        }

        // 요청 간 지연 (권장)
        delay(delayBetweenRequests)
    }
}
```

### 8.3 적용 예제 (애플리케이션 레이어)

```kotlin
// 애플리케이션 코드에서 Rate Limiter와 함께 사용
val rateLimiter = RateLimiter()
val openDartClient = OpenDartApiClient(apiKey = "YOUR_API_KEY")

class CorporateActionsCollector(
    private val openDartClient: OpenDartApiClient,
    private val rateLimiter: RateLimiter
) {
    suspend fun collectDividends(corpCode: String, year: Int): List<DividendInfo> {
        rateLimiter.acquire() // Rate Limiting 적용
        return openDartClient.getDividendInfo(corpCode, year)
    }
}
```

---

## 9. 구현 가이드

### 9.1 라이브러리 구조

```
kotlin-krx/
├── src/main/kotlin/
│   ├── api/
│   │   ├── KrxApiClient.kt          # KRX API (기존)
│   │   ├── NaverApiClient.kt        # Naver API (기존)
│   │   └── OpenDartApiClient.kt     # OPENDART API (신규)
│   ├── model/
│   │   ├── krx/                     # KRX 데이터 모델
│   │   ├── naver/                   # Naver 데이터 모델
│   │   └── opendart/                # OPENDART 데이터 모델 (신규)
│   │       ├── CorpCode.kt
│   │       ├── DividendInfo.kt
│   │       ├── StockSplitInfo.kt
│   │       └── DisclosureItem.kt
│   ├── internal/
│   │   ├── OpenDartApiClientImpl.kt # HTTP 호출 구현
│   │   ├── OpenDartRateLimiter.kt   # Rate Limiter
│   │   └── OpenDartResponseParser.kt # JSON 파싱
│   └── exception/
│       └── OpenDartException.kt     # 예외 클래스
```

### 9.2 의존성

```kotlin
// build.gradle.kts
dependencies {
    // HTTP 클라이언트
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // JSON 파싱
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // ZIP 처리 (corpCode API)
    // Java 기본 라이브러리 사용 (java.util.zip)
}
```

### 9.3 초기화 예제

```kotlin
// 애플리케이션 초기화
val openDartClient = OpenDartApiClient(
    apiKey = System.getenv("OPENDART_API_KEY")
)

// 고유번호 매핑 테이블 구축 (초기 1회)
runBlocking {
    val corpCodes = openDartClient.getCorpCodeList()

    // DB에 저장
    database.batchInsert("corp_code_mapping", corpCodes) { corpCode ->
        mapOf(
            "stock_code" to corpCode.stockCode,
            "corp_code" to corpCode.corpCode,
            "corp_name" to corpCode.corpName,
            "modify_date" to corpCode.modifyDate
        )
    }
}
```

### 9.4 ETF 배당 데이터 수집 워크플로우

```kotlin
class EtfDividendCollector(
    private val krxClient: KrxApiClient,
    private val openDartClient: OpenDartApiClient,
    private val database: Database
) {
    suspend fun collectDividends(ticker: String, year: Int) {
        // 1. stock_code → corp_code 매핑
        val corpCode = database.getCorpCode(ticker)
            ?: throw IllegalArgumentException("Corp code not found for $ticker")

        // 2. OPENDART에서 배당 정보 조회
        val dividends = openDartClient.getDividendInfo(
            corpCode = corpCode,
            year = year,
            reportCode = "11011" // 사업보고서
        )

        // 3. DB 저장
        dividends.forEach { dividend ->
            database.insert("etf_corporate_actions", mapOf(
                "ticker" to ticker,
                "action_type" to "DIVIDEND",
                "dividend_amount" to dividend.currentYear,
                "settlement_date" to dividend.settlementDate
                // ex_date는 별도 공시에서 확인 필요
            ))
        }
    }
}
```

### 9.5 에러 처리 전략

```kotlin
suspend fun safeFetchDividends(corpCode: String, year: Int): List<DividendInfo> {
    return try {
        openDartClient.getDividendInfo(corpCode, year)
    } catch (e: OpenDartException.NoData) {
        // 데이터 없음 → 빈 리스트 반환
        logger.info("No dividend data for $corpCode in $year")
        emptyList()
    } catch (e: OpenDartException.RateLimitExceeded) {
        // Rate limit 초과 → 1시간 대기 후 재시도
        logger.warn("Rate limit exceeded, waiting 1 hour...")
        delay(3600000) // 1시간
        openDartClient.getDividendInfo(corpCode, year)
    } catch (e: OpenDartException.SystemError) {
        // 시스템 오류 → 5분 후 재시도 (최대 3회)
        logger.error("System error, retrying...", e)
        retry(maxAttempts = 3, delayMillis = 300000) {
            openDartClient.getDividendInfo(corpCode, year)
        }
    } catch (e: Exception) {
        logger.error("Unexpected error fetching dividends", e)
        throw e
    }
}
```

---

## 참고 자료

- **OPENDART 공식**: https://opendart.fss.or.kr/
- **개발가이드**: https://opendart.fss.or.kr/guide/main.do
- **API 인증키 발급**: https://opendart.fss.or.kr/uss/umt/EgovMberInsertView.do
- **OpenDartReader (Python)**: https://github.com/FinanceData/OpenDartReader
- **DART API 문서**: https://dart-fss.readthedocs.io/

---

**작성일**: 2025-01-18
**버전**: v1.0
**작성자**: kotlin-krx 프로젝트
