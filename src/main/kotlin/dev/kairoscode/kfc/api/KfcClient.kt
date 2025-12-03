package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.infrastructure.krx.FundsApiImpl
import dev.kairoscode.kfc.infrastructure.krx.PriceApiImpl
import dev.kairoscode.kfc.infrastructure.krx.KrxFundsApiImpl
import dev.kairoscode.kfc.infrastructure.opendart.CorpApiImpl
import dev.kairoscode.kfc.infrastructure.opendart.OpenDartApiImpl
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
 * - **기업 공시 도메인**: 기업 공시 관련 데이터 (OPENDART)
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
 * // 배당 정보 조회 (from OPENDART)
 * val dividends = kfc.corp?.getDividendInfo(
 *     corpCode = "00164779",
 *     year = 2024
 * )
 * ```
 *
 * @property funds 펀드/증권상품 도메인 API (펀드 메타데이터, 포트폴리오, 성과 정보)
 * @property price 가격 정보 도메인 API (시세, OHLCV, 조정주가)
 * @property corp 기업 공시 도메인 API (API Key 제공 시에만 사용 가능)
 */
class KfcClient internal constructor(
    val funds: FundsApi,
    val price: PriceApi,
    val corp: CorpApi?
) {

    companion object {
        /**
         * KfcClient 인스턴스 생성
         *
         * @param opendartApiKey OPENDART API 인증키 (선택 사항)
         * @param rateLimitingSettings Rate Limiting 설정 (기본값: 모든 소스 초당 50 req/sec)
         * @return KfcClient 인스턴스
         */
        fun create(
            opendartApiKey: String? = null,
            rateLimitingSettings: RateLimitingSettings = RateLimitingSettings()
        ): KfcClient {
            // Rate Limiter 인스턴스 생성
            val krxRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.krx)
            val naverRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.naver)
            val opendartRateLimiter = TokenBucketRateLimiter(rateLimitingSettings.opendart)

            // 소스별 API 구현체 생성 (Funds 도메인)
            val krxFundsApi = KrxFundsApiImpl(rateLimiter = krxRateLimiter)

            // 소스별 API 구현체 생성 (Corp 도메인)
            val openDartApi = opendartApiKey?.let { OpenDartApiImpl(apiKey = it, rateLimiter = opendartRateLimiter) }

            // 도메인별 API 구현체 생성 (소스별 API 재사용)
            val fundsApi = FundsApiImpl(krxFundsApi = krxFundsApi)
            val priceApi = PriceApiImpl(krxFundsApi = krxFundsApi)
            val corpApi = openDartApi?.let { CorpApiImpl(openDartApi = it) }

            return KfcClient(
                funds = fundsApi,
                price = priceApi,
                corp = corpApi
            )
        }
    }
}
