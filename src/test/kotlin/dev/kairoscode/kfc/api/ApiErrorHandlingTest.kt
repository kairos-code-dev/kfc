package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.api.FundsApi
import dev.kairoscode.kfc.api.CorpApi
import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import dev.kairoscode.kfc.internal.FundsApiImpl
import dev.kairoscode.kfc.internal.CorpApiImpl
import dev.kairoscode.kfc.utils.UnitTestBase
import dev.kairoscode.kfc.model.FundType
import dev.kairoscode.kfc.model.krx.*
import dev.kairoscode.kfc.model.opendart.*
import dev.kairoscode.kfc.model.naver.NaverEtfOhlcv
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat

/**
 * API 예외 처리 테스트
 *
 * 입력 파라미터 검증으로 올바른 예외가 발생하는지 검증합니다.
 */
@DisplayName("API Error Handling")
class ApiErrorHandlingTest : UnitTestBase() {

    private fun createDummyKrxFundsApi() = object : dev.kairoscode.kfc.api.krx.KrxFundsApi {
        override suspend fun getEtfList(type: FundType?) = emptyList<EtfListItem>()
        override suspend fun getDetailedInfo(isin: String, tradeDate: LocalDate) = null
        override suspend fun getAllEtfDailyPrices(date: LocalDate) = emptyList<EtfDailyPrice>()
        override suspend fun getEtfOhlcv(isin: String, fromDate: LocalDate, toDate: LocalDate) = emptyList<EtfOhlcv>()
        override suspend fun getEtfPriceChanges(fromDate: LocalDate, toDate: LocalDate) = emptyList<EtfPriceChange>()
        override suspend fun getEtfPortfolio(isin: String, date: LocalDate) = emptyList<PortfolioConstituent>()
        override suspend fun getEtfTrackingError(isin: String, fromDate: LocalDate, toDate: LocalDate) = emptyList<TrackingError>()
        override suspend fun getEtfDivergenceRate(isin: String, fromDate: LocalDate, toDate: LocalDate) = emptyList<DivergenceRate>()
        override suspend fun getAllEtfInvestorTrading(date: LocalDate) = emptyList<InvestorTrading>()
        override suspend fun getAllEtfInvestorTradingByPeriod(fromDate: LocalDate, toDate: LocalDate) = emptyList<InvestorTradingByDate>()
        override suspend fun getEtfInvestorTrading(isin: String, date: LocalDate) = emptyList<InvestorTrading>()
        override suspend fun getEtfInvestorTradingByPeriod(isin: String, fromDate: LocalDate, toDate: LocalDate) = emptyList<InvestorTradingByDate>()
        override suspend fun getEtfShortSelling(isin: String, fromDate: LocalDate, toDate: LocalDate, type: FundType) = emptyList<ShortSelling>()
        override suspend fun getEtfShortBalance(isin: String, fromDate: LocalDate, toDate: LocalDate, type: FundType) = emptyList<ShortBalance>()
        override suspend fun getEtfIntradayBars(isin: String, tradeDate: LocalDate) = emptyList<EtfIntradayBar>()
        override suspend fun getEtfRecentDaily(isin: String, tradeDate: LocalDate) = emptyList<EtfRecentDaily>()
        override suspend fun getEtfGeneralInfo(isin: String, tradeDate: LocalDate): EtfGeneralInfo? = null
        override suspend fun getEtfPortfolioTop10(isin: String, date: LocalDate) = emptyList<PortfolioTopItem>()
    }

    private fun createDummyNaverFundsApi() = object : dev.kairoscode.kfc.api.naver.NaverFundsApi {
        override suspend fun getAdjustedOhlcv(ticker: String, fromDate: LocalDate, toDate: LocalDate) = emptyList<NaverEtfOhlcv>()
    }

    private fun createDummyOpenDartApi() = object : dev.kairoscode.kfc.api.opendart.OpenDartApi {
        override suspend fun getCorpCodeList() = emptyList<CorpCode>()
        override suspend fun getDividendInfo(corpCode: String, year: Int, reportCode: String) = emptyList<DividendInfo>()
        override suspend fun getStockSplitInfo(corpCode: String, year: Int, reportCode: String) = emptyList<StockSplitInfo>()
        override suspend fun searchDisclosures(corpCode: String?, startDate: LocalDate, endDate: LocalDate, pageNo: Int, pageCount: Int) = emptyList<DisclosureItem>()
    }

    @Test
    @DisplayName("ISIN 형식 검증 실패 - 너무 짧은 코드")
    fun `should throw exception for invalid ISIN format - too short`() = unitTest {
        // Given
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), createDummyNaverFundsApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getDetailedInfo("123", LocalDate.now())
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
        assertThat(exception.message).contains("ISIN")
    }

    @Test
    @DisplayName("ISIN 형식 검증 실패 - 잘못된 프리픽스")
    fun `should throw exception for invalid ISIN format - wrong prefix`() = unitTest {
        // Given
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), createDummyNaverFundsApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getDetailedInfo("US0000000000", LocalDate.now())
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
        assertThat(exception.message).contains("KR7")
    }

    @Test
    @DisplayName("ISIN 형식 검증 실패 - 비숫자 문자")
    fun `should throw exception for invalid ISIN format - non-numeric`() = unitTest {
        // Given
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), createDummyNaverFundsApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getDetailedInfo("KR7@@@@@@@@@@@", LocalDate.now())
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("ISIN 공백 입력")
    fun `should throw exception for blank ISIN`() = unitTest {
        // Given
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), createDummyNaverFundsApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getDetailedInfo("   ", LocalDate.now())
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("날짜 범위 검증 - fromDate > toDate")
    fun `should throw exception when fromDate is after toDate`() = unitTest {
        // Given
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), createDummyNaverFundsApi())
        initClient()
        val fromDate = LocalDate.of(2024, 12, 31)
        val toDate = LocalDate.of(2024, 1, 1)

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getOhlcv("KR7152100004", fromDate, toDate)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_DATE_RANGE)
    }

    @Test
    @DisplayName("날짜 검증 - 미래 날짜")
    fun `should throw exception for future date`() = unitTest {
        // Given
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), createDummyNaverFundsApi())
        initClient()
        val futureDate = LocalDate.now().plusDays(1)

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getOhlcv("KR7152100004", futureDate, futureDate)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("Corp API - corpCode 형식 검증 - 너무 짧음")
    fun `should throw exception for invalid corpCode format - too short`() = unitTest {
        // Given
        mockCorpApi = CorpApiImpl(createDummyOpenDartApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.corp?.getDividendInfo("123", 2024)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("Corp API - corpCode 형식 검증 - 비숫자")
    fun `should throw exception for invalid corpCode format - non-numeric`() = unitTest {
        // Given
        mockCorpApi = CorpApiImpl(createDummyOpenDartApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.corp?.getDividendInfo("0012638abc", 2024)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("Corp API - corpCode 공백 입력")
    fun `should throw exception for blank corpCode`() = unitTest {
        // Given
        mockCorpApi = CorpApiImpl(createDummyOpenDartApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.corp?.getDividendInfo("   ", 2024)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("Corp API - year 검증 - 미래 년도")
    fun `should throw exception when year is in the future`() = unitTest {
        // Given
        mockCorpApi = CorpApiImpl(createDummyOpenDartApi())
        initClient()
        val futureYear = LocalDate.now().year + 1

        // When & Then
        var exception: KfcException? = null
        try {
            client.corp?.getDividendInfo("00126380", futureYear)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("Corp API - year 검증 - 너무 오래된 해")
    fun `should throw exception when year is too old`() = unitTest {
        // Given
        mockCorpApi = CorpApiImpl(createDummyOpenDartApi())
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.corp?.getDividendInfo("00126380", 1800)
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("Naver API - HTTP 에러 응답")
    fun `should throw exception for Naver API HTTP error`() = unitTest {
        // Given
        val errorNaverApi = object : dev.kairoscode.kfc.api.naver.NaverFundsApi {
            override suspend fun getAdjustedOhlcv(ticker: String, fromDate: LocalDate, toDate: LocalDate): List<NaverEtfOhlcv> {
                throw KfcException(ErrorCode.HTTP_ERROR_RESPONSE)
            }
        }
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), errorNaverApi)
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getAdjustedOhlcv("069500", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.HTTP_ERROR_RESPONSE)
    }

    @Test
    @DisplayName("Naver API - XML 파싱 에러")
    fun `should throw exception for Naver API XML parse error`() = unitTest {
        // Given
        val errorNaverApi = object : dev.kairoscode.kfc.api.naver.NaverFundsApi {
            override suspend fun getAdjustedOhlcv(ticker: String, fromDate: LocalDate, toDate: LocalDate): List<NaverEtfOhlcv> {
                throw KfcException(ErrorCode.XML_PARSE_ERROR)
            }
        }
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), errorNaverApi)
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getAdjustedOhlcv("069500", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.XML_PARSE_ERROR)
    }

    @Test
    @DisplayName("Naver API - 네트워크 연결 에러")
    fun `should throw exception for Naver API network connection error`() = unitTest {
        // Given
        val errorNaverApi = object : dev.kairoscode.kfc.api.naver.NaverFundsApi {
            override suspend fun getAdjustedOhlcv(ticker: String, fromDate: LocalDate, toDate: LocalDate): List<NaverEtfOhlcv> {
                throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED)
            }
        }
        mockFundsApi = FundsApiImpl(createDummyKrxFundsApi(), errorNaverApi)
        initClient()

        // When & Then
        var exception: KfcException? = null
        try {
            client.funds.getAdjustedOhlcv("069500", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
        } catch (e: KfcException) {
            exception = e
        }

        assertThat(exception).isNotNull()
        assertThat(exception!!.errorCode).isEqualTo(ErrorCode.NETWORK_CONNECTION_FAILED)
    }
}
