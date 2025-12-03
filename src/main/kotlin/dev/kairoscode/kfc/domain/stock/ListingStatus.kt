package dev.kairoscode.kfc.domain.stock

/**
 * 상장 상태
 *
 * 종목의 상장/상폐 여부를 나타냅니다.
 */
enum class ListingStatus {
    /**
     * 상장
     */
    LISTED,

    /**
     * 상장폐지
     */
    DELISTED
}
