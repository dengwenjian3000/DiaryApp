package com.example.diaryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diaryapp.data.database.entities.Theme
import com.example.diaryapp.ui.theme.*

/**
 * 主题管理屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeManageScreen(
    systemThemes: List<Theme>,
    userThemes: List<Theme>,
    onCreateTheme: (String, String) -> Unit,
    onDeleteTheme: (Theme) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var themeToDelete by remember { mutableStateOf<Theme?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "主题管理",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = InkPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "创建主题",
                            tint = AccentWarm
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperLight
                )
            )
        },
        containerColor = PaperLight,
        floatingActionButton = {
            if (userThemes.isEmpty() && systemThemes.isEmpty()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = AccentWarm
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "创建主题"
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 系统预设主题
            if (systemThemes.isNotEmpty()) {
                item {
                    Text(
                        text = "系统预设主题",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(systemThemes, key = { it.id }) { theme ->
                    ThemeCard(
                        theme = theme,
                        canDelete = false,
                        onDelete = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 用户自定义主题
            if (userThemes.isNotEmpty()) {
                item {
                    Text(
                        text = "我的主题",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(userThemes, key = { it.id }) { theme ->
                    ThemeCard(
                        theme = theme,
                        canDelete = true,
                        onDelete = { themeToDelete = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 空状态
            if (systemThemes.isEmpty() && userThemes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "还没有主题",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右上角创建第一个主题",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkLight
                        )
                    }
                }
            }
        }
    }

    // 创建主题对话框
    if (showCreateDialog) {
        CreateThemeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, color ->
                onCreateTheme(name, color)
                showCreateDialog = false
            }
        )
    }

    // 删除确认对话框
    themeToDelete?.let { theme ->
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = { Text("删除主题") },
            text = { Text("确认删除主题「${theme.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTheme(theme)
                        themeToDelete = null
                    }
                ) {
                    Text("删除", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 主题卡片
 */
@Composable
private fun ThemeCard(
    theme: Theme,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 主题颜色指示器
                Surface(
                    color = Color(android.graphics.Color.parseColor(theme.color)),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {}

                Column {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (theme.isSystemPreset) {
                        Text(
                            text = "系统预设",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkLight
                        )
                    }
                }
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = InkLight
                    )
                }
            }
        }
    }
}

/**
 * 创建主题对话框
 */
@Composable
private fun CreateThemeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var themeName by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    val colorOptions = listOf(
        "#D87C4A" to "暖橙",
        "#7CB87C" to "清新绿",
        "#6B9BD1" to "天空蓝",
        "#E8B87D" to "柔和金",
        "#C75B5B" to "温暖红",
        "#B87C9E" to "淡紫",
        "#D4A574" to "大地棕",
        "#87CEEB" to "天蓝",
        "#9C7864" to "咖啡色",
        "#7CB8B8" to "青绿"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建主题") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text("主题名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSecondary
                )

                // 颜色选择网格
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.chunked(5).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEachIndexed { index, (color, name) ->
                                ColorOption(
                                    color = color,
                                    name = name,
                                    isSelected = selectedColorIndex == colorOptions.indexOf(color to name),
                                    onClick = { selectedColorIndex = colorOptions.indexOf(color to name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 填充剩余空间
                            if (row.size < 5) {
                                repeat(5 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (themeName.isNotBlank()) {
                        onConfirm(themeName, colorOptions[selectedColorIndex].first)
                    }
                },
                enabled = themeName.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ColorOption(
    color: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            color = Color(android.graphics.Color.parseColor(color)),
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(
                    3.dp,
                    InkPrimary
                )
            } else null
        ) {}
    }
}
