package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.ReportType
import dev.kairoscode.kfc.domain.financials.StatementType
import dev.kairoscode.kfc.domain.financials.getNetIncome
import dev.kairoscode.kfc.domain.financials.getRevenue
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * 손익계산서 조회 Integration Test
 *
 * getIncomeStatement() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 */
@DisplayName("FinancialsApi.getIncomeStatement() - 손익계산서 조회")
class FinancialsApiIncomeStatementSpec : IntegrationTestBase() {

    @Test
    @DisplayName("특정 법인의 연결 손익계산서를 조회할 수 있다")
    fun testGetIncomeStatement() = integrationTest {
        // Given: 삼성전자 corp_code (고정: 2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023

        // When: 손익계산서 조회
        val incomeStatement = client.financials!!.getIncomeStatement(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.ANNUAL,
            statementType = StatementType.CONSOLIDATED
        )

        // Then: 손익계산서 데이터 반환
        println("✅ 삼성전자 ${year}년 연결 손익계산서 항목 개수: ${incomeStatement.lineItems.size}")

        // 주요 계정과목 조회
        val revenue = incomeStatement.getRevenue()
        val netIncome = incomeStatement.getNetIncome()
        println("  - 매출액: $revenue")
        println("  - 당기순이익: $netIncome")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = incomeStatement,
            category = RecordingConfig.Paths.Financials.INCOME_STATEMENT,
            fileName = "samsung_income_statement_$year"
        )
    }

    @Test
    @DisplayName("분기 손익계산서를 조회할 수 있다")
    fun testGetIncomeStatementQuarterly() = integrationTest {
        // Given: 삼성전자 corp_code, Q1 (고정: 2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023

        // When: Q1 손익계산서 조회
        val incomeStatement = client.financials!!.getIncomeStatement(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.Q1,
            statementType = StatementType.CONSOLIDATED
        )

        // Then: 손익계산서 데이터 반환
        println("✅ 삼성전자 ${year}년 1분기 손익계산서 항목 개수: ${incomeStatement.lineItems.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = incomeStatement,
            category = RecordingConfig.Paths.Financials.INCOME_STATEMENT,
            fileName = "samsung_income_statement_${year}_q1"
        )
    }

    @Test
    @DisplayName("별도 손익계산서를 조회할 수 있다")
    fun testGetIncomeStatementSeparate() = integrationTest {
        // Given: 삼성전자 corp_code, 별도재무제표 (고정: 2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023

        // When: 별도 손익계산서 조회
        val incomeStatement = client.financials!!.getIncomeStatement(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.ANNUAL,
            statementType = StatementType.SEPARATE
        )

        // Then: 손익계산서 데이터 반환
        println("✅ 삼성전자 ${year}년 별도 손익계산서 항목 개수: ${incomeStatement.lineItems.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = incomeStatement,
            category = RecordingConfig.Paths.Financials.INCOME_STATEMENT,
            fileName = "samsung_income_statement_${year}_separate"
        )
    }
}
