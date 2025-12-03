package dev.kairoscode.kfc.api.funds

import dev.kairoscode.kfc.mock.MockFundsApi
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
        fun `basic usage - can retrieve comprehensive ETF info`() = unitTest {
            // Given
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            mockFundsApi = MockFundsApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When
            val info = client.funds.getDetailedInfo("KR7069500007")

            // Then
            assertThat(info).isNotNull
            assertThat(info!!.name).isNotBlank
            assertThat(info.closePrice).isPositive

            println("ETF 상세 정보: ${info.name}")
            println("  종가: ${info.closePrice}, 괴리율: ${info.calculateDivergenceRate()}%")
            println("  52주 고가: ${info.week52High}, 52주 저가: ${info.week52Low}")
        }

        @Test
        @DisplayName("활용 예제 - NAV 대비 괴리율을 확인할 수 있다")
        fun `usage example - can check divergence rate vs NAV`() = unitTest {
            // Given
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            mockFundsApi = MockFundsApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When
            val info = client.funds.getDetailedInfo("KR7069500007")

            // Then
            assertThat(info).isNotNull
            val divergenceRate = info!!.calculateDivergenceRate()
            assertThat(divergenceRate).isNotNull

            println("NAV: ${info.nav}, 종가: ${info.closePrice}")
            println("괴리율: ${divergenceRate}%")
        }
    }

    @Nested
    @DisplayName("getDetailedInfo() API 명세")
    inner class GetDetailedInfoSpecification {

        @Test
        @DisplayName("[명세] 모든 주요 필드를 포함한다")
        fun `specification - contains all major fields`() = unitTest {
            // Given
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            mockFundsApi = MockFundsApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When
            val info = client.funds.getDetailedInfo("KR7069500007")

            // Then: 주요 필드 검증
            assertThat(info).isNotNull
            with(info!!) {
                assertThat(openPrice).isPositive
                assertThat(highPrice).isPositive
                assertThat(lowPrice).isPositive
                assertThat(closePrice).isPositive
                assertThat(nav).isPositive
                assertThat(week52High).isPositive
                assertThat(week52Low).isPositive
            }
        }
    }
}
