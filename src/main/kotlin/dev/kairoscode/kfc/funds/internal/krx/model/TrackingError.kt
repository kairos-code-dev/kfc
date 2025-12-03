package dev.kairoscode.kfc.funds.internal.krx.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * ETF 추적 오차
 *
 * KRX API MDCSTAT05901에서 반환되는 추적 오차율 추이
 *
 * @property tradeDate 거래일
 * @property nav 순자산가치 (NAV)
 * @property navChangeRate NAV 변화율 (%)
 * @property indexValue 지수 값
 * @property indexChangeRate 지수 변화율 (%)
 * @property trackingMultiple 추적 배수
 * @property trackingErrorRate 추적 오차율 (%)
 */
data class TrackingError(
    val tradeDate: LocalDate,
    val nav: BigDecimal,
    val navChangeRate: Double,
    val indexValue: BigDecimal,
    val indexChangeRate: Double,
    val trackingMultiple: BigDecimal,
    val trackingErrorRate: Double
)
