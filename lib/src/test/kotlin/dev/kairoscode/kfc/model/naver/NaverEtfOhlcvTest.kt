package dev.kairoscode.kfc.model.naver

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset.offset
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode.*
import java.time.LocalDate

/**
 * NaverEtfOhlcv 데이터 모델 테스트
 */
class NaverEtfOhlcvTest {

    @Test
    fun `should create NaverEtfOhlcv with all fields`() {
        // === arrange & act ===
        val ohlcv = NaverEtfOhlcv(
            date = LocalDate.of(2024, 1, 15),
            open = BigDecimal("42800.50"),
            high = BigDecimal("43100.00"),
            low = BigDecimal("42700.25"),
            close = BigDecimal("42965.75"),
            volume = 192061L
        )

        // === assert ===
        assertThat(ohlcv.date).isEqualTo(LocalDate.of(2024, 1, 15))
        assertThat(ohlcv.close).isEqualTo(BigDecimal("42965.75"))
        assertThat(ohlcv.volume).isEqualTo(192061L)
    }

    @Test
    fun `should validate OHLC logic - high is highest price`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.open)
        assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.low)
        assertThat(ohlcv.high).isGreaterThanOrEqualTo(ohlcv.close)
    }

    @Test
    fun `should validate OHLC logic - low is lowest price`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.open)
        assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.high)
        assertThat(ohlcv.low).isLessThanOrEqualTo(ohlcv.close)
    }

    @Test
    fun `should have positive prices`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.open).isGreaterThan(BigDecimal.ZERO)
        assertThat(ohlcv.high).isGreaterThan(BigDecimal.ZERO)
        assertThat(ohlcv.low).isGreaterThan(BigDecimal.ZERO)
        assertThat(ohlcv.close).isGreaterThan(BigDecimal.ZERO)
    }

    @Test
    fun `should have non-negative volume`() {
        // === arrange ===
        val ohlcv = createSampleOhlcv()

        // === assert ===
        assertThat(ohlcv.volume).isGreaterThanOrEqualTo(0L)
    }

    @Test
    fun `should support decimal precision for adjusted prices`() {
        // === arrange & act ===
        val ohlcv = NaverEtfOhlcv(
            date = LocalDate.of(2024, 1, 15),
            open = BigDecimal("42800.123456"),
            high = BigDecimal("43100.987654"),
            low = BigDecimal("42700.555555"),
            close = BigDecimal("42965.111111"),
            volume = 192061L
        )

        // === assert ===
        // 조정주가는 고정밀 값 지원
        assertThat(ohlcv.open.scale()).isGreaterThanOrEqualTo(2)
        assertThat(ohlcv.close.scale()).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun `should calculate daily return`() {
        // === arrange ===
        val todayOhlcv = NaverEtfOhlcv(
            date = LocalDate.of(2024, 1, 15),
            open = BigDecimal("42800"),
            high = BigDecimal("43100"),
            low = BigDecimal("42700"),
            close = BigDecimal("42965"),
            volume = 192061L
        )

        val yesterdayOhlcv = NaverEtfOhlcv(
            date = LocalDate.of(2024, 1, 14),
            open = BigDecimal("42600"),
            high = BigDecimal("42900"),
            low = BigDecimal("42500"),
            close = BigDecimal("42800"),
            volume = 180000L
        )

        // === act ===
        // 일일 수익률 = ((오늘 종가 - 어제 종가) / 어제 종가) × 100
        // = ((42965 - 42800) / 42800) × 100 = (165 / 42800) × 100 = 0.3855...
        val dailyReturn = (todayOhlcv.close - yesterdayOhlcv.close)
            .divide(yesterdayOhlcv.close, 10, HALF_UP)
            .multiply(BigDecimal(100))
            .toDouble()

        // === assert ===
        // 정확한 계산값: 165 / 42800 * 100 = 0.3855140...
        assertThat(dailyReturn).isCloseTo(0.3855, offset(0.01))
    }

    @Test
    fun `should compare two OHLCVs by date`() {
        // === arrange ===
        val ohlcv1 = createSampleOhlcv()
        val ohlcv2 = createSampleOhlcv().copy(date = LocalDate.of(2024, 1, 16))

        // === act & assert ===
        assertThat(ohlcv1.date).isBefore(ohlcv2.date)
    }

    @Test
    fun `should handle zero volume`() {
        // === arrange & act ===
        val ohlcv = NaverEtfOhlcv(
            date = LocalDate.of(2024, 1, 15),
            open = BigDecimal("42800"),
            high = BigDecimal("42800"),
            low = BigDecimal("42800"),
            close = BigDecimal("42800"),
            volume = 0L // 거래 없음
        )

        // === assert ===
        assertThat(ohlcv.volume).isEqualTo(0L)
        assertThat(ohlcv.open).isEqualTo(ohlcv.close)
    }

    @Test
    fun `should create list of OHLCVs sorted by date`() {
        // === arrange ===
        val ohlcvList = listOf(
            NaverEtfOhlcv(
                date = LocalDate.of(2024, 1, 15),
                open = BigDecimal("42800"),
                high = BigDecimal("43100"),
                low = BigDecimal("42700"),
                close = BigDecimal("42965"),
                volume = 192061L
            ),
            NaverEtfOhlcv(
                date = LocalDate.of(2024, 1, 16),
                open = BigDecimal("43000"),
                high = BigDecimal("43200"),
                low = BigDecimal("42900"),
                close = BigDecimal("43050"),
                volume = 200000L
            ),
            NaverEtfOhlcv(
                date = LocalDate.of(2024, 1, 17),
                open = BigDecimal("43100"),
                high = BigDecimal("43400"),
                low = BigDecimal("43000"),
                close = BigDecimal("43250"),
                volume = 210000L
            )
        )

        // === act & assert ===
        for (i in 0 until ohlcvList.size - 1) {
            assertThat(ohlcvList[i].date).isBefore(ohlcvList[i + 1].date)
        }
    }

    // 헬퍼 함수
    private fun createSampleOhlcv() = NaverEtfOhlcv(
        date = LocalDate.of(2024, 1, 15),
        open = BigDecimal("42800.50"),
        high = BigDecimal("43100.00"),
        low = BigDecimal("42700.25"),
        close = BigDecimal("42965.75"),
        volume = 192061L
    )
}
