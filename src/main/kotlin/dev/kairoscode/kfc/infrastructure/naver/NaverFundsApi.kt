package dev.kairoscode.kfc.infrastructure.naver

import dev.kairoscode.kfc.infrastructure.naver.model.NaverEtfOhlcv
import java.time.LocalDate

/**
 * Naver 증권 펀드/증권상품 API 인터페이스
 *
 * 네이버 증권 차트 API를 통해 조정주가 OHLCV 데이터를 조회하는 공개 API입니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 *
 * **주의사항**:
 * - 네이버 증권 서비스 정책 변경 시 동작하지 않을 수 있습니다
 * - Rate Limiting이 적용되어 있으나, 과도한 요청은 자제해주세요
 * - 프로덕션 환경에서는 안정성을 위해 자체 조정주가 계산 권장
 */
interface NaverFundsApi {

    /**
     * 조정주가 OHLCV 조회
     *
     * 네이버 증권 차트 API에서 조정주가 데이터를 조회합니다.
     * 조정주가는 배당금 지급, 액면분할/병합 등의 이벤트가 반영된 가격입니다.
     *
     * @param ticker 6자리 티커 코드 (예: "069500")
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 조정주가 OHLCV 데이터 목록 (날짜순 정렬)
     * @throws dev.kairoscode.kfc.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getAdjustedOhlcv(
        ticker: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<NaverEtfOhlcv>
}
