package dev.kairoscode.kfc.internal

import dev.kairoscode.kfc.api.CorpApi
import dev.kairoscode.kfc.api.opendart.OpenDartApi
import dev.kairoscode.kfc.model.opendart.CorpCode
import dev.kairoscode.kfc.model.opendart.DividendInfo
import dev.kairoscode.kfc.model.opendart.DisclosureItem
import dev.kairoscode.kfc.model.opendart.StockSplitInfo
import java.time.LocalDate

/**
 * 기업 공시 도메인 API 구현체
 *
 * OPENDART API를 통합하여 기업 공시 관련 모든 데이터를 제공합니다.
 * 내부적으로 OpenDartApi를 사용하며, OPENDART 전용 RateLimiter를 적용합니다.
 */
internal class CorpApiImpl(
    private val openDartApi: OpenDartApi
) : CorpApi {

    override suspend fun getCorpCodeList(): List<CorpCode> {
        return openDartApi.getCorpCodeList()
    }

    override suspend fun getDividendInfo(
        corpCode: String,
        year: Int,
        reportCode: String
    ): List<DividendInfo> {
        return openDartApi.getDividendInfo(corpCode, year, reportCode)
    }

    override suspend fun getStockSplitInfo(
        corpCode: String,
        year: Int,
        reportCode: String
    ): List<StockSplitInfo> {
        return openDartApi.getStockSplitInfo(corpCode, year, reportCode)
    }

    override suspend fun searchDisclosures(
        corpCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        pageNo: Int,
        pageCount: Int
    ): List<DisclosureItem> {
        return openDartApi.searchDisclosures(corpCode, startDate, endDate, pageNo, pageCount)
    }
}
