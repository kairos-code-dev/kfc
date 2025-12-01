package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 등락률 조회 Live Test
 *
 * getPriceChanges() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfPriceChangesLiveTest : LiveTestBase() {

    @Test
    @DisplayName("1개월 기간별 등락률을 조회할 수 있다")
    fun testGetPriceChanges1Month() = liveTest {
        // Given: 시작일과 종료일 지정 (1개월)
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)

        // When: 등락률 조회
        val priceChanges = client.etf.getPriceChanges(fromDate, toDate)

        // Then: 모든 ETF의 기간 등락률 반환
        assertTrue(priceChanges.size >= 300, "등락률 데이터는 최소 300개 이상이어야 합니다. 실제: ${priceChanges.size}개")

        println("✅ 1개월 등락률 데이터 개수: ${priceChanges.size}")
        println("✅ 기간: $fromDate ~ $toDate")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = priceChanges,
            category = RecordingConfig.Paths.Etf.PRICE_CHANGES,
            fileName = "price_changes_1month"
        )
    }

    @Test
    @DisplayName("1년 기간별 등락률을 조회할 수 있다")
    fun testGetPriceChanges1Year() = liveTest {
        // Given: 시작일과 종료일 지정 (1년)
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusYears(1)

        // When: 등락률 조회
        val priceChanges = client.etf.getPriceChanges(fromDate, toDate)

        // Then: 등락률 데이터 반환
        assertTrue(priceChanges.isNotEmpty(), "등락률 데이터가 반환되어야 합니다")

        println("✅ 1년 등락률 데이터 개수: ${priceChanges.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = priceChanges,
            category = RecordingConfig.Paths.Etf.PRICE_CHANGES,
            fileName = "price_changes_1year"
        )
    }

    @Test
    @DisplayName("[활용] 수익률 상위 ETF를 찾을 수 있다")
    fun testTopPerformingEtfs() = liveTest {
        // Given: 1개월 등락률 데이터
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)
        val priceChanges = client.etf.getPriceChanges(fromDate, toDate)

        // When: 등락률 기준 내림차순 정렬
        val topPerformers = priceChanges
            .sortedByDescending { it.changeRate }
            .take(20)

        // Then: 상위 20개 ETF 출력
        println("\n=== 1개월 수익률 상위 20개 ETF ===")
        topPerformers.forEachIndexed { index, etf ->
            println("${index + 1}. ${etf.name}: ${etf.changeRate}%")
        }
    }

    @Test
    @DisplayName("[활용] 등락률 분포를 분석할 수 있다")
    fun testPriceChangeDistribution() = liveTest {
        // Given: 1개월 등락률 데이터
        val toDate = LocalDate.now().minusDays(7)
        val fromDate = toDate.minusMonths(1)
        val priceChanges = client.etf.getPriceChanges(fromDate, toDate)

        // When: 등락률 구간별 개수 계산
        val distribution = mapOf(
            "5% 이상 상승" to priceChanges.count { it.changeRate >= 5.0 },
            "0~5% 상승" to priceChanges.count { it.changeRate in 0.0..5.0 },
            "0~5% 하락" to priceChanges.count { it.changeRate in -5.0..0.0 },
            "5% 이상 하락" to priceChanges.count { it.changeRate <= -5.0 }
        )

        // Then: 분포 출력
        println("\n=== 1개월 등락률 분포 ===")
        distribution.forEach { (range, count) ->
            println("$range: ${count}개")
        }
    }
}
