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

        // BLD 코드 상수 (채권 수익률)
        // 참고: MDCSTAT04301/04302는 ETF 코드이므로 사용 불가
        private const val BLD_YIELDS_BY_DATE = "dbms/MDC/STAT/standard/MDCSTAT11401"
        private const val BLD_YIELDS_BY_PERIOD = "dbms/MDC/STAT/standard/MDCSTAT11402"

        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val krxDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    }

    override suspend fun getBondYieldsByDate(date: LocalDate): BondYieldSnapshot {
        rateLimiter.acquire()
        logger.debug { "Fetching bond yields for date: $date" }

        val parameters = mapOf(
            "bld" to BLD_YIELDS_BY_DATE,
            "inqTpCd" to "T",  // 조회 타입 코드: T = 전종목
            "trdDd" to date.format(dateFormatter)
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // Bond API는 "output" 필드명 사용 (MDCSTAT11401)
        val outBlock = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>()
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
            "inqTpCd" to "E",  // 조회 타입 코드: E = 개별추이
            "strtDd" to fromDate.format(dateFormatter),
            "endDd" to toDate.format(dateFormatter),
            "bndKindTpCd" to bondType.krxCode
        )

        val response = httpClient.post(BASE_URL, parameters)
        response.checkForErrors()

        // Bond API는 "output" 필드명 사용 (MDCSTAT11402)
        val outBlock = (response["output"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()

        val yields = outBlock.map { raw ->
            BondYield(
                date = raw.getString(KrxApiFields.Bond.TRADE_DATE).parseKrxDate(),
                bondType = bondType,
                yield = raw.getString(KrxApiFields.Bond.YIELD).toBigDecimal(),
                change = raw.getString(KrxApiFields.Bond.CHANGE).toBigDecimal()
            )
        }.sortedBy { it.date }  // KRX는 내림차순 반환 → 오름차순으로 정렬

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
