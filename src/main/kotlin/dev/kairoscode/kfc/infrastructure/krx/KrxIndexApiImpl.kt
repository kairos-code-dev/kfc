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
        // MDCSTAT00401: 전체지수 기본정보 (지수 목록)
        private const val BLD_INDEX_LIST = "dbms/MDC/STAT/standard/MDCSTAT00401"
        // MDCSTAT00201: 전체지수 등락률 (기간별)
        private const val BLD_INDEX_PRICE_CHANGE = "dbms/MDC/STAT/standard/MDCSTAT00201"
        // Note: BLD_INDEX_CONSTITUENTS도 MDCSTAT00401이지만 다른 파라미터를 사용 (지수코드 지정 시 구성종목 반환)
        // MDCSTAT00101: 전체지수시세 (특정 일자 전체 지수)
        private const val BLD_INDEX_OHLCV_BY_DATE = "dbms/MDC/STAT/standard/MDCSTAT00101"
        // MDCSTAT00301: 개별지수시세 (특정 지수 기간별)
        private const val BLD_INDEX_OHLCV_BY_TICKER = "dbms/MDC/STAT/standard/MDCSTAT00301"
        // MDCSTAT00601: 지수구성종목
        private const val BLD_INDEX_CONSTITUENTS_DETAIL = "dbms/MDC/STAT/standard/MDCSTAT00601"
        // MDCSTAT00701: 전체지수 PER/PBR/배당수익률 (특정 일자 전체 지수)
        private const val BLD_INDEX_FUNDAMENTAL_BY_TICKER = "dbms/MDC/STAT/standard/MDCSTAT00701"
        // MDCSTAT00702: 개별지수 PER/PBR/배당수익률 (특정 지수 기간별)
        private const val BLD_INDEX_FUNDAMENTAL_BY_DATE = "dbms/MDC/STAT/standard/MDCSTAT00702"
        // Note: 전체지수 등락률은 BLD_INDEX_LIST (MDCSTAT00201)와 동일한 BLD 사용 (날짜 파라미터로 구분)

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

        // MDCSTAT00401: 전체지수 기본정보
        // - idxIndMidclssCd: 시장 구분 코드 (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마)
        val parameters = mapOf(
            "bld" to BLD_INDEX_LIST,
            "idxIndMidclssCd" to market.ohlcvCode
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            // pykrx 방식: ticker = IND_TP_CD + IDX_IND_CD (예: "1" + "001" = "1001")
            val groupCode = raw.getString("IND_TP_CD")
            val indexCode = raw.getString("IDX_IND_CD")
            IndexInfo(
                ticker = groupCode + indexCode,
                name = raw.getString("IDX_NM"),
                market = market,
                baseDate = raw.getStringOrNull("BAS_TM_CONTN")?.replace(".", "/")?.toKrxDate()?.takeIf { it != LocalDate.MIN },
                announcementDate = raw.getStringOrNull("ANNC_TM_CONTN")?.replace(".", "/")?.toKrxDate()?.takeIf { it != LocalDate.MIN },
                baseIndex = raw.getStringOrNull("BAS_IDX_CONTN")?.toKrxBigDecimal(),
                constituentCount = raw.getStringOrNull("COMPST_ISU_CNT")?.toKrxInt()
            )
        }.also { logger.debug { "Fetched ${it.size} indexes for market ${market.koreanName}" } }
    }

    override suspend fun getIndexConstituents(ticker: String, date: LocalDate, market: IndexMarket): List<String> {
        rateLimiter.acquire()
        logger.debug { "Fetching index constituents for ticker: $ticker, market: $market, date: $date" }

        // MDCSTAT00601: 지수구성종목
        // pykrx 방식: ticker = "1028" → indIdx = "1" (첫 글자 = 시장코드), indIdx2 = "028" (나머지 = 지수코드)
        // - indIdx: 시장 구분 코드 (1=KOSPI, 2=KOSDAQ, 3=테마)
        // - indIdx2: 지수 코드 (시장코드 제외)
        // - trdDd: 거래일
        val groupId = ticker.first().toString()
        val indexCode = ticker.drop(1)

        val parameters = mapOf(
            "bld" to BLD_INDEX_CONSTITUENTS_DETAIL,
            "trdDd" to date.format(dateFormatter),
            "indIdx2" to indexCode,
            "indIdx" to groupId
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
        toDate: LocalDate,
        market: IndexMarket
    ): List<IndexOhlcv> {
        rateLimiter.acquire()
        logger.debug { "Fetching index OHLCV for ticker: $ticker, market: $market, from: $fromDate, to: $toDate" }

        // MDCSTAT00301: 개별지수 시세 추이
        // pykrx 방식: ticker = "1001" → indIdx = "1" (첫 글자 = 시장코드), indIdx2 = "001" (나머지 = 지수코드)
        // - indIdx: 시장 구분 코드 (1=KOSPI, 2=KOSDAQ, 3=테마)
        // - indIdx2: 지수 코드 (시장코드 제외)
        val groupId = ticker.first().toString()
        val indexCode = ticker.drop(1)

        val parameters = mapOf(
            "bld" to BLD_INDEX_OHLCV_BY_TICKER,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "indIdx2" to indexCode,
            "indIdx" to groupId
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexOhlcv(
                date = raw.getString("TRD_DD").toKrxDate(),
                ticker = ticker,
                open = raw.getString("OPNPRC_IDX").toKrxBigDecimal(),
                high = raw.getString("HGPRC_IDX").toKrxBigDecimal(),
                low = raw.getString("LWPRC_IDX").toKrxBigDecimal(),
                close = raw.getString("CLSPRC_IDX").toKrxBigDecimal(),
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

        // MDCSTAT00101: 전체지수 시세
        // - trdDd: 거래일
        // - idxIndMidclssCd: 시장 구분 코드 (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마)
        val parameters = mapOf(
            "bld" to BLD_INDEX_OHLCV_BY_DATE,
            "trdDd" to date.format(dateFormatter),
            "idxIndMidclssCd" to market.ohlcvCode
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexOhlcvSnapshot(
                name = raw.getString("IDX_NM"),
                open = raw.getStringOrNull("OPNPRC_IDX")?.toKrxBigDecimal() ?: raw.getString("TDD_OPNPRC").toKrxBigDecimal(),
                high = raw.getStringOrNull("HGPRC_IDX")?.toKrxBigDecimal() ?: raw.getString("TDD_HGPRC").toKrxBigDecimal(),
                low = raw.getStringOrNull("LWPRC_IDX")?.toKrxBigDecimal() ?: raw.getString("TDD_LWPRC").toKrxBigDecimal(),
                close = raw.getStringOrNull("CLSPRC_IDX")?.toKrxBigDecimal() ?: raw.getString("TDD_CLSPRC").toKrxBigDecimal(),
                volume = raw.getString("ACC_TRDVOL").toKrxLong(),
                tradingValue = raw.getStringOrNull("ACC_TRDVAL")?.toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} index snapshots for market ${market.koreanName}" } }
    }

    override suspend fun getFundamentalByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket
    ): List<IndexFundamental> {
        rateLimiter.acquire()
        logger.debug { "Fetching index fundamental for ticker: $ticker, market: $market, from: $fromDate, to: $toDate" }

        // MDCSTAT00702: 개별지수 PER/PBR/배당수익률
        // pykrx 방식: ticker = "1001" → indTpCd = "1" (첫 글자 = 시장코드), indTpCd2 = "001" (나머지 = 지수코드)
        // - indTpCd: 시장 구분 코드 (1=KOSPI, 2=KOSDAQ, etc.)
        // - indTpCd2: 지수 코드 (시장코드 제외)
        val groupId = ticker.first().toString()
        val indexCode = ticker.drop(1)

        val parameters = mapOf(
            "bld" to BLD_INDEX_FUNDAMENTAL_BY_DATE,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "indTpCd" to groupId,
            "indTpCd2" to indexCode
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexFundamental(
                date = raw.getString("TRD_DD").toKrxDate(),
                ticker = ticker,
                close = raw.getString("CLSPRC_IDX").toKrxBigDecimal(),
                changeRate = raw.getStringOrNull("FLUC_RT")?.toKrxRate(),
                per = raw.getStringOrNull("WT_PER")?.toKrxBigDecimal(),
                forwardPer = raw.getStringOrNull("FWD_PER")?.toKrxBigDecimal(),
                pbr = raw.getStringOrNull("WT_STKPRC_NETASST_RTO")?.toKrxBigDecimal(),
                dividendYield = raw.getStringOrNull("DIV_YD")?.toKrxRate()
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

        // MDCSTAT00701: 전체지수 PER/PBR/배당수익률
        // - trdDd: 거래일
        // - idxIndMidclssCd: 시장 구분 코드 (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마)
        val parameters = mapOf(
            "bld" to BLD_INDEX_FUNDAMENTAL_BY_TICKER,
            "trdDd" to date.format(dateFormatter),
            "idxIndMidclssCd" to market.ohlcvCode
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexFundamentalSnapshot(
                name = raw.getString("IDX_NM"),
                close = raw.getStringOrNull("CLSPRC_IDX")?.toKrxBigDecimal() ?: raw.getString("CLSPRC").toKrxBigDecimal(),
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

        // MDCSTAT00201: 전체지수 등락률 (기간별)
        // - strtDd: 시작일
        // - endDd: 종료일
        // - idxIndMidclssCd: 시장 구분 코드 (01=KRX, 02=KOSPI, 03=KOSDAQ, 04=테마)
        val parameters = mapOf(
            "bld" to BLD_INDEX_PRICE_CHANGE,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "idxIndMidclssCd" to market.ohlcvCode
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        val output = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            IndexPriceChange(
                name = raw.getString("IDX_IND_NM"),
                openPrice = raw.getString("OPN_DD_INDX").toKrxBigDecimal(),
                closePrice = raw.getString("END_DD_INDX").toKrxBigDecimal(),
                changeRate = raw.getString("FLUC_RT").toKrxRate(),
                volume = raw.getString("ACC_TRDVOL").toKrxLong(),
                tradingValue = raw.getStringOrNull("ACC_TRDVAL")?.toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} price change records" } }
    }
}
