package dev.kairoscode.kfc.integration.index

import dev.kairoscode.kfc.domain.index.IndexMarket
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.TestFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * [Index] IndexApi - 지수 정보 API 통합 테스트
 *
 * KRX API를 사용한 지수 정보 조회 기능을 검증합니다.
 * API 문서처럼 읽히도록 설계되었습니다.
 */
@DisplayName("[I][Index] IndexApi - 지수 정보 API")
class IndexApiSpec : IntegrationTestBase() {
    @Nested
    @DisplayName("지수 목록 API")
    inner class IndexListApi {
        @Nested
        @DisplayName("getIndexList() - 지수 목록 조회")
        inner class GetIndexList {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("코스피 지수 목록을 조회할 수 있다")
                fun get_kospi_index_list() =
                    integrationTest {
                        // Given: Market = KOSPI
                        println("\n📘 API: getIndexList()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        println("📥 Input Parameters:")
                        println("  • market: IndexMarket = KOSPI")

                        // When
                        val indexes = client.index.getIndexList(market = IndexMarket.KOSPI)

                        // Then
                        println("\n📤 Response: List<IndexInfo>")
                        println("  • Total indexes: ${indexes.size}개")
                        println("  • Sample indexes:")
                        indexes.take(5).forEach { index ->
                            println("    - ${index.ticker}: ${index.name}")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(indexes)
                        assertTrue(indexes.isNotEmpty(), "지수 목록이 비어있지 않아야 합니다")
                        assertTrue(indexes.size >= 10, "코스피 지수는 10개 이상이어야 합니다")
                    }

                @Test
                @DisplayName("코스닥 지수 목록을 조회할 수 있다")
                fun get_kosdaq_index_list() =
                    integrationTest {
                        // Given: Market = KOSDAQ
                        println("\n📘 API: getIndexList()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        println("📥 Input Parameters:")
                        println("  • market: IndexMarket = KOSDAQ")

                        // When
                        val indexes = client.index.getIndexList(market = IndexMarket.KOSDAQ)

                        // Then
                        println("\n📤 Response: List<IndexInfo>")
                        println("  • Total indexes: ${indexes.size}개")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(indexes)
                        assertTrue(indexes.isNotEmpty(), "지수 목록이 비어있지 않아야 합니다")
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("모든 지수는 ticker와 name을 가져야 한다")
                fun all_indexes_have_ticker_and_name() =
                    integrationTest {
                        // Given
                        println("\n📘 응답 검증: 필수 필드 확인")

                        // When
                        val indexes = client.index.getIndexList(IndexMarket.KOSPI)

                        // Then
                        assertTrue(indexes.all { it.ticker.isNotBlank() })
                        assertTrue(indexes.all { it.name.isNotBlank() })
                        println("  • 검증 대상: ${indexes.size}개 지수")
                        println("  • 규칙: ticker와 name은 필수값")
                        println("  ✅ 모든 지수가 ticker와 name을 가집니다\n")
                    }
            }

            @Nested
            @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
            inner class ParameterCombinations {
                @Test
                @DisplayName("[market: KOSPI, KOSDAQ] 시장별 지수 수 비교")
                fun compare_different_markets() =
                    integrationTest {
                        println("\n📘 파라미터 조합: market")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // When
                        val kospiIndexes = client.index.getIndexList(IndexMarket.KOSPI)
                        val kosdaqIndexes = client.index.getIndexList(IndexMarket.KOSDAQ)

                        // Then
                        println("📊 시장별 지수 수 비교:")
                        println("  • KOSPI: ${kospiIndexes.size}개")
                        println("  • KOSDAQ: ${kosdaqIndexes.size}개")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(kospiIndexes.isNotEmpty(), "KOSPI 지수 목록이 비어있지 않아야 합니다")
                        assertTrue(kosdaqIndexes.isNotEmpty(), "KOSDAQ 지수 목록이 비어있지 않아야 합니다")
                    }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                // 시장별 지수는 항상 존재하므로 특별한 엣지 케이스 없음
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[필터링] 코스피200 관련 지수만 추출")
                fun filter_kospi200_indexes() =
                    integrationTest {
                        println("\n📘 실무 활용: 코스피200 관련 지수 필터링")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // When
                        val allIndexes = client.index.getIndexList(IndexMarket.KOSPI)
                        val kospi200Indexes = allIndexes.filter { it.name.contains("200") }

                        // Then
                        println("📊 필터링 결과:")
                        println("  • 전체 지수: ${allIndexes.size}개")
                        println("  • 코스피200 관련 지수: ${kospi200Indexes.size}개")
                        println("  • 지수 목록:")
                        kospi200Indexes.forEach {
                            println("    - ${it.ticker}: ${it.name}")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(allIndexes.isNotEmpty(), "전체 지수 목록이 비어있지 않아야 합니다")
                        assertTrue(kospi200Indexes.isNotEmpty(), "코스피200 관련 지수가 존재해야 합니다")
                    }
            }
        }

        @Nested
        @DisplayName("getIndexName() - 지수명 조회")
        inner class GetIndexName {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("코스피(1001) 지수명을 조회할 수 있다")
                fun get_kospi_index_name() =
                    integrationTest {
                        // Given
                        val ticker = "1001"
                        println("\n📘 API: getIndexName()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        println("📥 Input Parameters:")
                        println("  • ticker: String = \"$ticker\"")

                        // When
                        val name = client.index.getIndexName(ticker)

                        // Then
                        println("\n📤 Response: String?")
                        println("  • name: ${name ?: "null"}")
                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(name, "지수명이 조회되어야 합니다")
                        assertTrue(name!!.contains("코스피") || name.contains("KOSPI"), "코스피 지수명을 포함해야 합니다")
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                // 단순 String 응답이므로 추가 검증 불필요
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                @Test
                @DisplayName("존재하지 않는 지수 코드는 null을 반환한다")
                fun returns_null_for_non_existent_ticker() =
                    integrationTest {
                        // Given
                        val invalidTicker = "999999"
                        println("\n📘 입력 검증: 존재하지 않는 지수 코드")

                        // When
                        val name = client.index.getIndexName(invalidTicker)

                        // Then
                        println("  • 입력: $invalidTicker")
                        println("  • 결과: ${name ?: "null"}")
                        println("  ✅ null 반환 확인\n")

                        assertNull(name)
                    }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                // null 반환으로 처리되므로 추가 엣지 케이스 없음
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[변환] 지수 코드 목록을 지수명 목록으로 변환")
                fun convert_tickers_to_names() =
                    integrationTest {
                        println("\n📘 실무 활용: 지수 코드 → 지수명 변환")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val tickers = listOf("1001", "1028", "2001")

                        // When
                        val tickerToName =
                            tickers.associateWith { ticker ->
                                client.index.getIndexName(ticker)
                            }

                        // Then
                        println("📊 변환 결과:")
                        tickerToName.forEach { (ticker, name) ->
                            println("  • $ticker → ${name ?: "null"}")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(tickerToName.values.all { it != null }, "모든 지수 코드가 유효한 지수명을 반환해야 합니다")
                    }
            }
        }
    }

    @Nested
    @DisplayName("지수 OHLCV API")
    inner class IndexOhlcvApi {
        @Nested
        @DisplayName("getOhlcvByDate() - 지수 OHLCV 조회 (기간별)")
        inner class GetOhlcvByDate {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("기간 조회 시 시작일부터 종료일까지의 OHLCV 데이터가 날짜순으로 반환된다")
                fun get_kospi_ohlcv_one_month() =
                    integrationTest {
                        // Given: 조회 기간 (2024-11-01 ~ 2024-11-29)
                        val ticker = "1001" // 코스피
                        val fromDate = TestFixtures.PERIOD_START
                        val toDate = TestFixtures.PERIOD_END

                        println("\n📘 API: getOhlcvByDate()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        println("📥 Input Parameters:")
                        println("  • ticker: String = \"$ticker\"")
                        println("  • fromDate: LocalDate = $fromDate")
                        println("  • toDate: LocalDate = $toDate")

                        // When: 코스피 지수 OHLCV 조회
                        val ohlcvList = client.index.getOhlcvByDate(ticker, fromDate, toDate)

                        // Then: 데이터가 존재하고 날짜순으로 정렬됨
                        println("\n📤 Response: List<IndexOhlcv>")
                        println("  • Total records: ${ohlcvList.size}개")
                        if (ohlcvList.isNotEmpty()) {
                            println("  • Sample:")
                            ohlcvList.take(3).forEach { ohlcv ->
                                println("    - ${ohlcv.date}: open=${ohlcv.open}, close=${ohlcv.close}")
                            }
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(ohlcvList)
                        assertTrue(ohlcvList.isNotEmpty(), "한 달 기간 데이터는 비어있지 않아야 합니다")
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("OHLCV 데이터는 고가 >= 저가 조건을 만족해야 한다")
                fun high_should_be_greater_than_or_equal_to_low() =
                    integrationTest {
                        // Given
                        val ticker = "1001"
                        val fromDate = TestFixtures.PERIOD_START
                        val toDate = TestFixtures.PERIOD_END
                        println("\n📘 응답 검증: 고가/저가 관계")

                        // When
                        val ohlcvList = client.index.getOhlcvByDate(ticker, fromDate, toDate)

                        // Then
                        assertTrue(ohlcvList.isNotEmpty(), "기간 데이터는 비어있지 않아야 합니다")
                        assertTrue(ohlcvList.all { it.high >= it.low })
                        println("  • 검증 대상: ${ohlcvList.size}개 레코드")
                        println("  • 규칙: high >= low")
                        println("  ✅ 모든 레코드가 조건을 만족합니다\n")
                    }
            }

            @Nested
            @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
            inner class ParameterCombinations {
                // 기간별 조회는 기본 동작에서 테스트됨
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                @Test
                @DisplayName("비거래일(주말)에는 KRX가 빈 결과를 반환하므로 빈 리스트가 반환된다")
                fun returns_empty_list_for_weekend() =
                    integrationTest {
                        // Given: 비거래일 (토요일)
                        val ticker = "1001"
                        val weekend = TestFixtures.WEEKEND
                        println("\n📘 엣지 케이스: 비거래일(주말) 조회")
                        println("  • 날짜: $weekend (토요일)")

                        // When: 비거래일의 OHLCV 조회
                        val ohlcvList = client.index.getOhlcvByDate(ticker, weekend, weekend)

                        // Then: KRX API가 빈 결과를 반환
                        println("  • 결과: ${ohlcvList.size}개 레코드")
                        println("  ✅ KRX API 동작: 비거래일은 빈 리스트 반환\n")

                        assertTrue(ohlcvList.isEmpty(), "비거래일은 빈 리스트를 반환해야 합니다")
                    }

                @Test
                @DisplayName("미래 날짜는 데이터가 없으므로 빈 리스트가 반환된다")
                fun returns_empty_list_for_future_date() =
                    integrationTest {
                        // Given: 미래 날짜
                        val ticker = "1001"
                        val futureDate = TestFixtures.FUTURE_DATE
                        println("\n📘 엣지 케이스: 미래 날짜 조회")
                        println("  • 날짜: $futureDate (미래)")

                        // When: 미래 날짜의 OHLCV 조회
                        val ohlcvList = client.index.getOhlcvByDate(ticker, futureDate, futureDate)

                        // Then: 미래 데이터는 존재하지 않음
                        println("  • 결과: ${ohlcvList.size}개 레코드")
                        println("  ✅ KRX API 동작: 미래 날짜는 빈 리스트 반환\n")

                        assertTrue(ohlcvList.isEmpty(), "미래 날짜는 빈 리스트를 반환해야 합니다")
                    }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[분석] 일별 수익률 계산")
                fun calculate_daily_returns() =
                    integrationTest {
                        println("\n📘 실무 활용: 일별 수익률 계산")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val ticker = "1001"
                        val fromDate = TestFixtures.PERIOD_START
                        val toDate = TestFixtures.PERIOD_END

                        // When
                        val ohlcvList = client.index.getOhlcvByDate(ticker, fromDate, toDate)
                        val returns = ohlcvList.map { it.calculateReturn() }

                        // Then
                        println("📊 일별 수익률:")
                        ohlcvList.zip(returns).take(5).forEach { (ohlcv, ret) ->
                            println("  • ${ohlcv.date}: $ret% (open=${ohlcv.open}, close=${ohlcv.close})")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(ohlcvList.isNotEmpty(), "기간 데이터는 비어있지 않아야 합니다")
                        assertEquals(ohlcvList.size, returns.size)
                    }
            }
        }
    }

    @Nested
    @DisplayName("지수 구성 종목 API")
    inner class IndexConstituentsApi {
        @Nested
        @DisplayName("getIndexConstituents() - 지수 구성 종목 조회")
        inner class GetIndexConstituents {
            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {
                @Test
                @DisplayName("코스피200 구성 종목을 조회할 수 있다")
                fun get_kospi200_constituents() =
                    integrationTest {
                        // Given
                        val ticker = "1028" // 코스피 200
                        val date = TestFixtures.TRADING_DAY

                        println("\n📘 API: getIndexConstituents()")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        println("📥 Input Parameters:")
                        println("  • ticker: String = \"$ticker\"")
                        println("  • date: LocalDate = $date")

                        // When
                        val constituents = client.index.getIndexConstituents(ticker, date)

                        // Then
                        println("\n📤 Response: List<String>")
                        println("  • Total constituents: ${constituents.size}개")
                        println("  • Sample:")
                        constituents.take(10).forEach { code ->
                            println("    - $code")
                        }

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertNotNull(constituents)
                        assertTrue(constituents.isNotEmpty(), "거래일 데이터는 비어있지 않아야 합니다")
                        assertTrue(constituents.size >= 150, "코스피200은 최소 150개 이상의 종목을 포함해야 합니다")
                    }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                @Test
                @DisplayName("모든 종목 코드는 6자리여야 한다")
                fun all_tickers_are_six_digits() =
                    integrationTest {
                        // Given
                        val ticker = "1028"
                        val date = TestFixtures.TRADING_DAY
                        println("\n📘 응답 검증: 종목 코드 형식")

                        // When
                        val constituents = client.index.getIndexConstituents(ticker, date)

                        // Then
                        assertTrue(constituents.isNotEmpty(), "거래일 데이터는 비어있지 않아야 합니다")
                        assertTrue(constituents.all { it.length == 6 })
                        println("  • 검증 대상: ${constituents.size}개 종목")
                        println("  • 규칙: ticker.length == 6")
                        println("  ✅ 모든 종목 코드가 6자리입니다\n")
                    }
            }

            @Nested
            @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
            inner class ParameterCombinations {
                // 날짜별 조회는 기본 동작에서 테스트됨
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                @Test
                @DisplayName("비거래일(주말)에는 KRX가 빈 결과를 반환하므로 빈 리스트가 반환된다")
                fun returns_empty_list_for_weekend() =
                    integrationTest {
                        // Given: 비거래일 (토요일)
                        val ticker = "1028"
                        val weekend = TestFixtures.WEEKEND
                        println("\n📘 엣지 케이스: 비거래일(주말) 조회")
                        println("  • 날짜: $weekend (토요일)")

                        // When: 비거래일의 구성 종목 조회
                        val constituents = client.index.getIndexConstituents(ticker, weekend)

                        // Then: KRX API가 빈 결과를 반환
                        println("  • 결과: ${constituents.size}개 종목")
                        println("  ✅ KRX API 동작: 비거래일은 빈 리스트 반환\n")

                        assertTrue(constituents.isEmpty(), "비거래일은 빈 리스트를 반환해야 합니다")
                    }

                @Test
                @DisplayName("미래 날짜는 데이터가 없으므로 빈 리스트가 반환된다")
                fun returns_empty_list_for_future_date() =
                    integrationTest {
                        // Given: 미래 날짜
                        val ticker = "1028"
                        val futureDate = TestFixtures.FUTURE_DATE
                        println("\n📘 엣지 케이스: 미래 날짜 조회")
                        println("  • 날짜: $futureDate (미래)")

                        // When: 미래 날짜의 구성 종목 조회
                        val constituents = client.index.getIndexConstituents(ticker, futureDate)

                        // Then: 미래 데이터는 존재하지 않음
                        println("  • 결과: ${constituents.size}개 종목")
                        println("  ✅ KRX API 동작: 미래 날짜는 빈 리스트 반환\n")

                        assertTrue(constituents.isEmpty(), "미래 날짜는 빈 리스트를 반환해야 합니다")
                    }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {
                @Test
                @DisplayName("[분석] 삼성전자가 코스피200에 포함되는지 확인")
                fun check_samsung_in_kospi200() =
                    integrationTest {
                        println("\n📘 실무 활용: 특정 종목 포함 여부 확인")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                        // Given
                        val ticker = "1028"
                        val date = TestFixtures.TRADING_DAY
                        val samsungTicker = "005930"

                        // When
                        val constituents = client.index.getIndexConstituents(ticker, date)
                        val containsSamsung = constituents.contains(samsungTicker)

                        // Then
                        println("📊 확인 결과:")
                        println("  • 지수: 코스피200 (1028)")
                        println("  • 종목: 삼성전자 ($samsungTicker)")
                        println("  • 포함 여부: $containsSamsung")

                        println("\n✅ 테스트 결과: 성공")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                        assertTrue(constituents.isNotEmpty(), "거래일 데이터는 비어있지 않아야 합니다")
                        assertTrue(containsSamsung, "삼성전자는 코스피200에 포함되어야 합니다")
                    }
            }
        }
    }
}
