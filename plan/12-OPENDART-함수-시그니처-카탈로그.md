# OPENDART API 함수 시그니처 카탈로그

> **API 인터페이스**: `OpenDartApi` (소스: OPENDART, 상품: 공통)
> **패키지**: `dev.kairoscode.kfc.api.opendart`
> **설계 원칙**: OPENDART API 엔드포인트 1:1 매핑
> **라이브러리 책임**: OPENDART API 호출 및 Raw Data 반환
> **애플리케이션 책임**: 비즈니스 로직, 데이터 조합, ETL 처리

---

## 목차

1. [고유번호 조회](#1-고유번호-조회)
2. [배당 정보](#2-배당-정보)
3. [증자감자 정보](#3-증자감자-정보)
4. [공시 검색](#4-공시-검색)
5. [함수 요약 테이블](#5-함수-요약-테이블)
6. [라이브러리 구조](#6-라이브러리-구조)

---

## 1. 고유번호 조회

### 1.1 `getCorpCodeList()` - 전체 고유번호 목록 조회

**OPENDART API**: corpCode.xml - 고유번호 다운로드

**목적**: 전체 상장법인의 고유번호(corp_code) 및 종목코드(stock_code) 매핑 테이블 구축

**사용 사례**:
- 초기 DB 구축 (stock_code ↔ corp_code 매핑 테이블)
- 주간/월간 매핑 테이블 갱신
- 신규 상장 ETF 감지

**함수 시그니처**:
```kotlin
suspend fun getCorpCodeList(): List<CorpCode>
```

**파라미터**: 없음 (API Key는 클라이언트 초기화 시 설정)

**반환 데이터**:
```kotlin
data class CorpCode(
    val corpCode: String,       // OPENDART 고유번호 (8자리, 예: 00164779)
    val corpName: String,       // 법인명 (예: KODEX 200)
    val stockCode: String?,     // 종목코드 (6자리, 예: 069500, 상장사만 존재)
    val modifyDate: LocalDate   // 최종 수정일
)
```

**OPENDART API 매핑**:
```
요청: GET https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key={API_KEY}
응답: ZIP 파일 (CORPCODE.xml 포함)

CORPCODE.xml 구조:
<result>
    <list>
        <corp_code>00164779</corp_code>
        <corp_name>KODEX 200</corp_name>
        <stock_code>069500</stock_code>
        <modify_date>20231225</modify_date>
    </list>
    ...
</result>
```

**응답 파싱 로직**:
```kotlin
suspend fun getCorpCodeList(): List<CorpCode> {
    val url = "$BASE_URL/corpCode.xml?crtfc_key=$apiKey"
    val response = httpClient.get(url)

    // 1. ZIP 압축 해제
    val zipInputStream = ZipInputStream(response.bodyAsStream())
    zipInputStream.nextEntry // CORPCODE.xml 진입

    // 2. XML 파싱
    val doc = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(zipInputStream)

    val listNodes = doc.getElementsByTagName("list")

    // 3. 데이터 변환
    return (0 until listNodes.length).map { i ->
        val node = listNodes.item(i) as Element
        CorpCode(
            corpCode = node.getTagText("corp_code"),
            corpName = node.getTagText("corp_name"),
            stockCode = node.getTagText("stock_code")?.takeIf { it.isNotBlank() },
            modifyDate = LocalDate.parse(
                node.getTagText("modify_date"),
                DateTimeFormatter.ofPattern("yyyyMMdd")
            )
        )
    }
}

private fun Element.getTagText(tagName: String): String {
    return this.getElementsByTagName(tagName).item(0)?.textContent ?: ""
}
```

**주의사항**:
- ZIP 파일 크기: 약 1~2MB
- 전체 법인 수: 약 60,000개 이상
- ETF만 필터링: `stockCode != null` && `corpName.contains("ETF") || corpName.matches(ETF_PATTERN)`
- 갱신 주기: 주 1회 권장
- 메모리 사용: 파싱 시 약 50~100MB

**사용 예제**:
```kotlin
// 초기 DB 구축
val corpCodes = openDartClient.getCorpCodeList()

// ETF만 필터링
val etfCorpCodes = corpCodes.filter { it.stockCode != null }

// DB에 저장
database.batchInsert("corp_code_mapping", etfCorpCodes)

// 매핑 조회 함수
fun getCorpCode(stockCode: String): String? {
    return database.query(
        "SELECT corp_code FROM corp_code_mapping WHERE stock_code = ?",
        stockCode
    ).singleOrNull()
}
```

---

### 1.2 `getCorpCodeByStockCode()` - 종목코드로 고유번호 조회 (Helper)

**목적**: 캐싱된 매핑 테이블에서 stock_code → corp_code 변환

**함수 시그니처**:
```kotlin
fun getCorpCodeByStockCode(stockCode: String): String?
```

**구현 전략**:
```kotlin
class OpenDartApiClient(
    private val apiKey: String,
    private val database: Database
) {
    // In-memory 캐시
    private val corpCodeCache = ConcurrentHashMap<String, String>()

    fun getCorpCodeByStockCode(stockCode: String): String? {
        // 1. 메모리 캐시 확인
        corpCodeCache[stockCode]?.let { return it }

        // 2. DB 조회
        val corpCode = database.query(
            "SELECT corp_code FROM corp_code_mapping WHERE stock_code = ?",
            stockCode
        ).singleOrNull()

        // 3. 캐시 저장
        corpCode?.let { corpCodeCache[stockCode] = it }

        return corpCode
    }
}
```

---

## 2. 배당 정보

### 2.1 `getDividendInfo()` - 배당 정보 조회

**OPENDART API**: alotMatter.json - 배당에 관한 사항

**목적**: ETF의 배당금 정보 조회 (조정주가 계산에 필수)

**사용 사례**:
- 배당락일 및 배당금 데이터 수집
- `etf_corporate_actions` 테이블 구축
- 조정주가 계산을 위한 배당 이력 관리

**함수 시그니처**:
```kotlin
suspend fun getDividendInfo(
    corpCode: String,
    businessYear: Int,
    reportCode: ReportCode = ReportCode.ANNUAL
): List<DividendInfo>
```

**파라미터**:
- `corpCode`: OPENDART 고유번호 (8자리, 예: "00164779")
- `businessYear`: 사업연도 (4자리, 예: 2024) ※ 2015년 이후만 제공
- `reportCode`: 보고서 코드 (기본값: 사업보고서)

**ReportCode Enum**:
```kotlin
enum class ReportCode(val code: String) {
    ANNUAL("11011"),      // 사업보고서
    SEMI_ANNUAL("11012"), // 반기보고서
    Q1("11013"),          // 1분기보고서
    Q2("11012"),          // 2분기보고서 (반기보고서와 동일)
    Q3("11014"),          // 3분기보고서
    Q4("11011");          // 4분기 (사업보고서와 동일)

    // Deprecated aliases for backward compatibility
    @Deprecated("Use Q1 instead", ReplaceWith("Q1"))
    val QUARTERLY_1 get() = Q1

    @Deprecated("Use Q3 instead", ReplaceWith("Q3"))
    val QUARTERLY_3 get() = Q3
}
```

**참고**: OPENDART는 1분기(Q1), 3분기(Q3)만 별도 보고서가 있으며, Q2는 반기보고서, Q4는 사업보고서로 대체됩니다.

**반환 데이터**:
```kotlin
data class DividendInfo(
    val rceptNo: String,              // 접수번호 (14자리)
    val corpCode: String,             // 고유번호
    val corpName: String,             // 법인명
    val dividendType: DividendType,   // 배당 구분
    val stockKind: String,            // 주식 종류 (보통주, 우선주)
    val currentYear: BigDecimal?,     // 당기 배당금 (원/주)
    val previousYear: BigDecimal?,    // 전기 배당금 (원/주)
    val twoYearsAgo: BigDecimal?,     // 전전기 배당금 (원/주)
    val settlementDate: LocalDate     // 결산기준일
)

enum class DividendType(val korean: String) {
    CASH("현금"),
    STOCK("주식"),
    MIXED("현금+주식")
}
```

**OPENDART API 매핑**:
```
요청: GET https://opendart.fss.or.kr/api/alotMatter.json
쿼리 파라미터:
  crtfc_key={API_KEY}
  corp_code=00164779
  bsns_year=2024
  reprt_code=11011

응답: JSON
{
  "status": "000",
  "message": "정상",
  "list": [
    {
      "rcept_no": "20240315000123",
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

**응답 파싱 로직**:
```kotlin
suspend fun getDividendInfo(
    corpCode: String,
    businessYear: Int,
    reportCode: ReportCode = ReportCode.ANNUAL
): List<DividendInfo> {
    val url = buildUrl("alotMatter", mapOf(
        "corp_code" to corpCode,
        "bsns_year" to businessYear.toString(),
        "reprt_code" to reportCode.code
    ))

    val response = httpClient.get(url).body<OpenDartResponse<DividendInfoRaw>>()

    if (response.status != "000") {
        if (response.status == "013") return emptyList() // 데이터 없음
        throw OpenDartException.from(response.status, response.message)
    }

    return response.list?.map { it.toDomain() } ?: emptyList()
}

@Serializable
private data class DividendInfoRaw(
    val rcept_no: String,
    val corp_code: String,
    val corp_name: String,
    val se: String,
    val stock_knd: String,
    val thstrm: String?,
    val frmtrm: String?,
    val lwfr: String?,
    val stlm_dt: String
) {
    fun toDomain() = DividendInfo(
        rceptNo = rcept_no,
        corpCode = corp_code,
        corpName = corp_name,
        dividendType = when (se) {
            "현금" -> DividendType.CASH
            "주식" -> DividendType.STOCK
            else -> DividendType.MIXED
        },
        stockKind = stock_knd,
        currentYear = thstrm?.toBigDecimalOrNull(),
        previousYear = frmtrm?.toBigDecimalOrNull(),
        twoYearsAgo = lwfr?.toBigDecimalOrNull(),
        settlementDate = LocalDate.parse(stlm_dt)
    )
}
```

**주의사항**:
- 정기보고서 기준이므로 배당락일(ex-date)은 별도 공시 확인 필요
- 월배당 ETF의 경우 분기/반기/사업보고서에 합산된 배당금 제공
- `thstrm` (당기)이 null인 경우 배당 미실시
- 2015년 이전 데이터는 제공되지 않음

**사용 예제**:
```kotlin
// 1. stock_code → corp_code 변환
val corpCode = openDartClient.getCorpCodeByStockCode("069500")
    ?: throw IllegalArgumentException("Corp code not found")

// 2. 2024년 배당 정보 조회
val dividends = openDartClient.getDividendInfo(
    corpCode = corpCode,
    businessYear = 2024,
    reportCode = ReportCode.ANNUAL
)

// 3. DB 저장
dividends.forEach { dividend ->
    database.insert("etf_corporate_actions", mapOf(
        "ticker" to "069500",
        "action_type" to "DIVIDEND",
        "dividend_amount" to dividend.currentYear,
        "settlement_date" to dividend.settlementDate
    ))
}
```

---

## 3. 증자감자 정보

### 3.1 `getStockSplitInfo()` - 증자/감자 정보 조회

**OPENDART API**: irdsSttus.json - 증자(감자) 현황

**목적**: 주식 분할/병합 정보 조회 (조정주가 계산에 필수)

**사용 사례**:
- 주식 분할/병합 이력 수집
- `etf_corporate_actions` 테이블 구축 (split_ratio)
- 조정주가 계산을 위한 분할 비율 관리

**함수 시그니처**:
```kotlin
suspend fun getStockSplitInfo(
    corpCode: String,
    businessYear: Int,
    reportCode: ReportCode = ReportCode.ANNUAL
): List<StockSplitInfo>
```

**파라미터**:
- `corpCode`: OPENDART 고유번호 (8자리)
- `businessYear`: 사업연도 (4자리)
- `reportCode`: 보고서 코드

**반환 데이터**:
```kotlin
data class StockSplitInfo(
    val rceptNo: String,          // 접수번호
    val corpCode: String,         // 고유번호
    val corpName: String,         // 법인명
    val eventDate: LocalDate,     // 증자/감자일
    val eventType: SplitEventType, // 증자/감자 방법
    val stockKind: String,        // 주식 종류
    val quantity: Long,           // 증자/감자 주식수
    val parValuePerShare: Int,    // 1주당 액면가액 (원)
    val totalAmount: Long         // 증가/감소 액면총액 (원)
)

enum class SplitEventType(val korean: String) {
    STOCK_SPLIT("주식분할"),
    STOCK_MERGE("주식병합"),
    PAID_IN_CAPITAL("유상증자"),
    FREE_ISSUE("무상증자"),
    OTHER("기타")
}
```

**OPENDART API 매핑**:
```
요청: GET https://opendart.fss.or.kr/api/irdsSttus.json
쿼리 파라미터:
  crtfc_key={API_KEY}
  corp_code=00164779
  bsns_year=2024
  reprt_code=11011

응답: JSON
{
  "status": "000",
  "message": "정상",
  "list": [
    {
      "rcept_no": "20240315000123",
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

**응답 파싱 로직**:
```kotlin
suspend fun getStockSplitInfo(
    corpCode: String,
    businessYear: Int,
    reportCode: ReportCode = ReportCode.ANNUAL
): List<StockSplitInfo> {
    val url = buildUrl("irdsSttus", mapOf(
        "corp_code" to corpCode,
        "bsns_year" to businessYear.toString(),
        "reprt_code" to reportCode.code
    ))

    val response = httpClient.get(url).body<OpenDartResponse<StockSplitInfoRaw>>()

    if (response.status != "000") {
        if (response.status == "013") return emptyList()
        throw OpenDartException.from(response.status, response.message)
    }

    return response.list?.map { it.toDomain() } ?: emptyList()
}

@Serializable
private data class StockSplitInfoRaw(
    val rcept_no: String,
    val corp_code: String,
    val corp_name: String,
    val isu_dcrs_de: String,
    val isu_dcrs_stle: String,
    val isu_dcrs_stock_knd: String,
    val isu_dcrs_qy: String,
    val isu_dcrs_mstvdv_fval_amount: String,
    val isu_dcrs_mstvdv_amount: String
) {
    fun toDomain() = StockSplitInfo(
        rceptNo = rcept_no,
        corpCode = corp_code,
        corpName = corp_name,
        eventDate = LocalDate.parse(isu_dcrs_de),
        eventType = SplitEventType.fromKorean(isu_dcrs_stle),
        stockKind = isu_dcrs_stock_knd,
        quantity = isu_dcrs_qy.replace(",", "").toLong(),
        parValuePerShare = isu_dcrs_mstvdv_fval_amount.replace(",", "").toInt(),
        totalAmount = isu_dcrs_mstvdv_amount.replace(",", "").toLong()
    )
}
```

**분할 비율 계산 Helper**:
```kotlin
/**
 * 주식 분할/병합 비율 계산
 *
 * 예시:
 * - 1주 → 2주 분할: 2.0
 * - 1주 → 5주 분할: 5.0
 * - 5주 → 1주 병합: 0.2
 * - 10주 → 1주 병합: 0.1
 */
fun StockSplitInfo.calculateSplitRatio(): BigDecimal {
    return when (eventType) {
        SplitEventType.STOCK_SPLIT -> {
            // 분할 비율은 공시 상세 내용에서 추출 필요
            // 또는 액면가 변화로 계산
            BigDecimal.valueOf(2.0) // 예시
        }
        SplitEventType.STOCK_MERGE -> {
            // 병합 비율 계산
            BigDecimal.valueOf(0.5) // 예시
        }
        else -> BigDecimal.ONE
    }
}
```

**주의사항**:
- ETF는 주식 분할/병합이 드물게 발생
- 분할 비율은 공시 상세 내용 확인 필요 (예: "1:2 분할")
- `isu_dcrs_de`가 실제 효력 발생일
- 조정주가 계산 시 분할일 기준으로 이전 가격 모두 재계산

**사용 예제**:
```kotlin
// 주식 분할 정보 조회
val splits = openDartClient.getStockSplitInfo(
    corpCode = corpCode,
    businessYear = 2024
)

// 분할만 필터링
val stockSplits = splits.filter {
    it.eventType == SplitEventType.STOCK_SPLIT ||
    it.eventType == SplitEventType.STOCK_MERGE
}

// DB 저장
stockSplits.forEach { split ->
    database.insert("etf_corporate_actions", mapOf(
        "ticker" to ticker,
        "action_type" to "SPLIT",
        "split_ratio" to split.calculateSplitRatio(),
        "ex_date" to split.eventDate
    ))
}
```

---

## 4. 공시 검색

### 4.1 `searchDisclosures()` - 공시 목록 검색

**OPENDART API**: list.json - 공시검색

**목적**: ETF 관련 공시 검색 (상장/폐지, 중요 공시 감지)

**사용 사례**:
- ETF 신규 상장 감지
- ETF 상장 폐지 감지
- 중요 공시 모니터링 (배당, 분할, 운용 변경 등)

**함수 시그니처**:
```kotlin
suspend fun searchDisclosures(
    corpCode: String? = null,
    startDate: LocalDate,
    endDate: LocalDate,
    publicationType: PublicationType? = null,
    lastReportOnly: Boolean = false,
    pageNo: Int = 1,
    pageCount: Int = 100
): DisclosureListResponse
```

**파라미터**:
- `corpCode`: 고유번호 (null이면 전체 검색)
- `startDate`: 검색 시작일
- `endDate`: 검색 종료일
- `publicationType`: 공시 유형 (null이면 전체)
- `lastReportOnly`: 최종보고서만 검색 여부
- `pageNo`: 페이지 번호 (1부터 시작)
- `pageCount`: 페이지당 건수 (최대 100)

**PublicationType Enum**:
```kotlin
enum class PublicationType(val code: String, val description: String) {
    REGULAR("A", "정기공시"),
    MAJOR("B", "주요사항보고"),
    ISSUANCE("C", "발행공시"),
    EQUITY("D", "지분공시"),
    OTHER("E", "기타공시"),
    EXTERNAL_AUDIT("F", "외부감사관련"),
    CORRECTION("G", "공정공시")
}
```

**반환 데이터**:
```kotlin
data class DisclosureListResponse(
    val status: String,                // 에러/정보 코드
    val message: String,               // 메시지
    val pageNo: Int,                   // 현재 페이지 번호
    val pageCount: Int,                // 페이지당 건수
    val totalCount: Int,               // 총 건수
    val totalPage: Int,                // 총 페이지 수
    val list: List<DisclosureItem>?    // 공시 목록
)

data class DisclosureItem(
    val corpCode: String,       // 고유번호
    val corpName: String,       // 법인명
    val stockCode: String?,     // 종목코드 (6자리, nullable)
    val corpCls: String,        // 법인구분 (Y/K/N/E)
    val reportName: String,     // 보고서명
    val rceptNo: String,        // 접수번호 (14자리)
    val filerName: String,      // 제출인명
    val rceptDate: LocalDate,   // 접수일자
    val remark: String?         // 비고
)
```

**OPENDART API 매핑**:
```
요청: GET https://opendart.fss.or.kr/api/list.json
쿼리 파라미터:
  crtfc_key={API_KEY}
  corp_code=00164779          # (선택)
  bgn_de=20240101             # (필수)
  end_de=20241231             # (필수)
  pblntf_ty=A                 # (선택) 공시유형
  last_reprt_at=Y             # (선택) 최종보고서만
  page_no=1                   # (선택, 기본값: 1)
  page_count=100              # (선택, 기본값: 10, 최대: 100)

응답: JSON
{
  "status": "000",
  "message": "정상",
  "page_no": 1,
  "page_count": 100,
  "total_count": 123,
  "total_page": 2,
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
      "rm": ""
    }
  ]
}
```

**응답 파싱 로직**:
```kotlin
suspend fun searchDisclosures(
    corpCode: String? = null,
    startDate: LocalDate,
    endDate: LocalDate,
    publicationType: PublicationType? = null,
    lastReportOnly: Boolean = false,
    pageNo: Int = 1,
    pageCount: Int = 100
): DisclosureListResponse {
    require(pageCount in 1..100) { "pageCount must be between 1 and 100" }

    val params = mutableMapOf(
        "bgn_de" to startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "end_de" to endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        "page_no" to pageNo.toString(),
        "page_count" to pageCount.toString()
    )

    corpCode?.let { params["corp_code"] = it }
    publicationType?.let { params["pblntf_ty"] = it.code }
    if (lastReportOnly) params["last_reprt_at"] = "Y"

    val url = buildUrl("list", params)
    val response = httpClient.get(url).body<DisclosureListResponseRaw>()

    if (response.status != "000") {
        if (response.status == "013") {
            return DisclosureListResponse(
                status = response.status,
                message = response.message,
                pageNo = pageNo,
                pageCount = pageCount,
                totalCount = 0,
                totalPage = 0,
                list = emptyList()
            )
        }
        throw OpenDartException.from(response.status, response.message)
    }

    return response.toDomain()
}
```

**페이징 Helper**:
```kotlin
/**
 * 전체 공시 목록 조회 (자동 페이징)
 */
suspend fun searchDisclosuresAll(
    corpCode: String? = null,
    startDate: LocalDate,
    endDate: LocalDate,
    publicationType: PublicationType? = null
): List<DisclosureItem> {
    val allItems = mutableListOf<DisclosureItem>()
    var currentPage = 1

    while (true) {
        val response = searchDisclosures(
            corpCode = corpCode,
            startDate = startDate,
            endDate = endDate,
            publicationType = publicationType,
            pageNo = currentPage,
            pageCount = 100
        )

        response.list?.let { allItems.addAll(it) }

        if (currentPage >= response.totalPage) break
        currentPage++

        // Rate limiting
        delay(100)
    }

    return allItems
}
```

**주의사항**:
- 검색 기간: 최대 1년
- 페이징: 최대 100건/페이지
- Rate limiting: 페이지 요청 간 100ms 지연 권장
- `stock_code`가 null인 경우 비상장 법인

**사용 예제**:
```kotlin
// ETF 신규 상장 감지 (최근 1개월)
val disclosures = openDartClient.searchDisclosures(
    startDate = LocalDate.now().minusMonths(1),
    endDate = LocalDate.now(),
    publicationType = PublicationType.ISSUANCE
)

val newEtfListings = disclosures.list?.filter {
    it.reportName.contains("증권발행실적") &&
    it.corpName.contains("ETF")
} ?: emptyList()

// 특정 ETF의 최근 공시 조회
val etfDisclosures = openDartClient.searchDisclosuresAll(
    corpCode = corpCode,
    startDate = LocalDate.now().minusYears(1),
    endDate = LocalDate.now()
)

// 배당 공시 필터링
val dividendDisclosures = etfDisclosures.filter {
    it.reportName.contains("배당") ||
    it.reportName.contains("분배금")
}
```

---

### 4.2 `getDisclosureDocument()` - 공시 문서 상세 조회 (선택)

**목적**: 특정 공시의 상세 내용 조회

**함수 시그니처**:
```kotlin
suspend fun getDisclosureDocument(rceptNo: String): String
```

**파라미터**:
- `rceptNo`: 접수번호 (14자리)

**반환**: HTML 문서 원본 또는 XML

**API 매핑**:
```
GET https://dart.fss.or.kr/dsaf001/main.do?rcpNo={rceptNo}
```

**주의**:
- 이 API는 OPENDART API가 아닌 DART 웹사이트 URL
- HTML 파싱 필요
- 구조화된 데이터 추출이 복잡하므로 선택적 구현

---

## 5. 함수 요약 테이블

| 카테고리 | 함수 개수 | API | 함수명 |
|---------|----------|-----|--------|
| 고유번호 | 2 | corpCode.xml | getCorpCodeList(), getCorpCodeByStockCode() |
| 배당 정보 | 1 | alotMatter.json | getDividendInfo() |
| 증자감자 | 1 | irdsSttus.json | getStockSplitInfo() |
| 공시 검색 | 2 | list.json | searchDisclosures(), searchDisclosuresAll() |
| **합계** | **6** | | |

**Helper 함수** (3개):
- `getCorpCodeByStockCode()` - 메모리/DB 캐시 조회
- `searchDisclosuresAll()` - 자동 페이징
- `StockSplitInfo.calculateSplitRatio()` - 분할 비율 계산

---

## 6. 라이브러리 구조

```
kotlin-krx/
├── src/main/kotlin/
│   ├── api/
│   │   ├── KrxApiClient.kt              # KRX API (15개 함수)
│   │   ├── NaverApiClient.kt            # Naver API (1개 함수)
│   │   └── OpenDartApiClient.kt         # OPENDART API (6개 함수) ⭐
│   │
│   ├── model/
│   │   ├── krx/                         # KRX 모델
│   │   ├── naver/                       # Naver 모델
│   │   └── opendart/                    # OPENDART 모델 ⭐
│   │       ├── CorpCode.kt              # 고유번호
│   │       ├── DividendInfo.kt          # 배당 정보
│   │       ├── StockSplitInfo.kt        # 증자/감자
│   │       ├── DisclosureItem.kt        # 공시 항목
│   │       └── enums/
│   │           ├── ReportCode.kt        # 보고서 코드
│   │           ├── DividendType.kt      # 배당 구분
│   │           ├── SplitEventType.kt    # 증자/감자 유형
│   │           └── PublicationType.kt   # 공시 유형
│   │
│   ├── internal/
│   │   ├── opendart/
│   │   │   ├── OpenDartApiClientImpl.kt # HTTP 호출
│   │   │   ├── OpenDartRateLimiter.kt   # Rate Limiter
│   │   │   ├── OpenDartXmlParser.kt     # XML 파싱 (corpCode)
│   │   │   └── OpenDartResponseParser.kt # JSON 파싱
│   │
│   └── exception/
│       └── OpenDartException.kt         # OPENDART 예외
│
└── src/test/kotlin/
    └── api/
        └── OpenDartApiClientTest.kt     # 통합 테스트
```

---

## 7. 사용 예제

### 7.1 클라이언트 초기화

```kotlin
import kotlin.time.Duration.Companion.milliseconds

val openDartClient = OpenDartApiClient(
    apiKey = System.getenv("OPENDART_API_KEY")
        ?: throw IllegalStateException("OPENDART_API_KEY not set"),
    rateLimiter = RateLimiter(
        maxRequestsPerDay = 20000,
        delayBetweenRequests = 100.milliseconds
    )
)
```

### 7.2 초기 설정 워크플로우

```kotlin
// 1. 고유번호 매핑 테이블 구축
suspend fun initializeCorpCodeMapping(
    openDartClient: OpenDartApiClient,
    database: Database
) {
    logger.info("Fetching corp code list...")
    val corpCodes = openDartClient.getCorpCodeList()

    logger.info("Total corps: ${corpCodes.size}")

    // ETF만 필터링
    val etfCorpCodes = corpCodes.filter {
        it.stockCode != null &&
        (it.corpName.contains("KODEX") ||
         it.corpName.contains("TIGER") ||
         it.corpName.contains("ARIRANG"))
    }

    logger.info("ETF corps: ${etfCorpCodes.size}")

    // DB에 저장
    database.batchInsert("corp_code_mapping", etfCorpCodes) { corpCode ->
        mapOf(
            "stock_code" to corpCode.stockCode,
            "corp_code" to corpCode.corpCode,
            "corp_name" to corpCode.corpName,
            "modify_date" to corpCode.modifyDate
        )
    }

    logger.info("Corp code mapping initialized")
}
```

### 7.3 배당 데이터 수집 워크플로우

```kotlin
// 2. ETF 배당 정보 수집
suspend fun collectEtfDividends(
    ticker: String,
    year: Int,
    openDartClient: OpenDartApiClient,
    database: Database
) {
    // 2-1. stock_code → corp_code 변환
    val corpCode = openDartClient.getCorpCodeByStockCode(ticker)
        ?: throw IllegalArgumentException("Corp code not found for $ticker")

    // 2-2. 배당 정보 조회 (사업보고서)
    val dividends = openDartClient.getDividendInfo(
        corpCode = corpCode,
        businessYear = year,
        reportCode = ReportCode.ANNUAL
    )

    // 2-3. DB 저장
    dividends.forEach { dividend ->
        if (dividend.currentYear != null && dividend.currentYear > BigDecimal.ZERO) {
            database.insert("etf_corporate_actions", mapOf(
                "ticker" to ticker,
                "action_type" to "DIVIDEND",
                "dividend_amount" to dividend.currentYear,
                "settlement_date" to dividend.settlementDate,
                "created_at" to LocalDateTime.now()
            ))
        }
    }

    logger.info("Collected ${dividends.size} dividend records for $ticker in $year")
}
```

### 7.4 주식 분할 데이터 수집

```kotlin
// 3. ETF 주식 분할 정보 수집
suspend fun collectEtfStockSplits(
    ticker: String,
    year: Int,
    openDartClient: OpenDartApiClient,
    database: Database
) {
    val corpCode = openDartClient.getCorpCodeByStockCode(ticker)
        ?: return

    val splits = openDartClient.getStockSplitInfo(
        corpCode = corpCode,
        businessYear = year
    )

    // 주식 분할/병합만 필터링
    val stockSplits = splits.filter {
        it.eventType == SplitEventType.STOCK_SPLIT ||
        it.eventType == SplitEventType.STOCK_MERGE
    }

    stockSplits.forEach { split ->
        database.insert("etf_corporate_actions", mapOf(
            "ticker" to ticker,
            "action_type" to "SPLIT",
            "split_ratio" to split.calculateSplitRatio(),
            "ex_date" to split.eventDate,
            "created_at" to LocalDateTime.now()
        ))
    }

    logger.info("Collected ${stockSplits.size} split records for $ticker in $year")
}
```

### 7.5 신규 ETF 상장 감지

```kotlin
// 4. 신규 ETF 상장 감지
suspend fun detectNewEtfListings(
    openDartClient: OpenDartApiClient,
    startDate: LocalDate = LocalDate.now().minusMonths(1),
    endDate: LocalDate = LocalDate.now()
): List<DisclosureItem> {
    val disclosures = openDartClient.searchDisclosuresAll(
        startDate = startDate,
        endDate = endDate,
        publicationType = PublicationType.ISSUANCE
    )

    return disclosures.filter {
        it.stockCode != null &&
        it.reportName.contains("증권발행실적") &&
        (it.corpName.contains("KODEX") ||
         it.corpName.contains("TIGER") ||
         it.corpName.contains("ARIRANG"))
    }
}
```

### 7.6 통합 ETF 데이터 수집 스케줄러

```kotlin
class EtfCorporateActionCollector(
    private val openDartClient: OpenDartApiClient,
    private val database: Database
) {
    /**
     * 전체 ETF의 배당/분할 정보 수집
     * 매년 4월 1일 실행 권장 (사업보고서 제출 후)
     */
    suspend fun collectAllEtfCorporateActions(year: Int) {
        // 1. 매핑 테이블 갱신
        initializeCorpCodeMapping(openDartClient, database)

        // 2. 전체 ETF 목록 조회
        val etfList = database.query("SELECT ticker FROM etf_master")

        logger.info("Collecting corporate actions for ${etfList.size} ETFs in $year")

        etfList.forEach { ticker ->
            try {
                // 3. 배당 정보 수집
                collectEtfDividends(ticker, year, openDartClient, database)

                // 4. 분할 정보 수집
                collectEtfStockSplits(ticker, year, openDartClient, database)

                delay(100) // Rate limiting

            } catch (e: OpenDartException.NoData) {
                logger.debug("No data for $ticker in $year")
            } catch (e: Exception) {
                logger.error("Error collecting data for $ticker", e)
            }
        }

        logger.info("Corporate action collection completed for $year")
    }
}
```

---

## 8. 테스트 전략

### 8.1 단위 테스트

```kotlin
class OpenDartApiClientTest {
    private val testApiKey = System.getenv("OPENDART_TEST_API_KEY")
    private lateinit var client: OpenDartApiClient

    @BeforeEach
    fun setup() {
        client = OpenDartApiClient(testApiKey)
    }

    @Test
    fun `getCorpCodeList should return corp codes`() = runBlocking {
        val corpCodes = client.getCorpCodeList()

        assertThat(corpCodes).isNotEmpty()
        assertThat(corpCodes.first().corpCode).hasSize(8)
    }

    @Test
    fun `getDividendInfo should return dividends for valid corp`() = runBlocking {
        val corpCode = "00164779" // KODEX 200
        val dividends = client.getDividendInfo(corpCode, 2023)

        // 데이터가 있을 수도, 없을 수도 있음
        assertThat(dividends).isNotNull()
    }

    @Test
    fun `searchDisclosures should return disclosures`() = runBlocking {
        val response = client.searchDisclosures(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31)
        )

        assertThat(response.status).isEqualTo("000")
    }
}
```

### 8.2 통합 테스트

```kotlin
@IntegrationTest
class EtfCorporateActionCollectorTest {
    @Test
    fun `collect KODEX 200 dividends for 2023`() = runBlocking {
        val collector = EtfCorporateActionCollector(openDartClient, database)

        collector.collectEtfDividends("069500", 2023, openDartClient, database)

        val actions = database.query(
            "SELECT * FROM etf_corporate_actions WHERE ticker = ? AND action_type = ?",
            "069500", "DIVIDEND"
        )

        assertThat(actions).isNotEmpty()
    }
}
```

---

## 9. 에러 처리 및 재시도 전략

### 9.1 재시도 로직

```kotlin
suspend fun <T> retryOnRateLimit(
    maxAttempts: Int = 3,
    initialDelay: Long = 60000, // 1분
    maxDelay: Long = 3600000,   // 1시간
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(maxAttempts - 1) { attempt ->
        try {
            return block()
        } catch (e: OpenDartException.RateLimitExceeded) {
            logger.warn("Rate limit exceeded, waiting ${currentDelay}ms (attempt ${attempt + 1})")
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return block() // 마지막 시도
}
```

### 9.2 사용 예제

```kotlin
val dividends = retryOnRateLimit {
    openDartClient.getDividendInfo(corpCode, year)
}
```

---

**작성일**: 2025-01-18
**버전**: v1.0
**작성자**: kotlin-krx 프로젝트
