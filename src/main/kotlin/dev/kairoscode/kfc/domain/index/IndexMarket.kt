package dev.kairoscode.kfc.domain.index

/**
 * 지수 시장 구분
 *
 * 한국 증시의 지수 시장 구분을 나타냅니다.
 *
 * @property code KRX API에서 사용하는 시장 코드
 * @property koreanName 한글 시장명
 */
enum class IndexMarket(val code: String, val koreanName: String) {
    /**
     * 코스피 지수
     */
    KOSPI("1", "코스피"),

    /**
     * 코스닥 지수
     */
    KOSDAQ("2", "코스닥"),

    /**
     * 파생상품 관련 지수
     */
    DERIVATIVES("3", "파생"),

    /**
     * 전체 시장
     */
    ALL("ALL", "전체");

    companion object {
        /**
         * KRX API 시장 코드로부터 IndexMarket enum 생성
         *
         * @param code KRX API 시장 코드 (예: "1", "2", "3", "ALL")
         * @return 매칭되는 IndexMarket enum, 없으면 ALL
         */
        fun fromCode(code: String): IndexMarket {
            return entries.find { it.code == code } ?: ALL
        }
    }
}
