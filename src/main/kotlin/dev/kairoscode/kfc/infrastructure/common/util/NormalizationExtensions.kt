package dev.kairoscode.kfc.infrastructure.common.util

import dev.kairoscode.kfc.domain.funds.Direction
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * KRX API 응답 데이터 정규화를 위한 확장 함수
 *
 * 이 파일은 KRX API의 원시 문자열 응답을 Kotlin의 타입 안전 모델로 변환하는
 * 확장 함수들을 제공합니다.
 */

// 날짜 형식
private val slashFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
private val compactFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

/**
 * String → BigDecimal 변환 (가격용)
 *
 * 사용 사례: 시가, 고가, 저가, 종가
 *
 * 처리 규칙:
 * - 콤마(,) 제거
 * - 공백 제거 (trim)
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - toBigDecimalOrNull() 사용, 실패 시 BigDecimal.ZERO 반환
 *
 * 스케일: 소수점 2자리 (RoundingMode.HALF_UP)
 *
 * 예제:
 * ```
 * "42,965".toKrxPrice()     // BigDecimal("42965.00")
 * "43,250.50".toKrxPrice()  // BigDecimal("43250.50")
 * "-".toKrxPrice()          // BigDecimal("0.00")
 * "".toKrxPrice()           // BigDecimal("0.00")
 * ```
 */
fun String.toKrxPrice(): BigDecimal {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toBigDecimalOrNull()
        ?.setScale(2, RoundingMode.HALF_UP)
        ?: BigDecimal.ZERO
}

/**
 * String → BigDecimal 변환 (금액용)
 *
 * 사용 사례: 시가총액, 거래대금, 투자자산총액
 *
 * 처리 규칙:
 * - 콤마 제거
 * - 공백 제거
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - toBigDecimalOrNull() 사용
 *
 * 스케일: 소수점 0자리 (정수 금액)
 *
 * 예제:
 * ```
 * "850,707,000,000".toKrxAmount()  // BigDecimal("850707000000")
 * "8,222,510,755".toKrxAmount()    // BigDecimal("8222510755")
 * "-".toKrxAmount()                 // BigDecimal("0")
 * ```
 */
fun String.toKrxAmount(): BigDecimal {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toBigDecimalOrNull()
        ?.setScale(0, RoundingMode.HALF_UP)
        ?: BigDecimal.ZERO
}

/**
 * String → BigDecimal 변환 (비율용)
 *
 * 사용 사례: 등락률, 괴리율, 추적오차율, 보수율
 *
 * 처리 규칙:
 * - 콤마 제거 (드물지만 존재 가능)
 * - 공백 제거
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - 음수 부호 유지
 * - toBigDecimalOrNull() 사용
 *
 * 스케일: 소수점 4자리 (RoundingMode.HALF_UP)
 *
 * 예제:
 * ```
 * "2.58".toKrxRate()     // BigDecimal("2.5800")
 * "-1.23".toKrxRate()    // BigDecimal("-1.2300") - 음수 유지
 * "0.15".toKrxRate()     // BigDecimal("0.1500")
 * "-".toKrxRate()        // BigDecimal("0.0000")
 * "".toKrxRate()         // BigDecimal("0.0000")
 * ```
 */
fun String.toKrxRate(): BigDecimal {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toBigDecimalOrNull()
        ?.setScale(4, RoundingMode.HALF_UP)
        ?: BigDecimal.ZERO
}

/**
 * String → BigDecimal 변환 (고정밀)
 *
 * 사용 사례: NAV, 지수 값, NAV 변화금액
 *
 * 처리 규칙:
 * - 콤마 제거
 * - 공백 제거
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - toBigDecimalOrNull() 사용, 실패 시 BigDecimal.ZERO 반환
 *
 * 스케일: 원본 정밀도 유지 (스케일 지정 안함)
 *
 * 예제:
 * ```
 * "43,079.14".toKrxBigDecimal()  // BigDecimal("43079.14")
 * "421.35".toKrxBigDecimal()     // BigDecimal("421.35")
 * "0.15".toKrxBigDecimal()       // BigDecimal("0.15")
 * "-".toKrxBigDecimal()          // BigDecimal.ZERO
 * ```
 */
fun String.toKrxBigDecimal(): BigDecimal {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toBigDecimalOrNull() ?: BigDecimal.ZERO
}

/**
 * String → Int 변환 (항상 양수)
 *
 * 사용 사례: 숫자 값 (정수)
 *
 * 처리 규칙:
 * - 콤마 제거
 * - 공백 제거
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - toIntOrNull() 사용, 실패 시 0 반환
 *
 * 예제:
 * ```
 * "1,234".toKrxInt()  // 1234
 * "42,965".toKrxInt() // 42965
 * "-".toKrxInt()      // 0
 * ```
 */
fun String.toKrxInt(): Int {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toIntOrNull() ?: 0
}

/**
 * String → Long 변환 (항상 양수)
 *
 * 사용 사례: 거래량, 상장주식수, 계약수 (정수만)
 *
 * 처리 규칙:
 * - 콤마 제거
 * - 공백 제거
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - toLongOrNull() 사용, 실패 시 0L 반환
 *
 * 예제:
 * ```
 * "192,061".toKrxLong()          // 192061L
 * "19,800,000".toKrxLong()       // 19800000L
 * "850,707,000,000".toKrxLong()  // 850707000000L
 * "-".toKrxLong()                // 0L
 * ```
 */
fun String.toKrxLong(): Long {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toLongOrNull() ?: 0L
}

/**
 * String → Long 변환 (부호 있음)
 *
 * 사용 사례: 순매수 거래대금, 순매수 거래량 (음수 가능)
 *
 * 처리 규칙:
 * - 콤마 제거
 * - 공백 제거
 * - "-" 단독 → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - 음수 부호 유지
 * - toLongOrNull() 사용
 *
 * 예제:
 * ```
 * "1,234,567".toKrxSignedLong()   // 1234567L
 * "-1,234,567".toKrxSignedLong()  // -1234567L (음수 유지)
 * "-".toKrxSignedLong()           // 0L
 * ```
 */
fun String.toKrxSignedLong(): Long {
    val clean = this.replace(",", "").trim()
    return when {
        clean.isEmpty() || clean == "-" -> 0L
        else -> clean.toLongOrNull() ?: 0L
    }
}

/**
 * String → LocalDate 변환
 *
 * 사용 사례: 거래일, 상장일, 52주 고가/저가 날짜
 *
 * 지원 형식:
 * - Slash 형식: "2024/01/02"
 * - Compact 형식: "20240102"
 * - 특수 값: "-", ""
 *
 * 처리 규칙:
 * - 공백 제거
 * - "-" → LocalDate.MIN 변환
 * - 빈 문자열 → LocalDate.MIN 변환
 * - "/" 포함 → "yyyy/MM/dd" 형식 파싱
 * - 그 외 → "yyyyMMdd" 형식 파싱
 *
 * 예제:
 * ```
 * "2024/01/02".toKrxDate()  // LocalDate.of(2024, 1, 2)
 * "20240102".toKrxDate()    // LocalDate.of(2024, 1, 2)
 * "-".toKrxDate()           // LocalDate.MIN
 * "".toKrxDate()            // LocalDate.MIN
 * ```
 */
fun String.toKrxDate(): LocalDate {
    val s = this.trim()
    return when {
        s.isEmpty() || s == "-" -> LocalDate.MIN
        s.contains("/") -> {
            try {
                LocalDate.parse(s, slashFormatter)
            } catch (e: Exception) {
                LocalDate.MIN
            }
        }
        else -> {
            try {
                LocalDate.parse(s, compactFormatter)
            } catch (e: Exception) {
                LocalDate.MIN
            }
        }
    }
}

/**
 * String → Double 변환
 *
 * 사용 사례: 부동소수점 숫자 값
 *
 * 처리 규칙:
 * - 콤마 제거
 * - 공백 제거
 * - "-" → "0" 변환
 * - 빈 문자열 → "0" 변환
 * - toDoubleOrNull() 사용, 실패 시 0.0 반환
 *
 * 예제:
 * ```
 * "42,965.50".toKrxDouble()  // 42965.50
 * "1.234".toKrxDouble()      // 1.234
 * "-".toKrxDouble()          // 0.0
 * ```
 */
fun String.toKrxDouble(): Double {
    return this.replace(",", "")
        .trim()
        .let { if (it == "-" || it.isEmpty()) "0" else it }
        .toDoubleOrNull() ?: 0.0
}

/**
 * String → Direction Enum 변환
 *
 * 사용 사례: 가격 방향, 지수 방향
 *
 * 매핑:
 * - "1" → Direction.UP (상승)
 * - "2" → Direction.DOWN (하락)
 * - "3" → Direction.UNCHANGED (보합)
 * - 기타 → Direction.UNCHANGED (기본값)
 *
 * 예제:
 * ```
 * "1".toKrxDirection()  // Direction.UP
 * "2".toKrxDirection()  // Direction.DOWN
 * "3".toKrxDirection()  // Direction.UNCHANGED
 * ```
 */
fun String.toKrxDirection(): Direction {
    return when (this.trim()) {
        "1" -> Direction.UP
        "2" -> Direction.DOWN
        "3" -> Direction.UNCHANGED
        else -> Direction.UNCHANGED
    }
}

/**
 * String → Direction Enum 변환 (별칭)
 *
 * toKrxDirection()의 짧은 별칭
 */
fun String.toDirection(): Direction = toKrxDirection()

/**
 * Any? → String 안전 변환
 *
 * null-safe toString
 *
 * 예제:
 * ```
 * val value: Any? = "test"
 * value.toStringSafe()  // "test"
 *
 * val nullValue: Any? = null
 * nullValue.toStringSafe()  // ""
 * ```
 */
fun Any?.toStringSafe(): String {
    return this?.toString() ?: ""
}

/**
 * Map<String, Any?>에서 String 값을 안전하게 추출
 *
 * KRX API 응답 JSON 파싱 시 사용
 *
 * 예제:
 * ```
 * val data = mapOf("ISU_CD" to "KR7152100004", "ISU_NM" to "ARIRANG 200")
 * data.getString("ISU_CD")  // "KR7152100004"
 * data.getString("MISSING") // "" (키가 없으면 빈 문자열)
 * ```
 */
fun Map<String, Any?>.getString(key: String): String {
    return this[key]?.toString() ?: ""
}

/**
 * Map<String, Any?>에서 String 값을 안전하게 추출 (nullable)
 *
 * KRX API 응답 JSON 파싱 시 사용 (null 허용 필드)
 *
 * 예제:
 * ```
 * val data = mapOf("ISU_CD" to "KR7152100004", "OPTIONAL" to null)
 * data.getStringOrNull("ISU_CD")  // "KR7152100004"
 * data.getStringOrNull("OPTIONAL") // null
 * data.getStringOrNull("MISSING")  // null
 * ```
 */
fun Map<String, Any?>.getStringOrNull(key: String): String? {
    val value = this[key] ?: return null
    val str = value.toString()
    return if (str.isEmpty() || str == "-") null else str
}
