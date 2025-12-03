package dev.kairoscode.kfc.domain.stock

import java.math.BigDecimal

/**
 * 종목 섹터/산업 분류 정보
 *
 * 종목의 섹터, 산업 분류 및 시가총액 정보를 담는 모델입니다.
 * KRX API MDCSTAT03901 (업종분류현황) 응답을 매핑합니다.
 *
 * @property ticker 종목 코드 (6자리, 예: "005930")
 * @property name 종목명 (예: "삼성전자")
 * @property market 시장 구분 (KOSPI, KOSDAQ 등)
 * @property industry 산업 분류명 (예: "전기전자")
 * @property closePrice 종가 (원, null 가능)
 * @property marketCap 시가총액 (원, null 가능)
 * @property priceChangeType 등락 구분 (상승/하락/보합, null 가능)
 */
data class StockSectorInfo(
    val ticker: String,
    val name: String,
    val market: Market,
    val industry: String,
    val closePrice: Long?,
    val marketCap: Long?,
    val priceChangeType: PriceChangeType?
) {
    /**
     * 섹터 내 비중 계산
     *
     * @param totalMarketCap 산업 전체 시가총액
     * @return 비중 (백분율, 0-100)
     */
    fun calculateSectorWeight(totalMarketCap: Long): BigDecimal {
        if (marketCap == null || totalMarketCap == 0L) return BigDecimal.ZERO
        return (marketCap.toBigDecimal() / totalMarketCap.toBigDecimal()) * BigDecimal(100)
    }

    /**
     * 가격 상승 여부
     */
    fun isPriceRising(): Boolean = priceChangeType == PriceChangeType.RISE
}
