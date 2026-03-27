/**
 * Type.kt
 * 高级字体排版系统 - Noto Serif SC 衬线字体 + 系统黑体
 * 精致、优雅、专业的设计风格
 */

package com.keling.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import com.keling.app.R

/**
 * 立体阴影效果（用于大标题）
 * 创造深度感和精致感
 */
private fun elegantShadow(blur: Float = 3f, alpha: Float = 0.12f) = Shadow(
    color = Color.Black.copy(alpha = alpha),
    offset = Offset(1f, 2f),
    blurRadius = blur
)

/**
 * 发光阴影效果
 */
private fun glowShadow(color: Color) = Shadow(
    color = color.copy(alpha = 0.3f),
    offset = Offset(0f, 0f),
    blurRadius = 8f
)

/**
 * Noto Serif SC 衬线字体家族
 * 用于标题和重要文字，展现优雅气质
 */
val NotoSerifFamily = FontFamily(
    Font(R.font.noto_serif_sc, FontWeight.Normal),
    Font(R.font.noto_serif_sc, FontWeight.Medium),
    Font(R.font.noto_serif_sc, FontWeight.Bold)
)

/**
 * 系统黑体字体家族
 * 用于正文和界面文字，确保可读性
 */
val HeiTiFontFamily = FontFamily.Default

/**
 * 混合字体排版系统
 * 衬线标题 + 黑体正文
 */
val Typography = Typography(
    // ==================== 展示级标题（衬线字体） ====================
    /** 超大标题 - 用于启动页、重要页面主标题 */
    displayLarge = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1).sp,
        shadow = elegantShadow(blur = 4f, alpha = 0.15f)
    ),

    /** 大型展示标题 - 用于特殊强调 */
    displayMedium = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
        shadow = elegantShadow()
    ),

    /** 展示小标题 */
    displaySmall = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp,
        shadow = elegantShadow(blur = 2f, alpha = 0.1f)
    ),

    // ==================== 标题级（衬线字体） ====================
    /** 大标题 - 用于页面主标题 */
    headlineLarge = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.2).sp
    ),

    /** 中标题 - 用于区块标题、卡片标题 */
    headlineMedium = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.1).sp
    ),

    /** 小标题 - 用于列表项标题 */
    headlineSmall = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),

    // ==================== 副标题级（黑体） ====================
    /** 大副标题 - 用于重要副标题 */
    titleLarge = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    /** 中副标题 - 默认副标题 */
    titleMedium = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    ),

    /** 小副标题 */
    titleSmall = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp
    ),

    // ==================== 正文级（黑体） ====================
    /** 大正文 - 主要内容 */
    bodyLarge = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp
    ),

    /** 中正文 - 默认正文 */
    bodyMedium = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),

    /** 小正文 - 辅助文字 */
    bodySmall = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),

    // ==================== 标签级（黑体） ====================
    /** 大标签 */
    labelLarge = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),

    /** 中标签 - 默认标签 */
    labelMedium = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),

    /** 小标签 */
    labelSmall = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

/**
 * 特殊用途的文字样式
 */
object SpecialTextStyle {
    /** 数字显示 - 用于计时器、统计数字 */
    val number = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        letterSpacing = (-1.5).sp,
        shadow = elegantShadow(blur = 3f, alpha = 0.1f)
    )

    /** 引用文字 - 优雅衬线 */
    val quote = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.3.sp
    )

    /** 代码文字 */
    val code = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    /** 强调文字 */
    val emphasis = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    )

    /** 装饰性标题 */
    val decorative = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp,
        shadow = elegantShadow(blur = 5f, alpha = 0.2f)
    )
}

/**
 * 游戏化文字样式
 * 混合字体风格
 */
object GameTextStyle {
    /** 游戏大标题 - 衬线字体带阴影 */
    val gameTitle = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp,
        shadow = Shadow(
            color = Color.Black.copy(alpha = 0.12f),
            offset = Offset(1.5f, 1.5f),
            blurRadius = 3f
        )
    )

    /** 游戏副标题 */
    val gameSubtitle = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp
    )

    /** 游戏卡片标题 */
    val cardTitle = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    )

    /** 游戏按钮文字 */
    val buttonText = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )

    /** 游戏标签文字 */
    val tagText = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp
    )

    /** 游戏奖励数字 - 衬线字体 */
    val rewardNumber = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.5).sp,
        shadow = elegantShadow(blur = 2f, alpha = 0.1f)
    )

    /** 游戏统计数字 */
    val statNumber = TextStyle(
        fontFamily = NotoSerifFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.8).sp
    )

    /** 游戏提示文字 */
    val hint = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.2.sp
    )

    /** 游戏描述文字 */
    val description = TextStyle(
        fontFamily = HeiTiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    )
}