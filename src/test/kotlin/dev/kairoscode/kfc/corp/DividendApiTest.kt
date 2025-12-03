package dev.kairoscode.kfc.corp

import dev.kairoscode.kfc.corp.fake.FakeCorpApi
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("CorpApi - getDividendInfo()")
class DividendApiTest : UnitTestBase() {

    @Test
    @DisplayName("특정 연도 배당 정보를 조회할 수 있다")
    fun getDividendInfo_withCorpCodeAndYear_returnsDividendInfo() = unitTest {
        // Given: 삼성전자의 2024년 배당 정보 응답을 준비
        val jsonResponse = loadDividendResponse("samsung_dividend_2024")
        fakeCorpApi = FakeCorpApi(dividendResponse = jsonResponse)
        initClient()

        // When: 특정 법인의 특정 연도 배당 정보를 조회
        val dividends = client.corp!!.getDividendInfo(
            corpCode = TestData.ValidCorp.SAMSUNG_CORP_CODE,
            year = 2024
        )

        // Then: 배당 정보가 조회되고, 각 항목이 올바른 데이터를 가짐
        assertThat(dividends)
            .describedAs("배당 정보가 비어있습니다 (corpCode: %s, year: %d)",
                TestData.ValidCorp.SAMSUNG_CORP_CODE, 2024)
            .isNotEmpty
        dividends.forEach { dividend ->
            assertThat(dividend.dividendType)
                .describedAs("배당 종류가 비어있습니다")
                .isNotBlank()
            if (dividend.currentYear != null) {
                assertThat(dividend.currentYear)
                    .describedAs("배당금액이 음수입니다 (type: %s)", dividend.dividendType)
                    .isGreaterThanOrEqualTo(BigDecimal.ZERO)
            }
        }
        println("배당 정보: ${dividends.size}건")
        dividends.forEach {
            println("  ${it.dividendType}: ${it.currentYear}원")
        }
    }
}
