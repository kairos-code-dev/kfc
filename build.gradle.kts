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

tasks.test {
    useJUnitPlatform {
        // excludeIntegration 프로퍼티가 true일 때만 integration 태그 제외
        if (project.hasProperty("excludeIntegration") &&
            project.property("excludeIntegration").toString().toBoolean()) {
            excludeTags("integration")
            println("Running unit tests only (integration tests excluded)")
        } else {
            println("Running all tests (including integration tests)")
        }
    }

    // Pass API key from local.properties to test JVM
    localProperties.getProperty("OPENDART_API_KEY")?.let { apiKey ->
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

    // 타임아웃 설정 (Integration Test 포함 시 오래 걸릴 수 있음)
    timeout.set(Duration.ofMinutes(30))

    // 병렬 실행 제한 (Rate Limiting 준수)
    maxParallelForks = 1

    // 항상 테스트 실행 (캐시 무시)
    outputs.upToDateWhen { false }
}
