# OPENDART API 테스트 시나리오 명세

> **목적**: OPENDART API 함수의 테스트 시나리오 정의
> **참조 문서**: plan/12-OPENDART-함수-시그니처-카탈로그.md
> **테스트 패턴**: AAA (Arrange-Act-Assert)

---

## 목차

1. [테스트 데이터](#1-테스트-데이터)
2. [기업 코드 조회](#2-기업-코드-조회)
3. [배당 정보](#3-배당-정보)
4. [주식 분할/병합](#4-주식-분할병합)
5. [공시 검색](#5-공시-검색)
6. [에러 처리](#6-에러-처리)
7. [통합 시나리오](#7-통합-시나리오)

---

## 1. 테스트 데이터

### 1.1 고정 테스트 데이터

| 항목 | 값 | 비고 |
|------|-----|------|
| **KODEX 200** | | |
| - Ticker | 069500 | 6자리 |
| - corp_code | 00164779 | 8자리 (가정) |
| - 운용사 | 삼성자산운용 | |
| **TIGER 200** | | |
| - Ticker | 102110 | |
| - corp_code | (조회 필요) | |
| **삼성전자** | | |
| - Ticker | 005930 | |
| - corp_code | 00126380 | 8자리 |
| **테스트 날짜** | | |
| - 사업연도 | 2023 | |
| - 검색 시작 | 2024-01-01 | |
| - 검색 종료 | 2024-01-31 | |
| **API 키** | | |
| - 환경 변수 | OPENDART_API_KEY | |

---

## 2. 기업 코드 조회

### 2.1 getCorpCodeList() - 전체 기업 코드 조회

#### TC-CORP-001: 전체 기업 코드 다운로드

**사전 조건**:
- OPENDART API 정상 작동
- API 키 유효

**테스트 단계**:
1. `getCorpCodeList()` 함수 호출
2. 반환된 목록 확인

**예상 결과**:
- 60,000개 이상의 기업 코드 반환
- 각 항목에 다음 필드 포함:
  - corpCode (8자리)
  - corpName (기업명)
  - stockCode (6자리 또는 null)
  - modifyDate (수정일)

**검증 항목**:
- [ ] 데이터 개수 >= 60,000
- [ ] corpCode 형식: 8자리 숫자
- [ ] stockCode 형식: 6자리 숫자 또는 null
- [ ] 모든 필수 필드 존재

---

#### TC-CORP-002: ZIP 파일 압축 해제

**사전 조건**:
- OPENDART corpCode.xml API 응답 (ZIP 형식)

**테스트 단계**:
1. corpCode.xml 엔드포인트 호출
2. ZIP 파일 다운로드
3. 압축 해제 및 XML 파싱
4. CorpCode 리스트 변환

**예상 결과**:
- ZIP 파일 다운로드 성공
- XML 파싱 성공
- 60,000개 이상의 데이터

**검증 항목**:
- [ ] ZIP 압축 해제 성공
- [ ] XML 파싱 성공
- [ ] 데이터 개수 >= 60,000
- [ ] 소요 시간 < 30초

---

#### TC-CORP-003: 상장 기업 필터링

**사전 조건**:
- `getCorpCodeList()` 정상 작동

**테스트 단계**:
1. `getCorpCodeList()` 호출
2. stockCode가 null이 아닌 항목만 필터링

**예상 결과**:
- 약 2,500개의 상장 기업
- 모든 항목에 stockCode 존재

**검증 항목**:
- [ ] 필터링 후 개수: 2,000 ~ 3,000
- [ ] 모든 항목의 stockCode != null
- [ ] stockCode 형식: `\d{6}`

---

### 2.2 getCorpCodeByStockCode() - Ticker로 corp_code 조회

#### TC-MAP-001: KODEX 200 매핑

**사전 조건**:
- stockCode: "069500" (KODEX 200)
- `getCorpCodeList()` 사전 호출 (매핑 데이터 로드)

**테스트 단계**:
1. `getCorpCodeByStockCode("069500")` 호출
2. 반환된 corp_code 확인

**예상 결과**:
- corp_code 반환 (8자리)
- 기업명에 "KODEX 200" 또는 "삼성자산운용" 포함

**검증 항목**:
- [ ] corp_code != null
- [ ] corp_code 형식: 8자리 숫자
- [ ] 기업명 확인

---

#### TC-MAP-002: 존재하지 않는 Ticker

**사전 조건**:
- stockCode: "999999" (존재하지 않음)

**테스트 단계**:
1. `getCorpCodeByStockCode("999999")` 호출

**예상 결과**:
- null 반환

**검증 항목**:
- [ ] 반환값 = null

---

#### TC-MAP-003: 비상장 기업 (stockCode 없음)

**사전 조건**:
- 비상장 기업 (stockCode가 없는 기업)

**테스트 단계**:
1. `getCorpCodeList()` 호출
2. stockCode가 null인 항목 선택
3. `getCorpCodeByStockCode(null)` 호출

**예상 결과**:
- null 반환 또는 IllegalArgumentException

**검증 항목**:
- [ ] null 반환 또는 예외 발생

---

## 3. 배당 정보

### 3.1 getDividendInfo() - 배당 정보 조회

#### TC-DIV-001: 삼성전자 배당 조회 (2023년)

**사전 조건**:
- corp_code: "00126380" (삼성전자)
- 사업연도: 2023
- reportCode: ANNUAL (연간)

**테스트 단계**:
1. `getDividendInfo(corp_code, 2023, ANNUAL)` 호출
2. 반환된 배당 정보 확인

**예상 결과**:
- 배당 정보 반환
- 각 항목에 다음 필드 포함:
  - dividendDate (배당 기준일)
  - dividendPerShare (주당 배당금)
  - dividendType (현금/주식)

**검증 항목**:
- [ ] 데이터 존재
- [ ] dividendPerShare > 0
- [ ] dividendDate 형식 유효

---

#### TC-DIV-002: 배당 미실시 기업

**사전 조건**:
- corp_code: (배당 미실시 기업)
- 사업연도: 2023

**테스트 단계**:
1. `getDividendInfo(corp_code, 2023, ANNUAL)` 호출

**예상 결과**:
- 빈 리스트 반환

**검증 항목**:
- [ ] 데이터 개수 = 0

---

#### TC-DIV-003: 분기 배당 조회

**사전 조건**:
- corp_code: "00126380" (삼성전자)
- 사업연도: 2023
- reportCode: Q1, Q2, Q3, Q4 (분기별)

**테스트 단계**:
1. `getDividendInfo(corp_code, 2023, Q1)` 호출
2. `getDividendInfo(corp_code, 2023, Q2)` 호출
3. `getDividendInfo(corp_code, 2023, Q3)` 호출
4. `getDividendInfo(corp_code, 2023, Q4)` 호출

**예상 결과**:
- 각 분기별 배당 정보 반환
- 연간 배당 = 분기 배당 합계

**검증 항목**:
- [ ] 각 분기 배당 데이터 존재
- [ ] 분기 배당 합계 ≈ 연간 배당

---

#### TC-DIV-004: 배당 유형 (현금/주식)

**사전 조건**:
- corp_code: "00126380"
- 사업연도: 2023

**테스트 단계**:
1. `getDividendInfo(corp_code, 2023, ANNUAL)` 호출
2. 배당 유형 확인

**예상 결과**:
- 배당 유형: "현금" 또는 "주식"
- 현금 배당의 경우 dividendPerShare > 0

**검증 항목**:
- [ ] dividendType 존재
- [ ] 현금 배당 시 금액 > 0

---

## 4. 주식 분할/병합

### 4.1 getStockSplitInfo() - 주식 분할 정보 조회

#### TC-SPLIT-001: 주식 분할 이벤트 조회

**사전 조건**:
- corp_code: (주식 분할 이력이 있는 기업)
- 사업연도: (분할 발생 연도)

**테스트 단계**:
1. `getStockSplitInfo(corp_code, 연도, ANNUAL)` 호출
2. 반환된 분할 정보 확인

**예상 결과**:
- 주식 분할 정보 반환
- 각 항목에 다음 필드 포함:
  - splitDate (분할 기준일)
  - splitRatio (분할 비율, 예: "2:1")
  - splitType (분할/병합)

**검증 항목**:
- [ ] 데이터 존재
- [ ] splitRatio 형식 유효
- [ ] splitDate 형식 유효

---

#### TC-SPLIT-002: 분할 이력 없는 기업

**사전 조건**:
- corp_code: (분할 이력 없는 기업)
- 사업연도: 2023

**테스트 단계**:
1. `getStockSplitInfo(corp_code, 2023, ANNUAL)` 호출

**예상 결과**:
- 빈 리스트 반환

**검증 항목**:
- [ ] 데이터 개수 = 0

---

#### TC-SPLIT-003: 주식 병합 이벤트

**사전 조건**:
- corp_code: (주식 병합 이력이 있는 기업)
- 사업연도: (병합 발생 연도)

**테스트 단계**:
1. `getStockSplitInfo(corp_code, 연도, ANNUAL)` 호출
2. splitType = "병합" 확인

**예상 결과**:
- 병합 정보 반환
- splitRatio: "1:2" (예: 2주를 1주로 병합)

**검증 항목**:
- [ ] splitType = "병합"
- [ ] splitRatio 형식 유효

---

## 5. 공시 검색

### 5.1 searchDisclosures() - 공시 검색

#### TC-SEARCH-001: 기업별 공시 검색 (1개월)

**사전 조건**:
- corp_code: "00126380" (삼성전자)
- 기간: 2024-01-01 ~ 2024-01-31

**테스트 단계**:
1. `searchDisclosures(corp_code, 시작일, 종료일)` 호출
2. 반환된 공시 목록 확인

**예상 결과**:
- 공시 목록 반환 (페이지당 최대 100건)
- 각 항목에 다음 필드 포함:
  - receiptNumber (접수번호)
  - corpName (기업명)
  - reportName (공시 제목)
  - receivedDate (접수일)

**검증 항목**:
- [ ] 데이터 개수 <= 100 (1페이지)
- [ ] corpName = "삼성전자"
- [ ] receivedDate 범위: 2024-01-01 ~ 2024-01-31

---

#### TC-SEARCH-002: 공시 유형 필터링

**사전 조건**:
- corp_code: "00126380"
- 기간: 2024-01-01 ~ 2024-01-31
- publicationType: "A" (정기공시)

**테스트 단계**:
1. `searchDisclosures(corp_code, 시작일, 종료일, publicationType = "A")` 호출
2. 반환된 공시 유형 확인

**예상 결과**:
- 정기공시만 반환
- 사업보고서, 분기보고서 등

**검증 항목**:
- [ ] 모든 공시가 정기공시 유형
- [ ] reportName에 "보고서" 포함

---

#### TC-SEARCH-003: 페이징 (2페이지 이상)

**사전 조건**:
- corp_code: "00126380"
- 기간: 2023-01-01 ~ 2023-12-31 (1년, 100건 초과 예상)

**테스트 단계**:
1. `searchDisclosures(corp_code, 시작일, 종료일, pageNo = 1)` 호출
2. 총 페이지 수 확인
3. `searchDisclosures(corp_code, 시작일, 종료일, pageNo = 2)` 호출

**예상 결과**:
- 1페이지: 최대 100건
- 2페이지: 나머지 데이터
- 중복 없음

**검증 항목**:
- [ ] 1페이지 데이터 <= 100
- [ ] 2페이지 데이터 존재
- [ ] 1페이지와 2페이지 중복 없음

---

#### TC-SEARCH-004: 전체 공시 검색 (corp_code 없음)

**사전 조건**:
- corp_code: null
- 기간: 2024-01-15 ~ 2024-01-15 (단일 날짜)

**테스트 단계**:
1. `searchDisclosures(null, 날짜, 날짜)` 호출

**예상 결과**:
- 해당 날짜의 모든 공시 반환 (100건 제한)

**검증 항목**:
- [ ] 데이터 개수 <= 100
- [ ] 다양한 기업의 공시 포함

---

### 5.2 searchDisclosuresAll() - 전체 페이지 자동 조회

#### TC-SEARCH-ALL-001: 1년 전체 공시 조회

**사전 조건**:
- corp_code: "00126380"
- 기간: 2023-01-01 ~ 2023-12-31

**테스트 단계**:
1. `searchDisclosuresAll(corp_code, 시작일, 종료일)` 호출
2. 자동 페이징으로 모든 데이터 수집

**예상 결과**:
- 100건 이상의 공시 반환 (페이지 제한 없음)
- 모든 페이지 데이터 병합
- 중복 없음

**검증 항목**:
- [ ] 데이터 개수 > 100
- [ ] 중복 receiptNumber 없음
- [ ] 날짜 범위 일치

---

#### TC-SEARCH-ALL-002: Rate Limiting 준수

**사전 조건**:
- corp_code: "00126380"
- 기간: 2020-01-01 ~ 2023-12-31 (4년, 여러 페이지)

**테스트 단계**:
1. `searchDisclosuresAll(corp_code, 시작일, 종료일)` 호출
2. 각 페이지 요청 간격 측정

**예상 결과**:
- 페이지 요청 간격: >= 100ms
- Rate limit 초과 에러 없음

**검증 항목**:
- [ ] 요청 간격 >= 100ms
- [ ] Rate limit 에러 없음

---

## 6. 에러 처리

### 6.1 잘못된 입력

#### TC-ERR-001: 잘못된 corp_code 형식

**사전 조건**:
- corp_code: "INVALID" (8자리 숫자 아님)

**테스트 단계**:
1. `getDividendInfo("INVALID", 2023, ANNUAL)` 호출

**예상 결과**:
- IllegalArgumentException 발생

**검증 항목**:
- [ ] IllegalArgumentException 발생

---

#### TC-ERR-002: 존재하지 않는 corp_code

**사전 조건**:
- corp_code: "99999999" (존재하지 않음)

**테스트 단계**:
- `getDividendInfo("99999999", 2023, ANNUAL)` 호출

**예상 결과**:
- 빈 리스트 반환 또는 NoDataException

**검증 항목**:
- [ ] 빈 데이터 또는 예외 발생

---

#### TC-ERR-003: 미래 날짜

**사전 조건**:
- corp_code: "00126380"
- 날짜: 현재 + 1년 (미래)

**테스트 단계**:
1. `searchDisclosures(corp_code, 미래날짜, 미래날짜)` 호출

**예상 결과**:
- 빈 리스트 반환

**검증 항목**:
- [ ] 데이터 개수 = 0

---

### 6.2 API 에러

#### TC-ERR-004: 잘못된 API 키

**사전 조건**:
- API 키: "INVALID_KEY"

**테스트 단계**:
1. 잘못된 API 키로 클라이언트 생성
2. `getCorpCodeList()` 호출

**예상 결과**:
- AuthenticationException 발생
- 에러 코드: "010" (인증 실패)

**검증 항목**:
- [ ] AuthenticationException 발생
- [ ] 에러 메시지 포함

---

#### TC-ERR-005: Rate Limit 초과

**사전 조건**:
- 일일 요청 한도: 20,000건
- 현재 요청 수: 19,995건

**테스트 단계**:
1. 10회 연속 API 호출 (한도 초과)

**예상 결과**:
- RateLimitExceededException 발생
- 에러 코드: "020"

**검증 항목**:
- [ ] RateLimitExceededException 발생
- [ ] 재시도 로직 작동

---

#### TC-ERR-006: 필수 파라미터 누락

**사전 조건**:
- corp_code: null

**테스트 단계**:
1. `getDividendInfo(null, 2023, ANNUAL)` 호출

**예상 결과**:
- IllegalArgumentException 발생

**검증 항목**:
- [ ] IllegalArgumentException 발생

---

## 7. 통합 시나리오

### 7.1 ETF 배당 정보 수집

#### TC-INT-001: Ticker → corp_code → 배당 정보

**시나리오**:
1. `getCorpCodeList()` 호출하여 전체 매핑 데이터 로드
2. `getCorpCodeByStockCode("069500")` 호출 → KODEX 200 corp_code 획득
3. `getDividendInfo(corp_code, 2023, ANNUAL)` 호출 → 배당 정보 조회
4. DB 저장 (dividend_info 테이블)

**예상 결과**:
- corp_code 매핑 성공
- 배당 정보 조회 완료
- DB 저장 성공

**검증 항목**:
- [ ] corp_code != null
- [ ] 배당 정보 존재
- [ ] DB 저장 성공

---

#### TC-INT-002: 전체 ETF 배당 정보 배치 수집

**시나리오**:
1. KRX API로 전체 ETF 목록 조회 (200개)
2. 각 ETF의 ticker → corp_code 매핑
3. 매핑 성공한 ETF에 대해 배당 정보 조회 (100ms 대기)
4. 결과를 DB에 저장

**예상 결과**:
- 200개 ETF 중 150개 이상 매핑 성공 (ETF 운용사 기준)
- 배당 정보 수집 완료
- 소요 시간: 약 20초

**검증 항목**:
- [ ] 매핑 성공률 >= 75%
- [ ] 배당 정보 수집 성공
- [ ] 소요 시간: 15초 ~ 30초

---

### 7.2 Corporate Actions 통합 검증

#### TC-INT-003: 배당 정보 + Naver 조정 가격

**시나리오**:
1. OPENDART: `getDividendInfo(corp_code, 2023, ANNUAL)` 호출 → 배당금 획득
2. 배당락일 확인
3. Naver API: 배당락일 전후 조정 종가 조회
4. 조정 인수 변화 = (배당금 / 배당락일 전 종가)
5. 계산값과 Naver 조정 인수 비교

**예상 결과**:
- 배당금 정보 획득
- Naver 조정 인수 변화와 계산값 일치 (±5%)

**검증 항목**:
- [ ] 배당금 > 0
- [ ] 배당락일 확인
- [ ] 조정 인수 일치도 > 95%

---

#### TC-INT-004: 주식 분할 + Naver 조정 가격

**시나리오**:
1. OPENDART: `getStockSplitInfo(corp_code, 연도, ANNUAL)` 호출 → 분할 비율 획득
2. 분할일 확인
3. Naver API: 분할일 전후 조정 종가 조회
4. 조정 인수 변화 = (분할 비율, 예: 2:1 → 0.5)
5. 계산값과 Naver 조정 인수 비교

**예상 결과**:
- 분할 정보 획득
- Naver 조정 인수 변화 ≈ 분할 비율

**검증 항목**:
- [ ] 분할 비율 확인
- [ ] 분할일 확인
- [ ] 조정 인수 일치

---

### 7.3 공시 모니터링

#### TC-INT-005: 일일 공시 모니터링

**시나리오**:
1. DB에서 모니터링 대상 ETF 리스트 조회 (50개)
2. 각 ETF의 corp_code 매핑
3. 오늘 날짜로 공시 검색: `searchDisclosures(corp_code, 오늘, 오늘)`
4. 배당 관련 공시 필터링: "배당" 키워드
5. 알림 발송

**예상 결과**:
- 50개 ETF 공시 검색 완료
- 배당 관련 공시 필터링 성공
- 알림 발송 (있는 경우)

**검증 항목**:
- [ ] 검색 성공률 = 100%
- [ ] 배당 공시 필터링 정확도
- [ ] 소요 시간 < 10초

---

### 7.4 데이터 일관성 검증

#### TC-INT-006: OPENDART vs KRX 데이터 교차 검증

**시나리오**:
1. KRX API: `getEtfList()` 호출 → 운용사명 획득
2. OPENDART: `getCorpCodeList()` 호출 → corp_code + 기업명 획득
3. 운용사명과 OPENDART 기업명 매칭
4. 일치율 계산

**예상 결과**:
- ETF 운용사와 OPENDART 기업명 일치
- 일치율 > 80%

**검증 항목**:
- [ ] 매칭 성공률 > 80%
- [ ] 불일치 케이스 로깅

---

## 요약

| 카테고리 | 시나리오 수 |
|---------|-----------|
| 기업 코드 조회 | 6 |
| 배당 정보 | 4 |
| 주식 분할/병합 | 3 |
| 공시 검색 | 6 |
| 에러 처리 | 6 |
| 통합 시나리오 | 6 |
| **합계** | **31** |

---

**작성일**: 2025-01-18
**버전**: v1.0
**작성자**: kotlin-krx 프로젝트
