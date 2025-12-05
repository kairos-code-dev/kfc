package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.bond.*
import dev.kairoscode.kfc.infrastructure.common.ratelimit.GlobalRateLimiters
import dev.kairoscode.kfc.infrastructure.common.ratelimit.RateLimiter
import dev.kairoscode.kfc.infrastructure.common.util.*
import dev.kairoscode.kfc.infrastructure.krx.internal.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * KRX 채권 수익률 API 구현체
 *
 * KrxBondApi 인터페이스의 내부 구현입니다.
 * HTTP 클라이언트를 사용하여 실제 KRX API와 통신합니다.
 *
 * @param httpClient KRX HTTP 클라이언트
 * @param rateLimiter KRX API Rate Limiter (기본값: GlobalRateLimiters의 KRX 싱글톤)
 */
internal class KrxBondApiImpl(
    private val httpClient: KrxHttpClient = KrxHttpClient(),
    private val rateLimiter: RateLimiter = GlobalRateLimiters.getKrxLimiter()
) : KrxBondApi {

    companion object {
        private const val BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd"

        // BLD 코드 상수
        private const val BLD_YIELDS_BY_DATE = "dbms/MDC/STAT/standard/MDCSTAT04301"
        private const val BLD_YIELDS_BY_PERIOD = "dbms/MDC/STAT/standard/MDCSTAT04302"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val krxDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    }

    override suspend fun getBondYieldsByDate(date: LocalDate): BondYieldSnapshot {
        rateLimiter.acquire()
        logger.debug { "Fetching bond yields for date: $date" }

        val parameters = mapOf(
            "bld" to BLD_YIELDS_BY_DATE,
            "trdDd" to date.format(dateFormatter)
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // KRX API는 "OutBlock_1" 또는 "block1" 등 다양한 응답 필드명 사용
        // 기존 ETF/Stock API와 달리 Bond API는 "OutBlock_1" 사용
        val outBlock = (response["OutBlock_1"] as? List<*>)?.filterIsInstance<Map<String, Any?>>()
            ?: emptyList()

        logger.debug { "Response contains ${outBlock.size} bond records" }

        val yields = outBlock.mapNotNull { raw ->
            val kindName = raw.getString(KrxApiFields.Bond.KIND_NAME)
            val bondType = BondType.fromKoreanName(kindName)

            if (bondType != null) {
                BondYieldItem(
                    bondType = bondType,
                    yield = raw.getString(KrxApiFields.Bond.YIELD).toBigDecimal(),
                    change = raw.getString(KrxApiFields.Bond.CHANGE).toBigDecimal()
                )
            } else {
                logger.warn { "Unknown bond type: $kindName (skipping)" }
                null
            }
        }

        logger.debug { "Fetched ${yields.size} bond yields" }

        return BondYieldSnapshot(
            date = date,
            yields = yields
        )
    }

    override suspend fun getBondYields(
        bondType: BondType,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<BondYield> {
        rateLimiter.acquire()
        logger.debug { "Fetching bond yields for $bondType from $fromDate to $toDate" }

        val parameters = mapOf(
            "bld" to BLD_YIELDS_BY_PERIOD,
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "bndKindTpCd" to bondType.code
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // "OutBlock_1" 필드 추출
        val outBlock = (response["OutBlock_1"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        val yields = outBlock.map { raw ->
            BondYield(
                date = raw.getString(KrxApiFields.Bond.TRADE_DATE).parseKrxDate(),
                bondType = bondType,
                yield = raw.getString(KrxApiFields.Bond.YIELD).toBigDecimal(),
                change = raw.getString(KrxApiFields.Bond.CHANGE).toBigDecimal()
            )
        }

        logger.debug { "Fetched ${yields.size} bond yields" }

        return yields
    }

    /**
     * KRX 날짜 형식 (YYYY/MM/DD) → LocalDate 변환
     */
    private fun String.parseKrxDate(): LocalDate {
        return LocalDate.parse(this, krxDateFormatter)
    }
}
