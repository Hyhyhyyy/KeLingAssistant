package com.keling.app.ui.components

/**
 * =========================
 * 签到日历组件
 * =========================
 *
 * 显示月历视图，标记签到日期
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.CheckInRecord
import com.keling.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * 签到日历
 */
@Composable
fun CheckInCalendar(
    records: List<CheckInRecord>,
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val signedDates = records.map { it.date }.toSet()
    val today = LocalDate.now()
    val todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    // 计算连续签到
    var consecutiveDays = 0
    var checkDate = today
    while (signedDates.contains(checkDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))) {
        consecutiveDays++
        checkDate = checkDate.minusDays(1)
    }

    Column(
        modifier = modifier
    ) {
        // 月份导航
        MonthNavigation(
            currentMonth = currentMonth,
            onPrevious = { onMonthChange(currentMonth.minusMonths(1)) },
            onNext = { onMonthChange(currentMonth.plusMonths(1)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 星期标题
        WeekdayHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // 日历网格
        CalendarGrid(
            currentMonth = currentMonth,
            signedDates = signedDates,
            todayStr = todayStr,
            consecutiveDays = consecutiveDays
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 统计信息
        CheckInStats(
            totalDays = signedDates.size,
            consecutiveDays = consecutiveDays,
            monthDays = signedDates.count { date ->
                val dateStr = date
                val year = dateStr.substring(0, 4).toInt()
                val month = dateStr.substring(4, 6).toInt()
                year == currentMonth.year && month == currentMonth.monthValue
            }
        )
    }
}

// ==================== 月份导航 ====================

@Composable
private fun MonthNavigation(
    currentMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy年MM月")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一月
        Surface(
            onClick = onPrevious,
            shape = RoundedCornerShape(8.dp),
            color = BeigeSurface
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    style = MaterialTheme.typography.titleLarge,
                    color = EarthBrown
                )
            }
        }

        // 当前月份
        Text(
            text = currentMonth.format(formatter),
            style = MaterialTheme.typography.titleLarge,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )

        // 下一月
        Surface(
            onClick = onNext,
            shape = RoundedCornerShape(8.dp),
            color = BeigeSurface
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    color = EarthBrown
                )
            }
        }
    }
}

// ==================== 星期标题 ====================

@Composable
private fun WeekdayHeader() {
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        weekdays.forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==================== 日历网格 ====================

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    signedDates: Set<String>,
    todayStr: String,
    consecutiveDays: Int
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()

    // 计算第一天是星期几（0=周日，1=周一...）
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = lastDayOfMonth.dayOfMonth

    // 计算连续签到的日期集合
    val consecutiveDates = mutableSetOf<String>()
    var checkDate = LocalDate.now()
    repeat(consecutiveDays) {
        consecutiveDates.add(checkDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        checkDate = checkDate.minusDays(1)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var dayCounter = 1

        // 最多6行
        for (week in 0 until 6) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (dayOfWeek in 0 until 7) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // 空白格子
                        Box(modifier = Modifier.weight(1f))
                    } else if (dayCounter <= daysInMonth) {
                        val date = currentMonth.atDay(dayCounter)
                        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        val isSigned = signedDates.contains(dateStr)
                        val isToday = dateStr == todayStr
                        val isConsecutive = consecutiveDates.contains(dateStr)
                        val isFuture = date.isAfter(LocalDate.now())

                        DayCell(
                            day = dayCounter,
                            isSigned = isSigned,
                            isToday = isToday,
                            isConsecutive = isConsecutive,
                            isFuture = isFuture,
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ==================== 日期格子 ====================

@Composable
private fun DayCell(
    day: Int,
    isSigned: Boolean,
    isToday: Boolean,
    isConsecutive: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "day")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (isToday) {
                    Modifier
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .drawBehind {
                            drawCircle(
                                color = StellarOrange.copy(alpha = pulseAlpha),
                                radius = size.minDimension / 2 + 4.dp.toPx()
                            )
                        }
                } else Modifier
            )
            .clip(CircleShape)
            .background(
                when {
                    isSigned && isConsecutive -> MintGreen
                    isSigned -> MintGreen.copy(alpha = 0.7f)
                    isToday -> WarmGray.copy(alpha = 0.2f)
                    isFuture -> Color.Transparent
                    else -> WarmGray.copy(alpha = 0.1f)
                }
            )
            .then(
                if (isToday && !isSigned) {
                    Modifier.border(2.dp, StellarOrange, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSigned) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isFuture -> EarthBrownLight.copy(alpha = 0.3f)
                    isToday -> StellarOrange
                    else -> EarthBrown
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ==================== 签到统计 ====================

@Composable
private fun CheckInStats(
    totalDays: Int,
    consecutiveDays: Int,
    monthDays: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "累计签到",
            value = "$totalDays 天",
            icon = "📅",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "连续签到",
            value = "$consecutiveDays 天",
            icon = "🔥",
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "本月签到",
            value = "$monthDays 天",
            icon = "📊",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CreamWhite.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrownLight
            )
        }
    }
}