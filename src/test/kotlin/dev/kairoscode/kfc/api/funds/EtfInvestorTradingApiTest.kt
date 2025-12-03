package dev.kairoscode.kfc.api.funds

import dev.kairoscode.kfc.mock.MockFundsApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EtfApi - Investor Trading")
class EtfInvestorTradingApiTest : UnitTestBase() {

    @Test
    @DisplayName("투자자별 거래 현황을 조회할 수 있다")
    fun `can retrieve investor trading`() = unitTest {
        // Given
        val jsonResponse = loadMockResponse("etf/investor_trading", "tiger200_investor_trading")
        mockFundsApi = MockFundsApi(investorTradingResponse = jsonResponse)
        initClient()

        // When
        val trading = client.funds.getInvestorTrading("KR7069500007")

        // Then
        assertThat(trading).isNotEmpty
        println("투자자별 거래: ${trading.size}개 유형")
        trading.forEach {
            println("  ${it.investorType}: 순매수 ${it.netBuyVolume}주")
        }
    }
}
