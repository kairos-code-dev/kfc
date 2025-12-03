# KFC 프로젝트 대규모 리팩토링 계획 (STEP 1)

## 개요

이 문서는 KFC(Korea Finance Collector) 프로젝트의 패키지 구조를 현재 **소스별 구조**에서 **도메인 우선 구조**로 리팩토링하는 상세한 계획입니다.

- **문서 버전**: 1.0
- **작성일**: 2025-12-03
- **적용 범위**: 전체 프로젝트 구조 재조직
- **목표**: 네임스페이스 표준(doc/네임스페이스.md)에 100% 부합하는 구조 달성

---

## 1. 새로운 패키지 구조

### 1.1 전체 구조도

```
dev.kairoscode.kfc/
├── funds/                              # 펀드 도메인
│   ├── FundsApi.kt                    # PUBLIC: 공개 도메인 인터페이스
│   ├── model/                         # PUBLIC: 도메인 모델 (추후 확장)
│   │   ├── Holding.kt                 # (추후 생성)
│   │   ├── AssetAllocation.kt         # (추후 생성)
│   │   ├── SectorWeight.kt            # (추후 생성)
│   │   ├── FundProfile.kt             # (추후 생성)
│   │   └── ...
│   └── internal/                      # INTERNAL: 구현체 및 소스별 코드
│       ├── FundsApiImpl.kt             # 펀드 도메인 구현체
│       ├── krx/                       # KRX 소스별 구현
│       │   ├── KrxFundsApi.kt
│       │   ├── KrxFundsApiImpl.kt
│       │   ├── KrxHttpClient.kt
│       │   ├── KrxApiParams.kt
│       │   ├── KrxApiFields.kt
│       │   ├── HttpExtensions.kt
│       │   └── model/                 # KRX 소스별 모델
│       │       ├── EtfListItem.kt
│       │       ├── EtfDetailedInfo.kt
│       │       ├── EtfGeneralInfo.kt
│       │       ├── EtfOhlcv.kt
│       │       ├── EtfDailyPrice.kt
│       │       ├── EtfIntradayBar.kt
│       │       ├── EtfRecentDaily.kt
│       │       ├── EtfPriceChange.kt
│       │       ├── EtfTrackingMetrics.kt
│       │       ├── DivergenceRate.kt
│       │       ├── Direction.kt
│       │       ├── TrackingError.kt
│       │       ├── InvestorTrading.kt
│       │       ├── InvestorTradingByDate.kt
│       │       ├── PortfolioConstituent.kt
│       │       ├── PortfolioTopItem.kt
│       │       ├── ShortSelling.kt
│       │       ├── ShortBalance.kt
│       │       └── EtfAdditionalMetadata.kt
│       └── naver/                     # Naver 소스별 구현
│           ├── NaverFundsApi.kt
│           ├── NaverFundsApiImpl.kt
│           └── model/
│               └── NaverEtfOhlcv.kt
│
├── corp/                               # 기업 공시 도메인
│   ├── CorpApi.kt                     # PUBLIC: 공개 도메인 인터페이스
│   ├── model/                         # PUBLIC: 도메인 모델 (추후 확장)
│   │   ├── Dividend.kt                # (추후 생성)
│   │   ├── StockSplit.kt              # (추후 생성)
│   │   ├── CorporateActions.kt        # (추후 생성)
│   │   └── ...
│   └── internal/                      # INTERNAL: 구현체 및 소스별 코드
│       ├── CorpApiImpl.kt              # 기업 도메인 구현체
│       └── opendart/                  # OpenDart 소스별 구현
│           ├── OpenDartApi.kt
│           ├── OpenDartApiImpl.kt
│           └── model/
│               ├── CorpCode.kt
│               ├── DividendInfo.kt
│               ├── StockSplitInfo.kt
│               ├── DisclosureItem.kt
│               └── OpenDartResponse.kt
│
├── common/                             # 공용 유틸리티 (모든 도메인이 공유)
│   ├── exception/
│   │   ├── ErrorCode.kt
│   │   └── KfcException.kt
│   ├── ratelimit/
│   │   ├── RateLimitConfig.kt
│   │   ├── RateLimiter.kt
│   │   ├── RateLimitException.kt
│   │   └── TokenBucketRateLimiter.kt
│   ├── support/
│   │   └── serializer/
│   │       ├── BigDecimalSerializer.kt
│   │       └── LocalDateSerializer.kt
│   ├── util/
│   │   └── NormalizationExtensions.kt
│   └── internal/
│       ├── ResponseRecordingContext.kt
│       └── ResponseRecordingInterceptor.kt
│
├── KfcClient.kt                        # PUBLIC: 통합 클라이언트 (진입점)
└── model/
    └── FundType.kt                     # PUBLIC: 공용 모델
```

### 1.2 계층 구조 원칙

```
상위 계층 (공개 API)
├── KfcClient (통합 진입점)
├── FundsApi, CorpApi (도메인별 공개 인터페이스)
└── 공개 모델들 (FundType, 도메인 모델)
    ↓
하위 계층 (내부 구현)
├── FundsApiImpl, CorpApiImpl (도메인 구현체)
├── 소스별 API (KrxFundsApi, OpenDartApi, etc.)
└── 소스별 모델 (internal 패키지)
```

---

## 2. 파일 이동 상세 매핑

### 2.1 파일 이동 테이블 (41개 파일)

#### 공용 유틸리티 (9개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| exception/ | common/exception/ | ErrorCode.kt | INTERNAL | 패키지명 유지 |
| exception/ | common/exception/ | KfcException.kt | PUBLIC | 패키지명 유지 |
| internal/ratelimit/ | common/ratelimit/ | RateLimitConfig.kt | INTERNAL | |
| internal/ratelimit/ | common/ratelimit/ | RateLimiter.kt | INTERNAL | |
| internal/ratelimit/ | common/ratelimit/ | RateLimitException.kt | INTERNAL | |
| internal/ratelimit/ | common/ratelimit/ | TokenBucketRateLimiter.kt | INTERNAL | |
| support/serializer/ | common/support/serializer/ | BigDecimalSerializer.kt | INTERNAL | |
| support/serializer/ | common/support/serializer/ | LocalDateSerializer.kt | INTERNAL | |
| util/ | common/util/ | NormalizationExtensions.kt | INTERNAL | |

#### 내부 인프라 (2개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| internal/ | common/internal/ | ResponseRecordingContext.kt | INTERNAL | |
| internal/ | common/internal/ | ResponseRecordingInterceptor.kt | INTERNAL | |

#### Funds 도메인 - 도메인 인터페이스 (1개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| api/ | funds/ | FundsApi.kt | PUBLIC | 도메인 진입점 |

#### Funds 도메인 - 도메인 구현 (1개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| internal/ | funds/internal/ | FundsApiImpl.kt | INTERNAL | |

#### Funds 도메인 - KRX 소스 (26개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| api/krx/ | funds/internal/krx/ | KrxFundsApi.kt | INTERNAL | 소스별 인터페이스 |
| internal/krx/ | funds/internal/krx/ | KrxFundsApiImpl.kt | INTERNAL | |
| internal/krx/ | funds/internal/krx/ | KrxHttpClient.kt | INTERNAL | |
| internal/krx/ | funds/internal/krx/ | KrxApiParams.kt | INTERNAL | |
| internal/krx/ | funds/internal/krx/ | KrxApiFields.kt | INTERNAL | |
| internal/krx/ | funds/internal/krx/ | HttpExtensions.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | Direction.kt | INTERNAL | 소스별 모델 |
| model/krx/ | funds/internal/krx/model/ | DivergenceRate.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfAdditionalMetadata.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfDailyPrice.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfDetailedInfo.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfGeneralInfo.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfIntradayBar.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfListItem.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfOhlcv.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfPriceChange.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfRecentDaily.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | EtfTrackingMetrics.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | InvestorTrading.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | InvestorTradingByDate.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | PortfolioConstituent.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | PortfolioTopItem.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | ShortBalance.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | ShortSelling.kt | INTERNAL | |
| model/krx/ | funds/internal/krx/model/ | TrackingError.kt | INTERNAL | |

#### Funds 도메인 - Naver 소스 (3개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| api/naver/ | funds/internal/naver/ | NaverFundsApi.kt | INTERNAL | 소스별 인터페이스 |
| internal/naver/ | funds/internal/naver/ | NaverFundsApiImpl.kt | INTERNAL | |
| model/naver/ | funds/internal/naver/model/ | NaverEtfOhlcv.kt | INTERNAL | 소스별 모델 |

#### Corp 도메인 - 도메인 인터페이스 (1개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| api/ | corp/ | CorpApi.kt | PUBLIC | 도메인 진입점 |

#### Corp 도메인 - 도메인 구현 (1개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| internal/ | corp/internal/ | CorpApiImpl.kt | INTERNAL | |

#### Corp 도메인 - OpenDart 소스 (7개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| api/opendart/ | corp/internal/opendart/ | OpenDartApi.kt | INTERNAL | 소스별 인터페이스 |
| internal/opendart/ | corp/internal/opendart/ | OpenDartApiImpl.kt | INTERNAL | |
| model/opendart/ | corp/internal/opendart/model/ | CorpCode.kt | INTERNAL | 소스별 모델 |
| model/opendart/ | corp/internal/opendart/model/ | DividendInfo.kt | INTERNAL | |
| model/opendart/ | corp/internal/opendart/model/ | DisclosureItem.kt | INTERNAL | |
| model/opendart/ | corp/internal/opendart/model/ | OpenDartResponse.kt | INTERNAL | |
| model/opendart/ | corp/internal/opendart/model/ | StockSplitInfo.kt | INTERNAL | |

#### 최상위 파일 (2개)

| 현재 경로 | 새 경로 | 파일명 | 공개/내부 | 비고 |
|----------|--------|--------|---------|------|
| (root) | (root) | KfcClient.kt | PUBLIC | 이동 없음, import만 수정 |
| model/ | model/ | FundType.kt | PUBLIC | 이동 없음 |

**합계**: 41개 파일 이동

---

## 3. 의존성 분석

### 3.1 의존성 그래프

```
┌────────────────────────────────────────────────────────┐
│                 KfcClient (공개 진입점)                   │
└────────────────┬────────────────────────┬────────────────┘
                 │                        │
        ┌────────▼────────┐      ┌────────▼────────┐
        │   FundsApi      │      │    CorpApi      │
        │   (공개 인터페이스)  │      │  (공개 인터페이스)   │
        └────────┬────────┘      └────────┬────────┘
                 │                        │
        ┌────────▼────────┐      ┌────────▼────────┐
        │  FundsApiImpl    │      │  CorpApiImpl     │
        │   (도메인 구현)    │      │  (도메인 구현)     │
        └────────┬────────┘      └────────┬────────┘
                 │                        │
        ┌────────▼────────────┐  ┌────────▼──────────┐
        │ KrxFundsApi        │  │ OpenDartApi      │
        │ NaverFundsApi      │  │ (소스별 인터페이스) │
        │ (소스별 인터페이스)    │  └────────┬──────────┘
        └────────┬────────────┘           │
                 │                        │
        ┌────────▼────────────┐  ┌────────▼──────────┐
        │ KrxFundsApiImpl      │  │ OpenDartApiImpl   │
        │ NaverFundsApiImpl    │  │ (소스별 구현체)     │
        │ (소스별 구현체)        │  └────────┬──────────┘
        └────────┬────────────┘           │
                 │                   ┌────▼────────┐
        ┌────────▼──────────────┐   │ 소스별 모델들  │
        │  KRX/Naver 모델들      │   │(CorpCode 등) │
        │(EtfListItem 등)       │   └─────────────┘
        └───────────────────────┘

모든 계층: ──────────► 공용 유틸 (Exception, RateLimiter, Serializers)
```

### 3.2 이동 순서 (의존성 하향식)

**1단계: 기초 계층 - 공용 유틸리티 (9개 파일)**
- exception/ErrorCode.kt, KfcException.kt
- support/serializer/*.kt (2개)
- util/NormalizationExtensions.kt

**2단계: 인프라 계층 - Rate Limiting 및 응답 녹화 (6개 파일)**
- internal/ratelimit/*.kt (4개)
- internal/ResponseRecording*.kt (2개)

**3단계: 도메인 모델 계층 - Funds 모델 (21개 파일)**
- model/krx/*.kt (20개 모델)
- model/naver/*.kt (1개 모델)

**4단계: Funds 소스별 구현 계층 (8개 파일)**
- api/krx/KrxFundsApi.kt
- internal/krx/KrxFundsApiImpl.kt, KrxHttpClient.kt, KrxApiParams.kt, KrxApiFields.kt, HttpExtensions.kt
- api/naver/NaverFundsApi.kt
- internal/naver/NaverFundsApiImpl.kt

**5단계: Funds 도메인 계층 (2개 파일)**
- api/FundsApi.kt
- internal/FundsApiImpl.kt

**6단계: 도메인 모델 계층 - Corp 모델 (5개 파일)**
- model/opendart/*.kt (5개 모델)

**7단계: Corp 소스별 구현 계층 (2개 파일)**
- api/opendart/OpenDartApi.kt
- internal/opendart/OpenDartApiImpl.kt

**8단계: Corp 도메인 계층 (2개 파일)**
- api/CorpApi.kt
- internal/CorpApiImpl.kt

**9단계: 최상위 통합 클라이언트 (1개 파일)**
- KfcClient.kt (import만 수정, 이동 없음)

**총 41개 파일**, 9개 단계

### 3.3 각 단계별 의존성 주의사항

| 단계 | 파일 수 | 의존성 | 주의사항 |
|-----|--------|--------|---------|
| 1 | 3 | 없음 | 가장 낮은 계층, 안전 |
| 2 | 6 | 1단계 | 다른 곳에서 많이 참조 |
| 3 | 21 | 1, 2단계 | 모델끼리는 독립적 |
| 4 | 8 | 1, 2, 3단계 | 순서 있음 (Impl이 먼저) |
| 5 | 2 | 1, 2, 3, 4단계 | 마지막 단계 (전제 필수) |
| 6 | 5 | 1, 2단계 | Funds와 독립적 |
| 7 | 2 | 1, 2, 6단계 | Corp 파트 |
| 8 | 2 | 1, 2, 6, 7단계 | Corp 파트 완료 |
| 9 | 1 | 모든 단계 | 마지막 (모든 파트 완료 후) |

---

## 4. 가능한 문제점과 해결책

### 4.1 순환 참조

**현황**: 현재 코드 설계상 순환 참조 없음 ✓
- KRX/Naver 모델들이 FundsApi를 참조하지 않음
- FundsApiImpl이 KrxFundsApi/NaverFundsApi를 주입받음 (의존성 주입)
- 도메인 구현이 소스별 구현을 의존 (한 방향)

**검증 방법**: 이동 후 `./gradlew clean build` 실행하여 자동 감지

### 4.2 패키지 가시성 규칙

**Kotlin의 internal 패키지 규칙**:
```kotlin
// internal 패키지의 클래스는 같은 모듈 내에서만 접근 가능
internal class MyClass  // 같은 모듈 내에서만 사용 가능
public class MyClass    // 모든 곳에서 사용 가능
```

**KFC 프로젝트의 적용**:
- `dev.kairoscode.kfc.funds.internal.*`: FundsApiImpl, KrxFundsApi, EtfListItem 등 → 라이브러리 내부용
- `dev.kairoscode.kfc.funds.FundsApi`: 공개 API → 라이브러리 사용자가 사용

### 4.3 테스트 파일 영향도

**현재 테스트 구조**:
```
src/test/kotlin/dev/kairoscode/kfc/
├── api/funds/
├── api/corp/
├── mock/
└── utils/
```

**변경 전략**:
- 테스트 구조는 소스 구조를 **따르지만** 완벽히 대응시킬 필요 없음
- `src/test/kotlin/dev/kairoscode/kfc/funds/` 형태로 유지 가능 (내부 테스트)
- Mock 객체들은 공개 API 기준이므로 그대로 유지

**수정 필요 항목**:
- 각 테스트 파일의 import 경로 수정
- src/test의 utils는 공유되므로 중복 없음

### 4.4 공개 API 경계선

**공개되어야 할 클래스들**:
```kotlin
// 라이브러리 사용자가 직접 사용
dev.kairoscode.kfc.KfcClient
dev.kairoscode.kfc.funds.FundsApi
dev.kairoscode.kfc.corp.CorpApi
dev.kairoscode.kfc.model.FundType
dev.kairoscode.kfc.exception.KfcException
dev.kairoscode.kfc.funds.internal.krx.model.EtfListItem  // 반환 타입이므로 공개
dev.kairoscode.kfc.corp.internal.opendart.model.CorpCode // 반환 타입이므로 공개
```

**숨겨져야 할 클래스들**:
```kotlin
// internal 패키지에 있어야 함
dev.kairoscode.kfc.funds.internal.FundsApiImpl
dev.kairoscode.kfc.corp.internal.CorpApiImpl
dev.kairoscode.kfc.funds.internal.krx.KrxFundsApi
dev.kairoscode.kfc.common.ratelimit.TokenBucketRateLimiter
```

---

## 5. 검증 체크리스트

### 5.1 구조 검증 (16개 체크포인트)

- [ ] `src/main/kotlin/dev/kairoscode/kfc/funds/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/funds/internal/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/funds/internal/krx/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/funds/internal/krx/model/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/funds/internal/naver/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/funds/internal/naver/model/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/corp/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/corp/internal/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/corp/internal/opendart/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/corp/internal/opendart/model/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/common/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/common/exception/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/common/ratelimit/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/common/support/serializer/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/common/util/` 디렉토리 생성
- [ ] `src/main/kotlin/dev/kairoscode/kfc/common/internal/` 디렉토리 생성

### 5.2 패키지 선언 검증 (41개 파일)

**공용 유틸 (3개)**:
- [ ] `common/exception/KfcException.kt`: `package dev.kairoscode.kfc.exception`
- [ ] `common/exception/ErrorCode.kt`: `package dev.kairoscode.kfc.exception`
- [ ] `common/util/NormalizationExtensions.kt`: `package dev.kairoscode.kfc.util`

**Rate Limiting (4개)**:
- [ ] `common/ratelimit/RateLimitConfig.kt`: `package dev.kairoscode.kfc.internal.ratelimit`
- [ ] `common/ratelimit/RateLimiter.kt`: `package dev.kairoscode.kfc.internal.ratelimit`
- [ ] `common/ratelimit/RateLimitException.kt`: `package dev.kairoscode.kfc.internal.ratelimit`
- [ ] `common/ratelimit/TokenBucketRateLimiter.kt`: `package dev.kairoscode.kfc.internal.ratelimit`

**Serializers (2개)**:
- [ ] `common/support/serializer/BigDecimalSerializer.kt`: `package dev.kairoscode.kfc.support.serializer`
- [ ] `common/support/serializer/LocalDateSerializer.kt`: `package dev.kairoscode.kfc.support.serializer`

**응답 녹화 (2개)**:
- [ ] `common/internal/ResponseRecordingContext.kt`: `package dev.kairoscode.kfc.internal`
- [ ] `common/internal/ResponseRecordingInterceptor.kt`: `package dev.kairoscode.kfc.internal`

**Funds 도메인 (29개)**:
- [ ] `funds/FundsApi.kt`: `package dev.kairoscode.kfc.funds`
- [ ] `funds/internal/FundsApiImpl.kt`: `package dev.kairoscode.kfc.funds.internal`
- [ ] `funds/internal/krx/KrxFundsApi.kt`: `package dev.kairoscode.kfc.funds.internal.krx`
- [ ] `funds/internal/krx/KrxFundsApiImpl.kt`: `package dev.kairoscode.kfc.funds.internal.krx`
- [ ] `funds/internal/krx/KrxHttpClient.kt`: `package dev.kairoscode.kfc.funds.internal.krx`
- [ ] `funds/internal/krx/KrxApiParams.kt`: `package dev.kairoscode.kfc.funds.internal.krx`
- [ ] `funds/internal/krx/KrxApiFields.kt`: `package dev.kairoscode.kfc.funds.internal.krx`
- [ ] `funds/internal/krx/HttpExtensions.kt`: `package dev.kairoscode.kfc.funds.internal.krx`
- [ ] `funds/internal/krx/model/*.kt` (20개): `package dev.kairoscode.kfc.funds.internal.krx.model`
- [ ] `funds/internal/naver/NaverFundsApi.kt`: `package dev.kairoscode.kfc.funds.internal.naver`
- [ ] `funds/internal/naver/NaverFundsApiImpl.kt`: `package dev.kairoscode.kfc.funds.internal.naver`
- [ ] `funds/internal/naver/model/NaverEtfOhlcv.kt`: `package dev.kairoscode.kfc.funds.internal.naver.model`

**Corp 도메인 (8개)**:
- [ ] `corp/CorpApi.kt`: `package dev.kairoscode.kfc.corp`
- [ ] `corp/internal/CorpApiImpl.kt`: `package dev.kairoscode.kfc.corp.internal`
- [ ] `corp/internal/opendart/OpenDartApi.kt`: `package dev.kairoscode.kfc.corp.internal.opendart`
- [ ] `corp/internal/opendart/OpenDartApiImpl.kt`: `package dev.kairoscode.kfc.corp.internal.opendart`
- [ ] `corp/internal/opendart/model/*.kt` (5개): `package dev.kairoscode.kfc.corp.internal.opendart.model`

**최상위 (2개)**:
- [ ] `KfcClient.kt`: `package dev.kairoscode.kfc` (이동 없음)
- [ ] `model/FundType.kt`: `package dev.kairoscode.kfc.model` (이동 없음)

### 5.3 컴파일 검증

- [ ] `./gradlew clean build` 성공
- [ ] 컴파일 에러 0개
- [ ] 경고 메시지 검토 및 해결

### 5.4 테스트 검증

- [ ] `./gradlew test` 성공 (모든 단위 테스트)
- [ ] `./gradlew integrationTest` 성공 (모든 통합 테스트)
- [ ] 테스트 import 경로 모두 수정 완료

### 5.5 공개 API 유효성

**공개 API 클래스 확인**:
- [ ] `dev.kairoscode.kfc.KfcClient` 접근 가능
- [ ] `dev.kairoscode.kfc.funds.FundsApi` 접근 가능
- [ ] `dev.kairoscode.kfc.corp.CorpApi` 접근 가능
- [ ] `dev.kairoscode.kfc.model.FundType` 접근 가능

**반환 타입 클래스 확인**:
- [ ] `dev.kairoscode.kfc.funds.internal.krx.model.*` 접근 가능 (반환 타입)
- [ ] `dev.kairoscode.kfc.corp.internal.opendart.model.*` 접근 가능 (반환 타입)

**내부 클래스 미노출 확인**:
- [ ] `dev.kairoscode.kfc.funds.internal.FundsApiImpl` 접근 불가
- [ ] `dev.kairoscode.kfc.common.ratelimit.*` 접근 불가

---

## 6. 특수 고려사항

### 6.1 Package 선언 규칙

**원칙**: 파일의 `package` 선언과 디렉토리 경로의 정확한 일치

```kotlin
// 파일: src/main/kotlin/dev/kairoscode/kfc/funds/FundsApi.kt
package dev.kairoscode.kfc.funds  // ✓ 일치

// 파일: src/main/kotlin/dev/kairoscode/kfc/funds/internal/krx/model/EtfListItem.kt
package dev.kairoscode.kfc.funds.internal.krx.model  // ✓ 일치
```

### 6.2 Exception 패키지 특수성

현재 구조를 유지하기 위해:
- **물리적 위치**: `src/main/kotlin/dev/kairoscode/kfc/common/exception/`
- **패키지 선언**: `package dev.kairoscode.kfc.exception` (common 제거)

**이유**: 기존 사용자 코드가 `dev.kairoscode.kfc.exception.KfcException`를 import하고 있음

### 6.3 Rate Limiter 패키지 특수성

현재 구조를 유지하기 위해:
- **물리적 위치**: `src/main/kotlin/dev/kairoscode/kfc/common/ratelimit/`
- **패키지 선언**: `package dev.kairoscode.kfc.internal.ratelimit` (common 제거)

**이유**: 기존 KfcClient가 `dev.kairoscode.kfc.internal.ratelimit.RateLimitingSettings`를 참조

### 6.4 Serializer 패키지 특수성

현재 구조를 유지하기 위해:
- **물리적 위치**: `src/main/kotlin/dev/kairoscode/kfc/common/support/serializer/`
- **패키지 선언**: `package dev.kairoscode.kfc.support.serializer` (common 제거)

---

## 7. Next Steps

### Step 2 (구현)
1. 디렉토리 구조 생성
2. 파일 이동 및 패키지 선언 수정
3. Import 경로 업데이트
4. 컴파일 및 테스트 검증

### Step 3 (문서 & 정리)
1. README.md 업데이트
2. 아키텍처 다이어그램 추가
3. 마이그레이션 가이드 작성

---

## 결론

이 계획은 41개 파일을 9개 단계로 나누어 도메인 우선 구조로 완전히 재조직합니다.

**핵심 특징**:
- ✓ 순환 참조 없음 (이미 잘 설계된 아키텍처)
- ✓ 모든 소스별 구현은 `internal` 패키지에 숨김
- ✓ 도메인별 공개 인터페이스만 노출 (FundsApi, CorpApi)
- ✓ 네임스페이스 표준 문서와 100% 부합
- ✓ 기존 사용자 코드 호환성 유지 (공개 API 불변)

**대규모 변화**:
- 현재: `api/`, `internal/`, `model/` (소스별 구조)
- 목표: `funds/`, `corp/`, `common/` (도메인별 구조)

