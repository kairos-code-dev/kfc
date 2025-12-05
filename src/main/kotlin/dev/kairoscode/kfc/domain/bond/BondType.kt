package dev.kairoscode.kfc.domain.bond

/**
 * 채권 종류 Enum
 *
 * 한국 장외 채권시장의 주요 채권 종류를 타입 안전하게 표현합니다.
 * KRX API의 채권 종류 코드와 한글명을 매핑합니다.
 *
 * @property code KRX API에서 사용하는 채권 종류 코드
 * @property koreanName 채권 종류 한글명
 * @property maturity 만기 (예: 1Y, 10Y, 3Y, 91D)
 * @property category 채권 카테고리
 */
enum class BondType(
    val code: String,
    val koreanName: String,
    val maturity: String,
    val category: BondCategory
) {
    /** 국고채 1년 */
    TREASURY_1Y("국고채1년", "국고채 1년", "1Y", BondCategory.TREASURY),

    /** 국고채 2년 */
    TREASURY_2Y("국고채2년", "국고채 2년", "2Y", BondCategory.TREASURY),

    /** 국고채 3년 */
    TREASURY_3Y("국고채3년", "국고채 3년", "3Y", BondCategory.TREASURY),

    /** 국고채 5년 */
    TREASURY_5Y("국고채5년", "국고채 5년", "5Y", BondCategory.TREASURY),

    /** 국고채 10년 (벤치마크) */
    TREASURY_10Y("국고채10년", "국고채 10년", "10Y", BondCategory.TREASURY),

    /** 국고채 20년 */
    TREASURY_20Y("국고채20년", "국고채 20년", "20Y", BondCategory.TREASURY),

    /** 국고채 30년 */
    TREASURY_30Y("국고채30년", "국고채 30년", "30Y", BondCategory.TREASURY),

    /** 국민주택 1종 5년 */
    HOUSING_5Y("국민주택1종5년", "국민주택 1종 5년", "5Y", BondCategory.SPECIAL),

    /** 회사채 AA-(무보증 3년) */
    CORPORATE_AA("회사채AA", "회사채 AA-(무보증 3년)", "3Y", BondCategory.CORPORATE),

    /** 회사채 BBB- (무보증 3년) */
    CORPORATE_BBB("회사채BBB", "회사채 BBB- (무보증 3년)", "3Y", BondCategory.CORPORATE),

    /** CD(91일) */
    CD_91("CD", "CD(91일)", "91D", BondCategory.SHORT_TERM);

    companion object {
        /**
         * KRX API 코드로 BondType 조회
         *
         * @param code KRX API 채권 종류 코드 (예: "국고채1년", "회사채AA")
         * @return 매칭되는 BondType, 없으면 null
         */
        fun fromCode(code: String): BondType? {
            return entries.find { it.code == code }
        }

        /**
         * 한글명으로 BondType 조회
         *
         * @param name 채권 종류 한글명 (예: "국고채 1년", "회사채 AA-(무보증 3년)")
         * @return 매칭되는 BondType, 없으면 null
         */
        fun fromKoreanName(name: String): BondType? {
            return entries.find { it.koreanName == name }
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
