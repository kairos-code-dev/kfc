# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### Stock API
- 종목 리스트 조회 (시장별: KOSPI, KOSDAQ, KONEX)
- 종목 기본정보 조회 (종목명, ISIN, 상장주식수 등)
- 섹터 및 산업 분류 조회

#### Funds API (ETF/ETN/REIT)
- ETF/ETN/REIT 목록 조회
- 상세정보 조회 (NAV, 괴리율, CU수 등)
- 포트폴리오 구성 종목 조회
- PDF (Portfolio Deposit File) 조회
- 투자자별 거래 데이터 조회
- 공매도 거래 데이터 조회
- 일별 OHLCV 데이터 조회

#### Index API
- 지수 목록 조회 (코스피, 코스닥, 테마지수 등)
- 일별 OHLCV 데이터 조회
- 지수 밸류에이션 조회 (PER, PBR, 배당수익률)
- 지수 구성 종목 조회

#### Bond API
- 채권 수익률 조회 (국고채, 회사채, CD, CP 등)
- 일별 수익률 스냅샷 조회
- 만기별 수익률 조회 (1년, 3년, 5년, 10년, 20년, 30년)

#### Future API
- 선물 상품 목록 조회 (코스피200, 미니코스피200 등)
- 일별 OHLCV 데이터 조회

#### Financials API (OPENDART)
- 손익계산서 조회 (매출액, 영업이익, 당기순이익 등)
- 재무상태표 조회 (자산, 부채, 자본 등)
- 현금흐름표 조회 (영업, 투자, 재무활동 현금흐름)
- 전체 재무제표 일괄 조회
- OPENDART API Key 통합 지원

#### Rate Limiting
- JVM 전역 Rate Limiter 구현 (GlobalRateLimiters)
- KRX API 자동 Rate Limiting (25 RPS)
- OPENDART API 자동 Rate Limiting (50 RPS)
- Naver Finance API 자동 Rate Limiting (50 RPS)
- 커스텀 Rate Limit 설정 지원

#### Testing Infrastructure
- 통합 테스트 프레임워크 구축
- JUnit 5 Assumptions를 활용한 API 키 검증
- 거래일/비거래일 테스트 케이스
- 네임스페이스 기반 테스트 구조화
- MockK를 활용한 단위 테스트
- 고정 날짜를 사용한 재현 가능한 테스트

#### CI/CD
- GitHub Actions 워크플로우 설정
- 병렬 테스트 실행 비활성화 (KRX API Rate Limit 준수)
- 자동화된 빌드 및 테스트 파이프라인

### Changed

#### Architecture Refactoring
- 소스 기반 아키텍처에서 도메인 기반 아키텍처로 전환
- Clean Architecture 적용 (API/Domain/Infrastructure 레이어 분리)
- 네임스페이스 기반 구조로 재설계
- 통합 테스트 우선 접근법 적용
- Agentic Coding 원칙 적용

#### API Improvements
- ETF API를 Funds API로 확장 및 이름 변경
- EtfDetailedInfo 모델을 실제 KRX API 응답에 맞게 단순화
- 중복 리소스 처리 개선 (liveTest와 test 소스셋 간)
- 테스트 명명 규칙 개선 (API 문서처럼 읽히도록)

#### Documentation
- README 작성 및 개선
- API 명세 문서 정리 (BLD 코드 수정, 테스트 섹션 제거)
- KFC 약어 의미 업데이트
- 프로젝트 설정 파일 및 문서화

### Fixed
- KRX API 파라미터 수정 (Bond 및 Index API)
- 병렬 실행 비활성화로 통합 테스트 Rate Limiting 이슈 해결
- IDE 테스트 디스커버리 수정
- 중복 리소스 핸들링 해결

### Security
- API 키 보안 처리 (.gitignore에 local.properties 추가)
- .idea/workspace.xml을 git tracking에서 제외

### Infrastructure
- Kotlin 2.0+ 지원
- JDK 21 요구사항
- Ktor Client 통합
- Gradle Kotlin DSL 설정
- EditorConfig 설정

## [1.0.0] - YYYY-MM-DD

### Note
Initial release version - 출시 예정

[Unreleased]: https://github.com/username/kfc/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/username/kfc/releases/tag/v1.0.0
