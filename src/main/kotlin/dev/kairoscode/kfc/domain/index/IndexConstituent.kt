package dev.kairoscode.kfc.domain.index

import java.time.LocalDate

/**
 * 지수 구성 종목
 *
 * 특정 지수에 포함된 종목 목록입니다.
 *
 * @property indexTicker 지수 코드 (예: "1028")
 * @property indexName 지수명 (예: "코스피 200")
 * @property constituents 구성 종목 티커 리스트 (예: ["005930", "000660", ...])
 * @property asOfDate 기준일
 */
data class IndexConstituent(
    val indexTicker: String,
    val indexName: String,
    val constituents: List<String>,
    val asOfDate: LocalDate,
) {
    /**
     * 구성 종목 개수
     */
    val count: Int
        get() = constituents.size

    /**
     * 특정 종목이 지수에 포함되어 있는지 확인
     *
     * @param ticker 종목 코드 (6자리)
     * @return true if 종목이 지수에 포함됨
     */
    fun contains(ticker: String): Boolean = constituents.contains(ticker)
}
