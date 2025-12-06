package dev.kairoscode.kfc.domain.exception

/**
 * KFC 라이브러리의 통합 예외 클래스
 *
 * 에러 코드 기반 예외 처리 시스템을 제공합니다.
 * 모든 예외는 [ErrorCode]를 포함하며, 선택적으로 컨텍스트 정보를 추가할 수 있습니다.
 *
 * @property errorCode 에러 코드
 * @property context 예외 발생 시 추가 컨텍스트 정보 (디버깅용)
 * @param message 사용자 정의 메시지 (기본값: errorCode.message)
 * @param cause 원인이 되는 예외
 *
 * ## 사용 예시
 *
 * ### 기본 예외 발생
 * ```kotlin
 * throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED)
 * throw KfcException(ErrorCode.JSON_PARSE_ERROR, cause = e)
 * ```
 *
 * ### Context를 포함한 예외 발생
 * ```kotlin
 * throw KfcException(
 *     ErrorCode.INVALID_PARAMETER,
 *     "ISIN 형식 오류",
 *     context = mapOf("input" to isin, "expectedLength" to 12)
 * )
 * ```
 *
 * ### Fluent API를 사용한 예외 발생
 * ```kotlin
 * throw KfcException(ErrorCode.INVALID_PARAMETER)
 *     .withContext("input", isin)
 *     .withContext("expectedLength", 12)
 * ```
 *
 * ### 예외 처리
 * ```kotlin
 * try {
 *     val etfList = client.krx.getEtfList()
 * } catch (e: KfcException) {
 *     when (e.errorCode) {
 *         ErrorCode.NETWORK_CONNECTION_FAILED -> println("네트워크 오류: ${e.message}")
 *         ErrorCode.JSON_PARSE_ERROR -> println("파싱 오류: ${e.message}")
 *         else -> println("오류: ${e.message}")
 *     }
 *     // Context 정보 확인
 *     e.context.forEach { (key, value) -> println("  $key: $value") }
 * }
 * ```
 */
class KfcException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
    val context: Map<String, Any?> = emptyMap(),
) : Exception(buildMessage(errorCode, message, context), cause) {
    /**
     * Secondary constructor for backward compatibility with Exception as second parameter
     */
    constructor(errorCode: ErrorCode, cause: Throwable) : this(errorCode, null, cause)

    /**
     * Fluent API: 컨텍스트 정보를 추가한 새로운 예외 인스턴스를 반환합니다.
     *
     * 기존 인스턴스는 변경되지 않으며, 새로운 인스턴스가 생성됩니다.
     *
     * @param key 컨텍스트 키
     * @param value 컨텍스트 값
     * @return 컨텍스트가 추가된 새로운 KfcException 인스턴스
     */
    fun withContext(
        key: String,
        value: Any?,
    ): KfcException =
        KfcException(
            errorCode = errorCode,
            message = message?.substringBefore(" [context:"), // 기존 메시지에서 context 부분 제거
            cause = cause,
            context = context + (key to value),
        )

    companion object {
        /**
         * 에러 코드, 메시지, 컨텍스트를 결합하여 최종 예외 메시지를 생성합니다.
         *
         * @param errorCode 에러 코드
         * @param message 사용자 정의 메시지 (null인 경우 errorCode.message 사용)
         * @param context 컨텍스트 정보
         * @return 최종 예외 메시지
         */
        private fun buildMessage(
            errorCode: ErrorCode,
            message: String?,
            context: Map<String, Any?>,
        ): String {
            val baseMessage = message ?: errorCode.message

            if (context.isEmpty()) {
                return baseMessage
            }

            val contextString =
                context.entries.joinToString(", ") { (key, value) ->
                    "$key=$value"
                }

            return "$baseMessage [context: $contextString]"
        }
    }
}
