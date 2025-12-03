package dev.kairoscode.kfc.internal.ratelimit

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
 * @param krx KRX API Rate Limiting 설정
 * @param naver Naver API Rate Limiting 설정
 * @param opendart OPENDART API Rate Limiting 설정
 */
data class RateLimitingSettings(
    val krx: RateLimitConfig = RateLimitConfig(),
    val naver: RateLimitConfig = RateLimitConfig(),
    val opendart: RateLimitConfig = RateLimitConfig()
) {
    companion object {
        /**
         * KRX API 기본 설정 (capacity=50, refillRate=50 req/sec)
         */
        fun krxDefault(): RateLimitConfig = RateLimitConfig(capacity = 50, refillRate = 50)

        /**
         * Naver API 기본 설정 (capacity=50, refillRate=50 req/sec)
         */
        fun naverDefault(): RateLimitConfig = RateLimitConfig(capacity = 50, refillRate = 50)

        /**
         * OPENDART API 기본 설정 (capacity=50, refillRate=50 req/sec)
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
