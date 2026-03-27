package com.keling.app.ai

import com.keling.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * =========================
 * 学习闭环引擎
 * =========================
 *
 * 功能：
 * - 诊断阶段：分析用户当前学习状态和薄弱点
 * - 计划阶段：生成个性化学习计划
 * - 执行阶段：追踪学习进度和任务完成
 * - 反馈阶段：生成学习反馈报告
 * - 调整阶段：基于反馈优化后续计划
 *
 * 这形成了完整的 "诊断→计划→执行→反馈→调整" 学习闭环
 */

// ==================== 数据模型 ====================

/**
 * 诊断结果
 */
data class DiagnosisResult(
    val timestamp: Long = System.currentTimeMillis(),
    val courseId: String?,
    val courseName: String?,
    val overallMastery: Float = 0f,
    val weakKnowledgeNodes: List<KnowledgeNodeDiagnosis> = emptyList(),
    val strongKnowledgeNodes: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val priority: DiagnosisPriority = DiagnosisPriority.NORMAL
)

/**
 * 知识点诊断详情
 */
data class KnowledgeNodeDiagnosis(
    val nodeId: String,
    val nodeName: String,
    val masteryLevel: Float,
    val issue: String,          // 问题描述
    val suggestedAction: String // 建议行动
)

/**
 * 诊断优先级
 */
enum class DiagnosisPriority {
    LOW,      // 状态良好
    NORMAL,   // 有一些薄弱点
    HIGH,     // 有明显薄弱点
    URGENT    // 急需关注
}

/**
 * 学习计划
 */
data class LearningPlan(
    val id: String = "plan_${System.currentTimeMillis()}",
    val createdAt: Long = System.currentTimeMillis(),
    val courseId: String?,
    val courseName: String?,
    val goals: List<LearningGoal>,
    val tasks: List<PlannedTask>,
    val estimatedMinutes: Int,
    val weeklySchedule: Map<Int, List<PlannedTask>> = emptyMap(), // dayOfWeek -> tasks
    val isActive: Boolean = true
)

/**
 * 学习目标
 */
data class LearningGoal(
    val id: String,
    val description: String,
    val targetKnowledgeNodes: List<String>,
    val targetMastery: Float,
    val deadline: Long? = null,
    var currentProgress: Float = 0f
)

/**
 * 计划中的任务
 */
data class PlannedTask(
    val taskId: String,
    val title: String,
    val type: TaskType,
    val estimatedMinutes: Int,
    val scheduledTime: Long? = null,
    val relatedNodeIds: List<String> = emptyList(),
    var status: TaskStatus = TaskStatus.PENDING,
    var completedAt: Long? = null,
    var actualMinutes: Int? = null
)

/**
 * 学习会话（一次完整的学习过程）
 */
data class LearningSession(
    val id: String = "session_${System.currentTimeMillis()}",
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    val planId: String?,
    var completedTasks: List<TaskResult> = emptyList(),
    var quizResults: List<QuizResult> = emptyList(),
    var userFeedback: SessionFeedback? = null,
    var aiComments: List<String> = emptyList()
) {
    val durationMinutes: Int
        get() = (((endTime ?: System.currentTimeMillis()) - startTime) / 60000).toInt()

    val totalEarned: Rewards
        get() = completedTasks.fold(Rewards()) { acc, result ->
            Rewards(
                energy = acc.energy + result.earnedEnergy,
                crystals = acc.crystals + result.earnedCrystals,
                exp = acc.exp + result.earnedExp
            )
        }
}

/**
 * 任务执行结果
 */
data class TaskResult(
    val taskId: String,
    val title: String,
    val estimatedMinutes: Int,
    val actualMinutes: Int,
    val completedAt: Long,
    val earnedEnergy: Int,
    val earnedCrystals: Int,
    val earnedExp: Int,
    val notes: String? = null
)

/**
 * 测验结果
 */
data class QuizResult(
    val quizId: String,
    val nodeId: String,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int
)

/**
 * 会话反馈
 */
data class SessionFeedback(
    val rating: Int,           // 1-5 星
    val difficulty: Int,       // 1-5 难度感知
    val enjoyment: Int,        // 1-5 愉悦度
    val comments: String? = null
)

/**
 * 反馈报告
 */
data class FeedbackReport(
    val sessionId: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val summary: String,
    val strengths: List<String>,
    val areasToImprove: List<String>,
    val nextSteps: List<String>,
    val motivationalMessage: String,
    val statistics: SessionStatistics
)

/**
 * 会话统计数据
 */
data class SessionStatistics(
    val totalMinutes: Int,
    val tasksCompleted: Int,
    val tasksSkipped: Int,
    val quizAccuracy: Float?,
    val energyEarned: Int,
    val crystalsEarned: Int,
    val expEarned: Int,
    val streakBonus: Int = 0
)

// ==================== 学习闭环引擎 ====================

/**
 * 学习闭环引擎
 */
class LearningLoopEngine(
    private val dataProvider: LearningDataProvider,
    private val aiCoordinator: AICoordinator,
    private val profileProvider: EnhancedLearningProfileProvider
) {

    private val _currentSession = MutableStateFlow<LearningSession?>(null)
    val currentSession: StateFlow<LearningSession?> = _currentSession.asStateFlow()

    private val _currentPlan = MutableStateFlow<LearningPlan?>(null)
    val currentPlan: StateFlow<LearningPlan?> = _currentPlan.asStateFlow()

    private val _sessionHistory = MutableStateFlow<List<LearningSession>>(emptyList())
    val sessionHistory: StateFlow<List<LearningSession>> = _sessionHistory.asStateFlow()

    // ==================== 1. 诊断阶段 ====================

    /**
     * 诊断用户的学习状态
     */
    suspend fun diagnose(courseId: String? = null): DiagnosisResult {
        val courses = if (courseId != null) {
            dataProvider.getCourses().filter { it.id == courseId }
        } else {
            dataProvider.getCourses()
        }

        if (courses.isEmpty()) {
            return DiagnosisResult(
                courseId = courseId,
                courseName = null,
                recommendations = listOf("请先添加课程开始学习")
            )
        }

        val targetCourse = courses.first()
        val knowledgeNodes = dataProvider.getKnowledgeNodes()
            .filter { it.courseId == targetCourse.id }

        // 分析薄弱知识点
        val weakNodes = knowledgeNodes
            .filter { it.masteryLevel < 0.6f }
            .sortedBy { it.masteryLevel }
            .map { node ->
                val issue = when {
                    node.masteryLevel < 0.3 -> "基础概念不清晰"
                    node.masteryLevel < 0.5 -> "理解不够深入"
                    else -> "需要巩固加强"
                }
                val action = when {
                    node.masteryLevel < 0.3 -> "建议从头学习基础概念"
                    node.masteryLevel < 0.5 -> "建议做针对性练习"
                    else -> "建议定期复习巩固"
                }
                KnowledgeNodeDiagnosis(
                    nodeId = node.id,
                    nodeName = node.name,
                    masteryLevel = node.masteryLevel,
                    issue = issue,
                    suggestedAction = action
                )
            }

        // 强势知识点
        val strongNodes = knowledgeNodes
            .filter { it.masteryLevel >= 0.8f }
            .map { it.name }

        // 生成建议
        val recommendations = generateDiagnosisRecommendations(
            course = targetCourse,
            weakNodes = weakNodes,
            strongNodes = strongNodes
        )

        // 确定优先级
        val priority = when {
            targetCourse.masteryLevel < 0.3 -> DiagnosisPriority.URGENT
            targetCourse.masteryLevel < 0.5 -> DiagnosisPriority.HIGH
            targetCourse.masteryLevel < 0.7 -> DiagnosisPriority.NORMAL
            else -> DiagnosisPriority.LOW
        }

        return DiagnosisResult(
            courseId = targetCourse.id,
            courseName = targetCourse.name,
            overallMastery = targetCourse.masteryLevel,
            weakKnowledgeNodes = weakNodes,
            strongKnowledgeNodes = strongNodes,
            recommendations = recommendations,
            priority = priority
        )
    }

    /**
     * 生成诊断建议
     */
    private fun generateDiagnosisRecommendations(
        course: Course,
        weakNodes: List<KnowledgeNodeDiagnosis>,
        strongNodes: List<String>
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (weakNodes.isNotEmpty()) {
            recommendations.add("「${weakNodes.first().nodeName}」是最需要关注的薄弱点")
        }

        if (strongNodes.isNotEmpty()) {
            recommendations.add("「${strongNodes.first()}」掌握良好，可以尝试更深入的内容")
        }

        when {
            course.masteryLevel < 0.4 -> {
                recommendations.add("建议每天投入至少45分钟进行系统学习")
            }
            course.masteryLevel < 0.7 -> {
                recommendations.add("建议每天投入30分钟进行巩固提升")
            }
            else -> {
                recommendations.add("保持每日复习，防止遗忘")
            }
        }

        return recommendations
    }

    // ==================== 2. 计划阶段 ====================

    /**
     * 创建学习计划
     */
    suspend fun createPlan(diagnosis: DiagnosisResult): LearningPlan {
        val goals = mutableListOf<LearningGoal>()
        val tasks = mutableListOf<PlannedTask>()
        val weeklySchedule = mutableMapOf<Int, MutableList<PlannedTask>>()

        // 基于诊断结果创建目标
        diagnosis.weakKnowledgeNodes.forEachIndexed { index, nodeDiagnosis ->
            val goal = LearningGoal(
                id = "goal_${diagnosis.courseId}_$index",
                description = "掌握「${nodeDiagnosis.nodeName}」",
                targetKnowledgeNodes = listOf(nodeDiagnosis.nodeId),
                targetMastery = 0.7f,
                currentProgress = nodeDiagnosis.masteryLevel
            )
            goals.add(goal)

            // 为每个薄弱点创建任务
            val task = PlannedTask(
                taskId = "task_${nodeDiagnosis.nodeId}",
                title = "${nodeDiagnosis.nodeName} ${if (nodeDiagnosis.masteryLevel < 0.4) "基础学习" else "巩固练习"}",
                type = if (nodeDiagnosis.masteryLevel < 0.4) TaskType.DEEP_EXPLORATION else TaskType.DAILY_CARE,
                estimatedMinutes = if (nodeDiagnosis.masteryLevel < 0.4) 45 else 25,
                relatedNodeIds = listOf(nodeDiagnosis.nodeId)
            )
            tasks.add(task)
        }

        // 计算总时长
        val totalMinutes = tasks.sumOf { it.estimatedMinutes }

        // 安排周计划（简化：均匀分配到每天）
        tasks.forEachIndexed { index, task ->
            val dayOfWeek = (index % 7) + 1
            weeklySchedule.getOrPut(dayOfWeek) { mutableListOf() }.add(task)
        }

        return LearningPlan(
            courseId = diagnosis.courseId,
            courseName = diagnosis.courseName,
            goals = goals,
            tasks = tasks,
            estimatedMinutes = totalMinutes,
            weeklySchedule = weeklySchedule
        ).also {
            _currentPlan.value = it
        }
    }

    /**
     * 创建快速学习计划（基于当前状态）
     */
    suspend fun createQuickPlan(durationMinutes: Int = 60): LearningPlan {
        val tasks = mutableListOf<PlannedTask>()

        // 获取待办任务
        val pendingTasks = dataProvider.getTasks()
            .filter { it.status == TaskStatus.PENDING }
            .sortedByDescending { it.priority }
            .take(5)

        pendingTasks.forEach { task ->
            tasks.add(
                PlannedTask(
                    taskId = task.id,
                    title = task.title,
                    type = task.type,
                    estimatedMinutes = task.estimatedMinutes,
                    relatedNodeIds = task.knowledgeNodeIds
                )
            )
        }

        // 如果任务不够，添加薄弱点任务
        if (tasks.isEmpty() || tasks.sumOf { it.estimatedMinutes } < durationMinutes) {
            val diagnosis = diagnose()
            if (diagnosis.weakKnowledgeNodes.isNotEmpty()) {
                val remainingMinutes = durationMinutes - tasks.sumOf { it.estimatedMinutes }
                var usedMinutes = 0

                diagnosis.weakKnowledgeNodes.forEach { node ->
                    if (usedMinutes < remainingMinutes) {
                        val taskMinutes = minOf(30, remainingMinutes - usedMinutes)
                        tasks.add(
                            PlannedTask(
                                taskId = "quick_task_${node.nodeId}",
                                title = "${node.nodeName} 快速练习",
                                type = TaskType.DAILY_CARE,
                                estimatedMinutes = taskMinutes,
                                relatedNodeIds = listOf(node.nodeId)
                            )
                        )
                        usedMinutes += taskMinutes
                    }
                }
            }
        }

        return LearningPlan(
            courseId = null,
            courseName = null,
            goals = emptyList(),
            tasks = tasks,
            estimatedMinutes = tasks.sumOf { it.estimatedMinutes }
        ).also {
            _currentPlan.value = it
        }
    }

    // ==================== 3. 执行阶段 ====================

    /**
     * 开始学习会话
     */
    fun startSession(planId: String? = null): LearningSession {
        val session = LearningSession(
            planId = planId ?: _currentPlan.value?.id
        )
        _currentSession.value = session
        return session
    }

    /**
     * 记录任务进度
     */
    fun recordProgress(taskId: String, result: TaskResult) {
        val session = _currentSession.value ?: return
        session.completedTasks = session.completedTasks + result
        _currentSession.value = session

        // 更新计划中的任务状态
        _currentPlan.value?.let { plan ->
            plan.tasks.find { it.taskId == taskId }?.let { task ->
                task.status = TaskStatus.COMPLETED
                task.completedAt = result.completedAt
                task.actualMinutes = result.actualMinutes
            }
        }
    }

    /**
     * 记录测验结果
     */
    fun recordQuizResult(result: QuizResult) {
        val session = _currentSession.value ?: return
        session.quizResults = session.quizResults + result
        _currentSession.value = session
    }

    /**
     * 添加 AI 评论
     */
    fun addAIComment(comment: String) {
        val session = _currentSession.value ?: return
        session.aiComments = session.aiComments + comment
        _currentSession.value = session
    }

    /**
     * 结束学习会话
     */
    fun endSession(feedback: SessionFeedback? = null): LearningSession? {
        val session = _currentSession.value ?: return null
        session.endTime = System.currentTimeMillis()
        session.userFeedback = feedback

        // 保存到历史
        _sessionHistory.value = _sessionHistory.value + session
        _currentSession.value = null

        return session
    }

    // ==================== 4. 反馈阶段 ====================

    /**
     * 生成反馈报告
     */
    suspend fun generateFeedback(session: LearningSession): FeedbackReport {
        val tasks = session.completedTasks
        val quizzes = session.quizResults

        // 计算统计数据
        val stats = SessionStatistics(
            totalMinutes = session.durationMinutes,
            tasksCompleted = tasks.size,
            tasksSkipped = _currentPlan.value?.tasks?.count { it.status == TaskStatus.PENDING } ?: 0,
            quizAccuracy = if (quizzes.isNotEmpty()) {
                quizzes.count { it.isCorrect }.toFloat() / quizzes.size
            } else null,
            energyEarned = session.totalEarned.energy,
            crystalsEarned = session.totalEarned.crystals,
            expEarned = session.totalEarned.exp
        )

        // 生成总结
        val summary = generateSummary(session, stats)

        // 识别优势
        val strengths = identifyStrengths(session, stats)

        // 识别改进空间
        val improvements = identifyImprovements(session, stats)

        // 下一步建议
        val nextSteps = generateNextSteps(session, stats)

        // 激励语
        val motivation = generateMotivation(session, stats)

        return FeedbackReport(
            sessionId = session.id,
            summary = summary,
            strengths = strengths,
            areasToImprove = improvements,
            nextSteps = nextSteps,
            motivationalMessage = motivation,
            statistics = stats
        )
    }

    /**
     * 生成会话总结
     */
    private fun generateSummary(session: LearningSession, stats: SessionStatistics): String {
        return buildString {
            append("本次学习共 ${stats.totalMinutes} 分钟，")
            append("完成了 ${stats.tasksCompleted} 个任务")

            if (stats.quizAccuracy != null) {
                val accuracy = (stats.quizAccuracy * 100).toInt()
                append("，测验正确率 $accuracy%")
            }

            append("。获得了 ${stats.energyEarned}⚡ ${stats.crystalsEarned}💎")
        }
    }

    /**
     * 识别优势
     */
    private fun identifyStrengths(session: LearningSession, stats: SessionStatistics): List<String> {
        val strengths = mutableListOf<String>()

        if (stats.totalMinutes >= 45) {
            strengths.add("学习时长充足，专注力很强")
        }

        if (stats.quizAccuracy != null && stats.quizAccuracy >= 0.8f) {
            strengths.add("知识掌握扎实，测验正确率高")
        }

        if (stats.tasksCompleted >= 3) {
            strengths.add("高效完成了多项任务")
        }

        session.userFeedback?.let { feedback ->
            if (feedback.enjoyment >= 4) {
                strengths.add("学习过程愉悦，状态很好")
            }
        }

        return strengths
    }

    /**
     * 识别改进空间
     */
    private fun identifyImprovements(session: LearningSession, stats: SessionStatistics): List<String> {
        val improvements = mutableListOf<String>()

        if (stats.quizAccuracy != null && stats.quizAccuracy < 0.6f) {
            improvements.add("部分知识点理解不够透彻，建议重新学习")
        }

        if (stats.tasksSkipped > 0) {
            improvements.add("有 ${stats.tasksSkipped} 个任务未完成，建议调整计划")
        }

        if (stats.totalMinutes < 20) {
            improvements.add("学习时间较短，建议延长至30分钟以上以加深记忆")
        }

        session.userFeedback?.let { feedback ->
            if (feedback.difficulty >= 4) {
                improvements.add("感觉学习内容有难度，可以尝试分解成更小的知识点")
            }
        }

        return improvements
    }

    /**
     * 生成下一步建议
     */
    private fun generateNextSteps(session: LearningSession, stats: SessionStatistics): List<String> {
        val nextSteps = mutableListOf<String>()

        // 简化：返回通用建议
        nextSteps.add("明天可以继续学习未完成的知识点")

        if (stats.quizAccuracy != null && stats.quizAccuracy < 0.7f) {
            nextSteps.add("建议对错误的知识点进行重点复习")
        }

        nextSteps.add("保持每日学习习惯，积累知识结晶")

        return nextSteps
    }

    /**
     * 生成激励语
     */
    private fun generateMotivation(session: LearningSession, stats: SessionStatistics): String {
        val messages = when {
            stats.tasksCompleted >= 5 -> listOf(
                "太棒了！今天的效率超高！",
                "你是学习小能手！继续保持！",
                "今天收获满满，成就感爆棚！"
            )
            stats.totalMinutes >= 60 -> listOf(
                "专注学习一小时，为你的坚持点赞！",
                "持续努力终会开花结果！",
                "每一分钟都是进步的阶梯！"
            )
            stats.quizAccuracy != null && stats.quizAccuracy >= 0.8f -> listOf(
                "知识掌握得很扎实，继续保持！",
                "理解力超棒，越学越顺！",
                "正确率很高，你已经入门了！"
            )
            else -> listOf(
                "每一步都是进步，继续加油！",
                "坚持学习就是最大的胜利！",
                "明天继续努力，一定会越来越好！"
            )
        }

        return messages.random()
    }

    // ==================== 5. 调整阶段 ====================

    /**
     * 基于反馈调整计划
     */
    suspend fun adjustPlan(feedback: FeedbackReport): LearningPlan? {
        val currentPlan = _currentPlan.value ?: return null

        // 分析反馈，调整后续计划
        val adjustments = mutableListOf<String>()

        // 如果正确率低，增加复习任务
        if (feedback.statistics.quizAccuracy != null && feedback.statistics.quizAccuracy < 0.7f) {
            adjustments.add("增加复习任务")
        }

        // 如果学习时间短，调整任务时长
        if (feedback.statistics.totalMinutes < 30) {
            adjustments.add("缩短单个任务时长，增加任务数量")
        }

        // 如果难度高，分解知识点
        if (feedback.areasToImprove.any { it.contains("难度") || it.contains("分解") }) {
            adjustments.add("将大任务分解为小任务")
        }

        // 创建调整后的计划
        return currentPlan.copy(
            tasks = currentPlan.tasks.map { task ->
                if (feedback.areasToImprove.any { it.contains("复习") && task.relatedNodeIds.isNotEmpty() }) {
                    task.copy(type = TaskType.REVIEW_RITUAL)
                } else {
                    task
                }
            }
        ).also {
            _currentPlan.value = it
        }
    }

    /**
     * 获取学习建议
     */
    suspend fun getLearningAdvice(): String {
        val diagnosis = diagnose()
        val profile = profileProvider.generateReport()

        return buildString {
            appendLine("📚 学习建议")
            appendLine()

            if (diagnosis.priority == DiagnosisPriority.URGENT) {
                appendLine("⚠️ 当前有紧急需要关注的知识点")
            }

            if (diagnosis.weakKnowledgeNodes.isNotEmpty()) {
                appendLine("🎯 建议优先攻克：${diagnosis.weakKnowledgeNodes.first().nodeName}")
            }

            val style = profile.style
            appendLine("📖 学习风格：${style.displayName} - ${style.description}")

            if (profile.peakHours.isNotEmpty()) {
                val bestHour = profile.peakHours.first().hour
                appendLine("⏰ 黄金时段：$bestHour:00 左右")
            }

            appendLine()
            appendLine(diagnosis.recommendations.firstOrNull() ?: "保持每日学习习惯")
        }.trimEnd()
    }
}