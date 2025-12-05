package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.ResponseRecorder
import dev.kairoscode.kfc.common.TestFixtures
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * FundsApi.getGeneralInfo() Integration Test Specification
 *
 * ## API 개요
 * ETF의 기본정보(메타데이터)를 조회하는 API입니다.
 *
 * ## 엔드포인트
 * ```kotlin
 * fun getGeneralInfo(isin: String, tradeDate: LocalDate): GeneralInfo?
 * ```
 *
 * ## 파라미터
 * - `isin`: String - ETF의 ISIN 코드 (12자리, 예: "KR7069500007")
 * - `tradeDate`: LocalDate - 조회 기준일 (거래일 또는 비거래일 모두 가능)
 *
 * ## 응답 데이터 (GeneralInfo)
 * - `name`: String - ETF 명칭
 * - `isin`: String - ISIN 코드 (12자리)
 * - `assetClassName`: String - 자산 분류
 * - `issuerName`: String - 발행사/운용사명
 * - `listingDate`: LocalDate - 상장일
 * - `netAssetTotal`: BigDecimal - 순자산총액 (원)
 * - `listedShares`: Long - 상장주식수
 * - `replicationMethodTypeCode`: String? - 복제 방법 코드
 * - `indexProviderName`: String? - 지수 산출 기관
 *
 * ## 특이사항
 * - 비거래일에도 정적 메타데이터 제공 (netAssetTotal 등은 최근 거래일 기준)
 * - API Key 불필요 (KRX 공개 데이터)
 * - 존재하지 않는 ISIN 조회시 null 반환
 */
@DisplayName("FundsApi.getGeneralInfo() - ETF 기본 정보 조회")
class FundsApiGeneralInfoSpec : IntegrationTestBase() {

    // ========================================
    // 1. 기본 동작 (Basic Operations)
    // ========================================

    @Nested
    @DisplayName("1. 기본 동작 (Basic Operations)")
    inner class BasicOperations {

        @Test
        @DisplayName("TIGER 200의 기본 정보를 거래일에 조회할 수 있다")
        fun get_tiger200_general_info_on_trading_day() = integrationTest {
            // ========== API 문서 ==========
            println("\n📘 API: getGeneralInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: TIGER 200 ISIN and trading day
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Request general info
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then: Returns general info data
            assertNotNull(info, "거래일에는 TIGER 200의 기본정보가 반환되어야 합니다")

            println("\n📤 Response: GeneralInfo")
            println("  • name: ${info?.name}")
            println("  • isin: ${info?.isin}")
            println("  • assetClassName: ${info?.assetClassName}")
            println("  • issuerName: ${info?.issuerName}")
            println("  • listingDate: ${info?.listingDate}")
            println("  • netAssetTotal: ${info?.netAssetTotal}원")
            println("  • listedShares: ${info?.listedShares}주")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.GENERAL_INFO,
                fileName = "tiger200_general_info"
            )
        }

        @Test
        @DisplayName("KODEX 200의 기본 정보를 거래일에 조회할 수 있다")
        fun get_kodex200_general_info_on_trading_day() = integrationTest {
            println("\n📘 API: getGeneralInfo()")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: KODEX 200 ISIN and trading day
            val isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input Parameters:")
            println("  • isin: String = \"$isin\"")
            println("  • tradeDate: LocalDate = $tradeDate")

            // When: Request general info
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then: Returns general info data
            assertNotNull(info, "거래일에는 KODEX 200의 기본정보가 반환되어야 합니다")

            println("\n📤 Response: GeneralInfo")
            println("  • name: ${info?.name}")
            println("  • assetClassName: ${info?.assetClassName}")

            println("\n✅ 테스트 결과: 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            // 응답 레코딩
            ResponseRecorder.record(
                data = info,
                category = RecordingConfig.Paths.EtfMetrics.GENERAL_INFO,
                fileName = "kodex200_general_info"
            )
        }

        @Test
        @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 ETF를 조회할 수 있다")
        fun get_different_etfs_by_different_isin() = integrationTest {
            println("\n📘 파라미터 테스트: isin")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Two different ISINs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Request with different ISINs
            val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

            // Then: Returns different ETF information
            assertNotNull(tiger200Info)
            assertNotNull(kodex200Info)
            assertNotEquals(tiger200Info?.name, kodex200Info?.name, "서로 다른 ISIN은 서로 다른 ETF를 반환해야 합니다")

            println("  Case 1: isin = \"$tiger200Isin\"")
            println("    → Result name: ${tiger200Info?.name}")
            println()
            println("  Case 2: isin = \"$kodex200Isin\"")
            println("    → Result name: ${kodex200Info?.name}")
            println()
            println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 ETF 조회 성공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 2. 응답 데이터 검증 (Response Validation)
    // ========================================

    @Nested
    @DisplayName("2. 응답 데이터 검증 (Response Validation)")
    inner class ResponseValidation {

        @Test
        @DisplayName("응답은 필수 필드(name, isin, assetClassName, issuerName)를 포함한다")
        fun response_contains_all_required_fields() = integrationTest {
            println("\n📘 응답 데이터 검증: 필수 필드")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then: Validate required fields
            assertNotNull(info, "응답 데이터가 있어야 합니다")
            assertTrue(info?.name?.isNotEmpty() == true, "name 필드는 비어있지 않아야 합니다")
            assertTrue(info?.isin?.isNotEmpty() == true, "isin 필드는 비어있지 않아야 합니다")
            assertTrue(info?.assetClassName?.isNotEmpty() == true, "assetClassName 필드는 비어있지 않아야 합니다")
            assertTrue(info?.issuerName?.isNotEmpty() == true, "issuerName 필드는 비어있지 않아야 합니다")

            println("✅ 필수 필드 검증 통과:")
            println("  • name: \"${info?.name}\" ✓")
            println("  • isin: \"${info?.isin}\" ✓")
            println("  • assetClassName: \"${info?.assetClassName}\" ✓")
            println("  • issuerName: \"${info?.issuerName}\" ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("ISIN 코드는 12자리 형식이며 'KR'로 시작한다")
        fun isin_format_is_12_characters_starting_with_kr() = integrationTest {
            println("\n📘 응답 데이터 검증: ISIN 형식")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "응답 데이터가 있어야 합니다")
            assertEquals(12, info?.isin?.length, "ISIN 코드는 12자리여야 합니다")
            assertTrue(info?.isin?.startsWith("KR") == true, "한국 ETF ISIN은 'KR'로 시작해야 합니다")

            println("✅ ISIN 형식 검증:")
            println("  • ISIN: ${info?.isin}")
            println("  • Length: ${info?.isin?.length} (Expected: 12) ✓")
            println("  • Prefix: ${info?.isin?.take(2)} (Expected: KR) ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("순자산총액(netAssetTotal)은 0 이상이다")
        fun net_asset_total_is_non_negative() = integrationTest {
            println("\n📘 응답 데이터 검증: 순자산총액 범위")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertNotNull(info, "응답 데이터가 있어야 합니다")
            assertTrue(info?.netAssetTotal?.toLong() ?: 0 >= 0, "순자산총액은 0 이상이어야 합니다")

            println("✅ 순자산총액 검증:")
            println("  • netAssetTotal: ${info?.netAssetTotal}원")
            println("  • Range: >= 0 ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("상장주식수(listedShares)는 양수다")
        fun listed_shares_is_positive() = integrationTest {
            println("\n📘 응답 데이터 검증: 상장주식수")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then
            assertNotNull(info)
            assertTrue(info?.listedShares ?: 0 > 0, "상장주식수는 양수여야 합니다")

            println("✅ 상장주식수 검증:")
            println("  • listedShares: ${info?.listedShares}주")
            println("  • Range: > 0 ✓")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 3. 입력 파라미터 검증 (Input Validation)
    // ========================================

    @Nested
    @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
    inner class InputValidation {

        @Test
        @DisplayName("존재하지 않는 ISIN 조회시 빈 GeneralInfo 객체를 반환한다")
        fun returns_empty_general_info_for_non_existent_isin() = integrationTest {
            println("\n📘 입력 검증: 존재하지 않는 ISIN")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Invalid ISIN that doesn't exist
            val invalidIsin = "KR7999999999"
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
            println("  • tradeDate: $tradeDate")

            // When
            val info = client.funds.getGeneralInfo(invalidIsin, tradeDate)

            // Then: Returns empty GeneralInfo for non-existent ISIN
            assertNotNull(info, "API는 빈 객체를 반환합니다")
            assertTrue(info!!.name.isEmpty(), "name 필드는 빈 문자열이어야 합니다")
            assertEquals(java.math.BigDecimal.ZERO, info.netAssetTotal, "netAssetTotal은 0이어야 합니다")
            assertEquals(0, info.listedShares, "listedShares는 0이어야 합니다")

            println("\n📤 Response: GeneralInfo (empty)")
            println("  • name: \"${info.name}\" (empty)")
            println("  • netAssetTotal: ${info.netAssetTotal} (0)")
            println("  • listedShares: ${info.listedShares} (0)")
            println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 GeneralInfo 객체 반환")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 4. 엣지 케이스 (Edge Cases)
    // ========================================

    @Nested
    @DisplayName("4. 엣지 케이스 (Edge Cases)")
    inner class EdgeCases {

        @Test
        @DisplayName("[파라미터: tradeDate] 비거래일에 조회하면 정적 메타데이터를 반환한다")
        fun returns_static_metadata_on_non_trading_day() = integrationTest {
            println("\n📘 엣지 케이스: 비거래일 조회")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Non-trading day (Saturday)
            val isin = TestFixtures.Etf.TIGER_200_ISIN
            val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

            println("📥 Input:")
            println("  • isin: \"$isin\"")
            println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

            // When
            val info = client.funds.getGeneralInfo(isin, tradeDate)

            // Then: Returns static metadata even on non-trading days
            assertNotNull(info, "비거래일에도 정적 메타데이터를 반환해야 합니다")

            println("\n📤 Response: GeneralInfo (정적 메타데이터)")
            println("  • name: ${info?.name}")
            println("  • issuerName: ${info?.issuerName}")
            println("  • listingDate: ${info?.listingDate}")
            println()
            println("  ℹ️ 참고: netAssetTotal 등 일부 필드는 최근 거래일 기준")

            println("\n✅ 처리 결과: 비거래일에도 데이터 제공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[파라미터: tradeDate] 거래일과 비거래일 데이터 비교")
        fun compare_trading_day_vs_non_trading_day() = integrationTest {
            println("\n📘 파라미터 비교: 거래일 vs 비거래일")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            val isin = TestFixtures.Etf.TIGER_200_ISIN

            // When: Query on both trading and non-trading days
            println("  Case 1: 거래일 (${TestFixtures.Dates.TRADING_DAY}, 월요일)")
            val tradingDayResult = client.funds.getGeneralInfo(isin, TestFixtures.Dates.TRADING_DAY)
            println("    → name: ${tradingDayResult?.name}")
            println("    → netAssetTotal: ${tradingDayResult?.netAssetTotal}")

            println("\n  Case 2: 비거래일 (${TestFixtures.Dates.NON_TRADING_DAY}, 토요일)")
            val nonTradingDayResult = client.funds.getGeneralInfo(isin, TestFixtures.Dates.NON_TRADING_DAY)
            println("    → name: ${nonTradingDayResult?.name}")
            println("    → netAssetTotal: ${nonTradingDayResult?.netAssetTotal}")

            // Then: Both should return data
            assertNotNull(tradingDayResult)
            assertNotNull(nonTradingDayResult)
            assertEquals(tradingDayResult?.name, nonTradingDayResult?.name, "ETF 명칭은 동일해야 합니다")

            println("\n  ✅ 분석: 비거래일에도 정적 메타데이터 제공")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    // ========================================
    // 5. 활용 예제 (Usage Examples)
    // ========================================

    @Nested
    @DisplayName("5. 활용 예제 (Usage Examples)")
    inner class UsageExamples {

        @Test
        @DisplayName("[활용] 여러 ETF의 발행사 정보를 비교할 수 있다")
        fun compare_issuer_information_across_etfs() = integrationTest {
            println("\n📘 활용 예제: ETF 발행사 비교")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given: Multiple ETF ISINs
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When: Fetch general info for both
            val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

            assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
            assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

            // Then: Compare issuer information
            println("\n=== ETF 발행사 정보 비교 ===")
            println("Trade Date: $tradeDate")
            println()
            println("TIGER 200 (${tiger200Info?.isin})")
            println("  • Name: ${tiger200Info?.name}")
            println("  • Issuer: ${tiger200Info?.issuerName}")
            println("  • Net Asset Total: ${tiger200Info?.netAssetTotal}원")
            println("  • Listed Shares: ${tiger200Info?.listedShares}주")
            println()
            println("KODEX 200 (${kodex200Info?.isin})")
            println("  • Name: ${kodex200Info?.name}")
            println("  • Issuer: ${kodex200Info?.issuerName}")
            println("  • Net Asset Total: ${kodex200Info?.netAssetTotal}원")
            println("  • Listed Shares: ${kodex200Info?.listedShares}주")
            println()

            if (tiger200Info?.issuerName == kodex200Info?.issuerName) {
                println("📊 분석: 두 ETF는 동일한 발행사(${tiger200Info?.issuerName})에서 운용됩니다")
            } else {
                println("📊 분석: 두 ETF는 서로 다른 발행사에서 운용됩니다")
                println("  • TIGER 200: ${tiger200Info?.issuerName}")
                println("  • KODEX 200: ${kodex200Info?.issuerName}")
            }
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] ETF의 자산 분류 정보를 분석할 수 있다")
        fun analyze_asset_class_information() = integrationTest {
            println("\n📘 활용 예제: ETF 자산 분류 분석")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

            assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
            assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

            // Then: Analyze asset classification
            println("\n=== ETF 자산 분류 정보 분석 ===")
            println()
            println("TIGER 200")
            println("  • Asset Class: ${tiger200Info?.assetClassName}")
            println("  • Replication Method: ${tiger200Info?.replicationMethodTypeCode ?: "N/A"}")
            println("  • Index Provider: ${tiger200Info?.indexProviderName ?: "N/A"}")
            println()
            println("KODEX 200")
            println("  • Asset Class: ${kodex200Info?.assetClassName}")
            println("  • Replication Method: ${kodex200Info?.replicationMethodTypeCode ?: "N/A"}")
            println("  • Index Provider: ${kodex200Info?.indexProviderName ?: "N/A"}")
            println()

            if (tiger200Info?.assetClassName == kodex200Info?.assetClassName) {
                println("📊 분석: 두 ETF는 동일한 자산 분류(${tiger200Info?.assetClassName})를 추종합니다")
            } else {
                println("📊 분석: 두 ETF는 서로 다른 자산 분류를 추종합니다")
            }
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        @Test
        @DisplayName("[활용] ETF 규모 비교를 통해 시장 점유율을 파악할 수 있다")
        fun compare_etf_market_share_by_net_assets() = integrationTest {
            println("\n📘 활용 예제: ETF 시장 점유율 분석")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Given
            val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
            val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
            val tradeDate = TestFixtures.Dates.TRADING_DAY

            // When
            val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
            val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

            assertNotNull(tiger200Info)
            assertNotNull(kodex200Info)

            // Then: Calculate market share
            val tiger200Assets = tiger200Info?.netAssetTotal?.toLong() ?: 0
            val kodex200Assets = kodex200Info?.netAssetTotal?.toLong() ?: 0
            val totalAssets = tiger200Assets + kodex200Assets
            val tiger200Share = if (totalAssets > 0) (tiger200Assets * 100.0 / totalAssets) else 0.0
            val kodex200Share = if (totalAssets > 0) (kodex200Assets * 100.0 / totalAssets) else 0.0

            println("\n=== KOSPI 200 추종 ETF 규모 비교 ===")
            println()
            println("Total Net Assets: ${totalAssets}원")
            println()
            println("TIGER 200")
            println("  • Net Assets: ${tiger200Assets}원")
            println("  • Market Share: ${"%.2f".format(tiger200Share)}%")
            println()
            println("KODEX 200")
            println("  • Net Assets: ${kodex200Assets}원")
            println("  • Market Share: ${"%.2f".format(kodex200Share)}%")
            println()
            println("📊 분석: 순자산총액 기준 KOSPI 200 추종 ETF 시장 점유율")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }
}
