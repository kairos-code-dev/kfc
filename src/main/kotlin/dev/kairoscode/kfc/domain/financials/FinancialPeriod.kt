package dev.kairoscode.kfc.domain.financials

/**
 * 재무 기간 정보
 *
 * 재무제표의 특정 기간 정보를 나타냅니다 (당기, 전기, 전전기).
 *
 * @property periodName 기명 (예: "제 31 기")
 * @property fiscalYear 회계연도
 */
data class FinancialPeriod(
    val periodName: String,
    val fiscalYear: Int,
)
