package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.ReportType
import dev.kairoscode.kfc.domain.financials.StatementType
import dev.kairoscode.kfc.domain.financials.getTotalAssets
import dev.kairoscode.kfc.domain.financials.getTotalLiabilities
import dev.kairoscode.kfc.domain.financials.getTotalEquity
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * 재무상태표 조회 Integration Test
 *
 * getBalanceSheet() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 */
@DisplayName("FinancialsApi.getBalanceSheet() - 재무상태표 조회")
class FinancialsApiBalanceSheetSpec : IntegrationTestBase() {

    @Test
    @DisplayName("특정 법인의 연결 재무상태표를 조회할 수 있다")
    fun testGetBalanceSheet() = integrationTest {
        // Given: 삼성전자 corp_code (고정: 2023년)
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023

        // When: 재무상태표 조회
        val balanceSheet = client.financials?.getBalanceSheet(
            corpCode = corpCode,
            year = year,
            reportType = ReportType.ANNUAL,
            statementType = StatementType.CONSOLIDATED
        ) ?: return@integrationTest

        // Then: 재무상태표 데이터 반환
        println("✅ 삼성전자 ${year}년 연결 재무상태표 항목 개수: ${balanceSheet.lineItems.size}")

        // 주요 계정과목 조회
        val totalAssets = balanceSheet.getTotalAssets()
        val totalLiabilities = balanceSheet.getTotalLiabilities()
        val totalEquity = balanceSheet.getTotalEquity()
        println("  - 자산총계: $totalAssets")
        println("  - 부채총계: $totalLiabilities")
        println("  - 자본총계: $totalEquity")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = balanceSheet,
            category = RecordingConfig.Paths.Financials.BALANCE_SHEET,
            fileName = "samsung_balance_sheet_$year"
        )
    }
}
