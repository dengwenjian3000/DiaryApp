package com.example.diaryapp.service

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 文件存储服务
 * 负责将内容URI复制到应用私有存储，确保文件持久化
 */
class FileStorageService(private val context: Context) {

    companion object {
        private const val IMAGES_DIR = "diary_images"
        private const val VIDEOS_DIR = "diary_videos"
        private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    }

    private val imagesDir: File by lazy {
        File(context.filesDir, IMAGES_DIR).apply { mkdirs() }
    }

    private val videosDir: File by lazy {
        File(context.filesDir, VIDEOS_DIR).apply { mkdirs() }
    }

    /**
     * 保存图片URI到本地存储
     * @return 本地文件路径
     */
    fun saveImage(uri: Uri): String {
        val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT)
        val extension = getFileExtension(context, uri)
        val fileName = "img_${timestamp}_${System.currentTimeMillis()}.$extension"
        val destFile = File(imagesDir, fileName)

        copyUriToFile(context, uri, destFile)

        return destFile.absolutePath
    }

    /**
     * 保存视频URI到本地存储
     * @return 本地文件路径
     */
    fun saveVideo(uri: Uri): String {
        val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT)
        val extension = getFileExtension(context, uri)
        val fileName = "video_${timestamp}_${System.currentTimeMillis()}.$extension"
        val destFile = File(videosDir, fileName)

        copyUriToFile(context, uri, destFile)

        return destFile.absolutePath
    }

    /**
     * 批量保存图片
     */
    fun saveImages(uris: List<Uri>): List<String> {
        return uris.mapNotNull { uri ->
            try {
                saveImage(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 批量保存视频
     */
    fun saveVideos(uris: List<Uri>): List<String> {
        return uris.mapNotNull { uri ->
            try {
                saveVideo(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 批量删除文件
     */
    fun deleteFiles(filePaths: List<String>) {
        filePaths.forEach { deleteFile(it) }
    }

    /**
     * 获取文件Uri（用于分享等）
     */
    fun getFileUri(filePath: String): Uri {
        val file = File(filePath)
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * 从URI复制文件到目标文件
     */
    private fun copyUriToFile(context: Context, uri: Uri, destFile: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("无法打开URI: $uri")
    }

    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            "video/3gpp" -> "3gp"
            else -> {
                // 尝试从URI获取扩展名
                val path = uri.path ?: return "jpg"
                val extension = path.substringAfterLast('.', "")
                if (extension.isNotEmpty()) extension else "jpg"
            }
        }
    }

    /**
     * 清理所有文件（慎用）
     */
    fun clearAllFiles() {
        imagesDir.deleteRecursively()
        videosDir.deleteRecursively()
        imagesDir.mkdirs()
        videosDir.mkdirs()
    }
}
