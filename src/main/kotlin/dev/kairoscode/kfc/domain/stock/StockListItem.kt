package dev.kairoscode.kfc.domain.stock

/**
 * 종목 리스트 항목
 *
 * 간단한 종목 목록 조회 시 사용하는 경량 모델입니다.
 * 종목 리스트 페이지네이션, 검색 결과 등에 적합합니다.
 *
 * @property ticker 종목 코드 (6자리)
 * @property name 종목명
 * @property isin ISIN 코드 (12자리)
 * @property market 시장 구분
 * @property listingStatus 상장 상태
 */
data class StockListItem(
    val ticker: String,
    val name: String,
    val isin: String,
    val market: Market,
    val listingStatus: ListingStatus
) {
    /**
     * 코스피 종목 여부 확인
     */
    fun isKospi(): Boolean = market == Market.KOSPI

    /**
     * 코스닥 종목 여부 확인
     */
    fun isKosdaq(): Boolean = market == Market.KOSDAQ

    /**
     * 상장 종목 여부 확인
     */
    fun isListed(): Boolean = listingStatus == ListingStatus.LISTED
}
