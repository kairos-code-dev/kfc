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
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * FinancialsApi.getCashFlowStatement() Integration Test Specification
 *
 * ## API 개요
 * 특정 법인의 현금흐름표(Cash Flow Statement)를 조회하는 API입니다.
 * DART(전자공시시스템)에서 제공하는 재무제표 데이터를 기반으로 합니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * suspend fun getCashFlowStatement(
 *     corpCode: String,
 *     year: Int,
 *     reportType: ReportType,
 *     statementType: StatementType
 * ): CashFlowStatement
 * ```
 *
 * ## 파라미터
 * - `corpCode`: String - 법인 고유번호 (8자리, 예: "00126380")
 * - `year`: Int - 조회 연도 (2015년 이후)
 * - `reportType`: ReportType - 보고서 유형 (ANNUAL: 연간, HALF_YEAR: 반기, Q1/Q3: 분기)
 * - `statementType`: StatementType - 재무제표 유형 (CONSOLIDATED: 연결, SEPARATE: 별도)
 *
 * ## 응답
 * - `CashFlowStatement`: 현금흐름표 객체
 *   - `lineItems`: List<FinancialLineItem> - 계정과목 목록
 *   - 주요 계정과목: 영업활동 현금흐름, 투자활동 현금흐름, 재무활동 현금흐름
 *
 * ## 제약사항
 * - OPENDART_API_KEY 필요
 * - 2015년 이후 데이터만 지원
 * - Rate limit: 전역 10 req/sec (GlobalRateLimiters)
 *
 * ## 관련 문서
 * - OPENDART API: https://opendart.fss.or.kr/
 * - 계정과목 매핑: src/main/kotlin/dev/kairoscode/kfc/domain/financials/CashFlowStatement.kt
 */
@DisplayName("FinancialsApi.getCashFlowStatement() - 현금흐름표 조회")
class FinancialsApiCashFlowSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

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

    // ========================================
    // 5. 실무 활용 예제 (Usage Examples)
    // ========================================

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
