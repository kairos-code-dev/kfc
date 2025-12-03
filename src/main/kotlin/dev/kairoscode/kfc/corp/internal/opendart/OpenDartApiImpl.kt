package dev.kairoscode.kfc.corp.internal.opendart

import dev.kairoscode.kfc.corp.internal.opendart.OpenDartApi
import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import dev.kairoscode.kfc.internal.installResponseRecording
import dev.kairoscode.kfc.internal.ratelimit.RateLimiter
import dev.kairoscode.kfc.internal.ratelimit.RateLimitingSettings
import dev.kairoscode.kfc.internal.ratelimit.TokenBucketRateLimiter
import dev.kairoscode.kfc.corp.internal.opendart.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * OPENDART API 구현체
 *
 * @property apiKey OPENDART API 인증키
 * @property rateLimiter Rate Limiting 관리자
 */
internal class OpenDartApiImpl(
    private val apiKey: String,
    private val rateLimiter: RateLimiter = TokenBucketRateLimiter(RateLimitingSettings.openDartDefault())
) : OpenDartApi {

    private val httpClient = HttpClient(CIO) {
        // 응답 레코딩 플러그인 설치 (ResponseRecordingContext가 있을 때만 동작)
        installResponseRecording()

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000 // 60초 (ZIP 다운로드 고려)
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }

        expectSuccess = false
    }

    private val logger = KotlinLogging.logger {}

    companion object {
        private const val BASE_URL = "https://opendart.fss.or.kr/api"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    override suspend fun getCorpCodeList(): List<CorpCode> {
        rateLimiter.acquire()
        logger.debug { "Fetching corp code list from OPENDART" }

        val url = "$BASE_URL/corpCode.xml"

        return try {
            val response = httpClient.get(url) {
                parameter("crtfc_key", apiKey)
            }

            when {
                response.status.isSuccess() -> {
                    // ZIP 파일을 ByteArray로 받아서 처리
                    val zipBytes = response.body<ByteArray>()
                    val zipInputStream = ZipInputStream(zipBytes.inputStream())
                    val corpCodes = parseCorpCodeZip(zipInputStream)
                    logger.debug { "Fetched ${corpCodes.size} corp codes" }
                    corpCodes
                }
                else -> {
                    throw KfcException(ErrorCode.HTTP_ERROR_RESPONSE)
                }
            }
        } catch (e: KfcException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch corp code list" }
            throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED, e)
        }
    }

    override suspend fun getDividendInfo(
        corpCode: String,
        year: Int,
        reportCode: String
    ): List<DividendInfo> {
        rateLimiter.acquire()
        logger.debug { "Fetching dividend info: corpCode=$corpCode, year=$year, reportCode=$reportCode" }

        val url = "$BASE_URL/alotMatter.json"

        val response = fetchJson<OpenDartResponse<DividendInfoRaw>>(url, mapOf(
            "corp_code" to corpCode,
            "bsns_year" to year.toString(),
            "reprt_code" to reportCode
        ))

        return response.list?.map { it.toDividendInfo() } ?: emptyList()
    }

    override suspend fun getStockSplitInfo(
        corpCode: String,
        year: Int,
        reportCode: String
    ): List<StockSplitInfo> {
        rateLimiter.acquire()
        logger.debug { "Fetching stock split info: corpCode=$corpCode, year=$year, reportCode=$reportCode" }

        val url = "$BASE_URL/irdsSttus.json"

        val response = fetchJson<OpenDartResponse<StockSplitInfoRaw>>(url, mapOf(
            "corp_code" to corpCode,
            "bsns_year" to year.toString(),
            "reprt_code" to reportCode
        ))

        return response.list?.mapNotNull { it.toStockSplitInfo() } ?: emptyList()
    }

    override suspend fun searchDisclosures(
        corpCode: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        pageNo: Int,
        pageCount: Int
    ): List<DisclosureItem> {
        rateLimiter.acquire()
        logger.debug { "Searching disclosures: corpCode=$corpCode, from=$startDate, to=$endDate" }

        val url = "$BASE_URL/list.json"

        val params = mutableMapOf(
            "bgn_de" to startDate.format(DATE_FORMATTER),
            "end_de" to endDate.format(DATE_FORMATTER),
            "page_no" to pageNo.toString(),
            "page_count" to pageCount.toString()
        )

        corpCode?.let { params["corp_code"] = it }

        val response = fetchJson<DisclosureListResponse>(url, params)

        return response.list?.map { it.toDisclosureItem() } ?: emptyList()
    }

    /**
     * JSON API 호출 공통 로직
     */
    private suspend inline fun <reified T> fetchJson(
        url: String,
        params: Map<String, String>
    ): T {
        return try {
            val response = httpClient.get(url) {
                parameter("crtfc_key", apiKey)
                params.forEach { (key, value) ->
                    parameter(key, value)
                }
            }

            when {
                response.status.isSuccess() -> {
                    try {
                        val body = response.body<T>()

                        // OpenDartResponse의 경우 status 체크
                        if (body is OpenDartResponse<*>) {
                            when (body.status) {
                                "000" -> body // 정상
                                "013" -> body // 데이터 없음 (빈 리스트 반환)
                                else -> throw KfcException(ErrorCode.OPENDART_API_ERROR)
                            }
                        } else if (body is DisclosureListResponse) {
                            when (body.status) {
                                "000" -> body
                                "013" -> body
                                else -> throw KfcException(ErrorCode.OPENDART_API_ERROR)
                            }
                        } else {
                            body
                        }
                    } catch (e: KfcException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to parse JSON response from $url" }
                        throw KfcException(ErrorCode.JSON_PARSE_ERROR, e)
                    }
                }
                else -> {
                    throw KfcException(ErrorCode.HTTP_ERROR_RESPONSE)
                }
            }
        } catch (e: KfcException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Network error during request to $url" }
            throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED, e)
        }
    }

    /**
     * ZIP 압축된 XML 파싱 (corpCode API)
     */
    private fun parseCorpCodeZip(zipInputStream: ZipInputStream): List<CorpCode> {
        return zipInputStream.use { zis ->
            try {
                // ZIP 엔트리 진입
                zis.nextEntry ?: throw KfcException(ErrorCode.ZIP_PARSE_ERROR)

                // XML 파싱 - DocumentBuilder.parse()는 스트림을 닫지 않도록 설정
                // InputSource를 사용하여 스트림이 자동으로 닫히지 않도록 방지
                val xmlContent = zis.readBytes().toString(Charsets.UTF_8)
                val doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(InputSource(StringReader(xmlContent)))

                val listNodes = doc.getElementsByTagName("list")
                val result = mutableListOf<CorpCode>()

                for (i in 0 until listNodes.length) {
                    val node = listNodes.item(i) as Element

                    val corpCode = node.getElementsByTagName("corp_code").item(0).textContent
                    val corpName = node.getElementsByTagName("corp_name").item(0).textContent
                    val stockCode = node.getElementsByTagName("stock_code").item(0)?.textContent?.let {
                        if (it.isBlank() || it == " ") null else it.trim()
                    }
                    val modifyDate = node.getElementsByTagName("modify_date").item(0).textContent

                    result.add(CorpCode(
                        corpCode = corpCode,
                        corpName = corpName,
                        stockCode = stockCode,
                        modifyDate = LocalDate.parse(modifyDate, DATE_FORMATTER)
                    ))
                }

                result
            } catch (e: KfcException) {
                throw e
            } catch (e: Exception) {
                throw KfcException(ErrorCode.ZIP_PARSE_ERROR, e)
            }
        }
    }

    /**
     * 리소스 정리
     */
    fun close() {
        httpClient.close()
        logger.debug { "OpenDartApiImpl closed" }
    }
}

/**
 * 원시 데이터 → DividendInfo 변환
 */
private fun DividendInfoRaw.toDividendInfo(): DividendInfo {
    return DividendInfo(
        rceptNo = rceptNo,
        corpCode = corpCode,
        corpName = corpName,
        dividendType = dividendType,
        stockKind = stockKind ?: "",
        currentYear = currentYear?.replace(",", "")?.replace("-", "")?.toBigDecimalOrNull(),
        previousYear = previousYear?.replace(",", "")?.replace("-", "")?.toBigDecimalOrNull(),
        twoYearsAgo = twoYearsAgo?.replace(",", "")?.replace("-", "")?.toBigDecimalOrNull(),
        settlementDate = LocalDate.parse(settlementDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
}

/**
 * 원시 데이터 → StockSplitInfo 변환
 *
 * 주의: OPENDART API는 증자/감자가 없을 때 모든 필드를 "-"로 반환합니다.
 * 이런 경우 null을 반환하여 필터링되도록 합니다.
 */
private fun StockSplitInfoRaw.toStockSplitInfo(): StockSplitInfo? {
    // eventDate가 null이거나 "-"인 경우 실제 데이터가 없음
    if (eventDate.isNullOrBlank() || eventDate == "-") {
        return null
    }

    // 다른 필수 필드도 체크
    if (eventType.isNullOrBlank() || eventType == "-") {
        return null
    }

    return StockSplitInfo(
        rceptNo = rceptNo,
        corpCode = corpCode,
        corpName = corpName,
        eventDate = LocalDate.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        eventType = eventType,
        stockKind = stockKind?.takeIf { it != "-" } ?: "",
        quantity = quantity?.takeIf { it != "-" }?.replace(",", "")?.toLongOrNull() ?: 0L,
        parValuePerShare = parValuePerShare?.takeIf { it != "-" }?.replace(",", "")?.toIntOrNull() ?: 0,
        totalAmount = totalAmount?.takeIf { it != "-" }?.replace(",", "")?.toLongOrNull() ?: 0L
    )
}

/**
 * 원시 데이터 → DisclosureItem 변환
 */
private fun DisclosureItemRaw.toDisclosureItem(): DisclosureItem {
    return DisclosureItem(
        corpCode = corpCode,
        corpName = corpName,
        stockCode = stockCode?.let { if (it.isBlank()) null else it.trim() },
        corpCls = corpCls,
        reportName = reportName,
        rceptNo = rceptNo,
        filerName = filerName,
        rceptDate = LocalDate.parse(rceptDate, DateTimeFormatter.ofPattern("yyyyMMdd")),
        remark = remark
    )
}
