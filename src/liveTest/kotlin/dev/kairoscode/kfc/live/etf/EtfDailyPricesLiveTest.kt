package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * 전체 ETF 일별 시세 조회 Live Test
 *
 * getAllDailyPrices() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfDailyPricesLiveTest : LiveTestBase() {

    @Test
    @DisplayName("특정 날짜의 전체 ETF 시세를 조회할 수 있다")
    fun testGetAllDailyPrices() = liveTest {
        // Given: 최근 거래일 지정 (일주일 전)
        val date = LocalDate.now().minusDays(7)

        // When: 전체 ETF 시세 조회
        val dailyPrices = client.etf.getAllDailyPrices(date)

        // Then: 300개 이상 ETF 시세 반환
        assertTrue(dailyPrices.size >= 300, "ETF 시세는 최소 300개 이상이어야 합니다. 실제: ${dailyPrices.size}개")

        // Then: 각 시세는 필수 필드 포함
        dailyPrices.forEach { price ->
            assertTrue(price.ticker.isNotBlank(), "티커는 비어있지 않아야 합니다")
            assertTrue(price.closePrice > 0, "종가는 0보다 커야 합니다")
        }

        println("✅ 조회 날짜: $date")
        println("✅ 전체 ETF 시세 개수: ${dailyPrices.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = dailyPrices,
            category = RecordingConfig.Paths.Etf.DAILY_PRICES,
            fileName = "all_etf_daily_prices"
        )
    }

    @Test
    @DisplayName("[활용] 등락률 상위 ETF를 찾을 수 있다")
    fun testTopGainersAndLosers() = liveTest {
        // Given: 전체 ETF 일별 시세
        val date = LocalDate.now().minusDays(7)
        val dailyPrices = client.etf.getAllDailyPrices(date)

        // When: 등락률 기준 정렬
        val topGainers = dailyPrices
            .sortedByDescending { it.priceChangeRate }
            .take(10)

        val topLosers = dailyPrices
            .sortedBy { it.priceChangeRate }
            .take(10)

        // Then: 상위 10개 ETF 출력
        println("\n=== 등락률 상위 10개 ETF ===")
        topGainers.forEach { etf ->
            println("${etf.name}: ${etf.priceChangeRate}% (${etf.closePrice}원)")
        }

        println("\n=== 등락률 하위 10개 ETF ===")
        topLosers.forEach { etf ->
            println("${etf.name}: ${etf.priceChangeRate}% (${etf.closePrice}원)")
        }
    }

    @Test
    @DisplayName("[활용] 거래량 상위 ETF를 찾을 수 있다")
    fun testTopVolumeEtfs() = liveTest {
        // Given: 전체 ETF 일별 시세
        val date = LocalDate.now().minusDays(7)
        val dailyPrices = client.etf.getAllDailyPrices(date)

        // When: 거래량 기준 정렬
        val topVolume = dailyPrices
            .sortedByDescending { it.volume }
            .take(10)

        // Then: 상위 10개 ETF 출력
        println("\n=== 거래량 상위 10개 ETF ===")
        topVolume.forEach { etf ->
            println("${etf.name}: ${etf.volume}주")
        }
    }
}
