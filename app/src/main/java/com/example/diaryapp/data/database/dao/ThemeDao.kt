package com.example.diaryapp.data.database.dao

import androidx.room.*
import com.example.diaryapp.data.database.entities.Theme
import kotlinx.coroutines.flow.Flow

/**
 * 主题数据访问对象
 */
@Dao
interface ThemeDao {
    /**
     * 插入主题
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(theme: Theme): Long

    /**
     * 更新主题
     */
    @Update
    suspend fun update(theme: Theme)

    /**
     * 删除主题
     */
    @Delete
    suspend fun delete(theme: Theme)

    /**
     * 根据ID删除主题
     */
    @Query("DELETE FROM themes WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取所有主题，按排序顺序
     */
    @Query("SELECT * FROM themes ORDER BY sortOrder ASC, name ASC")
    fun getAllThemes(): Flow<List<Theme>>

    /**
     * 获取系统预设主题
     */
    @Query("SELECT * FROM themes WHERE isSystemPreset = 1 ORDER BY sortOrder ASC")
    fun getSystemThemes(): Flow<List<Theme>>

    /**
     * 获取用户自定义主题
     */
    @Query("SELECT * FROM themes WHERE isSystemPreset = 0 ORDER BY sortOrder ASC, name ASC")
    fun getUserThemes(): Flow<List<Theme>>

    /**
     * 根据ID获取主题
     */
    @Query("SELECT * FROM themes WHERE id = :id")
    suspend fun getThemeById(id: Long): Theme?

    /**
     * 根据名称搜索主题
     */
    @Query("SELECT * FROM themes WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    fun searchThemesByName(name: String): Flow<List<Theme>>

    /**
     * 检查主题名称是否已存在
     */
    @Query("SELECT COUNT(*) FROM themes WHERE name = :name AND id != :excludeId")
    suspend fun isNameExists(name: String, excludeId: Long = 0): Int
}
