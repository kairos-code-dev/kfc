package dev.kairoscode.kfc.integration.corp

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import kotlinx.coroutines.delay
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


/**
 * 배당 정보 조회 Integration Test
 *
 * getDividendInfo() 함수의 실제 API 호출 테스트 및 응답 레코딩
 *
 * 주의사항:
 * - OPENDART_API_KEY가 필요합니다
 * - 2015년 이후 데이터만 지원합니다
 * - Rate Limiting을 고려하여 적절한 지연을 추가합니다
 */
@DisplayName("CorpApi.getDividendInfo() - 배당 정보 조회")
class CorpApiDividendSpec : IntegrationTestBase() {

    @Test
    @DisplayName("특정 법인의 배당 정보를 고정 연도로 조회할 수 있다")
    fun testGetDividendInfo() = integrationTest {
        // Given: 삼성전자 corp_code, year (고정: 2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023 // 고정 연도

        // When: 배당 정보 조회
        val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)

        // Then: 배당금 정보 반환 (현금배당, 주식배당 등)
        // 주의: 배당이 없는 경우 빈 리스트 반환 가능
        println("✅ 삼성전자 ${year}년 배당 정보 개수: ${dividendInfo.size}")

        if (dividendInfo.isNotEmpty()) {
            dividendInfo.forEach { info ->
                println("  - 배당 구분: ${info.dividendType}, 주식 종류: ${info.stockKind}")
            }
        }

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = dividendInfo,
            category = RecordingConfig.Paths.CorpActions.DIVIDEND,
            fileName = "samsung_dividend_$year"
        )
    }

    @Test
    @DisplayName("카카오 배당 정보를 고정 연도로 조회할 수 있다")
    fun testGetDividendInfoKakao() = integrationTest {
        // Given: 카카오 corp_code (고정: 2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
        val year = 2023 // 고정 연도

        // When: 배당 정보 조회
        val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)

        // Then: 배당 정보 반환
        println("✅ 카카오 ${year}년 배당 정보 개수: ${dividendInfo.size}")

        // 스마트 레코딩
        SmartRecorder.recordSmartly(
            data = dividendInfo,
            category = RecordingConfig.Paths.CorpActions.DIVIDEND,
            fileName = "kakao_dividend_$year"
        )
    }

    @Test
    @DisplayName("다양한 보고서 타입으로 고정 연도 기준 조회할 수 있다")
    fun testGetDividendInfoWithDifferentReportCodes() = integrationTest {
        // Given: 삼성전자 corp_code (고정: 2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val year = 2023 // 고정 연도

        val reportCodes = mapOf(
            "11011" to "사업보고서",
            "11012" to "반기보고서",
            "11013" to "1분기보고서",
            "11014" to "3분기보고서"
        )

        // When: reportCode를 변경하여 호출
        println("\n=== 보고서 타입별 배당 정보 (${year}년) ===")
        reportCodes.forEach { (reportCode, reportName) ->
            val dividendInfo = client.corp!!.getDividendInfo(corpCode, year, reportCode)
            println("$reportName ($reportCode): ${dividendInfo.size}건")

            // Rate Limiting 고려
            delay(500)
        }
    }

    @Test
    @DisplayName("[활용] 고정 기간 기준으로 배당 이력을 조회할 수 있다")
    fun testDividendHistory() = integrationTest {
        // Given: 고정 기간 배당 정보 (2021-2023년)
        requireOpendartApiKey()
        val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
        val years = listOf(2021, 2022, 2023) // 고정 연도 목록

        // When: 각 연도별 getDividendInfo() 호출
        println("\n=== 삼성전자 배당 이력 (2021-2023년) ===")
        years.forEach { year ->
            val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)
            println("${year}년: ${dividendInfo.size}건")

            // Rate Limiting 고려
            delay(500)
        }
    }
}
