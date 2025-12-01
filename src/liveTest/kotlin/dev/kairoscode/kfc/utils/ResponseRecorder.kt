package dev.kairoscode.kfc.utils

import java.nio.file.Files
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializer
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * API 응답을 JSON 파일로 저장하는 유틸리티
 *
 * Live Test 실행 중 실제 API 응답을 레코딩하여
 * Unit Test에서 Mock 데이터로 사용할 수 있도록 합니다.
 *
 * ## 사용 예제
 * ```kotlin
 * val etfList = client.etf.getList()
 * ResponseRecorder.recordList(
 *     data = etfList,
 *     category = RecordingConfig.Paths.Etf.LIST,
 *     fileName = "etf_list_all"
 * )
 * ```
 */
object ResponseRecorder {
    const val MAX_RECORD_SIZE = 10_000  // 최대 10,000개만 레코딩

    @PublishedApi
    internal val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(BigDecimal::class.java, JsonSerializer<BigDecimal> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.toPlainString())
        })
        .create()

    /**
     * 객체를 JSON 파일로 저장
     * @param data 저장할 데이터
     * @param category API 카테고리 (RecordingConfig.Paths 사용)
     * @param fileName 파일명 (확장자 제외)
     */
    inline fun <reified T> record(data: T, category: String, fileName: String) {
        if (!RecordingConfig.isRecordingEnabled) return

        val outputDir = RecordingConfig.baseOutputPath.resolve(category)
        Files.createDirectories(outputDir)

        val outputFile = outputDir.resolve("$fileName.json")
        val jsonString = gson.toJson(data)
        Files.writeString(outputFile, jsonString)

        println("✅ Recorded: $outputFile")
    }

    /**
     * 리스트 데이터를 JSON 파일로 저장
     * 데이터가 MAX_RECORD_SIZE를 초과하면 처음 MAX_RECORD_SIZE개만 레코딩
     */
    inline fun <reified T> recordList(data: List<T>, category: String, fileName: String) {
        if (!RecordingConfig.isRecordingEnabled) return

        if (data.isEmpty()) {
            println("⚠️ 경고: $category/$fileName 에 레코딩할 데이터가 없습니다.")
            return
        }

        val recordData = if (data.size > MAX_RECORD_SIZE) {
            println("⚠️ 데이터가 너무 큽니다 (${data.size}개). 처음 $MAX_RECORD_SIZE 개만 레코딩합니다.")
            data.take(MAX_RECORD_SIZE)
        } else {
            data
        }

        record(recordData, category, fileName)
    }
}
