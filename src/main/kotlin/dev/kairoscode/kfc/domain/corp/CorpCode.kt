package dev.kairoscode.kfc.domain.corp

import java.time.LocalDate

/**
 * OPENDART 고유번호 정보
 *
 * corpCode API에서 반환되는 법인/ETF 고유번호 매핑 데이터
 *
 * @property corpCode OPENDART 고유번호 (8자리)
 * @property corpName 법인명/ETF명
 * @property stockCode 종목코드 (6자리, 상장사만 존재)
 * @property modifyDate 최종 수정일
 */
data class CorpCode(
    val corpCode: String,
    val corpName: String,
    val stockCode: String?,
    val modifyDate: LocalDate,
)
