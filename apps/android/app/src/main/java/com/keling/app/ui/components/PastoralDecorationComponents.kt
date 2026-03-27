package com.keling.app.ui.components

/**
 * =========================
 * 田园治愈风装饰组件库
 * PastoralDecorationComponents.kt
 * =========================
 *
 * 包含：
 * - 漂浮花瓣/叶子动画
 * - 光晕/辉光效果
 * - 波浪/曲线边框
 * - 装饰性背景图案
 * - 游戏化按钮样式
 * - 进度条动画
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

// ==================== 漂浮装饰粒子 ====================

/**
 * 漂浮花瓣数据类
 */
data class FloatingPetal(
    val x: Float = Random.nextFloat(),
    val startY: Float = -0.1f - Random.nextFloat() * 0.3f,
    val size: Int = Random.nextInt(6, 14),
    val speed: Int = Random.nextInt(12000, 20000),
    val alpha: Float = Random.nextFloat() * 0.12f + 0.05f,
    val color: Color = listOf(
        WarmSunOrange, PeachPink, CreamYellow,
        LavenderPurple, MintGreen, SkyBlue
    ).random(),
    val rotation: Float = Random.nextFloat() * 360f,
    val swayAmplitude: Float = Random.nextFloat() * 40f + 20f,
    val swaySpeed: Int = Random.nextInt(2000, 4000)
)

/**
 * 漂浮花瓣动画组件
 */
@Composable
fun FloatingPetalsBackground(
    modifier: Modifier = Modifier,
    petalCount: Int = 12
) {
    val petals = remember { List(petalCount) { FloatingPetal() } }

    Box(modifier = modifier) {
        petals.forEach { petal ->
            AnimatedFloatingPetal(petal = petal)
        }
    }
}

@Composable
private fun AnimatedFloatingPetal(petal: FloatingPetal) {
    val infiniteTransition = rememberInfiniteTransition(label = "petal")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(petal.speed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val sway by infiniteTransition.animateFloat(
        initialValue = -petal.swayAmplitude,
        targetValue = petal.swayAmplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(petal.swaySpeed, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = petal.rotation,
        targetValue = petal.rotation + 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(petal.speed / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val y = petal.startY + (1.2f - petal.startY) * progress
        val x = petal.x + sway / size.width

        // 绘制花瓣形状
        rotate(rotation, pivot = Offset(x * size.width, y * size.height)) {
            drawPath(
                path = createPetalPath(
                    center = Offset(x * size.width, y * size.height),
                    size = petal.size.dp.toPx()
                ),
                color = petal.color.copy(alpha = petal.alpha),
                style = Fill
            )
        }
    }
}

private fun createPetalPath(center: Offset, size: Float): Path {
    return Path().apply {
        moveTo(center.x, center.y - size)
        cubicTo(
            center.x + size * 0.8f, center.y - size * 0.5f,
            center.x + size * 0.8f, center.y + size * 0.5f,
            center.x, center.y + size * 0.3f
        )
        cubicTo(
            center.x - size * 0.8f, center.y + size * 0.5f,
            center.x - size * 0.8f, center.y - size * 0.5f,
            center.x, center.y - size
        )
        close()
    }
}

// ==================== 光晕效果 ====================

/**
 * 脉动光晕背景
 */
@Composable
fun PulsingGlowBackground(
    modifier: Modifier = Modifier,
    color: Color = WarmSunOrange,
    intensity: Float = 0.15f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = intensity * 0.5f,
        targetValue = intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = glowAlpha),
                            color.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                        radius = size.maxDimension * 0.8f
                    )
                )
            }
    )
}

/**
 * 多层光晕效果
 */
@Composable
fun MultiLayerGlow(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(WarmSunOrange, PeachPink, CreamYellow)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "multiGlow")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        colors.forEachIndexed { index, color ->
            val offset = index * 120f
            val x = size.width * 0.5f + cos((phase + offset) * PI / 180f).toFloat() * size.width * 0.2f
            val y = size.height * 0.5f + sin((phase + offset) * PI / 180f).toFloat() * size.height * 0.2f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.12f),
                        color.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = size.maxDimension * 0.5f
                )
            )
        }
    }
}

// ==================== 波浪边框 ====================

/**
 * 波浪边框卡片
 */
@Composable
fun WavyBorderCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CreamWhite,
    borderColor: Color = WarmSunOrange.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .drawBehind {
                // 波浪边框
                val waveHeight = 3.dp.toPx()
                val waveLength = 30.dp.toPx()

                drawRoundRect(
                    color = backgroundColor,
                    cornerRadius = CornerRadius(20.dp.toPx())
                )

                // 顶部波浪
                val path = Path()
                path.moveTo(0f, 0f)
                var x = 0f
                while (x < size.width) {
                    val y = sin((x / waveLength + wavePhase * 2 * PI.toFloat()) * 2 * PI.toFloat()) * waveHeight
                    path.lineTo(x, y)
                    x += 2.dp.toPx()
                }
                path.lineTo(size.width, 0f)

                drawPath(
                    path = path,
                    color = borderColor,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            .padding(1.dp)
    ) {
        content()
    }
}

// ==================== 装饰性图案 ====================

/**
 * 点阵背景图案
 */
@Composable
fun DottedPatternBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = WarmSunOrange.copy(alpha = 0.08f),
    dotSize: Dp = 3.dp,
    spacing: Dp = 24.dp
) {
    Canvas(modifier = modifier) {
        val dotRadius = dotSize.toPx() / 2
        val spacingPx = spacing.toPx()

        var x = spacingPx
        while (x < size.width) {
            var y = spacingPx
            while (y < size.height) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                y += spacingPx
            }
            x += spacingPx
        }
    }
}

/**
 * 斜线背景图案
 */
@Composable
fun DiagonalLinesBackground(
    modifier: Modifier = Modifier,
    lineColor: Color = WarmSunOrange.copy(alpha = 0.04f),
    spacing: Dp = 16.dp
) {
    Canvas(modifier = modifier) {
        val spacingPx = spacing.toPx()
        val lineWidth = 1.dp.toPx()

        var offset = -size.height
        while (offset < size.width + size.height) {
            drawLine(
                color = lineColor,
                start = Offset(offset, 0f),
                end = Offset(offset + size.height, size.height),
                strokeWidth = lineWidth
            )
            offset += spacingPx
        }
    }
}

/**
 * 几何图案背景
 */
@Composable
fun GeometricPatternBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "geo")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        // 绘制装饰性六边形
        val hexSize = 60.dp.toPx()
        val hexSpacing = 80.dp.toPx()

        var x = hexSpacing / 2
        var row = 0
        while (x < size.width + hexSize) {
            var y = hexSpacing / 2 + if (row % 2 == 1) hexSpacing / 2 else 0f
            while (y < size.height + hexSize) {
                drawPath(
                    path = createHexagonPath(Offset(x, y), hexSize / 2),
                    color = WarmSunOrange.copy(alpha = 0.03f),
                    style = Stroke(width = 1.dp.toPx())
                )
                y += hexSpacing
            }
            x += hexSpacing * 0.866f // cos(30°)
            row++
        }
    }
}

private fun createHexagonPath(center: Offset, radius: Float): Path {
    return Path().apply {
        for (i in 0..5) {
            val angle = i * 60f * PI / 180f
            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

// ==================== 游戏化按钮样式 ====================

/**
 * 游戏化主按钮
 */
@Composable
fun GamePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btn")

    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .drawBehind {
                // 光泽效果
                val shimmerWidth = size.width * 0.3f
                val shimmerX = size.width * (shimmer + 1) / 2

                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        startX = shimmerX - shimmerWidth,
                        endX = shimmerX + shimmerWidth
                    )
                )
            },
        shape = RoundedCornerShape(24.dp),
        color = if (enabled) WarmSunOrange else EarthBrown.copy(alpha = 0.3f),
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * 游戏化次要按钮
 */
@Composable
fun GameSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = WarmSunOrange
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = CreamWhite
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = borderColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 游戏化图标按钮
 */
@Composable
fun GameIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = CreamWhite,
    iconColor: Color = EarthBrown,
    size: Dp = 44.dp
) {
    val interactionSource = remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = (size.value * 0.4f).sp,
                color = iconColor
            )
        }
    }
}

// ==================== 游戏化进度条 ====================

/**
 * 游戏化进度条
 */
@Composable
fun GameProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    backgroundColor: Color = BeigeSurface,
    progressColors: List<Color> = listOf(MintGreen, MintGreenDark),
    showGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
    ) {
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .drawBehind {
                    if (showGlow) {
                        drawRoundRect(
                            color = progressColors.first().copy(alpha = glowAlpha * 0.4f),
                            cornerRadius = CornerRadius(height.toPx() / 2),
                            size = Size(size.width + 8.dp.toPx(), size.height + 4.dp.toPx()),
                            topLeft = Offset(-4.dp.toPx(), -2.dp.toPx())
                        )
                    }
                }
                .background(
                    Brush.horizontalGradient(progressColors),
                    RoundedCornerShape(height / 2)
                )
        ) {
            // 光泽效果
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val shimmerWidth = size.width * 0.4f
                        val shimmerX = size.width * shimmer

                        if (progress > 0.1f) {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    startX = shimmerX - shimmerWidth,
                                    endX = shimmerX + shimmerWidth
                                )
                            )
                        }
                    }
            )
        }
    }
}

/**
 * 圆形进度条
 */
@Composable
fun CircularProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    backgroundColor: Color = BeigeSurface,
    progressColor: Color = MintGreen,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size)
        ) {
            // 背景圆
            drawCircle(
                color = backgroundColor,
                radius = (size - strokeWidth).toPx() / 2,
                style = Stroke(width = strokeWidth.toPx())
            )

            // 进度圆弧
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(progressColor, progressColor.copy(alpha = 0.7f)),
                    center = Offset(size.toPx() / 2, size.toPx() / 2)
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        content()
    }
}

// ==================== 装饰性标题 ====================

/**
 * 游戏化标题
 */
@Composable
fun GameTitle(
    text: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = WarmSunOrange
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 装饰线
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(accentColor, RoundedCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "✿",
                fontSize = 14.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.dp)
                    .background(accentColor, RoundedCornerShape(1.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = EarthBrown,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor
            )
        }
    }
}

/**
 * 装饰性分隔符
 */
@Composable
fun DecorativeDivider(
    modifier: Modifier = Modifier,
    color: Color = WarmSunOrange.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, color)
                    )
                )
        )
        Text(
            text = " ✿ ",
            color = color,
            fontSize = 12.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(color, Color.Transparent)
                    )
                )
        )
    }
}

// ==================== 统计卡片 ====================

/**
 * 游戏化统计卡片
 */
@Composable
fun GameStatCard(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = WarmSunOrange
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stat")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(16.dp),
        color = CreamWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标背景
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.15f),
                                    accentColor.copy(alpha = 0.05f)
                                ),
                                center = Offset(size.width * 0.3f, size.height * 0.3f),
                                radius = size.maxDimension
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrown.copy(alpha = 0.6f)
            )
        }
    }
}

// ==================== 徽章/标签组件 ====================

/**
 * 游戏化徽章
 */
@Composable
fun GameBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WarmSunOrange.copy(alpha = 0.15f),
    textColor: Color = WarmSunOrange
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/**
 * 奖励显示组件
 */
@Composable
fun RewardDisplay(
    icon: String,
    value: Int,
    modifier: Modifier = Modifier,
    color: Color = WarmSunOrange
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+$value",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== 空状态组件 ====================

/**
 * 游戏化空状态
 */
@Composable
fun GameEmptyState(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = floatOffset }
                    .size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                // 光晕
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(WarmSunOrange, CircleShape)
                )

                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = WarmSunOrange.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon,
                            fontSize = 36.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrown.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(20.dp))

                GamePrimaryButton(
                    text = actionText,
                    onClick = onAction,
                    icon = "✨"
                )
            }
        }
    }
}