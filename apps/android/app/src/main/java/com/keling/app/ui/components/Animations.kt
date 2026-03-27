/**
 * Animations.kt
 * 动画效果库 - 统一的动画规范和可复用的动画效果
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.keling.app.ui.theme.*

/**
 * 动画时长规范
 */
object AnimationDuration {
    const val instant = 50
    const val fast = 150
    const val normal = 300
    const val slow = 500
    const val verySlow = 800
    const val extraSlow = 1200
}

/**
 * 动画延迟规范
 */
object AnimationDelay {
    const val staggerShort = 50L
    const val staggerMedium = 100L
    const val staggerLong = 150L
}

/**
 * 预设动画规格
 */
object AnimationSpecs {
    /** 快速弹性 */
    val springQuick = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** 柔和弹性 */
    val springSoft = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    /** 平滑缓动 */
    val smooth = tween<Float>(
        durationMillis = AnimationDuration.normal,
        easing = FastOutSlowInEasing
    )

    /** 快速淡入淡出 */
    val fastFade = tween<Float>(
        durationMillis = AnimationDuration.fast,
        easing = LinearEasing
    )

    /** 缓慢呼吸 */
    val breathing = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 2000,
            easing = EaseInOutSine
        ),
        repeatMode = RepeatMode.Reverse
    )

    /** 脉冲效果 */
    val pulse = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 1000,
            easing = EaseInOutSine
        ),
        repeatMode = RepeatMode.Reverse
    )

    /** 缓慢旋转 */
    val slowRotate = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 20000,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
    )
}

/**
 * 呼吸脉冲效果
 * 用于当前课程、重要元素的高亮
 */
@Composable
fun rememberBreathingPulse(
    enabled: Boolean = true,
    minScale: Float = 1f,
    maxScale: Float = 1.08f
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition()

    return infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = AnimationSpecs.pulse
    )
}

/**
 * 发光脉冲Alpha值
 * 用于元素的光晕效果
 */
@Composable
fun rememberGlowPulse(
    enabled: Boolean = true,
    minAlpha: Float = 0.2f,
    maxAlpha: Float = 0.6f
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition()

    return if (enabled) {
        infiniteTransition.animateFloat(
            initialValue = minAlpha,
            targetValue = maxAlpha,
            animationSpec = AnimationSpecs.breathing
        )
    } else {
        remember { mutableStateOf(0f) }
    }
}

/**
 * 交错入场动画状态
 * 用于列表项依次出现
 */
@Composable
fun rememberStaggeredVisibility(
    index: Int,
    staggerDelay: Long = AnimationDelay.staggerMedium
): Boolean {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * staggerDelay)
        visible = true
    }

    return visible
}

/**
 * 闪烁效果（用于加载状态）
 */
@Composable
fun rememberShimmerProgress(): Float {
    val infiniteTransition = rememberInfiniteTransition()

    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    ).value
}

/**
 * 星球浮动效果
 * 缓慢的上下浮动，营造悬浮感
 */
@Composable
fun rememberFloatingOffset(
    amplitude: Dp = 6.dp,
    durationMs: Int = 3000
): State<Dp> {
    val infiniteTransition = rememberInfiniteTransition()

    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    return derivedStateOf { amplitude * progress.value }
}