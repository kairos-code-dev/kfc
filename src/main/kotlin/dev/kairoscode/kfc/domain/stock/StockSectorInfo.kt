package dev.kairoscode.kfc.domain.stock

import java.math.BigDecimal

/**
 * 섹터/산업 분류 정보
 *
 * 종목의 섹터, 산업 분류 및 시가총액 정보를 담는 모델입니다.
 * 업종별 분석, 섹터 로테이션 전략 등에 활용됩니다.
 *
 * @property ticker 종목 코드 (6자리)
 * @property name 종목명
 * @property market 시장 구분
 * @property industry 산업 분류명
 * @property closePrice 종가 (원, null 가능)
 * @property marketCap 시가총액 (원, null 가능)
 * @property priceChangeType 등락 구분 (null 가능)
 */
data class StockSectorInfo(
    val ticker: String,
    val name: String,
    val market: Market,
    val industry: String,
    val closePrice: Long? = null,
    val marketCap: Long? = null,
    val priceChangeType: PriceChangeType? = null
) {
    /**
     * 섹터 내 비중 계산
     *
     * @param totalMarketCap 섹터 전체 시가총액
     * @return 섹터 내 비중 (%), 시가총액이 null이면 BigDecimal.ZERO 반환
     */
    fun calculateSectorWeight(totalMarketCap: Long): BigDecimal {
        if (marketCap == null || totalMarketCap == 0L) return BigDecimal.ZERO
        return (marketCap.toBigDecimal() / totalMarketCap.toBigDecimal()) * BigDecimal(100)
    }

    /**
     * 주가 상승 여부 확인
     */
    fun isPriceRising(): Boolean = priceChangeType == PriceChangeType.RISE

    /**
     * 주가 하락 여부 확인
     */
    fun isPriceFalling(): Boolean = priceChangeType == PriceChangeType.FALL
}

/**
 * List<StockSectorInfo> 확장 함수
 */

/**
 * 산업별로 그룹화
 */
fun List<StockSectorInfo>.groupByIndustry(): Map<String, List<StockSectorInfo>> {
    return groupBy { it.industry }
}

/**
 * 최소 시가총액으로 필터링
 *
 * @param minCap 최소 시가총액 (원)
 * @return 필터링된 종목 리스트
 */
fun List<StockSectorInfo>.filterByMarketCap(minCap: Long): List<StockSectorInfo> {
    return filter { (it.marketCap ?: 0L) >= minCap }
}

/**
 * 시가총액 순으로 정렬
 *
 * @param descending true면 내림차순, false면 오름차순 (기본값: true)
 * @return 정렬된 종목 리스트
 */
fun List<StockSectorInfo>.sortByMarketCap(descending: Boolean = true): List<StockSectorInfo> {
    return if (descending) {
        sortedByDescending { it.marketCap ?: 0L }
    } else {
        sortedBy { it.marketCap ?: 0L }
    }
}

/**
 * 전체 시가총액 합계 계산
 *
 * @return 전체 시가총액 (원)
 */
fun List<StockSectorInfo>.calculateTotalMarketCap(): Long {
    return sumOf { it.marketCap ?: 0L }
}
