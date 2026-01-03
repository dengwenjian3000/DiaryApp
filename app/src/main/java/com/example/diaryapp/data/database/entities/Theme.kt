package com.example.diaryapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 主题实体类（用于分类日记，如"旅行"、"美食"、"工作"等）
 */
@Entity(tableName = "themes")
data class Theme(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 主题名称
    val name: String,

    // 主题颜色
    val color: String = "#D87C4A",

    // 主题图标
    val icon: String? = null,

    // 是否为系统预设
    val isSystemPreset: Boolean = false,

    // 创建时间
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    // 排序顺序
    val sortOrder: Int = 0
)
