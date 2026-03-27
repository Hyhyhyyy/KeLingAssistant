package com.keling.app.data

/**
 * Models.kt
 * 定义应用的所有数据结构
 * 使用Kotlin的data class，自动生成equals、hashCode、toString
 *
 * @Serializable用于JSON序列化，方便网络传输和本地存储
 */

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ==================== 用户相关 ====================

/**
 * 用户数据
 */
@Serializable
data class User(
    val id: String,
    val name: String = "星际园丁",
    val level: Int = 1,
    val exp: Int = 0,
    val energy: Int = 100,
    val crystals: Int = 10,
    val streakDays: Int = 0,
    val totalStudyMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val streakProtectionCards: Int = 0,
    val lastCheckInDate: String? = null,
    val avatarUrl: String? = null,
    val bio: String = "",
    val weeklyStudyGoal: Int = 300
)

// ==================== 课程与知识 ====================

@Serializable
data class Course(
    val id: String,
    val name: String,
    val code: String,
    val teacher: String,
    val schedule: List<ScheduleSlot> = emptyList(),
    val location: String = "",
    val themeColor: Long = 0xFFE8A87C,
    val masteryLevel: Float = 0f,
    val plantStage: Int = 0,
    val planetStyleIndex: Int = -1,
    val lastStudiedAt: Long? = null,
    val totalStudyMinutes: Int = 0,
    val isArchived: Boolean = false,
    val semester: String? = null,
    val credit: Int = 0,
    val examDate: Long? = null,
    val courseImageUrl: String? = null,
    val studySessionCount: Int = 0
)

@Serializable
data class ScheduleSlot(
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val durationMinutes: Int
)

@Serializable
data class KnowledgeNode(
    val id: String,
    val courseId: String,
    val name: String,
    val description: String = "",
    val parentIds: List<String> = emptyList(),
    val childIds: List<String> = emptyList(),
    val difficulty: Int = 3,
    val masteryLevel: Float = 0f,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.5f,
    val isUnlocked: Boolean = false
)

// ==================== 任务系统 ====================

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val type: TaskType,
    val courseId: String? = null,
    val knowledgeNodeIds: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Int = 3,
    val estimatedMinutes: Int = 25,
    val actualMinutes: Int? = null,
    val rewards: Rewards = Rewards(),
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,
    val completedAt: Long? = null
)

enum class TaskType {
    DAILY_CARE,
    DEEP_EXPLORATION,
    REVIEW_RITUAL,
    BOUNTY,
    RESCUE
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

@Serializable
data class Rewards(
    val energy: Int = 10,
    val crystals: Int = 5,
    val exp: Int = 20
)

// ==================== 笔记与AI ====================

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val sourceType: NoteSource,
    val aiExplanation: String? = null,
    val relatedNodeIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val lastReviewedAt: Long? = null
)

enum class NoteSource {
    AI_GENERATED,
    USER_CREATED,
    CLASS_CAPTURE,
    BOUNTY_REWARD
}

// ==================== 成就系统 ====================

@Serializable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val requirement: String = "",
    val rewardEnergy: Int = 50,
    val rewardCrystals: Int = 30,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
)

enum class AchievementCategory {
    LEARNING,
    STREAK,
    EXPLORATION,
    SOCIAL,
    MASTERY
}

// ==================== 签到系统 ====================

@Serializable
data class CheckInRecord(
    val date: String,
    val userId: String,
    val rewardReceived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 学习记录 ====================

@Serializable
data class StudyRecord(
    val id: String,
    val userId: String,
    val courseId: String? = null,
    val taskId: String? = null,
    val type: StudyType,
    val durationMinutes: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class StudyType {
    TASK_COMPLETION,
    COURSE_STUDY,
    REVIEW_SESSION,
    PRACTICE,
    AI_INTERACTION
}

// ==================== 任务模板 ====================

@Serializable
data class TaskTemplate(
    val id: String,
    val name: String,
    val description: String,
    val defaultDuration: Int,
    val defaultType: TaskType,
    val defaultPriority: Int = 3,
    val icon: String = "📋",
    val category: String = "general"
)

val TASK_TEMPLATES = listOf(
    TaskTemplate(
        id = "exam_prep",
        name = "考前冲刺",
        description = "考试前集中复习，重点突破",
        defaultDuration = 60,
        defaultType = TaskType.DEEP_EXPLORATION,
        defaultPriority = 5,
        icon = "🎯",
        category = "exam"
    ),
    TaskTemplate(
        id = "daily_review",
        name = "日常复习",
        description = "每日知识巩固，保持记忆",
        defaultDuration = 25,
        defaultType = TaskType.DAILY_CARE,
        defaultPriority = 3,
        icon = "🌱",
        category = "review"
    ),
    TaskTemplate(
        id = "quick_practice",
        name = "快速练习",
        description = "短时间高效刷题",
        defaultDuration = 15,
        defaultType = TaskType.DAILY_CARE,
        defaultPriority = 2,
        icon = "⚡",
        category = "practice"
    ),
    TaskTemplate(
        id = "deep_study",
        name = "深度学习",
        description = "深入理解概念，攻克难点",
        defaultDuration = 45,
        defaultType = TaskType.DEEP_EXPLORATION,
        defaultPriority = 4,
        icon = "🔬",
        category = "general"
    ),
    TaskTemplate(
        id = "spaced_review",
        name = "间隔复习",
        description = "基于遗忘曲线的科学复习",
        defaultDuration = 20,
        defaultType = TaskType.REVIEW_RITUAL,
        defaultPriority = 3,
        icon = "🔄",
        category = "review"
    ),
    TaskTemplate(
        id = "homework",
        name = "作业任务",
        description = "完成课后作业",
        defaultDuration = 40,
        defaultType = TaskType.DAILY_CARE,
        defaultPriority = 4,
        icon = "📝",
        category = "general"
    )
)

// ==================== 挑战系统 ====================

@Serializable
data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val target: Int,
    val progress: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val rewards: Rewards,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

enum class ChallengeType {
    DAILY_STUDY,
    TASK_COMPLETION,
    STREAK_DAYS,
    COURSE_MASTERY,
    NOTE_CREATION,
    KNOWLEDGE_UNLOCK
}

// ==================== 学习会话 ====================

@Serializable
data class StudySession(
    val id: String,
    val userId: String,
    val courseId: String?,
    val taskId: String?,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val type: StudyType,
    val notes: String = "",
    val focusScore: Float? = null,
    val distractions: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 任务推荐 ====================

@Serializable
data class TaskRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val courseId: String?,
    val type: TaskType,
    val estimatedMinutes: Int,
    val priority: Int,
    val reason: String,
    val relevanceScore: Float,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 番茄钟设置 ====================

@Serializable
data class PomodoroSettings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartBreak: Boolean = true,
    val autoStartNextSession: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

// ==================== 学习偏好 ====================

@Serializable
data class StudyPreferences(
    val preferredStudyHours: List<Int> = listOf(9, 10, 14, 15, 20, 21),
    val dailyGoalMinutes: Int = 60,
    val weeklyGoalMinutes: Int = 300,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val restReminderEnabled: Boolean = true,
    val maxContinuousStudyMinutes: Int = 90
)

// ==================== 笔记附件 ====================

@Serializable
data class NoteAttachment(
    val id: String,
    val noteId: String,
    val type: AttachmentType,
    val uri: String,
    val thumbnailUri: String? = null,
    val fileName: String = "",
    val fileSize: Long = 0,
    val duration: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AttachmentType {
    IMAGE,
    AUDIO,
    VIDEO,
    FILE,
    LINK
}

// ==================== 学习路径 ====================

@Serializable
data class LearningPath(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val nodes: List<LearningPathNode>,
    val totalEstimatedMinutes: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class LearningPathNode(
    val nodeId: String,
    val nodeName: String,
    val order: Int,
    val isCompleted: Boolean = false,
    val estimatedMinutes: Int = 30,
    val prerequisites: List<String> = emptyList()
)

// ==================== 学习报告 ====================

@Serializable
data class StudyReport(
    val id: String,
    val userId: String,
    val startDate: Long,
    val endDate: Long,
    val totalStudyMinutes: Int = 0,
    val completedTasks: Int = 0,
    val coursesStudied: Int = 0,
    val averageMastery: Float = 0f,
    val streakDays: Int = 0,
    val aiInsight: String = "",
    val strongPoints: List<String> = emptyList(),
    val weakPoints: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 签到奖励 ====================

@Serializable
data class CheckInReward(
    val day: Int,
    val energy: Int,
    val crystals: Int,
    val isSpecial: Boolean = false,
    val specialReward: String? = null
)

val CHECK_IN_REWARDS = listOf(
    CheckInReward(day = 1, energy = 10, crystals = 5),
    CheckInReward(day = 2, energy = 15, crystals = 8),
    CheckInReward(day = 3, energy = 20, crystals = 10),
    CheckInReward(day = 4, energy = 25, crystals = 12),
    CheckInReward(day = 5, energy = 30, crystals = 15),
    CheckInReward(day = 6, energy = 40, crystals = 20),
    CheckInReward(day = 7, energy = 50, crystals = 30, isSpecial = true, specialReward = "周奖励已解锁！")
)

// ==================== 预定义成就 ====================

val PREDEFINED_ACHIEVEMENTS = listOf(
    Achievement(
        id = "first_task",
        name = "初学者",
        description = "完成第一个学习任务",
        icon = "🌱",
        category = AchievementCategory.LEARNING,
        requirement = "完成1个任务",
        maxProgress = 1
    ),
    Achievement(
        id = "task_master_10",
        name = "任务达人",
        description = "累计完成10个学习任务",
        icon = "📋",
        category = AchievementCategory.LEARNING,
        requirement = "累计完成10个任务",
        rewardEnergy = 100,
        maxProgress = 10
    ),
    Achievement(
        id = "task_master_50",
        name = "任务大师",
        description = "累计完成50个学习任务",
        icon = "🏆",
        category = AchievementCategory.LEARNING,
        requirement = "累计完成50个任务",
        rewardEnergy = 300,
        rewardCrystals = 100,
        maxProgress = 50
    ),
    Achievement(
        id = "first_course",
        name = "星际探索者",
        description = "创建第一颗知识星球",
        icon = "🌍",
        category = AchievementCategory.EXPLORATION,
        requirement = "创建1个课程",
        maxProgress = 1
    ),
    Achievement(
        id = "mastery_80",
        name = "知识精通",
        description = "将任意课程掌握度提升到80%以上",
        icon = "✨",
        category = AchievementCategory.MASTERY,
        requirement = "单课程掌握度≥80%",
        rewardEnergy = 200,
        rewardCrystals = 50,
        maxProgress = 1
    ),
    Achievement(
        id = "streak_3",
        name = "坚持三天",
        description = "连续学习3天",
        icon = "🔥",
        category = AchievementCategory.STREAK,
        requirement = "连续学习3天",
        rewardEnergy = 50,
        maxProgress = 3
    ),
    Achievement(
        id = "streak_7",
        name = "周周坚持",
        description = "连续学习7天",
        icon = "🌟",
        category = AchievementCategory.STREAK,
        requirement = "连续学习7天",
        rewardEnergy = 150,
        rewardCrystals = 30,
        maxProgress = 7
    ),
    Achievement(
        id = "streak_30",
        name = "月度之星",
        description = "连续学习30天",
        icon = "💫",
        category = AchievementCategory.STREAK,
        requirement = "连续学习30天",
        rewardEnergy = 500,
        rewardCrystals = 200,
        maxProgress = 30
    ),
    Achievement(
        id = "knowledge_10",
        name = "知识收集者",
        description = "解锁10个知识点",
        icon = "📚",
        category = AchievementCategory.EXPLORATION,
        requirement = "解锁10个知识节点",
        rewardEnergy = 80,
        maxProgress = 10
    ),
    Achievement(
        id = "notes_5",
        name = "笔记达人",
        description = "创建5篇学习笔记",
        icon = "📝",
        category = AchievementCategory.LEARNING,
        requirement = "创建5篇笔记",
        rewardEnergy = 100,
        maxProgress = 5
    )
)

// ==================== 工具函数 ====================

val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

inline fun <reified T> T.toJson(): String = json.encodeToString(this)
inline fun <reified T> String.fromJson(): T = json.decodeFromString(this)

fun getTodayDateString(): String {
    val cal = java.util.Calendar.getInstance()
    return String.format(
        "%04d%02d%02d",
        cal.get(java.util.Calendar.YEAR),
        cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DAY_OF_MONTH)
    )
}

fun generateWeeklyChallenges(): List<Challenge> {
    val now = System.currentTimeMillis()
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    val weekStart = cal.timeInMillis
    val weekEnd = weekStart + 7 * 24 * 60 * 60 * 1000

    return listOf(
        Challenge(
            id = "weekly_study_${java.text.SimpleDateFormat("yyyyMMdd").format(weekStart)}",
            title = "周学习达人",
            description = "本周累计学习300分钟",
            type = ChallengeType.DAILY_STUDY,
            target = 300,
            startDate = weekStart,
            endDate = weekEnd,
            rewards = Rewards(energy = 100, crystals = 50, exp = 150)
        ),
        Challenge(
            id = "weekly_tasks_${java.text.SimpleDateFormat("yyyyMMdd").format(weekStart)}",
            title = "任务收割机",
            description = "本周完成10个任务",
            type = ChallengeType.TASK_COMPLETION,
            target = 10,
            startDate = weekStart,
            endDate = weekEnd,
            rewards = Rewards(energy = 80, crystals = 40, exp = 120)
        ),
        Challenge(
            id = "weekly_notes_${java.text.SimpleDateFormat("yyyyMMdd").format(weekStart)}",
            title = "笔记狂人",
            description = "本周创建3篇笔记",
            type = ChallengeType.NOTE_CREATION,
            target = 3,
            startDate = weekStart,
            endDate = weekEnd,
            rewards = Rewards(energy = 60, crystals = 30, exp = 80)
        )
    )
}