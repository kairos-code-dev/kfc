package dev.kairoscode.kfc.domain.financials

import java.math.BigDecimal

/**
 * 현금흐름표 (Cash Flow Statement)
 *
 * 일정 기간의 현금 유입과 유출을 나타내는 재무제표입니다.
 *
 * @property corpCode 법인 고유번호 (OPENDART 8자리)
 * @property reportType 보고서 유형 (사업보고서/반기/분기)
 * @property fiscalYear 사업연도
 * @property statementType 재무제표 구분 (연결/별도)
 * @property currentPeriod 당기 정보
 * @property previousPeriod 전기 정보 (없을 수 있음)
 * @property lineItems 계정과목 목록
 */
data class CashFlowStatement(
    val corpCode: String,
    val reportType: ReportType,
    val fiscalYear: Int,
    val statementType: StatementType,
    val currentPeriod: FinancialPeriod,
    val previousPeriod: FinancialPeriod?,
    val lineItems: List<FinancialLineItem>,
)

// =============================================================================
// 현금흐름표 확장 함수
// 주요 계정과목 조회 및 현금흐름 분석 기능을 제공합니다.
// =============================================================================

/**
 * 영업활동 현금흐름 조회
 */
fun CashFlowStatement.getOperatingCashFlow(): BigDecimal? = findAmountByKeywords("영업활동현금흐름", "영업활동으로인한현금흐름")

/**
 * 투자활동 현금흐름 조회
 */
fun CashFlowStatement.getInvestingCashFlow(): BigDecimal? = findAmountByKeywords("투자활동현금흐름", "투자활동으로인한현금흐름")

/**
 * 재무활동 현금흐름 조회
 */
fun CashFlowStatement.getFinancingCashFlow(): BigDecimal? = findAmountByKeywords("재무활동현금흐름", "재무활동으로인한현금흐름")

/**
 * 현금및현금성자산의증감 조회
 */
fun CashFlowStatement.getNetChangeInCash(): BigDecimal? = findAmountByKeywords("현금및현금성자산의증감", "현금및현금성자산의 순증감")

/**
 * 잉여현금흐름 계산 (Free Cash Flow)
 *
 * FCF = 영업활동 현금흐름 - 투자활동 현금흐름
 *
 * 주의: 투자활동 현금흐름은 보통 음수이므로, 실질적으로는 영업 + |투자| 형태입니다.
 */
fun CashFlowStatement.calculateFreeCashFlow(): BigDecimal? {
    val operatingCashFlow = getOperatingCashFlow() ?: return null
    val investingCashFlow = getInvestingCashFlow() ?: return null

    // 투자활동 현금흐름을 더하는 것이 맞습니다 (일반적으로 음수)
    return operatingCashFlow.add(investingCashFlow)
}

/**
 * 키워드로 계정과목 금액 조회 (내부 헬퍼 함수)
 */
private fun CashFlowStatement.findAmountByKeywords(vararg keywords: String): BigDecimal? =
    lineItems
        .find { item ->
            keywords.any { keyword ->
                item.accountName.contains(keyword, ignoreCase = true)
            }
        }?.currentPeriodAmount
