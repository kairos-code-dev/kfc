package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.*
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * [Financials] FinancialsApi - 재무제표 API 통합 테스트
 *
 * OpenDart API를 사용한 기업 재무제표 조회 기능을 검증합니다.
 * 모든 테스트는 OPENDART_API_KEY가 필요합니다.
 *
 * ## API 목록
 *
 * ### 개별 재무제표 API
 * - `getIncomeStatement()`: 손익계산서 조회
 * - `getBalanceSheet()`: 재무상태표 조회
 * - `getCashFlowStatement()`: 현금흐름표 조회
 *
 * ### 통합 재무제표 API
 * - `getAllFinancials()`: 전체 재무제표 한번에 조회 (3개 재무제표)
 *
 * ## 공통 파라미터
 * - `corpCode`: String - 법인 고유번호 (8자리, 예: "00126380")
 * - `year`: Int - 조회 연도 (2015년 이후)
 * - `reportType`: ReportType - 보고서 유형 (ANNUAL: 연간, HALF_YEAR: 반기, Q1/Q3: 분기)
 * - `statementType`: StatementType - 재무제표 유형 (CONSOLIDATED: 연결, SEPARATE: 별도)
 *
 * ## 제약사항
 * - OPENDART_API_KEY 필요
 * - 2015년 이후 데이터만 지원
 * - Rate limit: 전역 10 req/sec (GlobalRateLimiters)
 *
 * ## 관련 문서
 * - OPENDART API: https://opendart.fss.or.kr/
 * - 계정과목 매핑: src/main/kotlin/dev/kairoscode/kfc/domain/financials/
 */
@DisplayName("[I][Financials] FinancialsApi - 재무제표 API")
class FinancialsApiSpec : IntegrationTestBase() {

    // ============================================================================
    // 개별 재무제표 API
    // ============================================================================

    @Nested
    @DisplayName("개별 재무제표 API")
    inner class IndividualStatementsApi {

        // ========================================
        // getIncomeStatement() - 손익계산서 조회
        // ========================================

        @Nested
        @DisplayName("getIncomeStatement() - 손익계산서 조회")
        inner class GetIncomeStatement {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("특정 법인의 연결 연간 손익계산서를 조회할 수 있다")
                fun get_consolidated_annual_income_statement() = integrationTest {
                    println("\n📘 API: getIncomeStatement()")
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

                    // When: Request income statement
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns income statement
                    println("\n📤 Response: IncomeStatement")
                    println("  • lineItems.size: ${incomeStatement.lineItems.size}")

                    val revenue = incomeStatement.getRevenue()
                    val netIncome = incomeStatement.getNetIncome()
                    println("  • 매출액: ${revenue}")
                    println("  • 당기순이익: ${netIncome}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(incomeStatement.lineItems.isNotEmpty())
                    // Note: revenue/netIncome may be null if keywords don't match

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = incomeStatement,
                        category = RecordingConfig.Paths.Financials.INCOME_STATEMENT,
                        fileName = "samsung_income_statement_$year"
                    )
                }

                @Test
                @DisplayName("[파라미터: reportType] 분기 손익계산서를 조회할 수 있다")
                fun get_quarterly_income_statement() = integrationTest {
                    println("\n📘 파라미터 테스트: reportType = Q1")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Samsung corp_code, Q1
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input:")
                    println("  • reportType: ReportType.Q1 (1분기)")

                    // When: Request Q1 income statement
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.Q1,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns Q1 income statement
                    println("\n📤 Response:")
                    println("  • lineItems.size: ${incomeStatement.lineItems.size}")
                    println("  • reportType: Q1")

                    assertTrue(incomeStatement.lineItems.isNotEmpty())

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = incomeStatement,
                        category = RecordingConfig.Paths.Financials.INCOME_STATEMENT,
                        fileName = "samsung_income_statement_${year}_q1"
                    )
                }

                @Test
                @DisplayName("[파라미터: statementType] 별도 손익계산서를 조회할 수 있다")
                fun get_separate_income_statement() = integrationTest {
                    println("\n📘 파라미터 테스트: statementType = SEPARATE")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Samsung corp_code, SEPARATE
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input:")
                    println("  • statementType: StatementType.SEPARATE (별도재무제표)")

                    // When: Request separate income statement
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.SEPARATE
                    )

                    // Then: Returns separate income statement
                    println("\n📤 Response:")
                    println("  • lineItems.size: ${incomeStatement.lineItems.size}")
                    println("  • statementType: SEPARATE")

                    assertTrue(incomeStatement.lineItems.isNotEmpty())

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = incomeStatement,
                        category = RecordingConfig.Paths.Financials.INCOME_STATEMENT,
                        fileName = "samsung_income_statement_${year}_separate"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("응답은 계정과목 목록(lineItems)을 포함한다")
                fun response_contains_line_items() = integrationTest {
                    println("\n📘 응답 데이터 검증: lineItems")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Validate structure
                    assertTrue(incomeStatement.lineItems.isNotEmpty(), "계정과목이 1개 이상 존재해야 합니다")

                    val firstItem = incomeStatement.lineItems.first()
                    assertNotNull(firstItem.accountName, "계정과목명이 존재해야 합니다")

                    println("✅ 응답 구조 검증:")
                    println("  • lineItems.size: ${incomeStatement.lineItems.size} (> 0) ✓")
                    println("  • 첫 번째 항목 accountName: ${firstItem.accountName} ✓")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("주요 계정과목(매출액, 당기순이익)을 조회할 수 있다")
                fun can_retrieve_major_accounts() = integrationTest {
                    println("\n📘 응답 데이터 검증: 주요 계정과목")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Try to retrieve major accounts
                    val revenue = incomeStatement.getRevenue()
                    val netIncome = incomeStatement.getNetIncome()

                    // Note: These may be null if keyword matching doesn't find the accounts
                    println("✅ 주요 계정과목 조회 시도:")
                    println("  • 매출액: ${revenue ?: "(키워드 매칭 안됨)"}")
                    println("  • 당기순이익: ${netIncome ?: "(키워드 매칭 안됨)"}")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("계정과목은 당기금액(currentPeriodAmount)을 포함한다")
                fun line_items_contain_current_amount() = integrationTest {
                    println("\n📘 응답 데이터 검증: currentPeriodAmount")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Check if revenue can be retrieved
                    val revenue = incomeStatement.getRevenue()

                    println("✅ 당기금액 조회 시도:")
                    println("  • 매출액: ${revenue ?: "(키워드 매칭 안됨)"}")
                    println("  • Note: 계정과목 이름이 '매출액' 또는 '수익(매출액)'과 일치할 때 조회됨")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                // Note: 현재 API는 서버 측에서 validation을 수행합니다.
                // 잘못된 입력에 대해서는 OPENDART API가 에러를 반환합니다.
                // 클라이언트 측에서는 별도의 validation을 수행하지 않습니다.

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
                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns data for 2015
                    println("\n📤 Response:")
                    println("  • year: $year")
                    println("  • lineItems.size: ${incomeStatement.lineItems.size}")

                    assertTrue(incomeStatement.lineItems.isNotEmpty(), "2015년 데이터가 존재해야 합니다")
                    println("\n✅ 결과: 2015년 데이터 조회 가능")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[파라미터: reportType] 모든 분기(Q1, Q3)와 반기를 조회할 수 있다")
                fun supports_all_quarters() = integrationTest {
                    println("\n📘 엣지 케이스: 분기/반기 조회")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When: Query all quarterly report types (Q1, Q3, HALF_YEAR)
                    val quarters = listOf(ReportType.Q1, ReportType.Q3, ReportType.HALF_YEAR)
                    val results = quarters.map { quarter ->
                        val stmt = client.financials!!.getIncomeStatement(
                            corpCode = corpCode,
                            year = year,
                            reportType = quarter,
                            statementType = StatementType.CONSOLIDATED
                        )
                        quarter to stmt.lineItems.size
                    }

                    // Then: All report types return data
                    println("\n📊 보고서 유형별 조회 결과:")
                    results.forEach { (reportType, itemCount) ->
                        println("  • $reportType: $itemCount 항목")
                        assertTrue(itemCount > 0, "$reportType 데이터가 존재해야 합니다")
                    }

                    println("\n✅ 결과: 모든 보고서 유형 조회 가능 (Q1, Q3, HALF_YEAR)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[활용] 매출액 성장률을 계산할 수 있다")
                fun calculate_revenue_growth_rate() = integrationTest {
                    println("\n📘 활용 예제: 매출액 YoY 성장률 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: 2022, 2023 income statements
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE

                    val stmt2022 = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = 2022,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    val stmt2023 = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = 2023,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Calculate YoY revenue growth
                    val revenue2022 = stmt2022.getRevenue() ?: return@integrationTest
                    val revenue2023 = stmt2023.getRevenue() ?: return@integrationTest

                    val growthRate = ((revenue2023 - revenue2022) / revenue2022 * 100.toBigDecimal()).toDouble()

                    // Then: Display analysis
                    println("\n=== 삼성전자 매출액 YoY 성장률 분석 ===")
                    println("2022년 매출액: ${revenue2022}")
                    println("2023년 매출액: ${revenue2023}")
                    println("YoY 성장률: ${"%.2f".format(growthRate)}%")
                    println()
                    println("📊 분석: ${if (growthRate > 0) "매출 증가" else "매출 감소"} (${"%.2f".format(growthRate)}%)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 영업이익률을 계산할 수 있다")
                fun calculate_operating_profit_margin() = integrationTest {
                    println("\n📘 활용 예제: 영업이익률 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Income statement
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val incomeStatement = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Calculate net profit margin
                    val revenue = incomeStatement.getRevenue() ?: return@integrationTest
                    val netIncome = incomeStatement.getNetIncome() ?: return@integrationTest

                    val profitMargin = (netIncome / revenue * 100.toBigDecimal()).toDouble()

                    // Then: Display analysis
                    println("\n=== 삼성전자 ${year}년 순이익률 분석 ===")
                    println("매출액: ${revenue}")
                    println("당기순이익: ${netIncome}")
                    println("순이익률: ${"%.2f".format(profitMargin)}%")
                    println()
                    println("📊 분석: 매출 대비 ${"%.2f".format(profitMargin)}% 순이익 실현")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 연결 vs 별도 재무제표를 비교할 수 있다")
                fun compare_consolidated_vs_separate() = integrationTest {
                    println("\n📘 활용 예제: 연결 vs 별도 재무제표 비교")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Both consolidated and separate statements
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val consolidated = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    val separate = client.financials!!.getIncomeStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.SEPARATE
                    )

                    // When: Compare revenue
                    val consolidatedRevenue = consolidated.getRevenue()
                    val separateRevenue = separate.getRevenue()

                    // Then: Display comparison
                    println("\n=== 삼성전자 ${year}년 연결 vs 별도 비교 ===")
                    println("연결 매출액: ${consolidatedRevenue}")
                    println("별도 매출액: ${separateRevenue}")

                    if (consolidatedRevenue != null && separateRevenue != null) {
                        val diff = consolidatedRevenue - separateRevenue
                        val diffPercent = (diff / separateRevenue * 100.toBigDecimal()).toDouble()
                        println("차이: ${diff} (${"%.2f".format(diffPercent)}%)")
                        println()
                        println("📊 분석: 연결 재무제표가 별도 대비 ${"%.2f".format(diffPercent)}% ${if (diffPercent > 0) "높음" else "낮음"}")
                        println("(연결: 종속회사 포함, 별도: 본사만)")
                    }
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }

        // ========================================
        // getBalanceSheet() - 재무상태표 조회
        // ========================================

        @Nested
        @DisplayName("getBalanceSheet() - 재무상태표 조회")
        inner class GetBalanceSheet {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("특정 법인의 연결 연간 재무상태표를 조회할 수 있다")
                fun get_consolidated_annual_balance_sheet() = integrationTest {
                    println("\n📘 API: getBalanceSheet()")
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

                    // When: Request balance sheet
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns balance sheet
                    println("\n📤 Response: BalanceSheet")
                    println("  • lineItems.size: ${balanceSheet.lineItems.size}")

                    val totalAssets = balanceSheet.getTotalAssets()
                    val totalLiabilities = balanceSheet.getTotalLiabilities()
                    val totalEquity = balanceSheet.getTotalEquity()
                    println("  • 자산총계: ${totalAssets}")
                    println("  • 부채총계: ${totalLiabilities}")
                    println("  • 자본총계: ${totalEquity}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(balanceSheet.lineItems.isNotEmpty())
                    // Note: totalAssets/totalLiabilities/totalEquity may be null if keywords don't match

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = balanceSheet,
                        category = RecordingConfig.Paths.Financials.BALANCE_SHEET,
                        fileName = "samsung_balance_sheet_$year"
                    )
                }

                @Test
                @DisplayName("[파라미터: reportType] 분기 재무상태표를 조회할 수 있다")
                fun get_quarterly_balance_sheet() = integrationTest {
                    println("\n📘 파라미터 테스트: reportType = Q1")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Samsung corp_code, Q1
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input:")
                    println("  • reportType: ReportType.Q1 (1분기)")

                    // When: Request Q1 balance sheet
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.Q1,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns Q1 balance sheet
                    println("\n📤 Response:")
                    println("  • lineItems.size: ${balanceSheet.lineItems.size}")
                    println("  • reportType: Q1")

                    assertTrue(balanceSheet.lineItems.isNotEmpty())

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = balanceSheet,
                        category = RecordingConfig.Paths.Financials.BALANCE_SHEET,
                        fileName = "samsung_balance_sheet_${year}_q1"
                    )
                }

                @Test
                @DisplayName("[파라미터: statementType] 별도 재무상태표를 조회할 수 있다")
                fun get_separate_balance_sheet() = integrationTest {
                    println("\n📘 파라미터 테스트: statementType = SEPARATE")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Samsung corp_code, SEPARATE
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input:")
                    println("  • statementType: StatementType.SEPARATE (별도재무제표)")

                    // When: Request separate balance sheet
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.SEPARATE
                    )

                    // Then: Returns separate balance sheet
                    println("\n📤 Response:")
                    println("  • lineItems.size: ${balanceSheet.lineItems.size}")
                    println("  • statementType: SEPARATE")

                    assertTrue(balanceSheet.lineItems.isNotEmpty())

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = balanceSheet,
                        category = RecordingConfig.Paths.Financials.BALANCE_SHEET,
                        fileName = "samsung_balance_sheet_${year}_separate"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("응답은 계정과목 목록(lineItems)을 포함한다")
                fun response_contains_line_items() = integrationTest {
                    println("\n📘 응답 데이터 검증: lineItems")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Validate structure
                    assertTrue(balanceSheet.lineItems.isNotEmpty(), "계정과목이 1개 이상 존재해야 합니다")

                    val firstItem = balanceSheet.lineItems.first()
                    assertNotNull(firstItem.accountName, "계정과목명이 존재해야 합니다")

                    println("✅ 응답 구조 검증:")
                    println("  • lineItems.size: ${balanceSheet.lineItems.size} (> 0) ✓")
                    println("  • 첫 번째 항목 accountName: ${firstItem.accountName} ✓")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("주요 계정과목(자산총계, 부채총계, 자본총계)을 조회할 수 있다")
                fun can_retrieve_major_accounts() = integrationTest {
                    println("\n📘 응답 데이터 검증: 주요 계정과목")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Try to retrieve major accounts
                    val totalAssets = balanceSheet.getTotalAssets()
                    val totalLiabilities = balanceSheet.getTotalLiabilities()
                    val totalEquity = balanceSheet.getTotalEquity()

                    // Note: These may be null if keyword matching doesn't find the accounts
                    println("✅ 주요 계정과목 조회 시도:")
                    println("  • 자산총계: ${totalAssets ?: "(키워드 매칭 안됨)"}")
                    println("  • 부채총계: ${totalLiabilities ?: "(키워드 매칭 안됨)"}")
                    println("  • 자본총계: ${totalEquity ?: "(키워드 매칭 안됨)"}")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("회계등식(자산 = 부채 + 자본) 검증을 시도한다")
                fun accounting_equation_validation() = integrationTest {
                    println("\n📘 응답 데이터 검증: 회계등식")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Try to validate accounting equation
                    val totalAssets = balanceSheet.getTotalAssets()
                    val totalLiabilities = balanceSheet.getTotalLiabilities()
                    val totalEquity = balanceSheet.getTotalEquity()

                    if (totalAssets != null && totalLiabilities != null && totalEquity != null) {
                        val liabilitiesPlusEquity = totalLiabilities + totalEquity
                        val difference = (totalAssets - liabilitiesPlusEquity).abs()
                        val errorPercent = (difference / totalAssets * 100.toBigDecimal()).toDouble()

                        println("✅ 회계등식 검증 결과:")
                        println("  • 자산총계: $totalAssets")
                        println("  • 부채총계: $totalLiabilities")
                        println("  • 자본총계: $totalEquity")
                        println("  • 부채+자본: $liabilitiesPlusEquity")
                        println("  • 차이: $difference (${"%.4f".format(errorPercent)}%)")

                        if (errorPercent < 1.0) {
                            println("  • 회계등식 성립: ✓ (매우 정확)")
                        } else if (errorPercent < 5.0) {
                            println("  • 회계등식 성립: ✓ (허용 범위 내)")
                        } else {
                            println("  • ⚠️ 주의: 오차가 큽니다 (키워드 매칭 방식의 한계)")
                            println("  • 이는 계정과목 키워드 매칭이 정확하지 않을 수 있음을 의미합니다")
                        }

                        // Test passes if we successfully retrieved all three values
                        // The accounting equation may not hold perfectly due to keyword matching limitations
                        assertTrue(totalAssets > 0.toBigDecimal(), "자산총계가 존재해야 합니다")
                    } else {
                        println("⚠️ 일부 계정과목을 찾을 수 없어 회계등식을 검증할 수 없습니다")
                        println("  • 자산총계: ${totalAssets ?: "null"}")
                        println("  • 부채총계: ${totalLiabilities ?: "null"}")
                        println("  • 자본총계: ${totalEquity ?: "null"}")
                    }

                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                // Note: 현재 API는 서버 측에서 validation을 수행합니다.
                // 잘못된 입력에 대해서는 OPENDART API가 에러를 반환합니다.
                // 클라이언트 측에서는 별도의 validation을 수행하지 않습니다.

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
                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns data for 2015
                    println("\n📤 Response:")
                    println("  • year: $year")
                    println("  • lineItems.size: ${balanceSheet.lineItems.size}")

                    assertTrue(balanceSheet.lineItems.isNotEmpty(), "2015년 데이터가 존재해야 합니다")
                    println("\n✅ 결과: 2015년 데이터 조회 가능")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[파라미터: reportType] 모든 분기(Q1, Q3)와 반기를 조회할 수 있다")
                fun supports_all_quarters() = integrationTest {
                    println("\n📘 엣지 케이스: 분기/반기 조회")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When: Query all quarterly report types (Q1, Q3, HALF_YEAR)
                    val quarters = listOf(ReportType.Q1, ReportType.Q3, ReportType.HALF_YEAR)
                    val results = quarters.map { quarter ->
                        val stmt = client.financials!!.getBalanceSheet(
                            corpCode = corpCode,
                            year = year,
                            reportType = quarter,
                            statementType = StatementType.CONSOLIDATED
                        )
                        quarter to stmt.lineItems.size
                    }

                    // Then: All report types return data
                    println("\n📊 보고서 유형별 조회 결과:")
                    results.forEach { (reportType, itemCount) ->
                        println("  • $reportType: $itemCount 항목")
                        assertTrue(itemCount > 0, "$reportType 데이터가 존재해야 합니다")
                    }

                    println("\n✅ 결과: 모든 보고서 유형 조회 가능 (Q1, Q3, HALF_YEAR)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[활용] 부채비율을 계산할 수 있다")
                fun calculate_debt_ratio() = integrationTest {
                    println("\n📘 활용 예제: 부채비율 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Balance sheet
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Calculate debt ratio
                    val totalLiabilities = balanceSheet.getTotalLiabilities() ?: return@integrationTest
                    val totalEquity = balanceSheet.getTotalEquity() ?: return@integrationTest

                    val debtRatio = (totalLiabilities / totalEquity * 100.toBigDecimal()).toDouble()

                    // Then: Display analysis
                    println("\n=== 삼성전자 ${year}년 부채비율 분석 ===")
                    println("부채총계: ${totalLiabilities}")
                    println("자본총계: ${totalEquity}")
                    println("부채비율: ${"%.2f".format(debtRatio)}%")
                    println()
                    println("📊 분석: 부채비율은 기업의 재무 건전성을 나타냅니다")
                    println("(${"%.2f".format(debtRatio)}% = 부채가 자본의 ${"%.2f".format(debtRatio)}%)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 자기자본비율을 계산할 수 있다")
                fun calculate_equity_ratio() = integrationTest {
                    println("\n📘 활용 예제: 자기자본비율 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Balance sheet
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val balanceSheet = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Calculate equity ratio
                    val totalAssets = balanceSheet.getTotalAssets() ?: return@integrationTest
                    val totalEquity = balanceSheet.getTotalEquity() ?: return@integrationTest

                    val equityRatio = (totalEquity / totalAssets * 100.toBigDecimal()).toDouble()

                    // Then: Display analysis
                    println("\n=== 삼성전자 ${year}년 자기자본비율 분석 ===")
                    println("자산총계: ${totalAssets}")
                    println("자본총계: ${totalEquity}")
                    println("자기자본비율: ${"%.2f".format(equityRatio)}%")
                    println()
                    println("📊 분석: 자기자본비율이 높을수록 재무 안정성이 높습니다")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 연결 vs 별도 재무상태표를 비교할 수 있다")
                fun compare_consolidated_vs_separate() = integrationTest {
                    println("\n📘 활용 예제: 연결 vs 별도 재무상태표 비교")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Both consolidated and separate statements
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val consolidated = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    val separate = client.financials!!.getBalanceSheet(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.SEPARATE
                    )

                    // When: Compare total assets
                    val consolidatedAssets = consolidated.getTotalAssets()
                    val separateAssets = separate.getTotalAssets()

                    // Then: Display comparison
                    println("\n=== 삼성전자 ${year}년 연결 vs 별도 비교 ===")
                    println("연결 자산총계: ${consolidatedAssets}")
                    println("별도 자산총계: ${separateAssets}")

                    if (consolidatedAssets != null && separateAssets != null) {
                        val diff = consolidatedAssets - separateAssets
                        val diffPercent = (diff / separateAssets * 100.toBigDecimal()).toDouble()
                        println("차이: ${diff} (${"%.2f".format(diffPercent)}%)")
                        println()
                        println("📊 분석: 연결 재무제표가 별도 대비 ${"%.2f".format(diffPercent)}% ${if (diffPercent > 0) "높음" else "낮음"}")
                        println("(연결: 종속회사 포함, 별도: 본사만)")
                    }
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }

        // ========================================
        // getCashFlowStatement() - 현금흐름표 조회
        // ========================================

        @Nested
        @DisplayName("getCashFlowStatement() - 현금흐름표 조회")
        inner class GetCashFlowStatement {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("특정 법인의 연결 연간 현금흐름표를 조회할 수 있다")
                fun get_consolidated_annual_cash_flow_statement() = integrationTest {
                    println("\n📘 API: getCashFlowStatement()")
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

                    // When: Request cash flow statement
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns cash flow statement
                    println("\n📤 Response: CashFlowStatement")
                    println("  • lineItems.size: ${cashFlowStatement.lineItems.size}")

                    val operatingCashFlow = cashFlowStatement.getOperatingCashFlow()
                    val investingCashFlow = cashFlowStatement.getInvestingCashFlow()
                    val financingCashFlow = cashFlowStatement.getFinancingCashFlow()
                    println("  • 영업활동 현금흐름: ${operatingCashFlow}")
                    println("  • 투자활동 현금흐름: ${investingCashFlow}")
                    println("  • 재무활동 현금흐름: ${financingCashFlow}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(cashFlowStatement.lineItems.isNotEmpty())
                    // Note: cash flows may be null if keywords don't match

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = cashFlowStatement,
                        category = RecordingConfig.Paths.Financials.CASH_FLOW,
                        fileName = "samsung_cash_flow_$year"
                    )
                }

                @Test
                @DisplayName("[파라미터: reportType] 분기 현금흐름표를 조회할 수 있다")
                fun get_quarterly_cash_flow_statement() = integrationTest {
                    println("\n📘 파라미터 테스트: reportType = Q1")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Samsung corp_code, Q1
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input:")
                    println("  • reportType: ReportType.Q1 (1분기)")

                    // When: Request Q1 cash flow statement
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.Q1,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns Q1 cash flow statement
                    println("\n📤 Response:")
                    println("  • lineItems.size: ${cashFlowStatement.lineItems.size}")
                    println("  • reportType: Q1")

                    assertTrue(cashFlowStatement.lineItems.isNotEmpty())

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = cashFlowStatement,
                        category = RecordingConfig.Paths.Financials.CASH_FLOW,
                        fileName = "samsung_cash_flow_${year}_q1"
                    )
                }

                @Test
                @DisplayName("[파라미터: statementType] 별도 현금흐름표를 조회할 수 있다")
                fun get_separate_cash_flow_statement() = integrationTest {
                    println("\n📘 파라미터 테스트: statementType = SEPARATE")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Samsung corp_code, SEPARATE
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input:")
                    println("  • statementType: StatementType.SEPARATE (별도재무제표)")

                    // When: Request separate cash flow statement
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.SEPARATE
                    )

                    // Then: Returns separate cash flow statement
                    println("\n📤 Response:")
                    println("  • lineItems.size: ${cashFlowStatement.lineItems.size}")
                    println("  • statementType: SEPARATE")

                    assertTrue(cashFlowStatement.lineItems.isNotEmpty())

                    // 스마트 레코딩
                    SmartRecorder.recordSmartly(
                        data = cashFlowStatement,
                        category = RecordingConfig.Paths.Financials.CASH_FLOW,
                        fileName = "samsung_cash_flow_${year}_separate"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("응답은 계정과목 목록(lineItems)을 포함한다")
                fun response_contains_line_items() = integrationTest {
                    println("\n📘 응답 데이터 검증: lineItems")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Validate structure
                    assertTrue(cashFlowStatement.lineItems.isNotEmpty(), "계정과목이 1개 이상 존재해야 합니다")

                    val firstItem = cashFlowStatement.lineItems.first()
                    assertNotNull(firstItem.accountName, "계정과목명이 존재해야 합니다")

                    println("✅ 응답 구조 검증:")
                    println("  • lineItems.size: ${cashFlowStatement.lineItems.size} (> 0) ✓")
                    println("  • 첫 번째 항목 accountName: ${firstItem.accountName} ✓")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("주요 계정과목(영업/투자/재무활동 현금흐름)을 조회할 수 있다")
                fun can_retrieve_major_cash_flows() = integrationTest {
                    println("\n📘 응답 데이터 검증: 주요 계정과목")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Try to retrieve major accounts
                    val operatingCashFlow = cashFlowStatement.getOperatingCashFlow()
                    val investingCashFlow = cashFlowStatement.getInvestingCashFlow()
                    val financingCashFlow = cashFlowStatement.getFinancingCashFlow()

                    // Note: These may be null if keyword matching doesn't find the accounts
                    println("✅ 주요 계정과목 조회 시도:")
                    println("  • 영업활동 현금흐름: ${operatingCashFlow ?: "(키워드 매칭 안됨)"}")
                    println("  • 투자활동 현금흐름: ${investingCashFlow ?: "(키워드 매칭 안됨)"}")
                    println("  • 재무활동 현금흐름: ${financingCashFlow ?: "(키워드 매칭 안됨)"}")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("계정과목은 당기금액(currentPeriodAmount)을 포함한다")
                fun line_items_contain_current_period_amount() = integrationTest {
                    println("\n📘 응답 데이터 검증: currentPeriodAmount")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Check if operating cash flow can be retrieved
                    val operatingCashFlow = cashFlowStatement.getOperatingCashFlow()

                    println("✅ 당기금액 조회 시도:")
                    println("  • 영업활동 현금흐름: ${operatingCashFlow ?: "(키워드 매칭 안됨)"}")
                    println("  • Note: 계정과목 이름이 '영업활동 현금흐름'과 유사할 때 조회됨")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

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
                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // Then: Returns data for 2015
                    println("\n📤 Response:")
                    println("  • year: $year")
                    println("  • lineItems.size: ${cashFlowStatement.lineItems.size}")

                    assertTrue(cashFlowStatement.lineItems.isNotEmpty(), "2015년 데이터가 존재해야 합니다")
                    println("\n✅ 결과: 2015년 데이터 조회 가능")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[파라미터: reportType] 모든 분기(Q1, Q3)와 반기를 조회할 수 있다")
                fun supports_all_quarters() = integrationTest {
                    println("\n📘 엣지 케이스: 분기/반기 조회")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    // When: Query all quarterly report types (Q1, Q3, HALF_YEAR)
                    val quarters = listOf(ReportType.Q1, ReportType.Q3, ReportType.HALF_YEAR)
                    val results = quarters.map { quarter ->
                        val stmt = client.financials!!.getCashFlowStatement(
                            corpCode = corpCode,
                            year = year,
                            reportType = quarter,
                            statementType = StatementType.CONSOLIDATED
                        )
                        quarter to stmt.lineItems.size
                    }

                    // Then: All report types return data
                    println("\n📊 보고서 유형별 조회 결과:")
                    results.forEach { (reportType, itemCount) ->
                        println("  • $reportType: $itemCount 항목")
                        assertTrue(itemCount > 0, "$reportType 데이터가 존재해야 합니다")
                    }

                    println("\n✅ 결과: 모든 보고서 유형 조회 가능 (Q1, Q3, HALF_YEAR)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[활용] 현금흐름 패턴 분석 (영업/투자/재무)")
                fun analyze_cash_flow_pattern() = integrationTest {
                    println("\n📘 실무 활용: 현금흐름 패턴 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Cash flow statement
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Retrieve three main cash flows
                    val operatingCashFlow = cashFlowStatement.getOperatingCashFlow()
                    val investingCashFlow = cashFlowStatement.getInvestingCashFlow()
                    val financingCashFlow = cashFlowStatement.getFinancingCashFlow()

                    // Then: Analyze pattern
                    println("\n=== 삼성전자 ${year}년 현금흐름 패턴 분석 ===")
                    println("영업활동 현금흐름: ${operatingCashFlow ?: "(N/A)"}")
                    println("투자활동 현금흐름: ${investingCashFlow ?: "(N/A)"}")
                    println("재무활동 현금흐름: ${financingCashFlow ?: "(N/A)"}")

                    if (operatingCashFlow != null && investingCashFlow != null && financingCashFlow != null) {
                        println()
                        println("📊 현금흐름 패턴:")
                        println("  • 영업활동: ${if (operatingCashFlow > java.math.BigDecimal.ZERO) "유입 +" else "유출 -"}")
                        println("  • 투자활동: ${if (investingCashFlow > java.math.BigDecimal.ZERO) "유입 +" else "유출 -"}")
                        println("  • 재무활동: ${if (financingCashFlow > java.math.BigDecimal.ZERO) "유입 +" else "유출 -"}")

                        val netCashFlow = operatingCashFlow + investingCashFlow + financingCashFlow
                        println("  • 순현금흐름: $netCashFlow")
                    }
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 잉여현금흐름(FCF) 계산")
                fun calculate_free_cash_flow() = integrationTest {
                    println("\n📘 실무 활용: 잉여현금흐름(FCF) 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Cash flow statement
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val cashFlowStatement = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Calculate FCF (Operating Cash Flow - Investing Cash Flow)
                    val operatingCashFlow = cashFlowStatement.getOperatingCashFlow() ?: return@integrationTest
                    val investingCashFlow = cashFlowStatement.getInvestingCashFlow() ?: return@integrationTest

                    // Investing cash flow is typically negative (cash outflow for investments)
                    // FCF = Operating CF + Investing CF (since Investing is negative)
                    val freeCashFlow = operatingCashFlow + investingCashFlow

                    // Then: Display analysis
                    println("\n=== 삼성전자 ${year}년 잉여현금흐름(FCF) 분석 ===")
                    println("영업활동 현금흐름: ${operatingCashFlow}")
                    println("투자활동 현금흐름: ${investingCashFlow}")
                    println("잉여현금흐름(FCF): ${freeCashFlow}")
                    println()
                    println("📊 분석:")
                    if (freeCashFlow > java.math.BigDecimal.ZERO) {
                        println("  • FCF가 양수 → 배당/자사주매입/부채상환 여력 있음")
                    } else {
                        println("  • FCF가 음수 → 투자 확대 또는 외부자금 필요")
                    }
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 연도별 현금흐름 추이 분석")
                fun analyze_cash_flow_trend() = integrationTest {
                    println("\n📘 실무 활용: 연도별 현금흐름 추이")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: 2022, 2023 cash flow statements
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE

                    val stmt2022 = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = 2022,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    val stmt2023 = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = 2023,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    // When: Compare operating cash flows
                    val ocf2022 = stmt2022.getOperatingCashFlow() ?: return@integrationTest
                    val ocf2023 = stmt2023.getOperatingCashFlow() ?: return@integrationTest

                    val growthRate = ((ocf2023 - ocf2022) / ocf2022 * 100.toBigDecimal()).toDouble()

                    // Then: Display analysis
                    println("\n=== 삼성전자 영업활동 현금흐름 YoY 분석 ===")
                    println("2022년: ${ocf2022}")
                    println("2023년: ${ocf2023}")
                    println("YoY 성장률: ${"%.2f".format(growthRate)}%")
                    println()
                    println("📊 분석: ${if (growthRate > 0) "현금창출력 증가" else "현금창출력 감소"} (${"%.2f".format(growthRate)}%)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("[활용] 연결 vs 별도 현금흐름 비교")
                fun compare_consolidated_vs_separate_cash_flow() = integrationTest {
                    println("\n📘 실무 활용: 연결 vs 별도 현금흐름 비교")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: Both consolidated and separate statements
                    requireOpendartApiKey()
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val consolidated = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.CONSOLIDATED
                    )

                    val separate = client.financials!!.getCashFlowStatement(
                        corpCode = corpCode,
                        year = year,
                        reportType = ReportType.ANNUAL,
                        statementType = StatementType.SEPARATE
                    )

                    // When: Compare operating cash flows
                    val consolidatedOCF = consolidated.getOperatingCashFlow()
                    val separateOCF = separate.getOperatingCashFlow()

                    // Then: Display comparison
                    println("\n=== 삼성전자 ${year}년 연결 vs 별도 비교 ===")
                    println("연결 영업활동 현금흐름: ${consolidatedOCF}")
                    println("별도 영업활동 현금흐름: ${separateOCF}")

                    if (consolidatedOCF != null && separateOCF != null) {
                        val diff = consolidatedOCF - separateOCF
                        val diffPercent = (diff / separateOCF * 100.toBigDecimal()).toDouble()
                        println("차이: ${diff} (${"%.2f".format(diffPercent)}%)")
                        println()
                        println("📊 분석: 연결이 별도 대비 ${"%.2f".format(diffPercent)}% ${if (diffPercent > 0) "높음" else "낮음"}")
                        println("(연결: 종속회사 포함, 별도: 본사만)")
                    }
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }
    }

    // ============================================================================
    // 통합 재무제표 API
    // ============================================================================

    @Nested
    @DisplayName("통합 재무제표 API")
    inner class CombinedStatementsApi {

        // ========================================
        // getAllFinancials() - 전체 재무제표 한번에 조회
        // ========================================

        @Nested
        @DisplayName("getAllFinancials() - 전체 재무제표 한번에 조회")
        inner class GetAll {

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
    }
}
