package com.keling.app.ui.screens.settings

/**
 * =========================
 * 田园治愈风设置页面
 * =========================
 */

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.ui.theme.*

@Composable
fun PastoralSettingsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    // 设置状态
    var notificationEnabled by remember { mutableStateOf(true) }
    var studyReminderEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(false) }
    var autoCheckInReminder by remember { mutableStateOf(true) }

    // 关于对话框
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
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
                SettingsHeader(onBack = onBack)
            }

            // 账户设置
            item {
                SettingsSection(title = "账户") {
                    SettingItem(
                        icon = "👤",
                        title = "个人资料",
                        subtitle = "修改昵称和头像",
                        onClick = onNavigateToProfile
                    )
                    SettingItem(
                        icon = "🏆",
                        title = "成就管理",
                        subtitle = "查看已解锁成就",
                        onClick = onNavigateToAchievements
                    )
                }
            }

            // 通知设置
            item {
                SettingsSection(title = "通知") {
                    SettingSwitchItem(
                        icon = "🔔",
                        title = "推送通知",
                        subtitle = "接收学习提醒和任务通知",
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                    SettingSwitchItem(
                        icon = "⏰",
                        title = "学习提醒",
                        subtitle = "每日固定时间提醒学习",
                        checked = studyReminderEnabled,
                        onCheckedChange = { studyReminderEnabled = it }
                    )
                    SettingSwitchItem(
                        icon = "📅",
                        title = "签到提醒",
                        subtitle = "每日签到提醒通知",
                        checked = autoCheckInReminder,
                        onCheckedChange = { autoCheckInReminder = it }
                    )
                }
            }

            // 效果设置
            item {
                SettingsSection(title = "效果") {
                    SettingSwitchItem(
                        icon = "🔊",
                        title = "音效",
                        subtitle = "按钮点击和完成任务的音效",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    SettingSwitchItem(
                        icon = "📳",
                        title = "震动反馈",
                        subtitle = "交互时的震动提示",
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }
            }

            // 其他
            item {
                SettingsSection(title = "其他") {
                    SettingItem(
                        icon = "📖",
                        title = "使用指南",
                        subtitle = "了解如何使用课灵",
                        onClick = { /* 打开使用指南 */ }
                    )
                    SettingItem(
                        icon = "💬",
                        title = "意见反馈",
                        subtitle = "帮助我们改进应用",
                        onClick = { /* 打开反馈 */ }
                    )
                    SettingItem(
                        icon = "ℹ️",
                        title = "关于课灵",
                        subtitle = "版本 3.0.0",
                        onClick = { showAboutDialog = true }
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
 * 页面头部
 */
@Composable
private fun SettingsHeader(onBack: () -> Unit) {
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
                text = "设置",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "应用配置",
                style = MaterialTheme.typography.labelSmall,
                color = MintGreen
            )
        }

        // 占位
        Spacer(modifier = Modifier.width(52.dp))
    }
}

/**
 * 设置区块
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
                Column(content = content)
            }
        }
    }
}

/**
 * 设置项（可点击）
 */
@Composable
fun SettingItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BeigeSurface
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 文字
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight
            )
        }

        // 箭头
        Text(text = "›", fontSize = 20.sp, color = EarthBrownLight)
    }
}

/**
 * 设置项（开关）
 */
@Composable
fun SettingSwitchItem(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (checked) MintGreen.copy(alpha = 0.2f) else BeigeSurface
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 文字
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight
            )
        }

        // 开关
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MintGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = WarmGray
            )
        )
    }
}

/**
 * 关于对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CreamWhite
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "课灵Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "课灵",
                style = MaterialTheme.typography.headlineSmall,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "版本 3.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "一款AI驱动的学习管理应用\n让学习像培育星球一样有趣",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(onClick = onDismiss) {
                Text("关闭", color = WarmSunOrange)
            }
        }
    }
}
}