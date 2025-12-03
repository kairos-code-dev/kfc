package dev.kairoscode.kfc.utils

import dev.kairoscode.kfc.domain.funds.*
import dev.kairoscode.kfc.domain.corp.*
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.time.LocalDate

/**
 * KFC 도메인 전용 Assertion 헬퍼
 *
 * 테스트 실패 시 상세한 디버깅 정보를 제공합니다.
 */
object KfcAssertions {

    // =========================================
    // ETF/Fund 관련 Assertions
    // =========================================

    /**
     * ETF 목록이 유효한지 검증합니다.
     */
    fun assertValidEtfList(etfList: List<FundListItem>, context: String = "") {
        assertThat(etfList)
            .describedAs("ETF 목록이 비어있습니다 %s", context)
            .isNotEmpty

        etfList.forEachIndexed { index, etf ->
            assertValidFundListItem(etf, "[$index] ${etf.name}")
        }
    }

    /**
     * FundListItem이 유효한지 검증합니다.
     */
    fun assertValidFundListItem(item: FundListItem, context: String = "") {
        assertValidIsin(item.isin, context)
        assertThat(item.ticker)
            .describedAs("티커가 비어있습니다 %s (ISIN: %s)", context, item.isin)
            .isNotBlank
        assertThat(item.name)
            .describedAs("이름이 비어있습니다 %s (ISIN: %s)", context, item.isin)
            .isNotBlank
    }

    /**
     * ISIN 형식이 유효한지 검증합니다.
     */
    fun assertValidIsin(isin: String, context: String = "") {
        assertThat(isin)
            .describedAs("ISIN이 12자리가 아닙니다 %s (value: %s, length: %d)", context, isin, isin.length)
            .hasSize(12)
        assertThat(isin)
            .describedAs("ISIN이 'KR7'로 시작하지 않습니다 %s (value: %s)", context, isin)
            .startsWith("KR7")
    }

    /**
     * 가격이 유효한지 검증합니다 (양수).
     */
    fun assertValidPrice(price: BigDecimal, fieldName: String, context: String = "") {
        assertThat(price)
            .describedAs("%s 가격이 양수가 아닙니다 %s (value: %s)", fieldName, context, price)
            .isPositive
    }

    /**
     * 가격이 0 이상인지 검증합니다.
     */
    fun assertNonNegativePrice(price: BigDecimal, fieldName: String, context: String = "") {
        assertThat(price)
            .describedAs("%s 가격이 음수입니다 %s (value: %s)", fieldName, context, price)
            .isGreaterThanOrEqualTo(BigDecimal.ZERO)
    }

    /**
     * DetailedInfo가 유효한지 검증합니다.
     */
    fun assertValidDetailedInfo(info: DetailedInfo, context: String = "") {
        assertValidIsin(info.isin, context)
        assertValidPrice(info.closePrice, "종가", context)
        assertThat(info.tradeDate)
            .describedAs("거래일이 미래입니다 %s (value: %s)", context, info.tradeDate)
            .isBeforeOrEqualTo(LocalDate.now())
    }

    /**
     * 포트폴리오 구성 목록이 유효한지 검증합니다.
     */
    fun assertValidPortfolio(portfolio: List<PortfolioConstituent>, context: String = "") {
        assertThat(portfolio)
            .describedAs("포트폴리오가 비어있습니다 %s", context)
            .isNotEmpty

        portfolio.forEach { item ->
            assertThat(item.constituentName)
                .describedAs("종목명이 비어있습니다 %s", context)
                .isNotBlank()
        }
    }

    // =========================================
    // Corp 관련 Assertions
    // =========================================

    /**
     * 기업 코드가 유효한지 검증합니다.
     */
    fun assertValidCorpCode(corpCode: CorpCode, context: String = "") {
        assertThat(corpCode.corpCode)
            .describedAs("기업코드가 비어있습니다 %s", context)
            .isNotBlank
        assertThat(corpCode.corpName)
            .describedAs("기업명이 비어있습니다 %s (code: %s)", context, corpCode.corpCode)
            .isNotBlank
    }

    /**
     * 배당 정보가 유효한지 검증합니다.
     */
    fun assertValidDividendInfo(info: DividendInfo, context: String = "") {
        assertThat(info.corpCode)
            .describedAs("기업코드가 비어있습니다 %s", context)
            .isNotBlank
        assertThat(info.corpName)
            .describedAs("기업명이 비어있습니다 %s", context)
            .isNotBlank
    }

    // =========================================
    // 컬렉션 유틸리티
    // =========================================

    /**
     * 리스트가 비어있지 않은지 검증합니다.
     */
    fun <T> assertNotEmptyList(list: List<T>, listName: String, context: String = "") {
        assertThat(list)
            .describedAs("%s 목록이 비어있습니다 %s (size: %d)", listName, context, list.size)
            .isNotEmpty
    }

    /**
     * 리스트 크기가 최소값 이상인지 검증합니다.
     */
    fun <T> assertMinSize(list: List<T>, minSize: Int, listName: String, context: String = "") {
        assertThat(list.size)
            .describedAs("%s 목록 크기가 최소 %d 이상이어야 합니다 %s (actual: %d)", listName, minSize, context, list.size)
            .isGreaterThanOrEqualTo(minSize)
    }
}
