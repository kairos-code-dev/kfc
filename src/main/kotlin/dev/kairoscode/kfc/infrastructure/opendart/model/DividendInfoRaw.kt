package dev.kairoscode.kfc.infrastructure.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Public domain model은 dev.kairoscode.kfc.domain.corp.DividendInfo로 이동되었습니다.
// import dev.kairoscode.kfc.domain.corp.DividendInfo

/**
 * OPENDART API 배당 정보 원시 응답 데이터
 */
@Serializable
internal data class DividendInfoRaw(
    @SerialName("rcept_no")
    val rceptNo: String,
    @SerialName("corp_code")
    val corpCode: String,
    @SerialName("corp_name")
    val corpName: String,
    @SerialName("se")
    val dividendType: String,
    @SerialName("stock_knd")
    val stockKind: String? = null,
    @SerialName("thstrm")
    val currentYear: String? = null,
    @SerialName("frmtrm")
    val previousYear: String? = null,
    @SerialName("lwfr")
    val twoYearsAgo: String? = null,
    @SerialName("stlm_dt")
    val settlementDate: String,
)
