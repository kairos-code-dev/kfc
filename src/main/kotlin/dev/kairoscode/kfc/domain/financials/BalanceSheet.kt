package dev.kairoscode.kfc.domain.financials

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 재무상태표 (Balance Sheet)
 *
 * 특정 시점의 자산, 부채, 자본을 나타내는 재무제표입니다.
 *
 * @property corpCode 법인 고유번호 (OPENDART 8자리)
 * @property reportType 보고서 유형 (사업보고서/반기/분기)
 * @property fiscalYear 사업연도
 * @property statementType 재무제표 구분 (연결/별도)
 * @property currentPeriod 당기 정보
 * @property previousPeriod 전기 정보 (없을 수 있음)
 * @property lineItems 계정과목 목록
 */
data class BalanceSheet(
    val corpCode: String,
    val reportType: ReportType,
    val fiscalYear: Int,
    val statementType: StatementType,
    val currentPeriod: FinancialPeriod,
    val previousPeriod: FinancialPeriod?,
    val lineItems: List<FinancialLineItem>,
)

// =============================================================================
// 재무상태표 확장 함수
// 주요 계정과목 조회 및 재무비율 계산 기능을 제공합니다.
// =============================================================================

/**
 * 자산총계 조회
 */
fun BalanceSheet.getTotalAssets(): BigDecimal? = findAmountByKeywords("자산총계")

/**
 * 유동자산 조회
 */
fun BalanceSheet.getCurrentAssets(): BigDecimal? = findAmountByKeywords("유동자산")

/**
 * 비유동자산 조회
 */
fun BalanceSheet.getNoncurrentAssets(): BigDecimal? = findAmountByKeywords("비유동자산")

/**
 * 부채총계 조회
 */
fun BalanceSheet.getTotalLiabilities(): BigDecimal? = findAmountByKeywords("부채총계")

/**
 * 유동부채 조회
 */
fun BalanceSheet.getCurrentLiabilities(): BigDecimal? = findAmountByKeywords("유동부채")

/**
 * 비유동부채 조회
 */
fun BalanceSheet.getNoncurrentLiabilities(): BigDecimal? = findAmountByKeywords("비유동부채")

/**
 * 자본총계 조회
 */
fun BalanceSheet.getTotalEquity(): BigDecimal? = findAmountByKeywords("자본총계")

/**
 * 자본금 조회
 */
fun BalanceSheet.getShareCapital(): BigDecimal? = findAmountByKeywords("자본금")

/**
 * 이익잉여금 조회
 */
fun BalanceSheet.getRetainedEarnings(): BigDecimal? = findAmountByKeywords("이익잉여금")

/**
 * 부채비율 계산 (%)
 *
 * 부채비율 = (부채총계 / 자본총계) × 100
 */
fun BalanceSheet.calculateDebtToEquityRatio(): BigDecimal? {
    val totalLiabilities = getTotalLiabilities() ?: return null
    val totalEquity = getTotalEquity() ?: return null

    if (totalEquity == BigDecimal.ZERO) return null

    return totalLiabilities
        .divide(totalEquity, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal(100))
}

/**
 * 유동비율 계산 (%)
 *
 * 유동비율 = (유동자산 / 유동부채) × 100
 */
fun BalanceSheet.calculateCurrentRatio(): BigDecimal? {
    val currentAssets = getCurrentAssets() ?: return null
    val currentLiabilities = getCurrentLiabilities() ?: return null

    if (currentLiabilities == BigDecimal.ZERO) return null

    return currentAssets
        .divide(currentLiabilities, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal(100))
}

/**
 * 자기자본비율 계산 (%)
 *
 * 자기자본비율 = (자본총계 / 자산총계) × 100
 */
fun BalanceSheet.calculateEquityRatio(): BigDecimal? {
    val totalEquity = getTotalEquity() ?: return null
    val totalAssets = getTotalAssets() ?: return null

    if (totalAssets == BigDecimal.ZERO) return null

    return totalEquity
        .divide(totalAssets, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal(100))
}

/**
 * 키워드로 계정과목 금액 조회 (내부 헬퍼 함수)
 */
private fun BalanceSheet.findAmountByKeywords(vararg keywords: String): BigDecimal? =
    lineItems
        .find { item ->
            keywords.any { keyword ->
                item.accountName.contains(keyword, ignoreCase = true)
            }
        }?.currentPeriodAmount
