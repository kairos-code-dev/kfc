package dev.kairoscode.kfc.infrastructure.common.ratelimit

/**
 * Rate Limiter 인터페이스
 *
 * Token Bucket 알고리즘을 기반으로 요청 속도를 제한합니다.
 */
interface RateLimiter {
    /**
     * 주어진 개수의 토큰을 소비합니다.
     * 토큰이 부족하면 자동으로 토큰이 충전될 때까지 대기합니다.
     *
     * @param tokensNeeded 필요한 토큰 개수 (기본값: 1)
     * @throws RateLimitTimeoutException 대기 시간이 초과되었을 때
     */
    suspend fun acquire(tokensNeeded: Int = 1)

    /**
     * 현재 사용 가능한 토큰 개수를 반환합니다.
     *
     * @return 현재 사용 가능한 토큰 개수
     */
    fun getAvailableTokens(): Int

    /**
     * 1개의 토큰을 획득하는 데 필요한 대기 시간(밀리초)을 반환합니다.
     *
     * @return 밀리초 단위의 대기 시간
     */
    fun getWaitTimeMillis(): Long

    /**
     * Rate Limiter의 현재 상태를 반환합니다.
     *
     * @return RateLimiterStatus 객체
     */
    fun getStatus(): RateLimiterStatus
}

/**
 * Rate Limiter의 현재 상태를 나타내는 데이터 클래스
 *
 * @param availableTokens 현재 사용 가능한 토큰 개수
 * @param capacity 토큰 버킷의 최대 용량
 * @param refillRate 초당 충전되는 토큰 수
 * @param isEnabled Rate limiting 활성화 여부
 * @param estimatedWaitTimeMs 1개 토큰 획득에 필요한 예상 대기 시간 (밀리초)
 */
data class RateLimiterStatus(
    val availableTokens: Int,
    val capacity: Int,
    val refillRate: Int,
    val isEnabled: Boolean,
    val estimatedWaitTimeMs: Long,
)
