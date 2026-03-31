package com.keling.app.ui.screens.home

/**
 * =========================
 * 田园治愈风首页 - 增强版
 * 星球花园
 * =========================
 *
 * 特点：
 * - 丰富的装饰元素与图案
 * - 游戏化的卡片设计
 * - 多层次的视觉效果
 * - 流畅的动画交互
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.keling.app.R
import com.keling.app.data.Course
import com.keling.app.data.Task
import com.keling.app.data.TaskStatus
import com.keling.app.data.CheckInRecord
import com.keling.app.ui.theme.*
import com.keling.app.ui.components.CheckInCalendar
import com.keling.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import java.util.Calendar
import java.time.YearMonth
import kotlin.math.*
import kotlin.random.Random

// ==================== 星球资源文件名 ====================

val PLANET_STYLE_ASSET_FILE_NAMES = listOf(
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

fun resolvePlanetAssetFileName(course: Course): String {
    val idx = if (course.planetStyleIndex >= 0) {
        course.planetStyleIndex
    } else {
        kotlin.math.abs(course.id.hashCode()) % PLANET_STYLE_ASSET_FILE_NAMES.size
    }
    return PLANET_STYLE_ASSET_FILE_NAMES.getOrNull(idx) ?: PLANET_STYLE_ASSET_FILE_NAMES.first()
}

// ==================== 主页面 ====================

@Composable
fun PastoralHomeScreen(
    viewModel: AppViewModel,
    onNavigate: (String) -> Unit
) {
    val user = viewModel.currentUser.value
    val courses = viewModel.courses.value
    val tasks = viewModel.tasks.value
    val pendingTasks = tasks.count { it.status == TaskStatus.PENDING }
    val checkInRecords = viewModel.checkInRecords.value

    // 签到日历当前月份
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 滚动状态用于视差效果
    val scrollState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg_pastoral_landscape),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 漂浮花瓣装饰层
        EnhancedBackgroundLayers()

        // 主内容 - 带滑动效果
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // ===== 顶部欢迎区域 =====
            item {
                GlassmorphismGreetingHeader(user = user, pendingTasks = pendingTasks)
            }

            // ===== 星球精灵助手入口 =====
            item {
                GlassmorphismSpiritCard(
                    pendingTasks = pendingTasks,
                    onClick = { onNavigate("ai") }
                )
            }

            // ===== 签到日历 =====
            item {
                HomeCheckInCalendarSection(
                    records = checkInRecords,
                    currentMonth = currentMonth,
                    onMonthChange = { currentMonth = it }
                )
            }

            // ===== 我的星球花园 =====
            item {
                GlassmorphismSectionTitle(
                    title = "我的星球花园",
                    subtitle = "培育你的知识星球",
                    icon = "🌍",
                    actionText = if (courses.isNotEmpty()) "全部" else null,
                    onAction = { onNavigate("greenhouse") }
                )
            }

            item {
                if (courses.isEmpty()) {
                    GlassmorphismEmptyGardenCard(onClick = { onNavigate("greenhouse") })
                } else {
                    GlassmorphismPlanetGardenRow(
                        courses = courses,
                        onCourseClick = { viewModel.openCourseGreenhouse(it) }
                    )
                }
            }

            // ===== 今日任务简报 =====
            item {
                GlassmorphismSectionTitle(
                    title = "今日培育计划",
                    subtitle = "完成任务收获成长",
                    icon = "📋",
                    actionText = if (tasks.isNotEmpty()) "全部" else null,
                    onAction = { onNavigate("tasks") }
                )
            }

            item {
                if (tasks.isEmpty()) {
                    GlassmorphismEmptyTaskCard(onClick = { onNavigate("ai") })
                } else {
                    GlassmorphismTaskBriefList(
                        tasks = tasks.take(3),
                        onTaskClick = { viewModel.openTaskDetail(it) }
                    )
                }
            }

            // 底部留白（为固定底栏留出空间）
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ==================== 增强版组件 ====================

/**
 * 多层背景效果
 */
@Composable
private fun EnhancedBackgroundLayers() {
    // 点阵图案层
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dotSpacing = 32.dp.toPx()
        val dotRadius = 2.dp.toPx()
        var x = dotSpacing
        while (x < size.width) {
            var y = dotSpacing
            while (y < size.height) {
                drawCircle(
                    color = WarmSunOrange.copy(alpha = 0.04f),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                y += dotSpacing
            }
            x += dotSpacing
        }
    }

    // 漂浮花瓣
    val petals = remember { List(15) { FloatingPetalData() } }
    petals.forEach { petal ->
        AnimatedFloatingPetal(petal = petal)
    }
}

private data class FloatingPetalData(
    val x: Float = Random.nextFloat(),
    val startY: Float = -0.1f - Random.nextFloat() * 0.5f,
    val size: Int = Random.nextInt(6, 14),
    val speed: Int = Random.nextInt(15000, 25000),
    val alpha: Float = Random.nextFloat() * 0.1f + 0.03f,
    val color: Color = listOf(WarmSunOrange, PeachPink, CreamYellow, MintGreen, LavenderPurple).random(),
    val rotation: Float = Random.nextFloat() * 360f
)

@Composable
private fun AnimatedFloatingPetal(petal: FloatingPetalData) {
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
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + Random.nextInt(1000), easing = EaseInOutSine),
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
        val y = petal.startY + (1.3f - petal.startY) * progress
        val x = petal.x + sway / size.width

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
            center.x + size * 0.7f, center.y - size * 0.4f,
            center.x + size * 0.7f, center.y + size * 0.4f,
            center.x, center.y + size * 0.25f
        )
        cubicTo(
            center.x - size * 0.7f, center.y + size * 0.4f,
            center.x - size * 0.7f, center.y - size * 0.4f,
            center.x, center.y - size
        )
        close()
    }
}

/**
 * 增强版问候头部
 */
@Composable
private fun EnhancedGreetingHeader(
    user: com.keling.app.data.User,
    pendingTasks: Int
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "早安"
        in 12..17 -> "午安"
        else -> "晚安"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "header")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer { translationY = floatOffset },
                    contentAlignment = Alignment.Center
                ) {
                    // 多层光晕
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(18.dp)
                            .graphicsLayer { alpha = glowAlpha }
                            .background(WarmSunOrange, CircleShape)
                    )

                    // 装饰环
                    Canvas(modifier = Modifier.size(64.dp)) {
                        drawCircle(
                            color = WarmSunOrange.copy(alpha = 0.3f),
                            radius = 30.dp.toPx(),
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = WarmSunOrange
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.toString() ?: "✿",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // 问候信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting，${user.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 等级徽章
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            WarmSunOrange.copy(alpha = 0.2f),
                                            PeachPink.copy(alpha = 0.15f)
                                        )
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⭐",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lv.${user.level}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WarmSunOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "星球园丁",
                            style = MaterialTheme.typography.bodySmall,
                            color = EarthBrown.copy(alpha = 0.6f)
                        )
                    }
                }

                // 资源显示
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedResourceBadge(icon = "⚡", value = user.energy, color = WarmSunOrange)
                    EnhancedResourceBadge(icon = "✿", value = user.crystals, color = LavenderPurple)
                }
            }
        }
}

/**
 * 增强版资源徽章
 */
@Composable
private fun EnhancedResourceBadge(
    icon: String,
    value: Int,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * 增强版精灵卡片
 */
@Composable
private fun EnhancedSpiritCard(
    pendingTasks: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spirit")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val sparkleRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            WarmSunOrange.copy(alpha = 0.05f),
                            PeachPink.copy(alpha = 0.03f)
                        )
                    )
                )
        ) {
            // 装饰性星星
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(100.dp)
            ) {
                rotate(sparkleRotate) {
                    drawStar(
                        center = Offset(size.width * 0.7f, size.height * 0.3f),
                        color = WarmSunOrange.copy(alpha = 0.15f),
                        size = 30.dp.toPx()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 精灵图标
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_planet_sprite),
                            contentDescription = "星球精灵",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "星球精灵在线",
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (pendingTasks > 0) "$pendingTasks 个星球等待培育"
                        else "点击与精灵对话",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown.copy(alpha = 0.6f)
                    )
                }

                // 箭头按钮
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, color: Color, size: Float) {
    val path = Path()
    for (i in 0..4) {
        val angle = i * 72f - 90f
        val rad = angle * PI / 180f
        val x = center.x + cos(rad).toFloat() * size
        val y = center.y + sin(rad).toFloat() * size

        val innerAngle = angle + 36f
        val innerRad = innerAngle * PI / 180f
        val innerX = center.x + cos(innerRad).toFloat() * size * 0.4f
        val innerY = center.y + sin(innerRad).toFloat() * size * 0.4f

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        path.lineTo(innerX, innerY)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

/**
 * 增强版区块标题
 */
@Composable
private fun EnhancedSectionTitle(
    title: String,
    subtitle: String? = null,
    icon: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WarmSunOrange.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (actionText != null && onAction != null) {
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                color = WarmSunOrange.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmSunOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * 增强版空花园卡片
 */
@Composable
private fun EnhancedEmptyGardenCard(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.8f)
    ) {
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
                // 多层光晕
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(25.dp)
                        .background(MintGreen.copy(alpha = 0.2f), CircleShape)
                )

                // 装饰环
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = MintGreen.copy(alpha = 0.2f),
                        radius = 45.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = MintGreen.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌍", fontSize = 38.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "花园还是空的",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "种下你的第一颗知识星球",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrown.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * 增强版星球花园行
 */
@Composable
private fun EnhancedPlanetGardenRow(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(courses) { course ->
            EnhancedPlanetGardenItem(
                course = course,
                onClick = { onCourseClick(course.id) }
            )
        }
    }
}

/**
 * 增强版星球花园项
 */
@Composable
private fun EnhancedPlanetGardenItem(
    course: Course,
    onClick: () -> Unit
) {
    val planetFileName = resolvePlanetAssetFileName(course)
    val infiniteTransition = rememberInfiniteTransition(label = "planet")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val planetColor = PlanetColors.Custom.getOrNull(course.planetStyleIndex % PlanetColors.Custom.size) ?: WarmSunOrange

    Surface(
        onClick = onClick,
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 星球图像
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                // 星球图片 - 直接显示，不带圆形框
                AsyncImage(
                    model = "file:///android_asset/$planetFileName",
                    contentDescription = course.name,
                    modifier = Modifier.size(78.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 课程名
            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 掌握度进度条
            val masteryPercent = (course.masteryLevel * 100).toInt()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(course.masteryLevel)
                            .background(planetColor, RoundedCornerShape(2.5.dp))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$masteryPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = planetColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 增强版快捷入口区域
 */
@Composable
private fun EnhancedQuickAccessSection(onNavigate: (String) -> Unit) {
    Column {
        EnhancedSectionTitle(
            title = "快捷入口",
            icon = "✨"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnhancedQuickAccessCard(
                icon = "🌱",
                title = "温室",
                subtitle = "星球培育",
                color = MintGreen,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("greenhouse") }
            )

            EnhancedQuickAccessCard(
                icon = "📋",
                title = "任务",
                subtitle = "培育计划",
                color = WarmSunOrange,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("tasks") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnhancedQuickAccessCard(
                icon = "👤",
                title = "我的",
                subtitle = "个人中心",
                color = SkyBlue,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("profile") }
            )

            EnhancedQuickAccessCard(
                icon = "📝",
                title = "笔记",
                subtitle = "知识记录",
                color = LavenderPurple,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("notes") }
            )
        }
    }
}

/**
 * 增强版快捷入口卡片
 */
@Composable
private fun EnhancedQuickAccessCard(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "access")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标容器
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(12.dp)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(color, CircleShape)
                )

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrown.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 增强版空任务卡片
 */
@Composable
private fun EnhancedEmptyTaskCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = WarmSunOrange.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎯", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "今日暂无培育计划",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "和星球精灵聊聊，让它帮你制定计划",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrown.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 增强版任务简报列表
 */
@Composable
private fun EnhancedTaskBriefList(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tasks.forEachIndexed { index, task ->
            EnhancedTaskCard(
                task = task,
                index = index,
                onClick = { onTaskClick(task.id) }
            )
        }
    }
}

/**
 * 增强版任务卡片
 */
@Composable
private fun EnhancedTaskCard(
    task: Task,
    index: Int,
    onClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        5 -> PriorityColors.urgent
        4 -> PriorityColors.high
        3 -> PriorityColors.medium
        else -> PriorityColors.low
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 任务编号
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = priorityColor.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 任务信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 优先级标签
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "P${task.priority}",
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${task.estimatedMinutes}分钟",
                        style = MaterialTheme.typography.labelSmall,
                        color = EarthBrown.copy(alpha = 0.5f)
                    )
                }
            }

            // 奖励
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_icon),
                        contentDescription = "能量",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${task.rewards.energy}",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmSunOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (task.rewards.crystals > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_icon),
                            contentDescription = "水晶",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${task.rewards.crystals}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LavenderPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== 玻璃拟态组件 (Glassmorphism Components) ====================

/**
 * 玻璃拟态问候头部
 */
@Composable
private fun GlassmorphismGreetingHeader(
    user: com.keling.app.data.User,
    pendingTasks: Int
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "早安"
        in 12..17 -> "午安"
        else -> "晚安"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "header")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = floatOffset }
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_home),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user_avatar),
                            contentDescription = "用户头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting，${user.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    WarmSunOrange.copy(alpha = 0.15f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_icon),
                                    contentDescription = "等级",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lv.${user.level}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WarmSunOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "星球园丁",
                            style = MaterialTheme.typography.bodySmall,
                            color = EarthBrown.copy(alpha = 0.6f)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassmorphismResourceBadge(icon = "⚡", value = user.energy, color = WarmSunOrange)
                    GlassmorphismResourceBadge(icon = "✿", value = user.crystals, color = LavenderPurple)
                }
            }
        }
    }
}

/**
 * 玻璃拟态资源徽章
 */
@Composable
private fun GlassmorphismResourceBadge(
    icon: String,
    value: Int,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * 玻璃拟态精灵卡片
 */
@Composable
private fun GlassmorphismSpiritCard(
    pendingTasks: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spirit")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = floatOffset }
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_home),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                    // 精灵图标
                    Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = Color.Transparent
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_planet_sprite),
                                contentDescription = "星球精灵",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "星球精灵在线",
                            style = MaterialTheme.typography.titleMedium,
                            color = EarthBrown,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (pendingTasks > 0) "$pendingTasks 个星球等待培育"
                            else "点击与精灵对话",
                            style = MaterialTheme.typography.bodySmall,
                            color = EarthBrown.copy(alpha = 0.6f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = WarmSunOrange
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
            }
        }
    }
}

/**
 * 玻璃拟态区块标题
 */
@Composable
private fun GlassmorphismSectionTitle(
    title: String,
    subtitle: String? = null,
    icon: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_section_icon),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (actionText != null && onAction != null) {
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmSunOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * 玻璃拟态空花园卡片
 */
@Composable
private fun GlassmorphismEmptyGardenCard(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = floatOffset }
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_home),
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
                Surface(
                    shape = CircleShape,
                    color = MintGreen.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌍", fontSize = 38.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "花园还是空的",
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "种下你的第一颗知识星球",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrown.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 玻璃拟态星球花园行
 */
@Composable
private fun GlassmorphismPlanetGardenRow(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(courses) { course ->
            GlassmorphismPlanetGardenItem(
                course = course,
                onClick = { onCourseClick(course.id) }
            )
        }
    }
}

/**
 * 玻璃拟态星球花园项
 */
@Composable
private fun GlassmorphismPlanetGardenItem(
    course: Course,
    onClick: () -> Unit
) {
    val planetFileName = resolvePlanetAssetFileName(course)
    val infiniteTransition = rememberInfiniteTransition(label = "planet")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800 + (course.id.hashCode() % 1000), easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val planetColor = PlanetColors.Custom.getOrNull(course.planetStyleIndex % PlanetColors.Custom.size) ?: WarmSunOrange

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(130.dp)
            .graphicsLayer { translationY = floatOffset }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(82.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "file:///android_asset/$planetFileName",
                    contentDescription = course.name,
                    modifier = Modifier.size(78.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = course.name,
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            val masteryPercent = (course.masteryLevel * 100).toInt()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(course.masteryLevel)
                            .background(planetColor, RoundedCornerShape(2.5.dp))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$masteryPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = planetColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 玻璃拟态快捷入口区域
 */
@Composable
private fun GlassmorphismQuickAccessSection(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        GlassmorphismSectionTitle(
            title = "快捷入口",
            icon = "✨"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 四个图标紧密排列成一列 - 自适应高度
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassmorphismQuickAccessCard(
                    iconRes = R.drawable.ic_greenhouse,
                    title = "温室",
                    subtitle = "星球培育",
                    color = MintGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("greenhouse") }
                )

                GlassmorphismQuickAccessCard(
                    iconRes = R.drawable.ic_tasks,
                    title = "任务",
                    subtitle = "培育计划",
                    color = WarmSunOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("tasks") }
                )

                GlassmorphismQuickAccessCard(
                    iconRes = R.drawable.ic_profile,
                    title = "我的",
                    subtitle = "个人中心",
                    color = SkyBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("profile") }
                )

                GlassmorphismQuickAccessCard(
                    iconRes = R.drawable.ic_notes,
                    title = "笔记",
                    subtitle = "知识记录",
                    color = LavenderPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("notes") }
                )
            }
    }
}

/**
 * 玻璃拟态快捷入口卡片
 */
@Composable
private fun GlassmorphismQuickAccessCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite.copy(alpha = 0.8f)
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_home),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            // 内容决定卡片大小
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标图片
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrown.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * 玻璃拟态空任务卡片
 */
@Composable
private fun GlassmorphismEmptyTaskCard(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = floatOffset }
            .clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_home),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = WarmSunOrange.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎯", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "今日暂无培育计划",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "和星球精灵聊聊，让它帮你制定计划",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrown.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 玻璃拟态任务简报列表
 */
@Composable
private fun GlassmorphismTaskBriefList(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tasks.forEachIndexed { index, task ->
            GlassmorphismTaskCard(
                task = task,
                index = index,
                onClick = { onTaskClick(task.id) }
            )
        }
    }
}

/**
 * 玻璃拟态任务卡片
 */
@Composable
private fun GlassmorphismTaskCard(
    task: Task,
    index: Int,
    onClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        5 -> PriorityColors.urgent
        4 -> PriorityColors.high
        3 -> PriorityColors.medium
        else -> PriorityColors.low
    }

    val infiniteTransition = rememberInfiniteTransition(label = "task")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + index * 300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = floatOffset }
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            // 背景图片 - 自适应卡片大小
            Image(
                painter = painterResource(id = R.drawable.bg_card_home),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = priorityColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "P${task.priority}",
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${task.estimatedMinutes}分钟",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrown.copy(alpha = 0.5f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_icon),
                            contentDescription = "能量",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${task.rewards.energy}",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarmSunOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (task.rewards.crystals > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_icon),
                                contentDescription = "水晶",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+${task.rewards.crystals}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 首页签到日历区块
 */
@Composable
private fun HomeCheckInCalendarSection(
    records: List<CheckInRecord>,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "calendar")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = WarmSunOrange.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
                    topLeft = Offset(-3.dp.toPx(), -3.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite.copy(alpha = 0.9f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "签到日历",
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📅",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CheckInCalendar(
                    records = records,
                    currentMonth = currentMonth,
                    onMonthChange = onMonthChange
                )
            }
        }
    }
}