package dev.kairoscode.kfc.infrastructure.opendart

import dev.kairoscode.kfc.api.CorpApi
import dev.kairoscode.kfc.domain.corp.CorpCode
import dev.kairoscode.kfc.domain.corp.DisclosureItem
import dev.kairoscode.kfc.domain.corp.DividendInfo
import dev.kairoscode.kfc.domain.corp.StockSplitInfo
import dev.kairoscode.kfc.domain.exception.ErrorCode
import dev.kairoscode.kfc.domain.exception.KfcException
import dev.kairoscode.kfc.infrastructure.opendart.OpenDartApi
import java.time.LocalDate

/**
 * 기업 공시 도메인 API 구현체
 *
 * OPENDART API를 통합하여 기업 공시 관련 모든 데이터를 제공합니다.
 * 내부적으로 OpenDartApi를 사용하며, OPENDART 전용 RateLimiter를 적용합니다.
 */
internal class CorpApiImpl(
    private val openDartApi: OpenDartApi,
) : CorpApi {
    companion object {
        // corpCode 형식: 8자리 숫자
        private const val CORP_CODE_LENGTH = 8
        private const val MIN_VALID_YEAR = 1900
    }

    override suspend fun getCorpCodeList(): List<CorpCode> = openDartApi.getCorpCodeList()

    override suspend fun getDividendInfo(
        corpCode: String,
        year: Int,
        reportCode: String,
    ): List<DividendInfo> {
        validateCorpCode(corpCode)
        validateYear(year)
        validateReportCode(reportCode)
        return openDartApi.getDividendInfo(corpCode, year, reportCode)
    }

    override suspend fun getStockSplitInfo(
        corpCode: String,
        year: Int,
        reportCode: String,
    ): List<StockSplitInfo> {
        validateCorpCode(corpCode)
        validateYear(year)
        validateReportCode(reportCode)
        return openDartApi.getStockSplitInfo(corpCode, year, reportCode)
    }

    override suspend fun searchDisclosures(
        corpCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        pageNo: Int,
        pageCount: Int,
    ): List<DisclosureItem> {
        corpCode?.let { validateCorpCode(it) }
        validateDateRange(startDate, endDate)
        validatePageParams(pageNo, pageCount)
        return openDartApi.searchDisclosures(corpCode, startDate, endDate, pageNo, pageCount)
    }

    // ================================
    // Validation Functions
    // ================================

    /**
     * corpCode 검증
     * - 공백이 아니어야 함
     * - 정확히 8자리 숫자
     */
    private fun validateCorpCode(corpCode: String) {
        val trimmed = corpCode.trim()

        when {
            trimmed.isBlank() ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "corpCode는 공백이 아니어야 합니다")
            trimmed.length != CORP_CODE_LENGTH ->
                throw KfcException(
                    ErrorCode.INVALID_PARAMETER,
                    "corpCode는 정확히 ${CORP_CODE_LENGTH}자여야 합니다 (입력: $trimmed)",
                )
            !trimmed.all { it.isDigit() } ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "corpCode는 숫자만 포함해야 합니다 (입력: $trimmed)")
        }
    }

    /**
     * year 검증
     * - 1900 이상
     * - 현재 년도 이하
     */
    private fun validateYear(year: Int) {
        val currentYear = LocalDate.now().year

        when {
            year < MIN_VALID_YEAR ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "년도는 $MIN_VALID_YEAR 이상이어야 합니다 (입력: $year)")
            year > currentYear ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "년도는 현재 년도($currentYear) 이하여야 합니다 (입력: $year)")
        }
    }

    /**
     * reportCode 검증
     * - 공백이 아니어야 함
     * - 유효한 리포트 코드 (11014: 반기보고서, 11012: 사업보고서 등)
     */
    private fun validateReportCode(reportCode: String) {
        val validReportCodes =
            setOf(
                "11012", // 사업보고서
                "11014", // 반기보고서
                "11011", // 분기보고서
                "11013", // 정정신고서
            )

        val trimmed = reportCode.trim()

        when {
            trimmed.isBlank() ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "reportCode는 공백이 아니어야 합니다")
            trimmed !in validReportCodes ->
                throw KfcException(
                    ErrorCode.INVALID_PARAMETER,
                    "유효하지 않은 reportCode입니다 (입력: $trimmed, 유효한 값: $validReportCodes)",
                )
        }
    }

    /**
     * 날짜 범위 검증
     * - startDate <= endDate
     * - 둘 다 현재 또는 과거 날짜
     */
    private fun validateDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ) {
        val now = LocalDate.now()

        when {
            startDate > endDate ->
                throw KfcException(
                    ErrorCode.INVALID_DATE_RANGE,
                    "시작 날짜는 종료 날짜보다 이전이어야 합니다 (startDate: $startDate, endDate: $endDate)",
                )
            endDate > now ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "종료 날짜는 미래 날짜일 수 없습니다 (입력: $endDate)")
        }
    }

    /**
     * 페이지 파라미터 검증
     * - pageNo >= 1
     * - pageCount >= 1, <= 100
     */
    private fun validatePageParams(
        pageNo: Int,
        pageCount: Int,
    ) {
        when {
            pageNo < 1 ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "pageNo는 1 이상이어야 합니다 (입력: $pageNo)")
            pageCount < 1 ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "pageCount는 1 이상이어야 합니다 (입력: $pageCount)")
            pageCount > 100 ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "pageCount는 100 이하여야 합니다 (입력: $pageCount)")
        }
    }
}
