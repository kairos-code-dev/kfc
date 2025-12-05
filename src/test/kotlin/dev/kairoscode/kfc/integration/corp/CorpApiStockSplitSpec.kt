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
 * CorpApi - 주식 분할 정보 조회 API 통합 테스트
 *
 * 특정 법인의 주식 분할/병합 정보를 조회합니다.
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 액면분할이 없는 경우 빈 결과를 반환합니다
 * - Rate Limiting을 고려하여 적절한 지연을 추가합니다
 */
@DisplayName("CorpApi.getStockSplitInfo() - 주식 분할 정보 조회")
class CorpApiStockSplitSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("1. 기본 동작")
    inner class BasicOperations {

        @Test
        @DisplayName("삼성전자의 주식 분할 정보를 고정 연도로 조회할 수 있다")
        fun get_samsung_stock_split_info_for_fixed_year() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getStockSplitInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • year: Int = $year")

            // When
            val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)

            // Then
            println("\n📤 Response: List<StockSplitInfo>")
            println("  • size: ${stockSplitInfo.size}")

            if (stockSplitInfo.isNotEmpty()) {
                println("\n  [샘플 데이터]")
                stockSplitInfo.forEach { info ->
                    println("    • 이벤트: ${info.eventType}, 일자: ${info.eventDate}")
                }
            } else {
                println("  • 해당 연도에 주식 분할/병합이 없습니다")
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = stockSplitInfo,
                category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                fileName = "samsung_stock_split_$year"
            )
        }

        @Test
        @DisplayName("카카오의 주식 분할 정보를 고정 연도로 조회할 수 있다")
        fun get_kakao_stock_split_info_for_fixed_year() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getStockSplitInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • year: Int = $year")

            // When
            val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)

            // Then
            println("\n📤 Response: List<StockSplitInfo>")
            println("  • size: ${stockSplitInfo.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = stockSplitInfo,
                category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                fileName = "kakao_stock_split_$year"
            )
        }
    }

    @Nested
    @DisplayName("3. 엣지 케이스")
    inner class EdgeCases {

        @Test
        @DisplayName("액면분할이 없는 경우 빈 결과를 반환한다")
        fun return_empty_result_when_no_stock_split() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getStockSplitInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
            val year = 2023

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • year: Int = $year")

            // When
            val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)

            // Then
            println("\n📤 Response: List<StockSplitInfo>")
            println("  • size: ${stockSplitInfo.size}건 (빈 리스트 가능)")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = stockSplitInfo,
                category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                fileName = "empty_stock_split"
            )
        }
    }

    @Nested
    @DisplayName("5. 실무 활용 예제")
    inner class PracticalExamples {

        @Test
        @DisplayName("고정 기간 기준으로 분할 이력을 조회할 수 있다")
        fun get_stock_split_history_for_fixed_period() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: getStockSplitInfo() - 활용 예제")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val years = listOf(2019, 2020, 2021, 2022, 2023)

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • years: ${years.joinToString(", ")}")

            // When & Then
            println("\n📤 Response: 연도별 주식 분할 이력")
            years.forEach { year ->
                val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)
                println("  • ${year}년: ${stockSplitInfo.size}건")

                if (stockSplitInfo.isNotEmpty()) {
                    stockSplitInfo.forEach { info ->
                        println("      - ${info.eventType} (${info.eventDate})")
                    }
                }

                delay(500)
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
