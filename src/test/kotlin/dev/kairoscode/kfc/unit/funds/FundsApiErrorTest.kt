package dev.kairoscode.kfc.unit.funds

import dev.kairoscode.kfc.unit.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.unit.utils.UnitTestBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * FundsApi 에러 핸들링 테스트
 *
 * 정상 케이스는 통합 테스트(FundsApi*Spec)에서 검증합니다.
 * 이 테스트는 에러 상황만 검증합니다.
 *
 * ## 테스트 범위
 * - 네트워크 에러 시뮬레이션
 * - 잘못된 응답 파싱 처리
 * - 빈 응답 처리
 * - 필수 파라미터 검증
 *
 * @see FakeFundsApi
 */
@DisplayName("[U][Funds] FundsApi - 에러 핸들링")
class FundsApiErrorTest : UnitTestBase() {
    @Nested
    @DisplayName("Fake API 설정 에러")
    inner class FakeConfigurationErrors {
        @Test
        @DisplayName("응답이 설정되지 않은 경우 IllegalArgumentException을 던진다")
        fun `throws IllegalArgumentException when response is not configured`() =
            unitTest {
                // Given: 응답이 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        client.funds.getList()
                    }
                assertTrue(exception.message!!.contains("listResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getDetailedInfo - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getDetailedInfo throws exception when response is not configured`() =
            unitTest {
                // Given: comprehensiveResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        client.funds.getDetailedInfo("KR7069500007", java.time.LocalDate.now())
                    }
                assertTrue(exception.message!!.contains("comprehensiveResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getPortfolio - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getPortfolio throws exception when response is not configured`() =
            unitTest {
                // Given: portfolioResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        client.funds.getPortfolio("KR7069500007", java.time.LocalDate.now())
                    }
                assertTrue(exception.message!!.contains("portfolioResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getTrackingError - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getTrackingError throws exception when response is not configured`() =
            unitTest {
                // Given: trackingErrorResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        val fromDate =
                            java.time.LocalDate
                                .now()
                                .minusDays(7)
                        val toDate = java.time.LocalDate.now()
                        client.funds.getTrackingError("KR7069500007", fromDate, toDate)
                    }
                assertTrue(exception.message!!.contains("trackingErrorResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getDivergenceRate - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getDivergenceRate throws exception when response is not configured`() =
            unitTest {
                // Given: divergenceRateResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        val fromDate =
                            java.time.LocalDate
                                .now()
                                .minusDays(7)
                        val toDate = java.time.LocalDate.now()
                        client.funds.getDivergenceRate("KR7069500007", fromDate, toDate)
                    }
                assertTrue(exception.message!!.contains("divergenceRateResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getInvestorTrading - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getInvestorTrading throws exception when response is not configured`() =
            unitTest {
                // Given: investorTradingResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        client.funds.getInvestorTrading("KR7069500007", java.time.LocalDate.now())
                    }
                assertTrue(exception.message!!.contains("investorTradingResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getShortSelling - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getShortSelling throws exception when response is not configured`() =
            unitTest {
                // Given: shortSellingResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        val fromDate =
                            java.time.LocalDate
                                .now()
                                .minusDays(7)
                        val toDate = java.time.LocalDate.now()
                        client.funds.getShortSelling(
                            "KR7069500007",
                            fromDate,
                            toDate,
                            dev.kairoscode.kfc.domain.FundType.ETF,
                        )
                    }
                assertTrue(exception.message!!.contains("shortSellingResponse가 설정되지 않았습니다"))
            }

        @Test
        @DisplayName("getShortBalance - 응답이 설정되지 않은 경우 예외를 던진다")
        fun `getShortBalance throws exception when response is not configured`() =
            unitTest {
                // Given: shortBalanceResponse가 설정되지 않은 Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: API 호출 시 예외 발생
                val exception =
                    assertThrows<IllegalArgumentException> {
                        val fromDate =
                            java.time.LocalDate
                                .now()
                                .minusDays(7)
                        val toDate = java.time.LocalDate.now()
                        client.funds.getShortBalance(
                            "KR7069500007",
                            fromDate,
                            toDate,
                            dev.kairoscode.kfc.domain.FundType.ETF,
                        )
                    }
                assertTrue(exception.message!!.contains("shortBalanceResponse가 설정되지 않았습니다"))
            }
    }

    @Nested
    @DisplayName("파싱 에러")
    inner class ParsingErrors {
        @Test
        @DisplayName("빈 배열 응답 시 빈 리스트를 반환한다")
        fun `returns empty list on empty array response`() =
            unitTest {
                // Given: 빈 JSON 배열 응답
                fakeFundsApi =
                    FakeFundsApi(
                        listResponse = "[]",
                    )
                initClient()

                // When: API 호출
                val result = client.funds.getList()

                // Then: 빈 리스트 반환
                assertTrue(result.isEmpty())
            }

        @Test
        @DisplayName("getPortfolio - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `getPortfolio returns empty list on empty array response`() =
            unitTest {
                // Given: 빈 JSON 배열 응답
                fakeFundsApi =
                    FakeFundsApi(
                        portfolioResponse = "[]",
                    )
                initClient()

                // When: API 호출
                val result = client.funds.getPortfolio("KR7069500007", java.time.LocalDate.now())

                // Then: 빈 리스트 반환
                assertTrue(result.isEmpty())
            }

        @Test
        @DisplayName("getTrackingError - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `getTrackingError returns empty list on empty array response`() =
            unitTest {
                // Given: 빈 JSON 배열 응답
                fakeFundsApi =
                    FakeFundsApi(
                        trackingErrorResponse = "[]",
                    )
                initClient()

                // When: API 호출
                val fromDate =
                    java.time.LocalDate
                        .now()
                        .minusDays(7)
                val toDate = java.time.LocalDate.now()
                val result = client.funds.getTrackingError("KR7069500007", fromDate, toDate)

                // Then: 빈 리스트 반환
                assertTrue(result.isEmpty())
            }

        @Test
        @DisplayName("getDivergenceRate - 빈 배열 응답 시 빈 리스트를 반환한다")
        fun `getDivergenceRate returns empty list on empty array response`() =
            unitTest {
                // Given: 빈 JSON 배열 응답
                fakeFundsApi =
                    FakeFundsApi(
                        divergenceRateResponse = "[]",
                    )
                initClient()

                // When: API 호출
                val fromDate =
                    java.time.LocalDate
                        .now()
                        .minusDays(7)
                val toDate = java.time.LocalDate.now()
                val result = client.funds.getDivergenceRate("KR7069500007", fromDate, toDate)

                // Then: 빈 리스트 반환
                assertTrue(result.isEmpty())
            }
    }

    @Nested
    @DisplayName("지원하지 않는 메서드 호출")
    inner class UnsupportedOperations {
        @Test
        @DisplayName("getGeneralInfo - UnsupportedOperationException을 던진다")
        fun `getGeneralInfo throws UnsupportedOperationException`() =
            unitTest {
                // Given: Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: 지원하지 않는 메서드 호출 시 예외 발생
                val exception =
                    assertThrows<UnsupportedOperationException> {
                        client.funds.getGeneralInfo("KR7069500007", java.time.LocalDate.now())
                    }
                assertTrue(exception.message!!.contains("Fake에서 지원하지 않는 메서드입니다"))
            }

        @Test
        @DisplayName("getPortfolioTop10 - UnsupportedOperationException을 던진다")
        fun `getPortfolioTop10 throws UnsupportedOperationException`() =
            unitTest {
                // Given: Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: 지원하지 않는 메서드 호출 시 예외 발생
                val exception =
                    assertThrows<UnsupportedOperationException> {
                        client.funds.getPortfolioTop10("KR7069500007", java.time.LocalDate.now())
                    }
                assertTrue(exception.message!!.contains("Fake에서 지원하지 않는 메서드입니다"))
            }

        @Test
        @DisplayName("getAllInvestorTradingByPeriod - UnsupportedOperationException을 던진다")
        fun `getAllInvestorTradingByPeriod throws UnsupportedOperationException`() =
            unitTest {
                // Given: Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: 지원하지 않는 메서드 호출 시 예외 발생
                val exception =
                    assertThrows<UnsupportedOperationException> {
                        val fromDate =
                            java.time.LocalDate
                                .now()
                                .minusDays(7)
                        val toDate = java.time.LocalDate.now()
                        client.funds.getAllInvestorTradingByPeriod(fromDate, toDate)
                    }
                assertTrue(exception.message!!.contains("Fake에서 지원하지 않는 메서드입니다"))
            }

        @Test
        @DisplayName("getInvestorTradingByPeriod - UnsupportedOperationException을 던진다")
        fun `getInvestorTradingByPeriod throws UnsupportedOperationException`() =
            unitTest {
                // Given: Fake API
                fakeFundsApi = FakeFundsApi()
                initClient()

                // When & Then: 지원하지 않는 메서드 호출 시 예외 발생
                val exception =
                    assertThrows<UnsupportedOperationException> {
                        val fromDate =
                            java.time.LocalDate
                                .now()
                                .minusDays(7)
                        val toDate = java.time.LocalDate.now()
                        client.funds.getInvestorTradingByPeriod("KR7069500007", fromDate, toDate)
                    }
                assertTrue(exception.message!!.contains("Fake에서 지원하지 않는 메서드입니다"))
            }
    }
}
