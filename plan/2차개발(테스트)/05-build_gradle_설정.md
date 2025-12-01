# build.gradle.kts 테스트 설정

## 개요

테스트를 실행하기 위해 build.gradle.kts에 필요한 설정을 추가합니다.

## 1. 기존 dependencies 확인

현재 kfc 프로젝트의 `build.gradle.kts`에는 이미 테스트 의존성이 있습니다:

```kotlin
dependencies {
    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

## 2. 추가해야 할 설정

### 2.1 SourceSets 설정 (Live Test 전용)

Live Test를 별도 source set으로 분리합니다.

```kotlin
kotlin {
    // ... 기존 설정 유지 ...
}

// Live Test Source Set 추가
sourceSets {
    // Live Test용 소스 디렉토리
    create("liveTest") {
        kotlin {
            srcDir("src/liveTest/kotlin")
        }
        resources {
            srcDir("src/liveTest/resources")
        }
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

// Live Test용 Configuration
val liveTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    // 기존 dependencies...

    // Live Test 의존성 (test 의존성 상속)
    liveTestImplementation(sourceSets.test.get().output)
}
```

### 2.2 Live Test Task 생성

```kotlin
// Live Test Task 생성
val liveTest = tasks.register<Test>("liveTest") {
    description = "Runs the live tests (actual API calls with recording)"
    group = "verification"

    testClassesDirs = sourceSets["liveTest"].output.classesDirs
    classpath = sourceSets["liveTest"].runtimeClasspath

    useJUnitPlatform {
        includeTags("live")
    }

    // 레코딩 활성화 플래그 전달
    if (project.hasProperty("record.responses")) {
        systemProperty("record.responses", project.property("record.responses").toString())
    }

    // 타임아웃 설정 (Live Test는 오래 걸림)
    timeout.set(Duration.ofMinutes(30))

    // 병렬 실행 비활성화 (Rate Limiting 준수)
    maxParallelForks = 1

    shouldRunAfter(tasks.test)
}

// cleanLiveTest Task 생성 (레코딩 데이터 삭제)
val cleanLiveTest = tasks.register<Delete>("cleanLiveTest") {
    description = "Deletes all recorded API responses"
    group = "verification"

    delete("src/test/resources/responses")
}
```

### 2.3 기존 Test Task 수정

```kotlin
tasks.withType<Test> {
    useJUnitPlatform {
        // Unit Test만 실행 (live 태그 제외)
        excludeTags("live")
    }
}
```

## 3. 완전한 build.gradle.kts 예제

아래는 기존 설정에 테스트 설정을 추가한 전체 예제입니다:

```kotlin
import java.util.Properties

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
}

group = "dev.kairoscode"
version = "1.0.0-SNAPSHOT"

// Load local.properties if exists
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream -> localProperties.load(stream) }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)

    // Logging
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.logback.classic)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// ============================================
// Live Test 설정
// ============================================

// Live Test Source Set 추가
sourceSets {
    create("liveTest") {
        kotlin {
            srcDir("src/liveTest/kotlin")
        }
        resources {
            srcDir("src/liveTest/resources")
        }
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

// Live Test용 Configuration
val liveTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    liveTestImplementation(sourceSets.test.get().output)
}

// Unit Test Task 설정
tasks.withType<Test> {
    useJUnitPlatform {
        excludeTags("live")
    }
}

// Live Test Task 생성
val liveTest = tasks.register<Test>("liveTest") {
    description = "Runs the live tests (actual API calls with recording)"
    group = "verification"

    testClassesDirs = sourceSets["liveTest"].output.classesDirs
    classpath = sourceSets["liveTest"].runtimeClasspath

    useJUnitPlatform {
        includeTags("live")
    }

    // 레코딩 활성화 플래그
    if (project.hasProperty("record.responses")) {
        systemProperty("record.responses", project.property("record.responses").toString())
    }

    // 타임아웃 설정
    timeout.set(Duration.ofMinutes(30))

    // 병렬 실행 비활성화
    maxParallelForks = 1

    shouldRunAfter(tasks.test)
}

// cleanLiveTest Task 생성
val cleanLiveTest = tasks.register<Delete>("cleanLiveTest") {
    description = "Deletes all recorded API responses"
    group = "verification"

    delete("src/test/resources/responses")
}
```

## 4. Gradle 명령어

### Unit Test 실행
```bash
# 전체 Unit Test 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "EtfListApiSpec"

# 테스트 결과 리포트
open build/reports/tests/test/index.html
```

### Live Test 실행
```bash
# Live Test 실행 (레코딩 없음)
./gradlew liveTest

# Live Test 실행 + 응답 레코딩
./gradlew liveTest -Precord.responses=true
```

### 레코딩 데이터 관리
```bash
# 기존 레코딩 데이터 삭제
./gradlew cleanLiveTest

# 삭제 후 재레코딩
./gradlew cleanLiveTest liveTest -Precord.responses=true
```

### 전체 테스트 실행
```bash
# Unit Test + Live Test 순차 실행
./gradlew test liveTest

# 병렬 실행 (권장하지 않음 - Rate Limiting)
./gradlew test liveTest --parallel
```

## 5. IntelliJ IDEA 설정

### Live Test 실행 설정 생성

1. Run → Edit Configurations...
2. Add New Configuration → Gradle
3. 설정:
   - Name: `Live Test (with Recording)`
   - Gradle project: kfc
   - Tasks: `liveTest`
   - Arguments: `-Precord.responses=true`

### Unit Test 실행 설정

1. Run → Edit Configurations...
2. Add New Configuration → JUnit
3. 설정:
   - Name: `All Unit Tests`
   - Test kind: All in package
   - Package: `dev.kairoscode.kfc.api`
   - Search for tests: In whole project
   - Exclude tests with tag: `live`

## 6. .gitignore 업데이트

테스트 관련 파일 중 무시해야 할 파일들을 추가합니다:

```gitignore
# Local properties (API keys)
local.properties

# Test reports
build/reports/
build/test-results/

# IntelliJ IDEA
.idea/
*.iml
*.iws
*.ipr
out/

# Gradle
.gradle/
build/
```

## 7. 검증

모든 설정이 완료되었는지 확인합니다:

```bash
# 1. Unit Test 실행 확인
./gradlew test

# 2. Live Test Task 존재 확인
./gradlew tasks --group verification

# 출력:
# liveTest - Runs the live tests (actual API calls with recording)
# cleanLiveTest - Deletes all recorded API responses

# 3. Live Test 실행 확인 (레코딩 없이)
./gradlew liveTest

# 4. Live Test + 레코딩 실행 확인
./gradlew liveTest -Precord.responses=true
```

## 다음 단계

1. ✅ build.gradle.kts에 위 설정 추가
2. ✅ 테스트 헬퍼 클래스 작성 → `02-헬퍼_클래스_작성_가이드.md` 참고
3. ✅ 실제 테스트 작성 시작 → `03-EtfApi_테스트_계획.md`, `04-CorpApi_테스트_계획.md` 참고
4. ✅ 전체 체크리스트 확인 → `99-체크리스트.md` 참고
