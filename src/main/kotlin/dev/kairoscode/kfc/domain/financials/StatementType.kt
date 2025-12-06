package dev.kairoscode.kfc.domain.financials

/**
 * 재무제표 구분 (연결 vs 별도)
 *
 * OPENDART API의 fs_div 파라미터를 표준화한 Enum 클래스입니다.
 *
 * @property code OPENDART fs_div 코드
 * @property description 한글 설명
 */
enum class StatementType(
    val code: String,
    val description: String,
) {
    /**
     * 연결재무제표 (Consolidated Financial Statements)
     *
     * 그룹 전체의 재무 상태를 반영하므로 우선 사용을 권장합니다.
     */
    CONSOLIDATED("CFS", "연결재무제표"),

    /**
     * 재무제표 (Separate Financial Statements)
     *
     * 개별 법인의 재무 상태를 반영합니다.
     */
    SEPARATE("OFS", "재무제표"),
    ;

    companion object {
        /**
         * 코드로부터 StatementType 조회
         *
         * @param code OPENDART fs_div 코드
         * @return StatementType, 없으면 null
         */
        fun fromCode(code: String): StatementType? = entries.find { it.code == code }
    }
}
