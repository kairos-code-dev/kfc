package dev.kairoscode.kfc.domain.financials

import java.math.BigDecimal

/**
 * 재무제표 항목 (계정과목)
 *
 * 재무제표의 개별 항목(계정과목)을 나타냅니다.
 * 당기, 전기, 전전기의 금액을 포함합니다.
 *
 * @property accountId 계정 ID (XBRL 기반, 예: "ifrs-full_CurrentAssets")
 * @property accountName 계정명 (예: "유동자산")
 * @property accountDetail 계정상세 (선택적)
 * @property currentPeriodAmount 당기 금액
 * @property previousPeriodAmount 전기 금액 (없을 수 있음)
 * @property previous2PeriodAmount 전전기 금액 (없을 수 있음)
 * @property order 정렬순서 (재무제표 표준 순서 유지)
 */
data class FinancialLineItem(
    val accountId: String,
    val accountName: String,
    val accountDetail: String?,
    val currentPeriodAmount: BigDecimal,
    val previousPeriodAmount: BigDecimal?,
    val previous2PeriodAmount: BigDecimal?,
    val order: Int
)
