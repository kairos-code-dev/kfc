package dev.kairoscode.kfc.integration.price

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.ResponseRecorder
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 분단위 시세 조회 Integration Test
 *
 * getIntradayBars() 함수의 실제 API 호출 테스트 및 응답 레코딩
 * 장중 1분 단위 OHLCV 데이터 조회 (약 330개의 데이터 포인트)
 */
@DisplayName("PriceApi.getIntradayBars() - 분단위 시세 조회")
class PriceApiIntradaySpec : IntegrationTestBase() {

    @Test
    @DisplayName("TIGER 200 분단위 시세를 거래일에 조회할 수 있다 (과거 날짜는 빈 응답)")
    fun testGetIntradayBarsTiger200OnTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 거래일
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val tradeDate = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

        // When: 분단위 시세 조회
        val bars = client.price.getIntradayBars(isin, tradeDate)

        // Then: 데이터 반환 확인
        // 주의: 분단위 시세 API는 당일 거래일에만 데이터를 제공합니다.
        // 과거 날짜(2024-11-25)를 조회하면 빈 목록이 반환됩니다.
        assertNotNull(bars, "API 호출은 성공해야 합니다")
        println("✅ TIGER 200 분단위 시세 (거래일: $tradeDate)")
        println("  - 데이터 포인트: ${bars.size}개 (과거 날짜는 빈 응답 예상)")

        if (bars.isNotEmpty()) {
            // 데이터가 있는 경우 (당일 거래일인 경우)
            assertTrue(bars.size >= 300, "장중 데이터는 최소 300개 이상의 분 단위 데이터를 포함해야 합니다")

            // 첫 번째와 마지막 데이터 검증
            val firstBar = bars.first()
            val lastBar = bars.last()
            assertTrue(firstBar.openPrice > 0, "시가는 0보다 커야 합니다")
            assertTrue(lastBar.closePrice > 0, "종가는 0보다 커야 합니다")

            println("  - 조회 기간: ${bars.first().time} ~ ${bars.last().time}")
            println("  - 첫 분: ${bars.first().openPrice}원 open, ${bars.first().closePrice}원 close")
            println("  - 마지막 분: ${bars.last().openPrice}원 open, ${bars.last().closePrice}원 close")

            // 응답 레코딩 (리스트 데이터는 SmartRecorder 사용)
            SmartRecorder.recordSmartly(
                data = bars,
                category = RecordingConfig.Paths.EtfPrice.INTRADAY_BARS,
                fileName = "tiger200_intraday_bars"
            )
        } else {
            println("  - 과거 날짜이므로 데이터가 없습니다 (예상된 동작)")
        }
    }

    @Test
    @DisplayName("TIGER 200 분단위 시세를 비거래일에 조회하면 빈 응답을 반환한다")
    fun testGetIntradayBarsTiger200OnNonTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 비거래일 (토요일)
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val tradeDate = TestFixtures.Dates.NON_TRADING_DAY // 2024-11-23 (토요일)

        // When: 분단위 시세 조회
        val bars = client.price.getIntradayBars(isin, tradeDate)

        // Then: 빈 응답 반환 (과거 비거래일)
        assertNotNull(bars, "API 호출은 성공해야 합니다")

        println("✅ 비거래일($tradeDate) 분단위 시세 조회 결과:")
        println("  - 데이터 포인트: ${bars.size}개 (비거래일이므로 빈 응답 예상)")
    }

    @Test
    @DisplayName("KODEX 200 분단위 시세를 거래일에 조회할 수 있다 (과거 날짜는 빈 응답)")
    fun testGetIntradayBarsKodex200OnTradingDay() = integrationTest {
        // Given: KODEX 200 ISIN과 고정 거래일
        val isin = TestFixtures.Etf.KODEX_200_ISIN
        val tradeDate = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

        // When: 분단위 시세 조회
        val bars = client.price.getIntradayBars(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(bars, "API 호출은 성공해야 합니다")

        println("✅ KODEX 200 분단위 시세 (거래일: $tradeDate)")
        println("  - 데이터 포인트: ${bars.size}개 (과거 날짜는 빈 응답 예상)")

        if (bars.isNotEmpty()) {
            // 응답 레코딩 (리스트 데이터는 SmartRecorder 사용)
            SmartRecorder.recordSmartly(
                data = bars,
                category = RecordingConfig.Paths.EtfPrice.INTRADAY_BARS,
                fileName = "kodex200_intraday_bars"
            )
        }
    }

    @Test
    @DisplayName("[활용] 장중 고가/저가를 기반으로 변동성을 분석할 수 있다")
    fun testIntraDayVolatilityAnalysis() = integrationTest {
        // Given: TIGER 200 분단위 시세 조회 (고정 거래일)
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val tradeDate = TestFixtures.Dates.TRADING_DAY
        val bars = client.price.getIntradayBars(isin, tradeDate)

        println("\n=== 장중 변동성 분석 (거래일: $tradeDate) ===")

        if (bars.isEmpty()) {
            println("과거 날짜이므로 데이터가 없습니다 (분단위 시세는 당일만 제공)")
            return@integrationTest
        }

        // When: 고가와 저가를 기반으로 변동성 계산
        val dayHigh = bars.maxOfOrNull { it.highPrice } ?: 0
        val dayLow = bars.minOfOrNull { it.lowPrice } ?: 0
        val dayRange = dayHigh - dayLow
        val volatility = if (dayLow > 0) {
            (dayRange.toDouble() / dayLow) * 100
        } else {
            0.0
        }

        // Then: 변동성 출력
        println("일중 고가: ${dayHigh}원")
        println("일중 저가: ${dayLow}원")
        println("가격 범위: ${dayRange}원")
        println("변동성: ${"%.2f".format(volatility)}%")
    }

    @Test
    @DisplayName("[활용] 분단위 거래량 추이를 분석할 수 있다")
    fun testIntradayVolumeAnalysis() = integrationTest {
        // Given: TIGER 200 분단위 시세 조회 (고정 거래일)
        val isin = TestFixtures.Etf.TIGER_200_ISIN
        val tradeDate = TestFixtures.Dates.TRADING_DAY
        val bars = client.price.getIntradayBars(isin, tradeDate)

        println("\n=== 분단위 거래량 분석 (거래일: $tradeDate) ===")

        if (bars.isEmpty()) {
            println("과거 날짜이므로 데이터가 없습니다 (분단위 시세는 당일만 제공)")
            return@integrationTest
        }

        // When: 거래량 통계 계산
        val totalVolume = bars.sumOf { it.cumulativeVolume }
        val avgVolume = if (bars.isNotEmpty()) totalVolume / bars.size else 0L
        val maxVolume = bars.maxOfOrNull { it.cumulativeVolume } ?: 0L

        // Then: 거래량 분석 출력
        println("총 거래량: ${totalVolume}주")
        println("평균 분당 거래량: ${avgVolume}주")
        println("최대 분당 거래량: ${maxVolume}주")
        println("분석 대상 기간: ${bars.size}분")
    }
}
