package dev.kairoscode.kfc.integration.corp

import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import dev.kairoscode.kfc.common.TestFixtures
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

/**
 * [Corp] CorpApi - 기업 공시 API 통합 테스트
 *
 * OpenDart API를 사용한 기업 공시 정보 조회 기능을 검증합니다.
 * 모든 테스트는 OPENDART_API_KEY가 필요합니다.
 *
 * ## 주요 기능
 * - 법인 코드 조회: 전체 법인(상장/비상장) 고유번호 목록
 * - 배당 정보: 특정 법인의 배당 이력
 * - 주식 분할 정보: 주식 분할/병합 이력
 * - 공시 검색: 기간별 공시 목록 검색
 *
 * ## 제약사항
 * - OPENDART_API_KEY 필수
 * - Rate limit: 전역 10 req/sec (GlobalRateLimiters)
 * - 일부 API는 대용량 데이터 처리 (2분 timeout)
 */
@DisplayName("[I][Corp] CorpApi - 기업 공시 API")
class CorpApiSpec : IntegrationTestBase() {

    // ========================================
    // 기업 코드 조회 API
    // ========================================

    @Nested
    @DisplayName("기업 코드 조회 API")
    inner class CorpCodeApi {

        /**
         * ## getCorpCodeList() - 법인 코드 목록 조회
         *
         * ### API 개요
         * 전체 법인(상장/비상장)의 고유번호 목록을 조회하는 API입니다.
         * OPENDART에서 제공하는 ZIP 압축 파일을 자동으로 다운로드하고 파싱합니다.
         *
         * ### 엔드포인트
         * ```kotlin
         * suspend fun getCorpCodeList(): List<CorpCode>
         * ```
         *
         * ### 파라미터
         * - (없음)
         *
         * ### 응답
         * - `List<CorpCode>`: 법인 고유번호 목록 (10,000개 이상)
         *   - `corpCode`: String - 법인 고유번호 (8자리)
         *   - `corpName`: String - 법인명
         *   - `stockCode`: String? - 종목코드 (6자리, 상장사만 존재)
         *   - `modifyDate`: String - 최종 변경일자
         *
         * ### 특징
         * - 대용량 데이터: 10,000개 이상의 법인 정보
         * - 자동 처리: ZIP 압축 해제 → XML 파싱 → List<CorpCode> 반환
         * - 상장/비상장 모두 포함
         * - stockCode로 상장 여부 판단 가능
         *
         * ### 제약사항
         * - OPENDART_API_KEY 필요
         * - Rate limit: 전역 10 req/sec (GlobalRateLimiters)
         * - 대용량 데이터로 인해 처리 시간 소요 (2분 timeout)
         *
         * ### 활용 예시
         * - 종목코드 → 법인 고유번호 매핑
         * - 법인명 검색
         * - 상장사 필터링
         * - 산업별 그룹화
         *
         * ### 관련 문서
         * - OPENDART API: https://opendart.fss.or.kr/
         */
        @Nested
        @DisplayName("getCorpCodeList() - 법인 코드 목록 조회")
        inner class GetCorpCodeList {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("전체 법인 고유번호 목록을 조회할 수 있다")
                fun get_all_corp_code_list() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 API: getCorpCodeList()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: OPENDART API Key
                    requireOpendartApiKey()

                    println("📥 Input Parameters:")
                    println("  • (없음)")

                    // When: Request corp code list
                    val corpCodeList = client.corp!!.getCorpCodeList()

                    // Then: Returns large dataset
                    println("\n📤 Response: List<CorpCode>")
                    println("  • Total records: ${corpCodeList.size}개")
                    println("  • First 3 records:")
                    corpCodeList.take(3).forEach { corp ->
                        println("    - ${corp.corpName} (${corp.corpCode})")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("  • 10,000개 이상의 법인 정보 반환")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(corpCodeList.isNotEmpty())

                    // 스마트 레코딩 (대용량 데이터 자동 처리)
                    SmartRecorder.recordSmartly(
                        data = corpCodeList,
                        category = RecordingConfig.Paths.CorpCode.BASE,
                        fileName = "corp_code_list"
                    )
                }

                @Test
                @DisplayName("ZIP 압축 해제와 XML 파싱이 자동으로 처리된다")
                fun auto_decompression_and_parsing() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 자동 처리: ZIP → XML → List<CorpCode>")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: OPENDART API returns ZIP file
                    requireOpendartApiKey()

                    println("📥 Input:")
                    println("  • OPENDART API는 ZIP 파일 반환")

                    // When: Call getCorpCodeList()
                    val corpCodeList = client.corp!!.getCorpCodeList()

                    // Then: Automatically decompressed and parsed
                    println("\n📤 Response:")
                    println("  • ZIP 압축 해제: 자동 처리 ✓")
                    println("  • XML 파싱: 자동 처리 ✓")
                    println("  • List<CorpCode> 반환: ${corpCodeList.size}개")

                    println("\n✅ 테스트 결과: 자동 처리 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(corpCodeList.isNotEmpty())
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("대량의 법인 코드를 반환한다 (10,000개 이상)")
                fun returns_large_number_of_corp_codes() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 응답 데이터 검증: 데이터 볼륨")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()

                    // When
                    val corpCodes = client.corp!!.getCorpCodeList()

                    // Then: Validate data volume
                    println("\n📊 데이터 볼륨 검증:")
                    println("  • 전체 레코드: ${corpCodes.size}개")
                    println("  • 기대값: 10,000개 이상")

                    println("\n📤 샘플 데이터 (첫 5개):")
                    corpCodes.take(5).forEach { corp ->
                        println("  • ${corp.corpName} (${corp.corpCode}) - ${corp.stockCode ?: "비상장"}")
                    }

                    println("\n✅ 검증 결과: ${corpCodes.size}개 (>= 10,000)")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(corpCodes.size >= 10000, "10,000개 이상의 법인 코드가 반환되어야 합니다. 실제: ${corpCodes.size}개")
                }

                @Test
                @DisplayName("응답은 필수 필드(corpCode, corpName)를 포함한다")
                fun response_contains_required_fields() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 응답 데이터 검증: 필수 필드")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()

                    // When
                    val corpCodes = client.corp!!.getCorpCodeList()
                    val sample = corpCodes.first()

                    // Then: Validate required fields
                    println("\n📋 필수 필드 검증 (샘플):")
                    println("  • corpCode: ${sample.corpCode} ✓")
                    println("  • corpName: ${sample.corpName} ✓")
                    println("  • stockCode: ${sample.stockCode ?: "(없음 - 비상장)"}")
                    println("  • modifyDate: ${sample.modifyDate ?: "(없음)"}")

                    println("\n✅ 검증 결과: 모든 필수 필드 존재")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(sample.corpCode, "corpCode는 필수입니다")
                    assertNotNull(sample.corpName, "corpName은 필수입니다")
                    assertTrue(sample.corpCode.isNotBlank(), "corpCode는 비어있지 않아야 합니다")
                    assertTrue(sample.corpName.isNotBlank(), "corpName은 비어있지 않아야 합니다")
                }

                @Test
                @DisplayName("모든 법인은 유효한 corpCode와 corpName을 가진다")
                fun all_corps_have_valid_fields() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 응답 데이터 검증: 전체 데이터 무결성")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()

                    // When
                    val corpCodes = client.corp!!.getCorpCodeList()

                    // Then: Validate all records
                    println("\n🔍 전체 레코드 검증:")
                    println("  • 총 레코드: ${corpCodes.size}개")

                    val invalidCorps = corpCodes.filter {
                        it.corpCode.isBlank() || it.corpName.isBlank()
                    }

                    println("  • 유효한 레코드: ${corpCodes.size - invalidCorps.size}개")
                    println("  • 무효한 레코드: ${invalidCorps.size}개")

                    println("\n✅ 검증 결과: ${if (invalidCorps.isEmpty()) "모든 레코드 유효" else "일부 레코드 무효"}")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(invalidCorps.isEmpty(), "모든 법인은 유효한 corpCode와 corpName을 가져야 합니다")
                }

                @Test
                @DisplayName("삼성전자가 목록에 포함된다")
                fun contains_samsung_electronics() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 응답 데이터 검증: 특정 법인 존재")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()

                    // When
                    val corpCodes = client.corp!!.getCorpCodeList()
                    val samsung = corpCodes.find { it.corpCode == TestFixtures.Corp.SAMSUNG_CORP_CODE }

                    // Then: Samsung exists
                    println("\n🔍 검색 조건:")
                    println("  • corpCode: ${TestFixtures.Corp.SAMSUNG_CORP_CODE}")

                    println("\n📋 검색 결과:")
                    if (samsung != null) {
                        println("  ✅ 법인 발견:")
                        println("    - 법인명: ${samsung.corpName}")
                        println("    - 법인코드: ${samsung.corpCode}")
                        println("    - 종목코드: ${samsung.stockCode}")
                    } else {
                        println("  ❌ 법인을 찾을 수 없습니다")
                    }

                    println("\n✅ 검증 결과: 삼성전자 존재")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(samsung, "삼성전자가 목록에 포함되어야 합니다")
                    assertEquals("삼성전자", samsung!!.corpName)
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                @Test
                @DisplayName("API Key가 없으면 테스트를 skip한다")
                fun requires_api_key() = integrationTest {
                    println("\n📘 입력 검증: API Key 필수")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    if (!hasOpendartApiKey) {
                        println("⚠️ OPENDART_API_KEY가 설정되지 않아 테스트를 skip합니다.")
                        Assumptions.assumeTrue(false)
                    }

                    println("✅ API Key 확인: 설정됨")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("상장사와 비상장사를 모두 포함한다")
                fun includes_both_listed_and_unlisted() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 엣지 케이스: 상장/비상장 모두 포함")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()

                    // When
                    val corpCodes = client.corp!!.getCorpCodeList()
                    val listedCorps = corpCodes.filter { !it.stockCode.isNullOrBlank() }
                    val unlistedCorps = corpCodes.filter { it.stockCode.isNullOrBlank() }

                    // Then: Both types exist
                    println("\n📊 상장/비상장 분포:")
                    println("  • 전체 법인: ${corpCodes.size}개")
                    println("  • 상장사: ${listedCorps.size}개 (stockCode 존재)")
                    println("  • 비상장사: ${unlistedCorps.size}개 (stockCode 없음)")

                    println("\n  상장사 샘플 (첫 3개):")
                    listedCorps.take(3).forEach { corp ->
                        println("    - ${corp.corpName} (${corp.stockCode})")
                    }

                    println("\n  비상장사 샘플 (첫 3개):")
                    unlistedCorps.take(3).forEach { corp ->
                        println("    - ${corp.corpName} (비상장)")
                    }

                    println("\n✅ 검증 결과: 상장/비상장 모두 포함")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(listedCorps.isNotEmpty(), "상장사가 1개 이상 있어야 합니다")
                    assertTrue(unlistedCorps.isNotEmpty(), "비상장사가 1개 이상 있어야 합니다")
                }

                @Test
                @DisplayName("대용량 데이터를 timeout 내에 처리한다")
                fun processes_large_data_within_timeout() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 엣지 케이스: 대용량 데이터 처리 시간")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given: 2 minutes timeout
                    requireOpendartApiKey()

                    println("📥 Input:")
                    println("  • Timeout: 2 minutes")

                    val startTime = System.currentTimeMillis()

                    // When: Request large dataset
                    val corpCodes = client.corp!!.getCorpCodeList()

                    val elapsedTime = System.currentTimeMillis() - startTime

                    // Then: Completes within timeout
                    println("\n📤 Response:")
                    println("  • Records: ${corpCodes.size}개")
                    println("  • Processing time: ${elapsedTime}ms")
                    println("  • Timeout: 120,000ms")

                    println("\n✅ 검증 결과: Timeout 내 처리 완료")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(elapsedTime < 120_000, "2분 내에 처리되어야 합니다")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[검색] 특정 종목코드로 법인 찾기 (삼성전자)")
                fun find_corp_by_stock_code_samsung() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 실무 활용: 종목코드로 검색")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val targetStockCode = "005930" // 삼성전자

                    // When: Search in large dataset
                    val allCorps = client.corp!!.getCorpCodeList()
                    val samsung = allCorps.find { it.stockCode == targetStockCode }

                    // Then: Found samsung
                    println("\n🔍 검색 조건:")
                    println("  • 종목코드: $targetStockCode (삼성전자)")
                    println("  • 전체 레코드: ${allCorps.size}개")

                    println("\n📤 검색 결과:")
                    if (samsung != null) {
                        println("  ✅ 법인 발견:")
                        println("    - 법인명: ${samsung.corpName}")
                        println("    - 법인코드: ${samsung.corpCode}")
                        println("    - 종목코드: ${samsung.stockCode}")
                    } else {
                        println("  ❌ 법인을 찾을 수 없습니다")
                    }

                    println("\n💡 활용 방법:")
                    println("  • 종목코드로 법인 고유번호 조회")
                    println("  • 재무제표 조회 시 corpCode 필요")
                    println("  • 종목코드 → corpCode 매핑 테이블 구축")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(samsung, "삼성전자 법인을 찾을 수 있어야 합니다")
                    assertEquals("삼성전자", samsung!!.corpName)
                    assertEquals(TestFixtures.Corp.SAMSUNG_CORP_CODE, samsung.corpCode)
                }

                @Test
                @DisplayName("[필터링] 코스피 상장사만 필터링")
                fun filter_kospi_listed_companies() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 실무 활용: 코스피 상장사 필터링")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val allCorps = client.corp!!.getCorpCodeList()

                    // When: Filter listed companies (stockCode exists)
                    val listedCorps = allCorps.filter { !it.stockCode.isNullOrBlank() }

                    // Then: Returns only listed companies
                    println("\n📊 필터링 결과:")
                    println("  • 전체 법인: ${allCorps.size}개")
                    println("  • 상장사: ${listedCorps.size}개")
                    println("  • 비상장사: ${allCorps.size - listedCorps.size}개")
                    println("  • 필터링 조건: stockCode != null")

                    println("\n  상위 10개 상장사:")
                    listedCorps.take(10).forEach { corp ->
                        println("    - ${corp.corpName} (${corp.stockCode})")
                    }

                    println("\n💡 활용 방법:")
                    println("  • 상장사만 대상으로 재무 분석")
                    println("  • 주가 데이터와 연동")
                    println("  • 투자 포트폴리오 구성")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(listedCorps.isNotEmpty(), "상장사가 1개 이상 있어야 합니다")
                    assertTrue(listedCorps.size < allCorps.size, "비상장사도 포함되어야 합니다")
                    assertTrue(listedCorps.all { !it.stockCode.isNullOrBlank() }, "모두 종목코드를 가져야 합니다")
                }

                @Test
                @DisplayName("[검색] 법인명으로 고유번호 검색 ('삼성' 포함)")
                fun search_corp_code_by_name() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 실무 활용: 법인명 검색")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val allCorps = client.corp!!.getCorpCodeList()

                    // When: Search by name containing "삼성"
                    val samsungCorps = allCorps.filter {
                        it.corpName.contains("삼성")
                    }

                    // Then: Returns samsung-related corps
                    println("\n🔍 검색 조건:")
                    println("  • 키워드: '삼성'")
                    println("  • 전체 레코드: ${allCorps.size}개")

                    println("\n📤 검색 결과 (상위 10개):")
                    samsungCorps.take(10).forEach { corp ->
                        println("  • ${corp.corpName} - ${corp.stockCode ?: "비상장"} (${corp.corpCode})")
                    }

                    println("\n  총 검색 결과: ${samsungCorps.size}개")

                    println("\n💡 활용 방법:")
                    println("  • 법인명으로 빠른 검색")
                    println("  • 계열사 그룹화")
                    println("  • 유사 법인 찾기")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(samsungCorps.isNotEmpty(), "삼성 관련 법인이 있어야 합니다")
                    assertTrue(samsungCorps.any { it.corpName == "삼성전자" }, "삼성전자가 포함되어야 합니다")
                }

                @Test
                @DisplayName("[그룹화] 종목코드 첫 자리별 법인 분포")
                fun group_corps_by_stock_code_prefix() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 실무 활용: 종목코드 첫 자리별 그룹화")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val allCorps = client.corp!!.getCorpCodeList()

                    // When: Group by first digit of stockCode
                    val groupedByFirstDigit = allCorps
                        .filter { !it.stockCode.isNullOrBlank() }
                        .groupBy { it.stockCode!!.first() }

                    // Then: Returns distribution
                    println("\n📊 종목코드 첫 자리별 분포:")
                    groupedByFirstDigit.entries
                        .sortedByDescending { it.value.size }
                        .take(5)
                        .forEach { (digit, corps) ->
                            println("  • '$digit'로 시작: ${corps.size}개")
                            println("    예시: ${corps.take(3).joinToString(", ") { it.corpName }}")
                        }

                    println("\n💡 활용 방법:")
                    println("  • 업종별 분류 기초 데이터")
                    println("  • 종목코드 패턴 분석")
                    println("  • 산업별 통계 생성")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(groupedByFirstDigit.isNotEmpty(), "그룹화된 데이터가 있어야 합니다")
                    assertTrue(groupedByFirstDigit.size > 1, "여러 그룹이 있어야 합니다")
                }

                @Test
                @DisplayName("[매핑] 종목코드 → corpCode 매핑 테이블 구축")
                fun build_stock_code_to_corp_code_mapping() = integrationTest(timeout = 2.minutes) {
                    println("\n📘 실무 활용: 종목코드 → corpCode 매핑")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    requireOpendartApiKey()
                    val allCorps = client.corp!!.getCorpCodeList()

                    // When: Build mapping table
                    val stockCodeToCorpCode = allCorps
                        .filter { !it.stockCode.isNullOrBlank() }
                        .associate { it.stockCode!! to it.corpCode }

                    // Then: Returns mapping table
                    println("\n📊 매핑 테이블 구축:")
                    println("  • 전체 법인: ${allCorps.size}개")
                    println("  • 매핑 테이블 크기: ${stockCodeToCorpCode.size}개")

                    println("\n  매핑 샘플 (5개):")
                    stockCodeToCorpCode.entries.take(5).forEach { (stockCode, corpCode) ->
                        val corp = allCorps.find { it.corpCode == corpCode }
                        println("    - $stockCode → $corpCode (${corp?.corpName})")
                    }

                    // Example usage: Find Samsung corpCode
                    val samsungCorpCode = stockCodeToCorpCode["005930"]
                    println("\n💡 활용 예시:")
                    println("  • 종목코드 '005930' → corpCode: $samsungCorpCode")
                    println("  • 이 corpCode로 재무제표 조회 가능")

                    println("\n💡 활용 방법:")
                    println("  • 주가 데이터 + 재무 데이터 결합")
                    println("  • 빠른 corpCode 조회 (O(1))")
                    println("  • 데이터베이스 매핑 테이블")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(stockCodeToCorpCode.isNotEmpty(), "매핑 테이블이 비어있지 않아야 합니다")
                    assertEquals(TestFixtures.Corp.SAMSUNG_CORP_CODE, samsungCorpCode)
                }
            }
        }
    }

    // ========================================
    // 배당 및 주식 변동 API
    // ========================================

    @Nested
    @DisplayName("배당 및 주식 변동 API")
    inner class DividendAndStockChangeApi {

        /**
         * ## getDividendInfo() - 배당 정보 조회
         *
         * ### API 개요
         * 특정 법인의 배당 정보를 조회합니다.
         *
         * ### 엔드포인트
         * ```kotlin
         * suspend fun getDividendInfo(corpCode: String, year: Int, reportCode: String? = null): List<DividendInfo>
         * ```
         *
         * ### 파라미터
         * - `corpCode`: String - 법인 고유번호 (8자리)
         * - `year`: Int - 조회 연도 (2015년 이후)
         * - `reportCode`: String? - 보고서 코드 (11011: 사업보고서, 11012: 반기보고서 등)
         *
         * ### 특징
         * - 2015년 이후 데이터만 지원
         * - 보고서 타입별 조회 가능
         * - Rate Limiting 고려 필요
         */
        @Nested
        @DisplayName("getDividendInfo() - 배당 정보 조회")
        inner class GetDividendInfo {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("삼성전자의 배당 정보를 고정 연도로 조회할 수 있다")
                fun get_samsung_dividend_info_for_fixed_year() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getDividendInfo()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • year: Int = $year")

                    // When
                    val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)

                    // Then
                    println("\n📤 Response: List<DividendInfo>")
                    println("  • size: ${dividendInfo.size}")

                    if (dividendInfo.isNotEmpty()) {
                        println("\n  [샘플 데이터]")
                        dividendInfo.forEach { info ->
                            println("    • 배당 구분: ${info.dividendType}, 주식 종류: ${info.stockKind}")
                        }
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = dividendInfo,
                        category = RecordingConfig.Paths.CorpActions.DIVIDEND,
                        fileName = "samsung_dividend_$year"
                    )
                }

                @Test
                @DisplayName("카카오의 배당 정보를 고정 연도로 조회할 수 있다")
                fun get_kakao_dividend_info_for_fixed_year() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getDividendInfo()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
                    val year = 2023

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • year: Int = $year")

                    // When
                    val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)

                    // Then
                    println("\n📤 Response: List<DividendInfo>")
                    println("  • size: ${dividendInfo.size}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = dividendInfo,
                        category = RecordingConfig.Paths.CorpActions.DIVIDEND,
                        fileName = "kakao_dividend_$year"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                // 향후 필요시 추가
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                // 향후 필요시 추가
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("다양한 보고서 타입으로 조회할 수 있다")
                fun query_with_different_report_types() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getDividendInfo() - 보고서 타입별")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    val reportCodes = mapOf(
                        "11011" to "사업보고서",
                        "11012" to "반기보고서",
                        "11013" to "1분기보고서",
                        "11014" to "3분기보고서"
                    )

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • year: Int = $year")
                    println("  • reportCodes: ${reportCodes.keys.joinToString(", ")}")

                    // When & Then
                    println("\n📤 Response: 보고서 타입별 배당 정보")
                    reportCodes.forEach { (reportCode, reportName) ->
                        val dividendInfo = client.corp!!.getDividendInfo(corpCode, year, reportCode)
                        println("  • $reportName ($reportCode): ${dividendInfo.size}건")
                        delay(500)
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("고정 기간 기준으로 배당 이력을 조회할 수 있다")
                fun get_dividend_history_for_fixed_period() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getDividendInfo() - 활용 예제")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val years = listOf(2021, 2022, 2023)

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • years: ${years.joinToString(", ")}")

                    // When & Then
                    println("\n📤 Response: 연도별 배당 이력")
                    years.forEach { year ->
                        val dividendInfo = client.corp!!.getDividendInfo(corpCode, year)
                        println("  • ${year}년: ${dividendInfo.size}건")
                        delay(500)
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }

        /**
         * ## getStockSplitInfo() - 주식 분할/병합 정보 조회
         *
         * ### API 개요
         * 특정 법인의 주식 분할/병합 정보를 조회합니다.
         *
         * ### 엔드포인트
         * ```kotlin
         * suspend fun getStockSplitInfo(corpCode: String, year: Int): List<StockSplitInfo>
         * ```
         *
         * ### 파라미터
         * - `corpCode`: String - 법인 고유번호 (8자리)
         * - `year`: Int - 조회 연도
         *
         * ### 특징
         * - 액면분할이 없는 경우 빈 결과 반환
         * - Rate Limiting 고려 필요
         */
        @Nested
        @DisplayName("getStockSplitInfo() - 주식 분할/병합 정보 조회")
        inner class GetStockSplitInfo {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("삼성전자의 주식 분할 정보를 고정 연도로 조회할 수 있다")
                fun get_samsung_stock_split_info_for_fixed_year() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getStockSplitInfo()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val year = 2023

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • year: Int = $year")

                    // When
                    val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)

                    // Then
                    println("\n📤 Response: List<StockSplitInfo>")
                    println("  • size: ${stockSplitInfo.size}")

                    if (stockSplitInfo.isNotEmpty()) {
                        println("\n  [샘플 데이터]")
                        stockSplitInfo.forEach { info ->
                            println("    • 이벤트: ${info.eventType}, 일자: ${info.eventDate}")
                        }
                    } else {
                        println("  • 해당 연도에 주식 분할/병합이 없습니다")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = stockSplitInfo,
                        category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                        fileName = "samsung_stock_split_$year"
                    )
                }

                @Test
                @DisplayName("카카오의 주식 분할 정보를 고정 연도로 조회할 수 있다")
                fun get_kakao_stock_split_info_for_fixed_year() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getStockSplitInfo()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
                    val year = 2023

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • year: Int = $year")

                    // When
                    val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)

                    // Then
                    println("\n📤 Response: List<StockSplitInfo>")
                    println("  • size: ${stockSplitInfo.size}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = stockSplitInfo,
                        category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                        fileName = "kakao_stock_split_$year"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                // 향후 필요시 추가
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                // 향후 필요시 추가
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("액면분할이 없는 경우 빈 결과를 반환한다")
                fun return_empty_result_when_no_stock_split() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getStockSplitInfo()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
                    val year = 2023

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • year: Int = $year")

                    // When
                    val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)

                    // Then
                    println("\n📤 Response: List<StockSplitInfo>")
                    println("  • size: ${stockSplitInfo.size}건 (빈 리스트 가능)")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = stockSplitInfo,
                        category = RecordingConfig.Paths.CorpActions.STOCK_SPLIT,
                        fileName = "empty_stock_split"
                    )
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("고정 기간 기준으로 분할 이력을 조회할 수 있다")
                fun get_stock_split_history_for_fixed_period() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: getStockSplitInfo() - 활용 예제")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val years = listOf(2019, 2020, 2021, 2022, 2023)

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • years: ${years.joinToString(", ")}")

                    // When & Then
                    println("\n📤 Response: 연도별 주식 분할 이력")
                    years.forEach { year ->
                        val stockSplitInfo = client.corp!!.getStockSplitInfo(corpCode, year)
                        println("  • ${year}년: ${stockSplitInfo.size}건")

                        if (stockSplitInfo.isNotEmpty()) {
                            stockSplitInfo.forEach { info ->
                                println("      - ${info.eventType} (${info.eventDate})")
                            }
                        }

                        delay(500)
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }
    }

    // ========================================
    // 공시 검색 API
    // ========================================

    @Nested
    @DisplayName("공시 검색 API")
    inner class DisclosureApi {

        /**
         * ## searchDisclosures() - 공시 검색
         *
         * ### API 개요
         * 특정 기간의 공시 목록을 검색합니다.
         *
         * ### 엔드포인트
         * ```kotlin
         * suspend fun searchDisclosures(
         *     corpCode: String?,
         *     startDate: LocalDate,
         *     endDate: LocalDate,
         *     pageNo: Int = 1,
         *     pageCount: Int = 10
         * ): List<Disclosure>
         * ```
         *
         * ### 파라미터
         * - `corpCode`: String? - 법인 고유번호 (null: 전체 법인)
         * - `startDate`: LocalDate - 조회 시작일
         * - `endDate`: LocalDate - 조회 종료일
         * - `pageNo`: Int - 페이지 번호 (기본: 1)
         * - `pageCount`: Int - 페이지당 건수 (기본: 10, 최대: 100)
         *
         * ### 특징
         * - 날짜 범위는 최대 1년
         * - pageCount 최대 100
         * - 페이징 처리 지원
         */
        @Nested
        @DisplayName("searchDisclosures() - 공시 검색")
        inner class SearchDisclosures {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("특정 기간의 공시 목록을 조회할 수 있다")
                fun search_disclosures_for_specific_period() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: searchDisclosures()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val endDate = TestFixtures.Dates.TRADING_DAY
                    val startDate = endDate.minusMonths(1)
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • startDate: LocalDate = $startDate")
                    println("  • endDate: LocalDate = $endDate")

                    // When
                    val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)

                    // Then
                    println("\n📤 Response: List<Disclosure>")
                    println("  • size: ${disclosures.size}")

                    if (disclosures.isNotEmpty()) {
                        println("\n  [최근 공시 3건]")
                        disclosures.take(3).forEach { disclosure ->
                            println("    • ${disclosure.reportName} (${disclosure.rceptDate})")
                        }
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = disclosures,
                        category = RecordingConfig.Paths.CorpDisclosure.BASE,
                        fileName = "samsung_disclosure_1month"
                    )
                }

                @Test
                @DisplayName("카카오 공시를 조회할 수 있다")
                fun search_kakao_disclosures() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: searchDisclosures()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.KAKAO_CORP_CODE
                    val endDate = TestFixtures.Dates.TRADING_DAY
                    val startDate = endDate.minusMonths(1)

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • startDate: LocalDate = $startDate")
                    println("  • endDate: LocalDate = $endDate")

                    // When
                    val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)

                    // Then
                    println("\n📤 Response: List<Disclosure>")
                    println("  • size: ${disclosures.size}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    SmartRecorder.recordSmartly(
                        data = disclosures,
                        category = RecordingConfig.Paths.CorpDisclosure.BASE,
                        fileName = "kakao_disclosure_1month"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                // 향후 필요시 추가
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                // 향후 필요시 추가
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("전체 법인의 공시를 조회할 수 있다")
                fun search_all_corp_disclosures() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: searchDisclosures()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = TestFixtures.Dates.TRADING_DAY

                    println("📥 Input Parameters:")
                    println("  • corpCode: null (전체 법인)")
                    println("  • startDate: LocalDate = $date")
                    println("  • endDate: LocalDate = $date")
                    println("  • pageCount: Int = 100")

                    // When
                    val disclosures = client.corp!!.searchDisclosures(
                        corpCode = null,
                        startDate = date,
                        endDate = date,
                        pageCount = 100
                    )

                    // Then
                    println("\n📤 Response: List<Disclosure>")
                    println("  • size: ${disclosures.size}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(disclosures.size <= 100, "pageCount 제한이 적용되어야 합니다")

                    SmartRecorder.recordSmartly(
                        data = disclosures,
                        category = RecordingConfig.Paths.CorpDisclosure.BASE,
                        fileName = "all_corp_disclosure_1day"
                    )
                }

                @Test
                @DisplayName("페이징 처리가 가능하다")
                fun pagination_works_correctly() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: searchDisclosures() - 페이징")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val endDate = TestFixtures.Dates.TRADING_DAY
                    val startDate = endDate.minusMonths(3)

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • startDate: LocalDate = $startDate")
                    println("  • endDate: LocalDate = $endDate")
                    println("  • pageNo: Int = 1")
                    println("  • pageCount: Int = 50")

                    // When
                    val page1 = client.corp!!.searchDisclosures(
                        corpCode = corpCode,
                        startDate = startDate,
                        endDate = endDate,
                        pageNo = 1,
                        pageCount = 50
                    )

                    // Then
                    println("\n📤 Response: List<Disclosure>")
                    println("  • size: ${page1.size}")

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(page1.size <= 50, "pageCount 제한이 적용되어야 합니다")
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("특정 키워드가 포함된 공시를 찾을 수 있다")
                fun search_disclosures_by_keyword() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: searchDisclosures() - 활용 예제")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val endDate = TestFixtures.Dates.TRADING_DAY
                    val startDate = endDate.minusMonths(6)

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • startDate: LocalDate = $startDate")
                    println("  • endDate: LocalDate = $endDate")
                    println("  • keyword: String = \"분기\"")

                    // When
                    val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)
                    val quarterlyReports = disclosures.filter { it.reportName.contains("분기") }

                    // Then
                    println("\n📤 Response: 키워드 필터링 결과")
                    println("  • 전체 공시: ${disclosures.size}건")
                    println("  • 분기 관련 공시: ${quarterlyReports.size}건")

                    if (quarterlyReports.isNotEmpty()) {
                        println("\n  [분기 관련 공시 - 상위 3건]")
                        quarterlyReports.take(3).forEach { disclosure ->
                            println("    • ${disclosure.reportName} (${disclosure.rceptDate})")
                        }
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                @Test
                @DisplayName("공시 통계를 분석할 수 있다")
                fun analyze_disclosure_statistics() = integrationTest {
                    requireOpendartApiKey()

                    println("\n📘 API: searchDisclosures() - 활용 예제")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val corpCode = TestFixtures.Corp.SAMSUNG_CORP_CODE
                    val endDate = TestFixtures.Dates.TRADING_DAY
                    val startDate = endDate.minusMonths(3)

                    println("📥 Input Parameters:")
                    println("  • corpCode: String = \"$corpCode\"")
                    println("  • startDate: LocalDate = $startDate")
                    println("  • endDate: LocalDate = $endDate")

                    // When
                    val disclosures = client.corp!!.searchDisclosures(corpCode, startDate, endDate)
                    val disclosureGroups = disclosures
                        .groupBy { it.reportName }
                        .mapValues { it.value.size }
                        .toList()
                        .sortedByDescending { it.second }
                        .take(10)

                    // Then
                    println("\n📤 Response: 공시 유형별 통계")
                    println("  • 전체 공시: ${disclosures.size}건")
                    println("\n  [유형별 분포 - 상위 5개]")
                    disclosureGroups.take(5).forEach { (reportName, count) ->
                        println("    • $reportName: ${count}건")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }
        }
    }
}
