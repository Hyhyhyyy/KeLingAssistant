package com.keling.app.ui.screens.achievements

/**
 * =========================
 * 田园治愈风成就系统页面
 * =========================
 *
 * 特点：
 * - 成就分类展示
 * - 解锁动画
 * - 进度追踪
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.Achievement
import com.keling.app.data.AchievementCategory
import com.keling.app.data.PREDEFINED_ACHIEVEMENTS
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel

@Composable
fun PastoralAchievementsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val achievements = viewModel.achievements.value
    val unlockedCount = achievements.count { it.isUnlocked }

    // 按类别分组
    val groupedAchievements = achievements.groupBy { it.category }

    // 筛选状态
    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }

    val displayAchievements = if (selectedCategory != null) {
        groupedAchievements[selectedCategory] ?: emptyList()
    } else {
        achievements
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
                AchievementsHeader(
                    onBack = onBack,
                    unlockedCount = unlockedCount,
                    totalCount = achievements.size
                )
            }

            // 总进度
            item {
                AchievementProgressCard(
                    unlockedCount = unlockedCount,
                    totalCount = achievements.size
                )
            }

            // 类别筛选
            item {
                CategoryFilterRow(
                    categories = AchievementCategory.values().toList(),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // 成就列表
            items(displayAchievements) { achievement ->
                AchievementCard(achievement = achievement)
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
private fun AchievementsHeader(
    onBack: () -> Unit,
    unlockedCount: Int,
    totalCount: Int
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
                text = "成就殿堂",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$unlockedCount / $totalCount 已解锁",
                style = MaterialTheme.typography.labelSmall,
                color = MintGreen
            )
        }

        // 占位
        Spacer(modifier = Modifier.width(52.dp))
    }
}

/**
 * 进度卡片
 */
@Composable
private fun AchievementProgressCard(
    unlockedCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "progress")

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
                    color = CreamYellow.copy(alpha = glowAlpha),
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
            // 大图标
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(CreamYellow, CircleShape)
                )

                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = CreamYellow.copy(alpha = 0.3f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_achievement),
                            contentDescription = "成就",
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "成就收集进度",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = CreamYellow,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BeigeSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(CreamYellow, CreamYellowDark)
                            ),
                            RoundedCornerShape(6.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AchievementStatBadge(icon = "✨", value = "$unlockedCount", label = "已解锁")
                AchievementStatBadge(icon = "🔒", value = "${totalCount - unlockedCount}", label = "待解锁")
            }
        }
        }
    }
}

@Composable
private fun AchievementStatBadge(
    icon: String,
    value: String,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrownLight
        )
    }
}

/**
 * 类别筛选行
 */
@Composable
private fun CategoryFilterRow(
    categories: List<AchievementCategory>,
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 全部选项
        item {
            CategoryChip(
                label = "全部",
                icon = "📋",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }

        items(categories) { category ->
            CategoryChip(
                label = getCategoryLabel(category),
                icon = getCategoryIcon(category),
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) WarmSunOrange.copy(alpha = 0.15f) else Color.Transparent,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, WarmSunOrange)
        } else null,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) WarmSunOrange else EarthBrown,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * 成就卡片
 */
@Composable
private fun AchievementCard(achievement: Achievement) {
    val infiniteTransition = rememberInfiniteTransition(label = "achievement")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = if (achievement.isUnlocked) 0.12f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val progressPercent = if (achievement.maxProgress > 0) {
        achievement.progress.toFloat() / achievement.maxProgress
    } else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val glowColor = if (achievement.isUnlocked) CreamYellow else WarmGray
                drawRoundRect(
                    color = glowColor.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // 图标区域
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(14.dp)
                            .graphicsLayer { alpha = glowAlpha * 2 }
                            .background(CreamYellow, CircleShape)
                    )
                }

                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = if (achievement.isUnlocked) {
                        CreamYellow.copy(alpha = 0.2f)
                    } else {
                        WarmGray.copy(alpha = 0.3f)
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (achievement.isUnlocked) achievement.icon else "🔒",
                            fontSize = if (achievement.isUnlocked) 32.sp else 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 信息区域
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (achievement.isUnlocked) EarthBrown else EarthBrown.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (!achievement.isUnlocked) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (achievement.isUnlocked) EarthBrownLight else EarthBrownLight.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 进度条
                if (!achievement.isUnlocked && achievement.maxProgress > 1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(BeigeSurface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressPercent)
                                    .background(WarmSunOrange, RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${achievement.progress}/${achievement.maxProgress}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmSunOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 奖励标签
                if (achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RewardTag(icon = "⚡", value = "+${achievement.rewardEnergy}", color = WarmSunOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        RewardTag(icon = "💎", value = "+${achievement.rewardCrystals}", color = LavenderPurple)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun RewardTag(
    icon: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== 辅助函数 ====================

private fun getCategoryLabel(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.LEARNING -> "学习"
        AchievementCategory.STREAK -> "坚持"
        AchievementCategory.EXPLORATION -> "探索"
        AchievementCategory.SOCIAL -> "社交"
        AchievementCategory.MASTERY -> "精通"
    }
}

private fun getCategoryIcon(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.LEARNING -> "📚"
        AchievementCategory.STREAK -> "🔥"
        AchievementCategory.EXPLORATION -> "🌍"
        AchievementCategory.SOCIAL -> "🤝"
        AchievementCategory.MASTERY -> "✨"
    }
}