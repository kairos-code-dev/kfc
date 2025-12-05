package dev.kairoscode.kfc.unit.corp

import dev.kairoscode.kfc.unit.corp.fake.FakeCorpApi
import dev.kairoscode.kfc.unit.utils.UnitTestBase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * CorpApi 에러 핸들링 테스트
 *
 * 정상 케이스는 통합 테스트(CorpApi*Spec)에서 검증합니다.
 * 이 테스트는 에러 상황만 검증합니다.
 *
 * ## 테스트 범위
 * - 네트워크 에러 시뮬레이션
 * - 잘못된 응답 파싱 처리
 * - 빈 응답 처리
 * - 필수 파라미터 검증
 *
 * @see FakeCorpApi
 */
@DisplayName("[U][Corp] CorpApi - 에러 핸들링")
class CorpApiErrorTest : UnitTestBase() {

    @Nested
    @DisplayName("Fake API 설정 에러")
    inner class FakeConfigurationErrors {

        @Test
        @DisplayName("getCorpCodeList - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getCorpCodeList throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: corpCodeResponse가 설정되지 않은 Fake API
            fakeCorpApi = FakeCorpApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.corp!!.getCorpCodeList()
            }
            assertTrue(exception.message!!.contains("corpCodeResponse가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("getDividendInfo - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getDividendInfo throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: dividendResponse가 설정되지 않은 Fake API
            fakeCorpApi = FakeCorpApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.corp!!.getDividendInfo("00126380", 2024, "11011")
            }
            assertTrue(exception.message!!.contains("dividendResponse가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("getStockSplitInfo - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getStockSplitInfo throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: stockSplitResponse가 설정되지 않은 Fake API
            fakeCorpApi = FakeCorpApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.corp!!.getStockSplitInfo("00126380", 2024, "11011")
            }
            assertTrue(exception.message!!.contains("stockSplitResponse가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("searchDisclosures - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `searchDisclosures throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: disclosureResponse가 설정되지 않은 Fake API
            fakeCorpApi = FakeCorpApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                val startDate = java.time.LocalDate.now().minusDays(7)
                val endDate = java.time.LocalDate.now()
                client.corp!!.searchDisclosures(
                    corpCode = "00126380",
                    startDate = startDate,
                    endDate = endDate,
                    pageNo = 1,
                    pageCount = 10
                )
            }
            assertTrue(exception.message!!.contains("disclosureResponse가 설정되지 않았습니다"))
        }
    }

    @Nested
    @DisplayName("파싱 에러")
    inner class ParsingErrors {

        @Test
        @DisplayName("getCorpCodeList - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `getCorpCodeList returns empty list on empty array response`() = unitTest {
            // Given: 빈 JSON 배열 응답
            fakeCorpApi = FakeCorpApi(
                corpCodeResponse = "[]"
            )
            initClient()

            // When: API 호출
            val result = client.corp!!.getCorpCodeList()

            // Then: 빈 리스트 반환
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("getDividendInfo - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `getDividendInfo returns empty list on empty array response`() = unitTest {
            // Given: 빈 JSON 배열 응답
            fakeCorpApi = FakeCorpApi(
                dividendResponse = "[]"
            )
            initClient()

            // When: API 호출
            val result = client.corp!!.getDividendInfo("00126380", 2024, "11011")

            // Then: 빈 리스트 반환
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("getStockSplitInfo - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `getStockSplitInfo returns empty list on empty array response`() = unitTest {
            // Given: 빈 JSON 배열 응답
            fakeCorpApi = FakeCorpApi(
                stockSplitResponse = "[]"
            )
            initClient()

            // When: API 호출
            val result = client.corp!!.getStockSplitInfo("00126380", 2024, "11011")

            // Then: 빈 리스트 반환
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("searchDisclosures - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `searchDisclosures returns empty list on empty array response`() = unitTest {
            // Given: 빈 JSON 배열 응답
            fakeCorpApi = FakeCorpApi(
                disclosureResponse = "[]"
            )
            initClient()

            // When: API 호출
            val startDate = java.time.LocalDate.now().minusDays(7)
            val endDate = java.time.LocalDate.now()
            val result = client.corp!!.searchDisclosures(
                corpCode = "00126380",
                startDate = startDate,
                endDate = endDate,
                pageNo = 1,
                pageCount = 10
            )

            // Then: 빈 리스트 반환
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("CorpApi null 처리")
    inner class NullCorpApi {

        @Test
        @DisplayName("CorpApi가 null인 경우 NullPointerException을 던진다")
        fun `throws NullPointerException when CorpApi is null`() = unitTest {
            // Given: CorpApi를 설정하지 않음 (fakeFundsApi만 설정)
            fakeFundsApi = object : dev.kairoscode.kfc.api.FundsApi {
                override suspend fun getList(type: dev.kairoscode.kfc.domain.FundType?) = emptyList<dev.kairoscode.kfc.domain.funds.FundListItem>()
                override suspend fun getDetailedInfo(isin: String, tradeDate: java.time.LocalDate) = null
                override suspend fun getGeneralInfo(isin: String, tradeDate: java.time.LocalDate) = null
                override suspend fun getPortfolio(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.PortfolioConstituent>()
                override suspend fun getPortfolioTop10(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.PortfolioTopItem>()
                override suspend fun getTrackingError(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.TrackingError>()
                override suspend fun getDivergenceRate(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.DivergenceRate>()
                override suspend fun getAllInvestorTrading(date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTrading>()
                override suspend fun getAllInvestorTradingByPeriod(fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTradingByDate>()
                override suspend fun getInvestorTrading(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTrading>()
                override suspend fun getInvestorTradingByPeriod(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTradingByDate>()
                override suspend fun getShortSelling(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate, type: dev.kairoscode.kfc.domain.FundType) = emptyList<dev.kairoscode.kfc.domain.funds.ShortSelling>()
                override suspend fun getShortBalance(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate, type: dev.kairoscode.kfc.domain.FundType) = emptyList<dev.kairoscode.kfc.domain.funds.ShortBalance>()
            }
            initClient()

            // When & Then: CorpApi 접근 시 NullPointerException 발생
            assertThrows<NullPointerException> {
                client.corp!!.getCorpCodeList()
            }
        }
    }
}
