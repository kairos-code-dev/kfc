package dev.kairoscode.kfc.corp

import dev.kairoscode.kfc.corp.fake.FakeCorpApi
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CorpApi - getStockSplitInfo()")
class StockSplitApiTest : UnitTestBase() {

    @Test
    @DisplayName("주식 분할/병합 정보를 조회할 수 있다")
    fun getStockSplitInfo_withCorpCodeAndYear_returnsStockSplitInfo() = unitTest {
        // Given: 특정 법인의 2024년 주식 분할/병합 정보 응답을 준비
        val jsonResponse = loadStockSplitResponse("stock_split_2024")
        fakeCorpApi = FakeCorpApi(stockSplitResponse = jsonResponse)
        initClient()
        val testCorpCode = "00164470"

        // When: 특정 법인의 특정 연도 주식 분할/병합 정보를 조회
        val splits = client.corp!!.getStockSplitInfo(
            corpCode = testCorpCode,
            year = 2024
        )

        // Then: 주식 분할/병합 정보가 조회되고, 각 항목이 올바른 데이터를 가짐
        assertThat(splits)
            .describedAs("주식 분할/병합 정보가 비어있습니다 (corpCode: %s, year: %d)",
                testCorpCode, 2024)
            .isNotEmpty
        splits.forEach { split ->
            assertThat(split.eventType)
                .describedAs("이벤트 종류가 비어있습니다")
                .isNotBlank()
            assertThat(split.eventDate)
                .describedAs("이벤트 날짜가 null입니다 (type: %s)", split.eventType)
                .isNotNull()
        }
        println("주식 분할 정보: ${splits.size}건")
        splits.forEach {
            println("  ${it.eventType}: ${it.eventDate}")
        }
    }
}
