package dev.kairoscode.kfc.domain.bond

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 채권 수익률
 *
 * 특정 채권의 특정 일자 수익률 정보를 담는 도메인 모델입니다.
 * 시계열 데이터 분석에 적합하며, 채권 수익률 추이 추적에 사용됩니다.
 *
 * @property date 거래일
 * @property bondType 채권 종류
 * @property yield 수익률 (%)
 * @property change 전일 대비 변동폭 (bp)
 */
data class BondYield(
    val date: LocalDate,
    val bondType: BondType,
    val yield: BigDecimal,
    val change: BigDecimal
) {
    /**
     * 국고채 여부 확인
     */
    fun isTreasury(): Boolean {
        return bondType.category == BondCategory.TREASURY
    }

    /**
     * 회사채 여부 확인
     */
    fun isCorporate(): Boolean {
        return bondType.category == BondCategory.CORPORATE
    }

    /**
     * 수익률 상승 여부 확인
     */
    fun isYieldRising(): Boolean {
        return change.compareTo(BigDecimal.ZERO) > 0
    }

    /**
     * 수익률 하락 여부 확인
     */
    fun isYieldFalling(): Boolean {
        return change.compareTo(BigDecimal.ZERO) < 0
    }

    /**
     * 수익률 변동 없음 확인
     */
    fun isYieldUnchanged(): Boolean {
        return change.compareTo(BigDecimal.ZERO) == 0
    }
}
