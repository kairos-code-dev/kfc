package dev.kairoscode.kfc.domain.funds

import java.time.LocalDate

/**
 * 투자자별 일별 거래 정보
 *
 * KRX API MDCSTAT04802, MDCSTAT04902에서 반환되는 투자자별 일별 거래 실적
 *
 * @property tradeDate 거래일
 * @property investorType 투자자 유형
 * @property askVolume 매도 거래량
 * @property askValue 매도 거래대금
 * @property bidVolume 매수 거래량
 * @property bidValue 매수 거래대금
 * @property netBuyVolume 순매수 거래량 (매수 - 매도)
 * @property netBuyValue 순매수 거래대금 (매수 - 매도)
 */
data class InvestorTradingByDate(
    val tradeDate: LocalDate,
    val investorType: String,
    val askVolume: Long,
    val askValue: Long,
    val bidVolume: Long,
    val bidValue: Long,
    val netBuyVolume: Long,
    val netBuyValue: Long
)
