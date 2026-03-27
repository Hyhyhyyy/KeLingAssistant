/**
 * ScheduleComponents.kt
 * 课程表专用组件 - 轨道时间线、课程块、不规则形状
 */

package com.keling.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.keling.app.data.Course
import com.keling.app.data.ScheduleSlot
import com.keling.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ==================== 不规则形状定义 ====================

/**
 * 晶体形状 - 不对称的多边形
 * 用于课程卡片，打破矩形单调感
 */
val CrystalShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height

    moveTo(0f, h * 0.12f)
    cubicTo(
        w * 0.08f, 0f,
        w * 0.2f, 0f,
        w * 0.35f, h * 0.04f
    )
    lineTo(w * 0.82f, 0f)
    cubicTo(
        w * 0.95f, 0f,
        w, h * 0.08f,
        w, h * 0.18f
    )
    lineTo(w, h * 0.88f)
    cubicTo(
        w, h * 0.98f,
        w * 0.92f, h,
        w * 0.75f, h
    )
    lineTo(w * 0.15f, h)
    cubicTo(
        0f, h,
        0f, h * 0.92f,
        0f, h * 0.85f
    )
    close()
}

/**
 * 气泡形状 - 柔和的圆角矩形
 */
val BubbleShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val cornerRadius = w * 0.15f

    moveTo(cornerRadius, 0f)
    lineTo(w - cornerRadius, 0f)
    cubicTo(w, 0f, w, 0f, w, cornerRadius)
    lineTo(w, h - cornerRadius)
    cubicTo(w, h, w, h, w - cornerRadius, h)
    lineTo(cornerRadius, h)
    cubicTo(0f, h, 0f, h, 0f, h - cornerRadius)
    lineTo(0f, cornerRadius)
    cubicTo(0f, 0f, 0f, 0f, cornerRadius, 0f)
    close()
}

/**
 * 叶片形状 - 一侧圆角更大
 */
val LeafShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height

    moveTo(0f, h * 0.3f)
    cubicTo(0f, h * 0.1f, w * 0.2f, 0f, w * 0.4f, 0f)
    cubicTo(w * 0.8f, 0f, w, h * 0.2f, w, h * 0.5f)
    cubicTo(w, h * 0.8f, w * 0.8f, h, w * 0.4f, h)
    cubicTo(w * 0.2f, h, 0f, h * 0.9f, 0f, h * 0.7f)
    close()
}

// ==================== 轨道时间线组件 ====================

/**
 * 轨道时间线
 * 可视化展示课程时间安排
 */
@Composable
fun OrbitTimeline(
    slots: List<Pair<Course, ScheduleSlot>>,
    currentHour: Int,
    currentMinute: Int,
    onSlotClick: (Course, ScheduleSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 绘制主轨道
                val trackPath = Path().apply {
                    moveTo(20f, canvasHeight / 2)
                    quadraticBezierTo(
                        canvasWidth * 0.25f, canvasHeight * 0.35f,
                        canvasWidth * 0.5f, canvasHeight / 2
                    )
                    quadraticBezierTo(
                        canvasWidth * 0.75f, canvasHeight * 0.65f,
                        canvasWidth - 20f, canvasHeight / 2
                    )
                }

                drawPath(
                    path = trackPath,
                    color = MossGreen.copy(alpha = 0.1f),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                drawPath(
                    path = trackPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MossGreen.copy(alpha = 0.3f),
                            MossGreen.copy(alpha = 0.5f),
                            MossGreen.copy(alpha = 0.3f)
                        )
                    ),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                // 绘制时间节点
                slots.forEach { (course, slot) ->
                    val progress = calculateDayProgress(slot, currentHour, currentMinute)
                    val x = 40f + (canvasWidth - 80f) * progress
                    val y = canvasHeight / 2 + sin(x * 0.015f) * 15f

                    val color = Color(course.themeColor)
                    val isPast = isSlotPast(slot, currentHour, currentMinute)
                    val isCurrent = isSlotCurrent(slot, currentHour, currentMinute)

                    if (isCurrent) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                center = Offset(x, y),
                                radius = 50f
                            ),
                            radius = 50f,
                            center = Offset(x, y)
                        )
                    }

                    val nodeRadius = if (isCurrent) 18f else if (isPast) 12f else 14f
                    val nodeAlpha = if (isPast) 0.4f else 1f

                    drawCircle(
                        color = color.copy(alpha = nodeAlpha),
                        radius = nodeRadius,
                        center = Offset(x, y)
                    )

                    drawCircle(
                        color = DawnWhite.copy(alpha = nodeAlpha * 0.9f),
                        radius = nodeRadius * 0.6f,
                        center = Offset(x, y)
                    )
                }
            }
    )
}

/**
 * 课程时间线节点
 */
@Composable
fun CourseOrbitNode(
    course: Course,
    slot: ScheduleSlot,
    isCurrent: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val courseColor = Color(course.themeColor)

    val pulseScale by rememberBreathingPulse(enabled = isCurrent, maxScale = 1.12f)
    val glowAlpha by rememberGlowPulse(enabled = isCurrent, minAlpha = 0.3f, maxAlpha = 0.7f)

    val nodeSize = when {
        isCurrent -> 56.dp
        isPast -> 40.dp
        else -> 48.dp
    }

    val contentAlpha = if (isPast) 0.5f else 1f

    Box(
        modifier = modifier
            .size(nodeSize)
            .graphicsLayer {
                scaleX = if (isCurrent) pulseScale else 1f
                scaleY = if (isCurrent) pulseScale else 1f
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(12.dp)
                    .graphicsLayer { this.alpha = glowAlpha }
                    .background(courseColor, CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(courseColor.copy(alpha = contentAlpha))
                .border(
                    width = 2.dp,
                    color = DawnWhite.copy(alpha = contentAlpha * 0.8f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = course.name.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DawnWhite
            )
        }
    }
}

// ==================== 辅助函数 ====================

private fun calculateDayProgress(slot: ScheduleSlot, currentHour: Int, currentMinute: Int): Float {
    val slotStart = slot.startHour + slot.startMinute / 60f
    val dayStart = 6f
    val dayEnd = 22f
    val dayLength = dayEnd - dayStart

    return ((slotStart - dayStart) / dayLength).coerceIn(0f, 1f)
}

private fun isSlotPast(slot: ScheduleSlot, currentHour: Int, currentMinute: Int): Boolean {
    val slotEnd = slot.startHour * 60 + slot.startMinute + slot.durationMinutes
    val current = currentHour * 60 + currentMinute
    return current > slotEnd
}

private fun isSlotCurrent(slot: ScheduleSlot, currentHour: Int, currentMinute: Int): Boolean {
    val slotStart = slot.startHour * 60 + slot.startMinute
    val slotEnd = slotStart + slot.durationMinutes
    val current = currentHour * 60 + currentMinute
    return current in slotStart until slotEnd
}