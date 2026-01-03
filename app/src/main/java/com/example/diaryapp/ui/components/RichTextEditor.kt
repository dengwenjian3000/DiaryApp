package com.example.diaryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diaryapp.ui.theme.*

/**
 * 富文本编辑器组件
 * 支持加粗、斜体、下划线等基本格式
 */
@Composable
fun DiaryRichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "今天发生了什么？",
    enabled: Boolean = true
) {
    var text by remember { mutableStateOf(value) }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderline by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(value) {
        text = value
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PaperMedium, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // 工具栏
        RichTextToolbar(
            isBold = isBold,
            isItalic = isItalic,
            isUnderline = isUnderline,
            onBoldClick = { isBold = !isBold },
            onItalicClick = { isItalic = !isItalic },
            onUnderlineClick = { isUnderline = !isUnderline },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 文本输入区
        SelectionContainer {
            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    onValueChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp)
                    .focusRequester(focusRequester),
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (enabled) InkPrimary else InkLight,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = InkLight,
                                fontSize = 16.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 字数统计
        Text(
            text = "${text.length} 字",
            style = MaterialTheme.typography.labelSmall,
            color = InkLight,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * 富文本工具栏
 */
@Composable
private fun RichTextToolbar(
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = PaperLight,
        shape = RoundedCornerShape(8.dp),
        border = null
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 加粗按钮
            FormatButton(
                icon = Icons.Default.FormatBold,
                isSelected = isBold,
                onClick = onBoldClick,
                contentDescription = "加粗"
            )

            // 斜体按钮
            FormatButton(
                icon = Icons.Default.FormatItalic,
                isSelected = isItalic,
                onClick = onItalicClick,
                contentDescription = "斜体"
            )

            // 下划线按钮
            FormatButton(
                icon = Icons.Default.FormatUnderlined,
                isSelected = isUnderline,
                onClick = onUnderlineClick,
                contentDescription = "下划线"
            )
        }
    }
}

/**
 * 格式按钮
 */
@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String?
) {
    val backgroundColor = if (isSelected) AccentWarm else Color.Transparent
    val iconColor = if (isSelected) Color.White else InkSecondary

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 简化版的文本编辑器（仅支持纯文本，用于快速记录）
 */
@Composable
fun SimpleTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "今天发生了什么？",
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PaperMedium,
        shape = RoundedCornerShape(12.dp),
        border = null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SelectionContainer {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp),
                    enabled = enabled,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (enabled) InkPrimary else InkLight,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = InkLight,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${value.length} 字",
                style = MaterialTheme.typography.labelSmall,
                color = InkLight,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * 标题输入框
 */
@Composable
fun TitleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "标题",
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PaperMedium,
        shape = RoundedCornerShape(12.dp),
        border = null
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = if (enabled) InkPrimary else InkLight,
                fontWeight = FontWeight.SemiBold
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = InkLight
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}

/**
 * HTML 转换扩展函数
 */
private fun String.toHtml(): String {
    // 这里可以实现更复杂的HTML转换逻辑
    return this
        .replace("\n", "<br>")
}

/**
 * 从HTML转换为纯文本
 */
private fun String.fromHtml(): String {
    // 这里可以实现更复杂的HTML解析逻辑
    return this
        .replace("<br>", "\n")
        .replace(Regex("<[^>]+>"), "")
}
