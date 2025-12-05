package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.*

/**
 * FundsApi Performance Metrics Integration Test Specification
 *
 * ## API 개요
 * ETF 성과 지표를 조회하는 API입니다.
 * 추적 오차(Tracking Error)와 괴리율(Divergence Rate)을 제공합니다.
 *
 * ## 엔드포인트
 * ### 추적 오차
 * ```kotlin
 * suspend fun getTrackingError(isin: String, fromDate: LocalDate, toDate: LocalDate): List<TrackingError>
 * ```
 *
 * ### 괴리율
 * ```kotlin
 * suspend fun getDivergenceRate(isin: String, fromDate: LocalDate, toDate: LocalDate): List<DivergenceRate>
 * ```
 *
 * ## 파라미터
 * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
 * - `fromDate`: LocalDate - 조회 시작일
 * - `toDate`: LocalDate - 조회 종료일
 *
 * ## 응답 데이터
 * ### TrackingError
 * - `tradeDate`: LocalDate - 거래일
 * - `trackingErrorRate`: Double - 추적 오차율(%)
 *
 * ### DivergenceRate
 * - `tradeDate`: LocalDate - 거래일
 * - `divergenceRate`: Double - 괴리율(%)
 *
 * ## 특이사항
 * - API Key 불필요 (KRX 공개 데이터)
 * - 일별 데이터 제공 (거래일만)
 */
@DisplayName("FundsApi Performance Metrics - ETF 성과 지표 조회")
class FundsApiPerformanceSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

    @Nested
    @DisplayName("1. 기본 동작 (Basic Operations)")
    inner class BasicOperations {

        @Test
        @DisplayName("TIGER 200의 추적 오차를 조회할 수 있다")
        fun get_tracking_error_for_tiger200() = integrationTest {
            println("\n📘 API: getTrackingError()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 ISIN, 1 month period
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • fromDate: LocalDate = $fromDate")
            println("  • toDate: LocalDate = $toDate")

            // When: Request tracking error
            val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

            // Then: Returns tracking error data
            assertTrue(trackingErrors.isNotEmpty(), "추적 오차 데이터가 반환되어야 합니다")

            println("\n📤 Response: List<TrackingError>")
            println("  • dataPoints: ${trackingErrors.size}개")
            println("  • period: $fromDate ~ $toDate")

            if (trackingErrors.isNotEmpty()) {
                val firstItem = trackingErrors.first()
                val lastItem = trackingErrors.last()
                println("  • first: ${firstItem.tradeDate} - ${firstItem.trackingErrorRate}%")
                println("  • last: ${lastItem.tradeDate} - ${lastItem.trackingErrorRate}%")
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = trackingErrors,
                category = RecordingConfig.Paths.EtfMetrics.TRACKING_ERROR,
                fileName = "tiger200_tracking_error"
            )
        }

        @Test
        @DisplayName("TIGER 200의 괴리율을 조회할 수 있다")
        fun get_divergence_rate_for_tiger200() = integrationTest {
            println("\n📘 API: getDivergenceRate()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 ISIN, 1 month period
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • fromDate: LocalDate = $fromDate")
            println("  • toDate: LocalDate = $toDate")

            // When: Request divergence rate
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            // Then: Returns divergence rate data
            assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 반환되어야 합니다")

            println("\n📤 Response: List<DivergenceRate>")
            println("  • dataPoints: ${divergenceRates.size}개")
            println("  • period: $fromDate ~ $toDate")

            if (divergenceRates.isNotEmpty()) {
                val firstItem = divergenceRates.first()
                val lastItem = divergenceRates.last()
                println("  • first: ${firstItem.tradeDate} - ${firstItem.divergenceRate}%")
                println("  • last: ${lastItem.tradeDate} - ${lastItem.divergenceRate}%")
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = divergenceRates,
                category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
                fileName = "tiger200_divergence_rate"
            )
        }

        @Test
        @DisplayName("[파라미터: isin] KODEX 200의 괴리율을 조회할 수 있다")
        fun get_divergence_rate_for_kodex200() = integrationTest {
            println("\n📘 파라미터 테스트: isin = KODEX_200")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: KODEX 200 ISIN
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            println("📥 Input:")
            println("  • isin: \"$isin\" (KODEX 200)")

            // When: Request divergence rate
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            // Then: Returns data
            assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 반환되어야 합니다")

            println("\n📤 Response:")
            println("  • dataPoints: ${divergenceRates.size}개")

            println("\n✅ 결과: KODEX 200 괴리율 조회 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = divergenceRates,
                category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
                fileName = "kodex200_divergence_rate"
            )
        }
    }

    // ========================================
    // 2. 응답 데이터 검증 (Response Validation)
    // ========================================

    @Nested
    @DisplayName("2. 응답 데이터 검증 (Response Validation)")
    inner class ResponseValidation {

        @Test
        @DisplayName("추적 오차 응답은 일별 데이터를 포함한다")
        fun tracking_error_response_contains_daily_data() = integrationTest {
            println("\n📘 응답 데이터 검증: 추적 오차")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            // When
            val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

            // Then: Validate structure
            assertTrue(trackingErrors.isNotEmpty(), "최소 1개 이상의 데이터가 있어야 합니다")

            val firstItem = trackingErrors.first()
            assertNotNull(firstItem.tradeDate, "tradeDate가 있어야 합니다")

            println("✅ 응답 구조 검증 통과:")
            println("  • dataPoints: ${trackingErrors.size}개 (> 0) ✓")
            println("  • 첫 번째 항목 tradeDate: ${firstItem.tradeDate} ✓")
            println("  • 첫 번째 항목 trackingErrorRate: ${firstItem.trackingErrorRate}% ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("괴리율 응답은 일별 데이터를 포함한다")
        fun divergence_rate_response_contains_daily_data() = integrationTest {
            println("\n📘 응답 데이터 검증: 괴리율")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            // When
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            // Then: Validate structure
            assertTrue(divergenceRates.isNotEmpty(), "최소 1개 이상의 데이터가 있어야 합니다")

            val firstItem = divergenceRates.first()
            assertNotNull(firstItem.tradeDate, "tradeDate가 있어야 합니다")

            println("✅ 응답 구조 검증 통과:")
            println("  • dataPoints: ${divergenceRates.size}개 (> 0) ✓")
            println("  • 첫 번째 항목 tradeDate: ${firstItem.tradeDate} ✓")
            println("  • 첫 번째 항목 divergenceRate: ${firstItem.divergenceRate}% ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("데이터는 날짜순으로 정렬되어 있다")
        fun data_is_sorted_by_date() = integrationTest {
            println("\n📘 응답 데이터 검증: 날짜 정렬")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            // When
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            // Then: Verify sorted by date
            if (divergenceRates.size >= 2) {
                for (i in 0 until divergenceRates.size - 1) {
                    assertTrue(
                        divergenceRates[i].tradeDate <= divergenceRates[i + 1].tradeDate,
                        "데이터는 날짜순으로 정렬되어야 합니다"
                    )
                }
            }

            println("✅ 날짜 정렬 검증:")
            println("  • 첫 번째 날짜: ${divergenceRates.first().tradeDate}")
            println("  • 마지막 날짜: ${divergenceRates.last().tradeDate}")
            println("  • 정렬 순서: 오름차순 ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 3. 입력 파라미터 검증 (Input Validation)
    // ========================================

    @Nested
    @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
    inner class InputValidation {

        @Test
        @DisplayName("존재하지 않는 ISIN 조회시 빈 리스트를 반환한다")
        fun returns_empty_list_for_non_existent_isin() = integrationTest {
            println("\n📘 입력 검증: 존재하지 않는 ISIN")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Invalid ISIN that doesn't exist
            val invalidIsin = "KR7999999999"
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            println("📥 Input:")
            println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
            println("  • period: $fromDate ~ $toDate")

            // When
            val divergenceRates = client.funds.getDivergenceRate(invalidIsin, fromDate, toDate)

            // Then: Returns empty list for non-existent ISIN
            assertTrue(divergenceRates.isEmpty(), "존재하지 않는 ISIN은 빈 리스트를 반환해야 합니다")

            println("\n📤 Response: List<DivergenceRate> (empty)")
            println("  • dataPoints: ${divergenceRates.size}")
            println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 리스트 반환")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 4. 엣지 케이스 (Edge Cases)
    // ========================================

    @Nested
    @DisplayName("4. 엣지 케이스 (Edge Cases)")
    inner class EdgeCases {

        @Test
        @DisplayName("[파라미터: period] 짧은 기간(1주) 조회가 가능하다")
        fun supports_short_period_query() = integrationTest {
            println("\n📘 엣지 케이스: 짧은 기간 조회")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: 1 week period
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusWeeks(1)

            println("📥 Input:")
            println("  • period: $fromDate ~ $toDate (1주)")

            // When
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            // Then: Returns data for short period
            println("\n📤 Response:")
            println("  • dataPoints: ${divergenceRates.size}개")

            println("\n✅ 결과: 짧은 기간 조회 가능")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[파라미터: period] 긴 기간(3개월) 조회가 가능하다")
        fun supports_long_period_query() = integrationTest {
            println("\n📘 엣지 케이스: 긴 기간 조회")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: 3 months period
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(3)

            println("📥 Input:")
            println("  • period: $fromDate ~ $toDate (3개월)")

            // When
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            // Then: Returns data for long period
            println("\n📤 Response:")
            println("  • dataPoints: ${divergenceRates.size}개")

            println("\n✅ 결과: 긴 기간 조회 가능")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 5. 활용 예제 (Usage Examples)
    // ========================================

    @Nested
    @DisplayName("5. 활용 예제 (Usage Examples)")
    inner class UsageExamples {

        @Test
        @DisplayName("[활용] 평균 추적 오차를 계산할 수 있다")
        fun calculate_average_tracking_error() = integrationTest {
            println("\n📘 활용 예제: 평균 추적 오차 계산")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Tracking error data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)
            val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

            assertTrue(trackingErrors.isNotEmpty(), "추적 오차 데이터가 있어야 합니다")

            // When: Calculate average of absolute values
            val avgTrackingError = trackingErrors
                .map { abs(it.trackingErrorRate) }
                .average()

            // Then: Display analysis
            println("\n=== 평균 추적 오차 분석 ===")
            println("기간: $fromDate ~ $toDate")
            println("평균 추적 오차: ${"%.4f".format(avgTrackingError)}%")
            println()
            println("📊 분석: 추적 오차가 낮을수록 벤치마크 지수를 정확히 추종합니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 괴리율이 높은 날을 찾을 수 있다")
        fun find_high_divergence_rate_days() = integrationTest {
            println("\n📘 활용 예제: 괴리율 높은 날 찾기")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Divergence rate data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 있어야 합니다")

            // When: Filter by absolute value > 0.5%
            val highDivergenceDays = divergenceRates
                .filter { abs(it.divergenceRate) > 0.5 }
                .sortedByDescending { abs(it.divergenceRate) }

            // Then: Display high divergence days
            println("\n=== 괴리율이 높은 날 (±0.5% 초과) ===")
            if (highDivergenceDays.isNotEmpty()) {
                highDivergenceDays.forEach { day ->
                    println("${day.tradeDate}: ${"%.2f".format(day.divergenceRate)}%")
                }
            } else {
                println("괴리율이 ±0.5%를 초과하는 날이 없습니다.")
            }
            println()
            println("📊 분석: 괴리율이 높은 날은 ETF 가격이 NAV에서 많이 벗어난 날입니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 평균 괴리율을 계산할 수 있다")
        fun calculate_average_divergence_rate() = integrationTest {
            println("\n📘 활용 예제: 평균 괴리율 계산")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Divergence rate data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)
            val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

            assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 있어야 합니다")

            // When: Calculate average
            val avgDivergence = divergenceRates.map { it.divergenceRate }.average()
            val avgAbsDivergence = divergenceRates.map { abs(it.divergenceRate) }.average()

            // Then: Display analysis
            println("\n=== 평균 괴리율 분석 ===")
            println("기간: $fromDate ~ $toDate")
            println("평균 괴리율: ${"%.4f".format(avgDivergence)}%")
            println("평균 절대 괴리율: ${"%.4f".format(avgAbsDivergence)}%")
            println()
            println("📊 분석: 평균 괴리율이 0에 가까울수록 ETF 가격이 NAV에 가깝게 거래됩니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 여러 ETF의 괴리율을 비교할 수 있다")
        fun compare_divergence_rates_across_etfs() = integrationTest {
            println("\n📘 활용 예제: ETF 간 괴리율 비교")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Multiple ETFs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val toDate = TestFixtures.Dates.TRADING_DAY
            val fromDate = toDate.minusMonths(1)

            // When: Fetch divergence rates for both
            val tiger200Divergence = client.funds.getDivergenceRate(tiger200Isin, fromDate, toDate)
            val kodex200Divergence = client.funds.getDivergenceRate(kodex200Isin, fromDate, toDate)

            assertTrue(tiger200Divergence.isNotEmpty(), "TIGER 200 데이터가 있어야 합니다")
            assertTrue(kodex200Divergence.isNotEmpty(), "KODEX 200 데이터가 있어야 합니다")

            // Then: Compare average divergence rates
            val tiger200Avg = tiger200Divergence.map { abs(it.divergenceRate) }.average()
            val kodex200Avg = kodex200Divergence.map { abs(it.divergenceRate) }.average()

            println("\n=== KOSPI 200 추종 ETF 괴리율 비교 ===")
            println("기간: $fromDate ~ $toDate")
            println()
            println("TIGER 200")
            println("  • 평균 절대 괴리율: ${"%.4f".format(tiger200Avg)}%")
            println("  • 데이터 포인트: ${tiger200Divergence.size}개")
            println()
            println("KODEX 200")
            println("  • 평균 절대 괴리율: ${"%.4f".format(kodex200Avg)}%")
            println("  • 데이터 포인트: ${kodex200Divergence.size}개")
            println()
            println("📊 분석: 동일 지수 추종 ETF 간 괴리율을 비교하여 유동성/거래 효율성을 평가할 수 있습니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
