package dev.kairoscode.kfc.utils

import java.nio.file.Path
import kotlin.io.path.Path

/**
 * 레코딩 설정 및 경로 관리
 *
 * ResponseRecorder가 API 응답을 저장할 때 사용하는 설정입니다.
 * - 레코딩 활성화 여부: -Drecord.responses=true로 설정
 * - 저장 경로: src/liveTest/resources/responses/
 */
object RecordingConfig {
    /**
     * 레코딩 활성화 여부
     *
     * 기본값: true (Live Test 실행 시 자동으로 응답 레코딩)
     * 레코딩 비활성화: ./gradlew liveTest -Precord.responses=false
     *
     * 사용 예시:
     * ```bash
     * # 레코딩 활성화 (기본)
     * ./gradlew liveTest
     *
     * # 레코딩 비활성화
     * ./gradlew liveTest -Precord.responses=false
     * ```
     */
    val isRecordingEnabled: Boolean
        get() = System.getProperty("record.responses", "true").toBoolean()

    /**
     * 레코딩 파일 저장 경로
     * Live Test의 리소스 경로로 저장됩니다.
     */
    val baseOutputPath: Path = Path("src/liveTest/resources/responses")

    /**
     * API별 레코딩 경로 상수
     *
     * ResponseRecorder 및 JsonResponseLoader에서
     * 일관된 경로를 사용하도록 합니다.
     */
    object Paths {
        /**
         * ETF API 레코딩 경로
         */
        object Etf {
            const val LIST = "etf/list"
            const val COMPREHENSIVE = "etf/comprehensive"
            const val DAILY_PRICES = "etf/daily_prices"
            const val OHLCV = "etf/ohlcv"
            const val ADJUSTED_OHLCV = "etf/adjusted_ohlcv"
            const val PRICE_CHANGES = "etf/price_changes"
            const val PORTFOLIO = "etf/portfolio"
            const val TRACKING_ERROR = "etf/tracking_error"
            const val DIVERGENCE_RATE = "etf/divergence_rate"
            const val INVESTOR_TRADING = "etf/investor_trading"
            const val SHORT = "etf/short"
        }

        /**
         * Corp API 레코딩 경로
         */
        object Corp {
            const val CORP_CODE = "corp/corp_code"
            const val DIVIDEND = "corp/dividend"
            const val STOCK_SPLIT = "corp/stock_split"
            const val DISCLOSURE = "corp/disclosure"
        }
    }
}
