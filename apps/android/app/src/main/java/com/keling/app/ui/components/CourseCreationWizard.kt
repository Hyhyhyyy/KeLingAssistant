package com.keling.app.ui.components

/**
 * =========================
 * 课程创建向导组件
 * =========================
 *
 * 多步骤创建课程的对话框
 * Step 1: 基本信息（名称、代码、教师、地点）
 * Step 2: 时间安排（可视化周课表选择器）
 * Step 3: 主题设置（颜色、星球样式）
 * Step 4: 高级选项（学分、学期、考试日期）
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keling.app.R
import com.keling.app.data.*
import com.keling.app.ui.screens.greenhouse.PLANET_ASSETS
import com.keling.app.ui.screens.greenhouse.getPlanetAsset
import com.keling.app.ui.theme.*
import coil.compose.AsyncImage
import java.util.Calendar

// 预定义主题颜色
val COURSE_THEME_COLORS = listOf(
    Pair("苔藓绿", 0xFF85CDA9L),
    Pair("恒星橙", 0xFFE8A87CL),
    Pair("雾玫瑰", 0xFFC9A9A6L),
    Pair("天空蓝", 0xFF87CEEBL),
    Pair("薰衣草", 0xFFB8A9C9L),
    Pair("日落红", 0xFFE07A5FL),
    Pair("森林绿", 0xFF6B8E23L),
    Pair("深海蓝", 0xFF2E5A88L),
    Pair("樱花粉", 0xFFFFB7C5L),
    Pair("柠檬黄", 0xFFFFF44FL)
)

/**
 * 课程创建向导对话框
 */
@Composable
fun CourseCreationWizard(
    onDismiss: () -> Unit,
    onConfirm: (Course) -> Unit,
    editingCourse: Course? = null,
    existingCourses: List<Course> = emptyList()
) {
    // 步骤状态
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4

    // 表单数据
    var courseName by remember { mutableStateOf(editingCourse?.name ?: "") }
    var courseCode by remember { mutableStateOf(editingCourse?.code ?: "") }
    var teacherName by remember { mutableStateOf(editingCourse?.teacher ?: "") }
    var location by remember { mutableStateOf(editingCourse?.location ?: "") }

    // 课表
    var scheduleSlots by remember { mutableStateOf(editingCourse?.schedule ?: emptyList()) }

    // 主题
    var selectedColorIndex by remember { mutableStateOf(
        COURSE_THEME_COLORS.indexOfFirst { it.second == editingCourse?.themeColor }.coerceAtLeast(0)
    )}
    var planetStyleIndex by remember { mutableStateOf(editingCourse?.planetStyleIndex ?: -1) }

    // 高级选项
    var credit by remember { mutableStateOf(editingCourse?.credit ?: 0) }
    var semester by remember { mutableStateOf(editingCourse?.semester ?: getCurrentSemester()) }
    var examDate by remember { mutableStateOf(editingCourse?.examDate) }
    var showExamDatePicker by remember { mutableStateOf(false) }

    // 验证状态
    val canProceed = when (currentStep) {
        0 -> courseName.isNotBlank()
        1 -> true // 课表可选
        2 -> true
        3 -> true
        else -> false
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
                    .padding(24.dp)
            ) {
                // 标题和步骤指示器
                WizardHeader(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    isEditing = editingCourse != null
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 步骤内容
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } togetherWith
                                    slideOutHorizontally { width -> -width }
                        } else {
                            slideInHorizontally { width -> -width } togetherWith
                                    slideOutHorizontally { width -> width }
                        }
                    },
                    label = "wizard_content"
                ) { step ->
                    when (step) {
                        0 -> BasicInfoStep(
                            courseName = courseName,
                            onNameChange = { courseName = it },
                            courseCode = courseCode,
                            onCodeChange = { courseCode = it },
                            teacherName = teacherName,
                            onTeacherChange = { teacherName = it },
                            location = location,
                            onLocationChange = { location = it }
                        )
                        1 -> ScheduleStep(
                            scheduleSlots = scheduleSlots,
                            onSlotsChange = { scheduleSlots = it }
                        )
                        2 -> ThemeStep(
                            selectedColorIndex = selectedColorIndex,
                            onColorSelected = { selectedColorIndex = it },
                            planetStyleIndex = planetStyleIndex,
                            onPlanetSelected = { planetStyleIndex = it },
                            courseName = courseName
                        )
                        3 -> AdvancedStep(
                            credit = credit,
                            onCreditChange = { credit = it },
                            semester = semester,
                            onSemesterChange = { semester = it },
                            examDate = examDate,
                            onExamDateChange = { examDate = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 导航按钮
                WizardNavigation(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    canProceed = canProceed,
                    onBack = {
                        if (currentStep > 0) currentStep--
                        else onDismiss()
                    },
                    onNext = {
                        if (currentStep < totalSteps - 1) currentStep++
                        else {
                            // 创建课程
                            val course = Course(
                                id = editingCourse?.id ?: "course_${System.currentTimeMillis()}",
                                name = courseName.trim(),
                                code = courseCode.trim(),
                                teacher = teacherName.trim(),
                                schedule = scheduleSlots,
                                location = location.trim(),
                                themeColor = COURSE_THEME_COLORS[selectedColorIndex].second,
                                masteryLevel = editingCourse?.masteryLevel ?: 0f,
                                plantStage = editingCourse?.plantStage ?: 0,
                                planetStyleIndex = planetStyleIndex,
                                lastStudiedAt = editingCourse?.lastStudiedAt,
                                totalStudyMinutes = editingCourse?.totalStudyMinutes ?: 0,
                                isArchived = editingCourse?.isArchived ?: false,
                                semester = semester,
                                credit = credit,
                                examDate = examDate,
                                courseImageUrl = editingCourse?.courseImageUrl,
                                studySessionCount = editingCourse?.studySessionCount ?: 0
                            )
                            onConfirm(course)
                        }
                    }
                )
            }
        }
    }
}

// ==================== 步骤头部 ====================

@Composable
private fun WizardHeader(
    currentStep: Int,
    totalSteps: Int,
    isEditing: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditing) "编辑课程" else "创建新课程",
            style = MaterialTheme.typography.headlineSmall,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 步骤指示器
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stepLabels = listOf("基本信息", "时间安排", "主题设置", "高级选项")
            stepLabels.forEachIndexed { index, label ->
                StepIndicator(
                    stepNumber = index + 1,
                    label = label,
                    isActive = index == currentStep,
                    isCompleted = index < currentStep
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "step")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .drawBehind {
                    if (isActive) {
                        drawCircle(
                            color = MintGreen.copy(alpha = glowAlpha),
                            radius = size.minDimension / 2 + 4.dp.toPx()
                        )
                    }
                }
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MintGreen
                        isActive -> MintGreen
                        else -> WarmGray.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (isActive || isCompleted) Color.White else EarthBrownLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) EarthBrown else EarthBrownLight,
            maxLines = 1
        )
    }
}

// ==================== 步骤1: 基本信息 ====================

@Composable
private fun BasicInfoStep(
    courseName: String,
    onNameChange: (String) -> Unit,
    courseCode: String,
    onCodeChange: (String) -> Unit,
    teacherName: String,
    onTeacherChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 课程名称（必填）
        OutlinedTextField(
            value = courseName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("课程名称 *", color = EarthBrownLight) },
            placeholder = { Text("如：高等数学", color = EarthBrownLight.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray,
                focusedContainerColor = DawnWhite,
                unfocusedContainerColor = DawnWhite
            )
        )

        // 课程代码
        OutlinedTextField(
            value = courseCode,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("课程代码", color = EarthBrownLight) },
            placeholder = { Text("如：MA101", color = EarthBrownLight.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray
            )
        )

        // 授课教师
        OutlinedTextField(
            value = teacherName,
            onValueChange = onTeacherChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("授课教师", color = EarthBrownLight) },
            placeholder = { Text("教师姓名", color = EarthBrownLight.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray
            )
        )

        // 上课地点
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("上课地点", color = EarthBrownLight) },
            placeholder = { Text("如：教学楼A301", color = EarthBrownLight.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray
            )
        )
    }
}

// ==================== 步骤2: 时间安排 ====================

@Composable
private fun ScheduleStep(
    scheduleSlots: List<ScheduleSlot>,
    onSlotsChange: (List<ScheduleSlot>) -> Unit
) {
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val hours = (8..22).toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Text(
            text = "点击课表格子添加/删除上课时间",
            style = MaterialTheme.typography.bodySmall,
            color = EarthBrownLight
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 课表网格
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(BeigeSurface.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                // 星期标题行
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.width(36.dp))
                    days.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrown,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 时间行
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    hours.forEach { hour ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${hour}:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = EarthBrownLight,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )

                            days.forEachIndexed { dayIndex, _ ->
                                val dayOfWeek = dayIndex + 1
                                val hasSlot = scheduleSlots.any {
                                    it.dayOfWeek == dayOfWeek && it.startHour == hour
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(1.dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (hasSlot) MintGreen.copy(alpha = 0.6f)
                                            else WarmGray.copy(alpha = 0.1f)
                                        )
                                        .clickable {
                                            if (hasSlot) {
                                                // 移除
                                                onSlotsChange(
                                                    scheduleSlots.filterNot {
                                                        it.dayOfWeek == dayOfWeek && it.startHour == hour
                                                    }
                                                )
                                            } else {
                                                // 添加（默认90分钟）
                                                onSlotsChange(
                                                    scheduleSlots + ScheduleSlot(
                                                        dayOfWeek = dayOfWeek,
                                                        startHour = hour,
                                                        startMinute = 0,
                                                        durationMinutes = 90
                                                    )
                                                )
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 已选时间列表
        if (scheduleSlots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "已选时间：",
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrown
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scheduleSlots.sortedWith(compareBy({ it.dayOfWeek }, { it.startHour }))) { slot ->
                    val dayName = days[slot.dayOfWeek - 1]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintGreen.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$dayName ${slot.startHour}:${slot.startMinute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "×",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintGreen,
                                modifier = Modifier.clickable {
                                    onSlotsChange(scheduleSlots - slot)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== 步骤3: 主题设置 ====================

@Composable
private fun ThemeStep(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    planetStyleIndex: Int,
    onPlanetSelected: (Int) -> Unit,
    courseName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 颜色选择
        Text(
            text = "选择主题颜色",
            style = MaterialTheme.typography.titleMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(COURSE_THEME_COLORS) { index, (name, color) ->
                val isSelected = index == selectedColorIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, EarthBrown, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onColorSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = EarthBrownLight
                    )
                }
            }
        }

        // 星球样式选择
        Text(
            text = "选择星球形象",
            style = MaterialTheme.typography.titleMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // 随机选项
                val isSelected = planetStyleIndex == -1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BeigeSurface)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, MintGreen, RoundedCornerShape(16.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onPlanetSelected(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎲", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "随机",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MintGreen else EarthBrownLight
                    )
                }
            }

            itemsIndexed(PLANET_ASSETS.take(10)) { index, _ ->
                val actualIndex = index
                val isSelected = planetStyleIndex == actualIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BeigeSurface)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, MintGreen, RoundedCornerShape(16.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onPlanetSelected(actualIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = getPlanetAsset(actualIndex),
                            contentDescription = "星球$actualIndex",
                            modifier = Modifier.size(56.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "#${actualIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MintGreen else EarthBrownLight
                    )
                }
            }
        }

        // 预览
        if (courseName.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(COURSE_THEME_COLORS[selectedColorIndex].second).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (planetStyleIndex >= 0) {
                        AsyncImage(
                            model = getPlanetAsset(planetStyleIndex),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Text("🌍", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== 步骤4: 高级选项 ====================

@Composable
private fun AdvancedStep(
    credit: Int,
    onCreditChange: (Int) -> Unit,
    semester: String,
    onSemesterChange: (String) -> Unit,
    examDate: Long?,
    onExamDateChange: (Long?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 学分
        Text(
            text = "学分",
            style = MaterialTheme.typography.titleSmall,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (0..6).forEach { creditValue ->
                val isSelected = credit == creditValue
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = if (isSelected) MintGreen else BeigeSurface,
                    onClick = { onCreditChange(creditValue) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = creditValue.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else EarthBrown
                        )
                    }
                }
            }
        }

        Divider(color = WarmGray.copy(alpha = 0.3f))

        // 学期
        OutlinedTextField(
            value = semester,
            onValueChange = onSemesterChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("学期", color = EarthBrownLight) },
            placeholder = { Text("如：2025春季", color = EarthBrownLight.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray
            )
        )

        // 考试日期（简化版，显示日期选择提示）
        Text(
            text = "考试日期（可选）",
            style = MaterialTheme.typography.titleSmall,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        if (examDate != null) {
            val dateFormat = java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault())
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(java.util.Date(examDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onExamDateChange(null) }) {
                    Text("清除", color = EarthBrownLight)
                }
            }
        } else {
            Text(
                text = "点击设置考试日期（用于考前提醒）",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight,
                modifier = Modifier.clickable {
                    // 简化：设为30天后
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_MONTH, 30)
                    onExamDateChange(cal.timeInMillis)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 提示
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = WarmSunOrange.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "提示：学分和考试日期可以帮助系统更好地规划学习任务",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }
        }
    }
}

// ==================== 导航按钮 ====================

@Composable
private fun WizardNavigation(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 返回/取消按钮
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = EarthBrown
            )
        ) {
            Text(if (currentStep == 0) "取消" else "上一步")
        }

        // 下一步/完成按钮
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = canProceed,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MintGreen,
                disabledContainerColor = WarmGray.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = if (currentStep == totalSteps - 1) "完成" else "下一步",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== 工具函数 ====================

/**
 * 获取当前学期字符串
 */
private fun getCurrentSemester(): String {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1

    return if (month in 2..7) {
        "${year}春季"
    } else if (month in 8..12) {
        "${year}秋季"
    } else {
        "${year - 1}秋季"
    }
}