package com.example.diaryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.diaryapp.data.database.entities.DiaryEntry
import com.example.diaryapp.ui.components.*
import com.example.diaryapp.ui.theme.*
import java.time.LocalDateTime

/**
 * 主页屏幕
 * 显示日记时间线
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaries: List<DiaryEntry>,
    onDiaryClick: (Long) -> Unit,
    onNewDiaryClick: () -> Unit,
    onSearchClick: () -> Unit,
    onWeeklySummaryClick: () -> Unit,
    onThemeManageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDeleteDiary: (DiaryEntry) -> Unit,
    onToggleHighlight: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<DiaryEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的日记",
                        style = MaterialTheme.typography.titleLarge,
                        color = InkPrimary
                    )
                },
                actions = {
                    // 搜索按钮
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = InkPrimary
                        )
                    }

                    // 每周总结按钮
                    IconButton(onClick = onWeeklySummaryClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "每周总结",
                            tint = InkPrimary
                        )
                    }

                    // 更多菜单
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = InkPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("主题管理") },
                                onClick = {
                                    showMenu = false
                                    onThemeManageClick()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = {
                                    showMenu = false
                                    onSettingsClick()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperLight
                )
            )
        },
        floatingActionButton = {
            DiaryFloatingActionButton(onClick = onNewDiaryClick)
        },
        containerColor = PaperLight
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (diaries.isEmpty()) {
                // 空状态
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "还没有日记哦",
                    message = "点击右下角的按钮开始写下第一篇日记吧"
                )
            } else {
                // 日记列表
                DiaryTimeline(
                    diaries = diaries,
                    onDiaryClick = onDiaryClick,
                    onDeleteClick = { showDeleteDialog = it },
                    onToggleHighlight = onToggleHighlight,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 删除确认对话框
        showDeleteDialog?.let { diary ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("删除日记") },
                text = { Text("确认删除这篇日记吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteDiary(diary)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("删除", color = AccentRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

/**
 * 日记时间线
 */
@Composable
private fun DiaryTimeline(
    diaries: List<DiaryEntry>,
    onDiaryClick: (Long) -> Unit,
    onDeleteClick: (DiaryEntry) -> Unit,
    onToggleHighlight: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 按日期分组
    val groupedDiaries = diaries.groupBy { it.createdAt.toLocalDate() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedDiaries.forEach { (date, diariesOfDay) ->
            // 日期分隔符
            item(key = "date_$date") {
                DateHeader(
                    date = date.atStartOfDay(),
                    count = diariesOfDay.size
                )
            }

            // 当天的日记
            items(diariesOfDay, key = { it.id }) { diary ->
                var showMenu by remember { mutableStateOf(false) }

                Box {
                    DiaryCard(
                        title = diary.title,
                        content = diary.plainContent,
                        date = diary.createdAt,
                        imagePaths = diary.imagePaths,
                        tags = diary.autoTags + diary.manualTags,
                        mood = diary.mood,
                        isHighlighted = diary.isWeeklyHighlight,
                        onClick = { onDiaryClick(diary.id) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 长按菜单
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = InkLight
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (diary.isWeeklyHighlight) "取消精选" else "设为精选"
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleHighlight(diary.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = AccentRed) },
                            onClick = {
                                showMenu = false
                                onDeleteClick(diary)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 日期分隔符
 */
@Composable
private fun DateHeader(
    date: LocalDateTime,
    count: Int
) {
    val dateText = formatDateForHeader(date)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleSmall,
                color = InkSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count 篇",
                style = MaterialTheme.typography.labelSmall,
                color = InkLight
            )
        }
    }
}

/**
 * 格式化日期用于标题
 */
private fun formatDateForHeader(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val date = dateTime.toLocalDate()

    return when {
        date == today -> "今天"
        date == today.minusDays(1) -> "昨天"
        date.year == today.year -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("M月d日 EEEE"))
        else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年M月d日"))
    }
}
