package dev.kairoscode.kfc.domain.corp

import java.time.LocalDate

/**
 * 공시 정보
 *
 * OPENDART API list에서 반환되는 공시 목록 아이템
 *
 * @property corpCode OPENDART 고유번호
 * @property corpName 법인명
 * @property stockCode 종목코드 (6자리, 상장사만 존재)
 * @property corpCls 법인구분 (Y: 유가증권, K: 코스닥, N: 코넥스, E: 기타)
 * @property reportName 보고서명
 * @property rceptNo 접수번호
 * @property filerName 제출인명
 * @property rceptDate 접수일자
 * @property remark 비고
 */
data class DisclosureItem(
    val corpCode: String,
    val corpName: String,
    val stockCode: String?,
    val corpCls: String,
    val reportName: String,
    val rceptNo: String,
    val filerName: String,
    val rceptDate: LocalDate,
    val remark: String?,
)
