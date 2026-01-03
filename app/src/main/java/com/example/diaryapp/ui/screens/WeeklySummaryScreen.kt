package com.example.diaryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diaryapp.service.WeeklySummaryService
import com.example.diaryapp.ui.components.DiaryCard
import com.example.diaryapp.ui.theme.*

/**
 * 每周总结屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(
    summary: WeeklySummaryService.WeeklySummary,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    onShareClick: () -> Unit,
    onDiaryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "每周总结",
                            style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary
                        )
                        Text(
                            text = summary.getWeekDisplayText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = InkLight
                        )
                    }
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
                    IconButton(onClick = onExportClick) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "导出",
                            tint = InkPrimary
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 统计概览
            item {
                StatsOverview(
                    totalEntries = summary.totalEntries,
                    topTags = summary.topTags,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 文字总结
            item {
                SummaryCard(
                    title = "本周回顾",
                    content = summary.summary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 心情分布
            if (summary.moodDistribution.average != null) {
                item {
                    MoodDistributionCard(
                        moodDistribution = summary.moodDistribution,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 高频标签
            if (summary.topTags.isNotEmpty()) {
                item {
                    TopTagsCard(
                        topTags = summary.topTags,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 精选日记
            if (summary.featuredEntries.isNotEmpty()) {
                item {
                    Text(
                        text = "精选日记",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                items(summary.featuredEntries) { featured ->
                    FeaturedEntryCard(
                        featured = featured,
                        onClick = { onDiaryClick(featured.diary.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 统计概览卡片
 */
@Composable
private fun StatsOverview(
    totalEntries: Int,
    topTags: List<WeeklySummaryService.TagFrequency>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "本周统计",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    value = totalEntries.toString(),
                    label = "篇日记",
                    color = AccentWarm
                )

                if (topTags.isNotEmpty()) {
                    StatItem(
                        value = topTags.first().count.toString(),
                        label = topTags.first().tag,
                        color = AccentGreen
                    )
                }

                if (topTags.size >= 2) {
                    StatItem(
                        value = topTags[1].count.toString(),
                        label = topTags[1].tag,
                        color = AccentSoft
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = color,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = InkSecondary
        )
    }
}

/**
 * 总结卡片
 */
@Composable
private fun SummaryCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

/**
 * 心情分布卡片
 */
@Composable
private fun MoodDistributionCard(
    moodDistribution: WeeklySummaryService.MoodDistribution,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "心情分布",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 心情图表
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                moodDistribution.distribution.forEach { (mood, count) ->
                    if (count > 0) {
                        MoodBar(
                            mood = mood,
                            count = count,
                            maxCount = moodDistribution.distribution.values.maxOrNull() ?: 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 平均心情
            moodDistribution.average?.let { avg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "平均心情：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%.1f", avg),
                        style = MaterialTheme.typography.titleMedium,
                        color = MoodColors[avg.toInt().coerceIn(1, 5) - 1],
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodBar(
    mood: Int,
    count: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val height = (count.toFloat() / maxCount * 80).dp.coerceAtLeast(20.dp)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = InkSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(height)
                .background(MoodColors[mood - 1], RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 高频标签卡片
 */
@Composable
private fun TopTagsCard(
    topTags: List<WeeklySummaryService.TagFrequency>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "高频标签",
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topTags) { tagFreq ->
                    TagFrequencyChip(
                        tag = tagFreq.tag,
                        count = tagFreq.count
                    )
                }
            }
        }
    }
}

@Composable
private fun TagFrequencyChip(
    tag: String,
    count: Int
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PaperLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                color = InkSecondary
            )
            Surface(
                color = AccentWarm,
                shape = CircleShape
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 精选日记卡片
 */
@Composable
private fun FeaturedEntryCard(
    featured: WeeklySummaryService.FeaturedEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = featured.diary.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                HighlightBadge(
                    reason = featured.highlightReason
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            featured.diary.mood?.let {
                MoodIndicator(mood = it)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = featured.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary
            )
        }
    }
}

@Composable
private fun HighlightBadge(
    reason: WeeklySummaryService.HighlightReason
) {
    val text = when (reason) {
        WeeklySummaryService.HighlightReason.MOST_PHOTOS -> "图片最多"
        WeeklySummaryService.HighlightReason.LONGEST_CONTENT -> "内容最长"
        WeeklySummaryService.HighlightReason.HIGHLIGHTED -> "精选"
        WeeklySummaryService.HighlightReason.MOST_TAGS -> "标签最多"
        WeeklySummaryService.HighlightReason.MOOD_EXTREME -> "心情强烈"
    }

    Surface(
        color = AccentSoft,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = InkPrimary
        )
    }
}
