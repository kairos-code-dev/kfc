package dev.kairoscode.kfc.domain.future

import dev.kairoscode.kfc.domain.stock.PriceChangeType
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 선물 OHLCV 데이터
 *
 * 특정 일자의 선물 종목(만기별) OHLCV 데이터입니다.
 * 선물 가격은 소수점을 포함할 수 있으므로 BigDecimal을 사용합니다.
 *
 * @property date 거래일
 * @property productId 상품 ID (예: KRDRVFUEST)
 * @property issueCode 종목 코드 (만기 포함, 예: KRDRVFUEST202212)
 * @property issueName 종목명 (만기 포함, 예: EURO STOXX50 선물 2022/12)
 * @property open 시가
 * @property high 고가
 * @property low 저가
 * @property close 종가
 * @property changeFromPrev 전일대비 가격
 * @property changeRate 등락률 (%)
 * @property priceChangeType 등락 구분
 * @property volume 거래량 (계약 수)
 * @property tradingValue 거래대금 (원)
 */
data class FutureOhlcv(
    val date: LocalDate,
    val productId: String,
    val issueCode: String,
    val issueName: String,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val changeFromPrev: BigDecimal?,
    val changeRate: BigDecimal?,
    val priceChangeType: PriceChangeType?,
    val volume: Long,
    val tradingValue: Long?,
) {
    /**
     * 가격이 상승했는지 확인
     *
     * @return 상승 여부
     */
    fun isRising(): Boolean = priceChangeType == PriceChangeType.RISE

    /**
     * 가격이 하락했는지 확인
     *
     * @return 하락 여부
     */
    fun isFalling(): Boolean = priceChangeType == PriceChangeType.FALL

    /**
     * 가격이 보합인지 확인
     *
     * @return 보합 여부
     */
    fun isUnchanged(): Boolean = priceChangeType == PriceChangeType.UNCHANGED
}

/**
 * 특정 상품으로 필터링
 *
 * @param productId 필터링할 상품 ID
 * @return 필터링된 OHLCV 리스트
 */
fun List<FutureOhlcv>.filterByProduct(productId: String): List<FutureOhlcv> = filter { it.productId == productId }

/**
 * 거래량 순으로 정렬 (내림차순)
 *
 * @return 거래량 순으로 정렬된 OHLCV 리스트
 */
fun List<FutureOhlcv>.sortByVolume(): List<FutureOhlcv> = sortedByDescending { it.volume }
