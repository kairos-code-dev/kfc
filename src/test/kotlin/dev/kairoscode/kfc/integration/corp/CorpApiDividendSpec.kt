package dev.kairoscode.kfc.integration.corp

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import kotlinx.coroutines.delay
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * CorpApi - 배당 정보 조회 API 통합 테스트
 *
 * 특정 법인의 배당 정보를 조회합니다.
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 * - Rate Limiting을 고려하여 적절한 지연을 추가합니다
 */
@DisplayName("CorpApi.getDividendInfo() - 배당 정보 조회")
class CorpApiDividendSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("1. 기본 동작")
    inner class BasicOperations {

        @Test
        @DisplayName("삼성전자의 배당 정보를 고정 연도로 조회할 수 있다")
        fun get_samsung_dividend_info_for_fixed_year() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getDividendInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • year: Int = $year")

            // When
            val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)

            // Then
            println("\n📤 Response: List<DividendInfo>")
            println("  • size: ${dividendInfo.size}")

            if (dividendInfo.isNotEmpty()) {
                println("\n  [샘플 데이터]")
                dividendInfo.forEach { info ->
                    println("    • 배당 구분: ${info.dividendType}, 주식 종류: ${info.stockKind}")
                }
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = dividendInfo,
                category = RecordingConfig.Paths.CorpActions.DIVIDEND,
                fileName = "samsung_dividend_$year"
            )
        }

        @Test
        @DisplayName("카카오의 배당 정보를 고정 연도로 조회할 수 있다")
        fun get_kakao_dividend_info_for_fixed_year() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getDividendInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • year: Int = $year")

            // When
            val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)

            // Then
            println("\n📤 Response: List<DividendInfo>")
            println("  • size: ${dividendInfo.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = dividendInfo,
                category = RecordingConfig.Paths.CorpActions.DIVIDEND,
                fileName = "kakao_dividend_$year"
            )
        }
    }

    @Nested
    @DisplayName("4. 파라미터 조합")
    inner class ParameterCombinations {

        @Test
        @DisplayName("다양한 보고서 타입으로 조회할 수 있다")
        fun query_with_different_report_types() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getDividendInfo() - 보고서 타입별")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            val reportCodes = mapOf(
                "11011" to "사업보고서",
                "11012" to "반기보고서",
                "11013" to "1분기보고서",
                "11014" to "3분기보고서"
            )

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • year: Int = $year")
            println("  • reportCodes: ${reportCodes.keys.joinToString(", ")}")

            // When & Then
            println("\n📤 Response: 보고서 타입별 배당 정보")
            reportCodes.forEach { (reportCode, reportName) ->
                val dividendInfo = client.corp!!.getDividendInfo(corpCode, year, reportCode)
                println("  • $reportName ($reportCode): ${dividendInfo.size}건")
                delay(500)
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    @Nested
    @DisplayName("5. 실무 활용 예제")
    inner class PracticalExamples {

        @Test
        @DisplayName("고정 기간 기준으로 배당 이력을 조회할 수 있다")
        fun get_dividend_history_for_fixed_period() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getDividendInfo() - 활용 예제")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val years = listOf(2021, 2022, 2023)

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • years: ${years.joinToString(", ")}")

            // When & Then
            println("\n📤 Response: 연도별 배당 이력")
            years.forEach { year ->
                val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)
                println("  • ${year}년: ${dividendInfo.size}건")
                delay(500)
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
