package com.keling.app.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * =========================
 * 多轮对话管理器
 * =========================
 *
 * 功能：
 * - 识别当前对话模式/场景
 * - 管理追问和澄清流程
 * - 支持对话场景切换
 * - 收集用户反馈
 */

/**
 * 对话状态
 */
enum class ConversationState {
    IDLE,               // 空闲，等待用户输入
    COLLECTING_INFO,    // 正在收集信息
    WAITING_CLARIFICATION, // 等待用户澄清
    EXECUTING_TASK,     // 正在执行任务
    WAITING_FEEDBACK,   // 等待用户反馈
    COMPLETED           // 当前对话目标已完成
}

/**
 * 待澄清的问题
 */
data class ClarificationQuestion(
    val question: String,
    val options: List<String>? = null,  // 可选的预设选项
    val contextKey: String              // 用于存储答案的键
)

/**
 * 当前对话的目标
 */
data class ConversationGoal(
    val type: GoalType,
    val description: String,
    val requiredInfo: Map<String, Boolean> = emptyMap(),  // 需要收集的信息
    val collectedInfo: MutableMap<String, String> = mutableMapOf()  // 已收集的信息
) {
    val isComplete: Boolean
        get() = requiredInfo.all { (_, collected) -> collected }

    val missingInfo: List<String>
        get() = requiredInfo.filter { !it.value }.keys.toList()
}

/**
 * 目标类型
 */
enum class GoalType {
    CREATE_TASK,        // 创建任务
    CREATE_PLAN,        // 制定计划
    ANALYZE_WEAKNESS,   // 分析薄弱点
    GENERATE_QUIZ,      // 生成练习题
    EXPLAIN_CONCEPT,    // 解释概念
    REVIEW_KNOWLEDGE,   // 复习知识
    NAVIGATE,           // 页面导航
    GENERAL_CHAT        // 普通对话
}

/**
 * 对话管理器
 */
class ConversationManager(
    private val memoryManager: ChatMemoryManager = ChatMemoryManager()
) {

    private val _state = MutableStateFlow(ConversationState.IDLE)
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val _currentGoal = MutableStateFlow<ConversationGoal?>(null)
    val currentGoal: StateFlow<ConversationGoal?> = _currentGoal.asStateFlow()

    private val _pendingClarification = MutableStateFlow<ClarificationQuestion?>(null)
    val pendingClarification: StateFlow<ClarificationQuestion?> = _pendingClarification.asStateFlow()

    // 场景特定的提示模板
    private val scenarioPrompts = mapOf(
        AIScenario.QUICK_PLAN to """
            你正在帮助用户制定今日学习计划。
            - 结合用户的课表空隙和待办任务
            - 给出具体的时间安排建议
            - 询问用户是否需要调整
        """.trimIndent(),

        AIScenario.WEAKNESS_DIAGNOSE to """
            你正在诊断用户的知识薄弱点。
            - 分析各门课程的掌握度
            - 找出需要重点关注的领域
            - 给出具体的改进建议
        """.trimIndent(),

        AIScenario.EXAM_PREP to """
            你是一位考前冲刺教练。
            - 优先聚焦高频考点
            - 每次只讲解一个关键概念
            - 讲完立即出2道练习题检验
            - 根据答题结果决定是否进入下一个知识点
            - 保持高效，不浪费时间
        """.trimIndent(),

        AIScenario.CONCEPT_EXPLAIN to """
            你正在讲解一个概念。
            - 先用一句话概括核心
            - 再用类比让概念更容易理解
            - 最后给出一个简单例子
            - 询问用户是否理解或有疑问
        """.trimIndent(),

        AIScenario.PRACTICE_SESSION to """
            你正在进行练习环节。
            - 先确认要练习的知识点
            - 每次出一道题，等待用户回答
            - 根据回答给出反馈
            - 追踪正确率，决定是否继续
        """.trimIndent(),

        AIScenario.REVIEW_SESSION to """
            你正在帮助用户复习。
            - 基于遗忘曲线选择需要复习的内容
            - 用提问的方式引导回忆
            - 对遗忘的部分进行强化讲解
            - 建议下次复习时间
        """.trimIndent()
    )

    /**
     * 处理用户输入，返回需要执行的动作
     */
    fun processInput(userInput: String): ProcessResult {
        val normalizedInput = userInput.trim()

        // 1. 如果有待澄清的问题，处理答案
        val clarification = _pendingClarification.value
        if (clarification != null && _state.value == ConversationState.WAITING_CLARIFICATION) {
            return handleClarificationResponse(normalizedInput)
        }

        // 2. 检测场景切换
        val detectedScenario = detectScenario(normalizedInput)
        if (detectedScenario != null && detectedScenario != memoryManager.getCurrentScenario()) {
            memoryManager.setScenario(detectedScenario)
            return ProcessResult.ScenarioChanged(detectedScenario)
        }

        // 3. 检测用户目标
        val goal = detectGoal(normalizedInput)
        if (goal != null) {
            _currentGoal.value = goal
            _state.value = ConversationState.COLLECTING_INFO

            // 检查是否需要收集更多信息
            if (goal.missingInfo.isNotEmpty()) {
                val question = generateClarificationQuestion(goal.missingInfo.first())
                _pendingClarification.value = question
                _state.value = ConversationState.WAITING_CLARIFICATION
                return ProcessResult.NeedClarification(question)
            }

            // 信息足够，可以执行
            _state.value = ConversationState.EXECUTING_TASK
            return ProcessResult.ReadyToExecute(goal)
        }

        // 4. 检测快捷指令
        val quickAction = detectQuickAction(normalizedInput)
        if (quickAction != null) {
            return ProcessResult.QuickActionDetected(quickAction)
        }

        // 5. 普通对话，交给 AI 处理
        return ProcessResult.NormalChat(normalizedInput)
    }

    /**
     * 处理澄清问题的回答
     */
    private fun handleClarificationResponse(response: String): ProcessResult {
        val clarification = _pendingClarification.value ?: return ProcessResult.NormalChat(response)
        val goal = _currentGoal.value

        if (goal != null) {
            // 存储答案
            goal.collectedInfo[clarification.contextKey] = response

            // 检查是否还有缺失信息
            if (goal.missingInfo.isNotEmpty()) {
                val nextQuestion = generateClarificationQuestion(goal.missingInfo.first())
                _pendingClarification.value = nextQuestion
                return ProcessResult.NeedClarification(nextQuestion)
            }

            // 所有信息收集完毕
            _pendingClarification.value = null
            _state.value = ConversationState.EXECUTING_TASK
            return ProcessResult.ReadyToExecute(goal)
        }

        // 没有目标，当作普通回答处理
        _pendingClarification.value = null
        _state.value = ConversationState.IDLE
        return ProcessResult.NormalChat(response)
    }

    /**
     * 检测对话场景
     */
    private fun detectScenario(input: String): AIScenario? {
        return when {
            // 快速计划
            input.contains("计划") || input.contains("安排") || input == "今日计划"
                -> AIScenario.QUICK_PLAN

            // 薄弱诊断
            input.contains("薄弱") || input.contains("诊断") || input.contains("分析一下")
                -> AIScenario.WEAKNESS_DIAGNOSE

            // 考试冲刺
            input.contains("考试") || input.contains("冲刺") || input.contains("突击")
                -> AIScenario.EXAM_PREP

            // 概念讲解
            input.contains("讲解") || input.contains("解释") || input.contains("什么是")
                -> AIScenario.CONCEPT_EXPLAIN

            // 刷题练习
            input.contains("练习") || input.contains("做题") || input.contains("刷题")
                -> AIScenario.PRACTICE_SESSION

            // 复习
            input.contains("复习") || input.contains("回顾") || input.contains("记忆")
                -> AIScenario.REVIEW_SESSION

            else -> null
        }
    }

    /**
     * 检测用户目标
     */
    private fun detectGoal(input: String): ConversationGoal? {
        return when {
            // 创建任务
            input.contains("创建") && input.contains("任务") ||
            input.contains("添加") && input.contains("任务") ||
            input.startsWith("帮我") && input.contains("任务") -> {
                ConversationGoal(
                    type = GoalType.CREATE_TASK,
                    description = "创建学习任务",
                    requiredInfo = mapOf(
                        "title" to input.contains(Regex("任务|复习|学习|练习")),
                        "duration" to input.contains(Regex("\\d+分钟|小时"))
                    )
                )
            }

            // 制定计划
            input.contains("制定") && input.contains("计划") ||
            input == "帮我安排今天" -> {
                ConversationGoal(
                    type = GoalType.CREATE_PLAN,
                    description = "制定学习计划"
                )
            }

            // 分析薄弱点
            input.contains("分析") && (input.contains("薄弱") || input.contains("弱项")) -> {
                ConversationGoal(
                    type = GoalType.ANALYZE_WEAKNESS,
                    description = "分析知识薄弱点"
                )
            }

            // 生成练习题
            input.contains("出题") || input.contains("练习题") || input.contains("测验") -> {
                ConversationGoal(
                    type = GoalType.GENERATE_QUIZ,
                    description = "生成练习题",
                    requiredInfo = mapOf(
                        "topic" to input.contains(Regex("高数|数学|英语|物理|化学"))
                    )
                )
            }

            // 解释概念
            input.contains("什么是") || input.contains("解释") || input.contains("讲解") -> {
                ConversationGoal(
                    type = GoalType.EXPLAIN_CONCEPT,
                    description = "解释概念",
                    requiredInfo = mapOf(
                        "concept" to true  // 从输入中提取概念名
                    )
                )
            }

            else -> null
        }
    }

    /**
     * 检测快捷动作
     */
    private fun detectQuickAction(input: String): QuickAction? {
        return when {
            input == "查看任务" || input == "任务列表" -> QuickAction.Navigate("tasks")
            input == "查看课表" || input == "课表" -> QuickAction.Navigate("schedule_edit")
            input == "温室" || input == "星球" -> QuickAction.Navigate("greenhouse")
            input == "首页" || input == "主页" -> QuickAction.Navigate("home")
            input.contains("开始学习") -> QuickAction.StartLearning
            input.contains("休息一下") -> QuickAction.TakeBreak
            else -> null
        }
    }

    /**
     * 生成澄清问题
     */
    private fun generateClarificationQuestion(missingInfo: String): ClarificationQuestion {
        return when (missingInfo) {
            "title" -> ClarificationQuestion(
                question = "你想创建什么任务呢？比如「复习高数」或「背英语单词」",
                contextKey = "title"
            )
            "duration" -> ClarificationQuestion(
                question = "预计需要多长时间？（比如 30分钟、1小时）",
                options = listOf("15分钟", "30分钟", "45分钟", "1小时"),
                contextKey = "duration"
            )
            "topic" -> ClarificationQuestion(
                question = "想练习哪个科目？",
                options = listOf("高等数学", "大学英语", "大学物理", "其他"),
                contextKey = "topic"
            )
            "concept" -> ClarificationQuestion(
                question = "想了解哪个概念？请告诉我具体的知识点名称",
                contextKey = "concept"
            )
            else -> ClarificationQuestion(
                question = "请提供更多信息：$missingInfo",
                contextKey = missingInfo
            )
        }
    }

    /**
     * 获取当前场景的增强 Prompt
     */
    fun getScenarioPrompt(): String? {
        val scenario = memoryManager.getCurrentScenario()
        return scenarioPrompts[scenario]
    }

    /**
     * 标记任务完成
     */
    fun markCompleted() {
        _state.value = ConversationState.COMPLETED
        _currentGoal.value = null
        _pendingClarification.value = null
    }

    /**
     * 重置对话状态
     */
    fun reset() {
        _state.value = ConversationState.IDLE
        _currentGoal.value = null
        _pendingClarification.value = null
        memoryManager.startNewSession()
    }

    /**
     * 请求用户反馈
     */
    fun requestFeedback(): ClarificationQuestion {
        _state.value = ConversationState.WAITING_FEEDBACK
        return ClarificationQuestion(
            question = "这个回答对你有帮助吗？",
            options = listOf("很有帮助", "还行", "不太理解", "需要更详细的解释"),
            contextKey = "feedback"
        )
    }

    /**
     * 处理用户反馈
     */
    fun handleFeedback(feedback: String) {
        // 记录反馈，可用于后续优化
        memoryManager.addSystemMessage("用户反馈: $feedback")

        // 根据反馈调整
        when (feedback) {
            "不太理解" -> {
                // 可以在下一轮对话中提供更详细的解释
            }
            "需要更详细的解释" -> {
                // 标记需要详细模式
            }
        }

        _state.value = ConversationState.IDLE
    }

    /**
     * 构建完整的对话上下文（包含目标信息）
     */
    fun buildFullContext(): String {
        val sb = StringBuilder()

        // 当前场景
        val scenario = memoryManager.getCurrentScenario()
        sb.appendLine("【当前模式】${scenario.displayName}")

        // 当前目标
        val goal = _currentGoal.value
        if (goal != null) {
            sb.appendLine("【当前目标】${goal.description}")
            if (goal.collectedInfo.isNotEmpty()) {
                sb.append("【已收集信息】")
                goal.collectedInfo.forEach { (k, v) ->
                    sb.append("$k=$v; ")
                }
                sb.appendLine()
            }
        }

        // 对话历史
        val history = memoryManager.buildContextString()
        if (history.isNotEmpty()) {
            sb.appendLine(history)
        }

        return sb.toString()
    }
}

/**
 * 处理结果密封类
 */
sealed class ProcessResult {
    // 场景切换
    data class ScenarioChanged(val scenario: AIScenario) : ProcessResult()

    // 需要澄清
    data class NeedClarification(val question: ClarificationQuestion) : ProcessResult()

    // 准备执行
    data class ReadyToExecute(val goal: ConversationGoal) : ProcessResult()

    // 快捷动作
    data class QuickActionDetected(val action: QuickAction) : ProcessResult()

    // 普通对话
    data class NormalChat(val input: String) : ProcessResult()
}

/**
 * 快捷动作
 */
sealed class QuickAction {
    data class Navigate(val screen: String) : QuickAction()
    object StartLearning : QuickAction()
    object TakeBreak : QuickAction()
}