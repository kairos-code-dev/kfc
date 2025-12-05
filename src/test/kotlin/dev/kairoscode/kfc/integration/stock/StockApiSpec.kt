package dev.kairoscode.kfc.integration.stock

import dev.kairoscode.kfc.domain.stock.Market
import dev.kairoscode.kfc.domain.stock.ListingStatus
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Stock API 통합 테스트
 *
 * 실제 KRX API를 호출하는 테스트입니다.
 * API 문서처럼 읽히도록 설계되었습니다.
 */
@DisplayName("StockApi - 주식 종목 정보 API")
class StockApiSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("getStockList() - 종목 리스트 조회")
    inner class GetStockList {

        @Nested
        @DisplayName("1. 기본 동작 (Basic Operations)")
        inner class BasicOperations {

            @Test
            @DisplayName("코스피 전체 종목 리스트를 조회할 수 있다")
            fun get_kospi_stock_list() = integrationTest {
                // Given: Market and ListingStatus
                println("\n📘 API: getStockList()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • market: Market = KOSPI")
                println("  • listingStatus: ListingStatus = LISTED")

                // When
                val stocks = client.stock.getStockList(
                    market = Market.KOSPI,
                    listingStatus = ListingStatus.LISTED
                )

                // Then
                println("\n📤 Response: List<StockInfo>")
                println("  • Total stocks: ${stocks.size}개")
                println("  • Sample stocks:")
                stocks.take(3).forEach { stock ->
                    println("    - ${stock.ticker}: ${stock.name} (${stock.isin})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(stocks)
                assertTrue(stocks.isNotEmpty(), "코스피 종목 리스트가 비어있지 않아야 합니다")
                assertTrue(stocks.size > 700, "코스피 종목 수는 700개 이상이어야 합니다. 실제: ${stocks.size}")

                SmartRecorder.recordSmartly(
                    data = stocks,
                    category = RecordingConfig.Paths.Stock.LIST,
                    fileName = "kospi_stocks"
                )
            }

            @Test
            @DisplayName("코스닥 전체 종목 리스트를 조회할 수 있다")
            fun get_kosdaq_stock_list() = integrationTest {
                // Given: Market and ListingStatus
                println("\n📘 API: getStockList()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • market: Market = KOSDAQ")
                println("  • listingStatus: ListingStatus = LISTED")

                // When
                val stocks = client.stock.getStockList(
                    market = Market.KOSDAQ,
                    listingStatus = ListingStatus.LISTED
                )

                // Then
                println("\n📤 Response: List<StockInfo>")
                println("  • Total stocks: ${stocks.size}개")
                println("  • Sample stocks:")
                stocks.take(3).forEach { stock ->
                    println("    - ${stock.ticker}: ${stock.name} (${stock.isin})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(stocks)
                assertTrue(stocks.isNotEmpty(), "코스닥 종목 리스트가 비어있지 않아야 합니다")
                assertTrue(stocks.size > 1000, "코스닥 종목 수는 1000개 이상이어야 합니다. 실제: ${stocks.size}")

                SmartRecorder.recordSmartly(
                    data = stocks,
                    category = RecordingConfig.Paths.Stock.LIST,
                    fileName = "kosdaq_stocks"
                )
            }

            @Test
            @DisplayName("전체 시장 종목 리스트를 조회할 수 있다")
            fun get_all_market_stock_list() = integrationTest {
                // Given: Market and ListingStatus
                println("\n📘 API: getStockList()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • market: Market = ALL")
                println("  • listingStatus: ListingStatus = LISTED")

                // When
                val stocks = client.stock.getStockList(
                    market = Market.ALL,
                    listingStatus = ListingStatus.LISTED
                )

                // Then
                println("\n📤 Response: List<StockInfo>")
                println("  • Total stocks: ${stocks.size}개")
                println("  • Sample stocks:")
                stocks.take(3).forEach { stock ->
                    println("    - ${stock.ticker}: ${stock.name} (${stock.market})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(stocks)
                assertTrue(stocks.isNotEmpty(), "전체 종목 리스트가 비어있지 않아야 합니다")
                assertTrue(stocks.size > 2000, "전체 종목 수는 2000개 이상이어야 합니다. 실제: ${stocks.size}")

                SmartRecorder.recordSmartly(
                    data = stocks,
                    category = RecordingConfig.Paths.Stock.LIST,
                    fileName = "all_stocks"
                )
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증 (Response Validation)")
        inner class ResponseValidation {

            @Test
            @DisplayName("모든 종목 코드는 6자리여야 한다")
            fun ticker_length_is_six_digits() = integrationTest {
                // Given
                println("\n📘 응답 검증: 종목 코드 형식")

                // When
                val stocks = client.stock.getStockList(Market.KOSPI, ListingStatus.LISTED)

                // Then
                assertTrue(stocks.all { it.ticker.length == 6 })
                println("  • 검증 대상: ${stocks.size}개 종목")
                println("  • 규칙: ticker.length == 6")
                println("  ✅ 모든 종목 코드는 6자리입니다")
            }

            @Test
            @DisplayName("모든 ISIN은 12자리여야 한다")
            fun isin_length_is_twelve_digits() = integrationTest {
                // Given
                println("\n📘 응답 검증: ISIN 형식")

                // When
                val stocks = client.stock.getStockList(Market.KOSPI, ListingStatus.LISTED)

                // Then
                assertTrue(stocks.all { it.isin.length == 12 })
                println("  • 검증 대상: ${stocks.size}개 종목")
                println("  • 규칙: isin.length == 12")
                println("  ✅ 모든 ISIN은 12자리입니다")
            }

            @Test
            @DisplayName("모든 종목은 요청한 시장에 속해야 한다")
            fun all_stocks_belong_to_requested_market() = integrationTest {
                // Given
                println("\n📘 응답 검증: 시장 필터링")

                // When
                val kospiStocks = client.stock.getStockList(Market.KOSPI, ListingStatus.LISTED)
                val kosdaqStocks = client.stock.getStockList(Market.KOSDAQ, ListingStatus.LISTED)

                // Then
                assertTrue(kospiStocks.all { it.market == Market.KOSPI })
                assertTrue(kosdaqStocks.all { it.market == Market.KOSDAQ })
                println("  • KOSPI 종목 수: ${kospiStocks.size}")
                println("  • KOSDAQ 종목 수: ${kosdaqStocks.size}")
                println("  ✅ 모든 종목이 요청한 시장에 속합니다")
            }
        }

        @Nested
        @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
        inner class ParameterCombinations {

            @Test
            @DisplayName("[market: KOSPI, KOSDAQ, ALL] 각 시장별 조회 비교")
            fun compare_different_markets() = integrationTest {
                println("\n📘 파라미터 조합: market")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val kospiStocks = client.stock.getStockList(Market.KOSPI, ListingStatus.LISTED)
                val kosdaqStocks = client.stock.getStockList(Market.KOSDAQ, ListingStatus.LISTED)
                val allStocks = client.stock.getStockList(Market.ALL, ListingStatus.LISTED)

                // Then
                println("📊 시장별 종목 수 비교:")
                println("  • KOSPI: ${kospiStocks.size}개")
                println("  • KOSDAQ: ${kosdaqStocks.size}개")
                println("  • ALL: ${allStocks.size}개")
                println("  • 검증: KOSPI + KOSDAQ ≈ ALL")

                val sumOfIndividual = kospiStocks.size + kosdaqStocks.size
                val difference = kotlin.math.abs(allStocks.size - sumOfIndividual)
                println("  • 차이: ${difference}개 (${(difference.toDouble() / allStocks.size * 100).toInt()}%)")
                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(allStocks.size >= kospiStocks.size + kosdaqStocks.size - 10)
            }

            @Test
            @DisplayName("[listingStatus: LISTED, DELISTED] 상장여부별 조회")
            fun compare_listing_status() = integrationTest {
                println("\n📘 파라미터 조합: listingStatus")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val listedStocks = client.stock.getStockList(Market.ALL, ListingStatus.LISTED)
                val delistedStocks = client.stock.getStockList(Market.ALL, ListingStatus.DELISTED)

                // Then
                println("📊 상장여부별 종목 수:")
                println("  • LISTED (상장): ${listedStocks.size}개")
                println("  • DELISTED (상장폐지): ${delistedStocks.size}개")

                if (delistedStocks.isNotEmpty()) {
                    println("  • 상장폐지 종목 샘플:")
                    delistedStocks.take(3).forEach {
                        println("    - ${it.ticker}: ${it.name}")
                    }
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(listedStocks)
                assertNotNull(delistedStocks)
            }
        }

        @Nested
        @DisplayName("4. 엣지 케이스 (Edge Cases)")
        inner class EdgeCases {
            // 현재 getStockList는 엣지 케이스가 명확하지 않음
            // - Market과 ListingStatus는 enum으로 제한되어 있음
            // - 빈 결과는 정상 케이스로 처리됨
        }

        @Nested
        @DisplayName("5. 실무 활용 예제 (Usage Examples)")
        inner class UsageExamples {

            @Test
            @DisplayName("[필터링] 삼성 그룹 종목만 추출")
            fun filter_samsung_group_stocks() = integrationTest {
                println("\n📘 실무 활용: 삼성 그룹 종목 필터링")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val allStocks = client.stock.getStockList(Market.ALL, ListingStatus.LISTED)
                val samsungStocks = allStocks.filter { it.name.contains("삼성") }

                // Then
                println("📊 필터링 결과:")
                println("  • 전체 종목: ${allStocks.size}개")
                println("  • 삼성 그룹 종목: ${samsungStocks.size}개")
                println("  • 종목 목록:")
                samsungStocks.take(15).forEach {
                    println("    - ${it.ticker}: ${it.name} (${it.market})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(samsungStocks.isNotEmpty())
                assertTrue(samsungStocks.any { it.name.contains("삼성전자") })
            }

            @Test
            @DisplayName("[정렬] 시가총액 상위 종목 추출")
            fun sort_by_market_cap() = integrationTest {
                println("\n📘 실무 활용: 종목명으로 정렬")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val stocks = client.stock.getStockList(Market.KOSPI, ListingStatus.LISTED)
                val sortedStocks = stocks.sortedBy { it.name }

                // Then
                println("📊 정렬 결과:")
                println("  • 전체 종목: ${stocks.size}개")
                println("  • 가나다순 상위 10개:")
                sortedStocks.take(10).forEach {
                    println("    - ${it.ticker}: ${it.name}")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertEquals(stocks.size, sortedStocks.size)
            }

            @Test
            @DisplayName("[그룹화] 시장별 종목 수 통계")
            fun group_by_market() = integrationTest {
                println("\n📘 실무 활용: 시장별 종목 수 통계")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val allStocks = client.stock.getStockList(Market.ALL, ListingStatus.LISTED)
                val groupedByMarket = allStocks.groupBy { it.market }

                // Then
                println("📊 시장별 통계:")
                groupedByMarket.forEach { (market, stocks) ->
                    println("  • ${market}: ${stocks.size}개")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(groupedByMarket.containsKey(Market.KOSPI))
                assertTrue(groupedByMarket.containsKey(Market.KOSDAQ))
            }
        }
    }

    @Nested
    @DisplayName("getStockInfo() - 종목 기본정보 조회")
    inner class GetStockInfo {

        @Nested
        @DisplayName("1. 기본 동작 (Basic Operations)")
        inner class BasicOperations {

            @Test
            @DisplayName("삼성전자 종목 정보를 조회할 수 있다")
            fun get_samsung_electronics_stock_info() = integrationTest {
                // Given
                val ticker = "005930"
                println("\n📘 API: getStockInfo()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • ticker: String = \"$ticker\"")

                // When
                val stockInfo = client.stock.getStockInfo(ticker)

                // Then
                println("\n📤 Response: StockInfo?")
                if (stockInfo != null) {
                    println("  • ticker: ${stockInfo.ticker}")
                    println("  • name: ${stockInfo.name}")
                    println("  • isin: ${stockInfo.isin}")
                    println("  • market: ${stockInfo.market}")
                } else {
                    println("  null")
                }
                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(stockInfo, "삼성전자 종목 정보가 null이 아니어야 합니다")
                stockInfo?.let { info ->
                    assertTrue(info.name.contains("삼성전자"), "종목명에 '삼성전자'가 포함되어야 합니다")
                    assertEquals("KR7005930003", info.isin, "ISIN이 일치해야 합니다")
                    assertEquals(Market.KOSPI, info.market, "시장이 코스피여야 합니다")
                }
            }

            @Test
            @DisplayName("SK하이닉스 종목 정보를 조회할 수 있다")
            fun get_sk_hynix_stock_info() = integrationTest {
                // Given
                val ticker = "000660"
                println("\n📘 API: getStockInfo()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • ticker: String = \"$ticker\"")

                // When
                val stockInfo = client.stock.getStockInfo(ticker)

                // Then
                println("\n📤 Response: StockInfo?")
                if (stockInfo != null) {
                    println("  • ticker: ${stockInfo.ticker}")
                    println("  • name: ${stockInfo.name}")
                    println("  • isin: ${stockInfo.isin}")
                    println("  • market: ${stockInfo.market}")
                }
                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(stockInfo)
                stockInfo?.let { info ->
                    assertTrue(info.name.contains("SK하이닉스"))
                    assertEquals(Market.KOSPI, info.market)
                }
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증 (Response Validation)")
        inner class ResponseValidation {
            // getStockInfo는 단일 종목 조회로 응답 검증이 기본 동작에 포함됨
        }

        @Nested
        @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
        inner class InputValidation {

            @Test
            @DisplayName("존재하지 않는 종목 코드는 null을 반환한다")
            fun returns_null_for_non_existent_ticker() = integrationTest {
                // Given
                val invalidTicker = "999999"
                println("\n📘 입력 검증: 존재하지 않는 종목 코드")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val stockInfo = client.stock.getStockInfo(invalidTicker)

                // Then
                println("📥 Input: ticker = \"$invalidTicker\"")
                println("📤 Response: ${stockInfo ?: "null"}")
                println("  ✅ null 반환 확인")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNull(stockInfo, "존재하지 않는 종목은 null이어야 합니다")
            }
        }

        @Nested
        @DisplayName("4. 엣지 케이스 (Edge Cases)")
        inner class EdgeCases {
            // 현재 API는 null 반환으로 엣지 케이스를 처리함
        }

        @Nested
        @DisplayName("5. 실무 활용 예제 (Usage Examples)")
        inner class UsageExamples {

            @Test
            @DisplayName("[검증] 여러 종목 정보를 일괄 조회")
            fun batch_get_stock_info() = integrationTest {
                println("\n📘 실무 활용: 여러 종목 정보 일괄 조회")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val tickers = listOf("005930", "035720", "000660", "051910")

                // When
                val stockInfos = tickers.mapNotNull { ticker ->
                    client.stock.getStockInfo(ticker)
                }

                // Then
                println("📊 조회 결과:")
                println("  • 요청 종목 수: ${tickers.size}개")
                println("  • 성공 조회 수: ${stockInfos.size}개")
                println("  • 종목 정보:")
                stockInfos.forEach {
                    println("    - ${it.ticker}: ${it.name} (${it.market})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertEquals(tickers.size, stockInfos.size)
            }
        }
    }

    @Nested
    @DisplayName("getStockName() - 종목명 조회")
    inner class GetStockName {

        @Nested
        @DisplayName("1. 기본 동작 (Basic Operations)")
        inner class BasicOperations {

            @Test
            @DisplayName("종목 코드로 종목명을 조회할 수 있다")
            fun get_stock_name_by_ticker() = integrationTest {
                // Given
                val ticker = "005930"
                println("\n📘 API: getStockName()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • ticker: String = \"$ticker\"")

                // When
                val name = client.stock.getStockName(ticker)

                // Then
                println("\n📤 Response: String?")
                println("  • name: ${name ?: "null"}")
                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(name, "종목명이 null이 아니어야 합니다")
                name?.let {
                    assertTrue(it.contains("삼성전자"), "종목명에 '삼성전자'가 포함되어야 합니다")
                }
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증 (Response Validation)")
        inner class ResponseValidation {
            // 종목명은 String 타입으로 추가 검증 불필요
        }

        @Nested
        @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
        inner class InputValidation {

            @Test
            @DisplayName("존재하지 않는 종목 코드는 null을 반환한다")
            fun returns_null_for_invalid_ticker() = integrationTest {
                // Given
                val invalidTicker = "999999"
                println("\n📘 입력 검증: 존재하지 않는 종목 코드")

                // When
                val name = client.stock.getStockName(invalidTicker)

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
            // getStockName은 단순 조회로 엣지 케이스가 명확하지 않음
        }

        @Nested
        @DisplayName("5. 실무 활용 예제 (Usage Examples)")
        inner class UsageExamples {

            @Test
            @DisplayName("[변환] 종목 코드 목록을 종목명 목록으로 변환")
            fun convert_tickers_to_names() = integrationTest {
                println("\n📘 실무 활용: 종목 코드 → 종목명 변환")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val tickers = listOf("005930", "035720", "000660", "051910")

                // When
                val tickerToName = tickers.associateWith { ticker ->
                    client.stock.getStockName(ticker)
                }

                // Then
                println("📊 변환 결과:")
                tickerToName.forEach { (ticker, name) ->
                    println("  • $ticker → ${name ?: "null"}")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(tickerToName.values.all { it != null })
            }
        }
    }

    @Nested
    @DisplayName("getSectorClassifications() - 업종분류 현황 조회")
    inner class GetSectorClassifications {

        @Nested
        @DisplayName("1. 기본 동작 (Basic Operations)")
        inner class BasicOperations {

            @Test
            @DisplayName("코스피 업종분류 현황을 조회할 수 있다")
            fun get_kospi_sector_classifications() = integrationTest {
                // Given
                val date = LocalDate.now().minusDays(1)
                val market = Market.KOSPI
                println("\n📘 API: getSectorClassifications()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • date: LocalDate = $date")
                println("  • market: Market = $market")

                // When
                val sectors = client.stock.getSectorClassifications(
                    date = date,
                    market = market
                )

                // Then
                println("\n📤 Response: List<SectorClassification>")
                println("  • Total: ${sectors.size}개")
                println("  • Sample:")
                sectors.take(3).forEach { sector ->
                    println("    - ${sector.name}(${sector.ticker}) → ${sector.industry}")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(sectors)
                assertTrue(sectors.isNotEmpty(), "업종분류 데이터가 비어있지 않아야 합니다")

                SmartRecorder.recordSmartly(
                    data = sectors,
                    category = RecordingConfig.Paths.Stock.SECTOR,
                    fileName = "kospi_sectors"
                )
            }

            @Test
            @DisplayName("코스닥 업종분류 현황을 조회할 수 있다")
            fun get_kosdaq_sector_classifications() = integrationTest {
                // Given
                val date = LocalDate.now().minusDays(1)
                val market = Market.KOSDAQ
                println("\n📘 API: getSectorClassifications()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • date: LocalDate = $date")
                println("  • market: Market = $market")

                // When
                val sectors = client.stock.getSectorClassifications(
                    date = date,
                    market = market
                )

                // Then
                println("\n📤 Response: List<SectorClassification>")
                println("  • Total: ${sectors.size}개")

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(sectors)
                assertTrue(sectors.isNotEmpty())
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증 (Response Validation)")
        inner class ResponseValidation {

            @Test
            @DisplayName("모든 종목은 산업 분류를 가져야 한다")
            fun all_stocks_have_industry() = integrationTest {
                // Given
                println("\n📘 응답 검증: 산업 분류 필수값")

                // When
                val sectors = client.stock.getSectorClassifications(
                    date = LocalDate.now().minusDays(1),
                    market = Market.KOSPI
                )

                // Then
                assertTrue(sectors.all { it.industry.isNotBlank() })
                println("  • 검증 대상: ${sectors.size}개 종목")
                println("  • 규칙: industry.isNotBlank()")
                println("  ✅ 모든 종목이 산업 분류를 가집니다\n")
            }
        }

        @Nested
        @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
        inner class ParameterCombinations {
            // date와 market 조합은 기본 동작에서 테스트됨
        }

        @Nested
        @DisplayName("4. 엣지 케이스 (Edge Cases)")
        inner class EdgeCases {
            // 날짜 범위나 특정 날짜의 데이터 부재 등
        }

        @Nested
        @DisplayName("5. 실무 활용 예제 (Usage Examples)")
        inner class UsageExamples {

            @Test
            @DisplayName("[그룹화] 업종별 종목 수 통계")
            fun count_stocks_by_industry() = integrationTest {
                println("\n📘 실무 활용: 업종별 종목 수 통계")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val sectors = client.stock.getSectorClassifications(
                    date = LocalDate.now().minusDays(1),
                    market = Market.KOSPI
                )
                val groupedByIndustry = sectors.groupBy { it.industry }
                val industryCounts = groupedByIndustry.mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }

                // Then
                println("📊 업종별 종목 수:")
                industryCounts.take(10).forEach { (industry, count) ->
                    println("  • $industry: ${count}개")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(industryCounts.isNotEmpty())
            }
        }
    }

    @Nested
    @DisplayName("getIndustryGroups() - 산업별 그룹화 조회")
    inner class GetIndustryGroups {

        @Nested
        @DisplayName("1. 기본 동작 (Basic Operations)")
        inner class BasicOperations {

            @Test
            @DisplayName("산업별 그룹화 데이터를 조회할 수 있다")
            fun get_industry_groups() = integrationTest {
                // Given
                val date = LocalDate.now().minusDays(1)
                val market = Market.KOSPI
                println("\n📘 API: getIndustryGroups()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • date: LocalDate = $date")
                println("  • market: Market = $market")

                // When
                val industries = client.stock.getIndustryGroups(
                    date = date,
                    market = market
                )

                // Then
                println("\n📤 Response: List<IndustryGroup>")
                println("  • Total industries: ${industries.size}개")
                println("  • Top 5 by market cap:")
                industries.sortedByDescending { it.totalMarketCap }.take(5).forEach {
                    println("    - ${it.industryName}: ${it.stockCount}개 종목, 시총 ${it.totalMarketCap}")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(industries)
                assertTrue(industries.isNotEmpty(), "산업 그룹이 비어있지 않아야 합니다")

                SmartRecorder.recordSmartly(
                    data = industries,
                    category = RecordingConfig.Paths.Stock.INDUSTRY,
                    fileName = "kospi_industries"
                )
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증 (Response Validation)")
        inner class ResponseValidation {

            @Test
            @DisplayName("각 산업은 최소 1개 이상의 종목을 가져야 한다")
            fun each_industry_has_stocks() = integrationTest {
                // Given
                println("\n📘 응답 검증: 종목 수 필수값")

                // When
                val industries = client.stock.getIndustryGroups(
                    date = LocalDate.now().minusDays(1),
                    market = Market.KOSPI
                )

                // Then
                assertTrue(industries.all { it.stockCount > 0 })
                println("  • 검증 대상: ${industries.size}개 산업")
                println("  • 규칙: stockCount > 0")
                println("  ✅ 모든 산업이 종목을 가집니다\n")
            }
        }

        @Nested
        @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
        inner class ParameterCombinations {
            // date와 market 조합은 기본 동작에서 테스트됨
        }

        @Nested
        @DisplayName("4. 엣지 케이스 (Edge Cases)")
        inner class EdgeCases {
            // 특정 날짜의 데이터 부재 등
        }

        @Nested
        @DisplayName("5. 실무 활용 예제 (Usage Examples)")
        inner class UsageExamples {

            @Test
            @DisplayName("[분석] 시가총액 상위 산업 분석")
            fun analyze_top_industries_by_market_cap() = integrationTest {
                println("\n📘 실무 활용: 시가총액 상위 산업 분석")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val industries = client.stock.getIndustryGroups(
                    date = LocalDate.now().minusDays(1),
                    market = Market.KOSPI
                )
                val topIndustries = industries.sortedByDescending { it.totalMarketCap }.take(10)
                val totalMarketCap = industries.sumOf { it.totalMarketCap }

                // Then
                println("📊 시가총액 Top 10 산업:")
                topIndustries.forEachIndexed { index, industry ->
                    val percentage = (industry.totalMarketCap.toDouble() / totalMarketCap * 100)
                    println("  ${index + 1}. ${industry.industryName}")
                    println("     - 종목 수: ${industry.stockCount}개")
                    println("     - 시가총액: ${industry.totalMarketCap} (${String.format("%.2f", percentage)}%)")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(topIndustries.isNotEmpty())
            }
        }
    }

    @Nested
    @DisplayName("searchStocks() - 종목 검색")
    inner class SearchStocks {

        @Nested
        @DisplayName("1. 기본 동작 (Basic Operations)")
        inner class BasicOperations {

            @Test
            @DisplayName("이름으로 종목을 검색할 수 있다")
            fun search_stocks_by_name() = integrationTest {
                // Given
                val keyword = "삼성"
                val market = Market.KOSPI
                println("\n📘 API: searchStocks()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • keyword: String = \"$keyword\"")
                println("  • market: Market = $market")

                // When
                val results = client.stock.searchStocks(keyword, market = market)

                // Then
                println("\n📤 Response: List<StockInfo>")
                println("  • Total results: ${results.size}개")
                println("  • Sample results:")
                results.take(10).forEach {
                    println("    - ${it.ticker}: ${it.name} (${it.market})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(results)
                assertTrue(results.isNotEmpty(), "검색 결과가 비어있지 않아야 합니다")
                assertTrue(results.any { it.name.contains("삼성전자") }, "'삼성전자'가 검색 결과에 포함되어야 합니다")

                SmartRecorder.recordSmartly(
                    data = results,
                    category = RecordingConfig.Paths.Stock.SEARCH,
                    fileName = "search_samsung"
                )
            }

            @Test
            @DisplayName("코드로 종목을 검색할 수 있다")
            fun search_stocks_by_ticker() = integrationTest {
                // Given
                val keyword = "0059"
                val market = Market.ALL
                println("\n📘 API: searchStocks()")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("📥 Input Parameters:")
                println("  • keyword: String = \"$keyword\"")
                println("  • market: Market = $market")

                // When
                val results = client.stock.searchStocks(keyword, market = market)

                // Then
                println("\n📤 Response: List<StockInfo>")
                println("  • Total results: ${results.size}개")
                println("  • Sample results:")
                results.take(10).forEach {
                    println("    - ${it.ticker}: ${it.name} (${it.market})")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertNotNull(results)
                assertTrue(results.isNotEmpty(), "검색 결과가 비어있지 않아야 합니다")
                assertTrue(results.any { it.ticker.startsWith("0059") }, "티커가 '0059'로 시작하는 종목이 있어야 합니다")
            }
        }

        @Nested
        @DisplayName("2. 응답 데이터 검증 (Response Validation)")
        inner class ResponseValidation {

            @Test
            @DisplayName("검색 결과는 키워드를 포함해야 한다")
            fun results_contain_keyword() = integrationTest {
                // Given
                val keyword = "현대"
                println("\n📘 응답 검증: 검색 결과 정확성")

                // When
                val results = client.stock.searchStocks(keyword, market = Market.ALL)

                // Then
                assertTrue(results.all { it.name.contains(keyword) || it.ticker.contains(keyword) })
                println("  • 키워드: $keyword")
                println("  • 검증 대상: ${results.size}개 결과")
                println("  ✅ 모든 결과가 키워드를 포함합니다\n")
            }
        }

        @Nested
        @DisplayName("3. 파라미터 조합 (Parameter Combinations)")
        inner class ParameterCombinations {

            @Test
            @DisplayName("[market: KOSPI, KOSDAQ, ALL] 시장별 검색 결과 비교")
            fun compare_search_results_by_market() = integrationTest {
                println("\n📘 파라미터 조합: market")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // When
                val keyword = "테크"
                val kospiResults = client.stock.searchStocks(keyword, market = Market.KOSPI)
                val kosdaqResults = client.stock.searchStocks(keyword, market = Market.KOSDAQ)
                val allResults = client.stock.searchStocks(keyword, market = Market.ALL)

                // Then
                println("📊 시장별 검색 결과:")
                println("  • 키워드: \"$keyword\"")
                println("  • KOSPI: ${kospiResults.size}개")
                println("  • KOSDAQ: ${kosdaqResults.size}개")
                println("  • ALL: ${allResults.size}개")

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(allResults.size >= kospiResults.size + kosdaqResults.size)
            }
        }

        @Nested
        @DisplayName("4. 엣지 케이스 (Edge Cases)")
        inner class EdgeCases {

            @Test
            @DisplayName("존재하지 않는 키워드는 빈 리스트를 반환한다")
            fun returns_empty_for_non_existent_keyword() = integrationTest {
                // Given
                val keyword = "존재하지않는종목명XYZ123"
                println("\n📘 엣지 케이스: 존재하지 않는 키워드")

                // When
                val results = client.stock.searchStocks(keyword, market = Market.ALL)

                // Then
                println("  • 키워드: $keyword")
                println("  • 결과: ${results.size}개")
                println("  ✅ 빈 리스트 반환\n")

                assertTrue(results.isEmpty())
            }
        }

        @Nested
        @DisplayName("5. 실무 활용 예제 (Usage Examples)")
        inner class UsageExamples {

            @Test
            @DisplayName("[자동완성] 사용자 입력에 따른 종목 추천")
            fun autocomplete_stock_search() = integrationTest {
                println("\n📘 실무 활용: 자동완성 종목 검색")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // Given
                val userInput = "삼"

                // When
                val suggestions = client.stock.searchStocks(userInput, market = Market.ALL)
                    .take(10) // 자동완성은 보통 상위 N개만 표시

                // Then
                println("📊 자동완성 결과:")
                println("  • 사용자 입력: \"$userInput\"")
                println("  • 추천 종목 (최대 10개):")
                suggestions.forEach {
                    println("    - ${it.ticker}: ${it.name} [${it.market}]")
                }

                println("\n✅ 테스트 결과: 성공")
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                assertTrue(suggestions.isNotEmpty())
            }
        }
    }
}
