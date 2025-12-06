package dev.kairoscode.kfc.infrastructure.common.recording

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlin.coroutines.coroutineContext

private val logger = KotlinLogging.logger {}

/**
 * HTTP 응답을 자동으로 ResponseRecordingContext에 캡처하는 Ktor 플러그인
 *
 * 이 플러그인은 Ktor HttpClient의 모든 HTTP 응답을 가로채서,
 * 현재 코루틴 컨텍스트에 ResponseRecordingContext가 있을 경우 응답 body를 저장합니다.
 *
 * 주요 특징:
 * - ResponseRecordingContext가 없으면 아무 동작도 하지 않음 (성능 영향 없음)
 * - 응답 캡처 중 에러가 발생해도 원본 HTTP 통신에 영향 없음
 * - 원본 응답은 변경되지 않고 그대로 반환됨
 *
 * 사용 예시:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(ResponseRecordingPlugin)
 *     // ... 다른 플러그인들
 * }
 * ```
 */
val ResponseRecordingPlugin =
    createClientPlugin("ResponseRecordingPlugin") {
        onResponse { response ->
            try {
                // 현재 코루틴 컨텍스트에서 ResponseRecordingContext 조회
                val recordingContext = coroutineContext[ResponseRecordingContext]

                if (recordingContext != null) {
                    // 응답 body를 문자열로 읽어서 저장
                    // bodyAsText()는 응답을 캐싱하므로 이후 다시 읽을 수 있음
                    val bodyText = response.bodyAsText()
                    recordingContext.setResponseBody(bodyText)
                    logger.debug { "Recorded HTTP response: ${response.request.url} (${bodyText.length} chars)" }
                }
            } catch (e: Exception) {
                // 레코딩 실패는 로그만 남기고 원본 응답 처리 계속
                logger.warn(e) { "Failed to record HTTP response: ${response.request.url}" }
            }
        }
    }

/**
 * HttpClient 설정에 ResponseRecordingPlugin을 설치하는 확장 함수
 *
 * 이 함수는 HttpClientConfig에서 사용할 수 있으며,
 * install(ResponseRecordingPlugin)과 동일한 효과를 가집니다.
 *
 * 사용 예시:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     installResponseRecording()
 *     // ... 다른 설정들
 * }
 * ```
 */
fun HttpClientConfig<*>.installResponseRecording() {
    install(ResponseRecordingPlugin)
}
