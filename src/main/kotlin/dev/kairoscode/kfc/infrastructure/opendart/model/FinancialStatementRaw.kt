package dev.kairoscode.kfc.infrastructure.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OPENDART 단일회사 전체 재무제표 API 응답 (원시 데이터)
 *
 * API: fnlttSinglAcntAll.json
 */
@Serializable
data class FinancialStatementResponse(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("list") val list: List<FinancialStatementRaw>? = null
)

/**
 * 재무제표 항목 원시 데이터
 *
 * 주의: OPENDART API는 보고서 유형에 따라 일부 필드를 생략할 수 있습니다.
 * - 연간 보고서: 분기 필드(frmtrm_q_*) 생략
 * - 분기 보고서: 연간 필드 일부 생략 가능
 * 따라서 모든 선택적 필드에 기본값(= null)을 설정해야 합니다.
 */
@Serializable
data class FinancialStatementRaw(
    @SerialName("rcept_no") val rceptNo: String,
    @SerialName("reprt_code") val reprtCode: String,
    @SerialName("bsns_year") val bsnsYear: String,
    @SerialName("corp_code") val corpCode: String,
    @SerialName("sj_div") val sjDiv: String,
    @SerialName("sj_nm") val sjNm: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("account_nm") val accountNm: String,
    @SerialName("account_detail") val accountDetail: String? = null,
    @SerialName("thstrm_nm") val thstrmNm: String,
    @SerialName("thstrm_amount") val thstrmAmount: String,
    @SerialName("thstrm_add_amount") val thstrmAddAmount: String? = null,
    @SerialName("frmtrm_nm") val frmtrmNm: String? = null,
    @SerialName("frmtrm_amount") val frmtrmAmount: String? = null,
    @SerialName("frmtrm_q_nm") val frmtrmQNm: String? = null,
    @SerialName("frmtrm_q_amount") val frmtrmQAmount: String? = null,
    @SerialName("frmtrm_add_amount") val frmtrmAddAmount: String? = null,
    @SerialName("bfefrmtrm_nm") val bfefrmtrmNm: String? = null,
    @SerialName("bfefrmtrm_amount") val bfefrmtrmAmount: String? = null,
    @SerialName("ord") val ord: String
)
