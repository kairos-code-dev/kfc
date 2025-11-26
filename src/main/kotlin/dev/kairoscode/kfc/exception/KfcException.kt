package dev.kairoscode.kfc.exception

/**
 * KFC 라이브러리의 통합 예외 클래스
 *
 * 에러 코드 기반 예외 처리 시스템을 제공합니다.
 * 모든 예외는 [ErrorCode]를 포함합니다.
 *
 * @property errorCode 에러 코드
 * @param cause 원인이 되는 예외
 *
 * ## 사용 예시
 *
 * ### 예외 발생
 * ```kotlin
 * throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED)
 * throw KfcException(ErrorCode.JSON_PARSE_ERROR, e)
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
 * }
 * ```
 */
class KfcException(
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : Exception(errorCode.message, cause)
