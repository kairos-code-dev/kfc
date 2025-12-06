package dev.kairoscode.kfc.infrastructure.common.ratelimit

/**
 * Rate Limiting 관련 기본 예외 클래스
 */
sealed class RateLimitException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Rate limiting 대기 시간이 초과되었을 때 발생하는 예외
 *
 * @param source API 소스 이름 (예: "KRX", "Naver", "OPENDART")
 * @param config Rate limiting 설정
 * @param message 예외 메시지
 */
class RateLimitTimeoutException(
    val source: String,
    val config: RateLimitConfig,
    message: String = "Rate limit timeout exceeded for $source after ${config.waitTimeoutMillis}ms",
) : RateLimitException(message)

/**
 * Rate limiting 설정이 잘못되었을 때 발생하는 예외
 *
 * @param message 예외 메시지
 * @param cause 원인 예외
 */
class RateLimitConfigException(
    message: String,
    cause: Throwable? = null,
) : RateLimitException(message, cause)
