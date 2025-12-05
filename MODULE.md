# Module KFC

**Korea Free Financial Data Collector** - Kotlin 라이브러리로 한국 금융 데이터를 쉽고 안전하게 수집할 수 있습니다.

## Overview

KFC는 [pykrx](https://github.com/sharebook-kr/pykrx)에서 영감을 받아 만들어진 Kotlin 기반의 한국 금융 데이터 라이브러리입니다.
KRX(한국거래소)와 OPENDART의 데이터를 Kotlin/JVM 환경에서 타입 안전하게 조회할 수 있습니다.

## Key Features

- **100% Kotlin** - Kotlin Coroutines 네이티브 지원으로 비동기 처리
- **Type-Safe** - 모든 응답이 타입이 지정된 도메인 모델로 매핑
- **Auto Rate Limiting** - KRX API 제한(25 RPS)을 자동으로 준수
- **Clean Architecture** - 도메인 중심 설계로 확장 가능
- **Null Safety** - Kotlin의 null 안전성으로 NPE 방지

## Quick Start

```kotlin
import dev.kairoscode.kfc.api.KfcClient
import dev.kairoscode.kfc.domain.stock.Market
import java.time.LocalDate

suspend fun main() {
    // 클라이언트 생성
    val kfc = KfcClient.create()

    // 코스피 종목 리스트 조회
    val stocks = kfc.stock.getStockList(market = Market.KOSPI)
    println("코스피 종목 수: ${stocks.size}")

    // 삼성전자 기본정보 조회
    val samsung = kfc.stock.getStockInfo("005930")
    println("종목명: ${samsung?.name}, ISIN: ${samsung?.isin}")
}
```

## Main APIs

### Stock API
주식 종목 정보, 섹터/산업 분류를 제공합니다.
- `getStockList()` - 시장별 종목 리스트
- `getStockInfo()` - 종목 기본정보
- `getStockSector()` - 종목별 섹터 정보

### Funds API
ETF, ETN, REIT의 상세 정보와 거래 데이터를 제공합니다.
- `getList()` - 펀드 목록
- `getDetailedInfo()` - 펀드 상세정보 (NAV, 괴리율)
- `getPortfolio()` - 포트폴리오 구성
- `getShortSelling()` - 공매도 거래 데이터

### Index API
지수 정보와 OHLCV, 밸류에이션 데이터를 제공합니다.
- `getIndexList()` - 지수 목록
- `getOhlcvByDate()` - 지수 OHLCV
- `getFundamentalByDate()` - PER/PBR/배당수익률
- `getIndexConstituents()` - 지수 구성 종목

### Bond API
채권 수익률 데이터를 제공합니다.
- `getBondYieldsByDate()` - 특정일 전체 채권 수익률
- `getBondYields()` - 기간별 특정 채권 수익률

### Future API
선물 상품 정보와 시세를 제공합니다.
- `getFutureTickerList()` - 선물 상품 목록
- `getOhlcvByTicker()` - 선물 OHLCV

### Price API
주식 가격 데이터를 제공합니다.
- `getMinuteOhlcv()` - 분단위 OHLCV
- `getDailyOhlcv()` - 일별 OHLCV

### Financials API (OPENDART)
재무제표 데이터를 제공합니다. OPENDART API Key가 필요합니다.
- `getIncomeStatement()` - 손익계산서
- `getBalanceSheet()` - 재무상태표
- `getCashFlowStatement()` - 현금흐름표
- `getAllFinancials()` - 전체 재무제표

### Corp API (OPENDART)
기업 공시 데이터를 제공합니다. OPENDART API Key가 필요합니다.
- `getDividendInfo()` - 배당 정보
- `getStockSplit()` - 액면분할 정보

## Requirements

- JDK 21 or higher
- Kotlin 2.0 or higher

## License

```
Copyright 2024 Kairos Code

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Links

- [GitHub Repository](https://github.com/kairos-code-dev/kfc)
- [Issue Tracker](https://github.com/kairos-code-dev/kfc/issues)
- [KRX 정보데이터시스템](https://data.krx.co.kr)
- [OPENDART](https://opendart.fss.or.kr)
