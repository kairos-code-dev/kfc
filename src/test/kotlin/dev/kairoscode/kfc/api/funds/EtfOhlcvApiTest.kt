package dev.kairoscode.kfc.api.funds

import dev.kairoscode.kfc.mock.MockFundsApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - getOhlcv()")
class EtfOhlcvApiTest : UnitTestBase() {

    @Nested
    @DisplayName("getOhlcv() 사용법")
    inner class GetOhlcvUsage {

        @Test
        @DisplayName("기본 사용법 - 특정 기간의 OHLCV를 조회할 수 있다")
        fun `basic usage - can retrieve OHLCV for specific period`() = unitTest {
            // Given
            val jsonResponse = loadEtfOhlcvResponse("tiger200_1month")
            mockFundsApi = MockFundsApi(ohlcvResponse = jsonResponse)
            initClient()

            // When
            val ohlcv = client.funds.getOhlcv(
                isin = "KR7069500007",
                fromDate = LocalDate.of(2024, 10, 25),
                toDate = LocalDate.of(2024, 11, 25)
            )

            // Then
            assertThat(ohlcv).isNotEmpty
            
            println("OHLCV 데이터: ${ohlcv.size}개")
            ohlcv.take(3).forEach { data ->
                println("  ${data.tradeDate}: O=${data.openPrice}, H=${data.highPrice}, L=${data.lowPrice}, C=${data.closePrice}")
            }
        }
    }

    @Nested
    @DisplayName("getOhlcv() API 명세")
    inner class GetOhlcvSpecification {

        @Test
        @DisplayName("[명세] 고가는 시가, 저가, 종가보다 크거나 같다")
        fun `specification - high price validation`() = unitTest {
            // Given
            val jsonResponse = loadEtfOhlcvResponse("tiger200_1month")
            mockFundsApi = MockFundsApi(ohlcvResponse = jsonResponse)
            initClient()

            // When
            val ohlcv = client.funds.getOhlcv(
                isin = "KR7069500007",
                fromDate = LocalDate.of(2024, 10, 25),
                toDate = LocalDate.of(2024, 11, 25)
            )

            // Then: 가격 정합성 검증
            ohlcv.forEach { data ->
                assertThat(data.highPrice).isGreaterThanOrEqualTo(data.openPrice)
                assertThat(data.highPrice).isGreaterThanOrEqualTo(data.lowPrice)
                assertThat(data.highPrice).isGreaterThanOrEqualTo(data.closePrice)
            }
        }
    }
}
