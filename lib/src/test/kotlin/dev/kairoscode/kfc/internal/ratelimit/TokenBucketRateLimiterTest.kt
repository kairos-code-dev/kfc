package dev.kairoscode.kfc.internal.ratelimit

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import kotlin.time.Duration.Companion.seconds

class TokenBucketRateLimiterTest {
    @Test
    fun `should consume tokens normally`() = runTest {
        // === arrange ===
        val config = RateLimitConfig(capacity = 10, refillRate = 10)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(1)
        limiter.acquire(2)
        limiter.acquire(3)

        // === assert ===
        assertThat(limiter.getAvailableTokens()).isEqualTo(4)
    }

    @Test
    fun `should allow burst requests up to capacity`() = runTest {
        // === arrange ===
        val config = RateLimitConfig(capacity = 50, refillRate = 50)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(50)

        // === assert ===
        assertThat(limiter.getAvailableTokens()).isEqualTo(0)
    }

    @Test
    fun `should wait for tokens to be refilled`() = runTest(timeout = 5.seconds) {
        // === arrange ===
        val config = RateLimitConfig(
            capacity = 10,
            refillRate = 100,
            waitTimeoutMillis = 5000L
        )
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(10)
        assertThat(limiter.getAvailableTokens()).isEqualTo(0)

        val startTime = System.currentTimeMillis()
        limiter.acquire(5)  // 5개 토큰이 필요 (약 50ms 대기)
        val elapsedTime = System.currentTimeMillis() - startTime

        // === assert ===
        // 대기했으므로 토큰이 충전됨
        assertThat(elapsedTime).isGreaterThanOrEqualTo(30)
    }

    @Test
    fun `should throw exception when timeout exceeded`() = runTest(timeout = 5.seconds) {
        // === arrange ===
        val config = RateLimitConfig(
            capacity = 1,
            refillRate = 1,
            waitTimeoutMillis = 300L  // 300ms 타임아웃
        )
        val limiter = TokenBucketRateLimiter(config)

        // === act & assert ===
        limiter.acquire(1)  // 초기 토큰 소비

        // 10개 토큰이 필요하지만, 초당 1개씩만 충전되므로 타임아웃 발생
        assertThrows<RateLimitTimeoutException> {
            limiter.acquire(10)
        }
    }

    @Test
    fun `should return immediately when disabled`() = runTest {
        // === arrange ===
        val config = RateLimitConfig(enabled = false)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        val startTime = System.currentTimeMillis()
        limiter.acquire(1000)
        val elapsedTime = System.currentTimeMillis() - startTime

        // === assert ===
        assertThat(elapsedTime).isLessThan(100)
    }

    @Test
    fun `should return correct status`() = runTest {
        // === arrange ===
        val config = RateLimitConfig(capacity = 100, refillRate = 50)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(30)
        val status = limiter.getStatus()

        // === assert ===
        assertThat(status.availableTokens).isEqualTo(70)
        assertThat(status.capacity).isEqualTo(100)
        assertThat(status.refillRate).isEqualTo(50)
        assertThat(status.isEnabled).isTrue()
        assertThat(status.estimatedWaitTimeMs).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun `should calculate correct wait time`() = runTest {
        // === arrange ===
        val config = RateLimitConfig(capacity = 10, refillRate = 100)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(10)
        val waitTime = limiter.getWaitTimeMillis()

        // === assert ===
        assertThat(waitTime).isBetween(5L, 15L)
    }

    @Test
    fun `should handle multiple acquisitions`() = runTest {
        // === arrange ===
        val config = RateLimitConfig(capacity = 100, refillRate = 100)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(25)
        limiter.acquire(25)
        limiter.acquire(25)
        limiter.acquire(20)

        // === assert ===
        assertThat(limiter.getAvailableTokens()).isEqualTo(5)
    }

    @Test
    fun `should throw exception when tokensNeeded is zero`() {
        // === arrange ===
        val config = RateLimitConfig()
        val limiter = TokenBucketRateLimiter(config)

        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            runTest {
                limiter.acquire(0)
            }
        }
    }

    @Test
    fun `should throw exception when tokensNeeded is negative`() {
        // === arrange ===
        val config = RateLimitConfig()
        val limiter = TokenBucketRateLimiter(config)

        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            runTest {
                limiter.acquire(-1)
            }
        }
    }
}
