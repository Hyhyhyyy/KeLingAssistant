/**
 * MainActivity.kt（完整注释版）
 * 课灵App的主入口，采用Jetpack Compose构建UI
 * 包含：首页、AI助手、温室、任务四个主要页面
 */

package com.keling.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalDensity
import com.keling.app.components.Hexagon
import com.keling.app.ui.components.*
import com.keling.app.data.Course
import com.keling.app.data.KnowledgeNode
import com.keling.app.data.ScheduleSlot
import com.keling.app.data.Task
import com.keling.app.data.TaskStatus
import java.util.Calendar
import com.keling.app.data.json
import com.keling.app.ui.theme.*
import com.keling.app.ui.theme.Spacing
import com.keling.app.ui.theme.Radius
import com.keling.app.ui.theme.Elevation
import com.keling.app.viewmodel.AppViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

// ===== AI页面需要的额外导入 =====
import androidx.compose.foundation.lazy.LazyColumn          // 懒加载列表，性能更好
import androidx.compose.foundation.lazy.items               // 列表项渲染
import androidx.compose.foundation.lazy.rememberLazyListState // 列表滚动状态控制
import androidx.compose.foundation.text.KeyboardActions      // 键盘动作处理
import androidx.compose.foundation.text.KeyboardOptions       // 键盘选项配置
import androidx.compose.ui.text.input.ImeAction              // 输入法动作（如发送按钮）
import kotlinx.coroutines.CoroutineScope                     // 协程作用域
import kotlinx.coroutines.Dispatchers                        // 协程调度器（IO/Main线程）
import kotlinx.coroutines.launch                             // 启动协程
import kotlinx.coroutines.withContext                      // 切换线程上下文
import kotlinx.coroutines.delay
import com.keling.app.ai.AIResponse                          // AI响应数据类
import com.keling.app.ai.EnhancedLearningProfileProvider
import com.keling.app.ai.LearningContext
import com.keling.app.ai.ResponseType                        // 响应类型枚举
import com.keling.app.ai.SimpleAIService                     // AI服务单例对象
import com.keling.app.ai.ToolAction
import com.keling.app.ai.ToolCommand
import com.keling.app.ai.ToolCommandParser
import com.keling.app.ai.tools.AiToolExecutor
import com.keling.app.ai.tools.DefaultNavigationTool
import com.keling.app.ai.tools.DefaultTaskTool
import com.keling.app.ai.tools.DefaultKnowledgeGraphTool
import com.keling.app.ai.tools.DefaultNoteTool
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

import com.keling.app.ui.screens.greenhouse.PastoralGreenhouseScreen
import com.keling.app.ui.screens.home.PastoralHomeScreen
import com.keling.app.ui.screens.tasks.PastoralTasksScreen
import com.keling.app.ui.screens.ai.PastoralAIScreen
import com.keling.app.ui.screens.knowledge.MindMapKnowledgeGraphScreen
import com.keling.app.ui.screens.profile.PastoralProfileScreen
import com.keling.app.ui.screens.achievements.PastoralAchievementsScreen
import com.keling.app.ui.screens.report.PastoralStudyReportScreen
import com.keling.app.ui.screens.settings.PastoralSettingsScreen
import com.keling.app.ui.screens.notes.PastoralNotesScreen
import com.keling.app.ui.components.CheckInDialog

class MainActivity : ComponentActivity() {
    /**
     * Activity创建时的入口函数
     * setContent { } 是Compose的入口，所有UI在这里构建
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 应用主题包装器，包含颜色、字体等主题配置
            KeLingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),  // 填满整个屏幕
                    color = DawnWhite                    // 背景色：黎明白
                ) {
                    // 获取ViewModel实例，用于管理应用状态和数据
                    val viewModel: AppViewModel = viewModel()
                    val currentScreen by viewModel.currentScreen

                    // 页面路由控制器：根据 currentScreen 的值显示不同页面
                    // 类似网页的URL路由，但用字符串标识
                    when (currentScreen) {
                        "home" -> PastoralHomeScreen(
                            viewModel = viewModel,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                        "ai" -> PastoralAIScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("home") }
                        )
                        "greenhouse" -> PastoralGreenhouseScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("home") }
                        )
                        "greenhouse_course" -> GreenhouseCourseScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearSelectedCourse()
                                viewModel.navigateTo("greenhouse")
                            },
                            onAskAI = { viewModel.navigateTo("ai") }
                        )
                        "knowledge_graph" -> MindMapKnowledgeGraphScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearSelectedKnowledgeNode()
                                viewModel.navigateTo("greenhouse_course")
                            }
                        )
                        "tasks" -> PastoralTasksScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("home") }
                        )
                        "task_detail" -> TaskDetailScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearSelectedTask()
                                viewModel.navigateTo("tasks")
                            },
                            onAskAI = { viewModel.navigateTo("ai") }
                        )
                        "temple" -> TempleScreen(
                            onBack = { viewModel.navigateTo("home") }
                        )
                        "nebula" -> NebulaScreen(
                            onBack = { viewModel.navigateTo("home") }
                        )
                        "schedule_edit" -> ScheduleEditScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("home") }
                        )
                        "profile" -> PastoralProfileScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("home") },
                            onNavigateToAchievements = { viewModel.navigateTo("achievements") },
                            onNavigateToSettings = { viewModel.navigateTo("settings") },
                            onNavigateToReport = { viewModel.navigateTo("report") }
                        )
                        "achievements" -> PastoralAchievementsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("profile") }
                        )
                        "report" -> PastoralStudyReportScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("profile") }
                        )
                        "settings" -> PastoralSettingsScreen(
                            onBack = { viewModel.navigateTo("home") },
                            onNavigateToProfile = { viewModel.navigateTo("profile") },
                            onNavigateToAchievements = { viewModel.navigateTo("achievements") }
                        )
                        "notes" -> PastoralNotesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo("home") },
                            onAskAI = { viewModel.navigateTo("ai") }
                        )
                        else -> PastoralHomeScreen(
                            viewModel = viewModel,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    }

                    // 签到弹窗
                    if (viewModel.showCheckInDialog.value) {
                        CheckInDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.hideCheckInDialog() }
                        )
                    }
                }
            }
        }
    }
}

// ==================== 首页：星际导航 ====================

/** 根据当前小时返回问候语（早安/午安/晚安） */
private fun greetingByHour(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11 -> "早安"
        hour in 12..17 -> "午安"
        else -> "晚安"
    }
}

/**
 * 首页组件：应用的主界面
 * 功能：显示用户状态、知识星球概览、快捷入口、今日任务
 */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigate: (String) -> Unit
) {
    val user = viewModel.currentUser.value
    val courses = viewModel.courses.value
    val tasks = viewModel.tasks.value
    val pendingTasks = tasks.count { it.status == TaskStatus.PENDING }

    // 背景星云效果
    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            intensity = 0.05f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // ===== 顶部用户信息栏 =====
            item {
                EnhancedUserHeader(
                    user = user,
                    pendingTasks = pendingTasks
                )
            }

            // ===== AI引擎卡片 =====
            item {
                AIEngineCard(
                    pendingTasks = pendingTasks,
                    onClick = { onNavigate("ai") }
                )
            }

            // ===== 今日课表卡片 =====
            item {
                TodayScheduleCard(
                    viewModel = viewModel,
                    onEditSchedule = { onNavigate("schedule_edit") },
                    onCourseClick = { viewModel.openCourseGreenhouse(it) }
                )
            }

            // ===== 我的知识星球 =====
            item {
                SectionTitle(
                    title = "我的知识星球",
                    actionText = if (courses.isNotEmpty()) "查看全部" else null,
                    onAction = { onNavigate("greenhouse") }
                )
            }

            item {
                if (courses.isEmpty()) {
                    EmptyPlanetCard(onClick = { onNavigate("greenhouse") })
                } else {
                    PlanetCarousel(
                        courses = courses,
                        onCourseClick = { viewModel.openCourseGreenhouse(it) }
                    )
                }
            }

            // ===== 快捷入口 =====
            item {
                SectionTitle(title = "快捷入口")
            }

            item {
                QuickEntryGrid(onNavigate = onNavigate)
            }

            // ===== 今日培育计划 =====
            item {
                SectionTitle(
                    title = "今日培育计划",
                    actionText = if (tasks.isNotEmpty()) "查看全部" else null,
                    onAction = { onNavigate("tasks") }
                )
            }

            item {
                if (tasks.isEmpty()) {
                    EmptyTaskCard(onClick = { onNavigate("ai") })
                } else {
                    TaskBriefList(
                        tasks = tasks.take(3),
                        onTaskClick = { viewModel.openTaskDetail(it) }
                    )
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 增强版用户头部
 */
@Composable
private fun EnhancedUserHeader(
    user: com.keling.app.data.User,
    pendingTasks: Int
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = DawnWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：用户头像和名称
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    name = user.name,
                    size = 56.dp,
                    backgroundColor = StellarOrange
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    Text(
                        text = "${greetingByHour()}，${user.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lv.${user.level}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StellarOrange
                        )
                        Text(
                            text = " 星际园丁",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrownLight
                        )
                    }
                }
            }

            // 右侧：资源徽章
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ResourceBadge(
                    icon = "⚡",
                    value = user.energy,
                    color = StellarOrange
                )
                ResourceBadge(
                    icon = "💎",
                    value = user.crystals,
                    color = MossGreen
                )
            }
        }
    }
}

/**
 * AI引擎卡片
 */
@Composable
private fun AIEngineCard(
    pendingTasks: Int,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val glowAlpha by rememberGlowPulse(enabled = true, minAlpha = 0.1f, maxAlpha = 0.2f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { mutableStateOf(MutableInteractionSource()).value },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = StellarOrange.copy(alpha = 0.1f + glowAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI图标
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                // 发光背景
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(12.dp)
                        .graphicsLayer { alpha = glowAlpha * 2 }
                        .background(StellarOrange, CircleShape)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(com.keling.app.components.HexagonShape)
                        .background(StellarOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌟", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // 文字信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "恒星引擎在线",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown
                )
                Text(
                    text = if (pendingTasks > 0) "$pendingTasks 个任务待处理"
                           else "随时为你提供学习建议",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }

            // 箭头指示
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = StellarOrange
            )
        }
    }
}

/**
 * 区块标题
 */
@Composable
private fun SectionTitle(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = EarthBrown
        )

        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = StellarOrange
                )
            }
        }
    }
}

/**
 * 空星球卡片
 */
@Composable
private fun EmptyPlanetCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(containerColor = WarmSand.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌌", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "还没有知识星球",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = EarthBrown
            )
            Text(
                text = "点击创建你的第一个星球",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight
            )
        }
    }
}

/**
 * 星球轮播
 */
@Composable
private fun PlanetCarousel(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(courses.size) { index ->
            val isVisible = rememberStaggeredVisibility(index, 80L)

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 }
                ) + fadeIn() + scaleIn(initialScale = 0.9f)
            ) {
                PlanetCard(
                    course = courses[index],
                    onClick = { onCourseClick(courses[index].id) }
                )
            }
        }
    }
}

/**
 * 快捷入口网格
 */
@Composable
private fun QuickEntryGrid(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickEntryButton(
            icon = "🌱",
            label = "培育温室",
            color = MossGreen,
            onClick = { onNavigate("greenhouse") }
        )
        QuickEntryButton(
            icon = "📜",
            label = "赏金任务",
            color = StellarOrange,
            onClick = { onNavigate("tasks") }
        )
        QuickEntryButton(
            icon = "🏛️",
            label = "智慧圣殿",
            color = MistRose,
            onClick = { onNavigate("temple") }
        )
        QuickEntryButton(
            icon = "🌌",
            label = "协作星云",
            color = DuskGold,
            onClick = { onNavigate("nebula") }
        )
    }
}

/**
 * 空任务卡片
 */
@Composable
private fun EmptyTaskCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(containerColor = StellarOrange.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎯", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "今日暂无培育计划",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = EarthBrown
            )
            Text(
                text = "和恒星引擎聊聊，让它帮你制定学习计划",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight
            )
        }
    }
}

/**
 * 任务简报列表
 */
@Composable
private fun TaskBriefList(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        tasks.forEachIndexed { index, task ->
            val isVisible = rememberStaggeredVisibility(index, 100L)

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 }
                ) + fadeIn()
            ) {
                TaskBriefCard(
                    task = task,
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}

// ==================== 今日课表卡片（优化版） ====================

/**
 * 今日课表卡片 - 星际轨道风格
 * 可视化展示上一节、当前、下一节课程
 */
@Composable
private fun TodayScheduleCard(
    viewModel: AppViewModel,
    onEditSchedule: () -> Unit,
    onCourseClick: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)

    val (prev, current, next) = viewModel.getCurrentPrevNextSlots(dayOfWeek, hour, minute)
    val dayNames = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun formatTime(slot: ScheduleSlot): String {
        val endH = slot.startHour + (slot.startMinute + slot.durationMinutes) / 60
        val endM = (slot.startMinute + slot.durationMinutes) % 60
        return "%02d:%02d-%02d:%02d".format(
            slot.startHour, slot.startMinute,
            endH, endM
        )
    }

    // 背景光晕动画
    val glowAlpha by rememberGlowPulse(enabled = current != null, minAlpha = 0.05f, maxAlpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MossGreen.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(Radius.lg),
        border = BorderStroke(1.5.dp, MossGreen.copy(alpha = 0.25f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 当前课程发光背景
            if (current != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = glowAlpha }
                        .blur(30.dp)
                        .background(Color(current.first.themeColor))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md)
            ) {
                // 标题行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "课程表",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EarthBrown
                        )
                        Text(
                            text = dayNames.getOrElse(dayOfWeek) { "周?" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MossGreen
                        )
                    }

                    TextButton(onClick = onEditSchedule) {
                        Text(
                            "编辑",
                            style = MaterialTheme.typography.labelMedium,
                            color = MossGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // 课程列表
                if (prev == null && current == null && next == null) {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "今日暂无课程安排 ✨",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EarthBrownLight
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        prev?.let { (course, slot) ->
                            AnimatedScheduleSlotRow(
                                label = "已结束",
                                course = course,
                                slot = slot,
                                formatTime = ::formatTime,
                                onCourseClick = onCourseClick,
                                isPast = true,
                                delayMs = 0
                            )
                        }

                        current?.let { (course, slot) ->
                            AnimatedScheduleSlotRow(
                                label = "进行中",
                                course = course,
                                slot = slot,
                                formatTime = ::formatTime,
                                onCourseClick = onCourseClick,
                                isCurrent = true,
                                delayMs = 100
                            )
                        }

                        next?.let { (course, slot) ->
                            AnimatedScheduleSlotRow(
                                label = "即将开始",
                                course = course,
                                slot = slot,
                                formatTime = ::formatTime,
                                onCourseClick = onCourseClick,
                                delayMs = 200
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 带入场动画的课程时间行
 */
@Composable
private fun AnimatedScheduleSlotRow(
    label: String,
    course: Course,
    slot: ScheduleSlot,
    formatTime: (ScheduleSlot) -> String,
    onCourseClick: (String) -> Unit,
    isCurrent: Boolean = false,
    isPast: Boolean = false,
    delayMs: Long = 0
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 }
        ) + fadeIn() + scaleIn(initialScale = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        EnhancedScheduleSlotRow(
            label = label,
            course = course,
            slot = slot,
            formatTime = formatTime,
            onCourseClick = onCourseClick,
            isCurrent = isCurrent,
            isPast = isPast
        )
    }
}

/**
 * 增强版课程时间行
 * 不规则形状 + 渐变效果
 */
@Composable
private fun EnhancedScheduleSlotRow(
    label: String,
    course: Course,
    slot: ScheduleSlot,
    formatTime: (ScheduleSlot) -> String,
    onCourseClick: (String) -> Unit,
    isCurrent: Boolean = false,
    isPast: Boolean = false
) {
    val courseColor = Color(course.themeColor)

    // 当前课程的脉冲动画
    val pulseScale by rememberBreathingPulse(enabled = isCurrent, maxScale = 1.02f)
    val glowAlpha by rememberGlowPulse(enabled = isCurrent, minAlpha = 0.2f, maxAlpha = 0.5f)

    val bgAlpha = if (isCurrent) 0.2f else if (isPast) 0.08f else 0.1f
    val contentAlpha = if (isPast) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .clickable { onCourseClick(course.id) },
        colors = CardDefaults.cardColors(
            containerColor = courseColor.copy(alpha = bgAlpha)
        ),
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = courseColor.copy(alpha = if (isCurrent) 0.6f else 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 6.dp else 2.dp
        )
    ) {
        Box {
            // 当前课程发光效果
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = glowAlpha }
                        .blur(15.dp)
                        .background(courseColor)
                )
            }

            Row(
                modifier = Modifier
                    .padding(vertical = Spacing.sm, horizontal = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧标签
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(
                            if (isCurrent) courseColor.copy(alpha = 0.3f)
                            else courseColor.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = courseColor
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                // 课程信息
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = contentAlpha }
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = EarthBrown
                    )
                    Text(
                        text = if (course.location.isNotBlank())
                            "${formatTime(slot)} · ${course.location}"
                        else formatTime(slot),
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight
                    )
                }

                // 当前课程指示器
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(courseColor, CircleShape)
                    )
                }
            }
        }
    }
}

// ==================== 首页子组件 ====================

/**
 * 资源徽章组件
 * 显示带图标和数值的资源信息（如 ⚡ 100）
 *
 * @param icon 显示的emoji图标
 * @param value 数值
 * @param color 主题颜色
 */
@Composable
fun ResourceBadge(
    icon: String,
    value: Int,
    color: Color  // 使用导入的Color，简化类型声明
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            // 背景：带圆角的半透明色块
            .background(color.copy(alpha = 0.15f), MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

/**
 * 星球卡片组件
 * 显示课程信息，使用星球图片替代原有的方框/emoji
 *
 * @param course 课程数据对象
 * @param onClick 点击回调
 */
private val PLANET_STYLE_ASSET_FILE_NAMES = listOf(
    // 这些文件会由 build.gradle 中 sourceSets assets.srcDir 打包进 APK（黑色背景已转为透明）
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_07a20814018bed149e242871aa99cd10-faf0ff4f-e6fb-4d94-8384-dcbb07597090.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_0993b3eedeb68e3f1d013696dcd4ab95-133b2cd8-8881-4441-badb-9d993cf37dda.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_0b4b056395ccefcffe8e3593d2aaf993-8c429fb1-8f83-4e6c-9bc4-de6f21a18280.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_0e8f63059a67276753d02d05dc97c158-0b75cc75-ffc4-42d5-ac76-6de673dca899.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_0e8f63059a67276753d02d05dc97c158-6af0600b-3f39-48b6-ba11-615f8183cad6.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_16d9ebb7872c669e3bd5637e28a35a65-6c1877b7-a932-410a-8c61-44c40e617439.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_307b4a6a95817b450c7e945ce37d228b-48bf7838-1021-4fbe-bf69-4bd71a07b437.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_31b946b3ead010641244b6b7176c5ecb-1a24aad9-738e-4c70-81a2-99ecd163bce3.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_3a1fd122a8aab30c419db474d6e444d6-f2e03783-b936-4576-b5b3-9edd632aa12c.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_41fd91827d19f6f46417005300fa0809-6874d173-cd3f-4f9b-8eb6-42f94793779c.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_6032a41fb1565066336cd62a2a926045-3fb40176-487e-47f3-a4ea-41112b8816c9.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_70bf74b24efad13cc71e49e203fa8055-1721dc18-b433-440b-9233-d529652a484a.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_8f0ee1d0710ed6c6ed8f66b661b1a959-27d4bcf7-aff6-4edc-96ed-2099af867368.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_90d9936af5b4dc30de0696ae693bea5a-e48ee654-8c15-4280-8e6e-c558d3af1fcb.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_95d8fab3e680a70a7ec3e2ca5ee13bec-36b7becc-ff31-4236-b3cc-462c3eb01f3f.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_9de3180455383e811e245cd59fa5020a-b4217c91-ba74-41d2-a0f6-2c46d60385fe.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_aa577092503bea91703ba9f92c12d4ab-8d9f0ceb-961b-4680-8717-24041458cce9.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_b022f3718a9eea140918367b877cb70f-8ad7301d-b4f0-4965-a3d7-63fbeceae6c3.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_b5206486e496772663bd3b6978ca55db-5f32461e-63d4-4644-9110-eb824ccee506.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_b6fdd250d86d7b97f287a0b5a4fde16c-44faad32-f426-457b-8af3-e2061a40963f.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_d553cc07f9bb7cbac6562301b14ff8ff-da9e8789-960f-413b-8186-37aab671ff2c.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_d8165f6c0f4f770a2c33befb2510f19f-5579b4d1-158f-4bc1-ba3d-8eb4fbe1fd8e.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_d890ca4506f6940539db56c09b68551c-412232ae-0478-425f-8970-f7cd7c12259d.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_e9e8aaa3200e859adfea10472df644c0-56ee0a64-7813-4701-87cb-9c8dab7f8a8e.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_f4cb5609faae9ead6993f1b081d56a87-36b5efb0-dcb7-4733-bdf1-ad66833f72aa.png",
    "c__Users_13581_AppData_Roaming_Cursor_User_workspaceStorage_e1c746f1359e04a44268afce37572912_images_fe8cd156e1e20a602fffd498ead499e4-c3c369de-574c-4942-a792-223bd688e297.png"
)

private fun resolvePlanetAssetFileName(course: Course): String {
    val idx = if (course.planetStyleIndex >= 0) {
        course.planetStyleIndex
    } else {
        // 用 id 做稳定"随机"，保证同一课程每次进来显示同一种星球
        kotlin.math.abs(course.id.hashCode()) % PLANET_STYLE_ASSET_FILE_NAMES.size
    }
    return PLANET_STYLE_ASSET_FILE_NAMES.getOrNull(idx) ?: PLANET_STYLE_ASSET_FILE_NAMES.first()
}

@Composable
private fun PlanetImage(
    fileName: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val model = "file:///android_asset/$fileName"
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun PlanetStylePicker(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "星球形象",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = EarthBrown
        )
        Spacer(modifier = Modifier.height(8.dp))

        // -1 = 随机
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isRandom = selectedIndex < 0
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8A87C).copy(alpha = 0.12f))
                    .border(
                        width = if (isRandom) 2.dp else 1.dp,
                        color = if (isRandom) StellarOrange else EarthBrown.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
                    .clickable { onSelected(-1) },
                contentAlignment = Alignment.Center
            ) {
                Text("随机", fontSize = 12.sp, color = EarthBrown)
            }

            PLANET_STYLE_ASSET_FILE_NAMES.forEachIndexed { idx, fileName ->
                val isSelected = selectedIndex == idx
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8A87C).copy(alpha = 0.05f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) StellarOrange else EarthBrown.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                        .clickable { onSelected(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    PlanetImage(
                        fileName = fileName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentDescription = "planet_style_${idx}"
                    )
                }
            }
        }
    }
}

@Composable
fun PlanetCard(
    course: Course,
    onClick: () -> Unit
) {
    // 将课程的主题色转换为Compose的Color对象
    val color = Color(course.themeColor)
    val planetFileName = resolvePlanetAssetFileName(course)

    // 让图片显示区域更像"星球徽章"，替代原本的六边形+emoji
    val borderColor = color.copy(alpha = 0.6f)

    Card(
        modifier = Modifier
            .width(140.dp)  // 固定宽度
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally  // 内容水平居中
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
                    .border(width = 1.dp, color = borderColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PlanetImage(
                    fileName = planetFileName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown
            )

            // 掌握程度百分比
            Text(
                text = "${(course.masteryLevel * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

/**
 * 快捷按钮组件
 * 六边形图标 + 文字标签的垂直排列
 */
@Composable
fun QuickButton(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Hexagon(
            size = 56.dp,
            backgroundColor = color.copy(alpha = 0.2f),
            borderColor = color,
            borderWidth = 1.dp,
            content = { Text(icon, fontSize = 24.sp) }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = EarthBrown
        )
    }
}

/**
 * 任务简报组件
 * 显示任务标题、时长、奖励和状态
 * 左侧有优先级颜色指示条
 */
@Composable
fun TaskBrief(
    task: Task,
    onClick: () -> Unit
) {
    // 根据优先级(5最高)选择颜色
    val priorityColor = when (task.priority) {
        5 -> ErrorRed        // 紧急：红色
        4 -> StellarOrange   // 重要：橙色
        else -> MossGreen    // 普通：绿色
    }

    val isCompleted = task.status == TaskStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (isCompleted) base
                else base.clickable(onClick = onClick)
            },
        colors = CardDefaults.cardColors(
            containerColor = DawnWhite
        ),
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 8.dp,
            bottomStart = 8.dp,
            bottomEnd = 24.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) MossGreen.copy(alpha = 0.6f) else priorityColor.copy(alpha = 0.3f)
        )
    ) {
        Box {
            // 顶部斜切色带，让卡片轮廓更"有个性"
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 40.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 24.dp
                        )
                    )
                    .background(priorityColor.copy(alpha = 0.05f))
            )

            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧不规则色块 + 竖条
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(44.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 2.dp,
                                bottomStart = 2.dp,
                                bottomEnd = 12.dp
                            )
                        )
                        .background(priorityColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 中间：任务信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = EarthBrown
                    )
                    Text(
                        text = "${task.estimatedMinutes}分钟 · ${task.rewards.energy}⚡",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 右侧：打钩完成控件（仅展示，不可直接点击修改状态）
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            color = if (isCompleted) MossGreen.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) MossGreen else EarthBrown.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_check_complete),
                            contentDescription = "已完成",
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

// ==================== 其他页面 ====================

/**
 * 二级页面通用顶部栏：统一"返回 + 标题"的样式与点击区域。
 */
@Composable
fun KelingTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.height(52.dp)
        ) {
            Text("←", fontSize = 24.sp, color = EarthBrown)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = EarthBrown
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * 温室页面 - 星际花园
 * 管理知识星球，查看掌握度和相关任务
 */
@Composable
fun GreenhouseScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val courses = viewModel.courses.value
    val tasks = viewModel.tasks.value

    var showCourseEditorDialog by remember { mutableStateOf(false) }
    var editingCourseId by remember { mutableStateOf<String?>(null) }
    var dialogCourseName by remember { mutableStateOf("") }
    var dialogPlanetStyleIndex by remember { mutableStateOf(-1) }

    // 弹窗
    if (showCourseEditorDialog) {
        EnhancedCourseEditorDialog(
            editingCourseId = editingCourseId,
            dialogCourseName = dialogCourseName,
            dialogPlanetStyleIndex = dialogPlanetStyleIndex,
            onNameChange = { dialogCourseName = it },
            onStyleChange = { dialogPlanetStyleIndex = it },
            onConfirm = {
                val name = dialogCourseName.trim()
                if (name.isNotEmpty()) {
                    if (editingCourseId == null) {
                        viewModel.createCourse(name = name, planetStyleIndex = dialogPlanetStyleIndex)
                    } else {
                        viewModel.updateCourse(
                            courseId = editingCourseId!!,
                            newName = name,
                            planetStyleIndex = dialogPlanetStyleIndex
                        )
                    }
                    showCourseEditorDialog = false
                }
            },
            onDismiss = { showCourseEditorDialog = false }
        )
    }

    // 背景
    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = MossGreen,
            intensity = 0.04f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 顶部导航
            item {
                KeLingTopBar(
                    title = "培育温室",
                    onBack = onBack,
                    action = {
                        TextButton(onClick = {
                            editingCourseId = null
                            dialogCourseName = ""
                            dialogPlanetStyleIndex = -1
                            showCourseEditorDialog = true
                        }) {
                            Text("＋ 新建", color = StellarOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // 说明卡片
            item {
                KeLingCard(
                    variant = CardVariant.Outlined,
                    accentColor = MossGreen
                ) {
                    Text(
                        text = "在这里照看你的知识星球，培育它们茁壮成长 🌱",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight
                    )
                }
            }

            // 星球列表
            if (courses.isEmpty()) {
                item {
                    EmptyState(
                        icon = "🌌",
                        title = "还没有知识星球",
                        description = "点击右上角「新建」创建你的第一个星球\n或者和恒星引擎聊聊，让它帮你生成",
                        action = {
                            KeLingButton(
                                text = "和AI聊聊",
                                onClick = { /* 跳转AI */ },
                                color = StellarOrange,
                                size = ButtonSize.Small
                            )
                        }
                    )
                }
            } else {
                items(courses.size) { index ->
                    val course = courses[index]
                    val relatedTasks = tasks.filter { it.courseId == course.id }
                    val isVisible = rememberStaggeredVisibility(index, 100L)

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 3 }
                        ) + fadeIn() + scaleIn(initialScale = 0.95f)
                    ) {
                        CourseCard(
                            course = course,
                            relatedTasks = relatedTasks,
                            onCourseClick = { viewModel.openCourseGreenhouse(course.id) },
                            onTaskClick = { viewModel.openTaskDetail(it) },
                            onEditClick = {
                                editingCourseId = course.id
                                dialogCourseName = course.name
                                dialogPlanetStyleIndex = course.planetStyleIndex
                                showCourseEditorDialog = true
                            }
                        )
                    }
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 增强版课程编辑弹窗
 */
@Composable
private fun EnhancedCourseEditorDialog(
    editingCourseId: String?,
    dialogCourseName: String,
    dialogPlanetStyleIndex: Int,
    onNameChange: (String) -> Unit,
    onStyleChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Radius.xl),
        containerColor = DawnWhite,
        title = {
            Column {
                Text(
                    text = if (editingCourseId == null) "新建知识星球" else "编辑知识星球",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown
                )
                Text(
                    text = "为你的学习之旅创建一颗专属星球",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                OutlinedTextField(
                    value = dialogCourseName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入星球名称，如「高等数学」", color = EarthBrownLight)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StellarOrange,
                        unfocusedBorderColor = BorderGray
                    )
                )

                PlanetStylePicker(
                    selectedIndex = dialogPlanetStyleIndex,
                    onSelected = onStyleChange
                )
            }
        },
        confirmButton = {
            KeLingButton(
                text = "保存",
                onClick = onConfirm,
                color = StellarOrange,
                enabled = dialogCourseName.trim().isNotEmpty()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = EarthBrownLight)
            }
        }
    )
}

/**
 * 课程卡片
 */
@Composable
private fun CourseCard(
    course: Course,
    relatedTasks: List<Task>,
    onCourseClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onEditClick: () -> Unit
) {
    val courseColor = Color(course.themeColor)
    val glowAlpha by rememberGlowPulse(enabled = true, minAlpha = 0.05f, maxAlpha = 0.1f)

    KeLingCard(
        variant = CardVariant.Glowing,
        accentColor = courseColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 顶部：星球信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCourseClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 星球图片
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(12.dp)
                            .graphicsLayer { alpha = glowAlpha }
                            .background(courseColor, CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(courseColor.copy(alpha = 0.15f))
                            .border(1.5.dp, courseColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PlanetImage(
                            fileName = resolvePlanetAssetFileName(course),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                // 课程信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 掌握度进度条
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GradientProgressBar(
                            progress = course.masteryLevel,
                            color = courseColor,
                            height = 6.dp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "${(course.masteryLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = courseColor
                        )
                    }
                }

                // 编辑按钮
                TextButton(onClick = onEditClick) {
                    Text("编辑", color = courseColor, fontWeight = FontWeight.Bold)
                }
            }

            // 相关任务
            if (relatedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "相关任务",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrownLight
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                relatedTasks.take(2).forEach { task ->
                    TaskBriefCard(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        }
    }
}

/**
 * 单个课程的专属培育温室页面。
 * 上半部分展示星球成长进度与知识图谱入口，
 * 下半部分可扩展为该课程专属任务等内容。
 */
@Composable
fun GreenhouseCourseScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAskAI: () -> Unit
) {
    val courses = viewModel.courses.value
    val selectedId = viewModel.selectedCourseId.value
    val course = courses.find { it.id == selectedId }

    if (course == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            NebulaBackground(
                modifier = Modifier.fillMaxSize(),
                primaryColor = MossGreen,
                intensity = 0.03f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("未找到星球详情。", color = EarthBrown)
                Spacer(modifier = Modifier.height(Spacing.sm))
                KeLingButton(
                    text = "返回温室",
                    onClick = onBack,
                    color = MossGreen,
                    size = ButtonSize.Small
                )
            }
        }
        return
    }

    val courseColor = Color(course.themeColor)
    val tasks = viewModel.tasks.value.filter { it.courseId == course.id }
    val hasGraph = viewModel.isCourseGraphEnabled(course.id)

    var showGraphChoiceDialog by remember { mutableStateOf(false) }

    if (showGraphChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showGraphChoiceDialog = false },
            confirmButton = {
                KeLingButton(
                    text = "去生成并展示",
                    onClick = {
                        viewModel.enableCourseGraph(course.id)
                        showGraphChoiceDialog = false
                        onAskAI()
                    },
                    color = StellarOrange,
                    size = ButtonSize.Small
                )
            },
            dismissButton = {
                TextButton(onClick = { showGraphChoiceDialog = false }) {
                    Text("暂不展示", color = EarthBrownLight)
                }
            },
            title = { Text("生成知识图谱？", color = EarthBrown, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "恒星引擎可以根据本课程的知识结构，生成一份知识图谱草案。\n你可以先去和 AI 讨论生成内容，再决定是否长期展示在温室里。",
                    color = EarthBrownLight
                )
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = courseColor,
            intensity = 0.03f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                KeLingTopBar(title = "培育温室", onBack = onBack)
            }

            // 星球成长概览
            item {
                KeLingCard(
                    variant = CardVariant.Glowing,
                    accentColor = courseColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EarthBrown
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GradientProgressBar(
                                    progress = course.masteryLevel,
                                    color = courseColor,
                                    modifier = Modifier.width(120.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text(
                                    text = "${(course.masteryLevel * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = courseColor
                                )
                            }
                        }
                        // 星球图片
                        val glowAlpha by rememberGlowPulse(enabled = true, minAlpha = 0.1f, maxAlpha = 0.2f)
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .blur(12.dp)
                                    .graphicsLayer { alpha = glowAlpha }
                                    .background(courseColor, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(courseColor.copy(alpha = 0.15f))
                                    .border(1.5.dp, courseColor.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                PlanetImage(
                                    fileName = resolvePlanetAssetFileName(course),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // 知识图谱区域
            item {
                val knowledgeNodes = viewModel.knowledgeNodesForCourse(course.id)
                val nodeCount = knowledgeNodes.size

                if (nodeCount > 0 || hasGraph) {
                    KeLingCard(variant = CardVariant.Elevated) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "知识图谱",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EarthBrown
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = courseColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "$nodeCount 个知识点",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = courseColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (nodeCount > 0) {
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                // 显示知识节点列表（最多显示5个）
                                knowledgeNodes.take(5).forEach { node ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(courseColor.copy(alpha = 0.6f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = node.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = EarthBrown,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // 掌握度指示
                                        Text(
                                            text = "${(node.masteryLevel * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (node.masteryLevel >= 0.6f) MossGreen
                                                   else if (node.masteryLevel >= 0.3f) StellarOrange
                                                   else RoseRed
                                        )
                                    }
                                }

                                if (nodeCount > 5) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "还有 ${nodeCount - 5} 个知识点...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EarthBrownLight
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text = "还没有知识点，和恒星引擎聊聊，让它帮你添加知识点吧！",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EarthBrownLight
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.md))
                            KeLingButton(
                                text = if (nodeCount > 0) "查看完整知识图谱" else "去和恒星引擎添加知识点",
                                onClick = {
                                    if (nodeCount > 0) {
                                        viewModel.navigateTo("knowledge_graph")
                                    } else {
                                        onAskAI()
                                    }
                                },
                                color = courseColor,
                                size = ButtonSize.Small
                            )
                        }
                    }
                } else {
                    KeLingCard(
                        variant = CardVariant.Filled,
                        accentColor = StellarOrange
                    ) {
                        Column {
                            Text(
                                text = "还没有形成知识图谱",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EarthBrown
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "未形成知识图谱？让恒星引擎来帮忙~",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EarthBrownLight
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            KeLingButton(
                                text = "去和恒星引擎生成图谱",
                                onClick = { showGraphChoiceDialog = true },
                                color = StellarOrange,
                                size = ButtonSize.Small
                            )
                        }
                    }
                }
            }

            // 下半部分：该课程的相关任务
            if (tasks.isNotEmpty()) {
                item {
                    Text(
                        text = "本星球任务",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )
                }
                items(tasks) { task ->
                    TaskBriefCard(
                        task = task,
                        onClick = { viewModel.openTaskDetail(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 任务页面 - 赏金任务
 * 显示所有任务的完整列表
 */
@Composable
fun TasksScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val tasks = viewModel.tasks.value

    // 分类统计
    val pendingCount = tasks.count { it.status == TaskStatus.PENDING }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }

    // 筛选状态
    var selectedFilter by remember { mutableStateOf(0) } // 0=全部, 1=待完成, 2=已完成

    val filteredTasks = when (selectedFilter) {
        1 -> tasks.filter { it.status != TaskStatus.COMPLETED }
        2 -> tasks.filter { it.status == TaskStatus.COMPLETED }
        else -> tasks
    }.sortedWith(
        compareBy<Task> { it.status == TaskStatus.COMPLETED }
            .thenByDescending { it.priority }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = StellarOrange,
            intensity = 0.04f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // 顶部导航
            item {
                KeLingTopBar(
                    title = "赏金任务",
                    onBack = onBack
                )
            }

            // 统计卡片
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📋",
                        value = pendingCount.toString(),
                        label = "待完成",
                        color = StellarOrange
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "✅",
                        value = completedCount.toString(),
                        label = "已完成",
                        color = MossGreen
                    )
                }
            }

            // 筛选器
            item {
                SegmentedControl(
                    segments = listOf("全部", "待完成", "已完成"),
                    selectedSegment = selectedFilter,
                    onSegmentSelected = { selectedFilter = it }
                )
            }

            // 任务列表
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = "🎯",
                        title = if (selectedFilter == 1) "没有待完成的任务"
                               else if (selectedFilter == 2) "还没有完成的任务"
                               else "暂无任务",
                        description = if (selectedFilter == 0) "和恒星引擎聊聊，让它帮你制定学习计划"
                                     else "切换其他筛选条件看看",
                        modifier = Modifier.padding(top = Spacing.xl)
                    )
                }
            } else {
                items(filteredTasks.size) { index ->
                    val task = filteredTasks[index]
                    val isVisible = rememberStaggeredVisibility(index, 80L)

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 3 }
                        ) + fadeIn()
                    ) {
                        TaskBriefCard(
                            task = task,
                            onClick = { viewModel.openTaskDetail(task.id) },
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 统计卡片
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrownLight
            )
        }
    }
}

/**
 * 任务详情页面
 */
@Composable
fun TaskDetailScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAskAI: () -> Unit
) {
    val tasks = viewModel.tasks.value
    val selectedId = viewModel.selectedTaskId.value
    val task = tasks.find { it.id == selectedId }

    if (task == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("未找到任务详情", color = EarthBrown)
            Spacer(modifier = Modifier.height(Spacing.sm))
            KeLingButton(
                text = "返回",
                onClick = onBack,
                color = StellarOrange,
                size = ButtonSize.Small
            )
        }
        return
    }

    var isRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var showFinishConfirm by remember { mutableStateOf(false) }

    val taskCompleted = task.status == TaskStatus.COMPLETED

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)
    val rewards = task.rewards

    // 完成奖励弹窗
    if (showRewardDialog) {
        RewardDialog(
            minutes = minutes,
            rewards = rewards,
            onDismiss = {
                showRewardDialog = false
                onBack()
            }
        )
    }

    // 完成确认弹窗
    if (showFinishConfirm && !taskCompleted) {
        FinishConfirmDialog(
            minutes = minutes,
            onConfirm = {
                showFinishConfirm = false
                isRunning = false
                val minutesUsed = minutes.coerceAtLeast(1)
                viewModel.completeTask(task.id, minutesUsed)
                showRewardDialog = true
            },
            onDismiss = { showFinishConfirm = false }
        )
    }

    val priorityColor = when (task.priority) {
        5 -> ErrorRed
        4 -> StellarOrange
        else -> MossGreen
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = priorityColor,
            intensity = 0.03f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 顶部导航
            item {
                KeLingTopBar(
                    title = "任务详情",
                    onBack = onBack
                )
            }

            // 任务信息卡片
            item {
                KeLingCard(
                    variant = CardVariant.Elevated,
                    accentColor = priorityColor
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = when (task.priority) {
                                    5 -> "紧急"
                                    4 -> "重要"
                                    else -> "普通"
                                },
                                color = priorityColor,
                                icon = when (task.priority) {
                                    5 -> "🔥"
                                    4 -> "⭐"
                                    else -> "📌"
                                }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (taskCompleted) {
                                StatusBadge(
                                    text = "已完成",
                                    color = MossGreen,
                                    icon = "✓"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = EarthBrown
                        )

                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = EarthBrownLight
                            )
                        }
                    }
                }
            }

            // 计时器卡片
            item {
                KeLingCard(
                    variant = CardVariant.Glowing,
                    accentColor = if (isRunning) priorityColor else BorderGray
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "计时器",
                            style = MaterialTheme.typography.labelMedium,
                            color = EarthBrownLight
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // 时间显示
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isRunning) priorityColor else EarthBrown
                        )

                        Text(
                            text = "预计 ${task.estimatedMinutes} 分钟",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrownLight
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // 控制按钮
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            KeLingButton(
                                text = "开始",
                                onClick = {
                                    isRunning = true
                                    viewModel.startTask(task.id)
                                },
                                color = MossGreen,
                                enabled = !isRunning && !taskCompleted,
                                size = ButtonSize.Small
                            )

                            KeLingButton(
                                text = "暂停",
                                onClick = { isRunning = false },
                                color = StellarOrange,
                                style = ButtonStyle.Secondary,
                                enabled = isRunning && !taskCompleted,
                                size = ButtonSize.Small
                            )

                            KeLingButton(
                                text = "结束",
                                onClick = {
                                    isRunning = false
                                    if (!taskCompleted) {
                                        showFinishConfirm = true
                                    }
                                },
                                color = MistRose,
                                enabled = !taskCompleted,
                                size = ButtonSize.Small
                            )
                        }
                    }
                }
            }

            // 奖励预览
            item {
                KeLingCard(variant = CardVariant.Outlined) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RewardItem(icon = "⚡", value = rewards.energy, label = "能量")
                        RewardItem(icon = "💎", value = rewards.crystals, label = "结晶")
                        RewardItem(icon = "✨", value = rewards.exp, label = "经验")
                    }
                }
            }

            // AI帮助卡片
            if (!taskCompleted) {
                item {
                    KeLingCard(
                        variant = CardVariant.Filled,
                        accentColor = StellarOrange
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "对任务有困惑？",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EarthBrown
                                )
                                Text(
                                    text = "问问恒星引擎吧",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EarthBrownLight
                                )
                            }
                            KeLingButton(
                                text = "去问问",
                                onClick = onAskAI,
                                color = StellarOrange,
                                size = ButtonSize.Small
                            )
                        }
                    }
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 奖励项
 */
@Composable
private fun RewardItem(
    icon: String,
    value: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = EarthBrown
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrownLight
        )
    }
}

/**
 * 奖励弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardDialog(
    minutes: Int,
    rewards: com.keling.app.data.Rewards,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = "任务完成！",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = EarthBrown
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "本次任务用时约 ${minutes.coerceAtLeast(1)} 分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrownLight
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                RewardItem(icon = "⚡", value = rewards.energy, label = "能量")
                RewardItem(icon = "💎", value = rewards.crystals, label = "结晶")
                RewardItem(icon = "✨", value = rewards.exp, label = "经验")
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            KeLingButton(
                text = "收下奖励",
                onClick = onDismiss,
                color = StellarOrange
            )
        }
    }
}

/**
 * 完成确认弹窗
 */
@Composable
private fun FinishConfirmDialog(
    minutes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Radius.xl),
        containerColor = DawnWhite,
        title = {
            Text(
                text = "结束本次任务？",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = EarthBrown
            )
        },
        text = {
            Text(
                text = "本次任务已学习约 ${minutes.coerceAtLeast(1)} 分钟，结束后将根据奖励结算能量与结晶。",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrownLight
            )
        },
        confirmButton = {
            KeLingButton(
                text = "确认结束",
                onClick = onConfirm,
                color = StellarOrange
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("再学一会儿", color = EarthBrownLight)
            }
        }
    )
}

/**
 * 智慧圣殿：用于展示学习建议、知识卡片等静态内容。
 * 目前为静态设计，后续可以接入笔记与成就系统。
 */
@Composable
fun TempleScreen(
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = DuskGold,
            intensity = 0.03f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                KeLingTopBar(title = "智慧圣殿", onBack = onBack)
            }

            item {
                KeLingCard(variant = CardVariant.Outlined, accentColor = DuskGold) {
                    Text(
                        text = "这里沉淀你的高光时刻与深度理解，未来会承载笔记、知识卡片与成就。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EarthBrownLight
                    )
                }
            }

            item {
                KeLingCard(
                    variant = CardVariant.Glowing,
                    accentColor = DuskGold
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column {
                            Text(
                                text = "今日洞见",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EarthBrown
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "当你能用自己的话向别人解释一个概念时，这颗知识种子才真正发芽。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EarthBrownLight
                            )
                        }
                    }
                }
            }

            item {
                KeLingCard(
                    variant = CardVariant.Filled,
                    accentColor = StellarOrange
                ) {
                    Column {
                        Text(
                            text = "即将开放的功能",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StellarOrange
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "• AI 生成的知识卡片\n• 你的成就与称号墙\n• 高频错题与复盘笔记入口",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EarthBrownLight
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 协作星云：预留给未来的组队学习与互助功能。
 */
@Composable
fun NebulaScreen(
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = DeepSeaBlue,
            intensity = 0.03f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                KeLingTopBar(title = "协作星云", onBack = onBack)
            }

            item {
                KeLingCard(variant = CardVariant.Outlined, accentColor = DeepSeaBlue) {
                    Text(
                        text = "未来，你可以在这里与同学一起组队学习、互相发布与认领「星际救援」任务。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EarthBrownLight
                    )
                }
            }

            item {
                KeLingCard(
                    variant = CardVariant.Glowing,
                    accentColor = DuskGold
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚀", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "联机学习 · 规划中",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DuskGold
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "• 查看好友的星球生长情况\n• 互相发送鼓励与助力任务\n• 共同完成赏金与救援任务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EarthBrownLight
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

/**
 * 课程表编辑页面 - 星图规划风格
 * 周视图网格 + 优化过的添加弹窗
 */
@Composable
fun ScheduleEditScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val courses = viewModel.courses.value
    val weekSchedule = viewModel.getWeekSchedule()
    val dayNames = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

    // 弹窗状态
    var showAddDialog by remember { mutableStateOf(false) }
    var addCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: "") }
    var addDayOfWeek by remember { mutableStateOf(1) }
    var addStartHour by remember { mutableStateOf(8) }
    var addStartMinute by remember { mutableStateOf(0) }
    var addDuration by remember { mutableStateOf(90) }

    // 获取当前是周几，用于默认选择
    val currentDayOfWeek = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let { if (it == 1) 7 else it - 1 }
    }

    // 添加课时弹窗
    if (showAddDialog) {
        EnhancedAddSlotDialog(
            courses = courses,
            initialCourseId = addCourseId,
            initialDayOfWeek = addDayOfWeek,
            initialHour = addStartHour,
            initialMinute = addStartMinute,
            initialDuration = addDuration,
            onConfirm = { courseId, slot ->
                viewModel.addScheduleSlot(courseId, slot)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DawnWhite)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.headlineMedium, color = EarthBrown)
            }
            Text(
                text = "课程表",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = EarthBrown,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 说明卡片
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MossGreen.copy(alpha = 0.06f)
                    ),
                    shape = RoundedCornerShape(Radius.md),
                    border = BorderStroke(1.dp, MossGreen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "管理各课程的上课时间，首页将根据当前时间展示课程进度",
                            style = MaterialTheme.typography.bodySmall,
                            color = EarthBrownLight
                        )
                    }
                }
            }

            // 添加按钮
            item {
                Button(
                    onClick = {
                        if (courses.isNotEmpty()) {
                            addCourseId = courses.first().id
                            addDayOfWeek = currentDayOfWeek
                            addStartHour = 8
                            addStartMinute = 0
                            addDuration = 90
                            showAddDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StellarOrange
                    ),
                    enabled = courses.isNotEmpty()
                ) {
                    Text("＋ 添加课时", color = DawnWhite)
                }

                if (courses.isEmpty()) {
                    Text(
                        text = "请先在培育温室创建课程",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            // 课程列表
            if (weekSchedule.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🌌",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text = "暂无课时安排",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EarthBrownLight
                            )
                            Text(
                                text = "点击上方按钮添加第一节课",
                                style = MaterialTheme.typography.bodySmall,
                                color = EarthBrownLight.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                // 按星期分组展示
                items(weekSchedule.size) { index ->
                    val (course, slot) = weekSchedule[index]
                    val isVisible = rememberStaggeredVisibility(index, 80L)

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 3 }
                        ) + fadeIn() + scaleIn(initialScale = 0.95f)
                    ) {
                        EnhancedCourseSlotCard(
                            course = course,
                            slot = slot,
                            dayName = dayNames[slot.dayOfWeek],
                            onDelete = {
                                viewModel.removeScheduleSlot(
                                    course.id,
                                    slot.dayOfWeek,
                                    slot.startHour,
                                    slot.startMinute
                                )
                            }
                        )
                    }
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}

/**
 * 增强版添加课时弹窗
 */
@Composable
private fun EnhancedAddSlotDialog(
    courses: List<Course>,
    initialCourseId: String,
    initialDayOfWeek: Int,
    initialHour: Int,
    initialMinute: Int,
    initialDuration: Int,
    onConfirm: (String, ScheduleSlot) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCourseId by remember { mutableStateOf(initialCourseId) }
    var selectedDay by remember { mutableStateOf(initialDayOfWeek) }
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var selectedDuration by remember { mutableStateOf(initialDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Radius.xl),
        containerColor = DawnWhite,
        title = {
            Column {
                Text(
                    text = "添加课时",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown
                )
                Text(
                    text = "为课程安排上课时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // 课程选择
                CourseSelector(
                    courses = courses,
                    selectedCourseId = selectedCourseId,
                    onCourseSelected = { course: Course -> selectedCourseId = course.id }
                )

                // 星期选择
                Column {
                    Text(
                        text = "选择星期",
                        style = MaterialTheme.typography.labelMedium,
                        color = EarthBrown
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    WeekOrbitSelector(
                        selectedDay = selectedDay,
                        onDaySelected = { day: Int -> selectedDay = day }
                    )
                }

                // 时间选择
                Column {
                    Text(
                        text = "开始时间",
                        style = MaterialTheme.typography.labelMedium,
                        color = EarthBrown
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    CircularTimePicker(
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        onTimeSelected = { h: Int, m: Int ->
                            selectedHour = h
                            selectedMinute = m
                        },
                        modifier = Modifier.size(200.dp)
                    )
                }

                // 时长选择
                DurationSelector(
                    selectedDuration = selectedDuration,
                    onDurationSelected = { duration: Int -> selectedDuration = duration }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedCourseId,
                        ScheduleSlot(selectedDay, selectedHour, selectedMinute, selectedDuration)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StellarOrange
                ),
                shape = RoundedCornerShape(Radius.md)
            ) {
                Text("确认添加", color = DawnWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = EarthBrownLight)
            }
        }
    )
}

/**
 * 增强版课程时段卡片
 * 不规则形状 + 滑动删除
 */
@Composable
private fun EnhancedCourseSlotCard(
    course: Course,
    slot: ScheduleSlot,
    dayName: String,
    onDelete: () -> Unit
) {
    val courseColor = Color(course.themeColor)

    val endH = slot.startHour + (slot.startMinute + slot.durationMinutes) / 60
    val endM = (slot.startMinute + slot.durationMinutes) % 60
    val timeStr = "%02d:%02d-%02d:%02d".format(slot.startHour, slot.startMinute, endH, endM)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = courseColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.5.dp, courseColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 星期标签
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(courseColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = courseColor
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // 课程信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown
                )
                Text(
                    text = if (course.location.isNotBlank()) "$timeStr · ${course.location}" else timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }

            // 删除按钮
            TextButton(onClick = onDelete) {
                Text(
                    text = "删除",
                    style = MaterialTheme.typography.labelMedium,
                    color = MistRose
                )
            }
        }
    }
}

@Composable
fun KnowledgeGraphScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val selectedCourseId = viewModel.selectedCourseId.value
    val selectedKnowledgeNodeId = viewModel.selectedKnowledgeNodeId.value

    val courses = viewModel.courses.value
    val course = courses.find { it.id == selectedCourseId }

    val nodes = remember(selectedCourseId, viewModel.knowledgeNodes.value) {
        selectedCourseId?.let { viewModel.knowledgeNodesForCourse(it) } ?: emptyList()
    }

    val focusedNode = nodes.firstOrNull { it.id == selectedKnowledgeNodeId }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 当外部指定了节点聚焦时，首次进入/节点变化时自动居中
    var lastCenteredNodeId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedKnowledgeNodeId) {
        lastCenteredNodeId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DawnWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← 返回", color = EarthBrown)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = course?.name ?: "知识图谱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(DawnWhite)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }

            // 自动居中：把 focusedNode 移到画面中心
            LaunchedEffect(focusedNode?.id, nodes.size, widthPx, heightPx) {
                val id = focusedNode?.id
                if (id.isNullOrBlank() || id == lastCenteredNodeId) return@LaunchedEffect

                val nodeX = widthPx * (focusedNode?.positionX ?: 0.5f)
                val nodeY = heightPx * (focusedNode?.positionY ?: 0.5f)

                scale = 1f
                offset = Offset(
                    x = widthPx / 2f - nodeX,
                    y = heightPx / 2f - nodeY
                )
                lastCenteredNodeId = id
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                // 线条：根据 parentIds 绘制父子连线
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (nodes.isEmpty()) return@Canvas
                    val nodeById = nodes.associateBy { it.id }

                    nodes.forEach { child ->
                        val childX = widthPx * child.positionX
                        val childY = heightPx * child.positionY

                        child.parentIds.forEach { parentId ->
                            val parent = nodeById[parentId] ?: return@forEach
                            val parentX = widthPx * parent.positionX
                            val parentY = heightPx * parent.positionY

                            drawLine(
                                color = StellarOrange.copy(alpha = 0.35f),
                                start = Offset(parentX, parentY),
                                end = Offset(childX, childY),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                nodes.forEach { node ->
                    val nodeX = widthPx * node.positionX
                    val nodeY = heightPx * node.positionY

                    KnowledgeNodeChip(
                        name = node.name,
                        mastery = node.masteryLevel,
                        isFocused = node.id == selectedKnowledgeNodeId,
                        modifier = Modifier.absoluteOffset(
                            x = with(density) { (nodeX - 60.dp.toPx()).toDp() },
                            y = with(density) { (nodeY - 32.dp.toPx()).toDp() }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun KnowledgeNodeChip(
    name: String,
    mastery: Float,
    isFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = StellarOrange.copy(alpha = 0.12f)
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) StellarOrange else StellarOrange.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 120.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
            Text(
                text = "掌握度 ${(mastery * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrown.copy(alpha = 0.7f)
            )
        }
    }
}

// ==================== 预览 ====================

/**
 * 首页预览
 * 在Android Studio的设计视图中查看效果
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KeLingTheme {
        HomeScreen(viewModel = AppViewModel(), onNavigate = {})
    }
}

// ==================== AI助手页面（新增完整实现） ====================

/**
 * AI消息数据类（用于旧版 AIScreen）
 * 用于存储聊天消息的内容、发送者、类型和时间戳
 *
 * @param content 消息文本内容
 * @param isUser true=用户发送，false=AI回复
 * @param type 消息类型（计划、分析、复习等）
 * @param timestamp 时间戳，默认当前时间
 */
data class ChatMessageUi(
    val content: String,
    val isUser: Boolean,
    val type: ResponseType = ResponseType.GENERAL,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI助手页面：恒星引擎
 * 功能：类似微信的聊天界面，支持本地规则回复和云端AI回复
 *
 * @param onBack 返回回调，点击左上角返回按钮触发
 */
@Composable
fun AIScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    // ===== 状态管理 =====
    // 消息列表，初始包含一条AI欢迎语
    var messages by remember { mutableStateOf(listOf(
        ChatMessageUi(
            content = "你好！我是恒星引擎 🌟\n\n我是你的学习伙伴，可以帮你：\n• 制定学习计划\n• 分析薄弱点\n• 提醒复习\n\n试试输入「今天怎么学」",
            isUser = false,  // AI发送的消息
            type = ResponseType.GENERAL
        )
    )) }

    // 输入框当前文本
    var inputText by remember { mutableStateOf("") }

    // 是否正在加载（显示转圈动画）
    var isLoading by remember { mutableStateOf(false) }

    // 列表滚动状态，用于自动滚动到底部
    val listState = rememberLazyListState()

    // 根据消息数量自动滚动到最末条，避免 onComplete 里使用旧的 messages.size
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 协程作用域，用于启动后台任务（如网络请求）
    val scope = rememberCoroutineScope()

    // 工具执行器：让 AI 能够创建任务 / 导航页面
    val toolExecutor = remember {
        AiToolExecutor(
            taskTool = DefaultTaskTool(viewModel),
            navigationTool = DefaultNavigationTool(viewModel),
            noteTool = DefaultNoteTool(viewModel),
            knowledgeGraphTool = DefaultKnowledgeGraphTool(viewModel),
            scheduleTool = com.keling.app.ai.tools.DefaultScheduleTool(viewModel)
        )
    }

    // 学习画像提供器：为 AI 请求构建上下文
    // 使用简化的本地方法生成上下文
    val buildLearningContext: () -> String = {
        val cal = java.util.Calendar.getInstance()
        val dayOfWeek = (cal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
        val user = viewModel.currentUser.value
        val courses = viewModel.courses.value
        val tasks = viewModel.tasks.value
        val todaySchedule = viewModel.getTodaySchedule(dayOfWeek)

        buildString {
            appendLine("【用户画像】")
            appendLine("  姓名: ${user.name}, 等级: Lv.${user.level}")
            appendLine("  能量: ${user.energy}⚡ 结晶: ${user.crystals}💎")
            appendLine()

            appendLine("【今日状态】")
            val pending = tasks.filter { it.status == com.keling.app.data.TaskStatus.PENDING }
            appendLine("  待办任务: ${pending.size}个")
            if (pending.isNotEmpty()) {
                append("  优先: ")
                pending.sortedByDescending { it.priority }.take(3).forEach {
                    append("${it.title} ")
                }
                appendLine()
            }
            appendLine()

            if (todaySchedule.isNotEmpty()) {
                appendLine("【今日课表】")
                todaySchedule.forEach { (course, slot) ->
                    appendLine("  ${course.name} ${slot.startHour}:${slot.startMinute}")
                }
                appendLine()
            }

            val weakCourses = courses.filter { it.masteryLevel < 0.6f }
            if (weakCourses.isNotEmpty()) {
                appendLine("【薄弱点】")
                weakCourses.take(3).forEach {
                    append("  ${it.name}(${(it.masteryLevel * 100).toInt()}%) ")
                }
                appendLine()
            }
        }
    }

    // 等待用户确认的"创建任务"命令
    var pendingCreateCommand by remember { mutableStateOf<ToolCommand?>(null) }

    // 知识图谱预览卡片：当 AI 执行知识图谱 Tool 后，在聊天区域给出"节点列表 + 定位入口"
    var kgPreviewCourseId by remember { mutableStateOf<String?>(null) }
    var kgPreviewFocusNodeId by remember { mutableStateOf<String?>(null) }
    var kgPreviewNodes by remember { mutableStateOf<List<KnowledgeNode>>(emptyList()) }

    val onKgPreview: (String, String?) -> Unit = { courseIdOrName, focusNodeName ->
        val course = viewModel.courses.value.find { it.id == courseIdOrName || it.name == courseIdOrName }
        val finalCourseId = course?.id ?: courseIdOrName
        val nodes = viewModel.knowledgeNodesForCourse(finalCourseId)
        kgPreviewCourseId = finalCourseId
        kgPreviewNodes = nodes

        kgPreviewFocusNodeId = focusNodeName?.let { name ->
            nodes.firstOrNull { it.name == name }?.id
        }
    }

    // ===== UI布局 =====
    Box(modifier = Modifier.fillMaxSize()) {
        // 星云背景
        NebulaBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = StellarOrange,
            intensity = 0.03f
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ----- 顶部导航栏 -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", style = MaterialTheme.typography.headlineSmall, color = EarthBrown, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(Spacing.sm))

                // AI头像 - 带发光效果
                val glowAlpha by rememberGlowPulse(enabled = true, minAlpha = 0.1f, maxAlpha = 0.25f)
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(10.dp)
                            .graphicsLayer { alpha = glowAlpha }
                            .background(StellarOrange, CircleShape)
                    )
                    Hexagon(
                        size = 52.dp,
                        backgroundColor = StellarOrange,
                        content = { Text("🌟", fontSize = 26.sp) }
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))

            // 标题和状态
            Column {
                Text(
                    text = "恒星引擎",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EarthBrown
                )
                // 根据isLoading显示不同状态文字
                Text(
                    text = if (isLoading) "思考中..." else "在线",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLoading) StellarOrange else MossGreen
                )
            }
        }

        HorizontalDivider(color = EarthBrown.copy(alpha = 0.1f))  // 分隔线

        // ----- 消息列表区域 -----
        // LazyColumn：只渲染可见项，性能优化，适合长列表
        LazyColumn(
            state = listState,  // 绑定滚动状态，用于自动滚动
            modifier = Modifier
                .weight(1f)     // 占据所有剩余垂直空间
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),  // 消息间距
            contentPadding = PaddingValues(vertical = 16.dp)    // 列表内边距
        ) {
            // 渲染所有消息
            items(messages) { message ->
                MessageBubble(message = message)
            }

            // 如果正在加载，在底部显示加载动画
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = StellarOrange
                        )
                    }
                }
            }
        }

        // ----- AI 设计任务后的二次确认卡片 -----
        pendingCreateCommand?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StellarOrange.copy(alpha = 0.08f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AI 为你设计了一个学习任务草案",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = EarthBrown
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "你可以先看看本次回复中的任务描述，确认后再真正将其加入任务列表。",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                // 取消本次任务创建建议
                                pendingCreateCommand = null
                                messages = messages + ChatMessageUi(
                                    content = "已取消本次任务创建建议，如果需要我可以重新为你设计任务。",
                                    isUser = false,
                                    type = ResponseType.GENERAL
                                )
                            }
                        ) {
                            Text("暂不创建")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cmd = pendingCreateCommand ?: return@Button
                                val result = toolExecutor.execute(cmd)
                                pendingCreateCommand = null
                                messages = messages + ChatMessageUi(
                                    content = result.message,
                                    isUser = false,
                                    type = if (result.success) ResponseType.GENERAL else ResponseType.ERROR
                                )
                            }
                        ) {
                            Text("确认创建任务")
                        }
                    }
                }
            }
        }

        // 知识图谱预览卡片：让用户在 AI 对话中"看到卡片并可一键定位/查看"
        if (kgPreviewCourseId != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StellarOrange.copy(alpha = 0.08f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                val courseName = viewModel.courses.value.firstOrNull { it.id == kgPreviewCourseId }?.name
                    ?: "知识图谱"

                val limited = kgPreviewNodes.take(8)

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "知识图谱已更新（${courseName}）",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = EarthBrown
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (limited.isEmpty()) {
                        Text(
                            text = "当前还没有任何知识节点。",
                            style = MaterialTheme.typography.bodySmall,
                            color = EarthBrown.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "关键节点预览：",
                            style = MaterialTheme.typography.bodySmall,
                            color = EarthBrown.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        limited.forEach { node ->
                            Text(
                                text = "• ${node.name}（掌握度 ${(node.masteryLevel * 100).toInt()}%）",
                                style = MaterialTheme.typography.bodySmall,
                                color = EarthBrown.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                val finalCourseId = kgPreviewCourseId ?: return@TextButton
                                viewModel.openKnowledgeGraph(finalCourseId)
                            }
                        ) {
                            Text("打开图谱", color = StellarOrange)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val finalCourseId = kgPreviewCourseId ?: return@TextButton
                                val focusId = kgPreviewFocusNodeId
                                viewModel.openKnowledgeGraph(finalCourseId, focusId)
                            },
                            enabled = kgPreviewFocusNodeId != null
                        ) {
                            Text("定位到卡片", color = StellarOrange)
                        }
                    }
                }
            }
        }

        // ----- 快捷建议区域（只在消息少时显示）-----
        if (!isLoading && messages.size <= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),  // 可水平滚动
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 预设的快捷问题，点击直接发送
                listOf("今天怎么学", "我哪里薄弱", "提醒我复习", "高数怎么学").forEach { suggestion ->
                    SuggestionChip(
                        text = suggestion,
                        onClick = {
                            // 点击建议：立即发送该问题
                            val text = suggestion
                            inputText = ""  // 清空输入框
                            sendMessage(
                                text = text,
                                viewModel = viewModel,
                                scope = scope,
                                currentMessages = messages,
                                onUpdateMessages = { messages = it },      // 更新消息列表的回调
                                onLoadingChange = { isLoading = it },      // 更新加载状态的回调
                                toolExecutor = toolExecutor,
                                onPendingCreate = { cmd -> pendingCreateCommand = cmd },
                                buildContext = { history ->
                                    val historySnippet = history.takeLast(6).joinToString("\n") { msg ->
                                        val who = if (msg.isUser) "用户" else "AI"
                                        "$who：${msg.content}"
                                    }
                                    buildString {
                                        append(buildLearningContext())
                                        if (historySnippet.isNotBlank()) {
                                            append("\n\n【对话历史】\n")
                                            append(historySnippet)
                                        }
                                    }
                                },
                                onKnowledgeGraphPreview = onKgPreview,
                                onComplete = { }
                            )
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = EarthBrown.copy(alpha = 0.1f))

        // ----- 底部输入区域 -----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文本输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },  // 更新输入文本
                placeholder = {
                    Text("输入消息...", color = EarthBrown.copy(alpha = 0.5f))
                },
                modifier = Modifier.weight(1f),  // 占据大部分宽度
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DawnWhite,      // 聚焦时背景
                    unfocusedContainerColor = DawnWhite,    // 未聚焦时背景
                    focusedBorderColor = StellarOrange,     // 聚焦时边框（橙色）
                    unfocusedBorderColor = EarthBrown.copy(alpha = 0.2f)  // 未聚焦时边框
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send  // 键盘右下角显示"发送"按钮
                ),
                keyboardActions = KeyboardActions(
                    onSend = {  // 点击键盘发送按钮
                        if (inputText.isNotBlank() && !isLoading) {
                            val text = inputText
                            inputText = ""
                            sendMessage(
                                text = text,
                                viewModel = viewModel,
                                scope = scope,
                                currentMessages = messages,
                                onUpdateMessages = { messages = it },
                                onLoadingChange = { isLoading = it },
                                toolExecutor = toolExecutor,
                                onPendingCreate = { cmd -> pendingCreateCommand = cmd },
                                buildContext = { history ->
                                    val historySnippet = history.takeLast(6).joinToString("\n") { msg ->
                                        val who = if (msg.isUser) "用户" else "AI"
                                        "$who：${msg.content}"
                                    }
                                    buildString {
                                        append(buildLearningContext())
                                        if (historySnippet.isNotBlank()) {
                                            append("\n\n【对话历史】\n")
                                            append(historySnippet)
                                        }
                                    }
                                },
                                onKnowledgeGraphPreview = onKgPreview,
                                onComplete = { }
                            )
                        }
                    }
                ),
                maxLines = 3  // 最多显示3行
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 发送按钮（六边形）
            // 使用Box包裹Hexagon，因为Hexagon可能不支持modifier参数
            Box(
                modifier = Modifier.clickable(
                    enabled = inputText.isNotBlank() && !isLoading  // 有内容且不在加载时才可点击
                ) {
                    val text = inputText
                    inputText = ""
                    sendMessage(
                        text = text,
                        viewModel = viewModel,
                        scope = scope,
                        currentMessages = messages,
                        onUpdateMessages = { messages = it },
                        onLoadingChange = { isLoading = it },
                        toolExecutor = toolExecutor,
                        onPendingCreate = { cmd -> pendingCreateCommand = cmd },
                        buildContext = { history ->
                            val historySnippet = history.takeLast(6).joinToString("\n") { msg ->
                                val who = if (msg.isUser) "用户" else "AI"
                                "$who：${msg.content}"
                            }
                            buildString {
                                append(buildLearningContext())
                                if (historySnippet.isNotBlank()) {
                                    append("\n\n【对话历史】\n")
                                    append(historySnippet)
                                }
                            }
                        },
                        onKnowledgeGraphPreview = onKgPreview,
                        onComplete = { }
                    )
                }
            ) {
                Hexagon(
                    size = 48.dp,
                    backgroundColor = if (inputText.isNotBlank() && !isLoading)
                        StellarOrange      // 可发送：橙色
                    else
                        EarthBrown.copy(alpha = 0.2f),  // 不可发送：灰色
                    content = {
                        Text(
                            "➤",  // 发送箭头图标
                            fontSize = 20.sp,
                            color = if (inputText.isNotBlank() && !isLoading)
                                DawnWhite      // 可发送：白色
                            else
                                EarthBrown.copy(alpha = 0.4f)  // 不可发送：淡色
                        )
                    }
                )
            }
        }
    }
    }
}

/**
 * 发送消息的处理函数
 * 在后台协程中调用AI服务，避免阻塞UI线程
 *
 * @param text 要发送的消息文本
 * @param scope 协程作用域，用于启动后台任务
 * @param currentMessages 当前消息列表（用于构建新列表）
 * @param onUpdateMessages 更新消息列表的回调（在主线程执行）
 * @param onLoadingChange 更新加载状态的回调
 * @param onComplete 操作完成后的回调（如自动滚动）
 */
private fun sendMessage(
    text: String,
    viewModel: AppViewModel,
    scope: CoroutineScope,
    currentMessages: List<ChatMessageUi>,
    onUpdateMessages: (List<ChatMessageUi>) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    toolExecutor: AiToolExecutor,
    onPendingCreate: (ToolCommand) -> Unit,
    buildContext: (List<ChatMessageUi>) -> String,
    onKnowledgeGraphPreview: (courseIdOrName: String, focusNodeName: String?) -> Unit = { _, _ -> },
    onComplete: () -> Unit = {}  // 默认空实现
) {
    // 1. 立即添加用户消息到列表（UI立刻显示）
    val userMessage = ChatMessageUi(
        content = text,
        isUser = true,
        type = ResponseType.GENERAL
    )
    onUpdateMessages(currentMessages + userMessage)
    onLoadingChange(true)  // 显示加载动画

    // 2. 在IO线程（后台线程）中调用AI服务
    scope.launch(Dispatchers.IO) {
        try {
            val context = buildContext(currentMessages + userMessage)
            // 调用SimpleAIService，支持本地规则和云端AI
            val response = SimpleAIService.process(text, context)

            // 切换回主线程更新UI
            withContext(Dispatchers.Main) {
                onLoadingChange(false)

                val baseAiMessage = ChatMessageUi(
                    content = response.content,
                    isUser = false,  // AI消息
                    type = response.type  // 使用AI返回的类型（计划/分析等）
                )

                val messages = mutableListOf<ChatMessageUi>()
                messages += currentMessages
                messages += userMessage
                messages += baseAiMessage

                // 解析并执行工具指令（如果有）
                val cmd = ToolCommandParser.parse(response.toolCommandJson)
                if (cmd != null) {
                    if (cmd.action == ToolAction.CREATE_TASK) {
                        // 对于"创建任务"类操作，先走用户二次确认，不立刻写入任务系统
                        onPendingCreate(cmd)
                    } else {
                        val result = toolExecutor.execute(cmd)

                        // 知识图谱相关：生成后在聊天区域给出"卡片预览 + 定位入口"
                        if (
                            cmd.action == ToolAction.UPSERT_KG_NODE ||
                            cmd.action == ToolAction.DELETE_KG_NODE ||
                            cmd.action == ToolAction.UPDATE_KG_NODE ||
                            cmd.action == ToolAction.LIST_KG_NODES
                        ) {
                            val params = json.parseToJsonElement(cmd.rawParamsJson).jsonObject
                            val courseIdOrName = (params["courseId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                            val name = (params["name"] as? kotlinx.serialization.json.JsonPrimitive)?.content

                            if (!courseIdOrName.isNullOrBlank()) {
                                val focusName = when (cmd.action) {
                                    ToolAction.LIST_KG_NODES, ToolAction.DELETE_KG_NODE -> null
                                    else -> name
                                }
                                onKnowledgeGraphPreview(courseIdOrName, focusName)
                            }
                        }

                        if (result.message.isNotBlank()) {
                            messages += ChatMessageUi(
                                content = result.message,
                                isUser = false,
                                type = if (result.success) ResponseType.GENERAL else ResponseType.ERROR
                            )
                        }
                    }
                }

                onUpdateMessages(messages)
                onComplete()
            }
        } catch (e: Exception) {
            // 发生错误（如网络问题）
            withContext(Dispatchers.Main) {
                onLoadingChange(false)
                val errorMessage = ChatMessageUi(
                    content = "连接失败，请检查网络 🌑\n试试本地功能：「今天怎么学」「我哪里薄弱」",
                    isUser = false,
                    type = ResponseType.ERROR  // 错误类型
                )
                onUpdateMessages(currentMessages + userMessage + errorMessage)
                onComplete()
            }
        }
    }
}

/**
 * 消息气泡组件
 * 类似微信的左右气泡布局：用户消息在右，AI消息在左
 *
 * @param message 消息数据对象
 */
@Composable
fun MessageBubble(message: ChatMessageUi) {
    // 根据发送者选择颜色和布局
    val backgroundColor = if (message.isUser)
        StellarOrange.copy(alpha = 0.9f)  // 用户：橙色气泡
    else
        DawnWhite                          // AI：白色气泡

    val textColor = if (message.isUser) DawnWhite else EarthBrown

    // 整体行布局，根据发送者选择对齐方式
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        // AI消息：左侧显示头像
        if (!message.isUser) {
            Hexagon(
                size = 36.dp,
                backgroundColor = StellarOrange,
                content = { Text("🌟", fontSize = 16.sp) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // 消息内容列
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // 类型标签（只显示AI的特殊类型消息）
            if (!message.isUser && message.type != ResponseType.GENERAL) {
                val typeLabel = when (message.type) {
                    ResponseType.PLAN -> "📋 学习计划"
                    ResponseType.ANALYSIS -> "📊 分析报告"
                    ResponseType.REVIEW -> "🔄 复习提醒"
                    ResponseType.EXPLANATION -> "💡 知识讲解"
                    ResponseType.ERROR -> "⚠️ 提示"
                    else -> ""
                }
                if (typeLabel.isNotEmpty()) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = StellarOrange,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // 消息气泡（使用Surface实现阴影和边框）
            Surface(
                color = backgroundColor,
                shape = MaterialTheme.shapes.large,  // 大圆角
                shadowElevation = if (message.isUser) 0.dp else 2.dp,  // AI消息有轻微阴影
                border = if (!message.isUser)
                    BorderStroke(1.dp, EarthBrown.copy(alpha = 0.1f))  // AI消息有细边框
                else
                    null
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp  // 行高，提高可读性
                )
            }
        }

        // 用户消息：右侧显示头像
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Hexagon(
                size = 36.dp,
                backgroundColor = MossGreen,  // 用户用绿色区分
                content = { Text("🌱", fontSize = 16.sp) }  // 植物emoji代表用户
            )
        }
    }
}

/**
 * 建议标签组件
 * 显示为圆角边框按钮，点击后发送对应文本
 */
@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = StellarOrange.copy(alpha = 0.1f),  // 很淡的橙色背景
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, StellarOrange.copy(alpha = 0.3f)),  // 橙色边框
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = StellarOrange,  // 橙色文字
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// 文件结束