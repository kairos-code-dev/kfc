package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimiter
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimitingSettings
import dev.kairoscode.kfc.infrastructure.common.ratelimit.TokenBucketRateLimiter
import dev.kairoscode.kfc.infrastructure.common.util.*
import dev.kairoscode.kfc.infrastructure.krx.internal.*
import dev.kairoscode.kfc.domain.FundType
import dev.kairoscode.kfc.domain.funds.*
import dev.kairoscode.kfc.domain.price.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * KRX 펀드/증권상품 API 구현
 *
 * KrxFundsApi 인터페이스의 내부 구현입니다.
 * HTTP 클라이언트를 사용하여 실제 KRX API와 통신합니다.
 */
internal class KrxFundsApiImpl(
    private val httpClient: KrxHttpClient = KrxHttpClient(),
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.krxDefault())
) : KrxFundsApi {

    companion object {
        private const val BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd"

        // BLD 코드 상수 (KRX API 데이터셋 ID)
        // ================================
        // 기본 정보 및 메타데이터
        // ================================
        private const val BLD_ETF_LIST = "dbms/MDC/STAT/standard/MDCSTAT04601"  // ETF 목록
        private const val BLD_ETF_COMPREHENSIVE_INFO = "dbms/MDC/STAT/standard/MDCSTAT04701"  // ETF 종합정보 (NAV, 시가총액 등)
        private const val BLD_ETF_INTRADAY_BARS = "dbms/MDC/STAT/standard/MDCSTAT04702"  // ETF 분단위 시세
        private const val BLD_ETF_RECENT_DAILY = "dbms/MDC/STAT/standard/MDCSTAT04703"  // ETF 최근 일별 거래
        private const val BLD_ETF_GENERAL_INFO = "dbms/MDC/STAT/standard/MDCSTAT04704"  // ETF 기본정보 (정적 메타데이터)
        private const val BLD_ETF_PORTFOLIO_TOP10 = "dbms/MDC/STAT/standard/MDCSTAT04705"  // PDF 상위 10종목

        // ================================
        // 투자자별 거래
        // ================================
        private const val BLD_ETF_ALL_INVESTOR_TRADING_DAILY = "dbms/MDC/STAT/standard/MDCSTAT04801"  // 전체 ETF 일자별 투자자별 거래
        private const val BLD_ETF_ALL_INVESTOR_TRADING_PERIOD = "dbms/MDC/STAT/standard/MDCSTAT04802"  // 전체 ETF 기간별 투자자별 거래
        private const val BLD_ETF_INVESTOR_TRADING_DAILY = "dbms/MDC/STAT/standard/MDCSTAT04901"  // 개별 ETF 투자자별 거래
        private const val BLD_ETF_INVESTOR_TRADING_PERIOD = "dbms/MDC/STAT/standard/MDCSTAT04902"  // 개별 ETF 기간별 투자자별 거래

        // ================================
        // 포트폴리오 및 성과
        // ================================
        private const val BLD_ETF_PORTFOLIO = "dbms/MDC/STAT/standard/MDCSTAT05001"  // ETF 포트폴리오 구성
        private const val BLD_ETF_TRACKING_ERROR = "dbms/MDC/STAT/standard/MDCSTAT05901"  // ETF 추적오차 (NAV vs 지수)
        private const val BLD_ETF_DIVERGENCE_RATE = "dbms/MDC/STAT/standard/MDCSTAT06001"  // ETF 괴리율 (종가 vs NAV)

        // ================================
        // 공매도
        // ================================
        private const val BLD_ETF_SHORT_SELLING = "dbms/MDC/STAT/srt/MDCSTAT30102"  // 공매도 거래 현황
        private const val BLD_ETF_SHORT_BALANCE = "dbms/MDC/STAT/srt/MDCSTAT30502"  // 공매도 순보유 잔고

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    // ================================
    // 1. 펀드 목록 및 기본 정보
    // ================================

    override suspend fun getEtfList(type: FundType?): List<FundListItem> {
        // type이 null이면 모든 타입을 순회하여 조회
        if (type == null) {
            logger.debug { "Fetching all fund types (ETF, REIT, ETN, ELW)" }
            return FundType.entries.flatMap { fundType ->
                getEtfListByType(fundType)
            }
        }

        return getEtfListByType(type)
    }

    /**
     * 특정 펀드 타입의 목록 조회
     */
    private suspend fun getEtfListByType(type: FundType): List<FundListItem> {
        rateLimiter.acquire()
        logger.debug { "Fetching fund list for type: $type" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_LIST,
            KrxApiParams.SECURITY_GROUP_ID to type.krxSecurityGroupId
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            FundListItem(
                isin = raw.getString(KrxApiFields.Identity.ISIN),
                ticker = raw.getString(KrxApiFields.Identity.TICKER),
                name = raw.getString(KrxApiFields.Identity.NAME_SHORT),
                fullName = raw.getString(KrxApiFields.Identity.NAME_FULL),
                englishName = raw.getString(KrxApiFields.Identity.NAME_ENGLISH),
                listingDate = raw.getString(KrxApiFields.DateTime.LISTING_DATE).toKrxDate(),
                benchmarkIndex = raw.getString(KrxApiFields.EtfMetadata.BENCHMARK_INDEX),
                indexProvider = raw.getString(KrxApiFields.EtfMetadata.INDEX_PROVIDER),
                leverageType = raw.getStringOrNull(KrxApiFields.EtfMetadata.LEVERAGE_TYPE),
                replicationMethod = raw.getString(KrxApiFields.EtfMetadata.REPLICATION_METHOD),
                marketType = raw.getString(KrxApiFields.EtfMetadata.MARKET_CLASSIFICATION),
                assetClass = raw.getString(KrxApiFields.EtfMetadata.ASSET_CLASS),
                listedShares = raw.getString(KrxApiFields.Asset.LISTED_SHARES).toKrxLong(),
                assetManager = raw.getString(KrxApiFields.EtfMetadata.ASSET_MANAGER),
                cuQuantity = raw.getString(KrxApiFields.EtfMetadata.CREATION_UNIT).toKrxLong(),
                totalExpenseRatio = raw.getString(KrxApiFields.EtfMetadata.TOTAL_EXPENSE_RATIO).toKrxBigDecimal(),
                taxType = raw.getString(KrxApiFields.EtfMetadata.TAX_TYPE)
            )
        }.also { logger.debug { "Fetched ${it.size} funds for type $type" } }
    }

    override suspend fun getDetailedInfo(
        isin: String,
        tradeDate: LocalDate
    ): DetailedInfo? {
        rateLimiter.acquire()
        logger.debug { "Fetching fund detailed info for ISIN: $isin, date: $tradeDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_COMPREHENSIVE_INFO,
            KrxApiParams.TRADE_DATE to tradeDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        if (output.isEmpty()) {
            logger.warn { "No data found for ISIN: $isin on date: $tradeDate" }
            return null
        }

        val raw = output[0]
        // 입력받은 tradeDate를 override하여 사용 (API 응답에 TRD_DD 필드가 없을 수 있음)
        val info = DetailedInfo.fromRaw(raw, tradeDate)

        logger.debug { "Successfully fetched detailed info for ${info.name} (${info.ticker})" }

        return info
    }

    override suspend fun getIntradayBars(
        isin: String,
        tradeDate: LocalDate
    ): List<IntradayBar> {
        rateLimiter.acquire()
        logger.debug { "Fetching intraday bars for ISIN: $isin, date: $tradeDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_INTRADAY_BARS,
            KrxApiParams.TRADE_DATE to tradeDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            IntradayBar.fromRaw(raw)
        }.also { logger.debug { "Fetched ${it.size} intraday bars" } }
    }

    override suspend fun getRecentDaily(
        isin: String,
        tradeDate: LocalDate
    ): List<RecentDaily> {
        rateLimiter.acquire()
        logger.debug { "Fetching recent daily data for ISIN: $isin, date: $tradeDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_RECENT_DAILY,
            KrxApiParams.TRADE_DATE to tradeDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            RecentDaily.fromRaw(raw)
        }.sortedByDescending { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} recent daily records" } }
    }

    override suspend fun getGeneralInfo(
        isin: String,
        tradeDate: LocalDate
    ): GeneralInfo? {
        rateLimiter.acquire()
        logger.debug { "Fetching fund general info for ISIN: $isin, date: $tradeDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_GENERAL_INFO,
            KrxApiParams.TRADE_DATE to tradeDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        if (output.isEmpty()) {
            logger.warn { "No general info found for ISIN: $isin on date: $tradeDate" }
            return null
        }

        return GeneralInfo.fromRaw(output[0])
            .also { logger.debug { "Successfully fetched general info for ${it.name}" } }
    }

    // ================================
    // 2. 펀드 포트폴리오 구성
    // ================================

    override suspend fun getEtfPortfolio(isin: String, date: LocalDate): List<PortfolioConstituent> {
        rateLimiter.acquire()
        logger.debug { "Fetching fund portfolio for ISIN: $isin, date: $date" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_PORTFOLIO,
            KrxApiParams.TRADE_DATE to date.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            PortfolioConstituent(
                constituentCode = raw.getString(KrxApiFields.Portfolio.CONSTITUENT_CODE),
                constituentName = raw.getString(KrxApiFields.Portfolio.CONSTITUENT_NAME),
                sharesPerCu = raw.getString(KrxApiFields.Portfolio.SHARES_PER_CU).toKrxBigDecimal(),
                value = raw.getString(KrxApiFields.Asset.VALUATION_AMOUNT).toKrxLong(),
                constituentAmount = raw.getString(KrxApiFields.Portfolio.CONSTITUENT_AMOUNT).toKrxLong(),
                weightPercent = raw.getString(KrxApiFields.Portfolio.CONSTITUENT_WEIGHT).toKrxBigDecimal()
            )
        }.filter { it.value > 0 }  // 가치가 0인 항목 제외
            .also { logger.debug { "Fetched ${it.size} portfolio constituents" } }
    }

    override suspend fun getEtfPortfolioTop10(isin: String, date: LocalDate): List<PortfolioTopItem> {
        rateLimiter.acquire()
        logger.debug { "Fetching fund portfolio top 10 for ISIN: $isin, date: $date" }

        // MDCSTAT04705는 작동하지 않으므로, 전체 Portfolio API(MDCSTAT05001)를 사용하여 상위 10개 추출
        logger.debug { "Using full portfolio API and filtering top 10 items (MDCSTAT04705 endpoint not available)" }

        val portfolio = getEtfPortfolio(isin, date)

        return portfolio
            .sortedByDescending { it.weightPercent }
            .take(10)
            .map { constituent ->
                PortfolioTopItem(
                    ticker = constituent.constituentCode,
                    name = constituent.constituentName,
                    cuQuantity = constituent.sharesPerCu,
                    value = constituent.value,
                    compositionAmount = constituent.constituentAmount,
                    compositionRatio = constituent.weightPercent
                )
            }
            .also { logger.debug { "Fetched ${it.size} portfolio top items from full portfolio" } }
    }

    // ================================
    // 4. 펀드 성과 및 추적
    // ================================

    override suspend fun getEtfTrackingError(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<TrackingError> {
        rateLimiter.acquire()
        logger.debug { "Fetching tracking error for ISIN: $isin, from: $fromDate, to: $toDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_TRACKING_ERROR,
            KrxApiParams.START_DATE to fromDate.format(dateFormatter),
            KrxApiParams.END_DATE to toDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            TrackingError(
                tradeDate = raw.getString(KrxApiFields.DateTime.TRADE_DATE).toKrxDate(),
                nav = raw.getString(KrxApiFields.Nav.VALUE_LAST).toKrxBigDecimal(),
                navChangeRate = raw.getString(KrxApiFields.Nav.CHANGE_RATE).toKrxDouble(),
                indexValue = raw.getString(KrxApiFields.Index.VALUE).toKrxBigDecimal(),
                indexChangeRate = raw.getString(KrxApiFields.Index.CHANGE_RATIO).toKrxDouble(),
                trackingMultiple = raw.getString(KrxApiFields.TrackingPerformance.TRACKING_MULTIPLE).toKrxBigDecimal(),
                trackingErrorRate = raw.getString(KrxApiFields.TrackingPerformance.TRACKING_ERROR_RATE).toKrxDouble()
            )
        }.sortedBy { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} tracking error records" } }
    }

    override suspend fun getEtfDivergenceRate(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<DivergenceRate> {
        rateLimiter.acquire()
        logger.debug { "Fetching divergence rate for ISIN: $isin, from: $fromDate, to: $toDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_DIVERGENCE_RATE,
            KrxApiParams.START_DATE to fromDate.format(dateFormatter),
            KrxApiParams.END_DATE to toDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            DivergenceRate(
                tradeDate = raw.getString(KrxApiFields.DateTime.TRADE_DATE).toKrxDate(),
                closePrice = raw.getString(KrxApiFields.Price.CLOSE_ALT).toKrxInt(),
                nav = raw.getString(KrxApiFields.Nav.VALUE_LAST).toKrxBigDecimal(),
                divergenceRate = raw.getString(KrxApiFields.Nav.DIVERGENCE_RATE).toKrxDouble(),
                priceDirection = raw.getString(KrxApiFields.PriceChange.DIRECTION).toDirection()
            )
        }.sortedBy { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} divergence rate records" } }
    }

    // ================================
    // 5. 투자자별 거래
    // ================================

    override suspend fun getAllEtfInvestorTrading(date: LocalDate): List<InvestorTrading> {
        rateLimiter.acquire()
        logger.debug { "Fetching all fund investor trading for date: $date" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_ALL_INVESTOR_TRADING_DAILY,
            KrxApiParams.START_DATE to date.format(dateFormatter),
            KrxApiParams.END_DATE to date.format(dateFormatter)
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            InvestorTrading(
                investorType = raw.getString(KrxApiFields.InvestorTrading.INVESTOR_TYPE),
                askVolume = raw.getString(KrxApiFields.InvestorTrading.ASK_VOLUME).toKrxLong(),
                askValue = raw.getString(KrxApiFields.InvestorTrading.ASK_VALUE).toKrxLong(),
                bidVolume = raw.getString(KrxApiFields.InvestorTrading.BID_VOLUME).toKrxLong(),
                bidValue = raw.getString(KrxApiFields.InvestorTrading.BID_VALUE).toKrxLong(),
                netBuyVolume = raw.getString(KrxApiFields.InvestorTrading.NET_BUY_VOLUME).toKrxLong(),
                netBuyValue = raw.getString(KrxApiFields.InvestorTrading.NET_BUY_VALUE).toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} investor trading records" } }
    }

    override suspend fun getAllEtfInvestorTradingByPeriod(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
        rateLimiter.acquire()
        logger.debug { "Fetching all fund investor trading by period from: $fromDate, to: $toDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_ALL_INVESTOR_TRADING_PERIOD,
            KrxApiParams.START_DATE to fromDate.format(dateFormatter),
            KrxApiParams.END_DATE to toDate.format(dateFormatter),
            "inqCondTpCd1" to "1",  // 거래대금
            "inqCondTpCd2" to "1"   // 순매수
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        // 날짜별로 파싱하여 투자자별 데이터로 변환
        return output.flatMap { raw ->
            val tradeDate = raw.getString(KrxApiFields.DateTime.TRADE_DATE).toKrxDate()
            listOf(
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "기관",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.INSTITUTION_NET_BUY).toKrxLong()
                ),
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "기타법인",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.CORPORATE_NET_BUY).toKrxLong()
                ),
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "개인",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.INDIVIDUAL_NET_BUY).toKrxLong()
                ),
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "외국인",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.FOREIGN_NET_BUY).toKrxLong()
                )
            )
        }.sortedBy { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} investor trading by period records" } }
    }

    override suspend fun getEtfInvestorTrading(isin: String, date: LocalDate): List<InvestorTrading> {
        rateLimiter.acquire()
        logger.debug { "Fetching fund investor trading for ISIN: $isin, date: $date" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_INVESTOR_TRADING_DAILY,
            KrxApiParams.START_DATE to date.format(dateFormatter),
            KrxApiParams.END_DATE to date.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.map { raw ->
            InvestorTrading(
                investorType = raw.getString(KrxApiFields.InvestorTrading.INVESTOR_TYPE),
                askVolume = raw.getString(KrxApiFields.InvestorTrading.ASK_VOLUME).toKrxLong(),
                askValue = raw.getString(KrxApiFields.InvestorTrading.ASK_VALUE).toKrxLong(),
                bidVolume = raw.getString(KrxApiFields.InvestorTrading.BID_VOLUME).toKrxLong(),
                bidValue = raw.getString(KrxApiFields.InvestorTrading.BID_VALUE).toKrxLong(),
                netBuyVolume = raw.getString(KrxApiFields.InvestorTrading.NET_BUY_VOLUME).toKrxLong(),
                netBuyValue = raw.getString(KrxApiFields.InvestorTrading.NET_BUY_VALUE).toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} investor trading records" } }
    }

    override suspend fun getEtfInvestorTradingByPeriod(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
        rateLimiter.acquire()
        logger.debug { "Fetching fund investor trading by period for ISIN: $isin, from: $fromDate, to: $toDate" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_INVESTOR_TRADING_PERIOD,
            KrxApiParams.START_DATE to fromDate.format(dateFormatter),
            KrxApiParams.END_DATE to toDate.format(dateFormatter),
            KrxApiParams.ISIN_CODE to isin,
            "inqCondTpCd1" to "1",  // 거래대금
            "inqCondTpCd2" to "1"   // 순매수
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = response.extractOutput()

        return output.flatMap { raw ->
            val tradeDate = raw.getString(KrxApiFields.DateTime.TRADE_DATE).toKrxDate()
            listOf(
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "기관",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.INSTITUTION_NET_BUY).toKrxLong()
                ),
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "기타법인",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.CORPORATE_NET_BUY).toKrxLong()
                ),
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "개인",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.INDIVIDUAL_NET_BUY).toKrxLong()
                ),
                InvestorTradingByDate(
                    tradeDate = tradeDate,
                    investorType = "외국인",
                    askVolume = 0L,
                    askValue = 0L,
                    bidVolume = 0L,
                    bidValue = 0L,
                    netBuyVolume = 0L,
                    netBuyValue = raw.getString(KrxApiFields.InvestorTrading.FOREIGN_NET_BUY).toKrxLong()
                )
            )
        }.sortedBy { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} investor trading by period records" } }
    }

    // ================================
    // 6. 공매도 데이터
    // ================================

    override suspend fun getEtfShortSelling(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType
    ): List<ShortSelling> {
        rateLimiter.acquire()
        logger.debug { "Fetching short selling for ISIN: $isin, from: $fromDate, to: $toDate, type: $type" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_SHORT_SELLING,
            KrxApiParams.SEARCH_TYPE to "2",  // 개별종목 조회
            KrxApiParams.MARKET_ID to "STK",
            KrxApiParams.SECURITY_GROUP_ID to type.krxSecurityGroupId,  // FundType에 따라 동적 설정
            KrxApiParams.INQUIRY_CONDITION to type.krxInquiryCondition,  // FundType에 따라 동적 설정
            KrxApiParams.ISIN_CODE to isin,
            KrxApiParams.START_DATE to fromDate.format(dateFormatter),
            KrxApiParams.END_DATE to toDate.format(dateFormatter),
            KrxApiParams.SHARE to "1",
            KrxApiParams.MONEY to "1"
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = (response["OutBlock_1"] as? List<Map<String, Any?>>) ?: emptyList()

        // ISIN에서 ticker 추출 (KR7069500007 -> 069500)
        val ticker = if (isin.length >= 9) isin.substring(3, 9) else isin

        return output.mapNotNull { item ->
            val raw = item as? Map<*, *> ?: return@mapNotNull null

            // 공매도 거래량이 실제로 있는 데이터만 포함
            val shortVol = raw[KrxApiFields.Volume.SHORT_VOLUME]?.toString()?.replace(",", "")?.replace("-", "0")?.toLongOrNull() ?: 0L
            val shortVal = raw[KrxApiFields.Volume.SHORT_VALUE]?.toString()?.replace(",", "")?.replace("-", "0")?.toLongOrNull() ?: 0L

            ShortSelling(
                tradeDate = raw[KrxApiFields.DateTime.TRADE_DATE]?.toString()?.replace("/", "")?.toKrxDate() ?: return@mapNotNull null,
                ticker = ticker,
                name = "",  // API 응답에 name 필드 없음
                shortVolume = shortVol,
                shortValue = shortVal,
                totalVolume = raw[KrxApiFields.Volume.ACCUMULATED]?.toString()?.replace(",", "")?.toLongOrNull() ?: 0L,
                totalValue = raw[KrxApiFields.Volume.TRADING_VALUE]?.toString()?.replace(",", "")?.toLongOrNull() ?: 0L,
                shortVolumeRatio = raw[KrxApiFields.Volume.VOLUME_RATIO]?.toString()?.toDoubleOrNull() ?: 0.0,
                shortValueRatio = raw[KrxApiFields.Volume.VALUE_RATIO]?.toString()?.toDoubleOrNull() ?: 0.0
            )
        }.sortedBy { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} short selling records" } }
    }

    override suspend fun getEtfShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType
    ): List<ShortBalance> {
        rateLimiter.acquire()
        logger.debug { "Fetching short balance for ISIN: $isin, from: $fromDate, to: $toDate, type: $type" }

        val parameters = mapOf(
            KrxApiParams.BLD to BLD_ETF_SHORT_BALANCE,
            KrxApiParams.SEARCH_TYPE to "2",  // 개별종목 조회
            KrxApiParams.MARKET_TYPE_CODE to type.krxMarketTypeCode,  // FundType에 따라 동적 설정
            KrxApiParams.ISIN_CODE to isin,
            KrxApiParams.START_DATE to fromDate.format(dateFormatter),
            KrxApiParams.END_DATE to toDate.format(dateFormatter),
            KrxApiParams.SHARE to "1",
            KrxApiParams.MONEY to "1"
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()
        val output = (response["OutBlock_1"] as? List<Map<String, Any?>>) ?: emptyList()

        // ISIN에서 ticker 추출
        val ticker = if (isin.length >= 9) isin.substring(3, 9) else isin

        return output.mapNotNull { raw ->
            ShortBalance(
                tradeDate = raw[KrxApiFields.DateTime.REPORT_DATE]?.toString()?.replace("/", "")?.toKrxDate() ?: return@mapNotNull null,
                ticker = ticker,
                name = "",  // API 응답에 name 필드 없음
                shortBalance = raw[KrxApiFields.ShortSelling.BALANCE_SHARES]?.toString()?.replace(",", "")?.toLongOrNull() ?: 0L,
                shortBalanceValue = raw[KrxApiFields.ShortSelling.BALANCE_VALUE]?.toString()?.replace(",", "")?.toLongOrNull() ?: 0L,
                listedShares = raw[KrxApiFields.Asset.LISTED_SHARES]?.toString()?.replace(",", "")?.toLongOrNull() ?: 0L,
                shortBalanceRatio = raw[KrxApiFields.ShortSelling.BALANCE_RATIO]?.toString()?.toDoubleOrNull() ?: 0.0
            )
        }.sortedBy { it.tradeDate }
            .also { logger.debug { "Fetched ${it.size} short balance records" } }
    }

    /**
     * HTTP 클라이언트 종료
     *
     * 리소스 정리를 위해 사용 후 호출해야 합니다.
     */
    fun close() {
        httpClient.close()
    }
}
