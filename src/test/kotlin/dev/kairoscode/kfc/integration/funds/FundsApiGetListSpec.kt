package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FundsApi.getList() API 스펙
 *
 * 전체 펀드(ETF) 목록을 조회하는 API입니다.
 */
@DisplayName("FundsApi.getList() - 펀드 목록 조회")
class FundsApiGetListSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("1. 기본 동작")
    inner class BasicBehavior {

        @Test
        @DisplayName("전체 ETF 목록을 조회할 수 있다")
        fun returns_all_funds_when_called_without_parameters() = integrationTest {
            println("\n📘 API: getList()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            println("📥 Input Parameters:")
            println("  • (없음)")

            // When
            val etfList = client.funds.getList()

            // Then
            println("\n📤 Response: List<Fund>")
            println("  • size: ${etfList.size}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            assertTrue(etfList.size >= 1, "ETF 목록은 최소 1개 이상이어야 합니다. 실제: ${etfList.size}개")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = etfList,
                category = RecordingConfig.Paths.EtfList.BASE,
                fileName = "etf_list"
            )
        }
    }

    @Nested
    @DisplayName("2. 응답 데이터 검증")
    inner class ResponseValidation {

        @Test
        @DisplayName("각 항목은 ISIN, 티커, 이름, 자산구분을 포함한다")
        fun each_item_contains_required_fields() = integrationTest {
            // Given
            val etfList = client.funds.getList()

            // Then: 각 ETF는 필수 필드 포함
            etfList.forEach { etf ->
                assertTrue(etf.isin.isNotBlank(), "ISIN은 비어있지 않아야 합니다")
                assertTrue(etf.ticker.isNotBlank(), "티커는 비어있지 않아야 합니다")
                assertTrue(etf.name.isNotBlank(), "이름은 비어있지 않아야 합니다")
                assertTrue(etf.assetClass.isNotBlank(), "자산구분은 비어있지 않아야 합니다")
            }
        }

        @Test
        @DisplayName("TIGER 200과 KODEX 200이 목록에 포함된다")
        fun includes_major_etfs_like_tiger200_and_kodex200() = integrationTest {
            // Given
            val etfList = client.funds.getList()

            // Then: TIGER 200 포함 확인
            val tiger200 = etfList.find { it.isin == TestFixtures.Etf.TIGER_200_ISIN }
            assertTrue(tiger200 != null, "TIGER 200이 목록에 포함되어야 합니다")
            println("[IntegrationTest] TIGER 200: ${tiger200?.name}")

            // Then: KODEX 200 포함 확인
            val kodex200 = etfList.find { it.isin == TestFixtures.Etf.KODEX_200_ISIN }
            assertTrue(kodex200 != null, "KODEX 200이 목록에 포함되어야 합니다")
            println("[IntegrationTest] KODEX 200: ${kodex200?.name}")
        }

        @Test
        @DisplayName("다양한 자산구분이 포함된다")
        fun contains_various_asset_classes() = integrationTest {
            // Given
            val etfList = client.funds.getList()

            // When: 자산구분별로 그룹화
            val assetClassGroups = etfList.groupBy { it.assetClass }

            // Then: 다양한 자산구분 존재
            assertTrue(assetClassGroups.size >= 3, "최소 3개 이상의 자산구분이 있어야 합니다")

            // 콘솔 출력: 자산구분별 ETF 개수
            println("\n=== 자산구분별 ETF 개수 ===")
            assetClassGroups.entries
                .sortedByDescending { it.value.size }
                .forEach { (assetClass, etfs) ->
                    println("$assetClass: ${etfs.size}개")
                }
        }
    }

    @Nested
    @DisplayName("5. 실무 활용 예제")
    inner class PracticalExamples {

        @Test
        @DisplayName("이름으로 ETF를 검색할 수 있다")
        fun search_etf_by_name() = integrationTest {
            // Given
            val etfList = client.funds.getList()
            val searchKeyword = "TIGER"

            // When
            val searchResults = etfList.filter { it.name.contains(searchKeyword) }

            // Then
            assertTrue(searchResults.isNotEmpty(), "$searchKeyword 가 포함된 ETF가 있어야 합니다")
            println("\n=== '$searchKeyword' 검색 결과 ===")
            println("검색된 ETF 개수: ${searchResults.size}")
            searchResults.take(5).forEach { etf ->
                println("  - ${etf.name} (${etf.ticker})")
            }
        }

        @Test
        @DisplayName("자산구분으로 ETF를 필터링할 수 있다")
        fun filter_etf_by_asset_class() = integrationTest {
            // Given
            val etfList = client.funds.getList()

            // When: 자산구분별로 그룹화
            val assetClassGroups = etfList.groupBy { it.assetClass }

            // Then: 각 자산구분의 ETF 확인
            println("\n=== 자산구분별 ETF 필터링 예제 ===")
            assetClassGroups.entries
                .sortedByDescending { it.value.size }
                .take(3)
                .forEach { (assetClass, etfs) ->
                    println("\n[$assetClass] (${etfs.size}개)")
                    etfs.take(3).forEach { etf ->
                        println("  - ${etf.name} (${etf.ticker})")
                    }
                }
        }
    }
}
