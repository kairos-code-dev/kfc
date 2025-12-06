package dev.kairoscode.kfc.domain.funds

import dev.kairoscode.kfc.infrastructure.common.util.toKrxAmount
import dev.kairoscode.kfc.infrastructure.common.util.toKrxBigDecimal
import dev.kairoscode.kfc.infrastructure.common.util.toKrxDate
import dev.kairoscode.kfc.infrastructure.common.util.toKrxLong
import dev.kairoscode.kfc.infrastructure.common.util.toStringSafe
import java.math.BigDecimal
import java.time.LocalDate

/**
 * MDCSTAT04704 - 펀드 기본정보 (정적 메타데이터)
 *
 * 펀드 개별종목 종합정보 페이지에서 제공되는 정적 기본 정보 데이터 모델입니다.
 * 종목 식별 정보, 운용사, 지수 정보, 복제 방법, LP 정보 등을 포함합니다.
 *
 * 상장 후 거의 변경되지 않는 구조적 정보이며,
 * MDCSTAT04701(DetailedInfo)의 시간 의존 데이터와는 차별화됩니다.
 *
 * @property name 종목명 (ISU_NM)
 * @property isin ISIN 코드 (ISU_CD)
 * @property ticker 단축코드 (ISU_SRT_CD)
 * @property netAssetTotal 순자산총액 (NETASST_TOTAMT)
 * @property prevDayNav 전일 NAV (PREVDD_NAV)
 * @property etfTypeCode ETF 유형 코드 (ETF_TP_CD)
 * @property leverageInverseTypeCode 레버리지/인버스 유형 코드 (IDX_LVRG_INVRS_TP_CD)
 * @property underlyingDomesticForeignTypeCode 추적 기초자산 국내/해외 유형 코드 (TRACE_ULY_DOMFORN_TP_CD)
 * @property issuerName 발행사/운용사명 (ISUR_NM)
 * @property indexProviderName 지수 산출 기관명 (IDX_CALC_INST_NM)
 * @property listedShares 상장주식수 (LIST_SHRS)
 * @property listingDate 상장일 (LIST_DD)
 * @property taxTypeCode 과세 유형 코드 (TAX_TP_CD)
 * @property replicationMethodTypeCode 복제 방법 유형 코드 (ETF_REPLICA_METHD_TP_CD)
 * @property assetClassName 지수 자산 분류명 (IDX_ASST_CLSS_NM)
 * @property distributionPaymentBasisContent 신탁 분배금 지급 기준 내용 (TRST_DISTR_PAY_BAS_DD_CONTN)
 * @property lpName LP(유동성 공급자) 명 (LP_NM)
 */
data class GeneralInfo(
    // 기본 식별 정보
    val name: String,
    val isin: String,
    val ticker: String,
    // 자산 정보
    val netAssetTotal: BigDecimal,
    val prevDayNav: BigDecimal,
    // 유형 코드
    val etfTypeCode: String,
    val leverageInverseTypeCode: String,
    val underlyingDomesticForeignTypeCode: String,
    // 운용사 및 지수 정보
    val issuerName: String,
    val indexProviderName: String,
    // 상장 정보
    val listedShares: Long,
    val listingDate: LocalDate,
    val taxTypeCode: String,
    // 복제 방법 및 자산 분류
    val replicationMethodTypeCode: String,
    val assetClassName: String,
    // 분배금 및 LP 정보
    val distributionPaymentBasisContent: String,
    val lpName: String,
) {
    companion object {
        // KRX API 필드명 상수 (MDCSTAT04704 전용)
        private const val ISU_NM = "ISU_NM"
        private const val ISU_CD = "ISU_CD"
        private const val ISU_SRT_CD = "ISU_SRT_CD"
        private const val NETASST_TOTAMT = "NETASST_TOTAMT"
        private const val PREVDD_NAV = "PREVDD_NAV"
        private const val ETF_TP_CD = "ETF_TP_CD"
        private const val IDX_LVRG_INVRS_TP_CD = "IDX_LVRG_INVRS_TP_CD"
        private const val TRACE_ULY_DOMFORN_TP_CD = "TRACE_ULY_DOMFORN_TP_CD"
        private const val ISUR_NM = "ISUR_NM"
        private const val IDX_CALC_INST_NM = "IDX_CALC_INST_NM"
        private const val LIST_SHRS = "LIST_SHRS"
        private const val LIST_DD = "LIST_DD"
        private const val TAX_TP_CD = "TAX_TP_CD"
        private const val ETF_REPLICA_METHD_TP_CD = "ETF_REPLICA_METHD_TP_CD"
        private const val IDX_ASST_CLSS_NM = "IDX_ASST_CLSS_NM"
        private const val TRST_DISTR_PAY_BAS_DD_CONTN = "TRST_DISTR_PAY_BAS_DD_CONTN"
        private const val LP_NM = "LP_NM"

        /**
         * KRX API 원시 응답으로부터 GeneralInfo 생성
         *
         * @param raw KRX API 응답 Map
         * @return GeneralInfo 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): GeneralInfo =
            GeneralInfo(
                name = raw[ISU_NM].toStringSafe(),
                isin = raw[ISU_CD].toStringSafe(),
                ticker = raw[ISU_SRT_CD].toStringSafe(),
                netAssetTotal = raw[NETASST_TOTAMT].toStringSafe().toKrxAmount(),
                prevDayNav = raw[PREVDD_NAV].toStringSafe().toKrxBigDecimal(),
                etfTypeCode = raw[ETF_TP_CD].toStringSafe(),
                leverageInverseTypeCode = raw[IDX_LVRG_INVRS_TP_CD].toStringSafe(),
                underlyingDomesticForeignTypeCode = raw[TRACE_ULY_DOMFORN_TP_CD].toStringSafe(),
                issuerName = raw[ISUR_NM].toStringSafe(),
                indexProviderName = raw[IDX_CALC_INST_NM].toStringSafe(),
                listedShares = raw[LIST_SHRS].toStringSafe().toKrxLong(),
                listingDate = raw[LIST_DD].toStringSafe().toKrxDate(),
                taxTypeCode = raw[TAX_TP_CD].toStringSafe(),
                replicationMethodTypeCode = raw[ETF_REPLICA_METHD_TP_CD].toStringSafe(),
                assetClassName = raw[IDX_ASST_CLSS_NM].toStringSafe(),
                distributionPaymentBasisContent = raw[TRST_DISTR_PAY_BAS_DD_CONTN].toStringSafe(),
                lpName = raw[LP_NM].toStringSafe(),
            )
    }

    /**
     * 레버리지 ETF인지 확인
     *
     * @return 레버리지 ETF이면 true
     */
    fun isLeveraged(): Boolean =
        leverageInverseTypeCode.contains("레버리지") ||
            leverageInverseTypeCode.lowercase().contains("leverage")

    /**
     * 인버스 ETF인지 확인
     *
     * @return 인버스 ETF이면 true
     */
    fun isInverse(): Boolean =
        leverageInverseTypeCode.contains("인버스") ||
            leverageInverseTypeCode.lowercase().contains("inverse")

    /**
     * 해외 기초자산 추적 ETF인지 확인
     *
     * @return 해외 기초자산 ETF이면 true
     */
    fun isForeignUnderlying(): Boolean =
        underlyingDomesticForeignTypeCode.contains("해외") ||
            underlyingDomesticForeignTypeCode.lowercase().contains("foreign")

    /**
     * 합성(Synthetic) ETF인지 확인
     *
     * @return 합성 복제 방식이면 true
     */
    fun isSynthetic(): Boolean =
        replicationMethodTypeCode.contains("합성") ||
            replicationMethodTypeCode.lowercase().contains("synthetic")
}
