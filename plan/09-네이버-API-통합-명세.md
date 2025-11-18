# 네이버 API 통합 명세

## 개요

본 문서는 kotlin-krx 라이브러리에서 조정주가(adjusted price) 지원을 위한 네이버 증권 API 통합 방안을 제시합니다. pykrx의 네이버 통합 방식을 분석하고, Kotlin으로 포팅하기 위한 구현 가이드를 제공합니다.

**문서 작성일**: 2025-11-18
**대상 독자**: 백엔드 개발자, 라이브러리 메인테이너
**참고**: pykrx v1.0.42 기준

---

## 1. 왜 네이버 API인가?

### 문제점: KRX API는 조정주가 미제공

```
KRX API (MDCSTAT04501)
├─ 원본 OHLCV ✅
├─ NAV, 괴리율 ✅
└─ 조정주가 ❌ 미제공
```

**조정주가 필요 사유**:
- 배당금 지급 후 가격 급락을 보정
- 액면분할/병합 이벤트 반영
- 장기 백테스팅 정확도 향상
- 수익률 계산 정규화

### pykrx의 해결 방안

```python
# pykrx 접근 방식
def get_market_ohlcv(ticker, fromdate, todate, adjusted=True):
    if adjusted:
        return naver.get_market_ohlcv_by_date(...)  # 네이버 스크래핑
    else:
        return krx.get_market_ohlcv_by_date(...)    # KRX API
```

**선택 이유**:
1. ✅ Stateless (DB 불필요)
2. ✅ 즉시 사용 가능
3. ✅ 배당 이력 수집 불필요
4. ✅ 복잡한 계산 로직 불필요

---

## 2. 네이버 증권 API 분석

### 2.1 엔드포인트 구조

#### A. 차트 데이터 API (주로 사용)

**URL**:
```
https://fchart.stock.naver.com/sise.nhn
```

**파라미터**:
```kotlin
data class NaverChartParams(
    val symbol: String,      // 티커 (예: "069500")
    val timeframe: String,   // "day", "week", "month"
    val count: Int,          // 조회할 데이터 개수
    val requestType: String  // "0" (일반), "1" (외국인)
)
```

**요청 예시**:
```http
GET https://fchart.stock.naver.com/sise.nhn?symbol=069500&timeframe=day&count=100&requestType=0
```

**응답 형식**: XML

```xml
<protocol>
    <chartdata symbol="069500">
        <item data="20240102|42075|43250|41900|42965|192061" />
        <item data="20240103|43500|44120|43200|43850|235123" />
        <!-- 형식: 날짜|시가|고가|저가|종가|거래량 -->
    </chartdata>
</protocol>
```

#### B. 일별 시세 페이지 (스크래핑)

**URL**:
```
https://finance.naver.com/item/sise_day.nhn?code={ticker}&page={page}
```

**응답**: HTML 테이블

```html
<table class="type2">
    <tr>
        <td class="date">2024.01.02</td>
        <td class="num">42,965</td>  <!-- 종가 -->
        <td class="num">+1,080</td>  <!-- 대비 -->
        <td class="num">+2.58%</td>  <!-- 등락률 -->
        <td class="num">42,075</td>  <!-- 시가 -->
        <td class="num">43,250</td>  <!-- 고가 -->
        <td class="num">41,900</td>  <!-- 저가 -->
        <td class="num">192,061</td> <!-- 거래량 -->
    </tr>
</table>
```

---

### 2.2 pykrx 구현 분석

#### 소스 파일 구조

```
pykrx/
└── website/
    └── naver/
        ├── core.py      # HTTP 클라이언트, XML 파싱
        └── wrap.py      # 고수준 API
```

#### core.py 핵심 로직

```python
class Sise(NaverWebIo):
    def fetch(self, ticker, count, timeframe='day'):
        """
        네이버 차트 데이터 조회
        """
        result = self.read(
            symbol=ticker,
            timeframe=timeframe,
            count=count,
            requestType="0"
        )
        return result.text  # XML 응답

    def parse(self, xml_text):
        """
        XML 파싱하여 DataFrame 생성
        """
        root = ElementTree.fromstring(xml_text)
        items = root.findall(".//item")

        data = []
        for item in items:
            fields = item.get('data').split('|')
            data.append({
                '날짜': fields[0],
                '시가': int(fields[1]),
                '고가': int(fields[2]),
                '저가': int(fields[3]),
                '종가': int(fields[4]),
                '거래량': int(fields[5])
            })

        return pd.DataFrame(data)
```

#### wrap.py 고수준 API

```python
def get_market_ohlcv_by_date(fromdate, todate, ticker):
    """
    조정주가 OHLCV 조회
    """
    # 1. 날짜 차이 계산
    days = (todate - fromdate).days + 1

    # 2. 네이버 API 호출
    sise = Sise()
    xml_text = sise.fetch(ticker, count=days)

    # 3. 파싱 및 변환
    df = sise.parse(xml_text)

    # 4. 날짜 필터링
    df = df[(df['날짜'] >= fromdate) & (df['날짜'] <= todate)]

    return df.sort_values('날짜')
```

---

## 3. Kotlin 구현 설계

### 3.1 아키텍처

```
kotlin-krx/
└── src/main/kotlin/com/kairos/krx/
    ├── client/
    │   ├── KrxClient.kt           # KRX API 클라이언트
    │   └── NaverClient.kt         # ← 새로 추가
    ├── service/
    │   └── EtfService.kt          # adjusted 파라미터 지원
    ├── model/
    │   └── Ohlcv.kt
    └── util/
        └── NaverXmlParser.kt      # ← 새로 추가
```

### 3.2 NaverClient 구현

```kotlin
package com.kairos.krx.client

import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 네이버 증권 API 클라이언트
 *
 * 조정주가 데이터를 네이버 차트 API에서 조회
 */
class NaverClient(
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val BASE_URL = "https://fchart.stock.naver.com/sise.nhn"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    /**
     * 조정주가 OHLCV 조회
     *
     * @param ticker 6자리 티커
     * @param from 시작일
     * @param to 종료일
     * @return 조정주가 OHLCV 리스트
     * @throws NaverApiException 네이버 API 오류
     */
    fun getAdjustedOhlcv(
        ticker: String,
        from: LocalDate,
        to: LocalDate
    ): List<Ohlcv> {

        // 1. 조회할 일수 계산
        val days = ChronoUnit.DAYS.between(from, to).toInt() + 1
        val count = (days * 1.2).toInt()  // 여유분 20% 추가 (휴장일 고려)

        // 2. API 호출
        val xml = fetchChartData(ticker, count)

        // 3. XML 파싱
        val ohlcvList = parseXmlResponse(xml)

        // 4. 날짜 필터링
        return ohlcvList
            .filter { it.date in from..to }
            .sortedBy { it.date }
    }

    /**
     * 네이버 차트 API 호출
     */
    private fun fetchChartData(
        ticker: String,
        count: Int,
        timeframe: String = "day"
    ): String {

        val url = "$BASE_URL?symbol=$ticker&timeframe=$timeframe&count=$count&requestType=0"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Referer", "https://finance.naver.com/")
            .build()

        logger.debug("Fetching adjusted price from Naver: ticker={}, count={}", ticker, count)

        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw NaverApiException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.string()
                ?: throw NaverApiException("Empty response body")
        }
    }

    /**
     * XML 응답 파싱
     *
     * 형식: <item data="20240102|42075|43250|41900|42965|192061" />
     */
    private fun parseXmlResponse(xml: String): List<Ohlcv> {

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(InputSource(StringReader(xml)))

        val items = doc.getElementsByTagName("item")
        val result = mutableListOf<Ohlcv>()

        for (i in 0 until items.length) {
            val item = items.item(i) as Element
            val data = item.getAttribute("data")

            if (data.isNullOrBlank()) continue

            val fields = data.split("|")
            if (fields.size < 6) {
                logger.warn("Invalid data format: {}", data)
                continue
            }

            try {
                result.add(
                    Ohlcv(
                        date = parseDate(fields[0]),
                        open = parseBigDecimal(fields[1]),
                        high = parseBigDecimal(fields[2]),
                        low = parseBigDecimal(fields[3]),
                        close = parseBigDecimal(fields[4]),
                        volume = fields[5].toLong()
                    )
                )
            } catch (e: Exception) {
                logger.warn("Failed to parse item: {}", data, e)
            }
        }

        return result
    }

    /**
     * 날짜 파싱: "20240102" → LocalDate
     */
    private fun parseDate(yyyyMMdd: String): LocalDate {
        require(yyyyMMdd.length == 8) { "Invalid date format: $yyyyMMdd" }

        val year = yyyyMMdd.substring(0, 4).toInt()
        val month = yyyyMMdd.substring(4, 6).toInt()
        val day = yyyyMMdd.substring(6, 8).toInt()

        return LocalDate.of(year, month, day)
    }

    /**
     * 숫자 파싱: "42965" → BigDecimal
     */
    private fun parseBigDecimal(value: String): BigDecimal {
        return value.trim()
            .replace(",", "")
            .toBigDecimalOrNull()
            ?: BigDecimal.ZERO
    }
}

/**
 * 네이버 API 예외
 */
class NaverApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

---

### 3.3 EtfService 통합

```kotlin
package com.kairos.krx.service

import com.kairos.krx.client.KrxClient
import com.kairos.krx.client.NaverClient
import com.kairos.krx.model.Ohlcv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EtfService(
    private val krxClient: KrxClient,
    private val naverClient: NaverClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * OHLCV 조회
     *
     * @param adjusted true면 네이버 조정주가, false면 KRX 원본
     */
    fun getOhlcv(
        ticker: String,
        from: LocalDate,
        to: LocalDate = from,
        adjusted: Boolean = false
    ): List<Ohlcv> {

        return if (adjusted) {
            logger.info("Fetching adjusted OHLCV from Naver: {}", ticker)
            try {
                naverClient.getAdjustedOhlcv(ticker, from, to)
            } catch (e: Exception) {
                logger.error("Naver API failed, falling back to KRX raw data", e)
                // 폴백: KRX 원본 데이터 반환
                krxClient.getOhlcv(ticker, from, to)
            }
        } else {
            logger.info("Fetching raw OHLCV from KRX: {}", ticker)
            krxClient.getOhlcv(ticker, from, to)
        }
    }
}
```

---

## 4. 에러 처리 및 폴백 전략

### 4.1 발생 가능한 오류

| 오류 유형 | 원인 | 대응 |
|---------|------|------|
| HTTP 4xx/5xx | 네이버 서버 오류 | KRX 원본으로 폴백 |
| XML 파싱 오류 | 응답 형식 변경 | 예외 발생 + 로그 |
| 빈 응답 | 잘못된 티커 | 예외 발생 |
| 타임아웃 | 네트워크 지연 | 재시도 (최대 3회) |
| Rate Limiting | 과도한 요청 | Exponential backoff |

### 4.2 재시도 로직

```kotlin
class NaverClientWithRetry(
    private val httpClient: OkHttpClient,
    private val maxRetries: Int = 3
) {

    fun getAdjustedOhlcvWithRetry(
        ticker: String,
        from: LocalDate,
        to: LocalDate
    ): List<Ohlcv> {

        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                return getAdjustedOhlcv(ticker, from, to)
            } catch (e: Exception) {
                lastException = e
                logger.warn("Attempt ${attempt + 1} failed: {}", e.message)

                if (attempt < maxRetries - 1) {
                    // Exponential backoff: 1초, 2초, 4초
                    val delayMs = 1000L * (1 shl attempt)
                    Thread.sleep(delayMs)
                }
            }
        }

        throw NaverApiException(
            "Failed after $maxRetries attempts",
            lastException
        )
    }
}
```

### 4.3 Rate Limiting 대응

```kotlin
class RateLimitedNaverClient(
    private val naverClient: NaverClient
) {
    private val rateLimiter = RateLimiter.create(10.0)  // 초당 10 요청

    fun getAdjustedOhlcv(
        ticker: String,
        from: LocalDate,
        to: LocalDate
    ): List<Ohlcv> {

        // Rate limit 적용
        rateLimiter.acquire()

        return naverClient.getAdjustedOhlcv(ticker, from, to)
    }
}
```

---

## 5. 테스트 전략

### 5.1 Fake 구현

```kotlin
class FakeNaverClient : NaverClient {

    private val fakeResponses = mutableMapOf<String, List<Ohlcv>>()

    fun setFakeResponse(ticker: String, data: List<Ohlcv>) {
        fakeResponses[ticker] = data
    }

    override fun getAdjustedOhlcv(
        ticker: String,
        from: LocalDate,
        to: LocalDate
    ): List<Ohlcv> {

        val allData = fakeResponses[ticker]
            ?: throw NaverApiException("No fake data for ticker: $ticker")

        return allData.filter { it.date in from..to }
    }
}
```

### 5.2 통합 테스트

```kotlin
@SpringBootTest
class NaverClientIntegrationTest {

    @Autowired
    private lateinit var naverClient: NaverClient

    @Test
    fun `조정주가 조회 성공`() {
        // Given
        val ticker = "069500"  // KODEX 200
        val from = LocalDate.of(2024, 1, 2)
        val to = LocalDate.of(2024, 1, 10)

        // When
        val result = naverClient.getAdjustedOhlcv(ticker, from, to)

        // Then
        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it.date in from..to }
        assertThat(result).isSortedAccordingTo(compareBy { it.date })

        // 가격 검증
        result.forEach { ohlcv ->
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.low)
            assertThat(ohlcv.close).isBetween(ohlcv.low, ohlcv.high)
            assertThat(ohlcv.volume).isGreaterThan(0)
        }
    }

    @Test
    fun `잘못된 티커로 조회 시 예외 발생`() {
        // Given
        val invalidTicker = "999999"

        // When & Then
        assertThrows<NaverApiException> {
            naverClient.getAdjustedOhlcv(
                invalidTicker,
                LocalDate.now().minusDays(10),
                LocalDate.now()
            )
        }
    }
}
```

---

## 6. 성능 최적화

### 6.1 HTTP 클라이언트 튜닝

```kotlin
val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .connectionPool(ConnectionPool(
        maxIdleConnections = 5,
        keepAliveDuration = 5,
        TimeUnit.MINUTES
    ))
    .build()
```

### 6.2 병렬 처리

```kotlin
/**
 * 여러 티커의 조정주가를 병렬로 조회
 */
fun getAdjustedOhlcvForMultipleTickers(
    tickers: List<String>,
    from: LocalDate,
    to: LocalDate
): Map<String, List<Ohlcv>> {

    return tickers.parallelStream()
        .map { ticker ->
            ticker to naverClient.getAdjustedOhlcv(ticker, from, to)
        }
        .toList()
        .toMap()
}
```

---

## 7. 법적 고려사항

### 7.1 이용약관 (ToS)

**네이버 증권 로봇 배제 표준**:
```
https://finance.naver.com/robots.txt
```

**확인 사항**:
- ✅ API 엔드포인트가 robots.txt에서 허용되는지 확인
- ⚠️ 과도한 요청 자제 (Rate Limiting 적용)
- ⚠️ User-Agent 명시

### 7.2 권장사항

```kotlin
// User-Agent에 프로젝트 정보 명시
private const val USER_AGENT = "kotlin-krx/1.0.0 (https://github.com/your-org/kotlin-krx)"
```

### 7.3 면책 조항

**라이브러리 문서에 명시**:

> **네이버 API 사용 관련 주의사항**
>
> - `adjusted=true` 옵션은 네이버 증권에서 데이터를 스크래핑합니다
> - 네이버 서비스 정책 변경 시 동작하지 않을 수 있습니다
> - 프로덕션 환경에서는 안정성을 위해 자체 조정주가 계산 권장
> - Rate Limiting이 적용되어 있으나, 과도한 요청은 자제해주세요
> - 이 기능의 사용으로 인한 법적 책임은 사용자에게 있습니다

---

## 8. 대안: OPENDART 계산 방식

**네이버 스크래핑이 불안정할 경우 사용자가 직접 구현 가능**:

```kotlin
// 예제 코드 제공 (라이브러리에 포함 안 함)
class AdjustedPriceCalculator(
    private val opendartClient: OpendartClient,
    private val krxClient: KrxClient
) {

    fun calculateAdjustedPrices(
        ticker: String,
        from: LocalDate,
        to: LocalDate
    ): List<Ohlcv> {

        // 1. 원본 가격 조회
        val rawPrices = krxClient.getOhlcv(ticker, from, to)

        // 2. 배당 이력 조회
        val dividends = opendartClient.getDividends(ticker, from, to)

        // 3. 역순 누적 계산
        return adjustPrices(rawPrices, dividends)
    }
}
```

---

## 9. 구현 체크리스트

### Phase 1: 기본 구현 (3-5일)
- [ ] `NaverClient` 기본 구현
- [ ] XML 파싱 로직
- [ ] `EtfService`에 `adjusted` 파라미터 추가
- [ ] 단위 테스트 (Fake 사용)

### Phase 2: 안정화 (2-3일)
- [ ] 에러 처리 및 폴백
- [ ] 재시도 로직
- [ ] Rate Limiting
- [ ] 통합 테스트

### Phase 3: 최적화 (1-2일)
- [ ] HTTP 클라이언트 튜닝
- [ ] 병렬 처리 지원
- [ ] 성능 테스트

### Phase 4: 문서화 (1일)
- [ ] API 문서 업데이트
- [ ] 사용 예제 작성
- [ ] 면책 조항 추가
- [ ] README 업데이트

---

## 10. 마이그레이션 가이드

### pykrx → kotlin-krx

**pykrx**:
```python
from pykrx import stock

# 조정주가
df = stock.get_market_ohlcv(
    "20240101", "20240131", "069500",
    adjusted=True
)
```

**kotlin-krx**:
```kotlin
val service = etfService

// 조정주가
val ohlcv = service.getOhlcv(
    ticker = "069500",
    from = LocalDate.of(2024, 1, 1),
    to = LocalDate.of(2024, 1, 31),
    adjusted = true
)
```

---

## 부록

### A. 네이버 API 엔드포인트 상세

| 엔드포인트 | 용도 | 응답 형식 | 조정주가 |
|----------|------|---------|---------|
| `/sise.nhn` | 차트 데이터 | XML | ✅ 제공 |
| `/item/sise_day.nhn` | 일별 시세 | HTML | ✅ 제공 |
| `/item/coinfo.nhn` | 기업 정보 | HTML | ❌ |

### B. 참고 자료

- pykrx 소스: https://github.com/sharebook-kr/pykrx
- 네이버 증권: https://finance.naver.com/
- OkHttp 문서: https://square.github.io/okhttp/

---

**문서 버전**: 1.0
**최종 수정일**: 2025-11-18
**작성자**: Claude (Anthropic AI)
**검토자**: TBD
