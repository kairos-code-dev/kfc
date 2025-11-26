package dev.kairoscode.kfc

import dev.kairoscode.kfc.internal.ratelimit.RateLimitConfig
import dev.kairoscode.kfc.internal.ratelimit.RateLimitingSettings
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.system.measureTimeMillis

/**
 * KfcClient Rate Limiting 통합 테스트
 *
 * Rate Limiting이 정상적으로 작동하는지 검증합니다.
 */
@Disabled("Integration tests requiring real API calls - run manually")
class KfcClientRateLimitingIntegrationTest {

    @Test
    fun `client should respect configured rate limits`() = runBlocking {
        // === arrange ===
        // 매우 낮은 rate limit 설정 (초당 1 request)
        val limitedSettings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 1, refillRate = 1, enabled = true, waitTimeoutMillis = 5000),
            naver = RateLimitConfig(capacity = 1, refillRate = 1, enabled = true, waitTimeoutMillis = 5000),
            opendart = RateLimitConfig(capacity = 1, refillRate = 1, enabled = true, waitTimeoutMillis = 5000)
        )
        val client = KfcClient.create(rateLimitingSettings = limitedSettings)

        // === act ===
        // 첫 번째 요청 (즉시 실행됨)
        val startTime = System.currentTimeMillis()
        val etfList1 = client.krx.getEtfList()
        val firstRequestTime = System.currentTimeMillis() - startTime

        // 두 번째 요청 (rate limit에 의해 대기)
        val secondRequestStart = System.currentTimeMillis()
        val etfList2 = client.krx.getEtfList()
        val secondRequestTime = System.currentTimeMillis() - secondRequestStart

        // === assert ===
        // 첫 번째 요청은 빠르게 실행됨
        assertThat(firstRequestTime).isLessThan(3000) // 3초 이내

        // 두 번째 요청은 rate limit 때문에 대기함 (약 1초 이상 대기)
        assertThat(secondRequestTime).isGreaterThanOrEqualTo(800) // 최소 800ms 대기

        // 두 결과 모두 같음
        assertThat(etfList1.size).isEqualTo(etfList2.size)
    }

    @Test
    fun `client with disabled rate limiting should not wait`() = runBlocking {
        // === arrange ===
        // Rate limit 비활성화
        val unlimitedSettings = RateLimitingSettings(
            krx = RateLimitConfig(enabled = false),
            naver = RateLimitConfig(enabled = false),
            opendart = RateLimitConfig(enabled = false)
        )
        val client = KfcClient.create(rateLimitingSettings = unlimitedSettings)

        // === act ===
        val totalTime = measureTimeMillis {
            val etfList1 = client.krx.getEtfList()
            val etfList2 = client.krx.getEtfList()
            assertThat(etfList1).isNotEmpty
            assertThat(etfList2).isNotEmpty
        }

        // === assert ===
        // Rate limiting이 비활성화되었으므로 두 요청 모두 빠르게 실행됨
        assertThat(totalTime).isLessThan(10000) // 10초 이내
    }

    @Test
    fun `client should allow burst requests up to capacity`() = runBlocking {
        // === arrange ===
        // 초당 5개 request 허용 (capacity = 5, refillRate = 5)
        val burstSettings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 5, refillRate = 5, enabled = true, waitTimeoutMillis = 5000),
            naver = RateLimitConfig(capacity = 5, refillRate = 5, enabled = true, waitTimeoutMillis = 5000),
            opendart = RateLimitConfig(capacity = 5, refillRate = 5, enabled = true, waitTimeoutMillis = 5000)
        )
        val client = KfcClient.create(rateLimitingSettings = burstSettings)

        // === act ===
        val totalTime = measureTimeMillis {
            repeat(5) {
                client.krx.getEtfList()
            }
        }

        // === assert ===
        // 5개의 burst request가 즉시 처리됨 (capacity = 5이므로)
        // 실제 API 호출 시간만 소요됨
        assertThat(totalTime).isLessThan(10000) // 각 요청이 수초 소요될 수 있음
    }

    @Test
    fun `different API sources should have independent rate limiters`() = runBlocking {
        // === arrange ===
        val limitedSettings = RateLimitingSettings(
            krx = RateLimitConfig(capacity = 1, refillRate = 1, enabled = true, waitTimeoutMillis = 5000),
            naver = RateLimitConfig(capacity = 10, refillRate = 10, enabled = true, waitTimeoutMillis = 5000),
            opendart = RateLimitConfig(capacity = 5, refillRate = 5, enabled = true, waitTimeoutMillis = 5000)
        )
        val client = KfcClient.create(rateLimitingSettings = limitedSettings)
        val date = LocalDate.now().minusDays(1)

        // === act ===
        // KRX는 1 req/sec 제한
        val krxTime = measureTimeMillis {
            repeat(2) {
                client.krx.getEtfList()
            }
        }

        // Naver는 10 req/sec 제한 (더 관대함)
        val naverTime = measureTimeMillis {
            repeat(2) {
                client.naver.getAdjustedOhlcv("069500", date, date)
            }
        }

        // === assert ===
        // KRX는 rate limit이 더 엄격하므로 대기 시간이 김
        assertThat(krxTime).isGreaterThan(500)
        // Naver는 capacity가 크므로 버스트 요청 가능
        assertThat(naverTime).isLessThan(krxTime)
    }

    @Test
    fun `client should work with default rate limiting settings`() = runBlocking {
        // === arrange ===
        // 기본 설정 (초당 50 request)
        val client = KfcClient.create()

        // === act ===
        val etfList = client.krx.getEtfList()
        val ticker = "069500"
        val date = LocalDate.now().minusDays(1)
        val adjustedOhlcv = client.naver.getAdjustedOhlcv(ticker, date, date)

        // === assert ===
        assertThat(etfList).isNotEmpty
        assertThat(adjustedOhlcv).isNotEmpty
    }
}
