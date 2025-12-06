package dev.kairoscode.kfc.domain.stock

/**
 * 상장 상태
 *
 * 종목의 상장 여부를 구분하는 Enum 클래스입니다.
 */
enum class ListingStatus {
    /**
     * 상장 종목
     */
    LISTED,

    /**
     * 상장폐지 종목
     */
    DELISTED,
}
