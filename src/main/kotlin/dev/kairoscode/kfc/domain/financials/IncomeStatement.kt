package dev.kairoscode.kfc.domain.financials

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 손익계산서 (Income Statement)
 *
 * 일정 기간 동안의 수익과 비용, 순이익을 나타내는 재무제표입니다.
 *
 * @property corpCode 법인 고유번호 (OPENDART 8자리)
 * @property reportType 보고서 유형 (사업보고서/반기/분기)
 * @property fiscalYear 사업연도
 * @property statementType 재무제표 구분 (연결/별도)
 * @property currentPeriod 당기 정보
 * @property previousPeriod 전기 정보 (없을 수 있음)
 * @property lineItems 계정과목 목록
 */
data class IncomeStatement(
    val corpCode: String,
    val reportType: ReportType,
    val fiscalYear: Int,
    val statementType: StatementType,
    val currentPeriod: FinancialPeriod,
    val previousPeriod: FinancialPeriod?,
    val lineItems: List<FinancialLineItem>,
)

// =============================================================================
// 손익계산서 확장 함수
// 주요 계정과목 조회 및 재무비율 계산 기능을 제공합니다.
// =============================================================================

/**
 * 매출액 조회
 */
fun IncomeStatement.getRevenue(): BigDecimal? = findAmountByKeywords("매출액", "수익(매출액)")

/**
 * 매출원가 조회
 */
fun IncomeStatement.getCostOfRevenue(): BigDecimal? = findAmountByKeywords("매출원가")

/**
 * 매출총이익 조회
 */
fun IncomeStatement.getGrossProfit(): BigDecimal? = findAmountByKeywords("매출총이익")

/**
 * 판매비와관리비 조회
 */
fun IncomeStatement.getSellingGeneralAndAdministrativeExpense(): BigDecimal? =
    findAmountByKeywords("판매비와관리비", "판매비와 관리비")

/**
 * 영업이익 조회
 */
fun IncomeStatement.getOperatingIncome(): BigDecimal? = findAmountByKeywords("영업이익", "영업이익(손실)")

/**
 * 법인세비용차감전순이익 조회
 */
fun IncomeStatement.getPretaxIncome(): BigDecimal? = findAmountByKeywords("법인세비용차감전순이익", "법인세비용차감전순이익(손실)")

/**
 * 당기순이익 조회
 */
fun IncomeStatement.getNetIncome(): BigDecimal? = findAmountByKeywords("당기순이익", "당기순이익(손실)")

/**
 * 매출총이익률 계산 (%)
 *
 * 매출총이익률 = (매출총이익 / 매출액) × 100
 */
fun IncomeStatement.calculateGrossMargin(): BigDecimal? {
    val revenue = getRevenue() ?: return null
    val grossProfit = getGrossProfit() ?: return null

    if (revenue == BigDecimal.ZERO) return null

    return grossProfit
        .divide(revenue, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal(100))
}

/**
 * 영업이익률 계산 (%)
 *
 * 영업이익률 = (영업이익 / 매출액) × 100
 */
fun IncomeStatement.calculateOperatingMargin(): BigDecimal? {
    val revenue = getRevenue() ?: return null
    val operatingIncome = getOperatingIncome() ?: return null

    if (revenue == BigDecimal.ZERO) return null

    return operatingIncome
        .divide(revenue, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal(100))
}

/**
 * 순이익률 계산 (%)
 *
 * 순이익률 = (당기순이익 / 매출액) × 100
 */
fun IncomeStatement.calculateNetMargin(): BigDecimal? {
    val revenue = getRevenue() ?: return null
    val netIncome = getNetIncome() ?: return null

    if (revenue == BigDecimal.ZERO) return null

    return netIncome
        .divide(revenue, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal(100))
}

/**
 * 키워드로 계정과목 금액 조회 (내부 헬퍼 함수)
 */
private fun IncomeStatement.findAmountByKeywords(vararg keywords: String): BigDecimal? =
    lineItems
        .find { item ->
            keywords.any { keyword ->
                item.accountName.contains(keyword, ignoreCase = true)
            }
        }?.currentPeriodAmount
