package dev.kairoscode.kfc.domain.price

import dev.kairoscode.kfc.infrastructure.common.util.toKrxInt
import dev.kairoscode.kfc.infrastructure.common.util.toKrxLong
import dev.kairoscode.kfc.infrastructure.common.util.toStringSafe

/**
 * MDCSTAT04702 - 펀드 분단위 시세
 *
 * 장중 1분 단위로 제공되는 OHLCV 데이터입니다.
 * 09:00부터 14:56까지 1분 간격으로 약 330개 이상의 데이터 포인트가 제공됩니다.
 *
 * 주의사항:
 * - **당일 거래일에만 데이터가 제공됩니다** (과거 날짜는 빈 응답)
 * - 비거래일(주말, 공휴일)에는 빈 응답이 반환됩니다
 * - 누적 거래량은 해당 시간까지의 누적값입니다
 * - 기준가는 당일 기준가입니다
 *
 * @property time 시간 (HH:MM 형식, 예: "09:00", "14:56")
 * @property closePrice 현재가 (분단위 종가)
 * @property openPrice 시가 (분단위 시가)
 * @property highPrice 고가 (분단위 고가)
 * @property lowPrice 저가 (분단위 저가)
 * @property cumulativeVolume 누적 거래량 (해당 시간까지의 누적)
 * @property basePrice 기준가 (당일 기준가)
 */
data class IntradayBar(
    val time: String,
    val closePrice: Int,
    val openPrice: Int,
    val highPrice: Int,
    val lowPrice: Int,
    val cumulativeVolume: Long,
    val basePrice: Int
) {
    companion object {
        // KRX API 필드명 상수
        private const val TRD_DD = "TRD_DD"
        private const val TDD_CLSPRC = "TDD_CLSPRC"
        private const val TDD_OPNPRC = "TDD_OPNPRC"
        private const val TDD_HGPRC = "TDD_HGPRC"
        private const val TDD_LWPRC = "TDD_LWPRC"
        private const val ACC_TRDVOL = "ACC_TRDVOL"
        private const val BAS_PRC = "BAS_PRC"

        /**
         * KRX API 원시 응답으로부터 IntradayBar 생성
         *
         * @param raw KRX API 응답 Map
         * @return IntradayBar 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): IntradayBar {
            return IntradayBar(
                time = raw[TRD_DD].toStringSafe(),
                closePrice = raw[TDD_CLSPRC].toStringSafe().toKrxInt(),
                openPrice = raw[TDD_OPNPRC].toStringSafe().toKrxInt(),
                highPrice = raw[TDD_HGPRC].toStringSafe().toKrxInt(),
                lowPrice = raw[TDD_LWPRC].toStringSafe().toKrxInt(),
                cumulativeVolume = raw[ACC_TRDVOL].toStringSafe().toKrxLong(),
                basePrice = raw[BAS_PRC].toStringSafe().toKrxInt()
            )
        }
    }

    /**
     * 시간을 LocalTime으로 변환
     *
     * @return LocalTime 인스턴스
     */
    fun toLocalTime(): java.time.LocalTime {
        val parts = time.split(":")
        return java.time.LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }

    /**
     * 분단위 변동폭 계산
     *
     * @return 변동폭 (highPrice - lowPrice)
     */
    fun getPriceRange(): Int = highPrice - lowPrice

    /**
     * 분단위 변동률 계산 (%)
     *
     * @return 변동률
     */
    fun getChangeRate(): Double {
        if (openPrice == 0) return 0.0
        return ((closePrice - openPrice).toDouble() / openPrice) * 100
    }
}
