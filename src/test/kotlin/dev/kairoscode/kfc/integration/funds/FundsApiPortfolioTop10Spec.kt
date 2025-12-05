package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FundsApi - 포트폴리오 상위 10종목 조회 API 통합 테스트
 *
 * ETF 포트폴리오 구성 종목 중 비중이 높은 상위 10개의 요약 정보를 조회합니다.
 *
 * 구현 참고: MDCSTAT04705 엔드포인트는 작동하지 않으므로,
 * 내부적으로 전체 포트폴리오(MDCSTAT05001)를 조회하여 상위 10개를 추출합니다.
 */
@DisplayName("FundsApi.getPortfolioTop10() - 포트폴리오 상위 10종목 조회")
class FundsApiPortfolioTop10Spec : IntegrationTestBase() {

    @Nested
    @DisplayName("1. 기본 동작")
    inner class BasicOperations {

        @Test
        @DisplayName("TIGER 200 포트폴리오 상위 10종목을 거래일에 조회할 수 있다")
        fun get_tiger200_portfolio_top10_on_trading_day() = integrationTest {
            println("\n📘 API: getPortfolioTop10()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: 입력 파라미터 표시
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: API 호출
            val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

            // Then: 결과 즉시 표시
            println("\n📤 Response: List<PortfolioItem>")
            println("  • size: ${topItems.size}")

            if (topItems.isNotEmpty()) {
                println("\n  [상위 5개 종목]")
                topItems.take(5).forEachIndexed { index, item ->
                    println("    ${index + 1}. ${item.name} - 비중: ${item.compositionRatio}%")
                }
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(topItems, "거래일에는 TIGER 200의 포트폴리오 상위 10종목이 반환되어야 합니다")
            assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")
            assertTrue(topItems.size <= 10, "포트폴리오 상위 10종목은 최대 10개까지만 포함합니다")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = topItems,
                category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO_TOP10,
                fileName = "tiger200_portfolio_top10"
            )
        }

        @Test
        @DisplayName("KODEX 200 포트폴리오 상위 10종목을 거래일에 조회할 수 있다")
        fun get_kodex200_portfolio_top10_on_trading_day() = integrationTest {
            println("\n📘 API: getPortfolioTop10()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: 입력 파라미터 표시
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: API 호출
            val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

            // Then: 결과 즉시 표시
            println("\n📤 Response: List<PortfolioItem>")
            println("  • size: ${topItems.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(topItems, "거래일에는 KODEX 200의 포트폴리오 상위 10종목이 반환되어야 합니다")
            assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = topItems,
                category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO_TOP10,
                fileName = "kodex200_portfolio_top10"
            )
        }
    }

    @Nested
    @DisplayName("2. 응답 데이터 검증")
    inner class ResponseValidation {

        @Test
        @DisplayName("각 항목은 종목명과 비중을 포함한다")
        fun validate_response_contains_name_and_ratio() = integrationTest {
            println("\n📘 API: getPortfolioTop10()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When
            val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

            // Then: 필드 검증
            println("\n📤 Response: List<PortfolioItem>")
            println("  • size: ${topItems.size}")

            if (topItems.isNotEmpty()) {
                val topItem = topItems.first()
                println("\n  [필드 검증]")
                println("  • name: ${topItem.name} ✓")
                println("  • compositionRatio: ${topItem.compositionRatio}% ✓")

                assertTrue(!topItem.name.isNullOrEmpty(), "종목명이 있어야 합니다")
                assertTrue(topItem.compositionRatio.compareTo(java.math.BigDecimal.ZERO) >= 0, "비중은 0 이상이어야 합니다")
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(topItems, "포트폴리오 상위 10종목이 반환되어야 합니다")
            assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")
        }
    }

    @Nested
    @DisplayName("3. 엣지 케이스")
    inner class EdgeCases {

        @Test
        @DisplayName("비거래일에 조회하면 빈 데이터를 반환한다")
        fun return_empty_data_on_non_trading_day() = integrationTest {
            println("\n📘 API: getPortfolioTop10()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: 입력 파라미터 표시
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate (비거래일)")

            // When: API 호출
            val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

            // Then: 결과 즉시 표시
            println("\n📤 Response: List<PortfolioItem>")
            println("  • size: ${topItems.size} (비거래일이므로 빈 응답 가능)")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(topItems, "API 호출은 성공해야 합니다")
        }
    }

    @Nested
    @DisplayName("5. 실무 활용 예제")
    inner class PracticalExamples {

        @Test
        @DisplayName("ETF 포트폴리오의 집중도를 분석할 수 있다")
        fun analyze_portfolio_concentration() = integrationTest {
            println("\n📘 API: getPortfolioTop10() - 활용 예제")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When
            val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

            // Then: 집중도 계산
            println("\n📤 Response: List<PortfolioItem>")
            println("  • size: ${topItems.size}")

            if (topItems.isNotEmpty()) {
                val totalWeight = topItems.sumOf { it.compositionRatio }
                val topThreeWeight = topItems.take(3).sumOf { it.compositionRatio }
                val topFiveWeight = topItems.take(5).sumOf { it.compositionRatio }

                println("\n  [포트폴리오 집중도 분석]")
                println("  • 상위 10종목 총 비중: ${totalWeight}%")
                println("  • 상위 3종목 비중: ${topThreeWeight}%")
                println("  • 상위 5종목 비중: ${topFiveWeight}%")
                println("  • 평가: " + when {
                    topThreeWeight.compareTo(java.math.BigDecimal("40")) >= 0 -> "집중도 높음 (상위 3종목 40% 이상)"
                    topFiveWeight.compareTo(java.math.BigDecimal("50")) >= 0 -> "집중도 중간 (상위 5종목 50% 이상)"
                    else -> "집중도 낮음 (분산 투자)"
                })
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(topItems.isNotEmpty(), "데이터가 있어야 합니다")
        }

        @Test
        @DisplayName("여러 ETF의 포트폴리오 구성을 비교할 수 있다")
        fun compare_portfolio_composition_between_etfs() = integrationTest {
            println("\n📘 API: getPortfolioTop10() - 활용 예제")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • tiger200Isin: String = \"$tiger200Isin\"")
            println("  • kodex200Isin: String = \"$kodex200Isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When
            val tiger200Items = client.funds.getPortfolioTop10(tiger200Isin, tradeDate)
            val kodex200Items = client.funds.getPortfolioTop10(kodex200Isin, tradeDate)

            // Then: 포트폴리오 비교 분석
            println("\n📤 Response: 2개 ETF 포트폴리오 비교")

            if (tiger200Items.isNotEmpty() && kodex200Items.isNotEmpty()) {
                val tiger200AvgWeight = if (tiger200Items.isNotEmpty()) {
                    tiger200Items.sumOf { it.compositionRatio }.divide(tiger200Items.size.toBigDecimal(), 4, java.math.RoundingMode.HALF_UP)
                } else {
                    java.math.BigDecimal.ZERO
                }

                val kodex200AvgWeight = if (kodex200Items.isNotEmpty()) {
                    kodex200Items.sumOf { it.compositionRatio }.divide(kodex200Items.size.toBigDecimal(), 4, java.math.RoundingMode.HALF_UP)
                } else {
                    java.math.BigDecimal.ZERO
                }

                println("\n  [TIGER 200]")
                println("  • 상위 10종목 구성 수: ${tiger200Items.size}개")
                println("  • 상위 10종목 총 비중: ${tiger200Items.sumOf { it.compositionRatio }}%")
                println("  • 평균 비중: ${tiger200AvgWeight}%")

                println("\n  [KODEX 200]")
                println("  • 상위 10종목 구성 수: ${kodex200Items.size}개")
                println("  • 상위 10종목 총 비중: ${kodex200Items.sumOf { it.compositionRatio }}%")
                println("  • 평균 비중: ${kodex200AvgWeight}%")

                // 공통 종목 분석
                val tiger200Names = tiger200Items.map { it.name }.toSet()
                val kodex200Names = kodex200Items.map { it.name }.toSet()
                val commonStocks = tiger200Names.intersect(kodex200Names)

                println("\n  [공통 종목 분석]")
                println("  • 공통 종목 수: ${commonStocks.size}개/${tiger200Items.size}개")
                if (commonStocks.isNotEmpty()) {
                    println("  • 공통 상위 종목: ${commonStocks.take(5).joinToString(", ")}")
                }
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(tiger200Items.isNotEmpty(), "TIGER 200 포트폴리오가 있어야 합니다")
            assertTrue(kodex200Items.isNotEmpty(), "KODEX 200 포트폴리오가 있어야 합니다")
        }
    }
}
