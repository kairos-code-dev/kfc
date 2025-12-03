package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.ReportType
import dev.kairoscode.kfc.domain.financials.StatementType
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * 전체 재무제표 조회 Integration Test
 *
 * getAllFinancials() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 * - 단일 API 호출로 모든 재무제표를 가져옵니다
 */
@DisplayName("FinancialsApi.getAllFinancials() - 전체 재무제표 조회")
class FinancialsApiAllSpec : IntegrationTestBase() {

    @Test
    @DisplayName("특정 법인의 모든 재무제표를 한 번에 조회할 수 있다")
    fun testGetAllFinancials() = integrationTest {
        // Given: 삼성전자 corp_code (고정: 2023년)
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023

        // When: 전체 재무제표 조회
        val financialStatements = client.financials?.getAllFinancials(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.ANNUAL,
            statementType = StatementType.CONSOLIDATED
        ) ?: return@integrationTest

        // Then: 모든 재무제표 데이터 반환
        println("✅ 삼성전자 ${year}년 전체 재무제표 조회 성공")
        println("  - 손익계산서: ${financialStatements.incomeStatement?.lineItems?.size ?: 0}개 항목")
        println("  - 재무상태표: ${financialStatements.balanceSheet?.lineItems?.size ?: 0}개 항목")
        println("  - 현금흐름표: ${financialStatements.cashFlowStatement?.lineItems?.size ?: 0}개 항목")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = financialStatements,
            category = RecordingConfig.Paths.Financials.ALL_FINANCIALS,
            fileName = "samsung_all_financials_$year"
        )
    }

    @Test
    @DisplayName("카카오의 전체 재무제표를 조회할 수 있다")
    fun testGetAllFinancialsKakao() = integrationTest {
        // Given: 카카오 corp_code (고정: 2023년)
        val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
        val year = 2023

        // When: 전체 재무제표 조회
        val financialStatements = client.financials?.getAllFinancials(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.ANNUAL,
            statementType = StatementType.CONSOLIDATED
        ) ?: return@integrationTest

        // Then: 모든 재무제표 데이터 반환
        println("✅ 카카오 ${year}년 전체 재무제표 조회 성공")
        println("  - 손익계산서: ${financialStatements.incomeStatement?.lineItems?.size ?: 0}개 항목")
        println("  - 재무상태표: ${financialStatements.balanceSheet?.lineItems?.size ?: 0}개 항목")
        println("  - 현금흐름표: ${financialStatements.cashFlowStatement?.lineItems?.size ?: 0}개 항목")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = financialStatements,
            category = RecordingConfig.Paths.Financials.ALL_FINANCIALS,
            fileName = "kakao_all_financials_$year"
        )
    }
}
