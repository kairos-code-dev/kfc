package dev.kairoscode.kfc.funds.internal.krx.model

/**
 * 펀드 기간 등락률
 *
 * KRX API MDCSTAT04401에서 반환되는 기간별 등락률
 *
 * @property ticker 종목코드 (6자리)
 * @property name 종목 약명
 * @property startPrice 시작 가격
 * @property endPrice 종료 가격
 * @property priceChange 가격 변화
 * @property changeRate 등락률 (%)
 * @property changeDirection 등락 방향
 * @property totalVolume 누적 거래량
 * @property totalTradingValue 누적 거래대금
 */
data class PriceChange(
    val ticker: String,
    val name: String,
    val startPrice: Int,
    val endPrice: Int,
    val priceChange: Int,
    val changeRate: Double,
    val changeDirection: Direction,
    val totalVolume: Long,
    val totalTradingValue: Long
)
