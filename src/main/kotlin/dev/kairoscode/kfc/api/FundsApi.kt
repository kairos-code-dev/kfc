package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.FundType
import dev.kairoscode.kfc.domain.funds.*
import java.time.LocalDate

/**
 * 펀드/증권상품 도메인 통합 API 인터페이스
 *
 * ETF, REIT, ETN, ELW 등 모든 증권상품 관련 데이터를 조회하는 공개 API입니다.
 * KRX와 Naver 증권 API를 통합하여 도메인별 접근을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface FundsApi {

    // ================================
    // 1. 펀드 목록 및 기본 정보
    // ================================

    /**
     * 전체 펀드 목록 조회
     *
     * 상장된 모든 펀드의 기본 메타데이터를 조회합니다.
     *
     * @param type 펀드 유형 (null이면 전체, 값이 지정되면 해당 유형만 조회)
     * @return 펀드 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04601)
     */
    suspend fun getList(type: FundType? = null): List<FundListItem>

    /**
     * 펀드 상세정보 조회
     *
     * 단일 펀드의 모든 주요 정보를 조회합니다. 이 함수는 다음 데이터를 포함합니다:
     * - OHLCV (시가, 고가, 저가, 종가, 거래량)
     * - NAV 및 괴리율
     * - 52주 고가/저가 (다른 API에서 제공하지 않음)
     * - 총 보수 (다른 API에서 제공하지 않음)
     * - 시가총액, 상장주식수
     * - 펀드 기본 정보 (자산구분, 운용사, 벤치마크 등)
     *
     * 참고: GeneralInfo(MDCSTAT04704)는 상장 후 거의 변하지 않는 정적 메타데이터를 제공합니다.
     *
     * @param isin ISIN 코드 (예: "KR7152100004")
     * @param tradeDate 거래일 (기본값: 오늘)
     * @return 펀드 상세정보, 데이터가 없으면 null
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04701)
     */
    suspend fun getDetailedInfo(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): DetailedInfo?

    /**
     * 펀드 기본정보 조회
     *
     * 펀드의 상세 메타데이터 및 운용 정보를 조회합니다.
     * 상장 후 거의 변경되지 않는 정적 메타데이터를 제공합니다.
     *
     * @param isin ISIN 코드
     * @param tradeDate 기준일 (기본값: 오늘)
     * @return 펀드 기본정보, 데이터가 없으면 null
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04704)
     */
    suspend fun getGeneralInfo(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): GeneralInfo?

    // ================================
    // 2. 펀드 포트폴리오 구성
    // ================================

    /**
     * 펀드 포트폴리오 구성 종목 조회
     *
     * 펀드 바스켓 구성 종목 및 비중을 조회합니다.
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

    /**
     * 펀드 포트폴리오 상위 10종목 조회
     *
     * 펀드 포트폴리오 구성 종목 중 상위 10개의 요약 정보를 조회합니다.
     * 전체 포트폴리오(MDCSTAT05001)보다 빠른 응답을 제공합니다.
     *
     * @param isin ISIN 코드
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 포트폴리오 상위 10 종목 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04705)
     */
    suspend fun getPortfolioTop10(
        isin: String,
        date: LocalDate = LocalDate.now()
    ): List<PortfolioTopItem>

    // ================================
    // 3. 펀드 성과 및 추적
    // ================================

    /**
     * 펀드 추적 오차 조회
     *
     * 펀드와 벤치마크 지수 간의 추적 오차를 조회합니다.
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
     * 펀드 괴리율 조회
     *
     * 펀드 가격과 NAV 간의 괴리율을 조회합니다.
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
    // 4. 투자자별 거래
    // ================================

    /**
     * 전체 펀드 투자자별 거래 조회
     *
     * 모든 펀드의 투자자 유형별 거래 현황을 조회합니다.
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
     * 전체 펀드 투자자별 거래 (기간별) 조회
     *
     * 기간별 모든 펀드의 투자자 유형별 거래 추이를 조회합니다.
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
     * 개별 펀드 투자자별 거래 조회
     *
     * 특정 펀드의 투자자 유형별 거래 현황을 조회합니다.
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
     * 개별 펀드 투자자별 거래 (기간별) 조회
     *
     * 특정 펀드의 기간별 투자자 거래 추이를 조회합니다.
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
    // 5. 공매도 데이터
    // ================================

    /**
     * 펀드 공매도 거래 조회
     *
     * 개별 펀드의 공매도 거래 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param type 펀드 유형 (기본값: ETF)
     * @return 공매도 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT31401)
     */
    suspend fun getShortSelling(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType = FundType.ETF
    ): List<ShortSelling>

    /**
     * 펀드 공매도 잔고 조회
     *
     * 개별 펀드의 공매도 잔고 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param type 펀드 유형 (기본값: ETF)
     * @return 공매도 잔고 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT31501)
     */
    suspend fun getShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType = FundType.ETF
    ): List<ShortBalance>
}
