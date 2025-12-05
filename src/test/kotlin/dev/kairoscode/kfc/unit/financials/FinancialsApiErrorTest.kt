package dev.kairoscode.kfc.unit.financials

import dev.kairoscode.kfc.unit.financials.fake.FakeFinancialsApi
import dev.kairoscode.kfc.unit.utils.UnitTestBase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FinancialsApi 에러 핸들링 테스트
 *
 * 정상 케이스는 통합 테스트(FinancialsApi*Spec)에서 검증합니다.
 * 이 테스트는 에러 상황만 검증합니다.
 *
 * ## 테스트 범위
 * - Fake API 설정 에러
 * - 파싱 에러
 * - FinancialsApi null 처리
 *
 * @see FakeFinancialsApi
 */
@DisplayName("[U][Financials] FinancialsApi - 에러 핸들링")
class FinancialsApiErrorTest : UnitTestBase() {

    @Nested
    @DisplayName("Fake API 설정 에러")
    inner class FakeConfigurationErrors {

        @Test
        @DisplayName("getIncomeStatement - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getIncomeStatement throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: incomeStatementResponse가 설정되지 않은 Fake API
            fakeFinancialsApi = FakeFinancialsApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.financials!!.getIncomeStatement("00126380", 2024)
            }
            assertTrue(exception.message!!.contains("incomeStatementResponse가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("getBalanceSheet - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getBalanceSheet throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: balanceSheetResponse가 설정되지 않은 Fake API
            fakeFinancialsApi = FakeFinancialsApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.financials!!.getBalanceSheet("00126380", 2024)
            }
            assertTrue(exception.message!!.contains("balanceSheetResponse가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("getCashFlowStatement - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getCashFlowStatement throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: cashFlowStatementResponse가 설정되지 않은 Fake API
            fakeFinancialsApi = FakeFinancialsApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.financials!!.getCashFlowStatement("00126380", 2024)
            }
            assertTrue(exception.message!!.contains("cashFlowStatementResponse가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("getAllFinancials - 응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `getAllFinancials throws IllegalArgumentException when response is not configured`() = unitTest {
            // Given: allFinancialsResponse가 설정되지 않은 Fake API
            fakeFinancialsApi = FakeFinancialsApi()
            initClient()

            // When & Then: API 호출 시 예외 발생
            val exception = assertThrows<IllegalArgumentException> {
                client.financials!!.getAllFinancials("00126380", 2024)
            }
            assertTrue(exception.message!!.contains("allFinancialsResponse가 설정되지 않았습니다"))
        }
    }

    @Nested
    @DisplayName("FinancialsApi null 처리")
    inner class NullFinancialsApi {

        @Test
        @DisplayName("FinancialsApi가 null인 경우 NullPointerException을 던진다")
        fun `throws NullPointerException when FinancialsApi is null`() = unitTest {
            // Given: FinancialsApi를 설정하지 않음 (fakeFundsApi만 설정)
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

            // When & Then: FinancialsApi 접근 시 NullPointerException 발생
            assertThrows<NullPointerException> {
                client.financials!!.getIncomeStatement("00126380", 2024)
            }
        }
    }
}
