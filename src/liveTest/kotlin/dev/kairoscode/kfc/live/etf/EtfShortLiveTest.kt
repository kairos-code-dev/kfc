package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * 공매도 데이터 조회 Live Test
 *
 * getShortSelling(), getShortBalance() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfShortLiveTest : LiveTestBase() {

    // ================================
    // 공매도 거래 테스트
    // ================================

    @Test
    @DisplayName("ETF 공매도 거래 현황을 조회할 수 있다")
    fun testGetShortSelling() = liveTest {
        // Given: TIGER 200 ISIN, 1개월 기간
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 거래 조회
        val shortSellings = client.etf.getShortSelling(isin, fromDate, toDate)

        // Then: 일별 공매도 거래량, 금액 데이터 반환
        assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

        println("✅ 공매도 거래 데이터 개수: ${shortSellings.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = shortSellings,
            category = RecordingConfig.Paths.Etf.SHORT,
            fileName = "tiger200_short_selling"
        )
    }

    @Test
    @DisplayName("KODEX 200 공매도 거래를 조회할 수 있다")
    fun testGetShortSellingKodex200() = liveTest {
        // Given: KODEX 200 ISIN
        val isin = TestSymbols.KODEX_200_ISIN
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 거래 조회
        val shortSellings = client.etf.getShortSelling(isin, fromDate, toDate)

        // Then: 데이터 반환
        assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

        println("✅ KODEX 200 공매도 거래 데이터 개수: ${shortSellings.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = shortSellings,
            category = RecordingConfig.Paths.Etf.SHORT,
            fileName = "kodex200_short_selling"
        )
    }

    // ================================
    // 공매도 잔고 테스트
    // ================================

    @Test
    @DisplayName("ETF 공매도 잔고 현황을 조회할 수 있다")
    fun testGetShortBalance() = liveTest {
        // Given: TIGER 200 ISIN, 1개월 기간
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 잔고 조회
        val shortBalances = client.etf.getShortBalance(isin, fromDate, toDate)

        // Then: 일별 공매도 잔고 데이터 반환
        assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

        println("✅ 공매도 잔고 데이터 개수: ${shortBalances.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = shortBalances,
            category = RecordingConfig.Paths.Etf.SHORT,
            fileName = "tiger200_short_balance"
        )
    }

    @Test
    @DisplayName("KODEX 200 공매도 잔고를 조회할 수 있다")
    fun testGetShortBalanceKodex200() = liveTest {
        // Given: KODEX 200 ISIN
        val isin = TestSymbols.KODEX_200_ISIN
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)

        // When: 공매도 잔고 조회
        val shortBalances = client.etf.getShortBalance(isin, fromDate, toDate)

        // Then: 데이터 반환
        assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

        println("✅ KODEX 200 공매도 잔고 데이터 개수: ${shortBalances.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = shortBalances,
            category = RecordingConfig.Paths.Etf.SHORT,
            fileName = "kodex200_short_balance"
        )
    }

    // ================================
    // 활용 예제
    // ================================

    @Test
    @DisplayName("[활용] 공매도 비중을 계산할 수 있다")
    fun testCalculateShortRatio() = liveTest {
        // Given: 공매도 잔고 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusDays(7) // 최근 1일
        val shortBalances = client.etf.getShortBalance(isin, fromDate, toDate)

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
    fun testShortSellingTrend() = liveTest {
        // Given: 공매도 거래 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)
        val shortSellings = client.etf.getShortSelling(isin, fromDate, toDate)

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
