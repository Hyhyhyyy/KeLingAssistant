package com.keling.app.ai

/**
 * =========================
 * 智能学习计划生成器
 * =========================
 *
 * 根据用户课表、任务、偏好生成个性化学习计划
 */

import com.keling.app.data.*
import java.util.Calendar

/**
 * 学习计划生成器
 */
class StudyPlanGenerator {

    /**
     * 生成周学习计划
     */
    fun generateWeeklyPlan(
        courses: List<Course>,
        schedule: List<Pair<Course, ScheduleSlot>>,
        tasks: List<Task>,
        preferences: StudyPreferences,
        studyRecords: List<StudyRecord>
    ): WeeklyStudyPlan {
        val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val dailyPlans = mutableListOf<DailyPlan>()

        // 分析本周数据
        val weekStart = getWeekStartTime()
        val weekStudyMinutes = studyRecords
            .filter { it.createdAt >= weekStart }
            .sumOf { it.durationMinutes }

        // 生成每天的计划
        for (dayIndex in 0..6) {
            val daySchedule = schedule.filter { it.second.dayOfWeek == dayIndex + 1 }
            val dayPlan = generateDailyPlan(
                dayIndex = dayIndex,
                daySchedule = daySchedule,
                courses = courses,
                tasks = tasks.filter { /* 今天或未安排的任务 */ true },
                preferences = preferences
            )
            dailyPlans.add(dayPlan)
        }

        // 计算总学习时长目标
        val targetMinutes = preferences.weeklyGoalMinutes
        val remainingMinutes = (targetMinutes - weekStudyMinutes).coerceAtLeast(0)

        return WeeklyStudyPlan(
            id = "plan_${System.currentTimeMillis()}",
            dailyPlans = dailyPlans,
            totalTargetMinutes = targetMinutes,
            completedMinutes = weekStudyMinutes,
            remainingMinutes = remainingMinutes,
            suggestions = generatePlanSuggestions(courses, tasks, weekStudyMinutes, targetMinutes)
        )
    }

    /**
     * 生成单日学习计划
     */
    private fun generateDailyPlan(
        dayIndex: Int,
        daySchedule: List<Pair<Course, ScheduleSlot>>,
        courses: List<Course>,
        tasks: List<Task>,
        preferences: StudyPreferences
    ): DailyPlan {
        val timeSlots = mutableListOf<TimeSlot>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, if (dayIndex == 6) Calendar.SUNDAY else dayIndex + 2)

        // 1. 添加课程时间块
        daySchedule.sortedBy { it.second.startHour * 60 + it.second.startMinute }.forEach { (course, slot) ->
            timeSlots.add(
                TimeSlot(
                    startTime = "${slot.startHour}:${slot.startMinute.toString().padStart(2, '0')}",
                    endTime = calculateEndTime(slot),
                    type = SlotType.CLASS,
                    title = course.name,
                    courseId = course.id,
                    durationMinutes = slot.durationMinutes
                )
            )
        }

        // 2. 找出空闲时段
        val freeSlots = findFreeSlots(daySchedule, preferences)

        // 3. 在空闲时段安排学习任务
        val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
            .sortedByDescending { it.priority }

        for (freeSlot in freeSlots) {
            val availableMinutes = freeSlot.durationMinutes

            // 找一个适合的任务
            val suitableTask = pendingTasks.firstOrNull {
                it.estimatedMinutes <= availableMinutes
            }

            if (suitableTask != null) {
                timeSlots.add(
                    TimeSlot(
                        startTime = freeSlot.startTime,
                        endTime = calculateEndTimeFromStart(freeSlot.startTime, suitableTask.estimatedMinutes),
                        type = SlotType.STUDY,
                        title = suitableTask.title,
                        taskId = suitableTask.id,
                        courseId = suitableTask.courseId,
                        durationMinutes = suitableTask.estimatedMinutes
                    )
                )
            } else {
                // 没有合适任务，安排复习
                val weakCourse = courses.filter { it.masteryLevel < 0.5f }
                    .minByOrNull { it.masteryLevel }

                if (weakCourse != null) {
                    timeSlots.add(
                        TimeSlot(
                            startTime = freeSlot.startTime,
                            endTime = calculateEndTimeFromStart(freeSlot.startTime, 25),
                            type = SlotType.REVIEW,
                            title = "${weakCourse.name}·复习",
                            courseId = weakCourse.id,
                            durationMinutes = 25
                        )
                    )
                }
            }
        }

        // 按时间排序
        timeSlots.sortBy { parseTimeToMinutes(it.startTime) }

        return DailyPlan(
            dayOfWeek = dayIndex + 1,
            dayName = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[dayIndex],
            timeSlots = timeSlots,
            totalStudyMinutes = timeSlots.filter { it.type == SlotType.STUDY || it.type == SlotType.REVIEW }
                .sumOf { it.durationMinutes }
        )
    }

    /**
     * 找出空闲时段
     */
    private fun findFreeSlots(
        daySchedule: List<Pair<Course, ScheduleSlot>>,
        preferences: StudyPreferences
    ): List<FreeSlot> {
        val freeSlots = mutableListOf<FreeSlot>()
        val classTimes = daySchedule.map { it.second }
            .sortedBy { it.startHour * 60 + it.startMinute }

        // 从早上8点开始
        var currentHour = preferences.preferredStudyHours.minOrNull() ?: 8
        var currentMinute = 0

        for (classTime in classTimes) {
            val classStartMinutes = classTime.startHour * 60 + classTime.startMinute
            val currentMinutes = currentHour * 60 + currentMinute

            if (classStartMinutes > currentMinutes + 30) { // 至少30分钟空闲
                freeSlots.add(
                    FreeSlot(
                        startTime = "${currentHour}:${currentMinute.toString().padStart(2, '0')}",
                        durationMinutes = classStartMinutes - currentMinutes
                    )
                )
            }

            // 移动到课程结束
            currentHour = classTime.startHour + (classTime.startMinute + classTime.durationMinutes) / 60
            currentMinute = (classTime.startMinute + classTime.durationMinutes) % 60
        }

        // 晚上空闲时间
        val lastHour = preferences.preferredStudyHours.maxOrNull() ?: 22
        val currentMinutes = currentHour * 60 + currentMinute
        val lastMinutes = lastHour * 60

        if (lastMinutes > currentMinutes + 30) {
            freeSlots.add(
                FreeSlot(
                    startTime = "${currentHour}:${currentMinute.toString().padStart(2, '0')}",
                    durationMinutes = lastMinutes - currentMinutes
                )
            )
        }

        return freeSlots
    }

    /**
     * 生成计划建议
     */
    private fun generatePlanSuggestions(
        courses: List<Course>,
        tasks: List<Task>,
        weekStudyMinutes: Int,
        targetMinutes: Int
    ): List<String> {
        val suggestions = mutableListOf<String>()

        // 学习时长分析
        if (weekStudyMinutes < targetMinutes * 0.5) {
            suggestions.add("本周学习进度较慢，建议增加学习时间")
        } else if (weekStudyMinutes >= targetMinutes) {
            suggestions.add("🎉 已达成本周学习目标！继续保持")
        }

        // 任务完成情况
        val pendingTasks = tasks.count { it.status == TaskStatus.PENDING }
        if (pendingTasks > 5) {
            suggestions.add("有${pendingTasks}个待完成任务，建议优先处理高优先级任务")
        }

        // 课程掌握度分析
        val weakCourses = courses.filter { it.masteryLevel < 0.5f }
        if (weakCourses.isNotEmpty()) {
            suggestions.add("「${weakCourses.first().name}」掌握度较低，建议重点复习")
        }

        // 遗忘曲线提醒
        val daysSinceLastStudy = courses.map { course ->
            if (course.lastStudiedAt != null) {
                ((System.currentTimeMillis() - course.lastStudiedAt) / (1000 * 60 * 60 * 24)).toInt()
            } else 999
        }.maxOrNull() ?: 0

        if (daysSinceLastStudy >= 3) {
            suggestions.add("有些课程已超过3天未学习，建议及时复习防止遗忘")
        }

        return suggestions
    }

    // ==================== 辅助函数 ====================

    private fun calculateEndTime(slot: ScheduleSlot): String {
        val totalMinutes = slot.startHour * 60 + slot.startMinute + slot.durationMinutes
        val endHour = totalMinutes / 60
        val endMinute = totalMinutes % 60
        return "$endHour:${endMinute.toString().padStart(2, '0')}"
    }

    private fun calculateEndTimeFromStart(startTime: String, durationMinutes: Int): String {
        val parts = startTime.split(":")
        val startMinutes = parts[0].toInt() * 60 + parts[1].toInt()
        val endMinutes = startMinutes + durationMinutes
        val endHour = endMinutes / 60
        val endMinute = endMinutes % 60
        return "$endHour:${endMinute.toString().padStart(2, '0')}"
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun getWeekStartTime(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

// ==================== 数据模型 ====================

/**
 * 周学习计划
 */
data class WeeklyStudyPlan(
    val id: String,
    val dailyPlans: List<DailyPlan>,
    val totalTargetMinutes: Int,
    val completedMinutes: Int,
    val remainingMinutes: Int,
    val suggestions: List<String>
)

/**
 * 单日计划
 */
data class DailyPlan(
    val dayOfWeek: Int,
    val dayName: String,
    val timeSlots: List<TimeSlot>,
    val totalStudyMinutes: Int
)

/**
 * 时间段
 */
data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val type: SlotType,
    val title: String,
    val courseId: String? = null,
    val taskId: String? = null,
    val durationMinutes: Int
)

enum class SlotType {
    CLASS,    // 课程
    STUDY,    // 学习任务
    REVIEW,   // 复习
    BREAK,    // 休息
    FREE      // 空闲
}

/**
 * 空闲时段
 */
data class FreeSlot(
    val startTime: String,
    val durationMinutes: Int
)