/**
 * Pickers.kt
 * 选择器组件 - 环形时间选择器、星期轨道选择器等
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.keling.app.data.Course
import com.keling.app.ui.theme.*
import kotlin.math.*

/**
 * 环形时间选择器
 * 直观的时间选择交互
 */
@Composable
fun CircularTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    minHour: Int = 6,
    maxHour: Int = 22,
    showMinutes: Boolean = true
) {
    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外圈 - 小时选择
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = atan2(
                            change.position.y - center.y,
                            change.position.x - center.x
                        )
                        val degrees = Math.toDegrees(angle.toDouble()).toFloat() + 90f
                        val normalizedDegrees = ((degrees % 360) + 360) % 360
                        val hourRange = maxHour - minHour
                        val hourProgress = normalizedDegrees / 360f
                        val newHour = (minHour + (hourRange * hourProgress)).roundToInt()
                            .coerceIn(minHour, maxHour)
                        if (newHour != selectedHour) {
                            onTimeSelected(newHour, selectedMinute)
                        }
                    }
                }
                .drawBehind {
                    val outerRadius = size.minDimension / 2 - 10

                    // 绘制轨道背景
                    drawCircle(
                        color = BorderGray.copy(alpha = 0.3f),
                        radius = outerRadius,
                        style = Stroke(width = 30f)
                    )

                    // 绘制小时刻度
                    for (hour in minHour..maxHour) {
                        val angle = ((hour - minHour).toFloat() / (maxHour - minHour) * 360 - 90) * PI / 180
                        val x = this.center.x + outerRadius * cos(angle).toFloat()
                        val y = this.center.y + outerRadius * sin(angle).toFloat()

                        val isSelected = hour == selectedHour

                        drawCircle(
                            color = if (isSelected) StellarOrange else MossGreen.copy(alpha = 0.4f),
                            radius = if (isSelected) 12f else 6f,
                            center = Offset(x, y)
                        )
                    }

                    // 绘制选中指示器发光效果
                    val selectedAngle = ((selectedHour - minHour).toFloat() / (maxHour - minHour) * 360 - 90) * PI / 180
                    val indicatorX = this.center.x + outerRadius * cos(selectedAngle).toFloat()
                    val indicatorY = this.center.y + outerRadius * sin(selectedAngle).toFloat()

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StellarOrange.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(indicatorX, indicatorY),
                            radius = 30f
                        ),
                        radius = 30f,
                        center = Offset(indicatorX, indicatorY)
                    )
                }
        )

        // 中心显示
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%02d:%02d", selectedHour, selectedMinute),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
            Text(
                text = "开始时间",
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrownLight
            )
        }
    }
}

/**
 * 星期轨道选择器
 * 七个星球围绕轨道排列
 */
@Composable
fun WeekOrbitSelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val dayColors = listOf(
        MossGreen,
        StellarOrange,
        DeepSeaBlue,
        MistRose,
        DuskGold,
        StarPurple,
        StellarOrangeDark
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayNames.forEachIndexed { index, name ->
            val isSelected = selectedDay == index + 1
            val dayColor = dayColors[index]

            var offsetY by remember { mutableStateOf(0f) }
            LaunchedEffect(isSelected) {
                animate(
                    initialValue = if (isSelected) 0f else offsetY,
                    targetValue = if (isSelected) -8f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) { value, _ ->
                    offsetY = value
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { translationY = offsetY }
                    .clickable { onDaySelected(index + 1) }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 36.dp else 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(10.dp)
                                .background(dayColor.copy(alpha = 0.5f), CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (isSelected) dayColor
                                else dayColor.copy(alpha = 0.2f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) DawnWhite.copy(alpha = 0.5f)
                                       else dayColor.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DawnWhite else dayColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "周${name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) EarthBrown else EarthBrownLight
                )
            }
        }
    }
}

/**
 * 时长选择器
 * 滑块形式选择课程时长
 */
@Composable
fun DurationSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minDuration: Int = 30,
    maxDuration: Int = 180,
    step: Int = 15
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "课程时长",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(45, 90, 120).forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    label = { Text("${duration}分钟") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StellarOrange.copy(alpha = 0.2f),
                        selectedLabelColor = StellarOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var sliderValue by remember(selectedDuration) {
            mutableStateOf(selectedDuration.toFloat())
        }

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val snapped = ((sliderValue / step).roundToInt() * step)
                    .coerceIn(minDuration, maxDuration)
                onDurationSelected(snapped)
                sliderValue = snapped.toFloat()
            },
            valueRange = minDuration.toFloat()..maxDuration.toFloat(),
            steps = (maxDuration - minDuration) / step - 1,
            colors = SliderDefaults.colors(
                thumbColor = StellarOrange,
                activeTrackColor = StellarOrange,
                inactiveTrackColor = BorderGray
            )
        )

        Text(
            text = "${selectedDuration} 分钟",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = StellarOrange,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 课程选择器
 * 横向滚动的星球卡片
 */
@Composable
fun CourseSelector(
    courses: List<Course>,
    selectedCourseId: String?,
    onCourseSelected: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "选择课程",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (courses.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = WarmSand.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "暂无课程，请先在培育温室创建课程",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(courses) { course ->
                    CourseSelectorCard(
                        course = course,
                        isSelected = course.id == selectedCourseId,
                        onClick = { onCourseSelected(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseSelectorCard(
    course: Course,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val courseColor = Color(course.themeColor)

    var scale by remember { mutableStateOf(1f) }
    LaunchedEffect(isSelected) {
        animate(
            initialValue = if (isSelected) 1f else scale,
            targetValue = if (isSelected) 1.05f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) { value, _ ->
            scale = value
        }
    }

    Card(
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = courseColor.copy(alpha = if (isSelected) 0.15f else 0.08f)
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, courseColor)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(courseColor.copy(alpha = 0.2f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = courseColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = course.name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = courseColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = course.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) EarthBrown else EarthBrownLight,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}