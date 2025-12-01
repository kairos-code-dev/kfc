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
 * 배당 정보 조회 Live Test
 *
 * getDividendInfo() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 * - Rate Limiting을 고려하여 적절한 지연을 추가합니다
 */
class DividendLiveTest : LiveTestBase() {

    @Test
    @DisplayName("특정 법인의 배당 정보를 조회할 수 있다")
    fun testGetDividendInfo() = liveTest {
        // Given: 삼성전자 corp_code, year (2024)
        val corpCode = TestSymbols.SAMSUNG_CORP_CODE
        val year = LocalDate.now().year - 1 // 작년 데이터

        // When: 배당 정보 조회
        val dividendInfo = client.corp?.getDividendInfo(corpCode, year) ?: return@liveTest

        // Then: 배당금 정보 반환 (현금배당, 주식배당 등)
        // 주의: 배당이 없는 경우 빈 리스트 반환 가능
        println("✅ 삼성전자 ${year}년 배당 정보 개수: ${dividendInfo.size}")

        if (dividendInfo.isNotEmpty()) {
            dividendInfo.forEach { info ->
                println("  - 배당 구분: ${info.dividendType}, 주식 종류: ${info.stockKind}")
            }
        }

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = dividendInfo,
            category = RecordingConfig.Paths.Corp.DIVIDEND,
            fileName = "samsung_dividend_$year"
        )
    }

    @Test
    @DisplayName("카카오 배당 정보를 조회할 수 있다")
    fun testGetDividendInfoKakao() = liveTest {
        // Given: 카카오 corp_code
        val corpCode = TestSymbols.KAKAO_CORP_CODE
        val year = LocalDate.now().year - 1

        // When: 배당 정보 조회
        val dividendInfo = client.corp?.getDividendInfo(corpCode, year) ?: return@liveTest

        // Then: 배당 정보 반환
        println("✅ 카카오 ${year}년 배당 정보 개수: ${dividendInfo.size}")

        // 응답 레코딩
        ResponseRecorder.recordList(
            data = dividendInfo,
            category = RecordingConfig.Paths.Corp.DIVIDEND,
            fileName = "kakao_dividend_$year"
        )
    }

    @Test
    @DisplayName("다양한 보고서 타입으로 조회할 수 있다")
    fun testGetDividendInfoWithDifferentReportCodes() = liveTest {
        // Given: 삼성전자 corp_code
        val corpCode = TestSymbols.SAMSUNG_CORP_CODE
        val year = LocalDate.now().year - 1

        val reportCodes = mapOf(
            "11011" to "사업보고서",
            "11012" to "반기보고서",
            "11013" to "1분기보고서",
            "11014" to "3분기보고서"
        )

        // When: reportCode를 변경하여 호출
        println("\n=== 보고서 타입별 배당 정보 ===")
        reportCodes.forEach { (reportCode, reportName) ->
            val dividendInfo = client.corp?.getDividendInfo(corpCode, year, reportCode) ?: return@liveTest
            println("$reportName ($reportCode): ${dividendInfo.size}건")

            // Rate Limiting 고려
            delay(500)
        }
    }

    @Test
    @DisplayName("[활용] 배당 이력을 조회할 수 있다")
    fun testDividendHistory() = liveTest {
        // Given: 최근 3년 배당 정보
        val corpCode = TestSymbols.SAMSUNG_CORP_CODE
        val currentYear = LocalDate.now().year
        val years = listOf(currentYear - 3, currentYear - 2, currentYear - 1)

        // When: 각 연도별 getDividendInfo() 호출
        println("\n=== 삼성전자 배당 이력 (최근 3년) ===")
        years.forEach { year ->
            val dividendInfo = client.corp?.getDividendInfo(corpCode, year) ?: return@liveTest
            println("${year}년: ${dividendInfo.size}건")

            // Rate Limiting 고려
            delay(500)
        }
    }
}
