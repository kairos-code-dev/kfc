package dev.kairoscode.kfc.live.etf

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 목록 조회 Live Test
 *
 * getList() 함수의 실제 API 호출 테스트 및 응답 레코딩
 */
class EtfListLiveTest : LiveTestBase() {

    @Test
    @DisplayName("전체 ETF 목록을 조회할 수 있다")
    fun testGetList() = liveTest {
        // Given: KfcClient 초기화 완료
        val etfList = client.etf.getList()

        // Then: 1개 이상의 ETF 반환
        assertTrue(etfList.size >= 1, "ETF 목록은 최소 1개 이상이어야 합니다. 실제: ${etfList.size}개")

        // Then: TIGER 200 포함 확인
        val tiger200 = etfList.find { it.isin == TestSymbols.TIGER_200_ISIN }
        assertTrue(tiger200 != null, "TIGER 200이 목록에 포함되어야 합니다")

        // Then: KODEX 200 포함 확인
        val kodex200 = etfList.find { it.isin == TestSymbols.KODEX_200_ISIN }
        assertTrue(kodex200 != null, "KODEX 200이 목록에 포함되어야 합니다")

        // Then: 각 ETF는 ISIN, 티커, 이름, 자산구분 포함
        etfList.forEach { etf ->
            assertTrue(etf.isin.isNotBlank(), "ISIN은 비어있지 않아야 합니다")
            assertTrue(etf.ticker.isNotBlank(), "티커는 비어있지 않아야 합니다")
            assertTrue(etf.name.isNotBlank(), "이름은 비어있지 않아야 합니다")
            assertTrue(etf.assetClass.isNotBlank(), "자산구분은 비어있지 않아야 합니다")
        }

        println("✅ 전체 ETF 개수: ${etfList.size}")
        println("✅ TIGER 200: ${tiger200?.name}")
        println("✅ KODEX 200: ${kodex200?.name}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = etfList,
            category = RecordingConfig.Paths.Etf.LIST,
            fileName = "etf_list"
        )
    }

    @Test
    @DisplayName("ETF 목록의 다양한 자산구분을 확인할 수 있다")
    fun testGetListAssetClassDistribution() = liveTest {
        // Given: ETF 목록 조회
        val etfList = client.etf.getList()

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
