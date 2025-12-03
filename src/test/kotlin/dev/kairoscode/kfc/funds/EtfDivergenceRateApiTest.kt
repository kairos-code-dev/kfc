package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.mock.MockFundsApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getDivergenceRate()")
class EtfDivergenceRateApiTest : UnitTestBase() {

    @Test
    @DisplayName("괴리율을 조회할 수 있다")
    fun `can retrieve divergence rate`() = unitTest {
        // Given
        val jsonResponse = loadMockResponse("etf/divergence_rate", "tiger200_divergence_rate")
        mockFundsApi = MockFundsApi(divergenceRateResponse = jsonResponse)
        initClient()

        // When
        val divergenceRate = client.funds.getDivergenceRate(
            isin = "KR7069500007",
            fromDate = LocalDate.of(2024, 11, 1),
            toDate = LocalDate.of(2024, 11, 14)
        )

        // Then
        assertThat(divergenceRate).isNotEmpty
        println("괴리율: ${divergenceRate.size}개")
    }
}
