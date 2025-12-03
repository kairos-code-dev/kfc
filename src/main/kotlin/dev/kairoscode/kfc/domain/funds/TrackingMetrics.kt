package dev.kairoscode.kfc.domain.funds

import dev.kairoscode.kfc.infrastructure.common.util.*
import java.math.BigDecimal

/**
 * MDCSTAT04702 - 펀드 추적오차/괴리율 관련 메트릭스
 *
 * 펀드 개별종목 종합정보 페이지에서 제공되는 추적오차 및 괴리율 관련 데이터 모델입니다.
 *
 * **주의**: 이 모델은 KRX API의 MDCSTAT04702 응답 구조를 기반으로 합니다.
 * 실제 API 응답에 따라 필드가 추가되거나 수정될 수 있습니다.
 *
 * @property rawData 원시 응답 데이터 (유연한 접근을 위해 제공)
 * @property trackingErrorRate1M 1개월 추적오차율 (%)
 * @property trackingErrorRate3M 3개월 추적오차율 (%)
 * @property trackingErrorRate6M 6개월 추적오차율 (%)
 * @property trackingErrorRate1Y 1년 추적오차율 (%)
 * @property divergenceRate1M 1개월 괴리율 (%)
 * @property divergenceRate3M 3개월 괴리율 (%)
 * @property divergenceRate6M 6개월 괴리율 (%)
 * @property divergenceRate1Y 1년 괴리율 (%)
 */
data class TrackingMetrics(
    // 원시 데이터 (유연한 접근용)
    val rawData: Map<String, Any?>,

    // 추적오차율 (기간별)
    val trackingErrorRate1M: BigDecimal?,
    val trackingErrorRate3M: BigDecimal?,
    val trackingErrorRate6M: BigDecimal?,
    val trackingErrorRate1Y: BigDecimal?,

    // 괴리율 (기간별)
    val divergenceRate1M: BigDecimal?,
    val divergenceRate3M: BigDecimal?,
    val divergenceRate6M: BigDecimal?,
    val divergenceRate1Y: BigDecimal?
) {
    companion object {
        // 추정 필드명 (실제 API 응답 확인 후 조정 필요)
        private const val TRACE_ERR_RT_1M = "TRACE_ERR_RT_1M"
        private const val TRACE_ERR_RT_3M = "TRACE_ERR_RT_3M"
        private const val TRACE_ERR_RT_6M = "TRACE_ERR_RT_6M"
        private const val TRACE_ERR_RT_1Y = "TRACE_ERR_RT_1Y"
        private const val DIVRG_RT_1M = "DIVRG_RT_1M"
        private const val DIVRG_RT_3M = "DIVRG_RT_3M"
        private const val DIVRG_RT_6M = "DIVRG_RT_6M"
        private const val DIVRG_RT_1Y = "DIVRG_RT_1Y"

        /**
         * KRX API 원시 응답으로부터 TrackingMetrics 생성
         *
         * @param raw KRX API 응답 Map
         * @return TrackingMetrics 인스턴스
         */
        @Suppress("UNCHECKED_CAST")
        fun fromRaw(raw: Map<*, *>): TrackingMetrics {
            val rawData = raw.mapKeys { it.key.toString() }
                .mapValues { it.value } as Map<String, Any?>

            return TrackingMetrics(
                rawData = rawData,
                trackingErrorRate1M = raw[TRACE_ERR_RT_1M]?.toString()?.toKrxRateOrNull(),
                trackingErrorRate3M = raw[TRACE_ERR_RT_3M]?.toString()?.toKrxRateOrNull(),
                trackingErrorRate6M = raw[TRACE_ERR_RT_6M]?.toString()?.toKrxRateOrNull(),
                trackingErrorRate1Y = raw[TRACE_ERR_RT_1Y]?.toString()?.toKrxRateOrNull(),
                divergenceRate1M = raw[DIVRG_RT_1M]?.toString()?.toKrxRateOrNull(),
                divergenceRate3M = raw[DIVRG_RT_3M]?.toString()?.toKrxRateOrNull(),
                divergenceRate6M = raw[DIVRG_RT_6M]?.toString()?.toKrxRateOrNull(),
                divergenceRate1Y = raw[DIVRG_RT_1Y]?.toString()?.toKrxRateOrNull()
            )
        }

        /**
         * 문자열을 BigDecimal로 변환 (nullable)
         */
        private fun String.toKrxRateOrNull(): BigDecimal? {
            val clean = this.replace(",", "").trim()
            return if (clean.isEmpty() || clean == "-") null
            else clean.toBigDecimalOrNull()
        }
    }

    /**
     * 특정 키의 값을 문자열로 가져옵니다.
     *
     * @param key 필드 키
     * @return 해당 값의 문자열 표현, 없으면 null
     */
    fun getString(key: String): String? {
        return rawData[key]?.toString()
    }

    /**
     * 특정 키의 값을 BigDecimal로 가져옵니다.
     *
     * @param key 필드 키
     * @return 해당 값의 BigDecimal, 없거나 변환 실패 시 null
     */
    fun getBigDecimal(key: String): BigDecimal? {
        return rawData[key]?.toString()?.replace(",", "")?.trim()?.toBigDecimalOrNull()
    }

    /**
     * 특정 키의 값을 Long으로 가져옵니다.
     *
     * @param key 필드 키
     * @return 해당 값의 Long, 없거나 변환 실패 시 null
     */
    fun getLong(key: String): Long? {
        return rawData[key]?.toString()?.replace(",", "")?.trim()?.toLongOrNull()
    }
}
