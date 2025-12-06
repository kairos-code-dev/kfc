package dev.kairoscode.kfc.domain.stock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PriceChangeTypeTest {
    @Test
    fun `PriceChangeType Enum 값 테스트`() {
        assertEquals("1", PriceChangeType.RISE.code)
        assertEquals("2", PriceChangeType.FALL.code)
        assertEquals("3", PriceChangeType.UNCHANGED.code)
    }

    @Test
    fun `fromCode 메서드 테스트`() {
        assertEquals(PriceChangeType.RISE, PriceChangeType.fromCode("1"))
        assertEquals(PriceChangeType.FALL, PriceChangeType.fromCode("2"))
        assertEquals(PriceChangeType.UNCHANGED, PriceChangeType.fromCode("3"))
        assertNull(PriceChangeType.fromCode("UNKNOWN"))
    }
}
