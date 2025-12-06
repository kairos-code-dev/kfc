package dev.kairoscode.kfc.domain.funds

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 펀드 목록 항목
 *
 * KRX API MDCSTAT04601에서 반환되는 전체 펀드 기본정보
 *
 * @property isin ISIN 코드 (12자리, 예: KR7152100004)
 * @property ticker 종목코드 (6자리, 예: 152100)
 * @property name 종목 약명 (예: ARIRANG 200)
 * @property fullName 종목 전체명
 * @property englishName 영문 종목명
 * @property listingDate 상장일
 * @property benchmarkIndex 벤치마크 지수명
 * @property indexProvider 지수 산출기관 (예: KRX)
 * @property leverageType 레버리지/인버스 유형 (예: "2X 레버리지 (2)", null인 경우 일반형)
 * @property replicationMethod 복제 방법 (실물, 합성)
 * @property marketType 시장 구분 (국내, 해외)
 * @property assetClass 자산군 (주식, 채권, 상품, 파생)
 * @property listedShares 상장 주식 수
 * @property assetManager 운용사
 * @property cuQuantity CU 수량 (생성 단위)
 * @property totalExpenseRatio 총보수율 (%)
 * @property taxType 과세 유형
 */
data class FundListItem(
    val isin: String,
    val ticker: String,
    val name: String,
    val fullName: String,
    val englishName: String,
    val listingDate: LocalDate,
    val benchmarkIndex: String,
    val indexProvider: String,
    val leverageType: String?,
    val replicationMethod: String,
    val marketType: String,
    val assetClass: String,
    val listedShares: Long,
    val assetManager: String,
    val cuQuantity: Long,
    val totalExpenseRatio: BigDecimal,
    val taxType: String,
)
