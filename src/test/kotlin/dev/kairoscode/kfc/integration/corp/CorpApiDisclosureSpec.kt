package dev.kairoscode.kfc.integration.corp

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * CorpApi - 공시 검색 API 통합 테스트
 *
 * 특정 기간의 공시 목록을 검색합니다.
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - pageCount는 최대 100입니다
 * - 날짜 범위는 최대 1년입니다
 */
@DisplayName("CorpApi.searchDisclosures() - 공시 검색")
class CorpApiDisclosureSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("1. 기본 동작")
    inner class BasicOperations {

        @Test
        @DisplayName("특정 기간의 공시 목록을 조회할 수 있다")
        fun search_disclosures_for_specific_period() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: searchDisclosures()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val endDate = TestFixtures.Dates.TRADING_DAY
            val startDate = endDate.minusMonths(1)
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • startDate: LocalDate = $startDate")
            println("  • endDate: LocalDate = $endDate")

            // When
            val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)

            // Then
            println("\n📤 Response: List<Disclosure>")
            println("  • size: ${disclosures.size}")

            if (disclosures.isNotEmpty()) {
                println("\n  [최근 공시 3건]")
                disclosures.take(3).forEach { disclosure ->
                    println("    • ${disclosure.reportName} (${disclosure.rceptDate})")
                }
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = disclosures,
                category = RecordingConfig.Paths.CorpDisclosure.BASE,
                fileName = "samsung_disclosure_1month"
            )
        }

        @Test
        @DisplayName("카카오 공시를 조회할 수 있다")
        fun search_kakao_disclosures() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: searchDisclosures()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
            val endDate = TestFixtures.Dates.TRADING_DAY
            val startDate = endDate.minusMonths(1)

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • startDate: LocalDate = $startDate")
            println("  • endDate: LocalDate = $endDate")

            // When
            val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)

            // Then
            println("\n📤 Response: List<Disclosure>")
            println("  • size: ${disclosures.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            SmartRecorder.recordSmartly(
                data = disclosures,
                category = RecordingConfig.Paths.CorpDisclosure.BASE,
                fileName = "kakao_disclosure_1month"
            )
        }
    }

    @Nested
    @DisplayName("3. 엣지 케이스")
    inner class EdgeCases {

        @Test
        @DisplayName("전체 법인의 공시를 조회할 수 있다")
        fun search_all_corp_disclosures() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: searchDisclosures()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val date = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • corpCode: null (전체 법인)")
            println("  • startDate: LocalDate = $date")
            println("  • endDate: LocalDate = $date")
            println("  • pageCount: Int = 100")

            // When
            val disclosures = client.corp!!.searchDisclosures(
                corpCode = null,
                startDate = date,
                endDate = date,
                pageCount = 100
            )

            // Then
            println("\n📤 Response: List<Disclosure>")
            println("  • size: ${disclosures.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(disclosures.size <= 100, "pageCount 제한이 적용되어야 합니다")

            SmartRecorder.recordSmartly(
                data = disclosures,
                category = RecordingConfig.Paths.CorpDisclosure.BASE,
                fileName = "all_corp_disclosure_1day"
            )
        }
    }

    @Nested
    @DisplayName("4. 파라미터 조합")
    inner class ParameterCombinations {

        @Test
        @DisplayName("페이징 처리가 가능하다")
        fun pagination_works_correctly() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: searchDisclosures() - 페이징")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val endDate = TestFixtures.Dates.TRADING_DAY
            val startDate = endDate.minusMonths(3)

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • startDate: LocalDate = $startDate")
            println("  • endDate: LocalDate = $endDate")
            println("  • pageNo: Int = 1")
            println("  • pageCount: Int = 50")

            // When
            val page1 = client.corp!!.searchDisclosures(
                corpCode = corpCode,
                startDate = startDate,
                endDate = endDate,
                pageNo = 1,
                pageCount = 50
            )

            // Then
            println("\n📤 Response: List<Disclosure>")
            println("  • size: ${page1.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(page1.size <= 50, "pageCount 제한이 적용되어야 합니다")
        }
    }

    @Nested
    @DisplayName("5. 실무 활용 예제")
    inner class PracticalExamples {

        @Test
        @DisplayName("특정 키워드가 포함된 공시를 찾을 수 있다")
        fun search_disclosures_by_keyword() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: searchDisclosures() - 활용 예제")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val endDate = TestFixtures.Dates.TRADING_DAY
            val startDate = endDate.minusMonths(6)

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • startDate: LocalDate = $startDate")
            println("  • endDate: LocalDate = $endDate")
            println("  • keyword: String = \"분기\"")

            // When
            val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)
            val quarterlyReports = disclosures.filter { it.reportName.contains("분기") }

            // Then
            println("\n📤 Response: 키워드 필터링 결과")
            println("  • 전체 공시: ${disclosures.size}건")
            println("  • 분기 관련 공시: ${quarterlyReports.size}건")

            if (quarterlyReports.isNotEmpty()) {
                println("\n  [분기 관련 공시 - 상위 3건]")
                quarterlyReports.take(3).forEach { disclosure ->
                    println("    • ${disclosure.reportName} (${disclosure.rceptDate})")
                }
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("공시 통계를 분석할 수 있다")
        fun analyze_disclosure_statistics() = integrationTest {
            requireOpendartApiKey()

            println("\n📘 API: searchDisclosures() - 활용 예제")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
            val endDate = TestFixtures.Dates.TRADING_DAY
            val startDate = endDate.minusMonths(3)

            println("📥 Input Parameters:")
            println("  • corpCode: String = \"$corpCode\"")
            println("  • startDate: LocalDate = $startDate")
            println("  • endDate: LocalDate = $endDate")

            // When
            val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)
            val disclosureGroups = disclosures
                .groupBy { it.reportName }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)

            // Then
            println("\n📤 Response: 공시 유형별 통계")
            println("  • 전체 공시: ${disclosures.size}건")
            println("\n  [유형별 분포 - 상위 5개]")
            disclosureGroups.take(5).forEach { (reportName, count) ->
                println("    • $reportName: ${count}건")
            }

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
