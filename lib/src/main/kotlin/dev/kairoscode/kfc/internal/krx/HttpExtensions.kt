package dev.kairoscode.kfc.internal.krx

import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * HTTP 관련 유틸리티 확장 함수
 */

/**
 * API 응답에서 output 필드를 안전하게 추출
 *
 * KRX API 응답 구조:
 * - 일반 API (MDCSTAT04601 등):
 *   ```json
 *   {
 *     "result": {...},
 *     "output": [...]
 *   }
 *   ```
 *
 * - 단일 데이터 API (MDCSTAT04701 등):
 *   ```json
 *   {
 *     "ISU_CD": "...",
 *     "TDD_CLSPRC": "...",
 *     ...
 *   }
 *   ```
 *
 * @return output 리스트 또는 응답 자체 (없으면 빈 리스트)
 * @throws ParseException output 필드 타입이 잘못된 경우
 */
internal fun Map<String, Any?>.extractOutput(): List<Map<String, Any?>> {
    val output = this["output"]

    return when (output) {
        is List<*> -> {
            @Suppress("UNCHECKED_CAST")
            output.filterIsInstance<Map<String, Any?>>()
        }
        null -> {
            // output 필드가 없으면, 응답 자체가 데이터인지 확인
            // MDCSTAT04701 등의 API는 직접 데이터를 반환함
            if (containsKey("ISU_CD") || containsKey("ISU_ABBRV")) {
                logger.debug { "Response is direct data (no 'output' field), treating entire response as single record" }
                listOf(this)
            } else {
                logger.warn { "Response does not contain 'output' field and is not recognized as direct data" }
                emptyList()
            }
        }
        else -> {
            throw KfcException(ErrorCode.FIELD_TYPE_MISMATCH)
        }
    }
}

/**
 * API 응답에서 result 필드를 안전하게 추출
 *
 * @return result Map (없으면 빈 Map)
 */
internal fun Map<String, Any?>.extractResult(): Map<String, Any?> {
    val result = this["result"]

    return when (result) {
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            result as Map<String, Any?>
        }
        null -> {
            logger.warn { "Response does not contain 'result' field" }
            emptyMap()
        }
        else -> {
            throw KfcException(ErrorCode.FIELD_TYPE_MISMATCH)
        }
    }
}

/**
 * API 응답에서 에러를 확인하고 예외를 발생시킴
 *
 * KRX API 에러 응답 예시:
 * ```json
 * {
 *   "result": {
 *     "status": "error",
 *     "error_code": "E001",
 *     "error_message": "Invalid parameters"
 *   }
 * }
 * ```
 *
 * @throws ApiException API가 에러를 반환한 경우
 */
internal fun Map<String, Any?>.checkForErrors() {
    val result = extractResult()
    val status = result["status"]?.toString()

    if (status == "error" || status == "ERROR") {
        val errorCode = result["error_code"]?.toString()
        val errorMessage = result["error_message"]?.toString() ?: "Unknown API error"

        logger.error { "API returned error: [$errorCode] $errorMessage" }

        throw KfcException(ErrorCode.KRX_API_ERROR)
    }
}

/**
 * Map에서 필수 필드를 안전하게 추출
 *
 * @param key 필드 키
 * @return 필드 값 (String)
 * @throws ParseException 필드가 없거나 null인 경우
 */
internal fun Map<String, Any?>.requireField(key: String): String {
    val value = this[key]

    return when {
        value == null -> {
            throw KfcException(ErrorCode.REQUIRED_FIELD_MISSING)
        }
        else -> value.toString()
    }
}

/**
 * Map에서 선택적 필드를 안전하게 추출
 *
 * @param key 필드 키
 * @param defaultValue 기본값 (필드가 없을 경우)
 * @return 필드 값 (String) 또는 기본값
 */
internal fun Map<String, Any?>.optionalField(key: String, defaultValue: String = ""): String {
    return this[key]?.toString() ?: defaultValue
}
