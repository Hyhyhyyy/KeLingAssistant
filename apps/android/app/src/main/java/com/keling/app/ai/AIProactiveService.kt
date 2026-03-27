package com.keling.app.ai

import com.keling.app.data.*
import java.util.Calendar

/**
 * =========================
 * AI 主动提醒系统
 * =========================
 *
 * 功能：
 * - 薄弱点预警：检测长期未学习的薄弱课程
 * - 遗忘曲线提醒：基于艾宾浩斯曲线提醒复习
 * - 课表智能提醒：上课前提醒预习/复习
 * - 连续学习激励：维持学习动力的提醒
 * - 自定义提醒：用户设置的个性化提醒
 */

// ==================== 数据模型 ====================

/**
 * 提醒类型
 */
enum class AlertType(val displayName: String, val priority: Int) {
    WEAKNESS("薄弱点预警", 3),
    FORGETTING("遗忘提醒", 2),
    SCHEDULE("课表提醒", 4),
    STREAK("连续学习", 1),
    GOAL("目标提醒", 2),
    CUSTOM("自定义提醒", 1)
}

/**
 * 提醒消息
 */
data class Alert(
    val id: String = "alert_${System.currentTimeMillis()}",
    val type: AlertType,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val courseId: String? = null,
    val knowledgeNodeId: String? = null,
    val actionSuggestion: String? = null,  // 建议的操作
    var isRead: Boolean = false,
    var isDismissed: Boolean = false
)

/**
 * 薄弱点预警详情
 */
data class WeaknessAlert(
    val courseId: String,
    val courseName: String,
    val masteryLevel: Float,
    val daysSinceLastStudy: Int,
    val severity: Severity
)

enum class Severity {
    MILD,      // 轻微
    MODERATE,  // 中等
    SEVERE     // 严重
}

/**
 * 遗忘曲线提醒详情
 */
data class ForgettingAlert(
    val nodeId: String,
    val nodeName: String,
    val courseId: String?,
    val retentionRate: Float,
    val daysOverdue: Int,
    val recommendedAction: String
)

/**
 * 课表提醒详情
 */
data class ScheduleAlert(
    val courseId: String,
    val courseName: String,
    val startTime: String,
    val location: String,
    val minutesUntil: Int,
    val suggestedPrep: String
)

/**
 * 连续学习提醒
 */
data class StreakAlert(
    val currentStreak: Int,
    val bestStreak: Int,
    val message: String,
    val reward: Rewards?
)

// ==================== AI 主动提醒服务 ====================

/**
 * AI 主动提醒服务
 */
class AIProactiveService(
    private val dataProvider: LearningDataProvider
) {

    private val _alerts = mutableListOf<Alert>()
    val alerts: List<Alert> get() = _alerts.toList()

    private val _unreadCount = mutableStateOf(0)
    val unreadCount: Int get() = _unreadCount.value

    // ==================== 薄弱点预警 ====================

    /**
     * 检查薄弱点预警
     */
    fun checkWeaknessAlert(): Alert? {
        val courses = dataProvider.getCourses()
        val weakCourses = courses.filter { it.masteryLevel < 0.6f }

        if (weakCourses.isEmpty()) return null

        // 计算每门课距上次学习的天数
        val now = System.currentTimeMillis()
        val weakWithDays = weakCourses.map { course ->
            val daysSince = course.lastStudiedAt?.let {
                ((now - it) / (1000 * 60 * 60 * 24)).toInt()
            } ?: 999
            course to daysSince
        }.sortedByDescending { it.second }

        // 找到最需要关注的
        val (course, daysSince) = weakWithDays.first()

        // 判断严重程度
        val severity = when {
            daysSince >= 7 -> Severity.SEVERE
            daysSince >= 3 -> Severity.MODERATE
            else -> Severity.MILD
        }

        // 只有中等及以上才发提醒
        if (severity == Severity.MILD) return null

        val title = when (severity) {
            Severity.SEVERE -> "🚨 ${course.name} 严重干旱！"
            Severity.MODERATE -> "⚠️ ${course.name} 需要关注"
            else -> null
        } ?: return null

        val message = buildString {
            append("「${course.name}」掌握度 ${(course.masteryLevel * 100).toInt()}%")
            if (daysSince >= 999) {
                append("，从未学习过！")
            } else {
                append("，已 $daysSince 天未学习。")
            }
            append("建议尽快安排学习时间。")
        }

        return Alert(
            type = AlertType.WEAKNESS,
            title = title,
            message = message,
            courseId = course.id,
            actionSuggestion = "开始学习 ${course.name}"
        ).also { addAlert(it) }
    }

    /**
     * 获取所有薄弱点预警
     */
    fun getAllWeaknessAlerts(): List<WeaknessAlert> {
        val courses = dataProvider.getCourses()
        val now = System.currentTimeMillis()

        return courses
            .filter { it.masteryLevel < 0.6f }
            .map { course ->
                val daysSince = course.lastStudiedAt?.let {
                    ((now - it) / (1000 * 60 * 60 * 24)).toInt()
                } ?: 999

                val severity = when {
                    daysSince >= 7 -> Severity.SEVERE
                    daysSince >= 3 -> Severity.MODERATE
                    else -> Severity.MILD
                }

                WeaknessAlert(
                    courseId = course.id,
                    courseName = course.name,
                    masteryLevel = course.masteryLevel,
                    daysSinceLastStudy = daysSince,
                    severity = severity
                )
            }
            .sortedByDescending { it.severity.ordinal }
    }

    // ==================== 遗忘曲线提醒 ====================

    /**
     * 检查遗忘曲线提醒
     */
    fun checkForgettingCurve(): List<ForgettingAlert> {
        val nodes = dataProvider.getKnowledgeNodes()
        val notes = dataProvider.getNotes()
        val now = System.currentTimeMillis()

        val alerts = mutableListOf<ForgettingAlert>()

        nodes.forEach { node ->
            // 找到相关笔记
            val relatedNotes = notes.filter { node.id in it.relatedNodeIds }
            val lastReview = relatedNotes.maxOfOrNull { it.lastReviewedAt ?: it.updatedAt }
            val reviewCount = relatedNotes.sumOf { it.reviewCount }

            // 计算记忆保留率
            val daysSinceReview = lastReview?.let {
                ((now - it) / (1000 * 60 * 60 * 24)).toFloat()
            } ?: 30f

            val stability = 1f + reviewCount * 0.5f
            val retentionRate = kotlin.math.exp(-daysSinceReview / stability).toFloat()

            // 如果保留率低于50%或超过建议复习时间
            if (retentionRate < 0.5f || daysSinceReview > getReviewInterval(reviewCount)) {
                alerts.add(
                    ForgettingAlert(
                        nodeId = node.id,
                        nodeName = node.name,
                        courseId = node.courseId,
                        retentionRate = retentionRate,
                        daysOverdue = (daysSinceReview - getReviewInterval(reviewCount)).toInt().coerceAtLeast(0),
                        recommendedAction = getReviewAction(node, retentionRate)
                    )
                )
            }
        }

        return alerts.sortedBy { it.retentionRate }
    }

    /**
     * 获取建议复习间隔（天）
     */
    private fun getReviewInterval(reviewCount: Int): Float {
        // 艾宾浩斯遗忘曲线建议间隔
        val intervals = listOf(1f, 2f, 4f, 7f, 15f, 30f)
        return intervals.getOrNull(reviewCount) ?: 30f
    }

    /**
     * 获取复习行动建议
     */
    private fun getReviewAction(node: KnowledgeNode, retentionRate: Float): String {
        return when {
            retentionRate < 0.3 -> "建议重新学习基础概念"
            retentionRate < 0.5 -> "建议做针对性练习巩固"
            else -> "快速复习即可恢复记忆"
        }
    }

    /**
     * 生成遗忘提醒
     */
    fun generateForgettingAlert(): Alert? {
        val forgettingAlerts = checkForgettingCurve()
        if (forgettingAlerts.isEmpty()) return null

        val alert = forgettingAlerts.first()
        val retention = (alert.retentionRate * 100).toInt()

        return Alert(
            type = AlertType.FORGETTING,
            title = "🔄 「${alert.nodeName}」需要复习",
            message = "记忆保留率已降至 $retention%，建议尽快复习以巩固知识。",
            knowledgeNodeId = alert.nodeId,
            courseId = alert.courseId,
            actionSuggestion = "开始复习 ${alert.nodeName}"
        ).also { addAlert(it) }
    }

    // ==================== 课表提醒 ====================

    /**
     * 检查课表提醒
     */
    fun checkScheduleReminder(): Alert? {
        val cal = Calendar.getInstance()
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMinute

        val todaySchedule = dataProvider.getTodaySchedule(dayOfWeek)
        if (todaySchedule.isEmpty()) return null

        // 找到最近的课程
        val upcoming = todaySchedule.filter { (_, slot) ->
            val slotMinutes = slot.startHour * 60 + slot.startMinute
            slotMinutes > currentMinutes
        }.minByOrNull { (_, slot) ->
            slot.startHour * 60 + slot.startMinute
        }

        if (upcoming == null) return null

        val (course, slot) = upcoming
        val slotMinutes = slot.startHour * 60 + slot.startMinute
        val minutesUntil = slotMinutes - currentMinutes

        // 只在课前30分钟到5分钟提醒
        if (minutesUntil > 30 || minutesUntil < 5) return null

        // 生成预习建议
        val prepSuggestion = generatePrepSuggestion(course, minutesUntil)

        return Alert(
            type = AlertType.SCHEDULE,
            title = "📚 即将上课：${course.name}",
            message = "还有 $minutesUntil 分钟开始，地点：${course.location}。$prepSuggestion",
            courseId = course.id,
            actionSuggestion = "查看 ${course.name} 知识点"
        ).also { addAlert(it) }
    }

    /**
     * 生成预习建议
     */
    private fun generatePrepSuggestion(course: Course, minutesUntil: Int): String {
        val nodes = dataProvider.getKnowledgeNodes().filter { it.courseId == course.id }

        return when {
            minutesUntil >= 20 && nodes.any { it.masteryLevel < 0.5f } -> {
                val weakNode = nodes.filter { it.masteryLevel < 0.5f }.minByOrNull { it.masteryLevel }
                "建议快速复习「${weakNode?.name ?: "薄弱点"}」。"
            }
            minutesUntil >= 10 -> "可以简单回顾上节课内容。"
            else -> "准备好课本和笔记，准备上课。"
        }
    }

    /**
     * 获取今日课表提醒
     */
    fun getTodayScheduleAlerts(): List<ScheduleAlert> {
        val cal = Calendar.getInstance()
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMinute

        val todaySchedule = dataProvider.getTodaySchedule(dayOfWeek)

        return todaySchedule.map { (course, slot) ->
            val slotMinutes = slot.startHour * 60 + slot.startMinute
            val minutesUntil = slotMinutes - currentMinutes

            val prep = when {
                minutesUntil in 10..30 -> "建议复习上节课内容"
                minutesUntil < 10 -> "即将开始"
                else -> ""
            }

            ScheduleAlert(
                courseId = course.id,
                courseName = course.name,
                startTime = "${slot.startHour}:${"%02d".format(slot.startMinute)}",
                location = course.location,
                minutesUntil = minutesUntil,
                suggestedPrep = prep
            )
        }
    }

    // ==================== 连续学习提醒 ====================

    /**
     * 检查连续学习状态
     */
    fun checkStreakStatus(): StreakAlert? {
        val user = dataProvider.getUser()

        // 如果今天还没学习，提醒保持连续
        val todayTasks = dataProvider.getTasks().filter {
            it.status == TaskStatus.COMPLETED &&
            isToday(it.completedAt ?: 0)
        }

        if (todayTasks.isEmpty() && user.streakDays > 0) {
            return StreakAlert(
                currentStreak = user.streakDays,
                bestStreak = user.streakDays, // 实际应该有历史记录
                message = "今天还没学习，保持连续 ${user.streakDays} 天的记录！",
                reward = Rewards(energy = 20, crystals = 10, exp = 30)
            )
        }

        // 如果刚完成今天的学习，给予鼓励
        if (todayTasks.isNotEmpty() && user.streakDays > 0) {
            val message = when {
                user.streakDays >= 30 -> "太厉害了！连续学习 ${user.streakDays} 天！"
                user.streakDays >= 14 -> "坚持得很好！连续学习 ${user.streakDays} 天！"
                user.streakDays >= 7 -> "一周达成！连续学习 ${user.streakDays} 天！"
                else -> "继续保持！连续学习 ${user.streakDays} 天！"
            }

            return StreakAlert(
                currentStreak = user.streakDays,
                bestStreak = user.streakDays,
                message = message,
                reward = null
            )
        }

        return null
    }

    /**
     * 生成连续学习提醒
     */
    fun generateStreakAlert(): Alert? {
        val streak = checkStreakStatus() ?: return null

        return Alert(
            type = AlertType.STREAK,
            title = "🔥 学习连续性",
            message = streak.message,
            actionSuggestion = if (streak.reward != null) "开始学习" else null
        ).also { addAlert(it) }
    }

    // ==================== 综合提醒检查 ====================

    /**
     * 执行所有提醒检查
     */
    fun checkAllAlerts(): List<Alert> {
        val allAlerts = mutableListOf<Alert>()

        // 按优先级检查
        checkWeaknessAlert()?.let { allAlerts.add(it) }
        checkScheduleReminder()?.let { allAlerts.add(it) }
        generateForgettingAlert()?.let { allAlerts.add(it) }
        generateStreakAlert()?.let { allAlerts.add(it) }

        return allAlerts.sortedByDescending { it.type.priority }
    }

    /**
     * 获取当前应该显示的提醒
     */
    fun getCurrentAlert(): Alert? {
        return _alerts.firstOrNull { !it.isDismissed && !it.isRead }
    }

    /**
     * 获取所有未读提醒
     */
    fun getUnreadAlerts(): List<Alert> {
        return _alerts.filter { !it.isRead && !it.isDismissed }
    }

    // ==================== 提醒管理 ====================

    /**
     * 添加提醒
     */
    private fun addAlert(alert: Alert) {
        // 避免重复提醒
        if (_alerts.none { it.type == alert.type && it.courseId == alert.courseId && !it.isDismissed }) {
            _alerts.add(0, alert)
            _unreadCount.value++
        }
    }

    /**
     * 标记为已读
     */
    fun markAsRead(alertId: String) {
        _alerts.find { it.id == alertId }?.let {
            if (!it.isRead) {
                it.isRead = true
                _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    /**
     * 忽略提醒
     */
    fun dismissAlert(alertId: String) {
        _alerts.find { it.id == alertId }?.let {
            it.isDismissed = true
            if (!it.isRead) {
                _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    /**
     * 清除所有提醒
     */
    fun clearAllAlerts() {
        _alerts.clear()
        _unreadCount.value = 0
    }

    /**
     * 清除已读提醒
     */
    fun clearReadAlerts() {
        _alerts.removeAll { it.isRead || it.isDismissed }
        _unreadCount.value = _alerts.count { !it.isRead && !it.isDismissed }
    }

    // ==================== 工具方法 ====================

    private fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_YEAR)
        cal.timeInMillis = timestamp
        return cal.get(Calendar.DAY_OF_YEAR) == today
    }
}

// ==================== 状态包装器 ====================

private class mutableStateOf<T>(initialValue: T) {
    var value: T = initialValue
}