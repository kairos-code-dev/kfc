package dev.kairoscode.kfc.utils

import java.time.LocalDate

/**
 * 테스트에서 사용하는 공통 테스트 데이터
 *
 * 의미있는 이름을 사용하여 테스트의 가독성을 높입니다.
 * 네이밍 규칙: [유형]_[특성]
 * - valid: 유효한 입력
 * - invalid: 무효한 입력
 * - boundary: 경계값
 */
object TestData {

    // =========================================
    // 유효한 ETF 데이터
    // =========================================
    object ValidEtf {
        /** TIGER 200 ETF ISIN */
        const val TIGER_200_ISIN = "KR7102110004"

        /** KODEX 200 ETF ISIN */
        const val KODEX_200_ISIN = "KR7069500007"

        /** TIGER 200 티커 */
        const val TIGER_200_TICKER = "102110"

        /** KODEX 200 티커 */
        const val KODEX_200_TICKER = "069500"
    }

    // =========================================
    // 무효한 ISIN 데이터
    // =========================================
    object InvalidIsin {
        /** 너무 짧은 ISIN */
        const val TOO_SHORT = "KR7102"

        /** 너무 긴 ISIN */
        const val TOO_LONG = "KR71021100041234"

        /** 잘못된 접두사 */
        const val WRONG_PREFIX = "US7102110004"

        /** 빈 문자열 */
        const val EMPTY = ""

        /** 공백만 있는 문자열 */
        const val BLANK = "   "

        /** 특수문자 포함 */
        const val WITH_SPECIAL_CHARS = "KR7!@#$%^&*()"
    }

    // =========================================
    // 테스트 날짜
    // =========================================
    object TestDates {
        /** 거래일 (2024년 11월 25일, 월요일) */
        val TRADING_DAY: LocalDate = LocalDate.of(2024, 11, 25)

        /** 비거래일 (2024년 11월 23일, 토요일) */
        val NON_TRADING_DAY: LocalDate = LocalDate.of(2024, 11, 23)

        /** 1개월 전 */
        val ONE_MONTH_AGO: LocalDate = LocalDate.of(2024, 10, 25)

        /** 3개월 전 */
        val THREE_MONTHS_AGO: LocalDate = LocalDate.of(2024, 8, 25)

        /** 1년 전 */
        val ONE_YEAR_AGO: LocalDate = LocalDate.of(2023, 11, 25)

        /** 미래 날짜 */
        val FUTURE_DATE: LocalDate = LocalDate.now().plusDays(1)
    }

    // =========================================
    // 경계값 테스트 데이터
    // =========================================
    object Boundary {
        /** 최소 날짜 범위 (하루) */
        val MIN_DATE_RANGE_DAYS = 1

        /** 최대 날짜 범위 (1년) */
        val MAX_DATE_RANGE_DAYS = 365
    }

    // =========================================
    // Corp 테스트 데이터
    // =========================================
    object ValidCorp {
        /** 삼성전자 기업코드 */
        const val SAMSUNG_CORP_CODE = "00126380"

        /** 삼성전자 종목코드 */
        const val SAMSUNG_STOCK_CODE = "005930"
    }
}
