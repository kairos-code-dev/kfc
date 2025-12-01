package dev.kairoscode.kfc

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * KfcClient 통합 테스트
 *
 * 도메인별 API가 정상적으로 동작하는지 확인합니다.
 */
class KfcClientTest {

    @Test
    fun `ETF 도메인 API가 정상적으로 동작하는지 확인`() = runBlocking {
        // given
        val kfc = KfcClient.create()

        // when
        val etfList = kfc.etf.getList()

        // then
        assert(etfList.isNotEmpty()) { "ETF 목록이 비어있으면 안됩니다" }
        println("✅ ETF 목록 조회 성공: ${etfList.size}개")
    }

    @Test
    fun `ETF OHLCV 조회가 정상적으로 동작하는지 확인`() = runBlocking {
        // given
        val kfc = KfcClient.create()
        val isin = "KR7152100004" // ARIRANG 200

        // when
        val ohlcv = kfc.etf.getOhlcv(
            isin = isin,
            fromDate = LocalDate.of(2024, 1, 1),
            toDate = LocalDate.of(2024, 1, 31)
        )

        // then
        assert(ohlcv.isNotEmpty()) { "OHLCV 데이터가 비어있으면 안됩니다" }
        println("✅ OHLCV 조회 성공: ${ohlcv.size}일")
    }

    @Test
    fun `조정주가 OHLCV 조회가 정상적으로 동작하는지 확인`() = runBlocking {
        // given
        val kfc = KfcClient.create()
        val ticker = "152100"

        // when
        val adjustedOhlcv = kfc.etf.getAdjustedOhlcv(
            ticker = ticker,
            fromDate = LocalDate.of(2024, 1, 1),
            toDate = LocalDate.of(2024, 1, 31)
        )

        // then
        // Naver API는 데이터가 없을 수 있으므로 빈 리스트도 허용
        println("✅ 조정주가 OHLCV 조회 성공: ${adjustedOhlcv.size}일")
    }

    @Test
    fun `기업 공시 도메인 API가 정상적으로 동작하는지 확인 (API Key 없으면 스킵)`() = runBlocking {
        // given
        val opendartApiKey = System.getenv("OPENDART_API_KEY")
        if (opendartApiKey.isNullOrBlank()) {
            println("⚠️ OPENDART_API_KEY 환경변수가 없어서 테스트를 스킵합니다")
            return@runBlocking
        }

        val kfc = KfcClient.create(opendartApiKey = opendartApiKey)

        // when
        val corpCodes = kfc.corp?.getCorpCodeList()

        // then
        assert(corpCodes != null && corpCodes.isNotEmpty()) { "법인코드 목록이 비어있으면 안됩니다" }
        println("✅ 법인코드 목록 조회 성공: ${corpCodes?.size}개")
    }
}
