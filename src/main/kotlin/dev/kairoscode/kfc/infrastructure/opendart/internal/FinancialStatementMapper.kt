package dev.kairoscode.kfc.infrastructure.opendart.internal

import dev.kairoscode.kfc.domain.financials.*
import dev.kairoscode.kfc.infrastructure.opendart.model.FinancialStatementRaw
import java.math.BigDecimal

/**
 * OPENDART 원시 데이터를 도메인 모델로 변환하는 매퍼
 */
internal object FinancialStatementMapper {

    /**
     * 원시 데이터를 재무제표별로 그룹핑하고 도메인 모델로 변환
     */
    fun toFinancialStatements(
        rawList: List<FinancialStatementRaw>,
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): FinancialStatements {
        val groupedByCategory = rawList.groupBy { it.sjDiv }

        val incomeStatement = groupedByCategory["IS"]?.let {
            toIncomeStatement(it, corpCode, year, reportType, statementType)
        }

        val balanceSheet = groupedByCategory["BS"]?.let {
            toBalanceSheet(it, corpCode, year, reportType, statementType)
        }

        val cashFlowStatement = groupedByCategory["CF"]?.let {
            toCashFlowStatement(it, corpCode, year, reportType, statementType)
        }

        return FinancialStatements(
            corpCode = corpCode,
            fiscalYear = year,
            reportType = reportType,
            statementType = statementType,
            incomeStatement = incomeStatement,
            balanceSheet = balanceSheet,
            cashFlowStatement = cashFlowStatement
        )
    }

    /**
     * 손익계산서로 변환
     */
    fun toIncomeStatement(
        rawList: List<FinancialStatementRaw>,
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): IncomeStatement {
        val lineItems = rawList.map { toLineItem(it) }
        val (currentPeriod, previousPeriod) = extractPeriods(rawList.firstOrNull(), year)

        return IncomeStatement(
            corpCode = corpCode,
            reportType = reportType,
            fiscalYear = year,
            statementType = statementType,
            currentPeriod = currentPeriod,
            previousPeriod = previousPeriod,
            lineItems = lineItems
        )
    }

    /**
     * 재무상태표로 변환
     */
    fun toBalanceSheet(
        rawList: List<FinancialStatementRaw>,
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): BalanceSheet {
        val lineItems = rawList.map { toLineItem(it) }
        val (currentPeriod, previousPeriod) = extractPeriods(rawList.firstOrNull(), year)

        return BalanceSheet(
            corpCode = corpCode,
            reportType = reportType,
            fiscalYear = year,
            statementType = statementType,
            currentPeriod = currentPeriod,
            previousPeriod = previousPeriod,
            lineItems = lineItems
        )
    }

    /**
     * 현금흐름표로 변환
     */
    fun toCashFlowStatement(
        rawList: List<FinancialStatementRaw>,
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): CashFlowStatement {
        val lineItems = rawList.map { toLineItem(it) }
        val (currentPeriod, previousPeriod) = extractPeriods(rawList.firstOrNull(), year)

        return CashFlowStatement(
            corpCode = corpCode,
            reportType = reportType,
            fiscalYear = year,
            statementType = statementType,
            currentPeriod = currentPeriod,
            previousPeriod = previousPeriod,
            lineItems = lineItems
        )
    }

    /**
     * 원시 데이터 → FinancialLineItem 변환
     */
    private fun toLineItem(raw: FinancialStatementRaw): FinancialLineItem {
        return FinancialLineItem(
            accountId = raw.accountId,
            accountName = raw.accountNm,
            accountDetail = raw.accountDetail?.takeIf { it != "-" },
            currentPeriodAmount = raw.thstrmAmount.toFinancialAmount(),
            previousPeriodAmount = raw.frmtrmAmount?.toFinancialAmount(),
            previous2PeriodAmount = raw.bfefrmtrmAmount?.toFinancialAmount(),
            order = raw.ord.toIntOrNull() ?: 0
        )
    }

    /**
     * 당기/전기 기간 정보 추출
     */
    private fun extractPeriods(
        raw: FinancialStatementRaw?,
        fiscalYear: Int
    ): Pair<FinancialPeriod, FinancialPeriod?> {
        val currentPeriod = FinancialPeriod(
            periodName = raw?.thstrmNm ?: "",
            fiscalYear = fiscalYear
        )

        val previousPeriod = raw?.frmtrmNm?.let {
            FinancialPeriod(
                periodName = it,
                fiscalYear = fiscalYear - 1
            )
        }

        return currentPeriod to previousPeriod
    }

    /**
     * 금액 문자열 → BigDecimal 변환
     *
     * - 빈 문자열 또는 "-" → BigDecimal.ZERO
     * - 쉼표 제거 후 숫자 변환
     */
    private fun String?.toFinancialAmount(): BigDecimal {
        if (this.isNullOrBlank() || this == "-") {
            return BigDecimal.ZERO
        }

        return try {
            BigDecimal(this.replace(",", ""))
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
}
