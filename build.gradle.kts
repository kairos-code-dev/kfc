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
// Live Test Source Set 설정
// ============================================

sourceSets {
    // Live Test용 소스 디렉토리 (recorded API responses)
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

// ============================================
// Test Tasks 설정
// ============================================

// Unit Test Task 설정 (live 태그 제외)
tasks.test {
    useJUnitPlatform {
        excludeTags("live")
    }
    // Pass API key from local.properties to test JVM
    localProperties.getProperty("OPENDART_API_KEY")?.let { apiKey ->
        environment("OPENDART_API_KEY", apiKey)
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

    // 레코딩 활성화 플래그 전달 (기본값: true)
    systemProperty("record.responses",
        if (project.hasProperty("record.responses")) {
            project.property("record.responses").toString()
        } else {
            "true"
        }
    )

    // API key 전달
    localProperties.getProperty("OPENDART_API_KEY")?.let { apiKey ->
        environment("OPENDART_API_KEY", apiKey)
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

    delete("src/liveTest/resources/responses")
}
