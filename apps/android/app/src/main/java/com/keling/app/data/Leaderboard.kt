package com.keling.app.data

/**
 * =========================
 * 排行榜系统
 * =========================
 *
 * 多维度学习排行榜
 * - 学习时长榜
 * - 任务完成榜
 * - 连续签到榜
 * - 知识掌握榜
 * - 结晶收集榜
 * - 成就解锁榜
 */

import kotlinx.serialization.Serializable

// ==================== 排行榜模型 ====================

/**
 * 排行榜类型
 */
enum class LeaderboardType(val displayName: String) {
    DAILY("今日榜"),
    WEEKLY("周榜"),
    MONTHLY("月榜"),
    ALL_TIME("总榜")
}

/**
 * 排行榜分类
 */
enum class LeaderboardCategory(val displayName: String, val icon: String) {
    STUDY_TIME("学习时长", "⏱️"),
    TASKS("任务完成", "✅"),
    STREAK("连续签到", "🔥"),
    MASTERY("知识掌握", "📚"),
    CRYSTALS("结晶收集", "💎"),
    ACHIEVEMENTS("成就解锁", "🏆")
}

/**
 * 排行榜条目
 */
@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val avatar: String? = null,
    val score: Long,                         // 分数/时长/数量
    val level: Int,
    val streakDays: Int,
    val title: String? = null,               // 称号
    val isCurrentUser: Boolean = false,
    val change: Int = 0                      // 排名变化（正数上升，负数下降）
)

/**
 * 排行榜
 */
@Serializable
data class Leaderboard(
    val type: LeaderboardType,
    val category: LeaderboardCategory,
    val entries: List<LeaderboardEntry>,
    val totalParticipants: Int,
    val updatedAt: Long = System.currentTimeMillis(),
    val resetAt: Long? = null                // 下次重置时间
)

/**
 * 用户排名信息
 */
@Serializable
data class UserRankInfo(
    val userId: String,
    val category: LeaderboardCategory,
    val type: LeaderboardType,
    val rank: Int,
    val score: Long,
    val percentile: Float,                   // 百分位（前x%）
    val topPercentage: Float,                // 前%多少
    val change: Int,                         // 排名变化
    val history: List<RankHistoryEntry>
)

/**
 * 排名历史条目
 */
@Serializable
data class RankHistoryEntry(
    val date: String,                        // "2024-01-15"
    val rank: Int,
    val score: Long
)

// ==================== 排行榜管理器 ====================

/**
 * 排行榜管理器
 */
class LeaderboardManager {
    private val leaderboards = mutableMapOf<String, Leaderboard>()
    private val userRankHistory = mutableMapOf<String, MutableList<RankHistoryEntry>>()

    // 模拟用户数据（实际应从数据库或服务器获取）
    private val mockUsers = listOf(
        Triple("user1", "学霸小王", 15),
        Triple("user2", "学习达人", 12),
        Triple("user3", "知识探索者", 10),
        Triple("user4", "勤奋学子", 8),
        Triple("user5", "晨曦读者", 7),
        Triple("user6", "夜猫学霸", 6),
        Triple("user7", "图书馆常客", 5),
        Triple("user8", "笔记达人", 4),
        Triple("user9", "知识收集者", 3),
        Triple("user10", "新手上路", 1)
    )

    /**
     * 获取排行榜
     */
    fun getLeaderboard(
        type: LeaderboardType,
        category: LeaderboardCategory,
        currentUserId: String? = null
    ): Leaderboard {
        val key = "${type.name}_${category.name}"

        // 如果已有缓存且未过期，直接返回
        val cached = leaderboards[key]
        if (cached != null && !isExpired(cached)) {
            return cached
        }

        // 生成新的排行榜
        val leaderboard = generateLeaderboard(type, category, currentUserId)
        leaderboards[key] = leaderboard

        return leaderboard
    }

    /**
     * 检查是否过期
     */
    private fun isExpired(leaderboard: Leaderboard): Boolean {
        val now = System.currentTimeMillis()
        return leaderboard.resetAt != null && now >= leaderboard.resetAt
    }

    /**
     * 生成排行榜
     */
    private fun generateLeaderboard(
        type: LeaderboardType,
        category: LeaderboardCategory,
        currentUserId: String?
    ): Leaderboard {
        val now = System.currentTimeMillis()

        // 根据类型计算时间范围
        val (startTime, resetTime) = when (type) {
            LeaderboardType.DAILY -> {
                val start = getStartOfDay(now)
                val reset = start + 24 * 60 * 60 * 1000
                start to reset
            }
            LeaderboardType.WEEKLY -> {
                val start = getStartOfWeek(now)
                val reset = start + 7 * 24 * 60 * 60 * 1000
                start to reset
            }
            LeaderboardType.MONTHLY -> {
                val start = getStartOfMonth(now)
                val reset = getStartOfNextMonth(now)
                start to reset
            }
            LeaderboardType.ALL_TIME -> {
                0L to null
            }
        }

        // 生成条目（模拟数据）
        val entries = generateEntries(category, currentUserId, type)

        return Leaderboard(
            type = type,
            category = category,
            entries = entries,
            totalParticipants = entries.size,
            updatedAt = now,
            resetAt = resetTime
        )
    }

    /**
     * 生成排行榜条目
     */
    private fun generateEntries(
        category: LeaderboardCategory,
        currentUserId: String?,
        type: LeaderboardType
    ): List<LeaderboardEntry> {
        // 模拟数据生成
        val scores = when (category) {
            LeaderboardCategory.STUDY_TIME -> generateStudyTimeScores(type)
            LeaderboardCategory.TASKS -> generateTaskScores(type)
            LeaderboardCategory.STREAK -> generateStreakScores()
            LeaderboardCategory.MASTERY -> generateMasteryScores()
            LeaderboardCategory.CRYSTALS -> generateCrystalScores(type)
            LeaderboardCategory.ACHIEVEMENTS -> generateAchievementScores()
        }

        return mockUsers.mapIndexed { index, (userId, userName, level) ->
            val score = scores.getOrElse(index) { 0L }
            LeaderboardEntry(
                rank = index + 1,
                userId = userId,
                userName = userName,
                score = score,
                level = level,
                streakDays = (Math.random() * 30).toInt(),
                title = if (level >= 10) "学习大师" else null,
                isCurrentUser = userId == currentUserId,
                change = (-5..5).random()
            )
        }.sortedByDescending { it.score }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    private fun generateStudyTimeScores(type: LeaderboardType): List<Long> {
        val base = when (type) {
            LeaderboardType.DAILY -> 30..180
            LeaderboardType.WEEKLY -> 200..1200
            LeaderboardType.MONTHLY -> 1000..5000
            LeaderboardType.ALL_TIME -> 10000..100000
        }
        return base.toList().shuffled().take(10).map { it.toLong() }
    }

    private fun generateTaskScores(type: LeaderboardType): List<Long> {
        val base = when (type) {
            LeaderboardType.DAILY -> 1..10
            LeaderboardType.WEEKLY -> 5..50
            LeaderboardType.MONTHLY -> 20..200
            LeaderboardType.ALL_TIME -> 100..2000
        }
        return base.toList().shuffled().take(10).map { it.toLong() }
    }

    private fun generateStreakScores(): List<Long> {
        return (1..60).toList().shuffled().take(10).map { it.toLong() }
    }

    private fun generateMasteryScores(): List<Long> {
        return (30..98).toList().shuffled().take(10).map { it.toLong() }
    }

    private fun generateCrystalScores(type: LeaderboardType): List<Long> {
        val base = when (type) {
            LeaderboardType.DAILY -> 10..100
            LeaderboardType.WEEKLY -> 50..500
            LeaderboardType.MONTHLY -> 200..2000
            LeaderboardType.ALL_TIME -> 1000..20000
        }
        return base.toList().shuffled().take(10).map { it.toLong() }
    }

    private fun generateAchievementScores(): List<Long> {
        return (1..30).toList().shuffled().take(10).map { it.toLong() }
    }

    /**
     * 获取用户排名信息
     */
    fun getUserRankInfo(
        userId: String,
        category: LeaderboardCategory,
        type: LeaderboardType
    ): UserRankInfo {
        val leaderboard = getLeaderboard(type, category, userId)
        val entry = leaderboard.entries.find { it.userId == userId }

        if (entry != null) {
            val percentile = 1f - (entry.rank.toFloat() / leaderboard.totalParticipants)
            val topPercentage = (entry.rank.toFloat() / leaderboard.totalParticipants) * 100

            return UserRankInfo(
                userId = userId,
                category = category,
                type = type,
                rank = entry.rank,
                score = entry.score,
                percentile = percentile,
                topPercentage = topPercentage,
                change = entry.change,
                history = userRankHistory[userId] ?: emptyList()
            )
        }

        // 用户不在排行榜中
        return UserRankInfo(
            userId = userId,
            category = category,
            type = type,
            rank = leaderboard.totalParticipants + 1,
            score = 0,
            percentile = 0f,
            topPercentage = 100f,
            change = 0,
            history = emptyList()
        )
    }

    /**
     * 更新用户分数（用于本地测试）
     */
    fun updateUserScore(
        userId: String,
        category: LeaderboardCategory,
        score: Long
    ) {
        // 实际应该发送到服务器
        // 这里仅清除缓存以便下次重新生成
        leaderboards.keys.removeAll { it.contains(category.name) }
    }

    /**
     * 获取排行榜摘要
     */
    fun getLeaderboardSummary(currentUserId: String): LeaderboardSummary {
        return LeaderboardSummary(
            dailyStudyTime = getUserRankInfo(currentUserId, LeaderboardCategory.STUDY_TIME, LeaderboardType.DAILY),
            weeklyStudyTime = getUserRankInfo(currentUserId, LeaderboardCategory.STUDY_TIME, LeaderboardType.WEEKLY),
            streakRank = getUserRankInfo(currentUserId, LeaderboardCategory.STREAK, LeaderboardType.ALL_TIME),
            totalAchievements = getUserRankInfo(currentUserId, LeaderboardCategory.ACHIEVEMENTS, LeaderboardType.ALL_TIME)
        )
    }

    // ==================== 时间计算工具 ====================

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfNextMonth(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.add(java.util.Calendar.MONTH, 1)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

/**
 * 排行榜摘要
 */
data class LeaderboardSummary(
    val dailyStudyTime: UserRankInfo,
    val weeklyStudyTime: UserRankInfo,
    val streakRank: UserRankInfo,
    val totalAchievements: UserRankInfo
)

// ==================== 排行榜奖励 ====================

/**
 * 排行榜奖励
 */
data class LeaderboardReward(
    val rank: Int,
    val energy: Int,
    val crystals: Int,
    val exp: Int,
    val specialReward: String? = null
)

/**
 * 排行榜奖励配置
 */
object LeaderboardRewards {

    val DAILY_REWARDS = listOf(
        LeaderboardReward(1, 100, 50, 200, "日冠军徽章"),
        LeaderboardReward(2, 70, 35, 150, null),
        LeaderboardReward(3, 50, 25, 100, null),
        LeaderboardReward(4, 40, 20, 80, null),
        LeaderboardReward(5, 30, 15, 60, null),
        LeaderboardReward(6, 25, 12, 50, null),
        LeaderboardReward(7, 20, 10, 40, null),
        LeaderboardReward(8, 15, 8, 30, null),
        LeaderboardReward(9, 10, 5, 20, null),
        LeaderboardReward(10, 5, 3, 10, null)
    )

    val WEEKLY_REWARDS = listOf(
        LeaderboardReward(1, 300, 150, 500, "周冠军称号"),
        LeaderboardReward(2, 200, 100, 400, null),
        LeaderboardReward(3, 150, 75, 300, null),
        LeaderboardReward(4, 100, 50, 200, null),
        LeaderboardReward(5, 80, 40, 160, null),
        LeaderboardReward(6, 60, 30, 120, null),
        LeaderboardReward(7, 50, 25, 100, null),
        LeaderboardReward(8, 40, 20, 80, null),
        LeaderboardReward(9, 30, 15, 60, null),
        LeaderboardReward(10, 20, 10, 40, null)
    )

    val MONTHLY_REWARDS = listOf(
        LeaderboardReward(1, 1000, 500, 1500, "月度之星称号"),
        LeaderboardReward(2, 600, 300, 1000, null),
        LeaderboardReward(3, 400, 200, 700, null),
        LeaderboardReward(4, 300, 150, 500, null),
        LeaderboardReward(5, 200, 100, 400, null)
    )

    fun getReward(rank: Int, type: LeaderboardType): LeaderboardReward? {
        val rewards = when (type) {
            LeaderboardType.DAILY -> DAILY_REWARDS
            LeaderboardType.WEEKLY -> WEEKLY_REWARDS
            LeaderboardType.MONTHLY -> MONTHLY_REWARDS
            else -> return null
        }
        return rewards.find { it.rank == rank }
    }
}