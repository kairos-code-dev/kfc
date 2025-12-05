package dev.kairoscode.kfc.integration.bond

import dev.kairoscode.kfc.domain.bond.BondCategory
import dev.kairoscode.kfc.domain.bond.BondType
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * [Bond] BondApi - 채권 수익률 API 통합 테스트
 *
 * KRX API를 사용한 채권 수익률 조회 기능을 검증합니다.
 * API 문서처럼 읽히도록 설계되었습니다.
 */
@DisplayName("[Bond] BondApi - 채권 수익률 API")
class BondApiSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("채권 수익률 조회 API")
    inner class BondYieldApi {

        @Nested
        @DisplayName("getBondYieldsByDate() - 특정일 전체 채권 수익률 조회")
        inner class GetBondYieldsByDate {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("특정 일자의 전체 채권 수익률을 조회할 수 있다")
                fun get_bond_yields_by_date() = integrationTest {
                    // Given: 조회 날짜
                    val date = LocalDate.of(2022, 2, 4)
                    println("\n📘 API: getBondYieldsByDate()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters:")
                    println("  • date: LocalDate = $date")

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)

                    // Then
                    println("\n📤 Response: BondYieldSnapshot")
                    println("  • date: ${snapshot.date}")
                    println("  • Total yields: ${snapshot.yields.size}개")
                    println("  • Sample yields:")
                    snapshot.yields.take(5).forEach { item ->
                        println("    - ${item.bondType.koreanName}: ${item.yield}% (변동: ${item.change}bp)")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(snapshot)
                    assertEquals(date, snapshot.date)
                    assertEquals(11, snapshot.yields.size, "전체 채권 수는 11개여야 합니다 (국고채 7개 + 특수채 1개 + 회사채 2개 + CD 1개)")

                    SmartRecorder.recordSmartly(
                        data = snapshot,
                        category = RecordingConfig.Paths.Bond.YIELDS,
                        fileName = "yields_by_date_20220204"
                    )
                }

                @Test
                @DisplayName("오늘 날짜의 채권 수익률을 조회할 수 있다")
                fun get_todays_bond_yields() = integrationTest {
                    // Given: 오늘 날짜
                    val today = LocalDate.now().minusDays(1)  // 직전 영업일 사용
                    println("\n📘 API: getBondYieldsByDate()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters:")
                    println("  • date: LocalDate = $today (어제)")

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(today)

                    // Then
                    println("\n📤 Response: BondYieldSnapshot")
                    println("  • date: ${snapshot.date}")
                    println("  • Total yields: ${snapshot.yields.size}개")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(snapshot)
                    assertTrue(snapshot.yields.isNotEmpty(), "채권 수익률이 비어있지 않아야 합니다")
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("수익률은 양수 값이어야 한다")
                fun yields_should_be_positive() = integrationTest {
                    // Given
                    val date = LocalDate.of(2022, 2, 4)
                    println("\n📘 응답 검증: 수익률 양수 확인")

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)

                    // Then
                    assertTrue(snapshot.yields.all { it.yield >= BigDecimal.ZERO })
                    println("  • 검증 대상: ${snapshot.yields.size}개 채권")
                    println("  • 규칙: yield >= 0%")
                    println("  ✅ 모든 수익률은 양수입니다\n")
                }

                @Test
                @DisplayName("변동폭은 정상 범위 내에 있어야 한다")
                fun change_should_be_in_normal_range() = integrationTest {
                    // Given
                    val date = LocalDate.of(2022, 2, 4)
                    println("\n📘 응답 검증: 변동폭 범위 확인")

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)

                    // Then
                    val maxChange = BigDecimal("1.0")  // 1% 이내 변동
                    assertTrue(snapshot.yields.all { it.change.abs() <= maxChange })
                    println("  • 검증 대상: ${snapshot.yields.size}개 채권")
                    println("  • 규칙: |change| <= 1%")
                    println("  ✅ 모든 변동폭은 정상 범위입니다\n")
                }

                @Test
                @DisplayName("모든 주요 채권 종류가 포함되어 있어야 한다")
                fun all_major_bond_types_included() = integrationTest {
                    // Given
                    val date = LocalDate.of(2022, 2, 4)
                    println("\n📘 응답 검증: 주요 채권 포함 여부")

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)
                    val bondTypes = snapshot.yields.map { it.bondType }.toSet()

                    // Then
                    val majorBonds = listOf(
                        BondType.TREASURY_1Y,
                        BondType.TREASURY_10Y,
                        BondType.CORPORATE_AA,
                        BondType.CD_91
                    )
                    assertTrue(bondTypes.containsAll(majorBonds))
                    println("  • 필수 포함 채권:")
                    majorBonds.forEach { println("    - ${it.koreanName}: ✅") }
                    println("  ✅ 모든 주요 채권이 포함되어 있습니다\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                // 날짜 파라미터는 기본값 사용 가능하므로 별도 검증 불필요
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("휴장일 데이터 조회 시 빈 데이터 또는 오류 처리")
                fun weekend_date_returns_empty_or_error() = integrationTest {
                    // Given: 토요일 날짜
                    val weekend = LocalDate.of(2022, 2, 5)  // 2022-02-05는 토요일
                    println("\n📘 엣지 케이스: 주말 데이터 조회")
                    println("  • 날짜: $weekend (토요일)")

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(weekend)

                    // Then
                    println("  • 결과: ${snapshot.yields.size}개 채권")
                    println("  ✅ 휴장일 처리 확인\n")

                    // 휴장일은 빈 리스트 또는 직전 영업일 데이터 반환
                    assertTrue(
                        snapshot.yields.isEmpty() || snapshot.yields.size == 11,
                        "휴장일은 빈 리스트 또는 11개 채권 데이터를 반환해야 합니다"
                    )
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[분석] 장단기 금리 스프레드 계산 (10년-2년)")
                fun calculate_term_spread() = integrationTest {
                    println("\n📘 실무 활용: 장단기 금리 스프레드 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = LocalDate.of(2022, 2, 4)

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)
                    val termSpread = snapshot.calculateTermSpread()

                    // Then
                    println("📊 장단기 금리 스프레드:")
                    val treasury10Y = snapshot.getYieldByType(BondType.TREASURY_10Y)
                    val treasury2Y = snapshot.getYieldByType(BondType.TREASURY_2Y)
                    println("  • 국고채 10년: ${treasury10Y?.yield}%")
                    println("  • 국고채 2년: ${treasury2Y?.yield}%")
                    println("  • 스프레드 (10Y-2Y): ${termSpread}bp")
                    println("  • 의미: ${if (termSpread != null && termSpread > BigDecimal.ZERO) "정상 수익률 곡선" else "역전 가능성"}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(termSpread)
                }

                @Test
                @DisplayName("[분석] 회사채 신용 스프레드 계산 (AA- - 국고채 3년)")
                fun calculate_credit_spread() = integrationTest {
                    println("\n📘 실무 활용: 회사채 신용 스프레드 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = LocalDate.of(2022, 2, 4)

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)
                    val creditSpread = snapshot.calculateCreditSpread()

                    // Then
                    println("📊 신용 스프레드:")
                    val corporateAA = snapshot.getYieldByType(BondType.CORPORATE_AA)
                    val treasury3Y = snapshot.getYieldByType(BondType.TREASURY_3Y)
                    println("  • 회사채 AA-: ${corporateAA?.yield}%")
                    println("  • 국고채 3년: ${treasury3Y?.yield}%")
                    println("  • 신용 스프레드: ${creditSpread}bp")
                    println("  • 의미: 신용 리스크 프리미엄")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(creditSpread)
                }

                @Test
                @DisplayName("[필터링] 국고채만 추출")
                fun filter_treasury_bonds() = integrationTest {
                    println("\n📘 실무 활용: 국고채만 필터링")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = LocalDate.of(2022, 2, 4)

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)
                    val treasuryYields = snapshot.getTreasuryYields()

                    // Then
                    println("📊 국고채 수익률:")
                    treasuryYields.forEach { item ->
                        println("  • ${item.bondType.koreanName}: ${item.yield}%")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertEquals(7, treasuryYields.size, "국고채는 7개여야 합니다")
                    assertTrue(treasuryYields.all { it.bondType.category == BondCategory.TREASURY })
                }

                @Test
                @DisplayName("[필터링] 회사채만 추출")
                fun filter_corporate_bonds() = integrationTest {
                    println("\n📘 실무 활용: 회사채만 필터링")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = LocalDate.of(2022, 2, 4)

                    // When
                    val snapshot = client.bond.getBondYieldsByDate(date)
                    val corporateYields = snapshot.getCorporateYields()

                    // Then
                    println("📊 회사채 수익률:")
                    corporateYields.forEach { item ->
                        println("  • ${item.bondType.koreanName}: ${item.yield}%")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertEquals(2, corporateYields.size, "회사채는 2개여야 합니다")
                    assertTrue(corporateYields.all { it.bondType.category == BondCategory.CORPORATE })
                }
            }
        }

        @Nested
        @DisplayName("getBondYields() - 특정 채권 기간별 수익률 조회")
        inner class GetBondYields {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("국고채 10년물 기간별 수익률을 조회할 수 있다")
                fun get_treasury_10y_yields() = integrationTest {
                    // Given: 조회 기간
                    val bondType = BondType.TREASURY_10Y
                    val fromDate = LocalDate.of(2022, 1, 4)
                    val toDate = LocalDate.of(2022, 2, 4)
                    println("\n📘 API: getBondYields()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters:")
                    println("  • bondType: BondType = $bondType (${bondType.koreanName})")
                    println("  • fromDate: LocalDate = $fromDate")
                    println("  • toDate: LocalDate = $toDate")

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)

                    // Then
                    println("\n📤 Response: List<BondYield>")
                    println("  • Total records: ${yields.size}개")
                    println("  • Sample records:")
                    yields.take(5).forEach { yield ->
                        println("    - ${yield.date}: ${yield.yield}% (변동: ${yield.change}bp)")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(yields)
                    assertTrue(yields.isNotEmpty(), "수익률 데이터가 비어있지 않아야 합니다")
                    assertTrue(yields.all { it.bondType == bondType })

                    SmartRecorder.recordSmartly(
                        data = yields,
                        category = RecordingConfig.Paths.Bond.YIELDS,
                        fileName = "yields_treasury_10y"
                    )
                }

                @Test
                @DisplayName("회사채 AA- 기간별 수익률을 조회할 수 있다")
                fun get_corporate_aa_yields() = integrationTest {
                    // Given
                    val bondType = BondType.CORPORATE_AA
                    val fromDate = LocalDate.of(2022, 1, 4)
                    val toDate = LocalDate.of(2022, 1, 31)
                    println("\n📘 API: getBondYields()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters:")
                    println("  • bondType: BondType = $bondType (${bondType.koreanName})")
                    println("  • fromDate: LocalDate = $fromDate")
                    println("  • toDate: LocalDate = $toDate")

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)

                    // Then
                    println("\n📤 Response: List<BondYield>")
                    println("  • Total records: ${yields.size}개")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(yields)
                    assertTrue(yields.isNotEmpty())
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("날짜는 오름차순으로 정렬되어 있어야 한다")
                fun dates_should_be_sorted_ascending() = integrationTest {
                    // Given
                    val bondType = BondType.TREASURY_2Y
                    val fromDate = LocalDate.of(2022, 1, 4)
                    val toDate = LocalDate.of(2022, 1, 31)
                    println("\n📘 응답 검증: 날짜 정렬 확인")

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)

                    // Then
                    val sortedYields = yields.sortedBy { it.date }
                    assertEquals(sortedYields, yields, "날짜는 오름차순으로 정렬되어 있어야 합니다")
                    println("  • 검증 대상: ${yields.size}개 레코드")
                    println("  • 규칙: date 오름차순 정렬")
                    println("  ✅ 날짜가 올바르게 정렬되어 있습니다\n")
                }

                @Test
                @DisplayName("모든 데이터는 요청한 기간 내에 있어야 한다")
                fun all_dates_within_requested_period() = integrationTest {
                    // Given
                    val bondType = BondType.TREASURY_5Y
                    val fromDate = LocalDate.of(2022, 1, 4)
                    val toDate = LocalDate.of(2022, 1, 31)
                    println("\n📘 응답 검증: 기간 범위 확인")

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)

                    // Then
                    assertTrue(yields.all { it.date >= fromDate && it.date <= toDate })
                    println("  • 요청 기간: $fromDate ~ $toDate")
                    println("  • 데이터 기간: ${yields.firstOrNull()?.date} ~ ${yields.lastOrNull()?.date}")
                    println("  ✅ 모든 데이터가 요청 기간 내에 있습니다\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                @Test
                @DisplayName("시작일이 종료일보다 늦으면 빈 리스트를 반환한다")
                fun returns_empty_when_from_after_to() = integrationTest {
                    // Given
                    val bondType = BondType.TREASURY_3Y
                    val fromDate = LocalDate.of(2022, 2, 4)
                    val toDate = LocalDate.of(2022, 1, 4)
                    println("\n📘 입력 검증: 잘못된 기간 범위")
                    println("  • fromDate: $fromDate")
                    println("  • toDate: $toDate")

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)

                    // Then
                    println("  • 결과: ${yields.size}개")
                    println("  ✅ 빈 리스트 반환 확인\n")

                    assertTrue(yields.isEmpty(), "시작일이 종료일보다 늦으면 빈 리스트를 반환해야 합니다")
                }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("긴 기간 조회 시 정상 동작")
                fun long_period_query_works() = integrationTest {
                    // Given: 1년 기간
                    val bondType = BondType.TREASURY_10Y
                    val fromDate = LocalDate.of(2021, 1, 4)
                    val toDate = LocalDate.of(2021, 12, 31)
                    println("\n📘 엣지 케이스: 긴 기간 조회 (1년)")

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)

                    // Then
                    println("  • 기간: $fromDate ~ $toDate")
                    println("  • 결과: ${yields.size}개 레코드")
                    println("  ✅ 긴 기간 조회 성공\n")

                    assertTrue(yields.size > 200, "1년 기간은 약 240개 영업일이 포함되어야 합니다")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[분석] 수익률 변동성 계산")
                fun calculate_yield_volatility() = integrationTest {
                    println("\n📘 실무 활용: 수익률 변동성 계산")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val bondType = BondType.TREASURY_10Y
                    val fromDate = LocalDate.of(2022, 1, 4)
                    val toDate = LocalDate.of(2022, 1, 31)

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)
                    val maxYield = yields.maxOfOrNull { it.yield }
                    val minYield = yields.minOfOrNull { it.yield }
                    val avgYield = yields.map { it.yield.toDouble() }.average()
                    val volatility = maxYield?.let { max -> minYield?.let { min -> max - min } }

                    // Then
                    println("📊 수익률 통계:")
                    println("  • 채권: ${bondType.koreanName}")
                    println("  • 기간: $fromDate ~ $toDate")
                    println("  • 최고 수익률: $maxYield%")
                    println("  • 최저 수익률: $minYield%")
                    println("  • 평균 수익률: ${String.format("%.3f", avgYield)}%")
                    println("  • 변동폭: ${volatility}bp")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(maxYield)
                    assertNotNull(minYield)
                }

                @Test
                @DisplayName("[추이] 수익률 상승/하락 추세 분석")
                fun analyze_yield_trend() = integrationTest {
                    println("\n📘 실무 활용: 수익률 추세 분석")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val bondType = BondType.TREASURY_10Y
                    val fromDate = LocalDate.of(2022, 1, 4)
                    val toDate = LocalDate.of(2022, 1, 31)

                    // When
                    val yields = client.bond.getBondYields(bondType, fromDate, toDate)
                    val risingDays = yields.count { it.isYieldRising() }
                    val fallingDays = yields.count { it.isYieldFalling() }
                    val unchangedDays = yields.count { it.isYieldUnchanged() }

                    // Then
                    println("📊 추세 분석:")
                    println("  • 상승일: ${risingDays}일")
                    println("  • 하락일: ${fallingDays}일")
                    println("  • 보합일: ${unchangedDays}일")
                    println("  • 전체 추세: ${if (risingDays > fallingDays) "상승" else "하락"}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertEquals(yields.size, risingDays + fallingDays + unchangedDays)
                }
            }
        }
    }
}
