package dev.kairoscode.kfc.domain.stock

/**
 * 시장 구분
 *
 * 한국 주식 시장을 구분하는 Enum 클래스입니다.
 *
 * @property code KRX API에서 사용하는 시장 코드 (mktId, mktsel)
 * @property koreanName 한글 시장명
 */
enum class Market(
    val code: String,
    val koreanName: String,
) {
    /**
     * 코스피 (한국거래소 유가증권시장)
     */
    KOSPI("STK", "코스피"),

    /**
     * 코스닥 (코스닥시장)
     */
    KOSDAQ("KSQ", "코스닥"),

    /**
     * 코넥스 (중소기업전용시장)
     */
    KONEX("KNX", "코넥스"),

    /**
     * 전체 시장 (모든 시장 통합)
     */
    ALL("ALL", "전체"),
    ;

    companion object {
        /**
         * KRX API 코드로부터 Market Enum 반환
         *
         * @param code KRX API 코드 (STK, KSQ, KNX, ALL)
         * @return Market Enum
         */
        fun fromCode(code: String): Market = entries.find { it.code == code } ?: ALL
    }
}
