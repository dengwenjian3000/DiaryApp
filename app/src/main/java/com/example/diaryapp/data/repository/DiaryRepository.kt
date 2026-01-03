package com.example.diaryapp.data.repository

import com.example.diaryapp.data.database.dao.DiaryDao
import com.example.diaryapp.data.database.dao.ThemeDao
import com.example.diaryapp.data.database.entities.DiaryEntry
import com.example.diaryapp.data.database.entities.Theme
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * 日记数据仓库
 * 封装数据访问逻辑，提供统一的数据接口
 */
class DiaryRepository(
    private val diaryDao: DiaryDao,
    private val themeDao: ThemeDao
) {
    // ==================== 日记操作 ====================

    /**
     * 创建新日记
     */
    suspend fun createDiary(diary: DiaryEntry): Long {
        return diaryDao.insert(diary)
    }

    /**
     * 更新日记
     */
    suspend fun updateDiary(diary: DiaryEntry) {
        diaryDao.update(diary.copy(updatedAt = LocalDateTime.now()))
    }

    /**
     * 删除日记
     */
    suspend fun deleteDiary(diary: DiaryEntry) {
        diaryDao.delete(diary)
    }

    /**
     * 根据ID删除日记
     */
    suspend fun deleteDiaryById(id: Long) {
        diaryDao.deleteById(id)
    }

    /**
     * 获取所有日记
     */
    fun getAllDiaries(): Flow<List<DiaryEntry>> {
        return diaryDao.getAllDiaries()
    }

    /**
     * 根据ID获取日记
     */
    suspend fun getDiaryById(id: Long): DiaryEntry? {
        return diaryDao.getDiaryById(id)
    }

    /**
     * 根据ID获取日记（Flow）
     */
    fun getDiaryByIdFlow(id: Long): Flow<DiaryEntry?> {
        return diaryDao.getDiaryByIdFlow(id)
    }

    /**
     * 搜索日记
     */
    fun searchDiaries(query: String): Flow<List<DiaryEntry>> {
        return diaryDao.searchDiaries(query)
    }

    /**
     * 根据主题获取日记
     */
    fun getDiariesByTheme(themeId: Long): Flow<List<DiaryEntry>> {
        return diaryDao.getDiariesByTheme(themeId)
    }

    /**
     * 根据标签获取日记
     */
    fun getDiariesByTag(tag: String): Flow<List<DiaryEntry>> {
        return diaryDao.getDiariesByTag(tag)
    }

    /**
     * 获取指定日期范围的日记
     */
    fun getDiariesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<DiaryEntry>> {
        return diaryDao.getDiariesByDateRange(startDate, endDate)
    }

    /**
     * 获取本周日记
     */
    fun getThisWeekDiaries(
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): Flow<List<DiaryEntry>> {
        return diaryDao.getThisWeekDiaries(weekStart, weekEnd)
    }

    /**
     * 获取本周起止时间
     */
    fun getThisWeekRange(): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDateTime.now()
        val dayOfWeek = now.dayOfWeek.value
        val weekStart = now.minusDays((dayOfWeek - 1).toLong()).toLocalDate().atStartOfDay()
        val weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59)
        return Pair(weekStart, weekEnd)
    }

    /**
     * 获取每周精选日记
     */
    fun getWeeklyHighlights(): Flow<List<DiaryEntry>> {
        return diaryDao.getWeeklyHighlights()
    }

    /**
     * 设置/取消每周精选
     */
    suspend fun setWeeklyHighlight(diaryId: Long, isHighlight: Boolean) {
        diaryDao.updateWeeklyHighlight(diaryId, isHighlight)
    }

    /**
     * 获取最近的日记
     */
    fun getRecentDiaries(limit: Int = 10): Flow<List<DiaryEntry>> {
        return diaryDao.getRecentDiaries(limit)
    }

    /**
     * 获取日记数量
     */
    fun getDiaryCount(): Flow<Int> {
        return diaryDao.getDiaryCount()
    }

    /**
     * 根据心情获取日记
     */
    fun getDiariesByMood(mood: Int): Flow<List<DiaryEntry>> {
        return diaryDao.getDiariesByMood(mood)
    }

    // ==================== 主题操作 ====================

    /**
     * 创建主题
     */
    suspend fun createTheme(theme: Theme): Long {
        return themeDao.insert(theme)
    }

    /**
     * 更新主题
     */
    suspend fun updateTheme(theme: Theme) {
        themeDao.update(theme)
    }

    /**
     * 删除主题
     */
    suspend fun deleteTheme(theme: Theme) {
        themeDao.delete(theme)
    }

    /**
     * 根据ID删除主题
     */
    suspend fun deleteThemeById(id: Long) {
        themeDao.deleteById(id)
    }

    /**
     * 获取所有主题
     */
    fun getAllThemes(): Flow<List<Theme>> {
        return themeDao.getAllThemes()
    }

    /**
     * 获取系统预设主题
     */
    fun getSystemThemes(): Flow<List<Theme>> {
        return themeDao.getSystemThemes()
    }

    /**
     * 获取用户自定义主题
     */
    fun getUserThemes(): Flow<List<Theme>> {
        return themeDao.getUserThemes()
    }

    /**
     * 根据ID获取主题
     */
    suspend fun getThemeById(id: Long): Theme? {
        return themeDao.getThemeById(id)
    }

    /**
     * 搜索主题
     */
    fun searchThemesByName(name: String): Flow<List<Theme>> {
        return themeDao.searchThemesByName(name)
    }

    /**
     * 检查主题名称是否存在
     */
    suspend fun isThemeNameExists(name: String, excludeId: Long = 0): Boolean {
        return themeDao.isNameExists(name, excludeId) > 0
    }
}
