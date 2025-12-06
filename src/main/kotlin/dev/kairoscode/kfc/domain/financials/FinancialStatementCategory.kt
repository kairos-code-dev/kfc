package dev.kairoscode.kfc.domain.financials

/**
 * 재무제표 카테고리
 *
 * OPENDART API의 sj_div 필드를 표준화한 Enum 클래스입니다.
 *
 * @property code OPENDART sj_div 코드
 * @property koreanName 한글명
 * @property englishName 영문명
 */
enum class FinancialStatementCategory(
    val code: String,
    val koreanName: String,
    val englishName: String,
) {
    /**
     * 재무상태표
     */
    BALANCE_SHEET("BS", "재무상태표", "Balance Sheet"),

    /**
     * 손익계산서
     */
    INCOME_STATEMENT("IS", "손익계산서", "Income Statement"),

    /**
     * 현금흐름표
     */
    CASH_FLOW("CF", "현금흐름표", "Cash Flow Statement"),

    /**
     * 포괄손익계산서
     */
    COMPREHENSIVE_INCOME("CIS", "포괄손익계산서", "Comprehensive Income Statement"),

    /**
     * 자본변동표
     */
    CHANGES_IN_EQUITY("SCE", "자본변동표", "Statement of Changes in Equity"),
    ;

    companion object {
        /**
         * 코드로부터 FinancialStatementCategory 조회
         *
         * @param code OPENDART sj_div 코드
         * @return FinancialStatementCategory, 없으면 null
         */
        fun fromCode(code: String): FinancialStatementCategory? = entries.find { it.code == code }
    }
}
