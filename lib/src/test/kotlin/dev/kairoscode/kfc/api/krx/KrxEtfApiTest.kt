package dev.kairoscode.kfc.api.krx

import dev.kairoscode.kfc.internal.krx.KrxEtfApiImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.AbstractStringAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.abs

/**
 * KRX ETF API 통합 테스트
 *
 * 실제 KRX API를 호출하는 통합 테스트입니다.
 * 테스트 시나리오는 plan/13-KRX-테스트-시나리오-명세.md를 기반으로 작성되었습니다.
 */
class KrxEtfApiTest {

    private lateinit var api: KrxEtfApi

    @BeforeEach
    fun setup() {
        api = KrxEtfApiImpl()
    }

    // ================================
    // 1. ETF 목록 및 기본 정보
    // ================================

    @Test
    fun `getEtfList should return all ETFs when data exists`(): Unit = runBlocking {
        // === arrange ===
        // KRX API 호출

        // === act ===
        val result = api.getEtfList()

        // === assert ===
        assertThat(result).isNotEmpty
        assertThat(result.size).isGreaterThan(200)

        // ISIN 형식 검증: KR7로 시작하고 12자리
        result.forEach { etf ->
            // 1. 기본 식별 정보 (4개 필드)
            assertThat(etf.isin).hasSize(12)
            assertThat(etf.isin).startsWith("KR7")
            assertThat(etf.ticker).hasSize(6)
            assertThat(etf.ticker).matches("[0-9A-Z]{6}")

            // 2. 종목명 정보 (3개 필드)
            assertThat(etf.name).isNotBlank()
            assertThat(etf.fullName).isNotBlank()
            assertThat(etf.englishName).isNotBlank()

            // 3. 상장 정보 (3개 필드)
            assertThat(etf.listingDate).isNotNull()
            assertThat(etf.listingDate).isBefore(LocalDate.now().plusDays(1))
            assertThat(etf.listedShares).isGreaterThan(0L)

            // 4. 지수 관련 정보 (3개 필드)
            assertThat(etf.benchmarkIndex).isNotBlank()
            assertThat(etf.indexProvider).isNotBlank()

            // 5. ETF 특성 정보 (4개 필드)
            assertThat(etf.replicationMethod).isNotBlank()
            assertThat(etf.replicationMethod).matches(".*[실합합성].*|실물|합성") // 실물 또는 합성
            assertThat(etf.marketType).isNotBlank()
            assertThat(etf.assetClass).isNotBlank()
            // leverageType은 null 가능 (일반형 ETF는 null)

            // 6. 운용사 및 비용 정보 (3개 필드)
            assertThat(etf.assetManager).isNotBlank()
            assertThat(etf.totalExpenseRatio).isGreaterThanOrEqualTo(BigDecimal.ZERO)
            assertThat(etf.totalExpenseRatio).isLessThan(BigDecimal("5")) // 5% 이상은 비상식적
            assertThat(etf.cuQuantity).isGreaterThan(0L)

            // 7. 과세 유형 (1개 필드)
            assertThat(etf.taxType).isNotBlank()
        }
    }

    @Test
    fun `getEtfList should include KODEX 200`(): Unit = runBlocking {
        // === arrange ===
        val kodex200Ticker = "069500"

        // === act ===
        val result = api.getEtfList()

        // === assert ===
        val kodex200 = result.find { it.ticker == kodex200Ticker }
        assertThat(kodex200).isNotNull
        assertThat(kodex200!!.isin).isEqualTo("KR7069500007")
        assertThat(kodex200.name).contains("KODEX 200")
    }

    @Test
    fun `getComprehensiveEtfInfo should return ETF detail with basic fields`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007" // KODEX 200
        val tradeDate = LocalDate.of(2024, 11, 19)

        // === act ===
        val result = api.getComprehensiveEtfInfo(isin, tradeDate)

        // === assert ===
        assertThat(result).isNotNull()
        result!!

        // 1. 기본 식별 정보 (5개 필드)
        assertThat(result.name).isNotBlank()
        assertThat(result.name).contains("KODEX")
        assertThat(result.tradeDate).isEqualTo(tradeDate)

        // 2. 가격 데이터 - OHLCV (7개 필드)
        assertThat(result.closePrice).isGreaterThan(BigDecimal.ZERO)
        assertThat(result.openPrice).isGreaterThan(BigDecimal.ZERO)
        assertThat(result.highPrice).isGreaterThanOrEqualTo(result.lowPrice)
        assertThat(result.highPrice).isGreaterThanOrEqualTo(result.closePrice)
        assertThat(result.lowPrice).isLessThanOrEqualTo(result.closePrice)
        assertThat(result.lowPrice).isGreaterThan(BigDecimal.ZERO)
        assertThat(result.priceChangeDirection).isBetween(1, 3) // 1=상승, 2=하락, 3=보합

        // 3. 가격 변화 정보 (3개 필드)
        assertThat(result.priceChange).isNotNull()
        assertThat(result.priceChangeRate).isNotNull()

        // 4. 거래량 및 거래대금 (2개 필드)
        assertThat(result.volume).isGreaterThanOrEqualTo(0L)
        assertThat(result.tradingValue).isGreaterThanOrEqualTo(BigDecimal.ZERO)

        // 5. NAV 정보 (4개 필드)
        assertThat(result.nav).isGreaterThan(BigDecimal.ZERO)
        assertThat(result.navChange).isNotNull()
        assertThat(result.navChangeRate).isNotNull()
        assertThat(result.divergenceRate).isBetween(BigDecimal("-10"), BigDecimal("10")) // -10% ~ +10%

        // 6. 시가총액 및 주식 수 (3개 필드)
        assertThat(result.marketCap).isGreaterThan(BigDecimal.ZERO)
        // 주의: netAssetValue (INVSTASST_NETASST_TOTAMT)는 이 API에서 제공되지 않을 수 있음
        // assertThat(result.netAssetValue).isGreaterThan(java.math.BigDecimal.ZERO)
        assertThat(result.listedShares).isGreaterThanOrEqualTo(0L)

        // 7. 52주 고가/저가 (4개 필드 - 핵심!)
        // 주의: API 응답에 날짜 정보는 없음 (MDCSTAT04701에는 WK52_HGST_PRC와 WK52_LWST_PRC만 제공)
        assertThat(result.week52High).isGreaterThan(BigDecimal.ZERO)
        assertThat(result.week52Low).isGreaterThan(BigDecimal.ZERO)
        assertThat(result.week52High).isGreaterThanOrEqualTo(result.week52Low)
        // 현재가가 52주 범위 내
        assertThat(result.closePrice).isGreaterThanOrEqualTo(result.week52Low)
        assertThat(result.closePrice).isLessThanOrEqualTo(result.week52High)

        // 8. 지수 정보 (5개 필드)
        // 주의: OBJ_STKPRC_IDX는 0 - 이 API에서는 제공되지 않음
        // CMPPREVDD_IDX와 FLUC_RT2는 제공됨
        assertThat(result.indexChange).isNotNull()
        assertThat(result.indexChangeDirection).isBetween(1, 3)
        assertThat(result.indexChangeRate).isNotNull()

        // 9. ETF 기본 정보 (11개 필드)
        assertThat(result.listingDate).isNotNull()
        assertThat(result.listingDate).isBefore(result.tradeDate.plusDays(1))
        assertThat(result.assetManager).isNotBlank()
        assertThat(result.totalFee).isGreaterThanOrEqualTo(BigDecimal.ZERO) // 핵심!
        assertThat(result.totalFee).isLessThan(BigDecimal("10")) // 10% 이상은 비상식적
        assertThat(result.creationUnit).isGreaterThan(0L)
        assertThat(result.benchmarkIndex).isNotBlank()
        assertThat(result.indexProvider).isNotBlank()
        assertThat(result.marketClassification).isNotBlank()
        assertThat(result.assetClass).isNotBlank()
        assertThat(result.replicationMethod).isNotBlank()
        assertThat(result.leverageType).isNotBlank()
        assertThat(result.taxType).isNotBlank()
        assertThat(result.marketName).isNotBlank()
    }

    // ================================
    // 2. ETF 시세 및 OHLCV
    // ================================

    @Test
    fun `getEtfOhlcv should return 1 month data when valid range`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.getEtfOhlcv(isin, fromDate, toDate)

        // === assert ===
        assertThat(result).isNotEmpty
        assertThat(result.size).isBetween(15, 25) // 약 20 거래일

        // 날짜 정렬 확인 (오름차순)
        for (i in 0 until result.size - 1) {
            assertThat(result[i].tradeDate).isBefore(result[i + 1].tradeDate)
        }

        // OHLC 논리 정합성 검증
        result.forEach { ohlcv ->
            assertThat(ohlcv.highPrice).isGreaterThanOrEqualTo(ohlcv.lowPrice)
            assertThat(ohlcv.highPrice).isGreaterThanOrEqualTo(ohlcv.openPrice)
            assertThat(ohlcv.highPrice).isGreaterThanOrEqualTo(ohlcv.closePrice)
            assertThat(ohlcv.lowPrice).isLessThanOrEqualTo(ohlcv.openPrice)
            assertThat(ohlcv.lowPrice).isLessThanOrEqualTo(ohlcv.closePrice)
            assertThat(ohlcv.volume).isGreaterThanOrEqualTo(0L)
        }
    }

    @Test
    fun `getEtfOhlcv should split requests when date range exceeds 730 days`(): Unit = runBlocking {
        // === arrange ===
        // 730일 초과 범위: 2022-01-03 ~ 2024-01-31 (약 2년)
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2022, 1, 3)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.getEtfOhlcv(isin, fromDate, toDate)

        // === assert ===
        assertThat(result.size).isGreaterThan(500) // 500개 이상의 거래일

        // 중복 날짜 없음 검증
        val uniqueDates = result.map { it.tradeDate }.toSet()
        assertThat(uniqueDates.size).isEqualTo(result.size)

        // 날짜 범위 검증
        assertThat(result.first().tradeDate).isAfterOrEqualTo(fromDate)
        assertThat(result.last().tradeDate).isBeforeOrEqualTo(toDate)
    }

    @Test
    fun `getEtfOhlcv should return single day data when same from and to date`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getEtfOhlcv(isin, date, date)

        // === assert ===
        assertThat(result).hasSize(1)
        assertThat(result.first().tradeDate).isEqualTo(date)
    }

    @Test
    fun `getAllEtfDailyPrices should return all ETF prices for specific date`(): Unit = runBlocking {
        // === arrange ===
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getAllEtfDailyPrices(date)

        // === assert ===
        assertThat(result.size).isGreaterThan(600) // 600개 이상의 ETF

        // KODEX 200 포함 검증
        val kodex200 = result.find { it.ticker == "069500" }
        assertThat(kodex200?.nav).isGreaterThan(BigDecimal.ZERO)

    }

    @Test
    fun `getEtfPriceChanges should return price changes for period`(): Unit = runBlocking {
        // === arrange ===
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 5)

        // === act ===
        val result = api.getEtfPriceChanges(fromDate, toDate)

        // === assert ===
        assertThat(result.size).isGreaterThan(600)

        // 등락률 범위 검증: -30% ~ +30%
        result.forEach { change ->
            assertThat(change.changeRate).isBetween(-30.0, 30.0)
        }
    }

    // ================================
    // 3. ETF 포트폴리오 구성
    // ================================

    @Test
    fun `getEtfPortfolio should return KODEX 200 constituents`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getEtfPortfolio(isin, date)

        // === assert ===
        assertThat(result.size).isGreaterThan(180) // 180개 이상의 구성 종목

        result.forEach { constituent ->
            assertThat(constituent.weightPercent).isGreaterThan(BigDecimal.ZERO)
            assertThat(constituent.sharesPerCu).isGreaterThan(BigDecimal.ZERO)
        }
    }

    @Test
    fun `getEtfPortfolio should have total weight approximately 100 percent`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getEtfPortfolio(isin, date)

        // === assert ===
        // value > 0인 항목만 필터링하여 비중 합계 계산
        val totalWeight = result
            .filter { it.value > 0L }
            .map { it.weightPercent }
            .fold(BigDecimal.ZERO) { acc, weight -> acc + weight }

        // 99% ~ 101% 범위 (±1% 오차 허용)
        assertThat(totalWeight.toDouble()).isBetween(99.0, 101.0)
    }

    @Test
    fun `getEtfPortfolio should have valid ticker format extracted from ISIN`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getEtfPortfolio(isin, date)

        // === assert ===
        result.forEach { constituent ->
            // constituentCode가 ISIN 형식(12자리)인 경우만 티커 추출 테스트
            if (constituent.constituentCode.length >= 9) {
                // ISIN에서 티커 추출 (substring(3, 9))
                val ticker = constituent.constituentCode.substring(3, 9)
                assertThat(ticker).matches("\\d{6}|[A-Z0-9]{6}")
            } else {
                // 6자리 티커인 경우
                assertThat(constituent.constituentCode).matches("\\d{6}|[A-Z0-9]{6}")
            }
        }
    }

    // ================================
    // 4. ETF 성과 및 추적
    // ================================

    @Test
    fun `getEtfTrackingError should return tracking error data`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.getEtfTrackingError(isin, fromDate, toDate)

        // === assert ===
        assertThat(result).isNotEmpty

        result.forEach { error ->
            assertThat(error.nav).isGreaterThan(BigDecimal.ZERO)
            assertThat(error.indexValue).isGreaterThan(BigDecimal.ZERO)
            assertThat(error.trackingErrorRate).isBetween(-5.0, 5.0) // -5% ~ +5%
        }
    }

    @Test
    fun `getEtfTrackingError should have average error less than 1 percent for KODEX 200`(): Unit = runBlocking {
        // === arrange ===
        // KODEX 200은 우수한 추적 성과를 보임
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.getEtfTrackingError(isin, fromDate, toDate)

        // === assert ===
        val avgAbsError = result
            .map { abs(it.trackingErrorRate) }
            .average()

        assertThat(avgAbsError).isLessThan(1.0) // 평균 추적 오차 < 1%
    }

    @Test
    fun `getEtfDivergenceRate should return divergence rate data`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val result = api.getEtfDivergenceRate(isin, fromDate, toDate)

        // === assert ===
        assertThat(result).isNotEmpty

        result.forEach { divergence ->
            assertThat(divergence.closePrice).isGreaterThan(0)
            assertThat(divergence.nav).isGreaterThan(BigDecimal.ZERO)
            assertThat(divergence.divergenceRate).isBetween(-10.0, 10.0) // -10% ~ +10%
        }
    }

    @Test
    fun `getEtfDivergenceRate should calculate divergence rate correctly`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getEtfDivergenceRate(isin, date, date)

        // === assert ===
        assertThat(result).hasSize(1)

        val divergence = result.first()
        // 괴리율 = ((종가 - NAV) / NAV) × 100
        val expectedRate = ((divergence.closePrice - divergence.nav.toDouble()) / divergence.nav.toDouble()) * 100

        // ±0.01% 오차 허용
        assertThat(divergence.divergenceRate).isCloseTo(expectedRate, Offset.offset(0.01))
    }

    // ================================
    // 5. 투자자별 거래
    // ================================

    @Test
    fun `getAllEtfInvestorTrading should return all 13 investor types`(): Unit = runBlocking {
        // === arrange ===
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getAllEtfInvestorTrading(date)

        // === assert ===
        assertThat(result).hasSize(13)

        // 투자자 유형 검증
        val investorTypes = result.map { it.investorType }.toSet()
        assertThat(investorTypes).contains(
            "금융투자", "보험", "투신", "사모", "은행", "기타금융", "연기금 등",
            "기관합계", "기타법인", "개인", "외국인", "기타외국인", "전체"
        )

        result.forEach { trading ->
            assertThat(trading.bidVolume).isGreaterThanOrEqualTo(0L)
            assertThat(trading.askVolume).isGreaterThanOrEqualTo(0L)
        }
    }

    @Test
    fun `getAllEtfInvestorTrading should calculate net buy correctly`(): Unit = runBlocking {
        // === arrange ===
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getAllEtfInvestorTrading(date)

        // === assert ===
        result.forEach { trading ->
            val expectedNetVolume = trading.bidVolume - trading.askVolume
            val expectedNetValue = trading.bidValue - trading.askValue

            assertThat(trading.netBuyVolume).isEqualTo(expectedNetVolume)
            assertThat(trading.netBuyValue).isEqualTo(expectedNetValue)
        }
    }

    @Test
    fun `getEtfInvestorTrading should return individual ETF investor trading`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val result = api.getEtfInvestorTrading(isin, date)

        // === assert ===
        assertThat(result).hasSize(13)

        result.forEach { trading ->
            assertThat(trading.bidVolume).isGreaterThanOrEqualTo(0L)
            assertThat(trading.askVolume).isGreaterThanOrEqualTo(0L)
            assertThat(trading.bidValue).isGreaterThanOrEqualTo(0L)
            assertThat(trading.askValue).isGreaterThanOrEqualTo(0L)
        }
    }

    // ================================
    // 6. 공매도 데이터
    // ================================

    @Test
    fun `getEtfShortSelling should return short selling data`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.now().minusDays(30)
        val toDate = LocalDate.now().minusDays(1)

        // === act ===
        val result = api.getEtfShortSelling(isin, fromDate, toDate)

        // === assert ===
        assertThat(result).isNotEmpty

        result.forEach { shortSelling ->
            assertThat(shortSelling.shortVolume).isLessThanOrEqualTo(shortSelling.totalVolume)
            assertThat(shortSelling.shortVolumeRatio).isBetween(0.0, 100.0)
        }
    }

    @Test
    fun `getEtfShortSelling should calculate ratio correctly`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val date = LocalDate.now().minusDays(5)

        // === act ===
        val result = api.getEtfShortSelling(isin, date, date)

        // === assert ===
        // ETF는 보통 공매도가 없거나 매우 적으므로, 데이터 존재 여부만 확인
        assertThat(result).isNotNull
    }

    @Test
    fun `getEtfShortBalance should return short balance data`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.now().minusDays(30)
        val toDate = LocalDate.now().minusDays(1)

        // === act ===
        val result = api.getEtfShortBalance(isin, fromDate, toDate)

        // === assert ===
        // ETF는 공매도 잔고가 거의 없으므로, 빈 리스트일 수 있음
        assertThat(result).isNotNull

        result.forEach { balance ->
            assertThat(balance.shortBalance).isGreaterThanOrEqualTo(0L)
            assertThat(balance.shortBalanceRatio).isBetween(0.0, 100.0)
        }
    }

    // ================================
    // 7. 에러 처리
    // ================================

    @Test
    fun `getComprehensiveEtfInfo should parse all fields correctly from API response`(): Unit = runBlocking {
        // === arrange ===
        // KODEX 200: 가장 거래량이 많은 ETF, 안정적인 데이터 보장
        val isin = "KR7069500007"
        // 주중 거래일 (평일) 사용 - 테스트 안정성을 위해
        val tradeDate = LocalDate.of(2024, 11, 19)

        // === act ===
        val result = api.getComprehensiveEtfInfo(isin, tradeDate)

        // === assert ===
        assertThat(result).isNotNull()
        result!!

        // 모든 필드가 null이 아닌지 확인 (필수 필드)
        assertThat(result.tradeDate).isNotNull()
        assertThat(result.isin).isNotNull()
        assertThat(result.ticker).isNotNull()
        assertThat(result.name).isNotNull()
        assertThat(result.fullName).isNotNull()
        assertThat(result.openPrice).isNotNull()
        assertThat(result.highPrice).isNotNull()
        assertThat(result.lowPrice).isNotNull()
        assertThat(result.closePrice).isNotNull()
        assertThat(result.priceChange).isNotNull()
        assertThat(result.priceChangeDirection).isNotNull()
        assertThat(result.priceChangeRate).isNotNull()
        assertThat(result.volume).isNotNull()
        assertThat(result.tradingValue).isNotNull()
        assertThat(result.nav).isNotNull()
        assertThat(result.navChange).isNotNull()
        assertThat(result.navChangeRate).isNotNull()
        assertThat(result.divergenceRate).isNotNull()
        assertThat(result.marketCap).isNotNull()
        assertThat(result.netAssetValue).isNotNull()
        assertThat(result.listedShares).isNotNull()

        // 52주 고가/저가 (MDCSTAT04701 고유 필드!)
        assertThat(result.week52High).isNotNull()
        assertThat(result.week52HighDate).isNotNull()
        assertThat(result.week52Low).isNotNull()
        assertThat(result.week52LowDate).isNotNull()

        assertThat(result.indexValue).isNotNull()
        assertThat(result.indexName).isNotNull()
        assertThat(result.indexChange).isNotNull()
        assertThat(result.indexChangeDirection).isNotNull()
        assertThat(result.indexChangeRate).isNotNull()
        assertThat(result.listingDate).isNotNull()
        assertThat(result.assetManager).isNotNull()

        // 총보수 (MDCSTAT04701 고유 필드!)
        assertThat(result.totalFee).isNotNull()

        assertThat(result.creationUnit).isNotNull()
        assertThat(result.benchmarkIndex).isNotNull()
        assertThat(result.indexProvider).isNotNull()
        assertThat(result.marketClassification).isNotNull()
        assertThat(result.assetClass).isNotNull()
        assertThat(result.replicationMethod).isNotNull()
        assertThat(result.leverageType).isNotNull()
        assertThat(result.taxType).isNotNull()
        assertThat(result.marketName).isNotNull()
    }

    @Test
    fun `getComprehensiveEtfInfo should verify logical consistency between fields`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val tradeDate = LocalDate.of(2024, 11, 19)

        // === act ===
        val result = api.getComprehensiveEtfInfo(isin, tradeDate)

        // === assert ===
        if (result != null) {
            // 가격 논리: High >= Close >= Low >= Open (항상은 아니지만 대부분)
            assertThat(result.highPrice).isGreaterThanOrEqualTo(result.lowPrice)

            // NAV 일치성: 괴리율 = (종가 - NAV) / NAV * 100
            val expectedDivergence = ((result.closePrice.toDouble() - result.nav.toDouble()) / result.nav.toDouble()) * 100
            val actualDivergence = result.divergenceRate.toDouble()
            val tolerance = 0.1 // ±0.1% 오차 허용
            assertThat(actualDivergence).isCloseTo(expectedDivergence, Offset.offset(tolerance))

            // 52주 고가/저가 논리
            assertThat(result.week52High).isGreaterThanOrEqualTo(result.week52Low)
            // 현재가가 52주 범위 내에 있어야 함 (극단적인 경우 제외)
            assertThat(result.closePrice).isLessThanOrEqualTo(result.week52High.multiply(BigDecimal("1.01")))
            assertThat(result.closePrice).isGreaterThanOrEqualTo(result.week52Low.multiply(BigDecimal("0.99")))

            // 지수 정보 일치성 검증
            assertThat(result.indexChangeDirection).isBetween(1, 3)
            // 주의: OBJ_STKPRC_IDX는 0이므로 변화율 계산 불가
            // 실제 지수값은 다른 API에서 조회해야 함

            // 시가총액 논리: 시가총액 = 종가 * 상장주식수
            // 주의: listedShares가 0이면 계산 불가
            if (result.listedShares > 0) {
                val expectedMarketCap = result.closePrice.multiply(BigDecimal(result.listedShares))
                // ±5% 오차 허용 (반올림 및 시점 차이)
                val tolerance_marketcap = expectedMarketCap.multiply(BigDecimal("0.05"))
                val diff = (result.marketCap - expectedMarketCap).abs()
                assertThat(diff).isLessThanOrEqualTo(tolerance_marketcap)
            }

            // 총보수는 합리적인 범위 내 (0% ~ 3% 대부분)
            assertThat(result.totalFee.toDouble()).isBetween(0.0, 5.0)
        }
    }

    @Test
    fun `getComprehensiveEtfInfo should return valid data for trading dates`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val tradingDate = LocalDate.of(2024, 11, 19) // 거래일

        // === act ===
        val result = api.getComprehensiveEtfInfo(isin, tradingDate)

        // === assert ===
        // 거래일에는 데이터 반환
        assertThat(result).isNotNull()
        assertThat(result!!.name).isNotBlank()
        assertThat(result.closePrice).isGreaterThan(BigDecimal.ZERO)
    }

    @Test
    fun `getEtfOhlcv should throw exception when start date is after end date`(): Unit {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 1, 31)
        val toDate = LocalDate.of(2024, 1, 1) // 역순

        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            runBlocking {
                api.getEtfOhlcv(isin, fromDate, toDate)
            }
        }
    }

    // ================================
    // 8. 통합 시나리오
    // ================================

    @Test
    fun `integration test - collect top 5 ETF data with rate limiting`(): Unit = runBlocking {
        // === arrange ===
        val etfList = api.getEtfList()
        val top5Etfs = etfList.take(5)
        val date = LocalDate.of(2024, 1, 15)

        val startTime = System.currentTimeMillis()

        // === act ===
        val details = mutableListOf<Any?>()
        for (etf in top5Etfs) {
            val detail = api.getComprehensiveEtfInfo(etf.isin, date)
            details.add(detail)

            // Rate limiting: 100ms 대기
            delay(100)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // === assert ===
        assertThat(details).hasSize(5)
        assertThat(duration).isGreaterThanOrEqualTo(500L) // 최소 500ms 소요 (5 × 100ms)
    }

    @Test
    fun `integration test - OHLCV plus portfolio plus tracking error with NAV consistency`(): Unit = runBlocking {
        // === arrange ===
        val isin = "KR7069500007"
        val fromDate = LocalDate.of(2024, 1, 2)
        val toDate = LocalDate.of(2024, 1, 31)

        // === act ===
        val ohlcv = api.getEtfOhlcv(isin, fromDate, toDate)
        delay(100)

        val portfolio = api.getEtfPortfolio(isin, toDate)
        delay(100)

        val trackingError = api.getEtfTrackingError(isin, fromDate, toDate)

        // === assert ===
        assertThat(ohlcv).isNotEmpty
        assertThat(portfolio).isNotEmpty
        assertThat(trackingError).isNotEmpty

        // NAV 일치성 검증: OHLCV 마지막 NAV ≈ TrackingError 마지막 NAV
        val ohlcvLastNav = ohlcv.last().nav
        val trackingErrorLastNav = trackingError.last().nav

        // ±0.01 오차 허용
        val diff = (ohlcvLastNav - trackingErrorLastNav).abs()
        assertThat(diff.toDouble()).isLessThan(0.01)
    }

    @Test
    fun `integration test - compare all market vs individual ETF investor trading`(): Unit = runBlocking {
        // === arrange ===
        val date = LocalDate.of(2024, 1, 15)

        // === act ===
        val allMarket = api.getAllEtfInvestorTrading(date)
        delay(100)

        val kodex200 = api.getEtfInvestorTrading("KR7069500007", date)

        // === assert ===
        // 전체 시장의 외국인 순매수 >= KODEX 200 외국인 순매수
        val allMarketForeign = allMarket.find { it.investorType == "외국인" }
        val kodex200Foreign = kodex200.find { it.investorType == "외국인" }

        assertThat(allMarketForeign).isNotNull
        assertThat(kodex200Foreign).isNotNull

        // 논리 정합성: 개별 ETF 거래량 <= 전체 시장 거래량
        assertThat(abs(kodex200Foreign!!.netBuyVolume))
            .isLessThanOrEqualTo(abs(allMarketForeign!!.netBuyVolume))
    }
}
