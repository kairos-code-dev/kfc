package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EtfApi - getPortfolio()")
class EtfPortfolioApiTest : UnitTestBase() {

    @Test
    @DisplayName("ETF 포트폴리오 구성을 조회할 수 있다")
    fun `getPortfolio_validIsin_returnsConstituents`() = unitTest {
        // Given: TIGER 200 ETF의 포트폴리오 응답 준비
        val jsonResponse = loadEtfPortfolioResponse("tiger200_portfolio")
        fakeFundsApi = FakeFundsApi(portfolioResponse = jsonResponse)
        initClient()

        // When: 포트폴리오를 조회하면
        val portfolio = client.funds.getPortfolio("KR7069500007")

        // Then: 구성 종목 목록이 반환된다
        assertThat(portfolio)
            .describedAs("포트폴리오가 비어있습니다")
            .isNotEmpty

        println("포트폴리오 구성: ${portfolio.size}개 종목")
        portfolio.take(5).forEach {
            println("  ${it.constituentName}: ${it.weightPercent}%")
        }
    }
}
