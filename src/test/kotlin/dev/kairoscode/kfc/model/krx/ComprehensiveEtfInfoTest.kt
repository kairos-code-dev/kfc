package dev.kairoscode.kfc.model.krx

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDate

class ComprehensiveEtfInfoTest {

    @Test
    fun `fromRaw - 정상적인 데이터로 객체 생성`() {
        // Given: 실제 KRX API 응답과 유사한 데이터
        val raw = mapOf(
            "TRD_DD" to "2024/01/02",
            "ISU_CD" to "KR7152100004",
            "ISU_SRT_CD" to "152100",
            "ISU_ABBRV" to "ARIRANG 200",
            "ISU_NM" to "ARIRANG 200 증권상장지수투자신탁[주식]",

            "TDD_OPNPRC" to "42,075",
            "TDD_HGPRC" to "43,250",
            "TDD_LWPRC" to "41,900",
            "TDD_CLSPRC" to "42,965",
            "CMPPREVDD_PRC" to "1,080",
            "FLUC_TP_CD" to "1",
            "FLUC_RT" to "2.58",

            "ACC_TRDVOL" to "192,061",
            "ACC_TRDVAL" to "8,222,510,755",

            "LST_NAV" to "43,079.14",
            "NAV_CHG_VAL" to "1,095.82",
            "NAV_CHG_RT" to "2.61",
            "DIVRG_RT" to "-0.27",

            "MKTCAP" to "850,707,000,000",
            "INVSTASST_NETASST_TOTAMT" to "852,966,972,000",
            "LIST_SHRS" to "19,800,000",

            "WK52_HGST_PRC" to "45,230",
            "WK52_HGPR_DD" to "2023/11/28",
            "WK52_LWST_PRC" to "38,125",
            "WK52_LWPR_DD" to "2023/03/15",

            "OBJ_STKPRC_IDX" to "421.35",
            "IDX_IND_NM" to "코스피 200",
            "CMPPREVDD_IDX" to "10.85",
            "FLUC_TP_CD1" to "1",
            "IDX_FLUC_RT" to "2.64",

            "LIST_DD" to "2008/02/01",
            "COM_ABBRV" to "한국투자신탁운용",
            "ETF_TOT_FEE" to "0.15",
            "CU_QTY" to "100,000",
            "ETF_OBJ_IDX_NM" to "코스피 200 지수",
            "IDX_CALC_INST_NM1" to "KRX",
            "IDX_MKT_CLSS_NM" to "국내",
            "IDX_ASST_CLSS_NM" to "주식",
            "ETF_REPLICA_METHD_TP_CD" to "실물",
            "IDX_CALC_INST_NM2" to "일반",
            "TAX_TP_CD" to "배당소득세(보유기간과세)",
            "MKT_NM" to "KOSPI"
        )

        // When: fromRaw로 객체 생성
        val info = ComprehensiveEtfInfo.fromRaw(raw)

        // Then: 기본 식별 정보 검증
        assertEquals(LocalDate.of(2024, 1, 2), info.tradeDate)
        assertEquals("KR7152100004", info.isin)
        assertEquals("152100", info.ticker)
        assertEquals("ARIRANG 200", info.name)
        assertEquals("ARIRANG 200 증권상장지수투자신탁[주식]", info.fullName)

        // OHLCV 검증
        assertEquals(BigDecimal("42075.00"), info.openPrice)
        assertEquals(BigDecimal("43250.00"), info.highPrice)
        assertEquals(BigDecimal("41900.00"), info.lowPrice)
        assertEquals(BigDecimal("42965.00"), info.closePrice)
        assertEquals(192061L, info.volume)

        // NAV 검증
        assertEquals(BigDecimal("43079.14"), info.nav)
        assertEquals(BigDecimal("-0.2700"), info.divergenceRate)

        // 52주 고가/저가 검증 (핵심 필드)
        assertEquals(BigDecimal("45230.00"), info.week52High)
        assertEquals(BigDecimal("38125.00"), info.week52Low)

        // 총 보수 검증 (핵심 필드)
        assertEquals(BigDecimal("0.1500"), info.totalFee)

        // 자산구분 검증
        assertEquals("주식", info.assetClass)
    }

    @Test
    fun `fromRaw - 빈 값 처리 테스트`() {
        // Given: 일부 필드가 "-" 또는 빈 문자열인 데이터
        val raw = mapOf(
            "TRD_DD" to "-",
            "ISU_CD" to "KR7152100004",
            "ISU_SRT_CD" to "152100",
            "ISU_ABBRV" to "TEST ETF",
            "ISU_NM" to "TEST ETF",

            "TDD_OPNPRC" to "-",
            "TDD_HGPRC" to "",
            "TDD_LWPRC" to "-",
            "TDD_CLSPRC" to "10,000",
            "CMPPREVDD_PRC" to "-",
            "FLUC_TP_CD" to "3",
            "FLUC_RT" to "-",

            "ACC_TRDVOL" to "-",
            "ACC_TRDVAL" to "",

            "LST_NAV" to "10,000",
            "NAV_CHG_VAL" to "-",
            "NAV_CHG_RT" to "",
            "DIVRG_RT" to "-",

            "MKTCAP" to "-",
            "INVSTASST_NETASST_TOTAMT" to "",
            "LIST_SHRS" to "-",

            "WK52_HGST_PRC" to "-",
            "WK52_HGPR_DD" to "-",
            "WK52_LWST_PRC" to "",
            "WK52_LWPR_DD" to "",

            "OBJ_STKPRC_IDX" to "-",
            "IDX_IND_NM" to "",
            "CMPPREVDD_IDX" to "-",
            "FLUC_TP_CD1" to "",
            "IDX_FLUC_RT" to "-",

            "LIST_DD" to "2020/01/01",
            "COM_ABBRV" to "운용사",
            "ETF_TOT_FEE" to "0.5",
            "CU_QTY" to "-",
            "ETF_OBJ_IDX_NM" to "",
            "IDX_CALC_INST_NM1" to "",
            "IDX_MKT_CLSS_NM" to "",
            "IDX_ASST_CLSS_NM" to "",
            "ETF_REPLICA_METHD_TP_CD" to "",
            "IDX_CALC_INST_NM2" to "",
            "TAX_TP_CD" to "",
            "MKT_NM" to ""
        )

        // When: fromRaw로 객체 생성
        val info = ComprehensiveEtfInfo.fromRaw(raw)

        // Then: 빈 값들이 적절히 처리되었는지 검증
        assertEquals(LocalDate.MIN, info.tradeDate)
        assertEquals(BigDecimal.ZERO.setScale(2), info.openPrice)
        assertEquals(BigDecimal.ZERO.setScale(2), info.highPrice)
        assertEquals(0L, info.volume)
        assertEquals(BigDecimal.ZERO, info.marketCap)
    }

    @Test
    fun `isNear52WeekHigh - 52주 고가 근처 판별`() {
        // Given: 종가가 52주 고가의 96%인 경우
        val info = createTestInfo(
            closePrice = BigDecimal("48000.00"),
            week52High = BigDecimal("50000.00")
        )

        // When & Then
        assertTrue(info.isNear52WeekHigh())
    }

    @Test
    fun `isNear52WeekHigh - 52주 고가 근처가 아님`() {
        // Given: 종가가 52주 고가의 90%인 경우
        val info = createTestInfo(
            closePrice = BigDecimal("45000.00"),
            week52High = BigDecimal("50000.00")
        )

        // When & Then
        assertFalse(info.isNear52WeekHigh())
    }

    @Test
    fun `isNear52WeekLow - 52주 저가 근처 판별`() {
        // Given: 종가가 52주 저가의 103%인 경우
        val info = createTestInfo(
            closePrice = BigDecimal("10300.00"),
            week52Low = BigDecimal("10000.00")
        )

        // When & Then
        assertTrue(info.isNear52WeekLow())
    }

    @Test
    fun `isNear52WeekLow - 52주 저가 근처가 아님`() {
        // Given: 종가가 52주 저가의 110%인 경우
        val info = createTestInfo(
            closePrice = BigDecimal("11000.00"),
            week52Low = BigDecimal("10000.00")
        )

        // When & Then
        assertFalse(info.isNear52WeekLow())
    }

    @Test
    fun `hasExcessiveDivergence - 과도한 괴리율`() {
        // Given: 괴리율이 1.5%인 경우
        val info = createTestInfo(divergenceRate = BigDecimal("1.5000"))

        // When & Then
        assertTrue(info.hasExcessiveDivergence())
    }

    @Test
    fun `hasExcessiveDivergence - 음수 괴리율도 체크`() {
        // Given: 괴리율이 -1.5%인 경우
        val info = createTestInfo(divergenceRate = BigDecimal("-1.5000"))

        // When & Then
        assertTrue(info.hasExcessiveDivergence())
    }

    @Test
    fun `hasExcessiveDivergence - 정상 괴리율`() {
        // Given: 괴리율이 0.5%인 경우
        val info = createTestInfo(divergenceRate = BigDecimal("0.5000"))

        // When & Then
        assertFalse(info.hasExcessiveDivergence())
    }

    @Test
    fun `hasLowFee - 저렴한 보수`() {
        // Given: 총 보수가 0.15%인 경우
        val info = createTestInfo(totalFee = BigDecimal("0.1500"))

        // When & Then
        assertTrue(info.hasLowFee())
    }

    @Test
    fun `hasLowFee - 높은 보수`() {
        // Given: 총 보수가 0.5%인 경우
        val info = createTestInfo(totalFee = BigDecimal("0.5000"))

        // When & Then
        assertFalse(info.hasLowFee())
    }

    // 테스트용 헬퍼 함수
    private fun createTestInfo(
        closePrice: BigDecimal = BigDecimal("10000.00"),
        week52High: BigDecimal = BigDecimal("12000.00"),
        week52Low: BigDecimal = BigDecimal("8000.00"),
        divergenceRate: BigDecimal = BigDecimal("0.0000"),
        totalFee: BigDecimal = BigDecimal("0.3000")
    ): ComprehensiveEtfInfo {
        return ComprehensiveEtfInfo(
            tradeDate = LocalDate.now(),
            isin = "TEST",
            ticker = "000000",
            name = "TEST ETF",
            fullName = "TEST ETF",
            openPrice = closePrice,
            highPrice = closePrice,
            lowPrice = closePrice,
            closePrice = closePrice,
            priceChange = BigDecimal.ZERO,
            priceChangeDirection = 3,
            priceChangeRate = BigDecimal.ZERO,
            volume = 0L,
            tradingValue = BigDecimal.ZERO,
            nav = closePrice,
            navChange = BigDecimal.ZERO,
            navChangeRate = BigDecimal.ZERO,
            divergenceRate = divergenceRate,
            marketCap = BigDecimal.ZERO,
            netAssetValue = BigDecimal.ZERO,
            listedShares = 0L,
            week52High = week52High,
            week52HighDate = LocalDate.now(),
            week52Low = week52Low,
            week52LowDate = LocalDate.now(),
            indexValue = BigDecimal.ZERO,
            indexName = "",
            indexChange = BigDecimal.ZERO,
            indexChangeDirection = 3,
            indexChangeRate = BigDecimal.ZERO,
            listingDate = LocalDate.now(),
            assetManager = "",
            totalFee = totalFee,
            creationUnit = 0L,
            benchmarkIndex = "",
            indexProvider = "",
            marketClassification = "",
            assetClass = "",
            replicationMethod = "",
            leverageType = "",
            taxType = "",
            marketName = ""
        )
    }
}
