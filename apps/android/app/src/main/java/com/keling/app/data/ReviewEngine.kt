package com.keling.app.data

/**
 * =========================
 * 遗忘曲线复习引擎
 * =========================
 *
 * 基于艾宾浩斯遗忘曲线的智能复习系统
 * - 计算最佳复习时间
 * - 预测记忆保持率
 * - 动态调整复习间隔
 * - 生成复习提醒
 */

import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

// ==================== 复习项模型 ====================

/**
 * 复习项 - 需要复习的知识点
 */
@Serializable
data class ReviewItem(
    val id: String,
    val nodeId: String,
    val courseId: String,
    val nodeName: String,
    val courseName: String,
    val lastReviewAt: Long,
    val nextReviewAt: Long,
    val reviewCount: Int = 0,
    val easeFactor: Float = 2.5f,           // 记忆难易度因子 (SM-2算法)
    val interval: Int = 1,                   // 复习间隔(天)
    val retentionRate: Float = 1f,           // 预测记忆保持率
    val status: ReviewStatus = ReviewStatus.NEW,
    val difficulty: Int = 3,                 // 1-5 难度
    val importance: Int = 3,                 // 1-5 重要性
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 复习状态
 */
enum class ReviewStatus {
    NEW,         // 新知识点，未学习
    LEARNING,    // 学习中
    REVIEW,      // 复习中
    MASTERED,    // 已掌握
    LAPSED       // 遗忘，需要重新学习
}

/**
 * 复习记录
 */
@Serializable
data class ReviewRecord(
    val id: String,
    val itemId: String,
    val userId: String,
    val reviewedAt: Long,
    val quality: Int,                        // 回忆质量 0-5 (SM-2算法)
    val responseTime: Int,                   // 回答时间(秒)
    val wasCorrect: Boolean,
    val retentionBefore: Float,              // 复习前预测保持率
    val retentionAfter: Float,               // 复习后预测保持率
    val aiFeedback: String? = null
)

/**
 * 复习提醒
 */
@Serializable
data class ReviewReminder(
    val id: String,
    val itemId: String,
    val reminderTime: Long,
    val urgency: ReviewUrgency,
    val aiMessage: String,
    val isSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 复习紧急程度
 */
enum class ReviewUrgency {
    OVERDUE,      // 已过期
    URGENT,       // 今天必须复习
    TODAY,        // 今天复习
    TOMORROW,     // 明天复习
    THIS_WEEK     // 本周复习
}

// ==================== 遗忘曲线算法 ====================

/**
 * 遗忘曲线算法引擎
 * 结合艾宾浩斯曲线和SM-2算法
 */
object ForgettingCurveEngine {

    // 艾宾浩斯标准遗忘曲线参数
    private const val FORGETTING_RATE = 0.9  // 90%遗忘率系数

    // 标准复习间隔（艾宾浩斯）
    private val STANDARD_INTERVALS = listOf(
        0,      // 立即
        1,      // 1天后
        2,      // 2天后
        4,      // 4天后
        7,      // 1周后
        15,     // 15天后
        30,     // 1月后
        60,     // 2月后
        120,    // 4月后
        240     // 8月后
    )

    /**
     * 计算当前记忆保持率
     * @param daysSinceReview 距离上次复习的天数
     * @param difficulty 难度 1-5
     * @param reviewCount 复习次数
     * @return 保持率 0-1
     */
    fun calculateRetention(
        daysSinceReview: Float,
        difficulty: Int = 3,
        reviewCount: Int = 0
    ): Float {
        if (daysSinceReview <= 0) return 1f

        // 基础遗忘曲线: R = e^(-t/S)
        // S = 记忆强度，与难度和复习次数相关
        val baseStrength = when (difficulty) {
            1 -> 10f   // 很简单
            2 -> 7f
            3 -> 5f    // 中等
            4 -> 3f
            5 -> 2f    // 很难
            else -> 5f
        }

        // 复习次数加成
        val reviewBonus = 1f + reviewCount * 0.3f
        val strength = baseStrength * reviewBonus

        // 计算保持率
        val retention = exp(-daysSinceReview / strength)

        return retention.coerceIn(0f, 1f)
    }

    /**
     * 计算下次复习时间
     * 使用SM-2算法变体
     *
     * @param item 当前复习项
     * @param quality 回忆质量 0-5
     * @return 新的复习项
     */
    fun scheduleNextReview(
        item: ReviewItem,
        quality: Int
    ): ReviewItem {
        val now = System.currentTimeMillis()
        val daysToHours = 24 * 60 * 60 * 1000L

        // SM-2算法核心
        // 1. 更新难度因子
        val newEaseFactor = calculateNewEaseFactor(item.easeFactor, quality)

        // 2. 计算新间隔
        val newInterval = when {
            quality < 3 -> {
                // 回忆失败，重置间隔
                1
            }
            item.reviewCount == 0 -> {
                // 第一次复习
                1
            }
            item.reviewCount == 1 -> {
                // 第二次复习
                6
            }
            else -> {
                // 后续复习
                (item.interval * newEaseFactor).toInt().coerceIn(1, 365)
            }
        }

        // 3. 根据难度调整
        val adjustedInterval = when (item.difficulty) {
            5 -> (newInterval * 0.7).toInt()  // 很难，缩短间隔
            4 -> (newInterval * 0.85).toInt()
            2 -> (newInterval * 1.15).toInt()
            1 -> (newInterval * 1.3).toInt()  // 很简单，延长间隔
            else -> newInterval
        }

        // 4. 计算下次复习时间
        val nextReviewAt = now + adjustedInterval * daysToHours

        // 5. 计算新的保持率预测
        val newRetention = calculateRetention(0f, item.difficulty, item.reviewCount + 1)

        // 6. 确定新状态
        val newStatus = when {
            quality < 3 -> ReviewStatus.LAPSED
            adjustedInterval >= 30 -> ReviewStatus.MASTERED
            else -> ReviewStatus.REVIEW
        }

        return item.copy(
            lastReviewAt = now,
            nextReviewAt = nextReviewAt,
            reviewCount = item.reviewCount + 1,
            easeFactor = newEaseFactor,
            interval = adjustedInterval,
            retentionRate = newRetention,
            status = newStatus
        )
    }

    /**
     * 计算新的难度因子
     * SM-2算法公式: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
     */
    private fun calculateNewEaseFactor(currentEF: Float, quality: Int): Float {
        val q = quality.coerceIn(0, 5)
        val newEF = currentEF + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        return newEF.coerceIn(1.3f, 3.0f)
    }

    /**
     * 获取复习紧急程度
     */
    fun getUrgency(nextReviewAt: Long): ReviewUrgency {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return when {
            nextReviewAt < now -> ReviewUrgency.OVERDUE
            nextReviewAt < now + 6 * 60 * 60 * 1000 -> ReviewUrgency.URGENT    // 6小时内
            nextReviewAt < now + dayInMillis -> ReviewUrgency.TODAY
            nextReviewAt < now + 2 * dayInMillis -> ReviewUrgency.TOMORROW
            else -> ReviewUrgency.THIS_WEEK
        }
    }

    /**
     * 生成AI提醒消息
     */
    fun generateReminderMessage(
        item: ReviewItem,
        urgency: ReviewUrgency,
        retention: Float
    ): String {
        val retentionPercent = (retention * 100).toInt()

        return when (urgency) {
            ReviewUrgency.OVERDUE -> {
                "⚠️ 「${item.nodeName}」已经超过复习时间了！\n" +
                "如果不及时复习，记忆保持率可能已降至${retentionPercent}%以下。\n" +
                "现在复习可以挽回记忆，获得双倍结晶奖励！"
            }
            ReviewUrgency.URGENT -> {
                "🔥 「${item.nodeName}」需要紧急复习！\n" +
                "当前预测记忆保持率: ${retentionPercent}%\n" +
                "趁现在还记得，赶紧巩固一下吧！"
            }
            ReviewUrgency.TODAY -> {
                "📚 今天该复习「${item.nodeName}」了\n" +
                "保持率预计${retentionPercent}%，现在复习效果最佳！"
            }
            ReviewUrgency.TOMORROW -> {
                "📅 明天记得复习「${item.nodeName}」哦\n" +
                "合理安排时间，保持学习节奏~"
            }
            ReviewUrgency.THIS_WEEK -> {
                "📝 本周计划复习「${item.nodeName}」\n" +
                "当前掌握度不错，继续保持！"
            }
        }
    }
}

// ==================== 复习调度器 ====================

/**
 * 复习调度器
 * 管理所有复习项的调度
 */
class ReviewScheduler {
    private val reviewItems = mutableListOf<ReviewItem>()
    private val reviewHistory = mutableListOf<ReviewRecord>()

    /**
     * 添加新的复习项
     */
    fun addReviewItem(
        nodeId: String,
        courseId: String,
        nodeName: String,
        courseName: String,
        difficulty: Int = 3,
        importance: Int = 3
    ): ReviewItem {
        val now = System.currentTimeMillis()
        val item = ReviewItem(
            id = "review_${System.currentTimeMillis()}_${nodeId}",
            nodeId = nodeId,
            courseId = courseId,
            nodeName = nodeName,
            courseName = courseName,
            lastReviewAt = now,
            nextReviewAt = now,  // 新知识点立即复习
            difficulty = difficulty,
            importance = importance,
            status = ReviewStatus.NEW
        )
        reviewItems.add(item)
        return item
    }

    /**
     * 获取今日需要复习的项目
     */
    fun getTodayReviews(): List<ReviewItem> {
        val now = System.currentTimeMillis()
        val endOfDay = now + 24 * 60 * 60 * 1000

        return reviewItems.filter {
            it.nextReviewAt <= endOfDay && it.status != ReviewStatus.MASTERED
        }.sortedBy { it.nextReviewAt }
    }

    /**
     * 获取紧急复习项目
     */
    fun getUrgentReviews(): List<ReviewItem> {
        val now = System.currentTimeMillis()
        val sixHoursLater = now + 6 * 60 * 60 * 1000

        return reviewItems.filter {
            it.nextReviewAt <= sixHoursLater &&
            it.status != ReviewStatus.MASTERED &&
            it.status != ReviewStatus.NEW
        }.sortedBy { it.retentionRate }  // 按保持率排序，低的优先
    }

    /**
     * 获取过期的复习项目
     */
    fun getOverdueReviews(): List<ReviewItem> {
        val now = System.currentTimeMillis()

        return reviewItems.filter {
            it.nextReviewAt < now && it.status != ReviewStatus.MASTERED
        }.sortedBy { it.retentionRate }
    }

    /**
     * 完成复习
     */
    fun completeReview(
        itemId: String,
        quality: Int,
        responseTime: Int = 0,
        aiFeedback: String? = null
    ): ReviewItem? {
        val item = reviewItems.find { it.id == itemId } ?: return null
        val retentionBefore = ForgettingCurveEngine.calculateRetention(
            (System.currentTimeMillis() - item.lastReviewAt).toFloat() / (24 * 60 * 60 * 1000),
            item.difficulty,
            item.reviewCount
        )

        // 调度下次复习
        val updatedItem = ForgettingCurveEngine.scheduleNextReview(item, quality)
        val retentionAfter = updatedItem.retentionRate

        // 记录复习历史
        val record = ReviewRecord(
            id = "record_${System.currentTimeMillis()}",
            itemId = itemId,
            userId = "", // 将由调用者填充
            reviewedAt = System.currentTimeMillis(),
            quality = quality,
            responseTime = responseTime,
            wasCorrect = quality >= 3,
            retentionBefore = retentionBefore,
            retentionAfter = retentionAfter,
            aiFeedback = aiFeedback
        )
        reviewHistory.add(record)

        // 更新列表中的项目
        val index = reviewItems.indexOf(item)
        if (index >= 0) {
            reviewItems[index] = updatedItem
        }

        return updatedItem
    }

    /**
     * 获取复习统计
     */
    fun getReviewStats(): ReviewStats {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return ReviewStats(
            totalItems = reviewItems.size,
            newItems = reviewItems.count { it.status == ReviewStatus.NEW },
            learningItems = reviewItems.count { it.status == ReviewStatus.LEARNING },
            masteredItems = reviewItems.count { it.status == ReviewStatus.MASTERED },
            overdueItems = reviewItems.count { it.nextReviewAt < now },
            todayItems = reviewItems.count {
                it.nextReviewAt in now..(now + dayInMillis)
            },
            averageRetention = if (reviewItems.isNotEmpty()) {
                reviewItems.map { it.retentionRate }.average().toFloat()
            } else 1f,
            totalReviews = reviewHistory.size,
            correctRate = if (reviewHistory.isNotEmpty()) {
                reviewHistory.count { it.wasCorrect }.toFloat() / reviewHistory.size
            } else 0f
        )
    }

    /**
     * 生成复习提醒
     */
    fun generateReminders(): List<ReviewReminder> {
        val reminders = mutableListOf<ReviewReminder>()
        val now = System.currentTimeMillis()

        for (item in reviewItems) {
            val urgency = ForgettingCurveEngine.getUrgency(item.nextReviewAt)
            if (urgency == ReviewUrgency.THIS_WEEK) continue  // 本周的暂时不提醒

            val retention = ForgettingCurveEngine.calculateRetention(
                (now - item.lastReviewAt).toFloat() / (24 * 60 * 60 * 1000),
                item.difficulty,
                item.reviewCount
            )

            reminders.add(
                ReviewReminder(
                    id = "reminder_${item.id}",
                    itemId = item.id,
                    reminderTime = item.nextReviewAt,
                    urgency = urgency,
                    aiMessage = ForgettingCurveEngine.generateReminderMessage(item, urgency, retention)
                )
            )
        }

        return reminders.sortedBy { it.reminderTime }
    }

    /**
     * 清除所有数据
     */
    fun clearAll() {
        reviewItems.clear()
        reviewHistory.clear()
    }
}

/**
 * 复习统计
 */
data class ReviewStats(
    val totalItems: Int,
    val newItems: Int,
    val learningItems: Int,
    val masteredItems: Int,
    val overdueItems: Int,
    val todayItems: Int,
    val averageRetention: Float,
    val totalReviews: Int,
    val correctRate: Float
)

// ==================== 复习计划生成器 ====================

/**
 * 复习计划生成器
 * 根据用户情况生成每日复习计划
 */
object ReviewPlanGenerator {

    /**
     * 生成今日复习计划
     */
    fun generateDailyPlan(
        scheduler: ReviewScheduler,
        availableMinutes: Int = 60
    ): DailyReviewPlan {
        val now = System.currentTimeMillis()

        // 获取需要复习的项目
        val overdue = scheduler.getOverdueReviews()
        val urgent = scheduler.getUrgentReviews()
        val today = scheduler.getTodayReviews()

        // 合并去重
        val allItems = (overdue + urgent + today).distinctBy { it.id }

        // 按优先级排序
        val sortedItems = allItems.sortedWith(
            compareBy<ReviewItem> {
                // 过期最优先
                if (it.nextReviewAt < now) 0 else 1
            }.thenBy {
                // 保持率低的优先
                -it.retentionRate
            }.thenBy {
                // 重要性高的优先
                -it.importance
            }
        )

        // 估算每个项目需要的复习时间
        val itemsWithTime = sortedItems.map { item ->
            val estimatedMinutes = when (item.difficulty) {
                1 -> 5
                2 -> 8
                3 -> 12
                4 -> 18
                5 -> 25
                else -> 15
            }
            item to estimatedMinutes
        }

        // 选择能够完成的项目
        val selectedItems = mutableListOf<Pair<ReviewItem, Int>>()
        var totalMinutes = 0

        for ((item, time) in itemsWithTime) {
            if (totalMinutes + time <= availableMinutes) {
                selectedItems.add(item to time)
                totalMinutes += time
            }
        }

        return DailyReviewPlan(
            date = now,
            items = selectedItems.map { (item, time) ->
                ReviewPlanItem(
                    item = item,
                    estimatedMinutes = time,
                    urgency = ForgettingCurveEngine.getUrgency(item.nextReviewAt),
                    retention = ForgettingCurveEngine.calculateRetention(
                        (now - item.lastReviewAt).toFloat() / (24 * 60 * 60 * 1000),
                        item.difficulty,
                        item.reviewCount
                    )
                )
            },
            totalMinutes = totalMinutes,
            aiTips = generatePlanTips(selectedItems.map { it.first })
        )
    }

    /**
     * 生成计划提示
     */
    private fun generatePlanTips(items: List<ReviewItem>): String {
        if (items.isEmpty()) return "今天没有需要复习的内容，可以学习新知识！"

        val overdueCount = items.count {
            it.nextReviewAt < System.currentTimeMillis()
        }
        val avgRetention = items.map { it.retentionRate }.average()
        val difficultItems = items.filter { it.difficulty >= 4 }

        return buildString {
            if (overdueCount > 0) {
                append("有${overdueCount}个知识点已过期，建议优先复习。")
            }
            if (avgRetention < 0.5) {
                append("\n部分知识点记忆保持率较低，建议多花些时间巩固。")
            }
            if (difficultItems.isNotEmpty()) {
                append("\n「${difficultItems.take(3).joinToString("、") { it.nodeName }}」等知识点难度较高，建议专注理解。")
            }
            append("\n保持专注，每次复习都是巩固记忆的好机会！")
        }.trim()
    }
}

/**
 * 每日复习计划
 */
data class DailyReviewPlan(
    val date: Long,
    val items: List<ReviewPlanItem>,
    val totalMinutes: Int,
    val aiTips: String
)

/**
 * 复习计划项
 */
data class ReviewPlanItem(
    val item: ReviewItem,
    val estimatedMinutes: Int,
    val urgency: ReviewUrgency,
    val retention: Float
)

// ==================== 复习奖励计算 ====================

/**
 * 复习奖励计算器
 */
object ReviewRewardCalculator {

    /**
     * 计算复习奖励
     */
    fun calculateReward(
        item: ReviewItem,
        quality: Int,
        isOverdue: Boolean
    ): ReviewReward {
        // 基础奖励
        val baseEnergy = 10
        val baseCrystals = 5
        val baseExp = 15

        // 质量加成
        val qualityBonus = when (quality) {
            5 -> 1.5f   // 完美回忆
            4 -> 1.2f   // 轻微犹豫
            3 -> 1.0f   // 正常
            2 -> 0.7f   // 困难
            1 -> 0.5f   // 几乎忘记
            else -> 0.3f // 完全忘记
        }

        // 难度加成
        val difficultyBonus = 1f + (item.difficulty - 3) * 0.1f

        // 过期惩罚/奖励
        val overdueBonus = if (isOverdue) {
            1.3f  // 过期复习额外奖励（鼓励挽回）
        } else {
            1f
        }

        // 连续复习加成
        val streakBonus = 1f + minOf(item.reviewCount, 10) * 0.05f

        // 计算最终奖励
        val multiplier = qualityBonus * difficultyBonus * overdueBonus * streakBonus

        return ReviewReward(
            energy = (baseEnergy * multiplier).toInt(),
            crystals = (baseCrystals * multiplier).toInt(),
            exp = (baseExp * multiplier).toInt(),
            bonusReasons = buildList {
                if (quality == 5) add("完美回忆")
                if (item.difficulty >= 4) add("攻克难题")
                if (isOverdue) add("及时挽回")
                if (item.reviewCount >= 5) add("持续巩固")
            }
        )
    }
}

/**
 * 复习奖励
 */
data class ReviewReward(
    val energy: Int,
    val crystals: Int,
    val exp: Int,
    val bonusReasons: List<String> = emptyList()
)