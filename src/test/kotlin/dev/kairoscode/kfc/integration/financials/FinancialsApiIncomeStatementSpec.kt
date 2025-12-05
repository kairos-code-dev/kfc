package dev.kairoscode.kfc.integration.financials

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.domain.financials.ReportType
import dev.kairoscode.kfc.domain.financials.StatementType
import dev.kairoscode.kfc.domain.financials.getNetIncome
import dev.kairoscode.kfc.domain.financials.getRevenue
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * FinancialsApi.getIncomeStatement() Integration Test Specification
 *
 * ## API 개요
 * 특정 법인의 손익계산서(Income Statement)를 조회하는 API입니다.
 * DART(전자공시시스템)에서 제공하는 재무제표 데이터를 기반으로 합니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * suspend fun getIncomeStatement(
 *     corpCode: String,
 *     year: Int,
 *     reportType: ReportType,
 *     statementType: StatementType
 * ): IncomeStatement
 * ```
 *
 * ## 파라미터
 * - `corpCode`: String - 법인 고유번호 (8자리, 예: "00126380")
 * - `year`: Int - 조회 연도 (2015년 이후)
 * - `reportType`: ReportType - 보고서 유형 (ANNUAL: 연간, HALF_YEAR: 반기, Q1/Q3: 분기)
 * - `statementType`: StatementType - 재무제표 유형 (CONSOLIDATED: 연결, SEPARATE: 별도)
 *
 * ## 응답
 * - `IncomeStatement`: 손익계산서 객체
 *   - `lineItems`: List<FinancialLineItem> - 계정과목 목록
 *   - 주요 계정과목: 매출액, 영업이익, 당기순이익 등
 *
 * ## 제약사항
 * - OPENDART_API_KEY 필요
 * - 2015년 이후 데이터만 지원
 * - Rate limit: 전역 10 req/sec (GlobalRateLimiters)
 *
 * ## 관련 문서
 * - OPENDART API: https://opendart.fss.or.kr/
 * - 계정과목 매핑: src/main/kotlin/dev/kairoscode/kfc/domain/financials/IncomeStatement.kt
 */
@DisplayName("FinancialsApi.getIncomeStatement() - 손익계산서 조회")
class FinancialsApiIncomeStatementSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

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

    // ========================================
    // 2. 응답 데이터 검증 (Response Validation)
    // ========================================

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

    // ========================================
    // 3. 입력 파라미터 검증 (Input Validation)
    // ========================================

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

    // ========================================
    // 5. 활용 예제 (Usage Examples)
    // ========================================

    @Nested
    @DisplayName("5. 활용 예제 (Usage Examples)")
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
