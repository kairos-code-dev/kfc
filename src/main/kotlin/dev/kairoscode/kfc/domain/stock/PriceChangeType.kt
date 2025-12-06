package dev.kairoscode.kfc.domain.stock

/**
 * 등락 구분
 *
 * 주가의 전일 대비 등락 상태를 구분하는 Enum 클래스입니다.
 *
 * @property code KRX API에서 사용하는 등락 구분 코드 (FLUC_TP_CD)
 */
enum class PriceChangeType(
    val code: String,
) {
    /**
     * 상승 (전일 대비 가격 상승)
     */
    RISE("1"),

    /**
     * 하락 (전일 대비 가격 하락)
     */
    FALL("2"),

    /**
     * 보합 (전일 대비 가격 변동 없음)
     */
    UNCHANGED("3"),
    ;

    companion object {
        /**
         * KRX API 코드로부터 PriceChangeType Enum 반환
         *
         * @param code KRX API 등락 구분 코드 (1, 2, 3)
         * @return PriceChangeType Enum, 코드가 유효하지 않으면 null
         */
        fun fromCode(code: String): PriceChangeType? = entries.find { it.code == code }
    }
}
