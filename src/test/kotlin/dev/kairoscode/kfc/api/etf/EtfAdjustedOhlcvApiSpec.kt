package dev.kairoscode.kfc.api.etf

import dev.kairoscode.kfc.mock.MockEtfApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getAdjustedOhlcv()")
class EtfAdjustedOhlcvApiSpec : UnitTestBase() {

    @Test
    @DisplayName("티커로 조정주가를 조회할 수 있다")
    fun `can retrieve adjusted OHLCV by ticker`() = unitTest {
        // Given
        val jsonResponse = loadEtfAdjustedOhlcvResponse("069500_adjusted_1month")
        mockEtfApi = MockEtfApi(adjustedOhlcvResponse = jsonResponse)
        initClient()

        // When
        val ohlcv = client.etf.getAdjustedOhlcv(
            ticker = "069500",
            fromDate = LocalDate.of(2024, 10, 25),
            toDate = LocalDate.of(2024, 11, 25)
        )

        // Then
        assertThat(ohlcv).isNotEmpty
        println("조정주가 OHLCV: ${ohlcv.size}개")
    }
}
