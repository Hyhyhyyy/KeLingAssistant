/**
 * AdvancedDesignEffects.kt
 * 高级UI设计效果组件
 * 包含渐变、发光、模糊、动画等高级视觉效果
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.keling.app.ui.theme.*

// ==================== 高级阴影效果 ====================

/**
 * 多层阴影效果
 * 创造深度感
 */
fun Modifier.multiLayerShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    cornerRadius: Dp = 16.dp,
    shadowRadius: Dp = 20.dp,
    layers: Int = 3
): Modifier = this.then(
    Modifier.drawBehind {
        val spread = shadowRadius.toPx() / layers
        repeat(layers) { layer ->
            drawRoundRect(
                color = color.copy(alpha = color.alpha / (layer + 1)),
                topLeft = Offset(spread * (layer + 1) / 2, spread * (layer + 1) / 2),
                size = Size(size.width - spread * (layer + 1), size.height - spread * (layer + 1)),
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )
        }
    }
)

/**
 * 内发光效果
 */
fun Modifier.innerGlow(
    color: Color,
    radius: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = this.then(
    Modifier.drawBehind {
        // 简化的内发光效果
        drawRoundRect(
            color = color.copy(alpha = 0.3f),
            cornerRadius = CornerRadius(radius.toPx())
        )
    }
)

// ==================== 高级渐变效果 ====================

/**
 * 动态渐变背景
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(WarmSunOrangeLight, CreamWhite, MintGreenLight),
    durationMs: Int = 8000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = colors,
                start = Offset(0f, offset * 1000),
                end = Offset(offset * 1000 + 1000, 0f)
            )
        )
    )
}

/**
 * 玻璃态卡片效果
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.7f),
    borderGradient: List<Color> = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f)),
    cornerRadius: Dp = 20.dp,
    blurRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .blur(blurRadius)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(borderGradient),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

// ==================== 发光效果组件 ====================

/**
 * 发光圆形背景
 */
@Composable
fun GlowingCircle(
    modifier: Modifier = Modifier,
    color: Color = WarmSunOrange,
    size: Dp = 80.dp,
    glowRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .background(
                color.copy(alpha = glowAlpha),
                CircleShape
            )
            .blur(glowRadius),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size - glowRadius)
                .background(color, CircleShape),
            content = content
        )
    }
}

/**
 * 发光卡片
 */
@Composable
fun GlowingCard(
    modifier: Modifier = Modifier,
    glowColor: Color = WarmSunOrange.copy(alpha = 0.3f),
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(cornerRadius))
            .drawBehind {
                drawRoundRect(
                    color = glowColor,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
            .blur(1.dp),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = CreamWhite
        ),
        content = content
    )
}

// ==================== 高级文字效果 ====================

/**
 * 渐变文字
 */
@Composable
fun GradientText(
    text: String,
    style: TextStyle,
    gradientColors: List<Color> = listOf(WarmSunOrange, RoseRed),
    modifier: Modifier = Modifier
) {
    val brush = remember { Brush.linearGradient(gradientColors) }

    Text(
        text = text,
        style = style.merge(TextStyle(brush = brush)),
        modifier = modifier
    )
}

/**
 * 描边文字
 */
@Composable
fun OutlinedText(
    text: String,
    style: TextStyle,
    strokeColor: Color = WarmSunOrange,
    strokeWidth: Float = 2f,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style.merge(
            TextStyle(
                drawStyle = Stroke(width = strokeWidth),
                color = strokeColor
            )
        ),
        modifier = modifier
    )
}

// ==================== 高级按钮效果 ====================

/**
 * 渐变按钮
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(WarmSunOrange, WarmSunOrangeDark),
    cornerRadius: Dp = 12.dp,
    elevation: Dp = 4.dp,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(gradientColors),
                RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        content = content
    )
}

/**
 * 发光按钮
 */
@Composable
fun GlowingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = WarmSunOrange.copy(alpha = 0.5f),
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val glowSize by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 8.dp,
        animationSpec = tween(200),
        label = "glow"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .drawBehind {
                if (isPressed) {
                    drawRoundRect(
                        color = glowColor,
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            }
            .blur(if (isPressed) 2.dp else 0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WarmSunOrange
        ),
        content = content
    )
}

// ==================== 高级进度条效果 ====================

/**
 * 发光进度条
 */
@Composable
fun GlowingLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = WarmSunOrange,
    trackColor: Color = WarmGrayLight,
    cornerRadius: Dp = 8.dp,
    height: Dp = 8.dp,
    glowRadius: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    Brush.linearGradient(
                        listOf(color, color.copy(alpha = 0.8f))
                    ),
                    RoundedCornerShape(cornerRadius)
                )
                .blur(glowRadius)
        )
    }
}

/**
 * 圆形进度指示器
 */
@Composable
fun CircularGlowProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = WarmSunOrange,
    strokeWidth: Dp = 4.dp,
    size: Dp = 48.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = WarmGrayLight
        )
    }
}

// ==================== 装饰效果 ====================

/**
 * 星光闪烁效果
 */
@Composable
fun SparkleEffect(
    modifier: Modifier = Modifier,
    color: Color = CreamYellow,
    size: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size * scale)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

/**
 * 浮动装饰元素
 */
@Composable
fun FloatingDecoration(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier.offset(y = offsetY.dp)
    ) {
        content()
    }
}

/**
 * 脉冲动画效果
 */
@Composable
fun PulsingEffect(
    modifier: Modifier = Modifier,
    pulseColor: Color = WarmSunOrange.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(scale)
                .background(pulseColor.copy(alpha = alpha), CircleShape)
        )
        content()
    }
}