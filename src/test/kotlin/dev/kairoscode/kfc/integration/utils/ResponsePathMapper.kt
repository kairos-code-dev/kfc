package dev.kairoscode.kfc.integration.utils

import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * HTTP URL을 레코딩 카테고리와 파일명으로 자동 매핑하는 유틸리티
 *
 * KFC 프로젝트의 다양한 API(KRX, Naver, OpenDART)의 URL 패턴을 분석하여
 * 자동으로 적절한 레코딩 경로와 파일명을 결정합니다.
 *
 * ## 지원하는 API 소스
 * - KRX: 한국거래소 ETF 데이터 API
 * - Naver: 네이버 증권 조정주가 API
 * - OpenDART: 금융감독원 전자공시시스템 API
 *
 * ## 사용 예시
 * ```kotlin
 * // KRX API 매핑
 * ResponsePathMapper.mapKrxRequest(
 *     bldCode = "dbms/MDC/STAT/standard/MDCSTAT04601",
 *     params = mapOf("trdDd" to "20240115")
 * )
 * // Returns: MappingResult("etf/list", "etf_list_all", HIGH)
 *
 * // Naver API 매핑
 * ResponsePathMapper.mapUrl("https://fchart.stock.naver.com/sise.nhn?symbol=069500&timeframe=day&count=100")
 * // Returns: MappingResult("etf/price/adjusted", "069500_adjusted_ohlcv", HIGH)
 * ```
 *
 * @see RecordingConfig 레코딩 경로 상수 정의
 * @see ResponseRecorder 실제 레코딩 수행
 */
object ResponsePathMapper {

    private val logger = LoggerFactory.getLogger(ResponsePathMapper::class.java)

    // 날짜 포맷터
    private val KRX_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    // ========================================
    // 데이터 클래스
    // ========================================

    /**
     * URL 매핑 결과
     *
     * @property category 레코딩 카테고리 경로 (RecordingConfig.Paths 기반)
     * @property fileName 파일명 (확장자 제외)
     * @property confidence 매핑 신뢰도
     */
    data class MappingResult(
        val category: String,
        val fileName: String,
        val confidence: MappingConfidence = MappingConfidence.HIGH
    ) {
        override fun toString(): String =
            "MappingResult(category='$category', fileName='$fileName', confidence=$confidence)"
    }

    /**
     * 매핑 신뢰도
     *
     * URL 패턴 매칭의 정확도를 나타냅니다.
     */
    enum class MappingConfidence {
        /** URL 패턴이 명확히 매칭됨 - BLD 코드나 URL 경로가 정확히 식별됨 */
        HIGH,

        /** URL 패턴이 부분 매칭됨 - 일부 파라미터가 누락되었거나 추론이 필요함 */
        MEDIUM,

        /** URL 패턴이 불명확함 - 기본값으로 폴백됨 */
        LOW,

        /** 매칭 불가 - 알 수 없는 API 패턴 */
        UNKNOWN
    }

    // ========================================
    // KRX API BLD 코드 상수
    // ========================================

    private object KrxBldCodes {
        // ETF 목록 및 기본 정보
        const val ETF_LIST = "dbms/MDC/STAT/standard/MDCSTAT04601"
        const val ETF_COMPREHENSIVE_INFO = "dbms/MDC/STAT/standard/MDCSTAT04701"

        // ETF 시세 및 OHLCV
        const val ETF_ALL_DAILY_PRICES = "dbms/MDC/STAT/standard/MDCSTAT04301"
        const val ETF_PRICE_CHANGES = "dbms/MDC/STAT/standard/MDCSTAT04401"
        const val ETF_OHLCV = "dbms/MDC/STAT/standard/MDCSTAT04501"

        // 투자자별 거래
        const val ETF_ALL_INVESTOR_TRADING_DAILY = "dbms/MDC/STAT/standard/MDCSTAT04801"
        const val ETF_ALL_INVESTOR_TRADING_PERIOD = "dbms/MDC/STAT/standard/MDCSTAT04802"
        const val ETF_INVESTOR_TRADING_DAILY = "dbms/MDC/STAT/standard/MDCSTAT04901"
        const val ETF_INVESTOR_TRADING_PERIOD = "dbms/MDC/STAT/standard/MDCSTAT04902"

        // 포트폴리오 및 성과
        const val ETF_PORTFOLIO = "dbms/MDC/STAT/standard/MDCSTAT05001"
        const val ETF_TRACKING_ERROR = "dbms/MDC/STAT/standard/MDCSTAT05901"
        const val ETF_DIVERGENCE_RATE = "dbms/MDC/STAT/standard/MDCSTAT06001"

        // 공매도
        const val ETF_SHORT_SELLING = "dbms/MDC/STAT/srt/MDCSTAT30102"
        const val ETF_SHORT_BALANCE = "dbms/MDC/STAT/srt/MDCSTAT30502"
    }

    // ========================================
    // OpenDART API 엔드포인트 상수
    // ========================================

    private object OpenDartEndpoints {
        const val CORP_CODE = "/api/corpCode.xml"
        const val DIVIDEND = "/api/alotMatter.json"
        const val STOCK_SPLIT = "/api/irdsSttus.json"
        const val DISCLOSURE_LIST = "/api/list.json"
    }

    // ========================================
    // 메인 매핑 함수
    // ========================================

    /**
     * URL 문자열을 분석하여 레코딩 경로로 매핑
     *
     * 절대 URL과 상대 URL 모두 지원하며, 호스트 기반으로 API 소스를 판별합니다.
     *
     * @param url 매핑할 URL 문자열
     * @return 매핑 결과, 매핑 불가 시 null
     */
    fun mapUrl(url: String): MappingResult? {
        logger.debug("Mapping URL: {}", url)

        return try {
            val uri = parseUri(url)
            val host = uri.host?.lowercase() ?: ""
            val path = uri.path ?: ""
            val queryParams = extractQueryParams(uri.rawQuery)

            val result = when {
                host.contains("krx.co.kr") -> mapKrxUrl(path, queryParams)
                host.contains("naver.com") -> mapNaverUrl(path, queryParams)
                host.contains("opendart.fss.or.kr") -> mapOpenDartUrl(path, queryParams)
                else -> {
                    logger.warn("Unknown API host: {}", host)
                    null
                }
            }

            result?.also {
                logger.info("Mapped URL to: {}", it)
            }
        } catch (e: Exception) {
            logger.error("Failed to map URL: {}", url, e)
            null
        }
    }

    /**
     * KRX API 요청을 레코딩 경로로 매핑 (BLD 코드 기반)
     *
     * KRX API는 단일 엔드포인트(`getJsonData.cmd`)를 사용하고
     * BLD 파라미터로 데이터 타입을 구분하므로, BLD 코드 기반 매핑이 필요합니다.
     *
     * @param bldCode KRX API의 BLD 코드 (예: "dbms/MDC/STAT/standard/MDCSTAT04601")
     * @param params 요청 파라미터
     * @return 매핑 결과
     */
    fun mapKrxRequest(
        bldCode: String,
        params: Map<String, String> = emptyMap()
    ): MappingResult {
        logger.debug("Mapping KRX request - bldCode: {}, params: {}", bldCode, params)

        return when (bldCode) {
            // ETF 목록
            KrxBldCodes.ETF_LIST -> MappingResult(
                category = RecordingConfig.Paths.EtfList.ALL,
                fileName = "etf_list_all",
                confidence = MappingConfidence.HIGH
            )

            // ETF 종합 정보
            KrxBldCodes.ETF_COMPREHENSIVE_INFO -> {
                val isin = params["isuCd"] ?: "unknown"
                val date = params["trdDd"]?.let { formatDateSuffix(it) } ?: "unknown"
                val ticker = extractTickerFromIsin(isin)

                MappingResult(
                    category = RecordingConfig.Paths.EtfMetrics.COMPREHENSIVE,
                    fileName = "${ticker}_comprehensive_$date",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // ETF 전체 일별 시세
            KrxBldCodes.ETF_ALL_DAILY_PRICES -> {
                val date = params["trdDd"]?.let { formatDateSuffix(it) } ?: "unknown"

                MappingResult(
                    category = RecordingConfig.Paths.EtfPrice.DAILY,
                    fileName = "etf_daily_prices_$date",
                    confidence = MappingConfidence.HIGH
                )
            }

            // ETF 기간 등락률
            KrxBldCodes.ETF_PRICE_CHANGES -> {
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfPrice.CHANGES,
                    fileName = "etf_price_changes_$period",
                    confidence = MappingConfidence.HIGH
                )
            }

            // ETF 개별 OHLCV
            KrxBldCodes.ETF_OHLCV -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfPrice.OHLCV,
                    fileName = "${ticker}_ohlcv_$period",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // ETF 포트폴리오
            KrxBldCodes.ETF_PORTFOLIO -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val date = params["trdDd"]?.let { formatDateSuffix(it) } ?: "unknown"

                MappingResult(
                    category = RecordingConfig.Paths.EtfMetrics.PORTFOLIO,
                    fileName = "${ticker}_portfolio_$date",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // ETF 추적 오차
            KrxBldCodes.ETF_TRACKING_ERROR -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfMetrics.TRACKING_ERROR,
                    fileName = "${ticker}_tracking_error_$period",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // ETF 괴리율
            KrxBldCodes.ETF_DIVERGENCE_RATE -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfMetrics.DIVERGENCE_RATE,
                    fileName = "${ticker}_divergence_rate_$period",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // 전체 ETF 투자자별 거래 (일별)
            KrxBldCodes.ETF_ALL_INVESTOR_TRADING_DAILY -> {
                val date = params["strtDd"]?.let { formatDateSuffix(it) }
                    ?: params["endDd"]?.let { formatDateSuffix(it) }
                    ?: "unknown"

                MappingResult(
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "etf_all_investor_trading_$date",
                    confidence = MappingConfidence.HIGH
                )
            }

            // 전체 ETF 투자자별 거래 (기간별)
            KrxBldCodes.ETF_ALL_INVESTOR_TRADING_PERIOD -> {
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "etf_all_investor_trading_period_$period",
                    confidence = MappingConfidence.HIGH
                )
            }

            // 개별 ETF 투자자별 거래 (일별)
            KrxBldCodes.ETF_INVESTOR_TRADING_DAILY -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val date = params["strtDd"]?.let { formatDateSuffix(it) }
                    ?: params["endDd"]?.let { formatDateSuffix(it) }
                    ?: "unknown"

                MappingResult(
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "${ticker}_investor_trading_$date",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // 개별 ETF 투자자별 거래 (기간별)
            KrxBldCodes.ETF_INVESTOR_TRADING_PERIOD -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfTrading.INVESTOR,
                    fileName = "${ticker}_investor_trading_period_$period",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // ETF 공매도 거래
            KrxBldCodes.ETF_SHORT_SELLING -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfTrading.SHORT,
                    fileName = "${ticker}_short_selling_$period",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // ETF 공매도 잔고
            KrxBldCodes.ETF_SHORT_BALANCE -> {
                val isin = params["isuCd"] ?: "unknown"
                val ticker = extractTickerFromIsin(isin)
                val period = calculatePeriodSuffix(params["strtDd"], params["endDd"])

                MappingResult(
                    category = RecordingConfig.Paths.EtfTrading.SHORT,
                    fileName = "${ticker}_short_balance_$period",
                    confidence = if (isin != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            // 알 수 없는 BLD 코드
            else -> {
                logger.warn("Unknown KRX BLD code: {}", bldCode)
                MappingResult(
                    category = "krx/unknown",
                    fileName = "krx_unknown_${System.currentTimeMillis()}",
                    confidence = MappingConfidence.UNKNOWN
                )
            }
        }.also {
            logger.info("Mapped KRX request to: {}", it)
        }
    }

    /**
     * Naver 증권 API 요청을 레코딩 경로로 매핑
     *
     * @param ticker 종목 티커 코드
     * @param fromDate 시작일
     * @param toDate 종료일
     * @return 매핑 결과
     */
    fun mapNaverRequest(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): MappingResult {
        val period = calculatePeriodSuffix(fromDate, toDate)

        return MappingResult(
            category = RecordingConfig.Paths.EtfPrice.ADJUSTED,
            fileName = "${ticker}_adjusted_ohlcv_$period",
            confidence = MappingConfidence.HIGH
        ).also {
            logger.info("Mapped Naver request to: {}", it)
        }
    }

    /**
     * OpenDART API 요청을 레코딩 경로로 매핑
     *
     * @param endpoint API 엔드포인트 (예: "/api/corpCode.xml")
     * @param params 요청 파라미터
     * @return 매핑 결과
     */
    fun mapOpenDartRequest(
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): MappingResult {
        logger.debug("Mapping OpenDART request - endpoint: {}, params: {}", endpoint, params)

        return when {
            endpoint.contains("corpCode.xml") -> MappingResult(
                category = RecordingConfig.Paths.CorpCode.LOOKUP,
                fileName = "corp_code_list",
                confidence = MappingConfidence.HIGH
            )

            endpoint.contains("alotMatter.json") -> {
                val corpCode = params["corp_code"] ?: "unknown"
                val year = params["bsns_year"] ?: "unknown"
                val reportCode = params["reprt_code"] ?: "11011"

                MappingResult(
                    category = RecordingConfig.Paths.CorpActions.DIVIDEND,
                    fileName = "${corpCode}_dividend_${year}_$reportCode",
                    confidence = if (corpCode != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            endpoint.contains("irdsSttus.json") -> {
                val corpCode = params["corp_code"] ?: "unknown"
                val year = params["bsns_year"] ?: "unknown"
                val reportCode = params["reprt_code"] ?: "11011"

                MappingResult(
                    category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                    fileName = "${corpCode}_stock_split_${year}_$reportCode",
                    confidence = if (corpCode != "unknown") MappingConfidence.HIGH else MappingConfidence.MEDIUM
                )
            }

            endpoint.contains("list.json") -> {
                val corpCode = params["corp_code"]
                val period = calculatePeriodSuffixFromParams(params["bgn_de"], params["end_de"])

                val fileName = if (corpCode != null) {
                    "${corpCode}_disclosures_$period"
                } else {
                    "all_disclosures_$period"
                }

                MappingResult(
                    category = RecordingConfig.Paths.CorpDisclosure.LIST,
                    fileName = fileName,
                    confidence = MappingConfidence.HIGH
                )
            }

            else -> {
                logger.warn("Unknown OpenDART endpoint: {}", endpoint)
                MappingResult(
                    category = "corp/unknown",
                    fileName = "opendart_unknown_${System.currentTimeMillis()}",
                    confidence = MappingConfidence.UNKNOWN
                )
            }
        }.also {
            logger.info("Mapped OpenDART request to: {}", it)
        }
    }

    // ========================================
    // 내부 URL 매핑 함수
    // ========================================

    /**
     * KRX URL을 레코딩 경로로 매핑
     */
    private fun mapKrxUrl(path: String, queryParams: Map<String, String>): MappingResult? {
        // KRX는 단일 엔드포인트를 사용하므로 BLD 파라미터 확인
        val bldCode = queryParams["bld"] ?: return null
        return mapKrxRequest(bldCode, queryParams)
    }

    /**
     * Naver URL을 레코딩 경로로 매핑
     */
    private fun mapNaverUrl(path: String, queryParams: Map<String, String>): MappingResult? {
        // Naver 증권 차트 API
        if (!path.contains("sise.nhn")) {
            logger.warn("Unknown Naver API path: {}", path)
            return null
        }

        val ticker = queryParams["symbol"] ?: return null

        return MappingResult(
            category = RecordingConfig.Paths.EtfPrice.ADJUSTED,
            fileName = "${ticker}_adjusted_ohlcv",
            confidence = MappingConfidence.HIGH
        )
    }

    /**
     * OpenDART URL을 레코딩 경로로 매핑
     */
    private fun mapOpenDartUrl(path: String, queryParams: Map<String, String>): MappingResult? {
        return mapOpenDartRequest(path, queryParams)
    }

    // ========================================
    // 유틸리티 함수
    // ========================================

    /**
     * URL 문자열을 URI로 파싱
     */
    private fun parseUri(url: String): URI {
        // 상대 경로인 경우 임시 호스트 추가
        val normalizedUrl = if (url.startsWith("/")) {
            "http://localhost$url"
        } else if (!url.startsWith("http")) {
            "http://$url"
        } else {
            url
        }
        return URI(normalizedUrl)
    }

    /**
     * 쿼리 스트링에서 파라미터 추출
     *
     * @param queryString 쿼리 스트링 (예: "symbol=069500&timeframe=day")
     * @return 파라미터 맵
     */
    fun extractQueryParams(queryString: String?): Map<String, String> {
        if (queryString.isNullOrBlank()) return emptyMap()

        return try {
            queryString.split("&")
                .mapNotNull { param ->
                    val parts = param.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8)
                        val value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                        key to value
                    } else {
                        null
                    }
                }
                .toMap()
        } catch (e: Exception) {
            logger.warn("Failed to parse query string: {}", queryString, e)
            emptyMap()
        }
    }

    /**
     * URL 경로를 세그먼트로 분리
     *
     * @param path URL 경로 (예: "/etf/069500/prices")
     * @return 경로 세그먼트 리스트 (예: ["etf", "069500", "prices"])
     */
    fun extractPathSegments(path: String?): List<String> {
        if (path.isNullOrBlank()) return emptyList()

        return path.split("/")
            .filter { it.isNotBlank() }
    }

    /**
     * ISIN 코드에서 티커 추출
     *
     * ISIN 형식: KR7069500007 -> 069500
     *
     * @param isin ISIN 코드
     * @return 6자리 티커 코드
     */
    private fun extractTickerFromIsin(isin: String): String {
        return if (isin.length >= 9 && isin.startsWith("KR")) {
            isin.substring(3, 9)
        } else if (isin.length == 6) {
            isin // 이미 티커 형식
        } else {
            isin.take(6).ifEmpty { "unknown" }
        }
    }

    /**
     * KRX 날짜 형식(yyyyMMdd)을 파일명 suffix로 변환
     */
    private fun formatDateSuffix(yyyyMMdd: String): String {
        return try {
            val date = LocalDate.parse(yyyyMMdd, KRX_DATE_FORMATTER)
            date.format(ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            yyyyMMdd
        }
    }

    /**
     * 시작일/종료일 파라미터로부터 기간 suffix 계산 (KRX 형식)
     */
    private fun calculatePeriodSuffix(startDate: String?, endDate: String?): String {
        if (startDate == null || endDate == null) return "unknown"

        return try {
            val start = LocalDate.parse(startDate, KRX_DATE_FORMATTER)
            val end = LocalDate.parse(endDate, KRX_DATE_FORMATTER)
            calculatePeriodSuffix(start, end)
        } catch (e: Exception) {
            "${startDate}_$endDate"
        }
    }

    /**
     * OpenDART 날짜 형식으로부터 기간 suffix 계산
     */
    private fun calculatePeriodSuffixFromParams(startDate: String?, endDate: String?): String {
        if (startDate == null || endDate == null) return "unknown"

        return try {
            val start = LocalDate.parse(startDate, KRX_DATE_FORMATTER)
            val end = LocalDate.parse(endDate, KRX_DATE_FORMATTER)
            calculatePeriodSuffix(start, end)
        } catch (e: Exception) {
            "${startDate}_$endDate"
        }
    }

    /**
     * LocalDate 기반 기간 suffix 계산
     */
    private fun calculatePeriodSuffix(startDate: LocalDate, endDate: LocalDate): String {
        val days = ChronoUnit.DAYS.between(startDate, endDate).toInt()

        return when {
            days == 0 -> startDate.format(ISO_DATE_FORMATTER)
            days <= 7 -> "1week"
            days <= 31 -> "1month"
            days <= 93 -> "3months"
            days <= 186 -> "6months"
            days <= 366 -> "1year"
            days <= 732 -> "2years"
            else -> "${startDate.format(ISO_DATE_FORMATTER)}_to_${endDate.format(ISO_DATE_FORMATTER)}"
        }
    }

    // ========================================
    // 디버그 및 유틸리티
    // ========================================

    /**
     * 지원하는 KRX BLD 코드 목록 반환
     */
    fun getSupportedKrxBldCodes(): List<String> = listOf(
        KrxBldCodes.ETF_LIST,
        KrxBldCodes.ETF_COMPREHENSIVE_INFO,
        KrxBldCodes.ETF_ALL_DAILY_PRICES,
        KrxBldCodes.ETF_PRICE_CHANGES,
        KrxBldCodes.ETF_OHLCV,
        KrxBldCodes.ETF_ALL_INVESTOR_TRADING_DAILY,
        KrxBldCodes.ETF_ALL_INVESTOR_TRADING_PERIOD,
        KrxBldCodes.ETF_INVESTOR_TRADING_DAILY,
        KrxBldCodes.ETF_INVESTOR_TRADING_PERIOD,
        KrxBldCodes.ETF_PORTFOLIO,
        KrxBldCodes.ETF_TRACKING_ERROR,
        KrxBldCodes.ETF_DIVERGENCE_RATE,
        KrxBldCodes.ETF_SHORT_SELLING,
        KrxBldCodes.ETF_SHORT_BALANCE
    )

    /**
     * 지원하는 OpenDART 엔드포인트 목록 반환
     */
    fun getSupportedOpenDartEndpoints(): List<String> = listOf(
        OpenDartEndpoints.CORP_CODE,
        OpenDartEndpoints.DIVIDEND,
        OpenDartEndpoints.STOCK_SPLIT,
        OpenDartEndpoints.DISCLOSURE_LIST
    )

    /**
     * 매핑 가이드 문자열 반환
     */
    fun getMappingGuide(): String = buildString {
        appendLine("=== ResponsePathMapper 매핑 가이드 ===")
        appendLine()
        appendLine("# KRX API (BLD 코드 기반)")
        appendLine("  - ETF_LIST -> etf/list/all")
        appendLine("  - ETF_COMPREHENSIVE_INFO -> etf/metrics/comprehensive")
        appendLine("  - ETF_ALL_DAILY_PRICES -> etf/price/daily")
        appendLine("  - ETF_OHLCV -> etf/price/ohlcv")
        appendLine("  - ETF_PRICE_CHANGES -> etf/price/changes")
        appendLine("  - ETF_PORTFOLIO -> etf/metrics/portfolio")
        appendLine("  - ETF_TRACKING_ERROR -> etf/metrics/tracking_error")
        appendLine("  - ETF_DIVERGENCE_RATE -> etf/metrics/divergence_rate")
        appendLine("  - ETF_INVESTOR_TRADING -> etf/trading/investor")
        appendLine("  - ETF_SHORT_* -> etf/trading/short")
        appendLine()
        appendLine("# Naver API")
        appendLine("  - /sise.nhn -> etf/price/adjusted")
        appendLine()
        appendLine("# OpenDART API")
        appendLine("  - /api/corpCode.xml -> corp/code/lookup")
        appendLine("  - /api/alotMatter.json -> corp/actions/dividend")
        appendLine("  - /api/irdsSttus.json -> corp/actions/stock_split")
        appendLine("  - /api/list.json -> corp/disclosure/list")
    }
}
