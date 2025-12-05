package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FundsApi - 투자자별 거래 조회 API 통합 테스트
 *
 * 전체 ETF 및 개별 ETF의 투자자 유형별 매수/매도 데이터를 조회합니다.
 */
@DisplayName("FundsApi - 투자자별 거래 API")
class FundsApiInvestorTradingSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("getAllInvestorTrading() - 전체 ETF 투자자별 거래 조회 (특정일)")
    inner class GetAllInvestorTrading {

        @Nested
        @DisplayName("1. 기본 동작")
        inner class BasicOperations {

            @Test
            @DisplayName("특정 날짜의 전체 ETF 투자자별 거래를 조회할 수 있다")
            fun get_all_etf_investor_trading_on_specific_date() = integrationTest {
                println("\n📘 API: getAllInvestorTrading()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val date = TestFixtures.Dates.TRADING_DAY

                println("📥 Input Parameters:")
                println("  • date: LocalDate = $date")

                // When
                val investorTrading = client.funds.getAllInvestorTrading(date)

                // Then
                println("\n📤 Response: List<InvestorTrading>")
                println("  • size: ${investorTrading.size}")

                if (investorTrading.isNotEmpty()) {
                    println("\n  [샘플 데이터 - 상위 3개]")
                    investorTrading.take(3).forEach { trading ->
                        println("    • ${trading.investorType}: 순매수 ${trading.netBuyVolume}주")
                    }
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(investorTrading.isNotEmpty(), "투자자별 거래 데이터가 반환되어야 합니다")

                SmartRecorder.recordSmartly(
                    data = investorTrading,
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "all_etf_investor_trading"
                )
            }
        }
    }

    @Nested
    @DisplayName("getAllInvestorTradingByPeriod() - 전체 ETF 투자자별 거래 조회 (기간)")
    inner class GetAllInvestorTradingByPeriod {

        @Nested
        @DisplayName("1. 기본 동작")
        inner class BasicOperations {

            @Test
            @DisplayName("기간별 전체 ETF 투자자별 거래를 조회할 수 있다")
            fun get_all_etf_investor_trading_by_period() = integrationTest {
                println("\n📘 API: getAllInvestorTradingByPeriod()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When
                val investorTradingByDate = client.funds.getAllInvestorTradingByPeriod(fromDate, toDate)

                // Then
                println("\n📤 Response: List<InvestorTrading>")
                println("  • size: ${investorTradingByDate.size}")

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(investorTradingByDate.isNotEmpty(), "기간별 투자자 거래 데이터가 반환되어야 합니다")

                SmartRecorder.recordSmartly(
                    data = investorTradingByDate,
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "all_etf_investor_trading_period"
                )
            }
        }
    }

    @Nested
    @DisplayName("getInvestorTrading() - 개별 ETF 투자자별 거래 조회 (특정일)")
    inner class GetInvestorTrading {

        @Nested
        @DisplayName("1. 기본 동작")
        inner class BasicOperations {

            @Test
            @DisplayName("개별 ETF의 투자자별 거래를 조회할 수 있다")
            fun get_individual_etf_investor_trading() = integrationTest {
                println("\n📘 API: getInvestorTrading()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val date = TestFixtures.Dates.TRADING_DAY

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • date: LocalDate = $date")

                // When
                val investorTrading = client.funds.getInvestorTrading(isin, date)

                // Then
                println("\n📤 Response: List<InvestorTrading>")
                println("  • size: ${investorTrading.size}")

                if (investorTrading.isNotEmpty()) {
                    println("\n  [투자자 유형별 순매수]")
                    investorTrading.forEach { trading ->
                        println("    • ${trading.investorType}: ${trading.netBuyVolume}주 (${trading.netBuyValue}원)")
                    }
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(investorTrading.isNotEmpty(), "투자자별 거래 데이터가 반환되어야 합니다")

                SmartRecorder.recordSmartly(
                    data = investorTrading,
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "tiger200_investor_trading"
                )
            }
        }
    }

    @Nested
    @DisplayName("getInvestorTradingByPeriod() - 개별 ETF 투자자별 거래 조회 (기간)")
    inner class GetInvestorTradingByPeriod {

        @Nested
        @DisplayName("1. 기본 동작")
        inner class BasicOperations {

            @Test
            @DisplayName("개별 ETF의 기간별 투자자별 거래를 조회할 수 있다")
            fun get_individual_etf_investor_trading_by_period() = integrationTest {
                println("\n📘 API: getInvestorTradingByPeriod()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When
                val investorTradingByDate = client.funds.getInvestorTradingByPeriod(isin, fromDate, toDate)

                // Then
                println("\n📤 Response: List<InvestorTrading>")
                println("  • size: ${investorTradingByDate.size}")

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(investorTradingByDate.isNotEmpty(), "기간별 투자자 거래 데이터가 반환되어야 합니다")

                SmartRecorder.recordSmartly(
                    data = investorTradingByDate,
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "tiger200_investor_trading_period"
                )
            }
        }

        @Nested
        @DisplayName("5. 실무 활용 예제")
        inner class PracticalExamples {

            @Test
            @DisplayName("기관 매매 추이를 분석할 수 있다")
            fun analyze_institutional_trading_trend() = integrationTest {
                println("\n📘 API: getInvestorTradingByPeriod() - 활용 예제")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When
                val investorTradingByDate = client.funds.getInvestorTradingByPeriod(isin, fromDate, toDate)

                // Then: 기관 순매수 추이 분석
                println("\n📤 Response: List<InvestorTrading>")
                println("  • size: ${investorTradingByDate.size}")

                val institutionalNetBuy = investorTradingByDate
                    .filter { it.investorType.contains("기관") }
                    .map { Pair(it.tradeDate, it.netBuyVolume) }

                println("\n  [기관 순매수 추이 - 상위 5일]")
                institutionalNetBuy.take(5).forEach { (date, netBuy) ->
                    println("    • $date: ${netBuy}주")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            }
        }
    }
}
