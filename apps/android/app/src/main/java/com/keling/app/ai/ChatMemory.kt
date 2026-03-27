package com.keling.app.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * =========================
 * 对话上下文管理模块
 * =========================
 *
 * 功能：
 * - 管理多轮对话历史
 * - 支持滑动窗口保留最近 N 轮对话
 * - 自动构建上下文字符串
 * - 支持对话摘要（当历史过长时）
 */

/**
 * 单轮对话记录
 *
 * @property role 角色: "user" / "assistant" / "system"
 * @property content 消息内容
 * @property timestamp 时间戳
 * @property toolUsed 使用的工具（如果有）
 * @property toolResult 工具执行结果
 */
data class ChatTurn(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolUsed: ToolAction? = null,
    val toolResult: String? = null,
    val scenario: AIScenario? = null
) {
    /**
     * 格式化为对话历史字符串
     */
    fun toContextString(): String {
        val roleLabel = when (role) {
            "user" -> "用户"
            "assistant" -> "助手"
            "system" -> "系统"
            else -> role
        }
        return "【$roleLabel】$content"
    }

    /**
     * 获取简短预览（用于UI显示）
     */
    fun getPreview(maxLength: Int = 50): String {
        return if (content.length <= maxLength) {
            content
        } else {
            content.take(maxLength) + "..."
        }
    }
}

/**
 * AI 对话场景模式
 */
enum class AIScenario(val displayName: String, val description: String) {
    CASUAL_CHAT("自由对话", "日常问答，灵活应对"),
    QUICK_PLAN("快速计划", "制定今日学习计划"),
    WEAKNESS_DIAGNOSE("薄弱诊断", "分析知识薄弱点"),
    EXAM_PREP("考试冲刺", "考前重点突破"),
    CONCEPT_EXPLAIN("概念讲解", "深入理解知识点"),
    PRACTICE_SESSION("刷题练习", "巩固知识，检验理解"),
    REVIEW_SESSION("复习回顾", "基于遗忘曲线智能复习")
}

/**
 * 对话会话（一次完整的对话过程）
 */
data class ChatSession(
    val id: String = "session_${System.currentTimeMillis()}",
    val startTime: Long = System.currentTimeMillis(),
    var scenario: AIScenario = AIScenario.CASUAL_CHAT,
    var turns: MutableList<ChatTurn> = mutableListOf(),
    var summary: String? = null,
    var isActive: Boolean = true
) {
    val turnCount: Int get() = turns.size
    val lastTurn: ChatTurn? get() = turns.lastOrNull()

    /**
     * 获取当前会话时长（秒）
     */
    fun getDurationSeconds(): Long {
        return (System.currentTimeMillis() - startTime) / 1000
    }
}

/**
 * 对话上下文管理器
 *
 * 负责管理对话历史、构建上下文、生成摘要
 */
class ChatMemoryManager(
    private val maxTurns: Int = 10,           // 保留最近N轮对话
    private val maxContextLength: Int = 3000  // 最大上下文字符长度
) {

    private val _currentSession = MutableStateFlow(ChatSession())
    val currentSession: StateFlow<ChatSession> = _currentSession.asStateFlow()

    private val _allSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val allSessions: StateFlow<List<ChatSession>> = _allSessions.asStateFlow()

    // 日期格式化器
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    /**
     * 添加用户消息
     */
    fun addUserMessage(content: String, scenario: AIScenario? = null) {
        val turn = ChatTurn(
            role = "user",
            content = content,
            scenario = scenario
        )
        addTurn(turn)
    }

    /**
     * 添加 AI 助手回复
     */
    fun addAssistantMessage(
        content: String,
        toolUsed: ToolAction? = null,
        toolResult: String? = null
    ) {
        val turn = ChatTurn(
            role = "assistant",
            content = content,
            toolUsed = toolUsed,
            toolResult = toolResult
        )
        addTurn(turn)
    }

    /**
     * 添加系统消息
     */
    fun addSystemMessage(content: String) {
        val turn = ChatTurn(role = "system", content = content)
        addTurn(turn)
    }

    /**
     * 添加一轮对话
     */
    private fun addTurn(turn: ChatTurn) {
        val session = _currentSession.value

        // 检查是否需要滑动窗口
        if (session.turns.size >= maxTurns * 2) {  // *2 因为包含用户和助手两边
            // 保留最近的对，移除最老的
            val keepCount = maxTurns * 2 - 2
            session.turns = session.turns.drop(2).toMutableList()
        }

        session.turns.add(turn)
        _currentSession.value = session
    }

    /**
     * 设置当前对话场景
     */
    fun setScenario(scenario: AIScenario) {
        val session = _currentSession.value
        session.scenario = scenario
        _currentSession.value = session
    }

    /**
     * 构建用于 AI API 的消息列表
     */
    fun buildMessages(): List<ChatMessage> {
        val session = _currentSession.value
        return session.turns.map { turn ->
            ChatMessage(
                role = turn.role,
                content = turn.content
            )
        }
    }

    /**
     * 构建上下文字符串（用于 System Prompt 注入）
     */
    fun buildContextString(): String {
        val session = _currentSession.value
        if (session.turns.isEmpty()) return ""

        val sb = StringBuilder()
        sb.appendLine("【对话历史】")

        // 只取最近的对话
        val recentTurns = session.turns.takeLast(maxTurns)

        for (turn in recentTurns) {
            val timeStr = timeFormat.format(Date(turn.timestamp))
            val roleLabel = when (turn.role) {
                "user" -> "用户"
                "assistant" -> "助手"
                else -> turn.role
            }

            // 截断过长的内容
            val content = if (turn.content.length > 200) {
                turn.content.take(200) + "..."
            } else {
                turn.content
            }

            sb.appendLine("[$timeStr] $roleLabel: $content")

            // 如果有工具调用，也记录
            if (turn.toolUsed != null && turn.toolUsed != ToolAction.NO_ACTION) {
                sb.appendLine("  → 执行操作: ${turn.toolUsed.name}")
            }
        }

        return sb.toString().trimEnd()
    }

    /**
     * 获取最近 N 轮对话的摘要
     */
    fun getRecentSummary(turnCount: Int = 5): String {
        val session = _currentSession.value
        val recent = session.turns.takeLast(turnCount)

        if (recent.isEmpty()) return "暂无对话历史"

        return recent.joinToString("\n") { turn ->
            val role = if (turn.role == "user") "用户" else "助手"
            val preview = turn.getPreview(30)
            "$role: $preview"
        }
    }

    /**
     * 检查上下文是否过长，需要摘要
     */
    fun needsSummarization(): Boolean {
        val context = buildContextString()
        return context.length > maxContextLength
    }

    /**
     * 生成对话摘要（当历史过长时调用）
     * 这个摘要会替换旧的历史，保留关键信息
     */
    fun summarizeOldHistory(): String {
        val session = _currentSession.value
        if (session.turns.size <= 4) return ""

        // 取前半部分生成摘要
        val oldTurns = session.turns.dropLast(4)
        val recentTurns = session.turns.takeLast(4)

        val summary = buildString {
            appendLine("【历史摘要】")

            // 统计关键信息
            val userQuestions = oldTurns.filter { it.role == "user" }.map { it.getPreview(20) }
            val toolsUsed = oldTurns.mapNotNull { it.toolUsed }.filter { it != ToolAction.NO_ACTION }.distinct()

            if (userQuestions.isNotEmpty()) {
                append("用户曾询问: ")
                append(userQuestions.take(3).joinToString(", "))
                appendLine()
            }

            if (toolsUsed.isNotEmpty()) {
                append("执行过的操作: ")
                append(toolsUsed.joinToString(", ") { it.name })
                appendLine()
            }

            appendLine("【最近对话】")
            recentTurns.forEach { turn ->
                appendLine(turn.toContextString())
            }
        }

        // 更新会话
        session.turns = recentTurns.toMutableList()
        session.summary = summary
        _currentSession.value = session

        return summary
    }

    /**
     * 开始新会话
     */
    fun startNewSession(scenario: AIScenario = AIScenario.CASUAL_CHAT) {
        // 保存当前会话
        val current = _currentSession.value
        if (current.turns.isNotEmpty()) {
            current.isActive = false
            _allSessions.value = _allSessions.value + current
        }

        // 创建新会话
        _currentSession.value = ChatSession(scenario = scenario)
    }

    /**
     * 清空当前会话
     */
    fun clearCurrentSession() {
        _currentSession.value = ChatSession()
    }

    /**
     * 获取当前场景
     */
    fun getCurrentScenario(): AIScenario {
        return _currentSession.value.scenario
    }

    /**
     * 获取对话轮数
     */
    fun getTurnCount(): Int {
        return _currentSession.value.turnCount
    }

    /**
     * 检查是否有历史对话
     */
    fun hasHistory(): Boolean {
        return _currentSession.value.turns.isNotEmpty()
    }

    /**
     * 获取最后一条用户消息
     */
    fun getLastUserMessage(): String? {
        return _currentSession.value.turns.lastOrNull { it.role == "user" }?.content
    }

    /**
     * 获取最后一条助手消息
     */
    fun getLastAssistantMessage(): ChatTurn? {
        return _currentSession.value.turns.lastOrNull { it.role == "assistant" }
    }

    /**
     * 撤销最后一轮对话
     */
    fun undoLastTurn() {
        val session = _currentSession.value
        if (session.turns.size >= 2) {
            // 移除最后两条（用户+助手）
            session.turns = session.turns.dropLast(2).toMutableList()
            _currentSession.value = session
        }
    }

    /**
     * 导出当前会话为文本
     */
    fun exportSession(): String {
        val session = _currentSession.value
        val sb = StringBuilder()

        sb.appendLine("=== 对话记录 ===")
        sb.appendLine("时间: ${dateFormat.format(Date(session.startTime))}")
        sb.appendLine("场景: ${session.scenario.displayName}")
        sb.appendLine("轮数: ${session.turnCount}")
        sb.appendLine()

        session.turns.forEach { turn ->
            val timeStr = timeFormat.format(Date(turn.timestamp))
            val roleLabel = when (turn.role) {
                "user" -> "👤 用户"
                "assistant" -> "🤖 助手"
                else -> "📋 系统"
            }
            sb.appendLine("[$timeStr] $roleLabel")
            sb.appendLine(turn.content)
            if (turn.toolUsed != null && turn.toolUsed != ToolAction.NO_ACTION) {
                sb.appendLine("→ 执行: ${turn.toolUsed.name}")
            }
            sb.appendLine()
        }

        return sb.toString()
    }
}