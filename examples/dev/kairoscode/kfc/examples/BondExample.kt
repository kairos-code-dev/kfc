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

    // 국고채 수익률
    val treasury1Y = todayYields.getYieldByType(BondType.TREASURY_1Y)
    val treasury2Y = todayYields.getYieldByType(BondType.TREASURY_2Y)
    val treasury3Y = todayYields.getYieldByType(BondType.TREASURY_3Y)
    val treasury5Y = todayYields.getYieldByType(BondType.TREASURY_5Y)
    val treasury10Y = todayYields.getYieldByType(BondType.TREASURY_10Y)
    val treasury20Y = todayYields.getYieldByType(BondType.TREASURY_20Y)
    val treasury30Y = todayYields.getYieldByType(BondType.TREASURY_30Y)

    // 특수채
    val housing5Y = todayYields.getYieldByType(BondType.HOUSING_5Y)

    // 회사채
    val corporateAA = todayYields.getYieldByType(BondType.CORPORATE_AA)
    val corporateBBB = todayYields.getYieldByType(BondType.CORPORATE_BBB)

    // CD
    val cd91 = todayYields.getYieldByType(BondType.CD_91)

    println("\n전체 채권 수익률:")
    println("국고채:")
    treasury1Y?.let { println("  1년:  ${it.yield}% (전일대비: ${it.change}bp)") }
    treasury2Y?.let { println("  2년:  ${it.yield}% (전일대비: ${it.change}bp)") }
    treasury3Y?.let { println("  3년:  ${it.yield}% (전일대비: ${it.change}bp)") }
    treasury5Y?.let { println("  5년:  ${it.yield}% (전일대비: ${it.change}bp)") }
    treasury10Y?.let { println("  10년: ${it.yield}% (전일대비: ${it.change}bp)") }
    treasury20Y?.let { println("  20년: ${it.yield}% (전일대비: ${it.change}bp)") }
    treasury30Y?.let { println("  30년: ${it.yield}% (전일대비: ${it.change}bp)") }
    println("\n특수채:")
    housing5Y?.let { println("  국민주택 1종 5년: ${it.yield}% (전일대비: ${it.change}bp)") }
    println("\n회사채:")
    corporateAA?.let { println("  AA- 3년:  ${it.yield}% (전일대비: ${it.change}bp)") }
    corporateBBB?.let { println("  BBB- 3년: ${it.yield}% (전일대비: ${it.change}bp)") }
    println("\n단기금융:")
    cd91?.let { println("  CD 91일: ${it.yield}% (전일대비: ${it.change}bp)") }

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
            println("  ${bond.date}: ${bond.yield}% (전일대비: ${bond.change}bp)")
        }

        // 통계 계산
        val yields = treasury10YHistory.map { it.yield.toDouble() }
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
    val yieldCurve = listOf(
        "1년" to treasury1Y?.yield,
        "2년" to treasury2Y?.yield,
        "3년" to treasury3Y?.yield,
        "5년" to treasury5Y?.yield,
        "10년" to treasury10Y?.yield,
        "20년" to treasury20Y?.yield,
        "30년" to treasury30Y?.yield
    )
    println("국고채 수익률 곡선:")
    yieldCurve.forEach { (maturity, yieldVal) ->
        yieldVal?.let { println("  $maturity: $it%") }
    }

    // 수익률 곡선 형태 분석
    val shortTermYield = treasury1Y?.yield?.toDouble() ?: 0.0
    val midTermYield = treasury5Y?.yield?.toDouble() ?: 0.0
    val longTermYield = treasury10Y?.yield?.toDouble() ?: 0.0

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
    val treasury3YVal = treasury3Y?.yield?.toDouble() ?: 0.0
    val corporateAAVal = corporateAA?.yield?.toDouble() ?: 0.0
    val corporateBBBVal = corporateBBB?.yield?.toDouble() ?: 0.0

    val aaSpread = corporateAAVal - treasury3YVal
    val bbbSpread = corporateBBBVal - treasury3YVal
    val creditSpread = corporateBBBVal - corporateAAVal

    println("3년물 기준 스프레드:")
    println("  국고채 3년: ${String.format("%.3f", treasury3YVal)}%")
    println("  회사채 AA- 3년: ${String.format("%.3f", corporateAAVal)}% (스프레드: ${String.format("%.3f", aaSpread)}%p)")
    println("  회사채 BBB- 3년: ${String.format("%.3f", corporateBBBVal)}% (스프레드: ${String.format("%.3f", bbbSpread)}%p)")
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
            val firstYield = history.first().yield.toDouble()
            val lastYield = history.last().yield.toDouble()
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
        val cdYield = cdHistory.find { it.date == date }?.yield?.toDouble()
        val treasuryYield = treasury1YHistory.find { it.date == date }?.yield?.toDouble()

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
