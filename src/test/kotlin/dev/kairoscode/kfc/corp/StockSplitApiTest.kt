package dev.kairoscode.kfc.corp

import dev.kairoscode.kfc.corp.mock.MockCorpApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CorpApi - getStockSplitInfo()")
class StockSplitApiTest : UnitTestBase() {

    @Test
    @DisplayName("주식 분할/병합 정보를 조회할 수 있다")
    fun `can retrieve stock split info`() = unitTest {
        // Given
        val jsonResponse = loadStockSplitResponse("stock_split_2024")
        mockCorpApi = MockCorpApi(stockSplitResponse = jsonResponse)
        initClient()

        // When
        val splits = client.corp!!.getStockSplitInfo(
            corpCode = "00164470",
            year = 2024
        )

        // Then
        assertThat(splits).isNotEmpty
        println("주식 분할 정보: ${splits.size}건")
        splits.forEach {
            println("  ${it.eventType}: ${it.eventDate}")
        }
    }
}
