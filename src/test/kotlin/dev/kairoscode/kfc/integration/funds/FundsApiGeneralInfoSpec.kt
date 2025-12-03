package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.ResponseRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * FundsApi.getGeneralInfo() API 스펙
 *
 * ETF의 기본정보(메타데이터)를 조회하는 API입니다.
 * ETF 명, ISIN, 자산 분류, 발행사/운용사, 상장일, 순자산총액 등의 정보를 제공합니다.
 */
@DisplayName("FundsApi.getGeneralInfo() - 펀드 기본 정보 조회")
class FundsApiGeneralInfoSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("기본 동작")
    inner class BasicBehavior {

        @Test
        @DisplayName("거래일에 TIGER 200 기본정보를 조회할 수 있다")
        fun `returns general info for TIGER 200 on trading day`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "거래일에는 TIGER 200의 기본정보가 반환되어야 합니다")

            println("✅ TIGER 200 기본정보 (거래일: $tradeDate)")
            println("  - ETF 명: ${info?.name}")
            println("  - ISIN: ${info?.isin}")
            println("  - 자산 분류: ${info?.assetClassName}")
            println("  - 발행사/운용사: ${info?.issuerName}")
            println("  - 상장일: ${info?.listingDate}")
            println("  - 순자산총액: ${info?.netAssetTotal}원")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.GENERAL_INFO,
                fileName = "tiger200_general_info"
            )
        }

        @Test
        @DisplayName("거래일에 KODEX 200 기본정보를 조회할 수 있다")
        fun `returns general info for KODEX 200 on trading day`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY // 2024-11-25 (월요일)

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "거래일에는 KODEX 200의 기본정보가 반환되어야 합니다")
            println("✅ KODEX 200 기본정보 (거래일: $tradeDate)")
            println("  - ETF 명: ${info?.name}")
            println("  - 자산 분류: ${info?.assetClassName}")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.GENERAL_INFO,
                fileName = "kodex200_general_info"
            )
        }

        @Test
        @DisplayName("비거래일에 조회하면 정적 메타데이터를 반환한다")
        fun `returns static metadata on non-trading day`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.NON_TRADING_DAY // 2024-11-23 (토요일)

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "API는 비거래일에도 데이터를 반환합니다 (정적 메타데이터)")
            println("✅ 비거래일($tradeDate) 기본정보 조회 결과:")
            println("  - 데이터 존재: 예 (정적 메타데이터)")
            println("  - ETF 명: ${info?.name}")
        }
    }

    @Nested
    @DisplayName("응답 데이터 스펙")
    inner class ResponseSpec {

        @Test
        @DisplayName("필수 필드를 모두 포함한다")
        fun `contains all required fields`() = integrationTest {
            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertTrue(!info?.name.isNullOrEmpty(), "ETF 이름이 있어야 합니다")
            assertTrue(!info?.isin.isNullOrEmpty(), "ISIN 코드가 있어야 합니다")
            assertTrue(!info?.assetClassName.isNullOrEmpty(), "자산 분류가 있어야 합니다")
            assertTrue(!info?.issuerName.isNullOrEmpty(), "발행사/운용사명이 있어야 합니다")
        }
    }

    @Nested
    @DisplayName("활용 예제")
    inner class UsageExamples {

        @Test
        @DisplayName("ETF의 발행사 정보를 비교할 수 있다")
        fun `compare issuer information`() = integrationTest {
            // Given
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

            assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
            assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

            // Then
            println("\n=== ETF 발행사 정보 비교 (거래일: $tradeDate) ===")
            println("TIGER 200")
            println("  - 발행사/운용사: ${tiger200Info?.issuerName}")
            println("  - 순자산총액: ${tiger200Info?.netAssetTotal}원")
            println("  - 상장주식수: ${tiger200Info?.listedShares}주")

            println("\nKODEX 200")
            println("  - 발행사/운용사: ${kodex200Info?.issuerName}")
            println("  - 순자산총액: ${kodex200Info?.netAssetTotal}원")
            println("  - 상장주식수: ${kodex200Info?.listedShares}주")

            if (tiger200Info?.issuerName == kodex200Info?.issuerName) {
                println("\n분석: 두 ETF는 동일한 발행사에서 운용됩니다")
            } else {
                println("\n분석: 두 ETF는 서로 다른 발행사에서 운용됩니다")
            }
        }

        @Test
        @DisplayName("ETF의 자산 분류 정보를 분석할 수 있다")
        fun `analyze asset class information`() = integrationTest {
            // Given
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

            assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
            assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

            // Then
            println("\n=== ETF 자산 분류 정보 분석 ===")
            println("TIGER 200")
            println("  - 자산 분류: ${tiger200Info?.assetClassName}")
            println("  - 복제 방법: ${tiger200Info?.replicationMethodTypeCode}")
            println("  - 지수 산출 기관: ${tiger200Info?.indexProviderName}")

            println("\nKODEX 200")
            println("  - 자산 분류: ${kodex200Info?.assetClassName}")
            println("  - 복제 방법: ${kodex200Info?.replicationMethodTypeCode}")
            println("  - 지수 산출 기관: ${kodex200Info?.indexProviderName}")

            if (tiger200Info?.assetClassName == kodex200Info?.assetClassName) {
                println("\n분석: 두 ETF는 동일한 자산 분류(${tiger200Info?.assetClassName})를 추종합니다")
            } else {
                println("\n분석: 두 ETF는 서로 다른 자산 분류를 추종합니다")
            }
        }
    }
}
