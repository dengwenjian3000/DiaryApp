package com.example.diaryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.diaryapp.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 日记卡片组件
 */
@Composable
fun DiaryCard(
    title: String,
    content: String,
    date: LocalDateTime,
    imagePaths: List<String> = emptyList(),
    tags: List<String> = emptyList(),
    mood: Int? = null,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullContent by remember { mutableStateOf(false) }
    val displayContent = if (showFullContent) content else {
        if (content.length > 150) content.substring(0, 150) + "…" else content
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaperMedium
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：日期和高亮标记
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.labelMedium,
                    color = InkLight
                )
                if (isHighlighted) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "精选",
                        tint = AccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 标题
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = InkPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 心情
            mood?.let {
                MoodIndicator(mood = it, modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 内容预览
            Text(
                text = displayContent,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            if (content.length > 150) {
                TextButton(
                    onClick = { showFullContent = !showFullContent },
                    modifier = Modifier.padding(start = (-8).dp, top = 4.dp)
                ) {
                    Text(
                        text = if (showFullContent) "收起" else "展开",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentWarm
                    )
                }
            }

            // 图片预览
            if (imagePaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ImagePreviewRow(
                    imagePaths = imagePaths,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 标签
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                TagRow(
                    tags = tags,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 图片预览行
 */
@Composable
fun ImagePreviewRow(
    imagePaths: List<String>,
    modifier: Modifier = Modifier
) {
    val displayImages = imagePaths.take(3)

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(displayImages) { imagePath ->
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "日记图片",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (imagePaths.size > 3) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PaperDark
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = InkSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${imagePaths.size - 3}",
                                style = MaterialTheme.typography.labelMedium,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 标签行
 */
@Composable
fun TagRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            TagChip(
                tag = tag,
                onClick = { onTagClick?.invoke(tag) }
            )
        }
    }
}

/**
 * 标签芯片
 */
@Composable
fun TagChip(
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PaperLight,
        border = null
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = InkSecondary
        )
    }
}

/**
 * 心情指示器
 */
@Composable
fun MoodIndicator(
    mood: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            val isFilled = index < mood
            val icon = if (isFilled) Icons.Default.Favorite else Icons.Default.FavoriteBorder
            val tint = if (isFilled) MoodColors[mood - 1] else InkLight

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 心情选择器
 */
@Composable
fun MoodSelector(
    selectedMood: Int?,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "今天心情怎么样？",
            style = MaterialTheme.typography.titleMedium,
            color = InkPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(5) { mood ->
                MoodButton(
                    mood = mood + 1,
                    isSelected = selectedMood == mood + 1,
                    onClick = { onMoodSelected(mood + 1) }
                )
            }
        }
    }
}

/**
 * 心情按钮
 */
@Composable
private fun MoodButton(
    mood: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = MoodColors[mood - 1]
    val scale = if (isSelected) 1.2f else 1f

    Box(
        modifier = Modifier
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "心情 $mood",
                tint = if (isSelected) color else InkLight,
                modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)
            )
        }
    }
}

/**
 * 空状态组件
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkLight,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = InkPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = InkSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 浮动操作按钮
 */
@Composable
fun DiaryFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = AccentWarm,
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "写日记",
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 日期格式化
 */
private fun formatDate(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val date = dateTime.toLocalDate()

    return when {
        date == today -> "今天 " + dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        date == today.minusDays(1) -> "昨天 " + dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        date.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("M月d日 HH:mm"))
        else -> dateTime.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
    }
}
