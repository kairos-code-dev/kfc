package dev.kairoscode.kfc.infrastructure.krx.internal

/**
 * KRX API 요청 파라미터 키 상수
 *
 * KRX API는 단일 엔드포인트를 사용하며, `bld` 파라미터 값에 따라 다양한 데이터를 조회합니다.
 * 이 객체는 API 호출 시 사용되는 모든 파라미터 키를 상수로 관리합니다.
 *
 * **KRX API 구조:**
 * - 엔드포인트: `http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd`
 * - POST 요청으로 파라미터 전달
 * - `bld` 값으로 조회 데이터 타입 결정 (라우팅 키)
 * - 나머지 파라미터는 `bld` 값에 따라 달라짐
 */
internal object KrxApiParams {

    // ================================
    // 공통 파라미터
    // ================================

    /**
     * BLD 코드 - API 데이터 타입 라우팅 키
     *
     * 이 파라미터 값이 어떤 종류의 데이터를 반환할지 결정합니다.
     * 예: "dbms/MDC/STAT/standard/MDCSTAT04601" → ETF 목록
     */
    const val BLD = "bld"

    // ================================
    // 날짜 관련 파라미터
    // ================================

    /**
     * 거래일 (Trade Date)
     *
     * 특정 거래일의 데이터를 조회할 때 사용합니다.
     * 포맷: yyyyMMdd (예: "20240119")
     */
    const val TRADE_DATE = "trdDd"

    /**
     * 시작일 (Start Date)
     *
     * 조회 범위의 시작 날짜입니다.
     * 포맷: yyyyMMdd (예: "20240101")
     */
    const val START_DATE = "strtDd"

    /**
     * 종료일 (End Date)
     *
     * 조회 범위의 종료 날짜입니다.
     * 포맷: yyyyMMdd (예: "20241231")
     */
    const val END_DATE = "endDd"

    // ================================
    // 종목 관련 파라미터
    // ================================

    /**
     * 종목 코드 (Issue Code / ISIN)
     *
     * ETF의 ISIN 코드를 지정합니다.
     * 예: "KR7069500007"
     */
    const val ISIN_CODE = "isuCd"

    // ================================
    // 공매도 조회 파라미터
    // ================================

    /**
     * 검색 유형 (Search Type)
     *
     * 공매도 데이터 조회 시 사용됩니다.
     * - "1": 종목 카테고리 조회
     * - "2": 개별 종목 조회
     */
    const val SEARCH_TYPE = "searchType"

    /**
     * 시장 ID (Market ID)
     *
     * 공매도 데이터 조회 시 시장을 지정합니다.
     * - "STK": 주식 시장
     */
    const val MARKET_ID = "mktId"

    /**
     * 증권 그룹 ID (Security Group ID)
     *
     * 공매도 데이터 조회 시 증권 그룹을 지정합니다.
     * - "EF": ETF
     * - "ELW": ELW
     */
    const val SECURITY_GROUP_ID = "secugrpId"

    /**
     * 조회 조건 (Inquiry Condition)
     *
     * 공매도 데이터 조회 시 조회 대상을 지정합니다.
     * - "EF": ETF만 조회
     */
    const val INQUIRY_CONDITION = "inqCond"

    /**
     * 시장 타입 코드 (Market Type Code)
     *
     * 공매도 잔고 조회 시 시장 타입을 지정합니다.
     * - "2": ETF의 경우
     */
    const val MARKET_TYPE_CODE = "mktTpCd"

    // ================================
    // 리포트 파라미터
    // ================================

    /**
     * 주식 리포트 여부 (Share Report)
     *
     * 공매도 데이터에 주식 관련 정보 포함 여부를 지정합니다.
     * - "1": 포함
     */
    const val SHARE = "share"

    /**
     * 금액 리포트 여부 (Money Report)
     *
     * 공매도 데이터에 금액 관련 정보 포함 여부를 지정합니다.
     * - "1": 포함
     */
    const val MONEY = "money"
}
