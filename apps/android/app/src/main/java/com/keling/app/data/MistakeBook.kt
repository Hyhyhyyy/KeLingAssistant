package com.keling.app.data

/**
 * =========================
 * 错题本系统
 * =========================
 *
 * 智能错题管理与复习系统
 * - 错题记录与管理
 * - AI错误分析与解题思路
 * - 相似题推荐
 * - 错题复习追踪
 * - 掌握度评估
 */

import kotlinx.serialization.Serializable

// ==================== 错题模型 ====================

/**
 * 错题记录
 */
@Serializable
data class MistakeRecord(
    val id: String,
    val userId: String,
    val courseId: String,
    val courseName: String,
    val nodeId: String?,                    // 关联的知识点
    val nodeName: String?,
    val question: String,                   // 题目内容
    val questionType: QuestionType = QuestionType.UNKNOWN,
    val userAnswer: String,                 // 用户的错误答案
    val correctAnswer: String,              // 正确答案
    val mistakeType: MistakeType,           // 错误类型
    val difficulty: Int = 3,                // 难度 1-5
    val aiAnalysis: String? = null,         // AI分析错误原因
    val aiSolution: String? = null,         // AI解题思路
    val similarQuestions: List<String> = emptyList(), // AI推荐的相似题
    val tags: List<String> = emptyList(),   // 标签
    val source: MistakeSource = MistakeSource.MANUAL, // 来源
    val reviewCount: Int = 0,               // 复习次数
    val correctReviewCount: Int = 0,        // 正确复习次数
    val lastReviewAt: Long? = null,         // 最后复习时间
    val nextReviewAt: Long? = null,         // 下次复习时间
    val masteryLevel: Float = 0f,           // 掌握度 0-1
    val isMastered: Boolean = false,        // 是否已掌握
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 题目类型
 */
enum class QuestionType {
    SINGLE_CHOICE,    // 单选题
    MULTI_CHOICE,     // 多选题
    FILL_BLANK,       // 填空题
    TRUE_FALSE,       // 判断题
    SHORT_ANSWER,     // 简答题
    CALCULATION,      // 计算题
    PROOF,            // 证明题
    ESSAY,            // 论述题
    UNKNOWN           // 未知类型
}

/**
 * 错误类型
 */
enum class MistakeType(val displayName: String, val description: String) {
    CONCEPT("概念理解错误", "对基本概念的理解出现偏差"),
    CALCULATION("计算错误", "计算过程中出现错误"),
    CARELESS("粗心大意", "因马虎导致的错误"),
    METHOD("方法选择错误", "解题方法或思路选择不当"),
    KNOWLEDGE("知识点遗忘", "忘记了相关知识点"),
    LOGIC("逻辑推理错误", "推理过程中出现逻辑漏洞"),
    INCOMPLETE("答案不完整", "答案缺失关键内容"),
    MISUNDERSTAND("审题错误", "对题目理解有误")
}

/**
 * 错题来源
 */
enum class MistakeSource {
    MANUAL,           // 手动添加
    AI_PRACTICE,      // AI练习
    EXAM,             // 考试
    HOMEWORK,         // 作业
    IMPORT            // 导入
}

/**
 * 错题复习记录
 */
@Serializable
data class MistakeReviewRecord(
    val id: String,
    val mistakeId: String,
    val userId: String,
    val reviewedAt: Long,
    val userAnswer: String,
    val isCorrect: Boolean,
    val responseTime: Int,                  // 回答时间(秒)
    val difficultyFelt: Int,                // 感觉难度 1-5
    val notes: String? = null               // 复习笔记
)

// ==================== 错题本管理器 ====================

/**
 * 错题本管理器
 */
class MistakeBookManager {
    private val mistakes = mutableListOf<MistakeRecord>()
    private val reviewRecords = mutableListOf<MistakeReviewRecord>()

    /**
     * 添加错题
     */
    fun addMistake(
        userId: String,
        courseId: String,
        courseName: String,
        question: String,
        userAnswer: String,
        correctAnswer: String,
        mistakeType: MistakeType,
        nodeId: String? = null,
        nodeName: String? = null,
        difficulty: Int = 3,
        questionType: QuestionType = QuestionType.UNKNOWN
    ): MistakeRecord {
        val mistake = MistakeRecord(
            id = "mistake_${System.currentTimeMillis()}_${(0..9999).random()}",
            userId = userId,
            courseId = courseId,
            courseName = courseName,
            nodeId = nodeId,
            nodeName = nodeName,
            question = question,
            questionType = questionType,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            mistakeType = mistakeType,
            difficulty = difficulty,
            source = MistakeSource.MANUAL
        )
        mistakes.add(mistake)
        return mistake
    }

    /**
     * 更新错题（添加AI分析）
     */
    fun updateMistakeWithAI(
        mistakeId: String,
        aiAnalysis: String,
        aiSolution: String,
        similarQuestions: List<String>
    ): MistakeRecord? {
        val index = mistakes.indexOfFirst { it.id == mistakeId }
        if (index < 0) return null

        val updated = mistakes[index].copy(
            aiAnalysis = aiAnalysis,
            aiSolution = aiSolution,
            similarQuestions = similarQuestions
        )
        mistakes[index] = updated
        return updated
    }

    /**
     * 获取用户的所有错题
     */
    fun getUserMistakes(userId: String): List<MistakeRecord> {
        return mistakes.filter { it.userId == userId }
    }

    /**
     * 获取课程的错题
     */
    fun getCourseMistakes(courseId: String): List<MistakeRecord> {
        return mistakes.filter { it.courseId == courseId }
    }

    /**
     * 获取知识点的错题
     */
    fun getNodeMistakes(nodeId: String): List<MistakeRecord> {
        return mistakes.filter { it.nodeId == nodeId }
    }

    /**
     * 获取需要复习的错题
     */
    fun getMistakesForReview(userId: String): List<MistakeRecord> {
        val now = System.currentTimeMillis()
        return mistakes.filter {
            it.userId == userId &&
            !it.isMastered &&
            (it.nextReviewAt == null || it.nextReviewAt <= now)
        }.sortedWith(
            compareBy<MistakeRecord> {
                // 错误次数多的优先
                -it.reviewCount
            }.thenBy {
                // 掌握度低的优先
                it.masteryLevel
            }.thenBy {
                // 难度高的优先
                -it.difficulty
            }
        )
    }

    /**
     * 记录复习
     */
    fun recordReview(
        mistakeId: String,
        userId: String,
        userAnswer: String,
        isCorrect: Boolean,
        responseTime: Int,
        difficultyFelt: Int = 3,
        notes: String? = null
    ): MistakeRecord? {
        val mistake = mistakes.find { it.id == mistakeId } ?: return null
        val now = System.currentTimeMillis()

        // 记录复习历史
        val record = MistakeReviewRecord(
            id = "review_${System.currentTimeMillis()}",
            mistakeId = mistakeId,
            userId = userId,
            reviewedAt = now,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            responseTime = responseTime,
            difficultyFelt = difficultyFelt,
            notes = notes
        )
        reviewRecords.add(record)

        // 更新错题状态
        val newReviewCount = mistake.reviewCount + 1
        val newCorrectCount = if (isCorrect) mistake.correctReviewCount + 1 else mistake.correctReviewCount
        val newMasteryLevel = calculateMasteryLevel(newReviewCount, newCorrectCount)
        val isMastered = newMasteryLevel >= 0.8f && newCorrectCount >= 3

        // 计算下次复习时间
        val nextReview = if (isMastered) {
            null  // 已掌握，不再安排复习
        } else {
            calculateNextReviewTime(mistake, isCorrect, newReviewCount)
        }

        val updated = mistake.copy(
            reviewCount = newReviewCount,
            correctReviewCount = newCorrectCount,
            lastReviewAt = now,
            nextReviewAt = nextReview,
            masteryLevel = newMasteryLevel,
            isMastered = isMastered
        )

        val index = mistakes.indexOf(mistake)
        if (index >= 0) {
            mistakes[index] = updated
        }

        return updated
    }

    /**
     * 计算掌握度
     */
    private fun calculateMasteryLevel(reviewCount: Int, correctCount: Int): Float {
        if (reviewCount == 0) return 0f
        val baseRate = correctCount.toFloat() / reviewCount

        // 考虑复习次数的加成
        val countBonus = minOf(reviewCount * 0.05f, 0.2f)

        return (baseRate + countBonus).coerceIn(0f, 1f)
    }

    /**
     * 计算下次复习时间
     */
    private fun calculateNextReviewTime(
        mistake: MistakeRecord,
        isCorrect: Boolean,
        reviewCount: Int
    ): Long {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val baseInterval = when {
            !isCorrect -> 1  // 错误，1天后复习
            reviewCount == 1 -> 2  // 第一次正确，2天后
            reviewCount == 2 -> 4  // 第二次正确，4天后
            reviewCount == 3 -> 7  // 第三次正确，7天后
            else -> 14  // 之后14天
        }

        // 根据难度调整
        val adjustedInterval = when (mistake.difficulty) {
            5 -> (baseInterval * 0.7).toInt()
            4 -> (baseInterval * 0.85).toInt()
            2 -> (baseInterval * 1.15).toInt()
            1 -> (baseInterval * 1.3).toInt()
            else -> baseInterval
        }

        return now + adjustedInterval * dayInMillis
    }

    /**
     * 删除错题
     */
    fun deleteMistake(mistakeId: String): Boolean {
        return mistakes.removeIf { it.id == mistakeId }
    }

    /**
     * 获取错题统计
     */
    fun getMistakeStats(userId: String): MistakeStats {
        val userMistakes = mistakes.filter { it.userId == userId }
        val userReviews = reviewRecords.filter { it.userId == userId }

        return MistakeStats(
            totalMistakes = userMistakes.size,
            masteredMistakes = userMistakes.count { it.isMastered },
            pendingReview = userMistakes.count { !it.isMastered && (it.nextReviewAt == null || it.nextReviewAt <= System.currentTimeMillis()) },
            totalReviews = userReviews.size,
            correctRate = if (userReviews.isNotEmpty()) {
                userReviews.count { it.isCorrect }.toFloat() / userReviews.size
            } else 0f,
            byType = userMistakes.groupingBy { it.mistakeType }.eachCount(),
            byCourse = userMistakes.groupingBy { it.courseName }.eachCount(),
            averageMastery = if (userMistakes.isNotEmpty()) {
                userMistakes.map { it.masteryLevel }.average().toFloat()
            } else 0f
        )
    }

    /**
     * 清除所有数据
     */
    fun clearAll() {
        mistakes.clear()
        reviewRecords.clear()
    }
}

/**
 * 错题统计
 */
data class MistakeStats(
    val totalMistakes: Int,
    val masteredMistakes: Int,
    val pendingReview: Int,
    val totalReviews: Int,
    val correctRate: Float,
    val byType: Map<MistakeType, Int>,
    val byCourse: Map<String, Int>,
    val averageMastery: Float
)

// ==================== AI错题分析 ====================

/**
 * AI错题分析器
 * 为错题生成AI分析和建议
 */
object AIMistakeAnalyzer {

    /**
     * 分析错题
     */
    fun analyze(
        mistake: MistakeRecord,
        knowledgeContext: String? = null
    ): MistakeAnalysisResult {
        // 这里应该调用AI服务，目前使用规则生成

        val analysis = generateAnalysis(mistake)
        val solution = generateSolution(mistake)
        val similarQuestions = generateSimilarQuestions(mistake)
        val suggestions = generateSuggestions(mistake)

        return MistakeAnalysisResult(
            mistakeId = mistake.id,
            analysis = analysis,
            solution = solution,
            similarQuestions = similarQuestions,
            suggestions = suggestions,
            relatedKnowledge = knowledgeContext
        )
    }

    private fun generateAnalysis(mistake: MistakeRecord): String {
        return buildString {
            append("【错误分析】\n\n")
            append("错误类型：${mistake.mistakeType.displayName}\n")
            append("${mistake.mistakeType.description}\n\n")

            when (mistake.mistakeType) {
                MistakeType.CONCEPT -> {
                    append("这类错误通常源于对概念理解不够深入。\n")
                    append("建议：重新阅读教材相关章节，用自己的话复述概念。\n")
                }
                MistakeType.CALCULATION -> {
                    append("计算错误可能是由于：\n")
                    append("• 计算过程跳步太多\n")
                    append("• 符号处理不当\n")
                    append("• 时间紧张导致匆忙\n")
                    append("建议：放慢速度，写出完整步骤，养成检查习惯。\n")
                }
                MistakeType.CARELESS -> {
                    append("粗心错误最可惜，但也最容易改正。\n")
                    append("建议：\n")
                    append("• 审题时划出关键词\n")
                    append("• 计算完检查一遍\n")
                    append("• 不要过分追求速度\n")
                }
                MistakeType.METHOD -> {
                    append("方法选择错误说明对题型的判断还需要加强。\n")
                    append("建议：多做同类题，总结解题模式。\n")
                }
                MistakeType.KNOWLEDGE -> {
                    append("知识点遗忘是很正常的。\n")
                    append("建议：使用间隔复习法，定期回顾。\n")
                }
                MistakeType.LOGIC -> {
                    append("逻辑推理需要严密的思维训练。\n")
                    append("建议：画出推理链条，检查每一步。\n")
                }
                MistakeType.INCOMPLETE -> {
                    append("答案不完整可能是时间分配或审题问题。\n")
                    append("建议：先看分值判断答题量，分点作答。\n")
                }
                MistakeType.MISUNDERSTAND -> {
                    append("审题错误往往是因为匆忙或想当然。\n")
                    append("建议：仔细阅读题目，划出关键信息。\n")
                }
            }
        }
    }

    private fun generateSolution(mistake: MistakeRecord): String {
        return buildString {
            append("【正确解法】\n\n")
            append("正确答案：${mistake.correctAnswer}\n\n")
            append("你的答案：${mistake.userAnswer}\n\n")

            if (mistake.questionType == QuestionType.CALCULATION) {
                append("解题步骤：\n")
                append("1. 仔细审题，明确已知条件\n")
                append("2. 确定解题方法\n")
                append("3. 逐步计算，写清过程\n")
                append("4. 检验答案是否合理\n")
            }

            append("\n提示：理解解题思路比记住答案更重要。")
        }
    }

    private fun generateSimilarQuestions(mistake: MistakeRecord): List<String> {
        // 实际应该由AI生成相似题
        return listOf(
            "推荐练习相关题目巩固理解",
            "可以找同类题型进行专项训练"
        )
    }

    private fun generateSuggestions(mistake: MistakeRecord): List<String> {
        val suggestions = mutableListOf<String>()

        suggestions.add("将此题加入错题本，定期复习")

        if (mistake.nodeId != null) {
            suggestions.add("复习「${mistake.nodeName}」知识点")
        }

        when (mistake.mistakeType) {
            MistakeType.CONCEPT -> suggestions.add("建议重新阅读教材对应章节")
            MistakeType.CALCULATION -> suggestions.add("练习计算准确度，养成检验习惯")
            MistakeType.CARELESS -> suggestions.add("做题时放慢节奏，仔细审题")
            MistakeType.KNOWLEDGE -> suggestions.add("使用间隔复习法巩固知识点")
            else -> {}
        }

        if (mistake.difficulty >= 4) {
            suggestions.add("这道题难度较高，可以和同学讨论")
        }

        return suggestions
    }
}

/**
 * 错题分析结果
 */
data class MistakeAnalysisResult(
    val mistakeId: String,
    val analysis: String,
    val solution: String,
    val similarQuestions: List<String>,
    val suggestions: List<String>,
    val relatedKnowledge: String?
)

// ==================== 错题本UI数据 ====================

/**
 * 错题本视图状态
 */
data class MistakeBookViewState(
    val isLoading: Boolean = false,
    val mistakes: List<MistakeRecord> = emptyList(),
    val selectedCourseId: String? = null,
    val selectedType: MistakeType? = null,
    val showMastered: Boolean = false,
    val sortBy: MistakeSortBy = MistakeSortBy.RECENT,
    val stats: MistakeStats? = null,
    val errorMessage: String? = null
)

/**
 * 排序方式
 */
enum class MistakeSortBy {
    RECENT,        // 最近添加
    REVIEW_COUNT,  // 复习次数
    DIFFICULTY,    // 难度
    MASTERY        // 掌握度
}