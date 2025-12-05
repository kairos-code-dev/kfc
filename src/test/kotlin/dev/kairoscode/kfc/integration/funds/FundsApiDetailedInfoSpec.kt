package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.ResponseRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * FundsApi.getDetailedInfo() Integration Test Specification
 *
 * ## API 개요
 * ETF의 상세 정보를 조회하는 API입니다.
 * OHLCV(시가/고가/저가/종가/거래량), NAV, 괴리율, 52주 고가/저가, 총 보수 등의 정보를 제공합니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * suspend fun getDetailedInfo(isin: String, tradeDate: LocalDate): DetailedInfo?
 * ```
 *
 * ## 파라미터
 * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
 * - `tradeDate`: LocalDate - 조회 기준일 (거래일/비거래일)
 *
 * ## 응답 데이터 (DetailedInfo)
 * - `closePrice`: BigDecimal - 종가
 * - `nav`: BigDecimal - 순자산가치 (NAV)
 * - `openPrice`: BigDecimal - 시가
 * - `highPrice`: BigDecimal - 고가
 * - `lowPrice`: BigDecimal - 저가
 * - `volume`: Long - 거래량
 * - `week52High`: BigDecimal - 52주 최고가
 * - `week52Low`: BigDecimal - 52주 최저가
 * - `totalExpenseRatio`: BigDecimal - 총 보수
 *
 * ## 특이사항
 * - 비거래일 조회시 최근 거래일 데이터 반환
 * - API Key 불필요 (KRX 공개 데이터)
 * - 괴리율 계산 기능: calculateDivergenceRate() 메서드 제공
 */
@DisplayName("FundsApi.getDetailedInfo() - ETF 상세 정보 조회")
class FundsApiDetailedInfoSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

    @Nested
    @DisplayName("1. 기본 동작 (Basic Operations)")
    inner class BasicOperations {

        @Test
        @DisplayName("TIGER 200의 상세 정보를 거래일에 조회할 수 있다")
        fun get_tiger200_detailed_info_on_trading_day() = integrationTest {
            println("\n📘 API: getDetailedInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 ISIN and trading day
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Request detailed info
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then: Returns detailed info
            assertNotNull(info, "거래일에는 TIGER 200의 상세 정보가 반환되어야 합니다")

            println("\n📤 Response: DetailedInfo")
            println("  • closePrice: ${info?.closePrice}원")
            println("  • nav: ${info?.nav}원")
            println("  • divergenceRate: ${info?.calculateDivergenceRate()}%")
            println("  • openPrice: ${info?.openPrice}원")
            println("  • highPrice: ${info?.highPrice}원")
            println("  • lowPrice: ${info?.lowPrice}원")
            println("  • volume: ${info?.volume}주")
            println("  • week52High: ${info?.week52High}원")
            println("  • week52Low: ${info?.week52Low}원")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.DETAILED_INFO,
                fileName = "tiger200_detailedInfo"
            )
        }

        @Test
        @DisplayName("KODEX 200의 상세 정보를 거래일에 조회할 수 있다")
        fun get_kodex200_detailed_info_on_trading_day() = integrationTest {
            println("\n📘 API: getDetailedInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: KODEX 200 ISIN and trading day
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Request detailed info
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then: Returns detailed info
            assertNotNull(info, "거래일에는 KODEX 200의 상세 정보가 반환되어야 합니다")

            println("\n📤 Response: DetailedInfo")
            println("  • closePrice: ${info?.closePrice}원")
            println("  • nav: ${info?.nav}원")
            println("  • divergenceRate: ${info?.calculateDivergenceRate()}%")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.DETAILED_INFO,
                fileName = "kodex200_detailedInfo"
            )
        }

        @Test
        @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 ETF를 조회할 수 있다")
        fun get_different_etfs_by_different_isin() = integrationTest {
            println("\n📘 파라미터 테스트: isin")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Two different ISINs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Request with different ISINs
            val tiger200Info = client.funds.getDetailedInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getDetailedInfo(kodex200Isin, tradeDate)

            // Then: Returns different ETF information
            assertNotNull(tiger200Info)
            assertNotNull(kodex200Info)
            assertNotEquals(tiger200Info?.closePrice, kodex200Info?.closePrice, "서로 다른 ISIN은 서로 다른 가격을 가져야 합니다")

            println("  Case 1: isin = \"$tiger200Isin\"")
            println("    → closePrice: ${tiger200Info?.closePrice}원")
            println()
            println("  Case 2: isin = \"$kodex200Isin\"")
            println("    → closePrice: ${kodex200Info?.closePrice}원")
            println()
            println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 ETF 조회 성공")
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
        @DisplayName("응답은 필수 필드(closePrice, nav)를 포함한다")
        fun response_contains_required_fields() = integrationTest {
            println("\n📘 응답 데이터 검증: 필수 필드")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then: Validate required fields
            assertNotNull(info, "응답 데이터가 있어야 합니다")
            assertTrue(info?.closePrice?.compareTo(java.math.BigDecimal.ZERO) == 1, "종가는 0보다 커야 합니다")
            assertTrue(info?.nav?.compareTo(java.math.BigDecimal.ZERO) == 1, "NAV는 0보다 커야 합니다")

            println("✅ 필수 필드 검증 통과:")
            println("  • closePrice: ${info?.closePrice}원 (> 0) ✓")
            println("  • nav: ${info?.nav}원 (> 0) ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("OHLCV 데이터가 유효한 범위 내에 있다")
        fun ohlcv_data_is_within_valid_range() = integrationTest {
            println("\n📘 응답 데이터 검증: OHLCV 범위")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then: Validate OHLC relationships
            assertNotNull(info)
            assertTrue(info?.highPrice!! >= info.lowPrice, "고가는 저가보다 크거나 같아야 합니다")
            assertTrue(info.highPrice >= info.openPrice, "고가는 시가보다 크거나 같아야 합니다")
            assertTrue(info.highPrice >= info.closePrice, "고가는 종가보다 크거나 같아야 합니다")
            assertTrue(info.lowPrice <= info.openPrice, "저가는 시가보다 작거나 같아야 합니다")
            assertTrue(info.lowPrice <= info.closePrice, "저가는 종가보다 작거나 같아야 합니다")

            println("✅ OHLCV 범위 검증:")
            println("  • High: ${info.highPrice}원")
            println("  • Open: ${info.openPrice}원")
            println("  • Close: ${info.closePrice}원")
            println("  • Low: ${info.lowPrice}원")
            println("  • Volume: ${info.volume}주")
            println("  • 관계: Low ≤ Open, Close ≤ High ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("52주 고가/저가는 현재가를 포함하는 범위다")
        fun week52_range_includes_current_price() = integrationTest {
            println("\n📘 응답 데이터 검증: 52주 고가/저가 범위")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then: Current price should be within 52-week range
            assertNotNull(info)
            assertTrue(info?.closePrice!! <= info.week52High, "현재가는 52주 최고가 이하여야 합니다")
            assertTrue(info.closePrice >= info.week52Low, "현재가는 52주 최저가 이상이어야 합니다")

            println("✅ 52주 범위 검증:")
            println("  • 52주 고가: ${info.week52High}원")
            println("  • 현재 종가: ${info.closePrice}원")
            println("  • 52주 저가: ${info.week52Low}원")
            println("  • 범위: 52주 저가 ≤ 현재가 ≤ 52주 고가 ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("거래량은 0 이상이다")
        fun volume_is_non_negative() = integrationTest {
            println("\n📘 응답 데이터 검증: 거래량")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then
            assertNotNull(info)
            assertTrue(info?.volume!! >= 0, "거래량은 0 이상이어야 합니다")

            println("✅ 거래량 검증:")
            println("  • volume: ${info.volume}주 (>= 0) ✓")
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
        @DisplayName("존재하지 않는 ISIN 조회시 빈 데이터 객체를 반환한다")
        fun returns_empty_object_for_non_existent_isin() = integrationTest {
            println("\n📘 입력 검증: 존재하지 않는 ISIN")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Invalid ISIN that doesn't exist
            val invalidIsin = "KR7999999999"
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
            println("  • tradeDate: $tradeDate")

            // When
            val info = client.funds.getDetailedInfo(invalidIsin, tradeDate)

            // Then: Returns empty object (all fields are 0 or empty string)
            assertNotNull(info, "존재하지 않는 ISIN은 빈 객체를 반환합니다")
            assertEquals("", info?.isin ?: "", "ISIN이 빈 문자열이어야 합니다")
            assertEquals("", info?.name ?: "", "종목명이 빈 문자열이어야 합니다")

            println("\n📤 Response: DetailedInfo (빈 객체)")
            println("  • isin: \"${info?.isin}\" (빈 문자열)")
            println("  • name: \"${info?.name}\" (빈 문자열)")
            println("  • closePrice: ${info?.closePrice} (0)")
            println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 객체 반환")
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
        fun returns_latest_data_on_non_trading_day() = integrationTest {
            println("\n📘 엣지 케이스: 비거래일 조회")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Non-trading day (Saturday)
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$isin\"")
            println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then: Returns latest trading day data
            assertNotNull(info, "비거래일에도 최근 거래일 데이터를 반환해야 합니다")

            println("\n📤 Response: DetailedInfo (최근 거래일 데이터)")
            println("  • closePrice: ${info?.closePrice}원")
            println("  • nav: ${info?.nav}원")
            println()
            println("  ℹ️ 참고: API는 최근 거래일 데이터를 반환합니다")

            println("\n✅ 처리 결과: 비거래일에도 데이터 제공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[파라미터: tradeDate] 거래일과 비거래일 데이터 비교")
        fun compare_trading_day_vs_non_trading_day() = integrationTest {
            println("\n📘 파라미터 비교: 거래일 vs 비거래일")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val isin = TestFixtures.Etf.TIGER_200_ISIN

            // When: Query on both trading and non-trading days
            println("  Case 1: 거래일 (${TestFixtures.Dates.TRADING_DAY}, 월요일)")
            val tradingDayResult = client.funds.getDetailedInfo(isin, TestFixtures.Dates.TRADING_DAY)
            println("    → closePrice: ${tradingDayResult?.closePrice}원")
            println("    → nav: ${tradingDayResult?.nav}원")

            println("\n  Case 2: 비거래일 (${TestFixtures.Dates.NON_TRADING_DAY}, 토요일)")
            val nonTradingDayResult = client.funds.getDetailedInfo(isin, TestFixtures.Dates.NON_TRADING_DAY)
            println("    → closePrice: ${nonTradingDayResult?.closePrice}원")
            println("    → nav: ${nonTradingDayResult?.nav}원")

            // Then: Both should return data
            assertNotNull(tradingDayResult)
            assertNotNull(nonTradingDayResult)

            println("\n  ✅ 분석: 비거래일 조회시 최근 거래일 데이터 제공")
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
        @DisplayName("[활용] NAV 대비 괴리율을 계산할 수 있다")
        fun calculate_divergence_rate_from_nav() = integrationTest {
            println("\n📘 활용 예제: NAV 대비 괴리율 계산")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            assertNotNull(info, "거래일에는 상세 정보가 반환되어야 합니다")

            // When: Calculate divergence rate
            val calculatedDivergence = info?.calculateDivergenceRate()

            // Then: Display analysis
            println("\n=== NAV 대비 괴리율 분석 (거래일: $tradeDate) ===")
            println("종가: ${info?.closePrice}원")
            println("NAV: ${info?.nav}원")
            println("괴리율(계산): ${calculatedDivergence}%")
            println()
            println("📊 분석: 괴리율은 ETF 가격이 NAV 대비 ${if (calculatedDivergence!! > 0.toBigDecimal()) "할증" else "할인"} 상태임을 나타냅니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 52주 고가/저가 대비 현재가 위치를 확인할 수 있다")
        fun analyze_price_position_within_52_week_range() = integrationTest {
            println("\n📘 활용 예제: 52주 고가/저가 대비 현재가 위치")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            assertNotNull(info, "거래일에는 상세 정보가 반환되어야 합니다")

            // When: Calculate position within 52-week range
            val position = info?.let {
                val highLowRange = it.week52High.subtract(it.week52Low)
                if (highLowRange.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    it.closePrice.subtract(it.week52Low)
                        .divide(highLowRange, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(java.math.BigDecimal("100"))
                        .toDouble()
                } else {
                    50.0
                }
            } ?: 50.0

            // Then: Display analysis
            println("\n=== 52주 고가/저가 대비 현재가 위치 (거래일: $tradeDate) ===")
            println("52주 고가: ${info?.week52High}원")
            println("현재가: ${info?.closePrice}원")
            println("52주 저가: ${info?.week52Low}원")
            println("위치: ${"%.1f".format(position)}% (0%=저가, 100%=고가)")
            println()
            println("52주 고가 근처?: ${info?.isNear52WeekHigh()}")
            println("52주 저가 근처?: ${info?.isNear52WeekLow()}")
            println()
            println("📊 분석: 현재가는 52주 범위에서 ${"%.1f".format(position)}% 위치에 있습니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 여러 ETF의 괴리율을 비교할 수 있다")
        fun compare_divergence_rates_across_etfs() = integrationTest {
            println("\n📘 활용 예제: ETF 괴리율 비교")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Multiple ETFs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Fetch detailed info for both
            val tiger200Info = client.funds.getDetailedInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getDetailedInfo(kodex200Isin, tradeDate)

            assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
            assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

            // Then: Compare divergence rates
            val tiger200Divergence = tiger200Info?.calculateDivergenceRate()
            val kodex200Divergence = kodex200Info?.calculateDivergenceRate()

            println("\n=== KOSPI 200 추종 ETF 괴리율 비교 (거래일: $tradeDate) ===")
            println()
            println("TIGER 200")
            println("  • 종가: ${tiger200Info?.closePrice}원")
            println("  • NAV: ${tiger200Info?.nav}원")
            println("  • 괴리율: ${tiger200Divergence}%")
            println()
            println("KODEX 200")
            println("  • 종가: ${kodex200Info?.closePrice}원")
            println("  • NAV: ${kodex200Info?.nav}원")
            println("  • 괴리율: ${kodex200Divergence}%")
            println()
            println("📊 분석: 동일 지수 추종 ETF 간 괴리율 차이를 확인할 수 있습니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
