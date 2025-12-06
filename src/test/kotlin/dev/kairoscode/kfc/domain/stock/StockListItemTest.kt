package dev.kairoscode.kfc.domain.stock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StockListItemTest {
    @Test
    fun `StockListItem 생성 테스트`() {
        val stock =
            StockListItem(
                ticker = "005930",
                name = "삼성전자",
                isin = "KR7005930003",
                market = Market.KOSPI,
                listingStatus = ListingStatus.LISTED,
            )

        assertEquals("005930", stock.ticker)
        assertEquals("삼성전자", stock.name)
        assertEquals("KR7005930003", stock.isin)
        assertEquals(Market.KOSPI, stock.market)
        assertEquals(ListingStatus.LISTED, stock.listingStatus)
    }

    @Test
    fun `isKospi 메서드 테스트`() {
        val kospiStock =
            StockListItem(
                ticker = "005930",
                name = "삼성전자",
                isin = "KR7005930003",
                market = Market.KOSPI,
                listingStatus = ListingStatus.LISTED,
            )
        assertTrue(kospiStock.isKospi())
        assertFalse(kospiStock.isKosdaq())
    }

    @Test
    fun `isListed 메서드 테스트`() {
        val listedStock =
            StockListItem(
                ticker = "005930",
                name = "삼성전자",
                isin = "KR7005930003",
                market = Market.KOSPI,
                listingStatus = ListingStatus.LISTED,
            )
        assertTrue(listedStock.isListed())

        val delistedStock =
            StockListItem(
                ticker = "000020",
                name = "동화약품",
                isin = "KR7000020008",
                market = Market.KOSPI,
                listingStatus = ListingStatus.DELISTED,
            )
        assertFalse(delistedStock.isListed())
    }
}
