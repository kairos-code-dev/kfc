package dev.kairoscode.kfc.internal.krx

import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

private val logger = KotlinLogging.logger {}

/**
 * JsonElement를 Map<String, Any?>로 변환하는 확장 함수
 */
private fun JsonElement.toMap(): Map<String, Any?> {
    return when (this) {
        is JsonObject -> this.mapValues { (_, value) -> value.toAny() }
        else -> throw KfcException(ErrorCode.FIELD_TYPE_MISMATCH)
    }
}

/**
 * JsonElement를 Any?로 변환하는 확장 함수
 */
private fun JsonElement.toAny(): Any? {
    return when (this) {
        is JsonNull -> null
        is JsonPrimitive -> {
            when {
                this.isString -> this.content
                this.booleanOrNull != null -> this.boolean
                this.longOrNull != null -> this.long
                this.doubleOrNull != null -> this.double
                else -> this.content
            }
        }
        is JsonArray -> this.map { it.toAny() }
        is JsonObject -> this.mapValues { (_, value) -> value.toAny() }
    }
}

/**
 * KRX API 통신을 위한 HTTP 클라이언트
 *
 * 이 클래스는 KRX API와의 모든 HTTP 통신을 담당합니다.
 * Ktor HttpClient를 사용하여 구현되었으며, 다음과 같은 기능을 제공합니다:
 * - JSON 자동 직렬화/역직렬화
 * - 타임아웃 설정
 * - 공통 헤더 설정
 * - 에러 핸들링
 *
 * 이 클래스는 internal 가시성을 가지며, 라이브러리 사용자에게 노출되지 않습니다.
 */
internal class KrxHttpClient {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000 // 30초
            connectTimeoutMillis = 10_000 // 10초
            socketTimeoutMillis = 30_000   // 30초
        }

        // 기본 헤더 설정 (KRX API는 웹 브라우저 헤더 필요)
        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            header(HttpHeaders.Accept, "application/json, text/plain, */*")
            header(HttpHeaders.AcceptLanguage, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
            header(HttpHeaders.Referrer, "http://data.krx.co.kr/")
            header(HttpHeaders.Origin, "http://data.krx.co.kr")
        }

        // 에러 응답 검증
        expectSuccess = false
    }

    /**
     * POST 요청을 수행하고 응답을 Map으로 반환
     *
     * @param url 요청 URL
     * @param parameters 요청 파라미터
     * @return 응답 데이터 (Map 형태)
     * @throws NetworkException 네트워크 에러 발생 시
     * @throws ParseException 응답 파싱 실패 시
     */
    suspend fun post(url: String, parameters: Map<String, String>): Map<String, Any?> {
        logger.debug { "POST request to $url with parameters: $parameters" }

        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(parameters.entries.joinToString("&") { "${it.key}=${it.value}" })
            }

            when {
                response.status.isSuccess() -> {
                    try {
                        // KRX API는 JSON 형식으로 응답하므로 String으로 받아서 파싱
                        val bodyText = response.body<String>()
                        logger.debug { "POST response from $url: $bodyText" }

                        // JSON 파싱: JsonElement를 사용하여 파싱 후 Map으로 변환
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        }
                        val jsonElement = json.parseToJsonElement(bodyText)
                        val body = jsonElement.toMap()
                        body
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse response from $url" }
                        throw KfcException(ErrorCode.JSON_PARSE_ERROR, e)
                    }
                }
                else -> {
                    val statusCode = response.status.value
                    logger.error { "POST request failed: HTTP $statusCode from $url" }
                    throw KfcException(ErrorCode.HTTP_ERROR_RESPONSE)
                }
            }
        } catch (e: KfcException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Network error during POST to $url" }
            throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED, e)
        }
    }

    /**
     * GET 요청을 수행하고 응답을 Map으로 반환
     *
     * @param url 요청 URL
     * @param parameters 쿼리 파라미터 (optional)
     * @return 응답 데이터 (Map 형태)
     * @throws NetworkException 네트워크 에러 발생 시
     * @throws ParseException 응답 파싱 실패 시
     */
    suspend fun get(url: String, parameters: Map<String, String> = emptyMap()): Map<String, Any?> {
        logger.debug { "GET request to $url with parameters: $parameters" }

        return try {
            val response = httpClient.get(url) {
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }

            when {
                response.status.isSuccess() -> {
                    try {
                        val bodyText = response.body<String>()
                        logger.debug { "GET response from $url: success" }

                        // JSON 파싱: JsonElement를 사용하여 파싱 후 Map으로 변환
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        }
                        val jsonElement = json.parseToJsonElement(bodyText)
                        val body = jsonElement.toMap()
                        body
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse response from $url" }
                        throw KfcException(ErrorCode.JSON_PARSE_ERROR, e)
                    }
                }
                else -> {
                    val statusCode = response.status.value
                    logger.error { "GET request failed: HTTP $statusCode from $url" }
                    throw KfcException(ErrorCode.HTTP_ERROR_RESPONSE)
                }
            }
        } catch (e: KfcException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Network error during GET to $url" }
            throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED, e)
        }
    }

    /**
     * HttpClient를 종료합니다.
     *
     * 리소스 정리를 위해 사용이 끝난 후 반드시 호출해야 합니다.
     */
    fun close() {
        httpClient.close()
        logger.debug { "KrxHttpClient closed" }
    }
}
