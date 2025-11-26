package dev.kairoscode.kfc.model.krx

import java.time.LocalDate

/**
 * 공매도 거래 정보
 *
 * KRX API MDCSTAT31401에서 반환되는 공매도 거래 현황
 *
 * @property tradeDate 거래일
 * @property ticker 종목코드 (6자리)
 * @property name 종목 약명
 * @property shortVolume 공매도 거래량
 * @property shortValue 공매도 거래대금
 * @property totalVolume 전체 거래량
 * @property totalValue 전체 거래대금
 * @property shortVolumeRatio 공매도 거래량 비중 (%)
 * @property shortValueRatio 공매도 거래대금 비중 (%)
 */
data class ShortSelling(
    val tradeDate: LocalDate,
    val ticker: String,
    val name: String,
    val shortVolume: Long,
    val shortValue: Long,
    val totalVolume: Long,
    val totalValue: Long,
    val shortVolumeRatio: Double,
    val shortValueRatio: Double
)
