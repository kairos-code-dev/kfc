package dev.kairoscode.kfc.domain.stock

/**
 * 가격 등락 구분
 *
 * 종목의 전일 대비 가격 변동 방향을 나타냅니다.
 *
 * @property code KRX API에서 사용하는 등락 구분 코드
 */
enum class PriceChangeType(val code: String) {
    /**
     * 상승
     */
    RISE("1"),

    /**
     * 하락
     */
    FALL("2"),

    /**
     * 보합 (변동 없음)
     */
    UNCHANGED("3");

    companion object {
        /**
         * KRX API 등락 구분 코드로부터 PriceChangeType enum 생성
         *
         * @param code KRX API 등락 구분 코드 (예: "1", "2", "3")
         * @return 매칭되는 PriceChangeType enum, 없으면 null
         */
        fun fromCode(code: String): PriceChangeType? {
            return entries.find { it.code == code }
        }
    }
}
