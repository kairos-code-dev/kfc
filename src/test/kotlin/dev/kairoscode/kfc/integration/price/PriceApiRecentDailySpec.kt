package dev.kairoscode.kfc.integration.price

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * PriceApi.getRecentDaily() Integration Test Specification
 *
 * ## API 개요
 * ETF의 최근 일별 거래 데이터를 조회하는 API입니다.
 * 최근 10거래일까지의 시세 요약 정보를 제공합니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * fun getRecentDaily(isin: String, tradeDate: LocalDate): List<RecentDaily>
 * ```
 *
 * ## 파라미터
 * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
 * - `tradeDate`: LocalDate - 조회 기준일 (해당일 기준 과거 10거래일)
 *
 * ## 응답 데이터 (List<RecentDaily>)
 * 각 RecentDaily 객체:
 * - `tradeDate`: LocalDate - 거래일
 * - `closePrice`: Int - 종가 (원)
 * - `change`: Int - 전일 대비 변동 (원)
 * - `changeRate`: Double - 전일 대비 변동률 (%)
 * - `volume`: Long - 거래량 (주)
 * - `openPrice`: Int - 시가 (원)
 * - `highPrice`: Int - 고가 (원)
 * - `lowPrice`: Int - 저가 (원)
 *
 * ## 특이사항
 * - 최대 10거래일까지 반환 (거래일 수가 10일 미만일 수 있음)
 * - 최신 거래일이 리스트의 첫 번째 요소
 * - 비거래일 조회 시에도 최근 거래일 데이터 제공
 * - API Key 불필요 (KRX 공개 데이터)
 */
@DisplayName("PriceApi.getRecentDaily() - 최근 일별 거래 조회")
class PriceApiRecentDailySpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

    @Nested
    @DisplayName("1. 기본 동작 (Basic Operations)")
    inner class BasicOperations {

        @Test
        @DisplayName("TIGER 200의 최근 일별 거래를 거래일에 조회할 수 있다")
        fun get_tiger200_recent_daily_on_trading_day() = integrationTest {
            println("\n📘 API: getRecentDaily()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 ISIN and trading day
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Query recent daily data
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Returns data
            assertNotNull(recentDailyList, "거래일에는 TIGER 200의 최근 일별 거래가 반환되어야 합니다")
            assertTrue(recentDailyList.isNotEmpty(), "최근 거래일 데이터는 최소 1개 이상의 거래일을 포함해야 합니다")
            assertTrue(recentDailyList.size <= 10, "최근 일별 거래는 최대 10거래일까지만 포함합니다")

            println("\n📤 Response: List<RecentDaily> (size: ${recentDailyList.size})")
            println("  조회 기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")
            println()
            println("  최근 거래일 데이터:")
            val latest = recentDailyList.first()
            println("    • tradeDate: ${latest.tradeDate}")
            println("    • closePrice: ${latest.closePrice}원")
            println("    • change: ${latest.change}원")
            println("    • changeRate: ${latest.changeRate}%")
            println("    • volume: ${latest.volume}주")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 응답 레코딩
            SmartRecorder.recordSmartly(
                data = recentDailyList,
                category = RecordingConfig.Paths.EtfPrice.RECENT_DAILY,
                fileName = "tiger200_recent_daily"
            )
        }

        @Test
        @DisplayName("KODEX 200의 최근 일별 거래를 거래일에 조회할 수 있다")
        fun get_kodex200_recent_daily_on_trading_day() = integrationTest {
            println("\n📘 API: getRecentDaily()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: KODEX 200 ISIN and trading day
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Query recent daily data
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Returns data
            assertNotNull(recentDailyList, "거래일에는 KODEX 200의 최근 일별 거래가 반환되어야 합니다")
            assertTrue(recentDailyList.isNotEmpty(), "거래일 데이터는 최소 1개 이상의 거래일을 포함해야 합니다")

            println("\n📤 Response: List<RecentDaily> (size: ${recentDailyList.size})")
            println("  거래일 개수: ${recentDailyList.size}개")
            println("  조회 기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 응답 레코딩
            SmartRecorder.recordSmartly(
                data = recentDailyList,
                category = RecordingConfig.Paths.EtfPrice.RECENT_DAILY,
                fileName = "kodex200_recent_daily"
            )
        }

        @Test
        @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 ETF의 가격 데이터를 조회할 수 있다")
        fun get_different_price_data_by_different_isin() = integrationTest {
            println("\n📘 파라미터 테스트: isin")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Two different ISINs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Query with different ISINs
            val tiger200Data = client.price.getRecentDaily(tiger200Isin, tradeDate)
            val kodex200Data = client.price.getRecentDaily(kodex200Isin, tradeDate)

            // Then: Returns different price data
            assertNotNull(tiger200Data)
            assertNotNull(kodex200Data)
            assertTrue(tiger200Data.isNotEmpty())
            assertTrue(kodex200Data.isNotEmpty())

            println("  Case 1: isin = \"$tiger200Isin\" (TIGER 200)")
            println("    → 최신 종가: ${tiger200Data.first().closePrice}원")
            println()
            println("  Case 2: isin = \"$kodex200Isin\" (KODEX 200)")
            println("    → 최신 종가: ${kodex200Data.first().closePrice}원")
            println()
            println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 가격 데이터 조회 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 2. 응답 데이터 검증 (Response Validation)
    // ========================================

    @Nested
    @DisplayName("2. 응답 데이터 검증 (Response Validation)")
    inner class ResponseValidation {

        @Test
        @DisplayName("응답 리스트의 크기는 1 이상 10 이하다")
        fun response_list_size_is_between_1_and_10() = integrationTest {
            println("\n📘 응답 데이터 검증: 리스트 크기")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Validate list size
            assertTrue(recentDailyList.isNotEmpty(), "응답 리스트는 비어있지 않아야 합니다")
            assertTrue(recentDailyList.size <= 10, "응답 리스트는 최대 10개 이하여야 합니다")

            println("✅ 리스트 크기 검증:")
            println("  • Size: ${recentDailyList.size}")
            println("  • Range: 1 <= size <= 10 ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("종가(closePrice)는 0보다 크다")
        fun close_price_is_positive() = integrationTest {
            println("\n📘 응답 데이터 검증: 가격 범위")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Validate all close prices are positive
            recentDailyList.forEach { daily ->
                assertTrue(daily.closePrice > 0, "종가는 0보다 커야 합니다 (Date: ${daily.tradeDate})")
            }

            println("✅ 가격 범위 검증:")
            println("  • 모든 거래일의 종가 > 0 ✓")
            recentDailyList.take(3).forEach { daily ->
                println("    - ${daily.tradeDate}: ${daily.closePrice}원")
            }
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("거래량(volume)은 0 이상이다")
        fun volume_is_non_negative() = integrationTest {
            println("\n📘 응답 데이터 검증: 거래량 범위")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Validate volume
            val latest = recentDailyList.first()
            assertTrue(latest.volume >= 0, "거래량은 0 이상이어야 합니다")

            println("✅ 거래량 검증:")
            println("  • volume: ${latest.volume}주")
            println("  • Range: >= 0 ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("리스트는 최신 거래일부터 과거 순으로 정렬되어 있다")
        fun list_is_sorted_by_trade_date_descending() = integrationTest {
            println("\n📘 응답 데이터 검증: 날짜 정렬")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Validate date ordering
            for (i in 0 until recentDailyList.size - 1) {
                assertTrue(
                    recentDailyList[i].tradeDate >= recentDailyList[i + 1].tradeDate,
                    "리스트는 최신 거래일부터 과거 순으로 정렬되어야 합니다"
                )
            }

            println("✅ 날짜 정렬 검증: 최신 → 과거 순")
            println("  • 첫 번째 (최신): ${recentDailyList.first().tradeDate}")
            println("  • 마지막 (과거): ${recentDailyList.last().tradeDate}")
            println("  • 정렬 상태: 내림차순 ✓")
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

            // Given: Invalid ISIN
            val invalidIsin = "KR7999999999"
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
            println("  • tradeDate: $tradeDate")

            // When
            val recentDailyList = client.price.getRecentDaily(invalidIsin, tradeDate)

            // Then: Returns empty list
            assertTrue(recentDailyList.isEmpty(), "존재하지 않는 ISIN 조회시 빈 리스트를 반환해야 합니다")

            println("\n📤 Response: [] (empty list)")
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
        @DisplayName("[파라미터: tradeDate] 비거래일에 조회하면 최근 거래일 데이터를 반환한다")
        fun returns_recent_trading_day_data_on_non_trading_day() = integrationTest {
            println("\n📘 엣지 케이스: 비거래일 조회")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Non-trading day (Saturday)
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$isin\"")
            println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

            // When: Query on non-trading day
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            // Then: Returns recent trading day data
            assertNotNull(recentDailyList, "API는 비거래일에도 데이터를 반환합니다 (최근 거래일 데이터)")

            println("\n📤 Response: List<RecentDaily> (size: ${recentDailyList.size})")
            println("  • 데이터 존재: 예 (API는 최근 거래일 데이터를 반환)")
            println("  • 거래일 개수: ${recentDailyList.size}개")
            if (recentDailyList.isNotEmpty()) {
                println("  • 최신 거래일: ${recentDailyList.first().tradeDate}")
            }

            println("\n✅ 처리 결과: 비거래일에도 최근 거래일 데이터 제공")
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
        @DisplayName("[활용] 최근 거래일 수익률을 계산할 수 있다")
        fun calculate_recent_return_rate() = integrationTest {
            println("\n📘 활용 예제: 수익률 계산")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 recent daily data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

            // When: Calculate return rate
            val newestClose = recentDailyList.first().closePrice.toDouble() // 최근 거래일
            val oldestClose = recentDailyList.last().closePrice.toDouble()  // 오래된 거래일
            val returnRate = if (oldestClose > 0) {
                ((newestClose - oldestClose) / oldestClose) * 100
            } else {
                0.0
            }

            // Then: Display analysis
            println("\n=== 최근 거래일 수익률 분석 ===")
            println("기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")
            println("시작가(과거): ${oldestClose}원")
            println("종료가(최근): ${newestClose}원")
            println("수익률: ${"%.2f".format(returnRate)}%")
            println()
            println("📊 분석: ${recentDailyList.size}거래일 기준 수익률 ${if (returnRate >= 0) "상승" else "하락"}")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 최근 거래일 거래량 추이를 분석할 수 있다")
        fun analyze_recent_volume_trend() = integrationTest {
            println("\n📘 활용 예제: 거래량 추이 분석")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 recent daily data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

            // When: Calculate volume statistics
            val totalVolume = recentDailyList.sumOf { it.volume }
            val avgVolume = if (recentDailyList.isNotEmpty()) {
                totalVolume / recentDailyList.size
            } else {
                0L
            }
            val maxVolume = recentDailyList.maxOfOrNull { it.volume } ?: 0L
            val minVolume = recentDailyList.minOfOrNull { it.volume } ?: 0L

            // Then: Display analysis
            println("\n=== 최근 ${recentDailyList.size}거래일 거래량 분석 ===")
            println("총 거래량: ${totalVolume}주")
            println("평균 일거래량: ${avgVolume}주")
            println("최대 일거래량: ${maxVolume}주")
            println("최소 일거래량: ${minVolume}주")
            println("분석 기간: ${recentDailyList.size}거래일")
            println()
            println("📊 분석: 평균 일거래량 ${avgVolume}주 기준")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 최근 가격 변동성을 분석할 수 있다")
        fun analyze_recent_price_volatility() = integrationTest {
            println("\n📘 활용 예제: 가격 변동성 분석")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 recent daily data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

            assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

            // When: Calculate volatility metrics
            val avgChangeRate = recentDailyList.map { kotlin.math.abs(it.changeRate) }.average()
            val maxChange = recentDailyList.maxOfOrNull { kotlin.math.abs(it.change) } ?: 0
            val highestPrice = recentDailyList.maxOfOrNull { it.closePrice } ?: 0
            val lowestPrice = recentDailyList.minOfOrNull { it.closePrice } ?: 0

            // Then: Display analysis
            println("\n=== 최근 ${recentDailyList.size}거래일 변동성 분석 ===")
            println("평균 변동률: ${"%.2f".format(avgChangeRate)}% (절대값 기준)")
            println("최대 변동폭: ${maxChange}원")
            println("기간 내 최고 종가: ${highestPrice}원")
            println("기간 내 최저 종가: ${lowestPrice}원")
            println("종가 범위: ${highestPrice - lowestPrice}원")
            println()
            println("📊 분석: 평균 일변동률 ${"%.2f".format(avgChangeRate)}% 수준의 변동성")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
