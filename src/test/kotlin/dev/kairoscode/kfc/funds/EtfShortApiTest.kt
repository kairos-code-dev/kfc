package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
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
        fun getShortSelling_withValidPeriod_returnsData() = unitTest {
            // Given: TIGER 200 ETF의 공매도 거래 조회를 위한 Mock API 설정
            val jsonResponse = loadMockResponse("etf/short", "tiger200_short_selling")
            fakeFundsApi = FakeFundsApi(shortSellingResponse = jsonResponse)
            initClient()

            val isin = "KR7069500007"
            val fromDate = LocalDate.of(2024, 11, 1)
            val toDate = LocalDate.of(2024, 11, 14)

            // When: 지정된 기간의 공매도 거래 현황을 조회
            val shortSelling = client.funds.getShortSelling(
                isin = isin,
                fromDate = fromDate,
                toDate = toDate
            )

            // Then: 공매도 거래 데이터가 반환되어야 함
            assertThat(shortSelling)
                .describedAs("공매도 거래 결과가 비어있습니다 (ISIN: %s, 기간: %s ~ %s)", isin, fromDate, toDate)
                .isNotEmpty
            println("공매도 거래: ${shortSelling.size}개")
        }
    }

    @Nested
    @DisplayName("공매도 잔고")
    inner class ShortBalance {

        @Test
        @DisplayName("공매도 잔고 현황을 조회할 수 있다")
        fun getShortBalance_withValidPeriod_returnsData() = unitTest {
            // Given: TIGER 200 ETF의 공매도 잔고 조회를 위한 Mock API 설정
            val jsonResponse = loadMockResponse("etf/short", "tiger200_short_balance")
            fakeFundsApi = FakeFundsApi(shortBalanceResponse = jsonResponse)
            initClient()

            val isin = "KR7069500007"
            val fromDate = LocalDate.of(2024, 11, 1)
            val toDate = LocalDate.of(2024, 11, 14)

            // When: 지정된 기간의 공매도 잔고 현황을 조회
            val shortBalance = client.funds.getShortBalance(
                isin = isin,
                fromDate = fromDate,
                toDate = toDate
            )

            // Then: 공매도 잔고 데이터가 반환되어야 함
            assertThat(shortBalance)
                .describedAs("공매도 잔고 결과가 비어있습니다 (ISIN: %s, 기간: %s ~ %s)", isin, fromDate, toDate)
                .isNotEmpty
            println("공매도 잔고: ${shortBalance.size}개")
        }
    }
}
