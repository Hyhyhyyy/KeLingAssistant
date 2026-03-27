package com.keling.app.ui.components

/**
 * =========================
 * 任务创建对话框
 * =========================
 *
 * 支持手动创建和使用模板创建
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keling.app.R
import com.keling.app.data.*
import com.keling.app.ui.theme.*
import java.util.Calendar

/**
 * 任务创建对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit,
    courses: List<Course> = emptyList(),
    editingTask: Task? = null,
    templates: List<TaskTemplate> = TASK_TEMPLATES
) {
    // 模式：模板选择 或 手动创建
    var showTemplateSelection by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<TaskTemplate?>(null) }

    // 表单数据
    var taskTitle by remember { mutableStateOf(editingTask?.title ?: "") }
    var taskDescription by remember { mutableStateOf(editingTask?.description ?: "") }
    var selectedCourseId by remember { mutableStateOf(editingTask?.courseId) }
    var selectedType by remember { mutableStateOf(editingTask?.type ?: TaskType.DAILY_CARE) }
    var priority by remember { mutableStateOf(editingTask?.priority ?: 3) }
    var estimatedMinutes by remember { mutableStateOf(editingTask?.estimatedMinutes ?: 25) }
    var scheduledDate by remember { mutableStateOf(editingTask?.scheduledAt) }

    // 验证
    val canConfirm = taskTitle.isNotBlank()

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingTask != null) "编辑任务" else "创建任务",
                        style = MaterialTheme.typography.headlineSmall,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )

                    // 模板按钮
                    if (editingTask == null) {
                        TextButton(
                            onClick = { showTemplateSelection = !showTemplateSelection }
                        ) {
                            Text(
                                text = if (showTemplateSelection) "手动创建" else "使用模板",
                                style = MaterialTheme.typography.labelMedium,
                                color = MintGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showTemplateSelection && editingTask == null) {
                    // 模板选择
                    TemplateSelection(
                        templates = templates,
                        onTemplateSelected = { template ->
                            selectedTemplate = template
                            taskTitle = template.name
                            taskDescription = template.description
                            selectedType = template.defaultType
                            priority = template.defaultPriority
                            estimatedMinutes = template.defaultDuration
                            showTemplateSelection = false
                        }
                    )
                } else {
                    // 手动创建表单
                    ManualCreationForm(
                        taskTitle = taskTitle,
                        onTitleChange = { taskTitle = it },
                        taskDescription = taskDescription,
                        onDescriptionChange = { taskDescription = it },
                        courses = courses,
                        selectedCourseId = selectedCourseId,
                        onCourseSelected = { selectedCourseId = it },
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it },
                        priority = priority,
                        onPriorityChange = { priority = it },
                        estimatedMinutes = estimatedMinutes,
                        onEstimatedMinutesChange = { estimatedMinutes = it }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消", color = EarthBrownLight)
                    }

                    Button(
                        onClick = {
                            val task = Task(
                                id = editingTask?.id ?: "task_${System.currentTimeMillis()}",
                                title = taskTitle.trim(),
                                description = taskDescription.trim(),
                                type = selectedType,
                                courseId = selectedCourseId,
                                priority = priority,
                                estimatedMinutes = estimatedMinutes,
                                scheduledAt = scheduledDate,
                                rewards = Rewards(
                                    energy = estimatedMinutes,
                                    crystals = estimatedMinutes / 2,
                                    exp = estimatedMinutes * 2
                                ),
                                createdAt = editingTask?.createdAt ?: System.currentTimeMillis(),
                                status = editingTask?.status ?: TaskStatus.PENDING
                            )
                            onConfirm(task)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen
                        )
                    ) {
                        Text(
                            text = if (editingTask != null) "保存" else "创建",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== 模板选择 ====================

@Composable
private fun TemplateSelection(
    templates: List<TaskTemplate>,
    onTemplateSelected: (TaskTemplate) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(300.dp)
    ) {
        items(templates) { template ->
            TaskTemplateCard(
                template = template,
                onClick = { onTemplateSelected(template) }
            )
        }
    }
}

@Composable
private fun TaskTemplateCard(
    template: TaskTemplate,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "template")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val typeColor = when (template.defaultType) {
        TaskType.DAILY_CARE -> MintGreen
        TaskType.DEEP_EXPLORATION -> StellarOrange
        TaskType.REVIEW_RITUAL -> LavenderPurple
        TaskType.BOUNTY -> WarmSunOrange
        TaskType.RESCUE -> RoseRed
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = typeColor.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = typeColor.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = template.icon, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 内容
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight
                    )
                }

                // 时长
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BeigeSurface
                ) {
                    Text(
                        text = "${template.defaultDuration}分钟",
                        style = MaterialTheme.typography.labelSmall,
                        color = EarthBrown,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==================== 手动创建表单 ====================

@Composable
private fun ManualCreationForm(
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    taskDescription: String,
    onDescriptionChange: (String) -> Unit,
    courses: List<Course>,
    selectedCourseId: String?,
    onCourseSelected: (String?) -> Unit,
    selectedType: TaskType,
    onTypeSelected: (TaskType) -> Unit,
    priority: Int,
    onPriorityChange: (Int) -> Unit,
    estimatedMinutes: Int,
    onEstimatedMinutesChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 任务标题
        OutlinedTextField(
            value = taskTitle,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("任务标题 *", color = EarthBrownLight) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray
            )
        )

        // 任务描述
        OutlinedTextField(
            value = taskDescription,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            label = { Text("任务描述", color = EarthBrownLight) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray
            )
        )

        // 关联课程
        if (courses.isNotEmpty()) {
            Text(
                text = "关联课程",
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrown
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = selectedCourseId == null
                    Surface(
                        onClick = { onCourseSelected(null) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) WarmGray.copy(alpha = 0.3f) else BeigeSurface
                    ) {
                        Text(
                            text = "不关联",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) EarthBrown else EarthBrownLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                items(courses) { course ->
                    val isSelected = selectedCourseId == course.id
                    Surface(
                        onClick = { onCourseSelected(course.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(course.themeColor).copy(alpha = 0.3f) else BeigeSurface
                    ) {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) EarthBrown else EarthBrownLight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 任务类型
        Text(
            text = "任务类型",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )

        TaskTypeSelector(
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )

        // 优先级
        Text(
            text = "优先级",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )

        PrioritySelector(
            priority = priority,
            onPriorityChange = onPriorityChange
        )

        // 预计时长
        Text(
            text = "预计时长：$estimatedMinutes 分钟",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )

        Slider(
            value = estimatedMinutes.toFloat(),
            onValueChange = { onEstimatedMinutesChange(it.toInt()) },
            valueRange = 5f..120f,
            steps = 23,
            colors = SliderDefaults.colors(
                thumbColor = MintGreen,
                activeTrackColor = MintGreen,
                inactiveTrackColor = WarmGray.copy(alpha = 0.3f)
            )
        )
    }
}

// ==================== 任务类型选择器 ====================

@Composable
private fun TaskTypeSelector(
    selectedType: TaskType,
    onTypeSelected: (TaskType) -> Unit
) {
    val types = listOf(
        TaskType.DAILY_CARE to "🌱" to "日常培育",
        TaskType.DEEP_EXPLORATION to "🔬" to "深度探索",
        TaskType.REVIEW_RITUAL to "🔄" to "复习仪式",
        TaskType.BOUNTY to "🎯" to "赏金任务"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { (typeWithIcon, name) ->
            val (type, icon) = typeWithIcon
            val isSelected = selectedType == type

            Surface(
                onClick = { onTypeSelected(type) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MintGreen.copy(alpha = 0.2f) else BeigeSurface,
                modifier = Modifier.then(
                    if (isSelected) Modifier.border(1.dp, MintGreen, RoundedCornerShape(10.dp))
                    else Modifier
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MintGreen else EarthBrownLight
                    )
                }
            }
        }
    }
}

// ==================== 优先级选择器 ====================

@Composable
private fun PrioritySelector(
    priority: Int,
    onPriorityChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { level ->
            val isSelected = priority == level
            val color = when (level) {
                1 -> WarmGray
                2 -> MintGreen.copy(alpha = 0.6f)
                3 -> MintGreen
                4 -> WarmSunOrange
                5 -> RoseRed
                else -> WarmGray
            }

            Surface(
                onClick = { onPriorityChange(level) },
                shape = CircleShape,
                color = if (isSelected) color else color.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Text(
        text = when (priority) {
            1 -> "可选"
            2 -> "较低"
            3 -> "普通"
            4 -> "较高"
            5 -> "紧急"
            else -> ""
        },
        style = MaterialTheme.typography.labelSmall,
        color = EarthBrownLight
    )
}