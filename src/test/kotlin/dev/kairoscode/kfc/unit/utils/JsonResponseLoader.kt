package dev.kairoscode.kfc.unit.utils

import java.nio.file.Files
import java.nio.file.Paths

/**
 * JSON 응답 파일 로더
 *
 * Live Test에서 레코딩한 JSON 파일을 로드하여
 * Unit Test의 Mock 데이터로 사용합니다.
 *
 * ## 사용 예제
 * ```kotlin
 * val jsonData = JsonResponseLoader.load(
 *     category = "etf/list",
 *     fileName = "etf_list_all"
 * )
 * ```
 *
 * ## 파일 경로 규칙
 * - 기본 경로: `src/test/resources/responses/`
 * - 전체 경로: `src/test/resources/responses/{category}/{fileName}.json`
 */
object JsonResponseLoader {

    /**
     * JSON 응답 파일 로드
     *
     * @param category 응답 카테고리 (etf/list, corp/dividend 등)
     * @param fileName 파일명 (.json 확장자 제외)
     * @return JSON 문자열
     * @throws IllegalArgumentException 파일을 찾을 수 없을 때
     */
    fun load(category: String, fileName: String): String {
        val resourcePath = "responses/$category/$fileName.json"
        val resource = this::class.java.classLoader.getResource(resourcePath)
            ?: throw IllegalArgumentException(
                """
                테스트 리소스를 찾을 수 없습니다: $resourcePath

                Live Test를 먼저 실행하여 응답을 레코딩해야 합니다:
                ./gradlew liveTest -Precord.responses=true
                """.trimIndent()
            )

        return Files.readString(Paths.get(resource.toURI()))
    }

    /**
     * JSON 응답 파일이 존재하는지 확인
     *
     * @param category 응답 카테고리
     * @param fileName 파일명 (.json 확장자 제외)
     * @return 파일 존재 여부
     */
    fun exists(category: String, fileName: String): Boolean {
        val resourcePath = "responses/$category/$fileName.json"
        return this::class.java.classLoader.getResource(resourcePath) != null
    }

    /**
     * 카테고리 내의 모든 JSON 파일 목록 조회
     *
     * @param category 응답 카테고리
     * @return 파일명 목록 (.json 확장자 제외)
     */
    fun listFiles(category: String): List<String> {
        val resourcePath = "responses/$category"
        val resource = this::class.java.classLoader.getResource(resourcePath)
            ?: return emptyList()

        val directory = Paths.get(resource.toURI())
        return Files.list(directory)
            .filter { it.toString().endsWith(".json") }
            .map { it.fileName.toString().removeSuffix(".json") }
            .toList()
    }
}
