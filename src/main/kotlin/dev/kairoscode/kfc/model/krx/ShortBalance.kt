package dev.kairoscode.kfc.model.krx

import java.time.LocalDate

/**
 * 공매도 잔고 정보
 *
 * KRX API MDCSTAT31501에서 반환되는 공매도 잔고 현황
 *
 * @property tradeDate 거래일
 * @property ticker 종목코드 (6자리)
 * @property name 종목 약명
 * @property shortBalance 공매도 잔고 수량
 * @property shortBalanceValue 공매도 잔고 금액
 * @property listedShares 상장 주식 수
 * @property shortBalanceRatio 잔고 비율 (%)
 */
data class ShortBalance(
    val tradeDate: LocalDate,
    val ticker: String,
    val name: String,
    val shortBalance: Long,
    val shortBalanceValue: Long,
    val listedShares: Long,
    val shortBalanceRatio: Double
)
