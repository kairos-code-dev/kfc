package dev.kairoscode.kfc.integration.krx

import dev.kairoscode.kfc.common.TestFixtures
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * KRX API Rate Limit 한계 테스트
 *
 * 목적: KRX API의 최대 RPS(Requests Per Second)를 찾기
 */
@Disabled("별도 수동실행 ")
@DisplayName("KRX Rate Limit 한계 테스트")
@TestMethodOrder(OrderAnnotation::class)
class KrxRateLimitSpec : IntegrationTestBase() {
    companion object {
        private val TEST_ISIN = TestFixtures.Etf.TIGER_200_ISIN
        private val TRADE_DATE = LocalDate.now().minusDays(7)
        private val RESULTS_DIR = File("build/rate-limit-results")
        private val results = mutableListOf<String>()

        @BeforeAll
        @JvmStatic
        fun setup() {
            RESULTS_DIR.mkdirs()
        }

        @AfterAll
        @JvmStatic
        fun saveResults() {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val file = File(RESULTS_DIR, "rps-test-$timestamp.txt")
            file.writeText(results.joinToString("\n"))
            println("\n========== 결과 저장됨: ${file.absolutePath} ==========")
        }
    }

    private fun log(message: String) {
        println(message)
        results.add(message)
    }

    /**
     * 지정된 RPS로 요청을 보내고 성공률 측정
     */
    private suspend fun testRps(
        targetRps: Int,
        durationSeconds: Int = 5,
    ): RpsTestResult {
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val delayBetweenRequests = (1000.0 / targetRps).toLong()
        val totalRequests = targetRps * durationSeconds

        log("\n--- RPS $targetRps 테스트 시작 (${durationSeconds}초, 총 ${totalRequests}건) ---")

        val elapsed =
            measureTimeMillis {
                coroutineScope {
                    repeat(totalRequests) { i ->
                        launch {
                            try {
                                client.funds.getDetailedInfo(TEST_ISIN, TRADE_DATE)
                                successCount.incrementAndGet()
                            } catch (e: Exception) {
                                failCount.incrementAndGet()
                                if (failCount.get() <= 3) {
                                    log("  실패 #${failCount.get()}: ${e.message?.take(100)}")
                                }
                            }
                        }
                        delay(delayBetweenRequests)
                    }
                }
            }

        val actualRps = (successCount.get() + failCount.get()) * 1000.0 / elapsed
        val successRate = successCount.get() * 100.0 / totalRequests

        return RpsTestResult(
            targetRps = targetRps,
            actualRps = actualRps,
            totalRequests = totalRequests,
            successCount = successCount.get(),
            failCount = failCount.get(),
            successRate = successRate,
            elapsedMs = elapsed,
        )
    }

    data class RpsTestResult(
        val targetRps: Int,
        val actualRps: Double,
        val totalRequests: Int,
        val successCount: Int,
        val failCount: Int,
        val successRate: Double,
        val elapsedMs: Long,
    )

    /**
     * RPS 제한 검증 테스트
     *
     * 동시 연결 수는 높게 유지하면서 초당 요청 수만 조절
     * - 가설: 초당 20개 제한이면, 동시 연결 수와 관계없이 RPS 20 이하면 성공
     */
    @Test
    @Order(1)
    @DisplayName("RPS 제한 검증 (동시연결 10개, RPS 조절)")
    fun testRpsLimitVerification() =
        runBlocking {
            log("\n========== RPS 제한 검증 테스트 ==========")
            log("시작 시간: ${java.time.LocalDateTime.now()}")

            val semaphore = Semaphore(10) // 동시 10개 허용 (충분히 높게)
            val testCases = listOf(10, 15, 20, 25, 30) // 테스트할 RPS 값들

            for (targetRps in testCases) {
                delay(10000) // 각 테스트 사이 쿨다운

                val totalRequests = targetRps * 3 // 3초 동안 테스트
                val delayBetweenRequests = (1000.0 / targetRps).toLong()
                val successCount = AtomicInteger(0)
                val failCount = AtomicInteger(0)

                log("\n--- RPS $targetRps 테스트 (동시 10개, ${delayBetweenRequests}ms 간격) ---")

                val startTime = System.currentTimeMillis()

                coroutineScope {
                    repeat(totalRequests) {
                        launch {
                            semaphore.withPermit {
                                try {
                                    client.funds.getDetailedInfo(TEST_ISIN, TRADE_DATE)
                                    successCount.incrementAndGet()
                                } catch (e: Exception) {
                                    failCount.incrementAndGet()
                                }
                            }
                        }
                        delay(delayBetweenRequests)
                    }
                }

                val elapsedMs = System.currentTimeMillis() - startTime
                val successRate = successCount.get() * 100.0 / totalRequests
                val actualRps = successCount.get() * 1000.0 / elapsedMs

                log(
                    "RPS $targetRps: 성공=${successCount.get()}/$totalRequests (${String.format(
                        "%.0f",
                        successRate,
                    )}%), 실제 RPS=${String.format("%.1f", actualRps)}",
                )
            }

            log("\n종료 시간: ${java.time.LocalDateTime.now()}")
        }
}
