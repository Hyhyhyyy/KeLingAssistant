package com.keling.app.ai

import com.keling.app.data.*
import java.util.Calendar

/**
 * =========================
 * 增强版学习画像分析模块
 * =========================
 *
 * 功能：
 * - 基础用户画像摘要
 * - 学习风格识别（视觉型/听觉型/读写型/动觉型）
 * - 黄金学习时段分析
 * - 知识掌握趋势追踪
 * - 遗忘曲线预测
 * - 完整上下文构建
 */

/**
 * 学习风格类型
 */
enum class LearningStyle(val displayName: String, val description: String) {
    VISUAL("视觉型", "偏好图表、图像、思维导图"),
    AUDITORY("听觉型", "偏好讲解、讨论、音频材料"),
    READ_WRITE("读写型", "偏好文字、笔记、阅读材料"),
    KINESTHETIC("动觉型", "偏好实践、操作、动手练习"),
    MIXED("混合型", "多种学习方式均衡")
}

/**
 * 黄金学习时段
 */
data class PeakHour(
    val hour: Int,
    val score: Float,        // 效率评分 0-1
    val taskCount: Int,      // 该时段完成的任务数
    val avgDuration: Float   // 平均学习时长（分钟）
)

/**
 * 遗忘曲线预测
 */
data class ForgetPrediction(
    val nodeId: String,
    val nodeName: String,
    val lastReviewTime: Long?,
    val retentionRate: Float,     // 当前记忆保留率 0-1
    val nextReviewDue: Long,      // 下次建议复习时间
    val urgencyLevel: Int         // 紧急程度 1-5
)

/**
 * 知识掌握趋势
 */
data class MasteryTrend(
    val courseId: String,
    val courseName: String,
    val currentLevel: Float,
    val trend: TrendDirection,    // 趋势方向
    val weeklyData: List<Float>,  // 最近7天数据
    val weeklyChange: Float       // 周变化率
)

enum class TrendDirection {
    RISING,     // 上升
    STABLE,     // 稳定
    DECLINING   // 下降
}

/**
 * 学习画像完整报告
 */
data class LearningProfileReport(
    val userSummary: String,
    val style: LearningStyle,
    val peakHours: List<PeakHour>,
    val weaknessAnalysis: String,
    val masteryTrends: List<MasteryTrend>,
    val forgetPredictions: List<ForgetPrediction>,
    val recommendations: List<String>
)

/**
 * 增强版学习画像提供器
 */
class EnhancedLearningProfileProvider(
    private val viewModel: LearningDataProvider
) {

    // ==================== 基础画像 ====================

    /**
     * 构建用户基础摘要
     */
    fun buildUserSummary(): String {
        val user = viewModel.getUser()
        return buildString {
            appendLine("【用户画像】")
            appendLine("  姓名: ${user.name}")
            appendLine("  等级: Lv.${user.level}")
            appendLine("  连续学习: ${user.streakDays}天")
            appendLine("  总学习时长: ${formatMinutes(user.totalStudyMinutes)}")
            appendLine("  能量: ${user.energy}⚡ 结晶: ${user.crystals}💎")
        }.trimEnd()
    }

    /**
     * 构建今日摘要
     */
    fun buildTodaySummary(): String {
        val tasks = viewModel.getTasks()
        val pending = tasks.filter { it.status == TaskStatus.PENDING }
        val inProgress = tasks.filter { it.status == TaskStatus.IN_PROGRESS }
        val completed = tasks.filter { it.status == TaskStatus.COMPLETED }

        val remainingMinutes = (pending + inProgress).sumOf { it.estimatedMinutes.toLong() }
        val topTasks = (pending + inProgress)
            .sortedByDescending { it.priority }
            .take(5)

        return buildString {
            appendLine("【今日状态】")
            appendLine("  待完成: ${pending.size} 进行中: ${inProgress.size} 已完成: ${completed.size}")
            appendLine("  预计剩余: ${formatMinutes(remainingMinutes.toInt())}")

            if (topTasks.isNotEmpty()) {
                appendLine("  优先任务:")
                topTasks.forEach { task ->
                    val priorityIcon = when {
                        task.priority >= 5 -> "🔴"
                        task.priority >= 4 -> "🟡"
                        else -> "🟢"
                    }
                    appendLine("    $priorityIcon ${task.title} (${task.estimatedMinutes}min)")
                }
            }
        }.trimEnd()
    }

    // ==================== 学习风格识别 ====================

    /**
     * 识别用户学习风格
     * 基于用户行为数据推断
     */
    fun detectLearningStyle(): LearningStyle {
        val notes = viewModel.getNotes()
        val tasks = viewModel.getTasks()

        // 简化版本：基于笔记类型和任务偏好推断
        var visualScore = 0f
        var auditoryScore = 0f
        var readWriteScore = 0f
        var kinestheticScore = 0f

        // 分析笔记来源
        notes.forEach { note ->
            when (note.sourceType) {
                NoteSource.CLASS_CAPTURE -> {
                    // 课堂拍照可能是视觉型
                    visualScore += 0.5f
                }
                NoteSource.AI_GENERATED -> {
                    // AI 生成的文字解释，读写型
                    readWriteScore += 0.3f
                }
                NoteSource.USER_CREATED -> {
                    // 手写笔记，读写型
                    readWriteScore += 0.5f
                }
                else -> {}
            }
        }

        // 分析任务类型
        tasks.forEach { task ->
            when (task.type) {
                TaskType.DEEP_EXPLORATION -> readWriteScore += 0.3f
                TaskType.REVIEW_RITUAL -> readWriteScore += 0.2f
                TaskType.BOUNTY, TaskType.RESCUE -> kinestheticScore += 0.4f
                TaskType.DAILY_CARE -> kinestheticScore += 0.2f
            }
        }

        // 返回得分最高的风格
        val scores = mapOf(
            LearningStyle.VISUAL to visualScore,
            LearningStyle.AUDITORY to auditoryScore,
            LearningStyle.READ_WRITE to readWriteScore,
            LearningStyle.KINESTHETIC to kinestheticScore
        )

        val maxScore = scores.maxByOrNull { it.value }
        return if (maxScore != null && maxScore.value > 0) {
            // 如果分数差异不大，返回混合型
            val secondMax = scores.filter { it.key != maxScore.key }.maxByOrNull { it.value }
            if (secondMax != null && maxScore.value - secondMax.value < 0.3f) {
                LearningStyle.MIXED
            } else {
                maxScore.key
            }
        } else {
            LearningStyle.MIXED  // 默认混合型
        }
    }

    /**
     * 构建学习风格上下文
     */
    fun buildLearningStyleContext(): String {
        val style = detectLearningStyle()
        return buildString {
            appendLine("【学习风格】${style.displayName}")
            appendLine("  ${style.description}")
        }.trimEnd()
    }

    // ==================== 黄金时段分析 ====================

    /**
     * 分析黄金学习时段
     */
    fun analyzePeakHours(): List<PeakHour> {
        val tasks = viewModel.getTasks().filter { it.status == TaskStatus.COMPLETED }

        if (tasks.isEmpty()) {
            // 没有数据，返回默认建议
            return listOf(
                PeakHour(9, 0.8f, 0, 30f),
                PeakHour(14, 0.7f, 0, 30f),
                PeakHour(19, 0.6f, 0, 30f)
            )
        }

        // 按小时统计
        val hourStats = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()  // hour -> [(duration, priority)]

        tasks.forEach { task ->
            val completedHour = task.completedAt?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                cal.get(Calendar.HOUR_OF_DAY)
            } ?: return@forEach

            hourStats.getOrPut(completedHour) { mutableListOf() }
                .add(Pair(task.actualMinutes ?: task.estimatedMinutes, task.priority))
        }

        // 计算每个时段的效率评分
        return hourStats.map { (hour, stats) ->
            val count = stats.size
            val totalDuration = stats.sumOf { it.first }.toFloat()
            val avgDuration = if (count > 0) totalDuration / count else 30f
            val avgPriority = stats.map { it.second }.average().toFloat()

            // 效率评分 = 任务数量因子 + 优先级因子 + 时长因子
            val countFactor = (count / 5f).coerceIn(0f, 1f) * 0.4f
            val priorityFactor = (avgPriority / 5f).coerceIn(0f, 1f) * 0.3f
            val durationFactor = (avgDuration / 60f).coerceIn(0f, 1f) * 0.3f
            val score = (countFactor + priorityFactor + durationFactor).coerceIn(0f, 1f)

            PeakHour(hour, score, count, avgDuration)
        }.sortedByDescending { it.score }
    }

    /**
     * 构建黄金时段上下文
     */
    fun buildPeakHoursContext(): String {
        val peaks = analyzePeakHours().take(3)

        return buildString {
            appendLine("【黄金时段】")
            if (peaks.isEmpty()) {
                appendLine("  暂无足够数据分析，建议尝试在上午9点或下午2点学习")
            } else {
                peaks.forEach { peak ->
                    val timeStr = "${peak.hour}:00"
                    val efficiency = (peak.score * 100).toInt()
                    appendLine("  $timeStr (效率$efficiency%, ${peak.taskCount}次任务)")
                }
            }
        }.trimEnd()
    }

    // ==================== 遗忘曲线预测 ====================

    /**
     * 预测遗忘曲线
     * 基于艾宾浩斯遗忘曲线 + 用户历史复习效果
     */
    fun predictForgetCurve(nodeId: String): ForgetPrediction? {
        val node = viewModel.getKnowledgeNodes().find { it.id == nodeId } ?: return null
        val notes = viewModel.getNotes().filter { node.id in it.relatedNodeIds }

        val lastReviewTime = notes.maxOfOrNull { it.lastReviewedAt ?: it.updatedAt }
        val reviewCount = notes.sumOf { it.reviewCount }

        // 艾宾浩斯遗忘曲线简化模型
        // 记忆保留率 = e^(-t/S)，其中 t 是时间，S 是稳定性
        val now = System.currentTimeMillis()
        val daysSinceReview = lastReviewTime?.let {
            ((now - it) / (1000 * 60 * 60 * 24)).toFloat()
        } ?: 30f

        // 稳定性随复习次数增加
        val stability = 1f + reviewCount * 0.5f
        val retentionRate = kotlin.math.exp(-daysSinceReview / stability).coerceIn(0f, 1f)

        // 下次复习时间（基于遗忘曲线关键节点）
        val reviewIntervals = listOf(1, 2, 4, 7, 15, 30)  // 天
        val nextInterval = reviewIntervals.getOrNull(reviewCount) ?: 30
        val nextReviewDue = lastReviewTime?.plus(nextInterval * 24 * 60 * 60 * 1000L)
            ?: now - 24 * 60 * 60 * 1000L  // 如果从未复习，立即需要

        // 紧急程度
        val urgencyLevel = when {
            retentionRate < 0.3f -> 5
            retentionRate < 0.5f -> 4
            retentionRate < 0.7f -> 3
            nextReviewDue < now -> 4
            else -> 2
        }

        return ForgetPrediction(
            nodeId = node.id,
            nodeName = node.name,
            lastReviewTime = lastReviewTime,
            retentionRate = retentionRate,
            nextReviewDue = nextReviewDue,
            urgencyLevel = urgencyLevel
        )
    }

    /**
     * 获取所有需要复习的知识点
     */
    fun getAllForgetPredictions(): List<ForgetPrediction> {
        return viewModel.getKnowledgeNodes()
            .mapNotNull { predictForgetCurve(it.id) }
            .sortedByDescending { it.urgencyLevel }
    }

    /**
     * 构建复习预测上下文
     */
    fun buildForgetPredictionContext(): String {
        val predictions = getAllForgetPredictions().take(5)

        return buildString {
            appendLine("【遗忘预警】")
            if (predictions.isEmpty()) {
                appendLine("  暂无知识点需要复习")
            } else {
                predictions.forEach { pred ->
                    val urgencyIcon = when (pred.urgencyLevel) {
                        5 -> "🔴"
                        4 -> "🟠"
                        3 -> "🟡"
                        else -> "🟢"
                    }
                    val retention = (pred.retentionRate * 100).toInt()
                    appendLine("  $urgencyIcon ${pred.nodeName} (保留率$retention%)")
                }
            }
        }.trimEnd()
    }

    // ==================== 知识掌握趋势 ====================

    /**
     * 获取课程掌握度趋势
     */
    fun getMasteryTrend(courseId: String): MasteryTrend? {
        val course = viewModel.getCourses().find { it.id == courseId } ?: return null

        // 简化版本：目前只有一个当前值
        // 实际应用中应该追踪历史数据
        val currentLevel = course.masteryLevel

        // 模拟趋势数据（实际应该从历史记录读取）
        val weeklyData = (0..6).map { day ->
            currentLevel - (6 - day) * 0.02f + (kotlin.random.Random.nextFloat() * 0.02f)
        }.map { it.coerceIn(0f, 1f) }

        val weeklyChange = weeklyData.last() - weeklyData.first()
        val trend = when {
            weeklyChange > 0.05f -> TrendDirection.RISING
            weeklyChange < -0.05f -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }

        return MasteryTrend(
            courseId = course.id,
            courseName = course.name,
            currentLevel = currentLevel,
            trend = trend,
            weeklyData = weeklyData,
            weeklyChange = weeklyChange
        )
    }

    /**
     * 获取所有课程趋势
     */
    fun getAllMasteryTrends(): List<MasteryTrend> {
        return viewModel.getCourses().mapNotNull { getMasteryTrend(it.id) }
    }

    /**
     * 构建掌握度趋势上下文
     */
    fun buildMasteryTrendContext(): String {
        val trends = getAllMasteryTrends()

        return buildString {
            appendLine("【掌握度趋势】")
            trends.forEach { trend ->
                val trendIcon = when (trend.trend) {
                    TrendDirection.RISING -> "📈"
                    TrendDirection.STABLE -> "➡️"
                    TrendDirection.DECLINING -> "📉"
                }
                val level = (trend.currentLevel * 100).toInt()
                appendLine("  $trendIcon ${trend.courseName} $level%")
            }
        }.trimEnd()
    }

    // ==================== 薄弱点分析 ====================

    /**
     * 构建薄弱点画像
     */
    fun buildWeaknessProfile(): String {
        val courses = viewModel.getCourses()
        val tasks = viewModel.getTasks()

        if (courses.isEmpty()) return "【薄弱点】暂无课程数据"

        val weakCourses = courses
            .filter { it.masteryLevel < 0.6f }
            .sortedBy { it.masteryLevel }
            .take(5)

        return buildString {
            appendLine("【薄弱点分析】")
            if (weakCourses.isEmpty()) {
                appendLine("  各门课程状态良好")
            } else {
                weakCourses.forEach { course ->
                    val relatedPending = tasks.count {
                        it.courseId == course.id && it.status == TaskStatus.PENDING
                    }
                    val level = (course.masteryLevel * 100).toInt()
                    appendLine("  - ${course.name} (${level}%, ${relatedPending} tasks pending)")
                }
            }
        }.trimEnd()
    }

    // ==================== 课表上下文 ====================

    /**
     * 构建课表上下文
     */
    fun buildScheduleContext(): String {
        val cal = Calendar.getInstance()
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
        val todaySlots = viewModel.getTodaySchedule(dayOfWeek)
        val (prev, current, next) = viewModel.getCurrentPrevNextSlots(
            dayOfWeek,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )

        return buildString {
            appendLine("【今日课表】")
            if (todaySlots.isEmpty()) {
                appendLine("  今天没有课程安排")
            } else {
                todaySlots.forEach { (c, s) ->
                    val endH = s.startHour + (s.startMinute + s.durationMinutes) / 60
                    val endM = (s.startMinute + s.durationMinutes) % 60
                    appendLine("  ${c.name} ${s.startHour}:${"%02d".format(s.startMinute)}-${endH}:${"%02d".format(endM)}")
                }
                appendLine("  当前: 上一节=${prev?.first?.name ?: "无"}, 正在=${current?.first?.name ?: "无"}, 下一节=${next?.first?.name ?: "无"}")
            }
        }.trimEnd()
    }

    // ==================== 完整上下文 ====================

    /**
     * 构建"今日学习计划"模式的聚合上下文
     */
    fun buildTodayPlanContext(): String {
        return listOf(
            buildUserSummary(),
            buildTodaySummary(),
            buildScheduleContext(),
            buildWeaknessProfile()
        ).joinToString("\n\n")
    }

    /**
     * 构建完整的学习画像上下文（用于 AI 调用）
     */
    fun buildFullContext(): String {
        return listOf(
            buildUserSummary(),
            buildLearningStyleContext(),
            buildPeakHoursContext(),
            buildTodaySummary(),
            buildScheduleContext(),
            buildWeaknessProfile(),
            buildMasteryTrendContext(),
            buildForgetPredictionContext()
        ).joinToString("\n\n")
    }

    /**
     * 生成完整的学习画像报告
     */
    fun generateReport(): LearningProfileReport {
        return LearningProfileReport(
            userSummary = buildUserSummary(),
            style = detectLearningStyle(),
            peakHours = analyzePeakHours(),
            weaknessAnalysis = buildWeaknessProfile(),
            masteryTrends = getAllMasteryTrends(),
            forgetPredictions = getAllForgetPredictions(),
            recommendations = generateRecommendations()
        )
    }

    /**
     * 生成个性化建议
     */
    fun generateRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()

        // 基于薄弱点的建议
        val weakCourses = viewModel.getCourses().filter { it.masteryLevel < 0.6f }
        if (weakCourses.isNotEmpty()) {
            recommendations.add("优先攻克「${weakCourses.first().name}」，每天至少投入30分钟")
        }

        // 基于遗忘曲线的建议
        val urgentReviews = getAllForgetPredictions().filter { it.urgencyLevel >= 4 }
        if (urgentReviews.isNotEmpty()) {
            recommendations.add("「${urgentReviews.first().nodeName}」需要尽快复习，记忆保留率已下降")
        }

        // 基于学习风格的建议
        val style = detectLearningStyle()
        when (style) {
            LearningStyle.VISUAL -> recommendations.add("建议多使用思维导图、图表辅助学习")
            LearningStyle.AUDITORY -> recommendations.add("可以尝试听讲座、朗读笔记来加深记忆")
            LearningStyle.READ_WRITE -> recommendations.add("记笔记、写总结会帮助你更好地理解")
            LearningStyle.KINESTHETIC -> recommendations.add("多做题、动手实践能让你学得更牢固")
            LearningStyle.MIXED -> recommendations.add("可以尝试多种学习方式结合")
        }

        // 基于黄金时段的建议
        val peaks = analyzePeakHours()
        if (peaks.isNotEmpty()) {
            val bestHour = peaks.first().hour
            recommendations.add("你的黄金学习时段是 $bestHour:00 左右，重要任务尽量安排在这个时段")
        }

        return recommendations
    }

    // ==================== 工具方法 ====================

    private fun formatMinutes(minutes: Int): String {
        return when {
            minutes < 60 -> "${minutes}分钟"
            minutes < 24 * 60 -> "${minutes / 60}小时${minutes % 60}分钟"
            else -> "${minutes / (24 * 60)}天${(minutes % (24 * 60)) / 60}小时"
        }
    }
}

/**
 * 数据提供者接口
 * 用于解耦 ViewModel 和 LearningProfileProvider
 */
interface LearningDataProvider {
    fun getUser(): User
    fun getCourses(): List<Course>
    fun getTasks(): List<Task>
    fun getNotes(): List<Note>
    fun getKnowledgeNodes(): List<KnowledgeNode>
    fun getTodaySchedule(dayOfWeek: Int): List<Pair<Course, ScheduleSlot>>
    fun getCurrentPrevNextSlots(dayOfWeek: Int, hour: Int, minute: Int): Triple<Pair<Course, ScheduleSlot>?, Pair<Course, ScheduleSlot>?, Pair<Course, ScheduleSlot>?>
}