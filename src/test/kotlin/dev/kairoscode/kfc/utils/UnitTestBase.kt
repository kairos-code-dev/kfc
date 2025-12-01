package dev.kairoscode.kfc.utils

import dev.kairoscode.kfc.KfcClient
import dev.kairoscode.kfc.api.EtfApi
import dev.kairoscode.kfc.api.CorpApi
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
 * class EtfApiTest : UnitTestBase() {
 *     @Test
 *     fun `ETF 목록 조회 테스트`() = unitTest {
 *         // Mock 응답 설정
 *         mockEtfApi = MockEtfApi(
 *             listResponse = JsonResponseLoader.load("etf/list", "etf_list_all")
 *         )
 *         initClient()
 *
 *         // 테스트 실행
 *         val result = client.etf.getList()
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
    protected var mockEtfApi: EtfApi? = null
    protected var mockCorpApi: CorpApi? = null

    /**
     * Mock API를 주입하여 KfcClient 생성
     *
     * 테스트 케이스에서 mockEtfApi 또는 mockCorpApi를 설정한 후
     * 이 메서드를 호출하여 client를 초기화합니다.
     */
    protected fun initClient() {
        require(mockEtfApi != null || mockCorpApi != null) {
            "mockEtfApi 또는 mockCorpApi를 먼저 설정해야 합니다"
        }

        // KfcClient 생성자는 etf가 필수이고 corp는 optional이므로
        // mockEtfApi가 없으면 dummy EtfApi 생성
        val dummyEtfApi = object : EtfApi {
            override suspend fun getList() = emptyList<dev.kairoscode.kfc.model.krx.EtfListItem>()
            override suspend fun getComprehensiveInfo(isin: String, tradeDate: java.time.LocalDate) = null
            override suspend fun getAllDailyPrices(date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.EtfDailyPrice>()
            override suspend fun getOhlcv(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.EtfOhlcv>()
            override suspend fun getAdjustedOhlcv(ticker: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.naver.NaverEtfOhlcv>()
            override suspend fun getPriceChanges(fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.EtfPriceChange>()
            override suspend fun getPortfolio(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.PortfolioConstituent>()
            override suspend fun getTrackingError(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.TrackingError>()
            override suspend fun getDivergenceRate(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.DivergenceRate>()
            override suspend fun getAllInvestorTrading(date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.InvestorTrading>()
            override suspend fun getAllInvestorTradingByPeriod(fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.InvestorTradingByDate>()
            override suspend fun getInvestorTrading(isin: String, date: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.InvestorTrading>()
            override suspend fun getInvestorTradingByPeriod(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.InvestorTradingByDate>()
            override suspend fun getShortSelling(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.ShortSelling>()
            override suspend fun getShortBalance(isin: String, fromDate: java.time.LocalDate, toDate: java.time.LocalDate) = emptyList<dev.kairoscode.kfc.model.krx.ShortBalance>()
        }

        client = KfcClient(
            etf = mockEtfApi ?: dummyEtfApi,
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

    protected fun loadEtfComprehensiveResponse(fileName: String) =
        loadMockResponse("etf/comprehensive", fileName)

    protected fun loadEtfDailyPricesResponse(fileName: String) =
        loadMockResponse("etf/daily_prices", fileName)

    protected fun loadEtfOhlcvResponse(fileName: String) =
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
