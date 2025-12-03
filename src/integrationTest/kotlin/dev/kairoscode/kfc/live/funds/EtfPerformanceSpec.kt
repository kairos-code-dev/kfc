package dev.kairoscode.kfc.live.funds

import dev.kairoscode.kfc.utils.IntegrationTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.SmartRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 성과 지표 조회 Integration Test
 *
 * getTrackingError(), getDivergenceRate() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfPerformanceSpec : IntegrationTestBase() {

    // ================================
    // 추적 오차 테스트
    // ================================

    @Test
    @DisplayName("ETF와 벤치마크 간 추적 오차를 조회할 수 있다")
    fun testGetTrackingError() = integrationTest {
        // Given: TIGER 200 ISIN, 1개월 기간
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 추적 오차 조회
        val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

        // Then: 일별 추적 오차 데이터 반환
        assertTrue(trackingErrors.isNotEmpty(), "추적 오차 데이터가 반환되어야 합니다")

        println("✅ 추적 오차 데이터 개수: ${trackingErrors.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = trackingErrors,
            category = RecordingConfig.Paths.EtfMetrics.TRACKING_ERROR,
            fileName = "tiger200_tracking_error"
        )
    }

    @Test
    @DisplayName("[활용] 평균 추적 오차를 계산할 수 있다")
    fun testAverageTrackingError() = integrationTest {
        // Given: 추적 오차 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)
        val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

        assertTrue(trackingErrors.isNotEmpty(), "추적 오차 데이터가 있어야 합니다")

        // When: 절대값의 평균 계산
        val avgTrackingError = trackingErrors
            .map { abs(it.trackingErrorRate) }
            .average()

        // Then: 평균 추적 오차 출력
        println("\n=== 평균 추적 오차 ===")
        println("평균 추적 오차: ${"%.4f".format(avgTrackingError)}%")
    }

    // ================================
    // 괴리율 테스트
    // ================================

    @Test
    @DisplayName("ETF 가격과 NAV 간 괴리율을 조회할 수 있다")
    fun testGetDivergenceRate() = integrationTest {
        // Given: TIGER 200 ISIN, 1개월 기간
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 괴리율 조회
        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

        // Then: 일별 괴리율 데이터 반환
        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 반환되어야 합니다")

        println("✅ 괴리율 데이터 개수: ${divergenceRates.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = divergenceRates,
            category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
            fileName = "tiger200_divergence_rate"
        )
    }

    @Test
    @DisplayName("KODEX 200 괴리율을 조회할 수 있다")
    fun testGetDivergenceRateKodex200() = integrationTest {
        // Given: KODEX 200 ISIN
        val isin = TestSymbols.KODEX_200_ISIN
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 괴리율 조회
        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

        // Then: 데이터 반환
        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 반환되어야 합니다")

        println("✅ KODEX 200 괴리율 데이터 개수: ${divergenceRates.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = divergenceRates,
            category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
            fileName = "kodex200_divergence_rate"
        )
    }

    @Test
    @DisplayName("[활용] 괴리율이 높은 날을 찾을 수 있다")
    fun testHighDivergenceRateDays() = integrationTest {
        // Given: 괴리율 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)
        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 있어야 합니다")

        // When: 절대값 기준 정렬
        val highDivergenceDays = divergenceRates
            .filter { abs(it.divergenceRate) > 0.5 }
            .sortedByDescending { abs(it.divergenceRate) }

        // Then: 괴리율 ±0.5% 초과 날짜 출력
        println("\n=== 괴리율이 높은 날 (±0.5% 초과) ===")
        if (highDivergenceDays.isNotEmpty()) {
            highDivergenceDays.forEach { day ->
                println("${day.tradeDate}: ${"%.2f".format(day.divergenceRate)}%")
            }
        } else {
            println("괴리율이 ±0.5%를 초과하는 날이 없습니다.")
        }
    }

    @Test
    @DisplayName("[활용] 평균 괴리율을 계산할 수 있다")
    fun testAverageDivergenceRate() = integrationTest {
        // Given: 괴리율 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)
        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 있어야 합니다")

        // When: 평균 계산
        val avgDivergence = divergenceRates.map { it.divergenceRate }.average()

        // Then: 평균 괴리율 출력
        println("\n=== 평균 괴리율 ===")
        println("평균 괴리율: ${"%.4f".format(avgDivergence)}%")
    }
}
