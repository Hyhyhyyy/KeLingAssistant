/**
 * BackgroundEffects.kt
 * 背景效果组件 - 星云、渐变、粒子等
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.keling.app.R
import com.keling.app.ui.theme.*
import kotlin.random.Random

/**
 * 统一页面背景
 * 使用背景图片作为所有页面的背景
 */
@Composable
fun NebulaBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = MossGreen,
    secondaryColor: Color = StellarOrange,
    intensity: Float = 0.08f
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

/**
 * 深色星云背景
 * 用于深色主题
 */
@Composable
fun DarkNebulaBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

/**
 * 星点装饰背景
 * 随机分布的小星点
 */
@Composable
fun StarDustBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 30,
    starColor: Color = DawnWhite
) {
    // 使用固定的随机种子，确保星点位置稳定
    val stars = remember(starCount) {
        (0 until starCount).map {
            Random(it * 17 + 31).nextFloat() to Random(it * 23 + 47).nextFloat()
        }
    }

    val infiniteTransition = rememberInfiniteTransition()

    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.drawBehind {
            stars.forEachIndexed { index, (xRatio, yRatio) ->
                val x = size.width * xRatio
                val y = size.height * yRatio
                val alpha = twinkle * (0.5f + Random(index * 7).nextFloat() * 0.5f)
                val radius = 1f + Random(index * 11).nextFloat() * 2f

                drawCircle(
                    color = starColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    )
}

/**
 * 能量光环背景
 * 用于当前课程、重要提示等
 */
@Composable
fun EnergyGlowBackground(
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val glowAlpha by rememberGlowPulse(enabled = enabled, minAlpha = 0.1f, maxAlpha = 0.25f)

    Box(
        modifier = modifier.drawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha),
                        color.copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.maxDimension * 0.6f
                ),
                radius = size.maxDimension * 0.6f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
        }
    )
}

/**
 * 波纹背景
 * 同心圆渐变效果
 */
@Composable
fun RippleBackground(
    color: Color,
    modifier: Modifier = Modifier,
    rippleCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition()

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.drawBehind {
            val centerX = size.width * 0.5f
            val centerY = size.height * 0.5f
            val maxRadius = size.maxDimension * 0.8f

            (0 until rippleCount).forEach { i ->
                val baseRadius = maxRadius * (i.toFloat() / rippleCount)
                val animatedRadius = baseRadius + maxRadius * progress * 0.3f
                val alpha = (1f - progress) * 0.1f * (1f - i.toFloat() / rippleCount)

                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = animatedRadius,
                    center = Offset(centerX, centerY)
                )
            }
        }
    )
}

/**
 * 渐变分割线
 */
@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    color: Color = BorderGray,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.5f),
                        color,
                        color.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}