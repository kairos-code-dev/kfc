package dev.kairoscode.kfc.infrastructure.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Public domain model은 dev.kairoscode.kfc.domain.corp.DisclosureItem으로 이동되었습니다.
// import dev.kairoscode.kfc.domain.corp.DisclosureItem

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
    val remark: String? = null,
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
    val list: List<DisclosureItemRaw>? = null,
)
