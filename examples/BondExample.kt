package dev.kairoscode.kfc.examples

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.bond.BondType
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Bond API 사용 예제
 *
 * 이 예제는 KFC 라이브러리의 Bond API를 활용하여
 * 채권 수익률 데이터를 조회하는 방법을 보여줍니다.
 *
 * 주요 기능:
 * 1. 특정 일자 전체 채권 수익률 조회
 * 2. 특정 채권의 기간별 수익률 추이 조회
 * 3. 수익률 곡선(Yield Curve) 분석
 * 4. 국고채-회사채 스프레드 분석
 */
fun main() = runBlocking {
    // KfcClient 생성
    val kfc = KfcClient.create()

    println("=".repeat(80))
    println("KFC Bond API 사용 예제")
    println("=".repeat(80))

    // 1. 오늘의 전체 채권 수익률 조회
    println("\n[1] 오늘의 전체 채권 수익률 조회")
    println("-".repeat(80))
    val todayYields = kfc.bond.getBondYieldsByDate()
    println("조회 날짜: ${todayYields.date}")
    println("\n전체 채권 수익률:")
    println("국고채:")
    println("  1년:  ${todayYields.treasury1Y.yield}% (전일대비: ${todayYields.treasury1Y.change}%)")
    println("  2년:  ${todayYields.treasury2Y.yield}% (전일대비: ${todayYields.treasury2Y.change}%)")
    println("  3년:  ${todayYields.treasury3Y.yield}% (전일대비: ${todayYields.treasury3Y.change}%)")
    println("  5년:  ${todayYields.treasury5Y.yield}% (전일대비: ${todayYields.treasury5Y.change}%)")
    println("  10년: ${todayYields.treasury10Y.yield}% (전일대비: ${todayYields.treasury10Y.change}%)")
    println("  20년: ${todayYields.treasury20Y.yield}% (전일대비: ${todayYields.treasury20Y.change}%)")
    println("  30년: ${todayYields.treasury30Y.yield}% (전일대비: ${todayYields.treasury30Y.change}%)")
    println("\n특수채:")
    println("  국민주택 1종 5년: ${todayYields.housing5Y.yield}% (전일대비: ${todayYields.housing5Y.change}%)")
    println("\n회사채:")
    println("  AA- 3년:  ${todayYields.corporateAA.yield}% (전일대비: ${todayYields.corporateAA.change}%)")
    println("  BBB- 3년: ${todayYields.corporateBBB.yield}% (전일대비: ${todayYields.corporateBBB.change}%)")
    println("\n단기금융:")
    println("  CD 91일: ${todayYields.cd91.yield}% (전일대비: ${todayYields.cd91.change}%)")

    // 2. 국고채 10년물 수익률 추이 조회 (최근 90일)
    println("\n[2] 국고채 10년물 수익률 추이 조회 (최근 90일)")
    println("-".repeat(80))
    val toDate = LocalDate.now()
    val fromDate = toDate.minusDays(90)
    val treasury10YHistory = kfc.bond.getBondYields(
        bondType = BondType.TREASURY_10Y,
        fromDate = fromDate,
        toDate = toDate
    )
    println("조회 기간: $fromDate ~ $toDate")
    println("데이터 수: ${treasury10YHistory.size}건")
    if (treasury10YHistory.isNotEmpty()) {
        println("\n최근 10일 수익률:")
        treasury10YHistory.takeLast(10).forEach { bond ->
            println("  ${bond.date}: ${bond.yield}% (전일대비: ${bond.change}%)")
        }

        // 통계 계산
        val yields = treasury10YHistory.map { it.yield }
        val avgYield = yields.average()
        val maxYield = yields.maxOrNull() ?: 0.0
        val minYield = yields.minOrNull() ?: 0.0

        println("\n통계 (최근 90일):")
        println("  평균: ${String.format("%.3f", avgYield)}%")
        println("  최고: ${String.format("%.3f", maxYield)}%")
        println("  최저: ${String.format("%.3f", minYield)}%")
        println("  변동폭: ${String.format("%.3f", maxYield - minYield)}%")
    }

    // 3. 국고채 수익률 곡선 (Yield Curve) 분석
    println("\n[3] 국고채 수익률 곡선 분석 (오늘)")
    println("-".repeat(80))
    val yieldCurve = mapOf(
        "1년" to todayYields.treasury1Y.yield,
        "2년" to todayYields.treasury2Y.yield,
        "3년" to todayYields.treasury3Y.yield,
        "5년" to todayYields.treasury5Y.yield,
        "10년" to todayYields.treasury10Y.yield,
        "20년" to todayYields.treasury20Y.yield,
        "30년" to todayYields.treasury30Y.yield
    )
    println("국고채 수익률 곡선:")
    yieldCurve.forEach { (maturity, yield) ->
        println("  $maturity: ${String.format("%.3f", yield)}%")
    }

    // 수익률 곡선 형태 분석
    val shortTermYield = todayYields.treasury1Y.yield
    val midTermYield = todayYields.treasury5Y.yield
    val longTermYield = todayYields.treasury10Y.yield

    val curve = when {
        longTermYield > midTermYield && midTermYield > shortTermYield -> "정상형 (Normal)"
        longTermYield < shortTermYield -> "역전형 (Inverted)"
        else -> "평탄형 (Flat)"
    }
    println("\n수익률 곡선 형태: $curve")
    println("  단기(1년): ${String.format("%.3f", shortTermYield)}%")
    println("  중기(5년): ${String.format("%.3f", midTermYield)}%")
    println("  장기(10년): ${String.format("%.3f", longTermYield)}%")

    // 4. 회사채 스프레드 분석
    println("\n[4] 회사채 스프레드 분석 (오늘)")
    println("-".repeat(80))
    val treasury3Y = todayYields.treasury3Y.yield
    val corporateAA = todayYields.corporateAA.yield
    val corporateBBB = todayYields.corporateBBB.yield

    val aaSpread = corporateAA - treasury3Y
    val bbbSpread = corporateBBB - treasury3Y
    val creditSpread = corporateBBB - corporateAA

    println("3년물 기준 스프레드:")
    println("  국고채 3년: ${String.format("%.3f", treasury3Y)}%")
    println("  회사채 AA- 3년: ${String.format("%.3f", corporateAA)}% (스프레드: ${String.format("%.3f", aaSpread)}%p)")
    println("  회사채 BBB- 3년: ${String.format("%.3f", corporateBBB)}% (스프레드: ${String.format("%.3f", bbbSpread)}%p)")
    println("\n신용 스프레드 (BBB- - AA-):")
    println("  ${String.format("%.3f", creditSpread)}%p")

    // 5. 여러 채권의 수익률 비교 (최근 30일)
    println("\n[5] 여러 채권의 수익률 비교 (최근 30일)")
    println("-".repeat(80))
    val thirtyDaysAgo = toDate.minusDays(30)
    val bondsToCompare = listOf(
        BondType.TREASURY_1Y,
        BondType.TREASURY_3Y,
        BondType.TREASURY_10Y,
        BondType.CORPORATE_AA
    )

    println("조회 기간: $thirtyDaysAgo ~ $toDate")
    bondsToCompare.forEach { bondType ->
        val history = kfc.bond.getBondYields(
            bondType = bondType,
            fromDate = thirtyDaysAgo,
            toDate = toDate
        )
        if (history.isNotEmpty()) {
            val firstYield = history.first().yield
            val lastYield = history.last().yield
            val change = lastYield - firstYield

            println("\n${bondType.displayName}:")
            println("  시작 수익률: ${String.format("%.3f", firstYield)}%")
            println("  종료 수익률: ${String.format("%.3f", lastYield)}%")
            println("  변동: ${String.format("%+.3f", change)}%p")
        }
    }

    // 6. CD 금리와 국고채 1년물 비교 (최근 60일)
    println("\n[6] CD 금리와 국고채 1년물 비교 (최근 60일)")
    println("-".repeat(80))
    val sixtyDaysAgo = toDate.minusDays(60)
    val cdHistory = kfc.bond.getBondYields(
        bondType = BondType.CD_91,
        fromDate = sixtyDaysAgo,
        toDate = toDate
    )
    val treasury1YHistory = kfc.bond.getBondYields(
        bondType = BondType.TREASURY_1Y,
        fromDate = sixtyDaysAgo,
        toDate = toDate
    )

    println("조회 기간: $sixtyDaysAgo ~ $toDate")
    println("\n최근 5일 비교:")
    val lastFiveDates = cdHistory.takeLast(5).map { it.date }
    lastFiveDates.forEach { date ->
        val cdYield = cdHistory.find { it.date == date }?.yield
        val treasuryYield = treasury1YHistory.find { it.date == date }?.yield

        if (cdYield != null && treasuryYield != null) {
            val spread = cdYield - treasuryYield
            println("  $date:")
            println("    CD 91일: ${String.format("%.3f", cdYield)}%")
            println("    국고채 1년: ${String.format("%.3f", treasuryYield)}%")
            println("    스프레드: ${String.format("%+.3f", spread)}%p")
        }
    }

    println("\n" + "=".repeat(80))
    println("예제 실행 완료!")
    println("=".repeat(80))
}
