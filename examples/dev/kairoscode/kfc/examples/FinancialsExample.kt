package dev.kairoscode.kfc.examples

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.financials.*
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Financials API 사용 예제
 *
 * 이 예제는 KFC 라이브러리의 Financials API와 Corp API를 활용하여
 * 재무제표 및 기업 공시 데이터를 조회하는 방법을 보여줍니다.
 *
 * 주요 기능:
 * 1. OPENDART 고유번호 조회
 * 2. 손익계산서 조회
 * 3. 재무상태표 조회
 * 4. 현금흐름표 조회
 * 5. 전체 재무제표 한 번에 조회
 * 6. 배당 정보 조회
 * 7. 공시 검색
 *
 * 주의사항:
 * - OPENDART API Key가 필요합니다
 * - 환경변수 OPENDART_API_KEY 설정 필요
 * - 일일 요청 제한: 20,000건
 * - 2015년 이후 데이터만 지원
 */
fun main() = runBlocking {
    // OPENDART API Key 환경변수에서 가져오기
    val apiKey = System.getenv("OPENDART_API_KEY")
    if (apiKey.isNullOrBlank()) {
        println("ERROR: OPENDART_API_KEY 환경변수가 설정되지 않았습니다.")
        println("다음 명령어로 환경변수를 설정하세요:")
        println("export OPENDART_API_KEY='your-api-key'")
        return@runBlocking
    }

    // KfcClient 생성 (OPENDART API Key 포함)
    val kfc = KfcClient.create(opendartApiKey = apiKey)

    // Corp API와 Financials API가 null이 아닌지 확인
    val corpApi = kfc.corp
    val financialsApi = kfc.financials
    if (corpApi == null || financialsApi == null) {
        println("ERROR: Corp API 또는 Financials API를 사용할 수 없습니다.")
        return@runBlocking
    }

    println("=".repeat(80))
    println("KFC Financials API 사용 예제")
    println("=".repeat(80))

    // 1. OPENDART 고유번호 조회
    println("\n[1] OPENDART 고유번호 조회")
    println("-".repeat(80))
    val corpCodeList = corpApi.getCorpCodeList()
    println("전체 법인 수: ${corpCodeList.size}개")

    // 삼성전자 고유번호 찾기
    val samsungCorp = corpCodeList.find { it.stockCode == "005930" }
    if (samsungCorp == null) {
        println("ERROR: 삼성전자(005930) 고유번호를 찾을 수 없습니다.")
        return@runBlocking
    }
    println("\n삼성전자 정보:")
    println("  종목코드: ${samsungCorp.stockCode}")
    println("  법인명: ${samsungCorp.corpName}")
    println("  고유번호: ${samsungCorp.corpCode}")

    // 카카오 고유번호 찾기
    val kakaoCorp = corpCodeList.find { it.stockCode == "035720" }
    if (kakaoCorp == null) {
        println("ERROR: 카카오(035720) 고유번호를 찾을 수 없습니다.")
        return@runBlocking
    }
    println("\n카카오 정보:")
    println("  종목코드: ${kakaoCorp.stockCode}")
    println("  법인명: ${kakaoCorp.corpName}")
    println("  고유번호: ${kakaoCorp.corpCode}")

    // 2. 삼성전자 손익계산서 조회 (2023년)
    println("\n[2] 삼성전자 손익계산서 조회 (2023년)")
    println("-".repeat(80))
    val incomeStatement = financialsApi.getIncomeStatement(
        corpCode = samsungCorp.corpCode,
        year = 2023,
        reportType = ReportType.ANNUAL,
        statementType = StatementType.CONSOLIDATED
    )
    println("보고서 종류: ${incomeStatement.reportType}")
    println("재무제표 구분: ${incomeStatement.statementType}")
    println("계정과목 수: ${incomeStatement.lineItems.size}개")
    println("\n주요 항목:")
    incomeStatement.getRevenue()?.let { println("  매출액: ${String.format("%,d", it.toLong())}원") }
    incomeStatement.getOperatingIncome()?.let { println("  영업이익: ${String.format("%,d", it.toLong())}원") }
    incomeStatement.getNetIncome()?.let { println("  당기순이익: ${String.format("%,d", it.toLong())}원") }

    // 영업이익률 계산
    incomeStatement.calculateOperatingMargin()?.let {
        println("  영업이익률: ${String.format("%.2f", it)}%")
    }

    // 3. 삼성전자 재무상태표 조회 (2023년)
    println("\n[3] 삼성전자 재무상태표 조회 (2023년)")
    println("-".repeat(80))
    val balanceSheet = financialsApi.getBalanceSheet(
        corpCode = samsungCorp.corpCode,
        year = 2023,
        reportType = ReportType.ANNUAL,
        statementType = StatementType.CONSOLIDATED
    )
    println("보고서 종류: ${balanceSheet.reportType}")
    println("재무제표 구분: ${balanceSheet.statementType}")
    println("계정과목 수: ${balanceSheet.lineItems.size}개")
    println("\n주요 항목:")
    balanceSheet.getTotalAssets()?.let { println("  자산총계: ${String.format("%,d", it.toLong())}원") }
    balanceSheet.getTotalLiabilities()?.let { println("  부채총계: ${String.format("%,d", it.toLong())}원") }
    balanceSheet.getTotalEquity()?.let { println("  자본총계: ${String.format("%,d", it.toLong())}원") }
    balanceSheet.getCurrentAssets()?.let { println("  유동자산: ${String.format("%,d", it.toLong())}원") }
    balanceSheet.getNoncurrentAssets()?.let { println("  비유동자산: ${String.format("%,d", it.toLong())}원") }

    // 재무비율 계산
    println("\n재무비율:")
    balanceSheet.calculateDebtToEquityRatio()?.let {
        println("  부채비율: ${String.format("%.2f", it)}%")
    }
    balanceSheet.calculateCurrentRatio()?.let {
        println("  유동비율: ${String.format("%.2f", it)}%")
    }

    // 4. 삼성전자 현금흐름표 조회 (2023년)
    println("\n[4] 삼성전자 현금흐름표 조회 (2023년)")
    println("-".repeat(80))
    val cashFlowStatement = financialsApi.getCashFlowStatement(
        corpCode = samsungCorp.corpCode,
        year = 2023,
        reportType = ReportType.ANNUAL,
        statementType = StatementType.CONSOLIDATED
    )
    println("보고서 종류: ${cashFlowStatement.reportType}")
    println("재무제표 구분: ${cashFlowStatement.statementType}")
    println("계정과목 수: ${cashFlowStatement.lineItems.size}개")
    println("\n주요 항목:")
    cashFlowStatement.getOperatingCashFlow()?.let {
        println("  영업활동 현금흐름: ${String.format("%,d", it.toLong())}원")
    }
    cashFlowStatement.getInvestingCashFlow()?.let {
        println("  투자활동 현금흐름: ${String.format("%,d", it.toLong())}원")
    }
    cashFlowStatement.getFinancingCashFlow()?.let {
        println("  재무활동 현금흐름: ${String.format("%,d", it.toLong())}원")
    }

    // 5. 카카오 전체 재무제표 한 번에 조회 (2023년)
    println("\n[5] 카카오 전체 재무제표 한 번에 조회 (2023년)")
    println("-".repeat(80))
    val allFinancials = financialsApi.getAllFinancials(
        corpCode = kakaoCorp.corpCode,
        year = 2023,
        reportType = ReportType.ANNUAL,
        statementType = StatementType.CONSOLIDATED
    )
    println("법인명: ${kakaoCorp.corpName}")
    println("사업연도: ${allFinancials.fiscalYear}")

    allFinancials.incomeStatement?.let { is_ ->
        println("\n손익계산서:")
        is_.getRevenue()?.let { println("  매출액: ${String.format("%,d", it.toLong())}원") }
        is_.getOperatingIncome()?.let { println("  영업이익: ${String.format("%,d", it.toLong())}원") }
        is_.getNetIncome()?.let { println("  당기순이익: ${String.format("%,d", it.toLong())}원") }
    }

    allFinancials.balanceSheet?.let { bs ->
        println("\n재무상태표:")
        bs.getTotalAssets()?.let { println("  자산총계: ${String.format("%,d", it.toLong())}원") }
        bs.getTotalLiabilities()?.let { println("  부채총계: ${String.format("%,d", it.toLong())}원") }
        bs.getTotalEquity()?.let { println("  자본총계: ${String.format("%,d", it.toLong())}원") }
    }

    allFinancials.cashFlowStatement?.let { cf ->
        println("\n현금흐름표:")
        cf.getOperatingCashFlow()?.let { println("  영업활동 현금흐름: ${String.format("%,d", it.toLong())}원") }
        cf.getInvestingCashFlow()?.let { println("  투자활동 현금흐름: ${String.format("%,d", it.toLong())}원") }
        cf.getFinancingCashFlow()?.let { println("  재무활동 현금흐름: ${String.format("%,d", it.toLong())}원") }
    }

    // 6. 삼성전자 배당 정보 조회 (2023년)
    println("\n[6] 삼성전자 배당 정보 조회 (2023년)")
    println("-".repeat(80))
    val dividendInfo = corpApi.getDividendInfo(
        corpCode = samsungCorp.corpCode,
        year = 2023
    )
    if (dividendInfo.isNotEmpty()) {
        println("배당 정보:")
        dividendInfo.forEach { info ->
            println("  ${info.dividendType} (${info.stockKind}):")
            println("    당기 배당금: ${info.currentYear ?: "-"}원")
            println("    전기 배당금: ${info.previousYear ?: "-"}원")
            println("    결산기준일: ${info.settlementDate}")
        }
    } else {
        println("배당 정보가 없습니다.")
    }

    // 7. 삼성전자 최근 공시 검색 (최근 7일)
    println("\n[7] 삼성전자 최근 공시 검색 (최근 7일)")
    println("-".repeat(80))
    val endDate = LocalDate.now()
    val startDate = endDate.minusDays(7)
    val disclosures = corpApi.searchDisclosures(
        corpCode = samsungCorp.corpCode,
        startDate = startDate,
        endDate = endDate,
        pageNo = 1,
        pageCount = 10
    )
    println("조회 기간: $startDate ~ $endDate")
    println("공시 건수: ${disclosures.size}건")
    if (disclosures.isNotEmpty()) {
        println("\n최근 공시:")
        disclosures.take(5).forEach { disclosure ->
            println("\n  [${disclosure.rceptDate}]")
            println("  제목: ${disclosure.reportName}")
            println("  제출인: ${disclosure.filerName}")
            println("  접수번호: ${disclosure.rceptNo}")
        }
    }

    // 8. 여러 기업 재무제표 비교 (2023년)
    println("\n[8] 여러 기업 재무제표 비교 (2023년)")
    println("-".repeat(80))
    val companies = listOf(
        "005930" to "삼성전자",
        "035720" to "카카오",
        "000660" to "SK하이닉스"
    )

    println("기업별 재무 지표 비교:\n")
    companies.forEach { (ticker, name) ->
        val corp = corpCodeList.find { it.stockCode == ticker }
        if (corp != null) {
            try {
                val financials = financialsApi.getAllFinancials(
                    corpCode = corp.corpCode,
                    year = 2023,
                    reportType = ReportType.ANNUAL,
                    statementType = StatementType.CONSOLIDATED
                )

                val is_ = financials.incomeStatement
                val bs = financials.balanceSheet

                println("$name (${corp.stockCode}):")

                is_?.let {
                    it.getRevenue()?.let { rev -> println("  매출액: ${String.format("%,d", rev.toLong())}원") }
                    it.calculateOperatingMargin()?.let { margin ->
                        println("  영업이익률: ${String.format("%.2f", margin)}%")
                    }
                    it.calculateNetMargin()?.let { margin ->
                        println("  순이익률: ${String.format("%.2f", margin)}%")
                    }
                }

                bs?.let {
                    it.calculateDebtToEquityRatio()?.let { ratio ->
                        println("  부채비율: ${String.format("%.2f", ratio)}%")
                    }
                }

                println()
            } catch (e: Exception) {
                println("$name (${corp.stockCode}): 데이터 조회 실패 - ${e.message}\n")
            }
        }
    }

    println("=".repeat(80))
    println("예제 실행 완료!")
    println("=".repeat(80))
}
