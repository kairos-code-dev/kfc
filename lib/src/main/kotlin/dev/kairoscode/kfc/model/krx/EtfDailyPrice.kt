package dev.kairoscode.kfc.model.krx

import java.math.BigDecimal

/**
 * ETF 일별 시세
 *
 * KRX API MDCSTAT04301에서 반환되는 특정 날짜 전체 ETF 시세
 *
 * @property ticker 종목코드 (6자리)
 * @property name 종목 약명
 * @property closePrice 종가
 * @property priceChange 전일 대비 가격 변화
 * @property priceChangeRate 등락률 (%)
 * @property priceDirection 가격 방향 (UP, DOWN, UNCHANGED)
 * @property nav 순자산가치 (NAV)
 * @property openPrice 시가
 * @property highPrice 고가
 * @property lowPrice 저가
 * @property volume 거래량
 * @property tradingValue 거래대금
 * @property marketCap 시가총액
 * @property listedShares 상장 주식 수
 * @property indexName 지수명
 * @property indexValue 지수 값
 * @property indexChange 지수 전일 대비 변화
 * @property indexChangeRate 지수 등락률 (%)
 * @property indexDirection 지수 방향
 */
data class EtfDailyPrice(
    val ticker: String,
    val name: String,
    val closePrice: Int,
    val priceChange: Int,
    val priceChangeRate: Double,
    val priceDirection: Direction,
    val nav: BigDecimal,
    val openPrice: Int,
    val highPrice: Int,
    val lowPrice: Int,
    val volume: Long,
    val tradingValue: Long,
    val marketCap: Long,
    val listedShares: Long,
    val indexName: String,
    val indexValue: BigDecimal,
    val indexChange: BigDecimal,
    val indexChangeRate: Double,
    val indexDirection: Direction
)
