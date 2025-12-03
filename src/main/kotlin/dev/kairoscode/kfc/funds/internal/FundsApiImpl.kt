package dev.kairoscode.kfc.funds.internal

import dev.kairoscode.kfc.funds.FundsApi
import dev.kairoscode.kfc.funds.internal.krx.KrxFundsApi
import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import dev.kairoscode.kfc.model.FundType
import dev.kairoscode.kfc.funds.internal.krx.model.*
import java.time.LocalDate

/**
 * 펀드/증권상품 도메인 API 구현체
 *
 * KRX 증권 API를 통해 펀드 관련 모든 데이터를 제공합니다.
 * 내부적으로 KrxFundsApi를 사용하며, RateLimiter를 적용합니다.
 */
internal class FundsApiImpl(
    private val krxFundsApi: KrxFundsApi
) : FundsApi {

    companion object {
        // ISIN 코드 형식: KR7 + 9자리 숫자 (총 12자리)
        private const val ISIN_LENGTH = 12
        private const val ISIN_PREFIX = "KR7"
    }

    // ================================
    // 1. 펀드 목록 및 기본 정보
    // ================================

    override suspend fun getList(type: FundType?): List<FundListItem> {
        return krxFundsApi.getEtfList(type)
    }

    override suspend fun getDetailedInfo(
        isin: String,
        tradeDate: LocalDate
    ): DetailedInfo? {
        validateIsin(isin)
        validateTradeDate(tradeDate)
        return krxFundsApi.getDetailedInfo(isin, tradeDate)
    }

    override suspend fun getGeneralInfo(
        isin: String,
        tradeDate: LocalDate
    ): GeneralInfo? {
        validateIsin(isin)
        validateTradeDate(tradeDate)
        return krxFundsApi.getGeneralInfo(isin, tradeDate)
    }

    // ================================
    // 2. 펀드 포트폴리오 구성
    // ================================

    override suspend fun getPortfolio(
        isin: String,
        date: LocalDate
    ): List<PortfolioConstituent> {
        validateIsin(isin)
        validateTradeDate(date)
        return krxFundsApi.getEtfPortfolio(isin, date)
    }

    override suspend fun getPortfolioTop10(
        isin: String,
        date: LocalDate
    ): List<PortfolioTopItem> {
        validateIsin(isin)
        validateTradeDate(date)
        return krxFundsApi.getEtfPortfolioTop10(isin, date)
    }

    // ================================
    // 3. 펀드 성과 및 추적
    // ================================

    override suspend fun getTrackingError(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<TrackingError> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxFundsApi.getEtfTrackingError(isin, fromDate, toDate)
    }

    override suspend fun getDivergenceRate(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<DivergenceRate> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxFundsApi.getEtfDivergenceRate(isin, fromDate, toDate)
    }

    // ================================
    // 4. 투자자별 거래
    // ================================

    override suspend fun getAllInvestorTrading(
        date: LocalDate
    ): List<InvestorTrading> {
        return krxFundsApi.getAllEtfInvestorTrading(date)
    }

    override suspend fun getAllInvestorTradingByPeriod(
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
        return krxFundsApi.getAllEtfInvestorTradingByPeriod(fromDate, toDate)
    }

    override suspend fun getInvestorTrading(
        isin: String,
        date: LocalDate
    ): List<InvestorTrading> {
        validateIsin(isin)
        validateTradeDate(date)
        return krxFundsApi.getEtfInvestorTrading(isin, date)
    }

    override suspend fun getInvestorTradingByPeriod(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxFundsApi.getEtfInvestorTradingByPeriod(isin, fromDate, toDate)
    }

    // ================================
    // 5. 공매도 데이터
    // ================================

    override suspend fun getShortSelling(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType
    ): List<ShortSelling> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxFundsApi.getEtfShortSelling(isin, fromDate, toDate, type)
    }

    override suspend fun getShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        type: FundType
    ): List<ShortBalance> {
        validateIsin(isin)
        validateDateRange(fromDate, toDate)
        return krxFundsApi.getEtfShortBalance(isin, fromDate, toDate, type)
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
