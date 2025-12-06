package dev.kairoscode.kfc.domain.stock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IndustryClassificationTest {
    @Test
    fun `IndustryClassification 생성 테스트`() {
        val stocks =
            listOf(
                StockSectorInfo("005930", "삼성전자", Market.KOSPI, "전기전자", marketCap = 100000L),
                StockSectorInfo("000660", "SK하이닉스", Market.KOSPI, "전기전자", marketCap = 50000L),
            )

        val industry =
            IndustryClassification(
                industryName = "전기전자",
                market = Market.KOSPI,
                stocks = stocks,
                totalMarketCap = 150000L,
                stockCount = 2,
            )

        assertEquals("전기전자", industry.industryName)
        assertEquals(Market.KOSPI, industry.market)
        assertEquals(2, industry.stockCount)
        assertEquals(150000L, industry.totalMarketCap)
    }

    @Test
    fun `getTopNByMarketCap 메서드 테스트`() {
        val stocks =
            listOf(
                StockSectorInfo("005930", "삼성전자", Market.KOSPI, "전기전자", marketCap = 100000L),
                StockSectorInfo("000660", "SK하이닉스", Market.KOSPI, "전기전자", marketCap = 50000L),
                StockSectorInfo("066570", "LG전자", Market.KOSPI, "전기전자", marketCap = 30000L),
            )

        val industry =
            IndustryClassification(
                industryName = "전기전자",
                market = Market.KOSPI,
                stocks = stocks,
                totalMarketCap = 180000L,
                stockCount = 3,
            )

        val top2 = industry.getTopNByMarketCap(2)
        assertEquals(2, top2.size)
        assertEquals("005930", top2[0].ticker)
        assertEquals("000660", top2[1].ticker)
    }

    @Test
    fun `getAverageMarketCap 메서드 테스트`() {
        val stocks =
            listOf(
                StockSectorInfo("005930", "삼성전자", Market.KOSPI, "전기전자", marketCap = 100000L),
                StockSectorInfo("000660", "SK하이닉스", Market.KOSPI, "전기전자", marketCap = 50000L),
            )

        val industry =
            IndustryClassification(
                industryName = "전기전자",
                market = Market.KOSPI,
                stocks = stocks,
                totalMarketCap = 150000L,
                stockCount = 2,
            )

        assertEquals(75000L, industry.getAverageMarketCap())
    }
}
