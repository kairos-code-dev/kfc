package dev.kairoscode.kfc.domain.future

/**
 * 선물 상품 정보
 *
 * 한국거래소(KRX)에서 거래되는 선물 상품의 기본 정보입니다.
 * 선물 상품은 productId로 식별되며, 각 상품은 여러 만기의 종목을 가질 수 있습니다.
 *
 * @property productId 선물 상품 ID (예: KRDRVFUEST)
 * @property name 선물 상품명 (예: EURO STOXX50 선물)
 */
data class FutureProduct(
    val productId: String,
    val name: String
)
