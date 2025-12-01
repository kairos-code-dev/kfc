package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.model.krx.*
import dev.kairoscode.kfc.model.naver.NaverEtfOhlcv
import java.time.LocalDate

/**
 * ETF 도메인 통합 API 인터페이스
 *
 * ETF 관련 모든 데이터를 조회하는 공개 API입니다.
 * KRX와 Naver 증권 API를 통합하여 도메인별 접근을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface EtfApi {

    // ================================
    // 1. ETF 목록 및 기본 정보
    // ================================

    /**
     * 전체 ETF 목록 조회
     *
     * 상장된 모든 ETF의 기본 메타데이터를 조회합니다.
     *
     * @return ETF 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04601)
     */
    suspend fun getList(): List<EtfListItem>

    /**
     * ETF 종합 정보 조회
     *
     * 단일 ETF의 모든 주요 정보를 조회합니다. 이 함수는 다음 데이터를 포함합니다:
     * - OHLCV (시가, 고가, 저가, 종가, 거래량)
     * - NAV 및 괴리율
     * - 52주 고가/저가 (다른 API에서 제공하지 않음)
     * - 총 보수 (다른 API에서 제공하지 않음)
     * - 시가총액, 상장주식수
     * - ETF 기본 정보 (자산구분, 운용사, 벤치마크 등)
     *
     * @param isin ISIN 코드 (예: "KR7152100004")
     * @param tradeDate 거래일 (기본값: 오늘)
     * @return ETF 종합 정보, 데이터가 없으면 null
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04701)
     */
    suspend fun getComprehensiveInfo(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): ComprehensiveEtfInfo?

    // ================================
    // 2. ETF 시세 및 OHLCV
    // ================================

    /**
     * 전체 ETF 일별 시세 조회
     *
     * 특정 날짜의 모든 ETF 시세를 한 번에 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @return ETF 일별 시세 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04301)
     */
    suspend fun getAllDailyPrices(
        date: LocalDate = LocalDate.now()
    ): List<EtfDailyPrice>

    /**
     * 개별 ETF OHLCV 조회
     *
     * 단일 ETF의 과거 OHLCV 데이터를 조회합니다.
     * KRX API는 최대 730일까지만 지원하며, 더 긴 범위는 자동으로 분할 처리됩니다.
     *
     * @param isin ISIN 코드 (예: "KR7152100004")
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return OHLCV 데이터 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04501)
     */
    suspend fun getOhlcv(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfOhlcv>

    /**
     * 조정주가 OHLCV 조회
     *
     * 네이버 증권 차트 API에서 조정주가 데이터를 조회합니다.
     * 조정주가는 배당금 지급, 액면분할/병합 등의 이벤트가 반영된 가격입니다.
     *
     * **주의사항**:
     * - 네이버 증권 서비스 정책 변경 시 동작하지 않을 수 있습니다
     * - Rate Limiting이 적용되어 있으나, 과도한 요청은 자제해주세요
     * - 프로덕션 환경에서는 안정성을 위해 자체 조정주가 계산 권장
     *
     * @param ticker 6자리 티커 코드 (예: "069500")
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 조정주가 OHLCV 데이터 목록 (날짜순 정렬)
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source Naver 증권 API
     */
    suspend fun getAdjustedOhlcv(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<NaverEtfOhlcv>

    /**
     * ETF 기간 등락률 조회
     *
     * 특정 기간의 모든 ETF 가격 변화를 조회합니다.
     *
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 등락률 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04401)
     */
    suspend fun getPriceChanges(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfPriceChange>

    // ================================
    // 3. ETF 포트폴리오 구성
    // ================================

    /**
     * ETF 포트폴리오 구성 종목 조회
     *
     * ETF 바스켓 구성 종목 및 비중을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 포트폴리오 구성 종목 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT05001)
     */
    suspend fun getPortfolio(
        isin: String,
        date: LocalDate = LocalDate.now()
    ): List<PortfolioConstituent>

    // ================================
    // 4. ETF 성과 및 추적
    // ================================

    /**
     * ETF 추적 오차 조회
     *
     * ETF와 벤치마크 지수 간의 추적 오차를 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 추적 오차 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT05901)
     */
    suspend fun getTrackingError(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<TrackingError>

    /**
     * ETF 괴리율 조회
     *
     * ETF 가격과 NAV 간의 괴리율을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 괴리율 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT06001)
     */
    suspend fun getDivergenceRate(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<DivergenceRate>

    // ================================
    // 5. 투자자별 거래
    // ================================

    /**
     * 전체 ETF 투자자별 거래 조회
     *
     * 모든 ETF의 투자자 유형별 거래 현황을 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04801)
     */
    suspend fun getAllInvestorTrading(
        date: LocalDate = LocalDate.now()
    ): List<InvestorTrading>

    /**
     * 전체 ETF 투자자별 거래 (기간별) 조회
     *
     * 기간별 모든 ETF의 투자자 유형별 거래 추이를 조회합니다.
     *
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 일별 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04802)
     */
    suspend fun getAllInvestorTradingByPeriod(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate>

    /**
     * 개별 ETF 투자자별 거래 조회
     *
     * 특정 ETF의 투자자 유형별 거래 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04901)
     */
    suspend fun getInvestorTrading(
        isin: String,
        date: LocalDate = LocalDate.now()
    ): List<InvestorTrading>

    /**
     * 개별 ETF 투자자별 거래 (기간별) 조회
     *
     * 특정 ETF의 기간별 투자자 거래 추이를 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 일별 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04902)
     */
    suspend fun getInvestorTradingByPeriod(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate>

    // ================================
    // 6. 공매도 데이터
    // ================================

    /**
     * ETF 공매도 거래 조회
     *
     * 개별 ETF의 공매도 거래 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 공매도 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT31401)
     */
    suspend fun getShortSelling(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortSelling>

    /**
     * ETF 공매도 잔고 조회
     *
     * 개별 ETF의 공매도 잔고 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 공매도 잔고 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT31501)
     */
    suspend fun getShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortBalance>
}
