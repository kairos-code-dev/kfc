package dev.kairoscode.kfc.utils

import dev.kairoscode.kfc.KfcClient
import dev.kairoscode.kfc.funds.FundsApi
import dev.kairoscode.kfc.price.PriceApi
import dev.kairoscode.kfc.corp.CorpApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import kotlin.time.Duration.Companion.seconds

/**
 * Unit Test의 공통 베이스 클래스
 *
 * 실제 API 호출 없이 레코딩된 JSON 응답을 사용하여 테스트합니다.
 * Mock API 구현체를 주입하여 KfcClient를 생성합니다.
 *
 * ## 사용 예제
 * ```kotlin
 * class FundsApiTest : UnitTestBase() {
 *     @Test
 *     fun `펀드 목록 조회 테스트`() = unitTest {
 *         // Mock 응답 설정
 *         mockFundsApi = MockFundsApi(
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
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class UnitTestBase {

    protected lateinit var client: KfcClient
    protected var mockFundsApi: FundsApi? = null
    protected var mockPriceApi: PriceApi? = null
    protected var mockCorpApi: CorpApi? = null

    /**
     * Mock API를 주입하여 KfcClient 생성
     *
     * 테스트 케이스에서 mockFundsApi 또는 mockCorpApi를 설정한 후
     * 이 메서드를 호출하여 client를 초기화합니다.
     */
    protected fun initClient() {
        require(mockFundsApi != null || mockCorpApi != null) {
            "mockFundsApi 또는 mockCorpApi를 먼저 설정해야 합니다"
        }

        // KfcClient 생성자는 funds가 필수이고 price, corp는 optional이므로
        // mockFundsApi가 없으면 dummy FundsApi 생성
        val dummyFundsApi = object : FundsApi {
            override suspend fun getList(type: dev.kairoscode.kfc.model.FundType?) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.FundListItem>()
            override suspend fun getDetailedInfo(isin: String, tradeDate: java.time.LocalDate) = null
            override suspend fun getGeneralInfo(isin: String, tradeDate: java.time.LocalDate) = null
            override suspend fun getPortfolio(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.PortfolioConstituent>()
            override suspend fun getPortfolioTop10(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.PortfolioTopItem>()
            override suspend fun getTrackingError(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.TrackingError>()
            override suspend fun getDivergenceRate(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.DivergenceRate>()
            override suspend fun getAllInvestorTrading(date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.InvestorTrading>()
            override suspend fun getAllInvestorTradingByPeriod(fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.InvestorTradingByDate>()
            override suspend fun getInvestorTrading(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.InvestorTrading>()
            override suspend fun getInvestorTradingByPeriod(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.InvestorTradingByDate>()
            override suspend fun getShortSelling(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate, type: dev.kairoscode.kfc.model.FundType) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.ShortSelling>()
            override suspend fun getShortBalance(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate, type: dev.kairoscode.kfc.model.FundType) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.ShortBalance>()
        }

        val dummyPriceApi = object : PriceApi {
            override suspend fun getIntradayBars(isin: String, tradeDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.IntradayBar>()
            override suspend fun getRecentDaily(isin: String, tradeDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.funds.internal.krx.model.RecentDaily>()
        }

        client = KfcClient(
            funds = mockFundsApi ?: dummyFundsApi,
            price = mockPriceApi ?: dummyPriceApi,
            corp = mockCorpApi
        )
    }

    /**
     * JSON 파일을 로드하여 Mock 응답으로 설정하는 헬퍼 메서드
     *
     * @param category 응답 카테고리 (etf/list, corp/dividend 등)
     * @param fileName 파일명 (.json 확장자 제외)
     * @return JSON 문자열
     */
    protected fun loadMockResponse(category: String, fileName: String): String {
        return JsonResponseLoader.load(category, fileName)
    }

    // ETF API Mock 헬퍼 - JSON 파일 로드
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

    // Corp API Mock 헬퍼 - JSON 파일 로드
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
        // Mock API는 close 불필요
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
