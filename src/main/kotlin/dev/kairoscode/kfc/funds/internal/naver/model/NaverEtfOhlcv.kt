package dev.kairoscode.kfc.funds.internal.naver.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 네이버 증권 ETF OHLCV 데이터
 *
 * 네이버 차트 API에서 제공하는 조정주가 OHLCV 데이터
 *
 * @property date 거래일
 * @property open 시가 (조정주가)
 * @property high 고가 (조정주가)
 * @property low 저가 (조정주가)
 * @property close 종가 (조정주가)
 * @property volume 거래량
 */
data class NaverOhlcv(
    val date: LocalDate,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: Long
)
