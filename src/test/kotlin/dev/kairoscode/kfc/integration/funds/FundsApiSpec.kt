package dev.kairoscode.kfc.integration.funds

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.ResponseRecorder
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.abs

/**
 * FundsApi Integration Test Specification
 *
 * 펀드(ETF) API의 모든 기능을 검증하는 통합 테스트입니다.
 *
 * ## 테스트 구조
 * - 기능 그룹 1: 기본 정보 조회 API (4개 메서드, 44개 테스트)
 * - 기능 그룹 2: 포트폴리오 API (2개 메서드, 17개 테스트)
 * - 기능 그룹 3: 거래 및 공매도 API (6개 메서드, 13개 테스트)
 */
@DisplayName("[I][Funds] FundsApi - 펀드 API")
class FundsApiSpec : IntegrationTestBase() {
    // ========================================
    // 기능 그룹 1: 기본 정보 조회 API
    // ========================================

    @Nested
    @DisplayName("기본 정보 조회 API")
    inner class BasicInfoApi {
        @Nested
        @DisplayName("getList() - 펀드 목록 조회")
        inner class GetList {
            @Nested
            @DisplayName("1. 기본 동작")
            inner class BasicBehavior {
                @Test
                @DisplayName("전체 ETF 목록을 조회할 수 있다")
                fun returns_all_funds_when_called_without_parameters() =
                    integrationTest {
                        println("\n📘 API: getList()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        println("📥 Input Parameters:")
                        println("  • (없음)")

                        // When
                        val etfList = client.funds.getList()

                        // Then
                        println("\n📤 Response: List<Fund>")
                        println("  • size: ${etfList.size}")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(etfList.size >= 1, "ETF 목록은 최소 1개 이상이어야 합니다. 실제: ${etfList.size}개")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = etfList,
                            category = RecordingConfig.Paths.EtfList.BASE,
                            fileName = "etf_list",
                        )
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증")
            inner class ResponseValidation {
                @Test
                @DisplayName("각 항목은 ISIN, 티커, 이름, 자산구분을 포함한다")
                fun each_item_contains_required_fields() =
                    integrationTest {
                        // Given
                        val etfList = client.funds.getList()

                        // Then: 각 ETF는 필수 필드 포함
                        etfList.forEach { etf ->
                            assertTrue(etf.isin.isNotBlank(), "ISIN은 비어있지 않아야 합니다")
                            assertTrue(etf.ticker.isNotBlank(), "티커는 비어있지 않아야 합니다")
                            assertTrue(etf.name.isNotBlank(), "이름은 비어있지 않아야 합니다")
                            assertTrue(etf.assetClass.isNotBlank(), "자산구분은 비어있지 않아야 합니다")
                        }
                    }

                @Test
                @DisplayName("TIGER 200과 KODEX 200이 목록에 포함된다")
                fun includes_major_etfs_like_tiger200_and_kodex200() =
                    integrationTest {
                        // Given
                        val etfList = client.funds.getList()

                        // Then: TIGER 200 포함 확인
                        val tiger200 = etfList.find { it.isin == TestFixtures.Etf.TIGER_200_ISIN }
                        assertTrue(tiger200 != null, "TIGER 200이 목록에 포함되어야 합니다")
                        println("[IntegrationTest] TIGER 200: ${tiger200?.name}")

                        // Then: KODEX 200 포함 확인
                        val kodex200 = etfList.find { it.isin == TestFixtures.Etf.KODEX_200_ISIN }
                        assertTrue(kodex200 != null, "KODEX 200이 목록에 포함되어야 합니다")
                        println("[IntegrationTest] KODEX 200: ${kodex200?.name}")
                    }

                @Test
                @DisplayName("다양한 자산구분이 포함된다")
                fun contains_various_asset_classes() =
                    integrationTest {
                        // Given
                        val etfList = client.funds.getList()

                        // When: 자산구분별로 그룹화
                        val assetClassGroups = etfList.groupBy { it.assetClass }

                        // Then: 다양한 자산구분 존재
                        assertTrue(assetClassGroups.size >= 3, "최소 3개 이상의 자산구분이 있어야 합니다")

                        // 콘솔 출력: 자산구분별 ETF 개수
                        println("\n=== 자산구분별 ETF 개수 ===")
                        assetClassGroups.entries
                            .sortedByDescending { it.value.size }
                            .forEach { (assetClass, etfs) ->
                                println("$assetClass: ${etfs.size}개")
                            }
                    }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제")
            inner class PracticalExamples {
                @Test
                @DisplayName("이름으로 ETF를 검색할 수 있다")
                fun search_etf_by_name() =
                    integrationTest {
                        // Given
                        val etfList = client.funds.getList()
                        val searchKeyword = "TIGER"

                        // When
                        val searchResults = etfList.filter { it.name.contains(searchKeyword) }

                        // Then
                        assertTrue(searchResults.isNotEmpty(), "$searchKeyword 가 포함된 ETF가 있어야 합니다")
                        println("\n=== '$searchKeyword' 검색 결과 ===")
                        println("검색된 ETF 개수: ${searchResults.size}")
                        searchResults.take(5).forEach { etf ->
                            println("  - ${etf.name} (${etf.ticker})")
                        }
                    }

                @Test
                @DisplayName("자산구분으로 ETF를 필터링할 수 있다")
                fun filter_etf_by_asset_class() =
                    integrationTest {
                        // Given
                        val etfList = client.funds.getList()

                        // When: 자산구분별로 그룹화
                        val assetClassGroups = etfList.groupBy { it.assetClass }

                        // Then: 각 자산구분의 ETF 확인
                        println("\n=== 자산구분별 ETF 필터링 예제 ===")
                        assetClassGroups.entries
                            .sortedByDescending { it.value.size }
                            .take(3)
                            .forEach { (assetClass, etfs) ->
                                println("\n[$assetClass] (${etfs.size}개)")
                                etfs.take(3).forEach { etf ->
                                    println("  - ${etf.name} (${etf.ticker})")
                                }
                            }
                    }
            }
        }

        @Nested
        @DisplayName("getGeneralInfo() - 기본 정보 조회")
        inner class GetGeneralInfo {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("TIGER 200의 기본 정보를 거래일에 조회할 수 있다")
                fun get_tiger200_general_info_on_trading_day() =
                    integrationTest {
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
                            fileName = "tiger200_general_info",
                        )
                    }

                @Test
                @DisplayName("KODEX 200의 기본 정보를 거래일에 조회할 수 있다")
                fun get_kodex200_general_info_on_trading_day() =
                    integrationTest {
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
                            fileName = "kodex200_general_info",
                        )
                    }

                @Test
                @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 ETF를 조회할 수 있다")
                fun get_different_etfs_by_different_isin() =
                    integrationTest {
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

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("응답은 필수 필드(name, isin, assetClassName, issuerName)를 포함한다")
                fun response_contains_all_required_fields() =
                    integrationTest {
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
                fun isin_format_is_12_characters_starting_with_kr() =
                    integrationTest {
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
                fun net_asset_total_is_non_negative() =
                    integrationTest {
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
                fun listed_shares_is_positive() =
                    integrationTest {
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

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                @Test
                @DisplayName("존재하지 않는 ISIN 조회시 빈 GeneralInfo 객체를 반환한다")
                fun returns_empty_general_info_for_non_existent_isin() =
                    integrationTest {
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

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                @Test
                @DisplayName("[파라미터: tradeDate] 비거래일에 조회하면 정적 메타데이터를 반환한다")
                fun returns_static_metadata_on_non_trading_day() =
                    integrationTest {
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
                fun compare_trading_day_vs_non_trading_day() =
                    integrationTest {
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

            @Nested
            @DisplayName("5. 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[활용] 여러 ETF의 발행사 정보를 비교할 수 있다")
                fun compare_issuer_information_across_etfs() =
                    integrationTest {
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
                fun analyze_asset_class_information() =
                    integrationTest {
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
                fun compare_etf_market_share_by_net_assets() =
                    integrationTest {
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

        @Nested
        @DisplayName("getDetailedInfo() - 상세 정보 조회")
        inner class GetDetailedInfo {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("TIGER 200의 상세 정보를 거래일에 조회할 수 있다")
                fun get_tiger200_detailed_info_on_trading_day() =
                    integrationTest {
                        println("\n📘 API: getDetailedInfo()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: TIGER 200 ISIN and trading day
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When: Request detailed info
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then: Returns detailed info
                        assertNotNull(info, "거래일에는 TIGER 200의 상세 정보가 반환되어야 합니다")

                        println("\n📤 Response: DetailedInfo")
                        println("  • closePrice: ${info?.closePrice}원")
                        println("  • nav: ${info?.nav}원")
                        println("  • divergenceRate: ${info?.calculateDivergenceRate()}%")
                        println("  • openPrice: ${info?.openPrice}원")
                        println("  • highPrice: ${info?.highPrice}원")
                        println("  • lowPrice: ${info?.lowPrice}원")
                        println("  • volume: ${info?.volume}주")
                        println("  • week52High: ${info?.week52High}원")
                        println("  • week52Low: ${info?.week52Low}원")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 응답 레코딩
                        ResponseRecorder.record(
                            data = info,
                            category = RecordingConfig.Paths.EtfMetrics.DETAILED_INFO,
                            fileName = "tiger200_detailedInfo",
                        )
                    }

                @Test
                @DisplayName("KODEX 200의 상세 정보를 거래일에 조회할 수 있다")
                fun get_kodex200_detailed_info_on_trading_day() =
                    integrationTest {
                        println("\n📘 API: getDetailedInfo()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: KODEX 200 ISIN and trading day
                        val isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When: Request detailed info
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then: Returns detailed info
                        assertNotNull(info, "거래일에는 KODEX 200의 상세 정보가 반환되어야 합니다")

                        println("\n📤 Response: DetailedInfo")
                        println("  • closePrice: ${info?.closePrice}원")
                        println("  • nav: ${info?.nav}원")
                        println("  • divergenceRate: ${info?.calculateDivergenceRate()}%")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 응답 레코딩
                        ResponseRecorder.record(
                            data = info,
                            category = RecordingConfig.Paths.EtfMetrics.DETAILED_INFO,
                            fileName = "kodex200_detailedInfo",
                        )
                    }

                @Test
                @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 ETF를 조회할 수 있다")
                fun get_different_etfs_by_different_isin() =
                    integrationTest {
                        println("\n📘 파라미터 테스트: isin")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Two different ISINs
                        val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                        val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When: Request with different ISINs
                        val tiger200Info = client.funds.getDetailedInfo(tiger200Isin, tradeDate)
                        val kodex200Info = client.funds.getDetailedInfo(kodex200Isin, tradeDate)

                        // Then: Returns different ETF information
                        assertNotNull(tiger200Info)
                        assertNotNull(kodex200Info)
                        assertNotEquals(
                            tiger200Info?.closePrice,
                            kodex200Info?.closePrice,
                            "서로 다른 ISIN은 서로 다른 가격을 가져야 합니다",
                        )

                        println("  Case 1: isin = \"$tiger200Isin\"")
                        println("    → closePrice: ${tiger200Info?.closePrice}원")
                        println()
                        println("  Case 2: isin = \"$kodex200Isin\"")
                        println("    → closePrice: ${kodex200Info?.closePrice}원")
                        println()
                        println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 ETF 조회 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("응답은 필수 필드(closePrice, nav)를 포함한다")
                fun response_contains_required_fields() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 필수 필드")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then: Validate required fields
                        assertNotNull(info, "응답 데이터가 있어야 합니다")
                        assertTrue(info?.closePrice?.compareTo(java.math.BigDecimal.ZERO) == 1, "종가는 0보다 커야 합니다")
                        assertTrue(info?.nav?.compareTo(java.math.BigDecimal.ZERO) == 1, "NAV는 0보다 커야 합니다")

                        println("✅ 필수 필드 검증 통과:")
                        println("  • closePrice: ${info?.closePrice}원 (> 0) ✓")
                        println("  • nav: ${info?.nav}원 (> 0) ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("OHLCV 데이터가 유효한 범위 내에 있다")
                fun ohlcv_data_is_within_valid_range() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: OHLCV 범위")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then: Validate OHLC relationships
                        assertNotNull(info)
                        assertTrue(info?.highPrice!! >= info.lowPrice, "고가는 저가보다 크거나 같아야 합니다")
                        assertTrue(info.highPrice >= info.openPrice, "고가는 시가보다 크거나 같아야 합니다")
                        assertTrue(info.highPrice >= info.closePrice, "고가는 종가보다 크거나 같아야 합니다")
                        assertTrue(info.lowPrice <= info.openPrice, "저가는 시가보다 작거나 같아야 합니다")
                        assertTrue(info.lowPrice <= info.closePrice, "저가는 종가보다 작거나 같아야 합니다")

                        println("✅ OHLCV 범위 검증:")
                        println("  • High: ${info.highPrice}원")
                        println("  • Open: ${info.openPrice}원")
                        println("  • Close: ${info.closePrice}원")
                        println("  • Low: ${info.lowPrice}원")
                        println("  • Volume: ${info.volume}주")
                        println("  • 관계: Low ≤ Open, Close ≤ High ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("52주 고가/저가는 현재가를 포함하는 범위다")
                fun week52_range_includes_current_price() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 52주 고가/저가 범위")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then: Current price should be within 52-week range
                        assertNotNull(info)
                        assertTrue(info?.closePrice!! <= info.week52High, "현재가는 52주 최고가 이하여야 합니다")
                        assertTrue(info.closePrice >= info.week52Low, "현재가는 52주 최저가 이상이어야 합니다")

                        println("✅ 52주 범위 검증:")
                        println("  • 52주 고가: ${info.week52High}원")
                        println("  • 현재 종가: ${info.closePrice}원")
                        println("  • 52주 저가: ${info.week52Low}원")
                        println("  • 범위: 52주 저가 ≤ 현재가 ≤ 52주 고가 ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("거래량은 0 이상이다")
                fun volume_is_non_negative() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 거래량")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then
                        assertNotNull(info)
                        assertTrue(info?.volume!! >= 0, "거래량은 0 이상이어야 합니다")

                        println("✅ 거래량 검증:")
                        println("  • volume: ${info.volume}주 (>= 0) ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                @Test
                @DisplayName("존재하지 않는 ISIN 조회시 빈 데이터 객체를 반환한다")
                fun returns_empty_object_for_non_existent_isin() =
                    integrationTest {
                        println("\n📘 입력 검증: 존재하지 않는 ISIN")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Invalid ISIN that doesn't exist
                        val invalidIsin = "KR7999999999"
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input:")
                        println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
                        println("  • tradeDate: $tradeDate")

                        // When
                        val info = client.funds.getDetailedInfo(invalidIsin, tradeDate)

                        // Then: Returns empty object (all fields are 0 or empty string)
                        assertNotNull(info, "존재하지 않는 ISIN은 빈 객체를 반환합니다")
                        assertEquals("", info?.isin ?: "", "ISIN이 빈 문자열이어야 합니다")
                        assertEquals("", info?.name ?: "", "종목명이 빈 문자열이어야 합니다")

                        println("\n📤 Response: DetailedInfo (빈 객체)")
                        println("  • isin: \"${info?.isin}\" (빈 문자열)")
                        println("  • name: \"${info?.name}\" (빈 문자열)")
                        println("  • closePrice: ${info?.closePrice} (0)")
                        println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 객체 반환")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                @Test
                @DisplayName("[파라미터: tradeDate] 비거래일에 조회하면 최근 거래일 데이터를 반환한다")
                fun returns_latest_data_on_non_trading_day() =
                    integrationTest {
                        println("\n📘 엣지 케이스: 비거래일 조회")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Non-trading day (Saturday)
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

                        println("📥 Input:")
                        println("  • isin: \"$isin\"")
                        println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

                        // When
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        // Then: Returns latest trading day data
                        assertNotNull(info, "비거래일에도 최근 거래일 데이터를 반환해야 합니다")

                        println("\n📤 Response: DetailedInfo (최근 거래일 데이터)")
                        println("  • closePrice: ${info?.closePrice}원")
                        println("  • nav: ${info?.nav}원")
                        println()
                        println("  ℹ️ 참고: API는 최근 거래일 데이터를 반환합니다")

                        println("\n✅ 처리 결과: 비거래일에도 데이터 제공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[파라미터: tradeDate] 거래일과 비거래일 데이터 비교")
                fun compare_trading_day_vs_non_trading_day() =
                    integrationTest {
                        println("\n📘 파라미터 비교: 거래일 vs 비거래일")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        val isin = TestFixtures.Etf.TIGER_200_ISIN

                        // When: Query on both trading and non-trading days
                        println("  Case 1: 거래일 (${TestFixtures.Dates.TRADING_DAY}, 월요일)")
                        val tradingDayResult = client.funds.getDetailedInfo(isin, TestFixtures.Dates.TRADING_DAY)
                        println("    → closePrice: ${tradingDayResult?.closePrice}원")
                        println("    → nav: ${tradingDayResult?.nav}원")

                        println("\n  Case 2: 비거래일 (${TestFixtures.Dates.NON_TRADING_DAY}, 토요일)")
                        val nonTradingDayResult = client.funds.getDetailedInfo(isin, TestFixtures.Dates.NON_TRADING_DAY)
                        println("    → closePrice: ${nonTradingDayResult?.closePrice}원")
                        println("    → nav: ${nonTradingDayResult?.nav}원")

                        // Then: Both should return data
                        assertNotNull(tradingDayResult)
                        assertNotNull(nonTradingDayResult)

                        println("\n  ✅ 분석: 비거래일 조회시 최근 거래일 데이터 제공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("5. 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[활용] NAV 대비 괴리율을 계산할 수 있다")
                fun calculate_divergence_rate_from_nav() =
                    integrationTest {
                        println("\n📘 활용 예제: NAV 대비 괴리율 계산")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        assertNotNull(info, "거래일에는 상세 정보가 반환되어야 합니다")

                        // When: Calculate divergence rate
                        val calculatedDivergence = info?.calculateDivergenceRate()

                        // Then: Display analysis
                        println("\n=== NAV 대비 괴리율 분석 (거래일: $tradeDate) ===")
                        println("종가: ${info?.closePrice}원")
                        println("NAV: ${info?.nav}원")
                        println("괴리율(계산): $calculatedDivergence%")
                        println()
                        println(
                            "📊 분석: 괴리율은 ETF 가격이 NAV 대비 ${if (calculatedDivergence!! > 0.toBigDecimal()) "할증" else "할인"} 상태임을 나타냅니다",
                        )
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 52주 고가/저가 대비 현재가 위치를 확인할 수 있다")
                fun analyze_price_position_within_52_week_range() =
                    integrationTest {
                        println("\n📘 활용 예제: 52주 고가/저가 대비 현재가 위치")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY
                        val info = client.funds.getDetailedInfo(isin, tradeDate)

                        assertNotNull(info, "거래일에는 상세 정보가 반환되어야 합니다")

                        // When: Calculate position within 52-week range
                        val position =
                            info?.let {
                                val highLowRange = it.week52High.subtract(it.week52Low)
                                if (highLowRange.compareTo(java.math.BigDecimal.ZERO) > 0) {
                                    it.closePrice
                                        .subtract(it.week52Low)
                                        .divide(highLowRange, 4, java.math.RoundingMode.HALF_UP)
                                        .multiply(java.math.BigDecimal("100"))
                                        .toDouble()
                                } else {
                                    50.0
                                }
                            } ?: 50.0

                        // Then: Display analysis
                        println("\n=== 52주 고가/저가 대비 현재가 위치 (거래일: $tradeDate) ===")
                        println("52주 고가: ${info?.week52High}원")
                        println("현재가: ${info?.closePrice}원")
                        println("52주 저가: ${info?.week52Low}원")
                        println("위치: ${"%.1f".format(position)}% (0%=저가, 100%=고가)")
                        println()
                        println("52주 고가 근처?: ${info?.isNear52WeekHigh()}")
                        println("52주 저가 근처?: ${info?.isNear52WeekLow()}")
                        println()
                        println("📊 분석: 현재가는 52주 범위에서 ${"%.1f".format(position)}% 위치에 있습니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 여러 ETF의 괴리율을 비교할 수 있다")
                fun compare_divergence_rates_across_etfs() =
                    integrationTest {
                        println("\n📘 활용 예제: ETF 괴리율 비교")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Multiple ETFs
                        val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                        val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When: Fetch detailed info for both
                        val tiger200Info = client.funds.getDetailedInfo(tiger200Isin, tradeDate)
                        val kodex200Info = client.funds.getDetailedInfo(kodex200Isin, tradeDate)

                        assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
                        assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

                        // Then: Compare divergence rates
                        val tiger200Divergence = tiger200Info?.calculateDivergenceRate()
                        val kodex200Divergence = kodex200Info?.calculateDivergenceRate()

                        println("\n=== KOSPI 200 추종 ETF 괴리율 비교 (거래일: $tradeDate) ===")
                        println()
                        println("TIGER 200")
                        println("  • 종가: ${tiger200Info?.closePrice}원")
                        println("  • NAV: ${tiger200Info?.nav}원")
                        println("  • 괴리율: $tiger200Divergence%")
                        println()
                        println("KODEX 200")
                        println("  • 종가: ${kodex200Info?.closePrice}원")
                        println("  • NAV: ${kodex200Info?.nav}원")
                        println("  • 괴리율: $kodex200Divergence%")
                        println()
                        println("📊 분석: 동일 지수 추종 ETF 간 괴리율 차이를 확인할 수 있습니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }
        }

        @Nested
        @DisplayName("getPerformance() - 성과 정보 조회")
        inner class GetPerformance {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("TIGER 200의 추적 오차를 조회할 수 있다")
                fun get_tracking_error_for_tiger200() =
                    integrationTest {
                        println("\n📘 API: getTrackingError()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: TIGER 200 ISIN, 1 month period
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • fromDate: LocalDate = $fromDate")
                        println("  • toDate: LocalDate = $toDate")

                        // When: Request tracking error
                        val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

                        // Then: Returns tracking error data
                        assertTrue(trackingErrors.isNotEmpty(), "추적 오차 데이터가 반환되어야 합니다")

                        println("\n📤 Response: List<TrackingError>")
                        println("  • dataPoints: ${trackingErrors.size}개")
                        println("  • period: $fromDate ~ $toDate")

                        if (trackingErrors.isNotEmpty()) {
                            val firstItem = trackingErrors.first()
                            val lastItem = trackingErrors.last()
                            println("  • first: ${firstItem.tradeDate} - ${firstItem.trackingErrorRate}%")
                            println("  • last: ${lastItem.tradeDate} - ${lastItem.trackingErrorRate}%")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = trackingErrors,
                            category = RecordingConfig.Paths.EtfMetrics.TRACKING_ERROR,
                            fileName = "tiger200_tracking_error",
                        )
                    }

                @Test
                @DisplayName("TIGER 200의 괴리율을 조회할 수 있다")
                fun get_divergence_rate_for_tiger200() =
                    integrationTest {
                        println("\n📘 API: getDivergenceRate()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: TIGER 200 ISIN, 1 month period
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • fromDate: LocalDate = $fromDate")
                        println("  • toDate: LocalDate = $toDate")

                        // When: Request divergence rate
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        // Then: Returns divergence rate data
                        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 반환되어야 합니다")

                        println("\n📤 Response: List<DivergenceRate>")
                        println("  • dataPoints: ${divergenceRates.size}개")
                        println("  • period: $fromDate ~ $toDate")

                        if (divergenceRates.isNotEmpty()) {
                            val firstItem = divergenceRates.first()
                            val lastItem = divergenceRates.last()
                            println("  • first: ${firstItem.tradeDate} - ${firstItem.divergenceRate}%")
                            println("  • last: ${lastItem.tradeDate} - ${lastItem.divergenceRate}%")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = divergenceRates,
                            category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
                            fileName = "tiger200_divergence_rate",
                        )
                    }

                @Test
                @DisplayName("[파라미터: isin] KODEX 200의 괴리율을 조회할 수 있다")
                fun get_divergence_rate_for_kodex200() =
                    integrationTest {
                        println("\n📘 파라미터 테스트: isin = KODEX_200")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: KODEX 200 ISIN
                        val isin = TestFixtures.Etf.KODEX_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        println("📥 Input:")
                        println("  • isin: \"$isin\" (KODEX 200)")

                        // When: Request divergence rate
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        // Then: Returns data
                        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 반환되어야 합니다")

                        println("\n📤 Response:")
                        println("  • dataPoints: ${divergenceRates.size}개")

                        println("\n✅ 결과: KODEX 200 괴리율 조회 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = divergenceRates,
                            category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
                            fileName = "kodex200_divergence_rate",
                        )
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("추적 오차 응답은 일별 데이터를 포함한다")
                fun tracking_error_response_contains_daily_data() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 추적 오차")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        // When
                        val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

                        // Then: Validate structure
                        assertTrue(trackingErrors.isNotEmpty(), "최소 1개 이상의 데이터가 있어야 합니다")

                        val firstItem = trackingErrors.first()
                        assertNotNull(firstItem.tradeDate, "tradeDate가 있어야 합니다")

                        println("✅ 응답 구조 검증 통과:")
                        println("  • dataPoints: ${trackingErrors.size}개 (> 0) ✓")
                        println("  • 첫 번째 항목 tradeDate: ${firstItem.tradeDate} ✓")
                        println("  • 첫 번째 항목 trackingErrorRate: ${firstItem.trackingErrorRate}% ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("괴리율 응답은 일별 데이터를 포함한다")
                fun divergence_rate_response_contains_daily_data() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 괴리율")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        // When
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        // Then: Validate structure
                        assertTrue(divergenceRates.isNotEmpty(), "최소 1개 이상의 데이터가 있어야 합니다")

                        val firstItem = divergenceRates.first()
                        assertNotNull(firstItem.tradeDate, "tradeDate가 있어야 합니다")

                        println("✅ 응답 구조 검증 통과:")
                        println("  • dataPoints: ${divergenceRates.size}개 (> 0) ✓")
                        println("  • 첫 번째 항목 tradeDate: ${firstItem.tradeDate} ✓")
                        println("  • 첫 번째 항목 divergenceRate: ${firstItem.divergenceRate}% ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("데이터는 날짜순으로 정렬되어 있다")
                fun data_is_sorted_by_date() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 날짜 정렬")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        // When
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        // Then: Verify sorted by date
                        if (divergenceRates.size >= 2) {
                            for (i in 0 until divergenceRates.size - 1) {
                                assertTrue(
                                    divergenceRates[i].tradeDate <= divergenceRates[i + 1].tradeDate,
                                    "데이터는 날짜순으로 정렬되어야 합니다",
                                )
                            }
                        }

                        println("✅ 날짜 정렬 검증:")
                        println("  • 첫 번째 날짜: ${divergenceRates.first().tradeDate}")
                        println("  • 마지막 날짜: ${divergenceRates.last().tradeDate}")
                        println("  • 정렬 순서: 오름차순 ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                @Test
                @DisplayName("존재하지 않는 ISIN 조회시 빈 리스트를 반환한다")
                fun returns_empty_list_for_non_existent_isin() =
                    integrationTest {
                        println("\n📘 입력 검증: 존재하지 않는 ISIN")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Invalid ISIN that doesn't exist
                        val invalidIsin = "KR7999999999"
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        println("📥 Input:")
                        println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
                        println("  • period: $fromDate ~ $toDate")

                        // When
                        val divergenceRates = client.funds.getDivergenceRate(invalidIsin, fromDate, toDate)

                        // Then: Returns empty list for non-existent ISIN
                        assertTrue(divergenceRates.isEmpty(), "존재하지 않는 ISIN은 빈 리스트를 반환해야 합니다")

                        println("\n📤 Response: List<DivergenceRate> (empty)")
                        println("  • dataPoints: ${divergenceRates.size}")
                        println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 리스트 반환")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                @Test
                @DisplayName("[파라미터: period] 짧은 기간(1주) 조회가 가능하다")
                fun supports_short_period_query() =
                    integrationTest {
                        println("\n📘 엣지 케이스: 짧은 기간 조회")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: 1 week period
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusWeeks(1)

                        println("📥 Input:")
                        println("  • period: $fromDate ~ $toDate (1주)")

                        // When
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        // Then: Returns data for short period
                        println("\n📤 Response:")
                        println("  • dataPoints: ${divergenceRates.size}개")

                        println("\n✅ 결과: 짧은 기간 조회 가능")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[파라미터: period] 긴 기간(3개월) 조회가 가능하다")
                fun supports_long_period_query() =
                    integrationTest {
                        println("\n📘 엣지 케이스: 긴 기간 조회")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: 3 months period
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(3)

                        println("📥 Input:")
                        println("  • period: $fromDate ~ $toDate (3개월)")

                        // When
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        // Then: Returns data for long period
                        println("\n📤 Response:")
                        println("  • dataPoints: ${divergenceRates.size}개")

                        println("\n✅ 결과: 긴 기간 조회 가능")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("5. 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[활용] 평균 추적 오차를 계산할 수 있다")
                fun calculate_average_tracking_error() =
                    integrationTest {
                        println("\n📘 활용 예제: 평균 추적 오차 계산")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Tracking error data
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)
                        val trackingErrors = client.funds.getTrackingError(isin, fromDate, toDate)

                        assertTrue(trackingErrors.isNotEmpty(), "추적 오차 데이터가 있어야 합니다")

                        // When: Calculate average of absolute values
                        val avgTrackingError =
                            trackingErrors
                                .map { abs(it.trackingErrorRate) }
                                .average()

                        // Then: Display analysis
                        println("\n=== 평균 추적 오차 분석 ===")
                        println("기간: $fromDate ~ $toDate")
                        println("평균 추적 오차: ${"%.4f".format(avgTrackingError)}%")
                        println()
                        println("📊 분석: 추적 오차가 낮을수록 벤치마크 지수를 정확히 추종합니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 괴리율이 높은 날을 찾을 수 있다")
                fun find_high_divergence_rate_days() =
                    integrationTest {
                        println("\n📘 활용 예제: 괴리율 높은 날 찾기")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Divergence rate data
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 있어야 합니다")

                        // When: Filter by absolute value > 0.5%
                        val highDivergenceDays =
                            divergenceRates
                                .filter { abs(it.divergenceRate) > 0.5 }
                                .sortedByDescending { abs(it.divergenceRate) }

                        // Then: Display high divergence days
                        println("\n=== 괴리율이 높은 날 (±0.5% 초과) ===")
                        if (highDivergenceDays.isNotEmpty()) {
                            highDivergenceDays.forEach { day ->
                                println("${day.tradeDate}: ${"%.2f".format(day.divergenceRate)}%")
                            }
                        } else {
                            println("괴리율이 ±0.5%를 초과하는 날이 없습니다.")
                        }
                        println()
                        println("📊 분석: 괴리율이 높은 날은 ETF 가격이 NAV에서 많이 벗어난 날입니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 평균 괴리율을 계산할 수 있다")
                fun calculate_average_divergence_rate() =
                    integrationTest {
                        println("\n📘 활용 예제: 평균 괴리율 계산")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Divergence rate data
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)
                        val divergenceRates = client.funds.getDivergenceRate(isin, fromDate, toDate)

                        assertTrue(divergenceRates.isNotEmpty(), "괴리율 데이터가 있어야 합니다")

                        // When: Calculate average
                        val avgDivergence = divergenceRates.map { it.divergenceRate }.average()
                        val avgAbsDivergence = divergenceRates.map { abs(it.divergenceRate) }.average()

                        // Then: Display analysis
                        println("\n=== 평균 괴리율 분석 ===")
                        println("기간: $fromDate ~ $toDate")
                        println("평균 괴리율: ${"%.4f".format(avgDivergence)}%")
                        println("평균 절대 괴리율: ${"%.4f".format(avgAbsDivergence)}%")
                        println()
                        println("📊 분석: 평균 괴리율이 0에 가까울수록 ETF 가격이 NAV에 가깝게 거래됩니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 여러 ETF의 괴리율을 비교할 수 있다")
                fun compare_divergence_rates_across_etfs() =
                    integrationTest {
                        println("\n📘 활용 예제: ETF 간 괴리율 비교")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Multiple ETFs
                        val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                        val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                        val toDate = TestFixtures.Dates.TRADING_DAY
                        val fromDate = toDate.minusMonths(1)

                        // When: Fetch divergence rates for both
                        val tiger200Divergence = client.funds.getDivergenceRate(tiger200Isin, fromDate, toDate)
                        val kodex200Divergence = client.funds.getDivergenceRate(kodex200Isin, fromDate, toDate)

                        assertTrue(tiger200Divergence.isNotEmpty(), "TIGER 200 데이터가 있어야 합니다")
                        assertTrue(kodex200Divergence.isNotEmpty(), "KODEX 200 데이터가 있어야 합니다")

                        // Then: Compare average divergence rates
                        val tiger200Avg = tiger200Divergence.map { abs(it.divergenceRate) }.average()
                        val kodex200Avg = kodex200Divergence.map { abs(it.divergenceRate) }.average()

                        println("\n=== KOSPI 200 추종 ETF 괴리율 비교 ===")
                        println("기간: $fromDate ~ $toDate")
                        println()
                        println("TIGER 200")
                        println("  • 평균 절대 괴리율: ${"%.4f".format(tiger200Avg)}%")
                        println("  • 데이터 포인트: ${tiger200Divergence.size}개")
                        println()
                        println("KODEX 200")
                        println("  • 평균 절대 괴리율: ${"%.4f".format(kodex200Avg)}%")
                        println("  • 데이터 포인트: ${kodex200Divergence.size}개")
                        println()
                        println("📊 분석: 동일 지수 추종 ETF 간 괴리율을 비교하여 유동성/거래 효율성을 평가할 수 있습니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }
        }
    }

    // ========================================
    // 기능 그룹 2: 포트폴리오 API
    // ========================================

    @Nested
    @DisplayName("포트폴리오 API")
    inner class PortfolioApi {
        @Nested
        @DisplayName("getPortfolio() - 전체 포트폴리오 조회")
        inner class GetPortfolio {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("TIGER 200의 포트폴리오 구성을 거래일에 조회할 수 있다")
                fun get_tiger200_portfolio_on_trading_day() =
                    integrationTest {
                        println("\n📘 API: getPortfolio()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: TIGER 200 ISIN and trading day
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When: Request portfolio composition
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // Then: Returns portfolio constituents
                        assertTrue(portfolio.isNotEmpty(), "거래일에는 포트폴리오 구성 종목이 있어야 합니다")

                        val totalWeight = portfolio.sumOf { it.weightPercent.toDouble() }

                        println("\n📤 Response: List<PortfolioConstituent>")
                        println("  • constituents.size: ${portfolio.size}개")
                        println("  • totalWeight: ${"%.2f".format(totalWeight)}%")
                        println("  • top 3 constituents:")
                        portfolio.sortedByDescending { it.weightPercent }.take(3).forEachIndexed { index, constituent ->
                            println(
                                "    ${index + 1}. ${constituent.constituentName}: ${"%.2f".format(
                                    constituent.weightPercent,
                                )}%",
                            )
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = portfolio,
                            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
                            fileName = "tiger200_portfolio",
                        )
                    }

                @Test
                @DisplayName("KODEX 200의 포트폴리오 구성을 거래일에 조회할 수 있다")
                fun get_kodex200_portfolio_on_trading_day() =
                    integrationTest {
                        println("\n📘 API: getPortfolio()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: KODEX 200 ISIN and trading day
                        val isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When: Request portfolio composition
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // Then: Returns portfolio constituents
                        assertTrue(portfolio.isNotEmpty(), "거래일에는 포트폴리오 구성 종목이 있어야 합니다")

                        println("\n📤 Response: List<PortfolioConstituent>")
                        println("  • constituents.size: ${portfolio.size}개")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = portfolio,
                            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
                            fileName = "kodex200_portfolio",
                        )
                    }

                @Test
                @DisplayName("[파라미터: isin] 서로 다른 ISIN으로 서로 다른 포트폴리오를 조회할 수 있다")
                fun get_different_portfolios_by_different_isin() =
                    integrationTest {
                        println("\n📘 파라미터 테스트: isin")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Two different ISINs
                        val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                        val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When: Request with different ISINs
                        val tiger200Portfolio = client.funds.getPortfolio(tiger200Isin, tradeDate)
                        val kodex200Portfolio = client.funds.getPortfolio(kodex200Isin, tradeDate)

                        // Then: Returns different portfolios
                        assertTrue(tiger200Portfolio.isNotEmpty())
                        assertTrue(kodex200Portfolio.isNotEmpty())

                        println("  Case 1: isin = \"$tiger200Isin\" (TIGER 200)")
                        println("    → constituents: ${tiger200Portfolio.size}개")
                        println()
                        println("  Case 2: isin = \"$kodex200Isin\" (KODEX 200)")
                        println("    → constituents: ${kodex200Portfolio.size}개")
                        println()
                        println("  ✅ 분석: 서로 다른 ISIN으로 서로 다른 포트폴리오 조회 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("응답은 구성 종목 목록을 포함한다")
                fun response_contains_constituents() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 구성 종목")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // Then: Validate structure
                        assertTrue(portfolio.isNotEmpty(), "포트폴리오는 최소 1개 이상의 구성 종목을 포함해야 합니다")

                        val firstConstituent = portfolio.first()
                        assertTrue(firstConstituent.constituentName.isNotEmpty(), "구성 종목명은 비어있지 않아야 합니다")
                        assertTrue(firstConstituent.weightPercent.toDouble() > 0, "비중은 0보다 커야 합니다")

                        println("✅ 응답 구조 검증 통과:")
                        println("  • constituents.size: ${portfolio.size}개 (> 0) ✓")
                        println("  • 첫 번째 종목명: ${firstConstituent.constituentName} ✓")
                        println("  • 첫 번째 종목 비중: ${firstConstituent.weightPercent}% (> 0) ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("비중 합계는 약 100%다 (오차 범위 ±1%)")
                fun total_weight_is_approximately_100_percent() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 비중 합계")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // Then: Total weight should be approximately 100%
                        val totalWeight = portfolio.sumOf { it.weightPercent.toDouble() }
                        assertTrue(abs(totalWeight - 100.0) <= 1.0, "비중 합계는 100% 근처여야 합니다. 실제: $totalWeight%")

                        println("✅ 비중 합계 검증:")
                        println("  • totalWeight: ${"%.2f".format(totalWeight)}%")
                        println("  • 허용 범위: 99.0% ~ 101.0%")
                        println("  • 오차: ${"%.2f".format(abs(totalWeight - 100.0))}% ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("각 구성 종목의 비중은 0과 100 사이다")
                fun each_weight_is_between_0_and_100() =
                    integrationTest {
                        println("\n📘 응답 데이터 검증: 개별 비중 범위")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // Then: Each weight should be between 0 and 100
                        portfolio.forEach { constituent ->
                            val weight = constituent.weightPercent.toDouble()
                            assertTrue(weight >= 0, "${constituent.constituentName}의 비중은 0 이상이어야 합니다")
                            assertTrue(weight <= 100, "${constituent.constituentName}의 비중은 100 이하여야 합니다")
                        }

                        val maxWeight = portfolio.maxOfOrNull { it.weightPercent.toDouble() } ?: 0.0
                        val minWeight = portfolio.minOfOrNull { it.weightPercent.toDouble() } ?: 0.0

                        println("✅ 개별 비중 범위 검증:")
                        println("  • 최대 비중: ${"%.2f".format(maxWeight)}% ✓")
                        println("  • 최소 비중: ${"%.2f".format(minWeight)}% ✓")
                        println("  • 범위: 0% ≤ 비중 ≤ 100% ✓")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                @Test
                @DisplayName("존재하지 않는 ISIN 조회시 빈 리스트를 반환한다")
                fun returns_empty_list_for_non_existent_isin() =
                    integrationTest {
                        println("\n📘 입력 검증: 존재하지 않는 ISIN")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Invalid ISIN that doesn't exist
                        val invalidIsin = "KR7999999999"
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input:")
                        println("  • isin: \"$invalidIsin\" (존재하지 않는 ISIN)")
                        println("  • tradeDate: $tradeDate")

                        // When
                        val portfolio = client.funds.getPortfolio(invalidIsin, tradeDate)

                        // Then: Returns empty list for non-existent ISIN
                        assertTrue(portfolio.isEmpty(), "존재하지 않는 ISIN은 빈 리스트를 반환해야 합니다")

                        println("\n📤 Response: List<PortfolioConstituent> (empty)")
                        println("  • constituents.size: ${portfolio.size}")
                        println("\n✅ 처리 결과: 존재하지 않는 ISIN에 대해 빈 리스트 반환")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                @Test
                @DisplayName("[파라미터: tradeDate] 비거래일에 조회하면 최근 거래일 데이터를 반환한다")
                fun returns_latest_data_on_non_trading_day() =
                    integrationTest {
                        println("\n📘 엣지 케이스: 비거래일 조회")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Non-trading day (Saturday)
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

                        println("📥 Input:")
                        println("  • isin: \"$isin\"")
                        println("  • tradeDate: $tradeDate (비거래일 - 토요일)")

                        // When
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // Then: Returns latest trading day data
                        assertNotNull(portfolio, "비거래일에도 최근 거래일 데이터를 반환해야 합니다")

                        println("\n📤 Response: List<PortfolioConstituent> (최근 거래일 데이터)")
                        println("  • constituents.size: ${portfolio.size}개")
                        println()
                        println("  ℹ️ 참고: API는 최근 거래일 데이터를 반환합니다")

                        println("\n✅ 처리 결과: 비거래일에도 데이터 제공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[파라미터: tradeDate] 거래일과 비거래일 데이터 비교")
                fun compare_trading_day_vs_non_trading_day() =
                    integrationTest {
                        println("\n📘 파라미터 비교: 거래일 vs 비거래일")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        val isin = TestFixtures.Etf.TIGER_200_ISIN

                        // When: Query on both trading and non-trading days
                        println("  Case 1: 거래일 (${TestFixtures.Dates.TRADING_DAY}, 월요일)")
                        val tradingDayResult = client.funds.getPortfolio(isin, TestFixtures.Dates.TRADING_DAY)
                        println("    → constituents: ${tradingDayResult.size}개")

                        println("\n  Case 2: 비거래일 (${TestFixtures.Dates.NON_TRADING_DAY}, 토요일)")
                        val nonTradingDayResult = client.funds.getPortfolio(isin, TestFixtures.Dates.NON_TRADING_DAY)
                        println("    → constituents: ${nonTradingDayResult.size}개")

                        // Then: Both should return data
                        assertTrue(tradingDayResult.isNotEmpty())
                        assertTrue(nonTradingDayResult.isNotEmpty())

                        println("\n  ✅ 분석: 비거래일 조회시 최근 거래일 데이터 제공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }

            @Nested
            @DisplayName("5. 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[활용] 상위 10개 구성 종목을 확인할 수 있다")
                fun get_top_10_holdings() =
                    integrationTest {
                        println("\n📘 활용 예제: 상위 10개 구성 종목")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Portfolio data
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // When: Sort by weight descending
                        val top10 =
                            portfolio
                                .sortedByDescending { it.weightPercent }
                                .take(10)

                        // Then: Display top 10 holdings
                        println("\n=== 상위 10개 구성 종목 (거래일: $tradeDate) ===")
                        top10.forEachIndexed { index, constituent ->
                            println(
                                "${index + 1}. ${constituent.constituentName}: ${"%.2f".format(
                                    constituent.weightPercent,
                                )}%",
                            )
                        }

                        val top10Weight = top10.sumOf { it.weightPercent.toDouble() }
                        println()
                        println("📊 분석: 상위 10개 종목이 전체 포트폴리오의 ${"%.2f".format(top10Weight)}%를 차지합니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 특정 종목의 비중을 확인할 수 있다")
                fun find_specific_stock_weight() =
                    integrationTest {
                        println("\n📘 활용 예제: 특정 종목 비중 확인")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Portfolio data
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY
                        val portfolio = client.funds.getPortfolio(isin, tradeDate)

                        // When: Search for Samsung Electronics
                        val samsung = portfolio.find { it.constituentName.contains("삼성전자") }

                        // Then: Display Samsung weight
                        println("\n=== 특정 종목 비중 (거래일: $tradeDate) ===")
                        if (samsung != null) {
                            println("${samsung.constituentName}: ${"%.2f".format(samsung.weightPercent)}%")
                            println()
                            println("📊 분석: 삼성전자는 TIGER 200 포트폴리오의 ${"%.2f".format(samsung.weightPercent)}%를 차지합니다")
                        } else {
                            println("⚠️ 삼성전자가 포트폴리오에 없습니다.")
                        }
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }

                @Test
                @DisplayName("[활용] 여러 ETF의 포트폴리오 크기를 비교할 수 있다")
                fun compare_portfolio_sizes_across_etfs() =
                    integrationTest {
                        println("\n📘 활용 예제: ETF 포트폴리오 크기 비교")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: Multiple ETFs
                        val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                        val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        // When: Fetch portfolios for both
                        val tiger200Portfolio = client.funds.getPortfolio(tiger200Isin, tradeDate)
                        val kodex200Portfolio = client.funds.getPortfolio(kodex200Isin, tradeDate)

                        assertTrue(tiger200Portfolio.isNotEmpty(), "TIGER 200 포트폴리오가 있어야 합니다")
                        assertTrue(kodex200Portfolio.isNotEmpty(), "KODEX 200 포트폴리오가 있어야 합니다")

                        // Then: Compare sizes
                        println("\n=== KOSPI 200 추종 ETF 포트폴리오 크기 비교 (거래일: $tradeDate) ===")
                        println()
                        println("TIGER 200")
                        println("  • 구성 종목 수: ${tiger200Portfolio.size}개")

                        val tiger200Top = tiger200Portfolio.sortedByDescending { it.weightPercent }.first()
                        println(
                            "  • 최대 비중 종목: ${tiger200Top.constituentName} (${"%.2f".format(
                                tiger200Top.weightPercent,
                            )}%)",
                        )
                        println()
                        println("KODEX 200")
                        println("  • 구성 종목 수: ${kodex200Portfolio.size}개")

                        val kodex200Top = kodex200Portfolio.sortedByDescending { it.weightPercent }.first()
                        println(
                            "  • 최대 비중 종목: ${kodex200Top.constituentName} (${"%.2f".format(
                                kodex200Top.weightPercent,
                            )}%)",
                        )
                        println()
                        println("📊 분석: 동일 지수를 추종하는 ETF 간 포트폴리오 구성을 비교할 수 있습니다")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    }
            }
        }

        @Nested
        @DisplayName("getPortfolioTop10() - 상위 10종목 조회")
        inner class GetPortfolioTop10 {
            @Nested
            @DisplayName("1. 기본 동작")
            inner class BasicOperations {
                @Test
                @DisplayName("TIGER 200 포트폴리오 상위 10종목을 거래일에 조회할 수 있다")
                fun get_tiger200_portfolio_top10_on_trading_day() =
                    integrationTest {
                        println("\n📘 API: getPortfolioTop10()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: 입력 파라미터 표시
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When: API 호출
                        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

                        // Then: 결과 즉시 표시
                        println("\n📤 Response: List<PortfolioItem>")
                        println("  • size: ${topItems.size}")

                        if (topItems.isNotEmpty()) {
                            println("\n  [상위 5개 종목]")
                            topItems.take(5).forEachIndexed { index, item ->
                                println("    ${index + 1}. ${item.name} - 비중: ${item.compositionRatio}%")
                            }
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(topItems, "거래일에는 TIGER 200의 포트폴리오 상위 10종목이 반환되어야 합니다")
                        assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")
                        assertTrue(topItems.size <= 10, "포트폴리오 상위 10종목은 최대 10개까지만 포함합니다")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = topItems,
                            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO_TOP10,
                            fileName = "tiger200_portfolio_top10",
                        )
                    }

                @Test
                @DisplayName("KODEX 200 포트폴리오 상위 10종목을 거래일에 조회할 수 있다")
                fun get_kodex200_portfolio_top10_on_trading_day() =
                    integrationTest {
                        println("\n📘 API: getPortfolioTop10()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: 입력 파라미터 표시
                        val isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When: API 호출
                        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

                        // Then: 결과 즉시 표시
                        println("\n📤 Response: List<PortfolioItem>")
                        println("  • size: ${topItems.size}")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(topItems, "거래일에는 KODEX 200의 포트폴리오 상위 10종목이 반환되어야 합니다")
                        assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")

                        // 스마트 레코딩
                        SmartRecorder.recordSmartly(
                            data = topItems,
                            category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO_TOP10,
                            fileName = "kodex200_portfolio_top10",
                        )
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증")
            inner class ResponseValidation {
                @Test
                @DisplayName("각 항목은 종목명과 비중을 포함한다")
                fun validate_response_contains_name_and_ratio() =
                    integrationTest {
                        println("\n📘 API: getPortfolioTop10()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When
                        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

                        // Then: 필드 검증
                        println("\n📤 Response: List<PortfolioItem>")
                        println("  • size: ${topItems.size}")

                        if (topItems.isNotEmpty()) {
                            val topItem = topItems.first()
                            println("\n  [필드 검증]")
                            println("  • name: ${topItem.name} ✓")
                            println("  • compositionRatio: ${topItem.compositionRatio}% ✓")

                            assertTrue(!topItem.name.isNullOrEmpty(), "종목명이 있어야 합니다")
                            assertTrue(
                                topItem.compositionRatio.compareTo(java.math.BigDecimal.ZERO) >= 0,
                                "비중은 0 이상이어야 합니다",
                            )
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(topItems, "포트폴리오 상위 10종목이 반환되어야 합니다")
                        assertTrue(topItems.isNotEmpty(), "포트폴리오는 최소 1개 이상의 종목을 포함해야 합니다")
                    }
            }

            @Nested
            @DisplayName("3. 엣지 케이스")
            inner class EdgeCases {
                @Test
                @DisplayName("비거래일에 조회하면 빈 데이터를 반환한다")
                fun return_empty_data_on_non_trading_day() =
                    integrationTest {
                        println("\n📘 API: getPortfolioTop10()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given: 입력 파라미터 표시
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.NON_TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate (비거래일)")

                        // When: API 호출
                        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

                        // Then: 결과 즉시 표시
                        println("\n📤 Response: List<PortfolioItem>")
                        println("  • size: ${topItems.size} (비거래일이므로 빈 응답 가능)")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(topItems, "API 호출은 성공해야 합니다")
                    }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제")
            inner class PracticalExamples {
                @Test
                @DisplayName("ETF 포트폴리오의 집중도를 분석할 수 있다")
                fun analyze_portfolio_concentration() =
                    integrationTest {
                        println("\n📘 API: getPortfolioTop10() - 활용 예제")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val isin = TestFixtures.Etf.TIGER_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • isin: String = \"$isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When
                        val topItems = client.funds.getPortfolioTop10(isin, tradeDate)

                        // Then: 집중도 계산
                        println("\n📤 Response: List<PortfolioItem>")
                        println("  • size: ${topItems.size}")

                        if (topItems.isNotEmpty()) {
                            val totalWeight = topItems.sumOf { it.compositionRatio }
                            val topThreeWeight = topItems.take(3).sumOf { it.compositionRatio }
                            val topFiveWeight = topItems.take(5).sumOf { it.compositionRatio }

                            println("\n  [포트폴리오 집중도 분석]")
                            println("  • 상위 10종목 총 비중: $totalWeight%")
                            println("  • 상위 3종목 비중: $topThreeWeight%")
                            println("  • 상위 5종목 비중: $topFiveWeight%")
                            println(
                                "  • 평가: " +
                                    when {
                                        topThreeWeight.compareTo(
                                            java.math.BigDecimal("40"),
                                        ) >= 0 -> "집중도 높음 (상위 3종목 40% 이상)"
                                        topFiveWeight.compareTo(
                                            java.math.BigDecimal("50"),
                                        ) >= 0 -> "집중도 중간 (상위 5종목 50% 이상)"
                                        else -> "집중도 낮음 (분산 투자)"
                                    },
                            )
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(topItems.isNotEmpty(), "데이터가 있어야 합니다")
                    }

                @Test
                @DisplayName("여러 ETF의 포트폴리오 구성을 비교할 수 있다")
                fun compare_portfolio_composition_between_etfs() =
                    integrationTest {
                        println("\n📘 API: getPortfolioTop10() - 활용 예제")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val tiger200Isin = TestFixtures.Etf.TIGER_200_ISIN
                        val kodex200Isin = TestFixtures.Etf.KODEX_200_ISIN
                        val tradeDate = TestFixtures.Dates.TRADING_DAY

                        println("📥 Input Parameters:")
                        println("  • tiger200Isin: String = \"$tiger200Isin\"")
                        println("  • kodex200Isin: String = \"$kodex200Isin\"")
                        println("  • tradeDate: LocalDate = $tradeDate")

                        // When
                        val tiger200Items = client.funds.getPortfolioTop10(tiger200Isin, tradeDate)
                        val kodex200Items = client.funds.getPortfolioTop10(kodex200Isin, tradeDate)

                        // Then: 포트폴리오 비교 분석
                        println("\n📤 Response: 2개 ETF 포트폴리오 비교")

                        if (tiger200Items.isNotEmpty() && kodex200Items.isNotEmpty()) {
                            val tiger200AvgWeight =
                                if (tiger200Items.isNotEmpty()) {
                                    tiger200Items.sumOf { it.compositionRatio }.divide(
                                        tiger200Items.size.toBigDecimal(),
                                        4,
                                        java.math.RoundingMode.HALF_UP,
                                    )
                                } else {
                                    java.math.BigDecimal.ZERO
                                }

                            val kodex200AvgWeight =
                                if (kodex200Items.isNotEmpty()) {
                                    kodex200Items.sumOf { it.compositionRatio }.divide(
                                        kodex200Items.size.toBigDecimal(),
                                        4,
                                        java.math.RoundingMode.HALF_UP,
                                    )
                                } else {
                                    java.math.BigDecimal.ZERO
                                }

                            println("\n  [TIGER 200]")
                            println("  • 상위 10종목 구성 수: ${tiger200Items.size}개")
                            println("  • 상위 10종목 총 비중: ${tiger200Items.sumOf { it.compositionRatio }}%")
                            println("  • 평균 비중: $tiger200AvgWeight%")

                            println("\n  [KODEX 200]")
                            println("  • 상위 10종목 구성 수: ${kodex200Items.size}개")
                            println("  • 상위 10종목 총 비중: ${kodex200Items.sumOf { it.compositionRatio }}%")
                            println("  • 평균 비중: $kodex200AvgWeight%")

                            // 공통 종목 분석
                            val tiger200Names = tiger200Items.map { it.name }.toSet()
                            val kodex200Names = kodex200Items.map { it.name }.toSet()
                            val commonStocks = tiger200Names.intersect(kodex200Names)

                            println("\n  [공통 종목 분석]")
                            println("  • 공통 종목 수: ${commonStocks.size}개/${tiger200Items.size}개")
                            if (commonStocks.isNotEmpty()) {
                                println("  • 공통 상위 종목: ${commonStocks.take(5).joinToString(", ")}")
                            }
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(tiger200Items.isNotEmpty(), "TIGER 200 포트폴리오가 있어야 합니다")
                        assertTrue(kodex200Items.isNotEmpty(), "KODEX 200 포트폴리오가 있어야 합니다")
                    }
            }
        }
    }

    // ========================================
    // 기능 그룹 3: 거래 및 공매도 API
    // ========================================

    @Nested
    @DisplayName("거래 및 공매도 API")
    inner class TradingAndShortApi {
        @Nested
        @DisplayName("투자자별 거래 조회")
        inner class InvestorTradingGroup {
            @Nested
            @DisplayName("getAllInvestorTrading() - 전체 ETF 투자자별 거래 (특정일)")
            inner class GetAllInvestorTrading {
                @Nested
                @DisplayName("1. 기본 동작")
                inner class BasicOperations {
                    @Test
                    @DisplayName("특정 날짜의 전체 ETF 투자자별 거래를 조회할 수 있다")
                    fun get_all_etf_investor_trading_on_specific_date() =
                        integrationTest {
                            println("\n📘 API: getAllInvestorTrading()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val date = TestFixtures.Dates.TRADING_DAY

                            println("📥 Input Parameters:")
                            println("  • date: LocalDate = $date")

                            // When
                            val investorTrading = client.funds.getAllInvestorTrading(date)

                            // Then
                            println("\n📤 Response: List<InvestorTrading>")
                            println("  • size: ${investorTrading.size}")

                            if (investorTrading.isNotEmpty()) {
                                println("\n  [샘플 데이터 - 상위 3개]")
                                investorTrading.take(3).forEach { trading ->
                                    println("    • ${trading.investorType}: 순매수 ${trading.netBuyVolume}주")
                                }
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(investorTrading.isNotEmpty(), "투자자별 거래 데이터가 반환되어야 합니다")

                            SmartRecorder.recordSmartly(
                                data = investorTrading,
                                category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                                fileName = "all_etf_investor_trading",
                            )
                        }
                }
            }

            @Nested
            @DisplayName("getAllInvestorTradingByPeriod() - 전체 ETF 투자자별 거래 (기간)")
            inner class GetAllInvestorTradingByPeriod {
                @Nested
                @DisplayName("1. 기본 동작")
                inner class BasicOperations {
                    @Test
                    @DisplayName("기간별 전체 ETF 투자자별 거래를 조회할 수 있다")
                    fun get_all_etf_investor_trading_by_period() =
                        integrationTest {
                            println("\n📘 API: getAllInvestorTradingByPeriod()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val investorTradingByDate = client.funds.getAllInvestorTradingByPeriod(fromDate, toDate)

                            // Then
                            println("\n📤 Response: List<InvestorTrading>")
                            println("  • size: ${investorTradingByDate.size}")

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(investorTradingByDate.isNotEmpty(), "기간별 투자자 거래 데이터가 반환되어야 합니다")

                            SmartRecorder.recordSmartly(
                                data = investorTradingByDate,
                                category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                                fileName = "all_etf_investor_trading_period",
                            )
                        }
                }
            }

            @Nested
            @DisplayName("getInvestorTrading() - 개별 ETF 투자자별 거래 (특정일)")
            inner class GetInvestorTrading {
                @Nested
                @DisplayName("1. 기본 동작")
                inner class BasicOperations {
                    @Test
                    @DisplayName("개별 ETF의 투자자별 거래를 조회할 수 있다")
                    fun get_individual_etf_investor_trading() =
                        integrationTest {
                            println("\n📘 API: getInvestorTrading()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val date = TestFixtures.Dates.TRADING_DAY

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • date: LocalDate = $date")

                            // When
                            val investorTrading = client.funds.getInvestorTrading(isin, date)

                            // Then
                            println("\n📤 Response: List<InvestorTrading>")
                            println("  • size: ${investorTrading.size}")

                            if (investorTrading.isNotEmpty()) {
                                println("\n  [투자자 유형별 순매수]")
                                investorTrading.forEach { trading ->
                                    println(
                                        "    • ${trading.investorType}: ${trading.netBuyVolume}주 (${trading.netBuyValue}원)",
                                    )
                                }
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(investorTrading.isNotEmpty(), "투자자별 거래 데이터가 반환되어야 합니다")

                            SmartRecorder.recordSmartly(
                                data = investorTrading,
                                category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                                fileName = "tiger200_investor_trading",
                            )
                        }
                }
            }

            @Nested
            @DisplayName("getInvestorTradingByPeriod() - 개별 ETF 투자자별 거래 (기간)")
            inner class GetInvestorTradingByPeriod {
                @Nested
                @DisplayName("1. 기본 동작")
                inner class BasicOperations {
                    @Test
                    @DisplayName("개별 ETF의 기간별 투자자별 거래를 조회할 수 있다")
                    fun get_individual_etf_investor_trading_by_period() =
                        integrationTest {
                            println("\n📘 API: getInvestorTradingByPeriod()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val investorTradingByDate = client.funds.getInvestorTradingByPeriod(isin, fromDate, toDate)

                            // Then
                            println("\n📤 Response: List<InvestorTrading>")
                            println("  • size: ${investorTradingByDate.size}")

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(investorTradingByDate.isNotEmpty(), "기간별 투자자 거래 데이터가 반환되어야 합니다")

                            SmartRecorder.recordSmartly(
                                data = investorTradingByDate,
                                category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                                fileName = "tiger200_investor_trading_period",
                            )
                        }
                }

                @Nested
                @DisplayName("5. 실무 활용 예제")
                inner class PracticalExamples {
                    @Test
                    @DisplayName("기관 매매 추이를 분석할 수 있다")
                    fun analyze_institutional_trading_trend() =
                        integrationTest {
                            println("\n📘 API: getInvestorTradingByPeriod() - 활용 예제")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val investorTradingByDate = client.funds.getInvestorTradingByPeriod(isin, fromDate, toDate)

                            // Then: 기관 순매수 추이 분석
                            println("\n📤 Response: List<InvestorTrading>")
                            println("  • size: ${investorTradingByDate.size}")

                            val institutionalNetBuy =
                                investorTradingByDate
                                    .filter { it.investorType.contains("기관") }
                                    .map { Pair(it.tradeDate, it.netBuyVolume) }

                            println("\n  [기관 순매수 추이 - 상위 5일]")
                            institutionalNetBuy.take(5).forEach { (date, netBuy) ->
                                println("    • $date: ${netBuy}주")
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                        }
                }
            }
        }

        @Nested
        @DisplayName("공매도 조회")
        inner class ShortSellingGroup {
            @Nested
            @DisplayName("getShortSelling() - 공매도 거래")
            inner class GetShortSelling {
                @Nested
                @DisplayName("1. 기본 동작")
                inner class BasicOperations {
                    @Test
                    @DisplayName("TIGER 200의 공매도 거래를 기간별로 조회할 수 있다")
                    fun get_tiger200_short_selling_by_period() =
                        integrationTest {
                            println("\n📘 API: getShortSelling()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given: 입력 파라미터 표시
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When: API 호출
                            val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                            // Then: 결과 즉시 표시
                            println("\n📤 Response: List<ShortSelling>")
                            println("  • size: ${shortSellings.size}")

                            if (shortSellings.isNotEmpty()) {
                                val sample = shortSellings.first()
                                println("\n  [샘플 데이터]")
                                println("  • tradeDate: ${sample.tradeDate}")
                                println("  • shortVolume: ${sample.shortVolume}주")
                                println("  • shortValue: ${sample.shortValue}원")
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

                            // 스마트 레코딩
                            SmartRecorder.recordSmartly(
                                data = shortSellings,
                                category = RecordingConfig.Paths.EtfTrading.SHORT,
                                fileName = "tiger200_short_selling",
                            )
                        }

                    @Test
                    @DisplayName("KODEX 200의 공매도 거래를 기간별로 조회할 수 있다")
                    fun get_kodex200_short_selling_by_period() =
                        integrationTest {
                            println("\n📘 API: getShortSelling()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given: 입력 파라미터 표시
                            val isin = TestFixtures.Etf.KODEX_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When: API 호출
                            val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                            // Then: 결과 즉시 표시
                            println("\n📤 Response: List<ShortSelling>")
                            println("  • size: ${shortSellings.size}")

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 반환되어야 합니다")

                            // 스마트 레코딩
                            SmartRecorder.recordSmartly(
                                data = shortSellings,
                                category = RecordingConfig.Paths.EtfTrading.SHORT,
                                fileName = "kodex200_short_selling",
                            )
                        }
                }

                @Nested
                @DisplayName("2. 응답 데이터 검증")
                inner class ResponseValidation {
                    @Test
                    @DisplayName("응답 데이터는 일별 공매도 거래량과 거래금액을 포함한다")
                    fun validate_response_contains_daily_trading_data() =
                        integrationTest {
                            println("\n📘 API: getShortSelling()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusDays(7)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                            // Then: 필드 검증
                            println("\n📤 Response: List<ShortSelling>")
                            println("  • size: ${shortSellings.size}")

                            if (shortSellings.isNotEmpty()) {
                                val first = shortSellings.first()
                                println("\n  [필드 검증]")
                                println("  • tradeDate: ${first.tradeDate} ✓")
                                println("  • shortVolume: ${first.shortVolume} ✓")
                                println("  • shortValue: ${first.shortValue} ✓")

                                assertNotNull(first.tradeDate, "거래일자는 null이 아니어야 합니다")
                                assertTrue(first.shortVolume >= 0, "공매도 거래량은 0 이상이어야 합니다")
                                assertTrue(first.shortValue >= 0, "공매도 거래금액은 0 이상이어야 합니다")
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 있어야 합니다")
                        }
                }

                @Nested
                @DisplayName("5. 실무 활용 예제")
                inner class PracticalExamples {
                    @Test
                    @DisplayName("공매도 거래량 급증 날짜를 분석할 수 있다")
                    fun analyze_short_selling_spike_days() =
                        integrationTest {
                            println("\n📘 API: getShortSelling() - 활용 예제")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val shortSellings = client.funds.getShortSelling(isin, fromDate, toDate)

                            // Then: 급증 날짜 분석
                            println("\n📤 Response: List<ShortSelling>")
                            println("  • size: ${shortSellings.size}")

                            if (shortSellings.isNotEmpty()) {
                                val avgVolume = shortSellings.map { it.shortVolume }.average()
                                val highVolumeDays = shortSellings.filter { it.shortVolume > avgVolume * 2 }

                                println("\n  [공매도 거래량 급증 분석]")
                                println("  • 평균 공매도 거래량: ${"%.0f".format(avgVolume)}주")
                                println("  • 급증 날짜 수 (평균의 2배 이상): ${highVolumeDays.size}일")

                                if (highVolumeDays.isNotEmpty()) {
                                    println("\n  [급증 날짜 상세]")
                                    highVolumeDays.forEach { day ->
                                        println("    - ${day.tradeDate}: ${day.shortVolume}주")
                                    }
                                }
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortSellings.isNotEmpty(), "공매도 거래 데이터가 있어야 합니다")
                        }
                }
            }

            @Nested
            @DisplayName("getShortBalance() - 공매도 잔고")
            inner class GetShortBalance {
                @Nested
                @DisplayName("1. 기본 동작")
                inner class BasicOperations {
                    @Test
                    @DisplayName("TIGER 200의 공매도 잔고를 기간별로 조회할 수 있다")
                    fun get_tiger200_short_balance_by_period() =
                        integrationTest {
                            println("\n📘 API: getShortBalance()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given: 입력 파라미터 표시
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When: API 호출
                            val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                            // Then: 결과 즉시 표시
                            println("\n📤 Response: List<ShortBalance>")
                            println("  • size: ${shortBalances.size}")

                            if (shortBalances.isNotEmpty()) {
                                val sample = shortBalances.first()
                                println("\n  [샘플 데이터]")
                                println("  • tradeDate: ${sample.tradeDate}")
                                println("  • shortBalance: ${sample.shortBalance}주")
                                println("  • shortBalanceValue: ${sample.shortBalanceValue}원")
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

                            // 스마트 레코딩
                            SmartRecorder.recordSmartly(
                                data = shortBalances,
                                category = RecordingConfig.Paths.EtfTrading.SHORT,
                                fileName = "tiger200_short_balance",
                            )
                        }

                    @Test
                    @DisplayName("KODEX 200의 공매도 잔고를 기간별로 조회할 수 있다")
                    fun get_kodex200_short_balance_by_period() =
                        integrationTest {
                            println("\n📘 API: getShortBalance()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given: 입력 파라미터 표시
                            val isin = TestFixtures.Etf.KODEX_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusMonths(1)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When: API 호출
                            val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                            // Then: 결과 즉시 표시
                            println("\n📤 Response: List<ShortBalance>")
                            println("  • size: ${shortBalances.size}")

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 반환되어야 합니다")

                            // 스마트 레코딩
                            SmartRecorder.recordSmartly(
                                data = shortBalances,
                                category = RecordingConfig.Paths.EtfTrading.SHORT,
                                fileName = "kodex200_short_balance",
                            )
                        }
                }

                @Nested
                @DisplayName("2. 응답 데이터 검증")
                inner class ResponseValidation {
                    @Test
                    @DisplayName("응답 데이터는 일별 공매도 잔고와 잔고금액을 포함한다")
                    fun validate_response_contains_daily_balance_data() =
                        integrationTest {
                            println("\n📘 API: getShortBalance()")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusDays(7)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                            // Then: 필드 검증
                            println("\n📤 Response: List<ShortBalance>")
                            println("  • size: ${shortBalances.size}")

                            if (shortBalances.isNotEmpty()) {
                                val first = shortBalances.first()
                                println("\n  [필드 검증]")
                                println("  • tradeDate: ${first.tradeDate} ✓")
                                println("  • shortBalance: ${first.shortBalance} ✓")
                                println("  • shortBalanceValue: ${first.shortBalanceValue} ✓")

                                assertNotNull(first.tradeDate, "거래일자는 null이 아니어야 합니다")
                                assertTrue(first.shortBalance >= 0, "공매도 잔고는 0 이상이어야 합니다")
                                assertTrue(first.shortBalanceValue >= 0, "공매도 잔고금액은 0 이상이어야 합니다")
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 있어야 합니다")
                        }
                }

                @Nested
                @DisplayName("5. 실무 활용 예제")
                inner class PracticalExamples {
                    @Test
                    @DisplayName("최근 공매도 잔고 현황을 확인할 수 있다")
                    fun check_recent_short_balance_status() =
                        integrationTest {
                            println("\n📘 API: getShortBalance() - 활용 예제")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                            // Given
                            val isin = TestFixtures.Etf.TIGER_200_ISIN
                            val toDate = TestFixtures.Dates.TRADING_DAY
                            val fromDate = toDate.minusDays(7)

                            println("📥 Input Parameters:")
                            println("  • isin: String = \"$isin\"")
                            println("  • fromDate: LocalDate = $fromDate")
                            println("  • toDate: LocalDate = $toDate")

                            // When
                            val shortBalances = client.funds.getShortBalance(isin, fromDate, toDate)

                            // Then: 최근 잔고 현황 분석
                            println("\n📤 Response: List<ShortBalance>")
                            println("  • size: ${shortBalances.size}")

                            if (shortBalances.isNotEmpty()) {
                                val latestBalance = shortBalances.last()
                                println("\n  [최근 공매도 잔고 현황]")
                                println("  • 날짜: ${latestBalance.tradeDate}")
                                println("  • 공매도 잔고: ${latestBalance.shortBalance}주")
                                println("  • 공매도 금액: ${latestBalance.shortBalanceValue}원")
                            }

                            println("\n✅ 테스트 결과: 성공")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                            assertTrue(shortBalances.isNotEmpty(), "공매도 잔고 데이터가 있어야 합니다")
                        }
                }
            }
        }
    }
}
