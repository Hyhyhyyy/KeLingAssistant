package com.keling.app.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * =========================
 * AI 练习题生成器
 * =========================
 *
 * 功能：
 * - 基于知识点自动生成练习题
 * - 支持多种题型（选择题、判断题、简答题）
 * - 自动批改和生成解析
 * - 追踪练习历史和正确率
 */

// ==================== 数据模型 ====================

/**
 * 题型
 */
enum class QuizType(val displayName: String) {
    SINGLE_CHOICE("单选题"),
    MULTIPLE_CHOICE("多选题"),
    TRUE_FALSE("判断题"),
    SHORT_ANSWER("简答题"),
    FILL_BLANK("填空题")
}

/**
 * 难度等级
 */
enum class Difficulty(val displayName: String, val expMultiplier: Float) {
    EASY("简单", 1.0f),
    MEDIUM("中等", 1.5f),
    HARD("困难", 2.0f),
    EXPERT("挑战", 3.0f)
}

/**
 * 练习题
 */
@Serializable
data class Quiz(
    val id: String = "quiz_${System.currentTimeMillis()}",
    val nodeId: String,
    val nodeName: String,
    val type: QuizType,
    val difficulty: Difficulty,
    val question: String,
    val options: List<String>? = null,      // 选择题选项
    val correctAnswer: String,               // 正确答案
    val explanation: String,                 // 解析
    val hints: List<String> = emptyList(),   // 提示
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 用户答案
 */
@Serializable
data class UserAnswer(
    val quizId: String,
    val answer: String,
    val submittedAt: Long = System.currentTimeMillis(),
    val timeSpentSeconds: Int = 0
)

/**
 * 批改结果
 */
@Serializable
data class GradeResult(
    val quizId: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val score: Int,                    // 得分（满分100）
    val feedback: String,              // 反馈
    val correctExplanation: String,    // 正确解析
    val improvementTip: String? = null // 改进建议
)

/**
 * 练习会话
 */
data class QuizSession(
    val id: String = "quiz_session_${System.currentTimeMillis()}",
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    val quizzes: MutableList<Quiz> = mutableListOf(),
    val answers: MutableList<UserAnswer> = mutableListOf(),
    val results: MutableList<GradeResult> = mutableListOf(),
    var currentNodeIndex: Int = 0,
    var currentQuizIndex: Int = 0
) {
    val totalQuizzes: Int get() = quizzes.size
    val answeredCount: Int get() = results.count { it.isCorrect } + results.count { !it.isCorrect }
    val correctCount: Int get() = results.count { it.isCorrect }
    val accuracy: Float get() = if (answeredCount > 0) correctCount.toFloat() / answeredCount else 0f
    val totalTimeSeconds: Int get() = answers.sumOf { it.timeSpentSeconds }
    val isCompleted: Boolean get() = answeredCount >= totalQuizzes && totalQuizzes > 0
}

/**
 * 练习报告
 */
data class QuizReport(
    val sessionId: String,
    val summary: String,
    val totalQuizzes: Int,
    val correctCount: Int,
    val accuracy: Float,
    val totalTimeMinutes: Int,
    val weakPoints: List<String>,
    val strongPoints: List<String>,
    val recommendations: List<String>,
    val earnedExp: Int,
    val earnedCrystals: Int
)

// ==================== AI 练习题生成器 ====================

/**
 * AI 练习题生成器
 */
class AIQuizGenerator(
    private val aiCoordinator: AICoordinator
) {

    private val _currentSession = MutableStateFlow<QuizSession?>(null)
    val currentSession: StateFlow<QuizSession?> = _currentSession.asStateFlow()

    private val _sessionHistory = MutableStateFlow<List<QuizSession>>(emptyList())
    val sessionHistory: StateFlow<List<QuizSession>> = _sessionHistory.asStateFlow()

    // ==================== 题目生成 ====================

    /**
     * 为知识点生成练习题
     */
    suspend fun generateQuiz(
        nodeId: String,
        nodeName: String,
        type: QuizType = QuizType.SINGLE_CHOICE,
        difficulty: Difficulty = Difficulty.MEDIUM,
        count: Int = 3
    ): List<Quiz> {
        val prompt = buildQuizGenerationPrompt(nodeName, type, difficulty, count)

        val response = aiCoordinator.process(prompt)

        return parseQuizResponse(response.content, nodeId, nodeName, type, difficulty)
    }

    /**
     * 构建题目生成提示
     */
    private fun buildQuizGenerationPrompt(
        nodeName: String,
        type: QuizType,
        difficulty: Difficulty,
        count: Int
    ): String {
        return """
请为知识点「$nodeName」生成 $count 道${type.displayName}，难度为${difficulty.displayName}。

要求：
1. 题目要有实际意义，不能过于简单或无意义
2. 答案必须准确无误
3. 解析要清晰易懂

请按以下JSON格式输出：
```json
{
  "quizzes": [
    {
      "question": "题目内容",
      "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
      "answer": "正确答案",
      "explanation": "详细解析"
    }
  ]
}
```

注意：
- 如果是判断题，options 可省略，answer 为 "正确" 或 "错误"
- 如果是简答题，options 可省略，answer 为参考答案要点
- 请确保输出的是有效的 JSON 格式
        """.trimIndent()
    }

    /**
     * 解析 AI 返回的题目
     */
    private fun parseQuizResponse(
        response: String,
        nodeId: String,
        nodeName: String,
        type: QuizType,
        difficulty: Difficulty
    ): List<Quiz> {
        val quizzes = mutableListOf<Quiz>()

        try {
            // 尝试提取 JSON
            val jsonStr = extractJson(response)

            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(jsonStr).jsonObject
            val quizArray = root["quizzes"]?.jsonArray ?: return emptyList()

            quizArray.forEach { element ->
                val obj = element.jsonObject
                val question = obj["question"]?.jsonPrimitive?.content ?: return@forEach
                val answer = obj["answer"]?.jsonPrimitive?.content ?: return@forEach
                val explanation = obj["explanation"]?.jsonPrimitive?.content ?: ""

                val options = obj["options"]?.jsonArray?.map { it.jsonPrimitive.content }

                quizzes.add(
                    Quiz(
                        id = "quiz_${System.nanoTime()}",
                        nodeId = nodeId,
                        nodeName = nodeName,
                        type = type,
                        difficulty = difficulty,
                        question = question,
                        options = options,
                        correctAnswer = answer,
                        explanation = explanation
                    )
                )
            }
        } catch (e: Exception) {
            // 解析失败，生成默认题目
            return generateDefaultQuizzes(nodeId, nodeName, type, difficulty)
        }

        return quizzes
    }

    /**
     * 从响应中提取 JSON
     */
    private fun extractJson(response: String): String {
        // 尝试找到 JSON 块
        val jsonStart = response.indexOf("{")
        val jsonEnd = response.lastIndexOf("}")

        return if (jsonStart >= 0 && jsonEnd > jsonStart) {
            response.substring(jsonStart, jsonEnd + 1)
        } else {
            "{}"
        }
    }

    /**
     * 生成默认题目（当 AI 生成失败时）
     */
    private fun generateDefaultQuizzes(
        nodeId: String,
        nodeName: String,
        type: QuizType,
        difficulty: Difficulty
    ): List<Quiz> {
        return listOf(
            Quiz(
                nodeId = nodeId,
                nodeName = nodeName,
                type = type,
                difficulty = difficulty,
                question = "关于「$nodeName」，以下说法正确的是？",
                options = listOf(
                    "A. 它是一个重要的概念",
                    "B. 它无关紧要",
                    "C. 它只适用于特殊情况",
                    "D. 以上都不对"
                ),
                correctAnswer = "A",
                explanation = "「$nodeName」是该领域的重要概念，需要深入理解和掌握。"
            )
        )
    }

    // ==================== 批改功能 ====================

    /**
     * 批改用户答案
     */
    suspend fun gradeAnswer(quiz: Quiz, userAnswer: UserAnswer): GradeResult {
        val isCorrect = checkAnswer(quiz, userAnswer.answer)
        val score = calculateScore(quiz, userAnswer, isCorrect)
        val feedback = generateFeedback(quiz, userAnswer.answer, isCorrect)
        val tip = if (!isCorrect) generateImprovementTip(quiz) else null

        return GradeResult(
            quizId = quiz.id,
            userAnswer = userAnswer.answer,
            correctAnswer = quiz.correctAnswer,
            isCorrect = isCorrect,
            score = score,
            feedback = feedback,
            correctExplanation = quiz.explanation,
            improvementTip = tip
        )
    }

    /**
     * 检查答案是否正确
     */
    private fun checkAnswer(quiz: Quiz, answer: String): Boolean {
        return when (quiz.type) {
            QuizType.SINGLE_CHOICE -> {
                // 支持多种格式：A、A. 选项A、选项A的内容
                val normalizedAnswer = normalizeChoiceAnswer(answer)
                val normalizedCorrect = normalizeChoiceAnswer(quiz.correctAnswer)
                normalizedAnswer == normalizedCorrect
            }
            QuizType.MULTIPLE_CHOICE -> {
                // 多选题：排序后比较
                val userChoices = answer.uppercase().replace(" ", "").toList().sorted()
                val correctChoices = quiz.correctAnswer.uppercase().replace(" ", "").toList().sorted()
                userChoices == correctChoices
            }
            QuizType.TRUE_FALSE -> {
                val normalized = answer.trim().lowercase()
                val correct = quiz.correctAnswer.trim().lowercase()
                normalized == correct ||
                (normalized in listOf("对", "正确", "yes", "true", "√") && correct in listOf("对", "正确", "yes", "true", "√")) ||
                (normalized in listOf("错", "错误", "no", "false", "×") && correct in listOf("错", "错误", "no", "false", "×"))
            }
            QuizType.SHORT_ANSWER, QuizType.FILL_BLANK -> {
                // 简答题和填空题：关键词匹配
                val keywords = quiz.correctAnswer.split("，", "、", "；", ";", ",").map { it.trim() }
                keywords.any { keyword -> answer.contains(keyword, ignoreCase = true) }
            }
        }
    }

    /**
     * 标准化选择题答案
     */
    private fun normalizeChoiceAnswer(answer: String): String {
        // 提取字母选项
        val match = Regex("^[A-Fa-f]").find(answer.trim())
        return match?.value?.uppercase() ?: answer.trim().uppercase().first().toString()
    }

    /**
     * 计算得分
     */
    private fun calculateScore(quiz: Quiz, userAnswer: UserAnswer, isCorrect: Boolean): Int {
        if (!isCorrect) return 0

        val baseScore = 100
        val difficultyBonus = quiz.difficulty.expMultiplier
        val timeBonus = calculateTimeBonus(userAnswer.timeSpentSeconds)

        return (baseScore * difficultyBonus * timeBonus).toInt().coerceIn(0, 100)
    }

    /**
     * 计算时间奖励
     */
    private fun calculateTimeBonus(seconds: Int): Float {
        return when {
            seconds < 30 -> 1.2f
            seconds < 60 -> 1.1f
            seconds < 120 -> 1.0f
            else -> 0.9f
        }
    }

    /**
     * 生成反馈
     */
    private fun generateFeedback(quiz: Quiz, answer: String, isCorrect: Boolean): String {
        return if (isCorrect) {
            val praises = listOf(
                "回答正确！很棒！",
                "没错！继续保持！",
                "正确！你已经掌握了这个知识点。",
                "太棒了！回答正确！"
            )
            praises.random()
        } else {
            val encouragements = listOf(
                "这个答案不太对，再想想？",
                "接近了，但还有一点差距。",
                "不太对哦，看看解析吧。",
                "没关系，错题是学习的好机会！"
            )
            encouragements.random()
        }
    }

    /**
     * 生成改进建议
     */
    private fun generateImprovementTip(quiz: Quiz): String {
        return "建议复习「${quiz.nodeName}」相关内容，加深理解。"
    }

    // ==================== 练习会话管理 ====================

    /**
     * 开始练习会话
     */
    suspend fun startSession(
        nodeIds: List<Pair<String, String>>,  // (nodeId, nodeName)
        quizType: QuizType = QuizType.SINGLE_CHOICE,
        difficulty: Difficulty = Difficulty.MEDIUM,
        quizzesPerNode: Int = 3
    ): QuizSession {
        val session = QuizSession()

        // 为每个知识点生成题目
        for ((nodeId, nodeName) in nodeIds) {
            val quizzes = generateQuiz(nodeId, nodeName, quizType, difficulty, quizzesPerNode)
            session.quizzes.addAll(quizzes)
        }

        _currentSession.value = session
        return session
    }

    /**
     * 提交答案
     */
    suspend fun submitAnswer(quizId: String, answer: String, timeSpentSeconds: Int): GradeResult {
        val session = _currentSession.value ?: throw IllegalStateException("No active session")

        val quiz = session.quizzes.find { it.id == quizId }
            ?: throw IllegalArgumentException("Quiz not found: $quizId")

        val userAnswer = UserAnswer(quizId, answer, System.currentTimeMillis(), timeSpentSeconds)
        session.answers.add(userAnswer)

        val result = gradeAnswer(quiz, userAnswer)
        session.results.add(result)

        session.currentQuizIndex++

        return result
    }

    /**
     * 获取当前题目
     */
    fun getCurrentQuiz(): Quiz? {
        val session = _currentSession.value ?: return null
        return session.quizzes.getOrNull(session.currentQuizIndex)
    }

    /**
     * 跳过当前题目
     */
    fun skipCurrentQuiz() {
        val session = _currentSession.value ?: return
        session.currentQuizIndex++
    }

    /**
     * 使用提示
     */
    fun useHint(): String? {
        val quiz = getCurrentQuiz() ?: return null
        return quiz.hints.firstOrNull() ?: "仔细思考题目中的关键词。"
    }

    /**
     * 结束练习会话
     */
    fun endSession(): QuizReport? {
        val session = _currentSession.value ?: return null
        session.endTime = System.currentTimeMillis()

        // 生成报告
        val report = generateReport(session)

        // 保存到历史
        _sessionHistory.value = _sessionHistory.value + session
        _currentSession.value = null

        return report
    }

    /**
     * 生成练习报告
     */
    private fun generateReport(session: QuizSession): QuizReport {
        val accuracy = session.accuracy
        val totalTimeMinutes = session.totalTimeSeconds / 60

        // 分析薄弱点和强项
        val nodeResults = session.results.groupBy { session.quizzes.find { q -> q.id == it.quizId }?.nodeName ?: "" }
            .mapValues { (_, results) -> results.count { it.isCorrect }.toFloat() / results.size }

        val weakPoints = nodeResults.filter { it.value < 0.6f }.keys.toList()
        val strongPoints = nodeResults.filter { it.value >= 0.8f }.keys.toList()

        // 生成建议
        val recommendations = mutableListOf<String>()
        if (weakPoints.isNotEmpty()) {
            recommendations.add("建议重点复习「${weakPoints.first()}」")
        }
        if (accuracy < 0.6f) {
            recommendations.add("整体正确率较低，建议回顾基础概念")
        } else if (accuracy >= 0.9f) {
            recommendations.add("表现优秀！可以尝试更高难度的题目")
        }

        // 计算奖励
        val baseExp = 50
        val baseCrystals = 20
        val earnedExp = (baseExp * accuracy * session.totalQuizzes).toInt()
        val earnedCrystals = (baseCrystals * accuracy * session.totalQuizzes).toInt()

        val summary = buildString {
            append("本次练习共 ${session.totalQuizzes} 题，")
            append("答对 ${session.correctCount} 题，")
            append("正确率 ${(accuracy * 100).toInt()}%。")
            if (accuracy >= 0.8f) {
                append("表现很棒！")
            } else if (accuracy >= 0.6f) {
                append("继续努力！")
            } else {
                append("加油，多多练习！")
            }
        }

        return QuizReport(
            sessionId = session.id,
            summary = summary,
            totalQuizzes = session.totalQuizzes,
            correctCount = session.correctCount,
            accuracy = accuracy,
            totalTimeMinutes = totalTimeMinutes,
            weakPoints = weakPoints,
            strongPoints = strongPoints,
            recommendations = recommendations,
            earnedExp = earnedExp,
            earnedCrystals = earnedCrystals
        )
    }

    // ==================== 快速练习 ====================

    /**
     * 快速生成一道练习题
     */
    suspend fun quickQuiz(nodeId: String, nodeName: String): Quiz {
        val quizzes = generateQuiz(nodeId, nodeName, QuizType.SINGLE_CHOICE, Difficulty.MEDIUM, 1)
        return quizzes.firstOrNull() ?: generateDefaultQuizzes(nodeId, nodeName, QuizType.SINGLE_CHOICE, Difficulty.MEDIUM).first()
    }

    /**
     * 每日一练
     */
    suspend fun dailyQuiz(nodeIds: List<Pair<String, String>>): QuizSession {
        return startSession(
            nodeIds = nodeIds.take(3),  // 最多3个知识点
            quizType = QuizType.SINGLE_CHOICE,
            difficulty = Difficulty.MEDIUM,
            quizzesPerNode = 1
        )
    }
}