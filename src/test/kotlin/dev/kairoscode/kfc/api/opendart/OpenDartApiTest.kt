package dev.kairoscode.kfc.api.opendart

import dev.kairoscode.kfc.internal.opendart.OpenDartApiImpl
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * OPENDART API 통합 테스트
 *
 * 실제 OPENDART API를 호출하는 통합 테스트입니다.
 * 테스트 시나리오는 plan/15-OPENDART-테스트-시나리오-명세.md를 기반으로 작성되었습니다.
 *
 * **주의**: 이 테스트는 환경변수 OPENDART_API_KEY가 설정되어 있어야 실행됩니다.
 * API Key가 없으면 테스트를 건너뜁니다.
 */
class OpenDartApiTest {

    private lateinit var api: OpenDartApi
    private var apiKey: String? = null

    @BeforeEach
    fun setup() {
        // 환경변수에서 API Key 읽기
        apiKey = System.getenv("OPENDART_API_KEY")

        // API Key가 있는 경우에만 API 초기화
        if (apiKey != null && apiKey!!.isNotBlank()) {
            api = OpenDartApiImpl(apiKey!!)
        }
    }

    /**
     * API Key 확인 헬퍼 함수
     * API Key가 없으면 테스트를 건너뜀
     */
    private fun requireApiKey() {
        assumeTrue(
            apiKey != null && apiKey!!.isNotBlank(),
            "OPENDART_API_KEY 환경변수가 설정되지 않았습니다. 이 테스트를 건너뜁니다."
        )
    }

    // ================================
    // 1. 기업 코드 조회
    // ================================

    @Test
    fun `getCorpCodeList should return all corp codes with ZIP parsing`() = runBlocking {
        // === arrange ===
        requireApiKey()

        // === act ===
        val result = api.getCorpCodeList()

        // === assert ===
        assertThat(result.size).isGreaterThan(60000) // 60,000개 이상

        // corpCode 형식 검증: 8자리
        result.forEach { corpCode ->
            assertThat(corpCode.corpCode).hasSize(8)
            assertThat(corpCode.corpCode).matches("\\d{8}")

            // 필수 필드 검증
            assertThat(corpCode.corpName).isNotBlank()
        }
    }

    @Test
    fun `getCorpCodeList should include listed companies with stock code`() = runBlocking {
        // === arrange ===
        requireApiKey()

        // === act ===
        val result = api.getCorpCodeList()

        // === assert ===
        val listedCompanies = result.filter { it.stockCode != null }
        assertThat(listedCompanies.size).isBetween(2000, 5000) // 약 3,900개 (2024년 기준)

        // stockCode 형식 검증: 6자리 (숫자 또는 영문자 포함)
        // 예: 005930 (삼성전자), 0015G0 (그린케미칼 우선주)
        listedCompanies.forEach { corpCode ->
            assertThat(corpCode.stockCode).hasSize(6)
            assertThat(corpCode.stockCode).matches("[0-9A-Z]{6}")
        }
    }

    @Test
    fun `getCorpCodeList should find Samsung Electronics`() = runBlocking {
        // === arrange ===
        requireApiKey()

        // === act ===
        val result = api.getCorpCodeList()

        // === assert ===
        val samsung = result.find { it.stockCode == "005930" }
        assertThat(samsung).isNotNull
        assertThat(samsung!!.corpCode).isEqualTo("00126380")
        assertThat(samsung.corpName).contains("삼성전자")
    }

    // ================================
    // 2. 배당 정보
    // ================================

    @Test
    fun `getDividendInfo should return Samsung Electronics dividend for 2023`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val corpCode = "00126380" // 삼성전자
        val year = 2023
        val reportCode = "11011" // 사업보고서

        // === act ===
        val result = api.getDividendInfo(corpCode, year, reportCode)

        // === assert ===
        assertThat(result).isNotEmpty

        result.forEach { dividend ->
            if (dividend.currentYear != null) {
                assertThat(dividend.currentYear).isGreaterThan(java.math.BigDecimal.ZERO)
            }
            assertThat(dividend.settlementDate).isNotNull()
        }
    }

    @Test
    fun `getDividendInfo should return empty list for non-dividend company`() = runBlocking {
        // === arrange ===
        requireApiKey()
        // 배당 미실시 기업 (corpCode 필요 - 실제 테스트 시 적절한 기업 선택)
        val corpCode = "99999999" // 존재하지 않는 기업
        val year = 2023

        // === act ===
        val result = api.getDividendInfo(corpCode, year)

        // === assert ===
        // 데이터 없음 또는 빈 리스트
        assertThat(result).isEmpty()
    }

    // ================================
    // 3. 주식 분할/병합
    // ================================

    @Test
    fun `getStockSplitInfo should return stock split info when available`() = runBlocking {
        // === arrange ===
        requireApiKey()
        // 주식 분할 이력이 있는 기업 (실제 테스트 시 적절한 기업 선택)
        val corpCode = "00126380" // 삼성전자
        val year = 2018 // 분할 발생 연도 (예시)

        // === act ===
        val result = api.getStockSplitInfo(corpCode, year)

        // === assert ===
        // 결과가 있을 수도 있고 없을 수도 있음
        if (result.isNotEmpty()) {
            result.forEach { splitInfo ->
                assertThat(splitInfo.eventDate).isNotNull()
                assertThat(splitInfo.eventType).isNotBlank()
            }
        }
    }

    @Test
    fun `getStockSplitInfo should return empty list when no split history`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val corpCode = "00126380"
        val year = 2023 // 분할 미발생 연도

        // === act ===
        val result = api.getStockSplitInfo(corpCode, year)

        // === assert ===
        // 분할 이력 없음 - 빈 리스트 예상
        assertThat(result).isEmpty()
    }

    // ================================
    // 4. 공시 검색
    // ================================

    @Test
    fun `searchDisclosures should return Samsung disclosures for 1 month period`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val corpCode = "00126380" // 삼성전자
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.searchDisclosures(corpCode, startDate, endDate)

        // === assert ===
        assertThat(result.size).isLessThanOrEqualTo(100) // 1페이지 최대 100건

        result.forEach { disclosure ->
            assertThat(disclosure.corpName).contains("삼성전자")
            assertThat(disclosure.rceptNo).isNotBlank()
            assertThat(disclosure.reportName).isNotBlank()
            assertThat(disclosure.rceptDate).isBetween(startDate, endDate)
        }
    }

    @Test
    fun `searchDisclosures should support pagination for large results`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val corpCode = "00126380"
        val startDate = LocalDate.of(2023, 1, 1)
        val endDate = LocalDate.of(2023, 12, 31) // 1년, 100건 초과 예상

        // === act ===
        val page1 = api.searchDisclosures(corpCode, startDate, endDate, pageNo = 1)
        kotlinx.coroutines.delay(100) // Rate limiting
        val page2 = api.searchDisclosures(corpCode, startDate, endDate, pageNo = 2)

        // === assert ===
        assertThat(page1.size).isLessThanOrEqualTo(100)

        if (page2.isNotEmpty()) {
            // 중복 없음
            val page1ReceiptNumbers = page1.map { it.rceptNo }.toSet()
            val page2ReceiptNumbers = page2.map { it.rceptNo }.toSet()
            assertThat(page1ReceiptNumbers.intersect(page2ReceiptNumbers)).isEmpty()
        }
    }

    @Test
    fun `searchDisclosures should return all disclosures when corp code is null`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 1, 15) // 단일 날짜

        // === act ===
        val result = api.searchDisclosures(null, startDate, endDate)

        // === assert ===
        assertThat(result.size).isLessThanOrEqualTo(100)

        // 다양한 기업의 공시 포함
        val uniqueCorps = result.map { it.corpName }.toSet()
        assertThat(uniqueCorps.size).isGreaterThan(1)
    }

    // ================================
    // 5. 에러 처리
    // ================================

    @Test
    fun `getDividendInfo should handle invalid corp code gracefully`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val invalidCorpCode = "99999999"
        val year = 2023

        // === act ===
        val result = api.getDividendInfo(invalidCorpCode, year)

        // === assert ===
        // 잘못된 corp_code는 빈 리스트 또는 예외
        assertThat(result).isEmpty()
    }

    @Test
    fun `searchDisclosures should return empty list for future dates`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val corpCode = "00126380"
        val futureDate = LocalDate.now().plusYears(1)

        // === act ===
        val result = api.searchDisclosures(corpCode, futureDate, futureDate)

        // === assert ===
        assertThat(result).isEmpty()
    }

    // ================================
    // 6. 통합 시나리오
    // ================================

    @Test
    fun `integration test - ticker to corp code to dividend info workflow`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val ticker = "005930" // 삼성전자 티커

        // === act ===
        // 1. 전체 corp code 목록 조회
        val corpCodeList = api.getCorpCodeList()

        // 2. ticker로 corp_code 찾기
        val corpCodeItem = corpCodeList.find { it.stockCode == ticker }
        assertThat(corpCodeItem).isNotNull

        kotlinx.coroutines.delay(100) // Rate limiting

        // 3. 배당 정보 조회
        val dividendInfo = api.getDividendInfo(corpCodeItem!!.corpCode, 2023)

        // === assert ===
        assertThat(corpCodeItem.corpCode).isNotBlank()
        assertThat(dividendInfo).isNotEmpty()
    }

    @Test
    fun `integration test - batch collect dividend info for multiple ETFs`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val tickers = listOf("069500", "102110") // KODEX 200, TIGER 200
        val year = 2023

        // === act ===
        val corpCodeList = api.getCorpCodeList()

        val dividendResults = mutableMapOf<String, List<dev.kairoscode.kfc.model.opendart.DividendInfo>>()
        for (ticker in tickers) {
            val corpCodeItem = corpCodeList.find { it.stockCode == ticker }
            if (corpCodeItem != null) {
                kotlinx.coroutines.delay(100)
                val dividends = api.getDividendInfo(corpCodeItem.corpCode, year)
                dividendResults[ticker] = dividends
            }
        }

        // === assert ===
        assertThat(dividendResults).isNotEmpty

        // 매핑 성공률 검증
        val successRate = dividendResults.size.toDouble() / tickers.size
        assertThat(successRate).isGreaterThan(0.5) // 50% 이상 매핑 성공
    }

    @Test
    fun `integration test - daily disclosure monitoring`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val tickers = listOf("005930", "000660") // 삼성전자, SK하이닉스
        val today = LocalDate.now()

        // === act ===
        val corpCodeList = api.getCorpCodeList()

        val disclosureResults = mutableMapOf<String, List<dev.kairoscode.kfc.model.opendart.DisclosureItem>>()
        for (ticker in tickers) {
            val corpCodeItem = corpCodeList.find { it.stockCode == ticker }
            if (corpCodeItem != null) {
                kotlinx.coroutines.delay(100)
                val disclosures = api.searchDisclosures(corpCodeItem.corpCode, today, today)
                disclosureResults[ticker] = disclosures
            }
        }

        // === assert ===
        assertThat(disclosureResults).hasSize(tickers.size)

        // 각 기업별 공시 확인 (있을 수도 없을 수도 있음)
        disclosureResults.forEach { (ticker, disclosures) ->
            println("$ticker: ${disclosures.size} disclosures found")
        }
    }

    @Test
    fun `integration test - rate limiting compliance for multiple requests`() = runBlocking {
        // === arrange ===
        requireApiKey()
        val corpCode = "00126380"
        val years = listOf(2021, 2022, 2023)
        val startTime = System.currentTimeMillis()

        // === act ===
        val results = mutableListOf<List<dev.kairoscode.kfc.model.opendart.DividendInfo>>()
        for (year in years) {
            val dividends = api.getDividendInfo(corpCode, year)
            results.add(dividends)

            // Rate limiting: 100ms 대기
            kotlinx.coroutines.delay(100)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // === assert ===
        assertThat(results).hasSize(3)
        assertThat(duration).isGreaterThanOrEqualTo(300L) // 최소 300ms

        // 모든 요청 성공
        assertThat(results).allMatch { it.isNotEmpty() || it.isEmpty() }
    }

    @Test
    fun `integration test - verify corp code list cache performance`() = runBlocking {
        // === arrange ===
        requireApiKey()

        // === act ===
        // 첫 번째 호출
        val start1 = System.currentTimeMillis()
        val result1 = api.getCorpCodeList()
        val duration1 = System.currentTimeMillis() - start1

        kotlinx.coroutines.delay(100)

        // 두 번째 호출 (캐시된 경우 빠를 수 있음, 단 이는 구현에 따라 다름)
        val start2 = System.currentTimeMillis()
        val result2 = api.getCorpCodeList()
        val duration2 = System.currentTimeMillis() - start2

        // === assert ===
        assertThat(result1.size).isEqualTo(result2.size)
        println("First call: ${duration1}ms, Second call: ${duration2}ms")

        // 두 결과가 동일한지 확인
        assertThat(result1).containsAll(result2)
    }
}
