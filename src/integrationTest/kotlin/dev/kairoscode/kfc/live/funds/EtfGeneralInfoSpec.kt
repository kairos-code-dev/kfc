package dev.kairoscode.kfc.live.funds

import dev.kairoscode.kfc.utils.IntegrationTestBase
import dev.kairoscode.kfc.utils.RecordingConfig
import dev.kairoscode.kfc.utils.ResponseRecorder
import dev.kairoscode.kfc.utils.TestSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * ETF 기본정보 조회 Integration Test
 *
 * getGeneralInfo() 함수의 실제 API 호출 테스트 및 응답 레코딩
 * ETF의 상세 메타데이터 및 운용 정보 조회 (정적 데이터)
 */
class GeneralInfoSpec : IntegrationTestBase() {

    @Test
    @DisplayName("TIGER 200 기본정보를 거래일에 조회할 수 있다")
    fun testGetGeneralInfoTiger200OnTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 거래일
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY // 2024-11-25 (월요일)

        // When: 기본정보 조회
        val info = client.funds.getGeneralInfo(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(info, "거래일에는 TIGER 200의 기본정보가 반환되어야 합니다")

        // 기본정보의 주요 필드 검증
        assertTrue(!info?.name.isNullOrEmpty(), "ETF 이름이 있어야 합니다")
        assertTrue(!info?.isin.isNullOrEmpty(), "ISIN 코드가 있어야 합니다")
        assertTrue(!info?.assetClassName.isNullOrEmpty(), "자산 분류가 있어야 합니다")
        assertTrue(!info?.issuerName.isNullOrEmpty(), "발행사/운용사명이 있어야 합니다")

        println("✅ TIGER 200 기본정보 (거래일: $tradeDate)")
        println("  - ETF 명: ${info?.name}")
        println("  - ISIN: ${info?.isin}")
        println("  - 자산 분류: ${info?.assetClassName}")
        println("  - 발행사/운용사: ${info?.issuerName}")
        println("  - 상장일: ${info?.listingDate}")
        println("  - 순자산총액: ${info?.netAssetTotal}원")

        // 응답 레코딩 (단일 객체)
        ResponseRecorder.record(
            data = info,
            category = RecordingConfig.Paths.EtfMetrics.GENERAL_INFO,
            fileName = "tiger200_general_info"
        )
    }

    @Test
    @DisplayName("TIGER 200 기본정보를 비거래일에 조회하면 데이터를 반환한다 (정적 메타데이터)")
    fun testGetGeneralInfoTiger200OnNonTradingDay() = integrationTest {
        // Given: TIGER 200 ISIN과 고정 비거래일 (토요일)
        val isin = TestSymbols.TIGER_200_ISIN
        val tradeDate = TestSymbols.NON_TRADING_DAY // 2024-11-23 (토요일)

        // When: 기본정보 조회
        val info = client.funds.getGeneralInfo(isin, tradeDate)

        // Then: 데이터 반환 (기본정보는 정적 메타데이터이므로 거래일과 무관)
        assertNotNull(info, "API는 비거래일에도 데이터를 반환합니다 (정적 메타데이터)")

        println("✅ 비거래일($tradeDate) 기본정보 조회 결과:")
        println("  - 데이터 존재: 예 (정적 메타데이터)")
        println("  - ETF 명: ${info?.name}")
    }

    @Test
    @DisplayName("KODEX 200 기본정보를 거래일에 조회할 수 있다")
    fun testGetGeneralInfoKodex200OnTradingDay() = integrationTest {
        // Given: KODEX 200 ISIN과 고정 거래일
        val isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY // 2024-11-25 (월요일)

        // When: 기본정보 조회
        val info = client.funds.getGeneralInfo(isin, tradeDate)

        // Then: 정보 반환
        assertNotNull(info, "거래일에는 KODEX 200의 기본정보가 반환되어야 합니다")
        assertTrue(!info?.name.isNullOrEmpty(), "ETF 이름이 있어야 합니다")

        println("✅ KODEX 200 기본정보 (거래일: $tradeDate)")
        println("  - ETF 명: ${info?.name}")
        println("  - 자산 분류: ${info?.assetClassName}")

        // 응답 레코딩 (단일 객체)
        ResponseRecorder.record(
            data = info,
            category = RecordingConfig.Paths.EtfMetrics.GENERAL_INFO,
            fileName = "kodex200_general_info"
        )
    }

    @Test
    @DisplayName("[활용] ETF의 발행사 정보를 확인할 수 있다")
    fun testIssuerComparison() = integrationTest {
        // Given: TIGER 200과 KODEX 200의 기본정보 조회 (고정 거래일)
        val tiger200Isin = TestSymbols.TIGER_200_ISIN
        val kodex200Isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY

        val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
        val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

        assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
        assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

        // When & Then: 발행사 정보 비교
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
    @DisplayName("[활용] ETF의 자산 분류 정보를 확인할 수 있다")
    fun testAssetClassAnalysis() = integrationTest {
        // Given: 여러 ETF의 기본정보 조회 (고정 거래일)
        val tiger200Isin = TestSymbols.TIGER_200_ISIN
        val kodex200Isin = TestSymbols.KODEX_200_ISIN
        val tradeDate = TestSymbols.TRADING_DAY

        val tiger200Info = client.funds.getGeneralInfo(tiger200Isin, tradeDate)
        val kodex200Info = client.funds.getGeneralInfo(kodex200Isin, tradeDate)

        assertNotNull(tiger200Info, "TIGER 200 정보가 있어야 합니다")
        assertNotNull(kodex200Info, "KODEX 200 정보가 있어야 합니다")

        // When & Then: 자산 분류 정보 분석
        println("\n=== ETF 자산 분류 정보 분석 ===")
        println("TIGER 200")
        println("  - 자산 분류: ${tiger200Info?.assetClassName}")
        println("  - 복제 방법: ${tiger200Info?.replicationMethodTypeCode}")
        println("  - 지수 산출 기관: ${tiger200Info?.indexProviderName}")

        println("\nKODEX 200")
        println("  - 자산 분류: ${kodex200Info?.assetClassName}")
        println("  - 복제 방법: ${kodex200Info?.replicationMethodTypeCode}")
        println("  - 지수 산출 기관: ${kodex200Info?.indexProviderName}")

        // 동일한 자산 분류를 추종하는지 확인
        if (tiger200Info?.assetClassName == kodex200Info?.assetClassName) {
            println("\n분석: 두 ETF는 동일한 자산 분류(${tiger200Info?.assetClassName})를 추종합니다")
        } else {
            println("\n분석: 두 ETF는 서로 다른 자산 분류를 추종합니다")
        }
    }
}
