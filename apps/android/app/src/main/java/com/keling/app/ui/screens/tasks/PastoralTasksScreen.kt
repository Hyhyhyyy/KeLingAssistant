package com.keling.app.ui.screens.tasks

/**
 * =========================
 * 田园治愈风任务系统 - 增强版
 * 培育计划板
 * =========================
 *
 * 特点：
 * - 丰富的装饰元素与动画
 * - 游戏化的任务卡片
 * - 多层次的视觉设计
 * - 流畅的交互体验
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.keling.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.data.Task
import com.keling.app.data.TaskStatus
import com.keling.app.data.TaskType
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import com.keling.app.ui.components.TaskCreationDialog
import kotlin.math.*
import kotlin.random.Random

// ==================== 主页面 ====================

@Composable
fun PastoralTasksScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val tasks = viewModel.tasks.value
    val courses = viewModel.courses.value

    // 分类统计
    val pendingCount = tasks.count { it.status != TaskStatus.COMPLETED }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val totalEnergy = tasks.filter { it.status != TaskStatus.COMPLETED }.sumOf { it.rewards.energy }

    // 筛选状态
    var selectedFilter by remember { mutableStateOf(0) }

    // 任务创建对话框
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val filteredTasks = when (selectedFilter) {
        1 -> tasks.filter { it.status != TaskStatus.COMPLETED }
        2 -> tasks.filter { it.status == TaskStatus.COMPLETED }
        else -> tasks
    }.sortedWith(
        compareBy<Task> { it.status == TaskStatus.COMPLETED }
            .thenByDescending { it.priority }
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // ===== 顶部导航 =====
            item {
                EnhancedTasksHeader(onBack = onBack)
            }

            // ===== 任务概览 =====
            item {
                EnhancedTaskOverview(
                    pendingCount = pendingCount,
                    completedCount = completedCount,
                    totalEnergy = totalEnergy
                )
            }

            // ===== 筛选标签 =====
            item {
                EnhancedFilterTabs(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it }
                )
            }

            // ===== 任务列表 =====
            if (filteredTasks.isEmpty()) {
                item {
                    EnhancedEmptyState(
                        filterType = selectedFilter,
                        onCreateTask = {
                            editingTask = null
                            showCreateDialog = true
                        }
                    )
                }
            } else {
                items(filteredTasks) { task ->
                    EnhancedTaskCard(
                        task = task,
                        onClick = { viewModel.openTaskDetail(task.id) },
                        onComplete = {
                            if (task.status != TaskStatus.COMPLETED) {
                                viewModel.completeTask(task.id, task.estimatedMinutes)
                            }
                        }
                    )
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // 创建任务按钮
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                onClick = {
                    editingTask = null
                    showCreateDialog = true
                },
                shape = RoundedCornerShape(16.dp),
                color = WarmSunOrange,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_icon),
                        contentDescription = "创建",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "创建任务",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 任务创建对话框
    if (showCreateDialog) {
        TaskCreationDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { task ->
                viewModel.addTask(task)
                showCreateDialog = false
            },
            courses = courses,
            editingTask = editingTask
        )
    }
}

// ==================== 增强版组件 ====================

/**
 * 增强版背景
 */
@Composable
fun EnhancedTasksBackground() {
    // 点阵图案
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dotSpacing = 28.dp.toPx()
        val dotRadius = 1.5.dp.toPx()
        var x = dotSpacing
        while (x < size.width) {
            var y = dotSpacing
            while (y < size.height) {
                drawCircle(
                    color = WarmSunOrange.copy(alpha = 0.03f),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                y += dotSpacing
            }
            x += dotSpacing
        }
    }

    // 漂浮装饰
    val decorations = remember { List(10) { FloatingDecorationData() } }
    decorations.forEach { deco ->
        AnimatedFloatingDecoration(deco = deco)
    }
}

data class FloatingDecorationData(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val size: Int = Random.nextInt(4, 12),
    val alpha: Float = Random.nextFloat() * 0.08f + 0.02f,
    val speed: Int = Random.nextInt(3000, 6000),
    val color: Color = listOf(WarmSunOrange, PeachPink, MintGreen, LavenderPurple).random()
)

@Composable
fun AnimatedFloatingDecoration(deco: FloatingDecorationData) {
    val infiniteTransition = rememberInfiniteTransition(label = "deco")

    val alpha by infiniteTransition.animateFloat(
        initialValue = deco.alpha * 0.5f,
        targetValue = deco.alpha,
        animationSpec = infiniteRepeatable(
            animation = tween(deco.speed, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(deco.speed / 2, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = deco.color.copy(alpha = alpha),
            radius = deco.size.dp.toPx() * scale,
            center = Offset(deco.x * size.width, deco.y * size.height)
        )
    }
}

/**
 * 增强版头部
 */
@Composable
fun EnhancedTasksHeader(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = WarmSunOrange.copy(alpha = glowAlpha * 0.3f),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                    topLeft = Offset(-4.dp.toPx(), -4.dp.toPx())
                )
            }
            .background(Color.Transparent, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = BeigeSurface
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "返回",
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        // 标题
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_icon),
                    contentDescription = "任务",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "培育计划板",
                    style = MaterialTheme.typography.titleLarge,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Text(
                text = "今日任务清单",
                style = MaterialTheme.typography.labelSmall,
                color = WarmSunOrange.copy(alpha = 0.8f)
            )
        }

        // 占位
        Spacer(modifier = Modifier.width(52.dp))
    }
}

/**
 * 增强版任务概览
 */
@Composable
fun EnhancedTaskOverview(
    pendingCount: Int,
    completedCount: Int,
    totalEnergy: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "overview")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = WarmSunOrange.copy(alpha = glowAlpha * 0.5f),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
                    topLeft = Offset(-3.dp.toPx(), -3.dp.toPx())
                )
            },
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
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
                    }
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                // 统计行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedStatItem(
                        icon = "📋",
                        value = pendingCount.toString(),
                        label = "待执行",
                        color = WarmSunOrange
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(WarmGray.copy(alpha = 0.3f))
                    )

                    EnhancedStatItem(
                        icon = "✅",
                        value = completedCount.toString(),
                        label = "已完成",
                        color = MintGreen
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(WarmGray.copy(alpha = 0.3f))
                    )

                    EnhancedStatItem(
                        icon = "⚡",
                        value = totalEnergy.toString(),
                        label = "能量奖励",
                        color = CreamYellow
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 进度条
                val total = pendingCount + completedCount
                val progress = if (total > 0) completedCount.toFloat() / total else 0f

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "今日进度",
                            style = MaterialTheme.typography.labelMedium,
                            color = EarthBrown.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MintGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(BeigeSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .drawBehind {
                                    drawRoundRect(
                                        color = MintGreen.copy(alpha = 0.3f),
                                        cornerRadius = CornerRadius(5.dp.toPx()),
                                        size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                                        topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                                    )
                                }
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(MintGreen, MintGreenDark)
                                    ),
                                    RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun EnhancedStatItem(
    icon: String,
    value: String,
    label: String,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stat")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .drawBehind {
                    drawCircle(
                        color = color.copy(alpha = 0.15f),
                        radius = size.minDimension / 2
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrown.copy(alpha = 0.5f)
        )
    }
}

/**
 * 增强版筛选标签
 */
@Composable
fun EnhancedFilterTabs(
    selectedFilter: Int,
    onFilterChange: (Int) -> Unit
) {
    val tabs = listOf(
        Triple("全部任务", "📋", WarmSunOrange),
        Triple("待执行", "⏳", PeachPink),
        Triple("已完成", "✅", MintGreen)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tabs.forEachIndexed { index, (text, icon, color) ->
            val isSelected = selectedFilter == index

            val infiniteTransition = rememberInfiniteTransition(label = "tab$index")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = if (isSelected) 0.1f else 0f,
                targetValue = if (isSelected) 0.2f else 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow"
            )

            Surface(
                onClick = { onFilterChange(index) },
                modifier = Modifier
                    .weight(1f)
                    .drawBehind {
                        if (isSelected) {
                            drawRoundRect(
                                color = color.copy(alpha = glowAlpha),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                                topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                            )
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) color.copy(alpha = 0.12f) else Color.Transparent,
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, color)
                } else null,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) color else EarthBrown.copy(alpha = 0.6f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * 增强版任务卡片
 */
@Composable
fun EnhancedTaskCard(
    task: Task,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    // 优先级颜色
    val priorityColor = when (task.priority) {
        5 -> PriorityColors.urgent
        4 -> PriorityColors.high
        3 -> PriorityColors.medium
        else -> PriorityColors.low
    }

    // 任务类型图标和颜色
    val (typeIcon, typeColor) = when (task.type) {
        TaskType.DEEP_EXPLORATION -> "🔬" to LavenderPurple
        TaskType.REVIEW_RITUAL -> "📚" to SkyBlue
        TaskType.BOUNTY -> "💰" to CreamYellow
        TaskType.RESCUE -> "🆘" to RoseRed
        TaskType.DAILY_CARE -> "🌱" to MintGreen
        else -> "📋" to WarmSunOrange
    }

    val infiniteTransition = rememberInfiniteTransition(label = "task")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = if (isCompleted) 0.05f else if (task.priority >= 4) 0.12f else 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                drawRoundRect(
                    color = priorityColor.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = if (isCompleted) MintGreen.copy(alpha = 0.06f) else Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 任务类型图标
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // 背景光晕
                if (!isCompleted) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(14.dp)
                            .graphicsLayer { alpha = glowAlpha * 2 }
                            .background(priorityColor, CircleShape)
                    )
                }

                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = if (isCompleted) MintGreen.copy(alpha = 0.15f) else typeColor.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_check_complete),
                                contentDescription = "已完成",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = typeIcon,
                                fontSize = 22.sp,
                                color = typeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 任务信息
            Column(modifier = Modifier.weight(1f)) {
                // 标题
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCompleted) EarthBrown.copy(alpha = 0.5f) else EarthBrown,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 标签行
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 优先级标签
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "P${task.priority}",
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // 时长
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏱", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${task.estimatedMinutes}min",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrown.copy(alpha = 0.5f)
                        )
                    }
                }

                // 奖励行
                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EnhancedRewardChip(icon = "⚡", value = task.rewards.energy, color = WarmSunOrange)
                        Spacer(modifier = Modifier.width(10.dp))
                        if (task.rewards.crystals > 0) {
                            EnhancedRewardChip(icon = "✿", value = task.rewards.crystals, color = LavenderPurple)
                        }
                    }
                }
            }

            // 完成按钮
            if (!isCompleted) {
                Surface(
                    onClick = onComplete,
                    shape = RoundedCornerShape(12.dp),
                    color = MintGreen
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "完成",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "完成",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedRewardChip(
    icon: String,
    value: Int,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
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

/**
 * 增强版空状态
 */
@Composable
fun EnhancedEmptyState(
    filterType: Int,
    onCreateTask: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val (icon, title, description) = when (filterType) {
        1 -> Triple("📋", "没有待执行任务", "所有任务都已完成\n前往精灵助手获取新任务")
        2 -> Triple("🏆", "暂无完成记录", "完成任务后会在这里显示\n继续加油哦！")
        else -> Triple("🎯", "培育计划板空空的", "和星球精灵聊聊\n让它帮你规划学习任务")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = WarmSunOrange.copy(alpha = glowAlpha * 0.4f),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { translationY = floatOffset },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(25.dp)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(WarmSunOrange, CircleShape)
                )

                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = WarmSunOrange.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 38.sp)
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
                color = EarthBrown.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (filterType == 0) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    onClick = onCreateTask,
                    shape = RoundedCornerShape(14.dp),
                    color = WarmSunOrange
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_icon),
                            contentDescription = "创建",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "呼叫精灵助手",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            }
        }
    }
}