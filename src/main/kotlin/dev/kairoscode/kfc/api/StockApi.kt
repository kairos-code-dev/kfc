package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.stock.IndustryClassification
import dev.kairoscode.kfc.domain.stock.ListingStatus
import dev.kairoscode.kfc.domain.stock.Market
import dev.kairoscode.kfc.domain.stock.StockInfo
import dev.kairoscode.kfc.domain.stock.StockListItem
import dev.kairoscode.kfc.domain.stock.StockSectorInfo
import java.time.LocalDate

/**
 * 주식 종목 정보 도메인 통합 API 인터페이스
 *
 * 한국 상장 기업의 기본 정보 및 메타데이터를 조회하는 공개 API입니다.
 * KRX API를 통해 종목 리스트, 기본정보, 섹터/산업 분류 등을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface StockApi {
    /**
     * 종목 리스트 조회
     *
     * 특정 시장의 모든 종목 목록을 조회합니다.
     *
     * @param market 시장 구분 (기본값: ALL)
     * @param listingStatus 상장 상태 (기본값: LISTED)
     * @return 종목 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu / finder_listdelisu)
     */
    suspend fun getStockList(
        market: Market = Market.ALL,
        listingStatus: ListingStatus = ListingStatus.LISTED,
    ): List<StockListItem>

    /**
     * 종목 기본정보 조회
     *
     * 개별 종목의 상세 메타데이터를 조회합니다.
     * 종목이 존재하지 않으면 null을 반환합니다.
     *
     * @param ticker 종목 코드 (6자리)
     * @return 종목 기본정보, 없으면 null
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu)
     */
    suspend fun getStockInfo(ticker: String): StockInfo?

    /**
     * 종목명 조회
     *
     * 종목 코드로 종목명을 조회합니다.
     *
     * @param ticker 종목 코드 (6자리)
     * @return 종목명, 없으면 null
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu)
     */
    suspend fun getStockName(ticker: String): String?

    /**
     * 업종분류 현황 조회
     *
     * 모든 종목의 산업 분류 및 시가총액 정보를 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @param market 시장 구분 (기본값: ALL)
     * @return 업종분류 현황 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT03901)
     */
    suspend fun getSectorClassifications(
        date: LocalDate = LocalDate.now(),
        market: Market = Market.ALL,
    ): List<StockSectorInfo>

    /**
     * 산업별 그룹화 데이터 조회
     *
     * 업종분류 현황을 산업별로 그룹화하여 반환합니다.
     * 각 산업의 전체 시가총액 및 종목 수를 포함합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @param market 시장 구분 (기본값: ALL)
     * @return 산업별 그룹화 데이터 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT03901)
     */
    suspend fun getIndustryGroups(
        date: LocalDate = LocalDate.now(),
        market: Market = Market.ALL,
    ): List<IndustryClassification>

    /**
     * 종목 검색
     *
     * 종목명 또는 종목 코드로 종목을 검색합니다.
     * 부분 일치 검색을 지원합니다.
     *
     * **성능 고려사항**:
     * - 현재 구현: 전체 목록 조회 후 클라이언트 측 필터링
     * - 캐싱 적용 시 성능 개선 가능 (Phase 2)
     * - 대안: KRX API의 `searchText` 파라미터 활용 (서버 측 검색)
     *
     * @param keyword 검색 키워드 (종목명 또는 종목 코드)
     * @param market 시장 구분 (기본값: ALL)
     * @return 검색된 종목 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (finder_stkisu)
     */
    suspend fun searchStocks(
        keyword: String,
        market: Market = Market.ALL,
    ): List<StockListItem>
}
