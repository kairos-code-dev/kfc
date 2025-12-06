package dev.kairoscode.kfc.infrastructure.common.ratelimit

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex

/**
 * Token Bucket 알고리즘 기반 Rate Limiter 구현
 *
 * 초기에 버킷이 가득 찬 상태(capacity)로 시작하며,
 * 시간이 경과함에 따라 refillRate에 따라 토큰이 충전됩니다.
 *
 * @param config Rate limiting 설정
 * @see RateLimitConfig
 */
class TokenBucketRateLimiter(
    private val config: RateLimitConfig,
) : RateLimiter {
    private val lock = Mutex()

    // 부동소수점으로 정밀한 토큰 계산
    private var tokens: Double = config.capacity.toDouble()

    // 마지막 토큰 충전 시간 (밀리초)
    private var lastRefillTime: Long = System.currentTimeMillis()

    /**
     * 주어진 개수의 토큰을 소비합니다.
     * 토큰이 부족하면 필요한 토큰이 충전될 때까지 대기합니다.
     *
     * @param tokensNeeded 필요한 토큰 개수 (기본값: 1)
     * @throws RateLimitTimeoutException 대기 시간이 초과되었을 때
     */
    override suspend fun acquire(tokensNeeded: Int) {
        // Rate limiting이 비활성화된 경우 즉시 반환
        if (!config.enabled) {
            return
        }

        require(tokensNeeded > 0) { "tokensNeeded must be greater than 0" }

        val startTime = System.currentTimeMillis()

        while (true) {
            // 토큰 상태 확인
            val canAcquire = lock.tryLock()
            if (canAcquire) {
                try {
                    // 경과 시간에 따라 토큰 충전
                    refillTokens()

                    if (tokens >= tokensNeeded) {
                        // 토큰이 충분하면 소비
                        tokens -= tokensNeeded
                        return
                    }

                    // 필요한 대기 시간 계산
                    val waitTimeMs = calculateWaitTimeMs(tokensNeeded)

                    // 타임아웃 체크
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime + waitTimeMs > config.waitTimeoutMillis) {
                        throw RateLimitTimeoutException(
                            source = "Unknown",
                            config = config,
                            message =
                                "Rate limit timeout exceeded after ${elapsedTime}ms, " +
                                    "need to wait additional ${waitTimeMs}ms for $tokensNeeded tokens",
                        )
                    }
                } finally {
                    lock.unlock()
                }
            }

            // 토큰이 충전될 때까지 대기
            delay(10) // 짧은 대기로 스핀락 방지
        }
    }

    /**
     * 현재 사용 가능한 토큰 개수를 반환합니다.
     */
    override fun getAvailableTokens(): Int = tokens.toInt()

    /**
     * 1개의 토큰을 획득하는 데 필요한 대기 시간(밀리초)을 반환합니다.
     */
    override fun getWaitTimeMillis(): Long = calculateWaitTimeMs(1)

    /**
     * Rate Limiter의 현재 상태를 반환합니다.
     */
    override fun getStatus(): RateLimiterStatus =
        RateLimiterStatus(
            availableTokens = getAvailableTokens(),
            capacity = config.capacity,
            refillRate = config.refillRate,
            isEnabled = config.enabled,
            estimatedWaitTimeMs = getWaitTimeMillis(),
        )

    /**
     * 경과 시간에 따라 토큰을 충전합니다.
     * 이 함수는 반드시 lock을 획득한 상태에서 호출되어야 합니다.
     */
    private fun refillTokens() {
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - lastRefillTime) / 1000.0 // 부동소수점으로 정밀한 계산

        // 충전할 토큰 수 계산
        val tokensToAdd = elapsedSeconds * config.refillRate

        // 토큰 업데이트
        tokens = minOf(tokens + tokensToAdd, config.capacity.toDouble())

        // 마지막 충전 시간 업데이트
        lastRefillTime = now
    }

    /**
     * 주어진 토큰 개수를 획득하기 위해 필요한 대기 시간을 계산합니다.
     *
     * @param tokensNeeded 필요한 토큰 개수
     * @return 밀리초 단위의 대기 시간
     */
    private fun calculateWaitTimeMs(tokensNeeded: Int): Long {
        if (config.refillRate == 0) {
            return Long.MAX_VALUE
        }

        val tokensNeeded = (tokensNeeded - tokens).coerceAtLeast(0.0)
        val secondsNeeded = tokensNeeded / config.refillRate
        return (secondsNeeded * 1000).toLong().coerceAtLeast(1L) // 최소 1ms
    }
}
