package dev.kairoscode.kfc.domain.index

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 지수 밸류에이션 (PER, PBR, 배당수익률)
 *
 * 특정 지수의 일별 밸류에이션 지표입니다.
 *
 * @property date 거래일
 * @property ticker 지수 코드
 * @property close 종가
 * @property changeRate 등락률 (%)
 * @property per 주가수익비율
 * @property forwardPer 선행 주가수익비율
 * @property pbr 주가순자산비율
 * @property dividendYield 배당수익률 (%)
 */
data class IndexFundamental(
    val date: LocalDate,
    val ticker: String,
    val close: BigDecimal,
    val changeRate: BigDecimal? = null,
    val per: BigDecimal? = null,
    val forwardPer: BigDecimal? = null,
    val pbr: BigDecimal? = null,
    val dividendYield: BigDecimal? = null,
)
