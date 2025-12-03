package dev.kairoscode.kfc.domain.funds

import dev.kairoscode.kfc.infrastructure.common.util.*
import java.math.BigDecimal

/**
 * ETF 포트폴리오 상위 10 종목
 *
 * ETF 포트폴리오 구성 종목 중 비중이 높은 상위 10개의 요약 정보입니다.
 *
 * 참고: MDCSTAT04705 엔드포인트는 작동하지 않으므로,
 * 전체 포트폴리오(MDCSTAT05001) API에서 상위 10개를 추출합니다.
 *
 * @property ticker 구성종목코드 (6자리 티커)
 * @property name 구성종목명
 * @property cuQuantity CU당 수량 (소수점 가능)
 * @property value 현재 가치 (원)
 * @property compositionAmount 구성금액 (원)
 * @property compositionRatio 구성 비중 (백분율)
 */
data class PortfolioTopItem(
    val ticker: String,
    val name: String,
    val cuQuantity: BigDecimal,
    val value: Long,
    val compositionAmount: Long,
    val compositionRatio: BigDecimal
) {
    companion object {
        // KRX API 필드명 상수
        private const val ISU_CD = "ISU_CD"
        private const val ISU_ABBRV = "ISU_ABBRV"
        private const val COMPST_ISU_CU1_SHRS = "COMPST_ISU_CU1_SHRS"
        private const val VALU_AMT = "VALU_AMT"
        private const val COMPST_AMT = "COMPST_AMT"
        private const val COMPST_RTO = "COMPST_RTO"

        /**
         * KRX API 원시 응답으로부터 PortfolioTopItem 생성
         *
         * @param raw KRX API 응답 Map
         * @return PortfolioTopItem 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): PortfolioTopItem {
            return PortfolioTopItem(
                ticker = raw[ISU_CD].toStringSafe(),
                name = raw[ISU_ABBRV].toStringSafe(),
                cuQuantity = raw[COMPST_ISU_CU1_SHRS].toStringSafe().toKrxBigDecimal(),
                value = raw[VALU_AMT].toStringSafe().toKrxLong(),
                compositionAmount = raw[COMPST_AMT].toStringSafe().toKrxLong(),
                compositionRatio = raw[COMPST_RTO].toStringSafe().toKrxBigDecimal()
            )
        }
    }

    /**
     * 비중이 5% 이상인지 확인
     */
    fun isSignificantWeight(): Boolean = compositionRatio >= BigDecimal("5.0")

    /**
     * 비중을 소수로 반환 (예: 8.77% -> 0.0877)
     */
    fun getWeightAsDecimal(): BigDecimal = compositionRatio.divide(BigDecimal("100"))
}
