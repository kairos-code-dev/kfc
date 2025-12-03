package dev.kairoscode.kfc.corp

import dev.kairoscode.kfc.corp.internal.opendart.model.CorpCode
import dev.kairoscode.kfc.corp.internal.opendart.model.DividendInfo
import dev.kairoscode.kfc.corp.internal.opendart.model.DisclosureItem
import dev.kairoscode.kfc.corp.internal.opendart.model.StockSplitInfo
import java.time.LocalDate

/**
 * 기업 공시 도메인 통합 API 인터페이스
 *
 * 기업 공시 관련 모든 데이터를 조회하는 공개 API입니다.
 * OPENDART API를 통해 도메인별 접근을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 *
 * **주의사항**:
 * - OPENDART API Key가 필요합니다 (https://opendart.fss.or.kr/)
 * - 일일 요청 제한: 20,000건
 * - Rate Limiting 구현 권장 (애플리케이션 레이어)
 */
interface CorpApi {

    /**
     * 고유번호 목록 조회
     *
     * 모든 법인/ETF의 OPENDART 고유번호 매핑 정보를 조회합니다.
     * 이 데이터는 종목코드(stock_code) → OPENDART 고유번호(corp_code) 변환에 필요합니다.
     *
     * 응답은 ZIP 압축된 XML 파일이며, 자동으로 압축 해제 및 파싱됩니다.
     *
     * @return 고유번호 목록 (모든 상장/비상장 법인 포함)
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getCorpCodeList(): List<CorpCode>

    /**
     * 배당 정보 조회
     *
     * 특정 법인의 배당금 정보를 조회합니다.
     * 조정주가 계산을 위한 배당 데이터로 활용됩니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (예: 2024) ※ 2015년 이후만 지원
     * @param reportCode 보고서 코드 (11011: 사업보고서, 11012: 반기, 11013: 1분기, 11014: 3분기)
     * @return 배당 정보 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getDividendInfo(
        corpCode: String,
        year: Int,
        reportCode: String = "11011"
    ): List<DividendInfo>

    /**
     * 증자/감자 정보 조회
     *
     * 특정 법인의 주식 분할/병합 정보를 조회합니다.
     * 조정주가 계산 시 주식 분할/병합 이벤트 반영에 사용됩니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리)
     * @param year 사업연도 (예: 2024)
     * @param reportCode 보고서 코드 (11011: 사업보고서, 11012: 반기, 11013: 1분기, 11014: 3분기)
     * @return 증자/감자 정보 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun getStockSplitInfo(
        corpCode: String,
        year: Int,
        reportCode: String = "11011"
    ): List<StockSplitInfo>

    /**
     * 공시 검색
     *
     * 특정 기간의 공시 목록을 조회합니다.
     *
     * @param corpCode OPENDART 고유번호 (8자리, 미입력 시 전체 법인)
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageNo 페이지 번호 (기본값: 1)
     * @param pageCount 페이지당 건수 (기본값: 100, 최대: 100)
     * @return 공시 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source OPENDART API
     */
    suspend fun searchDisclosures(
        corpCode: String? = null,
        startDate: LocalDate,
        endDate: LocalDate,
        pageNo: Int = 1,
        pageCount: Int = 100
    ): List<DisclosureItem>
}
