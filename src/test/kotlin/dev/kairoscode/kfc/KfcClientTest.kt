package dev.kairoscode.kfc

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * KfcClient Facade 테스트
 *
 * 통합 클라이언트를 통한 API 접근 테스트입니다.
 */
class KfcClientTest {

    @Test
    fun `create should initialize KRX and Naver API clients`() {
        // === arrange & act ===
        val client = KfcClient.create()

        // === assert ===
        assertThat(client.krx).isNotNull
        assertThat(client.naver).isNotNull
        assertThat(client.opendart).isNull()
    }

    @Test
    fun `create should initialize OPENDART API when key is provided`() {
        // === arrange ===
        val apiKey = System.getenv("OPENDART_API_KEY")
        assumeTrue(
            apiKey != null && apiKey.isNotBlank(),
            "OPENDART_API_KEY 환경변수가 설정되지 않았습니다. 이 테스트를 건너뜁니다."
        )

        // === act ===
        val client = KfcClient.create(opendartApiKey = apiKey)

        // === assert ===
        assertThat(client.krx).isNotNull
        assertThat(client.naver).isNotNull
        assertThat(client.opendart).isNotNull
    }

    @Test
    fun `krx API should be accessible through client`() = runBlocking {
        // === arrange ===
        val client = KfcClient.create()

        // === act ===
        val etfList = client.krx.getEtfList()

        // === assert ===
        assertThat(etfList).isNotEmpty
        assertThat(etfList.size).isGreaterThan(200)
    }

    @Test
    fun `naver API should be accessible through client`() = runBlocking {
        // === arrange ===
        val client = KfcClient.create()
        val ticker = "069500"
        val date = LocalDate.now().minusDays(1)

        // === act ===
        val result = client.naver.getAdjustedOhlcv(ticker, date, date)

        // === assert ===
        assertThat(result).isNotEmpty
    }

    @Test
    fun `opendart API should be accessible through client when key is provided`() = runBlocking {
        // === arrange ===
        val apiKey = System.getenv("OPENDART_API_KEY")
        assumeTrue(
            apiKey != null && apiKey.isNotBlank(),
            "OPENDART_API_KEY 환경변수가 설정되지 않았습니다. 이 테스트를 건너뜁니다."
        )

        val client = KfcClient.create(opendartApiKey = apiKey)

        // === act ===
        val corpCodes = client.opendart?.getCorpCodeList()

        // === assert ===
        assertThat(corpCodes).isNotNull
        assertThat(corpCodes!!.size).isGreaterThan(60000)
    }

    @Test
    fun `integration test - KRX plus Naver data collection workflow`() = runBlocking {
        // === arrange ===
        val client = KfcClient.create()
        val date = LocalDate.of(2024, 8, 16)  // Fixed date with guaranteed data

        // === act ===
        // 1. KRX에서 ETF 목록 조회
        val etfList = client.krx.getEtfList()
        assertThat(etfList).isNotEmpty

        // 2. KODEX 200 찾기
        val kodex200 = etfList.find { it.ticker == "069500" }
        assertThat(kodex200).isNotNull

        kotlinx.coroutines.delay(100)

        // 3. KRX에서 KODEX 200 OHLCV 조회
        val krxOhlcv = client.krx.getEtfOhlcv(kodex200!!.isin, date, date)
        assertThat(krxOhlcv).hasSize(1)

        kotlinx.coroutines.delay(100)

        // 4. Naver에서 조정 종가 조회
        val naverOhlcv = client.naver.getAdjustedOhlcv(kodex200.ticker, date, date)
        assertThat(naverOhlcv).hasSize(1)

        // === assert ===
        // 날짜 일치
        assertThat(krxOhlcv.first().tradeDate).isEqualTo(naverOhlcv.first().date)

        // 가격 비교 (원본 종가와 조정 종가)
        println("KRX Close: ${krxOhlcv.first().closePrice}")
        println("Naver Adjusted Close: ${naverOhlcv.first().close}")
    }

    @Test
    fun `integration test - full workflow with all three APIs`() = runBlocking {
        // === arrange ===
        val apiKey = System.getenv("OPENDART_API_KEY")
        assumeTrue(
            apiKey != null && apiKey.isNotBlank(),
            "OPENDART_API_KEY 환경변수가 설정되지 않았습니다. 이 테스트를 건너뜁니다."
        )

        val client = KfcClient.create(opendartApiKey = apiKey)
        val ticker = "005930" // 삼성전자
        val date = LocalDate.now().minusDays(1)

        // === act ===
        // 1. OPENDART에서 corp_code 조회
        val corpCodes = client.opendart!!.getCorpCodeList()
        val corpCode = corpCodes.find { it.stockCode == ticker }
        assertThat(corpCode).isNotNull

        kotlinx.coroutines.delay(100)

        // 2. OPENDART에서 배당 정보 조회
        val dividends = client.opendart!!.getDividendInfo(corpCode!!.corpCode, 2023)

        kotlinx.coroutines.delay(100)

        // 3. Naver에서 조정 종가 조회
        val adjustedOhlcv = client.naver.getAdjustedOhlcv(ticker, date, date)

        // === assert ===
        println("Corp Code: ${corpCode.corpCode}")
        println("Dividends: ${dividends.size} records")
        println("Adjusted OHLCV: ${adjustedOhlcv.size} records")

        // 모든 데이터 수집 완료
        assertThat(corpCode.corpCode).isNotBlank()
        assertThat(adjustedOhlcv).isNotEmpty
    }

    @Test
    fun `multiple clients should work independently`() = runBlocking {
        // === arrange ===
        val client1 = KfcClient.create()
        val client2 = KfcClient.create()

        // === act ===
        val etfList1 = client1.krx.getEtfList()
        val etfList2 = client2.krx.getEtfList()

        // === assert ===
        assertThat(etfList1.size).isEqualTo(etfList2.size)
    }

    @Test
    fun `client should handle multiple sequential requests`() = runBlocking {
        // === arrange ===
        val client = KfcClient.create()
        val tickers = listOf("069500", "102110", "114800")
        val date = LocalDate.of(2024, 8, 16)  // Fixed date with guaranteed data

        // === act ===
        val results = mutableListOf<List<dev.kairoscode.kfc.model.naver.NaverEtfOhlcv>>()
        for (ticker in tickers) {
            val result = client.naver.getAdjustedOhlcv(ticker, date, date)
            results.add(result)
            kotlinx.coroutines.delay(100)
        }

        // === assert ===
        assertThat(results).hasSize(3)
        results.forEach { result ->
            assertThat(result).isNotEmpty
        }
    }
}
