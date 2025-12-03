package dev.kairoscode.kfc.api.corp

import dev.kairoscode.kfc.mock.MockCorpApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CorpApi - getCorpCodeList()")
class CorpCodeApiTest : UnitTestBase() {

    @Nested
    @DisplayName("getCorpCodeList() 사용법")
    inner class GetCorpCodeListUsage {

        @Test
        @DisplayName("기본 사용법 - 전체 법인 목록을 조회할 수 있다")
        fun `basic usage - can retrieve all corp code list`() = unitTest {
            // Given
            val jsonResponse = loadCorpCodeResponse("corp_code_list")
            mockCorpApi = MockCorpApi(corpCodeResponse = jsonResponse)
            initClient()

            // When
            val corpCodes = client.corp!!.getCorpCodeList()

            // Then
            assertThat(corpCodes).isNotEmpty
            println("법인 코드 목록: ${corpCodes.size}개")
            corpCodes.take(3).forEach {
                println("  ${it.corpName} (${it.corpCode}): ${it.stockCode}")
            }
        }

        @Test
        @DisplayName("검색 예제 - 종목코드로 고유번호를 찾을 수 있다")
        fun `search example - can find corp code by stock code`() = unitTest {
            // Given
            val jsonResponse = loadCorpCodeResponse("corp_code_list")
            mockCorpApi = MockCorpApi(corpCodeResponse = jsonResponse)
            initClient()

            // When
            val corpCodes = client.corp!!.getCorpCodeList()
            val samsung = corpCodes.find { it.stockCode == "005930" }

            // Then
            assertThat(samsung).isNotNull
            assertThat(samsung!!.corpName).contains("삼성전자")
            println("삼성전자 고유번호: ${samsung.corpCode}")
        }
    }

    @Nested
    @DisplayName("getCorpCodeList() API 명세")
    inner class GetCorpCodeListSpecification {

        @Test
        @DisplayName("[명세] corp_code는 8자리 숫자 문자열이다")
        fun `specification - corp code format`() = unitTest {
            // Given
            val jsonResponse = loadCorpCodeResponse("corp_code_list")
            mockCorpApi = MockCorpApi(corpCodeResponse = jsonResponse)
            initClient()

            // When
            val corpCodes = client.corp!!.getCorpCodeList()

            // Then: corpCode 형식 검증
            corpCodes.forEach { corpCode ->
                assertThat(corpCode.corpCode).hasSize(8)
                assertThat(corpCode.corpCode).matches("\\d{8}")
            }
        }
    }
}
