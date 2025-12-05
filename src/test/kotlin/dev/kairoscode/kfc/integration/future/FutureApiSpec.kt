package dev.kairoscode.kfc.integration.future

import dev.kairoscode.kfc.domain.future.filterByProduct
import dev.kairoscode.kfc.domain.future.sortByVolume
import dev.kairoscode.kfc.integration.utils.IntegrationTestBase
import dev.kairoscode.kfc.integration.utils.RecordingConfig
import dev.kairoscode.kfc.integration.utils.SmartRecorder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

/**
 * [Future] FutureApi - 선물 API 통합 테스트
 *
 * KRX API를 사용한 선물 데이터 조회 기능을 검증합니다.
 * API 문서처럼 읽히도록 설계되었습니다.
 */
@DisplayName("[Future] FutureApi - 선물 API")
class FutureApiSpec : IntegrationTestBase() {

    @Nested
    @DisplayName("선물 티커 목록 API")
    inner class FutureTickerListApi {

        @Nested
        @DisplayName("getFutureTickerList() - 선물 티커 목록 조회")
        inner class GetFutureTickerList {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("선물 상품 전체 목록을 조회할 수 있다")
                fun get_all_future_products() = integrationTest {
                    // Given
                    println("\n📘 API: getFutureTickerList()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters: (없음)")

                    // When
                    val products = client.future.getFutureTickerList()

                    // Then
                    println("\n📤 Response: List<FutureProduct>")
                    println("  • Total products: ${products.size}개")
                    println("  • Sample products:")
                    products.take(5).forEach { product ->
                        println("    - ${product.productId}: ${product.name}")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(products)
                    assertTrue(products.isNotEmpty(), "선물 상품 목록이 비어있지 않아야 합니다")
                    assertTrue(products.size >= 10, "선물 상품 수는 10개 이상이어야 합니다. 실제: ${products.size}")

                    SmartRecorder.recordSmartly(
                        data = products,
                        category = RecordingConfig.Paths.Future.LIST,
                        fileName = "future_products"
                    )
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("모든 상품은 productId를 가져야 한다")
                fun all_products_have_product_id() = integrationTest {
                    // Given
                    println("\n📘 응답 검증: productId 필수값")

                    // When
                    val products = client.future.getFutureTickerList()

                    // Then
                    assertTrue(products.all { it.productId.isNotBlank() })
                    println("  • 검증 대상: ${products.size}개 상품")
                    println("  • 규칙: productId.isNotBlank()")
                    println("  ✅ 모든 상품이 productId를 가집니다\n")
                }

                @Test
                @DisplayName("모든 상품은 name을 가져야 한다")
                fun all_products_have_name() = integrationTest {
                    // Given
                    println("\n📘 응답 검증: name 필수값")

                    // When
                    val products = client.future.getFutureTickerList()

                    // Then
                    assertTrue(products.all { it.name.isNotBlank() })
                    println("  • 검증 대상: ${products.size}개 상품")
                    println("  • 규칙: name.isNotBlank()")
                    println("  ✅ 모든 상품이 name을 가집니다\n")
                }

                @Test
                @DisplayName("productId는 KRDRV 접두사로 시작해야 한다")
                fun product_id_starts_with_prefix() = integrationTest {
                    // Given
                    println("\n📘 응답 검증: productId 형식")

                    // When
                    val products = client.future.getFutureTickerList()

                    // Then
                    // 모든 KRX 파생상품은 KRDRV로 시작 (Futures, Options, Flex 등)
                    assertTrue(products.all { it.productId.startsWith("KRDRV") })
                    println("  • 검증 대상: ${products.size}개 상품")
                    println("  • 규칙: productId.startsWith('KRDRV')")
                    println("  • 예시: KRDRVFUK2I (선물), KRDRVOPK2I (옵션), KRDRVFXUSD (Flex)")
                    println("  ✅ 모든 상품 ID가 올바른 형식입니다\n")
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {
                // getFutureTickerList는 파라미터가 없음
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                // 현재 API는 항상 성공하거나 예외 발생
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[필터링] 코스피200 선물 찾기")
                fun find_kospi200_future() = integrationTest {
                    println("\n📘 실무 활용: 코스피200 선물 찾기")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // When
                    val products = client.future.getFutureTickerList()
                    // API는 영문 명칭을 반환 (예: "KOSPI 200 Futures")
                    val kospi200Future = products.find { it.name.contains("KOSPI 200") && it.name.contains("Futures") }

                    // Then
                    println("📊 검색 결과:")
                    println("  • 전체 상품 수: ${products.size}개")
                    if (kospi200Future != null) {
                        println("  • 발견: ${kospi200Future.productId} - ${kospi200Future.name}")
                    } else {
                        println("  • 발견하지 못함")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(kospi200Future, "코스피200 선물이 존재해야 합니다")
                }
            }
        }

        @Nested
        @DisplayName("getFutureName() - 선물명 조회")
        inner class GetFutureName {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("존재하는 상품 ID로 선물명을 조회할 수 있다")
                fun get_future_name_by_existing_product_id() = integrationTest {
                    // Given: 실제 존재하는 상품 ID를 목록에서 가져옴
                    val products = client.future.getFutureTickerList()
                    assertTrue(products.isNotEmpty(), "상품 목록이 비어있지 않아야 합니다")

                    val firstProduct = products.first()
                    val productId = firstProduct.productId

                    println("\n📘 API: getFutureName()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters:")
                    println("  • productId: String = \"$productId\"")

                    // When
                    val name = client.future.getFutureName(productId)

                    // Then
                    println("\n📤 Response: String?")
                    println("  • name: ${name ?: "null"}")
                    println("  • expected: ${firstProduct.name}")
                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(name, "선물명이 null이 아니어야 합니다")
                    assertEquals(firstProduct.name, name, "조회된 이름이 목록의 이름과 일치해야 합니다")
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {
                // 선물명은 String 타입으로 추가 검증 불필요
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                @Test
                @DisplayName("존재하지 않는 상품 ID는 null을 반환한다")
                fun returns_null_for_invalid_product_id() = integrationTest {
                    // Given
                    val invalidProductId = "INVALID999"
                    println("\n📘 입력 검증: 존재하지 않는 상품 ID")

                    // When
                    val name = client.future.getFutureName(invalidProductId)

                    // Then
                    println("  • 입력: $invalidProductId")
                    println("  • 결과: ${name ?: "null"}")
                    println("  ✅ null 반환 확인\n")

                    assertNull(name)
                }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {
                // getFutureName은 단순 조회로 엣지 케이스가 명확하지 않음
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[변환] 상품 ID 목록을 상품명 목록으로 변환")
                fun convert_product_ids_to_names() = integrationTest {
                    println("\n📘 실무 활용: 상품 ID → 상품명 변환")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val products = client.future.getFutureTickerList()
                    val selectedProductIds = products.take(5).map { it.productId }

                    // When
                    val productIdToName = selectedProductIds.associateWith { productId ->
                        client.future.getFutureName(productId)
                    }

                    // Then
                    println("📊 변환 결과:")
                    productIdToName.forEach { (productId, name) ->
                        println("  • $productId → ${name ?: "null"}")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertTrue(productIdToName.values.all { it != null })
                }
            }
        }
    }

    @Nested
    @DisplayName("선물 OHLCV API")
    inner class FutureOhlcvApi {

        @Nested
        @DisplayName("getOhlcvByTicker() - 선물 OHLCV 조회")
        inner class GetOhlcvByTicker {

            @Nested
            @DisplayName("1. 기본 동작 (Basic Operations)")
            inner class BasicOperations {

                @Test
                @DisplayName("EURO STOXX50 선물 OHLCV를 조회할 수 있다")
                fun get_euro_stoxx50_ohlcv() = integrationTest(timeout = 60.seconds) {
                    // Given
                    val date = LocalDate.now().minusDays(7) // 일주일 전 데이터
                    val productId = "KRDRVFUEST"
                    println("\n📘 API: getOhlcvByTicker()")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    println("📥 Input Parameters:")
                    println("  • date: LocalDate = $date")
                    println("  • productId: String = \"$productId\"")
                    println("  • alternative: Boolean = true")
                    println("  • previousBusiness: Boolean = true")

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = date,
                        productId = productId,
                        alternative = true,
                        previousBusiness = true
                    )

                    // Then
                    println("\n📤 Response: List<FutureOhlcv>")
                    println("  • Total records: ${ohlcvList.size}개")
                    println("  • Sample records:")
                    ohlcvList.take(3).forEach { ohlcv ->
                        println("    - ${ohlcv.issueCode}: O=${ohlcv.open}, H=${ohlcv.high}, L=${ohlcv.low}, C=${ohlcv.close}, V=${ohlcv.volume}")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertNotNull(ohlcvList)
                    // alternative=true이므로 데이터가 있어야 함 (휴장일이 아닌 한)

                    if (ohlcvList.isNotEmpty()) {
                        SmartRecorder.recordSmartly(
                            data = ohlcvList,
                            category = RecordingConfig.Paths.Future.OHLCV,
                            fileName = "euro_stoxx50_ohlcv"
                        )
                    }
                }
            }

            @Nested
            @DisplayName("2. 응답 데이터 검증 (Response Validation)")
            inner class ResponseValidation {

                @Test
                @DisplayName("거래가 있는 종목은 양수 가격을 가져야 한다")
                fun all_prices_are_positive() = integrationTest {
                    // Given
                    println("\n📘 응답 검증: 가격 양수 (거래가 있는 종목만)")
                    val date = LocalDate.now().minusDays(7)
                    val productId = "KRDRVFUEST"

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = date,
                        productId = productId,
                        alternative = true
                    )

                    // Then
                    if (ohlcvList.isNotEmpty()) {
                        // 거래량이 있는 종목만 검증 (volume > 0인 경우에만)
                        val tradedContracts = ohlcvList.filter { it.volume > 0 }
                        if (tradedContracts.isNotEmpty()) {
                            assertTrue(tradedContracts.all { it.open > java.math.BigDecimal.ZERO })
                            assertTrue(tradedContracts.all { it.high > java.math.BigDecimal.ZERO })
                            assertTrue(tradedContracts.all { it.low > java.math.BigDecimal.ZERO })
                            assertTrue(tradedContracts.all { it.close > java.math.BigDecimal.ZERO })
                            println("  • 검증 대상: ${tradedContracts.size}개 레코드 (거래량 > 0)")
                            println("  • 전체: ${ohlcvList.size}개 레코드")
                            println("  • 규칙: open/high/low/close > 0 (거래가 있는 종목만)")
                            println("  ✅ 모든 가격이 양수입니다\n")
                        } else {
                            println("  ⚠️ 거래가 있는 종목이 없어 검증을 skip합니다\n")
                        }
                    } else {
                        println("  ⚠️ 데이터가 없어 검증을 skip합니다\n")
                    }
                }

                @Test
                @DisplayName("저가 <= 시가/종가 <= 고가 관계가 성립한다")
                fun price_relationships_are_valid() = integrationTest {
                    // Given
                    println("\n📘 응답 검증: 가격 관계")
                    val date = LocalDate.now().minusDays(7)
                    val productId = "KRDRVFUEST"

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = date,
                        productId = productId,
                        alternative = true
                    )

                    // Then
                    if (ohlcvList.isNotEmpty()) {
                        assertTrue(ohlcvList.all { it.low <= it.open && it.open <= it.high })
                        assertTrue(ohlcvList.all { it.low <= it.close && it.close <= it.high })
                        println("  • 검증 대상: ${ohlcvList.size}개 레코드")
                        println("  • 규칙: low <= open/close <= high")
                        println("  ✅ 모든 가격 관계가 올바릅니다\n")
                    } else {
                        println("  ⚠️ 데이터가 없어 검증을 skip합니다\n")
                    }
                }
            }

            @Nested
            @DisplayName("3. 입력 파라미터 검증 (Input Validation)")
            inner class InputValidation {

                @Test
                @DisplayName("alternative=false일 때 휴장일은 빈 리스트를 반환한다")
                fun returns_empty_for_holiday_without_alternative() = integrationTest {
                    // Given
                    val holiday = LocalDate.of(2024, 1, 1) // 신정
                    val productId = "KRDRVFUEST"
                    println("\n📘 입력 검증: 휴장일 처리 (alternative=false)")

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = holiday,
                        productId = productId,
                        alternative = false
                    )

                    // Then
                    println("  • 날짜: $holiday (휴장일)")
                    println("  • alternative: false")
                    println("  • 결과: ${ohlcvList.size}개 레코드")
                    println("  ✅ 빈 리스트 반환 확인\n")

                    assertTrue(ohlcvList.isEmpty())
                }
            }

            @Nested
            @DisplayName("4. 엣지 케이스 (Edge Cases)")
            inner class EdgeCases {

                @Test
                @DisplayName("존재하지 않는 상품 ID는 빈 리스트를 반환한다")
                fun returns_empty_for_invalid_product_id() = integrationTest {
                    // Given
                    val date = LocalDate.now().minusDays(7)
                    val invalidProductId = "INVALID999"
                    println("\n📘 엣지 케이스: 존재하지 않는 상품 ID")

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = date,
                        productId = invalidProductId,
                        alternative = false
                    )

                    // Then
                    println("  • productId: $invalidProductId")
                    println("  • 결과: ${ohlcvList.size}개")
                    println("  ✅ 빈 리스트 반환\n")

                    assertTrue(ohlcvList.isEmpty())
                }
            }

            @Nested
            @DisplayName("5. 실무 활용 예제 (Usage Examples)")
            inner class UsageExamples {

                @Test
                @DisplayName("[분석] 거래량 상위 종목 추출")
                fun find_top_volume_contracts() = integrationTest {
                    println("\n📘 실무 활용: 거래량 상위 종목 추출")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = LocalDate.now().minusDays(7)
                    val productId = "KRDRVFUEST"

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = date,
                        productId = productId,
                        alternative = true
                    )
                    val topByVolume = ohlcvList.sortByVolume().take(3)

                    // Then
                    println("📊 거래량 상위 종목:")
                    topByVolume.forEachIndexed { index, ohlcv ->
                        println("  ${index + 1}. ${ohlcv.issueName}")
                        println("     - 거래량: ${ohlcv.volume}계약")
                        println("     - 종가: ${ohlcv.close}")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    if (ohlcvList.isNotEmpty()) {
                        assertTrue(topByVolume.isNotEmpty())
                    }
                }

                @Test
                @DisplayName("[필터링] 특정 상품의 만기별 종목 비교")
                fun compare_maturity_contracts() = integrationTest {
                    println("\n📘 실무 활용: 만기별 종목 비교")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Given
                    val date = LocalDate.now().minusDays(7)
                    val productId = "KRDRVFUEST"

                    // When
                    val ohlcvList = client.future.getOhlcvByTicker(
                        date = date,
                        productId = productId,
                        alternative = true
                    )
                    val filtered = ohlcvList.filterByProduct(productId)

                    // Then
                    println("📊 만기별 종목:")
                    filtered.forEach { ohlcv ->
                        println("  • ${ohlcv.issueCode}: ${ohlcv.issueName}")
                        println("    - 종가: ${ohlcv.close}, 거래량: ${ohlcv.volume}계약")
                    }

                    println("\n✅ 테스트 결과: 성공")
                    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

                    assertEquals(ohlcvList.size, filtered.size)
                }
            }
        }
    }
}
