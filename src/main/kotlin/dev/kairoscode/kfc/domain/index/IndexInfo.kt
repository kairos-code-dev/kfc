package dev.kairoscode.kfc.domain.index

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 지수 기본정보
 *
 * 지수의 메타데이터를 담는 도메인 모델입니다.
 *
 * @property ticker 지수 코드 (예: "1001", "1028")
 * @property name 지수명 (예: "코스피", "코스피 200")
 * @property market 시장 구분
 * @property baseDate 기준시점 (지수 산출 기준일)
 * @property announcementDate 발표시점 (지수 최초 발표일)
 * @property baseIndex 기준지수 (기준시점의 지수값, 보통 100.00)
 * @property constituentCount 구성 종목수
 */
data class IndexInfo(
    val ticker: String,
    val name: String,
    val market: IndexMarket,
    val baseDate: LocalDate? = null,
    val announcementDate: LocalDate? = null,
    val baseIndex: BigDecimal? = null,
    val constituentCount: Int? = null,
) {
    /**
     * 코스피 지수 여부 확인
     */
    fun isKospi(): Boolean = market == IndexMarket.KOSPI

    /**
     * 코스닥 지수 여부 확인
     */
    fun isKosdaq(): Boolean = market == IndexMarket.KOSDAQ
}
