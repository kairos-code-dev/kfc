package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.api.StockApi
import dev.kairoscode.kfc.domain.stock.*
import java.time.LocalDate

/**
 * 주식 종목 정보 API 공개 구현체
 *
 * StockApi 인터페이스의 구현체로, KrxStockApi에 작업을 위임합니다.
 * 이 클래스는 infrastructure 레이어에 속하지만, 공개 API 레이어에 대한 구현을 제공합니다.
 */
internal class StockApiImpl(
    private val krxStockApi: KrxStockApi
) : StockApi {

    override suspend fun getStockList(
        market: Market,
        listingStatus: ListingStatus
    ): List<StockListItem> {
        return krxStockApi.getStockList(market, listingStatus)
    }

    override suspend fun getStockInfo(ticker: String): StockInfo? {
        // ticker로 종목 검색 후 StockInfo 구성
        val allStocks = krxStockApi.getStockList(Market.ALL, ListingStatus.LISTED)
        return allStocks.find { it.ticker == ticker }?.let {
            StockInfo(
                ticker = it.ticker,
                name = it.name,
                fullName = null,  // API에서 제공하지 않음
                isin = it.isin,
                market = it.market,
                listingStatus = it.listingStatus,
                listingDate = null,  // finder API에서는 제공하지 않음
                sharesOutstanding = null  // 별도 API 필요
            )
        }
    }

    override suspend fun getStockName(ticker: String): String? {
        return getStockInfo(ticker)?.name
    }

    override suspend fun getSectorClassifications(
        date: LocalDate,
        market: Market
    ): List<StockSectorInfo> {
        return krxStockApi.getSectorClassifications(date, market)
    }

    override suspend fun getIndustryGroups(
        date: LocalDate,
        market: Market
    ): List<IndustryClassification> {
        val sectors = getSectorClassifications(date, market)

        return sectors.groupBy { it.industry }.map { (industryName, stocks) ->
            IndustryClassification(
                industryName = industryName,
                market = market,
                stocks = stocks,
                totalMarketCap = stocks.sumOf { it.marketCap ?: 0L },
                stockCount = stocks.size
            )
        }
    }

    override suspend fun searchStocks(
        keyword: String,
        market: Market
    ): List<StockListItem> {
        // 방법 1: 클라이언트 측 필터링 (현재 구현)
        // - 장점: 간단, KRX API searchText 파라미터 동작 불안정
        // - 단점: 전체 목록 조회 필요 (캐싱으로 완화 가능)
        val allStocks = krxStockApi.getStockList(market, ListingStatus.LISTED)
        return allStocks.filter {
            it.name.contains(keyword, ignoreCase = true) ||
            it.ticker.contains(keyword, ignoreCase = true)
        }

        // 방법 2: 서버 측 검색 (Phase 2 고려)
        // - KRX API의 searchText 파라미터 활용
        // - 단점: 검색 결과가 불안정할 수 있음
        // return krxStockApi.searchStocks(keyword, market)
    }
}
