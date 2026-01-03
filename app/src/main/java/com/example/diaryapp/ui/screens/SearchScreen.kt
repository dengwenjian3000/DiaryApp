package com.example.diaryapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diaryapp.data.database.entities.DiaryEntry
import com.example.diaryapp.data.database.entities.Theme
import com.example.diaryapp.ui.components.*
import com.example.diaryapp.ui.theme.*

/**
 * 搜索类型
 */
enum class SearchType(val displayName: String) {
    ALL("全部"),
    CONTENT("内容"),
    TAGS("标签"),
    THEMES("主题")
}

/**
 * 搜索过滤器
 */
data class SearchFilter(
    val searchType: SearchType = SearchType.ALL,
    selectedTags: Set<String> = emptySet(),
    selectedThemeId: Long? = null
)

/**
 * 搜索屏幕（改进版，类似微信）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchResults: List<DiaryEntry>,
    allTags: List<String>,
    allThemes: List<Theme>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (query: String, filter: SearchFilter) -> Unit,
    onDiaryClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(SearchFilter()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("搜索日记、标签、主题…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // 筛选按钮
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "筛选",
                                        tint = if (currentFilter != SearchFilter()) AccentWarm else InkLight
                                    )
                                }
                                // 清除按钮
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        onSearchQueryChange("")
                                        currentFilter = SearchFilter()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "清除"
                                        )
                                    }
                                }
                            }
                        }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PaperLight
                )
            )
        },
        containerColor = PaperLight
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                searchQuery.isEmpty() -> {
                    // 搜索建议和历史
                    SearchSuggestionsWithFilters(
                        allTags = allTags,
                        allThemes = allThemes,
                        onSuggestionClick = { suggestion ->
                            onSearchQueryChange(suggestion)
                            onSearch(suggestion, currentFilter)
                        },
                        onFilterClick = {
                            showFilterSheet = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                searchResults.isEmpty() -> {
                    // 无结果
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "没有找到相关日记",
                        message = "试试其他关键词或调整筛选条件",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // 搜索结果
                    SearchResultsList(
                        results = searchResults,
                        onDiaryClick = onDiaryClick
                    )
                }
            }
        }
    }

    // 筛选底部表单
    if (showFilterSheet) {
        FilterBottomSheet(
            allTags = allTags,
            allThemes = allThemes,
            currentFilter = currentFilter,
            onFilterChange = { newFilter ->
                currentFilter = newFilter
                if (searchQuery.isNotEmpty()) {
                    onSearch(searchQuery, newFilter)
                }
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

/**
 * 搜索结果列表
 */
@Composable
private fun SearchResultsList(
    results: List<DiaryEntry>,
    onDiaryClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 结果统计
        item {
            Text(
                text = "找到 ${results.size} 篇日记",
                style = MaterialTheme.typography.labelMedium,
                color = InkLight,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(results, key = { it.id }) { diary ->
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
        }
    }
}

/**
 * 搜索建议（带分类和筛选）
 */
@Composable
private fun SearchSuggestionsWithFilters(
    allTags: List<String>,
    allThemes: List<Theme>,
    onSuggestionClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 搜索分类
        item {
            SearchCategorySection(
                title = "按分类搜索",
                items = listOf(
                    "今天的日记" to "今天",
                    "本周日记" to "本周",
                    "本月日记" to "本月",
                    "有图片的日记" to "图片"
                ),
                onItemClick = { onSuggestionClick(it) }
            )
        }

        // 标签搜索
        if (allTags.isNotEmpty()) {
            item {
                SearchTagsSection(
                    tags = allTags.take(12),
                    onTagClick = { tag -> onSuggestionClick(tag) }
                )
            }
        }

        // 主题搜索
        if (allThemes.isNotEmpty()) {
            item {
                SearchThemesSection(
                    themes = allThemes,
                    onThemeClick = { theme -> onSuggestionClick(theme.name) }
                )
            }
        }
    }
}

/**
 * 搜索分类区域
 */
@Composable
private fun SearchCategorySection(
    title: String,
    items: List<Pair<String, String>>,
    onItemClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = InkPrimary,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { (display, search) ->
                SuggestionChip(
                    onClick = { onItemClick(search) },
                    label = {
                        Text(
                            display,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}

/**
 * 标签搜索区域
 */
@Composable
private fun SearchTagsSection(
    tags: List<String>,
    onTagClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "标签",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "点击标签搜索",
                style = MaterialTheme.typography.labelSmall,
                color = InkLight
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                FilterChip(
                    onClick = { onTagClick(tag) },
                    label = { Text(tag, style = MaterialTheme.typography.bodySmall) },
                    selected = false,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = PaperMedium,
                        labelColor = InkSecondary,
                        selectedContainerColor = AccentWarm.copy(alpha = 0.3f),
                        selectedLabelColor = AccentWarm
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = null
                )
            }
        }
    }
}

/**
 * 主题搜索区域
 */
@Composable
private fun SearchThemesSection(
    themes: List<Theme>,
    onThemeClick: (Theme) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "主题",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "点击主题搜索",
                style = MaterialTheme.typography.labelSmall,
                color = InkLight
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themes) { theme ->
                Card(
                    onClick = { onThemeClick(theme) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PaperMedium
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(android.graphics.Color.parseColor(theme.color)),
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {}
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 筛选底部表单
 */
@Composable
private fun FilterBottomSheet(
    allTags: List<String>,
    allThemes: List<Theme>,
    currentFilter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSearchType by remember { mutableStateOf(currentFilter.searchType) }
    var selectedTags by remember { mutableStateOf(currentFilter.selectedTags) }
    var selectedTheme by remember { mutableStateOf(currentFilter.selectedThemeId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选条件") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 搜索类型
                Text(
                    text = "搜索范围",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSecondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchType.values().forEach { type ->
                        FilterChip(
                            onClick = { selectedSearchType = type },
                            label = { Text(type.displayName) },
                            selected = selectedSearchType == type,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentWarm.copy(alpha = 0.3f),
                                selectedLabelColor = AccentWarm
                            )
                        )
                    }
                }

                // 标签筛选
                if (selectedSearchType == SearchType.ALL || selectedSearchType == SearchType.TAGS) {
                    Text(
                        text = "标签筛选",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allTags.take(10).forEach { tag ->
                            val isSelected = tag in selectedTags
                            FilterChip(
                                onClick = {
                                    selectedTags = if (isSelected) {
                                        selectedTags - tag
                                    } else {
                                        selectedTags + tag
                                    }
                                },
                                label = { Text(tag) },
                                selected = isSelected,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentSoft.copy(alpha = 0.5f),
                                    selectedLabelColor = AccentWarm
                                )
                            )
                        }
                    }
                }

                // 主题筛选
                if (selectedSearchType == SearchType.ALL || selectedSearchType == SearchType.THEMES) {
                    Text(
                        text = "主题筛选",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allThemes) { theme ->
                            val isSelected = selectedTheme == theme.id
                            FilterChip(
                                onClick = {
                                    selectedTheme = if (isSelected) null else theme.id
                                },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(android.graphics.Color.parseColor(theme.color)),
                                            shape = CircleShape,
                                            modifier = Modifier.size(16.dp)
                                        ) {}
                                        Text(theme.name)
                                    }
                                },
                                selected = isSelected,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentSoft.copy(alpha = 0.5f),
                                    selectedLabelColor = AccentWarm
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onFilterChange(
                        SearchFilter(
                            searchType = selectedSearchType,
                            selectedTags = selectedTags,
                            selectedThemeId = selectedTheme
                        )
                    )
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
