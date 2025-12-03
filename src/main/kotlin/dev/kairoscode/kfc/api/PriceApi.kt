package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.price.IntradayBar
import dev.kairoscode.kfc.domain.price.RecentDaily
import java.time.LocalDate

/**
 * 가격 정보 도메인 통합 API 인터페이스
 *
 * ETF, REIT, ETN, ELW 등의 가격 및 거래 데이터를 조회하는 공개 API입니다.
 * KRX와 Naver 등의 데이터 소스를 통합하여 도메인별 접근을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface PriceApi {

    // ================================
    // 1. 분단위 시세 정보
    // ================================

    /**
     * 펀드 분단위 시세 조회
     *
     * 장중 1분 단위 OHLCV 데이터를 조회합니다.
     * 09:00부터 14:56까지 약 330개 이상의 데이터 포인트를 제공합니다.
     *
     * @param isin ISIN 코드
     * @param tradeDate 거래일 (기본값: 오늘)
     * @return 분단위 시세 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04702)
     */
    suspend fun getIntradayBars(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): List<IntradayBar>

    // ================================
    // 2. 일별 시세 정보
    // ================================

    /**
     * 펀드 최근 일별 거래 조회
     *
     * 최근 10거래일의 일별 시세 요약을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param tradeDate 기준일 (기본값: 오늘)
     * @return 최근 10일 시세 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04703)
     */
    suspend fun getRecentDaily(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): List<RecentDaily>
}
