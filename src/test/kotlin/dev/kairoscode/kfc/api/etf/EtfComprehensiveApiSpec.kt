package dev.kairoscode.kfc.api.etf

import dev.kairoscode.kfc.mock.MockEtfApi
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * EtfApi.getComprehensiveInfo() Unit Test
 *
 * ETF 종합 정보 조회 API의 동작을 검증하는 Unit Test입니다.
 */
@DisplayName("EtfApi - getComprehensiveInfo()")
class EtfComprehensiveApiSpec : UnitTestBase() {

    @Nested
    @DisplayName("getComprehensiveInfo() 사용법")
    inner class GetComprehensiveInfoUsage {

        @Test
        @DisplayName("기본 사용법 - ETF 종합 정보를 조회할 수 있다")
        fun `basic usage - can retrieve comprehensive ETF info`() = unitTest {
            // Given
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            mockEtfApi = MockEtfApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When
            val info = client.etf.getComprehensiveInfo("KR7069500007")

            // Then
            assertThat(info).isNotNull
            assertThat(info!!.name).isNotBlank
            assertThat(info.closePrice).isPositive

            println("ETF 종합 정보: ${info.name}")
            println("  종가: ${info.closePrice}, 괴리율: ${info.divergenceRate}%")
            println("  52주 고가: ${info.week52High}, 52주 저가: ${info.week52Low}")
        }

        @Test
        @DisplayName("활용 예제 - NAV 대비 괴리율을 확인할 수 있다")
        fun `usage example - can check divergence rate vs NAV`() = unitTest {
            // Given
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            mockEtfApi = MockEtfApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When
            val info = client.etf.getComprehensiveInfo("KR7069500007")

            // Then
            assertThat(info).isNotNull
            assertThat(info!!.divergenceRate).isNotNull

            println("NAV: ${info.nav}, 종가: ${info.closePrice}")
            println("괴리율: ${info.divergenceRate}%")
        }
    }

    @Nested
    @DisplayName("getComprehensiveInfo() API 명세")
    inner class GetComprehensiveInfoSpecification {

        @Test
        @DisplayName("[명세] 모든 주요 필드를 포함한다")
        fun `specification - contains all major fields`() = unitTest {
            // Given
            val jsonResponse = loadEtfComprehensiveResponse("tiger200")
            mockEtfApi = MockEtfApi(comprehensiveResponse = jsonResponse)
            initClient()

            // When
            val info = client.etf.getComprehensiveInfo("KR7069500007")

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
