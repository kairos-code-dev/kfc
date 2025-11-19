package dev.kairoscode.kfc.internal.naver

import dev.kairoscode.kfc.api.naver.NaverEtfApi
import dev.kairoscode.kfc.exception.ErrorCode
import dev.kairoscode.kfc.exception.KfcException
import dev.kairoscode.kfc.model.naver.NaverEtfOhlcv
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Naver 증권 ETF API 구현체
 *
 * 네이버 증권 차트 API를 통해 조정주가 데이터를 조회합니다.
 */
internal class NaverEtfApiImpl : NaverEtfApi {

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        expectSuccess = false
    }

    private val logger = KotlinLogging.logger {}

    companion object {
        private const val BASE_URL = "https://fchart.stock.naver.com/sise.nhn"
        private const val USER_AGENT = "kotlin-krx/1.0.0 (https://github.com/kairoscode/kotlin-krx)"
        private const val REFERER = "https://finance.naver.com/"

        // 날짜 포맷
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    override suspend fun getAdjustedOhlcv(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<NaverEtfOhlcv> {
        // 날짜 역순 검증 - 역순이면 빈 리스트 반환
        if (fromDate > toDate) {
            logger.warn { "fromDate ($fromDate) is after toDate ($toDate), returning empty list" }
            return emptyList()
        }

        logger.debug { "Fetching adjusted OHLCV from Naver: ticker=$ticker, from=$fromDate, to=$toDate" }

        // 1. 조회할 일수 계산
        // Naver API는 최근 N개의 데이터를 반환하므로, 오늘부터 fromDate까지의 일수를 계산해야 함
        // 휴장일 포함하여 여유분 50% 추가 (주말 + 공휴일 고려)
        val daysFromToday = ChronoUnit.DAYS.between(fromDate, LocalDate.now()).toInt()
        val daysInRange = ChronoUnit.DAYS.between(fromDate, toDate).toInt() + 1
        val count = ((daysFromToday + daysInRange) * 1.5).toInt().coerceAtMost(500) // Max 500

        // 2. API 호출
        val xml = try {
            fetchChartData(ticker, count)
        } catch (e: KfcException) {
            logger.warn { "Failed to fetch data for ticker $ticker: ${e.message}" }
            return emptyList()
        }

        // 3. XML 파싱
        val ohlcvList = try {
            parseXmlResponse(xml)
        } catch (e: KfcException) {
            logger.warn { "Failed to parse XML response for ticker $ticker: ${e.message}" }
            return emptyList()
        }

        // 4. 날짜 필터링 및 정렬
        return ohlcvList
            .filter { it.date in fromDate..toDate }
            .sortedBy { it.date }
            .also { logger.debug { "Fetched ${it.size} records from Naver" } }
    }

    /**
     * 네이버 차트 API 호출
     *
     * @param ticker 6자리 티커
     * @param count 조회할 데이터 개수
     * @param timeframe 시간 프레임 (day, week, month)
     * @return XML 응답 문자열
     */
    private suspend fun fetchChartData(
        ticker: String,
        count: Int,
        timeframe: String = "day"
    ): String {
        val url = "$BASE_URL?symbol=$ticker&timeframe=$timeframe&count=$count&requestType=0"

        logger.debug { "Requesting Naver API: $url" }

        return try {
            val response = httpClient.get(url) {
                header(HttpHeaders.UserAgent, USER_AGENT)
                header(HttpHeaders.Referrer, REFERER)
            }

            when {
                response.status.isSuccess() -> {
                    response.bodyAsText()
                }
                else -> {
                    val statusCode = response.status.value
                    logger.error { "Naver API returned HTTP $statusCode" }
                    throw KfcException(ErrorCode.HTTP_ERROR_RESPONSE)
                }
            }
        } catch (e: KfcException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Network error during request to Naver API" }
            throw KfcException(ErrorCode.NETWORK_CONNECTION_FAILED, e)
        }
    }

    /**
     * XML 응답 파싱
     *
     * 형식: <item data="20240102|42075|43250|41900|42965|192061" />
     *
     * @param xml XML 응답 문자열
     * @return OHLCV 데이터 목록
     */
    private fun parseXmlResponse(xml: String): List<NaverEtfOhlcv> {
        return try {
            val cleanedXml = xml.trimStart();
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(cleanedXml)))

            val items = doc.getElementsByTagName("item")
            val result = mutableListOf<NaverEtfOhlcv>()

            for (i in 0 until items.length) {
                val item = items.item(i) as Element
                val data = item.getAttribute("data")

                if (data.isNullOrBlank()) {
                    logger.warn { "Skipping item with empty data attribute" }
                    continue
                }

                val ohlcv = parseDataField(data)
                result.add(ohlcv)
            }

            result
        } catch (e: Exception) {
            throw KfcException(ErrorCode.XML_PARSE_ERROR, e)
        }
    }

    /**
     * data 필드 파싱
     *
     * 형식: "20240102|42075|43250|41900|42965|192061"
     *       날짜|시가|고가|저가|종가|거래량
     *
     * @param data 파싱할 데이터 문자열
     * @return NaverEtfOhlcv 객체
     */
    private fun parseDataField(data: String): NaverEtfOhlcv {
        val fields = data.split("|")
        require(fields.size >= 6) {
            "Invalid data format: expected 6 fields, got ${fields.size} in '$data'"
        }

        return NaverEtfOhlcv(
            date = parseDate(fields[0]),
            open = parseBigDecimal(fields[1]),
            high = parseBigDecimal(fields[2]),
            low = parseBigDecimal(fields[3]),
            close = parseBigDecimal(fields[4]),
            volume = parseLong(fields[5])
        )
    }

    /**
     * 날짜 파싱: "20240102" → LocalDate
     */
    private fun parseDate(yyyyMMdd: String): LocalDate {
        require(yyyyMMdd.length == 8) { "Invalid date format: $yyyyMMdd" }
        return LocalDate.parse(yyyyMMdd, DATE_FORMATTER)
    }

    /**
     * 숫자 파싱: "42965" → BigDecimal
     */
    private fun parseBigDecimal(value: String): BigDecimal {
        return value.trim()
            .replace(",", "")
            .toBigDecimalOrNull()
            ?: throw KfcException(ErrorCode.NUMBER_FORMAT_ERROR)
    }

    /**
     * 정수 파싱: "192061" → Long
     */
    private fun parseLong(value: String): Long {
        return value.trim()
            .replace(",", "")
            .toLongOrNull()
            ?: throw KfcException(ErrorCode.NUMBER_FORMAT_ERROR)
    }
}
