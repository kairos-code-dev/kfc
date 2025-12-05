package dev.kairoscode.kfc.domain.bond

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 특정일 전체 채권 수익률 스냅샷
 *
 * 특정 일자의 전체 채권 시장 수익률 현황을 담는 도메인 모델입니다.
 * 수익률 곡선 생성, 채권 간 스프레드 비교 등에 사용됩니다.
 *
 * @property date 거래일
 * @property yields 채권별 수익률 목록
 */
data class BondYieldSnapshot(
    val date: LocalDate,
    val yields: List<BondYieldItem>
) {
    /**
     * 국고채만 필터링
     */
    fun getTreasuryYields(): List<BondYieldItem> {
        return yields.filter { it.bondType.category == BondCategory.TREASURY }
    }

    /**
     * 회사채만 필터링
     */
    fun getCorporateYields(): List<BondYieldItem> {
        return yields.filter { it.bondType.category == BondCategory.CORPORATE }
    }

    /**
     * 특정 채권 종류의 수익률 조회
     *
     * @param bondType 조회할 채권 종류
     * @return 해당 채권의 수익률 정보, 없으면 null
     */
    fun getYieldByType(bondType: BondType): BondYieldItem? {
        return yields.find { it.bondType == bondType }
    }

    /**
     * 장단기 금리 스프레드 계산 (10년 - 2년)
     *
     * @return 10년물과 2년물의 수익률 차이 (bp), 데이터가 없으면 null
     */
    fun calculateTermSpread(): BigDecimal? {
        val treasury10Y = getYieldByType(BondType.TREASURY_10Y)?.yield
        val treasury2Y = getYieldByType(BondType.TREASURY_2Y)?.yield
        return if (treasury10Y != null && treasury2Y != null) {
            treasury10Y - treasury2Y
        } else null
    }

    /**
     * 신용 스프레드 계산 (회사채 AA- - 국고채 3년)
     *
     * @return 회사채 AA-와 국고채 3년물의 수익률 차이 (bp), 데이터가 없으면 null
     */
    fun calculateCreditSpread(): BigDecimal? {
        val corporateAA = getYieldByType(BondType.CORPORATE_AA)?.yield
        val treasury3Y = getYieldByType(BondType.TREASURY_3Y)?.yield
        return if (corporateAA != null && treasury3Y != null) {
            corporateAA - treasury3Y
        } else null
    }
}

/**
 * 채권 수익률 항목
 *
 * BondYieldSnapshot를 구성하는 개별 채권의 수익률 정보입니다.
 *
 * @property bondType 채권 종류
 * @property yield 수익률 (%)
 * @property change 전일 대비 변동폭 (bp)
 */
data class BondYieldItem(
    val bondType: BondType,
    val yield: BigDecimal,
    val change: BigDecimal
)
