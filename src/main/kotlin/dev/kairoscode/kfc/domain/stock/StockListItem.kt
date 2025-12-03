package dev.kairoscode.kfc.domain.stock

/**
 * 종목 리스트 항목
 *
 * 간단한 종목 목록 조회 시 사용하는 경량 모델입니다.
 * KRX API finder_stkisu 또는 finder_listdelisu 응답을 매핑합니다.
 *
 * @property ticker 종목 코드 (6자리, 예: "005930")
 * @property name 종목명 (예: "삼성전자")
 * @property isin ISIN 코드 (12자리, 예: "KR7005930003")
 * @property market 시장 구분 (KOSPI, KOSDAQ, KONEX 등)
 * @property listingStatus 상장 상태 (상장/상폐)
 */
data class StockListItem(
    val ticker: String,
    val name: String,
    val isin: String,
    val market: Market,
    val listingStatus: ListingStatus
) {
    /**
     * 코스피 종목 여부
     */
    fun isKospi(): Boolean = market == Market.KOSPI

    /**
     * 코스닥 종목 여부
     */
    fun isKosdaq(): Boolean = market == Market.KOSDAQ

    /**
     * 상장 종목 여부
     */
    fun isListed(): Boolean = listingStatus == ListingStatus.LISTED
}
