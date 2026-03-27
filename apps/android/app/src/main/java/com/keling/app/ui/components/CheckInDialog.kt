package com.keling.app.ui.components

/**
 * =========================
 * 每日签到弹窗组件
 * =========================
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.keling.app.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keling.app.data.CHECK_IN_REWARDS
import com.keling.app.data.CheckInReward
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel

/**
 * 签到弹窗
 */
@Composable
fun CheckInDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val consecutiveDays = viewModel.getConsecutiveCheckInDays()
    val hasCheckedIn = viewModel.hasCheckedInToday()

    var showRewardAnimation by remember { mutableStateOf(false) }
    var lastReward by remember { mutableStateOf<CheckInReward?>(null) }

    // 签到成功后显示动画
    LaunchedEffect(hasCheckedIn) {
        if (hasCheckedIn && lastReward != null) {
            showRewardAnimation = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CreamWhite,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = if (hasCheckedIn) "今日已签到 ✨" else "每日签到",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (hasCheckedIn) "已连续签到 $consecutiveDays 天" else "签到获取奖励",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 连续签到天数展示
                StreakDisplay(consecutiveDays = consecutiveDays)

                Spacer(modifier = Modifier.height(24.dp))

                // 7天奖励日历
                WeeklyRewardCalendar(
                    consecutiveDays = consecutiveDays,
                    hasCheckedInToday = hasCheckedIn
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 操作按钮
                if (!hasCheckedIn) {
                    // 签到按钮
                    val infiniteTransition = rememberInfiniteTransition(label = "button")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Surface(
                        onClick = {
                            lastReward = viewModel.checkIn()
                            if (lastReward != null) {
                                showRewardAnimation = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale),
                        shape = RoundedCornerShape(16.dp),
                        color = WarmSunOrange
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "签到领取奖励",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // 已签到状态
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MintGreen.copy(alpha = 0.2f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "明天再来吧~",
                                style = MaterialTheme.typography.titleMedium,
                                color = MintGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("关闭", color = EarthBrownLight)
                }
            }
        }
    }

    // 奖励动画
    if (showRewardAnimation && lastReward != null) {
        RewardAnimationPopup(
            reward = lastReward!!,
            onDismiss = { showRewardAnimation = false }
        )
    }
}

/**
 * 连续签到天数展示
 */
@Composable
private fun StreakDisplay(consecutiveDays: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // 光晕
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(24.dp)
                .graphicsLayer { alpha = glowAlpha }
                .background(WarmSunOrange, CircleShape)
        )

        Surface(
            modifier = Modifier.size(108.dp),
            shape = CircleShape,
            color = WarmSunOrange.copy(alpha = 0.2f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_icon),
                        contentDescription = "连续",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = consecutiveDays.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = WarmSunOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.labelMedium,
                        color = EarthBrown
                    )
                }
            }
        }
    }
}

/**
 * 7天奖励日历
 */
@Composable
private fun WeeklyRewardCalendar(
    consecutiveDays: Int,
    hasCheckedInToday: Boolean
) {
    val currentDayInCycle = ((consecutiveDays - 1) % 7) + 1
    val todayIndex = if (hasCheckedInToday) currentDayInCycle - 1 else currentDayInCycle - 1

    Column {
        Text(
            text = "本周奖励",
            style = MaterialTheme.typography.titleSmall,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(CHECK_IN_REWARDS) { index, reward ->
                val isCompleted = index < currentDayInCycle || (hasCheckedInToday && index == currentDayInCycle - 1)
                val isToday = index == currentDayInCycle - 1 && !hasCheckedInToday

                DayRewardItem(
                    day = reward.day,
                    energy = reward.energy,
                    crystals = reward.crystals,
                    isCompleted = isCompleted,
                    isToday = isToday,
                    isSpecial = reward.isSpecial
                )
            }
        }
    }
}

@Composable
private fun DayRewardItem(
    day: Int,
    energy: Int,
    crystals: Int,
    isCompleted: Boolean,
    isToday: Boolean,
    isSpecial: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "item")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isToday) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val backgroundColor = when {
        isCompleted -> MintGreen.copy(alpha = 0.2f)
        isToday -> WarmSunOrange.copy(alpha = 0.2f)
        isSpecial -> LavenderPurple.copy(alpha = 0.15f)
        else -> BeigeSurface
    }

    val borderColor = when {
        isToday -> WarmSunOrange
        isSpecial && !isCompleted -> LavenderPurple
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .size(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (borderColor != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        } else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isCompleted) {
                Image(
                    painter = painterResource(id = R.drawable.ic_check_complete),
                    contentDescription = "已完成",
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "D$day",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) WarmSunOrange else EarthBrownLight,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "⚡$energy",
                style = MaterialTheme.typography.labelSmall,
                color = if (isCompleted) MintGreen else if (isToday) WarmSunOrange else EarthBrownLight
            )
        }
    }
}

/**
 * 奖励动画弹窗
 */
@Composable
private fun RewardAnimationPopup(
    reward: CheckInReward,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        visible = false
        kotlinx.coroutines.delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CreamWhite,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "成功",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "签到成功！",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RewardDisplay(icon = "⚡", value = "+${reward.energy}", color = WarmSunOrange)
                    RewardDisplay(icon = "💎", value = "+${reward.crystals}", color = LavenderPurple)
                }
                if (reward.isSpecial) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = reward.specialReward ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = LavenderPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardDisplay(
    icon: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}