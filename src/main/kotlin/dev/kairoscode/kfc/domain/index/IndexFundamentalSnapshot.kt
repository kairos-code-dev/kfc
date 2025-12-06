package dev.kairoscode.kfc.domain.index

import java.math.BigDecimal

/**
 * 전체 지수 밸류에이션 스냅샷
 *
 * 특정 일자의 전체 지수 밸류에이션 조회 결과입니다.
 * 지수명 기준으로 조회하며 ticker는 포함하지 않습니다.
 *
 * @property name 지수명 (예: "코스피", "코스피 200")
 * @property close 종가
 * @property changeRate 등락률 (%)
 * @property per 주가수익비율
 * @property forwardPer 선행 주가수익비율
 * @property pbr 주가순자산비율
 * @property dividendYield 배당수익률 (%)
 */
data class IndexFundamentalSnapshot(
    val name: String,
    val close: BigDecimal,
    val changeRate: BigDecimal? = null,
    val per: BigDecimal? = null,
    val forwardPer: BigDecimal? = null,
    val pbr: BigDecimal? = null,
    val dividendYield: BigDecimal? = null,
)
