package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.ReportType
import dev.kairoscode.kfc.domain.financials.StatementType
import dev.kairoscode.kfc.domain.financials.getOperatingCashFlow
import dev.kairoscode.kfc.domain.financials.getInvestingCashFlow
import dev.kairoscode.kfc.domain.financials.getFinancingCashFlow
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * 현금흐름표 조회 Integration Test
 *
 * getCashFlowStatement() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 */
@DisplayName("FinancialsApi.getCashFlowStatement() - 현금흐름표 조회")
class FinancialsApiCashFlowSpec : IntegrationTestBase() {

    @Test
    @DisplayName("특정 법인의 연결 현금흐름표를 조회할 수 있다")
    fun testGetCashFlowStatement() = integrationTest {
        // Given: 삼성전자 corp_code (고정: 2023년)
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023

        // When: 현금흐름표 조회
        val cashFlowStatement = client.financials?.getCashFlowStatement(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.ANNUAL,
            statementType = StatementType.CONSOLIDATED
        ) ?: return@integrationTest

        // Then: 현금흐름표 데이터 반환
        println("✅ 삼성전자 ${year}년 연결 현금흐름표 항목 개수: ${cashFlowStatement.lineItems.size}")

        // 주요 계정과목 조회
        val operatingCashFlow = cashFlowStatement.getOperatingCashFlow()
        val investingCashFlow = cashFlowStatement.getInvestingCashFlow()
        val financingCashFlow = cashFlowStatement.getFinancingCashFlow()
        println("  - 영업활동 현금흐름: $operatingCashFlow")
        println("  - 투자활동 현금흐름: $investingCashFlow")
        println("  - 재무활동 현금흐름: $financingCashFlow")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = cashFlowStatement,
            category = RecordingConfig.Paths.Financials.CASH_FLOW,
            fileName = "samsung_cash_flow_$year"
        )
    }
}
