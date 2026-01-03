package com.example.diaryapp

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.database.entities.DiaryEntry
import com.example.diaryapp.data.database.entities.Theme
import com.example.diaryapp.data.repository.DiaryRepository
import com.example.diaryapp.service.TagAnalyzerService
import com.example.diaryapp.service.WeeklySummaryService
import com.example.diaryapp.util.ExportUtil
import com.example.diaryapp.util.ShareUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * UI 状态
 */
data class DiaryUiState(
    // 日记列表
    val diaries: List<DiaryEntry> = emptyList(),
    val searchResults: List<DiaryEntry> = emptyList(),
    val searchQuery: String = "",

    // 主题列表
    val themes: List<Theme> = emptyList(),
    val systemThemes: List<Theme> = emptyList(),
    val userThemes: List<Theme> = emptyList(),

    // 当前编辑的日记
    val isEditing: Boolean = false,
    val currentDiaryId: Long? = null,
    val currentTitle: String = "",
    val currentContent: String = "",
    val currentMood: Int? = null,
    val currentTheme: Theme? = null,
    val currentAutoTags: List<String> = emptyList(),
    val currentManualTags: List<String> = emptyList(),
    val currentImages: List<Uri> = emptyList(),
    val currentVideos: List<Uri> = emptyList(),

    // 每周总结
    val weeklySummary: WeeklySummaryService.WeeklySummary? = null,

    // 加载状态
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 主ViewModel
 */
class MainViewModel(
    private val repository: DiaryRepository,
    private val tagAnalyzer: TagAnalyzerService,
    private val weeklySummaryService: WeeklySummaryService,
    private val exportUtil: ExportUtil,
    private val shareUtil: ShareUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        loadDiaries()
        loadThemes()
    }

    /**
     * 加载日记列表
     */
    private fun loadDiaries() {
        viewModelScope.launch {
            repository.getAllDiaries().collect { diaries ->
                _uiState.update { it.copy(diaries = diaries) }
            }
        }
    }

    /**
     * 加载主题列表
     */
    private fun loadThemes() {
        viewModelScope.launch {
            combine(
                repository.getAllThemes(),
                repository.getSystemThemes(),
                repository.getUserThemes()
            ) { all, system, user ->
                Triple(all, system, user)
            }.collect { (all, system, user) ->
                _uiState.update {
                    it.copy(
                        themes = all,
                        systemThemes = system,
                        userThemes = user
                    )
                }
            }
        }
    }

    /**
     * 搜索日记
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.searchDiaries(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    /**
     * 选择要编辑的日记
     */
    fun selectDiary(diaryId: Long) {
        viewModelScope.launch {
            val diary = repository.getDiaryById(diaryId)
            diary?.let {
                _uiState.update { state ->
                    state.copy(
                        isEditing = true,
                        currentDiaryId = diary.id,
                        currentTitle = diary.title,
                        currentContent = diary.plainContent,
                        currentMood = diary.mood,
                        currentTheme = diary.themeId?.let { id ->
                            repository.getThemeById(id)
                        },
                        currentAutoTags = diary.autoTags,
                        currentManualTags = diary.manualTags,
                        currentImages = diary.imagePaths.map { Uri.parse(it) },
                        currentVideos = diary.videoPaths.map { Uri.parse(it) }
                    )
                }
            }
        }
    }

    /**
     * 创建新日记
     */
    fun createNewDiary() {
        _uiState.update {
            it.copy(
                isEditing = false,
                currentDiaryId = null,
                currentTitle = "",
                currentContent = "",
                currentMood = null,
                currentTheme = null,
                currentAutoTags = emptyList(),
                currentManualTags = emptyList(),
                currentImages = emptyList(),
                currentVideos = emptyList()
            )
        }
    }

    /**
     * 更新标题
     */
    fun updateTitle(title: String) {
        _uiState.update { it.copy(currentTitle = title) }
        updateAutoTags()
    }

    /**
     * 更新内容
     */
    fun updateContent(content: String) {
        _uiState.update { it.copy(currentContent = content) }
        updateAutoTags()
    }

    /**
     * 更新心情
     */
    fun updateMood(mood: Int?) {
        _uiState.update { it.copy(currentMood = mood) }
    }

    /**
     * 更新主题
     */
    fun updateTheme(theme: Theme?) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    /**
     * 更新自动标签
     */
    private fun updateAutoTags() {
        val state = _uiState.value
        val autoTags = tagAnalyzer.analyzeTags(state.currentTitle, state.currentContent)
        _uiState.update { it.copy(currentAutoTags = autoTags) }
    }

    /**
     * 添加手动标签
     */
    fun addManualTag(tag: String) {
        _uiState.update {
            it.copy(currentManualTags = it.currentManualTags + tag)
        }
    }

    /**
     * 移除手动标签
     */
    fun removeManualTag(tag: String) {
        _uiState.update {
            it.copy(currentManualTags = it.currentManualTags - tag)
        }
    }

    /**
     * 添加图片
     */
    fun addImage(uri: Uri) {
        _uiState.update {
            it.copy(currentImages = it.currentImages + uri)
        }
    }

    /**
     * 移除图片
     */
    fun removeImage(uri: Uri) {
        _uiState.update {
            it.copy(currentImages = it.currentImages - uri)
        }
    }

    /**
     * 添加视频
     */
    fun addVideo(uri: Uri) {
        _uiState.update {
            it.copy(currentVideos = it.currentVideos + uri)
        }
    }

    /**
     * 移除视频
     */
    fun removeVideo(uri: Uri) {
        _uiState.update {
            it.copy(currentVideos = it.currentVideos - uri)
        }
    }

    /**
     * 保存日记
     */
    fun saveDiary() {
        viewModelScope.launch {
            val state = _uiState.value

            // 验证
            if (state.currentTitle.isBlank() && state.currentContent.isBlank()) {
                return@launch
            }

            // 生成自动标签
            val autoTags = tagAnalyzer.analyzeTags(
                state.currentTitle,
                state.currentContent
            )

            val now = LocalDateTime.now()
            val diary = DiaryEntry(
                id = state.currentDiaryId ?: 0,
                title = state.currentTitle.ifBlank { "无标题" },
                content = state.currentContent, // 实际应该是HTML
                plainContent = state.currentContent,
                createdAt = if (state.isEditing) {
                    // 保持原有的创建时间
                    repository.getDiaryById(state.currentDiaryId!!)?.createdAt ?: now
                } else {
                    now
                },
                updatedAt = now,
                themeId = state.currentTheme?.id,
                mood = state.currentMood,
                imagePaths = state.currentImages.map { it.toString() },
                videoPaths = state.currentVideos.map { it.toString() },
                autoTags = autoTags,
                manualTags = state.currentManualTags
            )

            if (state.isEditing && state.currentDiaryId != null) {
                repository.updateDiary(diary)
            } else {
                repository.createDiary(diary)
            }

            // 重置编辑状态
            createNewDiary()
        }
    }

    /**
     * 删除日记
     */
    fun deleteDiary(diary: DiaryEntry) {
        viewModelScope.launch {
            repository.deleteDiary(diary)
        }
    }

    /**
     * 切换精选状态
     */
    fun toggleHighlight(diaryId: Long) {
        viewModelScope.launch {
            val diary = repository.getDiaryById(diaryId)
            diary?.let {
                repository.setWeeklyHighlight(diaryId, !it.isWeeklyHighlight)
            }
        }
    }

    /**
     * 生成每周总结
     */
    fun generateWeeklySummary() {
        viewModelScope.launch {
            val (weekStart, weekEnd) = repository.getThisWeekRange()
            repository.getThisWeekDiaries(weekStart, weekEnd).collect { diaries ->
                val summary = weeklySummaryService.generateWeeklySummary(diaries)
                _uiState.update { it.copy(weeklySummary = summary) }
            }
        }
    }

    /**
     * 导出每周总结
     */
    fun exportWeeklySummary() {
        viewModelScope.launch {
            val summary = _uiState.value.weeklySummary ?: return@launch
            // 实现导出逻辑
        }
    }

    /**
     * 分享每周总结
     */
    fun shareWeeklySummary() {
        viewModelScope.launch {
            val summary = _uiState.value.weeklySummary ?: return@launch
            shareUtil.shareText(summary.summary, "每周总结")
        }
    }

    /**
     * 创建主题
     */
    fun createTheme(name: String, color: String) {
        viewModelScope.launch {
            val theme = Theme(
                name = name,
                color = color,
                isSystemPreset = false
            )
            repository.createTheme(theme)
        }
    }

    /**
     * 删除主题
     */
    fun deleteTheme(theme: Theme) {
        viewModelScope.launch {
            if (!theme.isSystemPreset) {
                repository.deleteTheme(theme)
            }
        }
    }
}

/**
 * ViewModel Factory
 */
class MainViewModelFactory(
    private val repository: DiaryRepository,
    private val tagAnalyzer: TagAnalyzerService,
    private val weeklySummaryService: WeeklySummaryService,
    private val exportUtil: ExportUtil,
    private val shareUtil: ShareUtil
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                repository,
                tagAnalyzer,
                weeklySummaryService,
                exportUtil,
                shareUtil
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
