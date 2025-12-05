package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.future.FutureOhlcv
import dev.kairoscode.kfc.domain.future.FutureProduct
import dev.kairoscode.kfc.domain.stock.PriceChangeType
import dev.kairoscode.kfc.infrastructure.common.ratelimit.GlobalRateLimiters
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimiter
import dev.kairoscode.kfc.infrastructure.common.util.*
import dev.kairoscode.kfc.infrastructure.krx.internal.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * KRX 선물 API 구현체
 *
 * KrxFutureApi 인터페이스의 내부 구현입니다.
 * HTTP 클라이언트를 사용하여 실제 KRX API와 통신합니다.
 *
 * @param httpClient KRX HTTP 클라이언트
 * @param rateLimiter KRX API Rate Limiter (기본값: GlobalRateLimiters의 KRX 싱글톤)
 */
internal class KrxFutureApiImpl(
    private val httpClient: KrxHttpClient = KrxHttpClient(),
    private val rateLimiter: RateLimiter = GlobalRateLimiters.getKrxLimiter()
) : KrxFutureApi {

    companion object {
        private const val BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd"

        // BLD 코드 상수 (pykrx future/core.py 참조)
        private const val BLD_FUTURE_TICKER_LIST = "dbms/comm/component/drv_prod_clss"
        private const val BLD_FUTURE_OHLCV = "dbms/MDC/STAT/standard/MDCSTAT12501"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        // KRX API Future 응답 필드명 (pykrx future/core.py 참조)
        object FutureFields {
            // 티커 목록 API (drv_prod_clss)
            const val PRODUCT_ID = "value"
            const val PRODUCT_NAME = "name"

            // OHLCV API (MDCSTAT12501)
            const val ISSUE_CODE = "ISU_SRT_CD"  // Short code
            const val ISSUE_NAME = "ISU_NM"
            const val OPEN = "TDD_OPNPRC"
            const val HIGH = "TDD_HGPRC"
            const val LOW = "TDD_LWPRC"
            const val CLOSE = "TDD_CLSPRC"
            const val CHANGE_FROM_PREV = "CMPPREVDD_PRC"
            const val CHANGE_TYPE = "FLUC_TP_CD"
            const val VOLUME = "ACC_TRDVOL"
            const val TRADING_VALUE = "ACC_TRDVAL"
        }
    }

    override suspend fun getFutureTickerList(): List<FutureProduct> {
        rateLimiter.acquire()
        logger.debug { "Fetching future ticker list" }

        val parameters = mapOf(
            "bld" to BLD_FUTURE_TICKER_LIST,
            "secugrpId" to "ALL"
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "output" 필드 추출 (pykrx 참조)
        val output = response.extractOutput()

        return output.map { raw ->
            FutureProduct(
                productId = raw.getString(FutureFields.PRODUCT_ID),
                name = raw.getString(FutureFields.PRODUCT_NAME)
            )
        }.also { logger.debug { "Fetched ${it.size} future products" } }
    }

    override suspend fun getFutureOhlcv(
        date: LocalDate,
        productId: String
    ): List<FutureOhlcv> {
        rateLimiter.acquire()
        logger.debug { "Fetching future OHLCV for date: $date, productId: $productId" }

        val parameters = mapOf(
            "bld" to BLD_FUTURE_OHLCV,
            "trdDd" to date.format(dateFormatter),
            "prodId" to productId,
            "mktTpCd" to "T",  // 전체
            "rghtTpCd" to "T"  // 전체
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "output" 필드 추출 (pykrx 참조)
        val output = response.extractOutput()

        return output.map { raw ->
            FutureOhlcv(
                date = date,
                productId = productId,
                issueCode = raw.getString(FutureFields.ISSUE_CODE),
                issueName = raw.getString(FutureFields.ISSUE_NAME),
                open = raw.getString(FutureFields.OPEN).toKrxPrice(),
                high = raw.getString(FutureFields.HIGH).toKrxPrice(),
                low = raw.getString(FutureFields.LOW).toKrxPrice(),
                close = raw.getString(FutureFields.CLOSE).toKrxPrice(),
                changeFromPrev = raw.getStringOrNull(FutureFields.CHANGE_FROM_PREV)?.toKrxPrice(),
                changeRate = null,  // MDCSTAT12501 응답에는 등락률 없음
                priceChangeType = raw.getString(FutureFields.CHANGE_TYPE).toPriceChangeType(),
                volume = raw.getString(FutureFields.VOLUME).toKrxLong(),
                tradingValue = raw.getStringOrNull(FutureFields.TRADING_VALUE)?.toKrxLong()
            )
        }.also { logger.debug { "Fetched ${it.size} future OHLCV records" } }
    }
}

/**
 * String → PriceChangeType Enum 변환
 *
 * KRX API 등락 구분 코드를 PriceChangeType enum으로 변환합니다.
 *
 * @return PriceChangeType enum, 매칭 실패 시 null
 */
private fun String.toPriceChangeType(): PriceChangeType? {
    return when (this) {
        "1" -> PriceChangeType.RISE
        "2" -> PriceChangeType.FALL
        "3" -> PriceChangeType.UNCHANGED
        else -> null
    }
}
