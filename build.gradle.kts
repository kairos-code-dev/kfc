import java.util.Properties
import java.time.Duration

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
    testImplementation(libs.ktor.client.mock)
    testImplementation("com.google.code.gson:gson:2.11.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}


// ============================================
// Test Tasks 설정
// ============================================

/**
 * 공통 테스트 설정을 적용하는 확장 함수
 */
fun Test.configureCommonTestSettings() {
    // OPENDART API Key 설정 (우선순위: 환경변수 > local.properties)
    val opendartApiKey = System.getenv("OPENDART_API_KEY")
        ?: localProperties.getProperty("OPENDART_API_KEY")

    opendartApiKey?.let { apiKey ->
        environment("OPENDART_API_KEY", apiKey)
    }

    // Integration test용 레코딩 플래그 (기본값: false)
    systemProperty("record.responses",
        if (project.hasProperty("record.responses")) {
            project.property("record.responses").toString()
        } else {
            "false"
        }
    )

    // 타임아웃 설정
    timeout.set(Duration.ofMinutes(30))

    // 항상 테스트 실행 (캐시 무시)
    outputs.upToDateWhen { false }

    // 테스트 로깅
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

/**
 * test - 전체 테스트 실행 (unit + integration)
 *
 * 사용법:
 *   ./gradlew test                          # 전체 테스트
 *   ./gradlew test -Precord.responses=true  # 레코딩 활성화
 *
 * 주의: Integration 테스트가 포함되어 있으므로 순차 실행됩니다.
 * Unit 테스트만 병렬로 실행하려면 ./gradlew unitTest를 사용하세요.
 */
tasks.test {
    description = "Run all tests (unit + integration)"
    group = "verification"

    useJUnitPlatform()
    configureCommonTestSettings()

    // Integration 테스트 포함 시 순차 실행 (KRX API Rate Limiting)
    maxParallelForks = 1

    doFirst {
        println("🧪 Running all tests (unit + integration)")
        println("   Mode: Sequential (integration tests require rate limiting)")
    }
}

/**
 * unitTest - Unit 테스트만 실행
 *
 * 사용법:
 *   ./gradlew unitTest
 *
 * 특징:
 *   - @Tag("unit") 태그가 있는 테스트만 실행
 *   - 외부 API 호출 없이 빠르게 실행
 *   - 완전한 병렬 실행 가능
 */
val unitTest by tasks.registering(Test::class) {
    description = "Run unit tests only"
    group = "verification"

    useJUnitPlatform {
        includeTags("unit")
    }
    configureCommonTestSettings()

    // Unit 테스트는 완전 병렬 실행 가능
    maxParallelForks = Runtime.getRuntime().availableProcessors().coerceIn(1, 8)

    // JUnit 5 병렬 실행 활성화
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")

    doFirst {
        println("🔬 Running unit tests only")
        println("   Parallel forks: $maxParallelForks")
    }
}

/**
 * integrationTest - Integration 테스트만 실행
 *
 * 사용법:
 *   ./gradlew integrationTest                          # 통합 테스트 실행
 *   ./gradlew integrationTest -Precord.responses=true  # 레코딩 활성화fdg
 *
 * 특징:
 *   - @Tag("integration") 태그가 있는 테스트만 실행
 *   - 실제 외부 API (KRX, OPENDART) 호출
 *   - 순차 실행 (KRX API가 동시 요청 차단)
 */
val integrationTest by tasks.registering(Test::class) {
    description = "Run integration tests only"
    group = "verification"

    useJUnitPlatform {
        includeTags("integration")
    }
    configureCommonTestSettings()

    // Integration 테스트는 순차 실행 필수
    // 이유: GlobalRateLimiters는 JVM 프로세스별로 독립적이므로
    //       maxParallelForks > 1이면 각 fork마다 별도 RateLimiter 생성
    //       → 총 RPS = forks × limitPerProcess (KRX 25 RPS 제한 초과)
    maxParallelForks = 1

    doFirst {
        println("🌐 Running integration tests only")
        println("   Mode: Sequential (KRX API rate limiting)")
    }
}
