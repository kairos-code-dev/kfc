package dev.kairoscode.kfc

import dev.kairoscode.kfc.api.krx.KrxEtfApi
import dev.kairoscode.kfc.api.naver.NaverEtfApi
import dev.kairoscode.kfc.api.opendart.OpenDartApi
import dev.kairoscode.kfc.internal.krx.KrxEtfApiImpl
import dev.kairoscode.kfc.internal.naver.NaverEtfApiImpl
import dev.kairoscode.kfc.internal.opendart.OpenDartApiImpl
import dev.kairoscode.kfc.internal.ratelimit.RateLimitingSettings
import dev.kairoscode.kfc.internal.ratelimit.TokenBucketRateLimiter

/**
 * KFC (Korea Financial Client) 통합 클라이언트
 *
 * 한국 금융 데이터 조회를 위한 통합 Facade 클라이언트입니다.
 *
 * 이 클라이언트는 다음 API들에 대한 통합 접근을 제공합니다:
 * - **KRX API**: 한국거래소 ETF 데이터
 * - **Naver API**: 네이버 증권 조정주가 데이터
 * - **OPENDART API**: 금융감독원 공시 데이터
 *
 * ## 사용 예제
 *
 * ```kotlin
 * // 기본 생성 (KRX API만 사용)
 * val client = KfcClient.create()
 *
 * // OPENDART API 함께 사용
 * val clientWithOpendart = KfcClient.create(
 *     opendartApiKey = "YOUR_API_KEY"
 * )
 *
 * // KRX ETF 목록 조회
 * val etfList = client.krx.getEtfList()
 *
 * // 네이버 조정주가 조회
 * val adjustedPrice = client.naver.getAdjustedOhlcv(
 *     ticker = "069500",
 *     fromDate = LocalDate.of(2024, 1, 1),
 *     toDate = LocalDate.of(2024, 12, 31)
 * )
 *
 * // OPENDART 배당 정보 조회
 * val dividends = client.opendart?.getDividendInfo(
 *     corpCode = "00164779",
 *     year = 2024
 * )
 * ```
 *
 * @property krx KRX ETF API 클라이언트
 * @property naver Naver 증권 API 클라이언트
 * @property opendart OPENDART API 클라이언트 (API Key 제공 시에만 사용 가능)
 */
class KfcClient private constructor(
    val krx: KrxEtfApi,
    val naver: NaverEtfApi,
    val opendart: OpenDartApi?
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

            // API 구현체에 Rate Limiter 주입
            val krxApi = KrxEtfApiImpl(rateLimiter = krxRateLimiter)
            val naverApi = NaverEtfApiImpl(rateLimiter = naverRateLimiter)
            val opendartApi = opendartApiKey?.let { OpenDartApiImpl(apiKey = it, rateLimiter = opendartRateLimiter) }

            return KfcClient(
                krx = krxApi,
                naver = naverApi,
                opendart = opendartApi
            )
        }
    }
}
