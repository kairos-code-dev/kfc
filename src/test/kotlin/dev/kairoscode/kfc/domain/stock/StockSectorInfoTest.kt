package dev.kairoscode.kfc.domain.stock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StockSectorInfoTest {
    @Test
    fun `StockSectorInfo 생성 테스트`() {
        val sectorInfo =
            StockSectorInfo(
                ticker = "005930",
                name = "삼성전자",
                market = Market.KOSPI,
                industry = "전기전자",
                closePrice = 71500L,
                marketCap = 426789000000000L,
                priceChangeType = PriceChangeType.FALL,
            )

        assertEquals("005930", sectorInfo.ticker)
        assertEquals("삼성전자", sectorInfo.name)
        assertEquals("전기전자", sectorInfo.industry)
        assertEquals(71500L, sectorInfo.closePrice)
        assertEquals(426789000000000L, sectorInfo.marketCap)
        assertEquals(PriceChangeType.FALL, sectorInfo.priceChangeType)
    }

    @Test
    fun `calculateSectorWeight 메서드 테스트`() {
        // marketCap이 null인 경우에는 ZERO 반환
        val nullCapStock =
            StockSectorInfo(
                ticker = "000000",
                name = "테스트",
                market = Market.KOSPI,
                industry = "테스트",
                marketCap = null,
            )
        val weightNull = nullCapStock.calculateSectorWeight(1000000L)
        assertNotNull(weightNull)

        // totalMarketCap이 0인 경우에는 ZERO 반환
        val sectorInfo =
            StockSectorInfo(
                ticker = "005930",
                name = "삼성전자",
                market = Market.KOSPI,
                industry = "전기전자",
                marketCap = 100000L,
            )
        val weightZero = sectorInfo.calculateSectorWeight(0L)
        assertNotNull(weightZero)
    }

    @Test
    fun `isPriceRising 메서드 테스트`() {
        val risingStock =
            StockSectorInfo(
                ticker = "000660",
                name = "SK하이닉스",
                market = Market.KOSPI,
                industry = "전기전자",
                priceChangeType = PriceChangeType.RISE,
            )
        assertTrue(risingStock.isPriceRising())

        val fallingStock =
            StockSectorInfo(
                ticker = "005930",
                name = "삼성전자",
                market = Market.KOSPI,
                industry = "전기전자",
                priceChangeType = PriceChangeType.FALL,
            )
        assertFalse(fallingStock.isPriceRising())
        assertTrue(fallingStock.isPriceFalling())
    }

    @Test
    fun `List 확장 함수 - groupByIndustry 테스트`() {
        val stocks =
            listOf(
                StockSectorInfo("005930", "삼성전자", Market.KOSPI, "전기전자"),
                StockSectorInfo("000660", "SK하이닉스", Market.KOSPI, "전기전자"),
                StockSectorInfo("035420", "NAVER", Market.KOSPI, "서비스업"),
            )

        val grouped = stocks.groupByIndustry()
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["전기전자"]?.size)
        assertEquals(1, grouped["서비스업"]?.size)
    }

    @Test
    fun `List 확장 함수 - sortByMarketCap 테스트`() {
        val stocks =
            listOf(
                StockSectorInfo("005930", "삼성전자", Market.KOSPI, "전기전자", marketCap = 100000L),
                StockSectorInfo("000660", "SK하이닉스", Market.KOSPI, "전기전자", marketCap = 50000L),
                StockSectorInfo("035420", "NAVER", Market.KOSPI, "서비스업", marketCap = 75000L),
            )

        val sorted = stocks.sortByMarketCap(descending = true)
        assertEquals("005930", sorted[0].ticker)
        assertEquals("035420", sorted[1].ticker)
        assertEquals("000660", sorted[2].ticker)
    }

    @Test
    fun `List 확장 함수 - calculateTotalMarketCap 테스트`() {
        val stocks =
            listOf(
                StockSectorInfo("005930", "삼성전자", Market.KOSPI, "전기전자", marketCap = 100000L),
                StockSectorInfo("000660", "SK하이닉스", Market.KOSPI, "전기전자", marketCap = 50000L),
                StockSectorInfo("035420", "NAVER", Market.KOSPI, "서비스업", marketCap = 75000L),
            )

        val total = stocks.calculateTotalMarketCap()
        assertEquals(225000L, total)
    }
}
