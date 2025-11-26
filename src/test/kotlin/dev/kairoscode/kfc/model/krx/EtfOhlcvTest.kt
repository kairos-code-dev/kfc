package dev.kairoscode.kfc.model.krx

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * EtfOhlcv 데이터 모델 테스트
 */
class EtfOhlcvTest {

    @Test
    fun `should create EtfOhlcv with all fields`() {
        // === arrange & act ===
        val ohlcv = EtfOhlcv(
            tradeDate = LocalDate.of(2024, 1, 15),
            openPrice = 42800,
            highPrice = 43100,
            lowPrice = 42700,
            closePrice = 42965,
            volume = 192061L,
            tradingValue = 8222510755L,
            nav = BigDecimal("43079.14"),
            priceChange = 165,
            priceChangeRate = 0.39,
            priceDirection = Direction.UP,
            marketCap = 850707000000L,
            netAsset = 850707000000L,
            listedShares = 19800000L,
            indexName = "KOSPI 200",
            indexValue = BigDecimal("355.07"),
            indexChange = BigDecimal("1.32"),
            indexChangeRate = 0.37,
            indexDirection = Direction.UP
        )

        // === assert ===
        assertThat(ohlcv.tradeDate).isEqualTo(LocalDate.of(2024, 1, 15))
        assertThat(ohlcv.closePrice).isEqualTo(42965)
        assertThat(ohlcv.nav).isEqualTo(BigDecimal("43079.14"))
    }

    @Test
    fun `should validate OHLC logic - high is highest price`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.highPrice).isGreaterThanOrEqualTo(ohlcv.openPrice)
        assertThat(ohlcv.highPrice).isGreaterThanOrEqualTo(ohlcv.lowPrice)
        assertThat(ohlcv.highPrice).isGreaterThanOrEqualTo(ohlcv.closePrice)
    }

    @Test
    fun `should validate OHLC logic - low is lowest price`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.lowPrice).isLessThanOrEqualTo(ohlcv.openPrice)
        assertThat(ohlcv.lowPrice).isLessThanOrEqualTo(ohlcv.highPrice)
        assertThat(ohlcv.lowPrice).isLessThanOrEqualTo(ohlcv.closePrice)
    }

    @Test
    fun `should have positive prices`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.openPrice).isGreaterThan(0)
        assertThat(ohlcv.highPrice).isGreaterThan(0)
        assertThat(ohlcv.lowPrice).isGreaterThan(0)
        assertThat(ohlcv.closePrice).isGreaterThan(0)
    }

    @Test
    fun `should have non-negative volume`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.volume).isGreaterThanOrEqualTo(0L)
    }

    @Test
    fun `should have positive NAV`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.nav).isGreaterThan(BigDecimal.ZERO)
    }

    @Test
    fun `should calculate price change rate correctly`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()
        val previousClosePrice = ohlcv.closePrice - ohlcv.priceChange

        // === act ===
        val expectedRate = (ohlcv.priceChange.toDouble() / previousClosePrice) * 100

        // === assert ===
        assertThat(ohlcv.priceChangeRate).isCloseTo(expectedRate, org.assertj.core.data.Offset.offset(0.01))
    }

    @Test
    fun `should handle price direction UP`() {
        // === arrange & act ===
        val ohlcv = EtfOhlcv(
            tradeDate = LocalDate.of(2024, 1, 15),
            openPrice = 42800,
            highPrice = 43100,
            lowPrice = 42700,
            closePrice = 42965,
            volume = 192061L,
            tradingValue = 8222510755L,
            nav = BigDecimal("43079.14"),
            priceChange = 165,
            priceChangeRate = 0.39,
            priceDirection = Direction.UP,
            marketCap = 850707000000L,
            netAsset = 850707000000L,
            listedShares = 19800000L,
            indexName = "KOSPI 200",
            indexValue = BigDecimal("355.07"),
            indexChange = BigDecimal("1.32"),
            indexChangeRate = 0.37,
            indexDirection = Direction.UP
        )

        // === assert ===
        assertThat(ohlcv.priceDirection).isEqualTo(Direction.UP)
        assertThat(ohlcv.priceChange).isGreaterThan(0)
    }

    @Test
    fun `should handle price direction DOWN`() {
        // === arrange & act ===
        val ohlcv = EtfOhlcv(
            tradeDate = LocalDate.of(2024, 1, 15),
            openPrice = 42800,
            highPrice = 43100,
            lowPrice = 42700,
            closePrice = 42800,
            volume = 192061L,
            tradingValue = 8222510755L,
            nav = BigDecimal("43079.14"),
            priceChange = -200,
            priceChangeRate = -0.47,
            priceDirection = Direction.DOWN,
            marketCap = 850707000000L,
            netAsset = 850707000000L,
            listedShares = 19800000L,
            indexName = "KOSPI 200",
            indexValue = BigDecimal("355.07"),
            indexChange = BigDecimal("-1.50"),
            indexChangeRate = -0.42,
            indexDirection = Direction.DOWN
        )

        // === assert ===
        assertThat(ohlcv.priceDirection).isEqualTo(Direction.DOWN)
        assertThat(ohlcv.priceChange).isLessThan(0)
    }

    @Test
    fun `should handle price direction UNCHANGED`() {
        // === arrange & act ===
        val ohlcv = EtfOhlcv(
            tradeDate = LocalDate.of(2024, 1, 15),
            openPrice = 42800,
            highPrice = 43100,
            lowPrice = 42700,
            closePrice = 42800,
            volume = 192061L,
            tradingValue = 8222510755L,
            nav = BigDecimal("43079.14"),
            priceChange = 0,
            priceChangeRate = 0.0,
            priceDirection = Direction.UNCHANGED,
            marketCap = 850707000000L,
            netAsset = 850707000000L,
            listedShares = 19800000L,
            indexName = "KOSPI 200",
            indexValue = BigDecimal("355.07"),
            indexChange = BigDecimal.ZERO,
            indexChangeRate = 0.0,
            indexDirection = Direction.UNCHANGED
        )

        // === assert ===
        assertThat(ohlcv.priceDirection).isEqualTo(Direction.UNCHANGED)
        assertThat(ohlcv.priceChange).isEqualTo(0)
    }

    @Test
    fun `should compare two OHLCVs by date`() {
        // === arrange ===
        val ohlcv1 = createSampleOhlcv()
        val ohlcv2 = createSampleOhlcv().copy(tradeDate = LocalDate.of(2024, 1, 16))

        // === act & assert ===
        assertThat(ohlcv1.tradeDate).isBefore(ohlcv2.tradeDate)
    }

    // 헬퍼 함수
    private fun createSampleOhlcv() = EtfOhlcv(
        tradeDate = LocalDate.of(2024, 1, 15),
        openPrice = 42800,
        highPrice = 43100,
        lowPrice = 42700,
        closePrice = 42965,
        volume = 192061L,
        tradingValue = 8222510755L,
        nav = BigDecimal("43079.14"),
        priceChange = 165,
        priceChangeRate = 0.39,
        priceDirection = Direction.UP,
        marketCap = 850707000000L,
        netAsset = 850707000000L,
        listedShares = 19800000L,
        indexName = "KOSPI 200",
        indexValue = BigDecimal("355.07"),
        indexChange = BigDecimal("1.32"),
        indexChangeRate = 0.37,
        indexDirection = Direction.UP
    )
}
