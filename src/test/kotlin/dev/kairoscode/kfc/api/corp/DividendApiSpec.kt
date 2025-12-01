package dev.kairoscode.kfc.api.corp

import dev.kairoscode.kfc.mock.MockCorpApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CorpApi - getDividendInfo()")
class DividendApiSpec : UnitTestBase() {

    @Test
    @DisplayName("특정 연도 배당 정보를 조회할 수 있다")
    fun `can retrieve dividend info for specific year`() = unitTest {
        // Given
        val jsonResponse = loadDividendResponse("samsung_dividend_2024")
        mockCorpApi = MockCorpApi(dividendResponse = jsonResponse)
        initClient()

        // When
        val dividends = client.corp!!.getDividendInfo(
            corpCode = "00126380",
            year = 2024
        )

        // Then
        assertThat(dividends).isNotEmpty
        println("배당 정보: ${dividends.size}건")
        dividends.forEach {
            println("  ${it.dividendType}: ${it.currentYear}원")
        }
    }
}
