package com.example.diaryapp.data.database.dao

import androidx.room.*
import com.example.diaryapp.data.database.entities.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * 日记数据访问对象
 */
@Dao
interface DiaryDao {
    /**
     * 插入一条日记，返回插入后的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: DiaryEntry): Long

    /**
     * 更新日记
     */
    @Update
    suspend fun update(diary: DiaryEntry)

    /**
     * 删除日记
     */
    @Delete
    suspend fun delete(diary: DiaryEntry)

    /**
     * 根据ID删除日记
     */
    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取所有日记，按创建时间倒序
     */
    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun getAllDiaries(): Flow<List<DiaryEntry>>

    /**
     * 根据ID获取日记
     */
    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getDiaryById(id: Long): DiaryEntry?

    /**
     * 根据ID获取日记（Flow）
     */
    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun getDiaryByIdFlow(id: Long): Flow<DiaryEntry?>

    /**
     * 搜索日记（标题或内容包含关键词）
     */
    @Query("""
        SELECT * FROM diary_entries
        WHERE title LIKE '%' || :query || '%'
        OR plainContent LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchDiaries(query: String): Flow<List<DiaryEntry>>

    /**
     * 根据主题ID获取日记
     */
    @Query("SELECT * FROM diary_entries WHERE themeId = :themeId ORDER BY createdAt DESC")
    fun getDiariesByTheme(themeId: Long): Flow<List<DiaryEntry>>

    /**
     * 根据标签获取日记
     */
    @Query("""
        SELECT * FROM diary_entries
        WHERE :tag IN autoTags
        OR :tag IN manualTags
        ORDER BY createdAt DESC
    """)
    fun getDiariesByTag(tag: String): Flow<List<DiaryEntry>>

    /**
     * 获取指定日期范围的日记
     */
    @Query("""
        SELECT * FROM diary_entries
        WHERE createdAt >= :startDate
        AND createdAt <= :endDate
        ORDER BY createdAt DESC
    """)
    fun getDiariesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<DiaryEntry>>

    /**
     * 获取本周的日记
     */
    @Query("""
        SELECT * FROM diary_entries
        WHERE createdAt >= :weekStart
        AND createdAt <= :weekEnd
        ORDER BY createdAt DESC
    """)
    fun getThisWeekDiaries(
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): Flow<List<DiaryEntry>>

    /**
     * 获取设置为每周精选的日记
     */
    @Query("SELECT * FROM diary_entries WHERE isWeeklyHighlight = 1 ORDER BY createdAt DESC")
    fun getWeeklyHighlights(): Flow<List<DiaryEntry>>

    /**
     * 更新日记的每周精选状态
     */
    @Query("UPDATE diary_entries SET isWeeklyHighlight = :isHighlight WHERE id = :id")
    suspend fun updateWeeklyHighlight(id: Long, isHighlight: Boolean)

    /**
     * 获取最近的日记（限制数量）
     */
    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentDiaries(limit: Int): Flow<List<DiaryEntry>>

    /**
     * 获取所有日记数量
     */
    @Query("SELECT COUNT(*) FROM diary_entries")
    fun getDiaryCount(): Flow<Int>

    /**
     * 根据心情获取日记
     */
    @Query("SELECT * FROM diary_entries WHERE mood = :mood ORDER BY createdAt DESC")
    fun getDiariesByMood(mood: Int): Flow<List<DiaryEntry>>
}
