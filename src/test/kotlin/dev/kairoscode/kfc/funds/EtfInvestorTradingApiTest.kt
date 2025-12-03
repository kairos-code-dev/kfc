package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EtfApi - Investor Trading")
class EtfInvestorTradingApiTest : UnitTestBase() {

    @Test
    @DisplayName("투자자별 거래 현황을 조회할 수 있다")
    fun getInvestorTrading_withValidIsin_returnsData() = unitTest {
        // Given: TIGER 200 ETF의 투자자별 거래 조회를 위한 Mock API 설정
        val jsonResponse = loadMockResponse("etf/investor_trading", "tiger200_investor_trading")
        fakeFundsApi = FakeFundsApi(investorTradingResponse = jsonResponse)
        initClient()

        val isin = "KR7069500007"

        // When: 투자자별 거래 현황을 조회
        val trading = client.funds.getInvestorTrading(isin)

        // Then: 투자자 유형별 거래 데이터가 반환되어야 함
        assertThat(trading)
            .describedAs("투자자별 거래 결과가 비어있습니다 (ISIN: %s)", isin)
            .isNotEmpty
        println("투자자별 거래: ${trading.size}개 유형")
        trading.forEach {
            println("  ${it.investorType}: 순매수 ${it.netBuyVolume}주")
        }
    }
}
