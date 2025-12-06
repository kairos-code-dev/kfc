package dev.kairoscode.kfc.infrastructure.krx

import dev.kairoscode.kfc.api.BondApi
import dev.kairoscode.kfc.domain.bond.BondType
import dev.kairoscode.kfc.domain.bond.BondYield
import dev.kairoscode.kfc.domain.bond.BondYieldSnapshot
import java.time.LocalDate

/**
 * BondApi 구현체
 *
 * BondApi 인터페이스의 공개 구현체로, KrxBondApi에 위임합니다.
 *
 * @param krxBondApi KRX 채권 API 구현체
 */
internal class BondApiImpl(
    private val krxBondApi: KrxBondApi,
) : BondApi {
    override suspend fun getBondYieldsByDate(date: LocalDate): BondYieldSnapshot = krxBondApi.getBondYieldsByDate(date)

    override suspend fun getBondYields(
        bondType: BondType,
        fromDate: LocalDate,
        toDate: LocalDate,
    ): List<BondYield> = krxBondApi.getBondYields(bondType, fromDate, toDate)
}
