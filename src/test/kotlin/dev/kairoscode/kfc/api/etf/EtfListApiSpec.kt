package dev.kairoscode.kfc.api.etf

import dev.kairoscode.kfc.mock.MockEtfApi
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
@DisplayName("EtfApi - getList()")
class EtfListApiSpec : UnitTestBase() {

    @Nested
    @DisplayName("getList() 사용법")
    inner class GetListUsage {

        @Test
        @DisplayName("기본 사용법 - 전체 ETF 목록을 간단히 조회할 수 있다")
        fun `basic usage - can retrieve all ETF list`() = unitTest {
            // Given: Mock API 설정
            val jsonResponse = loadEtfListResponse("etf_list")
            mockEtfApi = MockEtfApi(listResponse = jsonResponse)
            initClient()

            // When: ETF 목록 조회
            val etfList = client.etf.getList()

            // Then: ETF 목록이 반환됨
            assertThat(etfList).isNotEmpty

            // 예제: 첫 번째 ETF 정보 출력
            val first = etfList.first()
            println("첫 번째 ETF: ${first.name} (${first.ticker})")
            println("  ISIN: ${first.isin}")
            println("  자산구분: ${first.assetClass}")
            println("  운용사: ${first.assetManager}")
        }

        @Test
        @DisplayName("필터링 예제 - 특정 자산구분의 ETF만 조회할 수 있다")
        fun `filtering example - can filter ETFs by asset class`() = unitTest {
            // Given: Mock API 설정
            val jsonResponse = loadEtfListResponse("etf_list")
            mockEtfApi = MockEtfApi(listResponse = jsonResponse)
            initClient()

            // When: 전체 목록 조회 후 주식형만 필터링
            val etfList = client.etf.getList()
            val stockEtfs = etfList.filter { it.assetClass == "주식" }

            // Then: 주식형 ETF만 포함
            assertThat(stockEtfs).allMatch { it.assetClass == "주식" }

            // 예제: 주식형 ETF 목록 출력
            println("주식형 ETF 개수: ${stockEtfs.size}")
            stockEtfs.take(3).forEach { etf ->
                println("  - ${etf.name}: ${etf.assetClass}")
            }
        }

        @Test
        @DisplayName("검색 예제 - 이름으로 ETF를 검색할 수 있다")
        fun `search example - can search ETFs by name`() = unitTest {
            // Given: Mock API 설정
            val jsonResponse = loadEtfListResponse("etf_list")
            mockEtfApi = MockEtfApi(listResponse = jsonResponse)
            initClient()

            // When: 이름에 'TIGER' 포함된 ETF 검색
            val etfList = client.etf.getList()
            val tigerEtfs = etfList.filter { it.name.contains("TIGER") }

            // Then: TIGER ETF들만 반환
            assertThat(tigerEtfs).allMatch { it.name.contains("TIGER") }

            // 예제: 검색 결과 출력
            println("TIGER ETF 검색 결과: ${tigerEtfs.size}개")
            tigerEtfs.take(5).forEach { etf ->
                println("  - ${etf.name} (${etf.ticker})")
            }
        }
    }

    @Nested
    @DisplayName("getList() API 명세")
    inner class GetListSpecification {

        @Test
        @DisplayName("[명세] 반환 타입은 List<EtfListItem>이다")
        fun `specification - returns list of EtfListItem`() = unitTest {
            // Given
            val jsonResponse = loadEtfListResponse("etf_list")
            mockEtfApi = MockEtfApi(listResponse = jsonResponse)
            initClient()

            // When
            val result = client.etf.getList()

            // Then: 반환 타입 확인
            assertThat(result).isInstanceOf(List::class.java)
            assertThat(result).isNotEmpty
        }

        @Test
        @DisplayName("[명세] 각 ETF는 ISIN, 티커, 이름, 자산구분을 포함한다")
        fun `specification - each ETF contains required fields`() = unitTest {
            // Given
            val jsonResponse = loadEtfListResponse("etf_list")
            mockEtfApi = MockEtfApi(listResponse = jsonResponse)
            initClient()

            // When
            val etfList = client.etf.getList()

            // Then: 필수 필드 존재 및 비어있지 않음
            etfList.forEach { etf ->
                assertThat(etf.isin).isNotBlank
                assertThat(etf.ticker).isNotBlank
                assertThat(etf.name).isNotBlank
                assertThat(etf.assetClass).isNotBlank
            }
        }

        @Test
        @DisplayName("[명세] ISIN 코드는 'KR7'로 시작하는 12자리 문자열이다")
        fun `specification - ISIN code format validation`() = unitTest {
            // Given
            val jsonResponse = loadEtfListResponse("etf_list")
            mockEtfApi = MockEtfApi(listResponse = jsonResponse)
            initClient()

            // When
            val etfList = client.etf.getList()

            // Then: ISIN 형식 검증
            etfList.forEach { etf ->
                assertThat(etf.isin)
                    .hasSize(12)
                    .startsWith("KR7")
            }
        }
    }
}
