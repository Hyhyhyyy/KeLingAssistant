package com.keling.app.ui.components

/**
 * =========================
 * 番茄钟组件
 * =========================
 *
 * 25分钟工作 + 5分钟休息的计时器
 * 支持自定义时长
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.PomodoroSettings
import com.keling.app.data.Task
import com.keling.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.min

/**
 * 番茄钟主组件
 */
@Composable
fun PomodoroTimer(
    task: Task?,
    timeLeft: Int,           // 秒
    isRunning: Boolean,
    isBreak: Boolean,
    sessionCount: Int,
    settings: PomodoroSettings,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTime = if (isBreak) {
        if (sessionCount % settings.sessionsBeforeLongBreak == 0) {
            settings.longBreakMinutes * 60
        } else {
            settings.shortBreakMinutes * 60
        }
    } else {
        settings.focusMinutes * 60
    }

    val progress = timeLeft.toFloat() / totalTime.toFloat()

    // 自动完成检测
    LaunchedEffect(timeLeft, isRunning) {
        if (isRunning && timeLeft <= 0) {
            onComplete()
        }
    }

    Box(
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_page_theme),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 任务信息
            if (task != null) {
                TaskInfoCard(task = task)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 计时器圆环
            TimerCircle(
                timeLeft = timeLeft,
                progress = progress,
                isBreak = isBreak,
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 状态文字
            TimerStatusText(
                isBreak = isBreak,
                sessionCount = sessionCount,
                settings = settings
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 控制按钮
            TimerControls(
                isRunning = isRunning,
                onStart = onStart,
                onPause = onPause,
                onReset = onReset
            )

            Spacer(modifier = Modifier.weight(1f))

            // 设置提示
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BeigeSurface.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "专注${settings.focusMinutes}分钟 → 休息${settings.shortBreakMinutes}分钟",
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ==================== 计时器圆环 ====================

@Composable
private fun TimerCircle(
    timeLeft: Int,
    progress: Float,
    isBreak: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timer")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // 颜色
    val progressColor = if (isBreak) MintGreen else StellarOrange
    val backgroundColor = WarmGray.copy(alpha = 0.2f)

    // 格式化时间
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            // 背景圆环
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
            )

            // 进度圆环
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
            )
        }

        // 中间文字
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp
            )

            if (isBreak) {
                Text(
                    text = "休息时间 ☕",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintGreen
                )
            } else {
                Text(
                    text = "专注中 🎯",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StellarOrange
                )
            }
        }
    }
}

// ==================== 任务信息卡片 ====================

@Composable
private fun TaskInfoCard(task: Task) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CreamWhite.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 任务类型图标
            val icon = when (task.type) {
                com.keling.app.data.TaskType.DAILY_CARE -> "🌱"
                com.keling.app.data.TaskType.DEEP_EXPLORATION -> "🔬"
                com.keling.app.data.TaskType.REVIEW_RITUAL -> "🔄"
                com.keling.app.data.TaskType.BOUNTY -> "🎯"
                com.keling.app.data.TaskType.RESCUE -> "🆘"
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StellarOrange.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "预计 ${task.estimatedMinutes} 分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }

            // 奖励预览
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "+${task.rewards.energy}⚡",
                    style = MaterialTheme.typography.labelMedium,
                    color = StellarOrange
                )
                Text(
                    text = "+${task.rewards.crystals}💎",
                    style = MaterialTheme.typography.labelMedium,
                    color = LavenderPurple
                )
            }
        }
    }
}

// ==================== 状态文字 ====================

@Composable
private fun TimerStatusText(
    isBreak: Boolean,
    sessionCount: Int,
    settings: PomodoroSettings
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isBreak) {
            val breakType = if (sessionCount % settings.sessionsBeforeLongBreak == 0) {
                "长休息"
            } else {
                "短休息"
            }
            Text(
                text = "$breakType 时间",
                style = MaterialTheme.typography.titleMedium,
                color = MintGreen,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "第 ${sessionCount + 1} 个番茄",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
        }

        // 番茄计数器
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            repeat(settings.sessionsBeforeLongBreak) { index ->
                val isCompleted = index < sessionCount % settings.sessionsBeforeLongBreak
                val isCurrent = index == sessionCount % settings.sessionsBeforeLongBreak && !isBreak

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> StellarOrange
                                isCurrent -> StellarOrange.copy(alpha = 0.5f)
                                else -> WarmGray.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }
    }
}

// ==================== 控制按钮 ====================

@Composable
private fun TimerControls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 重置按钮
        Surface(
            onClick = onReset,
            shape = CircleShape,
            color = WarmGray.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↺",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EarthBrown
                )
            }
        }

        // 开始/暂停按钮
        Surface(
            onClick = if (isRunning) onPause else onStart,
            shape = CircleShape,
            color = if (isRunning) RoseRed else MintGreen,
            modifier = Modifier.size(72.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "⏸" else "▶",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontSize = 32.sp
                )
            }
        }

        // 占位（保持对称）
        Spacer(modifier = Modifier.size(56.dp))
    }
}

// ==================== 番茄钟设置对话框 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsDialog(
    settings: PomodoroSettings,
    onDismiss: () -> Unit,
    onSave: (PomodoroSettings) -> Unit
) {
    var focusMinutes by remember { mutableStateOf(settings.focusMinutes) }
    var shortBreakMinutes by remember { mutableStateOf(settings.shortBreakMinutes) }
    var longBreakMinutes by remember { mutableStateOf(settings.longBreakMinutes) }
    var sessionsBeforeLongBreak by remember { mutableStateOf(settings.sessionsBeforeLongBreak) }

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CreamWhite
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "番茄钟设置",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 专注时长
                SettingSlider(
                    label = "专注时长",
                    value = focusMinutes,
                    range = 5..60,
                    unit = "分钟",
                    onValueChange = { focusMinutes = it }
                )

                // 短休息
                SettingSlider(
                    label = "短休息",
                    value = shortBreakMinutes,
                    range = 1..15,
                    unit = "分钟",
                    onValueChange = { shortBreakMinutes = it }
                )

                // 长休息
                SettingSlider(
                    label = "长休息",
                    value = longBreakMinutes,
                    range = 10..30,
                    unit = "分钟",
                    onValueChange = { longBreakMinutes = it }
                )

                // 番茄数
                SettingSlider(
                    label = "长休息间隔",
                    value = sessionsBeforeLongBreak,
                    range = 2..6,
                    unit = "个番茄",
                    onValueChange = { sessionsBeforeLongBreak = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = EarthBrownLight)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                PomodoroSettings(
                                    focusMinutes = focusMinutes,
                                    shortBreakMinutes = shortBreakMinutes,
                                    longBreakMinutes = longBreakMinutes,
                                    sessionsBeforeLongBreak = sessionsBeforeLongBreak
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen
                        )
                    ) {
                        Text("保存", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Int,
    range: IntRange,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown
            )
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MintGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            colors = SliderDefaults.colors(
                thumbColor = MintGreen,
                activeTrackColor = MintGreen
            )
        )
    }
}