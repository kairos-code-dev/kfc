package dev.kairoscode.kfc.model.krx

import dev.kairoscode.kfc.internal.krx.KrxApiFields
import dev.kairoscode.kfc.util.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * MDCSTAT04701 - ETF 개별종목 종합정보
 *
 * ETF의 모든 주요 정보를 단일 요청으로 제공하는 종합 데이터 모델입니다.
 * OHLCV, NAV, 시가총액, 52주 고가/저가, 기본 정보 등을 포함합니다.
 *
 * 이 모델은 KRX API의 MDCSTAT04701 엔드포인트 응답을 나타냅니다.
 * 다른 엔드포인트에서는 얻을 수 없는 중요한 데이터(52주 고가/저가, 총보수)를 포함합니다.
 *
 * @property tradeDate 거래일자
 * @property isin 종목코드 (ISIN)
 * @property ticker 단축코드 (티커)
 * @property name 종목약명
 * @property fullName 종목명 (전체)
 * @property openPrice 시가
 * @property highPrice 고가
 * @property lowPrice 저가
 * @property closePrice 종가
 * @property priceChange 전일대비
 * @property priceChangeDirection 등락구분 (1=상승, 2=하락, 3=보합)
 * @property priceChangeRate 등락률 (%)
 * @property volume 거래량
 * @property tradingValue 거래대금
 * @property nav NAV (순자산가치)
 * @property navChange NAV변화금액
 * @property navChangeRate NAV변화율 (%)
 * @property divergenceRate 괴리율 (%)
 * @property marketCap 시가총액
 * @property netAssetValue 순자산총액
 * @property listedShares 상장주식수
 * @property week52High 52주최고가 (중요 - 다른 엔드포인트에 없음)
 * @property week52HighDate 52주최고가일자
 * @property week52Low 52주최저가 (중요 - 다른 엔드포인트에 없음)
 * @property week52LowDate 52주최저가일자
 * @property indexValue 기초지수
 * @property indexName 지수명
 * @property indexChange 지수전일대비
 * @property indexChangeDirection 지수등락구분
 * @property indexChangeRate 지수등락률 (%)
 * @property listingDate 상장일
 * @property assetManager 운용사
 * @property totalFee 총보수 (%) (중요 - 다른 엔드포인트에 없음)
 * @property creationUnit CU수량
 * @property benchmarkIndex 기초지수명 (전체)
 * @property indexProvider 지수산출기관
 * @property marketClassification 시장구분 (국내/해외)
 * @property assetClass 자산구분 (주식/채권/상품/파생)
 * @property replicationMethod 복제방법 (실물/합성)
 * @property leverageType 레버리지/인버스구분 (일반/레버리지/인버스)
 * @property taxType 과세유형
 * @property marketName 시장명
 */
data class ComprehensiveEtfInfo(
    // 기본 식별 정보
    val tradeDate: LocalDate,
    val isin: String,
    val ticker: String,
    val name: String,
    val fullName: String,

    // 가격 데이터 (OHLCV) - 모두 BigDecimal
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

    // NAV 정보
    val nav: BigDecimal,
    val navChange: BigDecimal,
    val navChangeRate: BigDecimal,
    val divergenceRate: BigDecimal,  // 괴리율

    // 시가총액 및 주식 수
    val marketCap: BigDecimal,
    val netAssetValue: BigDecimal,
    val listedShares: Long,

    // 52주 고가/저가 (핵심 필드 - 다른 엔드포인트에 없음)
    val week52High: BigDecimal,
    val week52HighDate: LocalDate,
    val week52Low: BigDecimal,
    val week52LowDate: LocalDate,

    // 지수 정보
    val indexValue: BigDecimal,
    val indexName: String,
    val indexChange: BigDecimal,
    val indexChangeDirection: Int,
    val indexChangeRate: BigDecimal,

    // ETF 기본 정보
    val listingDate: LocalDate,
    val assetManager: String,
    val totalFee: BigDecimal,  // 핵심 필드 - 다른 엔드포인트에 없음
    val creationUnit: Long,
    val benchmarkIndex: String,
    val indexProvider: String,
    val marketClassification: String,  // 국내/해외
    val assetClass: String,  // 주식/채권/상품/파생
    val replicationMethod: String,  // 실물/합성
    val leverageType: String,  // 일반/레버리지/인버스
    val taxType: String,
    val marketName: String
) {
    companion object {
        /**
         * KRX API 원시 응답으로부터 ComprehensiveEtfInfo 생성
         *
         * @param raw KRX API 응답 Map
         * @param tradeDateOverride 거래일자 override (API 응답에 TRD_DD가 없을 경우 사용)
         * @return ComprehensiveEtfInfo 인스턴스
         */
        fun fromRaw(raw: Map<*, *>, tradeDateOverride: LocalDate? = null): ComprehensiveEtfInfo {
            return ComprehensiveEtfInfo(
                tradeDate = tradeDateOverride ?: raw[KrxApiFields.DateTime.TRADE_DATE].toStringSafe().toKrxDate(),
                isin = raw[KrxApiFields.Identity.ISIN].toStringSafe(),
                ticker = raw[KrxApiFields.Identity.TICKER].toStringSafe(),
                name = raw[KrxApiFields.Identity.NAME_SHORT].toStringSafe(),
                fullName = raw[KrxApiFields.Identity.NAME_FULL].toStringSafe(),

                openPrice = raw[KrxApiFields.Price.OPEN].toStringSafe().toKrxPrice(),
                highPrice = raw[KrxApiFields.Price.HIGH].toStringSafe().toKrxPrice(),
                lowPrice = raw[KrxApiFields.Price.LOW].toStringSafe().toKrxPrice(),
                closePrice = raw[KrxApiFields.Price.CLOSE].toStringSafe().toKrxPrice(),
                priceChange = raw[KrxApiFields.PriceChange.AMOUNT].toStringSafe().toKrxPrice(),
                priceChangeDirection = raw[KrxApiFields.PriceChange.DIRECTION].toStringSafe().toIntOrNull() ?: 3,
                priceChangeRate = raw[KrxApiFields.PriceChange.RATE].toStringSafe().toKrxRate(),

                volume = raw[KrxApiFields.Volume.ACCUMULATED].toStringSafe().toKrxLong(),
                tradingValue = raw[KrxApiFields.Volume.TRADING_VALUE].toStringSafe().toKrxAmount(),

                nav = raw[KrxApiFields.Nav.VALUE_LAST].toStringSafe().toKrxBigDecimal(),
                navChange = raw[KrxApiFields.Nav.CHANGE_AMOUNT].toStringSafe().toKrxBigDecimal(),
                navChangeRate = raw[KrxApiFields.Nav.CHANGE_RATE].toStringSafe().toKrxRate(),
                divergenceRate = raw[KrxApiFields.Nav.DIVERGENCE_RATE].toStringSafe().toKrxRate(),

                marketCap = raw[KrxApiFields.Asset.MARKET_CAP].toStringSafe().toKrxAmount(),
                netAssetValue = raw[KrxApiFields.Asset.NET_ASSET_TOTAL].toStringSafe().toKrxAmount(),
                listedShares = raw[KrxApiFields.Asset.LISTED_SHARES].toStringSafe().toKrxLong(),

                week52High = raw[KrxApiFields.Price.WEEK52_HIGH].toStringSafe().toKrxPrice(),
                week52HighDate = raw[KrxApiFields.DateTime.WEEK52_HIGH_DATE].toStringSafe().toKrxDate(),
                week52Low = raw[KrxApiFields.Price.WEEK52_LOW].toStringSafe().toKrxPrice(),
                week52LowDate = raw[KrxApiFields.DateTime.WEEK52_LOW_DATE].toStringSafe().toKrxDate(),

                indexValue = raw[KrxApiFields.Index.VALUE].toStringSafe().toKrxBigDecimal(),
                indexName = raw[KrxApiFields.Index.NAME].toStringSafe(),
                indexChange = raw[KrxApiFields.Index.CHANGE_AMOUNT].toStringSafe().toKrxBigDecimal(),
                indexChangeDirection = raw[KrxApiFields.PriceChange.INDEX_DIRECTION].toStringSafe().toIntOrNull() ?: 3,
                indexChangeRate = raw[KrxApiFields.Index.CHANGE_RATE].toStringSafe().toKrxRate(),

                listingDate = raw[KrxApiFields.DateTime.LISTING_DATE].toStringSafe().toKrxDate(),
                assetManager = raw[KrxApiFields.EtfMetadata.ASSET_MANAGER].toStringSafe(),
                totalFee = raw[KrxApiFields.EtfMetadata.TOTAL_EXPENSE_RATIO].toStringSafe().toKrxRate(),
                creationUnit = raw[KrxApiFields.EtfMetadata.CREATION_UNIT].toStringSafe().toKrxLong(),
                benchmarkIndex = raw[KrxApiFields.EtfMetadata.BENCHMARK_INDEX].toStringSafe(),
                indexProvider = raw[KrxApiFields.EtfMetadata.INDEX_PROVIDER].toStringSafe(),
                marketClassification = raw[KrxApiFields.EtfMetadata.MARKET_CLASSIFICATION].toStringSafe(),
                assetClass = raw[KrxApiFields.EtfMetadata.ASSET_CLASS].toStringSafe(),
                replicationMethod = raw[KrxApiFields.EtfMetadata.REPLICATION_METHOD].toStringSafe(),
                leverageType = raw[KrxApiFields.EtfMetadata.LEVERAGE_TYPE].toStringSafe(),
                taxType = raw[KrxApiFields.EtfMetadata.TAX_TYPE].toStringSafe(),
                marketName = raw[KrxApiFields.EtfMetadata.MARKET_NAME].toStringSafe()
            )
        }
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
     * 괴리율이 과도한지 확인 (절대값 1% 이상)
     *
     * @return 괴리율의 절대값이 1.0% 이상이면 true
     */
    fun hasExcessiveDivergence(): Boolean {
        return divergenceRate.abs() >= BigDecimal("1.0")
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
