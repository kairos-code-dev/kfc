package dev.kairoscode.kfc.examples

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.stock.Market
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Stock API 사용 예제
 *
 * 이 예제는 KFC 라이브러리의 Stock API를 활용하여
 * 주식 종목 정보를 조회하는 방법을 보여줍니다.
 *
 * 주요 기능:
 * 1. 주식 종목 리스트 조회
 * 2. 개별 종목 기본정보 조회
 * 3. 종목명 조회
 * 4. 업종분류 현황 조회
 * 5. 산업별 그룹화 데이터 조회
 * 6. 종목 검색
 */
fun main() = runBlocking {
    // KfcClient 생성
    val kfc = KfcClient.create()

    println("=".repeat(80))
    println("KFC Stock API 사용 예제")
    println("=".repeat(80))

    // 1. 코스피 종목 리스트 조회
    println("\n[1] 코스피 종목 리스트 조회")
    println("-".repeat(80))
    val kospiStocks = kfc.stock.getStockList(market = Market.KOSPI)
    println("코스피 상장 종목 수: ${kospiStocks.size}개")
    println("\n상위 5개 종목:")
    kospiStocks.take(5).forEach { stock ->
        println("  - ${stock.koreanName} (${stock.ticker}) | ISIN: ${stock.isin} | 시장: ${stock.market}")
    }

    // 2. 코스닥 종목 리스트 조회
    println("\n[2] 코스닥 종목 리스트 조회")
    println("-".repeat(80))
    val kosdaqStocks = kfc.stock.getStockList(market = Market.KOSDAQ)
    println("코스닥 상장 종목 수: ${kosdaqStocks.size}개")
    println("\n상위 5개 종목:")
    kosdaqStocks.take(5).forEach { stock ->
        println("  - ${stock.koreanName} (${stock.ticker}) | ISIN: ${stock.isin} | 시장: ${stock.market}")
    }

    // 3. 개별 종목 기본정보 조회
    println("\n[3] 삼성전자(005930) 기본정보 조회")
    println("-".repeat(80))
    val samsungInfo = kfc.stock.getStockInfo("005930")
    if (samsungInfo != null) {
        println("종목명: ${samsungInfo.koreanName}")
        println("영문명: ${samsungInfo.englishName}")
        println("종목코드: ${samsungInfo.ticker}")
        println("ISIN: ${samsungInfo.isin}")
        println("시장: ${samsungInfo.market}")
        println("상장일: ${samsungInfo.listingDate}")
    } else {
        println("종목 정보를 찾을 수 없습니다.")
    }

    // 4. 종목명 조회
    println("\n[4] 종목명 조회")
    println("-".repeat(80))
    val companyName = kfc.stock.getStockName("005930")
    println("005930의 종목명: $companyName")

    // 5. 종목 검색 - 삼성으로 시작하는 종목들
    println("\n[5] '삼성'으로 시작하는 종목 검색")
    println("-".repeat(80))
    val samsungStocks = kfc.stock.searchStocks(keyword = "삼성", market = Market.KOSPI)
    println("검색 결과: ${samsungStocks.size}개")
    samsungStocks.take(10).forEach { stock ->
        println("  - ${stock.koreanName} (${stock.ticker})")
    }

    // 6. 업종분류 현황 조회
    println("\n[6] 업종분류 현황 조회 (최근 거래일 기준)")
    println("-".repeat(80))
    val sectorClassifications = kfc.stock.getSectorClassifications(market = Market.KOSPI)
    println("전체 종목 수: ${sectorClassifications.size}개")
    println("\n상위 5개 종목 (시가총액 기준):")
    sectorClassifications
        .sortedByDescending { it.marketCap }
        .take(5)
        .forEach { info ->
            println("  - ${info.koreanName} (${info.ticker})")
            println("    산업: ${info.industryName} | 섹터: ${info.sectorName}")
            println("    시가총액: ${String.format("%,d", info.marketCap)}백만원")
        }

    // 7. 산업별 그룹화 데이터 조회
    println("\n[7] 산업별 그룹화 데이터 조회")
    println("-".repeat(80))
    val industryGroups = kfc.stock.getIndustryGroups(market = Market.ALL)
    println("총 산업 수: ${industryGroups.size}개")
    println("\n시가총액 상위 10개 산업:")
    industryGroups
        .sortedByDescending { it.totalMarketCap }
        .take(10)
        .forEach { industry ->
            println("  - ${industry.industryName}")
            println("    종목 수: ${industry.stockCount}개")
            println("    총 시가총액: ${String.format("%,d", industry.totalMarketCap)}백만원")
        }

    // 8. 특정 산업의 종목 리스트 조회
    println("\n[8] 특정 산업 내 종목 상세 정보")
    println("-".repeat(80))
    val semiconductorStocks = sectorClassifications
        .filter { it.industryName.contains("반도체") }
        .sortedByDescending { it.marketCap }
    println("반도체 산업 종목 수: ${semiconductorStocks.size}개")
    println("\n시가총액 상위 5개 종목:")
    semiconductorStocks.take(5).forEach { stock ->
        println("  - ${stock.koreanName} (${stock.ticker})")
        println("    시가총액: ${String.format("%,d", stock.marketCap)}백만원")
    }

    println("\n" + "=".repeat(80))
    println("예제 실행 완료!")
    println("=".repeat(80))
}
