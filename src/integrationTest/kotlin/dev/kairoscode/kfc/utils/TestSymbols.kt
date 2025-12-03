package dev.kairoscode.kfc.utils

import java.time.LocalDate

/**
 * 테스트용 ETF 및 종목 코드 상수
 *
 * Live Test와 Unit Test에서 일관되게 사용할 수 있는
 * 실제 거래되는 ETF 및 종목 코드를 정의합니다.
 *
 * ## ETF 선정 기준
 * - TIGER 200: 거래량이 많고 안정적인 국내 대표 ETF
 * - KODEX 200: 국내 최대 ETF, 높은 유동성
 * - TIGER 미국나스닥100: 해외 지수 추종 ETF
 *
 * ## 종목 선정 기준
 * - 삼성전자: 한국 대표 기업, 공시 데이터 풍부
 * - 카카오: IT 대표 기업, 활발한 공시 이력
 *
 * ## 고정 날짜 선정 기준
 * - 거래일/비거래일: 2024년 11월 데이터 (안정적인 과거 데이터)
 * - 공휴일 없는 기간 선택으로 일관성 보장
 */
object TestSymbols {
    // ================================
    // ETF ISIN 코드 (12자리)
    // ================================
    /** TIGER 200 ETF ISIN */
    const val TIGER_200_ISIN = "KR7102110004"

    /** KODEX 200 ETF ISIN */
    const val KODEX_200_ISIN = "KR7069500007"

    /** TIGER 미국나스닥100 ETF ISIN */
    const val TIGER_NASDAQ100_ISIN = "KR7133690008"

    // ================================
    // ETF 티커 (6자리)
    // ================================
    /** TIGER 200 ETF 티커 */
    const val TIGER_200_TICKER = "102110"

    /** KODEX 200 ETF 티커 */
    const val KODEX_200_TICKER = "069500"

    /** TIGER 미국나스닥100 ETF 티커 */
    const val TIGER_NASDAQ100_TICKER = "133690"

    // ================================
    // 법인 코드 (8자리)
    // ================================
    /** 삼성전자 법인 코드 */
    const val SAMSUNG_CORP_CODE = "00126380"

    /** 카카오 법인 코드 */
    const val KAKAO_CORP_CODE = "00222206"

    // ================================
    // 고정 날짜 (테스트 재현성 보장)
    // ================================
    /** 거래일 (2024-11-25, 월요일) - 안정적인 과거 거래일 */
    val TRADING_DAY: LocalDate = LocalDate.of(2024, 11, 25)

    /** 비거래일 (2024-11-23, 토요일) - 주말, 거래 없음 */
    val NON_TRADING_DAY: LocalDate = LocalDate.of(2024, 11, 23)

    /** 1개월 전 날짜 (2024-10-25) - 기간 테스트용 */
    val ONE_MONTH_AGO: LocalDate = LocalDate.of(2024, 10, 25)

    /** 3개월 전 날짜 (2024-08-25) - 기간 테스트용 */
    val THREE_MONTHS_AGO: LocalDate = LocalDate.of(2024, 8, 25)

    /** 1년 전 날짜 (2023-11-25) - 기간 테스트용 */
    val ONE_YEAR_AGO: LocalDate = LocalDate.of(2023, 11, 25)
}
