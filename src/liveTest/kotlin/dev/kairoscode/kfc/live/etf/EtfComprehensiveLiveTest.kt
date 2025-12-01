package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 종합 정보 조회 Live Test
 *
 * getComprehensiveInfo() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfComprehensiveLiveTest : LiveTestBase() {

    @Test
    @DisplayName("TIGER 200 종합 정보를 조회할 수 있다")
    fun testGetComprehensiveInfoTiger200() = liveTest {
        // Given: TIGER 200 ISIN
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = LocalDate.now().minusDays(7) // 최근 거래일

        // When: 종합 정보 조회
        val info = client.etf.getComprehensiveInfo(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(info, "TIGER 200의 종합 정보가 반환되어야 합니다")

        // Then: OHLCV, NAV, 괴리율, 52주 고가/저가, 총 보수 등 포함
        assertTrue(info?.closePrice?.compareTo(java.math.BigDecimal.ZERO) == 1, "종가는 0보다 커야 합니다")
        assertTrue(info?.nav?.compareTo(java.math.BigDecimal.ZERO) == 1, "NAV는 0보다 커야 합니다")

        println("✅ TIGER 200 종합 정보")
        println("  - 종가: ${info?.closePrice}원")
        println("  - NAV: ${info?.nav}원")
        println("  - 괴리율: ${info?.divergenceRate}%")
        println("  - 52주 고가: ${info?.week52High}원")
        println("  - 52주 저가: ${info?.week52Low}원")

        // 응답 레코딩
        ResponseRecorder.record(
            data = info,
            category = RecordingConfig.Paths.Etf.COMPREHENSIVE,
            fileName = "tiger200_comprehensive"
        )
    }

    @Test
    @DisplayName("KODEX 200 종합 정보를 조회할 수 있다")
    fun testGetComprehensiveInfoKodex200() = liveTest {
        // Given: KODEX 200 ISIN
        val isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = LocalDate.now().minusDays(7)

        // When: 종합 정보 조회
        val info = client.etf.getComprehensiveInfo(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(info, "KODEX 200의 종합 정보가 반환되어야 합니다")

        println("✅ KODEX 200 종합 정보")
        println("  - 종가: ${info?.closePrice}원")
        println("  - NAV: ${info?.nav}원")
        println("  - 괴리율: ${info?.divergenceRate}%")

        // 응답 레코딩
        ResponseRecorder.record(
            data = info,
            category = RecordingConfig.Paths.Etf.COMPREHENSIVE,
            fileName = "kodex200_comprehensive"
        )
    }

    @Test
    @DisplayName("[활용] NAV 대비 괴리율을 확인할 수 있다")
    fun testDivergenceRateCalculation() = liveTest {
        // Given: TIGER 200 종합 정보 조회
        val isin = TestSymbols.TIGER_200_ISIN
        val info = client.etf.getComprehensiveInfo(isin)

        assertNotNull(info, "종합 정보가 반환되어야 합니다")

        // When: NAV와 종가 비교
        val calculatedDivergence = info?.let {
            ((it.closePrice.toDouble() - it.nav.toDouble()) / it.nav.toDouble()) * 100
        } ?: 0.0

        // Then: 괴리율 계산
        println("\n=== NAV 대비 괴리율 분석 ===")
        println("종가: ${info?.closePrice}원")
        println("NAV: ${info?.nav}원")
        println("괴리율(API): ${info?.divergenceRate}%")
        println("괴리율(계산): ${"%.2f".format(calculatedDivergence)}%")
    }

    @Test
    @DisplayName("[활용] 52주 고가/저가 대비 현재가 위치를 확인할 수 있다")
    fun testWeek52HighLowPosition() = liveTest {
        // Given: TIGER 200 종합 정보 조회
        val isin = TestSymbols.TIGER_200_ISIN
        val info = client.etf.getComprehensiveInfo(isin)

        assertNotNull(info, "종합 정보가 반환되어야 합니다")

        // When: 현재가와 52주 고/저가 비교
        val position = info?.let {
            val highLowRange = it.week52High.subtract(it.week52Low)
            if (highLowRange.compareTo(java.math.BigDecimal.ZERO) > 0) {
                it.closePrice.subtract(it.week52Low).divide(highLowRange, 4, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal("100")).toDouble()
            } else {
                50.0
            }
        } ?: 50.0

        // Then: 위치 출력
        println("\n=== 52주 고가/저가 대비 현재가 위치 ===")
        println("52주 고가: ${info?.week52High}원")
        println("현재가: ${info?.closePrice}원")
        println("52주 저가: ${info?.week52Low}원")
        println("위치: ${"%.1f".format(position)}% (0%=저가, 100%=고가)")
    }
}
