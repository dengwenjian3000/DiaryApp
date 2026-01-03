package com.example.diaryapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 温暖纸张质感配色方案 - 浅色主题
private val LightColorScheme = lightColorScheme(
    // 主色调
    primary = AccentWarm,
    onPrimary = Color.White,
    primaryContainer = PaperMedium,
    onPrimaryContainer = InkPrimary,

    // 次要色调
    secondary = AccentSoft,
    onSecondary = InkPrimary,
    secondaryContainer = PaperMedium,
    onSecondaryContainer = InkSecondary,

    // 第三色调
    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = PaperMedium,
    onTertiaryContainer = InkPrimary,

    // 背景
    background = PaperLight,
    onBackground = InkPrimary,

    // 表面
    surface = PaperMedium,
    onSurface = InkPrimary,
    surfaceVariant = PaperDark,
    onSurfaceVariant = InkSecondary,

    // 错误
    error = AccentRed,
    onError = Color.White,
    errorContainer = PaperMedium,
    onErrorContainer = AccentRed,

    // 轮廓
    outline = PaperDark,
    outlineVariant = PaperDark,

    // 反转表面（用于浮层等）
    inverseSurface = InkPrimary,
    inverseOnSurface = PaperLight,

    // 其他
    scrim = Color.Black,
)

// 深色主题（可选，用于暗黑模式）
private val DarkColorScheme = darkColorScheme(
    primary = AccentWarm,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3E2723),
    onPrimaryContainer = PaperLight,

    secondary = AccentSoft,
    onSecondary = InkPrimary,
    secondaryContainer = Color(0xFF3E2723),
    onSecondaryContainer = PaperMedium,

    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3E2723),
    onTertiaryContainer = PaperLight,

    background = Color(0xFF2D2420),
    onBackground = PaperLight,

    surface = Color(0xFF3E2723),
    onSurface = PaperLight,
    surfaceVariant = Color(0xFF4A3728),
    onSurfaceVariant = PaperMedium,

    error = AccentRed,
    onError = Color.White,
    errorContainer = Color(0xFF3E2723),
    onErrorContainer = AccentRed,

    outline = PaperDark,
    outlineVariant = InkLight,

    inverseSurface = PaperLight,
    inverseOnSurface = InkPrimary,

    scrim = Color.Black,
)

@Composable
fun DiaryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 暂不使用动态颜色
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
