package dev.kairoscode.kfc.api.funds

import dev.kairoscode.kfc.mock.MockFundsApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EtfApi - getPortfolio()")
class EtfPortfolioApiTest : UnitTestBase() {

    @Test
    @DisplayName("ETF 포트폴리오 구성을 조회할 수 있다")
    fun `can retrieve ETF portfolio constituents`() = unitTest {
        // Given
        val jsonResponse = loadEtfPortfolioResponse("tiger200_portfolio")
        mockFundsApi = MockFundsApi(portfolioResponse = jsonResponse)
        initClient()

        // When
        val portfolio = client.funds.getPortfolio("KR7069500007")

        // Then
        assertThat(portfolio).isNotEmpty
        println("포트폴리오 구성: ${portfolio.size}개 종목")
        portfolio.take(5).forEach {
            println("  ${it.constituentName}: ${it.weightPercent}%")
        }
    }
}
