package dev.kairoscode.kfc.live.funds

import dev.kairoscode.kfc.utils.IntegrationTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.SmartRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 등락률 조회 Integration Test
 *
 * getPriceChanges() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfPriceChangesSpec : IntegrationTestBase() {

    @Test
    @DisplayName("1개월 기간별 등락률을 고정 날짜 기준으로 조회할 수 있다")
    fun testGetPriceChanges1Month() = integrationTest {
        // Given: 고정 기간 (1개월)
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.ONE_MONTH_AGO // 2024-10-25

        // When: 등락률 조회
        val priceChanges = client.funds.getPriceChanges(fromDate, toDate)

        // Then: 모든 ETF의 기간 등락률 반환
        assertTrue(priceChanges.size >= 300, "등락률 데이터는 최소 300개 이상이어야 합니다. 실제: ${priceChanges.size}개")

        println("✅ 1개월 등락률 데이터 개수: ${priceChanges.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩 (대용량 데이터 안전 처리)
        SmartRecorder.recordSmartly(
            data = priceChanges,
            category = RecordingConfig.Paths.EtfPrice.CHANGES,
            fileName = "price_changes_1month"
        )
    }

    @Test
    @DisplayName("1년 기간별 등락률을 고정 날짜 기준으로 조회할 수 있다")
    fun testGetPriceChanges1Year() = integrationTest {
        // Given: 고정 기간 (1년)
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.ONE_YEAR_AGO // 2023-11-25

        // When: 등락률 조회
        val priceChanges = client.funds.getPriceChanges(fromDate, toDate)

        // Then: 등락률 데이터 반환
        assertTrue(priceChanges.isNotEmpty(), "등락률 데이터가 반환되어야 합니다")

        println("✅ 1년 등락률 데이터 개수: ${priceChanges.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = priceChanges,
            category = RecordingConfig.Paths.EtfPrice.CHANGES,
            fileName = "price_changes_1year"
        )
    }

    @Test
    @DisplayName("[활용] 고정 기간 기준으로 수익률 상위 ETF를 찾을 수 있다")
    fun testTopPerformingEtfs() = integrationTest {
        // Given: 1개월 등락률 데이터 (고정 기간)
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.ONE_MONTH_AGO // 2024-10-25
        val priceChanges = client.funds.getPriceChanges(fromDate, toDate)

        // When: 등락률 기준 내림차순 정렬
        val topPerformers = priceChanges
            .sortedByDescending { it.changeRate }
            .take(20)

        // Then: 상위 20개 ETF 출력
        println("\n=== 1개월 수익률 상위 20개 ETF (기간: $fromDate ~ $toDate) ===")
        topPerformers.forEachIndexed { index, etf ->
            println("${index + 1}. ${etf.name}: ${etf.changeRate}%")
        }
    }

    @Test
    @DisplayName("[활용] 고정 기간 기준으로 등락률 분포를 분석할 수 있다")
    fun testPriceChangeDistribution() = integrationTest {
        // Given: 1개월 등락률 데이터 (고정 기간)
        val toDate = TestSymbols.TRADING_DAY // 2024-11-25
        val fromDate = TestSymbols.ONE_MONTH_AGO // 2024-10-25
        val priceChanges = client.funds.getPriceChanges(fromDate, toDate)

        // When: 등락률 구간별 개수 계산
        val distribution = mapOf(
            "5% 이상 상승" to priceChanges.count { it.changeRate >= 5.0 },
            "0~5% 상승" to priceChanges.count { it.changeRate in 0.0..5.0 },
            "0~5% 하락" to priceChanges.count { it.changeRate in -5.0..0.0 },
            "5% 이상 하락" to priceChanges.count { it.changeRate <= -5.0 }
        )

        // Then: 분포 출력
        println("\n=== 1개월 등락률 분포 (기간: $fromDate ~ $toDate) ===")
        distribution.forEach { (range, count) ->
            println("$range: ${count}개")
        }
    }
}
