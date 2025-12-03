package dev.kairoscode.kfc.integration.corp

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import kotlinx.coroutines.delay
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


/**
 * 주식 분할 정보 조회 Integration Test
 *
 * getStockSplitInfo() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 액면분할이 없는 경우 빈 결과를 반환합니다
 * - Rate Limiting을 고려하여 적절한 지연을 추가합니다
 */
@DisplayName("CorpApi.getStockSplitInfo() - 주식 분할 정보 조회")
class CorpApiStockSplitSpec : IntegrationTestBase() {

    @Test
    @DisplayName("주식 분할/병합 정보를 고정 연도로 조회할 수 있다")
    fun testGetStockSplitInfo() = integrationTest {
        // Given: 특정 법인 corp_code, year (고정: 2023년)
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023 // 고정 연도

        // When: 주식 분할/병합 정보 조회
        val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@integrationTest

        // Then: 주식 분할/병합 정보 반환 (없을 수 있음)
        println("✅ 삼성전자 ${year}년 주식 분할 정보 개수: ${stockSplitInfo.size}")

        if (stockSplitInfo.isNotEmpty()) {
            stockSplitInfo.forEach { info ->
                println("  - 이벤트: ${info.eventType}, 일자: ${info.eventDate}")
            }
        } else {
            println("  - 해당 연도에 주식 분할/병합이 없습니다")
        }

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = stockSplitInfo,
            category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
            fileName = "samsung_stock_split_$year"
        )
    }

    @Test
    @DisplayName("카카오 주식 분할 정보를 고정 연도로 조회할 수 있다")
    fun testGetStockSplitInfoKakao() = integrationTest {
        // Given: 카카오 corp_code (고정: 2023년)
        val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
        val year = 2023 // 고정 연도

        // When: 주식 분할 정보 조회
        val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@integrationTest

        // Then: 정보 반환
        println("✅ 카카오 ${year}년 주식 분할 정보 개수: ${stockSplitInfo.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = stockSplitInfo,
            category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
            fileName = "kakao_stock_split_$year"
        )
    }

    @Test
    @DisplayName("액면분할이 없는 경우 빈 결과를 반환한다")
    fun testEmptyStockSplitInfo() = integrationTest {
        // Given: 액면분할이 없을 가능성이 높은 법인 (고정: 2023년)
        val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
        val year = 2023 // 고정 연도

        // When: getStockSplitInfo() 호출
        val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@integrationTest

        // Then: 빈 리스트 반환 가능
        println("✅ ${year}년 주식 분할 정보: ${stockSplitInfo.size}건")

        // 스마트 레코딩 (빈 리스트도 레코딩)
        SmartRecorder.recordSmartly(
            data = stockSplitInfo,
            category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
            fileName = "empty_stock_split"
        )
    }

    @Test
    @DisplayName("[활용] 고정 기간 기준으로 분할 이력을 조회할 수 있다")
    fun testStockSplitHistory() = integrationTest {
        // Given: 고정 5년 분할 정보 (2019-2023년)
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val years = listOf(2019, 2020, 2021, 2022, 2023) // 고정 연도 목록

        // When: 각 연도별 getStockSplitInfo() 호출
        println("\n=== 삼성전자 주식 분할 이력 (2019-2023년) ===")
        years.forEach { year ->
            val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@integrationTest
            println("${year}년: ${stockSplitInfo.size}건")

            if (stockSplitInfo.isNotEmpty()) {
                stockSplitInfo.forEach { info ->
                    println("  - ${info.eventType} (${info.eventDate})")
                }
            }

            // Rate Limiting 고려
            delay(500)
        }
    }
}
