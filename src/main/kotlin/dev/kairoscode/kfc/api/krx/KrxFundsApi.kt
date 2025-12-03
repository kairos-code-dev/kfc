package dev.kairoscode.kfc.api.krx

import dev.kairoscode.kfc.model.FundType
import dev.kairoscode.kfc.model.krx.*
import java.time.LocalDate

/**
 * KRX 펀드/증권상품 API 인터페이스
 *
 * KRX(한국거래소) 펀드 및 증권상품 관련 데이터를 조회하는 공개 API입니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface KrxFundsApi {

    // ================================
    // 1. 펀드 목록 및 기본 정보
    // ================================

    /**
     * 전체 펀드 목록 조회 (MDCSTAT04601)
     *
     * 상장된 모든 펀드의 기본 메타데이터를 조회합니다.
     *
     * @param type 펀드 유형 (null이면 전체, 값이 지정되면 해당 유형만 조회)
     * @return 펀드 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfList(type: FundType? = null): List<EtfListItem>

    /**
     * 펀드 상세정보 조회 (MDCSTAT04701)
     *
     * 단일 펀드의 모든 주요 정보를 조회합니다. 이 함수는 다음 데이터를 포함합니다:
     * - OHLCV (시가, 고가, 저가, 종가, 거래량)
     * - NAV 및 괴리율
     * - 52주 고가/저가 (다른 API에서 제공하지 않음)
     * - 총 보수 (다른 API에서 제공하지 않음)
     * - 시가총액, 상장주식수
     * - 펀드 기본 정보 (자산구분, 운용사, 벤치마크 등)
     *
     * 참고: EtfGeneralInfo(MDCSTAT04704)는 상장 후 거의 변하지 않는 정적 메타데이터를 제공합니다.
     *
     * @param isin ISIN 코드 (예: "KR7152100004")
     * @param tradeDate 거래일 (기본값: 오늘)
     * @return 펀드 상세정보, 데이터가 없으면 null
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getDetailedInfo(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): EtfDetailedInfo?

    /**
     * 펀드 분단위 시세 조회 (MDCSTAT04702)
     *
     * 장중 1분 단위 OHLCV 데이터를 조회합니다.
     * 09:00부터 14:56까지 약 330개 이상의 데이터 포인트를 제공합니다.
     *
     * **중요**: 이 API는 **당일 거래일에만 데이터를 제공**합니다.
     * 과거 날짜를 조회하면 빈 목록이 반환됩니다.
     *
     * @param isin ISIN 코드
     * @param tradeDate 거래일 (기본값: 오늘)
     * @return 분단위 시세 목록 (과거 날짜의 경우 빈 목록)
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfIntradayBars(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): List<EtfIntradayBar>

    /**
     * 펀드 최근 일별 거래 조회 (MDCSTAT04703)
     *
     * 최근 10거래일의 일별 시세 요약을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param tradeDate 기준일 (기본값: 오늘)
     * @return 최근 10일 시세 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfRecentDaily(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): List<EtfRecentDaily>

    /**
     * 펀드 기본정보 조회 (MDCSTAT04704)
     *
     * 펀드의 상세 메타데이터 및 운용 정보를 조회합니다.
     * 상장 후 거의 변경되지 않는 정적 메타데이터를 제공합니다.
     *
     * @param isin ISIN 코드
     * @param tradeDate 기준일 (기본값: 오늘)
     * @return 펀드 기본정보, 데이터가 없으면 null
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfGeneralInfo(
        isin: String,
        tradeDate: LocalDate = LocalDate.now()
    ): EtfGeneralInfo?

    // ================================
    // 2. 펀드 시세 및 OHLCV
    // ================================

    /**
     * 전체 펀드 일별 시세 조회 (MDCSTAT04301)
     *
     * 특정 날짜의 모든 펀드 시세를 한 번에 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 펀드 일별 시세 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getAllEtfDailyPrices(
        date: LocalDate = LocalDate.now()
    ): List<EtfDailyPrice>

    /**
     * 개별 펀드 OHLCV 조회 (MDCSTAT04501)
     *
     * 단일 펀드의 과거 OHLCV 데이터를 조회합니다.
     * KRX API는 최대 730일까지만 지원하며, 더 긴 범위는 자동으로 분할 처리됩니다.
     *
     * @param isin ISIN 코드 (예: "KR7152100004")
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return OHLCV 데이터 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfOhlcv(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfOhlcv>

    /**
     * 펀드 기간 등락률 조회 (MDCSTAT04401)
     *
     * 특정 기간의 모든 펀드 가격 변화를 조회합니다.
     *
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 등락률 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfPriceChanges(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfPriceChange>

    // ================================
    // 3. 펀드 포트폴리오 구성
    // ================================

    /**
     * 펀드 포트폴리오 구성 종목 조회 (MDCSTAT05001)
     *
     * 펀드 바스켓 구성 종목 및 비중을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 포트폴리오 구성 종목 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfPortfolio(
        isin: String,
        date: LocalDate = LocalDate.now()
    ): List<PortfolioConstituent>

    /**
     * 펀드 포트폴리오 상위 10종목 조회
     *
     * 펀드 포트폴리오 구성 종목 중 비중이 높은 상위 10개 종목을 조회합니다.
     *
     * **구현 참고**: MDCSTAT04705 엔드포인트는 작동하지 않으므로,
     * 내부적으로 전체 포트폴리오(MDCSTAT05001)를 조회하여 상위 10개를 추출합니다.
     *
     * @param isin ISIN 코드
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 포트폴리오 상위 10 종목 목록 (비중 내림차순 정렬)
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfPortfolioTop10(
        isin: String,
        date: LocalDate = LocalDate.now()
    ): List<PortfolioTopItem>

    // ================================
    // 4. 펀드 성과 및 추적
    // ================================

    /**
     * 펀드 추적 오차 조회 (MDCSTAT05901)
     *
     * 펀드와 벤치마크 지수 간의 추적 오차를 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 추적 오차 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfTrackingError(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<TrackingError>

    /**
     * 펀드 괴리율 조회 (MDCSTAT06001)
     *
     * 펀드 가격과 NAV 간의 괴리율을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 괴리율 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfDivergenceRate(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<DivergenceRate>

    // ================================
    // 5. 투자자별 거래
    // ================================

    /**
     * 전체 펀드 투자자별 거래 조회 (MDCSTAT04801)
     *
     * 모든 펀드의 투자자 유형별 거래 현황을 조회합니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getAllEtfInvestorTrading(
        date: LocalDate = LocalDate.now()
    ): List<InvestorTrading>

    /**
     * 전체 펀드 투자자별 거래 (기간별) 조회 (MDCSTAT04802)
     *
     * 기간별 모든 펀드의 투자자 유형별 거래 추이를 조회합니다.
     *
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 일별 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getAllEtfInvestorTradingByPeriod(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate>

    /**
     * 개별 펀드 투자자별 거래 조회 (MDCSTAT04901)
     *
     * 특정 펀드의 투자자 유형별 거래 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfInvestorTrading(
        isin: String,
        date: LocalDate = LocalDate.now()
    ): List<InvestorTrading>

    /**
     * 개별 펀드 투자자별 거래 (기간별) 조회 (MDCSTAT04902)
     *
     * 특정 펀드의 기간별 투자자 거래 추이를 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 일별 투자자별 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfInvestorTradingByPeriod(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate>

    // ================================
    // 6. 공매도 데이터
    // ================================

    /**
     * 펀드 공매도 거래 조회 (MDCSTAT31401)
     *
     * 개별 펀드의 공매도 거래 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param type 펀드 유형 (기본값: ETF)
     * @return 공매도 거래 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfShortSelling(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType = FundType.ETF
    ): List<ShortSelling>

    /**
     * 펀드 공매도 잔고 조회 (MDCSTAT31501)
     *
     * 개별 펀드의 공매도 잔고 현황을 조회합니다.
     *
     * @param isin ISIN 코드
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @param type 펀드 유형 (기본값: ETF)
     * @return 공매도 잔고 목록
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getEtfShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType = FundType.ETF
    ): List<ShortBalance>
}
