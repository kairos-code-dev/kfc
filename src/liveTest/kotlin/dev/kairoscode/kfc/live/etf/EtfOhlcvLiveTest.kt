package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF OHLCV 조회 Live Test
 *
 * getOhlcv() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfOhlcvLiveTest : LiveTestBase() {

    @Test
    @DisplayName("1개월 OHLCV 데이터를 고정 기간으로 조회할 수 있다")
    fun testGetOhlcv1Month() = liveTest {
        // Given: 고정 1개월 기간 설정
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.ONE_MONTH_AGO // 2024-10-25
        val isin = TestSymbols.TIGER_200_ISIN

        // When: OHLCV 조회
        val ohlcvList = client.etf.getOhlcv(isin, fromDate, toDate)

        // Then: 약 20개 거래일 데이터 반환 (주말 제외)
        assertTrue(ohlcvList.size >= 15, "1개월 데이터는 최소 15개 이상이어야 합니다. 실제: ${ohlcvList.size}개")

        // Then: OHLCV 정합성 검증
        ohlcvList.forEach { ohlcv ->
            assertTrue(ohlcv.highPrice >= ohlcv.openPrice, "고가는 시가보다 크거나 같아야 합니다")
            assertTrue(ohlcv.highPrice >= ohlcv.closePrice, "고가는 종가보다 크거나 같아야 합니다")
            assertTrue(ohlcv.lowPrice <= ohlcv.openPrice, "저가는 시가보다 작거나 같아야 합니다")
            assertTrue(ohlcv.lowPrice <= ohlcv.closePrice, "저가는 종가보다 작거나 같아야 합니다")
            assertTrue(ohlcv.volume >= 0, "거래량은 0 이상이어야 합니다")
        }

        println("✅ 1개월 OHLCV 데이터 개수: ${ohlcvList.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = ohlcvList,
            category = RecordingConfig.Paths.Etf.OHLCV,
            fileName = "tiger200_1month"
        )
    }

    @Test
    @DisplayName("3개월 OHLCV 데이터를 고정 기간으로 조회할 수 있다")
    fun testGetOhlcv3Months() = liveTest {
        // Given: 고정 3개월 기간 설정
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.THREE_MONTHS_AGO // 2024-08-25
        val isin = TestSymbols.TIGER_200_ISIN

        // When: OHLCV 조회
        val ohlcvList = client.etf.getOhlcv(isin, fromDate, toDate)

        // Then: 약 60개 거래일 데이터 반환
        assertTrue(ohlcvList.size >= 50, "3개월 데이터는 최소 50개 이상이어야 합니다. 실제: ${ohlcvList.size}개")

        println("✅ 3개월 OHLCV 데이터 개수: ${ohlcvList.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = ohlcvList,
            category = RecordingConfig.Paths.Etf.OHLCV,
            fileName = "tiger200_3months"
        )
    }

    @Test
    @DisplayName("1년 OHLCV 데이터를 고정 기간으로 조회할 수 있다 (자동 분할 처리)")
    fun testGetOhlcv1Year() = liveTest {
        // Given: 고정 1년 기간 설정 (730일 초과)
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.ONE_YEAR_AGO // 2023-11-25
        val isin = TestSymbols.TIGER_200_ISIN

        // When: OHLCV 조회
        val ohlcvList = client.etf.getOhlcv(isin, fromDate, toDate)

        // Then: 약 250개 거래일 데이터 반환
        assertTrue(ohlcvList.size >= 200, "1년 데이터는 최소 200개 이상이어야 합니다. 실제: ${ohlcvList.size}개")

        // Then: 내부적으로 730일 단위로 자동 분할 처리됨
        println("✅ 1년 OHLCV 데이터 개수: ${ohlcvList.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = ohlcvList,
            category = RecordingConfig.Paths.Etf.OHLCV,
            fileName = "tiger200_1year"
        )
    }

    @Test
    @DisplayName("[활용] 고정 기간 기준으로 수익률을 계산할 수 있다")
    fun testReturnCalculation() = liveTest {
        // Given: 고정 3개월 OHLCV 데이터
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.THREE_MONTHS_AGO // 2024-08-25
        val isin = TestSymbols.TIGER_200_ISIN
        val ohlcvList = client.etf.getOhlcv(isin, fromDate, toDate)

        assertTrue(ohlcvList.isNotEmpty(), "OHLCV 데이터가 있어야 합니다")

        // When: 첫 종가와 마지막 종가 비교
        val firstClose = ohlcvList.first().closePrice.toDouble()
        val lastClose = ohlcvList.last().closePrice.toDouble()
        val returnRate = ((lastClose - firstClose) / firstClose) * 100

        // Then: 수익률 출력
        println("\n=== 3개월 수익률 계산 (기간: $fromDate ~ $toDate) ===")
        println("시작 종가: ${firstClose}원 (${ohlcvList.first().tradeDate})")
        println("마지막 종가: ${lastClose}원 (${ohlcvList.last().tradeDate})")
        println("3개월 수익률: ${"%.2f".format(returnRate)}%")
    }

    @Test
    @DisplayName("[활용] 고정 기간 기준으로 이동평균을 계산할 수 있다")
    fun testMovingAverageCalculation() = liveTest {
        // Given: 고정 3개월 OHLCV 데이터
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.THREE_MONTHS_AGO // 2024-08-25
        val isin = TestSymbols.TIGER_200_ISIN
        val ohlcvList = client.etf.getOhlcv(isin, fromDate, toDate)

        assertTrue(ohlcvList.size >= 20, "20일 이동평균 계산을 위해 최소 20개 데이터가 필요합니다")

        // When: 마지막 20개 종가의 평균 계산
        val last20Closes = ohlcvList.takeLast(20).map { it.closePrice.toDouble() }
        val ma20 = last20Closes.average()

        // Then: 20일 이동평균 출력
        println("\n=== 20일 이동평균 계산 (기준일: $toDate) ===")
        println("현재가: ${ohlcvList.last().closePrice}원")
        println("20일 이동평균: ${"%.2f".format(ma20)}원")
    }
}
