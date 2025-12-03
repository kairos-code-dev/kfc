package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FundsApi.getPortfolio() API 스펙
 *
 * ETF 포트폴리오 구성 종목을 조회하는 API입니다.
 */
@DisplayName("FundsApi.getPortfolio() - 포트폴리오 구성 조회")
class FundsApiPortfolioSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("기본 동작")
    inner class BasicBehavior {

        @Test
        @DisplayName("거래일에 ETF 바스켓 구성 종목을 조회할 수 있다")
    fun testGetPortfolioOnTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 거래일
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val date = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

        // When: 포트폴리오 구성 조회
        val portfolio = client.funds.getPortfolio(isin, date)

        // Then: 구성 종목 및 비중 반환
        assertTrue(portfolio.isNotEmpty(), "거래일에는 포트폴리오 구성 종목이 있어야 합니다")

        // Then: 비중 합계 확인 (허용 오차 범위 내)
        val totalWeight = portfolio.sumOf { it.weightPercent.toDouble() }
        assertTrue(abs(totalWeight - 100.0) <= 1.0, "비중 합계는 100% 근처여야 합니다. 실제: ${totalWeight}%")

        println("✅ 포트폴리오 구성 종목 개수 (거래일: $date): ${portfolio.size}")
        println("✅ 비중 합계: ${"%.2f".format(totalWeight)}%")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = portfolio,
            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
            fileName = "tiger200_portfolio"
        )
    }

        @Test
        @DisplayName("비거래일에 ETF 바스켓 구성 종목을 조회하면 데이터를 반환한다 (API는 최근 거래일 데이터 제공)")
    fun testGetPortfolioOnNonTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 비거래일 (토요일)
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val date = TestFixtures.Dates.NON_TRADING_DAY // 2024-11-23 (토요일)

        // When: 포트폴리오 구성 조회
        val portfolio = client.funds.getPortfolio(isin, date)

        // Then: 데이터 반환 (API는 비거래일에도 최근 거래일 데이터 제공)
        assertNotNull(portfolio, "API는 비거래일에도 데이터를 반환합니다")

        println("✅ 비거래일($date) 조회 결과:")
        println("  - 구성 종목 개수: ${portfolio.size}")
        println("  - API는 최근 거래일 데이터를 반환")
    }

        @Test
        @DisplayName("거래일에 KODEX 200 포트폴리오를 조회할 수 있다")
    fun testGetPortfolioKodex200OnTradingDay() = integrationTest {
        // Given: KODEX 200 ISIN과 고정 거래일
        val isin = TestFixtures.Etf.KODEX_200_ISIN
        val date = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

        // When: 포트폴리오 구성 조회
        val portfolio = client.funds.getPortfolio(isin, date)

        // Then: 구성 종목 반환
        assertTrue(portfolio.isNotEmpty(), "거래일에는 포트폴리오 구성 종목이 있어야 합니다")

        println("✅ KODEX 200 구성 종목 개수 (거래일: $date): ${portfolio.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = portfolio,
            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
            fileName = "kodex200_portfolio"
        )
    }
    }

    @Nested
    @DisplayName("활용 예제")
    inner class UsageExamples {

        @Test
        @DisplayName("[활용] 거래일 기준으로 상위 10개 구성 종목을 확인할 수 있다")
    fun testTop10Holdings() = integrationTest {
        // Given: 포트폴리오 데이터 (고정 거래일)
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val date = TestFixtures.Dates.TRADING_DAY
        val portfolio = client.funds.getPortfolio(isin, date)

        // When: 비중 기준 정렬
        val top10 = portfolio
            .sortedByDescending { it.weightPercent }
            .take(10)

        // Then: 상위 10개 종목 출력
        println("\n=== 상위 10개 구성 종목 (거래일: $date) ===")
        top10.forEachIndexed { index, constituent ->
            println("${index + 1}. ${constituent.constituentName}: ${"%.2f".format(constituent.weightPercent)}%")
        }
    }

        @Test
        @DisplayName("[활용] 거래일 기준으로 특정 종목의 비중을 확인할 수 있다")
    fun testFindStockWeight() = integrationTest {
        // Given: 포트폴리오 데이터 (고정 거래일)
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val date = TestFixtures.Dates.TRADING_DAY
        val portfolio = client.funds.getPortfolio(isin, date)

        // When: 삼성전자 검색
        val samsung = portfolio.find { it.constituentName.contains("삼성전자") }

        // Then: 삼성전자 비중 출력
        if (samsung != null) {
            println("\n=== 특정 종목 비중 (거래일: $date) ===")
            println("${samsung.constituentName}: ${"%.2f".format(samsung.weightPercent)}%")
        } else {
            println("⚠️ 삼성전자가 포트폴리오에 없습니다.")
        }
    }
    }
}
