package dev.kairoscode.kfc.integration.utils

import java.nio.file.Path
import java.nio.file.Paths as JavaPaths

/**
 * 레코딩 설정 및 경로 관리
 *
 * ResponseRecorder가 API 응답을 저장할 때 사용하는 설정입니다.
 * - 레코딩 활성화 여부: -Drecord.responses=true로 설정
 * - 저장 경로: src/integrationTest/resources/responses/
 *
 * 구조:
 * - Etf: ETF 관련 API (List, Price, Metrics, Trading)
 * - Corp: 기업 관련 API (Code, Corporate Actions, Disclosure)
 */
object RecordingConfig {
    /**
     * 레코딩 활성화 여부
     *
     * 기본값: true (Integration Test 실행 시 자동으로 응답 레코딩)
     * 레코딩 비활성화: ./gradlew integrationTest -Precord.responses=false
     *
     * 사용 예시:
     * ```bash
     * # 레코딩 활성화 (기본)
     * ./gradlew integrationTest
     *
     * # 레코딩 비활성화
     * ./gradlew integrationTest -Precord.responses=false
     * ```
     */
    val isRecordingEnabled: Boolean
        get() = System.getProperty("record.responses", "true").toBoolean()

    /**
     * 레코딩 파일 저장 경로
     * Integration Test의 리소스 경로로 저장됩니다.
     */
    val baseOutputPath: Path = JavaPaths.get("src/integrationTest/resources/responses")

    /**
     * API 네임스페이스별 레코딩 경로
     *
     * 구조:
     * - Etf: ETF 관련 API
     *   - List: ETF 목록 조회
     *   - Price: 가격 데이터 (일별, OHLCV, 수정주가)
     *   - Metrics: 성과 지표 (추적오차, 괴리율)
     *   - Trading: 거래 데이터 (투자자별, 공매도)
     * - Corp: 기업 관련 API
     *   - Code: 기업 코드
     *   - Actions: 기업 액션 (배당, 액면분할)
     *   - Disclosure: 공시 정보
     */
    object Paths {

        // ========================================
        // ETF API 네임스페이스
        // ========================================

        /**
         * ETF 목록 조회 API
         */
        object EtfList {
            const val BASE = "etf/list"
            const val ALL = "$BASE/all"
            const val BY_MARKET = "$BASE/by_market"
        }

        /**
         * ETF 가격 데이터 API
         */
        object EtfPrice {
            const val BASE = "etf/price"
            const val DAILY = "$BASE/daily"
            const val OHLCV = "$BASE/ohlcv"
            const val ADJUSTED = "$BASE/adjusted"
            const val CHANGES = "$BASE/changes"
            const val INTRADAY_BARS = "$BASE/intraday_bars"
            const val RECENT_DAILY = "$BASE/recent_daily"
        }

        /**
         * ETF 성과 지표 API
         */
        object EtfMetrics {
            const val BASE = "etf/metrics"
            const val DETAILED_INFO = "$BASE/detailed_info"
            const val TRACKING_ERROR = "$BASE/tracking_error"
            const val DIVERGENCE_RATE = "$BASE/divergence_rate"
            const val PORTFOLIO = "$BASE/portfolio"
            const val GENERAL_INFO = "$BASE/general_info"
            const val PORTFOLIO_TOP10 = "$BASE/portfolio_top10"

            @Deprecated("Use DETAILED_INFO instead", ReplaceWith("DETAILED_INFO"))
            const val COMPREHENSIVE = "$BASE/comprehensive"
        }

        /**
         * ETF 거래 데이터 API
         */
        object EtfTrading {
            const val BASE = "etf/trading"
            const val INVESTOR = "$BASE/investor"
            const val SHORT = "$BASE/short"
        }

        // ========================================
        // Corp API 네임스페이스
        // ========================================

        /**
         * 기업 코드 API
         */
        object CorpCode {
            const val BASE = "corp/code"
            const val LOOKUP = "$BASE/lookup"
        }

        /**
         * 기업 액션 API (배당, 액면분할 등)
         */
        object CorpActions {
            const val BASE = "corp/actions"
            const val DIVIDEND = "$BASE/dividend"
            const val STOCK_SPLIT = "$BASE/stock_split"
        }

        /**
         * 공시 정보 API
         */
        object CorpDisclosure {
            const val BASE = "corp/disclosure"
            const val LIST = "$BASE/list"
            const val DETAIL = "$BASE/detail"
        }

        // ========================================
        // 공통
        // ========================================

        /**
         * 에러 응답 저장 경로
         */
        const val ERRORS = "errors"

        // ========================================
        // 하위 호환성을 위한 기존 경로 (Deprecated)
        // ========================================

        /**
         * ETF 관련 기존 경로
         * 새로운 계층 구조로 마이그레이션 권장
         */
        object Etf {
            @Deprecated("Use EtfList.BASE instead", ReplaceWith("EtfList.BASE"))
            const val LIST = "etf/list"

            @Deprecated("Use EtfMetrics.COMPREHENSIVE instead", ReplaceWith("EtfMetrics.COMPREHENSIVE"))
            const val COMPREHENSIVE = "etf/comprehensive"

            @Deprecated("Use EtfPrice.DAILY instead", ReplaceWith("EtfPrice.DAILY"))
            const val DAILY_PRICES = "etf/daily_prices"

            @Deprecated("Use EtfPrice.OHLCV instead", ReplaceWith("EtfPrice.OHLCV"))
            const val OHLCV = "etf/ohlcv"

            @Deprecated("Use EtfPrice.ADJUSTED instead", ReplaceWith("EtfPrice.ADJUSTED"))
            const val ADJUSTED_OHLCV = "etf/adjusted_ohlcv"

            @Deprecated("Use EtfPrice.CHANGES instead", ReplaceWith("EtfPrice.CHANGES"))
            const val PRICE_CHANGES = "etf/price_changes"

            @Deprecated("Use EtfMetrics.PORTFOLIO instead", ReplaceWith("EtfMetrics.PORTFOLIO"))
            const val PORTFOLIO = "etf/portfolio"

            @Deprecated("Use EtfMetrics.TRACKING_ERROR instead", ReplaceWith("EtfMetrics.TRACKING_ERROR"))
            const val TRACKING_ERROR = "etf/tracking_error"

            @Deprecated("Use EtfMetrics.DIVERGENCE_RATE instead", ReplaceWith("EtfMetrics.DIVERGENCE_RATE"))
            const val DIVERGENCE_RATE = "etf/divergence_rate"

            @Deprecated("Use EtfTrading.INVESTOR instead", ReplaceWith("EtfTrading.INVESTOR"))
            const val INVESTOR_TRADING = "etf/investor_trading"

            @Deprecated("Use EtfTrading.SHORT instead", ReplaceWith("EtfTrading.SHORT"))
            const val SHORT = "etf/short"
        }

        /**
         * Corp 관련 기존 경로
         * 새로운 계층 구조로 마이그레이션 권장
         */
        object Corp {
            @Deprecated("Use CorpCode.BASE instead", ReplaceWith("CorpCode.BASE"))
            const val CORP_CODE = "corp/corp_code"

            @Deprecated("Use CorpActions.DIVIDEND instead", ReplaceWith("CorpActions.DIVIDEND"))
            const val DIVIDEND = "corp/dividend"

            @Deprecated("Use CorpActions.STOCK_SPLIT instead", ReplaceWith("CorpActions.STOCK_SPLIT"))
            const val STOCK_SPLIT = "corp/stock_split"

            @Deprecated("Use CorpDisclosure.BASE instead", ReplaceWith("CorpDisclosure.BASE"))
            const val DISCLOSURE = "corp/disclosure"
        }
    }
}
