package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.financials.*

/**
 * 재무제표 도메인 통합 API 인터페이스
 *
 * 기업의 재무제표 데이터를 조회하는 공개 API입니다.
 * OPENDART API를 통해 도메인별 접근을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 *
 * **주의사항**:
 * - OPENDART API Key가 필요합니다 (https://opendart.fss.or.kr/)
 * - 2015년 이후 데이터만 지원합니다
 * - 일일 요청 제한: 20,000건
 */
interface FinancialsApi {

    /**
     * 손익계산서 조회
     *
     * 특정 법인의 손익계산서를 조회합니다.
     * 일정 기간 동안의 수익, 비용, 순이익 정보를 포함합니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: 사업보고서)
     * @param statementType 재무제표 구분 (기본값: 연결재무제표)
     * @return 손익계산서 데이터
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getIncomeStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): IncomeStatement

    /**
     * 재무상태표 조회
     *
     * 특정 법인의 재무상태표를 조회합니다.
     * 특정 시점의 자산, 부채, 자본 정보를 포함합니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: 사업보고서)
     * @param statementType 재무제표 구분 (기본값: 연결재무제표)
     * @return 재무상태표 데이터
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getBalanceSheet(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): BalanceSheet

    /**
     * 현금흐름표 조회
     *
     * 특정 법인의 현금흐름표를 조회합니다.
     * 일정 기간의 현금 유입과 유출 정보를 포함합니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: 사업보고서)
     * @param statementType 재무제표 구분 (기본값: 연결재무제표)
     * @return 현금흐름표 데이터
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getCashFlowStatement(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): CashFlowStatement

    /**
     * 전체 재무제표 조회
     *
     * 손익계산서, 재무상태표, 현금흐름표를 한 번에 조회합니다.
     * 단일 API 호출로 모든 재무제표를 가져오므로 효율적입니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (2015년 이후)
     * @param reportType 보고서 유형 (기본값: 사업보고서)
     * @param statementType 재무제표 구분 (기본값: 연결재무제표)
     * @return 전체 재무제표 데이터
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getAllFinancials(
        corpCode: String,
        year: Int,
        reportType: ReportType = ReportType.ANNUAL,
        statementType: StatementType = StatementType.CONSOLIDATED
    ): FinancialStatements
}
