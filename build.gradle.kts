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
// Integration Test Source Set 설정
// ============================================

sourceSets {
    // Integration Test용 소스 디렉토리 (recorded API responses)
    create("integrationTest") {
        kotlin {
            srcDir("src/integrationTest/kotlin")
        }
        resources {
            srcDir("src/integrationTest/resources")
        }
        // Main 출력만 포함 - test와 완전 분리
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

// Integration Test용 Configuration
// Complete separation: integrationTest has its own dependencies
val integrationTestImplementation by configurations.getting {
    // Main 의존성을 포함하되, test output은 포함하지 않음
    extendsFrom(configurations.implementation.get())
}

dependencies {
    // integrationTest는 test 의존성 추가 (test output 제외)
    integrationTestImplementation(libs.junit.jupiter)
    integrationTestImplementation(libs.assertj.core)
    integrationTestImplementation(libs.kotlinx.coroutines.test)
    integrationTestImplementation("com.google.code.gson:gson:2.11.0")
}


// ============================================
// Resource Processing Tasks
// ============================================
// Note: 완전 분리되었으나, Gradle's resource processing에서 duplicate 감지
// (integrationTest와 test가 별도의 출력 디렉토리를 사용하므로 실제 충돌은 없음)
tasks.named("processIntegrationTestResources", Copy::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// ============================================
// Test Tasks 설정
// ============================================

// Unit Test Task 설정 (integration 태그 제외)
tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
    // Pass API key from local.properties to test JVM
    localProperties.getProperty("OPENDART_API_KEY")?.let { apiKey ->
        environment("OPENDART_API_KEY", apiKey)
    }
    // 항상 테스트 실행 (캐시 무시)
    outputs.upToDateWhen { false }
}

// Integration Test Task 생성
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests (actual API calls with recording)"
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform {
        includeTags("integration")
    }

    // 레코딩 활성화 플래그 전달 (기본값: true - IntelliJ에서도 기본 활성화)
    systemProperty("record.responses",
        if (project.hasProperty("record.responses")) {
            project.property("record.responses").toString()
        } else {
            "true"  // IntelliJ Gradle 실행 시에도 레코딩 기본 활성화
        }
    )

    // API key 전달
    localProperties.getProperty("OPENDART_API_KEY")?.let { apiKey ->
        environment("OPENDART_API_KEY", apiKey)
    }

    // 타임아웃 설정 (Integration Test는 오래 걸림)
    timeout.set(Duration.ofMinutes(30))

    // 병렬 실행 비활성화 (Rate Limiting 준수)
    maxParallelForks = 1

    shouldRunAfter(tasks.test)

    // 항상 테스트 실행 (캐시 무시)
    outputs.upToDateWhen { false }

    // responses 폴더 삭제 후 테스트 시작 (자동 레코딩)
    dependsOn(cleanIntegrationTest)
}

// cleanIntegrationTest Task 생성 (레코딩 데이터 삭제)
val cleanIntegrationTest = tasks.register<Delete>("cleanIntegrationTest") {
    description = "Deletes all recorded API responses for fresh recording"
    group = "verification"

    delete("src/integrationTest/resources/responses")

    // 삭제 후 디렉토리 재생성
    doLast {
        val responsesDir = file("src/integrationTest/resources/responses")
        responsesDir.mkdirs()
        println("\n✅ Cleaned responses directory: ${responsesDir.absolutePath}")
        println("   레코딩이 초기화되었습니다. integrationTest 실행 시 새로운 응답을 기록합니다.\n")
    }
}
