package dev.kairoscode.kfc.integration.utils

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.infrastructure.common.recording.ResponseRecordingContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.Properties
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Integration Test의 공통 베이스 클래스
 *
 * 실제 API 호출을 수행하며, 선택적으로 응답을 레코딩합니다.
 * - @Tag("integration"): JUnit 5 태그로 Integration Test 식별
 * - @TestInstance(PER_CLASS): 클래스당 하나의 인스턴스로 KfcClient 재사용
 * - local.properties에서 OPENDART_API_KEY 로드
 * - RecordingConfig.isRecordingEnabled로 레코딩 모드 확인
 *
 * ## 제공 메서드
 *
 * ### 기본 테스트 실행
 * - [integrationTest]: 기본 Integration Test 실행 (타임아웃 설정)
 *
 * ### 자동 레코딩 헬퍼
 * - [integrationTest]: Raw JSON 응답 자동 캡처 및 레코딩
 *
 * ## 사용 예제
 * ```kotlin
 * class MyApiSpec : IntegrationTestBase() {
 *
 *     @Test
 *     fun `기본 테스트`() = integrationTest {
 *         val result = client.someApi.getData()
 *         assertNotNull(result)
 *     }
 *
 *     @Test
 *     fun `Raw JSON 레코딩`() = integrationTestWithRecording(
 *         category = "api/data",
 *         fileName = "sample_response"
 *     ) {
 *         client.someApi.getData()  // 응답이 자동으로 캡처됨
 *     }
 * }
 * ```
 */
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {
    protected lateinit var client: KfcClient

    // 메모리 모니터링용
    private var initialMemoryUsed: Long = 0

    @BeforeAll
    fun setUp() {
        // 초기 메모리 사용량 기록
        initialMemoryUsed = getUsedMemoryMB()

        val apiKey = loadApiKey()

        // OPENDART API 키가 필요한 경우만 체크
        // KRX, Naver는 API 키 불필요

        client =
            if (apiKey != null) {
                KfcClient.create(opendartApiKey = apiKey)
            } else {
                println("[IntegrationTest] OPENDART_API_KEY가 설정되지 않았습니다. Corp API 테스트는 skip됩니다.")
                KfcClient.create(opendartApiKey = null) // Funds API는 키 없이도 동작
            }

        println("[IntegrationTest] Integration Test 시작")
        println("[IntegrationTest] Recording 활성화: ${RecordingConfig.isRecordingEnabled}")
        if (RecordingConfig.isRecordingEnabled) {
            println("[IntegrationTest] 레코딩 경로: ${RecordingConfig.baseOutputPath}")
            println("[IntegrationTest] 사용 가능한 헬퍼 메서드:")
            println("[IntegrationTest]   - integrationTest(): 기본 테스트 실행")
            println("[IntegrationTest]   - integrationTestWithRecording(): Raw JSON 자동 캡처")
        }
        println("[IntegrationTest] 초기 메모리: ${initialMemoryUsed}MB")
    }

    @AfterAll
    fun tearDown() {
        if (::client.isInitialized) {
            val finalMemoryUsed = getUsedMemoryMB()
            val maxMemory = getMaxMemoryMB()
            println("[IntegrationTest] Integration Test 종료")
            println(
                "[IntegrationTest] Memory usage: $finalMemoryUsed/$maxMemory MB (초기: ${initialMemoryUsed}MB, 증가: ${finalMemoryUsed - initialMemoryUsed}MB)",
            )
        }
    }

    /**
     * API 키를 환경변수 또는 local.properties에서 로드
     * 우선순위: 환경변수 > local.properties
     */
    private fun loadApiKey(): String? {
        // 1. 환경변수에서 먼저 확인 (GitHub Actions용)
        System.getenv("OPENDART_API_KEY")?.let { return it }

        // 2. local.properties에서 확인 (로컬 개발용)
        val localPropertiesFile = File("local.properties")
        if (localPropertiesFile.exists()) {
            val properties = Properties()
            localPropertiesFile.inputStream().use { properties.load(it) }
            return properties.getProperty("OPENDART_API_KEY")
        }
        return null
    }

    /**
     * OPENDART API Key 필요한 테스트에서 사용하는 skip 헬퍼
     *
     * API Key가 없으면 JUnit 5 Assumptions를 통해 테스트를 skip 처리합니다.
     * 테스트 결과에서 'passed'가 아닌 'skipped'로 표시됩니다.
     *
     * ## 사용 예제
     * ```kotlin
     * @Test
     * fun `법인 정보 조회`() = integrationTest {
     *     requireOpendartApiKey() // API Key 없으면 skip
     *     val result = client.corp!!.getCorpCodeList()
     *     assertNotNull(result)
     * }
     * ```
     */
    protected fun requireOpendartApiKey() {
        Assumptions.assumeTrue(
            hasOpendartApiKey,
            "OPENDART_API_KEY가 설정되지 않아 테스트를 skip합니다. " +
                "(환경변수 또는 local.properties에 설정 필요)",
        )
    }

    /**
     * OPENDART API Key 사용 가능 여부
     */
    protected val hasOpendartApiKey: Boolean
        get() = client.corp != null

    // ========================================
    // 메모리 모니터링 유틸리티
    // ========================================

    /**
     * 현재 사용 중인 메모리를 MB 단위로 반환
     */
    private fun getUsedMemoryMB(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    }

    /**
     * 최대 사용 가능 메모리를 MB 단위로 반환
     */
    private fun getMaxMemoryMB(): Long = Runtime.getRuntime().maxMemory() / (1024 * 1024)

    // ========================================
    // 기본 테스트 실행 헬퍼
    // ========================================

    /**
     * 테스트 실행 헬퍼 (타임아웃 설정)
     *
     * 기본적인 Integration Test 실행을 위한 헬퍼 메서드입니다.
     * 레코딩이 필요 없는 단순 테스트에 사용합니다.
     *
     * @param timeout 테스트 타임아웃 (기본값: 30초)
     * @param block 실행할 테스트 블록
     *
     * ## 사용 예제
     * ```kotlin
     * @Test
     * fun `펀드 목록 조회`() = integrationTest {
     *     val fundsList = client.funds.getList()
     *     assertNotNull(fundsList)
     *     assertTrue(fundsList.isNotEmpty())
     * }
     * ```
     */
    protected fun integrationTest(
        timeout: Duration = 30.seconds,
        block: suspend () -> Unit,
    ) = runTest(timeout = timeout) {
        block()
    }

    // ========================================
    // 자동 레코딩 헬퍼 메서드
    // ========================================

    /**
     * Raw JSON 응답을 자동으로 캡처하여 레코딩하는 테스트 헬퍼
     *
     * [ResponseRecordingContext]를 사용하여 API 응답을 자동으로 캡처하고,
     * [ResponseRecorder.recordRaw]를 통해 파일로 저장합니다.
     *
     * HTTP 클라이언트가 ResponseRecordingContext를 인식하여 응답 body를 자동으로 캡처합니다.
     * 캡처된 응답은 테스트 종료 후 지정된 경로에 JSON 파일로 저장됩니다.
     *
     * @param category 레코딩 카테고리 경로 (예: "eodhd/exchange", "opendart/corp")
     * @param fileName 저장할 파일명 (확장자 제외)
     * @param timeout 테스트 타임아웃 (기본값: 30초)
     * @param block 실행할 테스트 블록 (API 호출 포함)
     *
     * ## 동작 원리
     * 1. ResponseRecordingContext를 코루틴 컨텍스트에 추가
     * 2. block 내에서 API 호출 실행
     * 3. HTTP 클라이언트가 응답 body를 컨텍스트에 저장
     * 4. 테스트 종료 후 캡처된 JSON을 파일로 저장
     *
     * ## 사용 예제
     * ```kotlin
     * @Test
     * fun `거래소 심볼 목록 조회 및 레코딩`() = integrationTestWithRecording(
     *     category = "eodhd/exchange",
     *     fileName = "symbols_us"
     * ) {
     *     val symbols = client.eodhd.getExchangeSymbols("US")
     *     assertNotNull(symbols)
     * }
     * ```
     *
     * ## 주의사항
     * - 블록 내에서 여러 API 호출이 있으면 마지막 응답만 캡처됩니다
     * - RecordingConfig.isRecordingEnabled가 false이면 레코딩은 스킵됩니다
     *
     * @see ResponseRecordingContext
     * @see ResponseRecorder.recordRaw
     */
    protected inline fun integrationTest(
        category: String,
        fileName: String,
        timeout: Duration = 30.seconds,
        crossinline block: suspend () -> Unit,
    ): Unit =
        runTest(timeout = timeout) {
            val recordingContext = ResponseRecordingContext()
            withContext(recordingContext) {
                block()

                // 캡처된 응답 body가 있으면 레코딩
                val responseBody = recordingContext.getResponseBody()
                if (responseBody != null) {
                    ResponseRecorder.recordRaw(
                        jsonString = responseBody,
                        category = category,
                        fileName = fileName,
                    )
                } else if (RecordingConfig.isRecordingEnabled) {
                    println("[IntegrationTest] Warning: 캡처된 응답이 없습니다. ($category/$fileName)")
                }
            }
        }
}
