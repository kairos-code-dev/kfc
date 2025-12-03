package dev.kairoscode.kfc.price.internal

import dev.kairoscode.kfc.price.PriceApi
import dev.kairoscode.kfc.funds.internal.krx.KrxFundsApi
import dev.kairoscode.kfc.funds.internal.krx.model.IntradayBar
import dev.kairoscode.kfc.funds.internal.krx.model.RecentDaily
import java.time.LocalDate

/**
 * 가격 정보 도메인 API 구현체
 *
 * KRX 증권 API를 통해 가격 및 거래 데이터를 제공합니다.
 */
internal class PriceApiImpl(
    private val krxFundsApi: KrxFundsApi
) : PriceApi {

    companion object {
        // ISIN 코드 형식: KR7 + 9자리 숫자 (총 12자리)
        private const val ISIN_LENGTH = 12
        private const val ISIN_PREFIX = "KR7"
    }

    override suspend fun getIntradayBars(
        isin: String,
        tradeDate: LocalDate
    ): List<IntradayBar> {
        validateIsin(isin)
        validateTradeDate(tradeDate)
        return krxFundsApi.getIntradayBars(isin, tradeDate)
    }

    override suspend fun getRecentDaily(
        isin: String,
        tradeDate: LocalDate
    ): List<RecentDaily> {
        validateIsin(isin)
        validateTradeDate(tradeDate)
        return krxFundsApi.getRecentDaily(isin, tradeDate)
    }

    private fun validateIsin(isin: String) {
        if (isin.length != ISIN_LENGTH || !isin.startsWith(ISIN_PREFIX)) {
            throw IllegalArgumentException("Invalid ISIN code format: $isin")
        }
    }

    private fun validateTradeDate(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("Trade date cannot be in the future: $date")
        }
    }
}
