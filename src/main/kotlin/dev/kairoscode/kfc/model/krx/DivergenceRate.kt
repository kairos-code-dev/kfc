package dev.kairoscode.kfc.model.krx

import java.math.BigDecimal
import java.time.LocalDate

/**
 * ETF 괴리율
 *
 * KRX API MDCSTAT06001에서 반환되는 괴리율 추이
 *
 * @property tradeDate 거래일
 * @property closePrice 종가
 * @property nav 순자산가치 (NAV)
 * @property divergenceRate 괴리율 (%)
 * @property priceDirection 가격 방향
 */
data class DivergenceRate(
    val tradeDate: LocalDate,
    val closePrice: Int,
    val nav: BigDecimal,
    val divergenceRate: Double,
    val priceDirection: Direction
)
