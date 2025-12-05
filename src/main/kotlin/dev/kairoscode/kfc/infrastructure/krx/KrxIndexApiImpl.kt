package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.index.*
import dev.kairoscode.kfc.infrastructure.common.ratelimit.GlobalRateLimiters
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimiter
import dev.kairoscode.kfc.infrastructure.common.util.*
import dev.kairoscode.kfc.infrastructure.krx.internal.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * KRX 지수 정보 API 구현체
 *
 * KrxIndexApi 인터페이스의 내부 구현입니다.
 * HTTP 클라이언트를 사용하여 실제 KRX API와 통신합니다.
 *
 * @param httpClient KRX HTTP 클라이언트
 * @param rateLimiter KRX API Rate Limiter (기본값: GlobalRateLimiters의 KRX 싱글톤)
 */
internal class KrxIndexApiImpl(
    private val httpClient: KrxHttpClient = KrxHttpClient(),
    private val rateLimiter: RateLimiter = GlobalRateLimiters.getKrxLimiter()
) : KrxIndexApi {

    companion object {
        private const val BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd"

        // BLD 코드 상수
        private const val BLD_INDEX_LIST = "dbms/MDC/STAT/standard/MDCSTAT00201"
        private const val BLD_INDEX_CONSTITUENTS = "dbms/MDC/STAT/standard/MDCSTAT00401"
        private const val BLD_INDEX_OHLCV_BY_DATE = "dbms/MDC/STAT/standard/MDCSTAT00101"
        private const val BLD_INDEX_OHLCV_BY_TICKER = "dbms/MDC/STAT/standard/MDCSTAT00301"
        private const val BLD_INDEX_FUNDAMENTAL_BY_DATE = "dbms/MDC/STAT/standard/MDCSTAT00601"
        private const val BLD_INDEX_FUNDAMENTAL_BY_TICKER = "dbms/MDC/STAT/standard/MDCSTAT00701"
        private const val BLD_INDEX_PRICE_CHANGE = "dbms/MDC/STAT/standard/MDCSTAT00501"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    override suspend fun getIndexList(market: IndexMarket): List<IndexInfo> {
        logger.debug { "Fetching index list for market: $market" }

        // ALL 시장의 경우 개별 시장 데이터를 모두 조회하여 합침
        if (market == IndexMarket.ALL) {
            logger.debug { "Fetching all markets (KOSPI + KOSDAQ + DERIVATIVES)" }
            val kospiIndexes = getIndexList(IndexMarket.KOSPI)
            val kosdaqIndexes = getIndexList(IndexMarket.KOSDAQ)
            val derivativesIndexes = getIndexList(IndexMarket.DERIVATIVES)
            return (kospiIndexes + kosdaqIndexes + derivativesIndexes)
                .also { logger.debug { "Fetched total ${it.size} indexes from all markets" } }
        }

        rateLimiter.acquire()

        val parameters = mapOf(
            "bld" to BLD_INDEX_LIST,
            "mktId" to market.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexInfo(
                ticker = raw.getString("IDX_IND_CD"),
                name = raw.getString("IDX_NM"),
                market = market,
                baseDate = raw.getStringOrNull("BASE_TM")?.replace(".", "/")?.toKrxDate()?.takeIf { it != LocalDate.MIN },
                announcementDate = raw.getStringOrNull("ANN_TM")?.replace(".", "/")?.toKrxDate()?.takeIf { it != LocalDate.MIN },
                baseIndex = raw.getStringOrNull("BASE_IDX")?.toKrxBigDecimal(),
                constituentCount = raw.getStringOrNull("COMPST_ISU_CNT")?.toKrxInt()
            )
        }.also { logger.debug { "Fetched ${it.size} indexes for market ${market.koreanName}" } }
    }

    override suspend fun getIndexConstituents(ticker: String, date: LocalDate): List<String> {
        rateLimiter.acquire()
        logger.debug { "Fetching index constituents for ticker: $ticker, date: $date" }

        val parameters = mapOf(
            "bld" to BLD_INDEX_CONSTITUENTS,
            "trdDd" to date.format(dateFormatter),
            "indIdx" to ticker
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            raw.getString("ISU_SRT_CD")
        }.also { logger.debug { "Fetched ${it.size} constituents" } }
    }

    override suspend fun getOhlcvByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<IndexOhlcv> {
        rateLimiter.acquire()
        logger.debug { "Fetching index OHLCV for ticker: $ticker, from: $fromDate, to: $toDate" }

        val parameters = mapOf(
            "bld" to BLD_INDEX_OHLCV_BY_DATE,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "indIdx" to ticker
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexOhlcv(
                date = raw.getString("TRD_DD").toKrxDate(),
                ticker = ticker,
                open = raw.getString("TDD_OPNPRC").toKrxBigDecimal(),
                high = raw.getString("TDD_HGPRC").toKrxBigDecimal(),
                low = raw.getString("TDD_LWPRC").toKrxBigDecimal(),
                close = raw.getString("TDD_CLSPRC").toKrxBigDecimal(),
                volume = raw.getString("ACC_TRDVOL").toKrxLong(),
                tradingValue = raw.getStringOrNull("ACC_TRDVAL")?.toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} OHLCV records" } }
    }

    override suspend fun getOhlcvByTicker(
        date: LocalDate,
        market: IndexMarket
    ): List<IndexOhlcvSnapshot> {
        logger.debug { "Fetching all index OHLCV for date: $date, market: $market" }

        // ALL 시장의 경우 개별 시장 데이터를 모두 조회하여 합침
        if (market == IndexMarket.ALL) {
            logger.debug { "Fetching OHLCV from all markets (KOSPI + KOSDAQ + DERIVATIVES)" }
            val kospiOhlcv = getOhlcvByTicker(date, IndexMarket.KOSPI)
            val kosdaqOhlcv = getOhlcvByTicker(date, IndexMarket.KOSDAQ)
            val derivativesOhlcv = getOhlcvByTicker(date, IndexMarket.DERIVATIVES)
            return (kospiOhlcv + kosdaqOhlcv + derivativesOhlcv)
                .also { logger.debug { "Fetched total ${it.size} OHLCV snapshots from all markets" } }
        }

        rateLimiter.acquire()

        val parameters = mapOf(
            "bld" to BLD_INDEX_OHLCV_BY_TICKER,
            "trdDd" to date.format(dateFormatter),
            "mktId" to market.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexOhlcvSnapshot(
                name = raw.getString("IDX_NM"),
                open = raw.getString("TDD_OPNPRC").toKrxBigDecimal(),
                high = raw.getString("TDD_HGPRC").toKrxBigDecimal(),
                low = raw.getString("TDD_LWPRC").toKrxBigDecimal(),
                close = raw.getString("TDD_CLSPRC").toKrxBigDecimal(),
                volume = raw.getString("ACC_TRDVOL").toKrxLong(),
                tradingValue = raw.getStringOrNull("ACC_TRDVAL")?.toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} index snapshots for market ${market.koreanName}" } }
    }

    override suspend fun getFundamentalByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<IndexFundamental> {
        rateLimiter.acquire()
        logger.debug { "Fetching index fundamental for ticker: $ticker, from: $fromDate, to: $toDate" }

        val parameters = mapOf(
            "bld" to BLD_INDEX_FUNDAMENTAL_BY_DATE,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "indIdx" to ticker
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexFundamental(
                date = raw.getString("TRD_DD").toKrxDate(),
                ticker = ticker,
                close = raw.getString("CLSPRC").toKrxBigDecimal(),
                changeRate = raw.getStringOrNull("FLUC_RT")?.toKrxRate(),
                per = raw.getStringOrNull("PER")?.toKrxBigDecimal(),
                forwardPer = raw.getStringOrNull("FWD_PER")?.toKrxBigDecimal(),
                pbr = raw.getStringOrNull("PBR")?.toKrxBigDecimal(),
                dividendYield = raw.getStringOrNull("DVD_YLD")?.toKrxRate()
            )
        }.also { logger.debug { "Fetched ${it.size} fundamental records" } }
    }

    override suspend fun getFundamentalByTicker(
        date: LocalDate,
        market: IndexMarket
    ): List<IndexFundamentalSnapshot> {
        logger.debug { "Fetching all index fundamental for date: $date, market: $market" }

        // ALL 시장의 경우 개별 시장 데이터를 모두 조회하여 합침
        if (market == IndexMarket.ALL) {
            logger.debug { "Fetching fundamentals from all markets (KOSPI + KOSDAQ + DERIVATIVES)" }
            val kospiFundamentals = getFundamentalByTicker(date, IndexMarket.KOSPI)
            val kosdaqFundamentals = getFundamentalByTicker(date, IndexMarket.KOSDAQ)
            val derivativesFundamentals = getFundamentalByTicker(date, IndexMarket.DERIVATIVES)
            return (kospiFundamentals + kosdaqFundamentals + derivativesFundamentals)
                .also { logger.debug { "Fetched total ${it.size} fundamental snapshots from all markets" } }
        }

        rateLimiter.acquire()

        val parameters = mapOf(
            "bld" to BLD_INDEX_FUNDAMENTAL_BY_TICKER,
            "trdDd" to date.format(dateFormatter),
            "mktId" to market.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexFundamentalSnapshot(
                name = raw.getString("IDX_NM"),
                close = raw.getString("CLSPRC").toKrxBigDecimal(),
                changeRate = raw.getStringOrNull("FLUC_RT")?.toKrxRate(),
                per = raw.getStringOrNull("PER")?.toKrxBigDecimal(),
                forwardPer = raw.getStringOrNull("FWD_PER")?.toKrxBigDecimal(),
                pbr = raw.getStringOrNull("PBR")?.toKrxBigDecimal(),
                dividendYield = raw.getStringOrNull("DVD_YLD")?.toKrxRate()
            )
        }.also { logger.debug { "Fetched ${it.size} fundamental snapshots for market ${market.koreanName}" } }
    }

    override suspend fun getPriceChange(
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket
    ): List<IndexPriceChange> {
        logger.debug { "Fetching index price change from: $fromDate, to: $toDate, market: $market" }

        // ALL 시장의 경우 개별 시장 데이터를 모두 조회하여 합침
        if (market == IndexMarket.ALL) {
            logger.debug { "Fetching price changes from all markets (KOSPI + KOSDAQ + DERIVATIVES)" }
            val kospiChanges = getPriceChange(fromDate, toDate, IndexMarket.KOSPI)
            val kosdaqChanges = getPriceChange(fromDate, toDate, IndexMarket.KOSDAQ)
            val derivativesChanges = getPriceChange(fromDate, toDate, IndexMarket.DERIVATIVES)
            return (kospiChanges + kosdaqChanges + derivativesChanges)
                .also { logger.debug { "Fetched total ${it.size} price changes from all markets" } }
        }

        rateLimiter.acquire()

        val parameters = mapOf(
            "bld" to BLD_INDEX_PRICE_CHANGE,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "mktId" to market.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexPriceChange(
                name = raw.getString("IDX_NM"),
                openPrice = raw.getString("OPNPRC").toKrxBigDecimal(),
                closePrice = raw.getString("CLSPRC").toKrxBigDecimal(),
                changeRate = raw.getString("FLUC_RT").toKrxRate(),
                volume = raw.getString("ACC_TRDVOL").toKrxLong(),
                tradingValue = raw.getStringOrNull("ACC_TRDVAL")?.toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} price change records" } }
    }
}
