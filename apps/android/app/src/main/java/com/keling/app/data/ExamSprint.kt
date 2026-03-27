package com.keling.app.data

/**
 * =========================
 * 考试冲刺模式
 * =========================
 *
 * 考试倒计时与冲刺规划系统
 * - 考试日程管理
 * - 冲刺计划生成
 * - 阶段划分与追踪
 * - 每日复习任务
 * - AI预测分数
 */

import kotlinx.serialization.Serializable
import java.util.Calendar

// ==================== 考试模型 ====================

/**
 * 考试信息
 */
@Serializable
data class Exam(
    val id: String,
    val courseId: String,
    val courseName: String,
    val examName: String,
    val examDate: Long,
    val examType: ExamType = ExamType.FINAL,
    val totalScore: Int = 100,
    val targetScore: Int? = null,           // 目标分数
    val currentMastery: Float = 0f,         // 当前掌握度
    val predictedScore: Float? = null,      // AI预测分数
    val preparationDays: Int = 0,           // 准备天数
    val dailyStudyHours: Float = 2f,        // 每日学习时长
    val status: ExamStatus = ExamStatus.UPCOMING,
    val sprintPlanId: String? = null,       // 关联的冲刺计划
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 考试类型
 */
enum class ExamType(val displayName: String) {
    QUIZ("随堂测验"),
    MIDTERM("期中考试"),
    FINAL("期末考试"),
    ENTRANCE("入学考试"),
    CERTIFICATE("资格考试"),
    INTERVIEW("面试"),
    OTHER("其他")
}

/**
 * 考试状态
 */
enum class ExamStatus(val displayName: String) {
    UPCOMING("即将到来"),
    PREPARING("准备中"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消")
}

/**
 * 冲刺计划
 */
@Serializable
data class SprintPlan(
    val id: String,
    val examId: String,
    val courseId: String,
    val courseName: String,
    val examDate: Long,
    val totalDays: Int,
    val remainingDays: Int,
    val phases: List<SprintPhase>,
    val dailyTasks: List<DailySprintTask>,
    val predictedScore: Float,
    val confidenceLevel: Float,
    val riskAreas: List<RiskArea>,
    val studyTips: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 冲刺阶段
 */
@Serializable
data class SprintPhase(
    val phase: Int,
    val name: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val durationDays: Int,
    val dailyHours: Float,
    val focusTopics: List<String>,
    val targetMastery: Float,
    val tasks: List<String>,
    val isCompleted: Boolean = false,
    val progress: Float = 0f
)

/**
 * 每日冲刺任务
 */
@Serializable
data class DailySprintTask(
    val date: String,                        // "2024-01-15"
    val dayNumber: Int,                      // 第几天
    val phase: Int,                          // 第几阶段
    val tasks: List<SprintTaskItem>,
    val totalMinutes: Int,
    val completedMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val notes: String? = null
)

/**
 * 冲刺任务项
 */
@Serializable
data class SprintTaskItem(
    val id: String,
    val title: String,
    val description: String,
    val type: SprintTaskType,
    val estimatedMinutes: Int,
    val actualMinutes: Int? = null,
    val nodeId: String? = null,             // 关联知识点
    val isCompleted: Boolean = false,
    val priority: Int = 3
)

enum class SprintTaskType {
    REVIEW,        // 复习知识点
    PRACTICE,      // 刷题练习
    MOCK_EXAM,     // 模拟考试
    NOTE_REVIEW,   // 笔记复习
    WEAK_POINT,    // 薄弱点攻克
    MEMORIZATION   // 背诵记忆
}

/**
 * 风险领域
 */
@Serializable
data class RiskArea(
    val nodeId: String,
    val nodeName: String,
    val currentMastery: Float,
    val requiredMastery: Float,
    val riskLevel: RiskLevel,
    val recommendation: String
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// ==================== 冲刺计划生成器 ====================

/**
 * 冲刺计划生成器
 */
object SprintPlanGenerator {

    /**
     * 生成冲刺计划
     */
    fun generatePlan(
        exam: Exam,
        knowledgeNodes: List<KnowledgeNode>,
        studyRecords: List<StudyRecord>
    ): SprintPlan {
        val now = System.currentTimeMillis()
        val remainingDays = ((exam.examDate - now) / (24 * 60 * 60 * 1000)).toInt()
            .coerceAtLeast(1)

        // 生成阶段
        val phases = generatePhases(exam, remainingDays)

        // 生成每日任务
        val dailyTasks = generateDailyTasks(exam, phases, knowledgeNodes)

        // 预测分数
        val prediction = predictScore(exam, knowledgeNodes)

        // 识别风险领域
        val riskAreas = identifyRiskAreas(knowledgeNodes, exam.targetScore)

        // 生成学习建议
        val tips = generateStudyTips(remainingDays, riskAreas)

        return SprintPlan(
            id = "sprint_${exam.id}_${System.currentTimeMillis()}",
            examId = exam.id,
            courseId = exam.courseId,
            courseName = exam.courseName,
            examDate = exam.examDate,
            totalDays = remainingDays,
            remainingDays = remainingDays,
            phases = phases,
            dailyTasks = dailyTasks,
            predictedScore = prediction.score,
            confidenceLevel = prediction.confidence,
            riskAreas = riskAreas,
            studyTips = tips
        )
    }

    /**
     * 生成阶段划分
     */
    private fun generatePhases(exam: Exam, totalDays: Int): List<SprintPhase> {
        val phases = mutableListOf<SprintPhase>()
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        when {
            totalDays <= 3 -> {
                // 短期冲刺（3天内）
                phases.add(
                    SprintPhase(
                        phase = 1,
                        name = "集中冲刺",
                        description = "重点攻克核心知识点",
                        startDate = now,
                        endDate = exam.examDate,
                        durationDays = totalDays,
                        dailyHours = 4f,
                        focusTopics = listOf("核心知识点", "高频考点"),
                        targetMastery = 0.7f,
                        tasks = listOf("复习重点", "刷题", "背诵")
                    )
                )
            }
            totalDays <= 7 -> {
                // 一周冲刺
                phases.add(
                    SprintPhase(
                        phase = 1,
                        name = "知识梳理",
                        description = "系统复习所有知识点",
                        startDate = now,
                        endDate = now + 3 * dayInMillis,
                        durationDays = 3,
                        dailyHours = 3f,
                        focusTopics = listOf("全面复习"),
                        targetMastery = 0.6f,
                        tasks = listOf("知识点复习", "笔记整理")
                    )
                )
                phases.add(
                    SprintPhase(
                        phase = 2,
                        name = "重点突破",
                        description = "攻克薄弱知识点",
                        startDate = now + 3 * dayInMillis,
                        endDate = now + 5 * dayInMillis,
                        durationDays = 2,
                        dailyHours = 3.5f,
                        focusTopics = listOf("薄弱点", "难点"),
                        targetMastery = 0.75f,
                        tasks = listOf("专项练习", "错题分析")
                    )
                )
                phases.add(
                    SprintPhase(
                        phase = 3,
                        name = "考前冲刺",
                        description = "模拟考试与最后冲刺",
                        startDate = now + 5 * dayInMillis,
                        endDate = exam.examDate,
                        durationDays = 2,
                        dailyHours = 4f,
                        focusTopics = listOf("模拟测试", "背诵"),
                        targetMastery = 0.8f,
                        tasks = listOf("模拟考", "查漏补缺")
                    )
                )
            }
            else -> {
                // 长期准备（超过一周）
                val reviewDays = (totalDays * 0.3).toInt()
                val practiceDays = (totalDays * 0.4).toInt()
                val sprintDays = totalDays - reviewDays - practiceDays

                // 阶段1：基础复习
                phases.add(
                    SprintPhase(
                        phase = 1,
                        name = "基础巩固",
                        description = "系统复习所有知识点",
                        startDate = now,
                        endDate = now + reviewDays * dayInMillis,
                        durationDays = reviewDays,
                        dailyHours = 2f,
                        focusTopics = listOf("全部知识点"),
                        targetMastery = 0.5f,
                        tasks = listOf("章节复习", "笔记整理")
                    )
                )

                // 阶段2：专项练习
                phases.add(
                    SprintPhase(
                        phase = 2,
                        name = "专项提升",
                        description = "重点练习薄弱环节",
                        startDate = now + reviewDays * dayInMillis,
                        endDate = now + (reviewDays + practiceDays) * dayInMillis,
                        durationDays = practiceDays,
                        dailyHours = 2.5f,
                        focusTopics = listOf("薄弱点", "重难点"),
                        targetMastery = 0.7f,
                        tasks = listOf("专项练习", "错题攻克")
                    )
                )

                // 阶段3：冲刺
                phases.add(
                    SprintPhase(
                        phase = 3,
                        name = "考前冲刺",
                        description = "模拟考试与最后复习",
                        startDate = now + (reviewDays + practiceDays) * dayInMillis,
                        endDate = exam.examDate,
                        durationDays = sprintDays,
                        dailyHours = 3f,
                        focusTopics = listOf("模拟测试", "查漏补缺"),
                        targetMastery = 0.85f,
                        tasks = listOf("模拟考", "重点背诵", "错题复习")
                    )
                )
            }
        }

        return phases
    }

    /**
     * 生成每日任务
     */
    private fun generateDailyTasks(
        exam: Exam,
        phases: List<SprintPhase>,
        knowledgeNodes: List<KnowledgeNode>
    ): List<DailySprintTask> {
        val tasks = mutableListOf<DailySprintTask>()
        val dayInMillis = 24 * 60 * 60 * 1000L
        val cal = Calendar.getInstance()

        var dayNum = 1
        for (phase in phases) {
            var current = phase.startDate
            while (current < phase.endDate) {
                cal.timeInMillis = current
                val dateStr = String.format(
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                // 为每天生成任务
                val dailyTaskItems = generateDailyTaskItems(
                    phase = phase,
                    dayNumber = dayNum,
                    knowledgeNodes = knowledgeNodes
                )

                tasks.add(
                    DailySprintTask(
                        date = dateStr,
                        dayNumber = dayNum,
                        phase = phase.phase,
                        tasks = dailyTaskItems,
                        totalMinutes = (phase.dailyHours * 60).toInt()
                    )
                )

                current += dayInMillis
                dayNum++
            }
        }

        return tasks
    }

    /**
     * 生成每日任务项
     */
    private fun generateDailyTaskItems(
        phase: SprintPhase,
        dayNumber: Int,
        knowledgeNodes: List<KnowledgeNode>
    ): List<SprintTaskItem> {
        val items = mutableListOf<SprintTaskItem>()
        val totalMinutes = (phase.dailyHours * 60).toInt()

        when (phase.phase) {
            1 -> {
                // 基础复习阶段
                items.add(
                    SprintTaskItem(
                        id = "task_${phase.phase}_$dayNumber",
                        title = "知识点复习",
                        description = "复习今日知识点",
                        type = SprintTaskType.REVIEW,
                        estimatedMinutes = (totalMinutes * 0.5).toInt(),
                        priority = 5
                    )
                )
                items.add(
                    SprintTaskItem(
                        id = "practice_${phase.phase}_$dayNumber",
                        title = "基础练习",
                        description = "完成基础练习题",
                        type = SprintTaskType.PRACTICE,
                        estimatedMinutes = (totalMinutes * 0.3).toInt(),
                        priority = 4
                    )
                )
                items.add(
                    SprintTaskItem(
                        id = "note_${phase.phase}_$dayNumber",
                        title = "笔记整理",
                        description = "整理复习笔记",
                        type = SprintTaskType.NOTE_REVIEW,
                        estimatedMinutes = (totalMinutes * 0.2).toInt(),
                        priority = 3
                    )
                )
            }
            2 -> {
                // 专项提升阶段
                items.add(
                    SprintTaskItem(
                        id = "weak_${phase.phase}_$dayNumber",
                        title = "薄弱点攻克",
                        description = "针对薄弱知识点专项练习",
                        type = SprintTaskType.WEAK_POINT,
                        estimatedMinutes = (totalMinutes * 0.4).toInt(),
                        priority = 5
                    )
                )
                items.add(
                    SprintTaskItem(
                        id = "practice_${phase.phase}_$dayNumber",
                        title = "强化练习",
                        description = "完成强化练习题",
                        type = SprintTaskType.PRACTICE,
                        estimatedMinutes = (totalMinutes * 0.4).toInt(),
                        priority = 4
                    )
                )
                items.add(
                    SprintTaskItem(
                        id = "review_${phase.phase}_$dayNumber",
                        title = "错题复习",
                        description = "复习错题本",
                        type = SprintTaskType.NOTE_REVIEW,
                        estimatedMinutes = (totalMinutes * 0.2).toInt(),
                        priority = 3
                    )
                )
            }
            else -> {
                // 冲刺阶段
                items.add(
                    SprintTaskItem(
                        id = "mock_${phase.phase}_$dayNumber",
                        title = "模拟测试",
                        description = "完成模拟试卷",
                        type = SprintTaskType.MOCK_EXAM,
                        estimatedMinutes = 90,
                        priority = 5
                    )
                )
                items.add(
                    SprintTaskItem(
                        id = "analyze_${phase.phase}_$dayNumber",
                        title = "试卷分析",
                        description = "分析模拟考错误",
                        type = SprintTaskType.REVIEW,
                        estimatedMinutes = 30,
                        priority = 4
                    )
                )
                items.add(
                    SprintTaskItem(
                        id = "memo_${phase.phase}_$dayNumber",
                        title = "重点背诵",
                        description = "背诵重要内容",
                        type = SprintTaskType.MEMORIZATION,
                        estimatedMinutes = 30,
                        priority = 4
                    )
                )
            }
        }

        return items
    }

    /**
     * 预测分数
     */
    private fun predictScore(
        exam: Exam,
        knowledgeNodes: List<KnowledgeNode>
    ): ScorePrediction {
        if (knowledgeNodes.isEmpty()) {
            return ScorePrediction(
                score = exam.currentMastery * exam.totalScore,
                confidence = 0.3f
            )
        }

        // 基于知识节点掌握度预测
        val avgMastery = knowledgeNodes.map { it.masteryLevel }.average().toFloat()
        val weightedMastery = if (knowledgeNodes.any { it.difficulty >= 4 }) {
            // 高难度知识点加权
            val weightedSum = knowledgeNodes.map { node ->
                val weight = if (node.difficulty >= 4) 1.5f else 1f
                node.masteryLevel * weight
            }.sum()
            val totalWeight = knowledgeNodes.map { node ->
                if (node.difficulty >= 4) 1.5f else 1f
            }.sum()
            weightedSum / totalWeight
        } else {
            avgMastery
        }

        val predictedScore = weightedMastery * exam.totalScore

        // 置信度基于数据完整度
        val unlockedRatio = knowledgeNodes.count { it.isUnlocked }.toFloat() / knowledgeNodes.size
        val confidence = unlockedRatio * 0.7f + 0.3f

        return ScorePrediction(
            score = predictedScore.coerceIn(0f, exam.totalScore.toFloat()),
            confidence = confidence.coerceIn(0.3f, 0.9f)
        )
    }

    /**
     * 识别风险领域
     */
    private fun identifyRiskAreas(
        knowledgeNodes: List<KnowledgeNode>,
        targetScore: Int?
    ): List<RiskArea> {
        if (knowledgeNodes.isEmpty()) return emptyList()

        val targetMastery = (targetScore?.toFloat() ?: 60f) / 100f

        return knowledgeNodes
            .filter { it.masteryLevel < targetMastery }
            .sortedBy { it.masteryLevel }
            .take(5)
            .map { node ->
                val gap = targetMastery - node.masteryLevel
                RiskArea(
                    nodeId = node.id,
                    nodeName = node.name,
                    currentMastery = node.masteryLevel,
                    requiredMastery = targetMastery,
                    riskLevel = when {
                        gap > 0.5f -> RiskLevel.CRITICAL
                        gap > 0.3f -> RiskLevel.HIGH
                        gap > 0.15f -> RiskLevel.MEDIUM
                        else -> RiskLevel.LOW
                    },
                    recommendation = when {
                        gap > 0.5f -> "需要重点攻克，建议每天专项练习"
                        gap > 0.3f -> "需要加强练习，多做题巩固"
                        gap > 0.15f -> "需要适当复习，保持状态"
                        else -> "基本掌握，考前复习即可"
                    }
                )
            }
    }

    /**
     * 生成学习建议
     */
    private fun generateStudyTips(
        remainingDays: Int,
        riskAreas: List<RiskArea>
    ): List<String> {
        val tips = mutableListOf<String>()

        when {
            remainingDays <= 3 -> {
                tips.add("时间紧迫，集中精力攻克最重要知识点")
                tips.add("保证睡眠，避免熬夜影响考试状态")
                tips.add("每天至少做一套模拟题保持手感")
            }
            remainingDays <= 7 -> {
                tips.add("制定每日学习计划，严格执行")
                tips.add("错题本是最好的复习资料")
                tips.add("劳逸结合，适当运动放松")
            }
            else -> {
                tips.add("系统复习，打好基础")
                tips.add("定期自测，及时发现薄弱点")
                tips.add("保持学习节奏，不要拖延")
            }
        }

        // 根据风险领域添加建议
        if (riskAreas.any { it.riskLevel == RiskLevel.CRITICAL }) {
            tips.add("有严重薄弱点，建议优先攻克")
        }

        return tips
    }
}

/**
 * 分数预测结果
 */
data class ScorePrediction(
    val score: Float,
    val confidence: Float
)

// ==================== 考试管理器 ====================

/**
 * 考试管理器
 */
class ExamManager {
    private val exams = mutableListOf<Exam>()
    private val sprintPlans = mutableMapOf<String, SprintPlan>()

    /**
     * 添加考试
     */
    fun addExam(
        courseId: String,
        courseName: String,
        examName: String,
        examDate: Long,
        examType: ExamType = ExamType.FINAL,
        targetScore: Int? = null,
        currentMastery: Float = 0f
    ): Exam {
        val exam = Exam(
            id = "exam_${System.currentTimeMillis()}_${(0..9999).random()}",
            courseId = courseId,
            courseName = courseName,
            examName = examName,
            examDate = examDate,
            examType = examType,
            targetScore = targetScore,
            currentMastery = currentMastery,
            preparationDays = ((examDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
        )
        exams.add(exam)
        return exam
    }

    /**
     * 生成冲刺计划
     */
    fun generateSprintPlan(
        examId: String,
        knowledgeNodes: List<KnowledgeNode>,
        studyRecords: List<StudyRecord>
    ): SprintPlan? {
        val exam = exams.find { it.id == examId } ?: return null

        val plan = SprintPlanGenerator.generatePlan(exam, knowledgeNodes, studyRecords)
        sprintPlans[examId] = plan

        // 更新考试状态
        val index = exams.indexOf(exam)
        if (index >= 0) {
            exams[index] = exam.copy(
                status = ExamStatus.PREPARING,
                sprintPlanId = plan.id
            )
        }

        return plan
    }

    /**
     * 获取即将到来的考试
     */
    fun getUpcomingExams(): List<Exam> {
        val now = System.currentTimeMillis()
        return exams.filter {
            it.examDate > now && it.status != ExamStatus.COMPLETED && it.status != ExamStatus.CANCELLED
        }.sortedBy { it.examDate }
    }

    /**
     * 获取今日冲刺任务
     */
    fun getTodaySprintTasks(): List<DailySprintTask> {
        val today = String.format(
            "%04d-%02d-%02d",
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH) + 1,
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        return sprintPlans.values.mapNotNull { plan ->
            plan.dailyTasks.find { it.date == today }
        }
    }

    /**
     * 完成冲刺任务
     */
    fun completeSprintTask(
        examId: String,
        date: String,
        taskId: String
    ): Boolean {
        val plan = sprintPlans[examId] ?: return false
        val dayTask = plan.dailyTasks.find { it.date == date } ?: return false
        val taskIndex = dayTask.tasks.indexOfFirst { it.id == taskId }
        if (taskIndex < 0) return false

        // 更新任务状态
        val updatedTasks = dayTask.tasks.toMutableList()
        updatedTasks[taskIndex] = updatedTasks[taskIndex].copy(isCompleted = true)

        val completedMinutes = updatedTasks.filter { it.isCompleted }.sumOf { it.estimatedMinutes }
        val isCompleted = completedMinutes >= dayTask.totalMinutes * 0.8

        val updatedDayTask = dayTask.copy(
            tasks = updatedTasks,
            completedMinutes = completedMinutes,
            isCompleted = isCompleted
        )

        // 更新计划
        val dayIndex = plan.dailyTasks.indexOf(dayTask)
        val updatedDailyTasks = plan.dailyTasks.toMutableList()
        updatedDailyTasks[dayIndex] = updatedDayTask

        sprintPlans[examId] = plan.copy(
            dailyTasks = updatedDailyTasks,
            updatedAt = System.currentTimeMillis()
        )

        return true
    }

    /**
     * 更新考试结果
     */
    fun recordExamResult(
        examId: String,
        actualScore: Int
    ): Exam? {
        val exam = exams.find { it.id == examId } ?: return null
        val index = exams.indexOf(exam)

        val updated = exam.copy(
            status = ExamStatus.COMPLETED,
            predictedScore = exam.predictedScore ?: exam.currentMastery * 100
        )

        exams[index] = updated
        return updated
    }

    /**
     * 获取考试统计
     */
    fun getExamStats(): ExamStats {
        val completed = exams.filter { it.status == ExamStatus.COMPLETED }
        val upcoming = getUpcomingExams()

        return ExamStats(
            totalExams = exams.size,
            completedExams = completed.size,
            upcomingExams = upcoming.size,
            averageScore = if (completed.isNotEmpty()) {
                completed.mapNotNull { it.predictedScore }.average().toFloat()
            } else 0f,
            nearestExam = upcoming.firstOrNull()
        )
    }
}

/**
 * 考试统计
 */
data class ExamStats(
    val totalExams: Int,
    val completedExams: Int,
    val upcomingExams: Int,
    val averageScore: Float,
    val nearestExam: Exam?
)