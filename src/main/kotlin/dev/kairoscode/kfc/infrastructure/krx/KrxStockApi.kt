package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.stock.ListingStatus
import dev.kairoscode.kfc.domain.stock.Market
import dev.kairoscode.kfc.domain.stock.StockListItem
import dev.kairoscode.kfc.domain.stock.StockSectorInfo
import java.time.LocalDate

/**
 * KRX 주식 종목 정보 API 인터페이스 (내부용)
 *
 * KRX API를 통해 주식 종목 관련 데이터를 조회하는 내부 인터페이스입니다.
 * 이 인터페이스는 infrastructure 레이어 내부에서만 사용되며, 외부에 노출되지 않습니다.
 */
internal interface KrxStockApi {

    /**
     * 종목 리스트 조회
     *
     * 특정 시장의 모든 종목 목록을 조회합니다.
     *
     * @param market 시장 구분 (KOSPI, KOSDAQ, KONEX, ALL)
     * @param listingStatus 상장 상태 (LISTED: 상장, DELISTED: 상폐)
     * @return 종목 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu / finder_listdelisu)
     */
    suspend fun getStockList(
        market: Market,
        listingStatus: ListingStatus
    ): List<StockListItem>

    /**
     * 업종분류 현황 조회
     *
     * 모든 종목의 산업 분류 및 시가총액 정보를 조회합니다.
     *
     * @param date 조회 날짜
     * @param market 시장 구분
     * @return 업종분류 현황 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT03901)
     */
    suspend fun getSectorClassifications(
        date: LocalDate,
        market: Market
    ): List<StockSectorInfo>
}
