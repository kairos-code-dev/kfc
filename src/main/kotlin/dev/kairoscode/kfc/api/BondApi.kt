package dev.kairoscode.kfc.api

import dev.kairoscode.kfc.domain.bond.BondType
import dev.kairoscode.kfc.domain.bond.BondYield
import dev.kairoscode.kfc.domain.bond.BondYieldSnapshot
import java.time.LocalDate

/**
 * 채권 수익률 도메인 통합 API 인터페이스
 *
 * 한국 장외 채권시장의 수익률 정보를 조회하는 공개 API입니다.
 * KRX API를 통해 국고채, 회사채, CD 등의 수익률 및 변동 추이를 제공합니다.
 *
 * 이 인터페이스는 라이브러리의 공개 API 계층에 속하며,
 * 라이브러리 사용자가 직접 사용할 수 있습니다.
 */
interface BondApi {

    /**
     * 특정 일자의 전체 채권 수익률 조회
     *
     * 특정 일자의 모든 채권(국고채, 회사채, CD 등) 수익률 및 변동폭을 조회합니다.
     * 수익률 곡선 생성, 채권 간 스프레드 비교 등에 활용할 수 있습니다.
     *
     * @param date 조회 날짜 (기본값: 오늘)
     * @return 채권 수익률 스냅샷 (11개 채권 포함)
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04301)
     */
    suspend fun getBondYieldsByDate(date: LocalDate = LocalDate.now()): BondYieldSnapshot

    /**
     * 특정 채권의 기간별 수익률 추이 조회
     *
     * 특정 채권의 일별 수익률 시계열 데이터를 조회합니다.
     * 수익률 추이 분석, 차트 시각화 등에 활용할 수 있습니다.
     *
     * @param bondType 조회할 채권 종류
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return 채권 수익률 목록 (시계열, 오름차순 정렬)
     * @throws dev.kairoscode.kfc.domain.exception.KfcException 네트워크 에러, 파싱 실패, API 에러 발생 시
     * @source KRX API (MDCSTAT04302)
     */
    suspend fun getBondYields(
        bondType: BondType,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<BondYield>
}
