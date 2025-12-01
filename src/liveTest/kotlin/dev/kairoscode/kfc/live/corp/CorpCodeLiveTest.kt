package dev.kairoscode.kfc.live.corp

import dev.kairoscode.kfc.utils.LiveTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

/**
 * 법인 고유번호 목록 조회 Live Test
 *
 * getCorpCodeList() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 대용량 데이터 (10MB 이상)
 * - ZIP 압축 해제와 XML 파싱이 자동으로 처리됩니다
 */
class CorpCodeLiveTest : LiveTestBase() {

    @Test
    @DisplayName("전체 법인 고유번호 목록을 조회할 수 있다")
    fun testGetCorpCodeList() = liveTest(timeout = 2.minutes) {
        // Given: KfcClient with OPENDART API Key

        // When: 고유번호 목록 조회
        val corpCodeList = client.corp?.getCorpCodeList() ?: return@liveTest

        // Then: 10,000개 이상의 법인 정보 반환
        assertTrue(corpCodeList.size >= 10000, "법인 정보는 최소 10,000개 이상이어야 합니다. 실제: ${corpCodeList.size}개")

        // Then: 각 법인은 corp_code, corp_name 포함
        corpCodeList.forEach { corpCode ->
            assertTrue(corpCode.corpCode.isNotBlank(), "corp_code는 비어있지 않아야 합니다")
            assertTrue(corpCode.corpName.isNotBlank(), "corp_name은 비어있지 않아야 합니다")
        }

        // Then: 삼성전자 포함 확인
        val samsung = corpCodeList.find { it.corpCode == TestSymbols.SAMSUNG_CORP_CODE }
        assertTrue(samsung != null, "삼성전자가 목록에 포함되어야 합니다")

        println("✅ 전체 법인 개수: ${corpCodeList.size}")
        println("✅ 삼성전자: ${samsung?.corpName} (${samsung?.stockCode})")

        // 응답 레코딩 (대용량 데이터 주의)
        ResponseRecorder.recordList(
            data = corpCodeList,
            category = RecordingConfig.Paths.Corp.CORP_CODE,
            fileName = "corp_code_list"
        )
    }

    @Test
    @DisplayName("ZIP 압축 해제와 XML 파싱이 자동으로 처리된다")
    fun testAutoDecompressionAndParsing() = liveTest(timeout = 2.minutes) {
        // Given: OPENDART API는 ZIP 파일 반환

        // When: getCorpCodeList() 호출
        val corpCodeList = client.corp?.getCorpCodeList() ?: return@liveTest

        // Then: 자동으로 압축 해제 및 XML 파싱
        assertTrue(corpCodeList.isNotEmpty(), "압축 해제 및 파싱이 성공해야 합니다")

        // Then: List<CorpCode> 형태로 반환
        println("✅ ZIP 압축 해제 및 XML 파싱 성공")
        println("✅ 법인 개수: ${corpCodeList.size}")
    }

    @Test
    @DisplayName("[활용] 종목코드로 OPENDART 고유번호를 찾을 수 있다")
    fun testFindCorpCodeByStockCode() = liveTest(timeout = 2.minutes) {
        // Given: 고유번호 목록 조회
        val corpCodeList = client.corp?.getCorpCodeList() ?: return@liveTest

        // When: stock_code로 필터링 (예: "005930" 삼성전자)
        val samsung = corpCodeList.find { it.stockCode == "005930" }

        // Then: corp_code "00126380" 반환
        assertTrue(samsung != null, "삼성전자를 찾을 수 있어야 합니다")
        assertTrue(samsung!!.corpCode == TestSymbols.SAMSUNG_CORP_CODE, "올바른 corp_code를 반환해야 합니다")

        println("\n=== 종목코드로 고유번호 찾기 ===")
        println("종목코드: ${samsung.stockCode}")
        println("법인명: ${samsung.corpName}")
        println("고유번호: ${samsung.corpCode}")
    }

    @Test
    @DisplayName("[활용] 법인명으로 고유번호를 검색할 수 있다")
    fun testSearchCorpCodeByName() = liveTest(timeout = 2.minutes) {
        // Given: 고유번호 목록 조회
        val corpCodeList = client.corp?.getCorpCodeList() ?: return@liveTest

        // When: corp_name에 "삼성" 포함된 법인 검색
        val samsungCorps = corpCodeList.filter {
            it.corpName.contains("삼성")
        }.take(10) // 상위 10개만

        // Then: 삼성전자, 삼성SDI 등 여러 법인 반환
        assertTrue(samsungCorps.isNotEmpty(), "삼성 관련 법인이 있어야 합니다")

        println("\n=== 법인명 검색 결과 (삼성) ===")
        samsungCorps.forEach { corp ->
            println("${corp.corpName} - ${corp.stockCode ?: "비상장"} (${corp.corpCode})")
        }
    }

    @Test
    @DisplayName("[활용] 상장 법인만 필터링할 수 있다")
    fun testFilterListedCorps() = liveTest(timeout = 2.minutes) {
        // Given: 고유번호 목록 (상장/비상장 모두 포함)
        val corpCodeList = client.corp?.getCorpCodeList() ?: return@liveTest

        // When: stock_code가 null이 아닌 법인 필터링
        val listedCorps = corpCodeList.filter {
            it.stockCode != null
        }

        // Then: 상장 법인만 반환
        assertTrue(listedCorps.isNotEmpty(), "상장 법인이 있어야 합니다")
        assertTrue(listedCorps.all { it.stockCode != null }, "모두 종목코드를 가져야 합니다")

        println("\n=== 상장 법인 통계 ===")
        println("전체 법인: ${corpCodeList.size}개")
        println("상장 법인: ${listedCorps.size}개")
        println("비상장 법인: ${corpCodeList.size - listedCorps.size}개")
    }
}
