package dev.kairoscode.kfc.infrastructure.krx.internal

/**
 * KRX API 응답 필드명 상수
 *
 * KRX 데이터 API의 JSON/CSV 응답에서 사용되는 필드명을 카테고리별로 정의합니다.
 * 필드명을 상수화함으로써 오타를 방지하고 유지보수성을 높입니다.
 */
internal object KrxApiFields {
    /**
     * 기본 식별 정보 (ISIN, 티커, 종목명 등)
     */
    object Identity {
        const val ISIN = "ISU_CD" // 종목 코드 (예: KR7069500007)
        const val TICKER = "ISU_SRT_CD" // 종목 단축코드 (예: 069500)
        const val NAME_SHORT = "ISU_ABBRV" // 종목 약명 (예: KODEX 200)
        const val NAME_FULL = "ISU_NM" // 종목명 (전체) (예: KODEX 200)
        const val NAME_ENGLISH = "ISU_ENG_NM" // 종목 영문명
        const val SECURITY_GROUP = "SECUGRP_NM" // 증권구분 (예: ETF)
    }

    /**
     * 날짜/시간 정보
     */
    object DateTime {
        const val TRADE_DATE = "TRD_DD" // 거래일자 (예: 20240102)
        const val LISTING_DATE = "LIST_DD" // 상장일자
        const val WEEK52_HIGH_DATE = "WK52_HGPR_DD" // 52주 최고가 일자 (MDCSTAT04701에서는 미제공)
        const val WEEK52_LOW_DATE = "WK52_LWPR_DD" // 52주 최저가 일자 (MDCSTAT04701에서는 미제공)
        const val REPORT_DATE = "RPT_DUTY_OCCR_DD" // 보고의무발생일 (공매도용)
        const val CURRENT_DATETIME = "CURRENT_DATETIME" // 현재 시간 (MDCSTAT04701에서 제공)
    }

    /**
     * 가격 정보 (OHLCV)
     */
    object Price {
        const val OPEN = "TDD_OPNPRC" // 당일 시가
        const val HIGH = "TDD_HGPRC" // 당일 고가
        const val LOW = "TDD_LWPRC" // 당일 저가
        const val CLOSE = "TDD_CLSPRC" // 당일 종가
        const val CLOSE_ALT = "CLSPRC" // 종가 (대체 필드)
        const val BASE = "BAS_PRC" // 기준가격
        const val COMPARE = "CMP_PRC" // 비교가격
        const val WEEK52_HIGH = "WK52_HGST_PRC" // 52주 최고가 (MDCSTAT04701 실제 응답)
        const val WEEK52_LOW = "WK52_LWST_PRC" // 52주 최저가 (MDCSTAT04701 실제 응답)
    }

    /**
     * 가격 변동 정보
     */
    object PriceChange {
        const val AMOUNT = "CMPPREVDD_PRC" // 전일대비 가격
        const val RATE = "FLUC_RT" // 등락률 (%)
        const val DIRECTION = "FLUC_TP_CD" // 등락 구분 코드 (2: 상한, 1: 상승, 0: 보합, -1: 하락, -2: 하한)
        const val INDEX_RATE_ALT = "FLUC_RT1" // 등락률1 (지수용 대체)
        const val INDEX_DIRECTION = "FLUC_TP_CD1" // 등락 구분 코드1 (지수용)
        const val RATE_ALT1 = "FLUC_RT1" // 등락률1 (MDCSTAT04701에서 ETF의 등락률)
        const val DIRECTION_ALT1 = "FLUC_TP_CD1" // 등락 구분 코드1 (MDCSTAT04701에서 ETF의 등락구분)
        const val RATE_ALT2 = "FLUC_RT2" // 등락률2 (MDCSTAT04701에서 지수의 등락률)
        const val DIRECTION_ALT2 = "FLUC_TP_CD2" // 등락 구분 코드2 (MDCSTAT04701에서 지수의 등락구분)
    }

    /**
     * 거래량 및 거래대금
     */
    object Volume {
        const val ACCUMULATED = "ACC_TRDVOL" // 누적 거래량
        const val TRADING_VALUE = "ACC_TRDVAL" // 누적 거래대금
        const val SHORT_VOLUME = "CVSRTSELL_TRDVOL" // 공매도 거래량
        const val SHORT_VALUE = "CVSRTSELL_TRDVAL" // 공매도 거래대금
        const val VOLUME_RATIO = "TRDVOL_WT" // 거래량 비중 (%)
        const val VALUE_RATIO = "TRDVAL_WT" // 거래대금 비중 (%)
    }

    /**
     * NAV (순자산가치) 및 괴리율
     */
    object Nav {
        const val VALUE = "NAV" // 순자산가치 (NAV)
        const val VALUE_LAST = "LST_NAV" // 최근 NAV
        const val CHANGE_AMOUNT = "NAV_CHG_VAL" // NAV 변화값
        const val CHANGE_RATE = "NAV_CHG_RT" // NAV 변화율 (%)
        const val DIVERGENCE_RATE = "DIVRG_RT" // 괴리율 (%) = (종가 - NAV) / NAV * 100
    }

    /**
     * 시가총액 및 자산 정보
     */
    object Asset {
        const val MARKET_CAP = "MKTCAP" // 시가총액
        const val NET_ASSET_TOTAL = "INVSTASST_NETASST_TOTAMT" // 투자자산 순자산 총액
        const val LISTED_SHARES = "LIST_SHRS" // 상장 주식수
        const val VALUATION_AMOUNT = "VALU_AMT" // 평가금액
    }

    /**
     * 지수 정보
     */
    object Index {
        const val VALUE = "OBJ_STKPRC_IDX" // 목적 주가지수 (추적 대상 지수의 값)
        const val NAME = "IDX_IND_NM" // 지수 종목명 (예: KOSPI 200)
        const val CHANGE_AMOUNT = "CMPPREVDD_IDX" // 전일대비 지수
        const val CHANGE_RATE = "IDX_FLUC_RT" // 지수 등락률 (%)
        const val CHANGE_RATIO = "IDX_CHG_RTO" // 지수 변화율 (%)
    }

    /**
     * ETF 메타데이터 및 기본 정보
     */
    object EtfMetadata {
        // MDCSTAT04701 (상세정보)에서 제공하는 필드
        const val TOTAL_EXPENSE_RATIO = "ETF_TOT_FEE" // ETF 총 보수 (%) - MDCSTAT04701에서 제공
        const val ASSET_CLASS = "IDX_ASST_CLSS_NM" // 자산 분류명 (예: 주식-시장대표) - MDCSTAT04701에서 제공
        const val ASSET_CLASS_ID = "IDX_ASST_CLSS_ID" // 자산 분류 ID (예: 010100) - MDCSTAT04701에서 제공
        const val BENCHMARK_INDEX = "TRACE_IDX_NM" // 추적 지수명 (예: 코스피 200) - MDCSTAT04701에서 제공

        // MDCSTAT04704 (일반정보)에서만 제공하는 필드들
        const val REPLICATION_METHOD = "ETF_REPLICA_METHD_TP_CD" // ETF 복제방법 유형코드
        const val CREATION_UNIT = "CU_QTY" // 생성/소멸 단위 (CU) 수량
        const val ASSET_MANAGER = "COM_ABBRV" // 회사 약명 (운용사)
        const val INDEX_PROVIDER = "IDX_CALC_INST_NM1" // 지수 산출기관명1 (지수 제공자)
        const val LEVERAGE_TYPE = "IDX_CALC_INST_NM2" // 지수 산출기관명2 (레버리지/인버스 정보)
        const val MARKET_CLASSIFICATION = "IDX_MKT_CLSS_NM" // 지수 시장 분류명 (국내, 해외 등)
        const val TAX_TYPE = "TAX_TP_CD" // 과세 유형코드
        const val MARKET_NAME = "MKT_NM" // 시장명
    }

    /**
     * ETF 포트폴리오 구성 종목 정보
     */
    object Portfolio {
        const val CONSTITUENT_CODE = "COMPST_ISU_CD" // 구성종목 코드
        const val CONSTITUENT_NAME = "COMPST_ISU_NM" // 구성종목명
        const val SHARES_PER_CU = "COMPST_ISU_CU1_SHRS" // 구성종목 CU당 주식수
        const val CONSTITUENT_AMOUNT = "COMPST_AMT" // 구성금액
        const val CONSTITUENT_WEIGHT = "COMPST_RTO" // 구성비율 (%)
    }

    /**
     * 투자자별 거래 정보
     */
    object InvestorTrading {
        const val INVESTOR_TYPE = "INVST_NM" // 투자자명 (기관, 개인, 외국인 등)
        const val ASK_VOLUME = "ASK_TRDVOL" // 매도 거래량
        const val ASK_VALUE = "ASK_TRDVAL" // 매도 거래대금
        const val BID_VOLUME = "BID_TRDVOL" // 매수 거래량
        const val BID_VALUE = "BID_TRDVAL" // 매수 거래대금
        const val NET_BUY_VOLUME = "NETBID_TRDVOL" // 순매수 거래량 = (매수 - 매도)
        const val NET_BUY_VALUE = "NETBID_TRDVAL" // 순매수 거래대금
        const val INSTITUTION_NET_BUY = "NUM_ITM_VAL21" // 기관 순매수금액
        const val CORPORATE_NET_BUY = "NUM_ITM_VAL22" // 기타법인 순매수금액
        const val INDIVIDUAL_NET_BUY = "NUM_ITM_VAL23" // 개인 순매수금액
        const val FOREIGN_NET_BUY = "NUM_ITM_VAL24" // 외국인 순매수금액
    }

    /**
     * ETF 추적 성과 정보
     */
    object TrackingPerformance {
        const val TRACKING_MULTIPLE = "TRACE_YD_MULT" // 추적수익 배수 (ETF 수익 / 지수 수익)
        const val TRACKING_ERROR_RATE = "TRACE_ERR_RT" // 추적오차율 (%)
    }

    /**
     * 공매도 정보
     */
    object ShortSelling {
        const val BALANCE_SHARES = "BAL_QTY" // 공매도 잔고 수량
        const val BALANCE_VALUE = "BAL_AMT" // 공매도 잔고 금액
        const val BALANCE_RATIO = "BAL_RTO" // 공매도 잔고 비율 (%)
    }

    /**
     * 주식 종목 정보 (Stock Finder API용)
     *
     * finder_stkisu, finder_listdelisu 응답에서 사용되는 필드명
     */
    object Stock {
        const val FULL_CODE = "full_code" // ISIN 코드 (12자리, 예: KR7005930003)
        const val SHORT_CODE = "short_code" // 종목 코드 (6자리, 예: 005930)
        const val CODE_NAME = "codeName" // 종목명 (예: 삼성전자)
        const val MARKET_CODE = "marketCode" // 시장 코드 (예: STK, KSQ, KNX)
        const val MARKET_NAME = "marketName" // 시장명 (예: 코스피, 코스닥)
    }

    /**
     * 채권 수익률 정보 (Bond API용)
     *
     * MDCSTAT11401 (특정일 전종목 장외채권수익률)
     * MDCSTAT11402 (개별추이 장외채권수익률) 응답에서 사용되는 필드명
     */
    object Bond {
        const val KIND_NAME = "ITM_TP_NM" // 채권 종류명 (예: 국고채(1년))
        const val YIELD = "LST_ORD_BAS_YD" // 기준수익률 (%)
        const val CHANGE = "CMP_YD" // 전일 대비 변동폭 (bp)
        const val TRADE_DATE = "DISCLS_DD" // 공시일자 (YYYY/MM/DD)
    }
}
