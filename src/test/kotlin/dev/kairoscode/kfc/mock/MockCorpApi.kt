package dev.kairoscode.kfc.mock

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import dev.kairoscode.kfc.api.CorpApi
import dev.kairoscode.kfc.model.opendart.CorpCode
import dev.kairoscode.kfc.model.opendart.DividendInfo
import dev.kairoscode.kfc.model.opendart.DisclosureItem
import dev.kairoscode.kfc.model.opendart.StockSplitInfo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Unit Test용 Mock CorpApi 구현체
 *
 * JSON 파일 데이터를 파싱하여 반환하는 Mock 구현체입니다.
 * Unit Test에서 실제 API 호출 없이 테스트를 수행할 수 있습니다.
 *
 * @property corpCodeResponse 법인 코드 목록 JSON 데이터
 * @property dividendResponse 배당 정보 JSON 데이터
 * @property stockSplitResponse 주식 분할 정보 JSON 데이터
 * @property disclosureResponse 공시 목록 JSON 데이터
 */
class MockCorpApi(
    private val corpCodeResponse: String? = null,
    private val dividendResponse: String? = null,
    private val stockSplitResponse: String? = null,
    private val disclosureResponse: String? = null
) : CorpApi {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
            LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        })
        .registerTypeAdapter(BigDecimal::class.java, JsonDeserializer { json, _, _ ->
            BigDecimal(json.asString)
        })
        .create()

    override suspend fun getCorpCodeList(): List<CorpCode> {
        require(corpCodeResponse != null) { "corpCodeResponse가 설정되지 않았습니다" }
        return gson.fromJson(corpCodeResponse, Array<CorpCode>::class.java).toList()
    }

    override suspend fun getDividendInfo(corpCode: String, year: Int, reportCode: String): List<DividendInfo> {
        require(dividendResponse != null) { "dividendResponse가 설정되지 않았습니다" }
        return gson.fromJson(dividendResponse, Array<DividendInfo>::class.java).toList()
    }

    override suspend fun getStockSplitInfo(corpCode: String, year: Int, reportCode: String): List<StockSplitInfo> {
        require(stockSplitResponse != null) { "stockSplitResponse가 설정되지 않았습니다" }
        return gson.fromJson(stockSplitResponse, Array<StockSplitInfo>::class.java).toList()
    }

    override suspend fun searchDisclosures(
        corpCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        pageNo: Int,
        pageCount: Int
    ): List<DisclosureItem> {
        require(disclosureResponse != null) { "disclosureResponse가 설정되지 않았습니다" }
        return gson.fromJson(disclosureResponse, Array<DisclosureItem>::class.java).toList()
    }
}
