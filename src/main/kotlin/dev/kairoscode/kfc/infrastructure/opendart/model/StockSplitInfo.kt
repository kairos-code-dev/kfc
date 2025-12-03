package dev.kairoscode.kfc.infrastructure.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

// Public domain model은 dev.kairoscode.kfc.domain.corp.StockSplitInfo로 이동되었습니다.
// import dev.kairoscode.kfc.domain.corp.StockSplitInfo

/**
 * OPENDART API 증자/감자 정보 원시 응답 데이터
 */
@Serializable
internal data class StockSplitInfoRaw(
    @SerialName("rcept_no")
    val rceptNo: String,

    @SerialName("corp_code")
    val corpCode: String,

    @SerialName("corp_name")
    val corpName: String,

    @SerialName("isu_dcrs_de")
    val eventDate: String? = null,

    @SerialName("isu_dcrs_stle")
    val eventType: String? = null,

    @SerialName("isu_dcrs_stock_knd")
    val stockKind: String? = null,

    @SerialName("isu_dcrs_qy")
    val quantity: String? = null,

    @SerialName("isu_dcrs_mstvdv_fval_amount")
    val parValuePerShare: String? = null,

    @SerialName("isu_dcrs_mstvdv_amount")
    val totalAmount: String? = null
)
