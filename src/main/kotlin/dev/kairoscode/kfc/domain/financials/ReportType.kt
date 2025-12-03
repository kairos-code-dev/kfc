package dev.kairoscode.kfc.domain.financials

/**
 * 보고서 유형
 *
 * OPENDART API의 보고서 코드를 표준화한 Enum 클래스입니다.
 *
 * @property code OPENDART 보고서 코드
 * @property description 한글 설명
 */
enum class ReportType(val code: String, val description: String) {
    /**
     * 사업보고서 (연간)
     */
    ANNUAL("11011", "사업보고서"),

    /**
     * 반기보고서
     */
    HALF_YEAR("11012", "반기보고서"),

    /**
     * 1분기보고서
     */
    Q1("11013", "1분기보고서"),

    /**
     * 3분기보고서
     */
    Q3("11014", "3분기보고서");

    companion object {
        /**
         * 보고서 코드로부터 ReportType 조회
         *
         * @param code OPENDART 보고서 코드
         * @return ReportType, 없으면 null
         */
        fun fromCode(code: String): ReportType? {
            return entries.find { it.code == code }
        }
    }
}
