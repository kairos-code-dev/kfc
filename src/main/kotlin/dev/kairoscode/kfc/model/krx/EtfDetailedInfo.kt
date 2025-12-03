package dev.kairoscode.kfc.model.krx

import dev.kairoscode.kfc.internal.krx.KrxApiFields
import dev.kairoscode.kfc.util.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * MDCSTAT04701 - ETF 상세정보 (시간 의존 데이터)
 *
 * ETF의 주요 거래 정보를 단일 요청으로 제공하는 상세 데이터 모델입니다.
 * OHLCV, NAV, 시가총액, 52주 고가/저가, 총보수 등을 포함합니다.
 *
 * 이 모델은 KRX API의 MDCSTAT04701 엔드포인트 응답을 나타냅니다.
 * 거래일 기준으로 변하는 시간 의존 데이터만을 포함합니다.
 *
 * 참고: EtfGeneralInfo(MDCSTAT04704)는 상장 후 거의 변하지 않는 정적 메타데이터를 제공합니다.
 *
 * @property tradeDate 거래일자
 * @property isin 종목코드 (ISU_CD)
 * @property ticker 단축코드 (ISU_SRT_CD)
 * @property name 종목약명 (ISU_ABBRV)
 * @property securityGroup 증권구분 (SECUGRP_NM) - 예: ETF
 *
 * @property openPrice 시가 (TDD_OPNPRC)
 * @property highPrice 고가 (TDD_HGPRC)
 * @property lowPrice 저가 (TDD_LWPRC)
 * @property closePrice 종가 (TDD_CLSPRC)
 * @property priceChange 전일대비 (CMPPREVDD_PRC)
 * @property priceChangeDirection 등락구분 (FLUC_TP_CD1) - 1=상승, 2=하락, 3=보합
 * @property priceChangeRate 등락률 (FLUC_RT1) (%)
 *
 * @property volume 거래량 (ACC_TRDVOL)
 * @property tradingValue 거래대금 (ACC_TRDVAL)
 *
 * @property nav 최근 NAV (LST_NAV)
 * @property marketCap 시가총액 (MKTCAP)
 *
 * @property week52High 52주최고가 (WK52_HGST_PRC) - 중요: 날짜는 API에서 제공 안함
 * @property week52Low 52주최저가 (WK52_LWST_PRC) - 중요: 날짜는 API에서 제공 안함
 *
 * @property assetClass 자산분류 (IDX_ASST_CLSS_NM) - 예: 주식-시장대표
 * @property assetClassId 자산분류 ID (IDX_ASST_CLSS_ID)
 * @property totalFee 총보수 (ETF_TOT_FEE) (%) - 중요: 다른 엔드포인트에 없음
 * @property benchmarkIndex 기초지수명 (TRACE_IDX_NM)
 *
 * @property indexValue 기초지수값 (OBJ_STKPRC_IDX)
 * @property indexChange 지수전일대비 (CMPPREVDD_IDX)
 * @property indexChangeDirection 지수등락구분 (FLUC_TP_CD2) - 1=상승, 2=하락, 3=보합
 * @property indexChangeRate 지수등락률 (FLUC_RT2) (%)
 *
 * @property currentDateTime 현재시간 (CURRENT_DATETIME)
 */
data class EtfDetailedInfo(
    // 기본 식별 정보
    val tradeDate: LocalDate,
    val isin: String,
    val ticker: String,
    val name: String,
    val securityGroup: String,  // 예: ETF

    // 가격 데이터 (OHLCV)
    val openPrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
    val closePrice: BigDecimal,
    val priceChange: BigDecimal,
    val priceChangeDirection: Int,  // 1=상승, 2=하락, 3=보합
    val priceChangeRate: BigDecimal,

    // 거래량 및 거래대금
    val volume: Long,
    val tradingValue: BigDecimal,

    // NAV 및 시가총액
    val nav: BigDecimal,
    val marketCap: BigDecimal,

    // 52주 고가/저가 (핵심 필드 - 다른 엔드포인트에 없음)
    val week52High: BigDecimal,
    val week52Low: BigDecimal,

    // 자산분류 및 보수
    val assetClass: String,
    val assetClassId: String,
    val totalFee: BigDecimal,  // 핵심 필드 - 다른 엔드포인트에 없음
    val benchmarkIndex: String,

    // 지수 정보
    val indexValue: BigDecimal,
    val indexChange: BigDecimal,
    val indexChangeDirection: Int,
    val indexChangeRate: BigDecimal,

    // 조회 시간
    val currentDateTime: String
) {
    companion object {
        /**
         * KRX API 원시 응답으로부터 EtfDetailedInfo 생성
         *
         * @param raw KRX API 응답 Map (MDCSTAT04701 엔드포인트)
         * @param tradeDateOverride 거래일자 override (API 응답에 TRD_DD가 없을 경우 사용)
         * @return EtfDetailedInfo 인스턴스
         */
        fun fromRaw(raw: Map<*, *>, tradeDateOverride: LocalDate? = null): EtfDetailedInfo {
            return EtfDetailedInfo(
                // 기본 식별 정보
                tradeDate = tradeDateOverride ?: raw[KrxApiFields.DateTime.TRADE_DATE].toStringSafe().toKrxDate(),
                isin = raw[KrxApiFields.Identity.ISIN].toStringSafe(),
                ticker = raw[KrxApiFields.Identity.TICKER].toStringSafe(),
                name = raw[KrxApiFields.Identity.NAME_SHORT].toStringSafe(),
                securityGroup = raw[KrxApiFields.Identity.SECURITY_GROUP].toStringSafe(),

                // 가격 데이터
                openPrice = raw[KrxApiFields.Price.OPEN].toStringSafe().toKrxPrice(),
                highPrice = raw[KrxApiFields.Price.HIGH].toStringSafe().toKrxPrice(),
                lowPrice = raw[KrxApiFields.Price.LOW].toStringSafe().toKrxPrice(),
                closePrice = raw[KrxApiFields.Price.CLOSE].toStringSafe().toKrxPrice(),
                priceChange = raw[KrxApiFields.PriceChange.AMOUNT].toStringSafe().toKrxPrice(),
                priceChangeDirection = raw[KrxApiFields.PriceChange.DIRECTION].toStringSafe().toIntOrNull() ?: 3,
                priceChangeRate = raw[KrxApiFields.PriceChange.RATE_ALT1].toStringSafe().toKrxRate(),

                // 거래량 및 거래대금
                volume = raw[KrxApiFields.Volume.ACCUMULATED].toStringSafe().toKrxLong(),
                tradingValue = raw[KrxApiFields.Volume.TRADING_VALUE].toStringSafe().toKrxAmount(),

                // NAV 및 시가총액
                nav = raw[KrxApiFields.Nav.VALUE_LAST].toStringSafe().toKrxBigDecimal(),
                marketCap = raw[KrxApiFields.Asset.MARKET_CAP].toStringSafe().toKrxAmount(),

                // 52주 고가/저가 (날짜는 API에서 제공 안함)
                week52High = raw[KrxApiFields.Price.WEEK52_HIGH].toStringSafe().toKrxPrice(),
                week52Low = raw[KrxApiFields.Price.WEEK52_LOW].toStringSafe().toKrxPrice(),

                // 자산분류 및 보수
                assetClass = raw[KrxApiFields.EtfMetadata.ASSET_CLASS].toStringSafe(),
                assetClassId = raw[KrxApiFields.EtfMetadata.ASSET_CLASS_ID].toStringSafe(),
                totalFee = raw[KrxApiFields.EtfMetadata.TOTAL_EXPENSE_RATIO].toStringSafe().toKrxRate(),
                benchmarkIndex = raw[KrxApiFields.EtfMetadata.BENCHMARK_INDEX].toStringSafe(),

                // 지수 정보
                indexValue = raw[KrxApiFields.Index.VALUE].toStringSafe().toKrxBigDecimal(),
                indexChange = raw[KrxApiFields.Index.CHANGE_AMOUNT].toStringSafe().toKrxBigDecimal(),
                indexChangeDirection = raw[KrxApiFields.PriceChange.DIRECTION_ALT2].toStringSafe().toIntOrNull() ?: 3,
                indexChangeRate = raw[KrxApiFields.PriceChange.RATE_ALT2].toStringSafe().toKrxRate(),

                // 조회 시간
                currentDateTime = raw[KrxApiFields.DateTime.CURRENT_DATETIME].toStringSafe()
            )
        }
    }

    /**
     * 괴리율 계산 (NAV 대비 종가의 괴리율)
     * divergenceRate = (closePrice - nav) / nav * 100
     *
     * @return 괴리율 (%)
     */
    fun calculateDivergenceRate(): BigDecimal {
        if (nav == BigDecimal.ZERO) return BigDecimal.ZERO
        return closePrice.subtract(nav)
            .divide(nav, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
    }

    /**
     * 괴리율이 과도한지 확인 (절대값 1% 이상)
     *
     * @return 괴리율의 절대값이 1.0% 이상이면 true
     */
    fun hasExcessiveDivergence(): Boolean {
        return calculateDivergenceRate().abs() >= BigDecimal("1.0")
    }

    /**
     * 가격이 52주 고가 근처인지 확인 (95% 이상)
     *
     * @return 종가가 52주 고가의 95% 이상이면 true
     */
    fun isNear52WeekHigh(): Boolean {
        if (week52High == BigDecimal.ZERO) return false
        val threshold = week52High.multiply(BigDecimal("0.95"))
        return closePrice >= threshold
    }

    /**
     * 가격이 52주 저가 근처인지 확인 (105% 이하)
     *
     * @return 종가가 52주 저가의 105% 이하면 true
     */
    fun isNear52WeekLow(): Boolean {
        if (week52Low == BigDecimal.ZERO) return false
        val threshold = week52Low.multiply(BigDecimal("1.05"))
        return closePrice <= threshold
    }

    /**
     * 총 보수가 저렴한지 확인 (0.3% 이하)
     *
     * @return 총 보수가 0.3% 이하면 true
     */
    fun hasLowFee(): Boolean {
        return totalFee <= BigDecimal("0.3")
    }
}
