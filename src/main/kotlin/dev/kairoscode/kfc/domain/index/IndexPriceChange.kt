package dev.kairoscode.kfc.domain.index

import java.math.BigDecimal

/**
 * 지수 등락률
 *
 * 특정 기간 동안의 지수 등락률 정보입니다.
 *
 * @property name 지수명 (예: "코스피", "코스피 200")
 * @property openPrice 시작일 시가
 * @property closePrice 종료일 종가
 * @property changeRate 등락률 (%)
 * @property volume 누적 거래량
 * @property tradingValue 누적 거래대금 (원)
 */
data class IndexPriceChange(
    val name: String,
    val openPrice: BigDecimal,
    val closePrice: BigDecimal,
    val changeRate: BigDecimal,
    val volume: Long,
    val tradingValue: Long? = null
)
