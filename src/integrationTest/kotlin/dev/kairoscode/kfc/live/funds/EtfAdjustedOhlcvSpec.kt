package dev.kairoscode.kfc.live.funds

import dev.kairoscode.kfc.utils.IntegrationTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.SmartRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertTrue

/**
 * 조정주가 OHLCV 조회 Integration Test (Naver API)
 *
 * getAdjustedOhlcv() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfAdjustedOhlcvSpec : IntegrationTestBase() {

    @Test
    @DisplayName("조정주가 OHLCV를 조회할 수 있다")
    fun testGetAdjustedOhlcv() = integrationTest {
        // Given: TIGER 200 티커 (6자리)
        val ticker = TestSymbols.TIGER_200_TICKER
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 조정주가 OHLCV 조회
        val adjustedOhlcvList = client.funds.getAdjustedOhlcv(ticker, fromDate, toDate)

        // Then: 조정주가 데이터 반환
        assertTrue(adjustedOhlcvList.isNotEmpty(), "조정주가 데이터가 반환되어야 합니다")

        // Then: 각 데이터는 OHLCV 포함
        adjustedOhlcvList.forEach { ohlcv ->
            assertTrue(ohlcv.high >= ohlcv.open, "고가는 시가보다 크거나 같아야 합니다")
            assertTrue(ohlcv.high >= ohlcv.close, "고가는 종가보다 크거나 같아야 합니다")
            assertTrue(ohlcv.low <= ohlcv.open, "저가는 시가보다 작거나 같아야 합니다")
            assertTrue(ohlcv.low <= ohlcv.close, "저가는 종가보다 작거나 같아야 합니다")
        }

        println("✅ 조정주가 OHLCV 데이터 개수: ${adjustedOhlcvList.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = adjustedOhlcvList,
            category = RecordingConfig.Paths.EtfPrice.ADJUSTED,
            fileName = "${ticker}_adjusted_1month"
        )
    }

    @Test
    @DisplayName("KODEX 200 조정주가를 조회할 수 있다")
    fun testGetAdjustedOhlcvKodex200() = integrationTest {
        // Given: KODEX 200 티커
        val ticker = TestSymbols.KODEX_200_TICKER
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(1)

        // When: 조정주가 OHLCV 조회
        val adjustedOhlcvList = client.funds.getAdjustedOhlcv(ticker, fromDate, toDate)

        // Then: 데이터 반환
        assertTrue(adjustedOhlcvList.isNotEmpty(), "조정주가 데이터가 반환되어야 합니다")

        println("✅ KODEX 200 조정주가 데이터 개수: ${adjustedOhlcvList.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = adjustedOhlcvList,
            category = RecordingConfig.Paths.EtfPrice.ADJUSTED,
            fileName = "${ticker}_adjusted_1month"
        )
    }

    @Test
    @DisplayName("[활용] 조정주가 기반 수익률을 계산할 수 있다")
    fun testAdjustedReturnCalculation() = integrationTest {
        // Given: 조정주가 OHLCV 데이터
        val ticker = TestSymbols.TIGER_200_TICKER
        val toDate = TestSymbols.TRADING_DAY
        val fromDate = toDate.minusMonths(3)
        val adjustedOhlcvList = client.funds.getAdjustedOhlcv(ticker, fromDate, toDate)

        assertTrue(adjustedOhlcvList.isNotEmpty(), "조정주가 데이터가 있어야 합니다")

        // When: 첫 종가와 마지막 종가 비교
        val firstClose = adjustedOhlcvList.first().close
        val lastClose = adjustedOhlcvList.last().close
        val returnRate = ((lastClose.toDouble() - firstClose.toDouble()) / firstClose.toDouble()) * 100

        // Then: 수익률 출력
        println("\n=== 조정주가 기반 3개월 수익률 ===")
        println("시작 조정주가: ${firstClose}원 (${adjustedOhlcvList.first().date})")
        println("마지막 조정주가: ${lastClose}원 (${adjustedOhlcvList.last().date})")
        println("수익률: ${"%.2f".format(returnRate)}%")
    }
}
