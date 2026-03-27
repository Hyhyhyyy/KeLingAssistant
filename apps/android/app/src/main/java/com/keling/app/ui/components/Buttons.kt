/**
 * Buttons.kt
 * 通用按钮组件 - 统一的按钮样式和交互效果
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ui.theme.*

/**
 * 按钮样式枚举
 */
enum class ButtonStyle {
    Primary,    // 主要按钮 - 实心
    Secondary,  // 次要按钮 - 边框
    Ghost,      // 幽灵按钮 - 无背景
    Gradient    // 渐变按钮
}

/**
 * 通用按钮组件
 */
@Composable
fun KeLingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    color: Color = StellarOrange,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    size: ButtonSize = ButtonSize.Medium
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f
            isPressed -> 0.96f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val (height, horizontalPadding, textStyle) = when (size) {
        ButtonSize.Small -> Triple(36.dp, 12.dp, MaterialTheme.typography.labelMedium)
        ButtonSize.Medium -> Triple(44.dp, 16.dp, MaterialTheme.typography.labelLarge)
        ButtonSize.Large -> Triple(52.dp, 24.dp, MaterialTheme.typography.bodyMedium)
    }

    val containerColor = when (style) {
        ButtonStyle.Primary -> color
        ButtonStyle.Secondary -> Color.Transparent
        ButtonStyle.Ghost -> Color.Transparent
        ButtonStyle.Gradient -> Color.Transparent
    }

    val contentColor = when (style) {
        ButtonStyle.Primary -> DawnWhite
        ButtonStyle.Secondary -> color
        ButtonStyle.Ghost -> color
        ButtonStyle.Gradient -> DawnWhite
    }

    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .then(
                when (style) {
                    ButtonStyle.Gradient -> Modifier
                        .clip(RoundedCornerShape(Radius.md))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(color, color.copy(alpha = 0.7f))
                            )
                        )
                    ButtonStyle.Primary -> Modifier
                        .clip(RoundedCornerShape(Radius.md))
                        .background(containerColor.copy(alpha = alpha))
                    ButtonStyle.Secondary -> Modifier
                        .clip(RoundedCornerShape(Radius.md))
                        .border(1.5.dp, color.copy(alpha = alpha), RoundedCornerShape(Radius.md))
                    ButtonStyle.Ghost -> Modifier
                        .clip(RoundedCornerShape(Radius.md))
                }
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = contentColor.copy(alpha = alpha)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = textStyle,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = alpha)
                )
            }
        }
    }
}

enum class ButtonSize {
    Small, Medium, Large
}

/**
 * 资源徽章组件
 * 显示带图标和数值的资源信息
 */
@Composable
fun ResourceBadge(
    icon: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    showAnimation: Boolean = true
) {
    val glowAlpha by rememberGlowPulse(enabled = showAnimation, minAlpha = 0.1f, maxAlpha = 0.2f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(color.copy(alpha = 0.12f + glowAlpha))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 快捷入口按钮
 * 六边形图标 + 文字标签
 */
@Composable
fun QuickEntryButton(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val floatOffset by rememberFloatingOffset(amplitude = 3.dp, durationMs = 2500 + (label.hashCode() % 1000))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = floatOffset.toPx()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 4.dp)
    ) {
        // 六边形图标容器
        Box(
            modifier = Modifier
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // 发光背景
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(8.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape)
            )

            // 主容器
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(com.keling.app.components.HexagonShape)
                    .background(color.copy(alpha = 0.15f))
                    .border(1.5.dp, color.copy(alpha = 0.4f), com.keling.app.components.HexagonShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 26.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = EarthBrown
        )
    }
}

/**
 * 标签徽章
 * 用于状态显示、分类标签
 */
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (icon != null) {
            Text(icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 进度条组件
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    showGlow: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = EaseOutCubic)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(BorderGray.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(color, color.copy(alpha = 0.7f))
                    )
                )
                .then(
                    if (showGlow) {
                        Modifier.shadow(4.dp, RoundedCornerShape(height / 2))
                    } else Modifier
                )
        )
    }
}