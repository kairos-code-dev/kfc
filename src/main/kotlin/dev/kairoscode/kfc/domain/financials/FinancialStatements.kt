package dev.kairoscode.kfc.domain.financials

/**
 * 전체 재무제표 통합 모델
 *
 * 손익계산서, 재무상태표, 현금흐름표를 한 번에 담는 컨테이너 모델입니다.
 * 단일 API 호출로 모든 재무제표를 조회할 때 사용됩니다.
 *
 * @property corpCode 법인 고유번호 (OPENDART 8자리)
 * @property fiscalYear 사업연도
 * @property reportType 보고서 유형
 * @property statementType 재무제표 구분 (연결/별도)
 * @property incomeStatement 손익계산서 (없을 수 있음)
 * @property balanceSheet 재무상태표 (없을 수 있음)
 * @property cashFlowStatement 현금흐름표 (없을 수 있음)
 */
data class FinancialStatements(
    val corpCode: String,
    val fiscalYear: Int,
    val reportType: ReportType,
    val statementType: StatementType,
    val incomeStatement: IncomeStatement?,
    val balanceSheet: BalanceSheet?,
    val cashFlowStatement: CashFlowStatement?
)
