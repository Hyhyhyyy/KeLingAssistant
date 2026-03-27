package com.keling.app.data

/**
 * =========================
 * 学习状态追踪器
 * =========================
 *
 * 实时学习状态监测与分析
 * - 精力水平追踪
 * - 心情状态追踪
 * - 专注力趋势分析
 * - 生产力指数计算
 * - AI洞察与建议
 */

import kotlinx.serialization.Serializable
import kotlin.math.abs

// ==================== 状态模型 ====================

/**
 * 精力水平
 */
enum class EnergyLevel(val displayName: String, val value: Int) {
    EXHAUSTED("精疲力尽", 1),
    TIRED("疲惫", 2),
    LOW("精力不足", 3),
    MODERATE("一般", 4),
    GOOD("精力充沛", 5),
    ENERGETIC("精力旺盛", 6);

    companion object {
        fun fromValue(value: Int): EnergyLevel {
            return entries.find { it.value == value } ?: MODERATE
        }
    }
}

/**
 * 心情状态
 */
enum class MoodState(val displayName: String, val emoji: String, val value: Int) {
    VERY_BAD("很糟糕", "😢", 1),
    BAD("不太好", "😔", 2),
    NEUTRAL("一般", "😐", 3),
    GOOD("还不错", "😊", 4),
    GREAT("很棒", "😄", 5);

    companion object {
        fun fromValue(value: Int): MoodState {
            return entries.find { it.value == value } ?: NEUTRAL
        }
    }
}

/**
 * 专注状态
 */
enum class FocusState(val displayName: String, val description: String) {
    DEEP_FOCUS("深度专注", "进入心流状态，效率极高"),
    FOCUSED("专注", "注意力集中，效率良好"),
    NORMAL("一般", "正常学习状态"),
    DISTRACTED("分心", "注意力不集中"),
    UNFOCUSED("难以专注", "无法集中注意力")
}

/**
 * 学习状态快照
 */
@Serializable
data class LearningStateSnapshot(
    val id: String,
    val userId: String,
    val timestamp: Long,
    val energyLevel: Int,                   // 1-6
    val moodState: Int,                     // 1-5
    val focusScore: Float,                  // 0-1
    val focusState: String,
    val productivity: Float,                // 0-1
    val sessionMinutes: Int,                // 本次学习时长
    val todayMinutes: Int,                  // 今日累计时长
    val completedTasks: Int,                // 今日完成任务数
    val distractions: Int,                  // 干扰次数
    val screenTime: Long,                   // 屏幕使用时间
    val breakCount: Int,                    // 休息次数
    val environment: String? = null,        // 学习环境
    val notes: String? = null               // 备注
)

/**
 * 学习状态记录
 */
@Serializable
data class LearningStateRecord(
    val date: String,                       // "2024-01-15"
    val snapshots: List<LearningStateSnapshot>,
    val averageEnergy: Float,
    val averageMood: Float,
    val averageFocus: Float,
    val averageProductivity: Float,
    val totalStudyMinutes: Int,
    val peakHours: List<Int>,               // 最佳学习时段
    val aiInsights: String
)

/**
 * 状态趋势
 */
@Serializable
data class StateTrend(
    val metric: MetricType,
    val values: List<TrendPoint>,
    val trend: TrendDirection,
    val changePercent: Float
)

enum class MetricType {
    ENERGY,
    MOOD,
    FOCUS,
    PRODUCTIVITY,
    STUDY_TIME
}

enum class TrendDirection {
    UP,           // 上升
    DOWN,         // 下降
    STABLE,       // 稳定
    FLUCTUATING   // 波动
}

@Serializable
data class TrendPoint(
    val timestamp: Long,
    val value: Float
)

// ==================== 状态追踪器 ====================

/**
 * 学习状态追踪器
 */
class LearningStateTracker(
    private val userId: String
) {
    private val snapshots = mutableListOf<LearningStateSnapshot>()
    private val dailyRecords = mutableMapOf<String, LearningStateRecord>()

    // 当前状态
    private var currentSessionStart: Long? = null
    private var currentFocusScore: Float = 1f
    private var distractionCount: Int = 0
    private var breakCount: Int = 0

    /**
     * 开始学习会话
     */
    fun startSession() {
        currentSessionStart = System.currentTimeMillis()
        currentFocusScore = 1f
        distractionCount = 0
        breakCount = 0
    }

    /**
     * 记录状态快照
     */
    fun recordSnapshot(
        energyLevel: Int,
        moodState: Int,
        focusScore: Float? = null,
        sessionMinutes: Int = 0,
        todayMinutes: Int = 0,
        completedTasks: Int = 0,
        environment: String? = null
    ): LearningStateSnapshot {
        val now = System.currentTimeMillis()

        // 计算专注分数
        val calculatedFocus = focusScore ?: calculateFocusScore(
            sessionMinutes = sessionMinutes,
            distractions = distractionCount,
            breakCount = breakCount
        )

        // 计算生产力
        val productivity = calculateProductivity(
            focusScore = calculatedFocus,
            energyLevel = energyLevel,
            moodState = moodState,
            sessionMinutes = sessionMinutes
        )

        // 确定专注状态
        val focusState = determineFocusState(calculatedFocus)

        val snapshot = LearningStateSnapshot(
            id = "snapshot_$now",
            userId = userId,
            timestamp = now,
            energyLevel = energyLevel,
            moodState = moodState,
            focusScore = calculatedFocus,
            focusState = focusState,
            productivity = productivity,
            sessionMinutes = sessionMinutes,
            todayMinutes = todayMinutes,
            completedTasks = completedTasks,
            distractions = distractionCount,
            screenTime = 0,
            breakCount = breakCount,
            environment = environment
        )

        snapshots.add(snapshot)
        return snapshot
    }

    /**
     * 记录干扰
     */
    fun recordDistraction() {
        distractionCount++
        currentFocusScore = (currentFocusScore * 0.9f).coerceIn(0f, 1f)
    }

    /**
     * 记录休息
     */
    fun recordBreak() {
        breakCount++
        // 休息后恢复一些专注力
        currentFocusScore = (currentFocusScore + 0.2f).coerceIn(0f, 1f)
    }

    /**
     * 结束学习会话
     */
    fun endSession(): LearningStateSnapshot? {
        val startTime = currentSessionStart ?: return null
        val sessionMinutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()

        val snapshot = recordSnapshot(
            energyLevel = EnergyLevel.MODERATE.value,
            moodState = MoodState.NEUTRAL.value,
            focusScore = currentFocusScore,
            sessionMinutes = sessionMinutes
        )

        currentSessionStart = null
        return snapshot
    }

    /**
     * 获取当前状态
     */
    fun getCurrentState(): LearningStateSnapshot? {
        return snapshots.lastOrNull()
    }

    /**
     * 获取今日状态记录
     */
    fun getTodayRecord(): LearningStateRecord {
        val today = getTodayDateString()
        return dailyRecords.getOrPut(today) {
            createDailyRecord(today)
        }
    }

    /**
     * 获取状态趋势
     */
    fun getTrend(metric: MetricType, days: Int = 7): StateTrend {
        val records = dailyRecords.values
            .sortedByDescending { it.date }
            .take(days)

        val values = when (metric) {
            MetricType.ENERGY -> records.map {
                TrendPoint(it.date.hashCode().toLong(), it.averageEnergy)
            }
            MetricType.MOOD -> records.map {
                TrendPoint(it.date.hashCode().toLong(), it.averageMood)
            }
            MetricType.FOCUS -> records.map {
                TrendPoint(it.date.hashCode().toLong(), it.averageFocus)
            }
            MetricType.PRODUCTIVITY -> records.map {
                TrendPoint(it.date.hashCode().toLong(), it.averageProductivity)
            }
            MetricType.STUDY_TIME -> records.map {
                TrendPoint(it.date.hashCode().toLong(), it.totalStudyMinutes.toFloat())
            }
        }

        val trend = analyzeTrend(values.map { it.value })
        val changePercent = calculateChangePercent(values.map { it.value })

        return StateTrend(
            metric = metric,
            values = values,
            trend = trend,
            changePercent = changePercent
        )
    }

    /**
     * 获取AI洞察
     */
    fun generateAIInsights(): AIInsights {
        val recentSnapshots = snapshots.takeLast(20)
        val todayRecord = getTodayRecord()

        // 分析精力模式
        val energyByHour = recentSnapshots.groupBy {
            java.util.Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(java.util.Calendar.HOUR_OF_DAY)
        }.mapValues { (_, snapshots) ->
            snapshots.map { it.energyLevel }.average().toFloat()
        }

        val peakEnergyHours = energyByHour.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        // 分析心情模式
        val averageMood = recentSnapshots.map { it.moodState }.average().toFloat()
        val moodTrend = getTrend(MetricType.MOOD).trend

        // 分析专注模式
        val averageFocus = recentSnapshots.map { it.focusScore }.average().toFloat()
        val focusTrend = getTrend(MetricType.FOCUS).trend

        // 生成建议
        val suggestions = generateSuggestions(
            energyByHour = energyByHour,
            averageMood = averageMood,
            averageFocus = averageFocus,
            peakEnergyHours = peakEnergyHours
        )

        return AIInsights(
            averageEnergy = todayRecord.averageEnergy,
            averageMood = averageMood,
            averageFocus = averageFocus,
            averageProductivity = todayRecord.averageProductivity,
            peakEnergyHours = peakEnergyHours,
            energyTrend = getTrend(MetricType.ENERGY).trend,
            moodTrend = moodTrend,
            focusTrend = focusTrend,
            suggestions = suggestions,
            motivationalMessage = generateMotivationalMessage(averageFocus, averageMood)
        )
    }

    // ==================== 私有方法 ====================

    private fun calculateFocusScore(
        sessionMinutes: Int,
        distractions: Int,
        breakCount: Int
    ): Float {
        // 基础分数
        var score = 1f

        // 干扰惩罚
        score -= distractions * 0.1f

        // 休息惩罚/奖励（适度休息有好处）
        score -= abs(breakCount - sessionMinutes / 45) * 0.05f

        // 时间衰减（长时间学习专注力下降）
        if (sessionMinutes > 60) {
            score -= (sessionMinutes - 60) * 0.002f
        }

        return score.coerceIn(0f, 1f)
    }

    private fun calculateProductivity(
        focusScore: Float,
        energyLevel: Int,
        moodState: Int,
        sessionMinutes: Int
    ): Float {
        // 生产力 = 专注度 × 精力 × 心情 × 时间效率
        val energyFactor = energyLevel / 6f
        val moodFactor = moodState / 5f

        // 时间效率：25-45分钟最理想
        val timeEfficiency = when {
            sessionMinutes < 15 -> 0.6f
            sessionMinutes < 25 -> 0.8f
            sessionMinutes <= 45 -> 1f
            sessionMinutes <= 60 -> 0.9f
            sessionMinutes <= 90 -> 0.8f
            else -> 0.7f
        }

        return (focusScore * energyFactor * moodFactor * timeEfficiency).coerceIn(0f, 1f)
    }

    private fun determineFocusState(focusScore: Float): String {
        return when {
            focusScore >= 0.9f -> FocusState.DEEP_FOCUS.displayName
            focusScore >= 0.7f -> FocusState.FOCUSED.displayName
            focusScore >= 0.5f -> FocusState.NORMAL.displayName
            focusScore >= 0.3f -> FocusState.DISTRACTED.displayName
            else -> FocusState.UNFOCUSED.displayName
        }
    }

    private fun createDailyRecord(date: String): LearningStateRecord {
        val daySnapshots = snapshots.filter {
            formatDate(it.timestamp) == date
        }

        if (daySnapshots.isEmpty()) {
            return LearningStateRecord(
                date = date,
                snapshots = emptyList(),
                averageEnergy = 0f,
                averageMood = 0f,
                averageFocus = 0f,
                averageProductivity = 0f,
                totalStudyMinutes = 0,
                peakHours = emptyList(),
                aiInsights = "暂无数据"
            )
        }

        val avgEnergy = daySnapshots.map { it.energyLevel }.average().toFloat()
        val avgMood = daySnapshots.map { it.moodState }.average().toFloat()
        val avgFocus = daySnapshots.map { it.focusScore }.average().toFloat()
        val avgProductivity = daySnapshots.map { it.productivity }.average().toFloat()
        val totalMinutes = daySnapshots.sumOf { it.sessionMinutes }

        // 找出最佳学习时段
        val hourProductivity = daySnapshots.groupBy {
            java.util.Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(java.util.Calendar.HOUR_OF_DAY)
        }.mapValues { (_, snapshots) ->
            snapshots.map { it.productivity }.average()
        }

        val peakHours = hourProductivity.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        return LearningStateRecord(
            date = date,
            snapshots = daySnapshots,
            averageEnergy = avgEnergy,
            averageMood = avgMood,
            averageFocus = avgFocus,
            averageProductivity = avgProductivity,
            totalStudyMinutes = totalMinutes,
            peakHours = peakHours,
            aiInsights = generateDailyInsights(avgEnergy, avgMood, avgFocus, avgProductivity)
        )
    }

    private fun generateDailyInsights(
        energy: Float,
        mood: Float,
        focus: Float,
        productivity: Float
    ): String {
        return buildString {
            append("今日学习状态：")

            when {
                productivity >= 0.8f -> append("非常高效！")
                productivity >= 0.6f -> append("效率不错。")
                productivity >= 0.4f -> append("效率一般。")
                else -> append("效率有待提升。")
            }

            append("\n\n精力：${(energy / 6 * 100).toInt()}%")
            append("\n心情：${(mood / 5 * 100).toInt()}%")
            append("\n专注：${(focus * 100).toInt()}%")
        }
    }

    private fun generateSuggestions(
        energyByHour: Map<Int, Float>,
        averageMood: Float,
        averageFocus: Float,
        peakEnergyHours: List<Int>
    ): List<String> {
        val suggestions = mutableListOf<String>()

        // 精力建议
        if (peakEnergyHours.isNotEmpty()) {
            val hourStr = peakEnergyHours.joinToString("点、") { "$it" }
            suggestions.add("建议在${hourStr}点安排重要学习任务")
        }

        // 心情建议
        if (averageMood < 3f) {
            suggestions.add("心情不太好的时候，可以尝试轻松的学习任务")
        }

        // 专注建议
        if (averageFocus < 0.5f) {
            suggestions.add("专注力不足，建议减少干扰源，使用番茄钟")
        } else if (averageFocus > 0.8f) {
            suggestions.add("专注力很好！保持这个状态")
        }

        return suggestions
    }

    private fun generateMotivationalMessage(focus: Float, mood: Float): String {
        return when {
            focus > 0.8f && mood > 4f -> "状态极佳！趁热打铁，冲！"
            focus > 0.6f -> "专注力不错，继续保持！"
            mood < 3f -> "心情不太好？休息一下，明天又是新的一天。"
            focus < 0.4f -> "有点分心？试试5分钟冥想，重新聚焦。"
            else -> "稳扎稳打，每一步都是进步！"
        }
    }

    private fun analyzeTrend(values: List<Float>): TrendDirection {
        if (values.size < 2) return TrendDirection.STABLE

        val changes = values.zipWithNext { a, b -> b - a }
        val avgChange = changes.average().toFloat()

        return when {
            avgChange > 0.1f -> TrendDirection.UP
            avgChange < -0.1f -> TrendDirection.DOWN
            changes.any { abs(it) > 0.2f } -> TrendDirection.FLUCTUATING
            else -> TrendDirection.STABLE
        }
    }

    private fun calculateChangePercent(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val first = values.first()
        val last = values.last()
        if (first == 0f) return 0f
        return ((last - first) / first) * 100
    }

    private fun formatDate(timestamp: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        return String.format(
            "%04d-%02d-%02d",
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private fun getTodayDateString(): String {
        return formatDate(System.currentTimeMillis())
    }
}

/**
 * AI洞察结果
 */
data class AIInsights(
    val averageEnergy: Float,
    val averageMood: Float,
    val averageFocus: Float,
    val averageProductivity: Float,
    val peakEnergyHours: List<Int>,
    val energyTrend: TrendDirection,
    val moodTrend: TrendDirection,
    val focusTrend: TrendDirection,
    val suggestions: List<String>,
    val motivationalMessage: String
)

// ==================== 学习风格分析 ====================

/**
 * 学习风格分析器
 */
object LearningStyleAnalyzer {

    /**
     * 分析学习风格
     */
    fun analyze(snapshots: List<LearningStateSnapshot>): LearningStyleProfile {
        if (snapshots.isEmpty()) {
            return LearningStyleProfile.default()
        }

        // 分析最佳学习时段
        val productivityByHour = snapshots.groupBy {
            java.util.Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(java.util.Calendar.HOUR_OF_DAY)
        }.mapValues { (_, snaps) ->
            snaps.map { it.productivity }.average().toFloat()
        }

        val peakHours = productivityByHour.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { it.key }

        // 分析最佳学习时长
        val avgSessionLength = snapshots.map { it.sessionMinutes }
            .filter { it > 0 }
            .average()
            .toInt()
            .coerceIn(15, 60)

        // 分析专注模式
        val avgFocus = snapshots.map { it.focusScore }.average().toFloat()

        // 分析偏好
        val prefersLongSessions = avgSessionLength > 40
        val prefersDeepWork = avgFocus > 0.7f

        return LearningStyleProfile(
            peakHours = peakHours,
            optimalSessionLength = avgSessionLength,
            averageFocus = avgFocus,
            prefersLongSessions = prefersLongSessions,
            prefersDeepWork = prefersDeepWork,
            styleType = determineStyleType(peakHours, prefersLongSessions, prefersDeepWork),
            recommendations = generateRecommendations(peakHours, avgSessionLength, avgFocus)
        )
    }

    private fun determineStyleType(
        peakHours: List<Int>,
        prefersLongSessions: Boolean,
        prefersDeepWork: Boolean
    ): StyleType {
        val isMorningPerson = peakHours.all { it in 6..12 }
        val isNightPerson = peakHours.all { it in 20..24 }

        return when {
            isMorningPerson && prefersDeepWork -> StyleType.MORNING_SCHOLAR
            isNightPerson && prefersDeepWork -> StyleType.NIGHT_OWL
            prefersLongSessions && prefersDeepWork -> StyleType.DEEP_DIVER
            !prefersLongSessions -> StyleType.SPRINT_RUNNER
            else -> StyleType.BALANCED_LEARNER
        }
    }

    private fun generateRecommendations(
        peakHours: List<Int>,
        optimalSessionLength: Int,
        avgFocus: Float
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (peakHours.isNotEmpty()) {
            val hourStr = peakHours.take(2).joinToString("点、") { "$it" }
            recommendations.add("最佳学习时段：${hourStr}点")
        }

        recommendations.add("建议每次学习${optimalSessionLength}分钟")

        if (avgFocus < 0.6f) {
            recommendations.add("建议使用番茄钟提升专注力")
        }

        return recommendations
    }
}

/**
 * 学习风格画像
 */
data class LearningStyleProfile(
    val peakHours: List<Int>,
    val optimalSessionLength: Int,
    val averageFocus: Float,
    val prefersLongSessions: Boolean,
    val prefersDeepWork: Boolean,
    val styleType: StyleType,
    val recommendations: List<String>
) {
    companion object {
        fun default() = LearningStyleProfile(
            peakHours = listOf(9, 10, 14, 15),
            optimalSessionLength = 25,
            averageFocus = 0.5f,
            prefersLongSessions = false,
            prefersDeepWork = false,
            styleType = StyleType.BALANCED_LEARNER,
            recommendations = listOf("多学习几次以获取个性化建议")
        )
    }
}

/**
 * 学习风格类型
 */
enum class StyleType(val displayName: String, val description: String) {
    MORNING_SCHOLAR("晨型学者", "早起学习效率最高，适合安排重要任务"),
    NIGHT_OWL("夜猫学霸", "晚上学习效率更高，可以适当调整作息"),
    DEEP_DIVER("深度潜入者", "喜欢长时间沉浸学习，适合研究型任务"),
    SPRINT_RUNNER("短跑选手", "适合短时高效学习，建议使用番茄钟"),
    BALANCED_LEARNER("均衡学习者", "适应性强，可以灵活安排学习时间")
}