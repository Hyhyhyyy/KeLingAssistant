package com.keling.app.ui.components

/**
 * =========================
 * 课程表导入组件
 * =========================
 *
 * 支持手动输入和图片识别（预留接口）
 */

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keling.app.R
import com.keling.app.data.ScheduleSlot
import com.keling.app.ui.theme.*
import java.util.Calendar

/**
 * 课程表导入对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleImportDialog(
    onDismiss: () -> Unit,
    onImport: (List<ScheduleSlot>) -> Unit,
    existingSlots: List<ScheduleSlot> = emptyList()
) {
    var importMode by remember { mutableStateOf(ImportMode.MANUAL) }
    var scheduleSlots by remember { mutableStateOf(existingSlots.toMutableList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CreamWhite,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "导入课程表",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 模式选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImportModeButton(
                        label = "手动输入",
                        isSelected = importMode == ImportMode.MANUAL,
                        onClick = { importMode = ImportMode.MANUAL },
                        modifier = Modifier.weight(1f)
                    )
                    ImportModeButton(
                        label = "图片识别",
                        isSelected = importMode == ImportMode.IMAGE,
                        onClick = { importMode = ImportMode.IMAGE },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (importMode) {
                    ImportMode.MANUAL -> {
                        ManualScheduleInput(
                            slots = scheduleSlots,
                            onAddSlot = { slot ->
                                scheduleSlots.add(slot)
                            },
                            onRemoveSlot = { slot ->
                                scheduleSlots.remove(slot)
                            }
                        )
                    }
                    ImportMode.IMAGE -> {
                        ImageScheduleImport(
                            onRecognized = { slots ->
                                scheduleSlots.addAll(slots)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = EarthBrownLight)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onImport(scheduleSlots) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen
                        )
                    ) {
                        Text("导入", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 导入模式
 */
enum class ImportMode {
    MANUAL,  // 手动输入
    IMAGE    // 图片识别
}

/**
 * 模式选择按钮
 */
@Composable
private fun ImportModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MintGreen.copy(alpha = 0.2f) else BeigeSurface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MintGreen else EarthBrown,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * 手动输入课程表
 */
@Composable
private fun ManualScheduleInput(
    slots: List<ScheduleSlot>,
    onAddSlot: (ScheduleSlot) -> Unit,
    onRemoveSlot: (ScheduleSlot) -> Unit
) {
    var selectedDay by remember { mutableStateOf(1) }
    var startHour by remember { mutableStateOf(8) }
    var startMinute by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(90) }

    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Column {
        // 周几选择
        Text(
            text = "选择星期",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weekDays.forEachIndexed { index, day ->
                Surface(
                    onClick = { selectedDay = index + 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedDay == index + 1) StellarOrange.copy(alpha = 0.2f) else BeigeSurface,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedDay == index + 1) StellarOrange else EarthBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 时间选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 开始时间
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "开始时间",
                    style = MaterialTheme.typography.labelMedium,
                    color = EarthBrown
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 小时选择
                    var hourExpanded by remember { mutableStateOf(false) }
                    Surface(
                        onClick = { hourExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = BeigeSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d", startHour),
                                style = MaterialTheme.typography.bodyLarge,
                                color = EarthBrown,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "时",
                                style = MaterialTheme.typography.labelSmall,
                                color = EarthBrownLight,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 分钟选择
                    var minuteExpanded by remember { mutableStateOf(false) }
                    Surface(
                        onClick = { minuteExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = BeigeSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d", startMinute),
                                style = MaterialTheme.typography.bodyLarge,
                                color = EarthBrown,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "分",
                                style = MaterialTheme.typography.labelSmall,
                                color = EarthBrownLight,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // 时长
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "课程时长",
                    style = MaterialTheme.typography.labelMedium,
                    color = EarthBrown
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BeigeSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$duration",
                            style = MaterialTheme.typography.bodyLarge,
                            color = EarthBrown,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "分钟",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrownLight,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 添加按钮
        Button(
            onClick = {
                onAddSlot(
                    ScheduleSlot(
                        dayOfWeek = selectedDay,
                        startHour = startHour,
                        startMinute = startMinute,
                        durationMinutes = duration
                    )
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StellarOrange
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加时间", fontWeight = FontWeight.Bold)
        }

        // 已添加的时间列表
        if (slots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "已添加的时间",
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrown
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 150.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(slots) { slot ->
                    SlotItem(
                        slot = slot,
                        onRemove = { onRemoveSlot(slot) }
                    )
                }
            }
        }
    }
}

/**
 * 时间项
 */
@Composable
private fun SlotItem(
    slot: ScheduleSlot,
    onRemove: () -> Unit
) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BeigeSurface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${weekDays[slot.dayOfWeek - 1]} ${String.format("%02d:%02d", slot.startHour, slot.startMinute)} (${slot.durationMinutes}分钟)",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrown
            )
            TextButton(onClick = onRemove) {
                Text("删除", color = RoseRed, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * 图片识别导入（预留接口）
 */
@Composable
private fun ImageScheduleImport(
    onRecognized: (List<ScheduleSlot>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 占位图
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(16.dp),
            color = BeigeSurface.copy(alpha = 0.3f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📷",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击上传课程表图片",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EarthBrownLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "支持识别学校教务系统导出的课程表图片",
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrownLight.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 预留：调用图片识别API
        OutlinedButton(
            onClick = {
                // TODO: 实现图片选择和OCR识别
                // 目前使用模拟数据
                onRecognized(
                    listOf(
                        ScheduleSlot(1, 8, 0, 90),
                        ScheduleSlot(3, 10, 0, 90),
                        ScheduleSlot(5, 14, 0, 90)
                    )
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MintGreen
            )
        ) {
            Text("选择图片", fontWeight = FontWeight.Bold)
        }
    }
}