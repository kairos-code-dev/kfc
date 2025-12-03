package dev.kairoscode.kfc.integration.corp

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


/**
 * 공시 검색 Integration Test
 *
 * searchDisclosures() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - pageCount는 최대 100입니다
 * - 날짜 범위는 최대 1년입니다
 */
@DisplayName("CorpApi.searchDisclosures() - 공시 검색")
class CorpApiDisclosureSpec : IntegrationTestBase() {

    @Test
    @DisplayName("특정 기간의 공시 목록을 조회할 수 있다")
    fun testSearchDisclosures() = integrationTest {
        // Given: startDate, endDate 지정
        val endDate = TestFixtures.Dates.TRADING_DAY
        val startDate = endDate.minusMonths(1)
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE

        // When: 공시 검색
        val disclosures = client.corp?.searchDisclosures(corpCode, startDate, endDate) ?: return@integrationTest

        // Then: 해당 기간의 공시 목록 반환
        println("✅ 삼성전자 1개월 공시 개수: ${disclosures.size}")
        println("✅ 기간: $startDate ~ $endDate")

        if (disclosures.isNotEmpty()) {
            println("\n최근 공시 5건:")
            disclosures.take(5).forEach { disclosure ->
                println("  - ${disclosure.reportName} (${disclosure.rceptDate})")
            }
        }

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = disclosures,
            category = RecordingConfig.Paths.CorpDisclosure.BASE,
            fileName = "samsung_disclosure_1month"
        )
    }

    @Test
    @DisplayName("카카오 공시를 조회할 수 있다")
    fun testSearchDisclosuresKakao() = integrationTest {
        // Given: 카카오 corp_code
        val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
        val endDate = TestFixtures.Dates.TRADING_DAY
        val startDate = endDate.minusMonths(1)

        // When: 공시 검색
        val disclosures = client.corp?.searchDisclosures(corpCode, startDate, endDate) ?: return@integrationTest

        // Then: 공시 목록 반환
        println("✅ 카카오 1개월 공시 개수: ${disclosures.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = disclosures,
            category = RecordingConfig.Paths.CorpDisclosure.BASE,
            fileName = "kakao_disclosure_1month"
        )
    }

    @Test
    @DisplayName("전체 법인의 공시를 조회할 수 있다")
    fun testSearchAllCorpDisclosures() = integrationTest {
        // Given: corpCode = null, 특정 날짜
        val date = TestFixtures.Dates.TRADING_DAY

        // When: searchDisclosures(null, startDate, endDate) 호출
        val disclosures = client.corp?.searchDisclosures(
            corpCode = null,
            startDate = date,
            endDate = date,
            pageCount = 100
        ) ?: return@integrationTest

        // Then: 모든 법인의 공시 반환 (페이징 처리)
        println("✅ 전체 법인 공시 개수 (1일): ${disclosures.size}")
        assertTrue(disclosures.size <= 100, "pageCount 제한이 적용되어야 합니다")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = disclosures,
            category = RecordingConfig.Paths.CorpDisclosure.BASE,
            fileName = "all_corp_disclosure_1day"
        )
    }

    @Test
    @DisplayName("페이징 처리가 가능하다")
    fun testDisclosurePagination() = integrationTest {
        // Given: pageNo, pageCount 지정
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val endDate = TestFixtures.Dates.TRADING_DAY
        val startDate = endDate.minusMonths(3)

        // When: searchDisclosures(..., pageNo=1, pageCount=50) 호출
        val page1 = client.corp?.searchDisclosures(
            corpCode = corpCode,
            startDate = startDate,
            endDate = endDate,
            pageNo = 1,
            pageCount = 50
        ) ?: return@integrationTest

        // Then: 1페이지 결과 반환 (최대 50개)
        println("✅ 1페이지 공시 개수: ${page1.size}")
        assertTrue(page1.size <= 50, "pageCount 제한이 적용되어야 합니다")
    }

    @Test
    @DisplayName("[활용] 특정 키워드가 포함된 공시를 찾을 수 있다")
    fun testSearchDisclosuresByKeyword() = integrationTest {
        // Given: 공시 목록 조회
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val endDate = TestFixtures.Dates.TRADING_DAY
        val startDate = endDate.minusMonths(6)
        val disclosures = client.corp?.searchDisclosures(corpCode, startDate, endDate) ?: return@integrationTest

        // When: reportName에 "분기" 포함된 공시 필터링
        val quarterlyReports = disclosures.filter {
            it.reportName.contains("분기")
        }

        // Then: 분기 관련 공시만 반환
        println("\n=== 분기 관련 공시 ===")
        println("전체 공시: ${disclosures.size}건")
        println("분기 관련 공시: ${quarterlyReports.size}건")

        quarterlyReports.take(5).forEach { disclosure ->
            println("  - ${disclosure.reportName} (${disclosure.rceptDate})")
        }
    }

    @Test
    @DisplayName("[활용] 공시 통계를 분석할 수 있다")
    fun testDisclosureStatistics() = integrationTest {
        // Given: 3개월 공시 데이터
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val endDate = TestFixtures.Dates.TRADING_DAY
        val startDate = endDate.minusMonths(3)
        val disclosures = client.corp?.searchDisclosures(corpCode, startDate, endDate) ?: return@integrationTest

        // When: 공시 유형별 그룹화
        val disclosureGroups = disclosures
            .groupBy { it.reportName }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(10)

        // Then: 통계 출력
        println("\n=== 공시 유형별 통계 (상위 10개) ===")
        println("전체 공시: ${disclosures.size}건")
        println("\n유형별 분포:")
        disclosureGroups.forEach { (reportName, count) ->
            println("  - $reportName: ${count}건")
        }
    }
}
