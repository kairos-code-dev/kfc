package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.api.FutureApi
import dev.kairoscode.kfc.domain.future.FutureOhlcv
import dev.kairoscode.kfc.domain.future.FutureProduct
import java.time.LocalDate

/**
 * 선물 API 공개 구현체
 *
 * FutureApi 인터페이스의 구현체로, KrxFutureApi에 작업을 위임합니다.
 * 이 클래스는 infrastructure 레이어에 속하지만, 공개 API 레이어에 대한 구현을 제공합니다.
 */
internal class FutureApiImpl(
    private val krxFutureApi: KrxFutureApi
) : FutureApi {

    override suspend fun getFutureTickerList(): List<FutureProduct> {
        return krxFutureApi.getFutureTickerList()
    }

    override suspend fun getFutureName(productId: String): String? {
        // productId로 상품 검색
        val products = krxFutureApi.getFutureTickerList()
        return products.find { it.productId == productId }?.name
    }

    override suspend fun getOhlcvByTicker(
        date: LocalDate,
        productId: String,
        alternative: Boolean,
        previousBusiness: Boolean
    ): List<FutureOhlcv> {
        // 먼저 요청한 날짜로 조회
        val data = krxFutureApi.getFutureOhlcv(date, productId)

        // 데이터가 없고 alternative=true인 경우 대체 날짜 조회
        if (data.isEmpty() && alternative) {
            // 대체 날짜 조회 로직 (간단한 구현)
            // 이전/다음 영업일을 찾기 위해 최대 7일 범위 내에서 검색
            val searchRange = if (previousBusiness) {
                (1..7).map { date.minusDays(it.toLong()) }
            } else {
                (1..7).map { date.plusDays(it.toLong()) }
            }

            for (alternativeDate in searchRange) {
                val alternativeData = krxFutureApi.getFutureOhlcv(alternativeDate, productId)
                if (alternativeData.isNotEmpty()) {
                    return alternativeData
                }
            }
        }

        return data
    }
}
