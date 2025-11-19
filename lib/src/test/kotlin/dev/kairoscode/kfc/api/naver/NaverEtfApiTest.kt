package dev.kairoscode.kfc.api.naver

import dev.kairoscode.kfc.internal.naver.NaverEtfApiImpl
import dev.kairoscode.kfc.model.naver.NaverEtfOhlcv
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Naver ETF API 통합 테스트
 *
 * 실제 Naver Finance Chart API를 호출하는 통합 테스트입니다.
 * 테스트 시나리오는 plan/14-Naver-테스트-시나리오-명세.md를 기반으로 작성되었습니다.
 */
class NaverEtfApiTest {

    private lateinit var api: NaverEtfApi

    @BeforeEach
    fun setup() {
        api = NaverEtfApiImpl()
    }

    // ================================
    // 1. 조정 종가 조회
    // ================================

    @Test
    fun `getAdjustedOhlcv should return 1 month data when valid range`() = runBlocking {
        // === arrange ===
        val ticker = "069500" // KODEX 200
        // 최근 1개월 데이터 조회 (500일 이내)
        val toDate = LocalDate.now().minusDays(1) // 어제
        val fromDate = toDate.minusDays(30)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        assertThat(result).isNotEmpty
        assertThat(result.size).isBetween(15, 25) // 약 20 거래일

        // 날짜 범위 검증
        result.forEach { ohlcv ->
            assertThat(ohlcv.date).isBetween(fromDate, toDate)
        }

        // 모든 필드 검증
        result.forEach { ohlcv ->
            assertThat(ohlcv.close).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.open).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.high).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.low).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.volume).isGreaterThanOrEqualTo(0L)
        }

        // 날짜 정렬 확인 (오름차순)
        for (i in 0 until result.size - 1) {
            assertThat(result[i].date).isBefore(result[i + 1].date)
        }
    }

    @Test
    fun `getAdjustedOhlcv should return single day data when same from and to date`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, date, date)

        // === assert ===
        assertThat(result).hasSize(1)
        assertThat(result.first().date).isEqualTo(date)

        val ohlcv = result.first()
        assertThat(ohlcv.close).isGreaterThan(BigDecimal.ZERO)
        assertThat(ohlcv.volume).isGreaterThanOrEqualTo(0L)
    }

    @Test
    fun `getAdjustedOhlcv should return up to 500 days data`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        // 최근 500 거래일 (약 2년)
        val toDate = LocalDate.now()
        val fromDate = toDate.minusDays(730)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        assertThat(result.size).isLessThanOrEqualTo(500) // Naver API 최대 500일 제한

        // 중복 날짜 없음
        val uniqueDates = result.map { it.date }.toSet()
        assertThat(uniqueDates.size).isEqualTo(result.size)

        // 날짜 연속성
        for (i in 0 until result.size - 1) {
            assertThat(result[i].date).isBefore(result[i + 1].date)
        }
    }

    // ================================
    // 2. 데이터 검증
    // ================================

    @Test
    fun `getAdjustedOhlcv should have valid OHLC logic`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        val toDate = LocalDate.now().minusDays(1)
        val fromDate = toDate.minusDays(30)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        // 가격 논리 검증
        result.forEach { ohlcv ->
            assertThat(ohlcv.close).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.open).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.high).isGreaterThan(BigDecimal.ZERO)
            assertThat(ohlcv.low).isGreaterThan(BigDecimal.ZERO)

            // High >= Low
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.low)

            // High >= Open, Close
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.open)
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.close)

            // Low <= Open, Close
            assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.open)
            assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.close)
        }
    }

    @Test
    fun `getAdjustedOhlcv should return only trading days`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        val toDate = LocalDate.now().minusDays(1)
        val fromDate = toDate.minusDays(30)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        // 주말 날짜가 없어야 함
        result.forEach { ohlcv ->
            val dayOfWeek = ohlcv.date.dayOfWeek
            assertThat(dayOfWeek).isNotIn(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        }

        // 중복 날짜 없음
        val uniqueDates = result.map { it.date }.toSet()
        assertThat(uniqueDates.size).isEqualTo(result.size)
    }

    @Test
    fun `getAdjustedOhlcv should have valid volume`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        val toDate = LocalDate.now().minusDays(1)
        val fromDate = toDate.minusDays(30)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        result.forEach { ohlcv ->
            assertThat(ohlcv.volume).isGreaterThanOrEqualTo(0L)
        }

        // 대부분의 거래일에서 volume > 0
        val tradingDays = result.count { it.volume > 0 }
        assertThat(tradingDays.toDouble() / result.size).isGreaterThan(0.9) // 90% 이상
    }

    // ================================
    // 3. 조정 인수 검증
    // ================================

    @Test
    fun `getAdjustedOhlcv should have consistent prices over short period`() = runBlocking {
        // === arrange ===
        // 배당 미발생 기간 (조정 인수 ≈ 1)
        val ticker = "069500"
        val toDate = LocalDate.now().minusDays(1)
        val fromDate = toDate.minusDays(10) // 짧은 기간

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        // 조정 인수 계산: adjustedClose / (실제 종가는 알 수 없으므로 검증 제한적)
        // 대신 가격의 연속성 검증
        for (i in 0 until result.size - 1) {
            val current = result[i]
            val next = result[i + 1]

            // 일간 가격 변화가 -30% ~ +30% 범위 내
            val priceChangeRate = ((next.close - current.close) / current.close * BigDecimal(100)).toDouble()
            assertThat(priceChangeRate).isBetween(-30.0, 30.0)
        }
    }

    // ================================
    // 4. 에러 처리
    // ================================

    @Test
    fun `getAdjustedOhlcv should return empty list when invalid ticker`() = runBlocking {
        // === arrange ===
        val invalidTicker = "INVALID"
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.getAdjustedOhlcv(invalidTicker, fromDate, toDate)

        // === assert ===
        // 잘못된 티커는 빈 리스트 또는 예외
        assertThat(result).isEmpty()
    }

    @Test
    fun `getAdjustedOhlcv should return empty list when future date`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        val futureDate = LocalDate.now().plusDays(10)

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, futureDate, futureDate)

        // === assert ===
        assertThat(result).isEmpty()
    }

    @Test
    fun `getAdjustedOhlcv should handle reversed date range gracefully`() = runBlocking {
        // === arrange ===
        val ticker = "069500"
        val fromDate = LocalDate.of(2024, 1, 31)
        val toDate = LocalDate.of(2024, 1, 1) // 역순

        // === act ===
        val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        // 역순 날짜는 빈 리스트 또는 예외
        assertThat(result).isEmpty()
    }

    // ================================
    // 5. 통합 시나리오
    // ================================

    @Test
    fun `integration test - long period data collection with chunking`() = runBlocking {
        // === arrange ===
        // 500일 초과 기간 수집 (분할 요청 필요)
        val ticker = "069500"
        val toDate = LocalDate.of(2024, 1, 31)
        val fromDate = LocalDate.of(2022, 1, 3) // 약 2년

        // === act ===
        // 첫 번째 요청: 최근 500일
        val firstBatch = api.getAdjustedOhlcv(ticker, fromDate, toDate)

        // === assert ===
        assertThat(firstBatch.size).isLessThanOrEqualTo(500)

        // 500일 초과 시 추가 요청 필요
        if (firstBatch.size == 500) {
            val firstDate = firstBatch.first().date
            val previousEndDate = firstDate.minusDays(1)

            // 두 번째 요청
            kotlinx.coroutines.delay(100)
            val secondBatch = api.getAdjustedOhlcv(ticker, fromDate, previousEndDate)

            // 중복 없음
            val allDates = (firstBatch + secondBatch).map { it.date }.toSet()
            assertThat(allDates.size).isEqualTo(firstBatch.size + secondBatch.size)
        }
    }

    @Test
    fun `integration test - batch collection workflow for multiple ETFs`() = runBlocking {
        // === arrange ===
        val tickers = listOf("069500", "102110", "114800", "153130", "232080")
        val date = LocalDate.now().minusDays(1)
        val startTime = System.currentTimeMillis()

        // === act ===
        val results = mutableMapOf<String, List<NaverEtfOhlcv>>()
        for (ticker in tickers) {
            val result = api.getAdjustedOhlcv(ticker, date, date)
            results[ticker] = result

            // Rate limiting: 100ms 대기
            kotlinx.coroutines.delay(100)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // === assert ===
        assertThat(results).hasSize(5)
        assertThat(duration).isBetween(500L, 1000L) // 500ms ~ 1초

        // 성공률 검증
        val successCount = results.values.count { it.isNotEmpty() }
        assertThat(successCount.toDouble() / results.size).isGreaterThan(0.9) // 90% 이상 성공
    }

    @Test
    fun `integration test - verify OHLC logic across multiple ETFs`() = runBlocking {
        // === arrange ===
        val tickers = listOf("069500", "102110", "114800")
        val toDate = LocalDate.now().minusDays(1)
        val fromDate = toDate.minusDays(30)

        // === act ===
        val allResults = mutableListOf<NaverEtfOhlcv>()
        for (ticker in tickers) {
            val result = api.getAdjustedOhlcv(ticker, fromDate, toDate)
            allResults.addAll(result)
            kotlinx.coroutines.delay(100)
        }

        // === assert ===
        assertThat(allResults).isNotEmpty

        // 모든 데이터의 OHLC 논리 검증
        allResults.forEach { ohlcv ->
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.low)
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.open)
            assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.close)
            assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.open)
            assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.close)
        }
    }
}
