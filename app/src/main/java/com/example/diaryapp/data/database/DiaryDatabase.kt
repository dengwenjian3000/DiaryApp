package com.example.diaryapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.diaryapp.data.database.converters.Converters
import com.example.diaryapp.data.database.dao.DiaryDao
import com.example.diaryapp.data.database.dao.ThemeDao
import com.example.diaryapp.data.database.entities.DiaryEntry
import com.example.diaryapp.data.database.entities.Theme

/**
 * 日记应用数据库
 */
@Database(
    entities = [DiaryEntry::class, Theme::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun themeDao(): ThemeDao

    companion object {
        private const val DATABASE_NAME = "diary_database"

        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        /**
         * 获取数据库实例（单例）
         */
        fun getInstance(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    DATABASE_NAME
                )
                    // 添加可选的迁移策略
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 关闭数据库连接
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }

    /**
     * 初始化系统预设主题
     */
    suspend fun initializeSystemThemes() {
        val themeDao = themeDao()

        // 检查是否已经初始化（通过插入第一个主题来检查）
        val existing = themeDao.getThemeById(1)
        if (existing != null) {
            return // 已经初始化过了
        }

        // 系统预设主题
        val systemThemes = listOf(
            Theme(
                name = "生活",
                color = "#D87C4A",
                icon = "life",
                isSystemPreset = true,
                sortOrder = 1
            ),
            Theme(
                name = "工作",
                color = "#7CB87C",
                icon = "work",
                isSystemPreset = true,
                sortOrder = 2
            ),
            Theme(
                name = "学习",
                color = "#6B9BD1",
                icon = "study",
                isSystemPreset = true,
                sortOrder = 3
            ),
            Theme(
                name = "旅行",
                color = "#E8B87D",
                icon = "travel",
                isSystemPreset = true,
                sortOrder = 4
            ),
            Theme(
                name = "美食",
                color = "#C75B5B",
                icon = "food",
                isSystemPreset = true,
                sortOrder = 5
            ),
            Theme(
                name = "运动",
                color = "#7CB87C",
                icon = "sports",
                isSystemPreset = true,
                sortOrder = 6
            ),
            Theme(
                name = "心情",
                color = "#B87C9E",
                icon = "mood",
                isSystemPreset = true,
                sortOrder = 7
            ),
            Theme(
                name = "感悟",
                color = "#9C7864",
                icon = "insight",
                isSystemPreset = true,
                sortOrder = 8
            ),
            Theme(
                name = "家庭",
                color = "#D4A574",
                icon = "family",
                isSystemPreset = true,
                sortOrder = 9
            ),
            Theme(
                name = "朋友",
                color = "#87CEEB",
                icon = "friends",
                isSystemPreset = true,
                sortOrder = 10
            )
        )

        systemThemes.forEach { theme ->
            themeDao.insert(theme)
        }
    }
}
