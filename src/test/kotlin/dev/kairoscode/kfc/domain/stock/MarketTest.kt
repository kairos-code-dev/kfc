package dev.kairoscode.kfc.domain.stock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MarketTest {

    @Test
    fun `Market Enum 값 테스트`() {
        assertEquals("STK", Market.KOSPI.code)
        assertEquals("코스피", Market.KOSPI.koreanName)

        assertEquals("KSQ", Market.KOSDAQ.code)
        assertEquals("코스닥", Market.KOSDAQ.koreanName)

        assertEquals("KNX", Market.KONEX.code)
        assertEquals("코넥스", Market.KONEX.koreanName)

        assertEquals("ALL", Market.ALL.code)
        assertEquals("전체", Market.ALL.koreanName)
    }

    @Test
    fun `fromCode 메서드 테스트`() {
        assertEquals(Market.KOSPI, Market.fromCode("STK"))
        assertEquals(Market.KOSDAQ, Market.fromCode("KSQ"))
        assertEquals(Market.KONEX, Market.fromCode("KNX"))
        assertEquals(Market.ALL, Market.fromCode("ALL"))
        assertEquals(Market.ALL, Market.fromCode("UNKNOWN"))
    }
}
