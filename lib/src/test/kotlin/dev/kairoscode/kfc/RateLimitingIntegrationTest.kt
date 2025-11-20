package dev.kairoscode.kfc

import dev.kairoscode.kfc.internal.ratelimit.RateLimitConfig
import dev.kairoscode.kfc.internal.ratelimit.RateLimitingSettings
import dev.kairoscode.kfc.internal.ratelimit.TokenBucketRateLimiter
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * Rate Limiting 통합 테스트
 *
 * Rate Limiter가 여러 API에 걸쳐 정상적으로 작동하는지 검증합니다.
 */
class RateLimitingIntegrationTest {

    @Test
    fun `different API sources should have independent rate limiters`() = runBlocking {
        // === arrange ===
        val krxConfig = RateLimitConfig(capacity = 10, refillRate = 100, enabled = true)
        val naverConfig = RateLimitConfig(capacity = 20, refillRate = 200, enabled = true)
        val opendartConfig = RateLimitConfig(capacity = 5, refillRate = 50, enabled = true)

        val krxLimiter = TokenBucketRateLimiter(krxConfig)
        val naverLimiter = TokenBucketRateLimiter(naverConfig)
        val opendartLimiter = TokenBucketRateLimiter(opendartConfig)

        // === act ===
        krxLimiter.acquire(5)
        val krxStatus = krxLimiter.getStatus()

        naverLimiter.acquire(10)
        val naverStatus = naverLimiter.getStatus()

        opendartLimiter.acquire(2)
        val opendartStatus = opendartLimiter.getStatus()

        // === assert ===
        // Each limiter should maintain its own token state independently
        assertThat(krxStatus.availableTokens).isEqualTo(5) // 10 - 5
        assertThat(naverStatus.availableTokens).isEqualTo(10) // 20 - 10
        assertThat(opendartStatus.availableTokens).isEqualTo(3) // 5 - 2
    }

    @Test
    fun `rate limiting settings should initialize different configs per source`() {
        // === arrange & act ===
        val settings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 50, refillRate = 50, enabled = true),
            naver = RateLimitConfig(capacity = 100, refillRate = 100, enabled = true),
            opendart = RateLimitConfig(capacity = 30, refillRate = 30, enabled = true)
        )

        // === assert ===
        assertThat(settings.krx.capacity).isEqualTo(50)
        assertThat(settings.naver.capacity).isEqualTo(100)
        assertThat(settings.opendart.capacity).isEqualTo(30)

        assertThat(settings.krx.refillRate).isEqualTo(50)
        assertThat(settings.naver.refillRate).isEqualTo(100)
        assertThat(settings.opendart.refillRate).isEqualTo(30)
    }

    @Test
    fun `default rate limiting settings should have consistent values`() {
        // === arrange & act ===
        val settings = RateLimitingSettings()

        // === assert ===
        // All should have same default values (50 req/sec)
        assertThat(settings.krx.capacity).isEqualTo(50)
        assertThat(settings.naver.capacity).isEqualTo(50)
        assertThat(settings.opendart.capacity).isEqualTo(50)

        assertThat(settings.krx.refillRate).isEqualTo(50)
        assertThat(settings.naver.refillRate).isEqualTo(50)
        assertThat(settings.opendart.refillRate).isEqualTo(50)
    }

    @Test
    fun `rate limiters can be disabled independently`() = runBlocking {
        // === arrange ===
        val disabledConfig = RateLimitConfig(enabled = false)
        val enabledConfig = RateLimitConfig(enabled = true, capacity = 1, refillRate = 1)

        val disabledLimiter = TokenBucketRateLimiter(disabledConfig)
        val enabledLimiter = TokenBucketRateLimiter(enabledConfig)

        // === act ===
        val disabledTime = measureTimeMillis {
            repeat(100) {
                disabledLimiter.acquire(1)
            }
        }

        val enabledTime = measureTimeMillis {
            repeat(2) {
                enabledLimiter.acquire(1)
            }
        }

        // === assert ===
        // Disabled limiter should be fast (no waiting)
        assertThat(disabledTime).isLessThan(500)
        // Enabled limiter with low capacity should wait
        assertThat(enabledTime).isGreaterThan(500)
    }

    @Test
    fun `kfc client should create with custom rate limiting settings`() {
        // === arrange ===
        val customSettings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 25, refillRate = 25, enabled = true),
            naver = RateLimitConfig(capacity = 75, refillRate = 75, enabled = true),
            opendart = RateLimitConfig(capacity = 15, refillRate = 15, enabled = true)
        )

        // === act ===
        val client = KfcClient.create(rateLimitingSettings = customSettings)

        // === assert ===
        assertThat(client.krx).isNotNull
        assertThat(client.naver).isNotNull
        assertThat(client.opendart).isNull() // No API key provided
    }

    @Test
    fun `kfc client should create with default rate limiting settings`() {
        // === arrange & act ===
        val client = KfcClient.create()

        // === assert ===
        assertThat(client.krx).isNotNull
        assertThat(client.naver).isNotNull
        assertThat(client.opendart).isNull() // No API key provided
    }

    @Test
    fun `multiple clients should have independent rate limiters`() = runBlocking {
        // === arrange ===
        val client1 = KfcClient.create()
        val client2 = KfcClient.create()

        // === act & assert ===
        // Both clients should be usable independently
        assertThat(client1.krx).isNotSameAs(client2.krx)
        assertThat(client1.naver).isNotSameAs(client2.naver)
    }

    @Test
    fun `rate limiting should enforce acquisition order`() = runBlocking {
        // === arrange ===
        val config = RateLimitConfig(capacity = 3, refillRate = 100, enabled = true)
        val limiter = TokenBucketRateLimiter(config)

        // === act ===
        limiter.acquire(1)
        limiter.acquire(1)
        limiter.acquire(1)
        val statusAfterThree = limiter.getStatus()

        // === assert ===
        assertThat(statusAfterThree.availableTokens).isEqualTo(0)
        assertThat(statusAfterThree.capacity).isEqualTo(3)
    }
}
