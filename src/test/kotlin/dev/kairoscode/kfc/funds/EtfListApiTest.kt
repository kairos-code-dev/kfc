package dev.kairoscode.kfc.funds

import dev.kairoscode.kfc.funds.fake.FakeFundsApi
import dev.kairoscode.kfc.utils.KfcAssertions
import dev.kairoscode.kfc.utils.TestData
import dev.kairoscode.kfc.utils.UnitTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * EtfApi.getList() Unit Test
 *
 * ETF 목록 조회 API의 동작을 검증하는 Unit Test입니다.
 * Live Test에서 레코딩된 JSON 응답을 사용하여 테스트합니다.
 */
@DisplayName("FundsApi - getList()")
class EtfListApiTest : UnitTestBase() {

    // =========================================
    // 정상 케이스
    // =========================================
    @Nested
    @DisplayName("정상 케이스")
    inner class SuccessCases {

        @Test
        @DisplayName("전체 ETF 목록을 조회할 수 있다")
        fun getList_noFilter_returnsAllEtfs() = unitTest {
            // Given: Fake API 설정 - 레코딩된 ETF 목록 응답 사용
            val jsonResponse = loadEtfListResponse("etf_list")
            fakeFundsApi = FakeFundsApi(listResponse = jsonResponse)
            initClient()

            // When: ETF 목록 전체 조회
            val etfList = client.funds.getList()

            // Then: 유효한 ETF 목록이 반환됨
            assertThat(etfList)
                .describedAs("ETF 목록이 비어있습니다 (size: %d)", etfList.size)
                .isNotEmpty

            // 예제: 첫 번째 ETF 정보 출력
            val first = etfList.first()
            println("첫 번째 ETF: ${first.name} (${first.ticker})")
            println("  ISIN: ${first.isin}")
            println("  자산구분: ${first.assetClass}")
            println("  운용사: ${first.assetManager}")
        }

        @Test
        @DisplayName("특정 자산구분의 ETF만 필터링할 수 있다")
        fun getList_filterByAssetClass_returnsFilteredEtfs() = unitTest {
            // Given: Fake API 설정 - 레코딩된 ETF 목록 응답 사용
            val jsonResponse = loadEtfListResponse("etf_list")
            fakeFundsApi = FakeFundsApi(listResponse = jsonResponse)
            initClient()

            // When: 전체 목록 조회 후 주식형만 필터링
            val etfList = client.funds.getList()
            val stockEtfs = etfList.filter { it.assetClass == "주식" }

            // Then: 주식형 ETF만 포함되어 있음
            assertThat(stockEtfs)
                .describedAs("주식형 ETF가 없습니다 (전체: %d)", etfList.size)
                .isNotEmpty
                .allMatch { it.assetClass == "주식" }

            // 예제: 주식형 ETF 목록 출력
            println("주식형 ETF 개수: ${stockEtfs.size}")
            stockEtfs.take(3).forEach { etf ->
                println("  - ${etf.name}: ${etf.assetClass}")
            }
        }

        @Test
        @DisplayName("이름으로 ETF를 검색할 수 있다")
        fun getList_searchByName_returnsMatchingEtfs() = unitTest {
            // Given: Fake API 설정 - 레코딩된 ETF 목록 응답 사용
            val jsonResponse = loadEtfListResponse("etf_list")
            fakeFundsApi = FakeFundsApi(listResponse = jsonResponse)
            initClient()

            // When: 이름에 'TIGER' 포함된 ETF 검색
            val etfList = client.funds.getList()
            val tigerEtfs = etfList.filter { it.name.contains("TIGER") }

            // Then: TIGER ETF들만 반환됨
            assertThat(tigerEtfs)
                .describedAs("TIGER ETF가 없습니다 (전체: %d)", etfList.size)
                .isNotEmpty
                .allMatch { it.name.contains("TIGER") }

            // 예제: 검색 결과 출력
            println("TIGER ETF 검색 결과: ${tigerEtfs.size}개")
            tigerEtfs.take(5).forEach { etf ->
                println("  - ${etf.name} (${etf.ticker})")
            }
        }
    }

    // =========================================
    // API 명세 검증
    // =========================================
    @Nested
    @DisplayName("API 명세")
    inner class ApiSpecification {

        @Test
        @DisplayName("반환 타입은 List<FundListItem>이다")
        fun getList_specification_returnsListOfFundListItem() = unitTest {
            // Given: Fake API 설정 - 레코딩된 ETF 목록 응답 사용
            val jsonResponse = loadEtfListResponse("etf_list")
            fakeFundsApi = FakeFundsApi(listResponse = jsonResponse)
            initClient()

            // When: ETF 목록 조회
            val result = client.funds.getList()

            // Then: 반환 타입이 List이며 비어있지 않음
            assertThat(result)
                .describedAs("반환 타입이 List가 아닙니다")
                .isInstanceOf(List::class.java)
            assertThat(result)
                .describedAs("반환된 목록이 비어있습니다")
                .isNotEmpty
        }

        @Test
        @DisplayName("각 ETF는 ISIN, 티커, 이름, 자산구분을 포함한다")
        fun getList_specification_containsRequiredFields() = unitTest {
            // Given: Fake API 설정 - 레코딩된 ETF 목록 응답 사용
            val jsonResponse = loadEtfListResponse("etf_list")
            fakeFundsApi = FakeFundsApi(listResponse = jsonResponse)
            initClient()

            // When: ETF 목록 조회
            val etfList = client.funds.getList()

            // Then: 모든 ETF가 필수 필드를 포함하고 비어있지 않음
            etfList.forEach { etf ->
                assertThat(etf.isin)
                    .describedAs("ISIN이 비어있습니다 (ETF: %s)", etf.name)
                    .isNotBlank
                assertThat(etf.ticker)
                    .describedAs("티커가 비어있습니다 (ETF: %s)", etf.name)
                    .isNotBlank
                assertThat(etf.name)
                    .describedAs("이름이 비어있습니다 (ISIN: %s)", etf.isin)
                    .isNotBlank
                assertThat(etf.assetClass)
                    .describedAs("자산구분이 비어있습니다 (ETF: %s)", etf.name)
                    .isNotBlank
            }
        }

        @Test
        @DisplayName("ISIN 코드는 'KR7'로 시작하는 12자리 문자열이다")
        fun getList_specification_validIsinFormat() = unitTest {
            // Given: Fake API 설정 - 레코딩된 ETF 목록 응답 사용
            val jsonResponse = loadEtfListResponse("etf_list")
            fakeFundsApi = FakeFundsApi(listResponse = jsonResponse)
            initClient()

            // When: ETF 목록 조회
            val etfList = client.funds.getList()

            // Then: 모든 ISIN이 올바른 형식을 갖춤
            etfList.forEach { etf ->
                KfcAssertions.assertValidIsin(etf.isin, "(ETF: ${etf.name})")
            }
        }
    }
}
