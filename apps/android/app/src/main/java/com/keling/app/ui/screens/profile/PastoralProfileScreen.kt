package com.keling.app.ui.screens.profile

/**
 * =========================
 * 田园治愈风个人中心页面
 * =========================
 *
 * 特点：
 * - 用户信息展示与编辑
 * - 学习统计数据可视化
 * - 成就入口
 * - 签到入口
 */

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.keling.app.R
import com.keling.app.data.Achievement
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel

@Composable
fun PastoralProfileScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val user = viewModel.currentUser.value
    val statistics = viewModel.getStatisticsSummary()
    val achievements = viewModel.achievements.value

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(user.name) }

    // 编辑名称对话框
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = editingName,
            onNameChange = { editingName = it },
            onConfirm = {
                if (editingName.isNotBlank()) {
                    viewModel.updateUserName(editingName)
                }
                showEditNameDialog = false
            },
            onDismiss = {
                editingName = user.name
                showEditNameDialog = false
            }
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
            // 顶部导航
            item {
                ProfileHeader(onBack = onBack, onSettings = onNavigateToSettings)
            }

            // 用户卡片
            item {
                UserProfileCard(
                    user = user,
                    unlockedAchievements = statistics.unlockedAchievements,
                    totalAchievements = achievements.size,
                    onEditName = {
                        editingName = user.name
                        showEditNameDialog = true
                    }
                )
            }

            // 学习统计
            item {
                StatisticsSection(
                    statistics = statistics,
                    onViewReport = onNavigateToReport
                )
            }

            // 快捷功能
            item {
                QuickActionsSection(
                    onAchievements = onNavigateToAchievements,
                    onSettings = onNavigateToSettings,
                    onReport = onNavigateToReport,
                    streakDays = statistics.streakDays,
                    unlockedAchievements = statistics.unlockedAchievements
                )
            }

            // 成就预览
            item {
                AchievementPreview(
                    achievements = achievements.filter { it.isUnlocked }.take(4),
                    totalUnlocked = statistics.unlockedAchievements,
                    onViewAll = onNavigateToAchievements
                )
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
 * 页面头部
 */
@Composable
private fun ProfileHeader(
    onBack: () -> Unit,
    onSettings: () -> Unit
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
                text = "个人中心",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "记录你的成长",
                style = MaterialTheme.typography.labelSmall,
                color = MintGreen
            )
        }

        // 设置按钮
        Surface(
            onClick = onSettings,
            shape = RoundedCornerShape(12.dp),
            color = MintGreen.copy(alpha = 0.2f)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "设置",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 用户信息卡片
 */
@Composable
private fun UserProfileCard(
    user: com.keling.app.data.User,
    unlockedAchievements: Int,
    totalAchievements: Int,
    onEditName: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile")

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
            .drawBehind {
                drawRoundRect(
                    color = WarmSunOrange.copy(alpha = glowAlpha),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 光晕
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(20.dp)
                            .graphicsLayer { alpha = glowAlpha }
                            .background(WarmSunOrange, CircleShape)
                    )

                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = WarmSunOrange
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // 显示用户头像或首字母
                            if (!user.avatarUrl.isNullOrEmpty()) {
                                // 使用 AsyncImage 加载网络头像
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = "用户头像",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // 显示用户名首字母
                                Text(
                                    text = user.name.firstOrNull()?.toString() ?: "🧑‍🚀",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (user.name.isNotEmpty()) 36.sp else 40.sp
                                )
                            }
                        }
                    }

                    // 等级徽章
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = MintGreen
                    ) {
                        Text(
                            text = "Lv.${user.level}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            // 名称（可编辑）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEditName() }
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "✏️", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "星际园丁",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 资源展示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResourceDisplay(icon = "⚡", value = user.energy, label = "能量", color = WarmSunOrange)
                ResourceDisplay(icon = "💎", value = user.crystals, label = "结晶", color = LavenderPurple)
                ResourceDisplay(icon = "🏆", value = unlockedAchievements, label = "成就", color = CreamYellow)
            }
            }
        }
    }
}

@Composable
private fun ResourceDisplay(
    icon: String,
    value: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrownLight
        )
    }
}

/**
 * 统计区块
 */
@Composable
private fun StatisticsSection(
    statistics: com.keling.app.viewmodel.StatisticsSummary,
    onViewReport: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "学习统计",
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = onViewReport) {
                        Text("查看报告", color = WarmSunOrange, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 统计数据网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = "📚",
                    value = "${statistics.totalStudyMinutes}分钟",
                    label = "累计学习",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "🔥",
                    value = "${statistics.streakDays}天",
                    label = "连续学习",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = "✅",
                    value = "${statistics.completedTasks}",
                    label = "完成任务",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "📝",
                    value = "${statistics.totalNotes}",
                    label = "学习笔记",
                    modifier = Modifier.weight(1f)
                )
            }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = BeigeSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrownLight
            )
        }
    }
}

/**
 * 快捷功能区块
 */
@Composable
private fun QuickActionsSection(
    onAchievements: () -> Unit,
    onSettings: () -> Unit,
    onReport: () -> Unit,
    streakDays: Int,
    unlockedAchievements: Int
) {
    Column {
        Text(
            text = "快捷功能",
            style = MaterialTheme.typography.titleMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = "🏆",
                title = "成就",
                subtitle = "已解锁${unlockedAchievements}个",
                color = CreamYellow,
                onClick = onAchievements,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = "📊",
                title = "报告",
                subtitle = "学习分析",
                color = SkyBlue,
                onClick = onReport,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = "⚙️",
                title = "设置",
                subtitle = "应用配置",
                color = LavenderPurple,
                onClick = onSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight
                )
            }
        }
    }
}

/**
 * 成就预览
 */
@Composable
private fun AchievementPreview(
    achievements: List<Achievement>,
    totalUnlocked: Int,
    onViewAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已解锁成就",
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = onViewAll) {
                        Text("查看全部", color = WarmSunOrange, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (achievements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有解锁成就，快去学习吧~",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementChip(achievement = achievement)
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun AchievementChip(achievement: Achievement) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CreamYellow.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = achievement.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 编辑名称对话框
 */
@Composable
private fun EditNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CreamWhite,
        title = {
            Text(
                text = "修改昵称",
                style = MaterialTheme.typography.headlineSmall,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入新昵称", color = EarthBrownLight) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmSunOrange,
                    unfocusedBorderColor = WarmGray,
                    focusedContainerColor = BeigeSurfaceLight,
                    unfocusedContainerColor = BeigeSurfaceLight
                )
            )
        },
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                color = WarmSunOrange,
                enabled = currentName.isNotBlank()
            ) {
                Text(
                    text = "确认",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = EarthBrownLight)
            }
        }
    )
}