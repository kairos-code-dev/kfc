package dev.kairoscode.kfc.live.funds

import dev.kairoscode.kfc.utils.IntegrationTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.SmartRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 최근 일별 거래 조회 Integration Test
 *
 * getRecentDaily() 함수의 실제 API 호출 테스트 및 응답 레코딩
 * 최근 10거래일의 일별 시세 요약 데이터 조회
 */
class EtfRecentDailySpec : IntegrationTestBase() {

    @Test
    @DisplayName("TIGER 200 최근 일별 거래를 거래일에 조회할 수 있다")
    fun testGetRecentDailyTiger200OnTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 거래일
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY // 2024-11-25 (월요일)

        // When: 최근 일별 거래 조회
        val recentDailyList = client.funds.getRecentDaily(isin, tradeDate)

        // Then: 데이터 반환
        assertNotNull(recentDailyList, "거래일에는 TIGER 200의 최근 일별 거래가 반환되어야 합니다")
        assertTrue(recentDailyList.isNotEmpty(), "최근 거래일 데이터는 최소 1개 이상의 거래일을 포함해야 합니다")
        assertTrue(recentDailyList.size <= 10, "최근 일별 거래는 최대 10거래일까지만 포함합니다")

        // 첫 번째와 마지막 데이터 검증
        val latestDay = recentDailyList.first()  // 최근 거래일이 첫 번째
        val earliestDay = recentDailyList.last() // 가장 오래된 거래일이 마지막
        assertTrue(latestDay.closePrice > 0, "종가는 0보다 커야 합니다")
        assertTrue(latestDay.volume >= 0, "거래량은 0 이상이어야 합니다")

        println("✅ TIGER 200 최근 일별 거래 (기준일: $tradeDate)")
        println("  - 조회 기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")
        println("  - 거래일 개수: ${recentDailyList.size}개")
        println("  - 최근(${recentDailyList.first().tradeDate}): 종가 ${recentDailyList.first().closePrice}원, 변동 ${recentDailyList.first().change}원")
        println("  - 거래량: ${recentDailyList.first().volume}주")

        // 응답 레코딩 (리스트 데이터는 SmartRecorder 사용)
        SmartRecorder.recordSmartly(
            data = recentDailyList,
            category = RecordingConfig.Paths.EtfPrice.RECENT_DAILY,
            fileName = "tiger200_recent_daily"
        )
    }

    @Test
    @DisplayName("TIGER 200 최근 일별 거래를 비거래일에 조회하면 데이터를 반환한다 (API는 최근 거래일 데이터 제공)")
    fun testGetRecentDailyTiger200OnNonTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 비거래일 (토요일)
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.NON_TRADING_DAY // 2024-11-23 (토요일)

        // When: 최근 일별 거래 조회
        val recentDailyList = client.funds.getRecentDaily(isin, tradeDate)

        // Then: 데이터 반환 (API는 비거래일에도 최근 거래일 데이터 제공)
        assertNotNull(recentDailyList, "API는 비거래일에도 데이터를 반환합니다 (최근 거래일 데이터)")

        println("✅ 비거래일($tradeDate) 최근 일별 거래 조회 결과:")
        println("  - 데이터 존재: 예 (API는 최근 거래일 데이터를 반환)")
        println("  - 거래일 개수: ${recentDailyList.size}개")
    }

    @Test
    @DisplayName("KODEX 200 최근 일별 거래를 거래일에 조회할 수 있다")
    fun testGetRecentDailyKodex200OnTradingDay() = integrationTest {
        // Given: KODEX 200 ISIN과 고정 거래일
        val isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY // 2024-11-25 (월요일)

        // When: 최근 일별 거래 조회
        val recentDailyList = client.funds.getRecentDaily(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(recentDailyList, "거래일에는 KODEX 200의 최근 일별 거래가 반환되어야 합니다")
        assertTrue(recentDailyList.isNotEmpty(), "거래일 데이터는 최소 1개 이상의 거래일을 포함해야 합니다")

        println("✅ KODEX 200 최근 일별 거래 (거래일: $tradeDate)")
        println("  - 거래일 개수: ${recentDailyList.size}개")

        // 응답 레코딩 (리스트 데이터는 SmartRecorder 사용)
        SmartRecorder.recordSmartly(
            data = recentDailyList,
            category = RecordingConfig.Paths.EtfPrice.RECENT_DAILY,
            fileName = "kodex200_recent_daily"
        )
    }

    @Test
    @DisplayName("[활용] 최근 10거래일 수익률을 계산할 수 있다")
    fun testRecentReturnCalculation() = integrationTest {
        // Given: TIGER 200 최근 일별 거래 조회 (고정 거래일)
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY
        val recentDailyList = client.funds.getRecentDaily(isin, tradeDate)

        assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

        // When: 최근 수익률 계산 (최근 일부터 오래된 일까지 역순)
        val newestClose = recentDailyList.first().closePrice.toDouble() // 최근 거래일
        val oldestClose = recentDailyList.last().closePrice.toDouble()  // 오래된 거래일
        val returnRate = if (oldestClose > 0) {
            ((newestClose - oldestClose) / oldestClose) * 100
        } else {
            0.0
        }

        // Then: 수익률 출력
        println("\n=== 최근 거래일 수익률 분석 ===")
        println("기간: ${recentDailyList.last().tradeDate} ~ ${recentDailyList.first().tradeDate}")
        println("시작가(구 거래일): ${oldestClose}원")
        println("종료가(신 거래일): ${newestClose}원")
        println("수익률: ${"%.2f".format(returnRate)}%")
    }

    @Test
    @DisplayName("[활용] 최근 10거래일 거래량 추이를 분석할 수 있다")
    fun testRecentVolumeAnalysis() = integrationTest {
        // Given: TIGER 200 최근 일별 거래 조회 (고정 거래일)
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY
        val recentDailyList = client.funds.getRecentDaily(isin, tradeDate)

        assertTrue(recentDailyList.isNotEmpty(), "데이터가 있어야 합니다")

        // When: 거래량 통계 계산
        val totalVolume = recentDailyList.sumOf { it.volume }
        val avgVolume = if (recentDailyList.isNotEmpty()) {
            totalVolume / recentDailyList.size
        } else {
            0L
        }
        val maxVolume = recentDailyList.maxOfOrNull { it.volume } ?: 0L
        val minVolume = recentDailyList.minOfOrNull { it.volume } ?: 0L

        // Then: 거래량 분석 출력
        println("\n=== 최근 10거래일 거래량 분석 ===")
        println("총 거래량: ${totalVolume}주")
        println("평균 일거래량: ${avgVolume}주")
        println("최대 일거래량: ${maxVolume}주")
        println("최소 일거래량: ${minVolume}주")
        println("분석 기간: ${recentDailyList.size}거래일")
    }
}
