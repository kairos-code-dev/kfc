package dev.kairoscode.kfc.domain.stock

/**
 * 산업 분류 상세
 *
 * 특정 산업에 속한 모든 종목을 그룹화한 모델입니다.
 * 산업별 분석 및 비교에 활용됩니다.
 *
 * @property industryName 산업 분류명 (예: "전기전자")
 * @property market 시장 구분 (KOSPI, KOSDAQ 등)
 * @property stocks 해당 산업 종목 목록
 * @property totalMarketCap 산업 전체 시가총액 (원)
 * @property stockCount 종목 수
 */
data class IndustryClassification(
    val industryName: String,
    val market: Market,
    val stocks: List<StockSectorInfo>,
    val totalMarketCap: Long,
    val stockCount: Int
)

/**
 * List<StockSectorInfo> 확장 함수들
 */

/**
 * 산업별로 그룹화
 *
 * @return 산업명을 키로 하는 Map
 */
fun List<StockSectorInfo>.groupByIndustry(): Map<String, List<StockSectorInfo>> {
    return groupBy { it.industry }
}

/**
 * 시가총액 기준으로 필터링
 *
 * @param minCap 최소 시가총액 (원)
 * @return 필터링된 종목 리스트
 */
fun List<StockSectorInfo>.filterByMarketCap(minCap: Long): List<StockSectorInfo> {
    return filter { (it.marketCap ?: 0L) >= minCap }
}

/**
 * 시가총액 기준으로 정렬
 *
 * @param descending true면 내림차순, false면 오름차순 (기본: 내림차순)
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
 * 전체 시가총액 계산
 *
 * @return 합산 시가총액 (원)
 */
fun List<StockSectorInfo>.calculateTotalMarketCap(): Long {
    return sumOf { it.marketCap ?: 0L }
}
