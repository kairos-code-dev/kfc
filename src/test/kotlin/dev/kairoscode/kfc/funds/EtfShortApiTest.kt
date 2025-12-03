package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.mock.MockFundsApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("EtfApi - Short Selling & Balance")
class EtfShortApiTest : UnitTestBase() {

    @Nested
    @DisplayName("공매도 거래")
    inner class ShortSelling {

        @Test
        @DisplayName("공매도 거래 현황을 조회할 수 있다")
        fun `can retrieve short selling`() = unitTest {
            // Given
            val jsonResponse = loadMockResponse("etf/short", "tiger200_short_selling")
            mockFundsApi = MockFundsApi(shortSellingResponse = jsonResponse)
            initClient()

            // When
            val shortSelling = client.funds.getShortSelling(
                isin = "KR7069500007",
                fromDate = LocalDate.of(2024, 11, 1),
                toDate = LocalDate.of(2024, 11, 14)
            )

            // Then
            assertThat(shortSelling).isNotEmpty
            println("공매도 거래: ${shortSelling.size}개")
        }
    }

    @Nested
    @DisplayName("공매도 잔고")
    inner class ShortBalance {

        @Test
        @DisplayName("공매도 잔고 현황을 조회할 수 있다")
        fun `can retrieve short balance`() = unitTest {
            // Given
            val jsonResponse = loadMockResponse("etf/short", "tiger200_short_balance")
            mockFundsApi = MockFundsApi(shortBalanceResponse = jsonResponse)
            initClient()

            // When
            val shortBalance = client.funds.getShortBalance(
                isin = "KR7069500007",
                fromDate = LocalDate.of(2024, 11, 1),
                toDate = LocalDate.of(2024, 11, 14)
            )

            // Then
            assertThat(shortBalance).isNotEmpty
            println("공매도 잔고: ${shortBalance.size}개")
        }
    }
}
