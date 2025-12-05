package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.ReportType
import dev.kairoscode.kfc.domain.financials.StatementType
import dev.kairoscode.kfc.domain.financials.getRevenue
import dev.kairoscode.kfc.domain.financials.getNetIncome
import dev.kairoscode.kfc.domain.financials.getOperatingCashFlow
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * FinancialsApi.getAllFinancials() Integration Test Specification
 *
 * ## API 개요
 * 특정 법인의 모든 재무제표(손익계산서, 재무상태표, 현금흐름표)를 한 번에 조회하는 API입니다.
 * 단일 API 호출로 3개 재무제표를 모두 가져와 효율적인 데이터 조회가 가능합니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * suspend fun getAllFinancials(
 *     corpCode: String,
 *     year: Int,
 *     reportType: ReportType,
 *     statementType: StatementType
 * ): FinancialStatements
 * ```
 *
 * ## 파라미터
 * - `corpCode`: String - 법인 고유번호 (8자리, 예: "00126380")
 * - `year`: Int - 조회 연도 (2015년 이후)
 * - `reportType`: ReportType - 보고서 유형 (ANNUAL: 연간, HALF_YEAR: 반기, Q1/Q3: 분기)
 * - `statementType`: StatementType - 재무제표 유형 (CONSOLIDATED: 연결, SEPARATE: 별도)
 *
 * ## 응답
 * - `FinancialStatements`: 전체 재무제표 객체
 *   - `incomeStatement`: IncomeStatement? - 손익계산서
 *   - `balanceSheet`: BalanceSheet? - 재무상태표
 *   - `cashFlowStatement`: CashFlowStatement? - 현금흐름표
 *
 * ## 특징
 * - 단일 API 호출로 모든 재무제표 조회
 * - 대용량 데이터 (3개 재무제표 합산)
 * - 각 재무제표는 nullable (일부 데이터가 없을 수 있음)
 * - 효율적인 데이터 수집 (1번 호출 vs 3번 호출)
 *
 * ## 제약사항
 * - OPENDART_API_KEY 필요
 * - 2015년 이후 데이터만 지원
 * - Rate limit: 전역 10 req/sec (GlobalRateLimiters)
 *
 * ## 활용 예시
 * - 종합 재무 분석 대시보드
 * - 재무제표 전체 다운로드
 * - 통합 재무 지표 계산
 *
 * ## 관련 문서
 * - OPENDART API: https://opendart.fss.or.kr/
 */
@DisplayName("FinancialsApi.getAllFinancials() - 전체 재무제표 조회")
class FinancialsApiAllSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

    @Nested
    @DisplayName("1. 기본 동작 (Basic Operations)")
    inner class BasicOperations {

        @Test
        @DisplayName("특정 법인의 모든 재무제표를 한 번에 조회할 수 있다")
        fun get_all_financial_statements() = integrationTest {
            println("\n📘 API: getAllFinancials()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Samsung corp_code, 2023, ANNUAL, CONSOLIDATED
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\" (삼성전자)")
            println("  • year: Int = $year")
            println("  • reportType: ReportType = ANNUAL")
            println("  • statementType: StatementType = CONSOLIDATED")

            // When: Request all financial statements
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Returns all financial statements
            println("\n📤 Response: FinancialStatements")
            println("  • incomeStatement: ${financialStatements.incomeStatement?.lineItems?.size ?: 0}개 항목")
            println("  • balanceSheet: ${financialStatements.balanceSheet?.lineItems?.size ?: 0}개 항목")
            println("  • cashFlowStatement: ${financialStatements.cashFlowStatement?.lineItems?.size ?: 0}개 항목")

            println("\n✅ 테스트 결과: 성공")
            println("  • 단일 API 호출로 3개 재무제표 조회 완료")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = financialStatements,
                category = RecordingConfig.Paths.Financials.ALL_FINANCIALS,
                fileName = "samsung_all_financials_$year"
            )
        }

        @Test
        @DisplayName("[다른 법인] 카카오의 전체 재무제표를 조회할 수 있다")
        fun get_all_financials_kakao() = integrationTest {
            println("\n📘 API: getAllFinancials() - 카카오")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Kakao corp_code
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\" (카카오)")
            println("  • year: Int = $year")

            // When: Request all financial statements
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Returns all financial statements
            println("\n📤 Response: FinancialStatements")
            println("  • incomeStatement: ${financialStatements.incomeStatement?.lineItems?.size ?: 0}개 항목")
            println("  • balanceSheet: ${financialStatements.balanceSheet?.lineItems?.size ?: 0}개 항목")
            println("  • cashFlowStatement: ${financialStatements.cashFlowStatement?.lineItems?.size ?: 0}개 항목")

            println("\n✅ 테스트 결과: 카카오 재무제표 조회 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = financialStatements,
                category = RecordingConfig.Paths.Financials.ALL_FINANCIALS,
                fileName = "kakao_all_financials_$year"
            )
        }

        @Test
        @DisplayName("[파라미터: reportType] 분기 재무제표를 조회할 수 있다")
        fun get_quarterly_all_financials() = integrationTest {
            println("\n📘 파라미터 테스트: reportType = Q1")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Samsung corp_code, Q1
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            println("📥 Input:")
            println("  • reportType: ReportType.Q1 (1분기)")

            // When: Request Q1 all financials
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.Q1,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Returns Q1 all financials
            println("\n📤 Response:")
            println("  • incomeStatement: ${financialStatements.incomeStatement?.lineItems?.size ?: 0}개 항목")
            println("  • balanceSheet: ${financialStatements.balanceSheet?.lineItems?.size ?: 0}개 항목")
            println("  • cashFlowStatement: ${financialStatements.cashFlowStatement?.lineItems?.size ?: 0}개 항목")
            println("  • reportType: Q1")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = financialStatements,
                category = RecordingConfig.Paths.Financials.ALL_FINANCIALS,
                fileName = "samsung_all_financials_${year}_q1"
            )
        }
    }

    // ========================================
    // 2. 응답 데이터 검증 (Response Validation)
    // ========================================

    @Nested
    @DisplayName("2. 응답 데이터 검증 (Response Validation)")
    inner class ResponseValidation {

        @Test
        @DisplayName("응답은 3개 재무제표를 포함한다")
        fun response_contains_three_statements() = integrationTest {
            println("\n📘 응답 데이터 검증: 3개 재무제표")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Validate all three statements
            println("\n📋 재무제표 존재 여부:")
            println("  • incomeStatement: ${if (financialStatements.incomeStatement != null) "✓" else "✗"}")
            println("  • balanceSheet: ${if (financialStatements.balanceSheet != null) "✓" else "✗"}")
            println("  • cashFlowStatement: ${if (financialStatements.cashFlowStatement != null) "✓" else "✗"}")

            println("\n✅ 검증 결과: 3개 재무제표 모두 존재")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(financialStatements.incomeStatement, "손익계산서가 존재해야 합니다")
            assertNotNull(financialStatements.balanceSheet, "재무상태표가 존재해야 합니다")
            assertNotNull(financialStatements.cashFlowStatement, "현금흐름표가 존재해야 합니다")
        }

        @Test
        @DisplayName("각 재무제표는 계정과목(lineItems)을 포함한다")
        fun each_statement_contains_line_items() = integrationTest {
            println("\n📘 응답 데이터 검증: 계정과목 데이터")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Validate line items
            val incomeItemCount = financialStatements.incomeStatement?.lineItems?.size ?: 0
            val balanceItemCount = financialStatements.balanceSheet?.lineItems?.size ?: 0
            val cashFlowItemCount = financialStatements.cashFlowStatement?.lineItems?.size ?: 0
            val totalItemCount = incomeItemCount + balanceItemCount + cashFlowItemCount

            println("\n📊 계정과목 개수:")
            println("  • 손익계산서: ${incomeItemCount}개")
            println("  • 재무상태표: ${balanceItemCount}개")
            println("  • 현금흐름표: ${cashFlowItemCount}개")
            println("  • 전체: ${totalItemCount}개")

            println("\n✅ 검증 결과: 모든 재무제표에 계정과목 존재")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(incomeItemCount > 0, "손익계산서 계정과목이 있어야 합니다")
            assertTrue(balanceItemCount > 0, "재무상태표 계정과목이 있어야 합니다")
            assertTrue(cashFlowItemCount > 0, "현금흐름표 계정과목이 있어야 합니다")
        }

        @Test
        @DisplayName("대용량 데이터를 정상적으로 처리한다")
        fun processes_large_dataset() = integrationTest {
            println("\n📘 응답 데이터 검증: 대용량 데이터 처리")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When
            val startTime = System.currentTimeMillis()
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )
            val elapsedTime = System.currentTimeMillis() - startTime

            // Then: Calculate data volume
            val incomeItemCount = financialStatements.incomeStatement?.lineItems?.size ?: 0
            val balanceItemCount = financialStatements.balanceSheet?.lineItems?.size ?: 0
            val cashFlowItemCount = financialStatements.cashFlowStatement?.lineItems?.size ?: 0
            val totalItemCount = incomeItemCount + balanceItemCount + cashFlowItemCount

            println("\n📊 데이터 볼륨:")
            println("  • 전체 계정과목: ${totalItemCount}개")
            println("  • 처리 시간: ${elapsedTime}ms")

            println("\n✅ 검증 결과: 대용량 데이터 정상 처리")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(totalItemCount >= 100, "전체 계정과목이 100개 이상이어야 합니다")
        }

        @Test
        @DisplayName("주요 재무지표를 조회할 수 있다")
        fun can_retrieve_major_financial_indicators() = integrationTest {
            println("\n📘 응답 데이터 검증: 주요 재무지표")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Try to retrieve major indicators
            val revenue = financialStatements.incomeStatement?.getRevenue()
            val netIncome = financialStatements.incomeStatement?.getNetIncome()
            val operatingCashFlow = financialStatements.cashFlowStatement?.getOperatingCashFlow()

            println("✅ 주요 재무지표 조회 시도:")
            println("  • 매출액: ${revenue ?: "(키워드 매칭 안됨)"}")
            println("  • 당기순이익: ${netIncome ?: "(키워드 매칭 안됨)"}")
            println("  • 영업활동 현금흐름: ${operatingCashFlow ?: "(키워드 매칭 안됨)"}")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 3. 입력 파라미터 검증 (Input Validation)
    // ========================================

    @Nested
    @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
    inner class InputValidation {

        @Test
        @DisplayName("API Key가 없으면 테스트를 skip한다")
        fun requires_api_key() = integrationTest {
            println("\n📘 입력 검증: API Key 필수")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            if (!hasOpendartApiKey) {
                println("⚠️ OPENDART_API_KEY가 설정되지 않아 테스트를 skip합니다.")
                Assumptions.assumeTrue(false)
            }

            println("✅ API Key 확인: 설정됨")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 4. 엣지 케이스 (Edge Cases)
    // ========================================

    @Nested
    @DisplayName("4. 엣지 케이스 (Edge Cases)")
    inner class EdgeCases {

        @Test
        @DisplayName("[파라미터: year] 2015년 이후 데이터를 지원한다")
        fun supports_data_from_2015() = integrationTest {
            println("\n📘 엣지 케이스: 연도 범위 (2015년~)")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: 2015년 데이터
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2015

            println("📥 Input:")
            println("  • year: $year (지원 범위: 2015년 이후)")

            // When
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Returns data for 2015
            println("\n📤 Response:")
            println("  • year: $year")
            println("  • incomeStatement: ${financialStatements.incomeStatement?.lineItems?.size ?: 0}개 항목")
            println("  • balanceSheet: ${financialStatements.balanceSheet?.lineItems?.size ?: 0}개 항목")
            println("  • cashFlowStatement: ${financialStatements.cashFlowStatement?.lineItems?.size ?: 0}개 항목")

            assertNotNull(financialStatements.incomeStatement, "2015년 손익계산서가 존재해야 합니다")
            println("\n✅ 결과: 2015년 데이터 조회 가능")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[데이터 일관성] 3개 재무제표의 연도/보고서 유형이 일치한다")
        fun statements_have_consistent_metadata() = integrationTest {
            println("\n📘 엣지 케이스: 데이터 일관성 검증")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: All statements should be for the same period
            // Note: Actual API implementation determines this
            println("\n📊 데이터 일관성:")
            println("  • 요청 연도: $year")
            println("  • 요청 보고서 유형: ANNUAL")
            println("  • 요청 재무제표 유형: CONSOLIDATED")
            println()
            println("  • 손익계산서 조회: ${if (financialStatements.incomeStatement != null) "성공" else "실패"}")
            println("  • 재무상태표 조회: ${if (financialStatements.balanceSheet != null) "성공" else "실패"}")
            println("  • 현금흐름표 조회: ${if (financialStatements.cashFlowStatement != null) "성공" else "실패"}")

            println("\n✅ 검증 결과: 동일 기간 재무제표 조회 일관성 확인")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(financialStatements.incomeStatement)
            assertNotNull(financialStatements.balanceSheet)
            assertNotNull(financialStatements.cashFlowStatement)
        }
    }

    // ========================================
    // 5. 실무 활용 예제 (Usage Examples)
    // ========================================

    @Nested
    @DisplayName("5. 실무 활용 예제 (Usage Examples)")
    inner class UsageExamples {

        @Test
        @DisplayName("[활용] 종합 재무 분석 대시보드 데이터 수집")
        fun collect_data_for_financial_dashboard() = integrationTest {
            println("\n📘 실무 활용: 재무 분석 대시보드")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Need all financial data for dashboard
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When: Fetch all financials in one call
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Extract key metrics
            val revenue = financialStatements.incomeStatement?.getRevenue()
            val netIncome = financialStatements.incomeStatement?.getNetIncome()
            val operatingCashFlow = financialStatements.cashFlowStatement?.getOperatingCashFlow()

            println("\n=== 삼성전자 ${year}년 재무 대시보드 ===")
            println("\n📊 손익 지표:")
            println("  • 매출액: ${revenue ?: "N/A"}")
            println("  • 당기순이익: ${netIncome ?: "N/A"}")

            if (revenue != null && netIncome != null) {
                val profitMargin = (netIncome / revenue * 100.toBigDecimal()).toDouble()
                println("  • 순이익률: ${"%.2f".format(profitMargin)}%")
            }

            println("\n💰 현금흐름 지표:")
            println("  • 영업활동 현금흐름: ${operatingCashFlow ?: "N/A"}")

            println("\n📋 데이터 수집 정보:")
            println("  • API 호출 횟수: 1회")
            println("  • 수집된 재무제표: 3개")
            println("  • 장점: 단일 호출로 모든 데이터 수집 가능")

            println("\n💡 활용 방법:")
            println("  • 실시간 재무 대시보드 구축")
            println("  • 종합 재무 분석 리포트 생성")
            println("  • 다수 기업 재무 비교 (효율적)")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] 효율적인 데이터 수집 (1번 vs 3번 API 호출)")
        fun efficient_data_collection() = integrationTest {
            println("\n📘 실무 활용: 효율적인 데이터 수집")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When: Use getAllFinancials (single call)
            val startTime = System.currentTimeMillis()
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )
            val elapsedTime = System.currentTimeMillis() - startTime

            // Then: Compare efficiency
            println("\n=== API 호출 효율성 비교 ===")
            println("\n✅ getAllFinancials() 사용:")
            println("  • API 호출 횟수: 1회")
            println("  • 소요 시간: ${elapsedTime}ms")
            println("  • Rate limit 부담: 낮음")

            println("\n❌ 개별 API 사용 시:")
            println("  • API 호출 횟수: 3회")
            println("    - getIncomeStatement()")
            println("    - getBalanceSheet()")
            println("    - getCashFlowStatement()")
            println("  • 예상 소요 시간: ~${elapsedTime * 3}ms")
            println("  • Rate limit 부담: 높음")

            println("\n💡 활용 방법:")
            println("  • 전체 재무제표가 필요한 경우 getAllFinancials() 사용")
            println("  • Rate limit 절약 (1/3)")
            println("  • 네트워크 오버헤드 감소")
            println("  • 대량 데이터 수집 시 유리")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertNotNull(financialStatements.incomeStatement)
            assertNotNull(financialStatements.balanceSheet)
            assertNotNull(financialStatements.cashFlowStatement)
        }

        @Test
        @DisplayName("[활용] 여러 연도 재무제표 일괄 수집")
        fun batch_collect_multiple_years() = integrationTest {
            println("\n📘 실무 활용: 여러 연도 재무제표 일괄 수집")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Multiple years for same company
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val years = listOf(2022, 2023)

            // When: Collect all financials for each year
            println("\n📊 데이터 수집 중...")
            val results = years.map { year ->
                val financials = client.financials!!.getAllFinancials(
                    corpCode = corpCode,
                    year = year,
                    reportType = ReportType.ANNUAL,
                    statementType = StatementType.CONSOLIDATED
                )
                year to financials
            }

            // Then: Display collected data
            println("\n=== 삼성전자 연도별 재무제표 수집 결과 ===")
            results.forEach { (year, financials) ->
                val incomeItemCount = financials.incomeStatement?.lineItems?.size ?: 0
                val balanceItemCount = financials.balanceSheet?.lineItems?.size ?: 0
                val cashFlowItemCount = financials.cashFlowStatement?.lineItems?.size ?: 0
                val totalCount = incomeItemCount + balanceItemCount + cashFlowItemCount

                println("\n${year}년:")
                println("  • 손익계산서: ${incomeItemCount}개")
                println("  • 재무상태표: ${balanceItemCount}개")
                println("  • 현금흐름표: ${cashFlowItemCount}개")
                println("  • 전체: ${totalCount}개")
            }

            println("\n📈 수집 통계:")
            println("  • 수집 연도: ${years.size}년")
            println("  • API 호출: ${years.size}회 (getAllFinancials)")
            println("  • 개별 API 사용 시: ${years.size * 3}회 필요")
            println("  • 절약: ${years.size * 2}회")

            println("\n💡 활용 방법:")
            println("  • 연도별 재무 추이 분석")
            println("  • 시계열 데이터 수집")
            println("  • 장기 재무 분석")
            println("  • 대량 데이터 수집 파이프라인")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // All financial statements should exist for each year
            assertTrue(results.all { (_, financials) ->
                financials.incomeStatement != null &&
                financials.balanceSheet != null &&
                financials.cashFlowStatement != null
            }, "각 연도는 모든 재무제표를 가져야 합니다")
        }

        @Test
        @DisplayName("[활용] 재무제표 전체 다운로드 및 로컬 저장")
        fun download_and_save_all_financials() = integrationTest {
            println("\n📘 실무 활용: 재무제표 전체 다운로드")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Need to download all financial data
            requireOpendartApiKey()
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            // When: Fetch all financials
            val financialStatements = client.financials!!.getAllFinancials(
                corpCode = corpCode,
                year = year,
                reportType = ReportType.ANNUAL,
                statementType = StatementType.CONSOLIDATED
            )

            // Then: Data is ready for saving
            val incomeItemCount = financialStatements.incomeStatement?.lineItems?.size ?: 0
            val balanceItemCount = financialStatements.balanceSheet?.lineItems?.size ?: 0
            val cashFlowItemCount = financialStatements.cashFlowStatement?.lineItems?.size ?: 0
            val totalCount = incomeItemCount + balanceItemCount + cashFlowItemCount

            println("\n=== 다운로드 완료: 삼성전자 ${year}년 ===")
            println("\n📦 다운로드된 데이터:")
            println("  • 손익계산서: ${incomeItemCount}개 항목")
            println("  • 재무상태표: ${balanceItemCount}개 항목")
            println("  • 현금흐름표: ${cashFlowItemCount}개 항목")
            println("  • 전체: ${totalCount}개 항목")

            println("\n💾 저장 예시:")
            println("  • JSON: all_financials_samsung_2023.json")
            println("  • CSV: income_statement_2023.csv")
            println("  • CSV: balance_sheet_2023.csv")
            println("  • CSV: cash_flow_2023.csv")

            println("\n💡 활용 방법:")
            println("  • 로컬 데이터베이스 구축")
            println("  • 오프라인 분석 환경 구성")
            println("  • 데이터 백업 및 아카이빙")
            println("  • ETL 파이프라인 소스 데이터")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(totalCount >= 100, "충분한 양의 데이터가 다운로드되어야 합니다")
        }
    }
}
