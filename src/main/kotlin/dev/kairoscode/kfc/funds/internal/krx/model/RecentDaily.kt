package dev.kairoscode.kfc.funds.internal.krx.model

import dev.kairoscode.kfc.util.*
import java.time.LocalDate

/**
 * MDCSTAT04703 - 펀드 최근 일별 거래
 *
 * 최근 10거래일의 일별 시세 요약 데이터입니다.
 *
 * @property tradeDate 거래일
 * @property closePrice 종가
 * @property direction 등락구분 (UP, DOWN, UNCHANGED)
 * @property change 전일대비 (부호 있음)
 * @property changeRate 등락률 (%, 부호 있음)
 * @property volume 거래량
 * @property tradingValue 거래대금 (원)
 */
data class RecentDaily(
    val tradeDate: LocalDate,
    val closePrice: Int,
    val direction: Direction,
    val change: Int,
    val changeRate: Double,
    val volume: Long,
    val tradingValue: Long
) {
    companion object {
        // KRX API 필드명 상수
        private const val TRD_DD = "TRD_DD"
        private const val TDD_CLSPRC = "TDD_CLSPRC"
        private const val FLUC_TP_CD = "FLUC_TP_CD"
        private const val CMPPREVDD_PRC = "CMPPREVDD_PRC"
        private const val FLUC_RT = "FLUC_RT"
        private const val ACC_TRDVOL = "ACC_TRDVOL"
        private const val ACC_TRDVAL = "ACC_TRDVAL"

        /**
         * KRX API 원시 응답으로부터 RecentDaily 생성
         *
         * @param raw KRX API 응답 Map
         * @return RecentDaily 인스턴스
         */
        fun fromRaw(raw: Map<*, *>): RecentDaily {
            return RecentDaily(
                tradeDate = raw[TRD_DD].toStringSafe().toKrxDate(),
                closePrice = raw[TDD_CLSPRC].toStringSafe().toKrxInt(),
                direction = raw[FLUC_TP_CD].toStringSafe().toKrxDirection(),
                change = raw[CMPPREVDD_PRC].toStringSafe().toKrxInt(),
                changeRate = raw[FLUC_RT].toStringSafe().toKrxDouble(),
                volume = raw[ACC_TRDVOL].toStringSafe().toKrxLong(),
                tradingValue = raw[ACC_TRDVAL].toStringSafe().toKrxLong()
            )
        }
    }

    /**
     * 상승일인지 확인
     */
    fun isPositive(): Boolean = direction == Direction.UP

    /**
     * 하락일인지 확인
     */
    fun isNegative(): Boolean = direction == Direction.DOWN

    /**
     * 거래대금을 억원 단위로 반환
     */
    fun getTradingValueInBillions(): Double = tradingValue / 100_000_000.0
}
