package dev.kairoscode.kfc.domain.funds

import java.math.BigDecimal

/**
 * ETF 포트폴리오 구성 종목
 *
 * KRX API MDCSTAT05001에서 반환되는 PDF (Portfolio Deposit File) 정보
 *
 * @property constituentCode 구성 종목 코드 (ISIN 또는 티커, 혼합 형식)
 * @property constituentName 구성 종목명
 * @property sharesPerCu CU당 주식 수
 * @property value 가치 (원)
 * @property constituentAmount 구성 금액
 * @property weightPercent 비중 (%)
 */
data class PortfolioConstituent(
    val constituentCode: String,
    val constituentName: String,
    val sharesPerCu: BigDecimal,
    val value: Long,
    val constituentAmount: Long,
    val weightPercent: BigDecimal
)
