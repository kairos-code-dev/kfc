package dev.kairoscode.kfc.domain.exception

/**
 * KFC 라이브러리 에러 코드
 *
 * 모든 예외는 에러 코드와 메시지로 구성됩니다.
 * 에러 코드는 카테고리별로 번호대가 구분되어 있습니다:
 * - 1000번대: 네트워크 오류
 * - 2000번대: 파싱 오류
 * - 3000번대: API 오류
 * - 4000번대: Rate Limit 오류
 * - 5000번대: 검증 오류
 */
enum class ErrorCode(
    val code: Int,
    val message: String,
) {
    // ================================
    // 1000번대: 네트워크 오류
    // ================================

    /**
     * 네트워크 연결 실패
     */
    NETWORK_CONNECTION_FAILED(1001, "네트워크 연결에 실패했습니다"),

    /**
     * 네트워크 타임아웃
     */
    NETWORK_TIMEOUT(1002, "네트워크 요청 시간이 초과되었습니다"),

    /**
     * HTTP 요청 실패
     */
    HTTP_REQUEST_FAILED(1003, "HTTP 요청이 실패했습니다"),

    /**
     * HTTP 응답 오류 (4xx, 5xx)
     */
    HTTP_ERROR_RESPONSE(1004, "HTTP 요청이 오류 응답을 반환했습니다"),

    // ================================
    // 2000번대: 파싱 오류
    // ================================

    /**
     * JSON 파싱 실패
     */
    JSON_PARSE_ERROR(2001, "JSON 파싱에 실패했습니다"),

    /**
     * XML 파싱 실패
     */
    XML_PARSE_ERROR(2002, "XML 파싱에 실패했습니다"),

    /**
     * 잘못된 데이터 형식
     */
    INVALID_DATA_FORMAT(2003, "데이터 형식이 올바르지 않습니다"),

    /**
     * 필드 타입 불일치
     */
    FIELD_TYPE_MISMATCH(2004, "필드의 타입이 예상과 다릅니다"),

    /**
     * 필수 필드 누락
     */
    REQUIRED_FIELD_MISSING(2005, "필수 필드가 누락되었습니다"),

    /**
     * 숫자 변환 실패
     */
    NUMBER_FORMAT_ERROR(2006, "숫자 형식이 올바르지 않습니다"),

    /**
     * 날짜 형식 오류
     */
    DATE_FORMAT_ERROR(2007, "날짜 형식이 올바르지 않습니다"),

    /**
     * ZIP 파일 파싱 오류
     */
    ZIP_PARSE_ERROR(2008, "ZIP 파일 파싱에 실패했습니다"),

    // ================================
    // 3000번대: API 오류
    // ================================

    /**
     * KRX API 오류
     */
    KRX_API_ERROR(3001, "KRX API에서 오류가 발생했습니다"),

    /**
     * OPENDART API 오류
     */
    OPENDART_API_ERROR(3002, "OPENDART API에서 오류가 발생했습니다"),

    /**
     * Naver API 오류
     */
    NAVER_API_ERROR(3003, "Naver API에서 오류가 발생했습니다"),

    // ================================
    // 4000번대: Rate Limit 오류
    // ================================

    /**
     * API 호출 제한 초과
     */
    RATE_LIMIT_EXCEEDED(4001, "API 호출 제한을 초과했습니다"),

    // ================================
    // 5000번대: 검증 오류
    // ================================

    /**
     * 잘못된 날짜 범위
     */
    INVALID_DATE_RANGE(5001, "날짜 범위가 올바르지 않습니다"),

    /**
     * 잘못된 파라미터
     */
    INVALID_PARAMETER(5002, "파라미터가 올바르지 않습니다"),

    /**
     * 알 수 없는 오류
     */
    UNKNOWN_ERROR(9999, "알 수 없는 오류가 발생했습니다"),
    ;

    override fun toString(): String = "[$code] $message"
}
