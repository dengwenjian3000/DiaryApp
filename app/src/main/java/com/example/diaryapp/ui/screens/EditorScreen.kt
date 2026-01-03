package com.example.diaryapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.diaryapp.data.database.entities.Theme
import com.example.diaryapp.service.TagAnalyzerService
import com.example.diaryapp.ui.components.*
import com.example.diaryapp.ui.theme.*
import java.io.File
import java.time.LocalDateTime

/**
 * 编辑器屏幕 - 写新日记或编辑现有日记
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    title: String,
    content: String,
    mood: Int?,
    selectedTheme: Theme?,
    autoTags: List<String>,
    manualTags: List<String>,
    imageUris: List<Uri>,
    videoUris: List<Uri>,
    availableThemes: List<Theme>,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onMoodChange: (Int?) -> Unit,
    onThemeChange: (Theme?) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onAddImage: (Uri) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onAddVideo: (Uri) -> Unit,
    onRemoveVideo: (Uri) -> Unit,
    onGenerateTitle: () -> Unit,
    onGetTitleSuggestions: () -> List<String>,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMoodSelector by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showTitleSuggestions by remember { mutableStateOf(false) }

    // 自动分析标签
    val analyzedTags = remember(title, content) {
        val analyzer = TagAnalyzerService()
        analyzer.analyzeTags(title, content)
    }
    val allTags = remember(autoTags, manualTags, analyzedTags) {
        (autoTags + manualTags + analyzedTags).distinct()
    }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            onAddImage(uri)
        }
    }

    // 视频选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAddVideo(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "编辑日记" else "写日记",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = InkPrimary
                        )
                    }
                },
                actions = {
                    // 保存按钮
                    IconButton(
                        onClick = onSave,
                        enabled = title.isNotBlank() || content.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "保存",
                            tint = if (title.isNotBlank() || content.isNotBlank()) AccentGreen else InkLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperLight
                )
            )
        },
        containerColor = PaperLight
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入
            TitleTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = "标题"
            )

            // AI 标题生成
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 快速生成按钮
                OutlinedButton(
                    onClick = onGenerateTitle,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentWarm
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(AccentWarm.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI生成标题", style = MaterialTheme.typography.bodySmall)
                }

                // 查看更多建议
                OutlinedButton(
                    onClick = { showTitleSuggestions = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InkSecondary
                    )
                ) {
                    Text("更多建议", style = MaterialTheme.typography.bodySmall)
                }
            }

            // 主题选择
            ThemeSelector(
                selectedTheme = selectedTheme,
                themes = availableThemes,
                onThemeClick = { showThemeSelector = true },
                modifier = Modifier.fillMaxWidth()
            )

            // 心情选择
            if (showMoodSelector) {
                MoodSelector(
                    selectedMood = mood,
                    onMoodSelected = {
                        onMoodChange(it)
                        showMoodSelector = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                MoodButtonRow(
                    selectedMood = mood,
                    onClick = { showMoodSelector = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 内容输入
            SimpleTextEditor(
                value = content,
                onValueChange = onContentChange,
                placeholder = "今天发生了什么？",
                modifier = Modifier.fillMaxWidth()
            )

            // 图片预览
            if (imageUris.isNotEmpty()) {
                MediaPreviewRow(
                    uris = imageUris,
                    onRemove = onRemoveImage,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 视频预览
            if (videoUris.isNotEmpty()) {
                VideoPreviewRow(
                    uris = videoUris,
                    onRemove = onRemoveVideo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 标签显示
            if (allTags.isNotEmpty()) {
                TagRow(
                    tags = allTags,
                    onTagClick = { /* 可以跳转到该标签的搜索结果 */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 媒体添加按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InkSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加图片")
                }

                OutlinedButton(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InkSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加视频")
                }
            }

            // 添加标签按钮
            OutlinedButton(
                onClick = { showTagDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = InkSecondary
                )
            ) {
                Text("添加标签")
            }
        }
    }

    // 主题选择对话框
    if (showThemeSelector) {
        ThemeSelectorDialog(
            themes = availableThemes,
            selectedTheme = selectedTheme,
            onThemeSelected = {
                onThemeChange(it)
                showThemeSelector = false
            },
            onDismiss = { showThemeSelector = false }
        )
    }

    // 添加标签对话框
    if (showTagDialog) {
        AddTagDialog(
            existingTags = allTags,
            onAddTag = {
                onAddTag(it)
                showTagDialog = false
            },
            onDismiss = { showTagDialog = false }
        )
    }

    // 标题建议对话框
    if (showTitleSuggestions) {
        TitleSuggestionsDialog(
            suggestions = onGetTitleSuggestions(),
            onSuggestionClick = { suggestion ->
                onTitleChange(suggestion)
                showTitleSuggestions = false
            },
            onDismiss = { showTitleSuggestions = false }
        )
    }
}

/**
 * 标题建议对话框
 */
@Composable
private fun TitleSuggestionsDialog(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择标题") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    Card(
                        onClick = { onSuggestionClick(suggestion) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PaperMedium
                        )
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 心情按钮行
 */
@Composable
private fun MoodButtonRow(
    selectedMood: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = PaperMedium,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedMood != null) "心情" else "选择心情",
                style = MaterialTheme.typography.bodyMedium,
                color = InkPrimary
            )
            if (selectedMood != null) {
                MoodIndicator(mood = selectedMood)
            } else {
                Text(
                    text = "点击选择",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
            }
        }
    }
}

/**
 * 主题选择器
 */
@Composable
private fun ThemeSelector(
    selectedTheme: Theme?,
    themes: List<Theme>,
    onThemeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onThemeClick,
        modifier = modifier.fillMaxWidth(),
        color = PaperMedium,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedTheme?.name ?: "选择主题（可选）",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedTheme != null) InkPrimary else InkLight
            )
            if (selectedTheme != null) {
                Surface(
                    color = Color(android.graphics.Color.parseColor(selectedTheme.color)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(24.dp)
                ) {}
            }
        }
    }
}

/**
 * 媒体预览行
 */
@Composable
private fun MediaPreviewRow(
    uris: List<Uri>,
    onRemove: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uris, key = { it.toString() }) { uri ->
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(PaperMedium, RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "图片",
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 视频预览行
 */
@Composable
private fun VideoPreviewRow(
    uris: List<Uri>,
    onRemove: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uris, key = { it.toString() }) { uri ->
            Box(
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .background(PaperMedium, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "视频",
                    tint = InkSecondary,
                    modifier = Modifier.size(32.dp)
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 主题选择对话框
 */
@Composable
private fun ThemeSelectorDialog(
    themes: List<Theme>,
    selectedTheme: Theme?,
    onThemeSelected: (Theme?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 无主题选项
                ThemeOption(
                    name = "无主题",
                    color = null,
                    isSelected = selectedTheme == null,
                    onClick = { onThemeSelected(null) }
                )

                themes.forEach { theme ->
                    ThemeOption(
                        name = theme.name,
                        color = theme.color,
                        isSelected = selectedTheme?.id == theme.id,
                        onClick = { onThemeSelected(theme) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun ThemeOption(
    name: String,
    color: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (isSelected) PaperDark else PaperMedium,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = InkPrimary
            )
            if (color != null) {
                Surface(
                    color = Color(android.graphics.Color.parseColor(color)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(24.dp)
                ) {}
            }
        }
    }
}

/**
 * 添加标签对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTagDialog(
    existingTags: List<String>,
    onAddTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加标签") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    placeholder = { Text("输入标签名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 推荐标签
                val recommendedTags = listOf(
                    "生活", "工作", "学习", "旅行", "美食",
                    "运动", "心情", "感悟", "家庭", "朋友"
                ).filter { it !in existingTags }

                if (recommendedTags.isNotEmpty()) {
                    Text(
                        text = "推荐标签",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkLight
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recommendedTags.forEach { tag ->
                            SuggestionChip(
                                onClick = { onAddTag(tag) },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (newTag.isNotBlank()) onAddTag(newTag) },
                enabled = newTag.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
