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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// ==================== 星球组件 ====================

/**
 * 游戏化星球组件
 * 带发光效果和旋转动画
 */
@Composable
fun GamePlanet(
    modifier: Modifier = Modifier,
    color: Color,
    size: Dp = 80.dp,
    glowIntensity: Float = 0.4f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "planet")

    // 缓慢旋转
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // 呼吸效果
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // 发光脉冲
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = glowIntensity * 0.6f,
        targetValue = glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = glowAlpha }
                .offset((size * 0.1f))
                .background(color.copy(alpha = 0.3f), CircleShape)
        )

        // 主星球
        Box(
            modifier = Modifier
                .size(size * 0.8f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 1f),
                            color.copy(alpha = 0.8f),
                            color.copy(alpha = 0.6f)
                        )
                    ),
                    CircleShape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            content = content,
            contentAlignment = Alignment.Center
        )
    }
}

// ==================== 能量条组件 ====================

/**
 * 游戏化进度条
 * 带发光效果和动画
 */
@Composable
fun GameProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = GameGradients.energyBar,
    backgroundColor: Color = Color(0xFF2C3E50),
    height: Dp = 12.dp,
    cornerRadius: Dp = 6.dp,
    showGlow: Boolean = true,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "progressBar")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        if (animatedProgress > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
                    .drawBehind {
                        if (showGlow) {
                            // 发光效果
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = gradientColors.map { it.copy(alpha = 0.5f) }
                                ),
                                size = Size(size.width, size.height * 1.5f),
                                topLeft = Offset(0f, -size.height * 0.25f)
                            )
                        }

                        // 闪光效果
                        if (animated) {
                            val shimmerWidth = size.width * 0.3f
                            val offsetX = size.width * shimmerOffset
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    startX = offsetX - shimmerWidth,
                                    endX = offsetX + shimmerWidth
                                ),
                                size = Size(shimmerWidth * 2, size.height)
                            )
                        }
                    }
            )
        }
    }
}

// ==================== 经验球组件 ====================

/**
 * 经验/等级显示球
 */
@Composable
fun ExpOrb(
    currentExp: Int,
    expToNextLevel: Int,
    level: Int,
    modifier: Modifier = Modifier,
    color: Color = StarGold
) {
    val progress = (currentExp.toFloat() / expToNextLevel).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "expOrb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 等级圆环
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .drawBehind {
                    // 背景环
                    drawArc(
                        color = Color(0xFF2C3E50),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 进度环
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.7f),
                                color
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lv.$level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 经验数字
        Text(
            text = "$currentExp / $expToNextLevel",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

// ==================== 能量显示组件 ====================

/**
 * 能量/水晶资源显示
 */
@Composable
fun ResourceDisplay(
    icon: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    showGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "resource")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF2C3E50).copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 图标背景发光
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (showGlow) {
                            Modifier.graphicsLayer { alpha = glowAlpha }
                        } else {
                            Modifier
                        }
                    )
                    .background(color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 14.sp
                )
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ==================== 游戏化按钮 ====================

/**
 * 游戏风格按钮
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = StellarOrange,
    icon: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = if (enabled) scale else 1f
                scaleY = if (enabled) scale else 1f
            },
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) color else Color(0xFF5D6D7E)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .drawBehind {
                    if (enabled) {
                        // 顶部高光
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            size = Size(size.width, size.height * 0.5f)
                        )
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = 18.sp)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ==================== 任务卡片组件 ====================

/**
 * 游戏化任务卡片
 */
@Composable
fun GameTaskCard(
    title: String,
    description: String,
    priority: Int,
    reward: Triple<Int, Int, Int>, // energy, crystals, exp
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    val priorityColor = when (priority) {
        5 -> PriorityColors.urgent
        4 -> PriorityColors.high
        3 -> PriorityColors.medium
        2 -> PriorityColors.low
        else -> PriorityColors.optional
    }

    val infiniteTransition = rememberInfiniteTransition(label = "taskCard")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (!isCompleted && priority >= 4) {
                    drawRect(
                        color = priorityColor.copy(alpha = glowAlpha * 0.3f),
                        size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                        topLeft = Offset(-4.dp.toPx(), -4.dp.toPx())
                    )
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                LifeGreen.copy(alpha = 0.15f)
            } else {
                CosmicSurface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCompleted) 2.dp else 1.dp,
            color = if (isCompleted) LifeGreen else priorityColor.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // 优先级标记
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = priorityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "P$priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // 奖励行
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RewardItem("⚡", reward.first, StellarOrange)
                RewardItem("💎", reward.second, CrystalBlue)
                RewardItem("✨", reward.third, StarGold)
            }
        }
    }
}

@Composable
private fun RewardItem(icon: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "+$value",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ==================== 浮动粒子背景 ====================

/**
 * 浮动粒子效果
 */
@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    color: Color = Color.White.copy(alpha = 0.3f)
) {
    val particles = remember { List(particleCount) { ParticleState() } }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            FloatingParticle(particle = particle, color = color)
        }
    }
}

private class ParticleState {
    val initialX = Random.nextFloat()
    val initialY = Random.nextFloat()
    val size = Random.nextInt(2, 6)
    val speed = Random.nextFloat() * 0.5f + 0.5f
    val phase = Random.nextFloat() * (2 * PI).toFloat()
}

@Composable
private fun FloatingParticle(
    particle: ParticleState,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (3000 / particle.speed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "offset"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val y = (particle.initialY + offsetY).mod(1f)
                val x = particle.initialX + sin(particle.phase + offsetY * PI.toFloat()) * 0.05f

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = particle.size.dp.toPx(),
                    center = Offset(
                        x = x * size.width,
                        y = y * size.height
                    )
                )
            }
    )
}

// ==================== 星空背景 ====================

/**
 * 星空背景效果
 */
@Composable
fun StarfieldBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 100
) {
    val stars = remember { List(starCount) { StarState() } }

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(GameGradients.starfield))
    ) {
        stars.forEach { star ->
            TwinklingStar(star = star)
        }
    }
}

private class StarState {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val size = Random.nextFloat() * 2 + 1
    val twinkleSpeed = Random.nextInt(1000, 3000)
    val brightness = Random.nextFloat() * 0.5f + 0.5f
}

@Composable
private fun TwinklingStar(star: StarState) {
    val infiniteTransition = rememberInfiniteTransition(label = "star")

    val alpha by infiniteTransition.animateFloat(
        initialValue = star.brightness * 0.3f,
        targetValue = star.brightness,
        animationSpec = infiniteRepeatable(
            animation = tween(star.twinkleSpeed, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = star.size.dp.toPx(),
                    center = Offset(
                        x = star.x * size.width,
                        y = star.y * size.height
                    )
                )
            }
    )
}