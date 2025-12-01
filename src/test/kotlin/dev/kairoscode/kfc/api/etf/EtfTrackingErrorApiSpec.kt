package dev.kairoscode.kfc.api.etf

import dev.kairoscode.kfc.mock.MockEtfApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getTrackingError()")
class EtfTrackingErrorApiSpec : UnitTestBase() {

    @Test
    @DisplayName("추적 오차를 조회할 수 있다")
    fun `can retrieve tracking error`() = unitTest {
        // Given
        val jsonResponse = loadMockResponse("etf/tracking_error", "tiger200_tracking_error")
        mockEtfApi = MockEtfApi(trackingErrorResponse = jsonResponse)
        initClient()

        // When
        val trackingError = client.etf.getTrackingError(
            isin = "KR7069500007",
            fromDate = LocalDate.of(2024, 11, 1),
            toDate = LocalDate.of(2024, 11, 14)
        )

        // Then
        assertThat(trackingError).isNotEmpty
        println("추적 오차: ${trackingError.size}개")
    }
}
