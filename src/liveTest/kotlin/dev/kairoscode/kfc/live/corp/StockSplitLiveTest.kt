package dev.kairoscode.kfc.live.corp

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import kotlinx.coroutines.delay
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * 주식 분할 정보 조회 Live Test
 *
 * getStockSplitInfo() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 액면분할이 없는 경우 빈 결과를 반환합니다
 * - Rate Limiting을 고려하여 적절한 지연을 추가합니다
 */
class StockSplitLiveTest : LiveTestBase() {

    @Test
    @DisplayName("주식 분할/병합 정보를 조회할 수 있다")
    fun testGetStockSplitInfo() = liveTest {
        // Given: 특정 법인 corp_code, year
        val corpCode = TestSymbols.SAMSUNG_CORP_CODE
        val year = LocalDate.now().year - 1 // 작년 데이터

        // When: 주식 분할/병합 정보 조회
        val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@liveTest

        // Then: 주식 분할/병합 정보 반환 (없을 수 있음)
        println("✅ 삼성전자 ${year}년 주식 분할 정보 개수: ${stockSplitInfo.size}")

        if (stockSplitInfo.isNotEmpty()) {
            stockSplitInfo.forEach { info ->
                println("  - 이벤트: ${info.eventType}, 일자: ${info.eventDate}")
            }
        } else {
            println("  - 해당 연도에 주식 분할/병합이 없습니다")
        }

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = stockSplitInfo,
            category = RecordingConfig.Paths.Corp.STOCK_SPLIT,
            fileName = "samsung_stock_split_$year"
        )
    }

    @Test
    @DisplayName("카카오 주식 분할 정보를 조회할 수 있다")
    fun testGetStockSplitInfoKakao() = liveTest {
        // Given: 카카오 corp_code
        val corpCode = TestSymbols.KAKAO_CORP_CODE
        val year = LocalDate.now().year - 1

        // When: 주식 분할 정보 조회
        val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@liveTest

        // Then: 정보 반환
        println("✅ 카카오 ${year}년 주식 분할 정보 개수: ${stockSplitInfo.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = stockSplitInfo,
            category = RecordingConfig.Paths.Corp.STOCK_SPLIT,
            fileName = "kakao_stock_split_$year"
        )
    }

    @Test
    @DisplayName("액면분할이 없는 경우 빈 결과를 반환한다")
    fun testEmptyStockSplitInfo() = liveTest {
        // Given: 액면분할이 없을 가능성이 높은 법인
        val corpCode = TestSymbols.KAKAO_CORP_CODE
        val year = LocalDate.now().year - 1

        // When: getStockSplitInfo() 호출
        val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@liveTest

        // Then: 빈 리스트 반환 가능
        println("✅ 주식 분할 정보: ${stockSplitInfo.size}건")

        // 응답 레코딩 (빈 리스트도 레코딩)
        ResponseRecorder.recordList(
            data = stockSplitInfo,
            category = RecordingConfig.Paths.Corp.STOCK_SPLIT,
            fileName = "empty_stock_split"
        )
    }

    @Test
    @DisplayName("[활용] 분할 이력을 조회할 수 있다")
    fun testStockSplitHistory() = liveTest {
        // Given: 최근 5년 분할 정보
        val corpCode = TestSymbols.SAMSUNG_CORP_CODE
        val currentYear = LocalDate.now().year
        val years = (currentYear - 5 until currentYear).toList()

        // When: 각 연도별 getStockSplitInfo() 호출
        println("\n=== 삼성전자 주식 분할 이력 (최근 5년) ===")
        years.forEach { year ->
            val stockSplitInfo = client.corp?.getStockSplitInfo(corpCode, year) ?: return@liveTest
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
