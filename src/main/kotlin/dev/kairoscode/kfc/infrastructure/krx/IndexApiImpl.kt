package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.api.IndexApi
import dev.kairoscode.kfc.domain.index.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

/**
 * IndexApi 구현체
 *
 * KRX API를 사용하여 지수 정보를 조회합니다.
 *
 * @param krxIndexApi KRX 지수 API 구현체
 */
internal class IndexApiImpl(
    private val krxIndexApi: KrxIndexApi
) : IndexApi {

    override suspend fun getIndexList(market: IndexMarket): List<IndexInfo> {
        logger.debug { "getIndexList: market=$market" }
        return krxIndexApi.getIndexList(market)
    }

    override suspend fun getIndexName(ticker: String): String? {
        logger.debug { "getIndexName: ticker=$ticker" }
        return krxIndexApi.getIndexList(IndexMarket.ALL)
            .find { it.ticker == ticker }
            ?.name
    }

    override suspend fun getIndexInfo(ticker: String): IndexInfo? {
        logger.debug { "getIndexInfo: ticker=$ticker" }
        return krxIndexApi.getIndexList(IndexMarket.ALL)
            .find { it.ticker == ticker }
    }

    override suspend fun getIndexConstituents(ticker: String, date: LocalDate): List<String> {
        logger.debug { "getIndexConstituents: ticker=$ticker, date=$date" }
        return krxIndexApi.getIndexConstituents(ticker, date)
    }

    override suspend fun getOhlcvByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<IndexOhlcv> {
        logger.debug { "getOhlcvByDate: ticker=$ticker, from=$fromDate, to=$toDate" }
        return krxIndexApi.getOhlcvByDate(ticker, fromDate, toDate)
    }

    override suspend fun getOhlcvByTicker(
        date: LocalDate,
        market: IndexMarket
    ): List<IndexOhlcvSnapshot> {
        logger.debug { "getOhlcvByTicker: date=$date, market=$market" }
        return krxIndexApi.getOhlcvByTicker(date, market)
    }

    override suspend fun getFundamentalByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<IndexFundamental> {
        logger.debug { "getFundamentalByDate: ticker=$ticker, from=$fromDate, to=$toDate" }
        return krxIndexApi.getFundamentalByDate(ticker, fromDate, toDate)
    }

    override suspend fun getFundamentalByTicker(
        date: LocalDate,
        market: IndexMarket
    ): List<IndexFundamentalSnapshot> {
        logger.debug { "getFundamentalByTicker: date=$date, market=$market" }
        return krxIndexApi.getFundamentalByTicker(date, market)
    }

    override suspend fun getPriceChange(
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket
    ): List<IndexPriceChange> {
        logger.debug { "getPriceChange: from=$fromDate, to=$toDate, market=$market" }
        return krxIndexApi.getPriceChange(fromDate, toDate, market)
    }
}
