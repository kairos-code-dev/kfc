package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getTrackingError()")
class EtfTrackingErrorApiTest : UnitTestBase() {

    @Test
    @DisplayName("추적 오차를 조회할 수 있다")
    fun `getTrackingError_validDateRange_returnsData`() = unitTest {
        // Given: TIGER 200 ETF의 추적 오차 응답 준비
        val jsonResponse = loadMockResponse("etf/tracking_error", "tiger200_tracking_error")
        fakeFundsApi = FakeFundsApi(trackingErrorResponse = jsonResponse)
        initClient()

        // When: 특정 기간의 추적 오차를 조회하면
        val trackingError = client.funds.getTrackingError(
            isin = "KR7069500007",
            fromDate = LocalDate.of(2024, 11, 1),
            toDate = LocalDate.of(2024, 11, 14)
        )

        // Then: 추적 오차 데이터가 반환된다
        assertThat(trackingError)
            .describedAs("추적 오차 데이터가 비어있습니다")
            .isNotEmpty
        println("추적 오차: ${trackingError.size}개")
    }
}
