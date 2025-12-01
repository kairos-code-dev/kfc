package dev.kairoscode.kfc.utils

import dev.kairoscode.kfc.KfcClient
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.Properties
import kotlin.time.Duration.Companion.seconds

/**
 * Live Test의 공통 베이스 클래스
 *
 * 실제 API 호출을 수행하며, 선택적으로 응답을 레코딩합니다.
 * - @Tag("live"): JUnit 5 태그로 Live Test 식별
 * - @TestInstance(PER_CLASS): 클래스당 하나의 인스턴스로 KfcClient 재사용
 * - local.properties에서 OPENDART_API_KEY 로드
 * - RecordingConfig.isRecordingEnabled로 레코딩 모드 확인
 */
@Tag("live")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class LiveTestBase {

    protected lateinit var client: KfcClient

    @BeforeAll
    fun setUp() {
        val apiKey = loadApiKey()

        // OPENDART API 키가 필요한 경우만 체크
        // KRX, Naver는 API 키 불필요

        client = if (apiKey != null) {
            KfcClient.create(opendartApiKey = apiKey)
        } else {
            println("ℹ️  OPENDART_API_KEY가 설정되지 않았습니다. Corp API 테스트는 skip됩니다.")
            KfcClient.create(opendartApiKey = null) // ETF API는 키 없이도 동작
        }

        println("🚀 Live Test 시작 - Recording: ${RecordingConfig.isRecordingEnabled}")
    }

    @AfterAll
    fun tearDown() {
        if (::client.isInitialized) {
            println("🏁 Live Test 종료")
        }
    }

    /**
     * API 키를 local.properties에서 로드
     */
    private fun loadApiKey(): String? {
        val localPropertiesFile = File("local.properties")
        if (localPropertiesFile.exists()) {
            val properties = Properties()
            localPropertiesFile.inputStream().use { properties.load(it) }
            return properties.getProperty("OPENDART_API_KEY")
        }
        return null
    }

    /**
     * 테스트 실행 헬퍼 (타임아웃 설정)
     */
    protected fun liveTest(
        timeout: kotlin.time.Duration = 30.seconds,
        block: suspend () -> Unit
    ) = runTest(timeout = timeout) {
        block()
    }
}
