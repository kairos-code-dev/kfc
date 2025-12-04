package dev.kairoscode.kfc.infrastructure.common.ratelimit

/**
 * Rate Limiting 설정 데이터 클래스
 *
 * @param capacity 토큰 버킷의 최대 용량 (기본값: 50)
 * @param refillRate 초당 충전되는 토큰 수 (기본값: 50 req/sec)
 * @param enabled Rate limiting 활성화 여부 (기본값: true)
 * @param waitTimeoutMillis 대기 타임아웃 시간 (기본값: 60000ms = 60초)
 */
data class RateLimitConfig(
    val capacity: Int = 50,
    val refillRate: Int = 50,
    val enabled: Boolean = true,
    val waitTimeoutMillis: Long = 60000L
) {
    init {
        require(capacity > 0) { "capacity must be greater than 0" }
        require(refillRate > 0) { "refillRate must be greater than 0" }
        require(waitTimeoutMillis > 0) { "waitTimeoutMillis must be greater than 0" }
    }
}

/**
 * 모든 데이터 소스의 Rate Limiting 설정을 통합한 설정 클래스
 *
 * ## Rate Limit 기준 (테스트 결과 기반)
 *
 * ### KRX API
 * - **제한 방식**: 초당 요청 수 (RPS) 제한
 * - **한계**: 약 25 RPS (초당 25개 요청)
 * - **테스트 결과**: RPS 25까지 100% 성공, RPS 30부터 실패 시작
 * - **권장 설정**: capacity=25, refillRate=25
 *
 * ### OPENDART API
 * - **제한 방식**: 일일 요청 할당량
 * - **한계**: 하루 40,000건
 *
 * ## 동작 방식
 * 이 설정은 [GlobalRateLimiters]를 통해 JVM 프로세스별 싱글톤 Rate Limiter를 초기화하는 데 사용됩니다.
 * **첫 번째 [dev.kairoscode.kfc.api.KfcClient.create] 호출 시 전달된 설정이 해당 프로세스의 Rate Limiter를 초기화하며,
 * 이후 호출에서는 동일한 Rate Limiter 인스턴스가 재사용됩니다.**
 *
 * @param krx KRX API Rate Limiting 설정 (기본값: 25 RPS)
 * @param naver Naver API Rate Limiting 설정
 * @param opendart OPENDART API Rate Limiting 설정
 *
 * @see GlobalRateLimiters
 */
data class RateLimitingSettings(
    val krx: RateLimitConfig = RateLimitConfig(capacity = 25, refillRate = 25),
    val naver: RateLimitConfig = RateLimitConfig(),
    val opendart: RateLimitConfig = RateLimitConfig()
) {
    companion object {
        /**
         * KRX API 기본 설정
         *
         * 테스트 결과 KRX는 초당 약 25개 요청까지 허용합니다.
         * - RPS 25: 100% 성공
         * - RPS 30: 72% 성공 (한계 초과)
         */
        fun krxDefault(): RateLimitConfig = RateLimitConfig(capacity = 25, refillRate = 25)

        /**
         * Naver API 기본 설정 (capacity=50, refillRate=50 req/sec)
         */
        fun naverDefault(): RateLimitConfig = RateLimitConfig(capacity = 50, refillRate = 50)

        /**
         * OPENDART API 기본 설정 (capacity=50, refillRate=50 req/sec)
         *
         * 참고: OPENDART는 일일 40,000건 할당량 제한이 있습니다.
         */
        fun openDartDefault(): RateLimitConfig = RateLimitConfig(capacity = 50, refillRate = 50)

        /**
         * Rate Limiting 비활성화 설정 (모든 소스)
         */
        fun unlimited(): RateLimitingSettings = RateLimitingSettings(
            krx = RateLimitConfig(enabled = false),
            naver = RateLimitConfig(enabled = false),
            opendart = RateLimitConfig(enabled = false)
        )
    }
}
