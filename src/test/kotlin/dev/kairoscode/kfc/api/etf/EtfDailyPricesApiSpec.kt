package dev.kairoscode.kfc.api.etf

import dev.kairoscode.kfc.mock.MockEtfApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EtfApi - getAllDailyPrices()")
class EtfDailyPricesApiSpec : UnitTestBase() {

    @Test
    @DisplayName("전체 ETF 일별 시세를 조회할 수 있다")
    fun `can retrieve all ETF daily prices`() = unitTest {
        // Given
        val jsonResponse = loadEtfDailyPricesResponse("all_etf_20241125")
        mockEtfApi = MockEtfApi(dailyPricesResponse = jsonResponse)
        initClient()

        // When
        val prices = client.etf.getAllDailyPrices()

        // Then
        assertThat(prices).isNotEmpty
        println("전체 ETF 시세: ${prices.size}개")
    }
}
