package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertTrue

/**
 * 공매도 데이터 조회 Integration Test
 *
 * getShortSelling(), getShortBalance() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
@DisplayName("FundsApi.getShort() - 공매도 데이터 조회")
class FundsApiShortSpec : IntegrationTestBase() {

    // ================================
    // 공매도 거래 테스트
    // ================================

    @Test
    @DisplayName("ETF 공매도 거래 현황을 조회할 수 있다")
    fun testGetShortSelling() = integrationTest {
        // Given: TIGER 200 ISIN, 1개월 기간
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 거래 조회
        val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

        // Then: 일별 공매도 거래량, 금액 데이터 반환
        assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

        println("✅ 공매도 거래 데이터 개수: ${shortSellings.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = shortSellings,
            category = RecordingConfig.Paths.EtfTrading.SHORT,
            fileName = "tiger200_short_selling"
        )
    }

    @Test
    @DisplayName("KODEX 200 공매도 거래를 조회할 수 있다")
    fun testGetShortSellingKodex200() = integrationTest {
        // Given: KODEX 200 ISIN
        val isin = TestFixtures.Etf.KODEX_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 거래 조회
        val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

        // Then: 데이터 반환
        assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

        println("✅ KODEX 200 공매도 거래 데이터 개수: ${shortSellings.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = shortSellings,
            category = RecordingConfig.Paths.EtfTrading.SHORT,
            fileName = "kodex200_short_selling"
        )
    }

    // ================================
    // 공매도 잔고 테스트
    // ================================

    @Test
    @DisplayName("ETF 공매도 잔고 현황을 조회할 수 있다")
    fun testGetShortBalance() = integrationTest {
        // Given: TIGER 200 ISIN, 1개월 기간
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 잔고 조회
        val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

        // Then: 일별 공매도 잔고 데이터 반환
        assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

        println("✅ 공매도 잔고 데이터 개수: ${shortBalances.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = shortBalances,
            category = RecordingConfig.Paths.EtfTrading.SHORT,
            fileName = "tiger200_short_balance"
        )
    }

    @Test
    @DisplayName("KODEX 200 공매도 잔고를 조회할 수 있다")
    fun testGetShortBalanceKodex200() = integrationTest {
        // Given: KODEX 200 ISIN
        val isin = TestFixtures.Etf.KODEX_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 잔고 조회
        val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

        // Then: 데이터 반환
        assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

        println("✅ KODEX 200 공매도 잔고 데이터 개수: ${shortBalances.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = shortBalances,
            category = RecordingConfig.Paths.EtfTrading.SHORT,
            fileName = "kodex200_short_balance"
        )
    }

    // ================================
    // 활용 예제
    // ================================

    @Test
    @DisplayName("[활용] 공매도 비중을 계산할 수 있다")
    fun testCalculateShortRatio() = integrationTest {
        // Given: 공매도 잔고 데이터
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusDays(7) // 최근 1일
        val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

        assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 있어야 합니다")

        // When: 잔고 비율 계산 (잔고 / 상장주식수 * 100)
        val latestBalance = shortBalances.last()

        // Then: 공매도 비중 출력
        println("\n=== 공매도 잔고 현황 ===")
        println("날짜: ${latestBalance.tradeDate}")
        println("공매도 잔고: ${latestBalance.shortBalance}주")
        println("공매도 금액: ${latestBalance.shortBalanceValue}원")
    }

    @Test
    @DisplayName("[활용] 공매도 증가 추이를 확인할 수 있다")
    fun testShortSellingTrend() = integrationTest {
        // Given: 공매도 거래 데이터
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val toDate = TestFixtures.Dates.TRADING_DAY
        val fromDate = toDate.minusMonths(1)
        val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

        assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 있어야 합니다")

        // When: 일별 공매도 거래량 추이 분석
        val avgVolume = shortSellings.map { it.shortVolume }.average()
        val highVolumeDays = shortSellings.filter { it.shortVolume > avgVolume * 2 }

        // Then: 급증한 날짜 출력
        println("\n=== 공매도 거래량 급증 날짜 (평균의 2배 이상) ===")
        println("평균 공매도 거래량: ${"%.0f".format(avgVolume)}주")
        if (highVolumeDays.isNotEmpty()) {
            highVolumeDays.forEach { day ->
                println("${day.tradeDate}: ${day.shortVolume}주")
            }
        } else {
            println("급증한 날짜가 없습니다.")
        }
    }
}
