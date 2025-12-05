package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * FundsApi - 공매도/대차거래 API 통합 테스트
 *
 * 이 테스트는 공매도 거래 및 잔고 조회 API의 실제 동작을 검증합니다.
 */
@DisplayName("FundsApi - 공매도/대차거래 API")
class FundsApiShortSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("getShortSelling() - 공매도 거래 조회")
    inner class GetShortSelling {

        @Nested
        @DisplayName("1. 기본 동작")
        inner class BasicOperations {

            @Test
            @DisplayName("TIGER 200의 공매도 거래를 기간별로 조회할 수 있다")
            fun get_tiger200_short_selling_by_period() = integrationTest {
                println("\n📘 API: getShortSelling()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given: 입력 파라미터 표시
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When: API 호출
                val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                // Then: 결과 즉시 표시
                println("\n📤 Response: List<ShortSelling>")
                println("  • size: ${shortSellings.size}")

                if (shortSellings.isNotEmpty()) {
                    val sample = shortSellings.first()
                    println("\n  [샘플 데이터]")
                    println("  • tradeDate: ${sample.tradeDate}")
                    println("  • shortVolume: ${sample.shortVolume}주")
                    println("  • shortValue: ${sample.shortValue}원")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

                // 스마트 레코딩
                SmartRecorder.recordSmartly(
                    data = shortSellings,
                    category = RecordingConfig.Paths.EtfTrading.SHORT,
                    fileName = "tiger200_short_selling"
                )
            }

            @Test
            @DisplayName("KODEX 200의 공매도 거래를 기간별로 조회할 수 있다")
            fun get_kodex200_short_selling_by_period() = integrationTest {
                println("\n📘 API: getShortSelling()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given: 입력 파라미터 표시
                val isin = TestFixtures.Etf.KODEX_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When: API 호출
                val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                // Then: 결과 즉시 표시
                println("\n📤 Response: List<ShortSelling>")
                println("  • size: ${shortSellings.size}")

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

                // 스마트 레코딩
                SmartRecorder.recordSmartly(
                    data = shortSellings,
                    category = RecordingConfig.Paths.EtfTrading.SHORT,
                    fileName = "kodex200_short_selling"
                )
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증")
        inner class ResponseValidation {

            @Test
            @DisplayName("응답 데이터는 일별 공매도 거래량과 거래금액을 포함한다")
            fun validate_response_contains_daily_trading_data() = integrationTest {
                println("\n📘 API: getShortSelling()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusDays(7)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When
                val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                // Then: 필드 검증
                println("\n📤 Response: List<ShortSelling>")
                println("  • size: ${shortSellings.size}")

                if (shortSellings.isNotEmpty()) {
                    val first = shortSellings.first()
                    println("\n  [필드 검증]")
                    println("  • tradeDate: ${first.tradeDate} ✓")
                    println("  • shortVolume: ${first.shortVolume} ✓")
                    println("  • shortValue: ${first.shortValue} ✓")

                    assertNotNull(first.tradeDate, "거래일자는 null이 아니어야 합니다")
                    assertTrue(first.shortVolume >= 0, "공매도 거래량은 0 이상이어야 합니다")
                    assertTrue(first.shortValue >= 0, "공매도 거래금액은 0 이상이어야 합니다")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 있어야 합니다")
            }
        }

        @Nested
        @DisplayName("5. 실무 활용 예제")
        inner class PracticalExamples {

            @Test
            @DisplayName("공매도 거래량 급증 날짜를 분석할 수 있다")
            fun analyze_short_selling_spike_days() = integrationTest {
                println("\n📘 API: getShortSelling() - 활용 예제")
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
                val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                // Then: 급증 날짜 분석
                println("\n📤 Response: List<ShortSelling>")
                println("  • size: ${shortSellings.size}")

                if (shortSellings.isNotEmpty()) {
                    val avgVolume = shortSellings.map { it.shortVolume }.average()
                    val highVolumeDays = shortSellings.filter { it.shortVolume > avgVolume * 2 }

                    println("\n  [공매도 거래량 급증 분석]")
                    println("  • 평균 공매도 거래량: ${"%.0f".format(avgVolume)}주")
                    println("  • 급증 날짜 수 (평균의 2배 이상): ${highVolumeDays.size}일")

                    if (highVolumeDays.isNotEmpty()) {
                        println("\n  [급증 날짜 상세]")
                        highVolumeDays.forEach { day ->
                            println("    - ${day.tradeDate}: ${day.shortVolume}주")
                        }
                    }
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 있어야 합니다")
            }
        }
    }

    @Nested
    @DisplayName("getShortBalance() - 공매도 잔고 조회")
    inner class GetShortBalance {

        @Nested
        @DisplayName("1. 기본 동작")
        inner class BasicOperations {

            @Test
            @DisplayName("TIGER 200의 공매도 잔고를 기간별로 조회할 수 있다")
            fun get_tiger200_short_balance_by_period() = integrationTest {
                println("\n📘 API: getShortBalance()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given: 입력 파라미터 표시
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When: API 호출
                val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                // Then: 결과 즉시 표시
                println("\n📤 Response: List<ShortBalance>")
                println("  • size: ${shortBalances.size}")

                if (shortBalances.isNotEmpty()) {
                    val sample = shortBalances.first()
                    println("\n  [샘플 데이터]")
                    println("  • tradeDate: ${sample.tradeDate}")
                    println("  • shortBalance: ${sample.shortBalance}주")
                    println("  • shortBalanceValue: ${sample.shortBalanceValue}원")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

                // 스마트 레코딩
                SmartRecorder.recordSmartly(
                    data = shortBalances,
                    category = RecordingConfig.Paths.EtfTrading.SHORT,
                    fileName = "tiger200_short_balance"
                )
            }

            @Test
            @DisplayName("KODEX 200의 공매도 잔고를 기간별로 조회할 수 있다")
            fun get_kodex200_short_balance_by_period() = integrationTest {
                println("\n📘 API: getShortBalance()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given: 입력 파라미터 표시
                val isin = TestFixtures.Etf.KODEX_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusMonths(1)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When: API 호출
                val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                // Then: 결과 즉시 표시
                println("\n📤 Response: List<ShortBalance>")
                println("  • size: ${shortBalances.size}")

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

                // 스마트 레코딩
                SmartRecorder.recordSmartly(
                    data = shortBalances,
                    category = RecordingConfig.Paths.EtfTrading.SHORT,
                    fileName = "kodex200_short_balance"
                )
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증")
        inner class ResponseValidation {

            @Test
            @DisplayName("응답 데이터는 일별 공매도 잔고와 잔고금액을 포함한다")
            fun validate_response_contains_daily_balance_data() = integrationTest {
                println("\n📘 API: getShortBalance()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusDays(7)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When
                val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                // Then: 필드 검증
                println("\n📤 Response: List<ShortBalance>")
                println("  • size: ${shortBalances.size}")

                if (shortBalances.isNotEmpty()) {
                    val first = shortBalances.first()
                    println("\n  [필드 검증]")
                    println("  • tradeDate: ${first.tradeDate} ✓")
                    println("  • shortBalance: ${first.shortBalance} ✓")
                    println("  • shortBalanceValue: ${first.shortBalanceValue} ✓")

                    assertNotNull(first.tradeDate, "거래일자는 null이 아니어야 합니다")
                    assertTrue(first.shortBalance >= 0, "공매도 잔고는 0 이상이어야 합니다")
                    assertTrue(first.shortBalanceValue >= 0, "공매도 잔고금액은 0 이상이어야 합니다")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 있어야 합니다")
            }
        }

        @Nested
        @DisplayName("5. 실무 활용 예제")
        inner class PracticalExamples {

            @Test
            @DisplayName("최근 공매도 잔고 현황을 확인할 수 있다")
            fun check_recent_short_balance_status() = integrationTest {
                println("\n📘 API: getShortBalance() - 활용 예제")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val isin = TestFixtures.Etf.TIGER_200_ISIN
                val toDate = TestFixtures.Dates.TRADING_DAY
                val fromDate = toDate.minusDays(7)

                println("📥 Input Parameters:")
                println("  • isin: String = \"$isin\"")
                println("  • fromDate: LocalDate = $fromDate")
                println("  • toDate: LocalDate = $toDate")

                // When
                val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                // Then: 최근 잔고 현황 분석
                println("\n📤 Response: List<ShortBalance>")
                println("  • size: ${shortBalances.size}")

                if (shortBalances.isNotEmpty()) {
                    val latestBalance = shortBalances.last()
                    println("\n  [최근 공매도 잔고 현황]")
                    println("  • 날짜: ${latestBalance.tradeDate}")
                    println("  • 공매도 잔고: ${latestBalance.shortBalance}주")
                    println("  • 공매도 금액: ${latestBalance.shortBalanceValue}원")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 있어야 합니다")
            }
        }
    }
}
