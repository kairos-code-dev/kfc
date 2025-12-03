package dev.kairoscode.kfc.corp

import dev.kairoscode.kfc.corp.fake.FakeCorpApi
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("CorpApi - searchDisclosures()")
class DisclosureApiTest : UnitTestBase() {

    @Test
    @DisplayName("특정 법인의 공시를 조회할 수 있다")
    fun searchDisclosures_withCorpCodeAndDateRange_returnsDisclosures() = unitTest {
        // Given: 삼성전자의 1개월간 공시 정보 응답을 준비
        val jsonResponse = loadDisclosureResponse("samsung_disclosure_1month")
        fakeCorpApi = FakeCorpApi(disclosureResponse = jsonResponse)
        initClient()
        val startDate = LocalDate.of(2024, 10, 25)
        val endDate = LocalDate.of(2024, 11, 25)

        // When: 특정 법인의 특정 기간 공시를 조회
        val disclosures = client.corp!!.searchDisclosures(
            corpCode = TestData.ValidCorp.SAMSUNG_CORP_CODE,
            startDate = startDate,
            endDate = endDate
        )

        // Then: 공시 목록이 조회되고, 각 항목이 올바른 데이터를 가짐
        assertThat(disclosures)
            .describedAs("공시 목록이 비어있습니다 (corpCode: %s, period: %s ~ %s)",
                TestData.ValidCorp.SAMSUNG_CORP_CODE, startDate, endDate)
            .isNotEmpty
        disclosures.forEach { disclosure ->
            assertThat(disclosure.reportName)
                .describedAs("공시명이 비어있습니다")
                .isNotBlank()
            assertThat(disclosure.rceptDate)
                .describedAs("접수일자가 null입니다 (report: %s)", disclosure.reportName)
                .isNotNull()
        }
        println("공시 목록: ${disclosures.size}건")
        disclosures.take(3).forEach {
            println("  [${it.rceptDate}] ${it.reportName}")
        }
    }
}
