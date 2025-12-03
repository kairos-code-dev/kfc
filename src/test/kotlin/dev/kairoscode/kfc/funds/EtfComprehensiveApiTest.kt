package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * EtfApi.getDetailedInfo() Unit Test
 *
 * ETF 상세정보 조회 API의 동작을 검증하는 Unit Test입니다.
 */
@DisplayName("EtfApi - getDetailedInfo()")
class EtfComprehensiveApiTest : UnitTestBase() {

    @Nested
    @DisplayName("getDetailedInfo() 사용법")
    inner class GetDetailedInfoUsage {

        @Test
        @DisplayName("기본 사용법 - ETF 상세 정보를 조회할 수 있다")
        fun `basicUsage_retrieveComprehensiveInfo_success`() = unitTest {
            // Given: TIGER 200 ETF의 상세 정보 응답 준비
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            fakeFundsApi = FakeFundsApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When: 상세 정보를 조회하면
            val info = client.funds.getDetailedInfo("KR7069500007")

            // Then: 주요 필드가 유효한 값으로 반환된다
            assertThat(info)
                .describedAs("상세 정보가 null입니다")
                .isNotNull
            assertThat(info!!.name)
                .describedAs("ETF 이름이 비어있습니다")
                .isNotBlank
            assertThat(info.closePrice)
                .describedAs("종가가 양수가 아닙니다")
                .isPositive

            println("ETF 상세 정보: ${info.name}")
            println("  종가: ${info.closePrice}, 괴리율: ${info.calculateDivergenceRate()}%")
            println("  52주 고가: ${info.week52High}, 52주 저가: ${info.week52Low}")
        }

        @Test
        @DisplayName("활용 예제 - NAV 대비 괴리율을 확인할 수 있다")
        fun `usageExample_checkDivergenceRate_calculatesCorrectly`() = unitTest {
            // Given: TIGER 200 ETF의 상세 정보 응답 준비
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            fakeFundsApi = FakeFundsApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When: 상세 정보를 조회하여 괴리율을 계산하면
            val info = client.funds.getDetailedInfo("KR7069500007")

            // Then: 괴리율이 계산된다
            assertThat(info)
                .describedAs("상세 정보가 null입니다")
                .isNotNull
            val divergenceRate = info!!.calculateDivergenceRate()
            assertThat(divergenceRate)
                .describedAs("괴리율이 null입니다")
                .isNotNull

            println("NAV: ${info.nav}, 종가: ${info.closePrice}")
            println("괴리율: ${divergenceRate}%")
        }
    }

    @Nested
    @DisplayName("getDetailedInfo() API 명세")
    inner class GetDetailedInfoSpecification {

        @Test
        @DisplayName("[명세] 모든 주요 필드를 포함한다")
        fun `specification_allFields_containsMajorFields`() = unitTest {
            // Given: TIGER 200 ETF의 상세 정보 응답 준비
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            fakeFundsApi = FakeFundsApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When: 상세 정보를 조회하면
            val info = client.funds.getDetailedInfo("KR7069500007")

            // Then: 모든 주요 필드가 유효한 값으로 반환된다
            assertThat(info)
                .describedAs("상세 정보가 null입니다")
                .isNotNull
            with(info!!) {
                assertThat(openPrice)
                    .describedAs("시가가 양수가 아닙니다")
                    .isPositive
                assertThat(highPrice)
                    .describedAs("고가가 양수가 아닙니다")
                    .isPositive
                assertThat(lowPrice)
                    .describedAs("저가가 양수가 아닙니다")
                    .isPositive
                assertThat(closePrice)
                    .describedAs("종가가 양수가 아닙니다")
                    .isPositive
                assertThat(nav)
                    .describedAs("NAV가 양수가 아닙니다")
                    .isPositive
                assertThat(week52High)
                    .describedAs("52주 고가가 양수가 아닙니다")
                    .isPositive
                assertThat(week52Low)
                    .describedAs("52주 저가가 양수가 아닙니다")
                    .isPositive
            }
        }
    }
}
