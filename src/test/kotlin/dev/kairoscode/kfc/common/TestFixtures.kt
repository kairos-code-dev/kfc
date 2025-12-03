package dev.kairoscode.kfc.common

import java.time.LocalDate

/**
 * 테스트용 공통 데이터 픽스처
 *
 * 유닛 테스트와 통합 테스트에서 공통으로 사용하는 테스트 데이터를 정의합니다.
 *
 * ## 구성
 * - **Etf**: ETF 관련 ISIN 및 티커 코드
 * - **Corp**: 기업 관련 법인 코드 및 종목 코드
 * - **Dates**: 테스트용 고정 날짜 (재현성 보장)
 * - **Invalid**: 유효성 검증 테스트용 무효한 입력값
 * - **Boundary**: 경계값 테스트용 데이터
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
object TestFixtures {

    // =========================================
    // ETF 관련 데이터
    // =========================================
    object Etf {
        // === ISIN 코드 (12자리) ===
        /** TIGER 200 ETF ISIN */
        const val TIGER_200_ISIN = "KR7102110004"

        /** KODEX 200 ETF ISIN */
        const val KODEX_200_ISIN = "KR7069500007"

        /** TIGER 미국나스닥100 ETF ISIN */
        const val TIGER_NASDAQ100_ISIN = "KR7133690008"

        // === 티커 코드 (6자리) ===
        /** TIGER 200 ETF 티커 */
        const val TIGER_200_TICKER = "102110"

        /** KODEX 200 ETF 티커 */
        const val KODEX_200_TICKER = "069500"

        /** TIGER 미국나스닥100 ETF 티커 */
        const val TIGER_NASDAQ100_TICKER = "133690"
    }

    // =========================================
    // 기업 관련 데이터
    // =========================================
    object Corp {
        // === 법인 코드 (8자리) ===
        /** 삼성전자 법인 코드 */
        const val SAMSUNG_CORP_CODE = "00126380"

        /** 카카오 법인 코드 */
        const val KAKAO_CORP_CODE = "00222206"

        // === 종목 코드 (6자리) ===
        /** 삼성전자 종목 코드 */
        const val SAMSUNG_STOCK_CODE = "005930"
    }

    // =========================================
    // 날짜 관련 데이터 (테스트 재현성 보장)
    // =========================================
    object Dates {
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

        /** 미래 날짜 - 유효성 검증 테스트용 */
        val FUTURE_DATE: LocalDate = LocalDate.now().plusDays(1)
    }

    // =========================================
    // 무효한 입력 데이터 (에러 테스트용)
    // =========================================
    object Invalid {
        // === 무효한 ISIN ===
        /** 너무 짧은 ISIN */
        const val TOO_SHORT_ISIN = "KR7102"

        /** 너무 긴 ISIN */
        const val TOO_LONG_ISIN = "KR71021100041234"

        /** 잘못된 접두사 (KR이 아닌 US) */
        const val WRONG_PREFIX_ISIN = "US7102110004"

        /** 빈 문자열 */
        const val EMPTY_STRING = ""

        /** 공백만 있는 문자열 */
        const val BLANK_STRING = "   "

        /** 특수문자 포함 */
        const val WITH_SPECIAL_CHARS = "KR7!@#$%^&*()"
    }

    // =========================================
    // 경계값 테스트 데이터
    // =========================================
    object Boundary {
        /** 최소 날짜 범위 (하루) */
        const val MIN_DATE_RANGE_DAYS = 1

        /** 최대 날짜 범위 (1년) */
        const val MAX_DATE_RANGE_DAYS = 365
    }
}
