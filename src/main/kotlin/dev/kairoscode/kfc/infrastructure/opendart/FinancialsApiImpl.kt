package dev.kairoscode.kfc.infrastructure.opendart

import dev.kairoscode.kfc.api.FinancialsApi
import dev.kairoscode.kfc.domain.exception.ErrorCode
import dev.kairoscode.kfc.domain.exception.KfcException
import dev.kairoscode.kfc.domain.financials.*
import dev.kairoscode.kfc.infrastructure.opendart.internal.FinancialStatementMapper

/**
 * 재무제표 도메인 API 구현체
 *
 * OPENDART API를 통합하여 재무제표 관련 모든 데이터를 제공합니다.
 * 내부적으로 OpenDartApi를 사용하며, OPENDART 전용 RateLimiter를 적용합니다.
 */
internal class FinancialsApiImpl(
    private val openDartApi: OpenDartApi
) : FinancialsApi {

    companion object {
        private const val MIN_VALID_YEAR = 2015
        private const val CORP_CODE_LENGTH = 8
    }

    override suspend fun getIncomeStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): IncomeStatement {
        validateInputs(corpCode, year)

        val rawList = openDartApi.getAllFinancialStatements(
            corpCode = corpCode,
            year = year,
            reportCode = reportType.code,
            fsDiv = statementType.code
        )

        val groupedByCategory = rawList.groupBy { it.sjDiv }
        val incomeStatementRaw = groupedByCategory["IS"]
            ?: throw KfcException(
                ErrorCode.OPENDART_API_ERROR,
                "손익계산서 데이터가 없습니다",
                context = mapOf(
                    "corpCode" to corpCode,
                    "year" to year,
                    "reportType" to reportType.code
                )
            )

        return FinancialStatementMapper.toIncomeStatement(
            incomeStatementRaw,
            corpCode,
            year,
            reportType,
            statementType
        )
    }

    override suspend fun getBalanceSheet(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): BalanceSheet {
        validateInputs(corpCode, year)

        val rawList = openDartApi.getAllFinancialStatements(
            corpCode = corpCode,
            year = year,
            reportCode = reportType.code,
            fsDiv = statementType.code
        )

        val groupedByCategory = rawList.groupBy { it.sjDiv }
        val balanceSheetRaw = groupedByCategory["BS"]
            ?: throw KfcException(
                ErrorCode.OPENDART_API_ERROR,
                "재무상태표 데이터가 없습니다",
                context = mapOf(
                    "corpCode" to corpCode,
                    "year" to year,
                    "reportType" to reportType.code
                )
            )

        return FinancialStatementMapper.toBalanceSheet(
            balanceSheetRaw,
            corpCode,
            year,
            reportType,
            statementType
        )
    }

    override suspend fun getCashFlowStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): CashFlowStatement {
        validateInputs(corpCode, year)

        val rawList = openDartApi.getAllFinancialStatements(
            corpCode = corpCode,
            year = year,
            reportCode = reportType.code,
            fsDiv = statementType.code
        )

        val groupedByCategory = rawList.groupBy { it.sjDiv }
        val cashFlowStatementRaw = groupedByCategory["CF"]
            ?: throw KfcException(
                ErrorCode.OPENDART_API_ERROR,
                "현금흐름표 데이터가 없습니다",
                context = mapOf(
                    "corpCode" to corpCode,
                    "year" to year,
                    "reportType" to reportType.code
                )
            )

        return FinancialStatementMapper.toCashFlowStatement(
            cashFlowStatementRaw,
            corpCode,
            year,
            reportType,
            statementType
        )
    }

    override suspend fun getAllFinancials(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): FinancialStatements {
        validateInputs(corpCode, year)

        val rawList = openDartApi.getAllFinancialStatements(
            corpCode = corpCode,
            year = year,
            reportCode = reportType.code,
            fsDiv = statementType.code
        )

        return FinancialStatementMapper.toFinancialStatements(
            rawList,
            corpCode,
            year,
            reportType,
            statementType
        )
    }

    /**
     * 입력 파라미터 검증
     */
    private fun validateInputs(corpCode: String, year: Int) {
        validateCorpCode(corpCode)
        validateYear(year)
    }

    /**
     * corpCode 검증
     */
    private fun validateCorpCode(corpCode: String) {
        val trimmed = corpCode.trim()

        when {
            trimmed.isBlank() ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "corpCode는 공백이 아니어야 합니다")
            trimmed.length != CORP_CODE_LENGTH ->
                throw KfcException(
                    ErrorCode.INVALID_PARAMETER,
                    "corpCode는 정확히 ${CORP_CODE_LENGTH}자여야 합니다 (입력: $trimmed)"
                )
            !trimmed.all { it.isDigit() } ->
                throw KfcException(
                    ErrorCode.INVALID_PARAMETER,
                    "corpCode는 숫자만 포함해야 합니다 (입력: $trimmed)"
                )
        }
    }

    /**
     * year 검증 (2015년 이후만 지원)
     */
    private fun validateYear(year: Int) {
        val currentYear = java.time.LocalDate.now().year

        when {
            year < MIN_VALID_YEAR ->
                throw KfcException(
                    ErrorCode.INVALID_PARAMETER,
                    "OPENDART는 ${MIN_VALID_YEAR}년 이후 데이터만 지원합니다 (입력: $year)"
                )
            year > currentYear ->
                throw KfcException(
                    ErrorCode.INVALID_PARAMETER,
                    "년도는 현재 년도($currentYear) 이하여야 합니다 (입력: $year)"
                )
        }
    }
}
