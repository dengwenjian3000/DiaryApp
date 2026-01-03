package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.data.database.DiaryDatabase
import com.example.diaryapp.data.repository.DiaryRepository
import com.example.diaryapp.service.FileStorageService
import com.example.diaryapp.service.TagAnalyzerService
import com.example.diaryapp.service.WeeklySummaryService
import com.example.diaryapp.ui.screens.*
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.util.ExportUtil
import com.example.diaryapp.util.ShareUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 获取所有标签（从日记列表中提取）
 */
fun getAllTagsFromDiaries(diaries: List<com.example.diaryapp.data.database.entities.DiaryEntry>): List<String> {
    return diaries
        .flatMap { it.autoTags + it.manualTags }
        .distinct()
        .sorted()
}

/**
 * 主Activity
 */
class MainActivity : ComponentActivity() {
    private lateinit var database: DiaryDatabase
    private lateinit var repository: DiaryRepository
    private lateinit var tagAnalyzer: TagAnalyzerService
    private lateinit var weeklySummaryService: WeeklySummaryService
    private lateinit var fileStorageService: FileStorageService

    // 权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 处理权限结果
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据库和仓库
        database = DiaryDatabase.getInstance(applicationContext)
        repository = DiaryRepository(database.diaryDao(), database.themeDao())
        tagAnalyzer = TagAnalyzerService()
        weeklySummaryService = WeeklySummaryService()
        fileStorageService = FileStorageService(applicationContext)

        // 初始化系统主题
        CoroutineScope(Dispatchers.IO).launch {
            database.initializeSystemThemes()
        }

        setContent {
            DiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiaryApp(
                        repository = repository,
                        tagAnalyzer = tagAnalyzer,
                        weeklySummaryService = weeklySummaryService,
                        fileStorageService = fileStorageService,
                        exportUtil = ExportUtil(applicationContext),
                        shareUtil = ShareUtil(applicationContext)
                    )
                }
            }
        }
    }
}

/**
 * 应用主入口
 */
@Composable
fun DiaryApp(
    repository: DiaryRepository,
    tagAnalyzer: TagAnalyzerService,
    weeklySummaryService: WeeklySummaryService,
    fileStorageService: FileStorageService,
    exportUtil: ExportUtil,
    shareUtil: ShareUtil
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            repository,
            tagAnalyzer,
            weeklySummaryService,
            exportUtil,
            shareUtil,
            fileStorageService
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 主页
        composable("home") {
            HomeScreen(
                diaries = uiState.diaries,
                onDiaryClick = { diaryId ->
                    viewModel.selectDiary(diaryId)
                    navController.navigate("editor")
                },
                onNewDiaryClick = {
                    viewModel.createNewDiary()
                    navController.navigate("editor")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onWeeklySummaryClick = {
                    viewModel.generateWeeklySummary()
                    navController.navigate("summary")
                },
                onThemeManageClick = {
                    navController.navigate("themes")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onDeleteDiary = { diary ->
                    viewModel.deleteDiary(diary)
                },
                onToggleHighlight = { diaryId ->
                    viewModel.toggleHighlight(diaryId)
                }
            )
        }

        // 搜索页
        composable("search") {
            SearchScreen(
                searchResults = uiState.searchResults,
                allTags = getAllTagsFromDiaries(uiState.diaries),
                allThemes = uiState.themes,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { query, filter ->
                    viewModel.updateSearchQuery(query)
                },
                onDiaryClick = { diaryId ->
                    viewModel.selectDiary(diaryId)
                    navController.navigate("editor")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 编辑器页
        composable("editor") {
            EditorScreen(
                title = uiState.currentTitle,
                content = uiState.currentContent,
                mood = uiState.currentMood,
                selectedTheme = uiState.currentTheme,
                autoTags = uiState.currentAutoTags,
                manualTags = uiState.currentManualTags,
                imageUris = uiState.currentImages,
                videoUris = uiState.currentVideos,
                availableThemes = uiState.themes,
                isEditing = uiState.isEditing,
                onTitleChange = { viewModel.updateTitle(it) },
                onContentChange = { viewModel.updateContent(it) },
                onMoodChange = { viewModel.updateMood(it) },
                onThemeChange = { viewModel.updateTheme(it) },
                onAddTag = { viewModel.addManualTag(it) },
                onRemoveTag = { viewModel.removeManualTag(it) },
                onAddImage = { viewModel.addImage(it) },
                onRemoveImage = { viewModel.removeImage(it) },
                onAddVideo = { viewModel.addVideo(it) },
                onRemoveVideo = { viewModel.removeVideo(it) },
                onGenerateTitle = { viewModel.autoGenerateTitle() },
                onGetTitleSuggestions = { viewModel.generateTitleSuggestions() },
                onSave = {
                    viewModel.saveDiary()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 每周总结页
        composable("summary") {
            uiState.weeklySummary?.let { summary ->
                WeeklySummaryScreen(
                    summary = summary,
                    onBackClick = { navController.popBackStack() },
                    onExportClick = {
                        viewModel.exportWeeklySummary()
                    },
                    onShareClick = {
                        viewModel.shareWeeklySummary()
                    },
                    onDiaryClick = { diaryId ->
                        viewModel.selectDiary(diaryId)
                        navController.navigate("editor")
                    }
                )
            }
        }

        // 主题管理页
        composable("themes") {
            ThemeManageScreen(
                systemThemes = uiState.systemThemes,
                userThemes = uiState.userThemes,
                onCreateTheme = { name, color ->
                    viewModel.createTheme(name, color)
                },
                onDeleteTheme = { theme ->
                    viewModel.deleteTheme(theme)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 设置页
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onBackupClick = {
                    // TODO: 实现备份功能
                },
                onRestoreClick = {
                    // TODO: 实现恢复功能
                },
                onClearCacheClick = {
                    // TODO: 实现清除缓存功能
                },
                onAboutClick = {
                    // TODO: 实现关于页面
                }
            )
        }
    }
}
