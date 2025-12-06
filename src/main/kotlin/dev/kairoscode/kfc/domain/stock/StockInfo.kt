package dev.kairoscode.kfc.domain.stock

import java.time.LocalDate

/**
 * 종목 기본정보
 *
 * 개별 종목의 상세 메타데이터를 담는 모델입니다.
 * 종목 상세 페이지, 포트폴리오 관리 등에 활용됩니다.
 *
 * @property ticker 종목 코드 (6자리)
 * @property name 종목명
 * @property fullName 정식 종목명 (null 가능)
 * @property isin ISIN 코드 (12자리)
 * @property market 시장 구분
 * @property listingStatus 상장 상태
 * @property listingDate 상장일 (null 가능)
 * @property sharesOutstanding 발행주식수 (null 가능)
 */
data class StockInfo(
    val ticker: String,
    val name: String,
    val fullName: String? = null,
    val isin: String,
    val market: Market,
    val listingStatus: ListingStatus,
    val listingDate: LocalDate? = null,
    val sharesOutstanding: Long? = null,
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
