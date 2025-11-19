package dev.kairoscode.kfc.model.krx

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * EtfListItem 데이터 모델 테스트
 */
class EtfListItemTest {

    @Test
    fun `should create EtfListItem with all fields`() {
        // === arrange & act ===
        val etf = EtfListItem(
            isin = "KR7069500007",
            ticker = "069500",
            name = "KODEX 200",
            fullName = "KODEX 200 증권상장지수투자신탁[주식]",
            englishName = "KODEX 200",
            listingDate = LocalDate.of(2002, 10, 14),
            benchmarkIndex = "KOSPI 200",
            indexProvider = "KRX",
            leverageType = null,
            replicationMethod = "실물",
            marketType = "국내",
            assetClass = "주식",
            listedShares = 192061000L,
            assetManager = "삼성자산운용",
            cuQuantity = 10000L,
            totalExpenseRatio = BigDecimal("0.15"),
            taxType = "과세"
        )

        // === assert ===
        assertThat(etf.isin).isEqualTo("KR7069500007")
        assertThat(etf.ticker).isEqualTo("069500")
        assertThat(etf.name).isEqualTo("KODEX 200")
        assertThat(etf.benchmarkIndex).isEqualTo("KOSPI 200")
        assertThat(etf.leverageType).isNull()
    }

    @Test
    fun `should validate ISIN format`() {
        // === arrange ===
        val etf = EtfListItem(
            isin = "KR7069500007",
            ticker = "069500",
            name = "KODEX 200",
            fullName = "KODEX 200",
            englishName = "KODEX 200",
            listingDate = LocalDate.of(2002, 10, 14),
            benchmarkIndex = "KOSPI 200",
            indexProvider = "KRX",
            leverageType = null,
            replicationMethod = "실물",
            marketType = "국내",
            assetClass = "주식",
            listedShares = 192061000L,
            assetManager = "삼성자산운용",
            cuQuantity = 10000L,
            totalExpenseRatio = BigDecimal("0.15"),
            taxType = "과세"
        )

        // === assert ===
        assertThat(etf.isin).hasSize(12)
        assertThat(etf.isin).startsWith("KR7")
        assertThat(etf.isin).matches("KR7\\d{6}\\d{3}")
    }

    @Test
    fun `should validate ticker format`() {
        // === arrange ===
        val etf = EtfListItem(
            isin = "KR7069500007",
            ticker = "069500",
            name = "KODEX 200",
            fullName = "KODEX 200",
            englishName = "KODEX 200",
            listingDate = LocalDate.of(2002, 10, 14),
            benchmarkIndex = "KOSPI 200",
            indexProvider = "KRX",
            leverageType = null,
            replicationMethod = "실물",
            marketType = "국내",
            assetClass = "주식",
            listedShares = 192061000L,
            assetManager = "삼성자산운용",
            cuQuantity = 10000L,
            totalExpenseRatio = BigDecimal("0.15"),
            taxType = "과세"
        )

        // === assert ===
        assertThat(etf.ticker).hasSize(6)
        assertThat(etf.ticker).matches("\\d{6}")
    }

    @Test
    fun `should handle leveraged ETF type`() {
        // === arrange & act ===
        val leveragedEtf = EtfListItem(
            isin = "KR7122630009",
            ticker = "122630",
            name = "KODEX 레버리지",
            fullName = "KODEX 레버리지",
            englishName = "KODEX Leverage",
            listingDate = LocalDate.of(2010, 7, 1),
            benchmarkIndex = "KOSPI 200",
            indexProvider = "KRX",
            leverageType = "2X 레버리지 (2)",
            replicationMethod = "파생",
            marketType = "국내",
            assetClass = "주식",
            listedShares = 100000000L,
            assetManager = "삼성자산운용",
            cuQuantity = 10000L,
            totalExpenseRatio = BigDecimal("0.64"),
            taxType = "과세"
        )

        // === assert ===
        assertThat(leveragedEtf.leverageType).isNotNull()
        assertThat(leveragedEtf.leverageType).contains("레버리지")
    }

    @Test
    fun `should compare two ETFs by ISIN`() {
        // === arrange ===
        val etf1 = createKodex200()
        val etf2 = createKodex200()

        // === act & assert ===
        assertThat(etf1.isin).isEqualTo(etf2.isin)
        assertThat(etf1).isEqualTo(etf2)
    }

    @Test
    fun `should have valid listing date before today`() {
        // === arrange ===
        val etf = createKodex200()

        // === assert ===
        assertThat(etf.listingDate).isBefore(LocalDate.now())
    }

    @Test
    fun `should have positive total expense ratio`() {
        // === arrange ===
        val etf = createKodex200()

        // === assert ===
        assertThat(etf.totalExpenseRatio).isGreaterThanOrEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `should have positive listed shares`() {
        // === arrange ===
        val etf = createKodex200()

        // === assert ===
        assertThat(etf.listedShares).isGreaterThan(0L)
    }

    // 헬퍼 함수
    private fun createKodex200() = EtfListItem(
        isin = "KR7069500007",
        ticker = "069500",
        name = "KODEX 200",
        fullName = "KODEX 200 증권상장지수투자신탁[주식]",
        englishName = "KODEX 200",
        listingDate = LocalDate.of(2002, 10, 14),
        benchmarkIndex = "KOSPI 200",
        indexProvider = "KRX",
        leverageType = null,
        replicationMethod = "실물",
        marketType = "국내",
        assetClass = "주식",
        listedShares = 192061000L,
        assetManager = "삼성자산운용",
        cuQuantity = 10000L,
        totalExpenseRatio = BigDecimal("0.15"),
        taxType = "과세"
    )
}
