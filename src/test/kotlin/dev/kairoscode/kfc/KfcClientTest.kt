package dev.kairoscode.kfc

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import java.time.LocalDate

/**
 * KfcClient 통합 테스트
 *
 * 도메인별 API가 정상적으로 동작하는지 확인합니다.
 */
class KfcClientTest {

    @Test
    fun `펀드 도메인 API가 정상적으로 동작하는지 확인`() = runBlocking {
        // given
        val kfc = KfcClient.create()

        // when
        val fundsList = kfc.funds.getList()

        // then
        assert(fundsList.isNotEmpty()) { "펀드 목록이 비어있으면 안됩니다" }
        println("✅ 펀드 목록 조회 성공: ${fundsList.size}개")
    }

    @Test
    fun `가격 도메인 API가 정상적으로 동작하는지 확인`() = runBlocking {
        // given
        val kfc = KfcClient.create()
        val isin = "KR7152100004" // ARIRANG 200

        // when
        val intradayBars = kfc.price.getIntradayBars(isin)

        // then
        assertNotNull(intradayBars) { "분단위 시세 조회는 성공해야 합니다" }
        println("✅ 분단위 시세 조회 성공: ${intradayBars.size}개")
    }

    @Test
    fun `가격 도메인 최근 일별 거래 조회가 정상적으로 동작하는지 확인`() = runBlocking {
        // given
        val kfc = KfcClient.create()
        val isin = "KR7152100004" // ARIRANG 200

        // when
        val recentDaily = kfc.price.getRecentDaily(isin)

        // then
        assertNotNull(recentDaily) { "최근 일별 거래 조회는 성공해야 합니다" }
        println("✅ 최근 일별 거래 조회 성공: ${recentDaily.size}개")
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
