package dev.kairoscode.kfc.examples

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.FundType
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * ETF API 사용 예제
 *
 * 이 예제는 KFC 라이브러리의 Funds API를 활용하여
 * ETF 관련 데이터를 조회하는 방법을 보여줍니다.
 *
 * 주요 기능:
 * 1. ETF 목록 조회
 * 2. ETF 상세정보 조회
 * 3. ETF 포트폴리오 구성 조회
 * 4. ETF 공매도 데이터 조회
 */
fun main() = runBlocking {
    // KfcClient 생성
    val kfc = KfcClient.create()

    println("=".repeat(80))
    println("KFC ETF API 사용 예제")
    println("=".repeat(80))

    // 1. 전체 ETF 목록 조회
    println("\n[1] 전체 ETF 목록 조회")
    println("-".repeat(80))
    val allEtfs = kfc.funds.getList(type = FundType.ETF)
    println("전체 ETF 수: ${allEtfs.size}개")
    println("\n상위 10개 ETF:")
    allEtfs.take(10).forEach { etf ->
        println("  - ${etf.name} (${etf.ticker}) | ISIN: ${etf.isin}")
    }

    // 2. KODEX 200 ETF 상세정보 조회 (ISIN: KR7069500007)
    println("\n[2] KODEX 200 ETF 상세정보 조회")
    println("-".repeat(80))
    val kodex200Isin = "KR7069500007"
    val detailedInfo = kfc.funds.getDetailedInfo(isin = kodex200Isin)
    if (detailedInfo != null) {
        println("ETF명: ${detailedInfo.name}")
        println("ISIN: ${detailedInfo.isin}")
        println("종가: ${detailedInfo.closePrice}원")
        println("NAV: ${detailedInfo.nav}원")
        println("괴리율: ${detailedInfo.calculateDivergenceRate()}%")
        println("거래량: ${String.format("%,d", detailedInfo.volume)}주")
        println("거래대금: ${String.format("%,d", detailedInfo.tradingValue)}원")
        println("시가총액: ${String.format("%,d", detailedInfo.marketCap)}원")
        println("52주 고가: ${detailedInfo.week52High}원")
        println("52주 저가: ${detailedInfo.week52Low}원")
        println("총 보수: ${detailedInfo.totalFee}%")
    } else {
        println("ETF 상세정보를 찾을 수 없습니다.")
    }

    // 3. KODEX 200 ETF 기본정보 조회
    println("\n[3] KODEX 200 ETF 기본정보 조회")
    println("-".repeat(80))
    val generalInfo = kfc.funds.getGeneralInfo(isin = kodex200Isin)
    if (generalInfo != null) {
        println("ETF명: ${generalInfo.name}")
        println("자산분류: ${generalInfo.assetClassName}")
        println("운용사: ${generalInfo.issuerName}")
        println("지수산출기관: ${generalInfo.indexProviderName}")
        println("상장일: ${generalInfo.listingDate}")
        println("LP: ${generalInfo.lpName}")
    } else {
        println("ETF 기본정보를 찾을 수 없습니다.")
    }

    // 4. KODEX 200 포트폴리오 구성 조회
    println("\n[4] KODEX 200 포트폴리오 구성 조회")
    println("-".repeat(80))
    val portfolio = kfc.funds.getPortfolio(isin = kodex200Isin)
    println("포트폴리오 구성 종목 수: ${portfolio.size}개")
    println("\n상위 10개 구성 종목 (비중 기준):")
    portfolio
        .sortedByDescending { it.weightPercent }
        .take(10)
        .forEachIndexed { index, constituent ->
            println("  ${index + 1}. ${constituent.constituentName} (${constituent.constituentCode})")
            println("     비중: ${constituent.weightPercent}% | CU당 수량: ${constituent.sharesPerCu}주")
        }

    // 5. 포트폴리오 상위 10종목 빠른 조회
    println("\n[5] KODEX 200 포트폴리오 상위 10종목 빠른 조회")
    println("-".repeat(80))
    val portfolioTop10 = kfc.funds.getPortfolioTop10(isin = kodex200Isin)
    println("상위 10 종목:")
    portfolioTop10.forEachIndexed { index, item ->
        println("  ${index + 1}. ${item.name} (${item.ticker})")
        println("     비중: ${item.compositionRatio}% | 구성금액: ${String.format("%,d", item.compositionAmount)}원")
    }

    // 6. KODEX 200 추적 오차 조회 (최근 30일)
    println("\n[6] KODEX 200 추적 오차 조회 (최근 30일)")
    println("-".repeat(80))
    val toDate = LocalDate.now()
    val fromDate = toDate.minusDays(30)
    val trackingErrors = kfc.funds.getTrackingError(
        isin = kodex200Isin,
        fromDate = fromDate,
        toDate = toDate
    )
    println("조회 기간: $fromDate ~ $toDate")
    println("데이터 수: ${trackingErrors.size}건")
    if (trackingErrors.isNotEmpty()) {
        println("\n최근 5일 추적 오차:")
        trackingErrors.takeLast(5).forEach { error ->
            println("  ${error.tradeDate}: ${error.trackingErrorRate}%")
        }
    }

    // 7. KODEX 200 괴리율 조회 (최근 30일)
    println("\n[7] KODEX 200 괴리율 조회 (최근 30일)")
    println("-".repeat(80))
    val divergenceRates = kfc.funds.getDivergenceRate(
        isin = kodex200Isin,
        fromDate = fromDate,
        toDate = toDate
    )
    println("조회 기간: $fromDate ~ $toDate")
    println("데이터 수: ${divergenceRates.size}건")
    if (divergenceRates.isNotEmpty()) {
        println("\n최근 5일 괴리율:")
        divergenceRates.takeLast(5).forEach { rate ->
            println("  ${rate.tradeDate}: ${rate.divergenceRate}% (종가: ${rate.closePrice}원, NAV: ${rate.nav}원)")
        }
    }

    // 8. KODEX 200 공매도 거래 조회 (최근 30일)
    println("\n[8] KODEX 200 공매도 거래 조회 (최근 30일)")
    println("-".repeat(80))
    val shortSelling = kfc.funds.getShortSelling(
        isin = kodex200Isin,
        fromDate = fromDate,
        toDate = toDate,
        type = FundType.ETF
    )
    println("조회 기간: $fromDate ~ $toDate")
    println("데이터 수: ${shortSelling.size}건")
    if (shortSelling.isNotEmpty()) {
        println("\n최근 5일 공매도 거래:")
        shortSelling.takeLast(5).forEach { selling ->
            println("  ${selling.tradeDate}:")
            println("    공매도량: ${String.format("%,d", selling.shortVolume)}주")
            println("    공매도 거래대금: ${String.format("%,d", selling.shortValue)}원")
            println("    공매도 비중: ${selling.shortVolumeRatio}%")
        }
    }

    // 9. KODEX 200 공매도 잔고 조회 (최근 30일)
    println("\n[9] KODEX 200 공매도 잔고 조회 (최근 30일)")
    println("-".repeat(80))
    val shortBalance = kfc.funds.getShortBalance(
        isin = kodex200Isin,
        fromDate = fromDate,
        toDate = toDate,
        type = FundType.ETF
    )
    println("조회 기간: $fromDate ~ $toDate")
    println("데이터 수: ${shortBalance.size}건")
    if (shortBalance.isNotEmpty()) {
        println("\n최근 5일 공매도 잔고:")
        shortBalance.takeLast(5).forEach { balance ->
            println("  ${balance.tradeDate}:")
            println("    잔고량: ${String.format("%,d", balance.shortBalance)}주")
            println("    잔고금액: ${String.format("%,d", balance.shortBalanceValue)}원")
            println("    잔고비율: ${balance.shortBalanceRatio}%")
        }
    }

    // 10. 투자자별 거래 현황 조회 (오늘)
    println("\n[10] KODEX 200 투자자별 거래 현황 조회")
    println("-".repeat(80))
    val investorTrading = kfc.funds.getInvestorTrading(isin = kodex200Isin)
    println("투자자 유형별 거래 현황:")
    investorTrading.forEach { trading ->
        println("  ${trading.investorType}:")
        println("    매수량: ${String.format("%,d", trading.bidVolume)}주")
        println("    매도량: ${String.format("%,d", trading.askVolume)}주")
        println("    순매수: ${String.format("%,d", trading.netBuyVolume)}주")
    }

    println("\n" + "=".repeat(80))
    println("예제 실행 완료!")
    println("=".repeat(80))
}
