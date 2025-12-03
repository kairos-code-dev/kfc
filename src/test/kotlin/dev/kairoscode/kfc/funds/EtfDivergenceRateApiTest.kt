package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getDivergenceRate()")
class EtfDivergenceRateApiTest : UnitTestBase() {

    @Test
    @DisplayName("괴리율을 조회할 수 있다")
    fun getDivergenceRate_withValidPeriod_returnsData() = unitTest {
        // Given: TIGER 200 ETF의 괴리율 조회를 위한 Mock API 설정
        val jsonResponse = loadMockResponse("etf/divergence_rate", "tiger200_divergence_rate")
        fakeFundsApi = FakeFundsApi(divergenceRateResponse = jsonResponse)
        initClient()

        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 11, 1)
        val toDate = LocalDate.of(2024, 11, 14)

        // When: 지정된 기간의 괴리율을 조회
        val divergenceRate = client.funds.getDivergenceRate(
            isin = isin,
            fromDate = fromDate,
            toDate = toDate
        )

        // Then: 괴리율 데이터가 반환되어야 함
        assertThat(divergenceRate)
            .describedAs("괴리율 결과가 비어있습니다 (ISIN: %s, 기간: %s ~ %s)", isin, fromDate, toDate)
            .isNotEmpty
        println("괴리율: ${divergenceRate.size}개")
    }
}
