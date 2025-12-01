# EtfApi 테스트 시나리오

## 개요

EtfApi는 15개의 함수를 제공하며, 각 함수는 **Live Test**(실제 API 호출)와 **Unit Test**(Mock 기반)로 테스트합니다.

**테스트 작성 원칙**:
- 테스트 이름과 @DisplayName으로 시나리오를 명확히 표현
- Given-When-Then 패턴으로 시나리오 구조화
- 실제 사용 예제가 되도록 작성 (수익률 계산, 필터링, 검색 등)

## 1. getList() - ETF 목록 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfListLiveTest.kt`

#### 기본 시나리오
1. **"전체 ETF 목록을 조회할 수 있다"**
   - Given: KfcClient 초기화
   - When: `client.etf.getList()` 호출
   - Then: 300개 이상의 ETF 반환
   - Then: 각 ETF는 ISIN, 티커, 이름, 자산구분 포함
   - Then: TIGER 200, KODEX 200 포함 확인
   - 응답 레코딩: `etf_list.json`

2. **"ETF 목록의 다양한 자산구분을 확인할 수 있다"**
   - Given: ETF 목록 조회
   - When: 자산구분별로 그룹화
   - Then: 주식, 채권, 원자재 등 다양한 자산구분 존재
   - 콘솔 출력: 자산구분별 ETF 개수

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/etf/EtfListApiSpec.kt`

#### 사용법 시나리오 (@Nested "getList() 사용법")
1. **"기본 사용법 - 전체 ETF 목록을 간단히 조회할 수 있다"**
   - Mock: `etf_list.json`
   - 시나리오: 파라미터 없이 `getList()` 호출
   - 검증: ETF 목록 반환
   - 예제: 첫 번째 ETF 정보 출력

2. **"필터링 예제 - 특정 자산구분의 ETF만 조회할 수 있다"**
   - Mock: `etf_list.json`
   - 시나리오: 전체 목록 조회 → 주식형만 필터링
   - 검증: 주식형 ETF만 포함
   - 예제: `filter { it.assetClass == "주식" }`

3. **"검색 예제 - 이름으로 ETF를 검색할 수 있다"**
   - Mock: `etf_list.json`
   - 시나리오: 이름에 'TIGER' 포함된 ETF 검색
   - 검증: TIGER ETF들만 반환
   - 예제: `filter { it.name.contains("TIGER") }`

#### API 명세 시나리오 (@Nested "getList() API 명세")
1. **"[명세] 반환 타입은 List<EtfListItem>이다"**
   - 검증: 반환 타입 확인

2. **"[명세] 각 ETF는 ISIN, 티커, 이름, 자산구분을 포함한다"**
   - 검증: 필수 필드 존재 및 비어있지 않음

3. **"[명세] ISIN 코드는 'KR7'로 시작하는 12자리 문자열이다"**
   - 검증: ISIN 형식 (KR7 시작, 12자리)

---

## 2. getOhlcv() - ETF OHLCV 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfOhlcvLiveTest.kt`

#### 기본 시나리오 (@Nested "기본 사용법 테스트")
1. **"1개월 OHLCV 데이터를 조회할 수 있다"**
   - Given: 최근 1개월 기간 설정
   - When: `getOhlcv(isin, fromDate, toDate)` 호출
   - Then: 약 20개 거래일 데이터 반환 (주말 제외)
   - Then: OHLCV 정합성 검증 (high >= open/close, low <= open/close)
   - 응답 레코딩: `tiger200_1month.json`

2. **"1년 OHLCV 데이터를 조회할 수 있다 (자동 분할 처리)"**
   - Given: 최근 1년 기간 설정 (730일 초과)
   - When: `getOhlcv()` 호출
   - Then: 약 250개 거래일 데이터 반환
   - Then: 내부적으로 730일 단위로 자동 분할 처리됨
   - 응답 레코딩: `tiger200_1year.json`

#### 활용 예제 시나리오 (@Nested "활용 예제 테스트")
1. **"[활용] 수익률을 계산할 수 있다"**
   - Given: 최근 3개월 OHLCV 데이터
   - When: 첫 종가와 마지막 종가 비교
   - Then: 수익률 = (마지막종가 - 첫종가) / 첫종가 * 100
   - 콘솔 출력: "3개월 수익률: X.XX%"

2. **"[활용] 이동평균을 계산할 수 있다"**
   - Given: 최근 3개월 OHLCV 데이터
   - When: 마지막 20개 종가의 평균 계산
   - Then: 20일 이동평균 = sum(close[last 20]) / 20
   - 콘솔 출력: "20일 이동평균: XXXXX"

3. **"[활용] 변동성을 계산할 수 있다"** (추가 예정)
   - 일별 수익률 계산 → 표준편차 계산

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/etf/EtfOhlcvApiSpec.kt`

#### 사용법 시나리오 (@Nested "getOhlcv() 사용법")
1. **"기본 사용법 - 특정 기간의 OHLCV를 조회할 수 있다"**
   - Mock: `tiger200_1month.json`
   - 시나리오: fromDate, toDate 지정하여 조회
   - 예제: 종가 목록 출력

2. **"날짜 범위 예제 - 다양한 기간 조회가 가능하다"**
   - Mock: 여러 기간별 JSON 파일
   - 시나리오: 1주일, 1개월, 3개월, 1년 조회 예제

#### API 명세 시나리오 (@Nested "getOhlcv() API 명세")
1. **"[명세] 반환 데이터는 날짜순으로 정렬되어 있다"**
   - 검증: date[i] <= date[i+1]

2. **"[명세] 고가는 시가, 저가, 종가보다 크거나 같다"**
   - 검증: high >= open, high >= low, high >= close

3. **"[명세] 저가는 시가, 고가, 종가보다 작거나 같다"**
   - 검증: low <= open, low <= high, low <= close

4. **"[명세] 거래량은 0 이상이다"**
   - 검증: volume >= 0

5. **"[명세] 730일 초과 시 자동 분할 처리된다"**
   - Mock: 1년 데이터
   - 검증: 데이터 연속성 확인

---

## 3. getAdjustedOhlcv() - 조정주가 OHLCV 조회 (Naver API)

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfAdjustedOhlcvLiveTest.kt`

#### 기본 시나리오
1. **"조정주가 OHLCV를 조회할 수 있다"**
   - Given: TIGER 200 티커 (069500)
   - When: `getAdjustedOhlcv(ticker, fromDate, toDate)` 호출
   - Then: 조정주가 데이터 반환
   - 응답 레코딩: `069500_adjusted_1month.json`

2. **"Rate Limiting이 적용되어 있다"**
   - Given: 연속 호출 시나리오
   - When: 짧은 시간 내 여러 번 호출
   - Then: Rate Limit 대기 시간 적용됨 (내부 구현 확인)

#### 활용 예제
1. **"[활용] 조정주가와 종가를 비교할 수 있다"**
   - Given: 같은 기간의 OHLCV와 AdjustedOhlcv
   - When: 두 데이터 비교
   - Then: 배당/액면분할 이벤트 발생 시 차이 존재

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/etf/EtfAdjustedOhlcvApiSpec.kt`

#### 사용법 시나리오
1. **"기본 사용법 - 티커로 조정주가를 조회할 수 있다"**
   - Mock: `069500_adjusted_1month.json`
   - 시나리오: 6자리 티커 사용

#### API 명세 시나리오
1. **"[명세] Naver API 응답 형식을 따른다"**
   - 검증: NaverEtfOhlcv 타입 반환

2. **"[명세] 티커는 6자리 숫자 문자열이다"**
   - 검증: 티커 형식 (6자리, 숫자)

---

## 4. getComprehensiveInfo() - ETF 종합 정보 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfComprehensiveLiveTest.kt`

#### 기본 시나리오
1. **"ETF 종합 정보를 조회할 수 있다"**
   - Given: TIGER 200 ISIN
   - When: `getComprehensiveInfo(isin)` 호출
   - Then: OHLCV, NAV, 괴리율, 52주 고가/저가, 총 보수 등 포함
   - 응답 레코딩: `tiger200_comprehensive.json`

2. **"존재하지 않는 ISIN 조회 시 null 반환한다"**
   - Given: 잘못된 ISIN
   - When: `getComprehensiveInfo()` 호출
   - Then: null 반환
   - 응답 레코딩: `not_found.json`

#### 활용 예제
1. **"[활용] NAV 대비 괴리율을 확인할 수 있다"**
   - Given: 종합 정보 조회
   - When: NAV와 종가 비교
   - Then: 괴리율 = (종가 - NAV) / NAV * 100

2. **"[활용] 52주 고가/저가 대비 현재가 위치를 확인할 수 있다"**
   - Given: 종합 정보 조회
   - When: 현재가와 52주 고/저가 비교
   - Then: 위치 = (현재가 - 저가) / (고가 - 저가) * 100

---

## 5. getAllDailyPrices() - 전체 ETF 일별 시세 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfDailyPricesLiveTest.kt`

#### 기본 시나리오
1. **"특정 날짜의 전체 ETF 시세를 조회할 수 있다"**
   - Given: 특정 거래일 지정
   - When: `getAllDailyPrices(date)` 호출
   - Then: 300개 이상 ETF 시세 반환
   - 응답 레코딩: `all_etf_20241201.json`

2. **"비거래일 조회 시 빈 결과 반환한다"**
   - Given: 주말 또는 공휴일
   - When: `getAllDailyPrices(date)` 호출
   - Then: 빈 리스트 반환

#### 활용 예제
1. **"[활용] 등락률 상위 ETF를 찾을 수 있다"**
   - Given: 전체 ETF 일별 시세
   - When: 등락률 기준 정렬
   - Then: 상위 10개 ETF 출력

2. **"[활용] 거래량 상위 ETF를 찾을 수 있다"**
   - Given: 전체 ETF 일별 시세
   - When: 거래량 기준 정렬
   - Then: 상위 10개 ETF 출력

---

## 6. getPriceChanges() - ETF 기간 등락률 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfPriceChangesLiveTest.kt`

#### 기본 시나리오
1. **"기간별 등락률을 조회할 수 있다"**
   - Given: 시작일과 종료일 지정
   - When: `getPriceChanges(fromDate, toDate)` 호출
   - Then: 모든 ETF의 기간 등락률 반환
   - 응답 레코딩: `price_changes_1month.json`

#### 활용 예제
1. **"[활용] 수익률 상위 ETF를 찾을 수 있다"**
   - Given: 1개월 등락률 데이터
   - When: 등락률 기준 내림차순 정렬
   - Then: 상위 20개 ETF 출력

2. **"[활용] 자산구분별 평균 수익률을 계산할 수 있다"**
   - Given: 1개월 등락률 데이터
   - When: 자산구분별 그룹화 후 평균 계산
   - Then: "주식: X.X%, 채권: X.X%" 출력

---

## 7. getPortfolio() - ETF 포트폴리오 구성 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfPortfolioLiveTest.kt`

#### 기본 시나리오
1. **"ETF 바스켓 구성 종목을 조회할 수 있다"**
   - Given: TIGER 200 ISIN
   - When: `getPortfolio(isin)` 호출
   - Then: 구성 종목 및 비중 반환
   - Then: 비중 합계 = 100% (허용 오차 범위 내)
   - 응답 레코딩: `tiger200_portfolio.json`

#### 활용 예제
1. **"[활용] 상위 10개 구성 종목을 확인할 수 있다"**
   - Given: 포트폴리오 데이터
   - When: 비중 기준 정렬
   - Then: 상위 10개 종목 출력

2. **"[활용] 특정 종목의 비중을 확인할 수 있다"**
   - Given: 포트폴리오 데이터
   - When: 삼성전자 검색
   - Then: "삼성전자: X.X%" 출력

---

## 8-9. getTrackingError() / getDivergenceRate() - 추적 오차 / 괴리율 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfPerformanceLiveTest.kt`

#### 추적 오차 시나리오
1. **"ETF와 벤치마크 간 추적 오차를 조회할 수 있다"**
   - Given: TIGER 200 ISIN, 1개월 기간
   - When: `getTrackingError(isin, fromDate, toDate)` 호출
   - Then: 일별 추적 오차 데이터 반환
   - 응답 레코딩: `tiger200_tracking_error.json`

#### 괴리율 시나리오
1. **"ETF 가격과 NAV 간 괴리율을 조회할 수 있다"**
   - Given: TIGER 200 ISIN, 1개월 기간
   - When: `getDivergenceRate(isin, fromDate, toDate)` 호출
   - Then: 일별 괴리율 데이터 반환
   - 응답 레코딩: `tiger200_divergence_rate.json`

#### 활용 예제
1. **"[활용] 평균 추적 오차를 계산할 수 있다"**
   - Given: 추적 오차 데이터
   - When: 절대값의 평균 계산
   - Then: "평균 추적 오차: X.XX%" 출력

2. **"[활용] 괴리율이 높은 날을 찾을 수 있다"**
   - Given: 괴리율 데이터
   - When: 절대값 기준 정렬
   - Then: 괴리율 ±1% 초과 날짜 출력

---

## 10-13. 투자자별 거래 조회 (4개 함수)

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfInvestorTradingLiveTest.kt`

#### 전체 ETF 투자자별 거래 시나리오
1. **"특정 날짜의 전체 ETF 투자자별 거래를 조회할 수 있다"**
   - Given: 특정 거래일
   - When: `getAllInvestorTrading(date)` 호출
   - Then: 전체 ETF의 투자자 유형별 매수/매도 데이터 반환
   - 응답 레코딩: `all_etf_investor_trading.json`

2. **"기간별 전체 ETF 투자자별 거래를 조회할 수 있다"**
   - Given: 시작일, 종료일
   - When: `getAllInvestorTradingByPeriod(fromDate, toDate)` 호출
   - Then: 일별 투자자 거래 데이터 반환
   - 응답 레코딩: `all_etf_investor_trading_period.json`

#### 개별 ETF 투자자별 거래 시나리오
1. **"개별 ETF의 투자자별 거래를 조회할 수 있다"**
   - Given: TIGER 200 ISIN, 특정 날짜
   - When: `getInvestorTrading(isin, date)` 호출
   - Then: TIGER 200의 투자자 유형별 매수/매도 데이터 반환

2. **"개별 ETF의 기간별 투자자별 거래를 조회할 수 있다"**
   - Given: TIGER 200 ISIN, 기간
   - When: `getInvestorTradingByPeriod(isin, fromDate, toDate)` 호출
   - Then: 일별 투자자 거래 추이 반환

#### 활용 예제
1. **"[활용] 외국인 순매수 상위 ETF를 찾을 수 있다"**
   - Given: 전체 ETF 투자자별 거래 데이터
   - When: 외국인 순매수(매수-매도) 계산 후 정렬
   - Then: 상위 10개 ETF 출력

2. **"[활용] 기관 매매 추이를 분석할 수 있다"**
   - Given: 개별 ETF 기간별 데이터
   - When: 기관 순매수 시계열 차트 데이터 생성
   - Then: 일별 기관 순매수 추이 출력

---

## 14-15. 공매도 데이터 조회 (2개 함수)

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/etf/EtfShortLiveTest.kt`

#### 공매도 거래 시나리오
1. **"ETF 공매도 거래 현황을 조회할 수 있다"**
   - Given: TIGER 200 ISIN, 1개월 기간
   - When: `getShortSelling(isin, fromDate, toDate)` 호출
   - Then: 일별 공매도 거래량, 금액 데이터 반환
   - 응답 레코딩: `tiger200_short_selling.json`

#### 공매도 잔고 시나리오
1. **"ETF 공매도 잔고 현황을 조회할 수 있다"**
   - Given: TIGER 200 ISIN, 1개월 기간
   - When: `getShortBalance(isin, fromDate, toDate)` 호출
   - Then: 일별 공매도 잔고 데이터 반환
   - 응답 레코딩: `tiger200_short_balance.json`

#### 활용 예제
1. **"[활용] 공매도 비중을 계산할 수 있다"**
   - Given: 공매도 잔고 데이터
   - When: 잔고 / 상장주식수 * 100 계산
   - Then: "공매도 비중: X.X%" 출력

2. **"[활용] 공매도 증가 추이를 확인할 수 있다"**
   - Given: 공매도 거래 데이터
   - When: 일별 공매도 거래량 추이 분석
   - Then: 급증한 날짜 출력

---

## 전체 테스트 파일 구조

```
Live Test (10개 파일)
├── EtfListLiveTest.kt              (1)  getList
├── EtfComprehensiveLiveTest.kt     (2)  getComprehensiveInfo
├── EtfDailyPricesLiveTest.kt       (3)  getAllDailyPrices
├── EtfOhlcvLiveTest.kt             (4)  getOhlcv
├── EtfAdjustedOhlcvLiveTest.kt     (5)  getAdjustedOhlcv
├── EtfPriceChangesLiveTest.kt      (6)  getPriceChanges
├── EtfPortfolioLiveTest.kt         (7)  getPortfolio
├── EtfPerformanceLiveTest.kt       (8-9) getTrackingError, getDivergenceRate
├── EtfInvestorTradingLiveTest.kt   (10-13) 투자자별 거래 4개 함수
└── EtfShortLiveTest.kt             (14-15) getShortSelling, getShortBalance

Unit Test (11개 파일)
├── EtfListApiSpec.kt
├── EtfComprehensiveApiSpec.kt
├── EtfDailyPricesApiSpec.kt
├── EtfOhlcvApiSpec.kt
├── EtfAdjustedOhlcvApiSpec.kt
├── EtfPriceChangesApiSpec.kt
├── EtfPortfolioApiSpec.kt
├── EtfTrackingErrorApiSpec.kt
├── EtfDivergenceRateApiSpec.kt
├── EtfInvestorTradingApiSpec.kt
└── EtfShortApiSpec.kt
```

## 다음 단계

1. ✅ 위 시나리오를 참고하여 EtfApi 테스트 작성
2. ✅ CorpApi 테스트 작성 → `04-CorpApi_테스트_계획.md` 참고
3. ✅ 전체 체크리스트 확인 → `99-체크리스트.md` 참고
