package dev.kairoscode.kfc.domain

/**
 * 펀드 유형 (ETF, REIT, ETN, ELW)
 *
 * KRX API에서 펀드 유형을 구분하기 위한 파라미터 값들을 정의합니다.
 * 모든 펀드 유형은 동일한 BLD 코드와 데이터 구조를 사용하며,
 * secugrpId 파라미터로 구분됩니다.
 *
 * @property krxSecurityGroupId KRX API secugrpId 파라미터 값
 * @property krxInquiryCondition KRX API inqCond 파라미터 값
 * @property krxMarketTypeCode KRX API mktTpCd 파라미터 값
 */
enum class FundType(
    val krxSecurityGroupId: String, // secugrpId
    val krxInquiryCondition: String, // inqCond
    val krxMarketTypeCode: String, // mktTpCd
) {
    /**
     * 상장지수펀드 (Exchange Traded Fund)
     */
    ETF("EF", "EF", "2"),

    /**
     * 리츠/수익증권 (Real Estate Investment Trust)
     */
    REIT("BC", "BC", "3"),

    /**
     * 상장지수증권 (Exchange Traded Note)
     */
    ETN("EN", "EN", "4"),

    /**
     * 주식워런트증권 (Equity Linked Warrant)
     */
    ELW("EW", "EW", "5"),
}
