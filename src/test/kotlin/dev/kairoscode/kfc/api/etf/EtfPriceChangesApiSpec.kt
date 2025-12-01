package dev.kairoscode.kfc.api.etf

import dev.kairoscode.kfc.mock.MockEtfApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getPriceChanges()")
class EtfPriceChangesApiSpec : UnitTestBase() {

    @Test
    @DisplayName("기간별 등락률을 조회할 수 있다")
    fun `can retrieve price changes for period`() = unitTest {
        // Given
        val jsonResponse = loadEtfPriceChangesResponse("price_changes_1month")
        mockEtfApi = MockEtfApi(priceChangesResponse = jsonResponse)
        initClient()

        // When
        val changes = client.etf.getPriceChanges(
            fromDate = LocalDate.of(2024, 10, 25),
            toDate = LocalDate.of(2024, 11, 25)
        )

        // Then
        assertThat(changes).isNotEmpty
        println("ETF 등락률: ${changes.size}개")
        changes.take(3).forEach { 
            println("  ${it.name}: ${it.changeRate}%")
        }
    }
}
