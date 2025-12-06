package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.index.IndexFundamental
import dev.kairoscode.kfc.domain.index.IndexFundamentalSnapshot
import dev.kairoscode.kfc.domain.index.IndexInfo
import dev.kairoscode.kfc.domain.index.IndexMarket
import dev.kairoscode.kfc.domain.index.IndexOhlcv
import dev.kairoscode.kfc.domain.index.IndexOhlcvSnapshot
import dev.kairoscode.kfc.domain.index.IndexPriceChange
import java.time.LocalDate

/**
 * KRX 지수 정보 API 인터페이스 (내부용)
 *
 * KRX API를 통해 지수 관련 데이터를 조회하는 내부 인터페이스입니다.
 * 이 인터페이스는 infrastructure 레이어 내부에서만 사용되며, 외부에 노출되지 않습니다.
 */
internal interface KrxIndexApi {
    /**
     * 지수 목록 조회
     *
     * 특정 시장의 모든 지수 목록을 조회합니다.
     *
     * @param market 시장 구분 (KOSPI, KOSDAQ, ALL)
     * @return 지수 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00201)
     */
    suspend fun getIndexList(market: IndexMarket): List<IndexInfo>

    /**
     * 지수 구성 종목 조회
     *
     * 특정 지수의 구성 종목 티커 리스트를 조회합니다.
     *
     * @param ticker 지수 코드
     * @param date 조회 날짜
     * @param market 시장 구분 (기본값: KOSPI)
     * @return 구성 종목 티커 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00601)
     */
    suspend fun getIndexConstituents(
        ticker: String,
        date: LocalDate,
        market: IndexMarket = IndexMarket.KOSPI,
    ): List<String>

    /**
     * 지수 OHLCV 조회 (특정 지수 기간별)
     *
     * 특정 지수의 기간별 OHLCV를 조회합니다.
     *
     * @param ticker 지수 코드
     * @param fromDate 시작일
     * @param toDate 종료일
     * @param market 시장 구분 (기본값: KOSPI)
     * @return 지수 OHLCV 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00301)
     */
    suspend fun getOhlcvByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket = IndexMarket.KOSPI,
    ): List<IndexOhlcv>

    /**
     * 지수 OHLCV 조회 (특정 일자 전체 지수)
     *
     * 특정 일자의 전체 지수 OHLCV를 조회합니다.
     *
     * @param date 조회 날짜
     * @param market 시장 구분
     * @return 지수 OHLCV 스냅샷 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00301)
     */
    suspend fun getOhlcvByTicker(
        date: LocalDate,
        market: IndexMarket,
    ): List<IndexOhlcvSnapshot>

    /**
     * 지수 밸류에이션 조회 (특정 지수 기간별)
     *
     * 특정 지수의 기간별 PER/PBR/배당수익률을 조회합니다.
     *
     * @param ticker 지수 코드
     * @param fromDate 시작일
     * @param toDate 종료일
     * @param market 시장 구분 (기본값: KOSPI)
     * @return 지수 밸류에이션 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00702)
     */
    suspend fun getFundamentalByDate(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket = IndexMarket.KOSPI,
    ): List<IndexFundamental>

    /**
     * 지수 밸류에이션 조회 (특정 일자 전체 지수)
     *
     * 특정 일자의 전체 지수 밸류에이션을 조회합니다.
     *
     * @param date 조회 날짜
     * @param market 시장 구분
     * @return 지수 밸류에이션 스냅샷 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00701)
     */
    suspend fun getFundamentalByTicker(
        date: LocalDate,
        market: IndexMarket,
    ): List<IndexFundamentalSnapshot>

    /**
     * 지수 등락률 조회
     *
     * 특정 기간 전체 지수의 등락률을 조회합니다.
     *
     * @param fromDate 시작일
     * @param toDate 종료일
     * @param market 시장 구분
     * @return 지수 등락률 리스트
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT00501)
     */
    suspend fun getPriceChange(
        fromDate: LocalDate,
        toDate: LocalDate,
        market: IndexMarket,
    ): List<IndexPriceChange>
}
