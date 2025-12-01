package dev.kairoscode.kfc.api.corp

import dev.kairoscode.kfc.mock.MockCorpApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("CorpApi - searchDisclosures()")
class DisclosureApiSpec : UnitTestBase() {

    @Test
    @DisplayName("특정 법인의 공시를 조회할 수 있다")
    fun `can search disclosures for specific corp`() = unitTest {
        // Given
        val jsonResponse = loadDisclosureResponse("samsung_disclosure_1month")
        mockCorpApi = MockCorpApi(disclosureResponse = jsonResponse)
        initClient()

        // When
        val disclosures = client.corp!!.searchDisclosures(
            corpCode = "00126380",
            startDate = LocalDate.of(2024, 10, 25),
            endDate = LocalDate.of(2024, 11, 25)
        )

        // Then
        assertThat(disclosures).isNotEmpty
        println("공시 목록: ${disclosures.size}건")
        disclosures.take(3).forEach {
            println("  [${it.rceptDate}] ${it.reportName}")
        }
    }
}
