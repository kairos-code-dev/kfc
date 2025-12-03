package dev.kairoscode.kfc.domain.corp

import java.time.LocalDate

/**
 * 증자/감자 정보
 *
 * OPENDART API irdsSttus에서 반환되는 주식 분할/병합 정보
 *
 * @property rceptNo 접수번호
 * @property corpCode OPENDART 고유번호
 * @property corpName 법인명
 * @property eventDate 증자/감자일
 * @property eventType 증자/감자 방법 (주식분할, 주식병합 등)
 * @property stockKind 주식 종류
 * @property quantity 주식수
 * @property parValuePerShare 1주당 액면가
 * @property totalAmount 액면총액
 */
data class StockSplitInfo(
    val rceptNo: String,
    val corpCode: String,
    val corpName: String,
    val eventDate: LocalDate,
    val eventType: String,
    val stockKind: String,
    val quantity: Long,
    val parValuePerShare: Int,
    val totalAmount: Long
)
