package dev.kairoscode.kfc.internal

import dev.kairoscode.kfc.api.EtfApi
import dev.kairoscode.kfc.api.krx.KrxEtfApi
import dev.kairoscode.kfc.api.naver.NaverEtfApi
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
        return krxApi.getEtfTrackingError(isin, fromDate, toDate)
    }

    override suspend fun getDivergenceRate(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<DivergenceRate> {
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
        return krxApi.getEtfInvestorTrading(isin, date)
    }

    override suspend fun getInvestorTradingByPeriod(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<InvestorTradingByDate> {
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
        return krxApi.getEtfShortSelling(isin, fromDate, toDate)
    }

    override suspend fun getShortBalance(
        isin: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<ShortBalance> {
        return krxApi.getEtfShortBalance(isin, fromDate, toDate)
    }
}
