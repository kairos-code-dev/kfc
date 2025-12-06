package dev.kairoscode.kfc.unit.utils

import dev.kairoscode.kfc.domain.corp.CorpCode
import dev.kairoscode.kfc.domain.corp.DividendInfo
import dev.kairoscode.kfc.domain.funds.DailyPrice
import dev.kairoscode.kfc.domain.funds.DetailedInfo
import dev.kairoscode.kfc.domain.funds.FundListItem
import dev.kairoscode.kfc.domain.funds.Ohlcv
import dev.kairoscode.kfc.domain.funds.PortfolioConstituent
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal

// =============================================================================
// 테스트용 확장 함수 모음
// 공통 검증 로직을 확장 함수로 제공하여 테스트 코드의 가독성과 재사용성을 높입니다.
// =============================================================================

/**
 * FundListItem 유효성 검증
 *
 * ISIN, 티커, 이름이 비어있지 않은지 확인합니다.
 */
fun FundListItem.assertValidFundListItem() {
    assertThat(this.isin).isNotEmpty()
    assertThat(this.ticker).isNotEmpty()
    assertThat(this.name).isNotEmpty()
}

/**
 * Ohlcv 유효성 검증
 *
 * 날짜, OHLCV 값의 유효성을 확인합니다.
 */
fun Ohlcv.assertValidOhlcv() {
    assertThat(this.tradeDate).isNotNull()
    assertThat(this.openPrice).isGreaterThan(0)
    assertThat(this.highPrice).isGreaterThanOrEqualTo(this.lowPrice)
    assertThat(this.closePrice).isGreaterThan(0)
    assertThat(this.volume).isGreaterThanOrEqualTo(0)
}

/**
 * DetailedInfo 유효성 검증
 */
fun DetailedInfo.assertValidDetailedInfo() {
    assertThat(this.isin).isNotEmpty()
    assertThat(this.name).isNotEmpty()
    assertThat(this.nav).isGreaterThanOrEqualTo(BigDecimal.ZERO)
}

/**
 * DailyPrice 유효성 검증
 */
fun DailyPrice.assertValidDailyPrice() {
    assertThat(this.ticker).isNotEmpty()
    assertThat(this.name).isNotEmpty()
    assertThat(this.closePrice).isGreaterThan(0)
}

/**
 * PortfolioConstituent 유효성 검증
 */
fun PortfolioConstituent.assertValidPortfolioItem() {
    assertThat(this.constituentName).isNotEmpty()
    assertThat(this.weightPercent).isGreaterThanOrEqualTo(BigDecimal.ZERO)
}

/**
 * CorpCode 유효성 검증
 */
fun CorpCode.assertValidCorpCode() {
    assertThat(this.corpCode).isNotEmpty()
    assertThat(this.corpName).isNotEmpty()
}

/**
 * DividendInfo 유효성 검증
 */
fun DividendInfo.assertValidDividend() {
    assertThat(this.corpCode).isNotEmpty()
    assertThat(this.corpName).isNotEmpty()
}

/**
 * 리스트가 비어있지 않은지 검증
 */
fun <T> List<T>.assertNotEmpty() {
    assertThat(this)
        .withFailMessage("리스트는 비어있을 수 없습니다")
        .isNotEmpty()
}

/**
 * 리스트 크기 검증
 *
 * @param expectedSize 예상 크기
 */
fun <T> List<T>.assertSize(expectedSize: Int) {
    assertThat(this)
        .withFailMessage("리스트 크기는 $expectedSize 여야 합니다")
        .hasSize(expectedSize)
}

/**
 * 리스트 최소 크기 검증
 *
 * @param minSize 최소 크기
 */
fun <T> List<T>.assertMinSize(minSize: Int) {
    assertThat(this.size)
        .withFailMessage("리스트 크기는 최소 $minSize 이상이어야 합니다")
        .isGreaterThanOrEqualTo(minSize)
}

/**
 * 리스트 최대 크기 검증
 *
 * @param maxSize 최대 크기
 */
fun <T> List<T>.assertMaxSize(maxSize: Int) {
    assertThat(this.size)
        .withFailMessage("리스트 크기는 최대 $maxSize 이하여야 합니다")
        .isLessThanOrEqualTo(maxSize)
}

/**
 * 모든 요소가 조건을 만족하는지 검증
 */
fun <T> List<T>.assertAllMatch(
    predicate: (T) -> Boolean,
    description: String = "조건",
) {
    assertThat(this.all(predicate))
        .withFailMessage("모든 요소가 $description 을 만족해야 합니다")
        .isTrue()
}
