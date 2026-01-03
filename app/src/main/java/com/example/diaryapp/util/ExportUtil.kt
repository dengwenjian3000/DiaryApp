package com.example.diaryapp.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import com.example.diaryapp.data.database.entities.DiaryEntry
import com.example.diaryapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 导出工具类
 * 用于导出日记数据为各种格式
 */
class ExportUtil(private val context: Context) {

    /**
     * 导出日记为JSON文件
     */
    suspend fun exportDiariesToJson(
        diaries: List<DiaryEntry>,
        fileName: String = "diary_export_${System.currentTimeMillis()}.json"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonString(diaries)
            val file = saveToFile(fileName, json)
            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出日记为文本文件
     */
    suspend fun exportDiariesToText(
        diaries: List<DiaryEntry>,
        fileName: String = "diary_export_${System.currentTimeMillis()}.txt"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val text = buildTextString(diaries)
            val file = saveToFile(fileName, text)
            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 生成分享海报图片
     */
    suspend fun generateSharePoster(
        diary: DiaryEntry
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val bitmap = createPosterBitmap(diary)
            val file = saveBitmapToFile(
                "diary_poster_${diary.id}_${System.currentTimeMillis()}.png",
                bitmap
            )
            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 构建JSON字符串
     */
    private fun buildJsonString(diaries: List<DiaryEntry>): String {
        val sb = StringBuilder()
        sb.append("{\"diaries\":[")
        diaries.forEachIndexed { index, diary ->
            if (index > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":${diary.id},")
            sb.append("\"title\":\"${escapeJson(diary.title)}\",")
            sb.append("\"content\":\"${escapeJson(diary.plainContent)}\",")
            sb.append("\"createdAt\":\"${diary.createdAt}\",")
            sb.append("\"updatedAt\":\"${diary.updatedAt}\",")
            sb.append("\"themeId\":${diary.themeId},")
            sb.append("\"mood\":${diary.mood},")
            sb.append("\"tags\":[${diary.autoTags.plus(diary.manualTags).joinToString(",") { "\"$it\"" }}]")
            sb.append("}")
        }
        sb.append("]}")
        return sb.toString()
    }

    /**
     * 构建文本字符串
     */
    private fun buildTextString(diaries: List<DiaryEntry>): String {
        val sb = StringBuilder()
        sb.append("=== 日记导出 ===\n")
        sb.append("导出时间：${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
        sb.append("共 ${diaries.size} 篇日记\n\n")

        diaries.forEach { diary ->
            sb.append("====================================\n")
            sb.append("标题：${diary.title}\n")
            sb.append("时间：${diary.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}\n")
            diary.mood?.let { sb.append("心情：${"★".repeat(it)}\n") }
            if (diary.autoTags.isNotEmpty() || diary.manualTags.isNotEmpty()) {
                sb.append("标签：${(diary.autoTags + diary.manualTags).joinToString("、")}\n")
            }
            sb.append("内容：\n${diary.plainContent}\n")
            sb.append("====================================\n\n")
        }

        return sb.toString()
    }

    /**
     * 创建海报图片
     */
    private fun createPosterBitmap(diary: DiaryEntry): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景色
        canvas.drawColor(PaperLight.toArgb())

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 绘制纸张质感背景
        val paperPaint = Paint().apply {
            color = PaperMedium.toArgb()
            isAntiAlias = true
        }
        val margin = 60f
        canvas.drawRoundRect(
            margin,
            margin,
            width - margin,
            height - margin,
            40f,
            40f,
            paperPaint
        )

        // 标题
        paint.apply {
            color = InkPrimary.toArgb()
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        var y = margin + 120f
        canvas.drawText(diary.title, margin * 2, y, paint)

        // 日期
        paint.apply {
            color = InkLight.toArgb()
            textSize = 36f
            typeface = Typeface.DEFAULT
        }
        y += 80f
        val dateText = diary.createdAt.format(DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm"))
        canvas.drawText(dateText, margin * 2, y, paint)

        // 心情
        diary.mood?.let {
            y += 80f
            val moodText = "心情：" + "★".repeat(it)
            paint.color = MoodColors[it - 1].toArgb()
            canvas.drawText(moodText, margin * 2, y, paint)
        }

        // 分隔线
        y += 80f
        paint.color = PaperDark.toArgb()
        canvas.drawLine(
            margin * 2,
            y,
            width - margin * 2,
            y,
            paint
        )

        // 内容
        y += 80f
        paint.apply {
            color = InkPrimary.toArgb()
            textSize = 42f
            typeface = Typeface.DEFAULT
        }
        val contentLines = breakTextIntoLines(diary.plainContent, paint, width - margin * 4)
        contentLines.take(15).forEach { line ->
            if (y < height - margin - 200) {
                canvas.drawText(line, margin * 2, y, paint)
                y += 70f
            }
        }

        // 底部水印
        paint.apply {
            color = InkLight.toArgb()
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("来自 日记本", width / 2f, height - margin, paint)

        return bitmap
    }

    /**
     * 将文本拆分成行
     */
    private fun breakTextIntoLines(
        text: String,
        paint: Paint,
        maxWidth: Float
    ): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split("")
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else currentLine + word
            val width = paint.measureText(testLine)

            if (width < maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    /**
     * 保存文本到文件
     */
    private fun saveToFile(fileName: String, content: String): File {
        val exportsDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        val file = File(exportsDir, fileName)
        FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }
        return file
    }

    /**
     * 保存位图到文件
     */
    private fun saveBitmapToFile(fileName: String, bitmap: Bitmap): File {
        val exportsDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        val file = File(exportsDir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    /**
     * 转义JSON字符
     */
    private fun escapeJson(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}

/**
 * 分享工具类
 */
class ShareUtil(private val context: Context) {

    /**
     * 分享文本
     */
    fun shareText(text: String, title: String = "分享日记") {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            putExtra(android.content.Intent.EXTRA_TITLE, title)
        }
        context.startActivity(android.content.Intent.createChooser(intent, title))
    }

    /**
     * 分享图片
     */
    fun shareImage(uri: Uri, title: String = "分享日记") {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, title))
    }

    /**
     * 分享文件
     */
    fun shareFile(uri: Uri, mimeType: String, title: String = "分享") {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, title))
    }
}
