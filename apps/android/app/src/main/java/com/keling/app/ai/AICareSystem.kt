package com.keling.app.ai

/**
 * =========================
 * AI主动关怀系统
 * =========================
 *
 * 智能化的用户关怀与提醒系统
 * - 早安问候
 * - 学习提醒
 * - 休息建议
 * - 晚间总结
 * - 考试倒计时
 * - 连续签到预警
 * - 情绪关怀
 */

import com.keling.app.data.*
import kotlinx.serialization.Serializable
import java.util.Calendar

// ==================== 关怀事件模型 ====================

/**
 * 关怀类型
 */
enum class CareType {
    MORNING_GREETING,          // 早安问候
    STUDY_REMINDER,            // 学习提醒
    BREAK_SUGGESTION,          // 休息建议
    NIGHT_SUMMARY,             // 晚间总结
    ACHIEVEMENT_CELEBRATION,   // 成就庆祝
    STREAK_WARNING,            // 连续签到预警
    EXAM_COUNTDOWN,            // 考试倒计时
    WEATHER_ALERT,             // 天气提醒
    HEALTH_CHECK,              // 健康关怀
    WEEKLY_REPORT,             // 周报告
    MOTIVATION,                // 激励话语
    EMOTIONAL_SUPPORT          // 情感支持
}

/**
 * 关怀事件
 */
@Serializable
data class CareEvent(
    val id: String,
    val type: CareType,
    val scheduledTime: Long,
    val title: String,
    val message: String,
    val actionText: String? = null,       // 行动按钮文字
    val actionData: String? = null,       // 行动数据(JSON)
    val priority: Int = 3,                // 1-5 优先级
    val isSent: Boolean = false,
    val isRead: Boolean = false,
    val userAction: UserAction? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 用户行动
 */
enum class UserAction {
    POSITIVE,    // 正面反馈
    NEGATIVE,    // 负面反馈
    DISMISSED,   // 忽略
    ACTIONED,    // 采取了行动
    SNOOZED      // 稍后提醒
}

/**
 * 关怀配置
 */
@Serializable
data class CareConfig(
    val userId: String,
    val morningGreetingEnabled: Boolean = true,
    val morningGreetingTime: Int = 8,     // 小时
    val studyReminderEnabled: Boolean = true,
    val studyReminderInterval: Int = 120, // 分钟
    val breakReminderEnabled: Boolean = true,
    val breakReminderInterval: Int = 45,  // 分钟
    val nightSummaryEnabled: Boolean = true,
    val nightSummaryTime: Int = 22,       // 小时
    val streakWarningEnabled: Boolean = true,
    val examCountdownEnabled: Boolean = true,
    val healthCheckEnabled: Boolean = true,
    val healthCheckInterval: Int = 90,    // 分钟
    val motivationalMessages: Boolean = true,
    val careLevel: CareLevel = CareLevel.ATTENTIVE
)

/**
 * 关怀级别
 */
enum class CareLevel {
    ATTENTIVE,    // 细心 - 更频繁的关怀
    NORMAL,       // 正常 - 标准关怀频率
    RELAXED,      // 放松 - 减少关怀频率
    MINIMAL       // 极简 - 仅重要提醒
}

// ==================== AI关怀消息生成器 ====================

/**
 * AI关怀消息生成器
 * 根据用户状态和上下文生成个性化关怀消息
 */
object AICareMessageGenerator {

    // ==================== 早安问候 ====================

    /**
     * 生成早安问候
     */
    fun generateMorningGreeting(
        user: User,
        todaySchedule: List<Pair<Course, ScheduleSlot>>,
        pendingTasks: List<Task>,
        streakDays: Int
    ): CareEvent {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val greeting = when {
            hour < 6 -> "夜猫子还没睡呀"
            hour < 9 -> "早安"
            hour < 12 -> "上午好"
            else -> "中午好"
        }

        val name = user.name.ifEmpty { "星际园丁" }

        val message = buildString {
            append("$greeting，$name！")

            // 连续签到鼓励
            if (streakDays > 0) {
                append("\n已连续学习${streakDays}天，")
                when (streakDays) {
                    in 1..3 -> append("继续保持！")
                    in 4..7 -> append("形成了好习惯！")
                    in 8..14 -> append("真是学习的榜样！")
                    in 15..30 -> append("毅力令人敬佩！")
                    else -> append("传奇般的坚持！")
                }
            }

            // 今日安排概览
            if (todaySchedule.isNotEmpty()) {
                append("\n\n今日有${todaySchedule.size}节课")
                val firstClass = todaySchedule.minByOrNull { (_, slot) ->
                    slot.startHour * 60 + slot.startMinute
                }
                if (firstClass != null) {
                    val (course, slot) = firstClass
                    append("，第一节是${course.name}(${slot.startHour}:${String.format("%02d", slot.startMinute)})")
                }
            }

            // 待办提醒
            if (pendingTasks.isNotEmpty()) {
                append("\n\n还有${pendingTasks.size}个任务待完成，")
                val urgentTasks = pendingTasks.filter { it.priority >= 4 }
                if (urgentTasks.isNotEmpty()) {
                    append("其中${urgentTasks.size}个比较紧急哦！")
                } else {
                    append("今天时间充裕，可以慢慢来~")
                }
            }

            // 激励语
            append("\n\n")
            append(getMotivationalQuote())
        }

        return CareEvent(
            id = "morning_${now}",
            type = CareType.MORNING_GREETING,
            scheduledTime = now,
            title = "早安问候",
            message = message,
            actionText = if (pendingTasks.isNotEmpty()) "开始今日计划" else null,
            actionData = if (pendingTasks.isNotEmpty()) """{"action":"GO_TO","params":{"screen":"tasks"}}""" else null,
            priority = 3
        )
    }

    // ==================== 学习提醒 ====================

    /**
     * 生成学习提醒
     */
    fun generateStudyReminder(
        pendingTasks: List<Task>,
        currentStudyMinutes: Int,
        dailyGoal: Int,
        weakCourses: List<Course>
    ): CareEvent {
        val now = System.currentTimeMillis()

        val message = buildString {
            append("学习时间到啦！")

            // 目标进度
            val progress = (currentStudyMinutes.toFloat() / dailyGoal * 100).toInt()
            append("\n今日学习${currentStudyMinutes}分钟，目标完成${progress}%")

            when {
                progress < 30 -> {
                    append("\n今天还没开始学习呢，要不要现在开始？")
                }
                progress < 70 -> {
                    append("\n进度不错，继续加油！")
                }
                progress < 100 -> {
                    append("\n距离今日目标还差${dailyGoal - currentStudyMinutes}分钟！")
                }
                else -> {
                    append("\n今日目标已达成！要不要挑战更多？")
                }
            }

            // 薄弱课程提醒
            if (weakCourses.isNotEmpty()) {
                append("\n\n建议重点复习：")
                weakCourses.take(2).forEach {
                    append("\n• ${it.name}(${(it.masteryLevel * 100).toInt()}%)")
                }
            }

            // 推荐任务
            val nextTask = pendingTasks.filter {
                it.status == TaskStatus.PENDING
            }.sortedByDescending { it.priority }.firstOrNull()

            if (nextTask != null) {
                append("\n\n推荐先做：「${nextTask.title}」")
            }
        }

        return CareEvent(
            id = "study_reminder_${now}",
            type = CareType.STUDY_REMINDER,
            scheduledTime = now,
            title = "学习提醒",
            message = message,
            actionText = "开始学习",
            actionData = """{"action":"START_FOCUS","params":{}}""",
            priority = 4
        )
    }

    // ==================== 休息建议 ====================

    /**
     * 生成休息建议
     */
    fun generateBreakSuggestion(
        continuousStudyMinutes: Int,
        todayStudyMinutes: Int,
        lastBreakTime: Long?
    ): CareEvent {
        val now = System.currentTimeMillis()

        val message = buildString {
            append("休息一下，充充电吧！")

            append("\n已连续学习${continuousStudyMinutes}分钟")

            // 根据学习时长给出建议
            when {
                continuousStudyMinutes >= 90 -> {
                    append("\n\n大脑需要休息了！建议站起来活动一下，喝杯水，眺望远方。")
                    append("\n长时间学习会降低效率，适当休息反而能提高学习质量。")
                }
                continuousStudyMinutes >= 60 -> {
                    append("\n\n专注了挺久啦！可以休息5-10分钟，走动走动放松身体。")
                }
                continuousStudyMinutes >= 45 -> {
                    append("\n\n状态不错！如果感觉疲劳，可以稍作休息再继续。")
                }
                else -> {
                    append("\n\n刚开始学习，状态正好！累了就休息一下。")
                }
            }

            // 健康提醒
            val hoursSinceLastBreak = if (lastBreakTime != null) {
                (now - lastBreakTime) / (60 * 60 * 1000)
            } else {
                continuousStudyMinutes / 60L
            }

            if (hoursSinceLastBreak >= 2) {
                append("\n\n💡 小提示：每隔45-60分钟休息一次，有助于保持学习效率哦！")
            }
        }

        return CareEvent(
            id = "break_${now}",
            type = CareType.BREAK_SUGGESTION,
            scheduledTime = now,
            title = "休息提醒",
            message = message,
            actionText = "开始休息",
            actionData = """{"action":"START_BREAK","params":{"minutes":10}}""",
            priority = 3
        )
    }

    // ==================== 晚间总结 ====================

    /**
     * 生成晚间总结
     */
    fun generateNightSummary(
        user: User,
        todayStudyMinutes: Int,
        completedTasks: Int,
        totalTasks: Int,
        coursesStudied: List<String>,
        achievements: List<Achievement>,
        tomorrowSchedule: List<Pair<Course, ScheduleSlot>>
    ): CareEvent {
        val now = System.currentTimeMillis()

        val message = buildString {
            append("晚安，${user.name.ifEmpty { "星际园丁" }}！")
            append("\n\n今日学习总结：")

            // 学习时长
            append("\n• 学习时长：${todayStudyMinutes}分钟")
            if (todayStudyMinutes >= user.weeklyStudyGoal / 7) {
                append(" ✓ 达成每日目标")
            }

            // 任务完成
            append("\n• 完成任务：$completedTasks/$totalTasks")
            if (completedTasks == totalTasks && totalTasks > 0) {
                append(" ✓ 全部完成")
            }

            // 学习课程
            if (coursesStudied.isNotEmpty()) {
                append("\n• 学习课程：${coursesStudied.take(3).joinToString("、")}")
            }

            // 成就解锁
            if (achievements.isNotEmpty()) {
                append("\n\n解锁成就：")
                achievements.take(3).forEach {
                    append("\n  ${it.icon} ${it.name}")
                }
            }

            // 奖励总结
            val energyEarned = completedTasks * 10 + todayStudyMinutes / 10
            val crystalsEarned = completedTasks * 5
            append("\n\n今日获得：")
            append("\n⚡ 能量 +$energyEarned")
            append("\n💎 结晶 +$crystalsEarned")

            // 明日预告
            if (tomorrowSchedule.isNotEmpty()) {
                append("\n\n明日预告：")
                append("\n${tomorrowSchedule.size}节课")
                tomorrowSchedule.take(2).forEach { (course, _) ->
                    append("\n• ${course.name}")
                }
            }

            // 晚安语
            append("\n\n")
            append(getGoodnightQuote())
        }

        return CareEvent(
            id = "night_summary_${now}",
            type = CareType.NIGHT_SUMMARY,
            scheduledTime = now,
            title = "今日总结",
            message = message,
            priority = 4
        )
    }

    // ==================== 考试倒计时 ====================

    /**
     * 生成考试倒计时提醒
     */
    fun generateExamCountdown(
        course: Course,
        daysRemaining: Int,
        masteryLevel: Float,
        weakNodes: List<KnowledgeNode>
    ): CareEvent {
        val now = System.currentTimeMillis()

        val message = buildString {
            append("📅 考试倒计时")

            append("\n\n${course.name}")
            append("\n距离考试还有 ${daysRemaining} 天")

            // 掌握度警告
            val masteryPercent = (masteryLevel * 100).toInt()
            append("\n当前掌握度：$masteryPercent%")

            when {
                masteryPercent < 30 -> {
                    append("\n\n⚠️ 掌握度较低！建议立即开始复习。")
                }
                masteryPercent < 60 -> {
                    append("\n\n⚡ 还需加强！建议重点攻克薄弱知识点。")
                }
                masteryPercent < 80 -> {
                    append("\n\n💪 进展不错！继续保持复习节奏。")
                }
                else -> {
                    append("\n\n🌟 准备充分！可以适当放松，保持状态。")
                }
            }

            // 薄弱知识点
            if (weakNodes.isNotEmpty()) {
                append("\n\n重点复习：")
                weakNodes.take(3).forEach {
                    append("\n• ${it.name} (${(it.masteryLevel * 100).toInt()}%)")
                }
            }

            // 建议学习时长
            val suggestedHours = when {
                daysRemaining <= 3 -> 4
                daysRemaining <= 7 -> 3
                daysRemaining <= 14 -> 2
                else -> 1
            }
            append("\n\n建议每日学习${suggestedHours}小时")
        }

        return CareEvent(
            id = "exam_countdown_${course.id}_$now",
            type = CareType.EXAM_COUNTDOWN,
            scheduledTime = now,
            title = "考试倒计时",
            message = message,
            actionText = "开始复习",
            actionData = """{"action":"GO_TO","params":{"screen":"course","courseId":"${course.id}"}}""",
            priority = 5
        )
    }

    // ==================== 连续签到预警 ====================

    /**
     * 生成连续签到预警
     */
    fun generateStreakWarning(
        streakDays: Int,
        lastCheckInDate: String?,
        protectionCards: Int
    ): CareEvent {
        val now = System.currentTimeMillis()
        val today = getTodayDateString()

        val isAtRisk = lastCheckInDate != today

        val message = buildString {
            append("🔥 连续签到预警")

            if (streakDays >= 7) {
                append("\n\n已连续学习${streakDays}天！")
                append("\n坚持得很棒，别忘了今天签到哦~")
            } else if (streakDays >= 3) {
                append("\n\n已连续学习${streakDays}天")
                append("\n继续签到，养成学习习惯！")
            } else if (streakDays >= 1) {
                append("\n\n已经开始了学习之旅")
                append("\n每天签到，让学习成为习惯！")
            }

            if (isAtRisk) {
                append("\n\n⚠️ 今天还没有签到！")
                append("\n如果不签到，连续天数将归零。")

                if (protectionCards > 0) {
                    append("\n\n你有${protectionCards}张断签保护卡，")
                    append("可以保护连续天数不被中断。")
                }
            }

            append("\n\n连续签到奖励：")
            append("\n• 7天：周冠军礼包")
            append("\n• 30天：月度之星徽章")
            append("\n• 100天：传奇称号")
        }

        return CareEvent(
            id = "streak_warning_$now",
            type = CareType.STREAK_WARNING,
            scheduledTime = now,
            title = "签到提醒",
            message = message,
            actionText = "立即签到",
            actionData = """{"action":"CHECK_IN","params":{}}""",
            priority = 5
        )
    }

    // ==================== 成就庆祝 ====================

    /**
     * 生成成就庆祝消息
     */
    fun generateAchievementCelebration(achievement: Achievement): CareEvent {
        val now = System.currentTimeMillis()

        val message = buildString {
            append("🎉 成就解锁！")
            append("\n\n${achievement.icon} ${achievement.name}")
            append("\n${achievement.description}")

            append("\n\n奖励已发放：")
            append("\n⚡ 能量 +${achievement.rewardEnergy}")
            append("\n💎 结晶 +${achievement.rewardCrystals}")

            append("\n\n")
            append(getAchievementQuote(achievement))
        }

        return CareEvent(
            id = "achievement_${achievement.id}_$now",
            type = CareType.ACHIEVEMENT_CELEBRATION,
            scheduledTime = now,
            title = "成就解锁",
            message = message,
            actionText = "查看成就",
            actionData = """{"action":"GO_TO","params":{"screen":"achievements"}}""",
            priority = 5
        )
    }

    // ==================== 情感支持 ====================

    /**
     * 生成情感支持消息
     */
    fun generateEmotionalSupport(
        mood: MoodState,
        recentFailures: Int,
        stressLevel: Int
    ): CareEvent {
        val now = System.currentTimeMillis()

        val message = buildString {
            when (mood) {
                MoodState.FRUSTRATED -> {
                    append("感觉有些挫折吗？")
                    append("\n\n学习路上遇到困难很正常，")
                    append("每个伟大的学习者都经历过失败。")
                    append("\n\n💡 小建议：")
                    append("\n• 暂时放下，做点别的转换心情")
                    append("\n• 把大目标拆成小步骤")
                    append("\n• 找同学或老师讨论一下")
                }
                MoodState.ANXIOUS -> {
                    append("有些焦虑？")
                    append("\n\n焦虑说明你在乎，这是好事！")
                    append("但过度焦虑会影响表现。")
                    append("\n\n💡 试试这些：")
                    append("\n• 深呼吸几次")
                    append("\n• 写下担心的事情，逐一分析")
                    append("\n• 关注当下能做的事")
                }
                MoodState.OVERWHELMED -> {
                    append("事情太多了？")
                    append("\n\n一步步来，不用着急。")
                    append("把任务按优先级排序，")
                    append("先完成最重要的那一个。")
                    append("\n\n记住：你不需要一次做完所有事。")
                }
                MoodState.TIRED -> {
                    append("看起来很疲惫...")
                    append("\n\n休息也是学习的一部分。")
                    append("大脑需要时间整理和巩固知识。")
                    append("\n\n💡 建议：")
                    append("\n• 小睡20分钟")
                    append("\n• 喝杯水，吃点水果")
                    append("\n• 到户外走走")
                }
                else -> {
                    append("状态不错嘛！")
                    append("\n\n继续保持这个节奏，")
                    append("相信你能达成目标！")
                }
            }

            if (recentFailures > 2) {
                append("\n\n最近遇到了一些困难，")
                append("但不要放弃，坚持下去就会有突破！")
            }

            if (stressLevel >= 7) {
                append("\n\n⚠️ 压力较大，记得适当放松。")
                append("身心健康比成绩更重要。")
            }
        }

        return CareEvent(
            id = "emotional_support_$now",
            type = CareType.EMOTIONAL_SUPPORT,
            scheduledTime = now,
            title = "关怀",
            message = message,
            priority = 4
        )
    }

    // ==================== 激励语 ====================

    /**
     * 生成激励消息
     */
    fun generateMotivation(
        context: MotivationContext
    ): CareEvent {
        val now = System.currentTimeMillis()
        val quote = getMotivationalQuote(context)

        return CareEvent(
            id = "motivation_$now",
            type = CareType.MOTIVATION,
            scheduledTime = now,
            title = "每日激励",
            message = quote,
            priority = 2
        )
    }

    // ==================== 辅助方法 ====================

    private fun getMotivationalQuote(context: MotivationContext = MotivationContext.GENERAL): String {
        val quotes = when (context) {
            MotivationContext.MORNING -> listOf(
                "新的一天，新的开始。把握当下，成就未来！",
                "早起的人运气不会太差，今天也要元气满满！",
                "每一个清晨都是重新出发的机会。"
            )
            MotivationContext.STUDYING -> listOf(
                "学习是一场马拉松，不是短跑。保持节奏！",
                "专注当下，其他的事稍后再说。",
                "每一分钟的努力都在积累，量变终会质变。"
            )
            MotivationContext.TIRED -> listOf(
                "累了就休息，但不要放弃。",
                "调整状态再出发，比硬撑更有效率。",
                "坚持很难，但放弃更难。"
            )
            MotivationContext.ACHIEVEMENT -> listOf(
                "你的努力正在开花结果！",
                "每一步进步都值得庆祝！",
                "继续前进，更大的成就等着你！"
            )
            else -> listOf(
                "星光不负赶路人，时光不负有心人。",
                "今天的努力，是明天的底气。",
                "不积跬步，无以至千里。",
                "学习是点亮未来的灯塔。",
                "保持好奇，保持学习，保持成长。"
            )
        }
        return quotes.random()
    }

    private fun getGoodnightQuote(): String {
        val quotes = listOf(
            "好好休息，明天继续加油！晚安~",
            "充实的今天结束了，美好的明天在等待。晚安！",
            "梦里复习一下，明天记得更牢哦~ 晚安！",
            "辛苦了！好的睡眠是最好的学习。晚安！"
        )
        return quotes.random()
    }

    private fun getAchievementQuote(achievement: Achievement): String {
        return when (achievement.category) {
            AchievementCategory.LEARNING -> "学习之路上，你又前进了一步！"
            AchievementCategory.STREAK -> "坚持就是胜利，你做到了！"
            AchievementCategory.EXPLORATION -> "探索未知，收获成长！"
            AchievementCategory.SOCIAL -> "与他人一起，走得更远！"
            AchievementCategory.MASTERY -> "精通之路，永无止境！"
        }
    }
}

/**
 * 心情状态
 */
enum class MoodState {
    ENERGETIC,    // 精力充沛
    HAPPY,        // 愉快
    CALM,         // 平静
    ANXIOUS,      // 焦虑
    FRUSTRATED,   // 挫败
    TIRED,        // 疲惫
    OVERWHELMED   // 不堪重负
}

/**
 * 激励上下文
 */
enum class MotivationContext {
    GENERAL,
    MORNING,
    STUDYING,
    TIRED,
    ACHIEVEMENT
}

// ==================== 关怀调度器 ====================

/**
 * AI关怀调度器
 * 管理所有关怀事件的调度和触发
 */
class AICareScheduler(
    private val config: CareConfig = CareConfig("")
) {
    private val pendingCares = mutableListOf<CareEvent>()
    private val sentCares = mutableListOf<CareEvent>()

    /**
     * 检查是否需要发送关怀
     */
    fun checkAndGenerateCares(
        user: User,
        context: LearningContext
    ): List<CareEvent> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val cares = mutableListOf<CareEvent>()

        // 早安问候 (配置时间)
        if (config.morningGreetingEnabled && hour == config.morningGreetingTime && minute < 5) {
            cares.add(
                AICareMessageGenerator.generateMorningGreeting(
                    user = user,
                    todaySchedule = context.todaySchedule,
                    pendingTasks = context.pendingTasks,
                    streakDays = user.streakDays
                )
            )
        }

        // 连续签到预警 (晚上8点检查)
        if (config.streakWarningEnabled && hour == 20 && minute < 5) {
            val today = getTodayDateString()
            if (user.lastCheckInDate != today) {
                cares.add(
                    AICareMessageGenerator.generateStreakWarning(
                        streakDays = user.streakDays,
                        lastCheckInDate = user.lastCheckInDate,
                        protectionCards = user.streakProtectionCards
                    )
                )
            }
        }

        // 晚间总结 (配置时间)
        if (config.nightSummaryEnabled && hour == config.nightSummaryTime && minute < 5) {
            cares.add(
                AICareMessageGenerator.generateNightSummary(
                    user = user,
                    todayStudyMinutes = user.totalStudyMinutes,
                    completedTasks = context.pendingTasks.count { it.status == TaskStatus.COMPLETED },
                    totalTasks = context.pendingTasks.size,
                    coursesStudied = context.courses.map { it.name },
                    achievements = emptyList(), // 从成就系统获取
                    tomorrowSchedule = emptyList() // 获取明日课表
                )
            )
        }

        // 考试倒计时
        if (config.examCountdownEnabled) {
            context.courses.filter { it.examDate != null }.forEach { course ->
                val daysRemaining = ((course.examDate!! - now) / (24 * 60 * 60 * 1000)).toInt()
                if (daysRemaining in listOf(7, 3, 1)) {
                    cares.add(
                        AICareMessageGenerator.generateExamCountdown(
                            course = course,
                            daysRemaining = daysRemaining,
                            masteryLevel = course.masteryLevel,
                            weakNodes = emptyList() // 从知识图谱获取
                        )
                    )
                }
            }
        }

        return cares
    }

    /**
     * 手动触发关怀
     */
    fun triggerCare(type: CareType, vararg params: Any): CareEvent? {
        return when (type) {
            CareType.MOTIVATION -> AICareMessageGenerator.generateMotivation(MotivationContext.GENERAL)
            else -> null
        }
    }

    /**
     * 标记关怀已读
     */
    fun markAsRead(careId: String) {
        val care = pendingCares.find { it.id == careId }
        if (care != null) {
            pendingCares.remove(care)
            sentCares.add(care.copy(isRead = true))
        }
    }

    /**
     * 获取未读关怀
     */
    fun getUnreadCares(): List<CareEvent> {
        return pendingCares.filter { !it.isRead }
    }

    /**
     * 获取最近关怀历史
     */
    fun getRecentCares(limit: Int = 10): List<CareEvent> {
        return sentCares.sortedByDescending { it.createdAt }.take(limit)
    }
}