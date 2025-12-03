package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertTrue

/**
 * 투자자별 거래 조회 Integration Test
 *
 * getAllInvestorTrading(), getAllInvestorTradingByPeriod(),
 * getInvestorTrading(), getInvestorTradingByPeriod() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
@DisplayName("FundsApi.getInvestorTrading() - 투자자별 거래 조회")
class FundsApiInvestorTradingSpec : IntegrationTestBase() {

    // ================================
    // 전체 ETF 투자자별 거래
    // ================================

    @Test
    @DisplayName("특정 날짜의 전체 ETF 투자자별 거래를 조회할 수 있다")
    fun testGetAllInvestorTrading() = integrationTest {
        // Given: 특정 거래일
        val date = TestFixtures.Dates.TRADING_DAY

        // When: 전체 ETF 투자자별 거래 조회
        val investorTrading = client.funds.getAllInvestorTrading(date)

        // Then: 전체 ETF의 투자자 유형별 매수/매도 데이터 반환
        assertTrue(investorTrading.isNotEmpty(), "투자자별 거래 데이터가 반환되어야 합니다")

        println("✅ 전체 ETF 투자자별 거래 데이터 개수: ${investorTrading.size}")
        println("✅ 조회 날짜: $date")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = investorTrading,
            category = RecordingConfig.Paths.EtfTrading.INVESTOR,
            fileName = "all_etf_investor_trading"
        )
    }

    @Test
    @DisplayName("기간별 전체 ETF 투자자별 거래를 조회할 수 있다")
    fun testGetAllInvestorTradingByPeriod() = integrationTest {
        // Given: 시작일, 종료일 (1개월)
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 기간별 전체 ETF 투자자별 거래 조회
        val investorTradingByDate = client.funds.getAllInvestorTradingByPeriod(fromDate, toDate)

        // Then: 일별 투자자 거래 데이터 반환
        assertTrue(investorTradingByDate.isNotEmpty(), "기간별 투자자 거래 데이터가 반환되어야 합니다")

        println("✅ 기간별 전체 ETF 투자자별 거래 데이터 개수: ${investorTradingByDate.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = investorTradingByDate,
            category = RecordingConfig.Paths.EtfTrading.INVESTOR,
            fileName = "all_etf_investor_trading_period"
        )
    }

    // ================================
    // 개별 ETF 투자자별 거래
    // ================================

    @Test
    @DisplayName("개별 ETF의 투자자별 거래를 조회할 수 있다")
    fun testGetInvestorTrading() = integrationTest {
        // Given: TIGER 200 ISIN, 특정 날짜
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val date = TestFixtures.Dates.TRADING_DAY

        // When: 투자자별 거래 조회
        val investorTrading = client.funds.getInvestorTrading(isin, date)

        // Then: TIGER 200의 투자자 유형별 매수/매도 데이터 반환
        assertTrue(investorTrading.isNotEmpty(), "투자자별 거래 데이터가 반환되어야 합니다")

        println("✅ TIGER 200 투자자별 거래 데이터 개수: ${investorTrading.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = investorTrading,
            category = RecordingConfig.Paths.EtfTrading.INVESTOR,
            fileName = "tiger200_investor_trading"
        )
    }

    @Test
    @DisplayName("개별 ETF의 기간별 투자자별 거래를 조회할 수 있다")
    fun testGetInvestorTradingByPeriod() = integrationTest {
        // Given: TIGER 200 ISIN, 기간
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 기간별 투자자별 거래 조회
        val investorTradingByDate = client.funds.getInvestorTradingByPeriod(isin, fromDate, toDate)

        // Then: 일별 투자자 거래 추이 반환
        assertTrue(investorTradingByDate.isNotEmpty(), "기간별 투자자 거래 데이터가 반환되어야 합니다")

        println("✅ TIGER 200 기간별 투자자별 거래 데이터 개수: ${investorTradingByDate.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = investorTradingByDate,
            category = RecordingConfig.Paths.EtfTrading.INVESTOR,
            fileName = "tiger200_investor_trading_period"
        )
    }

    // ================================
    // 활용 예제
    // ================================

    @Test
    @DisplayName("[활용] 투자자 유형별 순매수를 분석할 수 있다")
    fun testAnalyzeInvestorNetBuy() = integrationTest {
        // Given: 전체 ETF 투자자별 거래 데이터
        val date = TestFixtures.Dates.TRADING_DAY
        val investorTrading = client.funds.getAllInvestorTrading(date)

        // When: 투자자 유형별 순매수 집계
        println("\n=== 투자자 유형별 순매수 현황 ===")
        investorTrading.forEach { trading ->
            println("${trading.investorType}: ${trading.netBuyVolume}주 (${trading.netBuyValue}원)")
        }
    }

    @Test
    @DisplayName("[활용] 기관 매매 추이를 분석할 수 있다")
    fun testAnalyzeInstitutionalTradingTrend() = integrationTest {
        // Given: 개별 ETF 기간별 데이터
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)
        val investorTradingByDate = client.funds.getInvestorTradingByPeriod(isin, fromDate, toDate)

        // When: 기관 순매수 시계열 데이터 생성
        val institutionalNetBuy = investorTradingByDate
            .filter { it.investorType.contains("기관") }
            .map {
                Pair(it.tradeDate, it.netBuyVolume)
            }

        // Then: 일별 기관 순매수 추이 출력
        println("\n=== TIGER 200 기관 순매수 추이 ===")
        institutionalNetBuy.forEach { (date, netBuy) ->
            println("$date: ${netBuy}주")
        }
    }
}
