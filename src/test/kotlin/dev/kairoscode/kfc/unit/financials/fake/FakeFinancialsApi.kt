package dev.kairoscode.kfc.unit.financials.fake

import dev.kairoscode.kfc.api.FinancialsApi
import dev.kairoscode.kfc.domain.financials.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * FinancialsApi의 Fake 구현체
 *
 * 단위 테스트에서 Mock 데이터를 반환하는 API입니다.
 */
class FakeFinancialsApi(
    private val incomeStatementResponse: String? = null,
    private val balanceSheetResponse: String? = null,
    private val cashFlowStatementResponse: String? = null,
    private val allFinancialsResponse: String? = null
) : FinancialsApi {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun getIncomeStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): IncomeStatement {
        require(incomeStatementResponse != null) { "incomeStatementResponse가 설정되지 않았습니다" }
        return json.decodeFromString(incomeStatementResponse)
    }

    override suspend fun getBalanceSheet(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): BalanceSheet {
        require(balanceSheetResponse != null) { "balanceSheetResponse가 설정되지 않았습니다" }
        return json.decodeFromString(balanceSheetResponse)
    }

    override suspend fun getCashFlowStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): CashFlowStatement {
        require(cashFlowStatementResponse != null) { "cashFlowStatementResponse가 설정되지 않았습니다" }
        return json.decodeFromString(cashFlowStatementResponse)
    }

    override suspend fun getAllFinancials(
        corpCode: String,
        year: Int,
        reportType: ReportType,
        statementType: StatementType
    ): FinancialStatements {
        require(allFinancialsResponse != null) { "allFinancialsResponse가 설정되지 않았습니다" }
        return json.decodeFromString(allFinancialsResponse)
    }
}
