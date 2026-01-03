package com.example.diaryapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.diaryapp.data.database.converters.Converters
import java.time.LocalDateTime

/**
 * 日记实体类
 */
@Entity(
    tableName = "diary_entries",
    foreignKeys = [
        ForeignKey(
            entity = Theme::class,
            parentColumns = ["id"],
            childColumns = ["themeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["themeId"]), Index(value = ["createdAt"])]
)
@TypeConverters(Converters::class)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 标题
    val title: String,

    // 内容（HTML格式，支持富文本）
    val content: String,

    // 纯文本内容（用于搜索）
    val plainContent: String,

    // 创建时间
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 更新时间
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 主题ID（可为空）
    val themeId: Long? = null,

    // 是否在每周总结中展示
    val isWeeklyHighlight: Boolean = false,

    // 天气（可选）
    val weather: String? = null,

    // 心情（1-5分）
    val mood: Int? = null,

    // 图片路径列表
    val imagePaths: List<String> = emptyList(),

    // 视频路径列表
    val videoPaths: List<String> = emptyList(),

    // 自动生成的标签
    val autoTags: List<String> = emptyList(),

    // 手动添加的标签
    val manualTags: List<String> = emptyList()
)
