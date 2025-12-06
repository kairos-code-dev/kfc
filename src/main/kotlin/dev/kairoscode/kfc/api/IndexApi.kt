package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.index.IndexFundamental
import dev.kairoscode.kfc.domain.index.IndexFundamentalSnapshot
import dev.kairoscode.kfc.domain.index.IndexInfo
import dev.kairoscode.kfc.domain.index.IndexMarket
import dev.kairoscode.kfc.domain.index.IndexOhlcv
import dev.kairoscode.kfc.domain.index.IndexOhlcvSnapshot
import dev.kairoscode.kfc.domain.index.IndexPriceChange
import java.time.LocalDate

/**
 * 지수 정보 도메인 통합 API 인터페이스
 *
 * 한국 증시의 주요 지수 데이터를 조회하는 공개 API입니다.
 * KRX API를 통해 지수 목록, OHLCV, 밸류에이션 지표, 구성 종목 등을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface IndexApi {
    /**
     * 지수 목록 조회
     *
     * 특정 시장의 모든 지수 목록을 조회합니다.
     *
     * @param market 시장 구분 (기본값: ALL)
     * @return 지수 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00201)
     */
    suspend fun getIndexList(market: IndexMarket = IndexMarket.ALL): List<IndexInfo>

    /**
     * 지수명 조회
     *
     * 지수 코드로 지수명을 조회합니다.
     *
     * @param ticker 지수 코드 (예: "1001", "1028")
     * @return 지수명, 없으면 null
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00201)
     */
    suspend fun getIndexName(ticker: String): String?

    /**
     * 지수 기본정보 조회
     *
     * 개별 지수의 상세 메타데이터를 조회합니다.
     * 지수가 존재하지 않으면 null을 반환합니다.
     *
     * @param ticker 지수 코드 (예: "1001", "1028")
     * @return 지수 기본정보, 없으면 null
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00201)
     */
    suspend fun getIndexInfo(ticker: String): IndexInfo?

    /**
     * 지수 구성 종목 조회
     *
     * 특정 지수의 구성 종목 티커 리스트를 조회합니다.
     *
     * @param ticker 지수 코드 (예: "1028" = 코스피 200)
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 구성 종목 티커 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00401)
     */
    suspend fun getIndexConstituents(
        ticker: String,
        date: LocalDate = LocalDate.now(),
    ): List<String>

    /**
     * 지수 OHLCV 조회 (특정 지수 기간별)
     *
     * 특정 지수의 기간별 OHLCV를 조회합니다.
     *
     * @param ticker 지수 코드 (예: "1001" = 코스피)
     * @param fromDate 시작일
     * @param toDate 종료일
     * @return 지수 OHLCV 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00101)
     */
    suspend fun getOhlcvByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): List<IndexOhlcv>

    /**
     * 지수 OHLCV 조회 (특정 일자 전체 지수)
     *
     * 특정 일자의 전체 지수 OHLCV를 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @param market 시장 구분 (기본값: ALL)
     * @return 지수 OHLCV 스냅샷 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00301)
     */
    suspend fun getOhlcvByTicker(
        date: LocalDate = LocalDate.now(),
        market: IndexMarket = IndexMarket.ALL,
    ): List<IndexOhlcvSnapshot>

    /**
     * 지수 밸류에이션 조회 (특정 지수 기간별)
     *
     * 특정 지수의 기간별 PER/PBR/배당수익률을 조회합니다.
     *
     * @param ticker 지수 코드
     * @param fromDate 시작일
     * @param toDate 종료일
     * @return 지수 밸류에이션 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00601)
     */
    suspend fun getFundamentalByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): List<IndexFundamental>

    /**
     * 지수 밸류에이션 조회 (특정 일자 전체 지수)
     *
     * 특정 일자의 전체 지수 밸류에이션을 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @param market 시장 구분 (기본값: ALL)
     * @return 지수 밸류에이션 스냅샷 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00701)
     */
    suspend fun getFundamentalByTicker(
        date: LocalDate = LocalDate.now(),
        market: IndexMarket = IndexMarket.ALL,
    ): List<IndexFundamentalSnapshot>

    /**
     * 지수 등락률 조회
     *
     * 특정 기간 전체 지수의 등락률을 조회합니다.
     *
     * @param fromDate 시작일
     * @param toDate 종료일
     * @param market 시장 구분 (기본값: ALL)
     * @return 지수 등락률 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00501)
     */
    suspend fun getPriceChange(
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket = IndexMarket.ALL,
    ): List<IndexPriceChange>
}
