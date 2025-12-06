package dev.kairoscode.kfc.integration.utils

import java.time.LocalDate

/**
 * Integration Test용 고정 날짜 픽스처
 *
 * 테스트의 결정성(determinism)을 보장하기 위해 동적 날짜(`LocalDate.now()`)를 사용하지 않고,
 * 고정된 과거 거래일을 사용합니다.
 *
 * ## 설계 원칙
 * - 실행 시점에 상관없이 동일한 결과를 보장 (Deterministic Test)
 * - 데이터가 확실히 존재하는 과거 거래일 사용
 * - 비거래일(주말, 공휴일) 테스트 케이스도 명시적으로 정의
 *
 * ## 날짜 선정 기준
 * - TRADING_DAY: 2024년 11월 마지막 주 금요일 (거래일 확정)
 * - TRADING_DAY_2: 연속된 과거 거래일
 * - WEEKEND: 주말 (토요일)
 * - HOLIDAY: 신정 (공휴일 확정)
 * - FUTURE_DATE: 미래 날짜 (데이터 존재 불가)
 * - PERIOD_START/END: 한 달 단위 기간 조회용
 *
 * ## 사용 예제
 * ```kotlin
 * @Test
 * fun `거래일 데이터 조회`() = integrationTest {
 *     val data = client.bond.getBondYieldsByDate(TestFixtures.TRADING_DAY)
 *     assertThat(data).isNotEmpty()
 * }
 *
 * @Test
 * fun `기간별 데이터 조회`() = integrationTest {
 *     val data = client.index.getOhlcvByDate(
 *         ticker = "1001",
 *         fromDate = TestFixtures.PERIOD_START,
 *         toDate = TestFixtures.PERIOD_END
 *     )
 *     assertThat(data).isNotEmpty()
 * }
 * ```
 *
 * @see IntegrationTestBase
 */
object TestFixtures {
    /**
     * 데이터가 존재하는 거래일 (금요일)
     * 2024년 2월 2일 (금요일) - KRX 데이터가 확실히 존재하는 날짜
     *
     * 주요 용도: 단일 거래일 데이터 조회 테스트
     */
    val TRADING_DAY: LocalDate = LocalDate.of(2024, 2, 2)

    /**
     * 데이터가 존재하는 이전 거래일 (목요일)
     * 2024년 2월 1일 (목요일)
     *
     * 주요 용도: 복수 거래일 비교, 연속 데이터 검증
     */
    val TRADING_DAY_2: LocalDate = LocalDate.of(2024, 2, 1)

    /**
     * 비거래일 - 주말 (토요일)
     * 2024년 2월 3일 (토요일)
     *
     * 주요 용도: 휴장일 처리 로직 검증 (KRX는 빈 결과 반환)
     */
    val WEEKEND: LocalDate = LocalDate.of(2024, 2, 3)

    /**
     * 비거래일 - 공휴일
     * 2024년 1월 1일 (신정)
     *
     * 주요 용도: 공휴일 처리 로직 검증
     */
    val HOLIDAY: LocalDate = LocalDate.of(2024, 1, 1)

    /**
     * 미래 날짜 - 데이터 존재 불가
     * 2099년 12월 31일
     *
     * 주요 용도: 미래 날짜 처리 로직 검증 (빈 결과 또는 예외)
     */
    val FUTURE_DATE: LocalDate = LocalDate.of(2099, 12, 31)

    /**
     * 기간 조회 시작일
     * 2024년 1월 2일 - 2024년 첫 거래일
     *
     * 주요 용도: 기간별 조회 테스트 (한 달 단위)
     */
    val PERIOD_START: LocalDate = LocalDate.of(2024, 1, 2)

    /**
     * 기간 조회 종료일
     * 2024년 1월 31일
     *
     * 주요 용도: 기간별 조회 테스트 (한 달 단위)
     */
    val PERIOD_END: LocalDate = LocalDate.of(2024, 1, 31)

}
