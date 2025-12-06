package dev.kairoscode.kfc.domain.stock

/**
 * 산업 분류 상세
 *
 * 특정 산업에 속한 모든 종목을 그룹화한 모델입니다.
 * 산업별 분석 및 비교 (예: 반도체 vs 자동차)에 활용됩니다.
 *
 * @property industryName 산업 분류명
 * @property market 시장 구분
 * @property stocks 해당 산업 종목 목록
 * @property totalMarketCap 산업 전체 시가총액 (원)
 * @property stockCount 종목 수
 */
data class IndustryClassification(
    val industryName: String,
    val market: Market,
    val stocks: List<StockSectorInfo>,
    val totalMarketCap: Long,
    val stockCount: Int,
) {
    /**
     * 시가총액 순위별 상위 N개 종목 조회
     *
     * @param n 조회할 종목 수
     * @return 시가총액 상위 N개 종목
     */
    fun getTopNByMarketCap(n: Int): List<StockSectorInfo> = stocks.sortByMarketCap(descending = true).take(n)

    /**
     * 평균 시가총액 계산
     *
     * @return 평균 시가총액 (원)
     */
    fun getAverageMarketCap(): Long = if (stockCount > 0) totalMarketCap / stockCount else 0L
}
