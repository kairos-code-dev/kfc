package dev.kairoscode.kfc.unit.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json

/**
 * Mock HTTP Client 생성 팩토리
 *
 * Unit Test에서 실제 API 호출 없이 테스트할 수 있도록
 * Mock HttpClient를 생성합니다.
 *
 * ## 사용 예제
 * ```kotlin
 * val mockClient = MockHttpClientFactory.createWithResponse("""
 *     {"status": "success", "data": [...]}
 * """)
 * ```
 */
object MockHttpClientFactory {
    /**
     * JSON 응답을 반환하는 Mock HttpClient 생성
     *
     * @param jsonResponse Mock으로 반환할 JSON 문자열
     * @return Mock HttpClient 인스턴스
     */
    fun createWithResponse(jsonResponse: String): HttpClient {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = ByteReadChannel(jsonResponse),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                        prettyPrint = true
                    },
                )
            }
        }
    }

    /**
     * 에러 응답을 반환하는 Mock HttpClient 생성
     *
     * @param statusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지 (optional)
     * @return Mock HttpClient 인스턴스
     */
    fun createWithError(
        statusCode: HttpStatusCode,
        errorMessage: String = statusCode.description,
    ): HttpClient {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = ByteReadChannel("""{"error": "$errorMessage"}"""),
                    status = statusCode,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    },
                )
            }
        }
    }

    /**
     * 여러 응답을 순차적으로 반환하는 Mock HttpClient 생성
     *
     * @param responses Mock으로 반환할 JSON 문자열 목록
     * @return Mock HttpClient 인스턴스
     */
    fun createWithMultipleResponses(responses: List<String>): HttpClient {
        var callCount = 0

        val mockEngine =
            MockEngine { _ ->
                val response =
                    if (callCount < responses.size) {
                        responses[callCount]
                    } else {
                        responses.last()
                    }
                callCount++

                respond(
                    content = ByteReadChannel(response),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    },
                )
            }
        }
    }
}
