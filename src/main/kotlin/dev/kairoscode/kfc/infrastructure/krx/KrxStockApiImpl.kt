package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.stock.*
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimiter
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimitingSettings
import dev.kairoscode.kfc.infrastructure.common.ratelimit.TokenBucketRateLimiter
import dev.kairoscode.kfc.infrastructure.common.util.*
import dev.kairoscode.kfc.infrastructure.krx.internal.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * KRX 주식 종목 정보 API 구현체
 *
 * KrxStockApi 인터페이스의 내부 구현입니다.
 * HTTP 클라이언트를 사용하여 실제 KRX API와 통신합니다.
 */
internal class KrxStockApiImpl(
    private val httpClient: KrxHttpClient = KrxHttpClient(),
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.krxDefault())
) : KrxStockApi {

    companion object {
        private const val BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd"

        // BLD 코드 상수
        private const val BLD_LISTED_STOCKS = "dbms/comm/finder/finder_stkisu"
        private const val BLD_DELISTED_STOCKS = "dbms/comm/finder/finder_listdelisu"
        private const val BLD_SECTOR_CLASSIFICATIONS = "dbms/MDC/STAT/standard/MDCSTAT03901"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    override suspend fun getStockList(
        market: Market,
        listingStatus: ListingStatus
    ): List<StockListItem> {
        rateLimiter.acquire()
        logger.debug { "Fetching stock list for market: $market, status: $listingStatus" }

        val bld = when (listingStatus) {
            ListingStatus.LISTED -> BLD_LISTED_STOCKS
            ListingStatus.DELISTED -> BLD_DELISTED_STOCKS
        }

        val parameters = mapOf(
            "bld" to bld,
            "mktsel" to market.code,
            "searchText" to "",
            "typeNo" to "0"
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "block1" 필드 추출
        val block1 = (response["block1"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return block1.map { raw ->
            StockListItem(
                ticker = raw.getString(KrxApiFields.Stock.SHORT_CODE),
                name = raw.getString(KrxApiFields.Stock.CODE_NAME),
                isin = raw.getString(KrxApiFields.Stock.FULL_CODE),
                market = raw.getString(KrxApiFields.Stock.MARKET_CODE).toMarket(),
                listingStatus = listingStatus
            )
        }.also { logger.debug { "Fetched ${it.size} stocks" } }
    }

    override suspend fun getSectorClassifications(
        date: LocalDate,
        market: Market
    ): List<StockSectorInfo> {
        rateLimiter.acquire()
        logger.debug { "Fetching sector classifications for date: $date, market: $market" }

        val parameters = mapOf(
            "bld" to BLD_SECTOR_CLASSIFICATIONS,
            "trdDd" to date.format(dateFormatter),
            "mktId" to market.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "block1" 필드 추출 (KRX MDCSTAT03901 응답은 block1 사용)
        val output = (response["block1"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        return output.map { raw ->
            StockSectorInfo(
                ticker = raw.getString(KrxApiFields.Identity.TICKER),
                name = raw.getString(KrxApiFields.Identity.NAME_SHORT),
                market = market,
                industry = raw.getString(KrxApiFields.Index.NAME),
                closePrice = raw.getString(KrxApiFields.Price.CLOSE)
                    .replace(",", "").toLongOrNull(),
                marketCap = raw.getString(KrxApiFields.Asset.MARKET_CAP)
                    .replace(",", "").toLongOrNull(),
                priceChangeType = raw.getString(KrxApiFields.PriceChange.DIRECTION).toPriceChangeType()
            )
        }.also { logger.debug { "Fetched ${it.size} sector classifications" } }
    }
}

/**
 * String → Market Enum 변환
 *
 * KRX API 시장 코드를 Market enum으로 변환합니다.
 *
 * @return Market enum
 */
private fun String.toMarket(): Market {
    return when (this) {
        "STK" -> Market.KOSPI
        "KSQ" -> Market.KOSDAQ
        "KNX" -> Market.KONEX
        else -> Market.ALL
    }
}

/**
 * String → PriceChangeType Enum 변환
 *
 * KRX API 등락 구분 코드를 PriceChangeType enum으로 변환합니다.
 *
 * @return PriceChangeType enum, 매칭 실패 시 null
 */
private fun String.toPriceChangeType(): PriceChangeType? {
    return when (this) {
        "1" -> PriceChangeType.RISE
        "2" -> PriceChangeType.FALL
        "3" -> PriceChangeType.UNCHANGED
        else -> null
    }
}
