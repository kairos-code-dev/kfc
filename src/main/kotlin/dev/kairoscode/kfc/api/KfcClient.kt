package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.infrastructure.krx.BondApiImpl
import dev.kairoscode.kfc.infrastructure.krx.FundsApiImpl
import dev.kairoscode.kfc.infrastructure.krx.PriceApiImpl
import dev.kairoscode.kfc.infrastructure.krx.StockApiImpl
import dev.kairoscode.kfc.infrastructure.krx.KrxBondApiImpl
import dev.kairoscode.kfc.infrastructure.krx.KrxFundsApiImpl
import dev.kairoscode.kfc.infrastructure.krx.KrxStockApiImpl
import dev.kairoscode.kfc.infrastructure.opendart.CorpApiImpl
import dev.kairoscode.kfc.infrastructure.opendart.FinancialsApiImpl
import dev.kairoscode.kfc.infrastructure.opendart.OpenDartApiImpl
import dev.kairoscode.kfc.infrastructure.common.ratelimit.GlobalRateLimiters
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimitingSettings
import dev.kairoscode.kfc.infrastructure.common.ratelimit.TokenBucketRateLimiter

/**
 * KFC (Korea Financial Client) 통합 클라이언트
 *
 * 한국 금융 데이터 조회를 위한 통합 Facade 클라이언트입니다.
 *
 * 이 클라이언트는 다음 도메인에 대한 통합 접근을 제공합니다:
 * - **펀드/증권상품 도메인**: ETF 및 기타 펀드 관련 메타데이터 (KRX)
 * - **가격 정보 도메인**: 시세 및 OHLCV 데이터 (KRX + Naver 통합)
 * - **주식 종목 도메인**: 종목 리스트, 기본정보, 섹터/산업 분류 (KRX)
 * - **채권 수익률 도메인**: 장외 채권 수익률 정보 (KRX)
 * - **기업 공시 도메인**: 기업 공시 관련 데이터 (OPENDART)
 * - **재무제표 도메인**: 손익계산서, 재무상태표, 현금흐름표 (OPENDART)
 *
 * ## 사용 예제
 *
 * ```kotlin
 * // 기본 생성 (펀드/증권상품 도메인만 사용)
 * val kfc = KfcClient.create()
 *
 * // OPENDART API 함께 사용
 * val kfcWithOpendart = KfcClient.create(
 *     opendartApiKey = "YOUR_API_KEY"
 * )
 *
 * // ETF 목록 조회 (from KRX)
 * val etfList = kfc.funds.getList()
 *
 * // OHLCV 조회 (from KRX)
 * val ohlcv = kfc.price.getOhlcv(
 *     isin = "KR7152100004",
 *     fromDate = LocalDate.of(2024, 1, 1),
 *     toDate = LocalDate.of(2024, 12, 31)
 * )
 *
 * // 조정주가 조회 (from Naver)
 * val adjustedPrice = kfc.price.getAdjustedOhlcv(
 *     ticker = "069500",
 *     fromDate = LocalDate.of(2024, 1, 1),
 *     toDate = LocalDate.of(2024, 12, 31)
 * )
 *
 * // 주식 종목 리스트 조회 (from KRX)
 * val stocks = kfc.stock.getStockList(market = Market.KOSPI)
 *
 * // 채권 수익률 조회 (from KRX)
 * val bondYields = kfc.bond.getBondYieldsByDate(LocalDate.now())
 *
 * // 배당 정보 조회 (from OPENDART)
 * val dividends = kfc.corp?.getDividendInfo(
 *     corpCode = "00164779",
 *     year = 2024
 * )
 *
 * // 재무제표 조회 (from OPENDART)
 * val incomeStatement = kfc.financials?.getIncomeStatement(
 *     corpCode = "00126380",
 *     year = 2024
 * )
 * ```
 *
 * @property funds 펀드/증권상품 도메인 API (펀드 메타데이터, 포트폴리오, 성과 정보)
 * @property price 가격 정보 도메인 API (시세, OHLCV, 조정주가)
 * @property stock 주식 종목 도메인 API (종목 리스트, 기본정보, 섹터/산업 분류)
 * @property bond 채권 수익률 도메인 API (장외 채권 수익률 정보)
 * @property corp 기업 공시 도메인 API (API Key 제공 시에만 사용 가능)
 * @property financials 재무제표 도메인 API (API Key 제공 시에만 사용 가능)
 */
class KfcClient internal constructor(
    val funds: FundsApi,
    val price: PriceApi,
    val stock: StockApi,
    val bond: BondApi,
    val corp: CorpApi?,
    val financials: FinancialsApi?
) {

    companion object {
        /**
         * KfcClient 인스턴스 생성
         *
         * Rate Limiting은 [GlobalRateLimiters] 싱글톤을 통해 관리되며,
         * 동일 JVM 프로세스 내의 모든 KfcClient 인스턴스가 소스별(KRX, Naver, OPENDART) Rate Limiter를 공유합니다.
         *
         * **중요**: 첫 번째 `create()` 호출 시 전달된 [rateLimitingSettings]가 해당 JVM 프로세스의
         * Rate Limiter 설정으로 영구 적용됩니다. 이후 호출에서 다른 설정을 전달해도 무시됩니다.
         *
         * ## 사용 예제
         * ```kotlin
         * // 첫 번째 생성 (이 설정이 적용됨)
         * val client1 = KfcClient.create(
         *     rateLimitingSettings = RateLimitingSettings(
         *         krx = RateLimitConfig(capacity = 25, refillRate = 25)
         *     )
         * )
         *
         * // 두 번째 생성 (client1과 동일한 Rate Limiter 공유)
         * val client2 = KfcClient.create() // client1과 동일한 KRX Rate Limiter 사용
         * ```
         *
         * @param opendartApiKey OPENDART API 인증키 (선택 사항)
         * @param rateLimitingSettings Rate Limiting 설정 (기본값: KRX 25 RPS, Naver/OPENDART 50 RPS)
         *                              **주의**: 첫 번째 호출의 설정만 적용됨
         * @return KfcClient 인스턴스
         *
         * @see GlobalRateLimiters
         * @see RateLimitingSettings
         */
        fun create(
            opendartApiKey: String? = null,
            rateLimitingSettings: RateLimitingSettings = RateLimitingSettings()
        ): KfcClient {
            // 글로벌 싱글톤 Rate Limiter 사용 (모든 KfcClient 인스턴스가 공유)
            val krxRateLimiter = GlobalRateLimiters.getKrxLimiter(rateLimitingSettings.krx)
            val naverRateLimiter = GlobalRateLimiters.getNaverLimiter(rateLimitingSettings.naver)
            val opendartRateLimiter = GlobalRateLimiters.getOpendartLimiter(rateLimitingSettings.opendart)

            // 소스별 API 구현체 생성 (Funds 도메인)
            val krxFundsApi = KrxFundsApiImpl(rateLimiter = krxRateLimiter)

            // 소스별 API 구현체 생성 (Stock 도메인)
            val krxStockApi = KrxStockApiImpl(rateLimiter = krxRateLimiter)

            // 소스별 API 구현체 생성 (Bond 도메인)
            val krxBondApi = KrxBondApiImpl(rateLimiter = krxRateLimiter)

            // 소스별 API 구현체 생성 (Corp 도메인, Financials 도메인)
            val openDartApi = opendartApiKey?.let { OpenDartApiImpl(apiKey = it, rateLimiter = opendartRateLimiter) }

            // 도메인별 API 구현체 생성 (소스별 API 재사용)
            val fundsApi = FundsApiImpl(krxFundsApi = krxFundsApi)
            val priceApi = PriceApiImpl(krxFundsApi = krxFundsApi)
            val stockApi = StockApiImpl(krxStockApi = krxStockApi)
            val bondApi = BondApiImpl(krxBondApi = krxBondApi)
            val corpApi = openDartApi?.let { CorpApiImpl(openDartApi = it) }
            val financialsApi = openDartApi?.let { FinancialsApiImpl(openDartApi = it) }

            return KfcClient(
                funds = fundsApi,
                price = priceApi,
                stock = stockApi,
                bond = bondApi,
                corp = corpApi,
                financials = financialsApi
            )
        }
    }
}
