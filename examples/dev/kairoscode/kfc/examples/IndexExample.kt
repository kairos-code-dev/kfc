package dev.kairoscode.kfc.examples

import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.index.IndexMarket
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * Index API 사용 예제
 *
 * 이 예제는 KFC 라이브러리의 Index API를 활용하여
 * 지수 관련 데이터를 조회하는 방법을 보여줍니다.
 *
 * 주요 기능:
 * 1. 지수 목록 조회
 * 2. 지수 OHLCV 조회
 * 3. 지수 밸류에이션(PER, PBR, 배당수익률) 조회
 * 4. 지수 구성 종목 조회
 */
fun main() =
    runBlocking {
        // KfcClient 생성
        val kfc = KfcClient.create()

        println("=".repeat(80))
        println("KFC Index API 사용 예제")
        println("=".repeat(80))

        // 1. 전체 지수 목록 조회
        println("\n[1] 전체 지수 목록 조회")
        println("-".repeat(80))
        val allIndices = kfc.index.getIndexList(market = IndexMarket.ALL)
        println("전체 지수 수: ${allIndices.size}개")
        println("\n주요 지수 (상위 10개):")
        allIndices.take(10).forEach { index ->
            println("  - ${index.name} (${index.ticker})")
        }

        // 2. 코스피 지수 기본정보 조회
        println("\n[2] 코스피 지수 기본정보 조회")
        println("-".repeat(80))
        val kospiInfo = kfc.index.getIndexInfo("1001") // 코스피 지수 코드: 1001
        if (kospiInfo != null) {
            println("지수명: ${kospiInfo.name}")
            println("지수코드: ${kospiInfo.ticker}")
            println("기준지수: ${kospiInfo.baseIndex}")
            println("시장: ${kospiInfo.market}")
        } else {
            println("지수 정보를 찾을 수 없습니다.")
        }

        // 3. 지수명 조회
        println("\n[3] 지수명 조회")
        println("-".repeat(80))
        val indexName1001 = kfc.index.getIndexName("1001")
        val indexName1028 = kfc.index.getIndexName("1028") // 코스피 200
        println("1001: $indexName1001")
        println("1028: $indexName1028")

        // 4. 코스피 지수 OHLCV 조회 (최근 30일)
        println("\n[4] 코스피 지수 OHLCV 조회 (최근 30일)")
        println("-".repeat(80))
        val toDate = LocalDate.now()
        val fromDate = toDate.minusDays(30)
        val kospiOhlcv =
            kfc.index.getOhlcvByDate(
                ticker = "1001",
                fromDate = fromDate,
                toDate = toDate,
            )
        println("조회 기간: $fromDate ~ $toDate")
        println("데이터 수: ${kospiOhlcv.size}건")
        if (kospiOhlcv.isNotEmpty()) {
            println("\n최근 5일 OHLCV:")
            kospiOhlcv.takeLast(5).forEach { ohlcv ->
                println("  ${ohlcv.date}:")
                println("    시가: ${ohlcv.open} | 고가: ${ohlcv.high} | 저가: ${ohlcv.low} | 종가: ${ohlcv.close}")
                println(
                    "    거래량: ${String.format("%,d", ohlcv.volume)}주 | 거래대금: ${String.format(
                        "%,d",
                        ohlcv.tradingValue ?: 0L,
                    )}원",
                )
            }
        }

        // 5. 특정 일자 전체 지수 OHLCV 조회
        println("\n[5] 특정 일자 전체 지수 OHLCV 조회 (오늘)")
        println("-".repeat(80))
        val allIndicesOhlcv = kfc.index.getOhlcvByTicker(market = IndexMarket.ALL)
        println("전체 지수 OHLCV 수: ${allIndicesOhlcv.size}개")
        println("\n주요 지수 OHLCV (상위 5개):")
        allIndicesOhlcv.take(5).forEach { ohlcv ->
            println("  ${ohlcv.name}:")
            println("    종가: ${ohlcv.close}")
            println("    거래량: ${String.format("%,d", ohlcv.volume)}주")
        }

        // 6. 코스피 200 지수 밸류에이션 조회 (최근 30일)
        println("\n[6] 코스피 200 지수 밸류에이션 조회 (최근 30일)")
        println("-".repeat(80))
        val kospi200Fundamental =
            kfc.index.getFundamentalByDate(
                ticker = "1028", // 코스피 200
                fromDate = fromDate,
                toDate = toDate,
            )
        println("조회 기간: $fromDate ~ $toDate")
        println("데이터 수: ${kospi200Fundamental.size}건")
        if (kospi200Fundamental.isNotEmpty()) {
            println("\n최근 5일 밸류에이션:")
            kospi200Fundamental.takeLast(5).forEach { fundamental ->
                println("  ${fundamental.date}:")
                println("    PER: ${fundamental.per} | PBR: ${fundamental.pbr} | 배당수익률: ${fundamental.dividendYield}%")
            }
        }

        // 7. 특정 일자 전체 지수 밸류에이션 조회
        println("\n[7] 특정 일자 전체 지수 밸류에이션 조회 (오늘)")
        println("-".repeat(80))
        val allIndicesFundamental = kfc.index.getFundamentalByTicker(market = IndexMarket.KOSPI)
        println("코스피 지수 밸류에이션 수: ${allIndicesFundamental.size}개")
        println("\n주요 지수 밸류에이션 (상위 5개):")
        allIndicesFundamental.take(5).forEach { fundamental ->
            println("  ${fundamental.name}:")
            println("    PER: ${fundamental.per} | PBR: ${fundamental.pbr} | 배당수익률: ${fundamental.dividendYield}%")
        }

        // 8. 코스피 200 구성 종목 조회
        println("\n[8] 코스피 200 구성 종목 조회")
        println("-".repeat(80))
        val kospi200Constituents = kfc.index.getIndexConstituents("1028")
        println("코스피 200 구성 종목 수: ${kospi200Constituents.size}개")
        println("\n상위 10개 구성 종목:")
        kospi200Constituents.take(10).forEach { ticker ->
            println("  - $ticker")
        }

        // 9. 지수 등락률 조회 (최근 7일)
        println("\n[9] 지수 등락률 조회 (최근 7일)")
        println("-".repeat(80))
        val weekFromDate = toDate.minusDays(7)
        val priceChanges =
            kfc.index.getPriceChange(
                fromDate = weekFromDate,
                toDate = toDate,
                market = IndexMarket.ALL,
            )
        println("조회 기간: $weekFromDate ~ $toDate")
        println("전체 지수 수: ${priceChanges.size}개")
        println("\n주요 지수 등락률 (상위 10개):")
        priceChanges
            .take(10)
            .forEach { change ->
                println("  ${change.name}:")
                println("    시작가: ${change.openPrice} | 종가: ${change.closePrice}")
                println("    등락률: ${change.changeRate}%")
            }

        // 10. 여러 지수 비교 분석
        println("\n[10] 주요 지수 비교 분석 (오늘)")
        println("-".repeat(80))
        val majorIndices = listOf("1001", "1028", "2001") // 코스피, 코스피 200, 코스닥
        println("주요 지수 비교:")
        majorIndices.forEach { ticker ->
            val indexInfo = kfc.index.getIndexInfo(ticker)
            val ohlcvList =
                kfc.index.getOhlcvByDate(
                    ticker = ticker,
                    fromDate = toDate,
                    toDate = toDate,
                )
            val fundamental =
                kfc.index.getFundamentalByDate(
                    ticker = ticker,
                    fromDate = toDate,
                    toDate = toDate,
                )

            if (indexInfo != null && ohlcvList.isNotEmpty()) {
                val ohlcv = ohlcvList.first()
                val fund = fundamental.firstOrNull()
                println("\n  ${indexInfo.name} (${indexInfo.ticker}):")
                println("    종가: ${ohlcv.close}")
                if (fund != null) {
                    println("    PER: ${fund.per} | PBR: ${fund.pbr} | 배당수익률: ${fund.dividendYield}%")
                }
                println("    거래량: ${String.format("%,d", ohlcv.volume)}주")
                println("    거래대금: ${String.format("%,d", ohlcv.tradingValue ?: 0L)}원")
            }
        }

        println("\n" + "=".repeat(80))
        println("예제 실행 완료!")
        println("=".repeat(80))
    }
