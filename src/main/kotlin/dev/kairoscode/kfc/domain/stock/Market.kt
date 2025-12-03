package dev.kairoscode.kfc.domain.stock

/**
 * 주식 시장 구분
 *
 * 한국 증권 시장의 구분을 나타냅니다.
 *
 * @property code KRX API에서 사용하는 시장 코드
 * @property koreanName 한글 시장명
 */
enum class Market(val code: String, val koreanName: String) {
    /**
     * 코스피 (KOSPI) - 유가증권시장
     */
    KOSPI("STK", "코스피"),

    /**
     * 코스닥 (KOSDAQ) - 코스닥시장
     */
    KOSDAQ("KSQ", "코스닥"),

    /**
     * 코넥스 (KONEX) - 중소기업전용시장
     */
    KONEX("KNX", "코넥스"),

    /**
     * 전체 시장
     */
    ALL("ALL", "전체");

    companion object {
        /**
         * KRX API 시장 코드로부터 Market enum 생성
         *
         * @param code KRX API 시장 코드 (예: "STK", "KSQ", "KNX", "ALL")
         * @return 매칭되는 Market enum, 없으면 ALL
         */
        fun fromCode(code: String): Market {
            return entries.find { it.code == code } ?: ALL
        }
    }
}
