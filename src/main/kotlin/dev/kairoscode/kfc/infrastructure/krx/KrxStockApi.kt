package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.stock.*
import java.time.LocalDate

/**
 * KRX Stock API 내부 인터페이스
 *
 * KRX API를 사용하여 주식 종목 정보를 조회하는 내부 인터페이스입니다.
 * 이 인터페이스는 라이브러리 내부에서만 사용되며, 외부에 노출되지 않습니다.
 */
internal interface KrxStockApi {

    /**
     * 종목 리스트 조회
     *
     * @param market 시장 구분
     * @param listingStatus 상장 상태
     * @return 종목 리스트
     */
    suspend fun getStockList(
        market: Market,
        listingStatus: ListingStatus
    ): List<StockListItem>

    /**
     * 업종분류 현황 조회
     *
     * @param date 조회 날짜
     * @param market 시장 구분
     * @return 업종분류 현황 목록
     */
    suspend fun getSectorClassifications(
        date: LocalDate,
        market: Market
    ): List<StockSectorInfo>
}
