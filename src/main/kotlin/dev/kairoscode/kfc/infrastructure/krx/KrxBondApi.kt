package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.domain.bond.BondType
import dev.kairoscode.kfc.domain.bond.BondYield
import dev.kairoscode.kfc.domain.bond.BondYieldSnapshot
import java.time.LocalDate

/**
 * KRX 채권 수익률 API 내부 인터페이스
 *
 * KRX API를 통해 장외 채권시장의 수익률 정보를 조회하는 내부 인터페이스입니다.
 * 이 인터페이스는 라이브러리 내부에서만 사용되며, 공개 API는 [dev.kairoscode.kfc.api.BondApi]입니다.
 *
 * @see dev.kairoscode.kfc.api.BondApi
 */
internal interface KrxBondApi {
    /**
     * 특정 일자의 전체 채권 수익률 조회
     *
     * KRX API MDCSTAT04301을 사용하여 특정 일자의 모든 채권 수익률을 조회합니다.
     *
     * @param date 조회 날짜
     * @return 채권 수익률 스냅샷
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getBondYieldsByDate(date: LocalDate): BondYieldSnapshot

    /**
     * 특정 채권의 기간별 수익률 조회
     *
     * KRX API MDCSTAT04302를 사용하여 특정 채권의 기간별 수익률 추이를 조회합니다.
     *
     * @param bondType 조회할 채권 종류
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 채권 수익률 목록 (시계열)
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     */
    suspend fun getBondYields(
        bondType: BondType,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): List<BondYield>
}
