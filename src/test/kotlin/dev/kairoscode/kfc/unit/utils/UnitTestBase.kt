package dev.kairoscode.kfc.unit.utils

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.api.FundsApi
import dev.kairoscode.kfc.api.PriceApi
import dev.kairoscode.kfc.api.CorpApi
import dev.kairoscode.kfc.api.FinancialsApi
import dev.kairoscode.kfc.domain.stock.Market
import dev.kairoscode.kfc.domain.stock.ListingStatus
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

/**
 * Unit Test의 공통 베이스 클래스
 *
 * 실제 API 호출 없이 레코딩된 JSON 응답을 사용하여 테스트합니다.
 * Fake API 구현체를 주입하여 KfcClient를 생성합니다.
 *
 * ## Fake 패턴에 대해
 * Fake는 실제 구현을 갖고 있지만 프로덕션에 적합하지 않은 간단한 구현입니다.
 * 여기서는 레코딩된 JSON 응답을 반환하는 Fake API를 사용하여
 * 외부 의존성 없이 빠르고 안정적인 테스트를 수행합니다.
 *
 * ## 사용 예제
 * ```kotlin
 * class FundsApiTest : UnitTestBase() {
 *     @Test
 *     fun `펀드 목록 조회 테스트`() = unitTest {
 *         // Fake 응답 설정
 *         fakeFundsApi = FakeFundsApi(
 *             listResponse = JsonResponseLoader.load("etf/list", "etf_list_all")
 *         )
 *         initClient()
 *
 *         // 테스트 실행
 *         val result = client.funds.getList()
 *         result.assertNotEmpty()
 *     }
 * }
 * ```
 *
 * **중요**: Unit Test는 레코딩된 JSON 파일에 의존합니다.
 * Live Test를 먼저 실행하여 응답을 레코딩해야 합니다:
 * ```bash
 * ./gradlew liveTest -Precord.responses=true
 * ```
 */
@Tag("unit")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class UnitTestBase {

    protected lateinit var client: KfcClient
    protected var fakeFundsApi: FundsApi? = null
    protected var fakePriceApi: PriceApi? = null
    protected var fakeCorpApi: CorpApi? = null
    protected var fakeFinancialsApi: FinancialsApi? = null

    /**
     * Fake API를 주입하여 KfcClient 생성
     *
     * 테스트 케이스에서 fakeFundsApi, fakeCorpApi, fakeFinancialsApi를 설정한 후
     * 이 메서드를 호출하여 client를 초기화합니다.
     */
    protected fun initClient() {
        require(fakeFundsApi != null || fakeCorpApi != null || fakeFinancialsApi != null) {
            "fakeFundsApi, fakeCorpApi 또는 fakeFinancialsApi를 먼저 설정해야 합니다"
        }

        // KfcClient 생성자는 funds가 필수이고 price, corp는 optional이므로
        // fakeFundsApi가 없으면 dummy FundsApi 생성
        val dummyFundsApi = object : FundsApi {
            override suspend fun getList(type: dev.kairoscode.kfc.domain.FundType?) = emptyList<dev.kairoscode.kfc.domain.funds.FundListItem>()
            override suspend fun getDetailedInfo(isin: String, tradeDate: java.time.LocalDate) = null
            override suspend fun getGeneralInfo(isin: String, tradeDate: java.time.LocalDate) = null
            override suspend fun getPortfolio(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.PortfolioConstituent>()
            override suspend fun getPortfolioTop10(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.PortfolioTopItem>()
            override suspend fun getTrackingError(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.TrackingError>()
            override suspend fun getDivergenceRate(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.DivergenceRate>()
            override suspend fun getAllInvestorTrading(date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTrading>()
            override suspend fun getAllInvestorTradingByPeriod(fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTradingByDate>()
            override suspend fun getInvestorTrading(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTrading>()
            override suspend fun getInvestorTradingByPeriod(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.funds.InvestorTradingByDate>()
            override suspend fun getShortSelling(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate, type: dev.kairoscode.kfc.domain.FundType) = emptyList<dev.kairoscode.kfc.domain.funds.ShortSelling>()
            override suspend fun getShortBalance(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate, type: dev.kairoscode.kfc.domain.FundType) = emptyList<dev.kairoscode.kfc.domain.funds.ShortBalance>()
        }

        val dummyPriceApi = object : PriceApi {
            override suspend fun getIntradayBars(isin: String, tradeDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.price.IntradayBar>()
            override suspend fun getRecentDaily(isin: String, tradeDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.domain.price.RecentDaily>()
        }

        val dummyStockApi = object : dev.kairoscode.kfc.api.StockApi {
            override suspend fun getStockList(market: Market, listingStatus: ListingStatus) = emptyList<dev.kairoscode.kfc.domain.stock.StockListItem>()
            override suspend fun getStockInfo(ticker: String) = null
            override suspend fun getStockName(ticker: String) = null
            override suspend fun getSectorClassifications(date: LocalDate, market: Market) = emptyList<dev.kairoscode.kfc.domain.stock.StockSectorInfo>()
            override suspend fun getIndustryGroups(date: LocalDate, market: Market) = emptyList<dev.kairoscode.kfc.domain.stock.IndustryClassification>()
            override suspend fun searchStocks(keyword: String, market: Market) = emptyList<dev.kairoscode.kfc.domain.stock.StockListItem>()
        }

        // Dummy BondApi
        val dummyBondApi = object : dev.kairoscode.kfc.api.BondApi {
            override suspend fun getBondYieldsByDate(date: LocalDate) = dev.kairoscode.kfc.domain.bond.BondYieldSnapshot(date, emptyList())
            override suspend fun getBondYields(bondType: dev.kairoscode.kfc.domain.bond.BondType, fromDate: LocalDate, toDate: LocalDate) = emptyList<dev.kairoscode.kfc.domain.bond.BondYield>()
        }

        client = KfcClient(
            funds = fakeFundsApi ?: dummyFundsApi,
            price = fakePriceApi ?: dummyPriceApi,
            stock = dummyStockApi,
            bond = dummyBondApi,
            corp = fakeCorpApi,
            financials = fakeFinancialsApi
        )
    }

    /**
     * JSON 파일을 로드하여 Fake 응답으로 설정하는 헬퍼 메서드
     *
     * @param category 응답 카테고리 (etf/list, corp/dividend 등)
     * @param fileName 파일명 (.json 확장자 제외)
     * @return JSON 문자열
     */
    protected fun loadMockResponse(category: String, fileName: String): String {
        return JsonResponseLoader.load(category, fileName)
    }

    // ETF API Fake 헬퍼 - JSON 파일 로드
    protected fun loadEtfListResponse(fileName: String) =
        loadMockResponse("etf/list", fileName)

    protected fun loadDetailedInfoResponse(fileName: String) =
        loadMockResponse("etf/metrics/detailed_info", fileName)

    @Deprecated("Use loadDetailedInfoResponse instead", ReplaceWith("loadDetailedInfoResponse(fileName)"))
    protected fun loadEtfComprehensiveResponse(fileName: String) =
        loadMockResponse("etf/comprehensive", fileName)

    protected fun loadDailyPricesResponse(fileName: String) =
        loadMockResponse("etf/daily_prices", fileName)

    protected fun loadOhlcvResponse(fileName: String) =
        loadMockResponse("etf/ohlcv", fileName)

    protected fun loadEtfAdjustedOhlcvResponse(fileName: String) =
        loadMockResponse("etf/adjusted_ohlcv", fileName)

    protected fun loadEtfPriceChangesResponse(fileName: String) =
        loadMockResponse("etf/price_changes", fileName)

    protected fun loadEtfPortfolioResponse(fileName: String) =
        loadMockResponse("etf/portfolio", fileName)

    // Corp API Fake 헬퍼 - JSON 파일 로드
    protected fun loadCorpCodeResponse(fileName: String) =
        loadMockResponse("corp/corp_code", fileName)

    protected fun loadDividendResponse(fileName: String) =
        loadMockResponse("corp/dividend", fileName)

    protected fun loadStockSplitResponse(fileName: String) =
        loadMockResponse("corp/stock_split", fileName)

    protected fun loadDisclosureResponse(fileName: String) =
        loadMockResponse("corp/disclosure", fileName)

    @AfterEach
    fun tearDown() {
        // Fake API는 close 불필요
        // 실제 리소스를 사용하지 않음
    }

    /**
     * 테스트 실행 헬퍼 (타임아웃 설정)
     */
    protected fun unitTest(
        timeout: kotlin.time.Duration = 10.seconds,
        block: suspend () -> Unit
    ) = runTest(timeout = timeout) {
        block()
    }
}
