package dev.kairoscode.kfc.model.krx

/**
 * 투자자별 거래 정보
 *
 * KRX API MDCSTAT04801, MDCSTAT04901에서 반환되는 투자자별 거래 실적
 *
 * @property investorType 투자자 유형 (금융투자, 보험, 투신, 사모, 은행, 기타금융, 연기금, 기관합계, 기타법인, 개인, 외국인, 기타외국인, 전체)
 * @property askVolume 매도 거래량
 * @property askValue 매도 거래대금
 * @property bidVolume 매수 거래량
 * @property bidValue 매수 거래대금
 * @property netBuyVolume 순매수 거래량 (매수 - 매도)
 * @property netBuyValue 순매수 거래대금 (매수 - 매도)
 */
data class InvestorTrading(
    val investorType: String,
    val askVolume: Long,
    val askValue: Long,
    val bidVolume: Long,
    val bidValue: Long,
    val netBuyVolume: Long,
    val netBuyValue: Long
)
