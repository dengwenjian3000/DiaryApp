package com.example.diaryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diaryapp.ui.theme.*

/**
 * 设置屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appVersion: String = "1.0.0",
    onBackClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
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
            // 数据管理区域
            item {
                Text(
                    text = "数据管理",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PaperMedium
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingItem(
                            title = "备份数据",
                            description = "将所有日记数据导出为备份文件",
                            onClick = onBackupClick
                        )
                        Divider(color = PaperDark, thickness = 1.dp)
                        SettingItem(
                            title = "恢复数据",
                            description = "从备份文件恢复日记数据",
                            onClick = onRestoreClick
                        )
                        Divider(color = PaperDark, thickness = 1.dp)
                        SettingItem(
                            title = "清除缓存",
                            description = "清除应用缓存中的临时文件",
                            onClick = onClearCacheClick
                        )
                    }
                }
            }

            // 关于区域
            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PaperMedium
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingItem(
                            title = "关于日记本",
                            description = "版本 $appVersion",
                            onClick = onAboutClick
                        )
                    }
                }
            }

            // 底部信息
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "日记本",
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "记录生活的美好瞬间",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkLight
                    )
                }
            }
        }
    }
}

/**
 * 设置项
 */
@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkLight
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = InkLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
