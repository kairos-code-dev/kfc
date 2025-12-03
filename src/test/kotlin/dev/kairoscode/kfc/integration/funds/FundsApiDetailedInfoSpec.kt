package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.ResponseRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FundsApi.getDetailedInfo() API 스펙
 *
 * ETF의 상세 정보를 조회하는 API입니다.
 * OHLCV, NAV, 괴리율, 52주 고가/저가, 총 보수 등의 정보를 제공합니다.
 */
@DisplayName("FundsApi.getDetailedInfo() - 펀드 상세 정보 조회")
class FundsApiDetailedInfoSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("기본 동작")
    inner class BasicBehavior {

        @Test
        @DisplayName("거래일에 TIGER 200 상세 정보를 조회할 수 있다")
        fun `returns detailed info for TIGER 200 on trading day`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "거래일에는 TIGER 200의 상세 정보가 반환되어야 합니다")

            println("✅ TIGER 200 상세 정보 (거래일: $tradeDate)")
            println("  - 종가: ${info?.closePrice}원")
            println("  - NAV: ${info?.nav}원")
            println("  - 괴리율: ${info?.calculateDivergenceRate()}%")
            println("  - 52주 고가: ${info?.week52High}원")
            println("  - 52주 저가: ${info?.week52Low}원")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.DETAILED_INFO,
                fileName = "tiger200_detailedInfo"
            )
        }

        @Test
        @DisplayName("거래일에 KODEX 200 상세 정보를 조회할 수 있다")
        fun `returns detailed info for KODEX 200 on trading day`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "거래일에는 KODEX 200의 상세 정보가 반환되어야 합니다")

            println("✅ KODEX 200 상세 정보 (거래일: $tradeDate)")
            println("  - 종가: ${info?.closePrice}원")
            println("  - NAV: ${info?.nav}원")
            println("  - 괴리율: ${info?.calculateDivergenceRate()}%")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.DETAILED_INFO,
                fileName = "kodex200_detailedInfo"
            )
        }

        @Test
        @DisplayName("비거래일에 조회하면 최근 거래일 데이터를 반환한다")
        fun `returns latest data on non-trading day`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.NON_TRADING_DAY // 2024-11-23 (토요일)

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "API는 비거래일에도 데이터를 반환합니다 (최근 거래일 데이터)")

            println("✅ 비거래일($tradeDate) 조회 결과:")
            println("  - 데이터 존재: 예 (API는 최근 거래일 데이터를 반환)")
            println("  - 종가: ${info?.closePrice}원")
        }
    }

    @Nested
    @DisplayName("응답 데이터 스펙")
    inner class ResponseSpec {

        @Test
        @DisplayName("종가와 NAV 정보를 포함한다")
        fun `contains close price and NAV`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            // Then
            assertTrue(info?.closePrice?.compareTo(java.math.BigDecimal.ZERO) == 1, "종가는 0보다 커야 합니다")
            assertTrue(info?.nav?.compareTo(java.math.BigDecimal.ZERO) == 1, "NAV는 0보다 커야 합니다")
        }
    }

    @Nested
    @DisplayName("활용 예제")
    inner class UsageExamples {

        @Test
        @DisplayName("NAV 대비 괴리율을 계산할 수 있다")
        fun `calculate divergence rate from NAV`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            assertNotNull(info, "거래일에는 상세 정보가 반환되어야 합니다")

            // When
            val calculatedDivergence = info?.calculateDivergenceRate()

            // Then
            println("\n=== NAV 대비 괴리율 분석 (거래일: $tradeDate) ===")
            println("종가: ${info?.closePrice}원")
            println("NAV: ${info?.nav}원")
            println("괴리율(계산): ${calculatedDivergence}%")
        }

        @Test
        @DisplayName("52주 고가/저가 대비 현재가 위치를 확인할 수 있다")
        fun `analyze price position within 52-week range`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val info = client.funds.getDetailedInfo(isin, tradeDate)

            assertNotNull(info, "거래일에는 상세 정보가 반환되어야 합니다")

            // When
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

            // Then
            println("\n=== 52주 고가/저가 대비 현재가 위치 (거래일: $tradeDate) ===")
            println("52주 고가: ${info?.week52High}원")
            println("현재가: ${info?.closePrice}원")
            println("52주 저가: ${info?.week52Low}원")
            println("위치: ${"%.1f".format(position)}% (0%=저가, 100%=고가)")
            println("52주 고가 근처?: ${info?.isNear52WeekHigh()}")
            println("52주 저가 근처?: ${info?.isNear52WeekLow()}")
        }
    }
}
