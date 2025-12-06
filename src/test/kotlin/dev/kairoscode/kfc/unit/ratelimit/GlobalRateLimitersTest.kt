package dev.kairoscode.kfc.unit.ratelimit

import dev.kairoscode.kfc.infrastructure.common.ratelimit.GlobalRateLimiters
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimitConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * GlobalRateLimiters 싱글톤 테스트
 *
 * GlobalRateLimiters의 핵심 기능을 검증합니다:
 * - 싱글톤 인스턴스 보장
 * - Thread-safety (멀티스레드 환경에서의 안전성)
 * - 소스별 RateLimiter 독립성
 * - 설정 우선순위 (첫 호출 우선)
 * - Reset 기능
 *
 * 주의: 이 테스트 클래스는 순차 실행이 필요합니다 (resetForTesting의 thread-safety 보장)
 *
 * @see GlobalRateLimiters
 */
@Tag("unit")
@DisplayName("GlobalRateLimiters 싱글톤 테스트")
@Execution(ExecutionMode.SAME_THREAD)
class GlobalRateLimitersTest {
    @BeforeEach
    fun setup() {
        // 병렬 테스트 실행 시 다른 테스트의 영향을 받지 않도록 각 테스트 전에 리셋
        GlobalRateLimiters.resetForTesting()
    }

    @AfterEach
    fun cleanup() {
        GlobalRateLimiters.resetForTesting()
    }

    @Test
    @DisplayName("getKrxLimiter는 항상 동일한 인스턴스를 반환한다")
    fun `getKrxLimiter returns same instance on multiple calls`() {
        // Given & When: getKrxLimiter를 두 번 호출
        val firstInstance = GlobalRateLimiters.getKrxLimiter()
        val secondInstance = GlobalRateLimiters.getKrxLimiter()

        // Then: 동일한 참조를 반환
        assertThat(firstInstance).isSameAs(secondInstance)
    }

    @Test
    @DisplayName("getNaverLimiter는 항상 동일한 인스턴스를 반환한다")
    fun `getNaverLimiter returns same instance on multiple calls`() {
        // Given & When: getNaverLimiter를 두 번 호출
        val firstInstance = GlobalRateLimiters.getNaverLimiter()
        val secondInstance = GlobalRateLimiters.getNaverLimiter()

        // Then: 동일한 참조를 반환
        assertThat(firstInstance).isSameAs(secondInstance)
    }

    @Test
    @DisplayName("getOpendartLimiter는 항상 동일한 인스턴스를 반환한다")
    fun `getOpendartLimiter returns same instance on multiple calls`() {
        // Given & When: getOpendartLimiter를 두 번 호출
        val firstInstance = GlobalRateLimiters.getOpendartLimiter()
        val secondInstance = GlobalRateLimiters.getOpendartLimiter()

        // Then: 동일한 참조를 반환
        assertThat(firstInstance).isSameAs(secondInstance)
    }

    @Test
    @DisplayName("멀티스레드 환경에서도 싱글톤이 보장된다")
    fun `getKrxLimiter is thread-safe and returns same instance across multiple threads`() {
        // Given: 100개 스레드가 동시에 실행되도록 CountDownLatch 준비
        val threadCount = 100
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val instances = ConcurrentHashMap<Int, Any>()

        // When: 100개 스레드가 동시에 getKrxLimiter() 호출
        repeat(threadCount) { threadIndex ->
            thread {
                try {
                    // 모든 스레드가 대기하다가 동시에 시작
                    startLatch.await()

                    // RateLimiter 인스턴스 획득
                    val instance = GlobalRateLimiters.getKrxLimiter()
                    instances[threadIndex] = instance
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        // 모든 스레드 동시 시작
        startLatch.countDown()

        // 모든 스레드가 완료될 때까지 대기 (최대 5초)
        val completed = doneLatch.await(5, TimeUnit.SECONDS)
        assertThat(completed).isTrue()

        // Then: 모든 스레드가 동일한 인스턴스를 받았는지 확인
        val uniqueInstances = instances.values.toSet()
        assertThat(uniqueInstances).hasSize(1)
    }

    @Test
    @DisplayName("각 소스별 RateLimiter는 독립적이다")
    fun `each source has independent RateLimiter instances`() {
        // Given & When: 각 소스의 RateLimiter 획득
        val krxLimiter = GlobalRateLimiters.getKrxLimiter()
        val naverLimiter = GlobalRateLimiters.getNaverLimiter()
        val opendartLimiter = GlobalRateLimiters.getOpendartLimiter()

        // Then: 각 인스턴스가 서로 다름
        assertThat(krxLimiter).isNotSameAs(naverLimiter)
        assertThat(naverLimiter).isNotSameAs(opendartLimiter)
        assertThat(krxLimiter).isNotSameAs(opendartLimiter)
    }

    @Test
    @DisplayName("첫 번째 호출의 설정이 우선적으로 적용된다")
    fun `first call configuration takes precedence over subsequent calls`() {
        // Given: 첫 번째 호출에는 작은 capacity 설정
        val firstConfig = RateLimitConfig(capacity = 10, refillRate = 10)
        val firstInstance = GlobalRateLimiters.getKrxLimiter(firstConfig)
        val firstStatus = firstInstance.getStatus()

        // When: 두 번째 호출에는 큰 capacity 설정 (무시되어야 함)
        val secondConfig = RateLimitConfig(capacity = 100, refillRate = 100)
        val secondInstance = GlobalRateLimiters.getKrxLimiter(secondConfig)
        val secondStatus = secondInstance.getStatus()

        // Then: 두 호출이 동일한 인스턴스를 반환
        assertThat(firstInstance).isSameAs(secondInstance)

        // Then: 두 번째 호출도 첫 번째 설정(10/10) 사용
        assertThat(firstStatus.capacity).isEqualTo(10)
        assertThat(firstStatus.refillRate).isEqualTo(10)
        assertThat(secondStatus.capacity).isEqualTo(10)
        assertThat(secondStatus.refillRate).isEqualTo(10)
    }

    @Test
    @DisplayName("resetForTesting 호출 후 새로운 인스턴스가 생성된다")
    fun `resetForTesting creates new instances after reset`() {
        // Given: 초기 인스턴스 획득
        val initialKrxInstance = GlobalRateLimiters.getKrxLimiter()
        val initialNaverInstance = GlobalRateLimiters.getNaverLimiter()
        val initialOpendartInstance = GlobalRateLimiters.getOpendartLimiter()

        // When: resetForTesting 호출
        GlobalRateLimiters.resetForTesting()

        // Then: 다시 획득한 인스턴스가 이전과 다름
        val newKrxInstance = GlobalRateLimiters.getKrxLimiter()
        val newNaverInstance = GlobalRateLimiters.getNaverLimiter()
        val newOpendartInstance = GlobalRateLimiters.getOpendartLimiter()

        assertThat(newKrxInstance).isNotSameAs(initialKrxInstance)
        assertThat(newNaverInstance).isNotSameAs(initialNaverInstance)
        assertThat(newOpendartInstance).isNotSameAs(initialOpendartInstance)
    }

    @Test
    @DisplayName("reset 후 새로운 설정이 적용된다")
    fun `new configuration can be applied after reset`() {
        // Given: 첫 번째 설정으로 인스턴스 생성
        val firstConfig = RateLimitConfig(capacity = 10, refillRate = 10)
        val firstInstance = GlobalRateLimiters.getKrxLimiter(firstConfig)
        val firstStatus = firstInstance.getStatus()

        assertThat(firstStatus.capacity).isEqualTo(10)
        assertThat(firstStatus.refillRate).isEqualTo(10)

        // When: reset 후 새로운 설정으로 인스턴스 생성
        GlobalRateLimiters.resetForTesting()

        val secondConfig = RateLimitConfig(capacity = 100, refillRate = 100)
        val secondInstance = GlobalRateLimiters.getKrxLimiter(secondConfig)
        val secondStatus = secondInstance.getStatus()

        // Then: 새로운 설정이 적용됨
        assertThat(secondInstance).isNotSameAs(firstInstance)
        assertThat(secondStatus.capacity).isEqualTo(100)
        assertThat(secondStatus.refillRate).isEqualTo(100)
    }

    @Test
    @DisplayName("멀티스레드 환경에서 Naver Limiter도 싱글톤이 보장된다")
    fun `getNaverLimiter is thread-safe across multiple threads`() {
        // Given: 50개 스레드가 동시에 실행되도록 준비
        val threadCount = 50
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val instances = ConcurrentHashMap<Int, Any>()

        // When: 50개 스레드가 동시에 getNaverLimiter() 호출
        repeat(threadCount) { threadIndex ->
            thread {
                try {
                    startLatch.await()
                    val instance = GlobalRateLimiters.getNaverLimiter()
                    instances[threadIndex] = instance
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        val completed = doneLatch.await(5, TimeUnit.SECONDS)
        assertThat(completed).isTrue()

        // Then: 모든 스레드가 동일한 인스턴스 획득
        val uniqueInstances = instances.values.toSet()
        assertThat(uniqueInstances).hasSize(1)
    }

    @Test
    @DisplayName("멀티스레드 환경에서 Opendart Limiter도 싱글톤이 보장된다")
    fun `getOpendartLimiter is thread-safe across multiple threads`() {
        // Given: 50개 스레드가 동시에 실행되도록 준비
        val threadCount = 50
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val instances = ConcurrentHashMap<Int, Any>()

        // When: 50개 스레드가 동시에 getOpendartLimiter() 호출
        repeat(threadCount) { threadIndex ->
            thread {
                try {
                    startLatch.await()
                    val instance = GlobalRateLimiters.getOpendartLimiter()
                    instances[threadIndex] = instance
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        val completed = doneLatch.await(5, TimeUnit.SECONDS)
        assertThat(completed).isTrue()

        // Then: 모든 스레드가 동일한 인스턴스 획득
        val uniqueInstances = instances.values.toSet()
        assertThat(uniqueInstances).hasSize(1)
    }
}
