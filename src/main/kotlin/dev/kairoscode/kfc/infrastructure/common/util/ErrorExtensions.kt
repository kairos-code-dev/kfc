package dev.kairoscode.kfc.infrastructure.common.util

import dev.kairoscode.kfc.domain.exception.ErrorCode
import dev.kairoscode.kfc.domain.exception.KfcException

/**
 * API 호출을 래핑하여 예외를 KfcException으로 변환합니다.
 *
 * 이미 KfcException인 경우는 그대로 re-throw하고,
 * 다른 예외는 지정된 ErrorCode와 context로 KfcException을 생성합니다.
 *
 * @param errorCode 변환할 에러 코드
 * @param context 디버깅을 위한 컨텍스트 정보
 * @param operation 작업 설명 (optional, 메시지로 사용)
 * @param block 실행할 코드 블록
 * @return 블록 실행 결과
 * @throws KfcException 블록 실행 중 예외 발생 시
 */
inline fun <T> wrapApiCall(
    errorCode: ErrorCode,
    context: Map<String, Any?> = emptyMap(),
    operation: String? = null,
    block: () -> T
): T {
    return try {
        block()
    } catch (e: KfcException) {
        // 이미 KfcException인 경우 그대로 re-throw
        throw e
    } catch (e: Exception) {
        // 다른 예외는 KfcException으로 래핑
        val message = operation ?: errorCode.message
        throw KfcException(
            errorCode = errorCode,
            message = message,
            cause = e,
            context = context
        )
    }
}

/**
 * suspend 함수용 API 호출 래퍼입니다.
 *
 * 코루틴 환경에서 동작하며, wrapApiCall과 동일한 예외 변환 로직을 제공합니다.
 *
 * @param errorCode 변환할 에러 코드
 * @param context 디버깅을 위한 컨텍스트 정보
 * @param operation 작업 설명 (optional, 메시지로 사용)
 * @param block 실행할 suspend 코드 블록
 * @return 블록 실행 결과
 * @throws KfcException 블록 실행 중 예외 발생 시
 */
suspend inline fun <T> wrapSuspendApiCall(
    errorCode: ErrorCode,
    context: Map<String, Any?> = emptyMap(),
    operation: String? = null,
    crossinline block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: KfcException) {
        // 이미 KfcException인 경우 그대로 re-throw
        throw e
    } catch (e: Exception) {
        // 다른 예외는 KfcException으로 래핑
        val message = operation ?: errorCode.message
        throw KfcException(
            errorCode = errorCode,
            message = message,
            cause = e,
            context = context
        )
    }
}
