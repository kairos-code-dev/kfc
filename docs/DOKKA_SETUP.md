# Dokka API Documentation Setup

이 문서는 KFC 프로젝트의 Dokka API 문서 생성 및 배포 설정에 대한 가이드입니다.

## 로컬에서 API 문서 생성

### HTML 문서 생성

```bash
./gradlew dokkaHtml
```

생성된 문서는 `build/dokka/html/` 디렉토리에 저장됩니다.

브라우저로 확인:
```bash
open build/dokka/html/index.html  # macOS
xdg-open build/dokka/html/index.html  # Linux
start build/dokka/html/index.html  # Windows
```

### 다른 포맷으로 생성

```bash
# Javadoc 포맷
./gradlew dokkaJavadoc

# GitHub Flavored Markdown 포맷
./gradlew dokkaGfm

# Jekyll 포맷
./gradlew dokkaJekyll
```

## GitHub Pages 배포

### 자동 배포 (권장)

태그를 푸시하면 GitHub Actions가 자동으로 API 문서를 생성하고 GitHub Pages에 배포합니다.

```bash
# 새 버전 태그 생성 및 푸시
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

워크플로우 실행 후 다음 URL에서 문서를 확인할 수 있습니다:
```
https://kairos-code-dev.github.io/kfc/
```

### GitHub Pages 활성화

처음 배포하기 전에 GitHub Pages를 활성화해야 합니다:

1. GitHub 저장소 > Settings > Pages
2. Source: GitHub Actions 선택
3. 저장

### 수동 배포

필요한 경우 workflow를 수동으로 실행할 수 있습니다:

1. GitHub 저장소 > Actions > API Documentation
2. "Run workflow" 버튼 클릭
3. 브랜치 선택 후 실행

## 문서 작성 가이드

### KDoc 작성

```kotlin
/**
 * 주식 종목 정보를 조회합니다.
 *
 * @param ticker 종목 코드 (예: "005930" - 삼성전자)
 * @return 종목 정보 또는 null (종목이 없는 경우)
 * @throws IllegalArgumentException 종목 코드 형식이 잘못된 경우
 * @sample dev.kairoscode.kfc.samples.StockSamples.getStockInfoSample
 */
suspend fun getStockInfo(ticker: String): StockInfo?
```

### 샘플 코드 추가

`src/test/kotlin/samples/` 디렉토리에 샘플 코드를 작성하면 API 문서에 자동으로 포함됩니다.

```kotlin
package dev.kairoscode.kfc.samples

class StockSamples {
    suspend fun getStockInfoSample() {
        val kfc = KfcClient.create()
        val samsung = kfc.stock.getStockInfo("005930")
        println("종목명: ${samsung?.name}")
    }
}
```

### 패키지 문서 추가

각 패키지에 `package-info.md` 파일을 추가하여 패키지 레벨 문서를 작성할 수 있습니다.

```markdown
# Package dev.kairoscode.kfc.api

KFC API의 진입점을 제공하는 패키지입니다.

## 주요 클래스

- [KfcClient] - KFC API의 메인 클라이언트
```

## Dokka 설정 커스터마이징

`build.gradle.kts`의 `dokkaHtml` 블록에서 다양한 설정을 변경할 수 있습니다:

```kotlin
tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka/html"))

    dokkaSourceSets {
        configureEach {
            // 모듈 이름
            moduleName.set("KFC")

            // 외부 문서 링크 추가
            externalDocumentationLink {
                url.set(uri("https://kotlinlang.org/api/kotlinx.coroutines/").toURL())
            }

            // 특정 패키지 제외
            perPackageOption {
                matchingRegex.set(".*\\.internal.*")
                suppress.set(true)
            }

            // 가시성 설정
            documentedVisibilities.set(
                setOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC,
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PROTECTED
                )
            )
        }
    }
}
```

## CI/CD 워크플로우

`.github/workflows/docs.yml` 파일이 다음 작업을 자동화합니다:

1. **태그 푸시 감지**: `v*` 패턴의 태그가 푸시되면 워크플로우 실행
2. **문서 생성**: `./gradlew dokkaHtml` 실행
3. **GitHub Pages 배포**: 생성된 문서를 GitHub Pages에 업로드
4. **알림**: 배포 완료 시 커밋에 코멘트 추가

## 트러블슈팅

### 문서가 생성되지 않는 경우

1. Gradle 캐시 정리:
   ```bash
   ./gradlew clean dokkaHtml
   ```

2. Dokka 버전 확인:
   ```bash
   ./gradlew dependencies --configuration dokkaHtmlPlugin
   ```

### GitHub Pages 배포 실패

1. GitHub Pages 활성화 여부 확인
2. 워크플로우 권한 확인:
   - Settings > Actions > General > Workflow permissions
   - "Read and write permissions" 활성화

3. 워크플로우 로그 확인:
   - Actions 탭에서 실패한 워크플로우 클릭
   - 상세 로그 확인

### 외부 링크가 작동하지 않는 경우

일부 외부 문서는 `package-list` 다운로드에 실패할 수 있습니다. 이는 경고일 뿐이며 문서 생성에는 영향을 주지 않습니다.

## 참고 자료

- [Dokka 공식 문서](https://kotlinlang.org/docs/dokka-introduction.html)
- [GitHub Pages 문서](https://docs.github.com/en/pages)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [KDoc 스타일 가이드](https://kotlinlang.org/docs/kotlin-doc.html)
