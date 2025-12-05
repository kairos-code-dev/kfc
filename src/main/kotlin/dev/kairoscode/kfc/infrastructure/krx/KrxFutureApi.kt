package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.future.FutureOhlcv
import dev.kairoscode.kfc.domain.future.FutureProduct
import java.time.LocalDate

/**
 * KRX 선물 API 인터페이스 (내부용)
 *
 * KRX API를 통해 선물 관련 데이터를 조회하는 내부 인터페이스입니다.
 * 이 인터페이스는 infrastructure 레이어 내부에서만 사용되며, 외부에 노출되지 않습니다.
 */
internal interface KrxFutureApi {

    /**
     * 선물 티커 목록 조회
     *
     * 거래 가능한 모든 선물 상품 목록을 조회합니다.
     *
     * @return 선물 상품 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT40001)
     */
    suspend fun getFutureTickerList(): List<FutureProduct>

    /**
     * 선물 OHLCV 조회
     *
     * 특정 일자의 특정 선물 상품의 전종목(만기별) OHLCV를 조회합니다.
     *
     * @param date 조회 일자
     * @param productId 선물 상품 ID (예: KRDRVFUEST)
     * @return 선물 OHLCV 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT40301)
     */
    suspend fun getFutureOhlcv(
        date: LocalDate,
        productId: String
    ): List<FutureOhlcv>
}
