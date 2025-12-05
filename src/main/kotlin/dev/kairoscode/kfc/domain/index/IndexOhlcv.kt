package dev.kairoscode.kfc.domain.index

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 지수 OHLCV (일별 시가/고가/저가/종가/거래량)
 *
 * 특정 지수의 일별 시가, 고가, 저가, 종가, 거래량 정보입니다.
 *
 * @property date 거래일
 * @property ticker 지수 코드
 * @property open 시가
 * @property high 고가
 * @property low 저가
 * @property close 종가
 * @property volume 거래량
 * @property tradingValue 거래대금 (원)
 */
data class IndexOhlcv(
    val date: LocalDate,
    val ticker: String,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long,
    val tradingValue: Long? = null
) {
    /**
     * 일별 수익률 계산 (종가 / 시가 - 1)
     *
     * @return 수익률 (소수 형태, 예: 0.0242 = 2.42%)
     */
    fun calculateReturn(): BigDecimal {
        if (open == BigDecimal.ZERO) return BigDecimal.ZERO
        return (close - open).divide(open, 6, BigDecimal.ROUND_HALF_UP)
    }

    /**
     * 가격 상승 여부 확인
     *
     * @return true if 종가 > 시가
     */
    fun isPriceRising(): Boolean = close > open
}
