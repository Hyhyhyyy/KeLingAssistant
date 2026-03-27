package com.keling.app.ui.components

/**
 * =========================
 * 能量值动画组件
 * =========================
 *
 * 粒子特效动画
 * - 能量变化时的粒子动画
 * - 升级特效
 * - 成就解锁动画
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.keling.app.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 粒子数据类
 */
data class Particle(
    val id: Int,
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var size: Float,
    var alpha: Float,
    val color: Color,
    var life: Float = 1f
)

/**
 * 能量变化动画
 */
@Composable
fun EnergyChangeAnimation(
    isPlaying: Boolean,
    energyChange: Int,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val particles = remember { mutableStateListOf<Particle>() }
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && !animationPlayed) {
            animationPlayed = true
            // 生成粒子
            val particleCount = minOf(abs(energyChange) * 3, 50)
            val isPositive = energyChange > 0

            repeat(particleCount) { index ->
                particles.add(
                    Particle(
                        id = index,
                        x = 0.5f,
                        y = 0.5f,
                        velocityX = Random.nextFloat() * 4 - 2,
                        velocityY = Random.nextFloat() * 4 - 2,
                        size = Random.nextFloat() * 8 + 4,
                        alpha = 1f,
                        color = if (isPositive) WarmSunOrange else RoseRed,
                        life = Random.nextFloat() * 0.5f + 0.5f
                    )
                )
            }

            // 动画更新
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2000) {
                particles.forEach { particle ->
                    particle.x += particle.velocityX * 0.01f
                    particle.y += particle.velocityY * 0.01f
                    particle.alpha -= 0.02f
                    particle.life -= 0.02f
                }
                particles.removeAll { it.life <= 0 }
                kotlinx.coroutines.delay(16)
            }

            particles.clear()
            onAnimationEnd()
            animationPlayed = false
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha.coerceIn(0f, 1f)),
                radius = particle.size,
                center = Offset(
                    size.width * particle.x,
                    size.height * particle.y
                )
            )
        }
    }
}

/**
 * 升级特效动画
 */
@Composable
fun LevelUpAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val particles = remember { mutableStateListOf<Particle>() }
    val infiniteTransition = rememberInfiniteTransition(label = "levelUp")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // 生成金色星星粒子
            repeat(60) { index ->
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                val speed = Random.nextFloat() * 5 + 2

                particles.add(
                    Particle(
                        id = index,
                        x = 0.5f,
                        y = 0.5f,
                        velocityX = cos(angle) * speed * 0.01f,
                        velocityY = sin(angle) * speed * 0.01f,
                        size = Random.nextFloat() * 12 + 6,
                        alpha = 1f,
                        color = listOf(
                            CreamYellow,
                            WarmSunOrange,
                            StellarOrange,
                            Color(0xFFFFD700)
                        ).random(),
                        life = 1f
                    )
                )
            }

            // 动画更新
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 3000) {
                particles.forEach { particle ->
                    particle.x += particle.velocityX
                    particle.y += particle.velocityY
                    particle.velocityY += 0.001f // 重力
                    particle.alpha -= 0.015f
                    particle.life -= 0.015f
                }
                particles.removeAll { it.life <= 0 }
                kotlinx.coroutines.delay(16)
            }

            particles.clear()
            onAnimationEnd()
        }
    }

    Canvas(modifier = modifier) {
        // 中心光晕
        if (isPlaying) {
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        CreamYellow.copy(alpha = glowAlpha * 0.8f),
                        WarmSunOrange.copy(alpha = glowAlpha * 0.4f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.3f,
                center = center
            )
        }

        // 粒子
        particles.forEach { particle ->
            drawStar(
                color = particle.color.copy(alpha = particle.alpha.coerceIn(0f, 1f)),
                center = Offset(
                    size.width * particle.x,
                    size.height * particle.y
                ),
                size = particle.size
            )
        }
    }
}

/**
 * 成就解锁动画
 */
@Composable
fun AchievementUnlockAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val particles = remember { mutableStateListOf<Particle>() }
    var scale by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // 缩放动画
            scale = 0f
            while (scale < 1f) {
                scale += 0.05f
                kotlinx.coroutines.delay(16)
            }

            // 生成彩虹粒子
            val rainbowColors = listOf(
                MintGreen,
                SkyBlue,
                LavenderPurple,
                CreamYellow,
                WarmSunOrange,
                RoseRed
            )

            repeat(50) { index ->
                val angle = (index / 50f) * 2 * PI.toFloat()
                particles.add(
                    Particle(
                        id = index,
                        x = 0.5f,
                        y = 0.5f,
                        velocityX = cos(angle) * 0.02f,
                        velocityY = sin(angle) * 0.02f,
                        size = Random.nextFloat() * 8 + 4,
                        alpha = 1f,
                        color = rainbowColors[index % rainbowColors.size],
                        life = 1f
                    )
                )
            }

            // 粒子扩散
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 1500) {
                particles.forEach { particle ->
                    particle.x += particle.velocityX
                    particle.y += particle.velocityY
                    particle.alpha -= 0.02f
                    particle.life -= 0.02f
                }
                particles.removeAll { it.life <= 0 }
                kotlinx.coroutines.delay(16)
            }

            particles.clear()
            scale = 0f
            onAnimationEnd()
        }
    }

    Canvas(modifier = modifier) {
        // 中心光环
        if (isPlaying && scale > 0) {
            drawCircle(
                color = CreamYellow.copy(alpha = 0.3f * scale),
                radius = size.minDimension * 0.2f * scale,
                center = center
            )
        }

        // 粒子
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha.coerceIn(0f, 1f)),
                radius = particle.size,
                center = Offset(
                    size.width * particle.x,
                    size.height * particle.y
                )
            )
        }
    }
}

/**
 * 连续签到庆祝动画
 */
@Composable
fun StreakCelebrationAnimation(
    isPlaying: Boolean,
    streakDays: Int,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // 根据连续天数调整粒子数量
            val particleCount = minOf(streakDays * 5, 100)

            repeat(particleCount) { index ->
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                val speed = Random.nextFloat() * 8 + 4

                particles.add(
                    Particle(
                        id = index,
                        x = Random.nextFloat(),
                        y = 1.2f, // 从底部开始
                        velocityX = cos(angle) * speed * 0.005f,
                        velocityY = -speed * 0.01f, // 向上
                        size = Random.nextFloat() * 10 + 5,
                        alpha = 1f,
                        color = when {
                            streakDays >= 30 -> Color(0xFFFFD700) // 金色
                            streakDays >= 14 -> CreamYellow
                            streakDays >= 7 -> WarmSunOrange
                            else -> StellarOrange
                        },
                        life = 1f
                    )
                )
            }

            // 动画更新（烟花效果）
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2500) {
                particles.forEach { particle ->
                    particle.x += particle.velocityX
                    particle.y += particle.velocityY
                    particle.velocityY += 0.0005f // 重力
                    particle.alpha -= 0.012f
                    particle.life -= 0.012f
                }
                particles.removeAll { it.life <= 0 || it.y > 1.5f }
                kotlinx.coroutines.delay(16)
            }

            particles.clear()
            onAnimationEnd()
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            drawStar(
                color = particle.color.copy(alpha = particle.alpha.coerceIn(0f, 1f)),
                center = Offset(
                    size.width * particle.x,
                    size.height * particle.y
                ),
                size = particle.size
            )
        }
    }
}

/**
 * 绘制星星形状
 */
private fun DrawScope.drawStar(
    color: Color,
    center: Offset,
    size: Float
) {
    val points = 5
    val innerRadius = size * 0.4f
    val outerRadius = size

    val path = androidx.compose.ui.graphics.Path()
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * PI / points - PI / 2).toFloat()
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = color
    )
}

/**
 * 能量动画容器组件
 */
@Composable
fun EnergyAnimationOverlay(
    showEnergyChange: Boolean = false,
    energyChange: Int = 0,
    showLevelUp: Boolean = false,
    showAchievement: Boolean = false,
    showStreak: Boolean = false,
    streakDays: Int = 0,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    Box(modifier = modifier) {
        // 能量变化动画
        if (showEnergyChange) {
            EnergyChangeAnimation(
                isPlaying = showEnergyChange,
                energyChange = energyChange,
                modifier = Modifier.matchParentSize(),
                onAnimationEnd = onAnimationEnd
            )
        }

        // 升级动画
        if (showLevelUp) {
            LevelUpAnimation(
                isPlaying = showLevelUp,
                modifier = Modifier.matchParentSize(),
                onAnimationEnd = onAnimationEnd
            )
        }

        // 成就解锁动画
        if (showAchievement) {
            AchievementUnlockAnimation(
                isPlaying = showAchievement,
                modifier = Modifier.matchParentSize(),
                onAnimationEnd = onAnimationEnd
            )
        }

        // 连续签到庆祝
        if (showStreak) {
            StreakCelebrationAnimation(
                isPlaying = showStreak,
                streakDays = streakDays,
                modifier = Modifier.matchParentSize(),
                onAnimationEnd = onAnimationEnd
            )
        }
    }
}