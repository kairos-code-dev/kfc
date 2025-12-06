package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.future.FutureOhlcv
import dev.kairoscode.kfc.domain.future.FutureProduct
import java.time.LocalDate

/**
 * 선물 API 인터페이스
 *
 * 한국 선물시장의 선물 거래 데이터를 조회하는 공개 API입니다.
 * KRX API를 통해 선물 티커 목록, 선물명, OHLCV 데이터 등을 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface FutureApi {
    /**
     * 선물 티커 목록 조회
     *
     * 거래 가능한 모든 선물 상품의 목록을 조회합니다.
     *
     * @return 선물 상품 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT40001)
     */
    suspend fun getFutureTickerList(): List<FutureProduct>

    /**
     * 선물명 조회
     *
     * 선물 상품 ID로 선물명을 조회합니다.
     *
     * @param productId 선물 상품 ID (예: KRDRVFUEST)
     * @return 선물명, 없으면 null
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT40001)
     */
    suspend fun getFutureName(productId: String): String?

    /**
     * 선물 OHLCV 조회
     *
     * 특정 일자의 특정 선물 상품의 전종목(만기별) OHLCV를 조회합니다.
     *
     * @param date 조회 일자 (기본값: 오늘)
     * @param productId 선물 상품 ID (예: KRDRVFUEST)
     * @param alternative 데이터 없을 시 대체 날짜 조회 여부 (기본값: false)
     * @param previousBusiness 대체 날짜 방향 (true: 이전 영업일, false: 다음 영업일) (기본값: true)
     * @return 선물 OHLCV 목록
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT40301)
     */
    suspend fun getOhlcvByTicker(
        date: LocalDate = LocalDate.now(),
        productId: String,
        alternative: Boolean = false,
        previousBusiness: Boolean = true,
    ): List<FutureOhlcv>
}
