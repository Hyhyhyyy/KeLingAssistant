package com.keling.app.ui.screens.greenhouse

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.keling.app.R
import com.keling.app.data.Course
import com.keling.app.data.Task
import com.keling.app.data.TaskStatus
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.keling.app.ui.components.CourseCreationWizard

/**
 * =========================
 * 田园治愈风温室
 * 星球培育花园
 * =========================
 *
 * 特点：
 * - 温暖的花园背景
 * - 星球作为可爱的"果实"
 * - 柔和的卡片设计
 * - 治愈系动画
 */

// ==================== 星球资源 ====================

val PLANET_ASSETS = listOf(
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

fun getPlanetAsset(index: Int): String {
    return PLANET_ASSETS.getOrNull(index % PLANET_ASSETS.size) ?: PLANET_ASSETS.first()
}

// ==================== 主页面 ====================

/**
 * 田园风温室页面
 */
@Composable
fun PastoralGreenhouseScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val courses = viewModel.courses.value
    val tasks = viewModel.tasks.value

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }

    // 使用新的课程创建向导
    if (showCreateDialog) {
        CourseCreationWizard(
            onDismiss = { showCreateDialog = false },
            onConfirm = { course ->
                viewModel.addCourse(course)
                showCreateDialog = false
            },
            editingCourse = editingCourse,
            existingCourses = courses
        )
    }

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
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // ===== 顶部导航 =====
            item {
                PastoralGreenhouseHeader(
                    onBack = onBack,
                    onAddPlanet = {
                        editingCourse = null
                        showCreateDialog = true
                    }
                )
            }

            // ===== 温室概览 =====
            item {
                GreenhouseOverview(
                    totalPlanets = courses.size,
                    healthyCount = courses.count { it.masteryLevel >= 0.6f },
                    needCareCount = courses.count { it.masteryLevel < 0.4f }
                )
            }

            // ===== 星球列表 =====
            if (courses.isEmpty()) {
                item {
                    EmptyGardenPlanetState(
                        onCreatePlanet = {
                            editingCourse = null
                            showCreateDialog = true
                        }
                    )
                }
            } else {
                items(courses) { course ->
                    val relatedTasks = tasks.filter { it.courseId == course.id }
                    val pendingTasks = relatedTasks.count { it.status == TaskStatus.PENDING }

                    PastoralPlanetCard(
                        course = course,
                        pendingTasks = pendingTasks,
                        onPlanetClick = { viewModel.openCourseGreenhouse(course.id) },
                        onEdit = {
                            editingCourse = course
                            showCreateDialog = true
                        }
                    )
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==================== 组件 ====================

/**
 * 温室头部
 */
@Composable
private fun PastoralGreenhouseHeader(
    onBack: () -> Unit,
    onAddPlanet: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = BeigeSurface,
            shadowElevation = 0.dp
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
            Text(
                text = "星球培育温室",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "呵护每一颗知识星球",
                style = MaterialTheme.typography.labelSmall,
                color = MintGreen
            )
        }

        // 添加按钮
        Surface(
            onClick = onAddPlanet,
            shape = RoundedCornerShape(12.dp),
            color = MintGreen.copy(alpha = 0.2f)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = MintGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 温室概览
 */
@Composable
private fun GreenhouseOverview(
    totalPlanets: Int,
    healthyCount: Int,
    needCareCount: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "overview")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = MintGreen.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                    topLeft = Offset(-4.dp.toPx(), -4.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = "🌍",
                value = totalPlanets.toString(),
                label = "培育星球",
                color = MintGreen
            )

            StatItem(
                icon = "✨",
                value = healthyCount.toString(),
                label = "茁壮成长",
                color = CreamYellow
            )

            StatItem(
                icon = "💧",
                value = needCareCount.toString(),
                label = "需要关爱",
                color = WarmSunOrange
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrownLight
        )
    }
}

/**
 * 田园风星球卡片
 */
@Composable
private fun PastoralPlanetCard(
    course: Course,
    pendingTasks: Int,
    onPlanetClick: () -> Unit,
    onEdit: () -> Unit
) {
    val planetAsset = getPlanetAsset(course.planetStyleIndex)
    val planetColor = PlanetColors.Custom.getOrNull(course.planetStyleIndex % PlanetColors.Custom.size) ?: WarmSunOrange

    val infiniteTransition = rememberInfiniteTransition(label = "planet")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 生长状态
    val growthStage = when {
        course.masteryLevel >= 0.8f -> GrowthStage.THRIVING
        course.masteryLevel >= 0.6f -> GrowthStage.HEALTHY
        course.masteryLevel >= 0.4f -> GrowthStage.GROWING
        else -> GrowthStage.NEEDS_CARE
    }

    Surface(
        onClick = onPlanetClick,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = planetColor.copy(alpha = glowAlpha * 0.5f),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
                    topLeft = Offset(-3.dp.toPx(), -3.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ===== 星球图像 =====
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // 星球图片 - 直接显示，不带圆形框
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            scaleX = breatheScale
                            scaleY = breatheScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "file:///android_asset/$planetAsset",
                        contentDescription = course.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 状态指示
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = growthStage.color,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = growthStage.icon,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ===== 星球信息 =====
            Column(modifier = Modifier.weight(1f)) {
                // 名称
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 状态标签
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = growthStage.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = growthStage.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = growthStage.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 掌握度进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BeigeSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(course.masteryLevel)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(planetColor, planetColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "掌握度 ${(course.masteryLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight
                )
            }

            // ===== 操作区 =====
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 编辑按钮
                Surface(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    color = BeigeSurface
                ) {
                    Text(
                        text = "✏️",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // 任务数量
                if (pendingTasks > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarmSunOrange.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_icon),
                                contentDescription = "任务",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$pendingTasks",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmSunOrange,
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
 * 生长阶段
 */
private enum class GrowthStage(
    val label: String,
    val icon: String,
    val color: Color
) {
    THRIVING("茁壮成长", "✨", CreamYellow),
    HEALTHY("健康成长", "🌱", MintGreen),
    GROWING("正在生长", "🌿", SkyBlue),
    NEEDS_CARE("需要关爱", "💧", WarmSunOrange)
}

/**
 * 空状态
 */
@Composable
private fun EmptyGardenPlanetState(
    onCreatePlanet: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = floatOffset }
                    .size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(MintGreen.copy(alpha = 0.2f), CircleShape)
                )

                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MintGreen.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_icon),
                            contentDescription = "课程",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "温室还是空的呢",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "种下你的第一颗知识星球\n开启你的星际培育之旅",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 创建按钮
            Surface(
                onClick = onCreatePlanet,
                shape = RoundedCornerShape(12.dp),
                color = MintGreen
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_icon),
                        contentDescription = "创建",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "种下星球",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 星球创建对话框
 */
@Composable
private fun PastoralPlanetCreationDialog(
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
        shape = RoundedCornerShape(24.dp),
        containerColor = CreamWhite,
        title = {
            Column {
                Text(
                    text = if (editingCourseId == null) "种下新星球" else "调整星球",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "为你的知识星球起个名字",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 名称输入
                OutlinedTextField(
                    value = dialogCourseName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "输入星球名称",
                            color = EarthBrownLight
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = WarmGray,
                        focusedContainerColor = BeigeSurfaceLight,
                        unfocusedContainerColor = BeigeSurfaceLight,
                        focusedTextColor = EarthBrown,
                        unfocusedTextColor = EarthBrown,
                        cursorColor = MintGreen
                    )
                )

                // 星球样式选择
                Text(
                    text = "选择星球外观",
                    style = MaterialTheme.typography.labelMedium,
                    color = EarthBrown
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(PLANET_ASSETS.size) { index ->
                        val asset = PLANET_ASSETS[index]
                        val isSelected = dialogPlanetStyleIndex == index

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MintGreen else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onStyleChange(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = "file:///android_asset/$asset",
                                contentDescription = "Planet $index",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                color = MintGreen,
                enabled = dialogCourseName.trim().isNotEmpty()
            ) {
                Text(
                    text = "确认种植",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = EarthBrownLight
                )
            }
        }
    )
}

/**
 * 背景装饰
 */
@Composable
private fun GreenhouseBackgroundDecorations() {
    val decorations = remember { List(6) { DecorationParticle() } }

    Box(modifier = Modifier.fillMaxSize()) {
        decorations.forEach { particle ->
            FloatingDecoration(particle = particle)
        }
    }
}

private class DecorationParticle {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val size = Random.nextInt(4, 10)
    val alpha = Random.nextFloat() * 0.08f + 0.03f
    val speed = Random.nextInt(4000, 8000)
}

@Composable
private fun FloatingDecoration(particle: DecorationParticle) {
    val alpha by rememberInfiniteTransition(label = "deco").animateFloat(
        initialValue = particle.alpha * 0.5f,
        targetValue = particle.alpha,
        animationSpec = infiniteRepeatable(
            animation = tween(particle.speed, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    color = MintGreen.copy(alpha = alpha),
                    radius = particle.size.dp.toPx(),
                    center = Offset(
                        x = particle.x * size.width,
                        y = particle.y * size.height
                    )
                )
            }
    )
}