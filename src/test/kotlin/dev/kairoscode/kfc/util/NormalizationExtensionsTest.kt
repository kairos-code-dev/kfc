package dev.kairoscode.kfc.util

import dev.kairoscode.kfc.model.krx.Direction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDate

class NormalizationExtensionsTest {

    // toKrxPrice 테스트
    @Test
    fun `toKrxPrice - 정상 값`() {
        assertEquals(BigDecimal("42965.00"), "42,965".toKrxPrice())
        assertEquals(BigDecimal("1080.00"), "1,080".toKrxPrice())
        assertEquals(BigDecimal("0.00"), "0".toKrxPrice())
        assertEquals(BigDecimal("43250.50"), "43,250.50".toKrxPrice())
    }

    @Test
    fun `toKrxPrice - 특수 값`() {
        assertEquals(BigDecimal.ZERO.setScale(2), "-".toKrxPrice())
        assertEquals(BigDecimal.ZERO.setScale(2), "".toKrxPrice())
        assertEquals(BigDecimal.ZERO.setScale(2), "  ".toKrxPrice())
    }

    @Test
    fun `toKrxPrice - 스케일 2자리`() {
        val result = "42965".toKrxPrice()
        assertEquals(2, result.scale())
        assertEquals(BigDecimal("42965.00"), result)
    }

    // toKrxAmount 테스트
    @Test
    fun `toKrxAmount - 대용량 값`() {
        assertEquals(BigDecimal("850707000000"), "850,707,000,000".toKrxAmount())
        assertEquals(BigDecimal("19800000"), "19,800,000".toKrxAmount())
        assertEquals(BigDecimal("8222510755"), "8,222,510,755".toKrxAmount())
    }

    @Test
    fun `toKrxAmount - 특수 값`() {
        assertEquals(BigDecimal.ZERO, "-".toKrxAmount())
        assertEquals(BigDecimal.ZERO, "".toKrxAmount())
    }

    @Test
    fun `toKrxAmount - 스케일 0자리`() {
        val result = "850707000000".toKrxAmount()
        assertEquals(0, result.scale())
    }

    // toKrxRate 테스트
    @Test
    fun `toKrxRate - 소수점 및 스케일`() {
        assertEquals(BigDecimal("2.5800"), "2.58".toKrxRate())
        assertEquals(BigDecimal("-1.2300"), "-1.23".toKrxRate())
        assertEquals(BigDecimal("0.1500"), "0.15".toKrxRate())
    }

    @Test
    fun `toKrxRate - 음수 부호 유지`() {
        val result = "-1.23".toKrxRate()
        assertTrue(result < BigDecimal.ZERO)
        assertEquals(BigDecimal("-1.2300"), result)
    }

    @Test
    fun `toKrxRate - 특수 값`() {
        assertEquals(BigDecimal("0.0000"), "-".toKrxRate())
        assertEquals(BigDecimal("0.0000"), "".toKrxRate())
    }

    @Test
    fun `toKrxRate - 스케일 4자리`() {
        val result = "2.58".toKrxRate()
        assertEquals(4, result.scale())
    }

    // toKrxBigDecimal 테스트
    @Test
    fun `toKrxBigDecimal - 고정밀 값`() {
        assertEquals(BigDecimal("43079.14"), "43,079.14".toKrxBigDecimal())
        assertEquals(BigDecimal("421.35"), "421.35".toKrxBigDecimal())
        assertEquals(BigDecimal("0.15"), "0.15".toKrxBigDecimal())
    }

    @Test
    fun `toKrxBigDecimal - 특수 값`() {
        assertEquals(BigDecimal.ZERO, "-".toKrxBigDecimal())
        assertEquals(BigDecimal.ZERO, "".toKrxBigDecimal())
    }

    @Test
    fun `toKrxBigDecimal - 원본 정밀도 유지`() {
        val result1 = "43079.14".toKrxBigDecimal()
        assertEquals(BigDecimal("43079.14"), result1)
        // 스케일 지정 없음 - 원본 유지
    }

    // toKrxLong 테스트
    @Test
    fun `toKrxLong - 대용량 값`() {
        assertEquals(850707000000L, "850,707,000,000".toKrxLong())
        assertEquals(19800000L, "19,800,000".toKrxLong())
        assertEquals(192061L, "192,061".toKrxLong())
    }

    @Test
    fun `toKrxLong - 특수 값`() {
        assertEquals(0L, "-".toKrxLong())
        assertEquals(0L, "".toKrxLong())
        assertEquals(0L, "  ".toKrxLong())
    }

    @Test
    fun `toKrxLong - 음수는 변환되지 않음 (항상 양수)`() {
        // toKrxLong은 음수 부호를 제거하지 않지만, 음수 값도 그대로 변환함
        // 실제로는 항상 양수가 들어온다고 가정
        assertEquals(0L, "-".toKrxLong())
    }

    // toKrxSignedLong 테스트
    @Test
    fun `toKrxSignedLong - 양수 값`() {
        assertEquals(1234567L, "1,234,567".toKrxSignedLong())
        assertEquals(123L, "123".toKrxSignedLong())
    }

    @Test
    fun `toKrxSignedLong - 음수 값 유지`() {
        assertEquals(-1234567L, "-1,234,567".toKrxSignedLong())
        assertEquals(-123L, "-123".toKrxSignedLong())
    }

    @Test
    fun `toKrxSignedLong - 특수 값`() {
        assertEquals(0L, "-".toKrxSignedLong())
        assertEquals(0L, "".toKrxSignedLong())
        assertEquals(0L, "  ".toKrxSignedLong())
    }

    // toKrxDate 테스트
    @Test
    fun `toKrxDate - Slash 형식`() {
        assertEquals(LocalDate.of(2024, 1, 2), "2024/01/02".toKrxDate())
        assertEquals(LocalDate.of(2023, 12, 31), "2023/12/31".toKrxDate())
    }

    @Test
    fun `toKrxDate - Compact 형식`() {
        assertEquals(LocalDate.of(2024, 1, 2), "20240102".toKrxDate())
        assertEquals(LocalDate.of(2023, 12, 31), "20231231".toKrxDate())
    }

    @Test
    fun `toKrxDate - 특수 값`() {
        assertEquals(LocalDate.MIN, "-".toKrxDate())
        assertEquals(LocalDate.MIN, "".toKrxDate())
        assertEquals(LocalDate.MIN, "  ".toKrxDate())
    }

    @Test
    fun `toKrxDate - 잘못된 형식은 LocalDate_MIN 반환`() {
        assertEquals(LocalDate.MIN, "invalid".toKrxDate())
        assertEquals(LocalDate.MIN, "2024-01-02".toKrxDate()) // 대시 형식은 지원 안함
    }

    // toKrxDirection 테스트
    @Test
    fun `toKrxDirection - 등락구분`() {
        assertEquals(Direction.UP, "1".toKrxDirection())
        assertEquals(Direction.DOWN, "2".toKrxDirection())
        assertEquals(Direction.UNCHANGED, "3".toKrxDirection())
    }

    @Test
    fun `toKrxDirection - 기본값은 UNCHANGED`() {
        assertEquals(Direction.UNCHANGED, "".toKrxDirection())
        assertEquals(Direction.UNCHANGED, "0".toKrxDirection())
        assertEquals(Direction.UNCHANGED, "4".toKrxDirection())
        assertEquals(Direction.UNCHANGED, "invalid".toKrxDirection())
    }

    @Test
    fun `toKrxDirection - 공백 처리`() {
        assertEquals(Direction.UP, " 1 ".toKrxDirection())
        assertEquals(Direction.DOWN, " 2 ".toKrxDirection())
    }

    // toStringSafe 테스트
    @Test
    fun `toStringSafe - 정상 값`() {
        val value: Any? = "test"
        assertEquals("test", value.toStringSafe())
    }

    @Test
    fun `toStringSafe - null 안전`() {
        val nullValue: Any? = null
        assertEquals("", nullValue.toStringSafe())
    }

    @Test
    fun `toStringSafe - 다양한 타입`() {
        assertEquals("123", 123.toStringSafe())
        assertEquals("45.67", 45.67.toStringSafe())
        assertEquals("true", true.toStringSafe())
    }

    // 통합 시나리오 테스트
    @Test
    fun `실제 KRX API 응답 시나리오`() {
        // 실제 KRX API에서 받을 수 있는 값들을 테스트
        val closePrice = "42,965".toKrxPrice()
        val volume = "192,061".toKrxLong()
        val marketCap = "850,707,000,000".toKrxAmount()
        val changeRate = "2.58".toKrxRate()
        val nav = "43,079.14".toKrxBigDecimal()
        val tradeDate = "2024/01/02".toKrxDate()
        val direction = "1".toKrxDirection()

        assertEquals(BigDecimal("42965.00"), closePrice)
        assertEquals(192061L, volume)
        assertEquals(BigDecimal("850707000000"), marketCap)
        assertEquals(BigDecimal("2.5800"), changeRate)
        assertEquals(BigDecimal("43079.14"), nav)
        assertEquals(LocalDate.of(2024, 1, 2), tradeDate)
        assertEquals(Direction.UP, direction)
    }

    @Test
    fun `음수 값 처리 시나리오`() {
        // 괴리율, 등락률 등 음수가 가능한 필드
        val negativeRate = "-1.23".toKrxRate()
        val negativeSignedLong = "-1,234,567".toKrxSignedLong()

        assertEquals(BigDecimal("-1.2300"), negativeRate)
        assertEquals(-1234567L, negativeSignedLong)

        // 음수 부호가 유지되는지 확인
        assertTrue(negativeRate < BigDecimal.ZERO)
        assertTrue(negativeSignedLong < 0)
    }

    @Test
    fun `빈 값 및 특수 문자 처리 시나리오`() {
        // 데이터 없음을 나타내는 다양한 형태
        val emptyPrice = "".toKrxPrice()
        val dashPrice = "-".toKrxPrice()
        val spacePrice = "  ".toKrxPrice()

        assertEquals(BigDecimal.ZERO.setScale(2), emptyPrice)
        assertEquals(BigDecimal.ZERO.setScale(2), dashPrice)
        assertEquals(BigDecimal.ZERO.setScale(2), spacePrice)
    }
}
