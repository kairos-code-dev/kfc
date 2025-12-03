package dev.kairoscode.kfc.integration.stock

import dev.kairoscode.kfc.domain.stock.Market
import dev.kairoscode.kfc.domain.stock.ListingStatus
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Stock API 통합 테스트
 *
 * 실제 KRX API를 호출하는 테스트입니다.
 */
@DisplayName("StockApi - 주식 종목 정보 API")
class StockApiIntegrationTest : IntegrationTestBase() {

    @Nested
    @DisplayName("getStockList() - 종목 리스트 조회")
    inner class GetStockListSpec {

        @Test
        @DisplayName("코스피 전체 종목 리스트를 조회할 수 있다")
        fun `returns KOSPI stock list`() = integrationTest {
            // When
            val stocks = client.stock.getStockList(market = Market.KOSPI, listingStatus = ListingStatus.LISTED)

            // Then
            assertNotNull(stocks)
            assertTrue(stocks.isNotEmpty(), "코스피 종목 리스트가 비어있지 않아야 합니다")
            assertTrue(stocks.size > 700, "코스피 종목 수는 700개 이상이어야 합니다. 실제: ${stocks.size}")
            assertTrue(stocks.all { it.ticker.length == 6 }, "모든 종목 코드는 6자리여야 합니다")
            assertTrue(stocks.all { it.isin.length == 12 }, "모든 ISIN은 12자리여야 합니다")
            assertTrue(stocks.all { it.market == Market.KOSPI }, "모든 종목은 코스피 시장이어야 합니다")

            println("✅ 코스피 상장 종목 수: ${stocks.size}")
            println("  샘플: ${stocks.take(5).joinToString { "${it.ticker}: ${it.name}" }}")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = stocks,
                category = RecordingConfig.Paths.Stock.LIST,
                fileName = "kospi_stocks"
            )
        }

        @Test
        @DisplayName("코스닥 전체 종목 리스트를 조회할 수 있다")
        fun `returns KOSDAQ stock list`() = integrationTest {
            // When
            val stocks = client.stock.getStockList(market = Market.KOSDAQ, listingStatus = ListingStatus.LISTED)

            // Then
            assertNotNull(stocks)
            assertTrue(stocks.isNotEmpty(), "코스닥 종목 리스트가 비어있지 않아야 합니다")
            assertTrue(stocks.size > 1000, "코스닥 종목 수는 1000개 이상이어야 합니다. 실제: ${stocks.size}")
            assertTrue(stocks.all { it.market == Market.KOSDAQ }, "모든 종목은 코스닥 시장이어야 합니다")

            println("✅ 코스닥 상장 종목 수: ${stocks.size}")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = stocks,
                category = RecordingConfig.Paths.Stock.LIST,
                fileName = "kosdaq_stocks"
            )
        }

        @Test
        @DisplayName("전체 시장 종목 리스트를 조회할 수 있다")
        fun `returns all market stock list`() = integrationTest {
            // When
            val stocks = client.stock.getStockList(market = Market.ALL, listingStatus = ListingStatus.LISTED)

            // Then
            assertNotNull(stocks)
            assertTrue(stocks.isNotEmpty(), "전체 종목 리스트가 비어있지 않아야 합니다")
            assertTrue(stocks.size > 2000, "전체 종목 수는 2000개 이상이어야 합니다. 실제: ${stocks.size}")

            println("✅ 전체 상장 종목 수: ${stocks.size}")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = stocks,
                category = RecordingConfig.Paths.Stock.LIST,
                fileName = "all_stocks"
            )
        }
    }

    @Nested
    @DisplayName("getStockInfo() - 종목 기본정보 조회")
    inner class GetStockInfoSpec {

        @Test
        @DisplayName("삼성전자 종목 정보를 조회할 수 있다")
        fun `returns Samsung Electronics stock info`() = integrationTest {
            // When
            val stockInfo = client.stock.getStockInfo("005930")

            // Then
            assertNotNull(stockInfo, "삼성전자 종목 정보가 null이 아니어야 합니다")
            stockInfo?.let { info ->
                assertTrue(info.name.contains("삼성전자"), "종목명에 '삼성전자'가 포함되어야 합니다")
                assertTrue(info.isin == "KR7005930003", "ISIN이 일치해야 합니다")
                assertTrue(info.market == Market.KOSPI, "시장이 코스피여야 합니다")

                println("✅ 삼성전자 종목 정보 조회 성공")
                println("  종목명: ${info.name}")
                println("  ISIN: ${info.isin}")
                println("  시장: ${info.market}")
            }
        }

        @Test
        @DisplayName("존재하지 않는 종목 코드는 null을 반환한다")
        fun `returns null for non-existent ticker`() = integrationTest {
            // When
            val stockInfo = client.stock.getStockInfo("999999")

            // Then
            assertTrue(stockInfo == null, "존재하지 않는 종목은 null이어야 합니다")
            println("✅ 존재하지 않는 종목 코드 처리: null 반환")
        }
    }

    @Nested
    @DisplayName("getStockName() - 종목명 조회")
    inner class GetStockNameSpec {

        @Test
        @DisplayName("종목 코드로 종목명을 조회할 수 있다")
        fun `returns stock name by ticker`() = integrationTest {
            // When
            val name = client.stock.getStockName("005930")

            // Then
            assertNotNull(name, "종목명이 null이 아니어야 합니다")
            name?.let {
                assertTrue(it.contains("삼성전자"), "종목명에 '삼성전자'가 포함되어야 합니다")
                println("✅ 종목명 조회: $it")
            }
        }
    }

    @Nested
    @DisplayName("getSectorClassifications() - 업종분류 현황 조회")
    inner class GetSectorClassificationsSpec {

        @Test
        @DisplayName("코스피 업종분류 현황을 조회할 수 있다")
        fun `returns KOSPI sector classifications`() = integrationTest {
            // When
            val sectors = client.stock.getSectorClassifications(
                date = LocalDate.now().minusDays(1),
                market = Market.KOSPI
            )

            // Then
            assertNotNull(sectors)
            assertTrue(sectors.isNotEmpty(), "업종분류 데이터가 비어있지 않아야 합니다")
            assertTrue(sectors.all { it.industry.isNotBlank() }, "모든 종목은 산업 분류가 있어야 합니다")

            println("✅ 업종분류 데이터 수: ${sectors.size}")
            println("  샘플: ${sectors.take(3).joinToString { "${it.name}(${it.ticker}) - ${it.industry}" }}")

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = sectors,
                category = RecordingConfig.Paths.Stock.SECTOR,
                fileName = "kospi_sectors"
            )
        }
    }

    @Nested
    @DisplayName("getIndustryGroups() - 산업별 그룹화 조회")
    inner class GetIndustryGroupsSpec {

        @Test
        @DisplayName("산업별 그룹화 데이터를 조회할 수 있다")
        fun `returns industry groups`() = integrationTest {
            // When
            val industries = client.stock.getIndustryGroups(
                date = LocalDate.now().minusDays(1),
                market = Market.KOSPI
            )

            // Then
            assertNotNull(industries)
            assertTrue(industries.isNotEmpty(), "산업 그룹이 비어있지 않아야 합니다")
            assertTrue(industries.all { it.stockCount > 0 }, "각 산업은 종목이 있어야 합니다")

            println("✅ 산업 분류 수: ${industries.size}")
            industries.sortedByDescending { it.totalMarketCap }.take(5).forEach {
                println("  ${it.industryName}: ${it.stockCount}개 종목, 시총 ${it.totalMarketCap}")
            }

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = industries,
                category = RecordingConfig.Paths.Stock.INDUSTRY,
                fileName = "kospi_industries"
            )
        }
    }

    @Nested
    @DisplayName("searchStocks() - 종목 검색")
    inner class SearchStocksSpec {

        @Test
        @DisplayName("이름으로 종목을 검색할 수 있다")
        fun `searches stocks by name`() = integrationTest {
            // When
            val results = client.stock.searchStocks("삼성", market = Market.KOSPI)

            // Then
            assertNotNull(results)
            assertTrue(results.isNotEmpty(), "검색 결과가 비어있지 않아야 합니다")
            assertTrue(results.any { it.name.contains("삼성전자") }, "'삼성전자'가 검색 결과에 포함되어야 합니다")

            println("✅ '삼성' 검색 결과 수: ${results.size}")
            results.take(10).forEach {
                println("  ${it.ticker}: ${it.name}")
            }

            // 스마트 레코딩
            SmartRecorder.recordSmartly(
                data = results,
                category = RecordingConfig.Paths.Stock.SEARCH,
                fileName = "search_samsung"
            )
        }

        @Test
        @DisplayName("코드로 종목을 검색할 수 있다")
        fun `searches stocks by ticker`() = integrationTest {
            // When
            val results = client.stock.searchStocks("0059", market = Market.ALL)

            // Then
            assertNotNull(results)
            assertTrue(results.isNotEmpty(), "검색 결과가 비어있지 않아야 합니다")
            assertTrue(results.any { it.ticker.startsWith("0059") }, "티커가 '0059'로 시작하는 종목이 있어야 합니다")

            println("✅ '0059' 검색 결과 수: ${results.size}")
            results.take(10).forEach {
                println("  ${it.ticker}: ${it.name}")
            }
        }
    }
}
