package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 포트폴리오 구성 조회 Live Test
 *
 * getPortfolio() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfPortfolioLiveTest : LiveTestBase() {

    @Test
    @DisplayName("ETF 바스켓 구성 종목을 조회할 수 있다")
    fun testGetPortfolio() = liveTest {
        // Given: TIGER 200 ISIN
        val isin = TestSymbols.TIGER_200_ISIN
        val date = LocalDate.now().minusDays(7)

        // When: 포트폴리오 구성 조회
        val portfolio = client.etf.getPortfolio(isin, date)

        // Then: 구성 종목 및 비중 반환
        assertTrue(portfolio.isNotEmpty(), "포트폴리오 구성 종목이 있어야 합니다")

        // Then: 비중 합계 확인 (허용 오차 범위 내)
        val totalWeight = portfolio.sumOf { it.weightPercent.toDouble() }
        assertTrue(abs(totalWeight - 100.0) <= 1.0, "비중 합계는 100% 근처여야 합니다. 실제: ${totalWeight}%")

        println("✅ 포트폴리오 구성 종목 개수: ${portfolio.size}")
        println("✅ 비중 합계: ${"%.2f".format(totalWeight)}%")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = portfolio,
            category = RecordingConfig.Paths.Etf.PORTFOLIO,
            fileName = "tiger200_portfolio"
        )
    }

    @Test
    @DisplayName("KODEX 200 포트폴리오를 조회할 수 있다")
    fun testGetPortfolioKodex200() = liveTest {
        // Given: KODEX 200 ISIN
        val isin = TestSymbols.KODEX_200_ISIN
        val date = LocalDate.now().minusDays(7)

        // When: 포트폴리오 구성 조회
        val portfolio = client.etf.getPortfolio(isin, date)

        // Then: 구성 종목 반환
        assertTrue(portfolio.isNotEmpty(), "포트폴리오 구성 종목이 있어야 합니다")

        println("✅ KODEX 200 구성 종목 개수: ${portfolio.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = portfolio,
            category = RecordingConfig.Paths.Etf.PORTFOLIO,
            fileName = "kodex200_portfolio"
        )
    }

    @Test
    @DisplayName("[활용] 상위 10개 구성 종목을 확인할 수 있다")
    fun testTop10Holdings() = liveTest {
        // Given: 포트폴리오 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val portfolio = client.etf.getPortfolio(isin)

        // When: 비중 기준 정렬
        val top10 = portfolio
            .sortedByDescending { it.weightPercent }
            .take(10)

        // Then: 상위 10개 종목 출력
        println("\n=== 상위 10개 구성 종목 ===")
        top10.forEachIndexed { index, constituent ->
            println("${index + 1}. ${constituent.constituentName}: ${"%.2f".format(constituent.weightPercent)}%")
        }
    }

    @Test
    @DisplayName("[활용] 특정 종목의 비중을 확인할 수 있다")
    fun testFindStockWeight() = liveTest {
        // Given: 포트폴리오 데이터
        val isin = TestSymbols.TIGER_200_ISIN
        val portfolio = client.etf.getPortfolio(isin)

        // When: 삼성전자 검색
        val samsung = portfolio.find { it.constituentName.contains("삼성전자") }

        // Then: 삼성전자 비중 출력
        if (samsung != null) {
            println("\n=== 특정 종목 비중 ===")
            println("${samsung.constituentName}: ${"%.2f".format(samsung.weightPercent)}%")
        } else {
            println("⚠️ 삼성전자가 포트폴리오에 없습니다.")
        }
    }
}
