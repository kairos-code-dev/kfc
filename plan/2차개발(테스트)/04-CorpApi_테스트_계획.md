# CorpApi 테스트 시나리오

## 개요

CorpApi는 OPENDART API를 사용하여 4개의 함수를 제공합니다.
각 함수는 **Live Test**(실제 API 호출)와 **Unit Test**(Mock 기반)로 테스트합니다.

**주의사항**:
- OPENDART API 키 필요 (https://opendart.fss.or.kr/)
- 일일 요청 제한: 20,000건
- Live Test 실행 시 키 소진에 주의

**테스트 작성 원칙**:
- API 사용 방법을 명확히 보여주는 예제 중심
- Given-When-Then 패턴으로 시나리오 구조화
- 실제 활용 예제 포함 (종목코드 변환, 배당 이력 조회 등)

---

## 1. getCorpCodeList() - 고유번호 목록 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/corp/CorpCodeLiveTest.kt`

#### 기본 시나리오
1. **"전체 법인 고유번호 목록을 조회할 수 있다"**
   - Given: KfcClient with OPENDART API Key
   - When: `client.corp.getCorpCodeList()` 호출
   - Then: 10,000개 이상의 법인 정보 반환
   - Then: 각 법인은 corp_code, stock_code, corp_name 포함
   - 응답 레코딩: `corp_code_list.json`
   - 주의: 대용량 데이터 (10MB 이상)

2. **"ZIP 압축 해제와 XML 파싱이 자동으로 처리된다"**
   - Given: OPENDART API는 ZIP 파일 반환
   - When: getCorpCodeList() 호출
   - Then: 자동으로 압축 해제 및 XML 파싱
   - Then: List<CorpCode> 형태로 반환

#### 활용 예제 시나리오
1. **"[활용] 종목코드로 OPENDART 고유번호를 찾을 수 있다"**
   - Given: 고유번호 목록 조회
   - When: stock_code로 필터링 (예: "005930" 삼성전자)
   - Then: corp_code "00126380" 반환
   - 사용 예제:
     ```
     val samsung = corpCodeList.find { it.stockCode == "005930" }
     val corpCode = samsung?.corpCode
     ```

2. **"[활용] 법인명으로 고유번호를 검색할 수 있다"**
   - Given: 고유번호 목록 조회
   - When: corp_name에 "삼성" 포함된 법인 검색
   - Then: 삼성전자, 삼성SDI 등 여러 법인 반환
   - 사용 예제:
     ```
     val samsungCorps = corpCodeList.filter {
         it.corpName.contains("삼성")
     }
     ```

3. **"[활용] 상장 법인만 필터링할 수 있다"**
   - Given: 고유번호 목록 (상장/비상장 모두 포함)
   - When: stock_code가 null이 아닌 법인 필터링
   - Then: 상장 법인만 반환
   - 사용 예제:
     ```
     val listedCorps = corpCodeList.filter {
         it.stockCode != null
     }
     ```

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/corp/CorpCodeApiSpec.kt`

#### 사용법 시나리오 (@Nested "getCorpCodeList() 사용법")
1. **"기본 사용법 - 전체 법인 목록을 조회할 수 있다"**
   - Mock: `corp_code_list.json`
   - 시나리오: 파라미터 없이 getCorpCodeList() 호출
   - 검증: 법인 목록 반환

2. **"검색 예제 - 특정 종목코드의 고유번호를 찾을 수 있다"**
   - Mock: `corp_code_list.json`
   - 시나리오: find { stock_code == "005930" }
   - 예제: 삼성전자 고유번호 조회

3. **"필터링 예제 - 상장 법인만 조회할 수 있다"**
   - Mock: `corp_code_list.json`
   - 시나리오: filter { stock_code != null }
   - 검증: 모든 결과가 stock_code 보유

#### API 명세 시나리오 (@Nested "getCorpCodeList() API 명세")
1. **"[명세] 반환 타입은 List<CorpCode>이다"**
   - 검증: 반환 타입 확인

2. **"[명세] 각 법인은 corp_code, corp_name을 포함한다"**
   - 검증: 필수 필드 존재

3. **"[명세] corp_code는 8자리 숫자 문자열이다"**
   - 검증: corp_code 형식 (8자리)

4. **"[명세] stock_code는 6자리 숫자 문자열이거나 null이다"**
   - 검증: stock_code 형식 (6자리 또는 null)

---

## 2. getDividendInfo() - 배당 정보 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/corp/DividendLiveTest.kt`

#### 기본 시나리오
1. **"특정 법인의 배당 정보를 조회할 수 있다"**
   - Given: 삼성전자 corp_code (00126380), year (2024)
   - When: `getDividendInfo(corpCode, year)` 호출
   - Then: 배당금 정보 반환 (현금배당, 주식배당 등)
   - 응답 레코딩: `samsung_dividend_2024.json`

2. **"다양한 보고서 타입으로 조회할 수 있다"**
   - Given: 삼성전자 corp_code
   - When: reportCode를 변경하여 호출
     - 11011: 사업보고서
     - 11012: 반기보고서
     - 11013: 1분기보고서
     - 11014: 3분기보고서
   - Then: 각 보고서의 배당 정보 반환

3. **"배당이 없는 법인 조회 시 빈 결과 반환한다"**
   - Given: 배당이 없는 법인
   - When: getDividendInfo() 호출
   - Then: 빈 리스트 반환
   - 응답 레코딩: `empty_dividend.json`

#### 활용 예제 시나리오
1. **"[활용] 연간 배당금 합계를 계산할 수 있다"**
   - Given: 2024년 배당 정보
   - When: 현금배당 금액 합산
   - Then: "2024년 총 배당금: X,XXX원" 출력

2. **"[활용] 배당 이력을 조회할 수 있다"**
   - Given: 최근 5년 배당 정보 (2020-2024)
   - When: 각 연도별 getDividendInfo() 호출
   - Then: 연도별 배당금 추이 데이터 생성

3. **"[활용] 배당수익률을 계산할 수 있다"**
   - Given: 배당금 정보와 현재 주가
   - When: (연간배당금 / 주가) * 100 계산
   - Then: "배당수익률: X.X%" 출력

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/corp/DividendApiSpec.kt`

#### 사용법 시나리오 (@Nested "getDividendInfo() 사용법")
1. **"기본 사용법 - 특정 연도 배당 정보를 조회할 수 있다"**
   - Mock: `samsung_dividend_2024.json`
   - 시나리오: corpCode, year 지정하여 조회
   - 예제: 현금배당 금액 출력

2. **"보고서 타입 예제 - 다양한 보고서의 배당 정보를 조회할 수 있다"**
   - Mock: 여러 보고서 타입별 JSON 파일
   - 시나리오: reportCode 변경하여 조회

#### API 명세 시나리오 (@Nested "getDividendInfo() API 명세")
1. **"[명세] 반환 타입은 List<DividendInfo>이다"**
   - 검증: 반환 타입 확인

2. **"[명세] 2015년 이후 데이터만 지원한다"**
   - 검증: 2014년 이하 조회 시 에러 또는 빈 결과

3. **"[명세] corpCode는 8자리 숫자 문자열이다"**
   - 검증: corpCode 형식

4. **"[명세] reportCode는 11011, 11012, 11013, 11014 중 하나이다"**
   - 검증: 잘못된 reportCode 시 에러

---

## 3. getStockSplitInfo() - 주식 분할 정보 조회

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/corp/StockSplitLiveTest.kt`

#### 기본 시나리오
1. **"주식 분할/병합 정보를 조회할 수 있다"**
   - Given: 특정 법인 corp_code, year
   - When: `getStockSplitInfo(corpCode, year)` 호출
   - Then: 주식 분할/병합 정보 반환
   - 응답 레코딩: `stock_split_2024.json`

2. **"액면분할이 없는 경우 빈 결과 반환한다"**
   - Given: 액면분할이 없는 법인
   - When: getStockSplitInfo() 호출
   - Then: 빈 리스트 반환
   - 응답 레코딩: `empty_stock_split.json`

#### 활용 예제 시나리오
1. **"[활용] 조정주가 계산 시 분할 비율을 적용할 수 있다"**
   - Given: 주식 분할 정보 (예: 1주 → 2주 분할)
   - When: 과거 주가에 분할 비율 적용
   - Then: 조정주가 = 원주가 / 2

2. **"[활용] 분할 이력을 조회할 수 있다"**
   - Given: 최근 10년 분할 정보
   - When: 각 연도별 getStockSplitInfo() 호출
   - Then: 분할 이벤트 타임라인 생성

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/corp/StockSplitApiSpec.kt`

#### 사용법 시나리오 (@Nested "getStockSplitInfo() 사용법")
1. **"기본 사용법 - 특정 연도 분할 정보를 조회할 수 있다"**
   - Mock: `stock_split_2024.json`
   - 시나리오: corpCode, year 지정하여 조회
   - 예제: 분할 비율 출력

#### API 명세 시나리오 (@Nested "getStockSplitInfo() API 명세")
1. **"[명세] 반환 타입은 List<StockSplitInfo>이다"**
   - 검증: 반환 타입 확인

2. **"[명세] 분할 비율 정보를 포함한다"**
   - 검증: 분할 전후 주식 수 정보 존재

---

## 4. searchDisclosures() - 공시 검색

### Live Test 시나리오

**파일**: `src/liveTest/kotlin/dev/kairoscode/kfc/live/corp/DisclosureLiveTest.kt`

#### 기본 시나리오
1. **"특정 기간의 공시 목록을 조회할 수 있다"**
   - Given: startDate, endDate 지정
   - When: `searchDisclosures(corpCode, startDate, endDate)` 호출
   - Then: 해당 기간의 공시 목록 반환
   - 응답 레코딩: `samsung_disclosure_1month.json`

2. **"전체 법인의 공시를 조회할 수 있다"**
   - Given: corpCode = null, 특정 날짜
   - When: searchDisclosures(null, startDate, endDate) 호출
   - Then: 모든 법인의 공시 반환 (페이징 처리)
   - 응답 레코딩: `all_corp_disclosure_1day.json`

3. **"페이징 처리가 가능하다"**
   - Given: pageNo, pageCount 지정
   - When: searchDisclosures(..., pageNo=2, pageCount=50) 호출
   - Then: 2페이지 결과 반환 (50개)

#### 활용 예제 시나리오
1. **"[활용] 최근 공시를 실시간으로 모니터링할 수 있다"**
   - Given: 오늘 날짜
   - When: corpCode=null, 1시간마다 조회
   - Then: 새로운 공시 감지 및 알림

2. **"[활용] 특정 키워드가 포함된 공시를 찾을 수 있다"**
   - Given: 공시 목록 조회
   - When: report_nm에 "배당" 포함된 공시 필터링
   - Then: 배당 관련 공시만 반환

3. **"[활용] 공시 통계를 분석할 수 있다"**
   - Given: 1개월 공시 데이터
   - When: 공시 유형별 그룹화
   - Then: "사업보고서: X건, 분기보고서: Y건" 출력

### Unit Test 시나리오

**파일**: `src/test/kotlin/dev/kairoscode/kfc/api/corp/DisclosureApiSpec.kt`

#### 사용법 시나리오 (@Nested "searchDisclosures() 사용법")
1. **"기본 사용법 - 특정 법인의 공시를 조회할 수 있다"**
   - Mock: `samsung_disclosure_1month.json`
   - 시나리오: corpCode, 기간 지정하여 조회
   - 예제: 공시 제목 목록 출력

2. **"전체 조회 예제 - 모든 법인의 공시를 조회할 수 있다"**
   - Mock: `all_corp_disclosure_1day.json`
   - 시나리오: corpCode=null로 조회
   - 검증: 다양한 법인의 공시 포함

3. **"페이징 예제 - 페이지 단위로 공시를 조회할 수 있다"**
   - Mock: 페이지별 JSON 파일
   - 시나리오: pageNo 변경하여 여러 페이지 조회

#### API 명세 시나리오 (@Nested "searchDisclosures() API 명세")
1. **"[명세] 반환 타입은 List<DisclosureItem>이다"**
   - 검증: 반환 타입 확인

2. **"[명세] pageCount는 최대 100이다"**
   - 검증: 100 초과 시 에러 또는 100으로 제한

3. **"[명세] 날짜 범위는 최대 1년이다"**
   - 검증: 1년 초과 시 에러 또는 자동 분할

4. **"[명세] 각 공시는 rcept_no, corp_name, report_nm을 포함한다"**
   - 검증: 필수 필드 존재

---

## 전체 테스트 파일 구조

```
Live Test (4개 파일)
├── CorpCodeLiveTest.kt         (1) getCorpCodeList
├── DividendLiveTest.kt          (2) getDividendInfo
├── StockSplitLiveTest.kt        (3) getStockSplitInfo
└── DisclosureLiveTest.kt        (4) searchDisclosures

Unit Test (4개 파일)
├── CorpCodeApiSpec.kt
├── DividendApiSpec.kt
├── StockSplitApiSpec.kt
└── DisclosureApiSpec.kt
```

## 테스트 작성 시 주의사항

### OPENDART API 제한사항
1. **일일 요청 제한**: 20,000건
   - Live Test 실행 시 소비량 체크
   - getCorpCodeList()는 1건으로 카운트

2. **응답 시간**: 일반적으로 1-3초
   - timeout 설정: 30초 권장

3. **에러 응답**:
   - 잘못된 API 키: 401 Unauthorized
   - 요청 제한 초과: 429 Too Many Requests
   - 잘못된 파라미터: 400 Bad Request

### Rate Limiting 전략
```kotlin
// 예제: Live Test에서 지연 시간 추가
@Test
fun `multiple API calls with rate limiting`() = liveTest {
    val corpCodes = listOf("00126380", "00222206", "00164779")

    corpCodes.forEach { corpCode ->
        val dividend = client.corp.getDividendInfo(corpCode, 2024)
        println("$corpCode: ${dividend.size}건")

        delay(500) // 0.5초 지연 (초당 2건 제한)
    }
}
```

## 다음 단계

1. ✅ 위 시나리오를 참고하여 CorpApi 테스트 작성
2. ✅ build.gradle.kts 설정 추가 → `05-build_gradle_설정.md` 참고
3. ✅ 전체 체크리스트 확인 → `99-체크리스트.md` 참고
