package dev.kairoscode.kfc.infrastructure.opendart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OPENDART API 공통 응답 구조
 *
 * 모든 OPENDART API는 이 구조로 응답합니다.
 *
 * @param T 응답 데이터 타입
 * @property status 응답 상태 코드 ("000": 정상, 그 외: 오류)
 * @property message 응답 메시지
 * @property list 응답 데이터 목록 (데이터가 없으면 null)
 */
@Serializable
data class OpenDartResponse<T>(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("list")
    val list: List<T>? = null,
)
