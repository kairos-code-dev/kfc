package dev.kairoscode.kfc.funds.mock

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import dev.kairoscode.kfc.funds.FundsApi
import dev.kairoscode.kfc.model.FundType
import dev.kairoscode.kfc.funds.internal.krx.model.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Unit Test용 Mock FundsApi 구현체
 *
 * JSON 파일 데이터를 파싱하여 반환하는 Mock 구현체입니다.
 * Unit Test에서 실제 API 호출 없이 테스트를 수행할 수 있습니다.
 *
 * @property listResponse 펀드 목록 JSON 데이터
 * @property comprehensiveResponse 펀드 상세 정보 JSON 데이터 (getDetailedInfo 사용)
 * @property dailyPricesResponse 전체 펀드 일별 시세 JSON 데이터
 * @property ohlcvResponse 펀드 OHLCV JSON 데이터
 * @property adjustedOhlcvResponse 조정주가 OHLCV JSON 데이터
 * @property priceChangesResponse 등락률 JSON 데이터
 * @property portfolioResponse 포트폴리오 JSON 데이터
 * @property trackingErrorResponse 추적오차 JSON 데이터
 * @property divergenceRateResponse 괴리율 JSON 데이터
 * @property investorTradingResponse 투자자별 거래 JSON 데이터
 * @property shortSellingResponse 공매도 거래 JSON 데이터
 * @property shortBalanceResponse 공매도 잔고 JSON 데이터
 */
class MockFundsApi(
    private val listResponse: String? = null,
    private val comprehensiveResponse: String? = null,
    private val dailyPricesResponse: String? = null,
    private val ohlcvResponse: String? = null,
    private val adjustedOhlcvResponse: String? = null,
    private val priceChangesResponse: String? = null,
    private val portfolioResponse: String? = null,
    private val trackingErrorResponse: String? = null,
    private val divergenceRateResponse: String? = null,
    private val investorTradingResponse: String? = null,
    private val shortSellingResponse: String? = null,
    private val shortBalanceResponse: String? = null
) : FundsApi {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
            LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        })
        .registerTypeAdapter(BigDecimal::class.java, JsonDeserializer { json, _, _ ->
            BigDecimal(json.asString)
        })
        .registerTypeAdapter(Direction::class.java, JsonDeserializer { json, _, _ ->
            Direction.valueOf(json.asString)
        })
        .create()

    override suspend fun getList(type: FundType?): List<FundListItem> {
        require(listResponse != null) { "listResponse가 설정되지 않았습니다" }
        return gson.fromJson(listResponse, Array<FundListItem>::class.java).toList()
    }

    override suspend fun getDetailedInfo(isin: String, tradeDate: LocalDate): DetailedInfo? {
        require(comprehensiveResponse != null) { "comprehensiveResponse가 설정되지 않았습니다" }
        return gson.fromJson(comprehensiveResponse, DetailedInfo::class.java)
    }

    override suspend fun getGeneralInfo(isin: String, tradeDate: LocalDate): GeneralInfo? {
        throw NotImplementedError("Mock에서는 지원하지 않습니다")
    }

    override suspend fun getPortfolio(isin: String, date: LocalDate): List<PortfolioConstituent> {
        require(portfolioResponse != null) { "portfolioResponse가 설정되지 않았습니다" }
        return gson.fromJson(portfolioResponse, Array<PortfolioConstituent>::class.java).toList()
    }

    override suspend fun getPortfolioTop10(isin: String, date: LocalDate): List<PortfolioTopItem> {
        throw NotImplementedError("Mock에서는 지원하지 않습니다")
    }

    override suspend fun getTrackingError(isin: String, fromDate: LocalDate, toDate: LocalDate): List<TrackingError> {
        require(trackingErrorResponse != null) { "trackingErrorResponse가 설정되지 않았습니다" }
        return gson.fromJson(trackingErrorResponse, Array<TrackingError>::class.java).toList()
    }

    override suspend fun getDivergenceRate(isin: String, fromDate: LocalDate, toDate: LocalDate): List<DivergenceRate> {
        require(divergenceRateResponse != null) { "divergenceRateResponse가 설정되지 않았습니다" }
        return gson.fromJson(divergenceRateResponse, Array<DivergenceRate>::class.java).toList()
    }

    override suspend fun getAllInvestorTrading(date: LocalDate): List<InvestorTrading> {
        require(investorTradingResponse != null) { "investorTradingResponse가 설정되지 않았습니다" }
        return gson.fromJson(investorTradingResponse, Array<InvestorTrading>::class.java).toList()
    }

    override suspend fun getAllInvestorTradingByPeriod(fromDate: LocalDate, toDate: LocalDate): List<InvestorTradingByDate> {
        throw NotImplementedError("Mock에서는 지원하지 않습니다")
    }

    override suspend fun getInvestorTrading(isin: String, date: LocalDate): List<InvestorTrading> {
        require(investorTradingResponse != null) { "investorTradingResponse가 설정되지 않았습니다" }
        return gson.fromJson(investorTradingResponse, Array<InvestorTrading>::class.java).toList()
    }

    override suspend fun getInvestorTradingByPeriod(isin: String, fromDate: LocalDate, toDate: LocalDate): List<InvestorTradingByDate> {
        throw NotImplementedError("Mock에서는 지원하지 않습니다")
    }

    override suspend fun getShortSelling(isin: String, fromDate: LocalDate, toDate: LocalDate, type: FundType): List<ShortSelling> {
        require(shortSellingResponse != null) { "shortSellingResponse가 설정되지 않았습니다" }
        return gson.fromJson(shortSellingResponse, Array<ShortSelling>::class.java).toList()
    }

    override suspend fun getShortBalance(isin: String, fromDate: LocalDate, toDate: LocalDate, type: FundType): List<ShortBalance> {
        require(shortBalanceResponse != null) { "shortBalanceResponse가 설정되지 않았습니다" }
        return gson.fromJson(shortBalanceResponse, Array<ShortBalance>::class.java).toList()
    }
}
