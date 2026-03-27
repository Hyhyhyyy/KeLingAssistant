/**
 * Cards.kt
 * 通用卡片组件 - 统一的卡片样式和效果
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.keling.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.data.Course
import com.keling.app.data.Task
import com.keling.app.data.TaskStatus
import com.keling.app.ui.theme.*

/**
 * 卡片样式
 */
enum class CardVariant {
    Elevated,    // 带阴影
    Outlined,    // 边框
    Filled,      // 填充背景
    Gradient,    // 渐变背景
    Glowing      // 发光效果
}

/**
 * 通用卡片组件
 */
@Composable
fun KeLingCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Elevated,
    backgroundColor: Color = DawnWhite,
    accentColor: Color = StellarOrange,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val glowAlpha by rememberGlowPulse(
        enabled = variant == CardVariant.Glowing,
        minAlpha = 0.05f,
        maxAlpha = 0.15f
    )

    val shape = RoundedCornerShape(Radius.md)

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                when (variant) {
                    CardVariant.Glowing -> Modifier.shadow(8.dp, shape)
                    CardVariant.Elevated -> Modifier.shadow(4.dp, shape)
                    else -> Modifier
                }
            )
            .clip(shape)
            .then(
                when (variant) {
                    CardVariant.Gradient -> Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.15f),
                                backgroundColor
                            )
                        )
                    )
                    CardVariant.Glowing -> Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = glowAlpha),
                                backgroundColor
                            )
                        )
                    )
                    else -> Modifier.background(backgroundColor)
                }
            )
            .then(
                when (variant) {
                    CardVariant.Outlined -> Modifier.border(
                        1.5.dp,
                        accentColor.copy(alpha = 0.3f),
                        shape
                    )
                    else -> Modifier
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
            .padding(Spacing.md)
    ) {
        Column(content = content)
    }
}

/**
 * 星球卡片
 * 用于展示课程
 */
@Composable
fun PlanetCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showGlow: Boolean = true
) {
    val courseColor = Color(course.themeColor)
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val floatOffset by rememberFloatingOffset(amplitude = 4.dp, durationMs = 3000 + course.id.hashCode() % 1000)

    Card(
        modifier = modifier
            .width(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = floatOffset.toPx()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = courseColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 星球图片容器
            Box(
                modifier = Modifier
                    .size(72.dp),
                contentAlignment = Alignment.Center
                ) {
                // 发光背景
                if (showGlow) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(12.dp)
                            .background(courseColor.copy(alpha = 0.15f), CircleShape)
                    )
                }

                // 星球图片
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(courseColor.copy(alpha = 0.12f))
                        .border(1.dp, courseColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // 这里应该加载星球图片，简化为首字母
                    Text(
                        text = course.name.first().toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = courseColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = course.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = EarthBrown,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 掌握度进度条
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                GradientProgressBar(
                    progress = course.masteryLevel,
                    color = courseColor,
                    height = 4.dp,
                    showGlow = false,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(course.masteryLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = courseColor
                )
            }
        }
    }
}

/**
 * 任务简报卡片
 */
@Composable
fun TaskBriefCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        5 -> ErrorRed
        4 -> StellarOrange
        else -> MossGreen
    }

    val isCompleted = task.status == TaskStatus.COMPLETED

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    KeLingCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        variant = if (isCompleted) CardVariant.Outlined else CardVariant.Elevated,
        accentColor = priorityColor,
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧优先级指示条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            // 任务信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) EarthBrownLight else EarthBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${task.estimatedMinutes}分钟 · ${task.rewards.energy}⚡",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }

            // 完成状态
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(
                        if (isCompleted) MossGreen.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (isCompleted) MossGreen else BorderGray,
                        RoundedCornerShape(Radius.sm)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_check_complete),
                        contentDescription = "已完成",
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

/**
 * 用户头像组件
 */
@Composable
fun UserAvatar(
    name: String,
    size: Dp = 56.dp,
    backgroundColor: Color = StellarOrange,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 圆形头像
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .then(
                    if (onClick != null) Modifier.clickable { onClick() }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_user_avatar),
                contentDescription = "用户头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * 空状态组件
 */
@Composable
fun EmptyState(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = EarthBrown
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = EarthBrownLight,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(Spacing.md))
            action()
        }
    }
}