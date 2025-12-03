package dev.kairoscode.kfc.corp.internal.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    val remark: String?
)

/**
 * OPENDART API 공시 정보 원시 응답 데이터
 */
@Serializable
internal data class DisclosureItemRaw(
    @SerialName("corp_code")
    val corpCode: String,

    @SerialName("corp_name")
    val corpName: String,

    @SerialName("stock_code")
    val stockCode: String? = null,

    @SerialName("corp_cls")
    val corpCls: String,

    @SerialName("report_nm")
    val reportName: String,

    @SerialName("rcept_no")
    val rceptNo: String,

    @SerialName("flr_nm")
    val filerName: String,

    @SerialName("rcept_dt")
    val rceptDate: String,

    @SerialName("rm")
    val remark: String? = null
)

/**
 * OPENDART API 공시 검색 응답
 */
@Serializable
internal data class DisclosureListResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String,

    @SerialName("page_no")
    val pageNo: Int? = null,

    @SerialName("page_count")
    val pageCount: Int? = null,

    @SerialName("total_count")
    val totalCount: Int? = null,

    @SerialName("total_page")
    val totalPage: Int? = null,

    @SerialName("list")
    val list: List<DisclosureItemRaw>? = null
)
