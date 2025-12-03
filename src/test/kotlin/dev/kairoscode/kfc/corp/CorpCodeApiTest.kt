package dev.kairoscode.kfc.corp

import dev.kairoscode.kfc.corp.fake.FakeCorpApi
import dev.kairoscode.kfc.utils.TestData
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
        fun getCorpCodeList_withNoFilter_returnsAllCorpCodes() = unitTest {
            // Given: FakeCorpApi를 사용하여 테스트 환경 설정
            val jsonResponse = loadCorpCodeResponse("corp_code_list")
            fakeCorpApi = FakeCorpApi(corpCodeResponse = jsonResponse)
            initClient()

            // When: 전체 법인 목록을 조회
            val corpCodes = client.corp!!.getCorpCodeList()

            // Then: 법인 목록이 비어있지 않고, 각 항목이 올바른 형식을 가짐
            assertThat(corpCodes)
                .describedAs("기업코드 목록이 비어있습니다")
                .isNotEmpty
            println("법인 코드 목록: ${corpCodes.size}개")
            corpCodes.take(3).forEach {
                println("  ${it.corpName} (${it.corpCode}): ${it.stockCode}")
            }
        }

        @Test
        @DisplayName("검색 예제 - 종목코드로 고유번호를 찾을 수 있다")
        fun getCorpCodeList_searchByStockCode_findsMatchingCorp() = unitTest {
            // Given: FakeCorpApi를 사용하여 테스트 환경 설정
            val jsonResponse = loadCorpCodeResponse("corp_code_list")
            fakeCorpApi = FakeCorpApi(corpCodeResponse = jsonResponse)
            initClient()

            // When: 전체 목록 조회 후 종목코드로 검색
            val corpCodes = client.corp!!.getCorpCodeList()
            val samsung = corpCodes.find { it.stockCode == TestData.ValidCorp.SAMSUNG_STOCK_CODE }

            // Then: 삼성전자 정보가 조회되고 올바른 기업명을 가짐
            assertThat(samsung)
                .describedAs("종목코드 %s로 기업을 찾을 수 없습니다", TestData.ValidCorp.SAMSUNG_STOCK_CODE)
                .isNotNull
            assertThat(samsung!!.corpName)
                .describedAs("삼성전자 기업명이 올바르지 않습니다")
                .contains("삼성전자")
            println("삼성전자 고유번호: ${samsung.corpCode}")
        }
    }

    @Nested
    @DisplayName("getCorpCodeList() API 명세")
    inner class GetCorpCodeListSpecification {

        @Test
        @DisplayName("[명세] corp_code는 8자리 숫자 문자열이다")
        fun getCorpCodeList_allCorpCodes_haveValidFormat() = unitTest {
            // Given: FakeCorpApi를 사용하여 테스트 환경 설정
            val jsonResponse = loadCorpCodeResponse("corp_code_list")
            fakeCorpApi = FakeCorpApi(corpCodeResponse = jsonResponse)
            initClient()

            // When: 전체 법인 목록을 조회
            val corpCodes = client.corp!!.getCorpCodeList()

            // Then: 모든 corpCode가 8자리 숫자 형식을 가짐
            corpCodes.forEach { corpCode ->
                assertThat(corpCode.corpCode)
                    .describedAs("기업코드 형식이 잘못되었습니다 (code: %s, name: %s)",
                        corpCode.corpCode, corpCode.corpName)
                    .hasSize(8)
                    .matches("\\d{8}")
                assertThat(corpCode.corpName)
                    .describedAs("기업명이 비어있습니다 (code: %s)", corpCode.corpCode)
                    .isNotBlank()
            }
        }
    }
}
