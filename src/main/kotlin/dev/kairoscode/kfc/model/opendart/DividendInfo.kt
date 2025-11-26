package dev.kairoscode.kfc.model.opendart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 배당 정보
 *
 * OPENDART API alotMatter에서 반환되는 배당 정보
 *
 * @property rceptNo 접수번호
 * @property corpCode OPENDART 고유번호
 * @property corpName 법인명
 * @property dividendType 배당 구분 (현금, 주식)
 * @property stockKind 주식 종류
 * @property currentYear 당기 배당금 (원)
 * @property previousYear 전기 배당금 (원)
 * @property twoYearsAgo 전전기 배당금 (원)
 * @property settlementDate 결산기준일
 */
data class DividendInfo(
    val rceptNo: String,
    val corpCode: String,
    val corpName: String,
    val dividendType: String,
    val stockKind: String,
    val currentYear: BigDecimal?,
    val previousYear: BigDecimal?,
    val twoYearsAgo: BigDecimal?,
    val settlementDate: LocalDate
)

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
    val settlementDate: String
)
