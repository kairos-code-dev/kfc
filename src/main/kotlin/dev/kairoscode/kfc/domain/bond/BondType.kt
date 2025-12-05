package dev.kairoscode.kfc.domain.bond

/**
 * 채권 종류 Enum
 *
 * 한국 장외 채권시장의 주요 채권 종류를 타입 안전하게 표현합니다.
 * KRX API의 채권 종류 코드와 한글명을 매핑합니다.
 *
 * @property krxCode KRX API의 기간별 조회에서 사용하는 숫자 코드 (MDCSTAT11402)
 * @property displayName 표시용 한글명 (공백 포함)
 * @property maturity 만기 (예: 1Y, 10Y, 3Y, 91D)
 * @property category 채권 카테고리
 */
enum class BondType(
    val krxCode: String,
    val displayName: String,
    val maturity: String,
    val category: BondCategory
) {
    /** 국고채 1년 */
    TREASURY_1Y("3006", "국고채 1년", "1Y", BondCategory.TREASURY),

    /** 국고채 2년 */
    TREASURY_2Y("3019", "국고채 2년", "2Y", BondCategory.TREASURY),

    /** 국고채 3년 */
    TREASURY_3Y("3000", "국고채 3년", "3Y", BondCategory.TREASURY),

    /** 국고채 5년 */
    TREASURY_5Y("3007", "국고채 5년", "5Y", BondCategory.TREASURY),

    /** 국고채 10년 (벤치마크) */
    TREASURY_10Y("3013", "국고채 10년", "10Y", BondCategory.TREASURY),

    /** 국고채 20년 */
    TREASURY_20Y("3014", "국고채 20년", "20Y", BondCategory.TREASURY),

    /** 국고채 30년 */
    TREASURY_30Y("3017", "국고채 30년", "30Y", BondCategory.TREASURY),

    /** 국민주택 1종 5년 */
    HOUSING_5Y("3008", "국민주택 1종 5년", "5Y", BondCategory.SPECIAL),

    /** 회사채 AA-(무보증 3년) */
    CORPORATE_AA("3009", "회사채 AA-(무보증 3년)", "3Y", BondCategory.CORPORATE),

    /** 회사채 BBB- (무보증 3년) */
    CORPORATE_BBB("3010", "회사채 BBB- (무보증 3년)", "3Y", BondCategory.CORPORATE),

    /** CD(91일) */
    CD_91("4000", "CD(91일)", "91D", BondCategory.SHORT_TERM);

    companion object {
        /**
         * KRX API 숫자 코드로 BondType 조회
         *
         * @param krxCode KRX API 채권 종류 숫자 코드 (예: "3006", "3009")
         * @return 매칭되는 BondType, 없으면 null
         */
        fun fromKrxCode(krxCode: String): BondType? {
            return entries.find { it.krxCode == krxCode }
        }

        /**
         * 한글명으로 BondType 조회
         *
         * KRX API 응답의 ITM_TP_NM 필드값과 매칭합니다.
         * 공백 유무와 관계없이 매칭할 수 있도록 정규화합니다.
         *
         * @param name 채권 종류 한글명 (예: "국고채 1년", "국고채1년", "회사채 AA-(무보증 3년)")
         * @return 매칭되는 BondType, 없으면 null
         */
        fun fromKoreanName(name: String): BondType? {
            val normalized = name.replace(" ", "")
            return entries.find { it.displayName.replace(" ", "") == normalized }
        }

        /**
         * 모든 국고채 BondType 목록
         */
        val treasuryBonds: List<BondType>
            get() = entries.filter { it.category == BondCategory.TREASURY }

        /**
         * 모든 회사채 BondType 목록
         */
        val corporateBonds: List<BondType>
            get() = entries.filter { it.category == BondCategory.CORPORATE }
    }
}

/**
 * 채권 카테고리 Enum
 *
 * 채권을 발행 주체별로 분류합니다.
 */
enum class BondCategory {
    /** 국고채 */
    TREASURY,

    /** 특수채 (국민주택채권 등) */
    SPECIAL,

    /** 회사채 */
    CORPORATE,

    /** 단기 금융상품 (CD 등) */
    SHORT_TERM
}
