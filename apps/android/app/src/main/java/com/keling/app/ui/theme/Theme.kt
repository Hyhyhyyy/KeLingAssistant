/**
 * Theme.kt
 * 应用主题配置 - 包含颜色方案、字体、形状
 * Material 3 设计系统
 */

package com.keling.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 浅色主题颜色方案
 */
private val LightColorScheme = lightColorScheme(
    primary = StellarOrange,
    onPrimary = DawnWhite,
    primaryContainer = StellarOrange.copy(alpha = 0.2f),
    onPrimaryContainer = StellarOrangeDark,

    secondary = MossGreen,
    onSecondary = DawnWhite,
    secondaryContainer = MossGreen.copy(alpha = 0.2f),
    onSecondaryContainer = MossGreenDark,

    tertiary = MistRose,
    onTertiary = DawnWhite,

    background = DawnWhite,
    onBackground = EarthBrown,

    surface = DawnWhite,
    onSurface = EarthBrown,
    surfaceVariant = WarmSand,
    onSurfaceVariant = EarthBrown.copy(alpha = 0.7f),

    error = ErrorRed,
    onError = DawnWhite,

    outline = BorderGray,
    outlineVariant = BorderGray.copy(alpha = 0.5f)
)

/**
 * 深色主题颜色方案
 */
private val DarkColorScheme = darkColorScheme(
    primary = StellarOrange,
    onPrimary = EarthBrown,
    primaryContainer = StellarOrangeDark,
    onPrimaryContainer = DawnWhite,

    secondary = MossGreen,
    onSecondary = EarthBrown,
    secondaryContainer = MossGreenDark,
    onSecondaryContainer = DawnWhite,

    tertiary = MistRose,
    onTertiary = EarthBrown,

    background = DarkBackground,
    onBackground = DawnWhite,

    surface = DarkSurface,
    onSurface = DawnWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DawnWhite.copy(alpha = 0.7f),

    error = ErrorRed,
    onError = DawnWhite,

    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.5f)
)

/**
 * 课灵主题组件
 * 所有页面都要包裹在这个主题内
 *
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor Android 12+的动态颜色，默认关闭以保持品牌一致性
 * @param content 主题包裹的内容
 */
@Composable
fun KeLingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 默认关闭动态颜色，保持品牌一致性
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 设置状态栏颜色
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 固定 fontScale=1f，确保字号设计一致性
    val density = LocalDensity.current
    val fixedDensity = Density(density = density.density, fontScale = 1f)

    CompositionLocalProvider(LocalDensity provides fixedDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}