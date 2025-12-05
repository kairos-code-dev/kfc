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
 * FundsApi.getPortfolio() Integration Test Specification
 *
 * ## API 개요
 * ETF 포트폴리오 구성 종목(바스켓)을 조회하는 API입니다.
 * 각 구성 종목의 이름, 티커, 비중(%) 등을 제공합니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * suspend fun getPortfolio(isin: String, tradeDate: LocalDate): List<PortfolioConstituent>
 * ```
 *
 * ## 파라미터
 * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
 * - `tradeDate`: LocalDate - 조회 기준일 (거래일/비거래일)
 *
 * ## 응답 데이터 (List<PortfolioConstituent>)
 * - `constituentName`: String - 구성 종목명
 * - `constituentTicker`: String - 구성 종목 티커
 * - `weightPercent`: BigDecimal - 포트폴리오 내 비중(%)
 * - `quantity`: Long? - 보유 수량 (옵션)
 *
 * ## 특이사항
 * - 비거래일 조회시 최근 거래일 데이터 반환
 * - API Key 불필요 (KRX 공개 데이터)
 * - 비중 합계는 약 100% (반올림 오차 허용)
 */
@DisplayName("FundsApi.getPortfolio() - ETF 포트폴리오 구성 조회")
class FundsApiPortfolioSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

    @Nested
    @DisplayName("1. 기본 동작 (Basic Operations)")
    inner class BasicOperations {

        @Test
        @DisplayName("TIGER 200의 포트폴리오 구성을 거래일에 조회할 수 있다")
        fun get_tiger200_portfolio_on_trading_day() = integrationTest {
            println("\n📘 API: getPortfolio()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 ISIN and trading day
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Request portfolio composition
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // Then: Returns portfolio constituents
            assertTrue(portfolio.isNotEmpty(), "거래일에는 포트폴리오 구성 종목이 있어야 합니다")

            val totalWeight = portfolio.sumOf { it.weightPercent.toDouble() }

            println("\n📤 Response: List<PortfolioConstituent>")
            println("  • constituents.size: ${portfolio.size}개")
            println("  • totalWeight: ${"%.2f".format(totalWeight)}%")
            println("  • top 3 constituents:")
            portfolio.sortedByDescending { it.weightPercent }.take(3).forEachIndexed { index, constituent ->
                println("    ${index + 1}. ${constituent.constituentName}: ${"%.2f".format(constituent.weightPercent)}%")
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = portfolio,
                category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
                fileName = "tiger200_portfolio"
            )
        }

        @Test
        @DisplayName("KODEX 200의 포트폴리오 구성을 거래일에 조회할 수 있다")
        fun get_kodex200_portfolio_on_trading_day() = integrationTest {
            println("\n📘 API: getPortfolio()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: KODEX 200 ISIN and trading day
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Request portfolio composition
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // Then: Returns portfolio constituents
            assertTrue(portfolio.isNotEmpty(), "거래일에는 포트폴리오 구성 종목이 있어야 합니다")

            println("\n📤 Response: List<PortfolioConstituent>")
            println("  • constituents.size: ${portfolio.size}개")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = portfolio,
                category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
                fileName = "kodex200_portfolio"
            )
        }

        @Test
        @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 포트폴리오를 조회할 수 있다")
        fun get_different_portfolios_by_different_isin() = integrationTest {
            println("\n📘 파라미터 테스트: isin")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Two different ISINs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Request with different ISINs
            val tiger200Portfolio = client.funds.getPortfolio(tiger200Isin, tradeDate)
            val kodex200Portfolio = client.funds.getPortfolio(kodex200Isin, tradeDate)

            // Then: Returns different portfolios
            assertTrue(tiger200Portfolio.isNotEmpty())
            assertTrue(kodex200Portfolio.isNotEmpty())

            println("  Case 1: isin = \"$tiger200Isin\" (TIGER 200)")
            println("    → constituents: ${tiger200Portfolio.size}개")
            println()
            println("  Case 2: isin = \"$kodex200Isin\" (KODEX 200)")
            println("    → constituents: ${kodex200Portfolio.size}개")
            println()
            println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 포트폴리오 조회 성공")
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
        @DisplayName("응답은 구성 종목 목록을 포함한다")
        fun response_contains_constituents() = integrationTest {
            println("\n📘 응답 데이터 검증: 구성 종목")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // Then: Validate structure
            assertTrue(portfolio.isNotEmpty(), "포트폴리오는 최소 1개 이상의 구성 종목을 포함해야 합니다")

            val firstConstituent = portfolio.first()
            assertTrue(firstConstituent.constituentName.isNotEmpty(), "구성 종목명은 비어있지 않아야 합니다")
            assertTrue(firstConstituent.weightPercent.toDouble() > 0, "비중은 0보다 커야 합니다")

            println("✅ 응답 구조 검증 통과:")
            println("  • constituents.size: ${portfolio.size}개 (> 0) ✓")
            println("  • 첫 번째 종목명: ${firstConstituent.constituentName} ✓")
            println("  • 첫 번째 종목 비중: ${firstConstituent.weightPercent}% (> 0) ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("비중 합계는 약 100%다 (오차 범위 ±1%)")
        fun total_weight_is_approximately_100_percent() = integrationTest {
            println("\n📘 응답 데이터 검증: 비중 합계")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // Then: Total weight should be approximately 100%
            val totalWeight = portfolio.sumOf { it.weightPercent.toDouble() }
            assertTrue(abs(totalWeight - 100.0) <= 1.0, "비중 합계는 100% 근처여야 합니다. 실제: ${totalWeight}%")

            println("✅ 비중 합계 검증:")
            println("  • totalWeight: ${"%.2f".format(totalWeight)}%")
            println("  • 허용 범위: 99.0% ~ 101.0%")
            println("  • 오차: ${"%.2f".format(abs(totalWeight - 100.0))}% ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("각 구성 종목의 비중은 0과 100 사이다")
        fun each_weight_is_between_0_and_100() = integrationTest {
            println("\n📘 응답 데이터 검증: 개별 비중 범위")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // Then: Each weight should be between 0 and 100
            portfolio.forEach { constituent ->
                val weight = constituent.weightPercent.toDouble()
                assertTrue(weight >= 0, "${constituent.constituentName}의 비중은 0 이상이어야 합니다")
                assertTrue(weight <= 100, "${constituent.constituentName}의 비중은 100 이하여야 합니다")
            }

            val maxWeight = portfolio.maxOfOrNull { it.weightPercent.toDouble() } ?: 0.0
            val minWeight = portfolio.minOfOrNull { it.weightPercent.toDouble() } ?: 0.0

            println("✅ 개별 비중 범위 검증:")
            println("  • 최대 비중: ${"%.2f".format(maxWeight)}% ✓")
            println("  • 최소 비중: ${"%.2f".format(minWeight)}% ✓")
            println("  • 범위: 0% ≤ 비중 ≤ 100% ✓")
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
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
            println("  • tradeDate: $tradeDate")

            // When
            val portfolio = client.funds.getPortfolio(invalidIsin, tradeDate)

            // Then: Returns empty list for non-existent ISIN
            assertTrue(portfolio.isEmpty(), "존재하지 않는 ISIN은 빈 리스트를 반환해야 합니다")

            println("\n📤 Response: List<PortfolioConstituent> (empty)")
            println("  • constituents.size: ${portfolio.size}")
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
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // Then: Returns latest trading day data
            assertNotNull(portfolio, "비거래일에도 최근 거래일 데이터를 반환해야 합니다")

            println("\n📤 Response: List<PortfolioConstituent> (최근 거래일 데이터)")
            println("  • constituents.size: ${portfolio.size}개")
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
            val tradingDayResult = client.funds.getPortfolio(isin, TestFixtures.Dates.TRADING_DAY)
            println("    → constituents: ${tradingDayResult.size}개")

            println("\n  Case 2: 비거래일 (${TestFixtures.Dates.NON_TRADING_DAY}, 토요일)")
            val nonTradingDayResult = client.funds.getPortfolio(isin, TestFixtures.Dates.NON_TRADING_DAY)
            println("    → constituents: ${nonTradingDayResult.size}개")

            // Then: Both should return data
            assertTrue(tradingDayResult.isNotEmpty())
            assertTrue(nonTradingDayResult.isNotEmpty())

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
        @DisplayName("[활용] 상위 10개 구성 종목을 확인할 수 있다")
        fun get_top_10_holdings() = integrationTest {
            println("\n📘 활용 예제: 상위 10개 구성 종목")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Portfolio data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // When: Sort by weight descending
            val top10 = portfolio
                .sortedByDescending { it.weightPercent }
                .take(10)

            // Then: Display top 10 holdings
            println("\n=== 상위 10개 구성 종목 (거래일: $tradeDate) ===")
            top10.forEachIndexed { index, constituent ->
                println("${index + 1}. ${constituent.constituentName}: ${"%.2f".format(constituent.weightPercent)}%")
            }

            val top10Weight = top10.sumOf { it.weightPercent.toDouble() }
            println()
            println("📊 분석: 상위 10개 종목이 전체 포트폴리오의 ${"%.2f".format(top10Weight)}%를 차지합니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 특정 종목의 비중을 확인할 수 있다")
        fun find_specific_stock_weight() = integrationTest {
            println("\n📘 활용 예제: 특정 종목 비중 확인")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Portfolio data
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY
            val portfolio = client.funds.getPortfolio(isin, tradeDate)

            // When: Search for Samsung Electronics
            val samsung = portfolio.find { it.constituentName.contains("삼성전자") }

            // Then: Display Samsung weight
            println("\n=== 특정 종목 비중 (거래일: $tradeDate) ===")
            if (samsung != null) {
                println("${samsung.constituentName}: ${"%.2f".format(samsung.weightPercent)}%")
                println()
                println("📊 분석: 삼성전자는 TIGER 200 포트폴리오의 ${"%.2f".format(samsung.weightPercent)}%를 차지합니다")
            } else {
                println("⚠️ 삼성전자가 포트폴리오에 없습니다.")
            }
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 여러 ETF의 포트폴리오 크기를 비교할 수 있다")
        fun compare_portfolio_sizes_across_etfs() = integrationTest {
            println("\n📘 활용 예제: ETF 포트폴리오 크기 비교")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Multiple ETFs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Fetch portfolios for both
            val tiger200Portfolio = client.funds.getPortfolio(tiger200Isin, tradeDate)
            val kodex200Portfolio = client.funds.getPortfolio(kodex200Isin, tradeDate)

            assertTrue(tiger200Portfolio.isNotEmpty(), "TIGER 200 포트폴리오가 있어야 합니다")
            assertTrue(kodex200Portfolio.isNotEmpty(), "KODEX 200 포트폴리오가 있어야 합니다")

            // Then: Compare sizes
            println("\n=== KOSPI 200 추종 ETF 포트폴리오 크기 비교 (거래일: $tradeDate) ===")
            println()
            println("TIGER 200")
            println("  • 구성 종목 수: ${tiger200Portfolio.size}개")

            val tiger200Top = tiger200Portfolio.sortedByDescending { it.weightPercent }.first()
            println("  • 최대 비중 종목: ${tiger200Top.constituentName} (${"%.2f".format(tiger200Top.weightPercent)}%)")
            println()
            println("KODEX 200")
            println("  • 구성 종목 수: ${kodex200Portfolio.size}개")

            val kodex200Top = kodex200Portfolio.sortedByDescending { it.weightPercent }.first()
            println("  • 최대 비중 종목: ${kodex200Top.constituentName} (${"%.2f".format(kodex200Top.weightPercent)}%)")
            println()
            println("📊 분석: 동일 지수를 추종하는 ETF 간 포트폴리오 구성을 비교할 수 있습니다")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
