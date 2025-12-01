package dev.kairoscode.kfc.internal

import dev.kairoscode.kfc.api.EtfApi
import dev.kairoscode.kfc.api.krx.KrxEtfApi
import dev.kairoscode.kfc.api.naver.NaverEtfApi
import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import dev.kairoscode.kfc.model.krx.*
import dev.kairoscode.kfc.model.naver.NaverEtfOhlcv
import java.time.LocalDate

/**
 * ETF 도메인 API 구현체
 *
 * KRX와 Naver 증권 API를 통합하여 ETF 관련 모든 데이터를 제공합니다.
 * 내부적으로 KrxEtfApi와 NaverEtfApi를 사용하며, 소스별 RateLimiter를 각각 적용합니다.
 */
internal class EtfApiImpl(
    private val krxApi: KrxEtfApi,
    private val naverApi: NaverEtfApi
) : EtfApi {

    companion object {
        // ISIN 코드 형식: KR7 + 9자리 숫자 (총 12자리)
        private const val ISIN_LENGTH = 12
        private const val ISIN_PREFIX = "KR7"
    }

    // ================================
    // 1. ETF 목록 및 기본 정보
    // ================================

    override suspend fun getList(): List<EtfListItem> {
        return krxApi.getEtfList()
    }

    override suspend fun getComprehensiveInfo(
        isin: String,
        tradeDate: LocalDate
    ): ComprehensiveEtfInfo? {
        validateIsin(isin)
        validateTradeDate(tradeDate)
        return krxApi.getComprehensiveEtfInfo(isin, tradeDate)
    }

    // ================================
    // 2. ETF 시세 및 OHLCV
    // ================================

    override suspend fun getAllDailyPrices(
        date: LocalDate
    ): List<EtfDailyPrice> {
        return krxApi.getAllEtfDailyPrices(date)
    }

    override suspend fun getOhlcv(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfOhlcv> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxApi.getEtfOhlcv(isin, fromDate, toDate)
    }

    override suspend fun getAdjustedOhlcv(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<NaverEtfOhlcv> {
        return naverApi.getAdjustedOhlcv(ticker, fromDate, toDate)
    }

    override suspend fun getPriceChanges(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<EtfPriceChange> {
        return krxApi.getEtfPriceChanges(fromDate, toDate)
    }

    // ================================
    // 3. ETF 포트폴리오 구성
    // ================================

    override suspend fun getPortfolio(
        isin: String,
        date: LocalDate
    ): List<PortfolioConstituent> {
        validateIsin(isin)
        validateTradeDate(date)
        return krxApi.getEtfPortfolio(isin, date)
    }

    // ================================
    // 4. ETF 성과 및 추적
    // ================================

    override suspend fun getTrackingError(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<TrackingError> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxApi.getEtfTrackingError(isin, fromDate, toDate)
    }

    override suspend fun getDivergenceRate(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<DivergenceRate> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxApi.getEtfDivergenceRate(isin, fromDate, toDate)
    }

    // ================================
    // 5. 투자자별 거래
    // ================================

    override suspend fun getAllInvestorTrading(
        date: LocalDate
    ): List<InvestorTrading> {
        return krxApi.getAllEtfInvestorTrading(date)
    }

    override suspend fun getAllInvestorTradingByPeriod(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
        return krxApi.getAllEtfInvestorTradingByPeriod(fromDate, toDate)
    }

    override suspend fun getInvestorTrading(
        isin: String,
        date: LocalDate
    ): List<InvestorTrading> {
        validateIsin(isin)
        validateTradeDate(date)
        return krxApi.getEtfInvestorTrading(isin, date)
    }

    override suspend fun getInvestorTradingByPeriod(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxApi.getEtfInvestorTradingByPeriod(isin, fromDate, toDate)
    }

    // ================================
    // 6. 공매도 데이터
    // ================================

    override suspend fun getShortSelling(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortSelling> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxApi.getEtfShortSelling(isin, fromDate, toDate)
    }

    override suspend fun getShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortBalance> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxApi.getEtfShortBalance(isin, fromDate, toDate)
    }

    // ================================
    // Validation Functions
    // ================================

    /**
     * ISIN 코드 검증
     * - 공백이 아니어야 함
     * - 정확히 12자 (KR7 + 9자리)
     * - KR7 프리픽스로 시작
     * - 나머지는 숫자로만 구성
     */
    private fun validateIsin(isin: String) {
        val trimmed = isin.trim()

        when {
            trimmed.isBlank() ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "ISIN 코드는 공백이 아니어야 합니다")
            trimmed.length != ISIN_LENGTH ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "ISIN 코드는 정확히 ${ISIN_LENGTH}자여야 합니다 (입력: $trimmed)")
            !trimmed.startsWith(ISIN_PREFIX) ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "ISIN 코드는 ${ISIN_PREFIX}로 시작해야 합니다 (입력: $trimmed)")
            !trimmed.drop(ISIN_PREFIX.length).all { it.isDigit() } ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "ISIN 코드는 ${ISIN_PREFIX} 이후 숫자만 포함해야 합니다 (입력: $trimmed)")
        }
    }

    /**
     * 거래 날짜 검증
     * - 현재 또는 과거 날짜만 허용
     */
    private fun validateTradeDate(date: LocalDate) {
        when {
            date > LocalDate.now() ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "거래 날짜는 미래 날짜일 수 없습니다 (입력: $date)")
        }
    }

    /**
     * 날짜 범위 검증
     * - fromDate <= toDate
     * - 둘 다 현재 또는 과거 날짜
     */
    private fun validateDateRange(fromDate: LocalDate, toDate: LocalDate) {
        when {
            fromDate > toDate ->
                throw KfcException(ErrorCode.INVALID_DATE_RANGE, "시작 날짜는 종료 날짜보다 이전이어야 합니다 (fromDate: $fromDate, toDate: $toDate)")
            toDate > LocalDate.now() ->
                throw KfcException(ErrorCode.INVALID_PARAMETER, "종료 날짜는 미래 날짜일 수 없습니다 (입력: $toDate)")
        }
    }
}
