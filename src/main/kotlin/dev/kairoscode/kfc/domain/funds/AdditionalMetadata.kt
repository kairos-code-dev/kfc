package dev.kairoscode.kfc.domain.funds

import java.math.BigDecimal

/**
 * MDCSTAT04703 - 펀드 추가 메타데이터
 *
 * 펀드 개별종목 종합정보 페이지에서 제공되는 추가 메타데이터 모델입니다.
 *
 * **주의**: 이 모델은 KRX API의 MDCSTAT04703 응답 구조를 기반으로 합니다.
 * 실제 API 응답에 따라 필드가 추가되거나 수정될 수 있습니다.
 *
 * @property rawData 원시 응답 데이터 (유연한 접근을 위해 제공)
 * @property benchmarkIndexName 벤치마크 지수명
 * @property indexCalculationInstitution 지수 산출 기관
 * @property totalExpenseRatio 총 보수 비율 (%)
 * @property creationUnitQuantity CU(Creation Unit) 수량
 * @property settlementType 결제 유형
 * @property distributionFrequency 분배금 지급 주기
 * @property lastDistributionDate 최근 분배금 지급일
 * @property lastDistributionAmount 최근 분배금 금액
 */
data class AdditionalMetadata(
    // 원시 데이터 (유연한 접근용)
    val rawData: Map<String, Any?>,
    // 지수 정보
    val benchmarkIndexName: String?,
    val indexCalculationInstitution: String?,
    // 비용 및 운용 정보
    val totalExpenseRatio: BigDecimal?,
    val creationUnitQuantity: Long?,
    val settlementType: String?,
    // 분배금 정보
    val distributionFrequency: String?,
    val lastDistributionDate: String?,
    val lastDistributionAmount: BigDecimal?,
) {
    companion object {
        // 추정 필드명 (실제 API 응답 확인 후 조정 필요)
        private const val ETF_OBJ_IDX_NM = "ETF_OBJ_IDX_NM"
        private const val IDX_CALC_INST_NM = "IDX_CALC_INST_NM"
        private const val ETF_TOT_FEE = "ETF_TOT_FEE"
        private const val CU_QTY = "CU_QTY"
        private const val SETL_TP_NM = "SETL_TP_NM"
        private const val DISTR_FREQ = "DISTR_FREQ"
        private const val LST_DISTR_DD = "LST_DISTR_DD"
        private const val LST_DISTR_AMT = "LST_DISTR_AMT"

        /**
         * KRX API 원시 응답으로부터 AdditionalMetadata 생성
         *
         * @param raw KRX API 응답 Map
         * @return AdditionalMetadata 인스턴스
         */
        @Suppress("UNCHECKED_CAST")
        fun fromRaw(raw: Map<*, *>): AdditionalMetadata {
            val rawData =
                raw
                    .mapKeys { it.key.toString() }
                    .mapValues { it.value } as Map<String, Any?>

            return AdditionalMetadata(
                rawData = rawData,
                benchmarkIndexName = raw[ETF_OBJ_IDX_NM]?.toString()?.takeIf { it.isNotBlank() && it != "-" },
                indexCalculationInstitution =
                    raw[IDX_CALC_INST_NM]?.toString()?.takeIf {
                        it.isNotBlank() && it != "-"
                    },
                totalExpenseRatio = raw[ETF_TOT_FEE]?.toString()?.toKrxRateOrNull(),
                creationUnitQuantity = raw[CU_QTY]?.toString()?.toKrxLongOrNull(),
                settlementType = raw[SETL_TP_NM]?.toString()?.takeIf { it.isNotBlank() && it != "-" },
                distributionFrequency = raw[DISTR_FREQ]?.toString()?.takeIf { it.isNotBlank() && it != "-" },
                lastDistributionDate = raw[LST_DISTR_DD]?.toString()?.takeIf { it.isNotBlank() && it != "-" },
                lastDistributionAmount = raw[LST_DISTR_AMT]?.toString()?.toKrxRateOrNull(),
            )
        }

        /**
         * 문자열을 BigDecimal로 변환 (nullable)
         */
        private fun String.toKrxRateOrNull(): BigDecimal? {
            val clean = this.replace(",", "").trim()
            return if (clean.isEmpty() || clean == "-") {
                null
            } else {
                clean.toBigDecimalOrNull()
            }
        }

        /**
         * 문자열을 Long으로 변환 (nullable)
         */
        private fun String.toKrxLongOrNull(): Long? {
            val clean = this.replace(",", "").trim()
            return if (clean.isEmpty() || clean == "-") {
                null
            } else {
                clean.toLongOrNull()
            }
        }
    }

    /**
     * 특정 키의 값을 문자열로 가져옵니다.
     *
     * @param key 필드 키
     * @return 해당 값의 문자열 표현, 없으면 null
     */
    fun getString(key: String): String? = rawData[key]?.toString()

    /**
     * 특정 키의 값을 BigDecimal로 가져옵니다.
     *
     * @param key 필드 키
     * @return 해당 값의 BigDecimal, 없거나 변환 실패 시 null
     */
    fun getBigDecimal(key: String): BigDecimal? =
        rawData[key]
            ?.toString()
            ?.replace(",", "")
            ?.trim()
            ?.toBigDecimalOrNull()

    /**
     * 특정 키의 값을 Long으로 가져옵니다.
     *
     * @param key 필드 키
     * @return 해당 값의 Long, 없거나 변환 실패 시 null
     */
    fun getLong(key: String): Long? =
        rawData[key]
            ?.toString()
            ?.replace(",", "")
            ?.trim()
            ?.toLongOrNull()

    /**
     * 모든 원시 데이터 키 목록을 반환합니다.
     *
     * @return 키 Set
     */
    fun getAvailableKeys(): Set<String> = rawData.keys
}
