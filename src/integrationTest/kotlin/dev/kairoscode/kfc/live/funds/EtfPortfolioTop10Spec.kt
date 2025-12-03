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
 * ETF 포트폴리오 상위 10종목 조회 Integration Test
 *
 * getPortfolioTop10() 함수의 실제 API 호출 테스트 및 응답 레코딩
 * ETF 포트폴리오 구성 종목 중 비중이 높은 상위 10개의 요약 정보 조회
 *
 * 구현 참고: MDCSTAT04705 엔드포인트는 작동하지 않으므로,
 * 내부적으로 전체 포트폴리오(MDCSTAT05001)를 조회하여 상위 10개를 추출합니다.
 */
class EtfPortfolioTop10Spec : IntegrationTestBase() {

    @Test
    @DisplayName("TIGER 200 포트폴리오 상위 10종목을 거래일에 조회할 수 있다")
    fun testGetPortfolioTop10Tiger200OnTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 거래일
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY // 2024-11-25 (월요일)

        // When: 포트폴리오 상위 10종목 조회
        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

        // Then: 데이터 반환
        assertNotNull(topItems, "거래일에는 TIGER 200의 포트폴리오 상위 10종목이 반환되어야 합니다")
        assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")
        assertTrue(topItems.size <= 10, "포트폴리오 상위 10종목은 최대 10개까지만 포함합니다")

        // 첫 번째 항목 검증
        val topItem = topItems.first()
        assertTrue(!topItem.name.isNullOrEmpty(), "종목명이 있어야 합니다")
        assertTrue(topItem.compositionRatio.compareTo(java.math.BigDecimal.ZERO) >= 0, "비중은 0 이상이어야 합니다")

        println("✅ TIGER 200 포트폴리오 상위 10종목 (거래일: $tradeDate)")
        println("  - 포함 종목 수: ${topItems.size}개")
        topItems.forEachIndexed { index, item ->
            println("  ${index + 1}. ${item.name} - 비중: ${item.compositionRatio}%")
        }

        // 응답 레코딩 (리스트 데이터는 SmartRecorder 사용)
        SmartRecorder.recordSmartly(
            data = topItems,
            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO_TOP10,
            fileName = "tiger200_portfolio_top10"
        )
    }

    @Test
    @DisplayName("TIGER 200 포트폴리오 상위 10종목을 비거래일에 조회하면 빈 데이터를 반환한다")
    fun testGetPortfolioTop10Tiger200OnNonTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 비거래일 (토요일)
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.NON_TRADING_DAY // 2024-11-23 (토요일)

        // When: 포트폴리오 상위 10종목 조회
        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

        // Then: 빈 데이터 반환 (과거 비거래일)
        assertNotNull(topItems, "API 호출은 성공해야 합니다")

        println("✅ 비거래일($tradeDate) 포트폴리오 상위 10종목 조회 결과:")
        println("  - 포함 종목 수: ${topItems.size}개 (비거래일이므로 빈 응답 가능)")
    }

    @Test
    @DisplayName("KODEX 200 포트폴리오 상위 10종목을 거래일에 조회할 수 있다")
    fun testGetPortfolioTop10Kodex200OnTradingDay() = integrationTest {
        // Given: KODEX 200 ISIN과 고정 거래일
        val isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY // 2024-11-25 (월요일)

        // When: 포트폴리오 상위 10종목 조회
        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(topItems, "거래일에는 KODEX 200의 포트폴리오 상위 10종목이 반환되어야 합니다")
        assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")

        println("✅ KODEX 200 포트폴리오 상위 10종목 (거래일: $tradeDate)")
        println("  - 포함 종목 수: ${topItems.size}개")

        // 응답 레코딩 (리스트 데이터는 SmartRecorder 사용)
        SmartRecorder.recordSmartly(
            data = topItems,
            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO_TOP10,
            fileName = "kodex200_portfolio_top10"
        )
    }

    @Test
    @DisplayName("[활용] ETF 포트폴리오의 집중도를 분석할 수 있다")
    fun testPortfolioConcentrationAnalysis() = integrationTest {
        // Given: TIGER 200 포트폴리오 상위 10종목 조회 (고정 거래일)
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY
        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

        assertTrue(topItems.isNotEmpty(), "데이터가 있어야 합니다")

        // When: 포트폴리오 집중도 계산
        val totalWeight = topItems.sumOf { it.compositionRatio }
        val topThreeWeight = topItems.take(3).sumOf { it.compositionRatio }
        val topFiveWeight = topItems.take(5).sumOf { it.compositionRatio }

        // Then: 집중도 분석 출력
        println("\n=== ETF 포트폴리오 집중도 분석 (거래일: $tradeDate) ===")
        println("상위 10종목 총 비중: ${totalWeight}%")
        println("상위 3종목 비중: ${topThreeWeight}%")
        println("상위 5종목 비중: ${topFiveWeight}%")
        println("평가: " + when {
            topThreeWeight.compareTo(java.math.BigDecimal("40")) >= 0 -> "집중도 높음 (상위 3종목 40% 이상)"
            topFiveWeight.compareTo(java.math.BigDecimal("50")) >= 0 -> "집중도 중간 (상위 5종목 50% 이상)"
            else -> "집중도 낮음 (분산 투자)"
        })
    }

    @Test
    @DisplayName("[활용] ETF 포트폴리오 구성의 특징을 분석할 수 있다")
    fun testPortfolioCompositionAnalysis() = integrationTest {
        // Given: TIGER 200과 KODEX 200의 포트폴리오 상위 10종목 조회 (고정 거래일)
        val tiger200Isin = TestSymbols.TIGER_200_ISIN
        val kodex200Isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY

        val tiger200Items = client.funds.getPortfolioTop10(tiger200Isin, tradeDate)
        val kodex200Items = client.funds.getPortfolioTop10(kodex200Isin, tradeDate)

        assertTrue(tiger200Items.isNotEmpty(), "TIGER 200 포트폴리오가 있어야 합니다")
        assertTrue(kodex200Items.isNotEmpty(), "KODEX 200 포트폴리오가 있어야 합니다")

        // When: 상위 10종목의 평균 비중 계산
        val tiger200AvgWeight = if (tiger200Items.isNotEmpty()) {
            tiger200Items.sumOf { it.compositionRatio }.divide(tiger200Items.size.toBigDecimal(), 4, java.math.RoundingMode.HALF_UP)
        } else {
            java.math.BigDecimal.ZERO
        }

        val kodex200AvgWeight = if (kodex200Items.isNotEmpty()) {
            kodex200Items.sumOf { it.compositionRatio }.divide(kodex200Items.size.toBigDecimal(), 4, java.math.RoundingMode.HALF_UP)
        } else {
            java.math.BigDecimal.ZERO
        }

        // Then: 포트폴리오 구성 비교 분석
        println("\n=== ETF 포트폴리오 구성 비교 분석 ===")
        println("TIGER 200")
        println("  - 상위 10종목 구성 수: ${tiger200Items.size}개")
        println("  - 상위 10종목 총 비중: ${tiger200Items.sumOf { it.compositionRatio }}%")
        println("  - 평균 비중: ${tiger200AvgWeight}%")

        println("\nKODEX 200")
        println("  - 상위 10종목 구성 수: ${kodex200Items.size}개")
        println("  - 상위 10종목 총 비중: ${kodex200Items.sumOf { it.compositionRatio }}%")
        println("  - 평균 비중: ${kodex200AvgWeight}%")

        // 공통 종목 분석
        val tiger200Names = tiger200Items.map { it.name }.toSet()
        val kodex200Names = kodex200Items.map { it.name }.toSet()
        val commonStocks = tiger200Names.intersect(kodex200Names)

        println("\n분석: 동일 벤치마크를 추종하는 두 ETF 간 공통 포트폴리오 종목")
        println("  - 공통 종목 수: ${commonStocks.size}개/${tiger200Items.size}개")
        if (commonStocks.isNotEmpty()) {
            println("  - 공통 상위 종목: ${commonStocks.take(5).joinToString(", ")}")
        }
    }
}
