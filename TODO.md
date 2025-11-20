# kfc 프로젝트 개발 TODO

> **목적**: 이 문서는 kfc(Korea Finance Collector) 프로젝트의 전체 개발 진행 상황을 추적합니다.
> **참고**: 상세한 기술 명세는 `plan/` 디렉토리의 관련 문서를 참조하세요.

---

## 📋 진행 상황 개요

- **총 단계**: 6 Phases
- **현재 단계**: Phase 6 (최종 통합 및 문서화)
- **전체 진행률**: 95%

---

## Phase 0: 프로젝트 초기 설정

> **목표**: Gradle 멀티모듈 프로젝트 구조 생성 및 기본 설정
> **참고 문서**: [plan/17-프로젝트-구조.md](./plan/17-프로젝트-구조.md)

### 0.1 프로젝트 구조 생성

- [x] Gradle 멀티모듈 프로젝트 초기화
  - [x] `settings.gradle.kts` 생성 (rootProject.name = "kfc")
  - [x] 루트 `build.gradle.kts` 생성 (빌드 설정)
  - [x] `gradle/libs.versions.toml` 생성 (버전 카탈로그)

- [x] 서브 모듈 생성
  - [x] `lib/` - 라이브러리 모듈 (메인)
  - [x] `app/` - 데모/테스트 애플리케이션 모듈

**참고**: `plan/17-프로젝트-구조.md` 섹션 4 "빌드 설정"

### 0.2 기본 의존성 설정

- [x] `libs.versions.toml` 의존성 정의
  - [x] Kotlin 2.2.21
  - [x] Ktor Client 3.3.2
  - [x] Kotlinx Serialization 1.9.0
  - [x] Kotlinx Datetime 0.5.0
  - [x] Kotlinx Coroutines 1.8.0
  - [x] Kotlin Logging 5.1.0

**참고**: `plan/17-프로젝트-구조.md` 섹션 4.2 "의존성"

### 0.3 패키지 구조 생성

- [x] `lib/src/main/kotlin/dev/kairoscode/kfc/` 생성
  - [x] `api/krx/` - KRX API 인터페이스
  - [x] `api/naver/` - Naver API 인터페이스
  - [x] `api/opendart/` - OPENDART API 인터페이스
  - [x] `model/krx/` - KRX 데이터 모델
  - [x] `model/naver/` - Naver 데이터 모델
  - [x] `model/opendart/` - OPENDART 데이터 모델
  - [x] `internal/` - 내부 구현 (HTTP, 파싱)
  - [x] `exception/` - 예외 클래스

- [x] `lib/src/test/kotlin/dev/kairoscode/kfc/` 생성
  - [x] 각 api/model 패키지에 대응하는 테스트 디렉토리

**참고**: `plan/17-프로젝트-구조.md` 섹션 2 "패키지 구조"

### 0.4 기본 설정 파일

- [x] `.gitignore` 확인/업데이트 (빌드 아티팩트, IDE 설정 제외)
- [x] `README.md` 확인 (프로젝트 개요, 사용법)
- [x] `LICENSE` 파일 확인 (Apache License 2.0)
- [x] `.editorconfig` 생성 (코드 스타일 통일)

**완료 기준**:
- ✅ `./gradlew build` 성공
- ✅ 패키지 구조가 `plan/17-프로젝트-구조.md`와 일치
- ✅ 모든 의존성이 최신 버전(Kotlin 2.2.21, Ktor 3.3.2 등)

---

## Phase 1: KRX API 기반 인프라 구축

> **목표**: HTTP 클라이언트, 에러 핸들링, 정규화 유틸리티 구현
> **참고 문서**: [plan/05-구현-로드맵.md](./plan/05-구현-로드맵.md) Phase 1

### 1.1 정규화 유틸리티 구현

- [x] `util/NormalizationExtensions.kt` 생성
  - [x] `String.toKrxPrice()` - 가격용 BigDecimal 변환
  - [x] `String.toKrxAmount()` - 금액용 BigDecimal 변환
  - [x] `String.toKrxRate()` - 비율용 BigDecimal 변환
  - [x] `String.toKrxLong()` - 콤마 제거 후 Long 변환
  - [x] `String.toKrxSignedLong()` - 부호 처리 Long 변환
  - [x] `String.toKrxBigDecimal()` - 고정밀 BigDecimal 변환
  - [x] `String.toKrxDate()` - YYYYMMDD → LocalDate
  - [x] `String.toKrxDirection()` - Direction enum 변환

- [x] `model/krx/Direction.kt` enum 생성
  - [x] UP(1), DOWN(2), UNCHANGED(3) 정의

- [x] 테스트: `util/NormalizationExtensionsTest.kt`
  - [x] 정상 값 테스트
  - [x] 엣지 케이스 테스트 ("-", "", null, "0", "123,456")
  - [x] 예외 상황 테스트

**참고**: `plan/04-데이터-매핑-명세.md` 섹션 3 "정규화 로직"

**완료 기준**:
- ✅ 테스트 커버리지 100%
- ✅ 모든 특수 값 처리 검증

### 1.2 내부 HTTP 클라이언트 구현

- [x] `internal/krx/KrxHttpClient.kt` 생성
  - [x] HttpClient 인스턴스 생성 (CIO 엔진)
  - [x] ContentNegotiation 플러그인 설치
  - [x] HttpTimeout 설정 (30초)
  - [x] 공통 헤더 설정 (User-Agent, Accept)

- [x] `internal/krx/HttpExtensions.kt` 생성
  - [x] POST/GET 요청 헬퍼 함수
  - [x] 에러 핸들링 래퍼 함수
  - [x] 응답 파싱 유틸리티 (extractOutput, extractResult)

**참고**: `plan/16-라이브러리-아키텍처.md` 섹션 3.3 "Internal 계층"

**완료 기준**:
- ✅ HttpClient 정상 초기화
- ✅ KRX API 연결 테스트 성공

### 1.3 에러 핸들링 구조

- [x] `exception/KfcException.kt` sealed class 정의
  - [x] `NetworkException` - 네트워크 에러
  - [x] `ParseException` - 파싱 에러
  - [x] `ApiException` - API 에러 응답
  - [x] `RateLimitException` - Rate Limit 에러

**참고**: `plan/06-API-설계.md` 섹션 5 "에러 처리"

**완료 기준**:
- ✅ 모든 예외 클래스 정의 완료
- ✅ 예외 메시지에 충분한 컨텍스트 포함

---

## Phase 2: MDCSTAT04701 구현 (ETF 종합정보)

> **목표**: 최우선 API 엔드포인트 완전 구현
> **참고 문서**: [plan/03-MDCSTAT04701-상세명세.md](./plan/03-MDCSTAT04701-상세명세.md)

### 2.1 데이터 모델 구현

- [x] `model/krx/ComprehensiveEtfInfo.kt` 생성
  - [x] 30+ 필드 data class 정의 (기본 식별, OHLCV, NAV, 시가총액, 52주 가격, 수익률, 추적오차, 괴리율, 보수, 복제방법)
  - [x] `companion object { fun fromRaw(raw: Map<*, *>) }` 팩토리 메서드
  - [x] 헬퍼 메서드: `isNear52WeekHigh()`, `isNear52WeekLow()`, `hasExcessiveDivergence()`, `hasLowFee()`
  - [x] KDoc 주석 작성

**참고**: `plan/03-MDCSTAT04701-상세명세.md` 섹션 2 "데이터 모델"

**완료 기준**:
- ✅ 모든 필드 타입 정확히 매핑
- ✅ fromRaw() 정상 동작
- ✅ 헬퍼 메서드 단위 테스트 통과

### 2.2 API 함수 구현

- [x] `api/krx/KrxEtfApi.kt` 인터페이스 정의
  - [x] `suspend fun getComprehensiveEtfInfo(isin: String, tradeDate: LocalDate): ComprehensiveEtfInfo?`

- [x] `internal/krx/KrxEtfApiImpl.kt` 구현
  - [x] POST 요청 로직 (bld=dbms/MDC/STAT/standard/MDCSTAT04701)
  - [x] 응답 파싱 (output 추출)
  - [x] `ComprehensiveEtfInfo.fromRaw()` 호출

**참고**: `plan/10-함수-시그니처-카탈로그.md` 섹션 1.2

**완료 기준**:
- ✅ API 호출 성공
- ✅ 응답 데이터 정확히 파싱
- ✅ 통합 테스트 통과

### 2.3 테스트 작성

- [x] 단위 테스트: `model/krx/ComprehensiveEtfInfoTest.kt`
  - [x] fromRaw() 테스트 (정상 케이스, null 값 처리)
  - [x] 헬퍼 메서드 테스트

- [ ] 통합 테스트: `api/krx/KrxEtfApiTest.kt`
  - [ ] getComprehensiveEtfInfo() 실제 API 호출 테스트
  - [ ] 에러 케이스 테스트

**참고**: `plan/13-KRX-테스트-시나리오-명세.md` 섹션 2

**완료 기준**:
- ✅ 모든 테스트 통과
- ✅ 실제 KRX API 응답 검증

---

## Phase 3: 나머지 KRX ETF API 구현 ✅ 완료

> **목표**: 15개 KRX API 함수 모두 구현
> **참고 문서**: [plan/10-함수-시그니처-카탈로그.md](./plan/10-함수-시그니처-카탈로그.md)

### 3.1 ETF 목록/일별시세 API

- [x] `getEtfList(): List<EtfListItem>` - MDCSTAT04601
- [x] `getAllEtfDailyPrices(date: LocalDate): List<EtfDailyPrice>` - MDCSTAT04301

### 3.2 가격/OHLCV API

- [x] `getEtfOhlcv(isin: String, fromDate: LocalDate, toDate: LocalDate): List<EtfOhlcv>` - MDCSTAT04501
- [x] `getEtfPriceChanges(fromDate: LocalDate, toDate: LocalDate): List<EtfPriceChange>` - MDCSTAT04401

### 3.3 포트폴리오 API

- [x] `getEtfPortfolio(isin: String, date: LocalDate): List<PortfolioConstituent>` - MDCSTAT05001

### 3.4 성과 추적 API

- [x] `getEtfTrackingError(isin: String, fromDate: LocalDate, toDate: LocalDate): List<TrackingError>` - MDCSTAT05901
- [x] `getEtfDivergenceRate(isin: String, fromDate: LocalDate, toDate: LocalDate): List<DivergenceRate>` - MDCSTAT06001

### 3.5 투자자별 매매 API

- [x] `getAllEtfInvestorTrading(date: LocalDate): List<InvestorTrading>` - MDCSTAT04801
- [x] `getAllEtfInvestorTradingByPeriod(fromDate: LocalDate, toDate: LocalDate): List<InvestorTradingByDate>` - MDCSTAT04802
- [x] `getEtfInvestorTrading(isin: String, date: LocalDate): List<InvestorTrading>` - MDCSTAT04901
- [x] `getEtfInvestorTradingByPeriod(isin: String, fromDate: LocalDate, toDate: LocalDate): List<InvestorTradingByDate>` - MDCSTAT04902

### 3.6 공매도 API

- [x] `getEtfShortSelling(isin: String, fromDate: LocalDate, toDate: LocalDate): List<ShortSelling>` - MDCSTAT31401
- [x] `getEtfShortBalance(isin: String, fromDate: LocalDate, toDate: LocalDate): List<ShortBalance>` - MDCSTAT31501

**완료 기준**:
- ✅ 15개 KRX API 함수 모두 구현 완료
- ✅ 모든 데이터 모델 KDoc 주석 완료
- ✅ Normalization 유틸리티 함수 추가 (toKrxInt, toKrxDouble, toDirection 등)
- [ ] 통합 테스트 작성 (실제 API 호출)

---

## Phase 4: Naver API 구현 ✅ 완료

> **목표**: 네이버 증권 조정주가 OHLCV API 구현
> **참고 문서**: [plan/09-네이버-API-통합-명세.md](./plan/09-네이버-API-통합-명세.md)

### 4.1 데이터 모델 구현

- [x] `model/naver/NaverEtfOhlcv.kt` 생성
  - [x] date, open, high, low, close, volume 필드
  - [x] 조정주가 데이터 (배당, 분할/병합 반영)

### 4.2 API 함수 구현

- [x] `api/naver/NaverEtfApi.kt` 인터페이스 정의
  - [x] `suspend fun getAdjustedOhlcv(ticker: String, fromDate: LocalDate, toDate: LocalDate): List<NaverEtfOhlcv>`

- [x] `internal/naver/NaverEtfApiImpl.kt` 구현
  - [x] Ktor HttpClient 사용
  - [x] XML 응답 파싱 (javax.xml.parsers)
  - [x] 날짜 필터링 및 정렬
  - [x] 휴장일 고려 (count * 1.2)

### 4.3 테스트 작성

- [ ] 통합 테스트: `api/naver/NaverEtfApiTest.kt`

**완료 기준**:
- ✅ XML 파싱 정상 동작
- ✅ Ktor HttpClient 통합
- [ ] 실제 Naver API 응답 테스트 통과

---

## Phase 5: OPENDART API 구현 ✅ 완료

> **목표**: 4개 OPENDART API 함수 구현
> **참고 문서**: [plan/11-OPENDART-API-통합-명세.md](./plan/11-OPENDART-API-통합-명세.md)

### 5.1 공통 모델 구현

- [x] `model/opendart/OpenDartResponse.kt` 제네릭 래퍼 클래스
- [x] `model/opendart/CorpCode.kt` - 고유번호
- [x] `model/opendart/DividendInfo.kt` - 배당 정보
- [x] `model/opendart/StockSplitInfo.kt` - 증자/감자
- [x] `model/opendart/DisclosureItem.kt` - 공시 정보

### 5.2 API 함수 구현

- [x] `api/opendart/OpenDartApi.kt` 인터페이스 정의
  - [x] `suspend fun getCorpCodeList(): List<CorpCode>`
  - [x] `suspend fun getDividendInfo(corpCode: String, year: Int, reportCode: String): List<DividendInfo>`
  - [x] `suspend fun getStockSplitInfo(corpCode: String, year: Int, reportCode: String): List<StockSplitInfo>`
  - [x] `suspend fun searchDisclosures(corpCode: String?, startDate: LocalDate, endDate: LocalDate, pageNo: Int, pageCount: Int): List<DisclosureItem>`

### 5.3 내부 구현

- [x] `internal/opendart/OpenDartApiImpl.kt` 구현
  - [x] API 키 생성자 주입
  - [x] ZIP 파일 처리 (corpCode API, ZipInputStream + XML 파싱)
  - [x] JSON 응답 파싱 (Kotlinx Serialization)
  - [x] 에러 핸들링 (status 코드 체크)
  - [x] 원시 데이터 → 도메인 모델 변환

### 5.4 테스트 작성

- [ ] 통합 테스트: `api/opendart/OpenDartApiTest.kt`
  - [ ] 각 API 함수별 실제 호출 테스트
  - [ ] API 키 환경변수 처리 (`OPENDART_API_KEY`)

**완료 기준**:
- ✅ 4개 OPENDART API 함수 모두 구현 완료
- ✅ ZIP/XML 파싱 정상 동작
- ✅ JSON 직렬화/역직렬화 구현
- [ ] 실제 API 호출 테스트 통과

---

## Phase 6: Facade 패턴 및 최종 통합 ✅ 완료

> **목표**: KfcClient 통합 인터페이스 구현 및 문서화
> **참고 문서**: [plan/16-라이브러리-아키텍처.md](./plan/16-라이브러리-아키텍처.md)

### 6.1 KfcClient Facade 구현

- [x] `KfcClient.kt` 클래스 생성
  - [x] KRX API 델리게이션 (krx 프로퍼티)
  - [x] Naver API 델리게이션 (naver 프로퍼티)
  - [x] OPENDART API 델리게이션 (opendart 프로퍼티, nullable)
  - [x] Companion object factory 패턴 (`create()`)
  - [x] API Key 선택적 파라미터 (opendartApiKey)

**완료 기준**:
- ✅ 모든 20개 API 함수 접근 가능 (KRX 15 + Naver 1 + OPENDART 4)
- ✅ 단일 진입점으로 동작
- ✅ KDoc 주석 완료

### 6.2 문서화

- [x] 각 API 함수 KDoc 주석 작성
  - [x] 파라미터 설명
  - [x] 반환값 설명
  - [x] 예외 상황 설명 (@throws)
  - [x] 사용 예시 코드 (KfcClient)

- [x] `README.md` 업데이트
  - [x] 설치 방법
  - [x] 빠른 시작 가이드
  - [x] Facade 사용 예제
  - [x] API 개수 정정 (22개 → 20개)
  - [x] Roadmap 업데이트

### 6.3 데모 애플리케이션

- [ ] `app/` 모듈에 샘플 코드 작성
  - [ ] ETF 목록 조회 예제
  - [ ] ETF 상세정보 조회 예제
  - [ ] OPENDART API 사용 예제

### 6.4 배포 준비

- [ ] Gradle 배포 설정
  - [ ] Maven Central 배포 설정 (선택사항)
  - [ ] JitPack 배포 설정 (권장)
  - [ ] 버전 관리 (semantic versioning)

- [ ] CI/CD 설정 (선택사항)
  - [ ] GitHub Actions 워크플로우
  - [ ] 자동 테스트 실행
  - [ ] 자동 배포

**완료 기준**:
- ✅ 모든 API 문서화 완료
- ✅ README.md 완성
- ✅ 데모 앱 정상 동작
- ✅ 배포 준비 완료

---

## 테스트 전략

> **참고 문서**: [plan/07-테스트-전략.md](./plan/07-테스트-전략.md), [plan/19-테스트-작성-원칙.md](./plan/19-테스트-작성-원칙.md)

### 테스트 레벨

- **단위 테스트**: 각 함수/클래스 개별 테스트
- **통합 테스트**: 실제 API 호출 테스트 (느림, CI에서 선택적 실행)
- **E2E 테스트**: KfcClient를 통한 전체 시나리오 테스트

### 테스트 커버리지 목표

- [ ] 유틸리티 함수: 100%
- [ ] 데이터 모델: 90% 이상
- [ ] API 구현: 80% 이상
- [ ] 전체 프로젝트: 85% 이상

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 단위 테스트만
./gradlew test --tests '*Test'

# 통합 테스트만 (느림)
./gradlew test --tests '*IntegrationTest'

# 커버리지 리포트
./gradlew koverHtmlReport
```

---

## 품질 체크리스트

### 코드 품질

- [ ] Kotlin 코딩 컨벤션 준수
- [ ] 모든 public API에 KDoc 주석
- [ ] 매직 넘버/문자열 상수화
- [ ] 에러 메시지 명확화

### 성능

- [ ] HTTP 커넥션 풀 재사용
- [ ] 불필요한 객체 생성 최소화
- [ ] Coroutine 적절히 사용

### 보안

- [ ] API 키 하드코딩 금지 (환경변수 사용)
- [ ] 사용자 입력 검증
- [ ] 민감 정보 로깅 금지

---

## 참고 문서 색인

| 문서 | 설명 |
|------|------|
| [01-프로젝트-개요.md](./plan/01-프로젝트-개요.md) | 프로젝트 전체 개요 |
| [02-KRX-ETF-엔드포인트-분석.md](./plan/02-KRX-ETF-엔드포인트-분석.md) | KRX API 엔드포인트 분석 |
| [03-MDCSTAT04701-상세명세.md](./plan/03-MDCSTAT04701-상세명세.md) | 우선 구현 대상 API 상세 |
| [04-데이터-매핑-명세.md](./plan/04-데이터-매핑-명세.md) | 데이터 매핑 규칙 |
| [05-구현-로드맵.md](./plan/05-구현-로드맵.md) | 상세 구현 로드맵 |
| [06-API-설계.md](./plan/06-API-설계.md) | API 설계 원칙 |
| [07-테스트-전략.md](./plan/07-테스트-전략.md) | 테스트 전략 |
| [08-한국-ETF-데이터-커버리지-분석.md](./plan/08-한국-ETF-데이터-커버리지-분석.md) | 데이터 커버리지 분석 |
| [09-네이버-API-통합-명세.md](./plan/09-네이버-API-통합-명세.md) | Naver API 명세 |
| [10-함수-시그니처-카탈로그.md](./plan/10-함수-시그니처-카탈로그.md) | KRX API 함수 시그니처 |
| [11-OPENDART-API-통합-명세.md](./plan/11-OPENDART-API-통합-명세.md) | OPENDART API 명세 |
| [12-OPENDART-함수-시그니처-카탈로그.md](./plan/12-OPENDART-함수-시그니처-카탈로그.md) | OPENDART API 함수 시그니처 |
| [13-KRX-테스트-시나리오-명세.md](./plan/13-KRX-테스트-시나리오-명세.md) | KRX 테스트 시나리오 |
| [14-Naver-테스트-시나리오-명세.md](./plan/14-Naver-테스트-시나리오-명세.md) | Naver 테스트 시나리오 |
| [15-OPENDART-테스트-시나리오-명세.md](./plan/15-OPENDART-테스트-시나리오-명세.md) | OPENDART 테스트 시나리오 |
| [16-라이브러리-아키텍처.md](./plan/16-라이브러리-아키텍처.md) | 아키텍처 설계 |
| [17-프로젝트-구조.md](./plan/17-프로젝트-구조.md) | 프로젝트 구조 및 빌드 |
| [18-확장-전략.md](./plan/18-확장-전략.md) | 향후 확장 전략 |
| [19-테스트-작성-원칙.md](./plan/19-테스트-작성-원칙.md) | 테스트 작성 원칙 |

---

## 마일스톤

| Phase | 목표 | 완료 기준 | 상태 |
|-------|------|----------|------|
| Phase 0 | 프로젝트 초기 설정 | Gradle 빌드 성공, 패키지 구조 생성 | ✅ 완료 |
| Phase 1 | 기반 인프라 구축 | HTTP 클라이언트, 정규화 유틸리티 구현 | ✅ 완료 |
| Phase 2 | MDCSTAT04701 구현 | ETF 종합정보 API 완전 구현 | ✅ 완료 |
| Phase 3 | KRX API 완성 | 15개 KRX API 모두 구현 | ✅ 완료 |
| Phase 4 | Naver API 구현 | 조정주가 OHLCV API 구현 | ✅ 완료 |
| Phase 5 | OPENDART API 구현 | 4개 OPENDART API 구현 | ✅ 완료 |
| Phase 6 | 최종 통합 | KfcClient Facade, 문서화 | ✅ 완료 |

---

## 진행 방법

### 컨텍스트 리셋 시

1. 이 `TODO.md` 파일을 먼저 읽습니다
2. 현재 Phase와 진행 중인 작업을 확인합니다
3. 해당 Phase의 "참고 문서"를 읽어 상세 명세를 파악합니다
4. 체크리스트를 따라 순차적으로 작업합니다
5. 작업 완료 시 체크박스를 `[x]`로 변경합니다

### 작업 순서 원칙

1. **순차 진행**: Phase 0 → Phase 6 순서대로 진행
2. **테스트 우선**: 각 구현 후 즉시 테스트 작성
3. **문서화 동시 진행**: 코드 작성 시 KDoc 주석 함께 작성
4. **점진적 검증**: 각 Phase 완료 시 전체 빌드 및 테스트 실행

### Git 커밋 전략

- Phase 단위로 커밋
- 커밋 메시지: `feat(phase-N): <작업 내용>`
- 예시: `feat(phase-1): implement KRX normalization utilities`

---

**마지막 업데이트**: 2025-11-19
**다음 작업**: 테스트 작성 (Phase 7)
