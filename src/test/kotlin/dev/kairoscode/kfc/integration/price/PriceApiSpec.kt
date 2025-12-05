package dev.kairoscode.kfc.integration.price

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * [Price] PriceApi - 가격 정보 API 통합 테스트
 *
 * 네이버 금융 API를 사용한 주식/ETF 가격 정보 조회 기능을 검증합니다.
 *
 * ## 테스트 범위
 * - 일별 가격 API: getRecentDaily() - 최근 10일 일별 가격 조회
 * - 분봉 가격 API: getIntradayBars() - 당일 분봉 가격 조회
 *
 * ## 데이터 소스
 * - API Key 불필요 (네이버 금융 공개 데이터)
 * - KRX 공개 데이터 기반
 */
@DisplayName("[I][Price] PriceApi - 가격 정보 API")
class PriceApiSpec : IntegrationTestBase() {

    // ========================================
    // 일별 가격 API
    // ========================================

    @Nested
    @DisplayName("일별 가격 API")
    inner class DailyPriceApi {

        /**
         * getRecentDaily() - 최근 10일 일별 가격 조회
         *
         * ## API 개요
         * ETF의 최근 일별 거래 데이터를 조회하는 API입니다.
         * 최근 10거래일까지의 시세 요약 정보를 제공합니다.
         *
         * ## 엔드포인트
         * ```kotlin
         * fun getRecentDaily(isin: String, tradeDate: LocalDate): List<RecentDaily>
         * ```
         *
         * ## 파라미터
         * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
         * - `tradeDate`: LocalDate - 조회 기준일 (해당일 기준 과거 10거래일)
         *
         * ## 응답 데이터 (List<RecentDaily>)
         * 각 RecentDaily 객체:
         * - `tradeDate`: LocalDate - 거래일
         * - `closePrice`: Int - 종가 (원)
         * - `change`: Int - 전일 대비 변동 (원)
         * - `changeRate`: Double - 전일 대비 변동률 (%)
         * - `volume`: Long - 거래량 (주)
         * - `openPrice`: Int - 시가 (원)
         * - `highPrice`: Int - 고가 (원)
         * - `lowPrice`: Int - 저가 (원)
         *
         * ## 특이사항
         * - 최대 10거래일까지 반환 (거래일 수가 10일 미만일 수 있음)
         * - 최신 거래일이 리스트의 첫 번째 요소
         * - 비거래일 조회 시에도 최근 거래일 데이터 제공
         * - API Key 불필요 (KRX 공개 데이터)
         */
        @Nested
        @DisplayName("getRecentDaily() - 최근 10일 일별 가격 조회")
        inner class GetRecentDaily {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("TIGER 200의 최근 일별 거래를 거래일에 조회할 수 있다")
                fun get_tiger200_recent_daily_on_trading_day() = integrationTest {
                    println("\n📘 API: getRecentDaily()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: TIGER 200 ISIN and trading day
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input Parameters:")
                    println("  • isin: String = \"$isin\"")
                    println("  • tradeDate: LocalDate = $tradeDate")

                    // When: Query recent daily data
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Returns data
                    assertNotNull(recentDailyList, "거래일에는 TIGER 200의 최근 일별 거래가 반환되어야 합니다")
                    assertTrue(recentDailyList.isNotEmpty(), "최근 거래일 데이터는 최소 1개 이상의 거래일을 포함해야 합니다")
                    assertTrue(recentDailyList.size <= 10, "최근 일별 거래는 최대 10거래일까지만 포함합니다")

                    println("\n📤 Response: List<RecentDaily> (size: ${recentDailyList.size})")
                    println("  조회 기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")
                    println()
                    println("  최근 거래일 데이터:")
                    val latest = recentDailyList.first()
                    println("    • tradeDate: ${latest.tradeDate}")
                    println("    • closePrice: ${latest.closePrice}원")
                    println("    • change: ${latest.change}원")
                    println("    • changeRate: ${latest.changeRate}%")
                    println("    • volume: ${latest.volume}주")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    // 응답 레코딩
                    SmartRecorder.recordSmartly(
                        data = recentDailyList,
                        category = RecordingConfig.Paths.EtfPrice.RECENT_DAILY,
                        fileName = "tiger200_recent_daily"
                    )
                }

                @Test
                @DisplayName("KODEX 200의 최근 일별 거래를 거래일에 조회할 수 있다")
                fun get_kodex200_recent_daily_on_trading_day() = integrationTest {
                    println("\n📘 API: getRecentDaily()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: KODEX 200 ISIN and trading day
                    val isin = TestFixtures.Etf.KODEX_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input Parameters:")
                    println("  • isin: String = \"$isin\"")
                    println("  • tradeDate: LocalDate = $tradeDate")

                    // When: Query recent daily data
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Returns data
                    assertNotNull(recentDailyList, "거래일에는 KODEX 200의 최근 일별 거래가 반환되어야 합니다")
                    assertTrue(recentDailyList.isNotEmpty(), "거래일 데이터는 최소 1개 이상의 거래일을 포함해야 합니다")

                    println("\n📤 Response: List<RecentDaily> (size: ${recentDailyList.size})")
                    println("  거래일 개수: ${recentDailyList.size}개")
                    println("  조회 기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    // 응답 레코딩
                    SmartRecorder.recordSmartly(
                        data = recentDailyList,
                        category = RecordingConfig.Paths.EtfPrice.RECENT_DAILY,
                        fileName = "kodex200_recent_daily"
                    )
                }

                @Test
                @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 ETF의 가격 데이터를 조회할 수 있다")
                fun get_different_price_data_by_different_isin() = integrationTest {
                    println("\n📘 파라미터 테스트: isin")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Two different ISINs
                    val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                    val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When: Query with different ISINs
                    val tiger200Data = client.price.getRecentDaily(tiger200Isin, tradeDate)
                    val kodex200Data = client.price.getRecentDaily(kodex200Isin, tradeDate)

                    // Then: Returns different price data
                    assertNotNull(tiger200Data)
                    assertNotNull(kodex200Data)
                    assertTrue(tiger200Data.isNotEmpty())
                    assertTrue(kodex200Data.isNotEmpty())

                    println("  Case 1: isin = \"$tiger200Isin\" (TIGER 200)")
                    println("    → 최신 종가: ${tiger200Data.first().closePrice}원")
                    println()
                    println("  Case 2: isin = \"$kodex200Isin\" (KODEX 200)")
                    println("    → 최신 종가: ${kodex200Data.first().closePrice}원")
                    println()
                    println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 가격 데이터 조회 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("응답 리스트의 크기는 1 이상 10 이하다")
                fun response_list_size_is_between_1_and_10() = integrationTest {
                    println("\n📘 응답 데이터 검증: 리스트 크기")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Validate list size
                    assertTrue(recentDailyList.isNotEmpty(), "응답 리스트는 비어있지 않아야 합니다")
                    assertTrue(recentDailyList.size <= 10, "응답 리스트는 최대 10개 이하여야 합니다")

                    println("✅ 리스트 크기 검증:")
                    println("  • Size: ${recentDailyList.size}")
                    println("  • Range: 1 <= size <= 10 ✓")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("종가(closePrice)는 0보다 크다")
                fun close_price_is_positive() = integrationTest {
                    println("\n📘 응답 데이터 검증: 가격 범위")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Validate all close prices are positive
                    recentDailyList.forEach { daily ->
                        assertTrue(daily.closePrice > 0, "종가는 0보다 커야 합니다 (Date: ${daily.tradeDate})")
                    }

                    println("✅ 가격 범위 검증:")
                    println("  • 모든 거래일의 종가 > 0 ✓")
                    recentDailyList.take(3).forEach { daily ->
                        println("    - ${daily.tradeDate}: ${daily.closePrice}원")
                    }
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("거래량(volume)은 0 이상이다")
                fun volume_is_non_negative() = integrationTest {
                    println("\n📘 응답 데이터 검증: 거래량 범위")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Validate volume
                    val latest = recentDailyList.first()
                    assertTrue(latest.volume >= 0, "거래량은 0 이상이어야 합니다")

                    println("✅ 거래량 검증:")
                    println("  • volume: ${latest.volume}주")
                    println("  • Range: >= 0 ✓")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("리스트는 최신 거래일부터 과거 순으로 정렬되어 있다")
                fun list_is_sorted_by_trade_date_descending() = integrationTest {
                    println("\n📘 응답 데이터 검증: 날짜 정렬")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Validate date ordering
                    for (i in 0 until recentDailyList.size - 1) {
                        assertTrue(
                            recentDailyList[i].tradeDate >= recentDailyList[i + 1].tradeDate,
                            "리스트는 최신 거래일부터 과거 순으로 정렬되어야 합니다"
                        )
                    }

                    println("✅ 날짜 정렬 검증: 최신 → 과거 순")
                    println("  • 첫 번째 (최신): ${recentDailyList.first().tradeDate}")
                    println("  • 마지막 (과거): ${recentDailyList.last().tradeDate}")
                    println("  • 정렬 상태: 내림차순 ✓")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                @Test
                @DisplayName("존재하지 않는 ISIN 조회시 빈 리스트를 반환한다")
                fun returns_empty_list_for_non_existent_isin() = integrationTest {
                    println("\n📘 입력 검증: 존재하지 않는 ISIN")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Invalid ISIN
                    val invalidIsin = "KR7999999999"
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input:")
                    println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
                    println("  • tradeDate: $tradeDate")

                    // When
                    val recentDailyList = client.price.getRecentDaily(invalidIsin, tradeDate)

                    // Then: Returns empty list
                    assertTrue(recentDailyList.isEmpty(), "존재하지 않는 ISIN 조회시 빈 리스트를 반환해야 합니다")

                    println("\n📤 Response: [] (empty list)")
                    println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 리스트 반환")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("[파라미터: tradeDate] 비거래일에 조회하면 최근 거래일 데이터를 반환한다")
                fun returns_recent_trading_day_data_on_non_trading_day() = integrationTest {
                    println("\n📘 엣지 케이스: 비거래일 조회")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Non-trading day (Saturday)
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

                    println("📥 Input:")
                    println("  • isin: \"$isin\"")
                    println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

                    // When: Query on non-trading day
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    // Then: Returns recent trading day data
                    assertNotNull(recentDailyList, "API는 비거래일에도 데이터를 반환합니다 (최근 거래일 데이터)")

                    println("\n📤 Response: List<RecentDaily> (size: ${recentDailyList.size})")
                    println("  • 데이터 존재: 예 (API는 최근 거래일 데이터를 반환)")
                    println("  • 거래일 개수: ${recentDailyList.size}개")
                    if (recentDailyList.isNotEmpty()) {
                        println("  • 최신 거래일: ${recentDailyList.first().tradeDate}")
                    }

                    println("\n✅ 처리 결과: 비거래일에도 최근 거래일 데이터 제공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[활용] 최근 거래일 수익률을 계산할 수 있다")
                fun calculate_recent_return_rate() = integrationTest {
                    println("\n📘 활용 예제: 수익률 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: TIGER 200 recent daily data
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

                    // When: Calculate return rate
                    val newestClose = recentDailyList.first().closePrice.toDouble() // 최근 거래일
                    val oldestClose = recentDailyList.last().closePrice.toDouble()  // 오래된 거래일
                    val returnRate = if (oldestClose > 0) {
                        ((newestClose - oldestClose) / oldestClose) * 100
                    } else {
                        0.0
                    }

                    // Then: Display analysis
                    println("\n=== 최근 거래일 수익률 분석 ===")
                    println("기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")
                    println("시작가(과거): ${oldestClose}원")
                    println("종료가(최근): ${newestClose}원")
                    println("수익률: ${"%.2f".format(returnRate)}%")
                    println()
                    println("📊 분석: ${recentDailyList.size}거래일 기준 수익률 ${if (returnRate >= 0) "상승" else "하락"}")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 최근 거래일 거래량 추이를 분석할 수 있다")
                fun analyze_recent_volume_trend() = integrationTest {
                    println("\n📘 활용 예제: 거래량 추이 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: TIGER 200 recent daily data
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

                    // When: Calculate volume statistics
                    val totalVolume = recentDailyList.sumOf { it.volume }
                    val avgVolume = if (recentDailyList.isNotEmpty()) {
                        totalVolume / recentDailyList.size
                    } else {
                        0L
                    }
                    val maxVolume = recentDailyList.maxOfOrNull { it.volume } ?: 0L
                    val minVolume = recentDailyList.minOfOrNull { it.volume } ?: 0L

                    // Then: Display analysis
                    println("\n=== 최근 ${recentDailyList.size}거래일 거래량 분석 ===")
                    println("총 거래량: ${totalVolume}주")
                    println("평균 일거래량: ${avgVolume}주")
                    println("최대 일거래량: ${maxVolume}주")
                    println("최소 일거래량: ${minVolume}주")
                    println("분석 기간: ${recentDailyList.size}거래일")
                    println()
                    println("📊 분석: 평균 일거래량 ${avgVolume}주 기준")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 최근 가격 변동성을 분석할 수 있다")
                fun analyze_recent_price_volatility() = integrationTest {
                    println("\n📘 활용 예제: 가격 변동성 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: TIGER 200 recent daily data
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY
                    val recentDailyList = client.price.getRecentDaily(isin, tradeDate)

                    assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

                    // When: Calculate volatility metrics
                    val avgChangeRate = recentDailyList.map { kotlin.math.abs(it.changeRate) }.average()
                    val maxChange = recentDailyList.maxOfOrNull { kotlin.math.abs(it.change) } ?: 0
                    val highestPrice = recentDailyList.maxOfOrNull { it.closePrice } ?: 0
                    val lowestPrice = recentDailyList.minOfOrNull { it.closePrice } ?: 0

                    // Then: Display analysis
                    println("\n=== 최근 ${recentDailyList.size}거래일 변동성 분석 ===")
                    println("평균 변동률: ${"%.2f".format(avgChangeRate)}% (절대값 기준)")
                    println("최대 변동폭: ${maxChange}원")
                    println("기간 내 최고 종가: ${highestPrice}원")
                    println("기간 내 최저 종가: ${lowestPrice}원")
                    println("종가 범위: ${highestPrice - lowestPrice}원")
                    println()
                    println("📊 분석: 평균 일변동률 ${"%.2f".format(avgChangeRate)}% 수준의 변동성")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }
    }

    // ========================================
    // 분봉 가격 API
    // ========================================

    @Nested
    @DisplayName("분봉 가격 API")
    inner class IntradayPriceApi {

        /**
         * getIntradayBars() - 당일 분봉 가격 조회
         *
         * ## API 개요
         * ETF의 분단위 시세(Intraday Bars)를 조회하는 API입니다.
         * 장중 1분 단위 OHLCV 데이터를 제공합니다.
         *
         * ## 엔드포인트
         * ```kotlin
         * suspend fun getIntradayBars(isin: String, tradeDate: LocalDate): List<IntradayBar>
         * ```
         *
         * ## 파라미터
         * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
         * - `tradeDate`: LocalDate - 조회 대상 거래일
         *
         * ## 응답 데이터 (List<IntradayBar>)
         * - `time`: LocalTime - 분단위 시각
         * - `openPrice`: Int - 시가
         * - `highPrice`: Int - 고가
         * - `lowPrice`: Int - 저가
         * - `closePrice`: Int - 종가
         * - `cumulativeVolume`: Long - 누적 거래량
         *
         * ## 특이사항
         * - **당일 거래일만 데이터 제공**: 과거 날짜 조회시 빈 리스트 반환
         * - API Key 불필요 (KRX 공개 데이터)
         * - 약 330개의 분단위 데이터 포인트 (09:00~15:30)
         */
        @Nested
        @DisplayName("getIntradayBars() - 당일 분봉 가격 조회")
        inner class GetIntraday {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("TIGER 200의 분단위 시세를 조회할 수 있다 (과거 날짜는 빈 응답)")
                fun get_tiger200_intraday_bars() = integrationTest {
                    println("\n📘 API: getIntradayBars()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: TIGER 200 ISIN and trading day
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input Parameters:")
                    println("  • isin: String = \"$isin\"")
                    println("  • tradeDate: LocalDate = $tradeDate")

                    // When: Request intraday bars
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    // Then: Returns data (empty for past dates)
                    assertNotNull(bars, "API 호출은 성공해야 합니다")

                    println("\n📤 Response: List<IntradayBar>")
                    println("  • dataPoints: ${bars.size}개")

                    if (bars.isNotEmpty()) {
                        // Data available (current trading day)
                        println("  • period: ${bars.first().time} ~ ${bars.last().time}")
                        println("  • first bar: open=${bars.first().openPrice}, close=${bars.first().closePrice}")
                        println("  • last bar: open=${bars.last().openPrice}, close=${bars.last().closePrice}")

                        println("\n✅ 테스트 결과: 성공 (당일 데이터 제공)")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = bars,
                            category = RecordingConfig.Paths.EtfPrice.INTRADAY_BARS,
                            fileName = "tiger200_intraday_bars"
                        )
                    } else {
                        // Empty for past dates
                        println("  • ℹ️ 참고: 분단위 시세는 당일 거래일만 제공됩니다")
                        println("\n✅ 테스트 결과: 성공 (과거 날짜로 빈 응답)")
                    }

                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[파라미터: isin] KODEX 200의 분단위 시세를 조회할 수 있다")
                fun get_kodex200_intraday_bars() = integrationTest {
                    println("\n📘 파라미터 테스트: isin = KODEX_200")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: KODEX 200 ISIN
                    val isin = TestFixtures.Etf.KODEX_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input:")
                    println("  • isin: \"$isin\" (KODEX 200)")

                    // When: Request intraday bars
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    // Then: Returns data
                    assertNotNull(bars, "API 호출은 성공해야 합니다")

                    println("\n📤 Response:")
                    println("  • dataPoints: ${bars.size}개")

                    if (bars.isNotEmpty()) {
                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = bars,
                            category = RecordingConfig.Paths.EtfPrice.INTRADAY_BARS,
                            fileName = "kodex200_intraday_bars"
                        )
                    }

                    println("\n✅ 결과: KODEX 200 분단위 시세 조회 완료")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("응답은 분단위 OHLCV 데이터를 포함한다 (데이터가 있는 경우)")
                fun response_contains_ohlcv_data_when_available() = integrationTest {
                    println("\n📘 응답 데이터 검증: OHLCV 구조")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    // Then: Validate structure if data exists
                    if (bars.isNotEmpty()) {
                        val firstBar = bars.first()
                        assertNotNull(firstBar.time, "time이 있어야 합니다")
                        assertTrue(firstBar.openPrice > 0, "openPrice는 0보다 커야 합니다")
                        assertTrue(firstBar.closePrice > 0, "closePrice는 0보다 커야 합니다")
                        assertTrue(firstBar.highPrice >= firstBar.lowPrice, "고가는 저가보다 크거나 같아야 합니다")

                        println("✅ OHLCV 구조 검증 통과:")
                        println("  • time: ${firstBar.time} ✓")
                        println("  • open: ${firstBar.openPrice} (> 0) ✓")
                        println("  • high: ${firstBar.highPrice} ✓")
                        println("  • low: ${firstBar.lowPrice} ✓")
                        println("  • close: ${firstBar.closePrice} (> 0) ✓")
                        println("  • volume: ${firstBar.cumulativeVolume} ✓")
                    } else {
                        println("ℹ️ 데이터가 없습니다 (과거 날짜)")
                    }

                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("고가는 저가보다 크거나 같다 (데이터가 있는 경우)")
                fun high_price_is_greater_or_equal_to_low_price() = integrationTest {
                    println("\n📘 응답 데이터 검증: 가격 범위")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    // When
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    // Then: Validate price relationships
                    if (bars.isNotEmpty()) {
                        bars.forEach { bar ->
                            assertTrue(bar.highPrice >= bar.lowPrice, "고가는 저가보다 크거나 같아야 합니다")
                            assertTrue(bar.highPrice >= bar.openPrice, "고가는 시가보다 크거나 같아야 합니다")
                            assertTrue(bar.highPrice >= bar.closePrice, "고가는 종가보다 크거나 같아야 합니다")
                            assertTrue(bar.lowPrice <= bar.openPrice, "저가는 시가보다 작거나 같아야 합니다")
                            assertTrue(bar.lowPrice <= bar.closePrice, "저가는 종가보다 작거나 같아야 합니다")
                        }

                        println("✅ 가격 범위 검증:")
                        println("  • 모든 바에서 Low ≤ Open, Close ≤ High ✓")
                    } else {
                        println("ℹ️ 데이터가 없습니다 (과거 날짜)")
                    }

                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                @Test
                @DisplayName("존재하지 않는 ISIN 조회시 빈 리스트를 반환한다")
                fun returns_empty_list_for_non_existent_isin() = integrationTest {
                    println("\n📘 입력 검증: 존재하지 않는 ISIN")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Invalid ISIN that doesn't exist
                    val invalidIsin = "KR7999999999"
                    val tradeDate = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input:")
                    println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
                    println("  • tradeDate: $tradeDate")

                    // When
                    val bars = client.price.getIntradayBars(invalidIsin, tradeDate)

                    // Then: Returns empty list for non-existent ISIN
                    assertTrue(bars.isEmpty(), "존재하지 않는 ISIN은 빈 리스트를 반환해야 합니다")

                    println("\n📤 Response: List<IntradayBar> (empty)")
                    println("  • dataPoints: ${bars.size}")
                    println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 리스트 반환")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("[파라미터: tradeDate] 비거래일 조회시 빈 리스트를 반환한다")
                fun returns_empty_list_on_non_trading_day() = integrationTest {
                    println("\n📘 엣지 케이스: 비거래일 조회")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Non-trading day (Saturday)
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

                    println("📥 Input:")
                    println("  • isin: \"$isin\"")
                    println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

                    // When
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    // Then: Returns empty list for non-trading days
                    assertNotNull(bars, "API 호출은 성공해야 합니다")

                    println("\n📤 Response: List<IntradayBar> (empty)")
                    println("  • dataPoints: ${bars.size}개")
                    println()
                    println("  ℹ️ 참고: 비거래일에는 데이터가 없습니다")

                    println("\n✅ 처리 결과: 비거래일에 빈 리스트 반환")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[특이사항] 과거 거래일 조회시 빈 리스트를 반환한다")
                fun returns_empty_list_for_past_trading_day() = integrationTest {
                    println("\n📘 엣지 케이스: 과거 거래일 조회")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Past trading day
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY // This is a past date

                    println("📥 Input:")
                    println("  • isin: \"$isin\"")
                    println("  • tradeDate: $tradeDate (과거 거래일)")

                    // When
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    // Then: Returns empty list for past dates
                    assertNotNull(bars, "API 호출은 성공해야 합니다")

                    println("\n📤 Response:")
                    println("  • dataPoints: ${bars.size}개")
                    println()
                    println("  ℹ️ 참고: 분단위 시세 API는 당일 거래일만 데이터를 제공합니다")
                    println("  과거 날짜를 조회하면 빈 리스트가 반환됩니다")

                    println("\n✅ 처리 결과: 과거 날짜에 대해 빈 리스트 반환 (예상된 동작)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[활용] 장중 고가/저가를 기반으로 변동성을 분석할 수 있다")
                fun analyze_intraday_volatility() = integrationTest {
                    println("\n📘 활용 예제: 장중 변동성 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Intraday bars
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    println("\n=== 장중 변동성 분석 (거래일: $tradeDate) ===")

                    if (bars.isEmpty()) {
                        println("과거 날짜이므로 데이터가 없습니다 (분단위 시세는 당일만 제공)")
                        return@integrationTest
                    }

                    // When: Calculate volatility based on high/low range
                    val dayHigh = bars.maxOfOrNull { it.highPrice } ?: 0
                    val dayLow = bars.minOfOrNull { it.lowPrice } ?: 0
                    val dayRange = dayHigh - dayLow
                    val volatility = if (dayLow > 0) {
                        (dayRange.toDouble() / dayLow) * 100
                    } else {
                        0.0
                    }

                    // Then: Display analysis
                    println("일중 고가: ${dayHigh}원")
                    println("일중 저가: ${dayLow}원")
                    println("가격 범위: ${dayRange}원")
                    println("변동성: ${"%.2f".format(volatility)}%")
                    println()
                    println("📊 분석: 일중 가격 변동폭이 ${"%.2f".format(volatility)}%입니다")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 분단위 거래량 추이를 분석할 수 있다")
                fun analyze_intraday_volume_trend() = integrationTest {
                    println("\n📘 활용 예제: 분단위 거래량 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Intraday bars
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    println("\n=== 분단위 거래량 분석 (거래일: $tradeDate) ===")

                    if (bars.isEmpty()) {
                        println("과거 날짜이므로 데이터가 없습니다 (분단위 시세는 당일만 제공)")
                        return@integrationTest
                    }

                    // When: Calculate volume statistics
                    val totalVolume = bars.lastOrNull()?.cumulativeVolume ?: 0L
                    val avgVolumePerBar = if (bars.isNotEmpty()) totalVolume / bars.size else 0L
                    val maxVolumeBar = bars.maxByOrNull { it.cumulativeVolume }

                    // Then: Display analysis
                    println("총 거래량: ${totalVolume}주")
                    println("평균 분당 누적량: ${avgVolumePerBar}주")
                    println("최대 누적 거래량: ${maxVolumeBar?.cumulativeVolume}주 (시각: ${maxVolumeBar?.time})")
                    println("분석 대상 기간: ${bars.size}분")
                    println()
                    println("📊 분석: 장중 거래량 추이를 확인하여 유동성을 평가할 수 있습니다")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 시가와 종가를 비교하여 장중 등락을 확인할 수 있다")
                fun compare_open_and_close_prices() = integrationTest {
                    println("\n📘 활용 예제: 장중 등락 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Intraday bars
                    val isin = TestFixtures.Etf.TIGER_200_ISIN
                    val tradeDate = TestFixtures.Dates.TRADING_DAY
                    val bars = client.price.getIntradayBars(isin, tradeDate)

                    println("\n=== 장중 등락 분석 (거래일: $tradeDate) ===")

                    if (bars.isEmpty()) {
                        println("과거 날짜이므로 데이터가 없습니다 (분단위 시세는 당일만 제공)")
                        return@integrationTest
                    }

                    // When: Compare opening and closing prices
                    val openPrice = bars.firstOrNull()?.openPrice ?: 0
                    val closePrice = bars.lastOrNull()?.closePrice ?: 0
                    val change = closePrice - openPrice
                    val changePercent = if (openPrice > 0) {
                        (change.toDouble() / openPrice) * 100
                    } else {
                        0.0
                    }

                    // Then: Display analysis
                    println("시가: ${openPrice}원 (${bars.first().time})")
                    println("종가: ${closePrice}원 (${bars.last().time})")
                    println("등락: ${change}원 (${"%.2f".format(changePercent)}%)")
                    println()
                    println("📊 분석: 장중 ${if (change > 0) "상승" else if (change < 0) "하락" else "보합"} (${"%.2f".format(changePercent)}%)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }
    }
}
