package dev.kairoscode.kfc.domain.index

import java.math.BigDecimal

/**
 * 전체 지수 OHLCV 스냅샷
 *
 * 특정 일자의 전체 지수 OHLCV 조회 결과입니다.
 * 지수명 기준으로 조회하며 ticker는 포함하지 않습니다.
 *
 * @property name 지수명 (예: "코스피", "코스피 200")
 * @property open 시가
 * @property high 고가
 * @property low 저가
 * @property close 종가
 * @property volume 거래량
 * @property tradingValue 거래대금 (원)
 */
data class IndexOhlcvSnapshot(
    val name: String,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long,
    val tradingValue: Long? = null,
)
