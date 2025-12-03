package dev.kairoscode.kfc.funds.internal.krx.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 펀드 OHLCV 데이터
 *
 * KRX API MDCSTAT04501에서 반환되는 개별 펀드 시세 추이
 *
 * @property tradeDate 거래일
 * @property openPrice 시가 (원본, 조정 안됨)
 * @property highPrice 고가 (원본, 조정 안됨)
 * @property lowPrice 저가 (원본, 조정 안됨)
 * @property closePrice 종가 (원본, 조정 안됨)
 * @property volume 거래량
 * @property tradingValue 거래대금
 * @property nav 순자산가치 (NAV)
 * @property priceChange 전일 대비 가격 변화
 * @property priceChangeRate 등락률 (%)
 * @property priceDirection 가격 방향
 * @property marketCap 시가총액
 * @property netAsset 순자산 총액
 * @property listedShares 상장 주식 수
 * @property indexName 지수명
 * @property indexValue 지수 값
 * @property indexChange 지수 전일 대비 변화
 * @property indexChangeRate 지수 등락률 (%)
 * @property indexDirection 지수 방향
 */
data class Ohlcv(
    val tradeDate: LocalDate,
    val openPrice: Int,
    val highPrice: Int,
    val lowPrice: Int,
    val closePrice: Int,
    val volume: Long,
    val tradingValue: Long,
    val nav: BigDecimal,
    val priceChange: Int,
    val priceChangeRate: Double,
    val priceDirection: Direction,
    val marketCap: Long,
    val netAsset: Long,
    val listedShares: Long,
    val indexName: String,
    val indexValue: BigDecimal,
    val indexChange: BigDecimal,
    val indexChangeRate: Double,
    val indexDirection: Direction
)
